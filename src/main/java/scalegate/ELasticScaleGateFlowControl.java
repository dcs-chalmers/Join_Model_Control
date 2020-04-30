/*  Copyright (C) 2019  Hannaneh Najdataei,
 * 			Ioannis Nikolakopoulos,
 * 			Vincenzo Gulisano,
 * 			Marina Papatriantafilou,
 * 			Philippas Tsigas
 * 
 * This program is free software: you can redistribute it and/or modify 
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *  
 *  Contact:
 *  	Hannaneh Najdataei, hannajd@chalmers.se
 *  	Vincenzo Gulisano vincenzo.gulisano@chalmers.se
 *
 */

package scalegate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReferenceArray;

import common.tuple.SGTuple.TupleT;
import common.tuple.Tuple;

public class ELasticScaleGateFlowControl<T extends Tuple> implements ElasticScaleGate<T> {

	final String __name;
	final int _repotBatchSize_;// = 50;
	final long minDelay = 500, maxDelay = 10000;
	final int maxTuplePending;// = 100;
	final int maxCapacity = Runtime.getRuntime().availableProcessors();

	final int maxlevels;
	final ESGNode<T> tail;

	ConcurrentHashMap<Integer, WriterThreadLocalData> writertld;
	ConcurrentHashMap<Integer, ReaderThreadLocalData> readertld;

	private PaddedAtomicLong[][] readerCount;
	private PaddedLong[] writerCount;

	final boolean flowControlEnabled;

	public ELasticScaleGateFlowControl(int maxlevels, int numberOfWriters, int numberOfReaders,
			boolean flowControlEnabled, String sgName) {
		_repotBatchSize_ = ((numberOfReaders / 5) + 1) * 10;
		maxTuplePending = _repotBatchSize_ * 2;
		this.__name = sgName;
		this.maxlevels = maxlevels;

		ESGNode<T> head = new ESGNode<T>(maxlevels, null, null, -1);
		this.tail = new ESGNode<T>(maxlevels, null, null, -1);

		for (int i = 0; i < maxlevels; i++)
			head.setNext(i, tail);

		writertld = new ConcurrentHashMap<>();
		for (int i = 0; i < numberOfWriters; i++) {
			writertld.put(i, new WriterThreadLocalData(head));
		}

		readertld = new ConcurrentHashMap<>();
		for (int i = 0; i < numberOfReaders; i++)
			readertld.put(i, new ReaderThreadLocalData(head));
		readerCount = new PaddedAtomicLong[maxCapacity][maxCapacity];
		for (int i = 0; i < maxCapacity; i++)
			for (int j = 0; j < maxCapacity; j++)
				readerCount[i][j] = new PaddedAtomicLong();
		writerCount = new PaddedLong[maxCapacity];
		for (int i = 0; i < maxCapacity; i++)
			writerCount[i] = new PaddedLong();

		this.flowControlEnabled = flowControlEnabled;
	}

	@Override
	public T getNextReadyTuple(int readerID) {
		ESGNode<T> next = getReaderLocal(readerID).localHead.getNext(0);

		if (next != tail && !next.isLastAdded()) {
			getReaderLocal(readerID).localHead = next;
			T tmp = next.getTuple();
			if (flowControlEnabled)
				readerCount[tmp.getInjectorId()][readerID].getAndIncrement();
			return tmp;
		}

		if (next != tail && next.isLastAdded()) {
			if (next.getTuple().getType() == TupleT.FLUSH) {
				getReaderLocal(readerID).localHead = next;
			}
		}
		return null;
	}

	@Override
	public void addTuple(T tuple, int writerID) {
		this.internalAddTuple(tuple, writerID);

		if (flowControlEnabled)
			flowControl(writerID);
	}

	public void flowControl(int writerID) {
		if ((++writerCount[writerID].val) % _repotBatchSize_ == 0) {
			long minReaderCounter, tmp, err;
			while (true) {
				minReaderCounter = readerCount[writerID][0].get();
				for (int i = 1; i < readertld.size(); i++) {
					tmp = readerCount[writerID][i].get();
					minReaderCounter = (tmp < minReaderCounter) ? tmp : minReaderCounter;
				}

				err = (writerCount[writerID].val - minReaderCounter) - maxTuplePending;
				if (err <= 0)
					break;

				err = (err < minDelay) ? minDelay : ((err > maxDelay) ? maxDelay : err);
				common.util.NativeSleep.sleep((int) err);
			}
		}
	}

	@Override
	public ESGNode<T> getReaderHandle(int readerId) {
		return getReaderLocal(readerId).localHead;
	}

	@Override
	public void announceReaders(ESGNode<T> sgInHandle, int startingNewReaderId, int numOfNewReaders) {
		if (startingNewReaderId < readertld.size() && readertld.get(startingNewReaderId) != null)
			return;

		for (int id = startingNewReaderId; id < startingNewReaderId + numOfNewReaders; id++) {
			readertld.put(id, new ReaderThreadLocalData(sgInHandle));

			if (flowControlEnabled)
				for (int i = 0; i < writertld.size(); i++) {
					readerCount[i][id].set(readerCount[i][0].get());
				}
		}
	}

	@Override
	public void announceWriters(T auxiliaryTuple, int startingNewWriterId, int numOfNewWriters) {
		if (numOfNewWriters < 1) {
			System.err.println("Invalid number of new writers " + numOfNewWriters);
			return;
		}

		ArrayList<ESGNode<T>> updcopy = new ArrayList<ESGNode<T>>(maxlevels);
		for (int i = 0; i < maxlevels; i++) {
			updcopy.add(null);
		}
		WriterThreadLocalData ltld = getWriterLocal(0);
		for (int i = maxlevels - 1; i >= 1; i--) {
			updcopy.set(i, ltld.update.get(i));
		}

		ESGNode<T> handle = ltld.written;
		updcopy.set(0, handle);

		HashMap<Integer, ESGNode<T>> tmpBlock = new HashMap<>(numOfNewWriters);
		for (int id = startingNewWriterId; id < startingNewWriterId + numOfNewWriters; id++) {
			ArrayList<ESGNode<T>> ucopy = new ArrayList<ESGNode<T>>(maxlevels);
			for (int i = 0; i < maxlevels; i++) {
				ucopy.add(updcopy.get(i));
			}
			writertld.put(id, new WriterThreadLocalData(handle, ucopy));

			int levels = 1;
			WriterThreadLocalData ln = getWriterLocal(id);

			while (ln.rand.nextBoolean() && levels < maxlevels)
				levels++;

			@SuppressWarnings("unchecked")
			ESGNode<T> newNode = new ESGNode<T>(levels, (T) auxiliaryTuple.getCopy(), ln, id);
			tmpBlock.put(id, newNode);
			if (id > startingNewWriterId) {
				tmpBlock.get(id - 1).setNext(0, newNode);
			}
		}

		WriterThreadLocalData ln = getWriterLocal(startingNewWriterId);
		AtomicReferenceArray<ESGNode<T>> update = ln.update;
		ESGNode<T> curNode = update.get(maxlevels - 1);

		int startlvl = curNode.next.length();
		for (int i = startlvl - 1; i >= 0; i--) {
			ESGNode<T> tx = curNode.getNext(i);

			while (tx != tail) {
				curNode = tx;
				tx = curNode.getNext(i);
			}
			update.set(i, curNode);
		}

		this.insertAnnouncementBlock(update.get(0), tmpBlock, startingNewWriterId, numOfNewWriters);

		for (int id = startingNewWriterId; id < startingNewWriterId + numOfNewWriters; id++) {
			getWriterLocal(id).written = tmpBlock.get(id);

			if (flowControlEnabled) {
				writerCount[id].val = 0;
				for (int i = 0; i < readertld.size(); i++)
					readerCount[id][i].set(0);
			}
		}

	}

	@Override
	public void removeReaders(int readerId) {
		readertld.remove(readerId);
	}

	@Override
	public void removeWriters(int writerId, T auxTuple) {
		this.addTuple(auxTuple, writerId);
		this.writertld.remove(writerId);
	}

	private WriterThreadLocalData getWriterLocal(int writerID) {
		return writertld.get(writerID);
	}

	private ReaderThreadLocalData getReaderLocal(int readerID) {
		return readertld.get(readerID);
	}

	private void insertAnnouncementBlock(ESGNode<T> fromNode, HashMap<Integer, ESGNode<T>> tmpBlock,
			int startingNewWriterId, int numberOfNewWriters) {
		ESGNode<T> firstNodeOfBlock = tmpBlock.get(startingNewWriterId);
		ESGNode<T> lastNodeOfBlock = tmpBlock.get(startingNewWriterId + numberOfNewWriters - 1);

		while (true) {
			ESGNode<T> next = fromNode.getNext(0);
			if (next == tail) {
				lastNodeOfBlock.setNext(0, next);
				if (fromNode.trySetNext(0, next, firstNodeOfBlock))
					break;
			} else {
				fromNode = next;
			}
		}
	}

	private ESGNode<T> internalAddTuple(T obj, int inputID) {
		int levels = 1;
		WriterThreadLocalData ln = getWriterLocal(inputID);

		while (ln.rand.nextBoolean() && levels < maxlevels)
			levels++;

		ESGNode<T> newNode = new ESGNode<T>(levels, obj, ln, inputID);
		AtomicReferenceArray<ESGNode<T>> update = ln.update;
		ESGNode<T> curNode = update.get(maxlevels - 1);

		for (int i = maxlevels - 1; i >= 0; i--) {
			ESGNode<T> tx = curNode.getNext(i);

			while (tx != tail && tx.getTuple().compareTo(obj) < 0) {
				curNode = tx;
				tx = curNode.getNext(i);
			}
			update.set(i, curNode);
		}

		for (int i = 0; i < levels; i++) {
			this.insertNode(update.get(i), newNode, obj, i);
		}

		ln.written = newNode;
		return newNode;
	}

	private void insertNode(ESGNode<T> fromNode, ESGNode<T> newNode, final T obj, final int level) {
		while (true) {
			ESGNode<T> next = fromNode.getNext(level);
			if (next == tail || next.getTuple().compareTo(obj) > 0) {
				newNode.setNext(level, next);
				if (fromNode.trySetNext(level, next, newNode)) {
					break;
				}
			} else {
				fromNode = next;
			}
		}
	}

	protected class WriterThreadLocalData {
		volatile ESGNode<T> written;

		AtomicReferenceArray<ESGNode<T>> update;
		final Random rand;

		public WriterThreadLocalData(ESGNode<T> localHead) {
			update = new AtomicReferenceArray<ESGNode<T>>(maxlevels);
			written = localHead;
			for (int i = 0; i < maxlevels; i++) {
				update.set(i, localHead);
			}
			rand = new Random();
		}

		public WriterThreadLocalData(ESGNode<T> localHead, ArrayList<ESGNode<T>> copiedUpd) {
			update = new AtomicReferenceArray<ESGNode<T>>(maxlevels);
			for (int i = maxlevels - 1; i >= 0; i--) {
				update.set(i, copiedUpd.get(i));
			}
			written = localHead;
			rand = new Random();
		}
	}

	protected class ReaderThreadLocalData {
		volatile ESGNode<T> localHead;

		public ReaderThreadLocalData(ESGNode<T> lhead) {
			localHead = lhead;
		}
	}

	protected static class PaddedAtomicLong extends AtomicLong {
		private static final long serialVersionUID = 1L;
		public volatile long p1, p2, p3, p4, p5, p6 = 7L;
	}

	protected static class PaddedLong {
		public volatile long val;
		public long p1, p2, p3, p4, p5, p6;
	}
}

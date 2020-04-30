/*  Copyright (C) 2019  Hannaneh Najdataei,
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

package streaming.api;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import common.tuple.ControlTuple;
import common.tuple.OutTuple;
import common.tuple.Tuple;
import common.tuple.SGTuple.TupleT;
import common.util.BackOff;
import controller.ElasticityControl;
import net.openhft.affinity.AffinityLock;
import scalegate.ESGNode;
import scalegate.ElasticScaleGate;
import common.statistics.ModelStats;
import common.statistics.RateStatistics;
import common.statistics.ReconfigStatistics;

public class ProcessingThread<T extends Tuple, S extends Bucket<T, S>> implements Runnable {

	private int threadPoolCapacity;
	private int threadID;
	private int numOfActiveThreads;
	private ElasticScaleGate<T> sgIn;
	private ElasticScaleGate<T> sgOut;
	private BackOff backoff;
	BlockingQueue<ThreadInternalConfig<T, S>> taskQueue;
	ThreadPool<T, S> mothership;
	private StateManager<T, S> stateManager;
	private int latestCtrl;
	private boolean isExtraThread;

	private Trimmer trimmer;

	RateStatistics stats;
	ReconfigStatistics configTime;
	ModelStats modelStat;

	public ProcessingThread(int id, ElasticityControl controller, Trimmer trimmer, int capacity, String directory)
			throws Exception {
		threadPoolCapacity = capacity;
		threadID = id;
		this.trimmer = trimmer;
		numOfActiveThreads = 0;
		taskQueue = new ArrayBlockingQueue<>(10);
		backoff = new BackOff(1, 10);
		latestCtrl = 0;
		configTime = new ReconfigStatistics(directory, "ReconfigTimeWorker_" + id);
		stats = new RateStatistics(directory, "PT_" + id);
		modelStat = new ModelStats(directory, "model_thread" + id);
	}

	@SuppressWarnings("unchecked")
	private void initialize(ThreadInternalConfig<T, S> task) throws InterruptedException {

		this.threadID = task.ThreadID;
		this.numOfActiveThreads = task.numOfActiveThreads;
		this.sgIn = task.sgIn;
		this.sgOut = task.sgOut;
		this.stateManager = task.stateManager;
		this.isExtraThread = task.activatingExtraWorker;

		if (task.activatingExtraWorker) {
			this.latestCtrl = task.latestCtrlID;
			if (task.synchronizer.compareAndSet(false, true)) {
				sgIn.announceReaders(task.sgInHandle, numOfActiveThreads - task.requiredThreads, task.requiredThreads);
				T aux = (T) OutTuple.getDummy(task.sgInHandle.getTuple().getTS(), threadID);
				sgOut.announceWriters(aux, numOfActiveThreads - task.requiredThreads, task.requiredThreads);
			}
			stateManager.ack(threadID);
		}
	}

	@SuppressWarnings("unchecked")
	private boolean handleCTRL(Tuple t) {
		ControlTuple ct = (ControlTuple) t;
		if (ct.eventID <= latestCtrl) {
			return false;
		}

		latestCtrl = ct.eventID;

		if (ct.eventType > 0) {
			ESGNode<T> sgInHandle = sgIn.getReaderHandle(threadID);
			numOfActiveThreads += ct.eventType;
			try {
				mothership.activateThreads(numOfActiveThreads, sgInHandle, latestCtrl);
			} catch (Exception e) {
				System.out.println("Initializing new threads caused an exception!");
				e.printStackTrace();
			}
			stateManager.announceReconfiguration(numOfActiveThreads, ct.mapping);
			stateManager.ack(threadID);

			long reconfigTime = System.nanoTime() - ct.getSystemTS();
			configTime.addDataLines(
					new String[] { Integer.toString(ct.eventID), Long.toString(System.currentTimeMillis()),
							Integer.toString(ct.eventType), Long.toString(reconfigTime / 1000000L) });
			return false;
		} else if (ct.eventType < 0) {
			if (numOfActiveThreads + ct.eventType <= 0) {
				System.err.println("Removing more workers than available!");
				return false;
			}

			int prevThreads = numOfActiveThreads;
			numOfActiveThreads += ct.eventType;
			boolean decommissioned = false;
			if (this.threadID >= numOfActiveThreads) {
				sgIn.removeReaders(this.threadID);
				T auxTuple = (T) OutTuple.getFlush(t.getTS(), threadID);
				sgOut.removeWriters(this.threadID, auxTuple);
				try {
					mothership.deactivateThreads(prevThreads, numOfActiveThreads);
				} catch (Exception e) {
					System.out.println("Removing threads caused an exception!");
					e.printStackTrace();
				}
				decommissioned = true;
			}

			stateManager.announceReconfiguration(numOfActiveThreads, ct.mapping);
			stateManager.ack(this.threadID);

			long reconfigTime = System.nanoTime() - ct.getSystemTS();
			configTime.addDataLines(
					new String[] { Integer.toString(ct.eventID), Long.toString(System.currentTimeMillis()),
							Integer.toString(ct.eventType), Long.toString(reconfigTime / 1000000L) });
			return decommissioned;
		}

		stateManager.announceReconfiguration(numOfActiveThreads, ct.mapping);
		stateManager.ack(threadID);
		return false;
	}

	public void pushTask(ThreadInternalConfig<T, S> threadInternalConfig) {
		this.taskQueue.add(threadInternalConfig);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void run() {
		AffinityLock al;
		if (threadPoolCapacity < (Runtime.getRuntime().availableProcessors() / 2))
			al = AffinityLock.acquireCore(true);
		else
			al = AffinityLock.acquireLock(true);

		try {
			task_loop: while (true) {
				try {
					initialize(taskQueue.take());
				} catch (InterruptedException e1) {
					return;
				}

				T t;

				if (isExtraThread) {
					this.isExtraThread = false;
				} else {
					retrieveDummy();
				}

				boolean isDecommissioned = false;
				thread_loop: while (true) {
					t = sgIn.getNextReadyTuple(threadID);
					while (t == null) {
						try {
							backoff.backoff();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						t = sgIn.getNextReadyTuple(threadID);
						if (t != null)
							backoff.resetLimit();
					}

					TupleT tupletype = t.getType();

					switch (tupletype) {
					case CTRL:
						isDecommissioned = handleCTRL(t);
						if (isDecommissioned)
							break thread_loop;
						continue;
					case FINAL:
						break thread_loop;
					case DUMMY:
					case FLUSH:
					case STD:
					default:
						break;
					}

					List<Integer> indx = trimmer.prune(stateManager.getBuckets(threadID), t);
					if (indx != null) {
						for (int stateCounter : indx) {
							List<T> results = stateManager.next(stateCounter).processTuple(t, threadID);
							long betaStartTime = System.nanoTime();
							for (T res : results) {
								sgOut.addTuple(res, threadID);
							}
							double beta = 0;
							if (results.size() > 0)
								beta = (System.nanoTime() - betaStartTime) / results.size();
							modelStat.add(stateManager.next(stateCounter).getAlpha(), beta,
									stateManager.next(stateCounter).getSigma());
							stats.collect(stateManager.next(stateCounter).getLastStat());
						}
						sgOut.addTuple((T) OutTuple.getFlush(t.getTS(), threadID), threadID);
					}
					stats.throughput();
				}
				stats.close();
				modelStat.close();

				if (!isDecommissioned) {
					T aux = (T) OutTuple.getFinal(Long.MAX_VALUE, threadID);
					sgOut.addTuple(aux, threadID);
					sgOut.addTuple(aux, threadID);
					break task_loop;
				}
			}
			configTime.close();
		} finally {
			al.release();
		}
	}

	@SuppressWarnings("unchecked")
	private void retrieveDummy() {
		T t;
		while (true) {
			t = sgIn.getNextReadyTuple(threadID);
			if (t != null)
				break;
			try {
				backoff.backoff();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		if (!isDummy(t))
			System.err.println("First Dummy not found in worker " + threadID);

		T aux = (T) OutTuple.getDummy(0, threadID);
		sgOut.addTuple(aux, threadID);
	}

	private boolean isDummy(Tuple t) {
		return t.getType() == Tuple.TupleT.DUMMY;
	}

}
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

package streaming.api;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import common.tuple.Tuple;
import controller.ElasticityControl;
import scalegate.ESGNode;
import scalegate.ElasticScaleGate;

public class ThreadPool<T extends Tuple, S extends Bucket<T, S>> {

	final int CAPACITY;

	Thread[] threads;
	ArrayList<ProcessingThread<T, S>> threadMeta;
	AtomicInteger numOfActiveThreads;

	ElasticScaleGate<T> sgIn;
	ElasticScaleGate<T> sgOut;
	StateManager<T, S> stateManager;

	public ThreadPool(ElasticScaleGate<T> sgIn, ElasticScaleGate<T> sgOut, StateManager<T, S> sm, Trimmer trimmer,
			ElasticityControl controller) throws Exception {

		this.stateManager = sm;
		this.CAPACITY = sm.config.getThreadPoolCapacity();

		threads = new Thread[CAPACITY];
		threadMeta = new ArrayList<ProcessingThread<T, S>>(CAPACITY);
		numOfActiveThreads = new AtomicInteger(0);

		this.sgIn = sgIn;
		this.sgOut = sgOut;

		for (int i = 0; i < CAPACITY; i++) {
			threadMeta.add(new ProcessingThread<T, S>(i, controller, trimmer, sm.config.getThreadPoolCapacity(),
					sm.config.getStatsDir()));
			threads[i] = new Thread(threadMeta.get(i));
			threads[i].setName("PT" + i);
		}

		if (!activateThreads(sm.config.getParallelism()))
			throw new Exception("Cannot activate the processing threads!");

	}

	public boolean activateThreads(int number) throws Exception {
		return activateThreads(number, null, 0);
	}

	public boolean activateThreads(int newNumber, ESGNode<T> sgInHandle, int latestCtrlID) {
		int currentNum = numOfActiveThreads.get();
		if (currentNum == newNumber)
			return false;

		boolean result = numOfActiveThreads.compareAndSet(currentNum, newNumber);

		if (result) {
			int requiredThreads = newNumber - currentNum;
			boolean activatingExtraWorkers = currentNum != 0;

			AtomicBoolean synchronizer = new AtomicBoolean(false);
			for (int i = currentNum; i < newNumber; i++) {
				threadMeta.get(i).pushTask(new ThreadInternalConfig<T, S>(i, newNumber, sgIn, sgOut, stateManager,
						activatingExtraWorkers, requiredThreads, synchronizer, latestCtrlID, sgInHandle));
			}
		}
		return result;
	}

	public void deactivateThreads(int prevThreads, int newThreads) {
		numOfActiveThreads.compareAndSet(prevThreads, newThreads);
	}

	public void startThreads() throws Exception {
		for (ProcessingThread<T, S> t : threadMeta) {
			t.mothership = this;
		}
		for (Thread t : threads) {
			t.start();
		}
	}

	public void joinActiveWorkers() throws InterruptedException {
		int numOfThreads = numOfActiveThreads.get();

		for (int i = 0; i < numOfThreads; i++) {
			threads[i].join();
		}
	}

	public void killInactiveWorkers() throws InterruptedException {
		int numOfThreads = numOfActiveThreads.get();

		for (int i = numOfThreads; i < CAPACITY; i++) {
			threads[i].interrupt();
			threads[i].join();
		}
	}

	public void joinAllThreads() throws InterruptedException {
		killInactiveWorkers();
		joinActiveWorkers();
	}

}

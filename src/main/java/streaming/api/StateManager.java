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

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.SerializationUtils;

import common.ExecutionConfiguration;
import common.tuple.Tuple;

public class StateManager<T extends Tuple, S extends Bucket<T, S>> {

	private S[] buckets;
	private int numOfBuckets;
	private int numOfThreads;
	private int newNumOfThreads;
	private AtomicInteger barrier;
	private AtomicBoolean reconfigured = new AtomicBoolean(true);
	private AtomicBoolean barrierSet = new AtomicBoolean(false);
	private AtomicBoolean FinishedReconfig = new AtomicBoolean(false);

	HashMap<Integer, List<Integer>> mapping;
	HashMap<Integer, List<Integer>> tmpNewMapping;

	ExecutionConfiguration config;

	public StateManager(ExecutionConfiguration config, S bucket, Class<S> type) {
		assert (config.getParallelism() >= config
				.getThreadPoolCapacity()) : "The number of processing threads can not exceed the capacity of the thread pool.";
		assert (config.getParallelism() > 0) : "Number of Processing threads cannot be negative nor zero";
		assert (numOfBuckets > 0) : "Number of buckets cannot be negative nor zero";
		assert (bucket != null) : "The function of the operator cannot be null";

		this.config = config;
		this.numOfThreads = config.getParallelism();
		this.newNumOfThreads = config.getParallelism();
		this.numOfBuckets = config.getNumberOfBuckets();

		@SuppressWarnings("unchecked")
		final S[] replicatedBuckets = (S[]) Array.newInstance(type, numOfBuckets);
		buckets = replicatedBuckets;
		for (int i = 0; i < numOfBuckets; i++) {
			buckets[i] = SerializationUtils.clone(bucket);
			buckets[i].bucketID = i;
		}
		this.mapping = config.getMapping();
	}

	public int getNumOfBuckets() {
		return numOfBuckets;
	}

	public List<Integer> getBuckets(int threadID) {
		assert (!mapping.isEmpty()) : "There is no mapping of buckets to threads!";
		assert (!mapping.containsKey(threadID)) : "There is no bucket assigned to thread with id:" + threadID;
		return mapping.get(threadID);
	}

	public S next(int counter) {
		assert (buckets != null) : "states is null, have you invoked setup?";
		assert (counter != -1) : "threadIndex is null, have you invoked setup?";

		return buckets[counter];
	}

	public void announceReconfiguration(int newNumOfThreads, HashMap<Integer, List<Integer>> newMapping) {
		assert (!newMapping.isEmpty()) : "The new mapping of buckets to threads is required!";
		if (reconfigured.compareAndSet(true, false)) {
			barrier = new AtomicInteger(Math.max(numOfThreads, newNumOfThreads));
			this.tmpNewMapping = newMapping;
			this.newNumOfThreads = newNumOfThreads;
			barrierSet.set(true);
		}
	}

	public void ack(int threadID) {

		while (!barrierSet.get())
			;

		int val = -1;
		do {
			val = barrier.get();
			if (val == 1) {
				updateMapping(tmpNewMapping);
				numOfThreads = newNumOfThreads;
				config.setParallelism(numOfThreads);
				reconfigured.compareAndSet(false, true);
				barrierSet.compareAndSet(true, false);
				barrier.set(0);
				FinishedReconfig.set(true);
				return;
			}
		} while (!barrier.compareAndSet(val, val - 1));

		while (barrier.get() != 0)
			;
	}

	public void updateMapping(HashMap<Integer, List<Integer>> mapping) {
		this.mapping = mapping;
	}

	public boolean isReconfigurationDone() {
		return FinishedReconfig.compareAndSet(true, false);
	}
}

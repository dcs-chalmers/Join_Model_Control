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

package common;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import controller.ControlEvent;

public class ExecutionConfiguration {

	private final int threadPoolCapacity, numOfOperatorInputThreads, numOfOperatorOutputThreads, numberOfBuckets,
			maxSGLevels;
	private final boolean flowControlFlag, controllerFlag;
	private final String statsDir;
	private HashMap<Integer, List<Integer>> mapping;
	private AtomicInteger parallelism;

	public ExecutionConfiguration(int threadPoolCapacity, int numOfOperatorInputThreads, int numOfOperatorOutputThreads,
			int numberOfBuckets, int maxSGLevels, boolean flowControlFlag, boolean controllerFlag, String statsDir) {
		this.threadPoolCapacity = threadPoolCapacity;
		this.numOfOperatorInputThreads = numOfOperatorInputThreads;
		this.numOfOperatorOutputThreads = numOfOperatorOutputThreads;
		this.numberOfBuckets = numberOfBuckets;
		this.maxSGLevels = maxSGLevels;
		this.flowControlFlag = flowControlFlag;
		this.controllerFlag = controllerFlag;
		this.statsDir = statsDir;
		
		parallelism = new AtomicInteger();
	}

	public int getNumberOfBuckets() {
		return numberOfBuckets;
	}

	public boolean getFlowControlFlag() {
		return flowControlFlag;
	}

	public boolean getControllerFlag() {
		return controllerFlag;
	}

	public int getMaxSGLevels() {
		return maxSGLevels;
	}

	public HashMap<Integer, List<Integer>> getMapping() {
		return mapping;
	}

	public void setMapping() {
		this.mapping = ControlEvent.RRMapping(parallelism.get(), numberOfBuckets);
	}

	public int getThreadPoolCapacity() {
		return threadPoolCapacity;
	}

	public int getParallelism() {
		return parallelism.get();
	}

	public void setParallelism(int newParallelism) {
		parallelism.set(newParallelism);
	}

	public int getNumOfOperatorInputThreads() {
		return numOfOperatorInputThreads;
	}

	public int getNumOfOperatorOutputThreads() {
		return numOfOperatorOutputThreads;
	}

	public String getStatsDir() {
		return statsDir;
	}

}

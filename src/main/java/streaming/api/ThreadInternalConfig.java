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

import java.util.concurrent.atomic.AtomicBoolean;

import common.tuple.Tuple;
import scalegate.ESGNode;
import scalegate.ElasticScaleGate;

public class ThreadInternalConfig<T extends Tuple, S extends Bucket<T, S>> {

	int ThreadID;
	int numOfActiveThreads;
	ElasticScaleGate<T> sgIn;
	ElasticScaleGate<T> sgOut;
	StateManager<T, S> stateManager;
	boolean activatingExtraWorker;
	int requiredThreads;
	AtomicBoolean synchronizer;
	int latestCtrlID;

	ESGNode<T> sgInHandle;

	public ThreadInternalConfig(int ThreadID, int numOfActiveThreads, ElasticScaleGate<T> sgIn,
			ElasticScaleGate<T> sgOut, StateManager<T, S> stateManager, boolean activatingExtraWorker,
			int requiredThreads, AtomicBoolean synchOthers, int latestCtrlID, ESGNode<T> sgInHandle) {
		this.ThreadID = ThreadID;
		this.numOfActiveThreads = numOfActiveThreads;
		this.sgIn = sgIn;
		this.sgOut = sgOut;
		this.stateManager = stateManager;
		this.activatingExtraWorker = activatingExtraWorker;
		this.requiredThreads = requiredThreads;
		this.synchronizer = synchOthers;
		this.latestCtrlID = latestCtrlID;
		this.sgInHandle = sgInHandle;
	}
}

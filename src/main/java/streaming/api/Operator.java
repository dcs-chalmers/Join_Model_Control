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

import common.ExecutionConfiguration;
import common.tuple.Tuple;
import controller.ElasticityControl;
import scalegate.ELasticScaleGateFlowControl;
import scalegate.ElasticScaleGate;

public abstract class Operator<T extends Tuple, S extends Bucket<T, S>, E extends ElasticityControl>
		implements Runnable {

	S function;
	ExecutionConfiguration config;
	ElasticScaleGate<T> sgIn;
	ElasticScaleGate<T> sgOut;
	StateManager<T, S> stateManager;
	ThreadPool<T, S> threadPool;
	E controller;

	public Operator(Class<S> type, S operatorFunction, ExecutionConfiguration config, Trimmer trimmer, E controller) {
		try {
			this.function = operatorFunction;
			this.config = config;

			sgIn = new ELasticScaleGateFlowControl<>(config.getMaxSGLevels(), config.getNumOfOperatorInputThreads(),
					config.getParallelism(), config.getFlowControlFlag(), "sgIn");
			sgOut = new ELasticScaleGateFlowControl<>(config.getMaxSGLevels(), config.getParallelism(),
					config.getNumOfOperatorOutputThreads(), false, "sgOut");
			stateManager = new StateManager<>(config, operatorFunction, type);

			threadPool = new ThreadPool<>(sgIn, sgOut, stateManager, trimmer, controller);

			this.controller = controller;
			if (!config.getControllerFlag())
				this.controller.setTermination();
		} catch (Exception e) {
			System.out.println("Cannot start the operator!");
			return;
		}
	}

	public ElasticScaleGate<T> getESGin() {
		return sgIn;
	}

	public ElasticScaleGate<T> getESGout() {
		return sgOut;
	}

	protected void startThreads() {
		try {
			threadPool.startThreads();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void stopThreads() {
		try {
			threadPool.joinAllThreads();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}

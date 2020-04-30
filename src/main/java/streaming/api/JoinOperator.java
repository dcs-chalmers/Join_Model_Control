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
import common.util.BackOff;
import controller.ElasticityControl;

public class JoinOperator<T extends Tuple, S extends JoinFunction<T, S>, E extends ElasticityControl>
		extends Operator<T, S, E> {

	public JoinOperator(Class<S> type, S operatorFunction, ExecutionConfiguration config, E controller) {
		this(type, operatorFunction, config, new JoinTrimmer(), controller);
	}

	public JoinOperator(Class<S> type, S operatorFunction, ExecutionConfiguration config, Trimmer trimmer,
			E controller) {
		super(type, operatorFunction, config, trimmer, controller);
	}

	@Override
	public void run() {
		BackOff backoff = new BackOff(1, 10);
		
		long reconfigTime = 0;
		startThreads();
		while (!controller.getTermination()) {
			if (controller.generateFeedback(reconfigTime)) {
				long startTime = System.currentTimeMillis();
				while (!stateManager.isReconfigurationDone()) {
					try {
						backoff.backoff();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					if (controller.getTermination())
						break;
				}
				reconfigTime = System.currentTimeMillis() - startTime;
				backoff.resetLimit();
			}
			
		}
		stopThreads();
	}

}

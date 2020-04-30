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

import common.statistics.RateStatistics;
import common.tuple.Tuple;
import common.tuple.SGTuple.TupleT;
import common.util.BackOff;
import controller.ElasticityControl;
import scalegate.ScaleGate;

public class OutputThread implements Runnable {

	private ScaleGate<Tuple> outputSG;
	private long resultsCounter;
	RateStatistics stats;
	ElasticityControl controller;

	public OutputThread(ScaleGate<Tuple> sg, String directory, ElasticityControl controller) {
		outputSG = sg;
		resultsCounter = 0;
		stats = new RateStatistics(directory, "OT");
		this.controller = controller;
	}

	public void run() {
		BackOff backoff = new BackOff(1, 50);
		while (true) {
			Tuple foo = outputSG.getNextReadyTuple(0);
			if (foo == null) {
				try {
					backoff.backoff();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				continue;
			}

			resultsCounter++;
			if (foo.getType() == TupleT.FINAL)
				break;

			if (foo.getType() == TupleT.STD)
				stats.latency(foo);
		}

		System.out.println("Output finished, results counter: " + resultsCounter);
		stats.close();
	}
}

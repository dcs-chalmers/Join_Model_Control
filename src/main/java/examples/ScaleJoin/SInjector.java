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

package examples.ScaleJoin;

import java.util.concurrent.ThreadLocalRandom;

import common.SourceConfig;
import common.tuple.Tuple;
import controller.ElasticityControl;
import scalegate.ScaleGate;
import streaming.api.InjectorThread;

public class SInjector extends InjectorThread {

	public SInjector(int i, ScaleGate<Tuple> sgIn, SourceConfig config, ElasticityControl controller, long event_dt) {
		super(i, sgIn, config, controller, event_dt);
	}

	@Override
	protected Tuple getNextTuple(long timestamp) {
		STuple t = new STuple(timestamp, System.nanoTime(), id, Tuple.TupleT.STD,
				ThreadLocalRandom.current().nextInt(10000), (float) ThreadLocalRandom.current().nextInt(10000), 0.0,
				false);
		return t;
	}

	@Override
	protected Tuple getDummyTuple(long timestamp) {
		STuple t = new STuple(timestamp, -1, id, Tuple.TupleT.DUMMY, 0, (float) 0, 0.0, false);
		return t;
	}

	@Override
	protected Tuple getFinalTuple(long timestamp) {
		STuple t = new STuple(timestamp, -1, id, Tuple.TupleT.FINAL, 0, (float) 0, 0.0, false);
		return t;
	}

}

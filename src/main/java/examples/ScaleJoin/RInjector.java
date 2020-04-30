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
import common.tuple.SGTuple.TupleT;
import controller.ElasticityControl;
import scalegate.ScaleGate;
import streaming.api.InjectorThread;

public class RInjector extends InjectorThread {

	public RInjector(int i, ScaleGate<Tuple> sgIn, SourceConfig config, ElasticityControl controller, long event_dt) {
		super(i, sgIn, config, controller, event_dt);
	}

	@Override
	protected Tuple getNextTuple(long timestamp) {
		RTuple t = new RTuple(timestamp, System.nanoTime(), id, Tuple.TupleT.STD,
				ThreadLocalRandom.current().nextInt(10000), (float) ThreadLocalRandom.current().nextInt(10000));
		return t;
	}

	@Override
	protected Tuple getDummyTuple(long timestamp) {
		RTuple t = new RTuple(timestamp, -1, id, TupleT.DUMMY, 0, (float) 0);
		return t;
	}

	@Override
	protected Tuple getFinalTuple(long timestamp) {
		RTuple t = new RTuple(timestamp, -1, id, TupleT.FINAL, 0, (float) 0);
		return t;
	}

}

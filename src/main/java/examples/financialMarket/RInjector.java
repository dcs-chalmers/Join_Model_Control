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

package examples.financialMarket;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;

import org.json.JSONObject;

import common.SourceConfig;
import common.tuple.Tuple;
import common.tuple.SGTuple.TupleT;
import controller.ElasticityControl;
import scalegate.ScaleGate;
import streaming.api.InjectorThread;

public class RInjector extends InjectorThread {

	BufferedReader br;
	long counter;

	public RInjector(int i, ScaleGate<Tuple> sgIn, SourceConfig config, ElasticityControl controller, long event_dt) {
		super(i, sgIn, config, controller, event_dt);
		counter = 0;
		try {
			br = new BufferedReader(new FileReader(config.getRFilePath()));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected Tuple getNextTuple(long timestamp) {
		try {
			String line = br.readLine();
			counter++;
			if (counter % 2 == 0) {
				line = br.readLine();
				counter++;
			}

			if (line == null) {
				return getFinalTuple(Long.MAX_VALUE);
			}

			JSONObject obj = new JSONObject(line);
			// long ts = obj.getLong("timestamp");
			// String name = obj.getString("name");
			int tuple_id = obj.getInt("id");
			double price = obj.getDouble("price");
			String exchange = obj.getString("exchange");
			RTuple t = new RTuple(timestamp, System.nanoTime(), id, TupleT.STD, tuple_id, price, exchange);
			return t;
		} catch (Exception e) {
			return getFinalTuple(Long.MAX_VALUE);
		}
	}

	@Override
	protected Tuple getDummyTuple(long timestamp) {
		return RTuple.getDummy(timestamp, id);
	}

	@Override
	protected Tuple getFinalTuple(long timestamp) {
		return RTuple.getFinal(timestamp, id);
	}

}

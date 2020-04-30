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

package examples.ScaleJoin;

import java.util.LinkedList;
import java.util.List;

import common.tuple.SGTuple.TupleT;
import common.tuple.Tuple;
import streaming.api.JoinFunction;

public class ScaleJoinFunction<T extends Tuple, S extends ScaleJoinFunction<T, S>> extends JoinFunction<T, S> {

	private static final long serialVersionUID = 1L;

	RTuple rTuple;
	STuple sTuple;
	long readyTupleCounter;
	long startTS;

	private MicroWinR rTuples;
	private MicroWinS sTuples;

	private long throughputCounter;
	private double alpha, sigma;

	public ScaleJoinFunction(int numberOfStates, long windowSize) {
		super(numberOfStates, windowSize, -1);

		rTuples = new MicroWinR();
		sTuples = new MicroWinS();
		throughputCounter = 0;
		alpha = 0;
	}

	@Override
	public List<T> processTuple(T t, int threadID) {
		readyTupleCounter++;

		throughputCounter = 0;

		startTS = t.getTS() - (windowSize * 1000000L);
		if (t instanceof RTuple) {
			rTuple = (RTuple) t;
			return processRTuple(rTuple.getTS(), rTuple.getSystemTS(), threadID, rTuple.x, rTuple.y);
		} else if (t instanceof STuple) {
			sTuple = (STuple) t;
			return processSTuple(sTuple.getTS(), sTuple.getSystemTS(), threadID, sTuple.a, sTuple.b, sTuple.c,
					sTuple.d);
		} else {
			assert (false) : "look like the tuple you passed is not RTuple nor STuple";
		}

		return null;
	}

	@SuppressWarnings("unchecked")
	private List<T> processRTuple(long ts, long systemTs, int threadID, int x, float y) {
		while (sTuples.size() > 0 && sTuples.getTS(0) < startTS)
			sTuples.discard();

		if (readyTupleCounter % numberOfBuckets == bucketID)
			rTuples.add(ts, x, y);

		List<T> result = new LinkedList<T>();

		double sigmaCounter = 0.0;
		long alphaTime = System.nanoTime();
		for (int i = 0; i < sTuples.size(); i++) {
			throughputCounter++;

			if (x >= sTuples.getA(i) - 10 && x <= sTuples.getA(i) + 10 && y >= sTuples.getB(i) - 10
					&& y <= sTuples.getB(i) + 10) {
				result.add((T) new RSTuple(ts, systemTs, threadID, TupleT.STD, x, y, sTuples.getA(i), sTuples.getB(i),
						sTuples.getC(i), sTuples.getD(i)));
				sigmaCounter++;

			}

		}
		if (throughputCounter > 0) {
			alpha = (System.nanoTime() - alphaTime) / throughputCounter;
			sigma = sigmaCounter / throughputCounter;
		}
		return result;

	}

	@SuppressWarnings("unchecked")
	private List<T> processSTuple(long ts, long systemTs, int threadID, int a, float b, double c, boolean d) {
		while (rTuples.size() > 0 && rTuples.getTS(0) < startTS)
			rTuples.discard();

		if (readyTupleCounter % numberOfBuckets == bucketID)
			sTuples.add(ts, a, b, c, d);

		List<T> result = new LinkedList<T>();

		double sigmaCounter = 0.0;
		long alphaTime = System.nanoTime();
		for (int i = 0; i < rTuples.size(); i++) {
			throughputCounter++;

			if (rTuples.getX(i) >= a - 10 && rTuples.getX(i) <= a + 10 && rTuples.getY(i) >= b - 10
					&& rTuples.getY(i) <= b + 10) {
				result.add((T) new RSTuple(ts, systemTs, threadID, TupleT.STD, rTuples.getX(i), rTuples.getY(i), a, b,
						c, d));
				sigmaCounter++;

			}

		}
		if (throughputCounter > 0) {
			alpha = (System.nanoTime() - alphaTime) / throughputCounter;
			sigma = sigmaCounter / throughputCounter;
		}
		return result;

	}

	@Override
	public long getLastStat() {
		return throughputCounter;
	}

	@Override
	public double getAlpha() {
		return alpha;
	}

	@Override
	public double getSigma() {
		return sigma;
	}

}

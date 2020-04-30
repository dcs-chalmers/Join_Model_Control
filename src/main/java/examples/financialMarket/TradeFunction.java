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

package examples.financialMarket;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import common.tuple.SGTuple.TupleT;
import common.tuple.Tuple;
import streaming.api.JoinFunction;

public class TradeFunction<T extends Tuple, S extends TradeFunction<T, S>> extends JoinFunction<T, S> {

	private static final long serialVersionUID = 1L;

	RTuple rTuple;
	STuple sTuple;
	long readyTupleCounter;
	long startTS;

	private MicroWin rTuples;
	private MicroWin sTuples;

	private long throughputCounter;
	private double alpha, sigma;

	private double[] avgPrice;

	public TradeFunction(int numberOfStates, long windowSize, String avgPriceFile) {
		super(numberOfStates, windowSize, -1);

		rTuples = new MicroWin();
		sTuples = new MicroWin();
		throughputCounter = 0;
		alpha = 0;

		fillAvgPrices(avgPriceFile);
	}

	private void fillAvgPrices(String avgPriceFile) {
		try {
			avgPrice = new double[10];

			HashMap<String, Integer> companyIds = new HashMap<>();
			List<String> compList = Arrays
					.asList(new String[] { "GOOG", "FB", "AMZN", "MSFT", "AAPL", "WMT", "BABA", "V", "VOD", "MA" });
			for (int i = 0; i < compList.size(); i++)
				companyIds.put(compList.get(i), i);

			BufferedReader br = new BufferedReader(new FileReader(new File(avgPriceFile)));
			String line;
			while ((line = br.readLine()) != null) {
				String[] str = line.split(",");
				if (companyIds.containsKey(str[0])) {
					avgPrice[companyIds.get(str[0])] = Double.parseDouble(str[1]);
				}
			}
			br.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public List<T> processTuple(T t, int threadID) {
		readyTupleCounter++;

		throughputCounter = 0;

		if (t.getType() != TupleT.STD)
			return new LinkedList<T>();

		startTS = t.getTS() - (windowSize * 1000000L);
		if (t instanceof RTuple) {
			rTuple = (RTuple) t;
			return processRTuple(rTuple.getTS(), rTuple.getSystemTS(), threadID, rTuple.id, rTuple.price,
					rTuple.exchange);
		} else if (t instanceof STuple) {
			sTuple = (STuple) t;
			return processSTuple(sTuple.getTS(), sTuple.getSystemTS(), threadID, sTuple.id, sTuple.price,
					sTuple.exchange);
		} else {
			assert (false) : "look like the tuple you passed is not RTuple nor STuple";
		}

		return null;
	}

	@SuppressWarnings("unchecked")
	private List<T> processRTuple(long ts, long systemTs, int threadID, int id, double price, String exchange) {
		while (sTuples.size() > 0 && sTuples.getTS(0) < startTS)
			sTuples.discard();

		if (readyTupleCounter % numberOfBuckets == bucketID)
			rTuples.add(ts, id, price, exchange);

		List<T> result = new LinkedList<T>();

		double sigmaCounter = 0.0;
		long alphaTime = System.nanoTime();

		for (int i = 0; i < sTuples.size(); i++) {
			throughputCounter++;
			if (id != sTuples.getId(i) && exchange.equals(sTuples.getTradeExchange(i))) {
				double hedge = (price / avgPrice[id] - 1) / (sTuples.getTradePrice(i) / avgPrice[sTuples.getId(i)] - 1);
				if (hedge <= -0.95 && hedge >= -1.05) {
					result.add((T) new OutTuple(ts, systemTs, threadID, TupleT.STD, sTuples.getId(i),
							sTuples.getTradePrice(i), id, price));
					sigmaCounter++;
				}
			}
		}
		if (throughputCounter > 0) {
			alpha = (System.nanoTime() - alphaTime) / throughputCounter;
			sigma = sigmaCounter / throughputCounter;
		} else {
			alpha = 0;
			sigma = 0;
		}
		return result;

	}

	@SuppressWarnings("unchecked")
	private List<T> processSTuple(long ts, long systemTs, int threadID, int id, double price, String exchange) {
		while (rTuples.size() > 0 && rTuples.getTS(0) < startTS)
			rTuples.discard();

		if (readyTupleCounter % numberOfBuckets == bucketID)
			sTuples.add(ts, id, price, exchange);

		List<T> result = new LinkedList<T>();

		double sigmaCounter = 0.0;
		long alphaTime = System.nanoTime();

		for (int i = 0; i < rTuples.size(); i++) {
			throughputCounter++;
			if (id!=rTuples.getId(i) && exchange.equals(rTuples.getTradeExchange(i))) {
				double hedge = (price / avgPrice[id] - 1) / (rTuples.getTradePrice(i) / avgPrice[rTuples.getId(i)] - 1);
				if (hedge <= -0.95 && hedge >= -1.05) {
					result.add((T) new OutTuple(ts, systemTs, threadID, TupleT.STD, rTuples.getId(i),
						rTuples.getTradePrice(i), id, price));
				sigmaCounter++;
				}

			}
		}
		if (throughputCounter > 0) {
			alpha = (System.nanoTime() - alphaTime) / throughputCounter;
			sigma = sigmaCounter / throughputCounter;
		} else {
			alpha = 0;
			sigma = 0;
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

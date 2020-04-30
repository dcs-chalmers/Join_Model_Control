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

package common.statistics;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;

import com.opencsv.CSVWriter;

import common.tuple.Tuple;
import common.util.Pair;
import common.tuple.SGTuple.TupleT;

public class RateStatistics extends Statistics {
	long throughputCounter;
	public long latencyCounter;
	public long latency;
	private long prevUtil;
	private long tupleCounter;

	public RateStatistics(String directory, String fileName) {
		super(directory, fileName);
		reset();
	}

	private void reset() {
		dataLines.clear();
		throughputCounter = 0;
		latencyCounter = 0;
		latency = 0;
		prevUtil = 0;
		tupleCounter = 0;
	}

	public void collect(long stat) {
		throughputCounter += stat;
	}

	public void throughput() {
		long thisSec = System.currentTimeMillis();
		if (thisSec - prevSec >= STATS_INTERVAL_MS) {
			dataLines.add(new String[] { Long.toString(thisSec), Long.toString(throughputCounter) });
			throughputCounter = 0;
			prevSec = thisSec;
		}
	}

	public long tupleBased() {
		if (++tupleCounter % STATS_INTERVAL_COUNTER == 0) {
			return tupleCounter;
		}
		return 0;
	}

	public long timeBased() {
		long val = tupleCounter - 1;
		tupleCounter = 1;
		return val;
	}

	public Pair<Long, Long> latency(Tuple t) {
		long thisSec = System.currentTimeMillis();
		if (t.getType() == TupleT.STD) {
			latency += System.nanoTime() - t.getSystemTS();
			latencyCounter++;
		}

		if (thisSec - prevSec >= STATS_INTERVAL_MS) {
			long val = 0;
			if (latencyCounter != 0) {
				val = (latency / latencyCounter) / 1000000;
				dataLines.add(new String[] { Long.toString(thisSec), Long.toString(val) });
			} else
				dataLines.add(new String[] { Long.toString(thisSec), "0" });
			latency = 0;
			latencyCounter = 0;
			prevSec = thisSec;

			return new Pair<Long, Long>(thisSec, val);
		}

		return null;
	}

	public void utilization(long util) {
		long thisSec = System.currentTimeMillis();
		if (thisSec - prevSec >= STATS_INTERVAL_MS) {
			dataLines.add(new String[] { Long.toString(thisSec),
					Double.toString(Math.min(0.99, ((util - prevUtil) / ((thisSec - prevSec) * 1000000.0))) * 100) });
			prevUtil = util;
			prevSec = thisSec;
		}
	}

	@Override
	public void addDataLines(String[] str) {
		dataLines.add(str);
	}

	@Override
	public void close() {
		if (dataLines.isEmpty())
			return;

		try {
			File dir = new File(directory);
			dir.mkdirs();
			File file = new File(dir,
					fileName + "_" + String.valueOf((LocalDateTime.now()).getHour())
							+ String.valueOf((LocalDateTime.now()).getMinute())
							+ String.valueOf((LocalDateTime.now()).getSecond()) + ".csv");

			CSVWriter csvWrite = new CSVWriter(new FileWriter(file));

			String[] entries = { "time", "value" };
			csvWrite.writeNext(entries);
			csvWrite.writeAll(dataLines);

			reset();

			csvWrite.flush();
			csvWrite.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

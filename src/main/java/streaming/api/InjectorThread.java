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

import java.util.List;

import common.InputRate;
import common.SourceConfig;
import common.statistics.RateStatistics;
import common.tuple.ControlTuple;
import common.tuple.Tuple;
import common.tuple.SGTuple.TupleT;
import common.util.NativeSleep;
import common.util.Util;
import controller.ControlEvent;
import controller.ElasticityControl;
import scalegate.ScaleGate;

public abstract class InjectorThread implements Runnable {

	protected ScaleGate<Tuple> sgIn;
	protected final int id;
	private int batchDelay;
	private int batchSize;
	final long DURATION;
	final long event_dt;
	long reportId;
	List<InputRate> inputRates;
	RateStatistics stats;
	ElasticityControl controller;

	public InjectorThread(int id, ScaleGate<Tuple> sgIn, SourceConfig sourceConfig, ElasticityControl controller,
			long event_dt) {
		this.id = id;
		this.sgIn = sgIn;
		this.DURATION = sourceConfig.getDuration();
		this.event_dt = event_dt * 1000000;
		this.controller = controller;

		if (sourceConfig.getInputRatesPath().equals("")) {
			this.batchDelay = 0;
			this.batchSize = 1;
		} else {
			inputRates = Util.parseInputRates(sourceConfig.getInputRatesPath());
			this.batchDelay = inputRates.get(0).getBatchDelay();
			this.batchSize = inputRates.get(0).getBatchSize();
			inputRates.remove(0);
		}

		stats = new RateStatistics(sourceConfig.getStatsDir(), "IT_" + id);
		reportId = 0;
	}

	@Override
	public void run() {

		long start_time = System.currentTimeMillis();
		long reference_nanosec = System.nanoTime();
		int totalTuplesCounter = 0;
		int batchTuplesCounter = 0;

		long before_ts = System.nanoTime();
		long accumulated_delay = 0;

		Tuple dummy = getDummyTuple(0);
		sgIn.addTuple(dummy, id);

		long prev_report_time = 0;
		long currenttime_ns = System.nanoTime();

		while (true) {
			if (batchTuplesCounter == batchSize) {
				batchTuplesCounter = 0;
				if (batchDelay > 0) {
					long after_ts = System.nanoTime();
					long sleep_us = (long) batchDelay - (after_ts - before_ts) / 1000 - accumulated_delay;

					if (sleep_us > 0) {
						long a = System.nanoTime();
						NativeSleep.sleep((int) sleep_us);
						long b = System.nanoTime();
						accumulated_delay = ((b - a) / 1000 - sleep_us);
					} else {
						accumulated_delay = -1 * sleep_us;
					}

					before_ts = System.nanoTime();
				}
				currenttime_ns = System.nanoTime();
			}

			totalTuplesCounter++;
			batchTuplesCounter++;

			Tuple foo = getNextTuple((currenttime_ns - reference_nanosec));
			if(foo.getType()==TupleT.FINAL)
				break;
			
			sgIn.addTuple(foo, id);

			long currentTime_ms = System.currentTimeMillis();
			if (inputRates != null && inputRates.size() != 0
					&& (currentTime_ms - start_time >= inputRates.get(0).getTime())) {
				batchSize = inputRates.get(0).getBatchSize();
				batchDelay = inputRates.get(0).getBatchDelay();
				inputRates.remove(0);
			}

			ControlEvent controlEvent = controller.getControlEvent(id);
			if (controlEvent != null) {
				Tuple ctrl = getControlTuple(TupleT.CTRL, foo.getTS(), id, controlEvent);
				sgIn.addTuple(ctrl, id);
			}

			stats.collect(1);
			stats.throughput();

			stats.tupleBased();
			if (foo.getTS() - prev_report_time >= event_dt) {
				controller.writeMeasuredOutput(stats.timeBased(), reportId, id);
				reportId++;
				prev_report_time = foo.getTS();
			}

			if (currentTime_ms - start_time >= DURATION) {
				System.out.println("work for " + (currentTime_ms - start_time) + " ms");
				break;
			}

		}

		Tuple foo = getFinalTuple(Long.MAX_VALUE);
		sgIn.addTuple(foo, id);
		foo = getFinalTuple(Long.MAX_VALUE);
		sgIn.addTuple(foo, id);
		controller.setTermination();

		System.out.println("Injector thread " + id + " done with " + totalTuplesCounter + " tuples.");

		stats.close();
	}

	protected abstract Tuple getNextTuple(long timestamp);

	protected abstract Tuple getDummyTuple(long timestamp);

	protected abstract Tuple getFinalTuple(long timestamp);

	protected static Tuple getControlTuple(TupleT type, long timestamp, int id, ControlEvent event) {
		return new ControlTuple(timestamp, event.getCreationTime(), id, type, event);
	}
}

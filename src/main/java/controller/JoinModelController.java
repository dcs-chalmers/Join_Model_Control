/*  Copyright (C) 2019  Hannaneh Najdataei,
 * 			Vincenzo Gulisano,
 *			Alessandro Vittorio Papadopoulos
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

package controller;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import common.ExecutionConfiguration;

public class JoinModelController extends ElasticityControl {

	private HashMap<Integer, Range> capacity;

	private double asb;
	final private long windowCounter;

	private long prev_ms;
	private boolean configFlag;
	private AtomicLong rReportInd, sReportInd;
	ConcurrentHashMap<Long, Long> rReports;
	ConcurrentHashMap<Long, Long> sReports;

	private long lastReportId, totalWorkToDo;
	private double totalWorkDone;

	public JoinModelController(ExecutionConfiguration config, double alpha, double sigma, double beta, double ws,
			long event_dt) {
		super(config);

		windowCounter = (long) (ws / event_dt);
		this.asb = alpha + (sigma * beta);

		rReports = new ConcurrentHashMap<Long, Long>();
		sReports = new ConcurrentHashMap<Long, Long>();
		rReportInd = new AtomicLong(-1);
		sReportInd = new AtomicLong(-1);

		capacity = new HashMap<>();
		for (int i = 1; i <= config.getThreadPoolCapacity(); i++) {
			capacity.put(i, new Range(i, this.asb));
		}

		lastReportId = -1;
		totalWorkToDo = 0;
		totalWorkDone = 0.0;
		configFlag = true;
		prev_ms = System.currentTimeMillis();
	}

	@Override
	ControlEvent generateControlEvent(long reconfigTime) {
		if (configFlag) {
			prev_ms += reconfigTime;
			configFlag = false;
		}

		int parallelism = config.getParallelism();
		long current_ms = System.currentTimeMillis();
		double deltaT = (current_ms - prev_ms) / 1000;
		if (deltaT == 0)
			return null;
		prev_ms = current_ms;

		double comparisonsToDo = updateComparisons();
		double actualComparisonsToDo = comparisonsToDo - totalWorkDone;
		double actualComparisonsToDo_sec = actualComparisonsToDo / deltaT;

		double workToDo = actualComparisonsToDo_sec;

		Range range = capacity.get(parallelism);
		totalWorkDone += deltaT * range.upper;
		if (totalWorkDone > comparisonsToDo)
			totalWorkDone = comparisonsToDo;

		if (parallelism < config.getThreadPoolCapacity() && workToDo > range.upper) {
			configFlag = true;
			for (int i = parallelism + 1; i < config.getThreadPoolCapacity(); i++) {
				Range newRange = capacity.get(i);
				if (workToDo <= newRange.upper)
					return new ControlEvent(i - parallelism);
			}
			return new ControlEvent(config.getThreadPoolCapacity() - parallelism);
		}
		if (parallelism > 1 && workToDo <= range.lower) {
			configFlag = true;
			for (int i = parallelism - 1; i > 1; i--) {
				Range newRange = capacity.get(i);
				if (workToDo > newRange.lower)
					return new ControlEvent(i - parallelism);
			}
			return new ControlEvent(1 - parallelism);
		}

		return null;

	}

	private long updateComparisons() {
		long workToDo = 0;
		long ind = Math.min(rReportInd.get(), sReportInd.get());
		long sReport, rReport;

		if (ind > lastReportId) {
			sReport = rReports.get(ind);
			rReport = rReports.get(ind);

			workToDo = sReport * rReport;
			for (long i = ind - 1; i >= ind - windowCounter && i >= 0; i--) {
				workToDo += sReport * rReports.get(i) + rReport * sReports.get(i);
			}

			for (long i = lastReportId + 1; i < ind; i++) {
				long rRate = rReports.get(i);
				long sRate = sReports.get(i);
				long rWinSize = 0;
				long sWinSize = 0;
				for (long j = i - 1; j >= i - windowCounter && j >= 0; j--) {
					rWinSize += rReports.get(j);
					sWinSize += sReports.get(j);
				}

				totalWorkToDo += (rRate * sRate) + (rRate * sWinSize) + (sRate * rWinSize);
				lastReportId = i;
			}
		}

		return (totalWorkToDo + workToDo);

	}

	@Override
	void updateR(long r, long reportId) {
		rReports.put(reportId, r);
		rReportInd.set(reportId);
	}

	@Override
	void updateS(long s, long reportId) {
		sReports.put(reportId, s);
		sReportInd.set(reportId);
	}

	private static class Range {
		public double upper, lower;
		private double upperThreshold = 0.8, lowerThreshold = 0.7;

		public Range(int n, double asb) {
			upper = upperThreshold * (n / asb);
			lower = lowerThreshold * ((n - 1) / asb);
		}
	}
}

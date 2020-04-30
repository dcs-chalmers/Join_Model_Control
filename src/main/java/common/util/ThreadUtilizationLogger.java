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

package common.util;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

import common.ExecutionConfiguration;
import common.statistics.RateStatistics;

public class ThreadUtilizationLogger {

	private static final long SAMPLE_FREQUENCY_MS = 1000;
	private static final String LOGGER_THREAD_NAME = "Utilization-Logger";
	private final Thread loggerThread;

	public ThreadUtilizationLogger(ExecutionConfiguration config) {
		loggerThread = new Thread(new Logger(config));
		loggerThread.setName(LOGGER_THREAD_NAME);
	}

	public void enable() {
		loggerThread.start();
	}

	public boolean isEnabled() {
		return loggerThread.isAlive();
	}

	public void disable() {
		loggerThread.interrupt();
		try {
			loggerThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private static class Logger implements Runnable {
		private RateStatistics[] stats;
		private int capacity;

		public Logger(ExecutionConfiguration config) {
			capacity = config.getThreadPoolCapacity();
			stats = new RateStatistics[capacity];
			for (int i = 0; i < capacity; i++)
				stats[i] = new RateStatistics(config.getStatsDir(), "CPU_" + i);
		}

		@Override
		public void run() {
			while (!Thread.currentThread().isInterrupted()) {
				writeThreadUtilizations();
				try {
					Thread.sleep(SAMPLE_FREQUENCY_MS);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					break;
				}
			}

			for (int i = 0; i < capacity; i++)
				stats[i].close();
		}

		private void writeThreadUtilizations() {
			ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
			for (long threadId : threadMXBean.getAllThreadIds()) {
				String name = threadMXBean.getThreadInfo(threadId).getThreadName();
				if (name.contains("PT")) {
					long cpuTime = threadMXBean.getThreadCpuTime(threadId);
					int id = Integer.parseInt(name.replace("PT", ""));
					stats[id].utilization(cpuTime);
				}
			}
		}
	}

}

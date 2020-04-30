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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicReference;

import common.ExecutionConfiguration;

public abstract class ElasticityControl {
	private boolean terminationFlag = false;
	private int eventID;
	protected ExecutionConfiguration config;

	HashMap<Integer, AtomicReference<ControlEvent>> controlEvents;
	HashMap<Integer, BlockingQueue<Long>> measurments;

	AtomicReference<Double> alpha;
	AtomicReference<Double> beta;
	AtomicReference<Double> sigma;

	public ElasticityControl(ExecutionConfiguration config) {
		this.config = config;

		controlEvents = new HashMap<>();
		for (int i = 0; i < config.getNumOfOperatorInputThreads(); i++)
			controlEvents.put(i, new AtomicReference<>());

		measurments = new HashMap<>();
		for (int i = 0; i < config.getNumOfOperatorInputThreads(); i++)
			measurments.put(i, new LinkedBlockingQueue<>());

		alpha = new AtomicReference<>();
		beta = new AtomicReference<>();
		sigma = new AtomicReference<>();
	}

	public ControlEvent getControlEvent(int readerId) {
		return controlEvents.get(readerId).get();
	}

	public void writeMeasuredOutput(long measurment, long reportId, int writerId) {
		if (writerId == 0)
			updateR(measurment, reportId);
		else
			updateS(measurment, reportId);

	}

	public boolean getTermination() {
		return terminationFlag;
	}

	public void setTermination() {
		terminationFlag = true;
	}

	public int nextEventID() {
		return ++eventID;
	}

	public boolean generateFeedback(long reconfigTime) {
		if (config.getControllerFlag()) {
			ControlEvent ctrlEvent = generateControlEvent(reconfigTime);
			if (ctrlEvent != null) {
				ctrlEvent.setEventID(nextEventID());
				if (ctrlEvent.mapping == null)
					ctrlEvent.setMapping(config.getParallelism() + ctrlEvent.eventType, config.getNumberOfBuckets());
				ctrlEvent.setCreationTime(System.nanoTime());

				for (int key : controlEvents.keySet())
					controlEvents.get(key).set(ctrlEvent);
				return true;
			}
		}
		return false;
	}

	public double getAlpha() {
		return alpha.get();
	}

	public void setAlpha(double alpha) {
		this.alpha.set(alpha);
	}

	public double getBeta() {
		return beta.get();
	}

	public void setBeta(double beta) {
		this.beta.set(beta);
	}

	public double getSigma() {
		return sigma.get();
	}

	public void setSigma(double sigma) {
		this.sigma.set(sigma);
	}

	abstract ControlEvent generateControlEvent(long reconfigTime);

	abstract void updateR(long measurment, long reportId);

	abstract void updateS(long measurment, long reportId);
}

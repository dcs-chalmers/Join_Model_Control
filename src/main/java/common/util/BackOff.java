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

package common.util;
import java.util.concurrent.ThreadLocalRandom;

public class BackOff {
	private final int minDelay, maxDelay;
	private int limit;

	public BackOff(int min, int max) {
		minDelay = min;
		maxDelay = max;
		limit = minDelay;
	}

	public void backoff() throws InterruptedException {
		int delay = ThreadLocalRandom.current().nextInt(limit);
		limit = Math.min(maxDelay, 2 * limit);
		Util.sleep(delay);
	}

	public void resetLimit() {
		limit = minDelay;
	}

}
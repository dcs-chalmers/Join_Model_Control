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

import java.io.Serializable;

class MicroWinS implements Serializable {
	private static final long serialVersionUID = 1L;
	static final int LENGTH = 5000;
	private int size = 0;
	private int addIndex = 0;
	private int readIndex = 0;
	private long[] _ts = new long[LENGTH];
	private int[] _a = new int[LENGTH];
	private float[] _b = new float[LENGTH];
	private double[] _c = new double[LENGTH];
	private boolean[] _d = new boolean[LENGTH];

	void add(long ts, int a, float b, double c, boolean d) {
		if (size == LENGTH)
			throw new RuntimeException("CircularWindowArray exhausted!");
		_ts[addIndex] = ts;
		_a[addIndex] = a;
		_b[addIndex] = b;
		_c[addIndex] = c;
		_d[addIndex] = d;
		addIndex = (addIndex + 1) % LENGTH;
		size++;
	}

	int getA(int i) {
		return _a[(readIndex + i) % LENGTH];
	}

	float getB(int i) {
		return _b[(readIndex + i) % LENGTH];
	}

	double getC(int i) {
		return _c[(readIndex + i) % LENGTH];
	}

	boolean getD(int i) {
		return _d[(readIndex + i) % LENGTH];
	}

	void discard() {
		readIndex = (readIndex + 1) % LENGTH;
		size--;
	}

	long getTS(int i) {
		return _ts[(readIndex + i) % LENGTH];
	}

	int size() {
		return size;
	}


}

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

import java.io.Serializable;

class MicroWin implements Serializable {
	private static final long serialVersionUID = 1L;
	static final int LENGTH = 5000;
	private int size = 0;
	private int addIndex = 0;
	private int readIndex = 0;
	private long[] _ts = new long[LENGTH];
	private int[] _id = new int[LENGTH];
	private double[] _price = new double[LENGTH];
	private String[] _exchange = new String[LENGTH];

	long historicalAdded = 0;

	void add(long ts, int id, double price, String exchange) {
		if (size == LENGTH)
			throw new RuntimeException("CircularWindowArray exhausted!");
		_ts[addIndex] = ts;
		_id[addIndex] = id;
		_price[addIndex] = price;
		_exchange[addIndex] = exchange;
		addIndex = (addIndex + 1) % LENGTH;
		size++;
		historicalAdded++;
	}

	int getId(int i) {
		return _id[(readIndex + i) % LENGTH];
	}

	double getTradePrice(int i) {
		return _price[(readIndex + i) % LENGTH];
	}

	String getTradeExchange(int i) {
		return _exchange[(readIndex + i) % LENGTH];
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

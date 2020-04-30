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

import common.tuple.Tuple;

public class OutTuple extends Tuple {

	final int id;
	final double price;
	final int affectingId;
	final double affectingPrice;

	public OutTuple(long ts, long systemTS, int injID, TupleT type, int id, double price, int affectingId,
			double affectingP) {
		super(ts, systemTS, injID, type);

		this.id = id;
		this.price = price;
		this.affectingId = affectingId;
		this.affectingPrice = affectingP;
	}

	@Override
	public String toString() {
		return "[ts=" + getTS() + ", id=" + id + ", price=" + price + ", affectedBy=" + affectingId
				+ ", affectingPrice=" + affectingPrice + "]";
	}

	@Override
	public Tuple getCopy() {
		return new OutTuple(getTS(), getSystemTS(), getInjectorId(), getType(), id, price, affectingId,
				affectingPrice);
	}

	public static Tuple getDummy(long ts, int injID) {
		OutTuple t = new OutTuple(ts, -1, injID, TupleT.DUMMY, -1, 0, -1, 0);
		return t;
	}

	public static Tuple getFinal(long ts, int injID) {
		OutTuple t = new OutTuple(ts, -1, injID, TupleT.FINAL, -1, 0, -1, 0);
		return t;
	}

	public static Tuple getCtrl(long ts, int injID) {
		OutTuple t = new OutTuple(ts, -1, injID, TupleT.CTRL, -1, 0, -1, 0);
		return t;
	}

	public static Tuple getFlush(long ts, int injID) {
		OutTuple t = new OutTuple(ts, -1, injID, TupleT.FLUSH, -1, 0, -1, 0);
		return t;
	}
}

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

import common.tuple.Tuple;

public class RSTuple extends Tuple {

	int x;
	float y;

	int a;
	float b;
	double c;
	boolean d;

	public RSTuple(long ts, long systemTS, int injID, TupleT type, int x, float y, int a, float b, double c,
			boolean d) {
		super(ts, systemTS, injID, type);

		this.x = x;
		this.y = y;

		this.a = a;
		this.b = b;
		this.c = c;
		this.d = d;
	}

	/*
	 * Copy-constructor
	 */
	public RSTuple(RSTuple t) {
		super(t.getTS(), t.getSystemTS(), t.getInjectorId(), t.getType());

		this.x = t.x;
		this.y = t.y;

		this.a = t.a;
		this.b = t.b;
		this.c = t.c;
		this.d = t.d;
	}

	@Override
	public String toString() {
		return "[ts=" + getTS() + ", x=" + x + ", y=" + y + ", a=" + a + ", b=" + b + ", c=" + c + ", d=" + d;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + a;
		result = prime * result + Float.floatToIntBits(b);
		long temp;
		temp = Double.doubleToLongBits(c);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + (d ? 1231 : 1237);
		result = prime * result + (int) (getTS() ^ (getTS() >>> 32));
		result = prime * result + x;
		result = prime * result + Float.floatToIntBits(y);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RSTuple other = (RSTuple) obj;
		if (a != other.a)
			return false;
		if (Float.floatToIntBits(b) != Float.floatToIntBits(other.b))
			return false;
		if (Double.doubleToLongBits(c) != Double.doubleToLongBits(other.c))
			return false;
		if (d != other.d)
			return false;
		if (getTS() != other.getTS())
			return false;
		if (x != other.x)
			return false;
		if (Float.floatToIntBits(y) != Float.floatToIntBits(other.y))
			return false;
		return true;
	}

	@Override
	public Tuple getCopy() {
		RSTuple tc = new RSTuple(this);
		return tc;
	}

	public static Tuple getDummyRS(long ts, int injID) {
		RSTuple t = new RSTuple(ts, -1, injID, TupleT.DUMMY, 0, (float) 0, 0, (float) 0, 0.0, false);
		return t;
	}

	public static Tuple getFinalRS(long ts, int injID) {
		RSTuple t = new RSTuple(ts, -1, injID, TupleT.FINAL, 0, (float) 0, 0, (float) 0, 0.0, false);
		return t;
	}

	public static Tuple getCtrlRS(long ts, int injID) {
		RSTuple t = new RSTuple(ts, -1, injID, TupleT.CTRL, 0, (float) 0, 0, (float) 0, 0.0, false);
		return t;
	}

	public static Tuple getFlushRS(long ts, int injID) {
		RSTuple t = new RSTuple(ts, -1, injID, TupleT.FLUSH, 0, (float) 0, 0, (float) 0, 0.0, false);
		return t;
	}
}

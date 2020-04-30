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

public class STuple extends Tuple {

	public final int a;
	public final float b;
	public final double c;
	public final boolean d;

	public STuple(long ts, long systemTS, int injId, TupleT type, int a, float b, double c, boolean d) {
		super(ts, systemTS, injId, type);

		this.a = a;
		this.b = b;
		this.c = c;
		this.d = d;
	}

	@Override
	public String toString() {
		return "[ts=" + getTS() + ", a=" + a + ", b=" + b + ", c=" + c + ", d=" + d + "]";
	}

	@Override
	public Tuple getCopy() {
		return new STuple(getTS(), getSystemTS(), getInjectorId(), getType(), a, b, c, d);
	}

}

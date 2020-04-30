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

package examples.ScaleJoin;

import common.tuple.Tuple;

public class RTuple extends Tuple {

	public final int x;
	public final float y;

	public RTuple(long ts, long systemTS, int injId, TupleT type, int x, float y) {
		super(ts, systemTS, injId, type);
		this.x = x;
		this.y = y;
	}

	@Override
	public String toString() {
		return "[ts=" + getTS() + ", x=" + x + ", y=" + y + "]";
	}

	@Override
	public Tuple getCopy() {
		return new RTuple(getTS(), getSystemTS(), getInjectorId(), getType(), x, y);
	}

}

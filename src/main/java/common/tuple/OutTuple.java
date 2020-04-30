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

package common.tuple;

public class OutTuple extends Tuple {

	public OutTuple(long timestamp, long systemTimestamp, int injectorID, TupleT type) {
		super(timestamp, systemTimestamp, injectorID, type);
	}

	@Override
	public Tuple getCopy() {
		return new OutTuple(this.getTS(), this.getSystemTS(), this.getInjectorId(), this.getType());
	}
	
	public static Tuple getDummy(long ts, int injectorID) {
		OutTuple t = new OutTuple(ts, -1, injectorID, TupleT.DUMMY);
		return t;
	}
	
	public static Tuple getFinal(long ts, int injectorID) {
		OutTuple t = new OutTuple(ts, -1, injectorID, TupleT.FINAL);
		return t;
	}
	
	public static Tuple getCtrl(long ts, int injectorID) {
		OutTuple t = new OutTuple(ts, -1, injectorID, TupleT.CTRL);
		return t;
	}
	
	public static Tuple getFlush(long ts, int injectorID) {
		OutTuple t = new OutTuple(ts, -1, injectorID, TupleT.FLUSH);
		return t;
	}
}
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

import java.util.HashMap;
import java.util.List;

import controller.ControlEvent;

public class ControlTuple extends Tuple {
	public final int eventID;
	public final int eventType;
	public final HashMap<Integer, List<Integer>> mapping;

	public ControlTuple(long timestamp, long systemTimestamp, int injectorID, TupleT type, int eventID, int eventType,
			HashMap<Integer, List<Integer>> mapping) {
		super(timestamp, systemTimestamp, injectorID, type);
		this.eventID = eventID;
		this.eventType = eventType;
		this.mapping = mapping;
	}

	public ControlTuple(long timestamp, long systemTimestamp, int injectorID, TupleT type, ControlEvent event) {
		super(timestamp, systemTimestamp, injectorID, type);
		
		this.eventID = event.getEventID();
		this.eventType = event.eventType;
		mapping = event.mapping;
	}

	public String toString() {
		return "[ts=" + this.getTS() + ", systs=" + this.getSystemTS() + " type=" + this.getType() + " "
				+ this.eventType + "]";
	}

	@Override
	public Tuple getCopy() {
		return new ControlTuple(this.getTS(), this.getSystemTS(), this.getInjectorId(), this.getType(),
				this.eventID, this.eventType, this.mapping);
	}
}

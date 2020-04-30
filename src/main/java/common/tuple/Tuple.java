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

public abstract class Tuple implements SGTuple {

	private final long timestamp, systemTS;
	private final int injectorID;
	private final TupleT type;
	
	public Tuple(long timestamp, long systemTimestamp, int injectorID, TupleT type) {
		this.timestamp = timestamp;
		this.systemTS = systemTimestamp;
		this.injectorID = injectorID;
		this.type = type;
	}
	
	@Override
	public long getTS() {
		return timestamp;
	}
	
    public long getSystemTS() {
    	return systemTS;
    }

    public int getInjectorId() {
    	return injectorID;
    }
    
    @Override
    public TupleT getType() {
    	return type;
    }
    
    @Override
	public int compareTo(SGTuple arg0) {
		if (getTS() == arg0.getTS()) {
			return 0;
		}
		return getTS() < arg0.getTS() ? -1 : 1;
	}

}

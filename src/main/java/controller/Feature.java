/*  Copyright (C) 2019  Hannaneh Najdataei,
 * 			Vincenzo Gulisano,
 *			Alessandro Vittorio Papadopoulos
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

package controller;

public class Feature {
	
	public static enum FeatureType {InputRate, Latency, Throughput, FINAL, CTRL};
	
	FeatureType type;
	long timestamp;
	long value;
	
	public Feature(FeatureType type, long ts, long value) {
		this.type = type;
		timestamp = ts;
		this.value = value;
	}
	
	FeatureType getType() {
		return type;
	}
	
	long getTS() {
		return timestamp;
	}
	
	long getValue() {
		return value;
	}
	
}

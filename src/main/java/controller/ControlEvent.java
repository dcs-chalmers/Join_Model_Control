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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;

import common.util.Util;

public class ControlEvent {
	private int eventID;
	public final int eventType;
	public HashMap<Integer, List<Integer>> mapping;
	private long creationTime; 

	public ControlEvent(JSONObject obj) {
		this.eventID = obj.getInt("eventID");
		this.eventType = obj.getInt("eventType");
		mapping = Util.getMapping(obj.getJSONArray("mapping"));
	}
	
	public ControlEvent(int eventType, HashMap<Integer, List<Integer>> mapping) {
		this.eventType = eventType;
		this.mapping = mapping;
	}
	
	public ControlEvent(int eventType) {
		this(eventType, null);
	}
	
	public void setEventID(int eventID) {
		this.eventID = eventID;
	}
	
	public int getEventID() {
		return eventID;
	}
	
	public long getCreationTime() {
		return creationTime;
	}

	public void setCreationTime(long creationTime) {
		this.creationTime = creationTime;
	}

	public void setMapping(int numberOfThreads, int numberOfBuckets) {
		mapping = RRMapping(numberOfThreads, numberOfBuckets);
	}
	
	public static HashMap<Integer, List<Integer>> RRMapping(int numberOfThreads, int numberOfBuckets){
		HashMap<Integer, List<Integer>> mapping = new HashMap<Integer, List<Integer>>();
		for (int i = 0; i < numberOfThreads; i++) {
			mapping.put(i, new ArrayList<Integer>());
		}
		
		for (int i = 0; i < numberOfBuckets; i++)
			mapping.get(i%numberOfThreads).add(i);
		
		return mapping;
	}
}

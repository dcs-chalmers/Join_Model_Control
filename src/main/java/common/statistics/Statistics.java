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

package common.statistics;

import java.util.ArrayList;
import java.util.List;

public abstract class Statistics {
	final long STATS_INTERVAL_MS = 1000;
	final long STATS_INTERVAL_COUNTER = 100;
	
	String directory;
	String fileName;
	long prevSec;
	List<String[]> dataLines;
	
	public Statistics(String directory, String fileName) {
		this.directory = directory;
		this.fileName = fileName;

		this.dataLines = new ArrayList<>();
		prevSec = System.currentTimeMillis();
	}
	
	public abstract void addDataLines(String[] str);
	
	public abstract void close();
}

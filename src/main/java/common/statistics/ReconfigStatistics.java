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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;

import com.opencsv.CSVWriter;

public class ReconfigStatistics extends Statistics{
	
	public ReconfigStatistics(String directory, String fileName) {
		super(directory, fileName);
	}

	@Override
	public void addDataLines(String[] str) {
		dataLines.add(str);
	}

	@Override
	public void close() {
		if (dataLines.isEmpty())
			return;

		try {
			File dir = new File(directory);
			dir.mkdirs();
			File file = new File(dir, fileName + "_" + String.valueOf((LocalDateTime.now()).getHour()) + String.valueOf((LocalDateTime.now()).getMinute()) + String.valueOf((LocalDateTime.now()).getSecond()) + ".csv");

			CSVWriter csvWrite = new CSVWriter(new FileWriter(file));

			String[] entries = { "id", "ts", "type", "duration"};
			csvWrite.writeNext(entries);
			csvWrite.writeAll(dataLines);

			csvWrite.flush();
			csvWrite.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}

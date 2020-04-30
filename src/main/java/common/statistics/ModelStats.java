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

public class ModelStats extends Statistics {

	private double alpha, beta, sigma, counter;

	public ModelStats(String directory, String fileName) {
		super(directory, fileName);
		counter = 0;
	}

	public void add(double alpha, double beta, double sigma) {
		if (alpha == 0)
			return;
		this.alpha += alpha;
		this.beta += beta;
		this.sigma += sigma;
		counter++;
	}

	@Override
	public void addDataLines(String[] str) {}

	@Override
	public void close() {
		try {
			File dir = new File(directory);
			dir.mkdirs();
			File file = new File(dir,
					fileName + "_" + String.valueOf((LocalDateTime.now()).getHour())
							+ String.valueOf((LocalDateTime.now()).getMinute())
							+ String.valueOf((LocalDateTime.now()).getSecond()) + ".csv");

			CSVWriter csvWrite = new CSVWriter(new FileWriter(file));

			String[] entries = { "alpha", "beta", "sigma" };
			csvWrite.writeNext(entries);
			csvWrite.writeNext(new String[] { Double.toString((alpha / counter) / 1000000000L),
					Double.toString((beta / counter) / 1000000000L), Double.toString(sigma / counter) });
			csvWrite.flush();
			csvWrite.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}

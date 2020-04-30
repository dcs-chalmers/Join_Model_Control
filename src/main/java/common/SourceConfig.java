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

package common;

public class SourceConfig {
	private int batchDelay;
	private int batchSize;
	private long duration;
	private String statsDir, inputRatesPath;
	private String rFilePath, sFilePath;
	
	public int getBatchDelay() {
		return batchDelay;
	}
	public void setBatchDelay(int batchDelay) {
		this.batchDelay = batchDelay;
	}
	public int getBatchSize() {
		return batchSize;
	}
	public void setBatchSize(int batchSize) {
		this.batchSize = batchSize;
	}
	public long getDuration() {
		return duration;
	}
	public void setDuration(long duration) {
		this.duration = duration;
	}
	public String getStatsDir() {
		return statsDir;
	}
	public void setStatsDir(String statsDir) {
		this.statsDir = statsDir;
	}
	public void setInputRatesPath(String path) {
		this.inputRatesPath = path;
	}
	public String getInputRatesPath() {
		return inputRatesPath;
	}
	public void setRFilePath(String path) {
		this.rFilePath = path;
	}
	public String getRFilePath() {
		return rFilePath;
	}
	public void setSFilePath(String path) {
		this.sFilePath = path;
	}
	public String getSFilePath() {
		return sFilePath;
	}
}

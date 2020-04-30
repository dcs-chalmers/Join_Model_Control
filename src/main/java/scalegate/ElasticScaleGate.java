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

package scalegate;

import common.tuple.Tuple;

public interface ElasticScaleGate<T extends Tuple> extends ScaleGate<T> {

	public ESGNode<T> getReaderHandle(int readerId);

	public void announceReaders(ESGNode<T> sgInHandle, int startingNewReaderId, int numOfNewReaders);

	public void announceWriters(T auxiliaryTuple, int startingNewWriterId, int numOfNewWriters);

	public void removeReaders(int readerId);

	public void removeWriters(int writerId, T auxTuple);
}

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

import java.util.concurrent.atomic.AtomicReferenceArray;

import common.tuple.Tuple;

public class ESGNode<T extends Tuple> {
	final AtomicReferenceArray<ESGNode<T>> next;
	final T obj;
	final ELasticScaleGateFlowControl<T>.WriterThreadLocalData ln;
	final int writerID;
	volatile boolean assigned;

	public ESGNode(int levels, T t, ELasticScaleGateFlowControl<T>.WriterThreadLocalData ln, int writerID) {
		next = new AtomicReferenceArray<ESGNode<T>>(levels);
		for (int i = 0; i < levels; i++) {
			next.set(i, null);
		}
		this.obj = t;
		this.assigned = false;
		this.ln = ln;
		this.writerID = writerID;
	}

	public ESGNode<T> getNext(int level) {
		return next.get(level);
	}

	public T getTuple() {
		return this.obj;
	}

	public void setNext(int i, ESGNode<T> newNode) {
		next.set(i, newNode);
	}

	public boolean trySetNext(int i, ESGNode<T> oldNode, ESGNode<T> newNode) {
		return next.compareAndSet(i, oldNode, newNode);
	}

	public boolean isLastAdded() {
		return this == ln.written;
	}
}

/*
* This implementation is based on https://github.com/ErkoRisthein/conformance-checker
* 
* Copyright 2019 Erko Risthein
* 
* Copyrights licensed under the MIT Open-Source License
* https://opensource.org/licenses/MIT
* 
* Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
* The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

package edu.uic.prominent.processmining.decaypns.pnmetrics.util;

import java.util.ArrayList;
import java.util.List;

import edu.uic.prominent.processmining.decaypns.log.util.Trace;

public class ConformanceParameters {
	private Trace trace;
	private int count; // How often is this trace present in the log?
	private int missing;
	private int remaining;
	private int consumed;
	private int produced;
	private List<Integer> enabledTransitions = new ArrayList<>();

	public ConformanceParameters(Trace trace, int count) {
		this.trace = trace;
		this.count = count;
	}
	
	public boolean isNull(){
		boolean isNull = false;
		try{
			if(missing == 1 || remaining == 1 || consumed == 1 || produced == 1){}
		}catch(Exception e){
			isNull = true;
		}
		return isNull;
	}

	public Trace trace() {
		return trace;
	}

	public int count() {
		return count;
	}

	public int missing() {
		return missing;
	}

	public int remaining() {
		return remaining;
	}

	public int consumed() {
		return consumed;
	}

	public int produced() {
		return produced;
	}

	public void incrementMissing() {
		missing++;
	}
	
	public void incrementMissing(int incrementBy) {
		missing += incrementBy;
	}

	public void setRemaining(int remaining) {
		this.remaining = remaining;
	}

	public void incrementConsumed() {
		consumed++;
	}
		
	public void incrementConsumed(int incrementBy) {
		consumed += incrementBy;
	}

	public void incrementProduced() {
		produced++;
	}
	
	public void incrementProduced(int incrementBy) {
		produced += incrementBy;
	}

	public void addEnabledTransition(Integer count) {
		enabledTransitions.add(count);
	}

	public double getMeanEnabledTransitions() {
		double sum = 0;
		for (Integer c : enabledTransitions) {
			sum += c;
		}
		return sum / enabledTransitions.size();
	}

	@Override
	public String toString() {
		return "{" +
				"n=" + count +
				", m=" + missing +
				", r=" + remaining +
				", c=" + consumed +
				", p=" + produced +
				//", mean=" + getMeanEnabledTransitions() +
				'}';
	}
}
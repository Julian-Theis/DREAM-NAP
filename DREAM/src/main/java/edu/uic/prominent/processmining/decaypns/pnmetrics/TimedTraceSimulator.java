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

package edu.uic.prominent.processmining.decaypns.pnmetrics;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.model.impl.XAttributeTimestampImpl;

import edu.uic.prominent.processmining.decaypns.log.util.Event;
import edu.uic.prominent.processmining.decaypns.log.util.Trace;
import edu.uic.prominent.processmining.decaypns.petrinet.PetriNet;
import edu.uic.prominent.processmining.decaypns.petrinet.TimedTransition;
import edu.uic.prominent.processmining.decaypns.petrinet.Transition;


public class TimedTraceSimulator {
	private PetriNet petriNet;
	private List<Trace> traceBuffer;
	
	private int timebase;

	public TimedTraceSimulator(PetriNet petriNet, int timebase) {
		this.petriNet = petriNet;
		new Random();
		this.traceBuffer = new ArrayList<Trace>();
		this.timebase = timebase;
	}
	
	public List<Trace> simulate(int runs){
		return simulation(runs, Integer.MAX_VALUE);
	}
	
	public List<Trace> simulate(int runs, int maxTraceLength){
		return simulation(runs, maxTraceLength);
	}
	
	private List<Trace> simulation(int runs, int maxTraceLength){
		List<String> buffer = new ArrayList<String>(Arrays.asList(new String[runs]));
		for(int i = 0; i < buffer.size(); i++){
			Trace tr = createTrace(this.petriNet, maxTraceLength, i);
			if(tr != null)
				traceBuffer.add(tr);
		}
		return this.traceBuffer;
	}
	
	public void toCSV(String file){
		try{
			PrintWriter writer = new PrintWriter("logs/" + file, "UTF-8");			
			int c = 1;
			writer.println("Activity,Timestmap,Case");
			for(Trace t : traceBuffer){
				for(Event e : t.trace){
					writer.println(e.name() + "," + e.time() + "," + c);
				}
				c++;
			}
			writer.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private Trace createTrace(PetriNet pn, int maxTraceLength, int caseId) {
		Trace trace = new Trace(caseId, 0L);
		pn.cleanUpRemainingTokens();
		pn.addStartTokens();
		
		long time = 0L;
		double delta_time = 0;
		
		//Map<Transition, Boolean> stayedEnabledMap = new HashMap<Transition, Boolean>();
		Map<Transition, Boolean> enabledTransitionsMap = new HashMap<Transition, Boolean>();
		Map<Transition, Double> timeSampleMap = new HashMap<Transition, Double>();
		for(Transition t : pn.getTransitions()){
				//stayedEnabledMap.put(t, false);
				enabledTransitionsMap.put(t, false);
				timeSampleMap.put(t, null);
		}
				
		while (!pn.finalMarkingReached()) {
			if(trace.length() > maxTraceLength){
				break;
			}
			
			// Which transitions are enabled?
			// Which transitions are disabled?
			List<Transition> enabledTransitions = pn.enabledTransitions();
			if (enabledTransitions == null){
				break;
			}
			List<Transition> disabledTransitions = pn.getTransitions();
			for(Transition t : enabledTransitions){
				disabledTransitions.remove(t);
			}
			
			// if changes from disabled to enable --> sample
			// if changes from enabled to disabled --> null
			// if stays enabled --> add differential
			for(Transition t : enabledTransitions){
				if(!enabledTransitionsMap.get(t)){
					TimedTransition ti = (TimedTransition) t;
					enabledTransitionsMap.put(ti, true);
					timeSampleMap.put(ti, (ti.sample() * timebase));
				}else if(enabledTransitionsMap.get(t)){
					double newVal = timeSampleMap.get(t) - delta_time;
					timeSampleMap.put(t, newVal);
				}
			}
			
			for(Transition t : disabledTransitions){
				if(enabledTransitionsMap.get(t)){
					TimedTransition ti = (TimedTransition) t;
					enabledTransitionsMap.put(ti, false);
					timeSampleMap.put(ti, null);
				}
			}
			

			//Calculate MIN value and update time
			Iterator<Entry<Transition, Double>> iter = timeSampleMap.entrySet().iterator();
			while(iter.hasNext()){
				Entry<Transition, Double> entry = iter.next();
				if(entry.getValue() == null){
					//System.out.println(entry.getKey().name() + " null");
				}else{
					//System.out.println(entry.getKey().name() + " " + entry.getValue().doubleValue());
				}
			}
						
			Entry<Transition, Double> firingTransition = getMin(timeSampleMap);
			delta_time = firingTransition.getValue().doubleValue();
			time = time + firingTransition.getValue().longValue();
			
			//System.out.println("Firing: " + firingTransition.getKey().name() + " " + firingTransition.getValue().doubleValue());
			//System.out.println("________");
			
			
			firingTransition.getKey().consumeInputTokens();
			firingTransition.getKey().produceOutputTokens();
			if (firingTransition.getKey().isVisible()){
				trace.addEvent(new Event(firingTransition.getKey().name(), new XAttributeTimestampImpl(XTimeExtension.KEY_TIMESTAMP, time)));
			}
		}
		return trace;
	}

		
	private Entry<Transition, Double> getMin(Map<Transition, Double> map){
		Entry<Transition, Double> min = null;
		double minValue = Double.MAX_VALUE;
		Iterator<Entry<Transition, Double>> iter = map.entrySet().iterator();
		while(iter.hasNext()){
			Entry<Transition, Double> entry = iter.next();
			if(entry.getValue() != null){
				if(entry.getValue().doubleValue() < minValue){
					minValue = entry.getValue().doubleValue();
					min = entry;
				}
			}
		}
		return min;
	}
}

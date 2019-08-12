package edu.uic.prominent.processmining.decaypns.pnmetrics;

import java.util.Collection;
import java.util.HashMap;
import java.util.Random;

import edu.uic.prominent.processmining.decaypns.log.EventLog;
import edu.uic.prominent.processmining.decaypns.log.util.Trace;
import edu.uic.prominent.processmining.decaypns.petrinet.PetriNet;
import edu.uic.prominent.processmining.decaypns.petrinet.util.TransitionFiringCountLimitation;

public class Precision {

	private PetriNet petriNet;
	private EventLog eventLog;
	private HashMap<String, Trace> traceBuffer;
	private HashMap<String, Trace> trueTraces;

	public Precision(PetriNet petriNet, EventLog eventLog) {
		this.petriNet = petriNet;
		this.eventLog = eventLog;
		new Random();

		this.traceBuffer = new HashMap<String, Trace>();
		this.trueTraces = new HashMap<String, Trace>();
	}

	public double calculatePrecision(int runs, HashMap<String, TransitionFiringCountLimitation> transFireLimits) {
		eventLog.forEach((entry) -> {
			if(!this.trueTraces.containsKey(entry.getKey().toEventString())){
				trueTraces.put(entry.getKey().toEventString(),entry.getKey());
			}
		});
		
		int maxTraceLength = Integer.MIN_VALUE;
		for(Trace t : trueTraces.values()){
			if(t.length() > maxTraceLength){
				maxTraceLength = t.length();
			}
		}
		
		TraceSimulator simulator = new TraceSimulator(petriNet);
		Collection<Trace> simulatedTraces = simulator.simulate(runs, maxTraceLength, transFireLimits);
		
		for(Trace sim : simulatedTraces){
			if(sim != null){
				traceBuffer.put(sim.toEventString(), sim);
			}
		}	
			
		int denom = traceBuffer.size();
		int nom = 0;
		
		System.out.println("unique true traces " + trueTraces.keySet().size());
		System.out.println("unique sample traces " + traceBuffer.keySet().size());
		for(String sim : traceBuffer.keySet()){
			if(trueTraces.containsKey(sim)){
				nom++;
			}				
		}
		System.out.println("Nominator " + nom);
		
		return ((double) nom) / denom;
	}
}
package edu.uic.prominent.processmining.decaypns.pnmetrics;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.model.impl.XAttributeTimestampImpl;

import edu.uic.prominent.processmining.decaypns.log.util.Event;
import edu.uic.prominent.processmining.decaypns.log.util.Trace;
import edu.uic.prominent.processmining.decaypns.misc.parallelize.DeepCopy;
import edu.uic.prominent.processmining.decaypns.petrinet.PetriNet;
import edu.uic.prominent.processmining.decaypns.petrinet.Transition;
import edu.uic.prominent.processmining.decaypns.petrinet.util.TransitionFiringCountLimitation;

public class TraceSimulator {
	private PetriNet petriNet;
	private Random random;

	private Map<Integer, Trace> traceBuffer;

	public TraceSimulator(PetriNet petriNet) {
		this.petriNet = petriNet;
		this.random = new Random();
		this.traceBuffer = new HashMap<Integer, Trace>();
	}

	public Collection<Trace> simulate(int runs, HashMap<String, TransitionFiringCountLimitation> transFireLimits) {
		return simulation(runs, Integer.MAX_VALUE, transFireLimits);
	}

	public Collection<Trace> simulate(int runs, int maxTraceLength, HashMap<String, TransitionFiringCountLimitation> transFireLimits) {
		return simulation(runs, maxTraceLength, transFireLimits);
	}

	private Collection<Trace> simulation(int runs, int maxTraceLength, HashMap<String, TransitionFiringCountLimitation> transFireLimits) {
		List<String> buffer = new ArrayList<String>(Arrays.asList(new String[runs]));
		buffer.parallelStream().forEach((str) -> {
			PetriNet parallel_pn = (PetriNet) DeepCopy.copy(this.petriNet);
			@SuppressWarnings("unchecked")
			HashMap<String, TransitionFiringCountLimitation> fireLimits = (HashMap<String, TransitionFiringCountLimitation>) DeepCopy.copy(transFireLimits);
			Trace tr = createTrace(parallel_pn, maxTraceLength, fireLimits, buffer.indexOf(str));
			if (tr != null)
				traceBuffer.put(System.identityHashCode(tr), tr);
		});
		return this.traceBuffer.values();
	}

	public void toCSV(String file) {
		try {
			PrintWriter writer = new PrintWriter("logs/" + file, "UTF-8");
			int c = 1;
			writer.println("Activity,Timestmap,Case");
			for (Trace t : traceBuffer.values()) {
				if(t != null){
					for (Event e : t.trace) {
						writer.println(e.name() + "," + e.time() + "," + c);
					}
					c++;
				}
			}
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Trace createTrace(PetriNet pn, int maxTraceLength, HashMap<String, TransitionFiringCountLimitation> transFireLimits, int caseId) {
		Trace trace = new Trace(caseId, 0L);
		try {
			pn.cleanUpRemainingTokens();
			pn.addStartTokens();

			long time = 0L;
			while (!pn.finalMarkingReached()) {
				// System.out.println("MaxTraceLengthReached");
				if (trace.length() >= maxTraceLength) {
					break;
				}

				List<Transition> transitions = pn.enabledTransitions();

				if (transitions == null) {
					break;
				}

				// Transition Firing Count Limitation --->
				if(transFireLimits != null){
					for(TransitionFiringCountLimitation limit : transFireLimits.values()){
						Transition del = null;
						if (limit.getCount() >= limit.getLimitation()) {
							for (Transition t : transitions) {
								if (t != null) {
									if (t.name().equals(limit.getTransition())) {
										del = t;
										break;
									}
								}
							}
							transitions.remove(del);
						}
					}
				}
				// Transition Firing Count Limitation
				

				int fire = 0;
				if (transitions.size() > 1) {
					fire = random.nextInt(transitions.size());
				}
				Transition t = null;
				try {
					t = transitions.get(fire);
					
					if(transFireLimits.containsKey(t.name())){
						transFireLimits.get(t.name()).increment();
					}
				} catch (Exception e) {
					t = null;
				}

				if (t == null) {
					break;
				}

				t.consumeInputTokens();
				t.produceOutputTokens();
				if (t.isVisible()) {
					time = time + 1L;
					trace.addEvent(
							new Event(t.name(), new XAttributeTimestampImpl(XTimeExtension.KEY_TIMESTAMP, time)));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return trace;
	}

}

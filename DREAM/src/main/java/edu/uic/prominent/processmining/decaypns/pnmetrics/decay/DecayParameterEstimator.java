package edu.uic.prominent.processmining.decaypns.pnmetrics.decay;

import edu.uic.prominent.processmining.decaypns.petrinet.Transition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import java.util.Set;

import edu.uic.prominent.processmining.decaypns.log.EventLog;
import edu.uic.prominent.processmining.decaypns.log.util.Event;
import edu.uic.prominent.processmining.decaypns.log.util.Trace;
import edu.uic.prominent.processmining.decaypns.petrinet.Place;

public class DecayParameterEstimator {
	final static Logger logger = Logger.getLogger(DecayParameterEstimator.class);
	
	private Map<String, Set<Place>> transition2places;

	private Map<Place, List<Tuple<Integer, Long>>> activations;
	private Map<Place, List<Tuple<Integer, Double>>> deltaActivations;

	private Map<Place, EstimationStatistic> estimatedParameter;
	private EventLog eventLog;

	private long maxDur = Long.MIN_VALUE;
	private long minDur = Long.MAX_VALUE;

	public DecayParameterEstimator(List<Transition> transitions, EventLog eventLog) {
		transition2places = new HashMap<String, Set<Place>>();
		estimatedParameter = new HashMap<Place, EstimationStatistic>();

		activations = new HashMap<Place, List<Tuple<Integer, Long>>>();
		deltaActivations = new HashMap<Place, List<Tuple<Integer, Double>>>();

		for (Transition t : transitions) {
			transition2places.put(t.name(), t.getOutputs());
			Set<Place> plcs = t.getOutputs();
			for (Place p : plcs) {
				if (!activations.containsKey(p)) {
					activations.put(p, new ArrayList<Tuple<Integer, Long>>());
					deltaActivations.put(p, new ArrayList<Tuple<Integer, Double>>());
				}
			}
		}
		this.eventLog = eventLog;

		estimate();
	}
	
	public Map<Place, EstimationStatistic> getEstimatedParameters(){
		return estimatedParameter;
	}

	private void estimate() {
		Iterator<Entry<Trace, Integer>> traceIter = this.eventLog.getLog().iterator();

		int trace = 0;
		while (traceIter.hasNext()) {
			Trace t = traceIter.next().getKey();

			Iterator<Event> eventIter = t.trace.iterator();

			long min = Long.MAX_VALUE;
			long max = Long.MIN_VALUE;

			while (eventIter.hasNext()) {
				Event event = eventIter.next();

				if (event.time() > max) {
					max = event.time();
				}

				if (event.time() < min) {
					min = event.time();
				}

				Set<Place> activePlaces = transition2places.get(event.name());
				// RECHECK THIS
				if(activePlaces != null){
					for (Place p : activePlaces) {
							activations.get(p).add(new Tuple<Integer, Long>(trace, event.time()));
							activations.put(p, activations.get(p));
					}
				}
				// ---->
			}

			if ((max - min) > this.maxDur) {
				this.maxDur = (max - min);
			}
			if ((max - min) < this.minDur) {
				this.minDur = (max - min);
			}
			trace++;
		}

		for (Place key : activations.keySet()) {
			int currTpl = -1;
			List<Tuple<Integer, Long>> actLst = activations.get(key);
			long prevTime = 0L;
			double delta = -1.0;
			for (Tuple<?, ?> tpl : actLst) {
				if (currTpl != (int) tpl.x) {
					currTpl = (int) tpl.x;
					delta = -1.0;
					prevTime = (long) tpl.y;
				} else {
					delta = new Long((long) tpl.y - prevTime).doubleValue();
				}
				prevTime = (long) tpl.y;
				deltaActivations.get(key).add(new Tuple<Integer, Double>(currTpl, delta));
				deltaActivations.put(key, deltaActivations.get(key));
			}
		}

		for (Place key : deltaActivations.keySet()) {
			List<Tuple<Integer, Double>> values = deltaActivations.get(key);

			List<Double> vec = new ArrayList<Double>();
			for (Tuple<?, ?> t : values) {
				if ((double) t.y != -1.0) {
					vec.add((double) t.y);
				}
			}
			EstimationStatistic stat = new EstimationStatistic();
			stat.max = this.maxDur;
			stat.min = this.minDur;
			stat.mean = mean(vec);
			stat.stddev = sd(vec);
			stat.calculateParameter();
			estimatedParameter.put(key, stat);
		}

	}

	private double mean(List<Double> vec) {
		double mean = 0.0;
		for (Double d : vec) {
			mean += d;
		}
		if (vec.size() > 0)
			return (mean / vec.size());
		else
			return -1.0;
	}

	public double sd(List<Double> vec) {
		if (vec.size() > 0) {
			double mean = mean(vec);
			double temp = 0;
			for (int i = 0; i < vec.size(); i++) {
				temp = Math.pow(i - mean, 2);
			}
			temp = temp / vec.size();
			return Math.sqrt(temp);
		} else {
			return -1.0;
		}
	}

	public class Tuple<X, Y> {
		public final X x;
		public final Y y;

		public Tuple(X x, Y y) {
			this.x = x;
			this.y = y;
		}
	}
}

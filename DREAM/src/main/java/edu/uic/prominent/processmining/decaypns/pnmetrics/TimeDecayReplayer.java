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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import edu.uic.prominent.processmining.decaypns.log.EventLog;
import edu.uic.prominent.processmining.decaypns.log.util.Trace;
import edu.uic.prominent.processmining.decaypns.petrinet.PetriNet;
import edu.uic.prominent.processmining.decaypns.petrinet.Place;
import edu.uic.prominent.processmining.decaypns.petrinet.Transition;
import edu.uic.prominent.processmining.decaypns.pnmetrics.decay.DecayVector;
import edu.uic.prominent.processmining.decaypns.pnmetrics.decay.DecayVectorPrediction;
import edu.uic.prominent.processmining.decaypns.pnmetrics.decay.EstimationStatistic;
import edu.uic.prominent.processmining.decaypns.pnmetrics.util.ConformanceParameters;
import edu.uic.prominent.processmining.decaypns.pnmetrics.util.Constants;
import edu.uic.prominent.processmining.decaypns.misc.astar.AStarInvisible;
import edu.uic.prominent.processmining.decaypns.misc.astar.Node;
import edu.uic.prominent.processmining.decaypns.misc.parallelize.DeepCopy;


/**
 * TimeDecayReplayer class utilizing A* algorithm to find hidden transitions. 
 * This replayer is used to create Decay Vectors while replaying.
 * This class outputs decay vectors (DecayVector) at every timestamp t during replay as well as a supervised datasets with (DecayVector, Next Event) based on the DecayVectorPrediction class.
 * The initial setup has been created by Erko Rishtein: https://github.com/ErkoRisthein/conformance-checker/
 * 
 * @author Julian
 *
 */
public class TimeDecayReplayer {
	final static Logger logger = Logger.getLogger(TimeDecayReplayer.class);
	
	private int controlTokens;
	private PetriNet petriNet;
	private EventLog eventLog;

	private String name;
	
	private DecayVector decayVec;
	private ArrayList<DecayVectorPrediction> decayVecPreds;

	private DecayVectorPrediction decayPred;
	Map<Transition, Long> enabledTransitions;
	List<ConformanceParameters> conformanceParams = new ArrayList<>();
	
	private String predictionTimeBase;

	public TimeDecayReplayer(String name, PetriNet petriNet, EventLog eventLog, int controlTokens, Map<Place, EstimationStatistic> estParam, String predictionTimeBase) {
		this.name = name;
		this.petriNet = petriNet;
		this.eventLog = eventLog;
		this.controlTokens = controlTokens;

		this.decayVec = new DecayVector(petriNet.getPlaces(), estParam);
		this.decayVecPreds = new ArrayList<DecayVectorPrediction>();
		
		this.predictionTimeBase = predictionTimeBase;
	}
	
	public TimeDecayReplayer(String name, PetriNet petriNet, EventLog eventLog, int controlTokens, String predictionTimeBase) {
		this.name = name;
		this.petriNet = petriNet;
		this.eventLog = eventLog;
		//this.aStar = new AStarSearch(this.petriNet);
		//this.aStarInvisible = new AStarInvisible(this.petriNet);
		this.controlTokens = controlTokens;

		this.decayVec = new DecayVector(petriNet.getPlaces());
		this.decayVecPreds = new ArrayList<DecayVectorPrediction>();
		this.predictionTimeBase = predictionTimeBase;
	}

	public void replayLog() {
		//eventLog.forEach((entry) -> {
			
		for(Entry<Trace, Integer> entry : eventLog.getLog()) //.traceCounts.entrySet())
		{
			
			PetriNet parallel_pn = (PetriNet) DeepCopy.copy(this.petriNet);
			AStarInvisible parallel_astar = new AStarInvisible(parallel_pn);
			
			this.decayVec.reset();
			ConformanceParameters params = new ConformanceParameters(entry.getKey(), entry.getValue());

			replay(parallel_pn, parallel_astar, params);
			conformanceParams.add(params);
		}
	}
	
	private void replay(PetriNet petriNet,AStarInvisible aStarInvisible, ConformanceParameters params) {
		addStartToken(petriNet, params);	
		long initialTime = 0L;
		this.addEnabledTransitions(initialTime);
				
		this.decayPred = new DecayVectorPrediction(this.decayVec.getVector(), this.decayVec.getCounterVector(), petriNet.getMarking(), null, initialTime, -1, new ArrayList<String>());
			
		replayEvents(petriNet, aStarInvisible, params);
		consumeEndToken(petriNet, params);
		setRemainingTokens(petriNet, params);
		
	}
	
	/**
	 * Counts the remaining tokens in the PetriNet after a trace has been replayed.
	 * Afterwards, the remaining tokens are cleaned up such that the PN has zero tokens.
	 * @param params ConformanceParameters keeping track of the token movements
	 */
	private void setRemainingTokens(PetriNet petriNet, ConformanceParameters params) {
		int remainingTokens = petriNet.countRemainingTokens();
		params.setRemaining((remainingTokens - controlTokens));
		petriNet.cleanUpRemainingTokens();
	}

	
	/**
	 * Consumes the end token. If the final marking has not been reached, it increments the number of missing tokens by figuring out
	 * how many tokens are missing. Then, the according missing end Tokens are added.
	 * 
	 * Afterwards (and if the final marking has been reached), the end tokens will be removed and consumed.
	 * @param params
	 */
	private void consumeEndToken(PetriNet petriNet, ConformanceParameters params) {
		if (!petriNet.finalMarkingReached()) {	
			//System.out.println("Missing: " + (petriNet.finalMarkingTokens() - petriNet.tokensInFinalMarkingPosition()));
			params.incrementMissing((petriNet.finalMarkingTokens() - petriNet.tokensInFinalMarkingPosition()));
			petriNet.addEndToken();
		}
		
		petriNet.removeEndToken();
		params.incrementConsumed(petriNet.tokensInFinalMarkingPosition());
	}
	
	private List<Transition> reachTransitionInvisibly(PetriNet petriNet, AStarInvisible aStarInvisible, Transition targetTrans) {
		List<Transition> returnList = null;

		List<Place> starts = petriNet.getPlacesWithToken();
		Set<Place> targets = targetTrans.getInputs();
		boolean allReached = true;
		
		returnList = new ArrayList<Transition>();
		
		for(Place target : targets){
			if(!target.hasTokens()){

				for(Place start : starts){
					List<Node> invisiblePath = aStarInvisible.searchPath(start.name(), target.name());
					for (Node n : invisiblePath) {
						if (n.isTransition) {
							returnList.add(petriNet.getTransition(n.value));
						}
					}
					if (invisiblePath.size() <= 1)
						allReached = false;
				}
			}
		}
		
		if(allReached)
			return returnList;
		else
			return null;
	}
	
	private void replayEvents(PetriNet petriNet, AStarInvisible aStarInvisible, ConformanceParameters params) {
		params.trace().forEach(event -> {
			this.decayPred.setEvent(event.name());
			
			//System.out.println("TimeDecayReplayer " + event.resources().size());
			
			/*
			 * Get Time, add time, update the vector!
			 */
			if(this.predictionTimeBase == Constants.DECAYVECTORPREDICTION_TIMEBASE_Te){
				this.decayVec.moveTime(event.time());
			}
						
			ArrayList<String> cloned = new ArrayList<String>();
			for(String p : this.decayPred.resources)
				cloned.add(p);
			this.decayVecPreds.add(new DecayVectorPrediction(this.decayPred.vector.clone(), this.decayPred.vectorCounter.clone(), this.decayPred.marking.clone(), this.decayPred.event, 0L, event.getCaseId(), cloned));

			long time = event.time();
			Map<Place, Integer> before = petriNet.getPlacesAndTokens();
			
			Transition transition = petriNet.getTransition(event.name());
			
			// Falls die Transition nicht aktiviert ist, pruefe ob man mit Invisible transitions die 
			// token fuer die praemisse erstellen kann
			if (!transition.hasAllInputTokens()) {

				List<Transition> invisibleTransitions = reachTransitionInvisibly(petriNet, aStarInvisible, transition);					
				if (invisibleTransitions != null) {
					//System.out.println("Reachable by invisible: " + transition.name());
					// it is reachable by invisible path, so put token out
					// output!
									
					for(Transition t : invisibleTransitions){
						Set<Place> inPlaces = t.getInputs();
						if(petriNet.placesHaveTokens(inPlaces)){
							consumeInputTokens(petriNet, t, params);
							produceOutputTokens(petriNet, t, params);
						}
					}
				}
			}
			
			if (!transition.hasAllInputTokens()) {
				createMissingTokens(petriNet, transition, params);
			}else{
				consumeInputTokens(petriNet, transition, params);
				produceOutputTokens(petriNet, transition, params);
			}
			
			Map<Place, Integer> after = petriNet.getPlacesAndTokens();
			List<Place> newPlaces = activatedPlaces(before, after, transition);
			for (Place p : newPlaces) {
				this.decayVec.placeActivation(p, time);
			}
			
			if(event.resources().size() > 0){
				for(int a = 1; a <= event.resources().size(); a++){
					this.decayPred.addResource(event.resources().get("Resource" + a));
				}
			}
			
			cloned = new ArrayList<String>();
			for(String p : this.decayPred.resources)
				cloned.add(p);
			this.decayPred = new DecayVectorPrediction(this.decayVec.getVector(), this.decayVec.getCounterVector(), petriNet.getMarking(), null, time, -1, cloned);
		});
	}
	
	private List<Place> activatedPlaces(Map<Place, Integer> before, Map<Place, Integer> after, Transition transition) {
		List<Place> lst = new ArrayList<Place>();

		for (Place a : after.keySet()) {
			boolean found = false;
			int tokens = after.get(a).intValue();

			for (Place b : before.keySet()) {
				if (b.name().equals(a.name()) && tokens == before.get(b).intValue()) {
					found = true;
					break;
				}
			}

			if (!found) {
				lst.add(a);
			}
		}
		
		//Add self looping ones as newly activated
		for (Place a : transition.getOutputs()) {
			if(!lst.contains(a)){
				lst.add(a);
			}
		}

		return lst;
	}

	private void createMissingTokens(PetriNet petriNet, Transition transition, ConformanceParameters params) {
		Set<Place> inPs = transition.getInputs();
		int missing = 0;
		for(Place p : inPs){
			if(p.getTokenCount() == 0){
				missing++;
				petriNet.addToken(p);
			}
		}
		params.incrementMissing(missing);
		consumeInputTokens(petriNet, transition, params);
		produceOutputTokens(petriNet, transition, params);
	}

	// VERIFIED @ 18-10-22
	private void produceOutputTokens(PetriNet petriNet, Transition transition, ConformanceParameters params) {
		transition.produceOutputTokens();
		params.incrementProduced(transition.getOutputs().size());
	}
	// -->

	// VERIFIED @ 18-10-22
	private void consumeInputTokens(PetriNet petriNet, Transition transition, ConformanceParameters params) {
		transition.consumeInputTokens();
		params.incrementConsumed(transition.getInputs().size());
	}
	// -->

	// VERIFIED @ 18-10-22
	private void addStartToken(PetriNet petriNet, ConformanceParameters params) {
		petriNet.addStartTokens();
		params.incrementProduced(petriNet.startTokens());
	}
	// -->

	
	private void addEnabledTransitions(Long eventTime) {
		List<Transition> newlyEnabled = petriNet.enabledTransitions();
		for (Transition t : newlyEnabled) {
			enabledTransitions.put(t, eventTime);
		}
	}
	
	
	public void createDataset(String suffix){
		
		/// COUNT NUMBER OF ; and add accordingly
		
		List<String> data = new ArrayList<String>();		
		for (DecayVectorPrediction pred : decayVecPreds) {
			data.add(pred.toString());
		}
		
		int max_sep = 0;
		for (String str : data) {
			int cnt = StringUtils.countMatches(str, ";");
			if(cnt > max_sep)
				max_sep = cnt;
		}
		
		List<String> dataset = new ArrayList<String>();
		for (String str : data) {
			int fill = max_sep - StringUtils.countMatches(str, ";");
			for(int i = 0; i < fill; i++){
				str += ";";
			}
			dataset.add(str);
		}	
		
		//List<String> dataset = new ArrayList<String>(data);	
		try {
			File fouttrain = new File("decaydata/" + this.name + "_" + suffix + ".csv");
			FileOutputStream fostrain = new FileOutputStream(fouttrain);
			BufferedWriter bwtrain = new BufferedWriter(new OutputStreamWriter(fostrain));
			for (String str : dataset) {
					bwtrain.write(str);
					bwtrain.newLine();
			}
			bwtrain.close();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}
}


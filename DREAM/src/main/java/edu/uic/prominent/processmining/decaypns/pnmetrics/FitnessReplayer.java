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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import edu.uic.prominent.processmining.decaypns.log.EventLog;
import edu.uic.prominent.processmining.decaypns.petrinet.PetriNet;
import edu.uic.prominent.processmining.decaypns.petrinet.Place;
import edu.uic.prominent.processmining.decaypns.petrinet.Transition;
import edu.uic.prominent.processmining.decaypns.pnmetrics.util.ConformanceParameters;
import edu.uic.prominent.processmining.decaypns.misc.astar.AStarInvisible;
import edu.uic.prominent.processmining.decaypns.misc.astar.Node;
import edu.uic.prominent.processmining.decaypns.misc.parallelize.DeepCopy;

/**
 * Replayer class utilizing A* algorithm to find hidden transitions. 
 * Can be used to calculate the fitness of a log and to calculate the structural appropriateness.
 * The initial setup has been created by Erko Rishtein: https://github.com/ErkoRisthein/conformance-checker/
 * 
 * @author Julian
 *
 */
public class FitnessReplayer {
	
	private int controlTokens;
	private PetriNet petriNet;
	private EventLog eventLog;
	//private AStarSearch aStar;
	//private AStarInvisible aStarInvisible;
	
	List<ConformanceParameters> conformanceParams = new ArrayList<>();

	/**
	 * Creates a Replayer instance. Replayer can be used to calculate the Fitness of a log as well as the structural appropriateness of a Petri Net
	 * @param petriNet of class PetriNet 
	 * @param eventLog of class EventLog
	 * @param controlTokens Integer number of Control Tokens of the Petri Net
	 */
	public FitnessReplayer(PetriNet petriNet, EventLog eventLog, int controlTokens) {
		this.controlTokens = controlTokens;
		this.petriNet = petriNet;
		this.eventLog = eventLog;
		//this.aStar = new AStarSearch(this.petriNet);		
		//this.aStarInvisible = new AStarInvisible(this.petriNet);
	}

	/**
	 * Calculate the fitness value given an event log
	 * @return double Fitness value
	 */
	public HashMap<String, Double> getFitness() {
		HashMap<String, Double> fitness = new HashMap<String, Double>();
		replayLog();
		fitness.put("MaxFitnessRatio", maxFitnessRatio());
		fitness.put("Fitness", calculateFitness());
		return fitness;
	}

	/**
	 * Calculates the structural appropriateness based on the number of Transitions as well as the number of places.
	 * @return double Structural Appropriateness
	 */
	public double getSimpleStructuralAppropriateness() {
		double L = petriNet.countTransitions();
		double N = L + petriNet.countPlaces();
		return (L + 2) / N;
	}

	/**
	 * Replay the log. Take each trace and unique trace count and replay it.
	 * Creates Conformance Parameters for each unique trace.
	 * Conformance Parameters are being updated while replaying.
	 * Conformance Parameters are added to the list of all obtained conformance Parameters
	 */
	private void replayLog() {
		/*
		eventLog.forEach((trace, count) -> {
			ConformanceParameters params = new ConformanceParameters(trace, count);
			replay(params);
			//System.out.println(params.toString());
			conformanceParams.add(params);
		});
		*/
		
		eventLog.forEach((entry) -> {
				PetriNet parallel_pn = (PetriNet) DeepCopy.copy(this.petriNet);
				AStarInvisible parallel_astar = new AStarInvisible(parallel_pn);
	
				//System.out.println(System.identityHashCode(parallel_pn));
				ConformanceParameters params = new ConformanceParameters(entry.getKey(), entry.getValue());
				replay(parallel_pn, parallel_astar, params);
				//System.out.println(params.toString());
				conformanceParams.add(params);
		});
	}

	/**
	 * Initial replay function call. Adds Start token, then calls replay events. As soon as all events of the trace are replayed,
	 * the end tokens will be consumed and remaining tokens are counted.
	 * @param params ConformanceParameters which take track of the token movements
	 */
	private void replay(PetriNet petriNet,AStarInvisible aStarInvisible, ConformanceParameters params) {
		addStartToken(petriNet, params);	
		//System.out.println(petriNet.getState());
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
			
			//System.out.println(this.petriNet.getState());
			//System.out.println("Incoming Event: " + event.name());
			
			//System.out.println(" ------- Incoming Event " + event.name());
			Transition transition = petriNet.getTransition(event.name());
			
			// Falls die Transition nicht aktiviert ist, pruefe ob man mit Invisible transitions die 
			// token fuer die praemisse erstellen kann
			if (!transition.hasAllInputTokens()) {
				//System.out.println("Not Enabled " + transition.name());

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
			
			//System.out.println(this.petriNet.getState());
			//System.out.println("---------------------");
		});
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
			
			/*
			// how far do we have to go? Then, increment missing by how far we
			// had to go!
			List<Place> currentTokens = petriNet.getPlacesWithToken();

			Place tokenToMove = null;
			int minDistance = Integer.MAX_VALUE;
			for (Place token : currentTokens) {
				Place[] targetPlaces = transition.getInputs().toArray(new Place[transition.getInputs().size()]);
				Place targetPlace = targetPlaces[0];

				int incrementBy = aStar.search(token.name(), targetPlace.name());

				if (incrementBy < minDistance) {
					minDistance = incrementBy;
					tokenToMove = token;
				}
				// CONSIDER MULTIPLE INPUTS REQUIRED FOR A TRANSITION!!!!!!!!
			}

			if (minDistance > 0) {
				// petriNet.cleanUpRemainingTokens();
				petriNet.removeToken(tokenToMove);
				// System.out.println("Create Missing Token " +
				// transition.name() +
				// " incrementBy " + incrementBy );
				transition.createMissingToken();
				params.incrementMissing(minDistance);
				System.out.println("Missing: " + minDistance);
			}
			*/
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

	private double calculateFitness() {
		double missing = 0;
		double remaining = 0;
		double consumed = 0;
		double produced = 0;

		for (ConformanceParameters p : conformanceParams) {
			if(p != null){
				missing += p.count() * p.missing();
				remaining += p.count() * p.remaining();
				consumed += p.count() * p.consumed();
				produced += p.count() * p.produced();
			}
			// System.out.println(p.count());
		}

		// System.out.println("Missing: " + missing);
		// System.out.println("remaining: " + remaining);
		// System.out.println("consumed: " + consumed);
		// System.out.println("produced: " + produced);

		return 0.5 * (1 - (missing / consumed)) + 0.5 * (1 - (remaining / produced));
	}
	
	private double maxFitnessRatio() {
		double missing = 0;
		double remaining = 0;
		double consumed = 0;
		double produced = 0;
		double fitness = 0;
		
		int numTracesWithMaxFitness = 0;

		for (ConformanceParameters p : conformanceParams) {
			if(p != null){
				if(!p.isNull()){
					missing = p.count() * p.missing();
					remaining = p.count() * p.remaining();
					consumed = p.count() * p.consumed();
					produced = p.count() * p.produced();
					fitness = 0.5 * (1 - (missing / consumed)) + 0.5 * (1 - (remaining / produced));
				}else{
					fitness = 0.0;
				}
				
				if(fitness == 1.0)
					numTracesWithMaxFitness++;
			}
		}
		return ((double) numTracesWithMaxFitness / conformanceParams.size());
	}
}


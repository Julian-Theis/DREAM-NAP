package edu.uic.prominent.processmining.decaypns;

import java.util.List;

import edu.uic.prominent.processmining.decaypns.eval.Predictions;
import edu.uic.prominent.processmining.decaypns.petrinet.PetriNet;
import edu.uic.prominent.processmining.decaypns.petrinet.Transition;
import edu.uic.prominent.processmining.decaypns.pntools.PetriNetParser;
import edu.uic.prominent.processmining.decaypns.util.BenchmarkDataset;

public class PredictionCheck {

	public static void main(String[] args) {
		String benchmark = "helpdesk";
		String predFile = "predictions//predictions.csv";
		int fold = 0;
		BenchmarkDataset dataset = new BenchmarkDataset();
		
		String petriNetFileName = dataset.getFileNameOfPn(benchmark, 2, fold);
		
		PetriNetParser pnParser = new PetriNetParser();
		PetriNet petriNet = pnParser.getPetriNetFromFile(petriNetFileName);
		
		double[] currentMarking = petriNet.getMarking();

		
		Predictions predictions = new Predictions(predFile);
		
		for(int i = 0; i < predictions.size(); i++) {
			double[] marking = predictions.getMarking(i);
			int trueLabel = predictions.getTrueLabel(i) + 1;
			int predictedLabel = predictions.getPredictedLabel(i) + 1;
			
			petriNet.setMarking(marking);
			
			Transition t = petriNet.getTransition(trueLabel + "");
			
			boolean isEnabled = petriNet.isTransitionEnabled(t);
			System.out.println("Is Enabled? "  + isEnabled);
			
			System.out.println("********");
			
			/* MARKINGS ARE THE SAME !!!
			double[] pnMark = petriNet.getMarking();
			boolean isEqual = true;
			for(int a = 0; a < marking.length; a++) {
				if(marking[a] != pnMark[a]) {
					isEqual = false;
				}
			}
			System.out.println(isEqual);
			*/
		}
	}
}

package edu.uic.prominent.processmining.decaypns;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.HashMap;

import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;

import com.raffaeleconforti.context.FakePluginContext;
import com.raffaeleconforti.conversion.bpmn.BPMNToPetriNetConverter;
import com.raffaeleconforti.conversion.petrinet.PetriNetToBPMNConverter;
import com.raffaeleconforti.marking.MarkingDiscoverer;
import com.raffaeleconforti.wrappers.PetrinetWithMarking;

import au.edu.qut.processmining.miners.splitminer.SplitMiner;
import au.edu.qut.processmining.miners.splitminer.ui.dfgp.DFGPUIResult;
import au.edu.qut.processmining.miners.splitminer.ui.miner.SplitMinerUIResult;
import edu.uic.prominent.processmining.decaypns.log.XLogLoader;
import edu.uic.prominent.processmining.decaypns.prom.model.ModelUtils;



public class PNDiscovery {

	private static String logname = "logs/cyclic_880_cased.csv";
	private static String outputlog = "cyclic_880_cased.xes";
	
	private static String name = "cyclic_880_cased";
	
	public static void main(String[] args) {
		double p1 = 0.0;
		double p2 = 0.5;
		
		HashMap<String, Integer> logConfiguration = new HashMap<String, Integer>();		
		logConfiguration.put("Timestamp", 0);
		logConfiguration.put("Activity", 1);
		logConfiguration.put("Case", 2);
		
		XLogLoader logLoader = new XLogLoader(logConfiguration);
		XLog log = logLoader.importCSV(logname, true, false, "yyyy/MM/dd HH:mm:s.S");
		logLoader.saveAsXES(outputlog);
		
		PetrinetWithMarking pn = testMiner(p1, p2, log, 2);
		ModelUtils.exportPetrinet(new FakePluginContext(), pn,
				"models/" + name + "_" + 2 + "_" + p1 + "_" + p2 + "_model.pnml");
	}
	
	/* 
	 * The subsequent functions are part of the library "ResearchCode" of Raffaele Conforti
	 * https://github.com/raffaeleconforti/ResearchCode
	 * 
	 * Copyright (C) 2019 Raffaele Conforti
	 * The code is available as open source code; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
	 * You should have received a copy of the GNU Lesser General Public License along with this it. If not, see http://www.gnu.org/licenses/lgpl-3.0.html.
	 */
	
	private static PetrinetWithMarking testMiner(double p1, double p2, XLog log, int miner) {//, String logname){
		FakePluginContext context = new FakePluginContext();
		SplitMiner yam = new SplitMiner();
        BPMNDiagram bpmn = null;
        PetrinetWithMarking petrinet = null;
        XEventNameClassifier xEventClassifier = new XEventNameClassifier();
        
		Double f_threshold = p1; //percentileFrequencyThreshold  both 0.3 before
		Double p_threshold = p2; //parallelismsThreshold
		
		bpmn = yam.mineBPMNModel(log, xEventClassifier, f_threshold, p_threshold, DFGPUIResult.FilterType.WTH, true, true, false, SplitMinerUIResult.StructuringTime.NONE);
		petrinet = convertToPetrinet(context.getRootContext(), bpmn);

		return petrinet;
	}
	
	private static PetrinetWithMarking convertToPetrinet(UIPluginContext context, BPMNDiagram diagram) {
		Object[] result = BPMNToPetriNetConverter.convert(diagram);

		if (result[1] == null)
			result[1] = PetriNetToBPMNConverter.guessInitialMarking((Petrinet) result[0]);
		if (result[2] == null)
			result[2] = PetriNetToBPMNConverter.guessFinalMarking((Petrinet) result[0]);

		if (result[1] == null)
			result[1] = MarkingDiscoverer.constructInitialMarking(context, (Petrinet) result[0]);
		else
			MarkingDiscoverer.createInitialMarkingConnection(context, (Petrinet) result[0], (Marking) result[1]);

		if (result[2] == null)
			result[2] = MarkingDiscoverer.constructFinalMarking(context, (Petrinet) result[0]);
		else
			MarkingDiscoverer.createFinalMarkingConnection(context, (Petrinet) result[0], (Marking) result[1]);
		System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));

		return new PetrinetWithMarking((Petrinet) result[0], (Marking) result[1], (Marking) result[2]);
	}
}

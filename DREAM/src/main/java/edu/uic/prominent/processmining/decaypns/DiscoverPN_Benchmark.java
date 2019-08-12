package edu.uic.prominent.processmining.decaypns;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.model.XLog;
import org.json.JSONObject;
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
import edu.uic.prominent.processmining.decaypns.log.EventLog;
import edu.uic.prominent.processmining.decaypns.log.XLogLoader;
import edu.uic.prominent.processmining.decaypns.petrinet.PetriNet;
import edu.uic.prominent.processmining.decaypns.pnmetrics.FitnessReplayer;
import edu.uic.prominent.processmining.decaypns.pntools.PetriNetParser;
import edu.uic.prominent.processmining.decaypns.prom.model.ModelUtils;
import edu.uic.prominent.processmining.decaypns.util.BenchmarkDataset;

public class DiscoverPN_Benchmark {

	final static Logger logger = Logger.getLogger(DiscoverPN_Benchmark.class);

	public static void main(String[] args) {
		try {
			String benchmarkName = args[0];
			boolean mineAndValidatePN = Boolean.parseBoolean(args[1]);

			int k = 10;
			int algorithm = 2;
			double param_step = 0.1;

			double param1_min = 0.0;
			double param1_max = 1.0;
			double param2_min = 0.0;
			double param2_max = 1.0;
			// -------------->

			BenchmarkDataset datasets = new BenchmarkDataset();
			EventLog log = datasets.getLog(benchmarkName);
			log.setOrdering(datasets.benchmarkMetadata.get(benchmarkName).get("ordering"));
			log.kFoldCrossValidationSplit(benchmarkName, k);

			if (mineAndValidatePN) {
				JSONObject json = new JSONObject();

				for (int i = 0; i < k; i++) {
					logger.info("Cross Validation - Fold " + i);
					datasets.getLogCVTrain(benchmarkName, i);
					datasets.getLogCVTest(benchmarkName, i);

					XLogLoader logLoader = new XLogLoader();
					XLog xlog = logLoader
							.importXES(benchmarkName + "/" + benchmarkName + "_kfoldcv_" + i + "_train.xes");

					for (double param1 = param1_min; param1 <= param1_max; param1 += param_step) {
						for (double param2 = param2_min; param2 <= param2_max; param2 += param_step) {
							param1 = round(param1, 1);
							param2 = round(param2, 1);
							mineModel(param1, param2, algorithm, benchmarkName, xlog, i);
							logger.info("Mined PetriNet with parameters " + param1 + " and " + param2);
						}
					}
					double bestScore = 0.0;
					Tuple bestParamSet = new Tuple(0.0, 0.0);

					
					for (double param1 = param1_min; param1 <= param1_max; param1 += param_step) {
						for (double param2 = param2_min; param2 <= param2_max; param2 += param_step) {
							param1 = round(param1, 1);
							param2 = round(param2, 1);

							// Calculate fitness
							String petriNetFileName = benchmarkName + "_" + algorithm + "_kfoldcv_" + i + "_" + param1
									+ "_" + param2 + "_model.pnml";
							PetriNetParser pnParser = new PetriNetParser();
							
							PetriNet petriNet = pnParser.getPetriNetFromFile(petriNetFileName);
							int controlTokens = 0;
							
							FitnessReplayer replay = new FitnessReplayer(petriNet, log, controlTokens);
							
							HashMap<String, Double> fitnessMap = replay.getFitness(); // --- problem in here !! >
							
							logger.info("Fold " + i + ": Fitness of " + param1 + " and " + param2 + " : "
									+ fitnessMap.get("Fitness"));
	
							if (fitnessMap.get("Fitness") > bestScore) {
								bestScore = fitnessMap.get("Fitness");
								bestParamSet = new Tuple(param1, param2);
							}
						
						}
					}
					

					JSONObject params = new JSONObject();
					params.put("fitness", bestScore);
					params.put("param1", bestParamSet.x);
					params.put("param2", bestParamSet.y);
					json.put(i + "", params);
				}

				logger.info(json.toString());
				datasets.writeCVMinedPetriNetParameters(benchmarkName, json);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	public static double round(double value, int places) {
		if (places < 0)
			throw new IllegalArgumentException();

		BigDecimal bd = new BigDecimal(value);
		bd = bd.setScale(places, RoundingMode.HALF_UP);
		return bd.doubleValue();
	}
	
	
	/* 
	 * The subsequent functions are part of the library "ResearchCode" of Raffaele Conforti
	 * https://github.com/raffaeleconforti/ResearchCode
	 * 
	 * Copyright (C) 2019 Raffaele Conforti
	 * The code is available as open source code; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
	 * You should have received a copy of the GNU Lesser General Public License along with this it. If not, see http://www.gnu.org/licenses/lgpl-3.0.html.
	 */

	private static PetrinetWithMarking mineModel(double p1, double p2, int algo, String logname, XLog log, int fold) {
		PetrinetWithMarking pn = testMiner(p1, p2, log, algo, logname);
		ModelUtils.exportPetrinet(new FakePluginContext(), pn,
				"models/" + logname + "_" + algo + "_kfoldcv_" + fold + "_" + p1 + "_" + p2 + "_model.pnml");
		return pn;
	}

	private static PetrinetWithMarking testMiner(double p1, double p2, XLog log, int miner, String logname) {
		FakePluginContext context = new FakePluginContext();
		SplitMiner yam = new SplitMiner();
		BPMNDiagram bpmn = null;
		PetrinetWithMarking petrinet = null;
		XEventNameClassifier xEventClassifier = new XEventNameClassifier();

		Double f_threshold = p1; // percentileFrequencyThreshold both 0.3 before
		Double p_threshold = p2; // parallelismsThreshold

		bpmn = yam.mineBPMNModel(log, xEventClassifier, f_threshold, p_threshold, DFGPUIResult.FilterType.WTH, true,
				true, false, SplitMinerUIResult.StructuringTime.NONE);
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

	
	/*
	 * ---->
	 */
	public static class Tuple {
		public double x;
		public double y;

		public Tuple(double x, double y) {
			this.x = x;
			this.y = y;
		}
	}

}

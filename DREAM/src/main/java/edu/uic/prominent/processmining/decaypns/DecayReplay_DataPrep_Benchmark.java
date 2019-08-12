package edu.uic.prominent.processmining.decaypns;

import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import edu.uic.prominent.processmining.decaypns.log.EventLog;
import edu.uic.prominent.processmining.decaypns.petrinet.PetriNet;
import edu.uic.prominent.processmining.decaypns.petrinet.Place;
import edu.uic.prominent.processmining.decaypns.pnmetrics.TimeDecayReplayer;
import edu.uic.prominent.processmining.decaypns.pnmetrics.decay.DecayParameterEstimator;
import edu.uic.prominent.processmining.decaypns.pnmetrics.decay.EstimationStatistic;
import edu.uic.prominent.processmining.decaypns.pntools.PetriNetParser;
import edu.uic.prominent.processmining.decaypns.util.BenchmarkDataset;

public class DecayReplay_DataPrep_Benchmark {

	final static Logger logger = Logger.getLogger(DecayReplay_DataPrep_Benchmark.class);

	public static void main(String[] args) {
		try {
			BenchmarkDataset datasets = new BenchmarkDataset();
			String benchmarkName = args[0];

			int k = 10;
			int algorithm = 2;

			for (int i = 0; i < k; i++) {
				JSONObject json = datasets.getParameterJSONfromCVMinedPetriNetParameters(benchmarkName, i);

				EventLog log = datasets.getLogCVTrain(benchmarkName, i);
				// log.setOrdering(datasets.benchmarkMetadata.get(benchmarkName).get("ordering"));

				EventLog logTest = datasets.getLogCVTest(benchmarkName, i);

				String petriNetFileName = benchmarkName + "_" + algorithm + "_kfoldcv_" + i + "_"
						+ json.getDouble("param1") + "_" + json.getDouble("param2") + "_model.pnml";

				PetriNetParser pnParser = new PetriNetParser();
				PetriNet petriNet = pnParser.getPetriNetFromFile(petriNetFileName);

				// Decay Dataset
				DecayParameterEstimator decayParamEst = new DecayParameterEstimator(petriNet.getTransitions(), log);
				Map<Place, EstimationStatistic> estParam = decayParamEst.getEstimatedParameters();

				TimeDecayReplayer replayer = new TimeDecayReplayer(benchmarkName, petriNet, log, 0, estParam,
						datasets.benchmarkMetadata.get(benchmarkName).get("timebase"));
				replayer.replayLog();
				replayer.createDataset("kfoldcv_" + i + "_train");

				replayer = new TimeDecayReplayer(benchmarkName, petriNet, logTest, 0, estParam,
						datasets.benchmarkMetadata.get(benchmarkName).get("timebase"));
				replayer.replayLog();
				replayer.createDataset("kfoldcv_" + i + "_test");
				// -------->

			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

	}
}

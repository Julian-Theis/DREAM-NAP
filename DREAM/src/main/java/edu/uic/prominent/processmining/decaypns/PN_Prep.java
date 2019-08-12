package edu.uic.prominent.processmining.decaypns;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

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
import edu.uic.prominent.processmining.decaypns.log.EventLog;
import edu.uic.prominent.processmining.decaypns.log.EventLogParser;
import edu.uic.prominent.processmining.decaypns.log.XLogLoader;
import edu.uic.prominent.processmining.decaypns.petrinet.PetriNet;
import edu.uic.prominent.processmining.decaypns.petrinet.Place;
import edu.uic.prominent.processmining.decaypns.pnmetrics.TimeDecayReplayer;
import edu.uic.prominent.processmining.decaypns.pnmetrics.decay.DecayParameterEstimator;
import edu.uic.prominent.processmining.decaypns.pnmetrics.decay.EstimationStatistic;
import edu.uic.prominent.processmining.decaypns.pnmetrics.util.Constants;
import edu.uic.prominent.processmining.decaypns.pntools.PetriNetParser;
import edu.uic.prominent.processmining.decaypns.prom.model.ModelUtils;



public class PN_Prep {

	private static String logname = "logs/adv_cyclic_880_cased.csv";
	private static String outputlog = "adv_cyclic_880_cased.xes";
	
	private static String petriNetFileName = "adv_cyclic_880_cased_2_0.0_0.5_model.pnml";
	
	private static String name = "adv_cyclic_880_cased";
	
	public static void main(String[] args) {
		double p1 = 0.0;
		double p2 = 0.5;
		
		HashMap<String, Integer> logConfiguration = new HashMap<String, Integer>();		
		logConfiguration.put("Timestamp", 0);
		logConfiguration.put("Activity", 1);
		logConfiguration.put("Case", 2);
		
		//XLogLoader logLoader = new XLogLoader(logConfiguration);
		//XLog log = logLoader.importCSV(logname, true, false, "yyyy/MM/dd HH:mm:s.S");
		EventLog log = new EventLogParser().getEventLogFromFile(name + ".xes");
		
		
		PetriNetParser pnParser = new PetriNetParser();
		PetriNet petriNet = pnParser.getPetriNetFromFile(petriNetFileName);
		
		// Decay Dataset
		DecayParameterEstimator decayParamEst = new DecayParameterEstimator(petriNet.getTransitions(), log);
		Map<Place, EstimationStatistic> estParam = decayParamEst.getEstimatedParameters();

		TimeDecayReplayer replayer = new TimeDecayReplayer(name, petriNet, log, 0, estParam, Constants.DECAYVECTORPREDICTION_TIMEBASE_T0);
		replayer.replayLog();
		replayer.createDataset(name);

		// -------->
		
	}
	
	
	
}

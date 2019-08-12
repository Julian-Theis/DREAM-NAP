package edu.uic.prominent.processmining.decaypns;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import edu.uic.prominent.processmining.decaypns.log.EventLog;
import edu.uic.prominent.processmining.decaypns.log.util.Event;
import edu.uic.prominent.processmining.decaypns.log.util.Trace;
import edu.uic.prominent.processmining.decaypns.util.BenchmarkDataset;

public class LogStatistics {

	final static Logger logger = Logger.getLogger(LogStatistics.class);

	public static void main(String[] args) {
		try {
			BenchmarkDataset datasets = new BenchmarkDataset();
			String benchmarkName = args[0];

			int traces = 0;
			int events = 2;
			List<String> evs = new ArrayList<String>();
			EventLog log = datasets.getLogCVTrain(benchmarkName, 0);

			for (Entry<Trace, Integer> entry : log.getLog())
			{
				for (Event e : entry.getKey().trace) {
					events++;
					if (!evs.contains(e.name())) {
						evs.add(e.name());
					}
				}
			}

			traces += log.getNumTraces();

			log = datasets.getLogCVTest(benchmarkName, 0);
			traces += log.getNumTraces();
			for (Entry<Trace, Integer> entry : log.getLog()) {
				for (Event e : entry.getKey().trace) {
					events++;
					if (!evs.contains(e.name())) {
						evs.add(e.name());
					}
				}
			}

			System.out.println("Number of events: " + events);
			System.out.println("Number of unique events: " + evs.size());
			System.out.println("Number of traces: " + traces);

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}
}

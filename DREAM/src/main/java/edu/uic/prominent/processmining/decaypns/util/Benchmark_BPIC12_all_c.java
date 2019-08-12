package edu.uic.prominent.processmining.decaypns.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;

import edu.uic.prominent.processmining.decaypns.log.XLogLoader;

public class Benchmark_BPIC12_all_c {

	public static void main(String[] args) {
		try {
			String benchmarkName = "bpic12_all_complete";
			String logFile = "BPI_Challenge_2012.xes";
			String filter_status = "COMPLETE";

			XLogLoader logLoader = new XLogLoader();
			XLog xlog = logLoader.importXES(logFile);
			SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.US);

			List<String> output = new ArrayList<String>();
			output.add("CaseID,ActivityID,Timestamp,Resource1,Resource2");

			XTrace trace = null;
			String caseId = "";
			String status = "";
			String activity = "";
			String timeString = "";
			int count = 0;
			long timestamp = 0L;
			String resource1 = "";
			String resource2 = "";

			// Quantize the cont variables
			List<Integer> amount_reqs = new ArrayList<Integer>();
			for (int x = 0; x < xlog.size(); x++) {
				trace = xlog.get(x);
				amount_reqs.add(Integer.parseInt(trace.getAttributes().get("AMOUNT_REQ").toString()));
			}

			Set<Integer> hashset = new HashSet<Integer>(amount_reqs);
			System.out.printf("\nUnique values using HashSet: %s%n", hashset);
			System.out.println("Number of unique values: " + hashset.size());

			Map<Integer, Long> counters = amount_reqs.stream()
					.collect(Collectors.groupingBy(p -> p, Collectors.counting()));
			SortedSet<Integer> keys = new TreeSet<Integer>(counters.keySet());
			for (Integer key : keys) {
				System.out.println(key + ": " + counters.get(key));

			}
			/// ----->

			// QUANTIZE
			List<Quant> quants = quantize();

			for (int t = 0; t < xlog.size(); t++) {
				trace = xlog.get(t);
				caseId = trace.getAttributes().get("concept:name").toString();
				resource2 = trace.getAttributes().get("AMOUNT_REQ").toString();

				int res2int = Integer.parseInt(resource2);
				boolean found = false;
				int cc = 0;
				while (!found) {
					Quant q = quants.get(cc);
					if (q.lower < res2int && q.upper >= res2int) {
						found = true;
						resource2 = q.label + "";
					}
					cc++;
				}
				
				for (int i = 0; i < trace.size(); i++) {
					XEvent event = trace.get(i);

					status = event.getAttributes().get("lifecycle:transition").toString();
					activity = event.getAttributes().get("concept:name").toString();
					if (status.equals(filter_status)) {
						timeString = event.getAttributes().get("time:timestamp").toString();
						count = StringUtils.countMatches(timeString, ".");
						
						if(event.getAttributes().containsKey("org:resource")){
							resource1 = event.getAttributes().get("org:resource").toString();
						}else{
							resource1 = "null";
						}
						
						if (count == 0) {
							String[] timeStringSplit = timeString.split("\\-");
							timeString = timeStringSplit[0] + "-" + timeStringSplit[1] + "-" + timeStringSplit[2]
									+ ".000-" + timeStringSplit[3];
						}
						timestamp = f.parse(timeString).getTime();

						String input = caseId + "," + activity + "," + timestamp + "," + resource1+ "," + resource2;
						output.add(input);
					}
				}
			}

			File fout = new File("logs/" + benchmarkName + "/" + benchmarkName + ".csv");
			FileOutputStream fos = new FileOutputStream(fout);
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
			for (String str : output) {
				bw.write(str);
				bw.newLine();
			}
			bw.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static List<Quant> quantize() {
		String csvFile = "logs/bpic12_all/quantize.csv";
		String line = "";
		String cvsSplitBy = ",";

		List<Quant> quants = new ArrayList<Quant>();
		try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
			while ((line = br.readLine()) != null) {
				String[] values = line.split(cvsSplitBy);
				quants.add(new Quant(Integer.parseInt(values[0]),
						Integer.parseInt(values[1]),
						Integer.parseInt(values[2])));
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		return quants;
	}
	
	public static class Quant{
		public int lower;
		public int upper;
		public int label;
		
		public Quant(int lower, int upper, int label){
			this.lower = lower;
			this.upper = upper;
			this.label = label;
		}
	}
}

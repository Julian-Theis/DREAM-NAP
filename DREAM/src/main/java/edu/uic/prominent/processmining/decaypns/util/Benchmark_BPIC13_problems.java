package edu.uic.prominent.processmining.decaypns.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;

import edu.uic.prominent.processmining.decaypns.log.XLogLoader;

public class Benchmark_BPIC13_problems {

	public static void main(String[] args) {
		try {
			String benchmarkName = "bpic13_p";
			String logFile1 = "BPI_Challenge_2013_open_problems.xes";
			String logFile2 = "BPI_Challenge_2013_closed_problems.xes";
			
			XLogLoader logLoader = new XLogLoader();
			XLog xlog1 = logLoader.importXES(logFile1);
			XLog xlog2 = logLoader.importXES(logFile2);
			SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.US);

			List<String> output = new ArrayList<String>();
			output.add("CaseID,ActivityID,Timestamp,Resource1,Resource2,Resource3,Resource4,Resource5,Resource6,Resource7,Resource8");

			XTrace trace = null;
			String caseId = "";
			String activity = "";
			String timeString = "";
			String resource1 = "";
			String resource2 = "";
			String resource3 = "";
			String resource4 = "";
			String resource5 = "";
			String resource6 = "";
			String resource7 = "";
			String resource8 = "";
			
			
			int count = 0;
			long timestamp = 0L;

			for (int t = 0; t < xlog1.size(); t++) {
				trace = xlog1.get(t);
				caseId = trace.getAttributes().get("concept:name").toString().substring(2);
				for (int i = 0; i < trace.size(); i++) {
					XEvent event = trace.get(i);

					activity = event.getAttributes().get("concept:name").toString();
					timeString = event.getAttributes().get("time:timestamp").toString();
					
					resource1 = event.getAttributes().get("org:group").toString();
					resource2 = event.getAttributes().get("resource country").toString();
					
					if(event.getAttributes().containsKey("organization country")){
						resource3 = event.getAttributes().get("organization country").toString();
					}else{
						resource3 = "null";
					}
					
					resource4 = event.getAttributes().get("org:resource").toString();

					if(event.getAttributes().containsKey("org:role")){
						resource5 = event.getAttributes().get("org:role").toString();
					}else{
						resource5 = "null";
					}
					
					resource6 = event.getAttributes().get("impact").toString();
					resource7 = event.getAttributes().get("product").toString();
					
					if(event.getAttributes().containsKey("organization involved")){
						resource8 = event.getAttributes().get("organization involved").toString();
					}else{
						resource8 = "null";
					}
					
					count = StringUtils.countMatches(timeString, ".");
					if (count == 0) {
						String[] timeStringSplit = timeString.split("\\-");
						timeString = timeStringSplit[0] + "-" + timeStringSplit[1] + "-" + timeStringSplit[2] + ".000-"
								+ timeStringSplit[3];
					}
					timestamp = f.parse(timeString).getTime();
					String input = caseId + "," + activity+ "," + timestamp + "," + resource1 + "," + resource2 + "," + resource3 + "," + resource4 + "," + resource5 + "," + resource6 + "," + resource7+ "," + resource8;
					output.add(input);
				}
			}
			
			for (int t = 0; t < xlog2.size(); t++) {
				trace = xlog2.get(t);
				caseId = trace.getAttributes().get("concept:name").toString().substring(2);
				for (int i = 0; i < trace.size(); i++) {
					XEvent event = trace.get(i);

					activity = event.getAttributes().get("concept:name").toString();
					timeString = event.getAttributes().get("time:timestamp").toString();
					
					resource1 = event.getAttributes().get("org:group").toString();
					resource2 = event.getAttributes().get("resource country").toString();
					resource3 = event.getAttributes().get("organization country").toString();
					resource4 = event.getAttributes().get("org:resource").toString();
					if(event.getAttributes().containsKey("org:role")){
						resource5 = event.getAttributes().get("org:role").toString();
					}else{
						resource5 = "null";
					}
					resource6 = event.getAttributes().get("impact").toString();
					resource7 = event.getAttributes().get("product").toString();
					resource8 = event.getAttributes().get("organization involved").toString();
					
					
					count = StringUtils.countMatches(timeString, ".");
					if (count == 0) {
						String[] timeStringSplit = timeString.split("\\-");
						timeString = timeStringSplit[0] + "-" + timeStringSplit[1] + "-" + timeStringSplit[2] + ".000-"
								+ timeStringSplit[3];
					}
					timestamp = f.parse(timeString).getTime();
					String input = caseId + "," + activity + "_" + resource1 + "," + timestamp + "," + resource1 + "," + resource2 + "," + resource3 + "," + resource4 + "," + resource5 + "," + resource6 + "," + resource7 + "," + resource8;
					output.add(input);
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
}

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

public class Benchmark_Helpdesk_inverse {

	public static void main(String[] args) {
		try {
			String benchmarkName = "helpdesk_inverse";
			String logFile = "helpdesk_inverse.xes";

			XLogLoader logLoader = new XLogLoader();
			XLog xlog = logLoader.importXES(logFile);
			SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.US);

			List<String> output = new ArrayList<String>();
			output.add("CaseID,ActivityID,Timestamp");

			XTrace trace = null;
			String caseId = "";
			String status = "";
			String activity = "";
			String timeString = "";
			int count = 0;
			long timestamp = 0L;

			for (int t = 0; t < xlog.size(); t++) {
				trace = xlog.get(t);
				caseId = trace.getAttributes().get("concept:name").toString();

				for (int i = trace.size()-1; i >=0 ; i--) {
					XEvent event = trace.get(i);

					status = event.getAttributes().get("lifecycle:transition").toString();
					activity = event.getAttributes().get("concept:name").toString();

					timeString = event.getAttributes().get("time:timestamp").toString();
					count = StringUtils.countMatches(timeString, ".");
					if (count == 0) {
						String[] timeStringSplit = timeString.split("\\-");
						timeString = timeStringSplit[0] + "-" + timeStringSplit[1] + "-" + timeStringSplit[2] + ".000-"
								+ timeStringSplit[3];
					}
					timestamp = f.parse(timeString).getTime()*-1;
					System.out.println(status);

					String input = caseId + "," + activity + "," + timestamp;
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

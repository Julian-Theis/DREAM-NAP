package edu.uic.prominent.processmining.decaypns.pntools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
//j  a v  a2 s .co  m
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class PnmlPreprocess {

	private String file;

	public PnmlPreprocess(String name) {
		this.file = name;
		try {

			InputSource is = new InputSource();
			is.setCharacterStream(new StringReader(readFile(file)));

			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();

			Document doc = db.parse(is);
			NodeList nodes = doc.getElementsByTagName("place");
			int placeIndex = 0;
			for (int i = 0; i < nodes.getLength(); i++) {
				Element element = (Element) nodes.item(i);

				if (element.hasAttribute("id")) {
					//System.out.println(element.getAttribute("id"));

					NodeList names = element.getElementsByTagName("name");
					for (int n = 0; n < names.getLength(); n++) {
						Element nam = (Element) names.item(n);

						NodeList childs = nam.getChildNodes();
						for (int c = 0; c < childs.getLength(); c++) {
							nam.removeChild(childs.item(c));
						}
						//System.out.println(nam.hasChildNodes());
						nam.setTextContent("<text>p" + placeIndex + "</text>");
						//System.out.println(nam.getNodeValue());
					}
					placeIndex++;
				}
			}
			writeToFile(doc, "modified.pnml");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void writeToFile(Document doc, String name) {
		try {
			// write the content into xml file
			DOMSource source = new DOMSource(doc);
			FileWriter writer = new FileWriter(new File("models/" + name));
			StreamResult result = new StreamResult(writer);

			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.transform(source, result);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static String readFile(String file) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line = null;
		StringBuilder stringBuilder = new StringBuilder();
		String ls = System.getProperty("line.separator");

		try {
			while ((line = reader.readLine()) != null) {
				stringBuilder.append(line);
				stringBuilder.append(ls);
			}

			return stringBuilder.toString();
		} finally {
			reader.close();
		}
	}
}
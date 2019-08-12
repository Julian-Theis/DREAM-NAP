package edu.uic.prominent.processmining.decaypns.pntools;

import java.io.File;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.uic.prominent.processmining.decaypns.pntools.util.IncidenceMatrix;

public class IncidenceMatrix2Pnml {

	private IncidenceMatrix d;
	private Document doc;
	List<String> initMarking;
	List<String> finalMarking;
	List<String> places;
	List<String> transitions;
	
	private int initTokens = 1;
	private int finalTokens = 1;
	
	final static Logger logger = Logger.getLogger(IncidenceMatrix2Pnml.class);
	
	public IncidenceMatrix2Pnml(IncidenceMatrix d) {
		this.d = d;
		
		// Create Places
		places = new ArrayList<String>();
		for(int i = 0; i < d.numPlaces(); i++) {
			places.add("p" + i);
		}
		
		// Create Transitions
		transitions = new ArrayList<String>();
		for(int i = 0; i < d.numTransitions(); i++) {
			transitions.add("t" + i);
		}
		
		//Create initMarking
		initMarking = new ArrayList<String>();
		initMarking.add("p0");
		
		//Create finalMarking
		finalMarking = new ArrayList<String>();
		finalMarking.add("p" + (d.numPlaces()-1));
		
		toPnml();
	}
	
	public String getPnml() {
		String xmlString = null;
		try {
			TransformerFactory transfac = TransformerFactory.newInstance();
			Transformer trans = transfac.newTransformer();
			trans.setOutputProperty(OutputKeys.METHOD, "xml");
			trans.setOutputProperty(OutputKeys.INDENT, "no");
			trans.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", Integer.toString(2));

			StringWriter sw = new StringWriter();
			StreamResult result = new StreamResult(sw);
			DOMSource source = new DOMSource(doc.getDocumentElement());

			trans.transform(source, result);
			xmlString = sw.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return xmlString;
	}
	
	public void savePnml(String filename) {
		try {
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File("models/" + filename));
			transformer.transform(source, result);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void toPnml() {
		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

			// root elements
			doc = docBuilder.newDocument();
			Element rootElement = doc.createElement("pnml");
			doc.appendChild(rootElement);

			// net element
			Element net = doc.createElement("net");
			rootElement.appendChild(net);

			Attr attr = doc.createAttribute("id");
			attr.setValue("net1");
			net.setAttributeNode(attr);
			attr = doc.createAttribute("type");
			attr.setValue("http://www.pnml.org/version-2009/grammar/pnmlcoremodel");
			net.setAttributeNode(attr);

			// Create page
			Element page = doc.createElement("page");
			net.appendChild(page);
			attr = doc.createAttribute("id");
			attr.setValue("n0");
			page.setAttributeNode(attr);

			for (String p : places) {
				Element place = doc.createElement("place");
				page.appendChild(place);
				attr = doc.createAttribute("id");
				attr.setValue(p);
				place.setAttributeNode(attr);

				// Add Initial Marking
				boolean found = false;
				for (String im : initMarking) {
					if (p.equals(im)) {
						found = true;
						break;
					}
				}
				if (found) {
					Element initMarking = doc.createElement("initialMarking");
					place.appendChild(initMarking);
					Element text = doc.createElement("text");
					text.appendChild(doc.createTextNode(this.initTokens + ""));
					initMarking.appendChild(text);
				}

			}

			for (String t : transitions) {
				Element transition = doc.createElement("transition");
				page.appendChild(transition);
				attr = doc.createAttribute("id");
				attr.setValue(t);
				transition.setAttributeNode(attr);

				Element name = doc.createElement("name");
				transition.appendChild(name);
				Element text = doc.createElement("text");
				text.appendChild(doc.createTextNode(t));
				name.appendChild(text);
			}

			// ARCS
			List<Arc> arcs = new ArrayList<Arc>();
			for(int t = 0; t < d.numTransitions(); t++) {
				String trans = "t" + t;
				
				List<String> ins = new ArrayList<String>();
				for(int p = 0; p < d.numPlaces(); p++) {
					if(this.d.getValue(t, p) == -1)
						ins.add("p"+p);
				}
				for (String in : ins) {
					arcs.add(new Arc(in, trans));
				}

				List<String> outs = new ArrayList<String>();
				for(int p = 0; p < d.numPlaces(); p++) {
					if(this.d.getValue(t, p) == 1)
						outs.add("p"+p);
				}
				for (String out : outs) {
					arcs.add(new Arc(trans, out));
				}
			}

			for(int arcCnt = 0; arcCnt < arcs.size(); arcCnt++){
				Arc a = arcs.get(arcCnt);
				
				Element arc = doc.createElement("arc");
				page.appendChild(arc);
				attr = doc.createAttribute("id");
				attr.setValue("arc" + arcCnt);
				arc.setAttributeNode(attr);
				attr = doc.createAttribute("source");
				attr.setValue(a.in);
				arc.setAttributeNode(attr);
				attr = doc.createAttribute("target");
				attr.setValue(a.out);
				arc.setAttributeNode(attr);

				Element name = doc.createElement("name");
				arc.appendChild(name);
				Element text = doc.createElement("text");
				text.appendChild(doc.createTextNode("1"));
				name.appendChild(text);

				Element arctype = doc.createElement("arctype");
				arc.appendChild(arctype);
				text = doc.createElement("text");
				text.appendChild(doc.createTextNode("normal"));
				arctype.appendChild(text);
			}

			// finalmarkings
			Element finalmarkings = doc.createElement("finalmarkings");
			net.appendChild(finalmarkings);
			Element marking = doc.createElement("marking");
			finalmarkings.appendChild(marking);

			for (String p : places) {
				Element place = doc.createElement("place");
				marking.appendChild(place);
				attr = doc.createAttribute("idref");
				attr.setValue(p);
				place.setAttributeNode(attr);

				boolean found = false;
				for (String mark : finalMarking) {
					if (mark.equals(p)) {
						found = true;
						break;
					}
				}

				Element text = doc.createElement("text");
				if (found)
					text.appendChild(doc.createTextNode(this.finalTokens + ""));
				else
					text.appendChild(doc.createTextNode("0"));
				place.appendChild(text);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public class Arc {
		public String in;
		public String out;

		public Arc(String in, String out) {
			this.in = in;
			this.out = out;
		}
	}
}

package edu.uic.prominent.processmining.decaypns.pntools;

import java.io.File;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.uic.prominent.processmining.decaypns.petrinet.PetriNet;
import edu.uic.prominent.processmining.decaypns.petrinet.Place;
import edu.uic.prominent.processmining.decaypns.petrinet.Transition;

//http://pnml.lip6.fr 
public class PetriNetEditor {

	private PetriNet model;
	private Document doc;
	private int arcCnt = 1;

	public PetriNetEditor(PetriNet model) {
		this.model = model;
		this.transform2Pnml();
	}
	
	public PetriNet updatePetriNet(){
		String pnml = getPnml();
		
		PetriNet pn = new PetriNetParser().getPetriNetFromString(pnml);
		pn.setPlaceIds_new2old(this.model.getPlaceIds_new2old());
		pn.setPlaceIds_old2new(this.model.getPlaceIds_old2new());
		pn.setTransIds_new2old(this.model.getTransIds_new2old());
		pn.setTransIds_old2new(this.model.getTransIds_old2new());
		this.model = pn;
		this.transform2Pnml();

		return model;
	}
	
	private void transform2Pnml() {
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

			List<Place> initMark = this.model.initialMarking();
			List<Place> places = this.model.getPlaces();
			for (Place p : places) {
				Element place = doc.createElement("place");
				page.appendChild(place);
				attr = doc.createAttribute("id");
				attr.setValue(p.name());
				place.setAttributeNode(attr);

				// Add Initial Marking
				boolean found = false;
				for (Place im : initMark) {
					if (p == im) {
						found = true;
						break;
					}
				}
				if (found) {
					Element initMarking = doc.createElement("initialMarking");
					place.appendChild(initMarking);
					Element text = doc.createElement("text");
					text.appendChild(doc.createTextNode(p.initTokens() + ""));
					initMarking.appendChild(text);
				}

			}

			List<Transition> transitions = this.model.getTransitions();
			for (Transition t : transitions) {
				Element transition = doc.createElement("transition");
				page.appendChild(transition);
				attr = doc.createAttribute("id");
				attr.setValue(t.name());
				transition.setAttributeNode(attr);

				Element name = doc.createElement("name");
				transition.appendChild(name);
				Element text = doc.createElement("text");
				text.appendChild(doc.createTextNode(t.name()));
				name.appendChild(text);

				if (!t.isVisible()) {
					Element toolspecific = doc.createElement("toolspecific");
					attr = doc.createAttribute("tool");
					attr.setValue("ProM");
					toolspecific.setAttributeNode(attr);
					attr = doc.createAttribute("version");
					attr.setValue("6.4");
					toolspecific.setAttributeNode(attr);
					attr = doc.createAttribute("activity");
					attr.setValue("$invisible$");
					toolspecific.setAttributeNode(attr);
					transition.appendChild(toolspecific);
				}
			}

			// ARCS
			List<Arc> arcs = new ArrayList<Arc>();
			for (Transition t : transitions) {
				String trans = t.name();

				Set<Place> ins = t.getInputs();
				for (Place in : ins) {
					arcs.add(new Arc(in.name(), trans));
				}

				Set<Place> outs = t.getOutputs();
				for (Place out : outs) {
					arcs.add(new Arc(trans, out.name()));
				}
			}

			for (Arc a : arcs) {
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

				arcCnt++;
			}

			// finalmarkings
			Element finalmarkings = doc.createElement("finalmarkings");
			net.appendChild(finalmarkings);
			Element marking = doc.createElement("marking");
			finalmarkings.appendChild(marking);

			List<Place> finalMark = this.model.finalMarking();
			for (Place p : places) {
				Element place = doc.createElement("place");
				marking.appendChild(place);
				attr = doc.createAttribute("idref");
				attr.setValue(p.name());
				place.setAttributeNode(attr);

				boolean found = false;
				for (Place mark : finalMark) {
					if (mark.name().equals(p.name())) {
						found = true;
						break;
					}
				}

				Element text = doc.createElement("text");
				if (found)
					text.appendChild(doc.createTextNode(p.getFinalMarking() + ""));
				else
					text.appendChild(doc.createTextNode("0"));
				place.appendChild(text);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void removeTransition(String transName) {
		NodeList transitions = doc.getElementsByTagName("transition");
		for (int i = 0; i < transitions.getLength(); i++) {
			if (transitions.item(i).getAttributes().getNamedItem("id").toString().equals("id=\"" + transName + "\"")) {
				Node parent = transitions.item(i).getParentNode();
				parent.removeChild(transitions.item(i));
			}
		}
	}

	public void removePlace(String placeName) {
		NodeList places = doc.getElementsByTagName("place");
		for (int i = 0; i < places.getLength(); i++) {
			Node item = places.item(i);
			boolean hasIdref = false;
			for (int c = 0; c < item.getAttributes().getLength(); c++) {
				if (item.getAttributes().item(c).toString().contains("idref=")) {
					hasIdref = true;
					break;
				}
			}

			if (hasIdref) {
				if (item.getAttributes().getNamedItem("idref").toString().equals("idref=\"" + placeName + "\"")) {
					Node parent = item.getParentNode();
					parent.removeChild(item);
				}
			} else {
				if (item.getAttributes().getNamedItem("id").toString().equals("id=\"" + placeName + "\"")) {
					Node parent = item.getParentNode();
					parent.removeChild(item);
				}
			}
		}
	}

	public void removeArc(String source, String target) {
		NodeList places = doc.getElementsByTagName("arc");
		for (int i = 0; i < places.getLength(); i++) {
			Node item = places.item(i);
			if (item.getAttributes().getNamedItem("source").toString().equals("source=\"" + source + "\"")
					&& item.getAttributes().getNamedItem("target").toString().equals("target=\"" + target + "\"")) {
				Node parent = item.getParentNode();
				parent.removeChild(item);
			}
		}
	}

	public void initialMarking(String placeName, int amount) {
		if (amount > 0) {
			NodeList places = doc.getElementsByTagName("place");
			for (int i = 0; i < places.getLength(); i++) {
				Node item = places.item(i);

				if (item.getAttributes().getNamedItem("id").toString().equals("id=\"" + placeName + "\"")) {
					if (item.getChildNodes().getLength() > 0) {
						item.removeChild(item.getChildNodes().item(0));
					}

					Element initMarking = doc.createElement("initialMarking");
					item.appendChild(initMarking);
					Element text = doc.createElement("text");
					text.appendChild(doc.createTextNode("" + amount));
					initMarking.appendChild(text);

					break;
				}
			}
		} else {
			System.out.println("Initial Marking has to be greater than zero");
		}
	}

	public void removeInitialMarking(String placeName) {
		NodeList places = doc.getElementsByTagName("place");
		for (int i = 0; i < places.getLength(); i++) {
			Node item = places.item(i);

			if (item.getAttributes().getNamedItem("id").toString().equals("id=\"" + placeName + "\"")) {
				if (item.getChildNodes().getLength() > 0) {
					item.removeChild(item.getChildNodes().item(0));
				}
				break;
			}
		}
	}

	public void addFinalMarking(String placeName, int amount) {
		if (amount > 0) {
			Node marking = doc.getElementsByTagName("marking").item(0);

			Element place = doc.createElement("place");
			marking.appendChild(place);
			Attr attr = doc.createAttribute("idref");
			attr.setValue(placeName);
			place.setAttributeNode(attr);

			Element text = doc.createElement("text");
			text.appendChild(doc.createTextNode("" + amount));
			place.appendChild(text);
		}
	}

	public void finalMarking(String placeName, int amount) {
		if (amount > 0) {
			NodeList places = doc.getElementsByTagName("finalmarkings").item(0).getFirstChild().getChildNodes();
			
			//NodeList places = doc.getElementsByTagName("place");

			for (int i = 0; i < places.getLength(); i++) {
				Node item = places.item(i);
				
				boolean hasIdref = false;
				for (int c = 0; c < item.getAttributes().getLength(); c++) {
					if (item.getAttributes().item(c).toString().contains("idref=")) {
						hasIdref = true;
						//System.out.println(item.getAttributes().getNamedItem("idref").getNodeValue());
						break;
					}
				}
				
				

				if (hasIdref) {
					if (item.getAttributes().getNamedItem("idref").toString().equals("idref=\"" + placeName + "\"")) {
						Node parent = item.getParentNode();
						parent.removeChild(item);
						
						System.out.println("FOUND it");

						Element text = doc.createElement("text");
						text.appendChild(doc.createTextNode("" + amount));
						item.appendChild(text);

						break;
					}
				}

			}
		} else {
			System.out.println("New final marking has to be greater than zero");
		}
	}

	public void removeFinalMarking(String placeName) {
		NodeList places = doc.getElementsByTagName("place");

		for (int i = 0; i < places.getLength(); i++) {
			Node item = places.item(i);

			boolean hasIdref = false;
			for (int c = 0; c < item.getAttributes().getLength(); c++) {
				if (item.getAttributes().item(c).toString().contains("idref=")) {
					hasIdref = true;
					break;
				}
			}

			if (hasIdref) {
				if (item.getAttributes().getNamedItem("idref").toString().equals("idref=\"" + placeName + "\"")) {
					Node parent = item.getParentNode();
					parent.removeChild(item);

					Element text = doc.createElement("text");
					text.appendChild(doc.createTextNode("0"));
					item.appendChild(text);

					break;
				}
			}

		}
	}

	public void addTransition(String transName, boolean visible) {
		Node page = doc.getElementsByTagName("page").item(0);

		Element transition = doc.createElement("transition");
		page.appendChild(transition);
		Attr attr = doc.createAttribute("id");
		attr.setValue(transName);
		transition.setAttributeNode(attr);

		Element name = doc.createElement("name");
		transition.appendChild(name);
		Element text = doc.createElement("text");
		text.appendChild(doc.createTextNode(transName));
		name.appendChild(text);

		if (!visible) {
			Element toolspecific = doc.createElement("toolspecific");
			attr = doc.createAttribute("tool");
			attr.setValue("ProM");
			toolspecific.setAttributeNode(attr);
			attr = doc.createAttribute("version");
			attr.setValue("6.4");
			toolspecific.setAttributeNode(attr);
			attr = doc.createAttribute("activity");
			attr.setValue("$invisible$");
			toolspecific.setAttributeNode(attr);
			transition.appendChild(toolspecific);
		}
	}

	public void addPlace(String placeName) {
		Node page = doc.getElementsByTagName("page").item(0);

		Element place = doc.createElement("place");
		page.appendChild(place);
		Attr attr = doc.createAttribute("id");
		attr.setValue(placeName);
		place.setAttributeNode(attr);
	}

	public void addArc(String source, String target) {
		Node page = doc.getElementsByTagName("page").item(0);

		Element arc = doc.createElement("arc");
		page.appendChild(arc);
		Attr attr = doc.createAttribute("id");
		attr.setValue("arc" + arcCnt);
		arc.setAttributeNode(attr);
		attr = doc.createAttribute("source");
		attr.setValue(source);
		arc.setAttributeNode(attr);
		attr = doc.createAttribute("target");
		attr.setValue(target);
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

		arcCnt++;
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

	public class Arc {
		public String in;
		public String out;

		public Arc(String in, String out) {
			this.in = in;
			this.out = out;
		}
	}
}

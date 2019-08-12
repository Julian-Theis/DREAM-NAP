package edu.uic.prominent.processmining.decaypns.pntools;

import java.io.File;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import edu.uic.prominent.processmining.decaypns.petrinet.PetriNet;
import edu.uic.prominent.processmining.decaypns.petrinet.Place;
import edu.uic.prominent.processmining.decaypns.petrinet.Transition;

public class PetriNetParser {
	final static Logger logger = Logger.getLogger(PetriNetParser.class);
	
	private PetriNet petriNet;
	private Document doc;

	public PetriNet getPetriNetFromFile(String fileName){
		File f = new File("models/" + fileName);
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			doc = dBuilder.parse(f);
			petriNet = new PetriNet();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return createPetriNet();
	}
	
	public PetriNet getPetriNetFromString(String xmlString) {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			InputSource is = new InputSource(new StringReader(xmlString));
			doc = builder.parse(is);
			petriNet = new PetriNet();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return createPetriNet();
	}
	
	private PetriNet createPetriNet() {
		Map<String, String> placeIds_new2old = new HashMap<String, String>();
		Map<String, String> placeIds_old2new = new HashMap<String, String>();

		Map<String, String> transIds_new2old = new HashMap<String, String>();
		Map<String, String> transIds_old2new = new HashMap<String, String>();

		Map<String, Place> places = new HashMap<String, Place>();
		Map<String, Transition> transitions = new HashMap<String, Transition>();
		Node page = doc.getElementsByTagName("page").item(0);

		int p_counter = 0;
		int inv_trans_counter = 0;
		int initTokens = 0;
		boolean visible = true;
		String newId = null;
		String oldId = null;
		Place p = null;
		Transition trans = null;
		String source;
		String target;

		NodeList childs = page.getChildNodes();
		for (int i = 0; i < childs.getLength(); i++) {
			Node item = childs.item(i);

			if (item.getNodeName().equals("place")) {
				oldId = item.getAttributes().item(0).getNodeValue();
				newId = "pl" + p_counter;

				initTokens = 0;
				NodeList markingSearch = item.getChildNodes();
				Node n;
				for (int c = 0; c < markingSearch.getLength(); c++) {
					n = markingSearch.item(c);
					if (n.getNodeName().equals("initialMarking")) {
						initTokens = 0;
						try{
							initTokens = Integer.parseInt(n.getFirstChild().getFirstChild().getNodeValue());
						}catch(Exception e){
							
						}
					}
				}
				p = new Place(newId, initTokens);
				places.put(newId, p);
				placeIds_new2old.put(newId, oldId);
				placeIds_old2new.put(oldId, newId);
				p_counter++;
			} else if (item.getNodeName().equals("transition")) {
				visible = true;
				oldId = item.getAttributes().item(0).getNodeValue();

				NodeList transNodes = item.getChildNodes();
				Node n;
				for (int c = 0; c < transNodes.getLength(); c++) {
					n = transNodes.item(c);
					if (n.getNodeName().equals("name")) {
						if (n.getFirstChild().hasChildNodes()) {
							newId = n.getFirstChild().getFirstChild().getNodeValue();
						} else {
							newId = "NULL";
						}
					} else if (n.getNodeName().equals("toolspecific")) {
						if (n.getAttributes().getNamedItem("activity").getNodeValue().equals("$invisible$")) {
							visible = false;
							newId = "inv" + inv_trans_counter;
							inv_trans_counter++;
						}
					}
				}
				trans = new Transition(newId, visible);
				transitions.put(newId, trans);
				transIds_new2old.put(newId, oldId);
				transIds_old2new.put(oldId, newId);
			} else if (item.getNodeName().equals("arc")) {
				source = item.getAttributes().getNamedItem("source").getNodeValue();
				target = item.getAttributes().getNamedItem("target").getNodeValue();

				boolean sourceIsPlace = false;
				boolean targetIsPlace = false;

				if (placeIds_old2new.containsKey(source)) {
					source = placeIds_old2new.get(source);
					sourceIsPlace = true;
				} else if (transIds_old2new.containsKey(source)) {
					source = transIds_old2new.get(source);
				}

				if (placeIds_old2new.containsKey(target)) {
					targetIsPlace = true;
					target = placeIds_old2new.get(target);
				} else if (transIds_old2new.containsKey(target)) {
					target = transIds_old2new.get(target);
				}

				if (sourceIsPlace && !targetIsPlace) {
					Place src = places.get(source);
					Transition dest = transitions.get(target);
					
					src.to(dest);
					dest.from(src);
					
				} else if (!sourceIsPlace && targetIsPlace) {
					Place dest = places.get(target);
					Transition src = transitions.get(source);
					src.to(dest);
					dest.from(src);
				} else {
					System.out.println("UNSUPPORTED ARC WHILE PARSING");
				}
			}
		}

		Node marking = null;
		String placeId = null;
		int value = -1;
		NodeList finalMarking = doc.getElementsByTagName("finalmarkings").item(0).getFirstChild().getChildNodes();
		for (int c = 0; c < finalMarking.getLength(); c++) {
			marking = finalMarking.item(c);
			placeId = placeIds_old2new.get(marking.getAttributes().getNamedItem("idref").getNodeValue());
			value = -1;
			try {
				value = Integer.parseInt(marking.getFirstChild().getFirstChild().getNodeValue());
			} catch (Exception e) {
				value = 1;
			}
			places.get(placeId).setFinalTokens(value);
		}
	
		this.petriNet.addPlace(places.values().toArray(new Place[0]));
		this.petriNet.addTransition(transitions.values().toArray(new Transition[0]));
		this.petriNet.setIdTranslations(placeIds_new2old, placeIds_old2new, transIds_new2old, transIds_old2new);
		
		
		
		/*
		 * Logging Information
		 */		
		Iterator<String> iter = placeIds_old2new.keySet().iterator();
		while(iter.hasNext()){
			String key = iter.next();
			logger.info("Place IDs old2new: " + key + " to " + placeIds_old2new.get(key));
		}
		
		iter = transIds_old2new.keySet().iterator();
		while(iter.hasNext()){
			String key = iter.next();
			logger.info("Transition IDs old2new: " + key + " to " + transIds_old2new.get(key));
		}
		/*
		* ---->
		*/
				
		return this.petriNet;
	}
}

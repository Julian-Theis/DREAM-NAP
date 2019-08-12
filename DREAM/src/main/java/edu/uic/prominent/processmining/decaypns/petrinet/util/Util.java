package edu.uic.prominent.processmining.decaypns.petrinet.util;

import edu.uic.prominent.processmining.decaypns.petrinet.PetriNet;
import edu.uic.prominent.processmining.decaypns.petrinet.TimedTransition;
import edu.uic.prominent.processmining.decaypns.petrinet.Transition;

public class Util {
	public static PetriNet transition2TimedTransition(PetriNet pn, String transitionId, Object distribution){
		Transition t = pn.getTransitions().stream().filter(trans -> trans.name().equals(transitionId)).findFirst().get();   
		TimedTransition timedTrans = new TimedTransition(t, distribution);
		pn.updateTransition2TimedTransition(t, timedTrans);
		return pn;
	}
}

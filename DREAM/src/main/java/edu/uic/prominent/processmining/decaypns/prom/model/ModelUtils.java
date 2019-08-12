/* 
 * This code consists of parts of the library "ResearchCode" of Raffaele Conforti
 * https://github.com/raffaeleconforti/ResearchCode
 * 
 * Copyright (C) 2019 Raffaele Conforti
 * The code is available as open source code; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 * You should have received a copy of the GNU Lesser General Public License along with this it. If not, see http://www.gnu.org/licenses/lgpl-3.0.html.
 */

package edu.uic.prominent.processmining.decaypns.prom.model;

import java.io.File;

import org.processmining.acceptingpetrinet.models.impl.AcceptingPetriNetImpl;
import org.processmining.acceptingpetrinet.plugins.ExportAcceptingPetriNetPlugin;
import org.processmining.contexts.uitopia.UIContext;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;
import org.processmining.plugins.bpmn.plugins.BpmnExportPlugin;

import com.raffaeleconforti.wrappers.PetrinetWithMarking;

public class ModelUtils {
	
	private ModelUtils(){
	}
	
	 public static void exportBPMN(BPMNDiagram diagram, String path) {
	        BpmnExportPlugin bpmnExportPlugin = new BpmnExportPlugin();
	        UIContext context = new UIContext();
	        UIPluginContext uiPluginContext = context.getMainPluginContext();
	        try {
	            bpmnExportPlugin.export(uiPluginContext, diagram, new File(path));
	        } catch (Exception e) { System.out.println("ERROR - impossible to export .bpmn result of split-miner"); }
	    }
	    
	    public static void exportPetrinet(UIPluginContext context, PetrinetWithMarking petrinetWithMarking, String path) {
	        ExportAcceptingPetriNetPlugin exportAcceptingPetriNetPlugin = new ExportAcceptingPetriNetPlugin();
	        try {
	            exportAcceptingPetriNetPlugin.export(
	                    context,
	                    new AcceptingPetriNetImpl(petrinetWithMarking.getPetrinet(), petrinetWithMarking.getInitialMarking(), petrinetWithMarking.getFinalMarking()),
	                    new File(path));
	        } catch (Exception e) {
	            System.out.println("ERROR - impossible to export the petrinet to: " + path);
	            return;
	        }
	    }
}

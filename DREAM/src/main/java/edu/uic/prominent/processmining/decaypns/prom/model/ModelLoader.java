package edu.uic.prominent.processmining.decaypns.prom.model;

import org.deckfour.xes.classification.XEventNameClassifier;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.acceptingpetrinet.plugins.ImportAcceptingPetriNetPlugin;
import org.processmining.framework.plugin.PluginContext;

import com.raffaeleconforti.context.FakePluginContext;

public class ModelLoader {

	private FakePluginContext uiContext;
	private PluginContext context;
	
	public ModelLoader(){
		new XEventNameClassifier();
		this.uiContext = new FakePluginContext();
		this.context = uiContext.getParentContext();
	}
	
	public FakePluginContext getUiContext(){
		return this.uiContext;
	}
	
	public PluginContext getContext(){
		return this.context;
	}
	
	public AcceptingPetriNet importPetriNet(String path){
		ImportAcceptingPetriNetPlugin importplugin = new ImportAcceptingPetriNetPlugin();
		AcceptingPetriNet loadedNet = null;
		try {
			loadedNet = (AcceptingPetriNet) importplugin.importFile(this.uiContext, path);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return loadedNet;
	}
}

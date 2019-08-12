/*
* This basic PetriNet implementation is based on https://github.com/ErkoRisthein/conformance-checker
* 
* Copyright 2019 Erko Risthein
* 
* Copyrights licensed under the MIT Open-Source License
* https://opensource.org/licenses/MIT
* 
* Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
* The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/


package edu.uic.prominent.processmining.decaypns.petrinet;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static java.util.Collections.addAll;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Place implements Serializable {
	public static final Place NULL = new Place("null", 0);

	private String name;
	private Set<Transition> inputs = new HashSet<>();
	private Set<Transition> outputs = new HashSet<>();
	private int tokens;
	private int initTokens;
	
	private int finalTokens;

	public Place(String name, int initTokens) {
		this.name = name;
		this.initTokens = initTokens;
		this.finalTokens = -1;
	}
	
	
	public void replaceInputTransition2TimedTransition(Transition t, TimedTransition tt){
		inputs.remove(t);
		inputs.add(tt);
	}
	
	public void replaceOutputTransition2TimedTransition(Transition t, TimedTransition tt){
		outputs.remove(t);
		outputs.add(tt);
	}
	
	public boolean hasInputTransition(Transition t){
		return inputs.contains(t);
	}
	
	public boolean hasOutputTransition(Transition t){
		return outputs.contains(t);
	}
	
	public void setFinalTokens(int tokens){
		this.finalTokens = tokens;
	}
	
	public boolean hasFinalMarking(){
		if(this.finalTokens > 0){
			return true;
		}else{
			return false;
		}
	}
	
	public int getFinalMarking(){
		return this.finalTokens;
	}
	
	public boolean hasInitMarking(){
		if(this.initTokens > 0){
			return true;
		}else{
			return false;
		}
	}
	
	public int initTokens(){
		return this.initTokens;
	}
	
	public void setName(String name){
		this.name = name;
	}

	public String name() {
		return name;
	}


	public Place from(Transition... transitions) {
		addAll(inputs, transitions);
		return this;
	}

	public Place to(Transition... transitions) {
		addAll(outputs, transitions);
		return this;
	}

	public boolean hasZeroInputs() {
		return inputs.isEmpty();
	}

	public boolean hasZeroOutputs() {
		return outputs.isEmpty();
	}
	
	public  Set<Transition> getInputs() {
		return inputs;
	}
	
	public  Set<Transition> getOutputs() {
		return outputs;
	}

	public int getOutputCount() {
		return outputs.size();
	}

	public int getTokenCount() {
		return tokens;
	}

	public void addToken() {
		tokens++;
	}
	
	public void addTokens(int tokens){
		this.tokens += tokens;
	}

	public boolean hasTokens() {
		return tokens > 0;
	}

	public void removeToken() {
		tokens--;
	}
	
	public void removeTokens(int tokens) {
		this.tokens-= tokens;
	}

	public void removeAllTokens() {
		tokens = 0;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Place place = (Place) o;
		return Objects.equals(name, place.name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name);
	}

	@Override
	public String toString() {
		return inputs + "->" + name + "(" + tokens + ")->" + outputs;
	}
}


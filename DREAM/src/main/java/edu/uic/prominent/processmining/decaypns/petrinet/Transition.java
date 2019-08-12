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
public class Transition implements Serializable{
	public static final Transition NULL = new Transition("null");

	private final String name;
	private Set<Place> inputs = new HashSet<>();
	private Set<Place> outputs = new HashSet<>();
	private boolean visible;

	public Transition(String name) {
		this.name = name;
		this.visible = true;
	}
	public Transition(String name, boolean visible) {
		this.name = name;
		this.visible = visible;
	}
	public Transition(String name, Set<Place> inputs, Set<Place> outputs, boolean visible){
		this.name = name;
		this.inputs = inputs;
		this.outputs = outputs;
		this.visible = visible;
	}
	
	
	public boolean isVisible(){
		return this.visible;
	}

	public String name() {
		return name;
	}

	public Transition from(Place... places) {
		addAll(inputs, places);
		return this;
	}

	public Transition to(Place... places) {
		addAll(outputs, places);
		return this;
	}
	
	public Set<Place> getOutputs(){
		return outputs;
	}
	
	public Set<Place> getInputs(){
		return inputs;
	}

	public void consumeInputTokens() {
		inputs.forEach(Place::removeToken);
	}

	public void produceOutputTokens() {
		outputs.forEach(Place::addToken);
	}

	public void createMissingToken() {
		inputs.stream().filter(input -> !input.hasTokens()).forEach(Place::addToken);
	}

	public boolean hasAllInputTokens() {
		return inputs.stream().allMatch(Place::hasTokens);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Transition that = (Transition) o;
		return Objects.equals(name, that.name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name);
	}

	@Override
	public String toString() {
		return name;
	}	
}


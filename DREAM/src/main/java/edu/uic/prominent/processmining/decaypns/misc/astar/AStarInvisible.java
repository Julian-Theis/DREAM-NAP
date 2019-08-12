/*
 * Copyright (c) 2016-2019 Raymond Chua
 * 
 * https://gist.github.com/raymondchua/8064159
 */

package edu.uic.prominent.processmining.decaypns.misc.astar;

import java.util.PriorityQueue;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

import edu.uic.prominent.processmining.decaypns.petrinet.PetriNet;
import edu.uic.prominent.processmining.decaypns.petrinet.Place;
import edu.uic.prominent.processmining.decaypns.petrinet.Transition;

import java.util.List;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.Collections;

public class AStarInvisible{

	private PetriNet pn;
	private List<Node> nodes;

	public AStarInvisible(PetriNet pn) {
		this.pn = pn;
		this.nodes = new ArrayList<Node>();
		setUp();
		
		// If length == 0, then it is directly executable
		//System.out.println(search("p1", "E"));
		//System.out.println(search("E", "p1"));
	}

	public void setUp() {
		List<Place> places = pn.getPlaces();
		List<Transition> transitions = pn.getTransitions();
		
		// Create initial set of nodes
		for (Place p : places) {
			Node n = new Node(p.name(), 1);
			n.isPlace = true;
			this.nodes.add(n);
		}
		for (Transition t : transitions) {
			if(!t.isVisible()){
				Node n = new Node(t.name(), 1);
				n.isInvisible = !t.isVisible();
				n.isTransition = true;
				this.nodes.add(n);
			}
		}
		

		// Create adjacencies
		
		for (Place p : places) {
			Node p_node = getNode(p.name());			
			Iterator<Transition> iterator = p.getOutputs().iterator();
			
			/** Muss das hier auskommentiert bleiben? **/
			while (iterator.hasNext()) {
				//System.out.println("has out");
				Transition t = iterator.next();
				if(!t.isVisible()){
					p_node.addAdjacentNode(getNode(t.name()));
				}
			}
			
			
			iterator = p.getInputs().iterator();
			while (iterator.hasNext()) {
				Transition t = iterator.next();
				if(!t.isVisible()){
					p_node.addAdjacentNode(getNode(t.name()));
				}
			}
		}
		
		
		
		for (Transition t : transitions) {
			if(!t.isVisible()){
				Node t_node = getNode(t.name());
				Iterator<Place> iterator = t.getOutputs().iterator();
				while (iterator.hasNext()) {
					t_node.addAdjacentNode(getNode(iterator.next().name()));
				}
				iterator = t.getInputs().iterator();
				while (iterator.hasNext()) {
					t_node.addAdjacentNode(getNode(iterator.next().name()));
				}
			}
		}

		// Create edges
		for (Node n : nodes) {
			n.createAdjacenciesArray();
		}
	}

	public int search(String start, String end) {
		Node a = getNode(start);
		Node z = getNode(end);
		search(a, z);
		List<Node> path = printPath(z);
		//System.out.println("INVISIBLE Path for  " + start + " to " + end + " is " + path);
		reset();
		return path.size();
	}
	
	public List<Node> searchPath(String start, String end){
		Node a = getNode(start);
		Node z = getNode(end);
		
		if(a == null || z == null){
			List<Node> path = new ArrayList<Node>();
			//System.out.println(z);
			return path;
		}else{
			//System.out.println(a.value + " " + z.value);
			search(a, z);
			List<Node> path = printPath(z);
			//System.out.println("INVISIBLE Path for  " + start + " to " + end + " is " + path);
			reset();
			return path;
		}
	}
	
	private void reset(){
		for(Node n : nodes){
			n.reset();
		}
	}

	private Node getNode(String name) {
		Node f = null;
		for(Node n : nodes) {
			if (n.toString().equals(name)) {
				f = n;
				break;
			}
		}
		return f;
	}

	public static List<Node> printPath(Node target) {
		int threshold = 500;
		int count = 0;
		List<Node> path = new ArrayList<Node>();

		for (Node node = target; node != null; node = node.parent) {
			if(count < threshold)
				path.add(node);
			else
				break;
			count++;
		}
		Collections.reverse(path);
		List<Node> list = path.stream().distinct().collect(Collectors.toList());

		return list;
	}

	public static void search(Node source, Node goal) {
		Set<Node> explored = new HashSet<Node>();

		PriorityQueue<Node> queue = new PriorityQueue<Node>(20, new Comparator<Node>() {
			// override compare method
			public int compare(Node i, Node j) {
				if (i.f_scores > j.f_scores) {
					return 1;
				}

				else if (i.f_scores < j.f_scores) {
					return -1;
				}

				else {
					return 0;
				}
			}

		});

		// cost from start
		source.g_scores = 0;

		queue.add(source);

		boolean found = false;
		
		while ((!queue.isEmpty()) && (!found)) {

			// the node in having the lowest f_score value
			Node current = queue.poll();

			explored.add(current);

			// goal found
			if (current.value.equals(goal.value)) {
				found = true;
			}

			
			// check every child of current node
			for (Edge e : current.adjacencies) {
				Node child = e.target;
				double cost = e.cost;
				double temp_g_scores = current.g_scores + cost;
				double temp_f_scores = temp_g_scores + child.h_scores;
				
				/*
				 * if child node has been evaluated and the newer f_score is
				 * higher, skip
				 */

				if ((explored.contains(child)) && (temp_f_scores >= child.f_scores)) {
					//System.out.println("continue " + found);
					continue;
				}

				/*
				 * else if child node is not in queue or newer f_score is lower
				 */

				else if ((!queue.contains(child)) || (temp_f_scores < child.f_scores)) {

					child.parent = current;
					child.g_scores = temp_g_scores;
					child.f_scores = temp_f_scores;
					
					//System.out.println(temp_g_scores + " " + temp_f_scores);

					if (queue.contains(child)) {
						queue.remove(child);
					}

					queue.add(child);
				}
			}
		}
	}
}
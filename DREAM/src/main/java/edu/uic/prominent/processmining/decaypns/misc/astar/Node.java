package edu.uic.prominent.processmining.decaypns.misc.astar;

import java.util.ArrayList;
import java.util.List;

public class Node {
	 public final String value;
     public double g_scores;
     public final double h_scores;
     public double f_scores = 0;
     public Edge[] adjacencies;
     public Node parent;
     
     public boolean isPlace = false;
     public boolean isTransition = false;
     public boolean isInvisible = false;
     
     private List<Node> adjacentNodes;

     public Node(String val, double hVal){
             this.value = val;
             this.h_scores = hVal;
             this.adjacentNodes = new ArrayList<Node>();
     }
          
     public void reset(){
    	 this.f_scores = 0;
    	 this.g_scores = 0;
     }

     public String toString(){
             return value;
     }
     
     public void addAdjacentNode(Node n){
    	 this.adjacentNodes.add(n);
     }
     
     public void createAdjacenciesArray(){
    	 List<Edge> edges = new ArrayList<Edge>();
    	 for(Node n : adjacentNodes){
    		 edges.add(new Edge(n, 1));
    	 }
    	 this.adjacencies = edges.toArray(new Edge[edges.size()]);
     }
}

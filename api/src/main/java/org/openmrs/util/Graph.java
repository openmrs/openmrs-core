/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Graph<T> {
	
	private Set<T> nodes = new HashSet<T>();
	
	private Set<Edge> edges = new HashSet<Edge>();
	
	public class Edge {
		
		private T fromNode;
		
		private T toNode;
		
		public Edge(T aFromNode, T aToNode) {
			fromNode = aFromNode;
			toNode = aToNode;
		}
		
		public T getFromNode() {
			return fromNode;
		}
		
		public void setFromNode(T fromNode) {
			this.fromNode = fromNode;
		}
		
		public T getToNode() {
			return toNode;
		}
		
		public void setToNode(T toNode) {
			this.toNode = toNode;
		}
		
		@Override
		public String toString() {
			return fromNode.toString() + "->" + toNode.toString();
		}
		
	}
	
	public void addNode(T aNode) {
		nodes.add(aNode);
	}
	
	public void addEdge(Edge anEdge) {
		edges.add(anEdge);
	}
	
	public Set<T> getNodes() {
		return nodes;
	}
	
	public Set<Edge> getEdges() {
		return edges;
	}
	
	public T getNode(T element) {
		for (T node : nodes) {
			if (node.equals(element)) {
				return node;
			}
		}
		return null;
	}
	
	/**
	 * Obtains all nodes without incoming edges 
	 * @return
	 */
	private Set<T> getNodesWithNoIncomingEdges() {
		Set<T> nodesWithIncomingEdges = new HashSet<T>();
		Set<T> nodesWithoutIncomingEdges = new HashSet<T>();
		for (Edge edge : edges) {
			nodesWithIncomingEdges.add(edge.getToNode());
		}
		nodesWithoutIncomingEdges.addAll(nodes);
		for (T node : nodesWithIncomingEdges) {
			nodesWithoutIncomingEdges.remove(node);
		}
		return nodesWithoutIncomingEdges;
	}
	
	/**
	 * Determines if a node has incoming edges 
	 * @param node
	 * @return
	 */
	private boolean hasIncomingEdges(T node) {
		for (Edge edge : edges) {
			if (edge.getToNode().equals(node)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Obtains the edges starting with a given node 
	 * @param aNode
	 * @return
	 */
	private Set<Edge> getEdgesStartingWith(T aNode) {
		Set<Edge> edgesPointing = new HashSet<Edge>();
		for (Edge edge : edges) {
			if (edge.getFromNode().equals(aNode)) {
				edgesPointing.add(edge);
			}
		}
		return edgesPointing;
	}
	
	/**
	 * Obtains the edges ending with a given node 
	 * @param aNode
	 * @return
	 */
	public Set<Edge> getEdgesEndingWith(T aNode) {
		Set<Edge> edgesPointing = new HashSet<Edge>();
		for (Edge edge : edges) {
			if (edge.getToNode().equals(aNode)) {
				edgesPointing.add(edge);
			}
		}
		return edgesPointing;
	}
	
	/**
	 * Sort a graph in topological order
	 * 
	 * @return
	 * @throws CycleException
	 */
	public List<T> topologicalSort() throws CycleException {
		
		Set<T> queue = getNodesWithNoIncomingEdges();
		List<T> result = new ArrayList<T>();
		
		// The initial edges are stored.
		List<Edge> initialEdges = new ArrayList<Edge>();
		initialEdges.addAll(edges);
		while (!queue.isEmpty()) {
			T node = queue.iterator().next();
			queue.remove(node);
			result.add(node);
			Set<Edge> edgesStarting = getEdgesStartingWith(node);
			for (Edge edge : edgesStarting) {
				edges.remove(edge);
				if (!hasIncomingEdges(edge.getToNode())) {
					queue.add(edge.getToNode());
				}
			}
		}
		if (!edges.isEmpty()) {
			throw new CycleException();
		}
		// The old edges are restored in order to maintain the graph integrity.
		edges.addAll(initialEdges);
		return result;
	}
	
}

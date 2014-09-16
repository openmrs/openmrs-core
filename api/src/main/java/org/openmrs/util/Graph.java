package org.openmrs.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;

public class Graph<T> {

	private Set<Node<T>> nodes = new HashSet<Node<T>>();
	private Set<Edge> edges = new HashSet<Edge>();
	
	public static class Node<E> {
		
		private E obj;

		public Node(E anObject) {
			obj = anObject;
		}

		public E getObj() {
			return obj;
		}

		public void setObj(E obj) {
			this.obj = obj;
		}

		@Override
		public String toString() {
			return obj.toString();
		}

		@Override
		public int hashCode() {
			return obj.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			return ObjectUtils.equals(this, obj);
		}
		
	}
	
	public class Edge {
		
		private Node<T> fromNode;
		private Node<T> toNode;

		public Edge(Node<T> aFromNode, Node<T> aToNode) {
			fromNode = aFromNode;
			toNode = aToNode;
		}

		public Node<T> getFromNode() {
			return fromNode;
		}

		public void setFromNode(Node<T> fromNode) {
			this.fromNode = fromNode;
		}

		public Node<T> getToNode() {
			return toNode;
		}

		public void setToNode(Node<T> toNode) {
			this.toNode = toNode;
		}

		@Override
		public String toString() {
			return fromNode.toString() + "->" + toNode.toString();
		}

	}	

	public void addNode(Node<T> aNode) {
		nodes.add(aNode);
	}

	public void addEdge(Edge anEdge) {
		edges.add(anEdge);
	}

	public Set<Node<T>> getNodes() {
		return nodes;
	}
	
	public Set<Edge> getEdges() {
		return edges;
	}
	
	public Node<T> getNode(T element) {
		for (Node<T> node : nodes) {
			if (node.getObj().equals(element)) {
				return node;
			}
		}
		return null;
	}

	/**
	 * Obtains all nodes without incoming edges 
	 * @return
	 */
	private Set<Node<T>> getNodesWithNoIncomingEdges() {
		Set<Node<T>> nodesWithIncomingEdges = new HashSet<Node<T>>();
		Set<Node<T>> nodesWithoutIncomingEdges = new HashSet<Node<T>>();
		for (Edge edge : edges) {
			nodesWithIncomingEdges.add(edge.getToNode());
		}
		nodesWithoutIncomingEdges.addAll(nodes);
		for (Node<T> node : nodesWithIncomingEdges) {
			nodesWithoutIncomingEdges.remove(node);
		}
		return nodesWithoutIncomingEdges;
	}

	/**
	 * Determines if a node has incoming edges 
	 * @param node
	 * @return
	 */
	private boolean hasIncomingEdges(Node<T> node) {
		for (Edge edge : edges) {
			if (edge.getToNode().getObj().equals(node.getObj())) {
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
	private Set<Edge> getEdgesStartingWith(Node<T> aNode) {
		Set<Edge> edgesPointing = new HashSet<Edge>();
		for (Edge edge : edges) {
			if (edge.getFromNode().getObj().equals(aNode.getObj())) {
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
	public Set<Edge> getEdgesEndingWith(Node<T> aNode) {
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
	public List<Node<T>> topologicalSort() throws CycleException {
		
		Set<Node<T>> queue = getNodesWithNoIncomingEdges();
		List<Node<T>> result = new ArrayList<Node<T>>();
		
		// The initial edges are stored.
		List<Edge> initialEdges = new ArrayList<Edge>();
		initialEdges.addAll(edges);
		while (!queue.isEmpty()) {
			Node<T> node = queue.iterator().next();
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
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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Tests the methods on the {@link Graph} class
 */
public class GraphTest {
	
	/**
	 * @throws CycleException 
	 * @see Graph#topologicalSort()
	 */
	@Test
	public void topologicalSort_shouldSortGraphInTopologicalOrder() throws CycleException {
		
		Graph<String> graph = new Graph<>();
		
		graph.addNode("E");
		graph.addNode("D");
		graph.addNode("C");
		graph.addNode("B");
		graph.addNode("A");
		
		graph.addEdge(graph.new Edge(
		                             "A", "B"));
		graph.addEdge(graph.new Edge(
		                             "B", "C"));
		graph.addEdge(graph.new Edge(
		                             "A", "C"));
		graph.addEdge(graph.new Edge(
		                             "B", "D"));
		graph.addEdge(graph.new Edge(
		                             "D", "E"));
		
		List<String> sortedNodes = graph.topologicalSort();
		assertTrue(sortedNodes.indexOf("A") < sortedNodes.indexOf("B"));
		assertTrue(sortedNodes.indexOf("A") < sortedNodes.indexOf("C"));
		assertTrue(sortedNodes.indexOf("B") < sortedNodes.indexOf("C"));
		assertTrue(sortedNodes.indexOf("B") < sortedNodes.indexOf("D"));
		assertTrue(sortedNodes.indexOf("D") < sortedNodes.indexOf("E"));
	}
	
	/**
	 * @throws CycleException 
	 * @see Graph#topologicalSort()
	 */
	@Test
	public void topologicalSort_shouldThrowCycleException() throws CycleException {
		
		Graph<String> graph = new Graph<>();
		
		graph.addNode("E");
		graph.addNode("D");
		graph.addNode("C");
		graph.addNode("B");
		graph.addNode("A");
		
		graph.addEdge(graph.new Edge(
		                             "A", "B"));
		graph.addEdge(graph.new Edge(
		                             "A", "C"));
		graph.addEdge(graph.new Edge(
		                             "B", "C"));
		graph.addEdge(graph.new Edge(
		                             "B", "D"));
		graph.addEdge(graph.new Edge(
		                             "D", "A"));
		
		assertThrows(CycleException.class, () -> graph.topologicalSort());
	}
	
}

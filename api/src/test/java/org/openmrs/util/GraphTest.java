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

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.test.Verifies;

/**
 * Tests the methods on the {@link Graph} class
 */
public class GraphTest {
	
	/**
	 * @throws CycleException 
	 * @see {@link Graph#topologicalSort()}
	 */
	@Test
	@Verifies(value = "should sort graph in topological order", method = "topologicalSort()")
	public void topologicalSort_shouldSortGraphInTopologicalOrder() throws CycleException {
		
		Graph<String> graph = new Graph<String>();
		
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
		Assert.assertTrue(sortedNodes.indexOf("A") < sortedNodes.indexOf("B"));
		Assert.assertTrue(sortedNodes.indexOf("A") < sortedNodes.indexOf("C"));
		Assert.assertTrue(sortedNodes.indexOf("B") < sortedNodes.indexOf("C"));
		Assert.assertTrue(sortedNodes.indexOf("B") < sortedNodes.indexOf("D"));
		Assert.assertTrue(sortedNodes.indexOf("D") < sortedNodes.indexOf("E"));
	}
	
	/**
	 * @throws CycleException 
	 * @see {@link Graph#topologicalSort()}
	 */
	@Test(expected = CycleException.class)
	@Verifies(value = "should throw CycleException", method = "topologicalSort()")
	public void topologicalSort_shouldThrowCycleException() throws CycleException {
		
		Graph<String> graph = new Graph<String>();
		
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
		
		graph.topologicalSort();
	}
	
}

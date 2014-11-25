/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
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

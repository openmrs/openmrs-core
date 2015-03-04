/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.Matchers.is;
import static org.openmrs.test.OpenmrsMatchers.hasId;

import java.util.List;

import javax.annotation.Resource;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.openmrs.Concept;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.SkipBaseSetup;

import com.carrotsearch.junitbenchmarks.BenchmarkRule;
import com.carrotsearch.junitbenchmarks.annotation.BenchmarkHistoryChart;

/**
 * Performance tests for {@link ConceptService}.
 */
@SkipBaseSetup
@BenchmarkHistoryChart
public class ConceptServicePT extends BaseContextSensitiveTest {
	
	@Rule
	public TestRule benchmarkRule = new BenchmarkRule();
	
	@Resource(name = "conceptService")
	ConceptService conceptService;
	
	static boolean dictionaryLoaded = false;
	
	@Before
	public void loadDictionary() throws Exception {
		if (!dictionaryLoaded) {
			initializeInMemoryDatabase();
			
			executeLargeDataSet("org/openmrs/contrib/mvpconceptdictionary/dbunit.xml");
			
			getConnection().commit();
			
			updateSearchIndex();
			
			dictionaryLoaded = true;
		}
		
		authenticate();
	}
	
	@Test
	public void shouldTestGetConceptsByName() {
		List<Concept> concepts = conceptService.getConceptsByName("hiv positive");
		
		assertThat(concepts, containsInAnyOrder(hasId(138571), hasId(159804)));
	}
	
	@Test
	public void shouldTestGetConceptByName() {
		Concept concept = conceptService.getConceptByName("hiv positive");
		
		assertThat(concept, hasId(138571));
	}
	
	@Test
	public void shouldReturnDiabetesMellitusFirstForDiabetesMellit() {
		List<Concept> concepts = conceptService.getConceptsByName("diabetes mellit", null, false);
		
		assertThat(concepts.get(0).getName().getName(), equalToIgnoringCase("diabetes mellitus"));
	}
}

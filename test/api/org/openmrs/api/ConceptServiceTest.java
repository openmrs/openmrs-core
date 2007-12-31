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
package org.openmrs.api;

import java.util.List;

import org.openmrs.BaseContextSensitiveTest;
import org.openmrs.Concept;
import org.openmrs.api.context.Context;

/**
 * This test class (should) contain tests for all of the ConcepService methods
 * 
 * TODO clean up and finish this test class
 * 
 * @see org.openmrs.api.ConceptService
 */
public class ConceptServiceTest extends BaseContextSensitiveTest {
	
	protected ConceptService conceptService;
	protected static final String INITIAL_CONCEPTS_XML = "org/openmrs/include/ConceptServiceTest-initialConcepts.xml";
	
	/**
	 * Authenticate the user for all of the tests
	 * 
	 * @see org.openmrs.BaseTest#onSetUpBeforeTransaction()
	 */
	@Override
	protected void onSetUpInTransaction() throws Exception {
		initializeInMemoryDatabase();
		authenticate();
		conceptService = Context.getConceptService();
	}

	/**
	 * Test getting a concept by name and by partial name. 
	 * 
	 * NOTE: This test assumes that there are at least a few concepts 
	 * in the database system
	 * 
	 * @throws Exception
	 */
	public void testGetConceptByName() throws Exception {
		
		executeDataSet(INITIAL_CONCEPTS_XML);
		
		// get the first concept in the dictionary
		Concept firstConcept = conceptService.getConcept(1);
		assertNotNull("There should be a concept with id of 1", firstConcept);
		
		// get the concept by its name
		String name = firstConcept.getName().getName();
		Concept firstConceptByName = conceptService.getConceptByName(name);
		assertNotNull("You should be able to get this concept by name", firstConceptByName);
		
		// substring the name and try to get it again
		List<Concept> firstConceptsByPartialNameList = conceptService.getConceptsByName(name.substring(1));
		assertTrue("You should be able to get the concept by partial name", firstConceptsByPartialNameList.contains(firstConcept));
		
	}

}

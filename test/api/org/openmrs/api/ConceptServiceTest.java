package org.openmrs.api;

import java.util.List;

import org.openmrs.BaseTest;
import org.openmrs.Concept;
import org.openmrs.api.context.Context;

/**
 * This test class (should) contain tests for all of the ConcepService methods
 * 
 * @see org.openmrs.api.ConceptService
 */
public class ConceptServiceTest extends BaseTest {
	
	protected ConceptService conceptService;

	/**
	 * Authenticate the user for all of the tests
	 * 
	 * @see org.openmrs.BaseTest#onSetUpBeforeTransaction()
	 */
	@Override
	protected void onSetUpBeforeTransaction() throws Exception {
		super.onSetUpBeforeTransaction();
		authenticate();
		conceptService = Context.getConceptService();
	}

	/**
	 * Test getting a concept by name and by partial name. 
	 * Assumes there are at least a few concepts in the system
	 * 
	 * @throws Exception
	 */
	public void testGetConceptByName() throws Exception {
		// get the first concept in the dictionary
		Concept firstConcept = conceptService.getConcept(1);
		assertNotNull(firstConcept);
		
		// get the concept by its name
		String name = firstConcept.getName().getName();
		Concept firstConceptByName = conceptService.getConceptByName(name);
		assertNotNull(firstConceptByName);
		
		// substring the name and try to get it again
		List<Concept> firstConceptsByPartialNameList = conceptService.getConceptsByName(name.substring(1));
		assertTrue(firstConceptsByPartialNameList.contains(firstConcept));
		
	}

}

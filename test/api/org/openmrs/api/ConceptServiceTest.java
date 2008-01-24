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

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.BaseContextSensitiveTest;
import org.openmrs.Concept;
import org.openmrs.ConceptAnswer;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptDatatype;
import org.openmrs.ConceptName;
import org.openmrs.ConceptSet;
import org.openmrs.ConceptSynonym;
import org.openmrs.api.context.Context;

/**
 * This test class (should) contain tests for all of the ConcepService methods
 * 
 * TODO clean up and finish this test class
 * 
 * @see org.openmrs.api.ConceptService
 */
public class ConceptServiceTest extends BaseContextSensitiveTest {
	
	protected final Log log = LogFactory.getLog(getClass());
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
		
		log.info("First concept: " + firstConcept.getName());
		
		// get the concept by its name
		String name = firstConcept.getName().getName();
		Concept firstConceptByName = conceptService.getConceptByName(name);
		assertNotNull("You should be able to get this concept by name", firstConceptByName);
		
		// substring the name and try to get it again
		List<Concept> firstConceptsByPartialNameList = conceptService.getConceptsByName(name.substring(1));
		assertTrue("You should be able to get the concept by partial name", firstConceptsByPartialNameList.contains(firstConcept));
		
	}
	
	
	/**
	 *	Adds a concept name to an existing concept. 
	 * 
	 * @throws Exception
	 */
	public void testAddConceptName() throws Exception { 		
		executeDataSet(INITIAL_CONCEPTS_XML);
		
		// get the first concept in the dictionary
		Concept weight = conceptService.getConcept(1);
		assertNotNull("WEIGHT should not be null", weight);
		
		assertTrue("WEIGHT should have 2 names", 
		           weight.getNames().size() == 2);
		
		// Add a new concept name
		ConceptName conceptName = new ConceptName();
		conceptName.setConcept(weight);
		conceptName.setName("New Name");
		conceptName.setShortName("New Short Name");		
		conceptName.setDescription("New Description");
		conceptName.setDateCreated(new Date());
		conceptName.setLocale("en");
		conceptName.setGuid(UUID.randomUUID().toString());
		weight.addName(conceptName);
		
		// Update the concept
		conceptService.updateConcept(weight);
	
		// Get the same concept and check the concept names
		Concept testConcept = conceptService.getConcept(1);
		
		assertTrue("WEIGHT should have 3 names", 
		           testConcept.getNames().size() == 3);
		
	}

	/**
	 *	Removes a concept name from an existing concept. 
	 * 
	 * @throws Exception
	 */
	public void testRemoveConceptName() throws Exception { 
		executeDataSet(INITIAL_CONCEPTS_XML);
		
		// get the first concept in the dictionary
		Concept civilStatus = conceptService.getConcept(2);
		assertNotNull("CIVIL STATUS should not be null", civilStatus);

		assertTrue("CIVIL STATUS should have 1 names", 
		           civilStatus.getNames().size() == 1);

		// Remove the concepts first name
		ConceptName conceptName = civilStatus.getNames().iterator().next();		
		civilStatus.removeName(conceptName);
		
		// Update the concept
		conceptService.updateConcept(civilStatus);
	
		// Get the same concept and check the concept names
		Concept testConcept = conceptService.getConcept(2);
		
		assertTrue("CIVIL STATUS should have 0 names", 
		           testConcept.getNames().size() == 0);
	}	
	
	
	/**
	 *	Adds a concept name to an existing concept. 
	 * 
	 * @throws Exception
	 */
	public void testAddConceptAnswer() throws Exception { 		
		executeDataSet(INITIAL_CONCEPTS_XML);
		
		// get the first concept in the dictionary
		Concept civilStatus = conceptService.getConcept(2);
		assertNotNull("CIVIL STATUS concept should not be null", civilStatus);
		
		Concept unmarried = conceptService.getConcept(6);
		assertNotNull("UNMARRIED concept should not be null", civilStatus);

		// Make sure the answer does not already exist
		assertTrue("CIVIL STATUS should have 3 answer", 
		           civilStatus.getAnswers().size() == 3);		
		
		// Create a new concept name
		ConceptAnswer conceptAnswer = new ConceptAnswer();
		conceptAnswer.setConcept(civilStatus);
		conceptAnswer.setAnswerConcept(unmarried);
		conceptAnswer.setAnswerDrug(null);
		conceptAnswer.setDateCreated(new Date());
		conceptAnswer.setGuid(UUID.randomUUID().toString());	
		
		
		// Add answer to concept
		civilStatus.addAnswer(conceptAnswer);
		
		// Update the concept in the database
		conceptService.updateConcept(civilStatus);		

		// Check to make sure the concept answer was added successfully
		Concept testConcept = conceptService.getConcept(2);

		assertTrue("CIVIL STATUS should have 4 answers", 
		           testConcept.getAnswers().size() == 4);
	}

	/**
	 * Removes a concept name from an existing concept. 
	 * 
	 * @throws Exception
	 */
	public void testRemoveConceptAnswer() throws Exception { 
		executeDataSet(INITIAL_CONCEPTS_XML);
		
		// get the first concept in the dictionary
		Concept civilStatus = conceptService.getConcept(2);
		assertNotNull("CIVIL STATUS should not be null", civilStatus);
			
		// Make sure the answer does not already exist
		assertTrue("CIVIL STATUS should have 3 answers", 
		           civilStatus.getAnswers().size()==3);
		
		// Remove the concepts first name
		ConceptAnswer conceptAnswer = civilStatus.getAnswers().iterator().next();		
		
		// Remove answer from concept
		civilStatus.removeAnswer(conceptAnswer);
		
		// Update the concept in the database
		conceptService.updateConcept(civilStatus);		

		// Check to make sure the concept answer was added successfully
		Concept testConcept = conceptService.getConcept(2);

		assertTrue("CIVIL STATUS should have 2 answers", 
		           testConcept.getAnswers().size() == 2);
		
	}	
	

	/**
	 *	Adds a concept name to an existing concept. 
	 * 
	 * @throws Exception
	 */
	public void testAddConceptSynonym() throws Exception { 		
		executeDataSet(INITIAL_CONCEPTS_XML);
		
		// get the first concept in the dictionary
		Concept civilStatus = conceptService.getConcept(2);
		assertNotNull("There should be a CIVIL STATUS concept", civilStatus);
		
		// Check to make sure the concept answer was added successfully
		assertTrue("CIVIL STATUS should have only 0 synonym", 
		           civilStatus.getSynonyms().size()==0);		
		
		// Add a new concept name
		ConceptSynonym conceptSynonym = new ConceptSynonym();
		conceptSynonym.setConcept(civilStatus);
		conceptSynonym.setLocale("en");
		conceptSynonym.setSynonym("MARITAL STATUS");
		conceptSynonym.setDateCreated(new Date());
		conceptSynonym.setGuid(UUID.randomUUID().toString());
		
		
		civilStatus.addSynonym(conceptSynonym);
		
		// Update the concept
		conceptService.updateConcept(civilStatus);
	
		// Check to make sure the concept answer was added successfully
		Concept testConcept = conceptService.getConcept(2);
		
		assertTrue("CIVIL STATUS should have 1 synonym", 
		           testConcept.getSynonyms().size()==1);
		
	}

	/**
	 * Removes a concept name from an existing concept. 
	 * 
	 * @throws Exception
	 */
	public void testRemoveConceptSynonym() throws Exception { 
		executeDataSet(INITIAL_CONCEPTS_XML);
		
		// get the fifth concept in the dictionary
		Concept civilStatusWidowed = conceptService.getConcept(5);
		assertNotNull("There should be a civil status concept", civilStatusWidowed);

		// Check to make sure the concept answer was added successfully
		assertTrue("Concept " + civilStatusWidowed.getName() + " should have only 1 synonym", 
		           civilStatusWidowed.getSynonyms().size()==1);		
		
		
		// Remove the concepts first synonym
		ConceptSynonym conceptSynonym = civilStatusWidowed.getSynonyms().iterator().next();		
		civilStatusWidowed.removeSynonym(conceptSynonym);		
		conceptService.updateConcept(civilStatusWidowed);
	
		// Get the same concept and check the concept names
		Concept testConcept = conceptService.getConcept(2);
		
		// Check to make sure the concept answer was added successfully
		assertTrue("Concept " + testConcept.getName() + " should have 0 synonyms", 
		           testConcept.getSynonyms().size()==0);		
	}		
	
	
	/**
	 * Method to test concept set capaabilities.
	 * 
	 * @throws Exception
	 */
	public void testConceptSet() throws Exception { 
		executeDataSet(INITIAL_CONCEPTS_XML);
		
		// get the fifth concept in the dictionary
		Concept vitalSigns = conceptService.getConcept(1114);
		assertNotNull("VITAL SIGNS should not be null", vitalSigns);
		assertTrue("VITAL SIGNS should be a set", vitalSigns.isSet());
		
		for (ConceptSet set : vitalSigns.getConceptSets()) { 			
			log.info("Concept set: " + set.getConcept().getName());
		}
		
		
		//conceptService.getConceptSetByGuid(guid);
		//conceptService.getConceptSets(c);
		//conceptService.getConceptSetDerivedByGuid(guid);
		
		ConceptClass testClass = conceptService.getConceptClass(1);
		ConceptDatatype numericDatatype = conceptService.getConceptDatatype(1);
		
		Concept newConcept = new Concept();
		newConcept.setConceptId(5283);
		newConcept.setDatatype(numericDatatype);
		newConcept.setAnswers(null);
		newConcept.setSet(false);
		newConcept.setGuid("922d768c-11ef-102b-a33e-82966c5b8177");
		newConcept.setConceptClass(testClass);

		ConceptName newName = new ConceptName();
		newName.setConceptNameId(3387);
		newName.setConcept(newConcept);
		newName.setDescription("Zero to 100 scale commonly used for assessing terminally ill patients.");
		newName.setName("KARNOFSKY PERFORMANCE SCORE");
		newName.setGuid("92b1580d-11ef-102b-a33e-82966c5b8177");
		newName.setLocale("en");
		newConcept.addName(newName);
		
		
		conceptService.updateConcept(newConcept);
		
		
		ConceptSet set = new ConceptSet();		
		set.setConcept(newConcept);
		set.setConceptSet(vitalSigns);
		set.setGuid("92b1570d-11ef-102b-a33e-82966c5b8177");
		set.setDateCreated(new Date());
		set.setSortWeight(1.0);

		vitalSigns.getConceptSets().add(set);
		
		conceptService.createConceptSet(set);

		
		//conceptService.createConceptSet(set, isForced);
		
		conceptService.createConceptSet(set);
		
		Concept testConcept = conceptService.getConcept(1114);		
		for (ConceptSet testSet : testConcept.getConceptSets()) { 			
			log.info("Concept set: " + testSet.getConcept().getName());
		}		
		
		//	Check to make sure the concept answer was added successfully
		assertTrue("Concept " + testConcept.getName() + " should have 5 concepts in its set", 
		           testConcept.getConceptSets().size()==5);			
		
	}
	
}

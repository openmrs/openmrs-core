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
package org.openmrs.api.db;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;

/**
 *
 */
public class ConceptDAOTest extends BaseContextSensitiveTest {
	
	private ConceptDAO dao = null;
	
	/**
	 * Run this before each unit test in this class.
	 * 
	 * @throws Exception
	 */
	@Before
	public void runBeforeEachTest() throws Exception {
		
		if (dao == null)
			// fetch the dao from the spring application context
			dao = (ConceptDAO) applicationContext.getBean("conceptDAO");
	}
	
	@Test
	@Verifies(value = "should delete concept from datastore", method = "purgeConcept")
	public void purgeConcept_shouldDeleteConceptWithWords() throws Exception {
		executeDataSet("org/openmrs/api/include/ConceptServiceTest-words.xml");
		Concept concept = dao.getConcept(5497);
		dao.purgeConcept(concept);
		
		assertNull(dao.getConcept(5497));
	}
	
	@Test
	@Verifies(value = "should update concept in datastore", method = "updateConcept")
	public void updateConceptWord_shouldUpdateConceptWithWords() throws Exception {
		executeDataSet("org/openmrs/api/include/ConceptServiceTest-words.xml");
		Concept concept = dao.getConcept(5497);
		dao.updateConceptWord(concept);
		
		assertNotNull(dao.getConcept(5497));
	}
	
}

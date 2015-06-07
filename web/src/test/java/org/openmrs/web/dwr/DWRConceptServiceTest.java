/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.dwr;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.GlobalProperty;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsUtil;
import org.openmrs.web.test.BaseWebContextSensitiveTest;

public class DWRConceptServiceTest extends BaseWebContextSensitiveTest {
	
	private DWRConceptService dwrConceptService = new DWRConceptService();
	
	@Before
	public void before() throws Exception {
		executeDataSet("org/openmrs/web/dwr/include/DWRConceptServiceTest-coded-concept-with-no-answers.xml");
		updateSearchIndex();
	}
	
	/**
	 * @see DWRConceptService#findConceptAnswers(String,Integer,boolean,boolean)
	 * @verifies not fail if the specified concept has no answers (regression test for TRUNK-2807)
	 */
	@Test
	public void findConceptAnswers_shouldNotFailIfTheSpecifiedConceptHasNoAnswersRegressionTestForTRUNK2807()
	        throws Exception {
		dwrConceptService.findConceptAnswers("", 1000, false, true);
		// if we got here, we've passed, because we didn't get a NullPointerException
	}
	
	/**
	 * @see DWRConceptService#findBatchOfConcepts(String, boolean, java.util.List, java.util.List,
	 *      java.util.List, java.util.List, Integer, Integer)
	 * @verifies return concept by given id if exclude and include lists are empty
	 */
	@Test
	public void findBatchOfConcepts_shouldReturnConceptByGivenIdIfExclusionAndInclusionListsAreEmpty() throws Exception {
		String phrase = "1000";
		Concept expected = Context.getConceptService().getConcept(phrase);
		List<Object> result = dwrConceptService.findBatchOfConcepts(phrase, Boolean.FALSE, null, null, null, null, null,
		    null);
		Assert.assertNotNull(result);
		Assert.assertTrue(isConceptFound(expected, result));
	}
	
	/**
	 * @see DWRConceptService#findBatchOfConcepts(String, boolean, java.util.List, java.util.List,
	 *      java.util.List, java.util.List, Integer, Integer)
	 * @verifies return concept by given id if classname is included
	 */
	@Test
	public void findBatchOfConcepts_shouldReturnConceptByGivenIdIfClassnameIsIncluded() throws Exception {
		String phrase = "1000";
		Concept expected = Context.getConceptService().getConcept(phrase);
		// prepare include concept classnames list
		List<String> includeClassNames = new ArrayList<String>();
		includeClassNames.add("Question");
		List<Object> result = dwrConceptService.findBatchOfConcepts(phrase, Boolean.FALSE, includeClassNames, null, null,
		    null, null, null);
		Assert.assertNotNull(result);
		Assert.assertTrue(isConceptFound(expected, result));
	}
	
	/**
	 * @see DWRConceptService#findBatchOfConcepts(String, boolean, java.util.List, java.util.List,
	 *      java.util.List, java.util.List, Integer, Integer)
	 * @verifies not return concept by given id if classname is not included
	 */
	@Test
	public void findBatchOfConcepts_shouldNotReturnConceptByGivenIdIfClassnameIsNotIncluded() throws Exception {
		String phrase = "1000";
		Concept expected = Context.getConceptService().getConcept(phrase);
		// prepare include concept classnames list and
		// intentionally do not add expected concept class name
		List<String> includeClassNames = new ArrayList<String>();
		includeClassNames.add("test");
		List<Object> result = dwrConceptService.findBatchOfConcepts(phrase, Boolean.FALSE, includeClassNames, null, null,
		    null, null, null);
		Assert.assertNotNull(result);
		Assert.assertFalse(isConceptFound(expected, result));
	}
	
	/**
	 * @see DWRConceptService#findBatchOfConcepts(String, boolean, java.util.List, java.util.List,
	 *      java.util.List, java.util.List, Integer, Integer)
	 * @verifies not return concept by given id if classname is excluded
	 */
	@Test
	public void findBatchOfConcepts_shouldNotReturnConceptByGivenIdIfClassnameIsExcluded() throws Exception {
		String phrase = "1000";
		Concept expected = Context.getConceptService().getConcept(phrase);
		// prepare exclude concept classnames list
		List<String> excludeClassNames = new ArrayList<String>();
		excludeClassNames.add("Question");
		List<Object> result = dwrConceptService.findBatchOfConcepts(phrase, Boolean.FALSE, null, excludeClassNames, null,
		    null, null, null);
		Assert.assertNotNull(result);
		Assert.assertFalse(isConceptFound(expected, result));
	}
	
	/**
	 * @see DWRConceptService#findBatchOfConcepts(String, boolean, java.util.List, java.util.List,
	 *      java.util.List, java.util.List, Integer, Integer)
	 * @verifies return concept by given id if datatype is included
	 */
	@Test
	public void findBatchOfConcepts_shouldReturnConceptByGivenIdIfDatatypeIsIncluded() throws Exception {
		String phrase = "1000";
		Concept expected = Context.getConceptService().getConcept(phrase);
		// prepare include concept datatypes list
		List<String> includeDatatypes = new ArrayList<String>();
		includeDatatypes.add("Coded");
		List<Object> result = dwrConceptService.findBatchOfConcepts(phrase, Boolean.FALSE, null, null, includeDatatypes,
		    null, null, null);
		Assert.assertNotNull(result);
		Assert.assertTrue(isConceptFound(expected, result));
	}
	
	/**
	 * @see DWRConceptService#findBatchOfConcepts(String, boolean, java.util.List, java.util.List,
	 *      java.util.List, java.util.List, Integer, Integer)
	 * @verifies not return concept by given id if datatype is not included
	 */
	@Test
	public void findBatchOfConcepts_shouldNotReturnConceptByGivenIdIfDatatypeIsNotIncluded() throws Exception {
		String phrase = "1000";
		Concept expected = Context.getConceptService().getConcept(phrase);
		// prepare include concept datatypes list and
		// intentionally do not add expected concept data type
		List<String> includeDatatypes = new ArrayList<String>();
		includeDatatypes.add("test");
		List<Object> result = dwrConceptService.findBatchOfConcepts(phrase, Boolean.FALSE, null, null, includeDatatypes,
		    null, null, null);
		Assert.assertNotNull(result);
		Assert.assertFalse(isConceptFound(expected, result));
	}
	
	/**
	 * @see DWRConceptService#findBatchOfConcepts(String, boolean, java.util.List, java.util.List,
	 *      java.util.List, java.util.List, Integer, Integer)
	 * @verifies not return concept by given id if datatype is excluded
	 */
	@Test
	public void findBatchOfConcepts_shouldNotReturnConceptByGivenIdIfDatatypeIsExcluded() throws Exception {
		String phrase = "1000";
		Concept expected = Context.getConceptService().getConcept(phrase);
		// prepare exclude concept datatypes list
		List<String> excludeDatatypes = new ArrayList<String>();
		excludeDatatypes.add("Coded");
		List<Object> result = dwrConceptService.findBatchOfConcepts(phrase, Boolean.FALSE, null, null, null,
		    excludeDatatypes, null, null);
		Assert.assertNotNull(result);
		Assert.assertFalse(isConceptFound(expected, result));
	}
	
	/**
	 * Convenient method that determines whether given concept is present in result list or not
	 * 
	 * @param expected the concept to be checked
	 * @param result the list of concept lookup result items
	 * @return true if given concept is present among result items
	 */
	private boolean isConceptFound(Concept expected, List<Object> result) {
		boolean found = Boolean.FALSE;
		if (result != null) {
			for (Iterator<?> iterator = result.iterator(); iterator.hasNext();) {
				Object item = iterator.next();
				if (item instanceof ConceptListItem) {
					ConceptListItem resultItem = (ConceptListItem) item;
					if (resultItem != null && OpenmrsUtil.nullSafeEquals(resultItem.getConceptId(), expected.getConceptId())) {
						found = Boolean.TRUE;
						break;
					}
				}
			}
		}
		return found;
	}
	
	/**
	 * @see DWRConceptService#findConceptAnswers(String,Integer,boolean,boolean)
	 * @verifies search for concept answers in all search locales
	 */
	@Test
	public void findConceptAnswers_shouldSearchForConceptAnswersInAllSearchLocales() throws Exception {
		//Given
		Context.getAdministrationService().saveGlobalProperty(
		    new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOCALE_ALLOWED_LIST, "en_GB, en_US, pl"));
		
		User user = Context.getAuthenticatedUser();
		user.setUserProperty(OpenmrsConstants.USER_PROPERTY_PROFICIENT_LOCALES, "en_GB, en_US, pl");
		Context.getUserService().saveUser(user, null);
		
		Concept answer1 = Context.getConceptService().getConcept(7);
		answer1.addName(new ConceptName("TAK", new Locale("pl")));
		Context.getConceptService().saveConcept(answer1);
		
		Concept answer2 = Context.getConceptService().getConcept(8);
		answer2.addName(new ConceptName("T", new Locale("en")));
		Context.getConceptService().saveConcept(answer2);
		
		Concept answer3 = Context.getConceptService().getConcept(22);
		answer3.addName(new ConceptName("T", new Locale("es")));
		Context.getConceptService().saveConcept(answer3);
		
		updateSearchIndex();
		
		//when
		List<Object> findConceptAnswers = dwrConceptService.findConceptAnswers("T", 21, false, true);
		
		//then
		Assert.assertEquals(2, findConceptAnswers.size());
		for (Object findConceptAnswer : findConceptAnswers) {
			ConceptListItem answer = (ConceptListItem) findConceptAnswer;
			if (!answer.getConceptId().equals(7) && !answer.getConceptId().equals(8)) {
				Assert.fail("Should have found an answer with id 7 or 8");
			}
		}
	}
	
	/**
	 * @see DWRConceptService#findConceptAnswers(String,Integer,boolean,boolean)
	 * @verifies not return duplicates
	 */
	@Test
	public void findConceptAnswers_shouldNotReturnDuplicates() throws Exception {
		//Given
		Context.getAdministrationService().saveGlobalProperty(
		    new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOCALE_ALLOWED_LIST, "en_GB, en_US, pl"));
		
		User user = Context.getAuthenticatedUser();
		user.setUserProperty(OpenmrsConstants.USER_PROPERTY_PROFICIENT_LOCALES, "en_GB, en_US, pl");
		Context.getUserService().saveUser(user, null);
		
		Concept answer1 = Context.getConceptService().getConcept(7);
		answer1.addName(new ConceptName("TAK", new Locale("pl")));
		Context.getConceptService().saveConcept(answer1);
		
		Concept answer2 = Context.getConceptService().getConcept(7);
		answer2.addName(new ConceptName("True", new Locale("en")));
		Context.getConceptService().saveConcept(answer2);
		
		updateSearchIndex();
		
		//when
		List<Object> findConceptAnswers = dwrConceptService.findConceptAnswers("T", 21, false, true);
		
		//then
		Assert.assertEquals(1, findConceptAnswers.size());
		for (Object findConceptAnswer : findConceptAnswers) {
			ConceptListItem answer = (ConceptListItem) findConceptAnswer;
			if (!answer.getConceptId().equals(7)) {
				Assert.fail("Should have found an answer with id 7");
			}
		}
		
	}
}

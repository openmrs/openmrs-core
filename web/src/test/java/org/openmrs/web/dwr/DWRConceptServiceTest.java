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
package org.openmrs.web.dwr;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsUtil;
import org.openmrs.web.test.BaseWebContextSensitiveTest;

public class DWRConceptServiceTest extends BaseWebContextSensitiveTest {
	
	private DWRConceptService dwrConceptService = new DWRConceptService();
	
	@Before
	public void before() throws Exception {
		executeDataSet("org/openmrs/web/dwr/include/DWRConceptServiceTest-coded-concept-with-no-answers.xml");
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
}

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

import java.util.List;
import java.util.Locale;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.GlobalProperty;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.web.test.BaseWebContextSensitiveTest;

public class DWRConceptServiceTest extends BaseWebContextSensitiveTest {
	
	private DWRConceptService dwrConceptService = new DWRConceptService();
	
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

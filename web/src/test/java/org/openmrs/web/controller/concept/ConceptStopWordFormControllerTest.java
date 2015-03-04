/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.controller.concept;

import java.util.Locale;

import javax.servlet.http.HttpSession;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.ConceptStopWord;
import org.openmrs.test.Verifies;
import org.openmrs.web.WebConstants;
import org.openmrs.web.test.BaseWebContextSensitiveTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;

/**
 * Tests the {@link ConceptStopWordFormController}
 */
public class ConceptStopWordFormControllerTest extends BaseWebContextSensitiveTest {
	
	/**
	 * @see {@link ConceptStopWordFormController#handleSubmission(HttpSession, ConceptStopWordFormBackingObject, org.springframework.validation.BindingResult)
	 */
	@Test
	@Verifies(value = "should add new ConceptStopWord", method = "handleSubmission(HttpSession, ConceptStopWordFormBackingObject, BindingResult)")
	public void handleSubmission_shouldAddNewConceptStopWord() throws Exception {
		ConceptStopWordFormController controller = (ConceptStopWordFormController) applicationContext
		        .getBean("conceptStopWordFormController");
		
		HttpSession mockSession = new MockHttpSession();
		
		mockSession.setAttribute("value", "As");
		BindException errors = new BindException(new ConceptStopWord("As", Locale.ENGLISH), "value");
		
		controller.handleSubmission(mockSession, new ConceptStopWord("As", Locale.ENGLISH), errors);
		
		Assert.assertEquals("ConceptStopWord.saved", mockSession.getAttribute(WebConstants.OPENMRS_MSG_ATTR));
		Assert.assertNull(mockSession.getAttribute(WebConstants.OPENMRS_ERROR_ATTR));
	}
	
	/**
	 * @see {@link ConceptStopWordFormController#handleSubmission(HttpSession, ConceptStopWordFormBackingObject, org.springframework.validation.BindingResult)
	 */
	@Test
	@Verifies(value = "should return error message for an empty ConceptStopWord", method = "handleSubmission(HttpSession, ConceptStopWordFormBackingObject, BindingResult)")
	public void handleSubmission_shouldReturnErrorMessageForAnEmptyConceptStopWord() throws Exception {
		ConceptStopWordFormController controller = (ConceptStopWordFormController) applicationContext
		        .getBean("conceptStopWordFormController");
		
		HttpSession mockSession = new MockHttpSession();
		
		ConceptStopWord conceptStopWord = new ConceptStopWord("", Locale.CANADA);
		
		mockSession.setAttribute("value", conceptStopWord.getValue());
		BindException errors = new BindException(conceptStopWord, "value");
		
		controller.handleSubmission(mockSession, conceptStopWord, errors);
		ObjectError objectError = (ObjectError) errors.getAllErrors().get(0);
		
		Assert.assertTrue(errors.hasErrors());
		Assert.assertEquals(1, errors.getErrorCount());
		Assert.assertEquals("ConceptStopWord.error.value.empty", objectError.getCode());
	}
}

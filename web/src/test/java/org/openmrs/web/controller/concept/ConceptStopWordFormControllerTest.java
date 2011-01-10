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

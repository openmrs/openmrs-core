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

import java.util.List;

import javax.servlet.http.HttpSession;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.ConceptStopWord;
import org.openmrs.test.Verifies;
import org.openmrs.web.WebConstants;
import org.openmrs.web.test.BaseWebContextSensitiveTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;

/**
 * Tests the {@link ConceptStopWordListController}
 */
public class ConceptStopWordListControllerTest extends BaseWebContextSensitiveTest {
	
	/**
	 * @see {@link ConceptStopWordListController#showForm(javax.servlet.http.HttpSession)}
	 */
	@Test
	@Verifies(value = "should return Concept Stop Word List View", method = "showForm(HttpSession)")
	public void showForm_shouldReturnConceptStopWordListView() throws Exception {
		ConceptStopWordListController controller = (ConceptStopWordListController) applicationContext
		        .getBean("conceptStopWordListController");
		
		MockHttpServletRequest mockRequest = new MockHttpServletRequest();
		mockRequest.setMethod("POST");
		
		String showFormResult = controller.showForm(mockRequest.getSession());
		
		Assert.assertNotNull(showFormResult);
		Assert.assertEquals("admin/concepts/conceptStopWordList", showFormResult);
	}
	
	/**
	 * @see {@link ConceptStopWordListController#showForm(javax.servlet.http.HttpSession)}
	 */
	@SuppressWarnings("unchecked")
	@Test
	@Verifies(value = "should add all ConceptStopWords in session attribute", method = "showForm(HttpSession)")
	public void showForm_shouldAddAllConceptStopWordsInSessionAttribute() throws Exception {
		ConceptStopWordListController controller = (ConceptStopWordListController) applicationContext
		        .getBean("conceptStopWordListController");
		
		MockHttpServletRequest mockRequest = new MockHttpServletRequest();
		mockRequest.setMethod("POST");
		
		controller.showForm(mockRequest.getSession());
		
		List<ConceptStopWord> conceptStopWordList = (List<ConceptStopWord>) mockRequest.getSession().getAttribute(
		    "conceptStopWordList");
		Assert.assertNotNull(conceptStopWordList);
		Assert.assertEquals(4, conceptStopWordList.size());
	}
	
	/**
	 * @see {@link ConceptStopWordListController#handleSubmission(javax.servlet.http.HttpSession, String[])
	 */
	@SuppressWarnings("unchecked")
	@Test
	@Verifies(value = "should delete the given ConceptStopWord in the request parameter", method = "handleSubmission(HttpSession, String[])")
	public void handleSubmission_shouldDeleteGivenConceptStopWordFromDB() throws Exception {
		ConceptStopWordListController controller = (ConceptStopWordListController) applicationContext
		        .getBean("conceptStopWordListController");
		
		HttpSession mockSession = new MockHttpSession();
		
		controller.handleSubmission(mockSession, new String[] { "1" });
		
		List<ConceptStopWord> conceptStopWordList = (List<ConceptStopWord>) mockSession.getAttribute("conceptStopWordList");
		Assert.assertNotNull(conceptStopWordList);
		Assert.assertEquals(3, conceptStopWordList.size());
	}
	
	/**
	 * @see {@link ConceptStopWordListController#handleSubmission(javax.servlet.http.HttpSession, String[])
	 */
	@Test
	@Verifies(value = "should add the success delete message in session attribute", method = "handleSubmission(HttpSession, String[])")
	public void handleSubmission_shouldAddTheDeleteSuccessMessageInSession() throws Exception {
		ConceptStopWordListController controller = (ConceptStopWordListController) applicationContext
		        .getBean("conceptStopWordListController");
		
		HttpSession mockSession = new MockHttpSession();
		
		controller.handleSubmission(mockSession, new String[] { "2" });
		
		String successMessage = (String) mockSession.getAttribute(WebConstants.OPENMRS_MSG_ATTR);
		String errorMessage = (String) mockSession.getAttribute(WebConstants.OPENMRS_ERROR_ATTR);
		
		Assert.assertNotNull(successMessage);
		Assert.assertNull(errorMessage);
		Assert.assertEquals("general.deleted", successMessage);
	}
	
	/**
	 * @see {@link ConceptStopWordListController#handleSubmission(javax.servlet.http.HttpSession, String[])
	 */
	@Test
	@Verifies(value = "should add the already deleted error message in session attribute if delete the same word twice", method = "handleSubmission(HttpSession, String[])")
	public void handleSubmission_shouldAddTheDeleteErrorMessageInSession() throws Exception {
		ConceptStopWordListController controller = (ConceptStopWordListController) applicationContext
		        .getBean("conceptStopWordListController");
		
		HttpSession mockSession = new MockHttpSession();
		
		controller.handleSubmission(mockSession, new String[] { "1", "1" });
		
		String successMessage = (String) mockSession.getAttribute(WebConstants.OPENMRS_MSG_ATTR);
		String errorMessage = (String) mockSession.getAttribute(WebConstants.OPENMRS_ERROR_ATTR);
		
		Assert.assertNotNull(successMessage);
		Assert.assertNotNull(errorMessage);
		Assert.assertEquals("ConceptStopWord.error.notfound", errorMessage);
	}
}

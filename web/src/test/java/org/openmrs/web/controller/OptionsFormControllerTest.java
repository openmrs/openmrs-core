/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.controller;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.net.BindException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.GlobalProperty;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.LoginCredential;
import org.openmrs.api.db.UserDAO;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.Security;
import org.openmrs.web.OptionsForm;
import org.openmrs.web.test.BaseWebContextSensitiveTest;
import org.openmrs.web.test.WebTestHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.servlet.ModelAndView;

public class OptionsFormControllerTest extends BaseWebContextSensitiveTest {
	
	private User user;
	
	private UserDAO userDao;
	
	private OptionsFormController controller;
	
	@Autowired
	private WebTestHelper testHelper;
	
	@Before
	public void setUp() {
		Context.authenticate("admin", "test");
		user = Context.getAuthenticatedUser();
		controller = (OptionsFormController) applicationContext.getBean("optionsForm");
		userDao = (UserDAO) applicationContext.getBean("userDAO");
	}
	
	@Test
	public void shouldChangeSecretQuestionAndAnswer() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "");
		request.setParameter("secretQuestionPassword", "test");
		request.setParameter("secretQuestionNew", "test_question");
		
		String answer = "test_answer";
		String hashedAnswer = Security.encodeString(answer);
		request.setParameter("secretAnswerNew", answer);
		request.setParameter("secretAnswerConfirm", answer);
		
		HttpServletResponse response = new MockHttpServletResponse();
		controller.handleRequest(request, response);
		
		LoginCredential loginCredential = userDao.getLoginCredential(user);
		assertEquals(Security.encodeString(answer + loginCredential.getSalt()), loginCredential.getSecretAnswer());
	}
	
	@Test
	public void shouldRejectEmptySecretAnswer() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "");
		request.setParameter("secretQuestionPassword", "test");
		request.setParameter("secretQuestionNew", "test_question");
		
		String emptyAnswer = "";
		request.setParameter("secretAnswerNew", emptyAnswer);
		request.setParameter("secretAnswerConfirm", emptyAnswer);
		
		HttpServletResponse response = new MockHttpServletResponse();
		controller.handleRequest(request, response);
		
		LoginCredential loginCredential = userDao.getLoginCredential(user);
		assertNull(loginCredential.getSecretAnswer());
	}
	
	@Test
	public void shouldRejectEmptySecretAnswerWhenSecretQuestionPasswordIsNotSet() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "");
		request.setParameter("secretQuestionPassword", "");
		request.setParameter("secretQuestionNew", "test_question");
		
		String emptyAnswer = "";
		request.setParameter("secretAnswerNew", emptyAnswer);
		request.setParameter("secretAnswerConfirm", emptyAnswer);
		
		HttpServletResponse response = new MockHttpServletResponse();
		controller.handleRequest(request, response);
		
		LoginCredential loginCredential = userDao.getLoginCredential(user);
		assertNull(loginCredential.getSecretAnswer());
	}
	
	@Test
	public void shouldRejectEmptySecretQuestion() throws Exception {
		LoginCredential loginCredential = userDao.getLoginCredential(user);
		String originalQuestion = loginCredential.getSecretQuestion();
		
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "");
		request.setParameter("secretQuestionPassword", "test");
		request.setParameter("secretQuestionNew", "");
		
		String emptyAnswer = "test_answer";
		request.setParameter("secretAnswerNew", emptyAnswer);
		request.setParameter("secretAnswerConfirm", emptyAnswer);
		
		HttpServletResponse response = new MockHttpServletResponse();
		controller.handleRequest(request, response);
		
		loginCredential = userDao.getLoginCredential(user);
		assertEquals(originalQuestion, loginCredential.getSecretQuestion());
	}
	
	/**
	 * @see OptionsFormController#onSubmit(HttpServletRequest,HttpServletResponse,Object,BindException)
	 * @verifies accept email address as username if enabled
	 */
	@Test
	public void onSubmit_shouldAcceptEmailAddressAsUsernameIfEnabled() throws Exception {
		//given
		Context.getAdministrationService().saveGlobalProperty(
		    new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_USER_REQUIRE_EMAIL_AS_USERNAME, "true"));
		MockHttpServletRequest post = testHelper.newPOST("/options.form");
		post.addParameter("username", "ab@gmail.com");
		
		//when
		testHelper.handle(post);
		
		//then
		Assert.assertThat("ab@gmail.com", is(Context.getAuthenticatedUser().getUsername()));
	}
	
	/**
	 * @see OptionsFormController#onSubmit(HttpServletRequest,HttpServletResponse,Object,BindException)
	 * @verifies reject invalid email address as username if enabled
	 */
	@Test
	public void onSubmit_shouldRejectInvalidEmailAddressAsUsernameIfEnabled() throws Exception {
		//given
		Context.getAdministrationService().saveGlobalProperty(
		    new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_USER_REQUIRE_EMAIL_AS_USERNAME, "true"));
		MockHttpServletRequest post = testHelper.newPOST("/options.form");
		post.addParameter("username", "ab@");
		
		//when
		testHelper.handle(post);
		
		//then
		Assert.assertThat("ab@", is(not(Context.getAuthenticatedUser().getUsername())));
	}
	
	/**
	 * @see OptionsFormController#onSubmit(HttpServletRequest,HttpServletResponse,Object,BindException)
	 * @verifies accept 2 characters as username
	 */
	@Test
	public void onSubmit_shouldAccept2CharactersAsUsername() throws Exception {
		//given
		MockHttpServletRequest post = testHelper.newPOST("/options.form");
		post.addParameter("username", "ab");
		
		//when
		testHelper.handle(post);
		
		//then
		Assert.assertThat("ab", is(Context.getAuthenticatedUser().getUsername()));
	}
	
	/**
	 * @see OptionsFormController#onSubmit(HttpServletRequest,HttpServletResponse,Object,BindException)
	 * @verifies reject 1 character as username
	 */
	@Test
	public void onSubmit_shouldReject1CharacterAsUsername() throws Exception {
		//given
		MockHttpServletRequest post = testHelper.newPOST("/options.form");
		post.addParameter("username", "a");
		
		//when
		testHelper.handle(post);
		
		//then
		Assert.assertThat("a", is(not(Context.getAuthenticatedUser().getUsername())));
	}
	
	@Test
	public void shouldRejectInvalidNotificationAddress() throws Exception {
		final String incorrectAddress = "gayan@gmail";
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "");
		request.setParameter("notification", "email");
		request.setParameter("notificationAddress", incorrectAddress);
		
		HttpServletResponse response = new MockHttpServletResponse();
		ModelAndView modelAndView = controller.handleRequest(request, response);
		
		OptionsForm optionsForm = (OptionsForm) controller.formBackingObject(request);
		assertEquals(incorrectAddress, optionsForm.getNotificationAddress());
		
		BeanPropertyBindingResult bindingResult = (BeanPropertyBindingResult) modelAndView.getModel().get(
		    "org.springframework.validation.BindingResult.opts");
		Assert.assertTrue(bindingResult.hasErrors());
	}
	
	@Test
	public void shouldAcceptValidNotificationAddress() throws Exception {
		String notificationTypes[] = { "internal", "internalProtected", "email" };
		String correctAddress = "gayan@gmail.com";
		
		for (String notifyType : notificationTypes) {
			MockHttpServletRequest request = new MockHttpServletRequest("POST", "");
			request.setParameter("notification", notifyType);
			request.setParameter("notificationAddress", correctAddress);
			
			HttpServletResponse response = new MockHttpServletResponse();
			controller.handleRequest(request, response);
			
			OptionsForm optionsForm = (OptionsForm) controller.formBackingObject(request);
			assertEquals(correctAddress, optionsForm.getNotificationAddress());
		}
	}
	
	@Test
	public void shouldRejectEmptyNotificationAddress() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "");
		request.setParameter("notification", "email");
		request.setParameter("notificationAddress", "");
		
		HttpServletResponse response = new MockHttpServletResponse();
		ModelAndView modelAndView = controller.handleRequest(request, response);
		
		BeanPropertyBindingResult bindingResult = (BeanPropertyBindingResult) modelAndView.getModel().get(
		    "org.springframework.validation.BindingResult.opts");
		Assert.assertTrue(bindingResult.hasErrors());
	}
	
	@Test
	public void shouldNotOverwriteUserSecretQuestionOrAnswerWhenChangingPassword() throws Exception {
		LoginCredential loginCredential = userDao.getLoginCredential(user);
		HttpServletResponse response = new MockHttpServletResponse();
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "");
		
		request.setParameter("secretQuestionPassword", "test");
		request.setParameter("secretQuestionNew", "easy question");
		request.setParameter("secretAnswerNew", "easy answer");
		request.setParameter("secretAnswerConfirm", "easy answer");
		
		controller.handleRequest(request, response);
		Assert.assertEquals("easy question", loginCredential.getSecretQuestion());
		String hashedAnswer = Security.encodeString("easy answer" + loginCredential.getSalt());
		Assert.assertEquals(hashedAnswer, loginCredential.getSecretAnswer());
		String oldPassword = loginCredential.getHashedPassword();
		
		request.removeAllParameters();
		request.addParameter("secretQuestionNew", "easy question");
		request.setParameter("oldPassword", "test");
		request.setParameter("newPassword", "OpenMRS1");
		request.setParameter("confirmPassword", "OpenMRS1");
		ModelAndView mav = controller.handleRequest(request, response);
		
		if (oldPassword == loginCredential.getHashedPassword()) {
			request.setParameter("secretQuestionNew", "");
			mav = controller.handleRequest(request, response);
		}
		Assert.assertEquals(hashedAnswer, loginCredential.getSecretAnswer());
		Assert.assertEquals("easy question", loginCredential.getSecretQuestion());
	}
}

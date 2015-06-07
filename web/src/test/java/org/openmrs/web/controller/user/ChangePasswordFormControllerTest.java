/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.controller.user;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import javax.servlet.http.HttpSession;

import org.junit.Test;
import org.openmrs.User;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.openmrs.test.Verifies;
import org.openmrs.web.test.BaseWebContextSensitiveTest;
import org.openmrs.web.user.UserProperties;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;

/**
 * Test the different aspects of
 * {@link org.openmrs.web.controller.user.ChangePasswordFormController}
 */
public class ChangePasswordFormControllerTest extends BaseWebContextSensitiveTest {
	
	/**
	 * @see {@link ChangePasswordFormController#formBackingObject()} 
	 */
	@Test
	@Verifies(value = "return an authenticated User", method = "formBackingObject()")
	public void formBackingObject_shouldReturnAuthenticatedUser() throws Exception {
		ChangePasswordFormController controller = new ChangePasswordFormController();
		User user = controller.formBackingObject();
		assertNotNull(user);
		assertEquals(Context.getAuthenticatedUser(), user);
	}
	
	/**
	 * @see {@link ChangePasswordFormController#handleSubmission(HttpSession, String, String, String, String, String, User, BindingResult)}
	 */
	@Test
	@Verifies(value = "display an error message when the password and confirm password entries are different", method = "handleSubmission()")
	public void handleSubmission_shouldDisplayErrorMessageWhenPasswordAndConfirmPasswordAreNotSame() throws Exception {
		ChangePasswordFormController controller = new ChangePasswordFormController();
		BindException errors = new BindException(controller.formBackingObject(), "user");
		
		String result = controller.handleSubmission(new MockHttpSession(), "password", "differentPassword", "", "", "",
		    Context.getAuthenticatedUser(), errors);
		
		assertTrue(errors.hasErrors());
		assertEquals("error.password.match", errors.getGlobalError().getCode());
	}
	
	/**
	 * @see {@link ChangePasswordFormController#handleSubmission(HttpSession, String, String, String, String, String, User, BindingResult)}
	 */
	@Test
	@Verifies(value = "not display error message if password and confirm password are the same", method = "handleSubmission()")
	public void handleSubmission_shouldRedirectToIndexPageWhenPasswordAndConfirmPasswordAreTheSame() throws Exception {
		ChangePasswordFormController controller = new ChangePasswordFormController();
		BindException errors = new BindException(controller.formBackingObject(), "user");
		
		String result = controller.handleSubmission(new MockHttpSession(), "Passw0rd", "Passw0rd", "", "", "", Context
		        .getAuthenticatedUser(), errors);
		
		assertTrue(!errors.hasErrors());
		assertEquals("redirect:/index.htm", result);
	}
	
	/**
	 * @see {@link ChangePasswordFormController#handleSubmission(HttpSession, String, String, String, String, String, User, BindingResult)}
	 *           test =
	 */
	@Test
	@Verifies(value = "display error message when the password is empty", method = "handleSubmission()")
	public void handleSubmission_shouldDisplayErrorMessageWhenPasswordIsEmpty() throws Exception {
		ChangePasswordFormController controller = new ChangePasswordFormController();
		BindException errors = new BindException(controller.formBackingObject(), "user");
		
		String result = controller.handleSubmission(new MockHttpSession(), "", "", "", "", "", Context
		        .getAuthenticatedUser(), errors);
		
		assertTrue(errors.hasErrors());
		assertEquals("error.password.weak", errors.getGlobalError().getCode());
	}
	
	/**
	 * @see {@link ChangePasswordFormController#handleSubmission(HttpSession, String, String, String, String, String, User, BindingResult)}
	 */
	@Test
	@Verifies(value = "display error message if password is weak", method = "handleSubmission()")
	public void handleSubmission_shouldDiplayErrorMessageOnWeakPasswords() throws Exception {
		ChangePasswordFormController controller = new ChangePasswordFormController();
		BindException errors = new BindException(controller.formBackingObject(), "user");
		
		String result = controller.handleSubmission(new MockHttpSession(), "password", "password", "", "", "", Context
		        .getAuthenticatedUser(), errors);
		
		assertTrue(errors.hasErrors());
		assertEquals("error.password.requireMixedCase", errors.getGlobalError().getCode());
	}
	
	/**
	 * @see {@link ChangePasswordFormController#handleSubmission(HttpSession, String, String, String, String, String, User, BindingResult)}
	 */
	@Test
	@Verifies(value = "display error message when question is empty and answer is not empty", method = "handleSubmission()")
	public void handleSubmission_shouldDiplayErrorMessageIfQuestionIsEmptyAndAnswerIsNotEmpty() throws Exception {
		ChangePasswordFormController controller = new ChangePasswordFormController();
		BindException errors = new BindException(controller.formBackingObject(), "user");
		
		String result = controller.handleSubmission(new MockHttpSession(), "Passw0rd", "Passw0rd", "", "answer", "answer",
		    Context.getAuthenticatedUser(), errors);
		
		assertTrue(errors.hasErrors());
		assertEquals("auth.question.empty", errors.getGlobalError().getCode());
	}
	
	/**
	 * @see {@link ChangePasswordFormController#handleSubmission(HttpSession, String, String, String, String, String, User, BindingResult)}
	 */
	@Test
	@Verifies(value = "display error message when the answer and the confirm answer entered are not the same", method = "handleSubmission()")
	public void handleSubmission_shouldDiplayErrorMessageIfAnswerAndConfirmAnswerAreNotTheSame() throws Exception {
		ChangePasswordFormController controller = new ChangePasswordFormController();
		BindException errors = new BindException(controller.formBackingObject(), "user");
		
		String result = controller.handleSubmission(new MockHttpSession(), "Passw0rd", "Passw0rd", "question", "answer",
		    "confirmanswer", Context.getAuthenticatedUser(), errors);
		
		assertTrue(errors.hasErrors());
		assertEquals("error.options.secretAnswer.match", errors.getGlobalError().getCode());
	}
	
	/**
	 * @see {@link ChangePasswordFormController#handleSubmission(HttpSession, String, String, String, String, String, User, BindingResult)}
	 */
	@Test
	@Verifies(value = "display error message when the answer is empty and question is not empty", method = "handleSubmission()")
	public void handleSubmission_shouldDisplayErrorMessageIfQuestionIsNotEmptyAndAnswerIsEmpty() throws Exception {
		ChangePasswordFormController controller = new ChangePasswordFormController();
		BindException errors = new BindException(controller.formBackingObject(), "user");
		
		String result = controller.handleSubmission(new MockHttpSession(), "Passw0rd", "Passw0rd", "question", "", "",
		    Context.getAuthenticatedUser(), errors);
		
		assertTrue(errors.hasErrors());
		assertEquals("auth.question.fill", errors.getGlobalError().getCode());
	}
	
	/**
	 * @see {@link ChangePasswordFormController#handleSubmission(HttpSession, String, String, String, String, String, User, BindingResult)}
	 */
	@Test
	@Verifies(value = "navigate to the home page if the authentication is successful", method = "handleSubmission()")
	public void handleSubmission_shouldProceedToHomePageIfOperationIsSuccesful() throws Exception {
		ChangePasswordFormController controller = new ChangePasswordFormController();
		BindException errors = new BindException(controller.formBackingObject(), "user");
		
		String result = controller.handleSubmission(new MockHttpSession(), "Passw0rd", "Passw0rd", "question", "answer",
		    "answer", Context.getAuthenticatedUser(), errors);
		
		assertTrue(!errors.hasErrors());
		assertEquals("redirect:/index.htm", result);
	}
	
	/**
	 * @see {@link ChangePasswordFormController#handleSubmission(HttpSession, String, String, String, String, String, User, BindingResult)}
	 */
	@Test
	@Verifies(value = "set the user property forcePassword to false after successful password change", method = "handleSubmission()")
	public void handleSubmission_shouldChangeTheUserPropertyForcePasswordChangeToFalse() throws Exception {
		User user = Context.getAuthenticatedUser();
		new UserProperties(user.getUserProperties()).setSupposedToChangePassword(true);
		
		UserService us = Context.getUserService();
		us.saveUser(user, "Openmr5xy");
		
		ChangePasswordFormController controller = new ChangePasswordFormController();
		BindException errors = new BindException(controller.formBackingObject(), "user");
		
		controller.handleSubmission(new MockHttpSession(), "Passw0rd", "Passw0rd", "", "", "", Context
		        .getAuthenticatedUser(), errors);
		
		User modifiedUser = us.getUser(user.getId());
		assertTrue(!new UserProperties(modifiedUser.getUserProperties()).isSupposedToChangePassword());
	}
	
	/**
	 * @see {@link ChangePasswordFormController#handleSubmission(HttpSession, String, String, String, String, String, User, BindingResult)}
	 */
	@Test
	@Verifies(value = "do not set the user property forcePassword to false after unsuccessful password change", method = "handleSubmission()")
	public void handleSubmission_shouldNotChangeTheUserPropertyForcePasswordChangeToFalse() throws Exception {
		User user = Context.getAuthenticatedUser();
		new UserProperties(user.getUserProperties()).setSupposedToChangePassword(true);
		
		UserService us = Context.getUserService();
		us.saveUser(user, "Openmr5xy");
		
		ChangePasswordFormController controller = new ChangePasswordFormController();
		BindException errors = new BindException(controller.formBackingObject(), "user");
		
		controller.handleSubmission(new MockHttpSession(), "Passw0rd", "Pasw0rd", "", "", "",
		    Context.getAuthenticatedUser(), errors);
		
		User modifiedUser = us.getUser(user.getId());
		assertTrue(new UserProperties(modifiedUser.getUserProperties()).isSupposedToChangePassword());
	}
	
	/**
	 * @see {@link ChangePasswordFormController#formBackingObject()} 
	 */
	@Test
	@Verifies(value = "remain on the changePasswordForm page if there are errors", method = "formBackingObject()")
	public void formBackingObject_shouldRemainOnChangePasswordFormPageIfThereAreErrors() throws Exception {
		ChangePasswordFormController controller = new ChangePasswordFormController();
		BindException errors = new BindException(controller.formBackingObject(), "user");
		errors.addError(new ObjectError("Test", "Test Error"));
		String result = controller.handleSubmission(new MockHttpSession(), "password", "differentPassword", "", "", "",
		    Context.getAuthenticatedUser(), errors);
		assertEquals("/admin/users/changePasswordForm", result);
	}
	
	/**
	 * @see {@link ChangePasswordFormController#handleSubmission(HttpSession, String, String, String, String, String, User, BindingResult)}
	 */
	@Test
	@Verifies(value = "set the secret question and answer of the user", method = "handleSubmission()")
	public void handleSubmission_shouldSetTheUserSecretQuestionAndAnswer() throws Exception {
		User user = Context.getAuthenticatedUser();
		new UserProperties(user.getUserProperties()).setSupposedToChangePassword(true);
		
		UserService us = Context.getUserService();
		us.saveUser(user, "Openmr5xy");
		
		ChangePasswordFormController controller = new ChangePasswordFormController();
		BindException errors = new BindException(controller.formBackingObject(), "user");
		
		controller.handleSubmission(new MockHttpSession(), "Passw0rd", "Passw0rd", "test_question", "test_answer",
		    "test_answer", Context.getAuthenticatedUser(), errors);
		
		User modifiedUser = us.getUser(user.getId());
		
		assertTrue(us.isSecretAnswer(modifiedUser, "test_answer"));
	}
	
}

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

import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.openmrs.User;
import org.openmrs.api.PasswordException;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsUtil;
import org.openmrs.util.PrivilegeConstants;
import org.openmrs.web.WebConstants;
import org.openmrs.web.user.UserProperties;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Used for changing the password when the force password change option is set during a new user
 * creation.
 */
@Controller
@RequestMapping(value = "/admin/users/changePassword.form")
public class ChangePasswordFormController {
	
	/**
	 * The model from which the data binding happens on the view
	 * 
	 * @should return an authenticated User
	 * @return authenticated user
	 */
	@ModelAttribute("user")
	public User formBackingObject() {
		return Context.getAuthenticatedUser();
	}
	
	/**
	 * This method will display the change password form
	 * 
	 * @param httpSession current browser session
	 * @return the view to be rendered
	 */
	@RequestMapping(method = RequestMethod.GET)
	public String showForm(HttpSession httpSession) {
		httpSession.setAttribute(WebConstants.OPENMRS_HEADER_USE_MINIMAL, "false");
		httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "User.password.change");
		return "/admin/users/changePasswordForm";
	}
	
	/**
	 * Method to save changes of the new password for a user. The password will be validated against
	 * the current rules and will display error messages in case the password is not strong enough.
	 * 
	 * @should display an error message when the password and confirm password entries are different
	 * @should not display error message if password and confirm password are the same
	 * @should display error message when the password is empty
	 * @should display error message if password is weak
	 * @should display error message when question is empty and answer is not empty
	 * @should display error message when the answer and the confirm answer entered are not the same
	 * @should display error message when the answer is empty and question is not empty
	 * @should navigate to the home page if the authentication is successful
	 * @should set the user property forcePassword to false after successful password change
	 * @should not set the user property forcePassword to false after unsuccessful password change
	 * @should remain on the changePasswordForm page if there are errors
	 * @should set the secret question and answer of the user
	 * @param password to be applied
	 * @param confirmPassword confirmation for the password to be applied
	 * @param question in case of a forgotten password
	 * @param answer answer for the question
	 * @param confirmAnswer confirmation of the answer for the question
	 * @param errors while processing the form
	 */
	@RequestMapping(method = RequestMethod.POST)
	public String handleSubmission(HttpSession httpSession,
	        @RequestParam(required = true, value = "password") String password,
	        @RequestParam(required = true, value = "confirmPassword") String confirmPassword,
	        @RequestParam(required = false, value = "question") String question,
	        @RequestParam(required = false, value = "answer") String answer,
	        @RequestParam(required = false, value = "confirmAnswer") String confirmAnswer,
	        @ModelAttribute("user") User user, BindingResult errors) {
		
		NewPassword newPassword = new NewPassword(password, confirmPassword);
		NewQuestionAnswer newQuestionAnswer = new NewQuestionAnswer(question, answer, confirmAnswer);
		new NewPasswordValidator(user).validate(newPassword, errors);
		new NewQuestionAnswerValidator().validate(newQuestionAnswer, errors);
		
		if (errors.hasErrors()) {
			return showForm(httpSession);
		}
		
		changeUserPasswordAndQuestion(user, newPassword, newQuestionAnswer);
		httpSession.removeAttribute(WebConstants.OPENMRS_MSG_ATTR);
		return "redirect:/index.htm";
		
	}
	
	/**
	 * Utility method to change the password and question/answer of the currently logged in user.
	 * 
	 * @param user for whom the password has to to be changed
	 * @param password new password
	 * @param questionAnswer (optional) security question and answer
	 */
	private void changeUserPasswordAndQuestion(User user, NewPassword password, NewQuestionAnswer questionAnswer) {
		try {
			Context.addProxyPrivilege(PrivilegeConstants.EDIT_USERS);
			Context.addProxyPrivilege(PrivilegeConstants.VIEW_USERS);
			Context.addProxyPrivilege(PrivilegeConstants.EDIT_USER_PASSWORDS);
			
			UserService userService = Context.getUserService();
			User currentUser = userService.getUser(user.getId());
			
			userService.changePassword(currentUser, password.getPassword());
			
			new UserProperties(currentUser.getUserProperties()).setSupposedToChangePassword(false);
			userService.saveUser(currentUser, password.getPassword());
			if (StringUtils.isNotBlank(questionAnswer.getQuestion()) || StringUtils.isNotBlank(questionAnswer.getAnswer())) {
				userService.changeQuestionAnswer(currentUser, questionAnswer.getQuestion(), questionAnswer.getAnswer());
			}
			
			Context.refreshAuthenticatedUser();
		}
		finally {
			Context.removeProxyPrivilege(PrivilegeConstants.EDIT_USERS);
			Context.removeProxyPrivilege(PrivilegeConstants.VIEW_USERS);
			Context.removeProxyPrivilege(PrivilegeConstants.EDIT_USER_PASSWORDS);
		}
	}
	
	private class NewQuestionAnswerValidator implements Validator {
		
		/**
		 * @see org.springframework.validation.Validator#supports(java.lang.Class)
		 */
		public boolean supports(Class c) {
			return c.equals(NewQuestionAnswer.class);
		}
		
		/**
		 * @see org.springframework.validation.Validator#validate(java.lang.Object,
		 *      org.springframework.validation.Errors)
		 */
		@Override
		public void validate(Object object, Errors errors) {
			NewQuestionAnswer questionAnswer = (NewQuestionAnswer) object;
			if (questionAnswer.isQuestionNotEmpty()) {
				if (questionAnswer.isAnswerAndConfirmAnswerNotTheSame()) {
					errors.reject("error.options.secretAnswer.match");
				} else if (questionAnswer.isAnswerEmpty()) {
					errors.reject("auth.question.fill");
				}
			} else if (questionAnswer.isAnswerNotEmpty()) {
				errors.reject("auth.question.empty");
			}
		}
	}
	
	private class NewQuestionAnswer {
		
		private final String question;
		
		private final String answer;
		
		private final String confirmAnswer;
		
		/**
		 * @param question to be used as the security question in case of forgotten password
		 * @param answer answer to the security question
		 * @param confirmAnswer confirmation of answer
		 */
		public NewQuestionAnswer(String question, String answer, String confirmAnswer) {
			this.question = question == null ? "" : question;
			this.answer = answer == null ? "" : answer;
			this.confirmAnswer = confirmAnswer == null ? "" : confirmAnswer;
		}
		
		/**
		 * @return Answer
		 */
		public String getAnswer() {
			return answer;
		}
		
		/**
		 * @return Question
		 */
		public String getQuestion() {
			return question;
		}
		
		/**
		 * @return true if the answer is not empty
		 */
		public boolean isAnswerNotEmpty() {
			return !isAnswerEmpty();
		}
		
		/**
		 * @return true only if the answer is empty
		 */
		public boolean isAnswerEmpty() {
			return answer.isEmpty();
		}
		
		/**
		 * @return true only if answer and confirmAnswer are not the same
		 */
		public boolean isAnswerAndConfirmAnswerNotTheSame() {
			return !answer.equals(confirmAnswer);
		}
		
		/**
		 * @return true only if question is not an empty string
		 */
		public boolean isQuestionNotEmpty() {
			return !question.isEmpty();
		}
		
	}
	
	private class NewPasswordValidator implements Validator {
		
		private final User user;
		
		/**
		 * @param user authenticated user
		 */
		public NewPasswordValidator(User user) {
			this.user = user;
		}
		
		/**
		 * @see org.springframework.validation.Validator#supports(java.lang.Class)
		 */
		public boolean supports(Class c) {
			return c.equals(NewPassword.class);
		}
		
		/**
		 * @see org.springframework.validation.Validator#validate(java.lang.Object,
		 *      org.springframework.validation.Errors)
		 */
		public void validate(Object object, Errors errors) {
			NewPassword newPassword = (NewPassword) object;
			if (newPassword.isNotSameAsConfirmPassword()) {
				errors.reject("error.password.match");
			} else if (newPassword.isEmpty()) {
				errors.reject("error.password.weak");
			} else {
				try {
					newPassword.checkStrength(user);
				}
				catch (PasswordException e) {
					errors.reject(e.getMessage());
				}
			}
		}
		
	}
	
	private class NewPassword {
		
		private final String password;
		
		private final String confirmPassword;
		
		/**
		 * @param password to be set
		 * @param confirmPassword to verify
		 */
		public NewPassword(String password, String confirmPassword) {
			this.password = password == null ? "" : password;
			this.confirmPassword = confirmPassword == null ? "" : confirmPassword;
		}
		
		/**
		 * Method to check the strength of the password for the user
		 * 
		 * @param user for which the password strength should be verified
		 */
		public void checkStrength(User user) throws PasswordException {
			OpenmrsUtil.validatePassword(user.getUsername(), password, user.getSystemId());
		}
		
		/**
		 * Checks if the password field is empty
		 * 
		 * @return true if the password is empty
		 */
		public boolean isEmpty() {
			return getPassword().isEmpty();
		}
		
		/**
		 * This checks if the password and the confirmPassword fields are same or not
		 * 
		 * @return true of the passwords are same
		 */
		public boolean isNotSameAsConfirmPassword() {
			return !getPassword().equals(confirmPassword);
		}
		
		/**
		 * @return the password
		 */
		public String getPassword() {
			return password;
		}
		
	}
}

/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.validator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.openmrs.Person;
import org.openmrs.User;
import org.openmrs.annotation.Handler;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.PrivilegeConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Validates attributes on the User object
 *
 * @since 1.5
 */
@Handler(supports = { User.class }, order = 50)
public class UserValidator implements Validator {
	
	/** Logger for this class and subclasses */
	private static final Logger log = LoggerFactory.getLogger(UserValidator.class);
	
	@Autowired
	private PersonValidator personValidator;

	/**
	 * Determines if the command object being submitted is a valid type
	 *
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	@Override
	public boolean supports(Class<?> clazz) {
		return User.class.isAssignableFrom(clazz);
	}
	
	/**
	 * Checks the form object for any inconsistencies/errors
	 *
	 * @see org.springframework.validation.Validator#validate(java.lang.Object,
	 *      org.springframework.validation.Errors)
	 * <strong>Should</strong> fail validation if retired and retireReason is null
	 * <strong>Should</strong> fail validation if retired and retireReason is empty
	 * <strong>Should</strong> fail validation if retired and retireReason is whitespace
	 * <strong>Should</strong> pass validation if all required fields have proper values
	 * <strong>Should</strong> fail validation if email as username enabled and email invalid
	 * <strong>Should</strong> fail validation if email as username disabled and email provided
	 * <strong>Should</strong> not throw NPE when user is null
	 * <strong>Should</strong> pass validation if field lengths are correct
	 * <strong>Should</strong> fail validation if field lengths are not correct
	 */
	@Override
	public void validate(Object obj, Errors errors) {
		User user = (User) obj;
		if (user == null) {
			errors.reject("error.general");
		} else {
			if (user.getRetired() && StringUtils.isBlank(user.getRetireReason())) {
				errors.rejectValue("retireReason", "error.null");
			}
			if (user.getPerson() == null) {
				errors.rejectValue("person", "error.null");
			} else {
				// check that required person details are filled out
				Person person = user.getPerson();
				if (person.getGender() == null) {
					errors.rejectValue("person.gender", "error.null");
				}
				if (person.getDead() == null) {
					errors.rejectValue("person.dead", "error.null");
				}
				if (person.getVoided() == null) {
					errors.rejectValue("person.voided", "error.null");
				}
				if (person.getPersonName() == null || StringUtils.isEmpty(person.getPersonName().getFullName())) {
					errors.rejectValue("person", "Person.names.length");
				}
				errors.pushNestedPath("person");
				try {
					personValidator.validate(person, errors);
				} finally {
					errors.popNestedPath();
				}
					
			}
			
			AdministrationService as = Context.getAdministrationService();
			boolean emailAsUsername;
			try {
				Context.addProxyPrivilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES);
				emailAsUsername = Boolean.parseBoolean(as.getGlobalProperty(
				    OpenmrsConstants.GLOBAL_PROPERTY_USER_REQUIRE_EMAIL_AS_USERNAME, "false"));
			}
			finally {
				Context.removeProxyPrivilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES);
			}
			
			if (emailAsUsername) {
				boolean isValidUserName = isUserNameAsEmailValid(user.getUsername());
				if (!isValidUserName) {
					errors.rejectValue("username", "error.username.email");
				}
			} else {
				boolean isValidUserName = isUserNameValid(user.getUsername());
				if (!isValidUserName) {
					errors.rejectValue("username", "error.username.pattern");
				}
			}
			
			if (!StringUtils.isBlank(user.getEmail()) && !isEmailValid(user.getEmail())) {
				errors.rejectValue("email", "error.email.invalid");
			}
			
			ValidateUtil.validateFieldLengths(errors, obj.getClass(), "username", "systemId", "retireReason");
		}
	}
	
	/**
	 * Convenience method to check the given username against the regular expression. <br>
	 * <br>
	 * A valid username will have following: <ul><li>Begins with Alphanumeric characters <li>only
	 * followed by more alphanumeric characters (may include . - _) <li>can be at most 50 characters
	 * <li>minimum 2 chars case-insensitive Examples: <li>The following username will pass
	 * validation: A123_.-XYZ9</ul>
	 *
	 * @param username the username string to check
	 * @return true if the username is ok
	 * <strong>Should</strong> validate username with only alpha numerics
	 * <strong>Should</strong> validate username with alpha dash and underscore
	 * <strong>Should</strong> validate username with alpha dash underscore and dot
	 * <strong>Should</strong> validate username with exactly max size name
	 * <strong>Should</strong> not validate username with less than minimumLength
	 * <strong>Should</strong> not validate username with invalid character
	 * <strong>Should</strong> not validate username with more than maximum size
	 * <strong>Should</strong> validate when username is null
	 * <strong>Should</strong> validate when username is the empty string
	 * <strong>Should</strong> not validate when username is whitespace only
	 */
	public boolean isUserNameValid(String username) {
		//Initialize reg ex for userName pattern
		// ^ = start of line
		// \w = [a-zA-Z_0-9]
		// \Q = quote everything until \E 
		// $ = end of line
		// complete meaning = 2-50 characters, the first must be a letter, digit, or _, and the rest may also be - or .
		String expression = "^[\\w][\\Q_\\E\\w-\\.]{1,49}$";
		// empty usernames are allowed
		if (StringUtils.isEmpty(username)) {
			return true;
		}
		
		try {
			//Make the comparison case-insensitive.
			Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
			Matcher matcher = pattern.matcher(username);
			return matcher.matches();
		}
		catch (PatternSyntaxException pex) {
			log.error("Username Pattern Syntax exception in UserValidator", pex);
			return false;
		}
	}
	
	/**
	 * Returns true if the given username is a valid e-mail.
	 *
	 * @param username
	 * @return true if valid
	 *
	 * <strong>Should</strong> return false if email invalid
	 * <strong>Should</strong> return true if email valid
	 */
	public boolean isUserNameAsEmailValid(String username) {
		return EmailValidator.getInstance().isValid(username);
	}
	
	/**
	 * @return true if email is valid or false otherwise
	 * @param email
	 */
	private boolean isEmailValid(String email) {
		return EmailValidator.getInstance().isValid(email);
	}
}

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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Person;
import org.openmrs.User;
import org.openmrs.annotation.Handler;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.PrivilegeConstants;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Validates attributes on the User object
 *
 * @since 1.5
 */
@Handler(supports = { User.class }, order = 50)
public class UserValidator implements Validator {
	
	/** Log for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());
	
	private static final Pattern EMAIL_PATTERN = Pattern
	        .compile("^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");
	
	/**
	 * Determines if the command object being submitted is a valid type
	 *
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	public boolean supports(Class c) {
		return c.equals(User.class);
	}
	
	/**
	 * Checks the form object for any inconsistencies/errors
	 *
	 * @see org.springframework.validation.Validator#validate(java.lang.Object,
	 *      org.springframework.validation.Errors)
	 * @should fail validation if retired and retireReason is null or empty or whitespace
	 * @should pass validation if all required fields have proper values
	 * @should fail validation if email as username enabled and email invalid
	 * @should fail validation if email as username disabled and email provided
	 * @should not throw NPE when user is null
	 * @should pass validation if field lengths are correct
	 * @should fail validation if field lengths are not correct
	 */
	public void validate(Object obj, Errors errors) {
		User user = (User) obj;
		if (user == null) {
			errors.reject("error.general");
		} else {
			if (user.isRetired() && StringUtils.isBlank(user.getRetireReason())) {
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
			}
			
			AdministrationService as = Context.getAdministrationService();
			boolean emailAsUsername = false;
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
			ValidateUtil.validateFieldLengths(errors, obj.getClass(), "username", "systemId", "retireReason",
			    "secretQuestion");
		}
	}
	
	/**
	 * Convenience method to check the given username against the regular expression. <br/>
	 * <br/>
	 * A valid username will have following: <li>Begins with Alphanumeric characters <li>only
	 * followed by more alphanumeric characters (may include . - _) <li>can be at most 50 characters
	 * <li>minimum 2 chars case-insensitive Examples: <li>The following username will pass
	 * validation: A123_.-XYZ9
	 *
	 * @param username the username string to check
	 * @return true if the username is ok
	 * @should validate username with only alpha numerics
	 * @should validate username with alpha dash and underscore
	 * @should validate username with alpha dash underscore and dot
	 * @should validate username with exactly max size name
	 * @should not validate username with less than minimumLength
	 * @should not validate username with invalid character
	 * @should not validate username with more than maximum size
	 * @should validate when username is null
	 * @should validate when username is the empty string
	 * @should not validate when username is whitespace only
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
	 * @should return false if email invalid
	 * @should return true if email valid
	 */
	public boolean isUserNameAsEmailValid(String username) {
		if (StringUtils.isBlank(username)) {
			return false;
		}
		
		Matcher matcher = EMAIL_PATTERN.matcher(username);
		return matcher.matches();
	}
}

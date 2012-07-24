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
package org.openmrs.validator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Person;
import org.openmrs.User;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Validates attributes on the User object
 * 
 * @since 1.5
 */
public class UserValidator implements Validator {
	
	/** Log for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());
	
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
	 */
	
	public void validate(Object obj, Errors errors) {
		User user = (User) obj;
		if (user == null) {
			errors.rejectValue("user", "error.general");
		} else {
			if (user.isRetired() && !StringUtils.hasText(user.getRetireReason()))
				errors.rejectValue("retireReason", "error.null");
			if (user.getPerson() == null) {
				errors.rejectValue("person", "error.null");
			} else {
				// check that required person details are filled out
				Person person = user.getPerson();
				if (person.getGender() == null)
					errors.rejectValue("person.gender", "error.null");
				if (person.getDead() == null)
					errors.rejectValue("person.dead", "error.null");
				if (person.getVoided() == null)
					errors.rejectValue("person.voided", "error.null");
				if (person.getPersonName() == null || !StringUtils.hasText(person.getPersonName().toString()))
					errors.rejectValue("person", "Person.names.length");
			}
		}
		
		if (!isUserNameValid(user.getUsername()))
			errors.rejectValue("username", "error.username.pattern");
	}
	
	/**
	 * Convenience method to check the given username against the regular expression. <br/>
	 * <br/>
	 * A valid username will have following:
	 * <li>Begins with Alphanumeric characters
	 * <li>only followed by more alphanumeric characters (may include . - _) 
	 * <li>can be at most 50 characters
	 * <li>minimum 2 chars case-insensitive Examples:
	 * <li>The following username will pass validation: A123_.-XYZ9
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
	 * @should not validate when username is null
	 */
	public boolean isUserNameValid(String username) {
		//Initialize reg ex for userName pattern 
		String expression = "^[\\w][\\Q_\\E\\w-\\.]{1,49}$";
		
		if (username == null)
			return false;
		
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
	
}

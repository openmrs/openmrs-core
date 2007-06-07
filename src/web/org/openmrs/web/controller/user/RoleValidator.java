package org.openmrs.web.controller.user;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Role;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

public class RoleValidator implements Validator {

	/** Log for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());

	/**
	 * 
	 * Determines if the command object being submitted is a valid type
	 * 
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	public boolean supports(Class c) {
		return c.equals(Role.class);
	}

	/**
	 * 
	 * Checks the form object for any inconsistencies/errors
	 * 
	 * @see org.springframework.validation.Validator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	public void validate(Object obj, Errors errors) {
		Role role = (Role)obj;
		if (role == null) {
			errors.rejectValue("role", "error.general");
		}
		else {
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "role", "error.role");
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "description", "error.description");
		}
	}

}
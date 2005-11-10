package org.openmrs.web.controller.user;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Privilege;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class PrivilegeValidator implements Validator {

	/** Log for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());

	/**
	 * 
	 * Determines if the command object being submitted is a valid type
	 * 
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	public boolean supports(Class c) {
		return c.equals(Privilege.class);
	}

	/**
	 * 
	 * Checks the form object for any inconsistencies/errors
	 * 
	 * @see org.springframework.validation.Validator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	public void validate(Object obj, Errors errors) {
		Privilege privilege = (Privilege)obj;
		if (privilege == null) {
			errors.rejectValue("privilege", "error.general");
		}
		else {
			if (privilege.getPrivilege() == null || privilege.getPrivilege().equals("")) {
				errors.rejectValue("privilege", "error.privilege");
			}
			if (privilege.getDescription() == null || privilege.getDescription().equals("")) {
				errors.rejectValue("description", "error.description");
			}
		}
		//log.debug("errors: " + errors.getAllErrors().toString());
	}

}
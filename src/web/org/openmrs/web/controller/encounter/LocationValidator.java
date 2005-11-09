package org.openmrs.web.controller.encounter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Location;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class LocationValidator implements Validator {

	/** Log for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());

	/**
	 * 
	 * Determines if the command object being submitted is a valid type
	 * 
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	public boolean supports(Class c) {
		return c.equals(Location.class);
	}

	/**
	 * 
	 * Checks the form object for any inconsistencies/errors
	 * 
	 * @see org.springframework.validation.Validator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	public void validate(Object obj, Errors errors) {
		Location location = (Location)obj;
		if (location == null) {
			errors.rejectValue("location", "general.error");
		}
		else {
			if (location.getName() == null || location.getName().equals("")) {
				errors.rejectValue("name", "general.name.error");
			}
			if (location.getDescription() == null || location.getDescription().equals("")) {
				errors.rejectValue("description", "general.description.error");
			}
		}
		//log.debug("errors: " + errors.getAllErrors().toString());
	}

}
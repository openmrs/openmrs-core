package org.openmrs.web.controller.observation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.MimeType;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

public class MimeTypeValidator implements Validator {

	/** Log for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());

	/**
	 * 
	 * Determines if the command object being submitted is a valid type
	 * 
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	public boolean supports(Class c) {
		return c.equals(MimeType.class);
	}

	/**
	 * 
	 * Checks the form object for any inconsistencies/errors
	 * 
	 * @see org.springframework.validation.Validator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	public void validate(Object obj, Errors errors) {
		MimeType orderType = (MimeType)obj;
		if (orderType == null) {
			errors.rejectValue("orderType", "error.general");
		}
		else {
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "mimeType", "error.mimeType");
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "description", "error.description");
			if (orderType.getMimeType() == null || orderType.getMimeType().equals("")) {
				errors.rejectValue("mimeType", "error.name");
			}
			if (orderType.getDescription() == null || orderType.getDescription().equals("")) {
				errors.rejectValue("description", "error.description");
			}
		}
		//log.debug("errors: " + errors.getAllErrors().toString());
	}

}
package org.openmrs.web.controller.patient;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.PatientIdentifierType;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class PatientIdentifierTypeValidator implements Validator {

	/** Log for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());

	/**
	 * 
	 * Determines if the command object being submitted is a valid type
	 * 
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	public boolean supports(Class c) {
		return c.equals(PatientIdentifierType.class);
	}

	/**
	 * 
	 * Checks the form object for any inconsistencies/errors
	 * 
	 * @see org.springframework.validation.Validator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	public void validate(Object obj, Errors errors) {
		PatientIdentifierType identifierType = (PatientIdentifierType)obj;
		if (identifierType == null) {
			errors.rejectValue("identifierType", "error.general");
		}
		else {
			if (identifierType.getName() == null || identifierType.getName().equals("")) {
				errors.rejectValue("name", "error.name");
			}
			if (identifierType.getDescription() == null || identifierType.getDescription().equals("")) {
				errors.rejectValue("description", "error.description");
			}
		}
	}

}
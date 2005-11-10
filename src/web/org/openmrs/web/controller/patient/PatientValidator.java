package org.openmrs.web.controller.patient;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class PatientValidator implements Validator {

	/** Log for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());

	/**
	 * 
	 * Determines if the command object being submitted is a valid type
	 * 
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	public boolean supports(Class c) {
		return c.equals(Patient.class);
	}

	/**
	 * 
	 * Checks the form object for any inconsistencies/errors
	 * 
	 * @see org.springframework.validation.Validator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	public void validate(Object obj, Errors errors) {
		Patient patient = (Patient)obj;
		if (patient == null) {
			errors.rejectValue("patient", "error.general");
		}
		else {
			if (patient.getIdentifiers() == null || patient.getIdentifiers().size() < 1) {
				errors.rejectValue("identifiers", "error.identifiers");
			}
			if (patient.getNames() == null || patient.getNames().size() < 1) {
				errors.rejectValue("names", "error.name");
			}
		}
	}

}
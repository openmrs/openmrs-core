package org.openmrs.reporting;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class DrugOrderPatientFilterValidator implements Validator {

	public boolean supports(Class cls) {
		return (cls.equals(DrugOrderPatientFilter.class));
	}

	public void validate(Object arg0, Errors arg1) {
		// do nothing
	}

}

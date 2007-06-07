package org.openmrs.reporting;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class NumericObsPatientFilterValidator implements Validator {

	public boolean supports(Class cls) {
		return (cls.equals(NumericObsPatientFilter.class));
	}

	public void validate(Object obj, Errors errors) {
		// do nothing
	}
}

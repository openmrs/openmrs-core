package org.openmrs.reporting;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class TextObsPatientFilterValidator implements Validator {

	public boolean supports(Class cls) {
		return (cls.equals(NumericObsPatientFilter.class));
	}

	public void validate(Object arg0, Errors arg1) {
		// TODO Auto-generated method stub
	}

}

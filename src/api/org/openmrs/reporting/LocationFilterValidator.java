package org.openmrs.reporting;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class LocationFilterValidator implements Validator {

	public boolean supports(Class c) {
		return (c.equals(LocationFilter.class));
	}

	public void validate(Object arg0, Errors arg1) {
		// TODO Auto-generated method stub

	}

}

package org.openmrs.dynamicformentry;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class PatientFormEntryModelValidator implements Validator {

	public boolean supports(Class c) {
		return c.equals(PatientFormEntryModel.class);
	}

	public void validate(Object o, Errors errors) {
		PatientFormEntryModel model = (PatientFormEntryModel) o;
	}

}

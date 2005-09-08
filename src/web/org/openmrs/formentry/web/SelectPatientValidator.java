package org.openmrs.formentry.web;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class SelectPatientValidator implements Validator {

	// private final Log logger = LogFactory.getLog(getClass());

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	public boolean supports(Class c) {
		return c.equals(SimpleQueryCommand.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.validation.Validator#validate(java.lang.Object,
	 *      org.springframework.validation.Errors)
	 */
	public void validate(Object obj, Errors errors) {
		SimpleQueryCommand query = (SimpleQueryCommand) obj;

		String q = query.getQ();

		if (q == null || q.length() < 1) {
			errors.rejectValue("q", "error.required-value", null,
					"Required field");
		}

	}
}

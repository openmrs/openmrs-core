package org.openmrs.formentry.web;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class PatientSearchValidator implements Validator {
	
	public final int MINIMUM_SEARCH_LENGTH = 3;

//	private final Log logger = LogFactory.getLog(getClass());

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
			errors.rejectValue("q", "error.required-value",
					new Object[] {"Patient name or identifier "},
					"Required field");
		} else {
			// q is not null and at least 1 character in length
			if (q.length() < MINIMUM_SEARCH_LENGTH) {
				errors.rejectValue("q", "error.minimum-length",
						new Object[] {String.valueOf(MINIMUM_SEARCH_LENGTH)},
						"You must enter more for search");
			}
		}

	}
}

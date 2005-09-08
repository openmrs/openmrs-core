package org.openmrs.web;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class LoginValidator implements Validator {

	private final Log logger = LogFactory.getLog(getClass());

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	public boolean supports(Class c) {
		return c.equals(Credentials.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.validation.Validator#validate(java.lang.Object,
	 *      org.springframework.validation.Errors)
	 */
	public void validate(Object obj, Errors errors) {
		Credentials credentials = (Credentials) obj;
		if (credentials == null) {
			errors.rejectValue("username", "error.login.username-required", null, "Username required");
		} else {
			logger.info("Validating credentials for "
					+ credentials.getUsername());

			if (credentials.getUsername() == null
					|| credentials.getUsername().length() < 1) {
				errors.rejectValue("username", "error.login.username-required", null, "Username required");
			}
			if (credentials.getPassword() == null
					|| credentials.getPassword().length() < 1) {
				errors.rejectValue("password", "error.login.password-required", null, "Password required");
			}
			// Context context = ContextFactory.getContext();
			// try {
			// context.authenticate(credentials.getUsername(),
			// credentials.getPassword());
			// } catch (ContextAuthenticationException e) {
			// errors.reject("Invalid credentials");
			// }
			//			
			// if (!context.isAuthenticated()) {
			// errors.reject("Login failed");
			// }

		}
		// TODO Auto-generated method stub

	}

}

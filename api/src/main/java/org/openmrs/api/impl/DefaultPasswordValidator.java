package org.openmrs.api.impl;

import org.openmrs.User;
import org.openmrs.api.PasswordException;
import org.openmrs.api.PasswordValidator;
import org.openmrs.util.OpenmrsUtil;
import org.springframework.stereotype.Component;

/**
 * Default implementation that uses the existing OpenmrsUtil validation logic.
 */
 @Component("defaultPasswordValidator")
public class DefaultPasswordValidator implements PasswordValidator {


	@Override
	public void validate(User user, String password) throws PasswordException {
		OpenmrsUtil.validatePassword(user.getUsername() , password , user.getSystemId());
	}
}

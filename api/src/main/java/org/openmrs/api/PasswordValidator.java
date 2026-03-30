package org.openmrs.api;

import org.openmrs.User;

public interface PasswordValidator {
	 
	   /**
	   * Validates the given password for the specified user.
	   * 
	   * @param user the user whose password is being set/changed
	   * @param password the plain-text password to validate
	   * @throws PasswordException if the password does not meet requirements
	   */
	   
	   void validate(User user , String password) throws PasswordException;
}

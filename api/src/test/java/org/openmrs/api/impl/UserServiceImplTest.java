/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.impl;

import org.junit.Test;
import org.openmrs.User;
import org.openmrs.api.PasswordException;
import org.openmrs.api.UserService;

/**
 * Unit tests for methods that are specific to the {@link UserServiceImpl}. General tests that
 * would span implementations should go on the {@link UserService}.
 */
public class UserServiceImplTest {

	// testing UserServiceImpl.changePassword(String user, String pw)
	@Test(expected = PasswordException.class)
	public void changePassword_shouldThrowPassordExceptionWithEmptyStringPassword() throws Exception {
		User user = new User();
		user.setUsername("john");
		user.setSystemId("1");
		
		UserService userService = new UserServiceImpl();
		
		userService.changePassword(user, "");
	}
	
	// testing UserServiceImpl.changePassword(User user, String hashedPassword, String salt)
	@Test(expected = PasswordException.class)
	public void changePassword_shouldThrowPasswordExceptionWithNullPassword() throws Exception {
		User user = new User();
		user.setUsername("john");
		user.setSystemId("1");
		
		UserService userService = new UserServiceImpl();
		userService.changeHashedPassword(user, null, "");
	}
	
	// testing UserServiceImpl.changePassword(String pw, String pw2)
	@Test(expected = PasswordException.class)
	public void changePassord_shouldThrowPasswordExceptionWithAlldigitPassword() throws Exception {
		String newPwd = "12345"; // invalid
		String oldPwd = "oldpassword";
		
		UserService userService = new UserServiceImpl();
		
		userService.changePassword(oldPwd, newPwd);
	}
}

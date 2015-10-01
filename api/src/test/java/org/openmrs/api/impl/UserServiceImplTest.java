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

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openmrs.User;
import org.openmrs.api.InvalidCharactersPasswordException;
import org.openmrs.api.ShortPasswordException;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;

public class UserServiceImplTest extends BaseContextSensitiveTest {

	UserService userService;
	User user;
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	@Before
	public void before() {
		userService = Context.getUserService();
		user = Context.getAuthenticatedUser();
	}
	
	@Test
	public void changePassword_shouldThrowShortPasswordExceptionWithShortPassword() throws Exception {
		thrown.expect(ShortPasswordException.class);
		thrown.expectMessage("error.password.length");

		userService.changePassword("test", "");
	}

	@Test
	public void changePassword_shouldThrowInvalidCharactersPasswordExceptionWithAllDigitPassword() throws Exception {
		thrown.expect(InvalidCharactersPasswordException.class);
		thrown.expectMessage("error.password.requireMixedCase");
		userService.changePassword(user, "12341111111111111111111111");
	}
	
}

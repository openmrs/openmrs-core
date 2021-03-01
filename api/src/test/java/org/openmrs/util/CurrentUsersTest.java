/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.util;

import java.util.Set;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import org.openmrs.User;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.Credentials;
import org.openmrs.api.context.UsernamePasswordCredentials;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

public class CurrentUsersTest extends BaseContextSensitiveTest {
	private static final String USER_SET = "org/openmrs/util/CurrentUserTest.xml";
	@Autowired
	UserService userService;

	@Test
	public void getCurrentUsernames_shouldReturnUserNamesForLoggedInUsers() {
		executeDataSet(USER_SET);
		User user = userService.getUser(5508);
		Credentials credentials = new UsernamePasswordCredentials("Mukembo","Mukembo123");
		Context.authenticate(credentials);
		Assert.assertEquals(Context.getAuthenticatedUser().getUsername(),user.getUsername());
		Set<String> currentUserNames = CurrentUsers.getCurrentUsernames();
		Assert.assertTrue(currentUserNames.contains("Mukembo"));
	}

}

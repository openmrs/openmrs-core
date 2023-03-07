/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.controller.openmrs1_8;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openmrs.Privilege;
import org.openmrs.Role;
import org.openmrs.User;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.ContextAuthenticationException;
import org.openmrs.module.webservices.rest.web.RestTestConstants1_8;
import org.openmrs.module.webservices.rest.web.v1_0.controller.RestControllerTestUtils;
import org.openmrs.module.webservices.validation.ValidationException;
import org.openmrs.util.PrivilegeConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletResponse;

public class ChangePasswordController1_8Test extends RestControllerTestUtils {
	
	private static final String PASSWORD_URI = "password";
	
	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	
	@Autowired
	@Qualifier("userService")
	private UserService service;
	
	@Test
	public void updateUser_shouldUpdateTheUserPassword() throws Exception {
		User user = service.getUserByUuid(RestTestConstants1_8.USER_UUID);
		assertNotNull(user);
		assertNotEquals(user, Context.getAuthenticatedUser());
		final String username = user.getUsername();
		final String newPassword = "SomeOtherPassword123";
		
		ContextAuthenticationException exception = null;
		try {
			Context.authenticate(username, newPassword);
		}
		catch (ContextAuthenticationException e) {
			exception = e;
		}
		assertNotNull(exception);
		assertEquals("Invalid username and/or password: " + username, exception.getMessage());
		
		handle(newPostRequest("password" + "/" + user.getUuid(), "{\"newPassword\":\"" + newPassword + "\"}"));
		Context.logout();
		
		Context.authenticate(username, newPassword);
		assertEquals(user, Context.getAuthenticatedUser());
	}
	
	@Test
	public void testChangeUsersOwnPassword() throws Exception {
		setUpUser("butch");
		
		String oldPassword = "SomeOtherPassword123";
		String newPassword = "newPassword9";
		
		MockHttpServletResponse response = handle(newPostRequest(PASSWORD_URI, "{\"newPassword\":\"" + newPassword + "\""
		        + "," + "\"oldPassword\":\"" + oldPassword + "\"}"));
		assertEquals(200, response.getStatus());
	}
	
	@Test
	public void testChangeUsersOwnPasswordWithOutAuthentication() throws Exception {
		// we log out, so there is no authenticated user
		Context.logout();
		String oldPassword = "SomeOtherPassword123";
		String newPassword = "newPassword9";
		
		expectedException.expect(APIAuthenticationException.class);
		expectedException.expectMessage("Must be authenticated to change your own password");
		
		handle(newPostRequest(PASSWORD_URI, "{\"newPassword\":\"" + newPassword + "\"" + "," + "\"oldPassword\":\""
		        + oldPassword + "\"}"));
	}
	
	@Test
	public void testChangeUsersOwnPasswordWithIncorrectOldPassword() throws Exception {
		setUpUser("butch");
		
		String wrongOldPassword = "WrongPassword";
		String newPassword = "newPassword9";
		
		expectedException.expect(ValidationException.class);
		expectedException.expectMessage("Passwords don't match");
		
		handle(newPostRequest(PASSWORD_URI, "{\"newPassword\":\"" + newPassword + "\"" + "," + "\"oldPassword\":\""
		        + wrongOldPassword + "\"}"));
	}
	
	@Test
	public void testUserChangeOtherUsersPassword() throws Exception {
		User authenticatedUser = setUpUser("daemon");
		
		Role role = new Role("Privileged Role");
		role.addPrivilege(new Privilege(PrivilegeConstants.EDIT_USER_PASSWORDS));
		authenticatedUser.addRole(role);
		
		String newPassword = "newPassword9";
		
		MockHttpServletResponse response = handle(newPostRequest(PASSWORD_URI + "/" + RestTestConstants1_8.USER_UUID,
		    "{\"password\":\"" + newPassword + "\"}"));
		
		assertEquals(200, response.getStatus());
	}
	
	@Test
	public void testUserChangeOtherUsersPasswordWithOutPrivilege() throws Exception {
		setUpUser("daemon");
		
		String newPassword = "newPassword9";
		
		expectedException.expect(APIAuthenticationException.class);
		expectedException.expectMessage("Privileges required: [Edit User Passwords]");
		
		handle(newPostRequest(PASSWORD_URI + "/" + RestTestConstants1_8.USER_UUID, "{\"newPassword\":\"" + newPassword
		        + "\"}"));
		
	}
	
	@Test
	public void testThrowExceptionIfUserIsNotAvailable() throws Exception {
		setUpUser("daemon");
		
		String newPassword = "newPassword9";
		
		expectedException.expect(NullPointerException.class);
		
		handle(newPostRequest(PASSWORD_URI + "/" + "someRandomUserUuid", "{\"newPassword\":\"" + newPassword + "\"}"));
	}
	
	private User setUpUser(String userName) throws Exception {
		User user = service.getUserByUsername(userName);
		final String newPassword = "SomeOtherPassword123";
		
		service.changePassword(user, newPassword);
		
		Context.logout();
		
		Context.authenticate(userName, newPassword);
		return Context.getAuthenticatedUser();
	}
	
}

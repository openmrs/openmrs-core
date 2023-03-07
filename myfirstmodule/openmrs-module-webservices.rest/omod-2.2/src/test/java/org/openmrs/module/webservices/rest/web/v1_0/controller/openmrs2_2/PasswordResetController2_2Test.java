/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.controller.openmrs2_2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openmrs.User;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.LoginCredential;
import org.openmrs.api.db.UserDAO;
import org.openmrs.module.webservices.rest.web.v1_0.controller.RestControllerTestUtils;
import org.openmrs.notification.MessageException;
import org.openmrs.util.OpenmrsConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletResponse;

public class PasswordResetController2_2Test extends RestControllerTestUtils {
	
	private static final String RESET_PASSWORD_URI = "passwordreset";
	
	@Autowired
	@Qualifier("userService")
	private UserService userService;
	
	@Autowired
	private UserDAO dao;
	
	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	
	@Before
	public void before() {
		Context.getAdministrationService().setGlobalProperty(OpenmrsConstants.GP_HOST_URL,
		    "http://localhost:8080/openmrs/admin/users/changePassword.form/{activationKey}");
	}
	
	@Test
	public void requestPasswordReset_shouldCreateUserActivationKeyGivenUsername() throws Exception {
		User user = userService.getUserByUuid("c98a1558-e131-11de-babe-001e378eb67e");
		assertNull(dao.getLoginCredential(user).getActivationKey());
		expectedException.expect(MessageException.class);
		handle(newPostRequest(RESET_PASSWORD_URI, "{\"usernameOrEmail\":\"" + user.getUsername() + "\"}"));
		assertNotNull(dao.getLoginCredential(user).getActivationKey());
	}
	
	@Test
	public void requestPasswordReset_shouldCreateUserActivationKeyGivenEmail() throws Exception {
		User user = userService.getUserByUuid("c98a1558-e131-11de-babe-001e378eb67e");
		user.setEmail("fanyuih@gmail.com");
		userService.saveUser(user);
		assertNull(dao.getLoginCredential(user).getActivationKey());
		expectedException.expect(MessageException.class);
		handle(newPostRequest(RESET_PASSWORD_URI, "{\"usernameOrEmail\":\"" + user.getEmail() + "\"}"));
		assertNotNull(dao.getLoginCredential(user).getActivationKey());
	}
	
	@Test
	public void resetPassword_shouldResetUserPasswordIfActivationKeyIsCorrect() throws Exception {
		User user = userService.getUserByUuid("c98a1558-e131-11de-babe-001e378eb67e");
		String key = "h4ph0fpNzQCIPSw8plJI";
		int validTime = 10 * 60 * 1000; //equivalent to 10 minutes for token to be valid
		Long tokenTime = System.currentTimeMillis() + validTime;
		LoginCredential credentials = dao.getLoginCredential(user);
		credentials
		        .setActivationKey("b071c88d6d877922e35af2e6a90dd57d37ac61143a03bb986c5f353566f3972a86ce9b2604c31a22dfa467922dcfd54fa7d18b0a7c7648d94ca3d97a88ea2fd0:"
		                + tokenTime);
		dao.updateLoginCredential(credentials);
		String newPassword = "newPasswordString123";
		MockHttpServletResponse response = handle(newPostRequest(RESET_PASSWORD_URI + "/" + key, "{\"newPassword\":\""
		        + newPassword + "\"}"));
		assertEquals(200, response.getStatus());
		Context.authenticate(user.getUsername(), newPassword);
		
	}
}

/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.filter.update;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.servlet.ServletException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.openmrs.api.context.Context;
import org.openmrs.web.test.jupiter.BaseWebContextSensitiveTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Tests some of the methods on the {@link UpdateFilter}
 */
public class UpdateFilterTest extends BaseWebContextSensitiveTest {
	
	/**
	 * @throws ServletException
	 * @see UpdateFilter#authenticateAsSuperUser(String,String)
	 */
	@Test
	public void authenticateAsSuperUser_shouldReturnFalseIfGivenInvalidCredentials() throws ServletException {
		assertFalse(new UpdateFilter().authenticateAsSuperUser("a-bad-username", "a-bad-password"));
	}
	
	/**
	 * @throws ServletException
	 * @see UpdateFilter#authenticateAsSuperUser(String,String)
	 */
	@Test
	public void authenticateAsSuperUser_shouldReturnFalseIfGivenUserIsNotSuperuser() throws ServletException {
		// can switch to using "butch" in standardDataSet once we know bruno's password
		executeDataSet("org/openmrs/api/include/UserServiceTest.xml");
		Context.authenticate("userWithSha512Hash", "test"); // sanity check
		Context.logout();
		
		assertFalse(new UpdateFilter().authenticateAsSuperUser("userWithSha512Hash", "test"));
	}
	
	/**
	 * @throws ServletException
	 * @see UpdateFilter#authenticateAsSuperUser(String,String)
	 */
	@Test
	public void authenticateAsSuperUser_shouldReturnTrueIfGivenUserIsSuperuser() throws ServletException {
		assertTrue(new UpdateFilter().authenticateAsSuperUser("admin", "test"));
	}
	
	/**
	 * @throws SQLException
	 * @throws Exception
	 * @see UpdateFilter#isSuperUser(Connection,Integer)
	 */
	@Test
	public void isSuperUser_shouldReturnTrueIfGivenUserHasSuperuserRole() throws SQLException {
		assertTrue(new UpdateFilter().isSuperUser(getConnection(), 1));
	}
	
	/**
	 * @throws SQLException
	 * @throws Exception
	 * @see UpdateFilter#isSuperUser(Connection,Integer)
	 */
	@Test
	public void isSuperUser_shouldReturnFalseIfGivenUserDoesNotHaveTheSuperUserRole() throws SQLException {
		assertFalse(new UpdateFilter().isSuperUser(getConnection(), 502));
	}

	@Test
	public void doPost_shouldHtmlEscapeUpdateWarnings() throws Exception {
		UpdateFilter filter = new UpdateFilter();
		reportUpdateWarning(filter, "criteria <img src=x onerror=alert(1)> & more");
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setParameter("page", "updateProgress.vm.ajaxRequest");
		MockHttpServletResponse response = new MockHttpServletResponse();

		filter.doPost(request, response);

		String content = response.getContentAsString();
		assertTrue(content.contains("&lt;img src=x onerror=alert(1)&gt; &amp; more"));
		assertFalse(content.contains("<img"));
	}

	private void reportUpdateWarning(UpdateFilter filter, String warning) throws Exception {
		Class<?> jobClass = Class.forName(UpdateFilter.class.getName() + "$UpdateFilterCompletion");
		Constructor<?> constructor = jobClass.getDeclaredConstructor(UpdateFilter.class);
		constructor.setAccessible(true);
		Object job = constructor.newInstance(filter);
		Method reportWarnings = jobClass.getDeclaredMethod("reportWarnings", List.class);
		reportWarnings.setAccessible(true);
		reportWarnings.invoke(job, Collections.singletonList(warning));
		Field field = UpdateFilter.class.getDeclaredField("updateJob");
		field.setAccessible(true);
		field.set(filter, job);
	}
}

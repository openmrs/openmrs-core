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
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.beanutils.PropertyUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.User;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.ContextAuthenticationException;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.test.Util;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.RestTestConstants1_8;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.api.SearchConfig;
import org.openmrs.module.webservices.rest.web.resource.api.SearchHandler;
import org.openmrs.module.webservices.rest.web.resource.api.SearchParameter;
import org.openmrs.module.webservices.rest.web.resource.api.SearchQuery;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest;
import org.openmrs.module.webservices.rest.web.v1_0.wrapper.openmrs1_8.UserAndPassword1_8;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.request.WebRequest;

public class UserController1_8Test extends MainResourceControllerTest {
	
	public static final String MOCKED_USER_NAME_FOR_FIRST_COMPONENT = "FirstComponent";
	
	public static final String MOCKED_USER_NAME_FOR_SECOND_COMPONENT = "SecondComponent";
	
	public static final String MOCKED_USER_NAME_FOR_THRID_COMPONENT = "ThirdComponent";
	
	private UserService service;
	
	@Before
	public void init() {
		service = Context.getUserService();
	}
	
	@Component
	public static class UserSearchHandlerWithRequiredUsernameAndOptionalLocalesParams implements SearchHandler {
		
		@Autowired
		@Qualifier("userService")
		UserService userService;
		
		@Override
		public SearchConfig getSearchConfig() {
			return new SearchConfig("config-for-first-test", RestConstants.VERSION_1 + "/user",
					Collections.singletonList("1.8.* - 9.*"),
			        new SearchQuery.Builder(
			                "Allows you to find users by username")
			                .withRequiredParameters(new SearchParameter("username", "admin"))
			                .withOptionalParameters("preferredLocales").build());
		}
		
		@Override
		public PageableResult search(RequestContext context) throws ResponseException {
			User user = new User();
			user.setUsername(MOCKED_USER_NAME_FOR_FIRST_COMPONENT);
			List<UserAndPassword1_8> users = new ArrayList<UserAndPassword1_8>();
			users.add(new UserAndPassword1_8(user));
			return new NeedsPaging<UserAndPassword1_8>(users, context);
		}
	}
	
	@Component
	public static class UserSearchHandlerWithRequiredIdAndOptionalUsernameParams implements SearchHandler {
		
		@Autowired
		@Qualifier("userService")
		UserService userService;
		
		@Override
		public SearchConfig getSearchConfig() {
			return new SearchConfig("config-for-second-test", RestConstants.VERSION_1 + "/user",
					Collections.singletonList("1.8.* - 9.*"),
			        new SearchQuery.Builder(
			                "Allows you to find users by username").withRequiredParameters(new SearchParameter("systemId"))
			                .withOptionalParameters(new SearchParameter("username", "bruno")).build());
		}
		
		@Override
		public PageableResult search(RequestContext context) throws ResponseException {
			User user = new User();
			user.setUsername(MOCKED_USER_NAME_FOR_SECOND_COMPONENT);
			List<UserAndPassword1_8> users = new ArrayList<UserAndPassword1_8>();
			users.add(new UserAndPassword1_8(user));
			return new NeedsPaging<UserAndPassword1_8>(users, context);
		}
	}
	
	@Component
	public static class UserSearchHandlerWithOptionalParam implements SearchHandler {
		
		@Autowired
		@Qualifier("userService")
		UserService userService;
		
		@Override
		public SearchConfig getSearchConfig() {
			return new SearchConfig("config-for-third-test", RestConstants.VERSION_1 + "/user",
					Collections.singletonList("1.8.* - 9.*"),
			        new SearchQuery.Builder(
			                "Allows you to find users by username").withOptionalParameters(
			            new SearchParameter("username", "bruno"))
			                .build());
		}
		
		@Override
		public PageableResult search(RequestContext context) throws ResponseException {
			User user = new User();
			user.setUsername(MOCKED_USER_NAME_FOR_THRID_COMPONENT);
			List<UserAndPassword1_8> users = new ArrayList<UserAndPassword1_8>();
			users.add(new UserAndPassword1_8(user));
			return new NeedsPaging<UserAndPassword1_8>(users, context);
		}
	}
	
	/**
	 * @see UserController#createUser(SimpleObject,WebRequest)
	 * @throws Exception
	 * @verifies create a new user
	 */
	@Test
	public void createUser_shouldCreateANewUser() throws Exception {
		
		long originalCount = getAllCount();
		
		SimpleObject user = new SimpleObject();
		user.add("username", "testuser");
		user.add("password", "Secret123");
		user.add("person", "da7f524f-27ce-4bb2-86d6-6d1d05312bd5");
		
		String json = new ObjectMapper().writeValueAsString(user);
		
		MockHttpServletRequest req = request(RequestMethod.POST, getURI());
		req.setContent(json.getBytes());
		
		SimpleObject newUser = deserialize(handle(req));
		
		Util.log("Created User", newUser);
		Assert.assertNotNull(PropertyUtils.getProperty(newUser, "uuid"));
		assertEquals(originalCount + 1, getAllCount());
	}
	
	/**
	 * @see UserController#createUser(SimpleObject,WebRequest)
	 * @throws Exception
	 * @verifies create a new user
	 */
	@Test
	public void createUser_shouldCreateANewUserWithRoles() throws Exception {
		
		long originalCount = getAllCount();
		
		SimpleObject user = new SimpleObject();
		user.add("username", "testuser");
		user.add("password", "Secret123");
		user.add("person", "da7f524f-27ce-4bb2-86d6-6d1d05312bd5");
		user.add("roles", new String[] { "3480cb6d-c291-46c8-8d3a-96dc33d199fb" });
		
		String json = new ObjectMapper().writeValueAsString(user);
		
		MockHttpServletRequest req = request(RequestMethod.POST, getURI());
		req.setContent(json.getBytes());
		
		SimpleObject newUser = deserialize(handle(req));
		
		Util.log("Created another user with a role this time.", newUser);
		Assert.assertNotNull(PropertyUtils.getProperty(newUser, "uuid"));
		assertEquals(originalCount + 1, getAllCount());
		
		User createdUser = service.getUserByUuid(getUuid());
		Assert.assertNotNull(createdUser);
		assertTrue(createdUser.hasRole("Provider"));
	}
	
	/**
	 * @see UserController#getUser(UserAndPassword1_8,WebRequest)
	 * @throws Exception
	 * @verifies get a default representation of a UserAndPassword
	 */
	@Test
	public void getUser_shouldGetADefaultRepresentationOfAUser() throws Exception {
		
		final String userName = "butch";
		
		MockHttpServletRequest req = request(RequestMethod.GET, getURI() + "/" + getUuid());
		SimpleObject result = deserialize(handle(req));
		
		Assert.assertNotNull(result);
		Util.log("User retrieved (default)", result);
		
		assertEquals(getUuid(), PropertyUtils.getProperty(result, "uuid"));
		Assert.assertNotNull(PropertyUtils.getProperty(result, "username"));
		
		assertEquals(userName, PropertyUtils.getProperty(result, "username"));
		Assert.assertNull(PropertyUtils.getProperty(result, "auditInfo"));
	}
	
	/**
	 * @see PatientController#getPatient(String,WebRequest)
	 * @throws Exception
	 * @verifies get a full representation of a patient
	 */
	@Test
	public void getUser_shouldGetAFullRepresentationOfAPatient() throws Exception {
		
		MockHttpServletRequest req = request(RequestMethod.GET, getURI() + "/" + getUuid());
		req.addParameter(RestConstants.REQUEST_PROPERTY_FOR_REPRESENTATION, RestConstants.REPRESENTATION_FULL);
		
		SimpleObject result = deserialize(handle(req));
		Util.log("User retrieved (full)", result);
		
		Assert.assertNotNull(result);
		assertEquals(getUuid(), PropertyUtils.getProperty(result, "uuid"));
		
		Assert.assertNotNull(PropertyUtils.getProperty(result, "secretQuestion"));
		assertEquals("", PropertyUtils.getProperty(result, "secretQuestion"));
	}
	
	/**
	 * @see UserController#updateUser(UserAndPassword1_8,SimpleObject,WebRequest)
	 * @throws Exception
	 * @verifies change a property on a patient
	 */
	@Test
	public void updateUser_shouldChangeAPropertyOnAUser() throws Exception {
		
		User user = service.getUserByUuid(getUuid());
		Assert.assertNotNull(user);
		Assert.assertFalse("5-6".equals(user.getSystemId()));
		Util.log("Old User SystemId: ", user.getSystemId());
		
		String json = "{\"systemId\":\"5-6\",\"password\":\"Admin@123\"}";
		MockHttpServletRequest req = request(RequestMethod.POST, getURI() + "/" + getUuid());
		req.setContent(json.getBytes());
		handle(req);
		
		User editedUser = service.getUserByUuid(getUuid());
		Assert.assertNotNull(editedUser);
		assertEquals("5-6", editedUser.getSystemId());
		Util.log("Edited User SystemId: ", editedUser.getSystemId());
	}
	
	/**
	 * @see UserController#retireUser(User,String,WebRequest)
	 * @throws Exception
	 * @verifies void a patient
	 */
	@Test
	public void retireUser_shouldRetireAUser() throws Exception {
		
		User user = service.getUserByUuid(getUuid());
		Assert.assertFalse(user.isRetired());
		
		MockHttpServletRequest req = request(RequestMethod.DELETE, getURI() + "/" + user.getUuid());
		req.addParameter("!purge", "");
		req.addParameter("reason", "unit test");
		handle(req);
		
		User retiredUser = service.getUserByUuid(getUuid());
		assertTrue(retiredUser.isRetired());
		assertEquals("unit test", retiredUser.getRetireReason());
	}
	
	/**
	 * @see UserController#findUsers(String,WebRequest,HttpServletResponse)
	 * @throws Exception
	 * @verifies return no results if there are no matching users
	 */
	@Test
	public void findUsers_shouldReturnNoResultsIfThereAreNoMatchingUsers() throws Exception {
		
		MockHttpServletRequest req = request(RequestMethod.GET, getURI());
		req.addParameter("q", "foo-bar-baz");
		
		SimpleObject result = deserialize(handle(req));
		Assert.assertNotNull(result);
		
		List<User> hits = (List<User>) result.get("results");
		assertEquals(0, hits.size());
	}
	
	/**
	 * @see UserController#findUsers(String,WebRequest,HttpServletResponse)
	 * @throws Exception
	 * @verifies find matching users
	 */
	@Test
	public void findUsers_shouldFindMatchingUsers() throws Exception {
		
		MockHttpServletRequest req = request(RequestMethod.GET, getURI());
		req.addParameter("q", "but");
		
		SimpleObject result = deserialize(handle(req));
		Assert.assertNotNull(result);
		
		List<Object> hits = (List<Object>) result.get("results");
		assertEquals(1, hits.size());
		
		Util.log("Found " + hits.size() + " user(s)", result);
		assertEquals(service.getUserByUuid(getUuid()).getUuid(), PropertyUtils.getProperty(hits.get(0), "uuid"));
	}
	
	@Test
	public void shouldFindUserByUsername() throws Exception {
		SimpleObject response = deserialize(handle(newGetRequest(getURI(), new Parameter("username", "butch"))));
		List<Object> results = Util.getResultsList(response);
		
		assertEquals(1, results.size());
		Object next = results.iterator().next();
		
		Util.log("Found " + results.size() + " user(s) by username", response);
		assertEquals(getUuid(), PropertyUtils.getProperty(next, "uuid"));
	}
	
	@Test
	public void shouldFindUserByUsernameUsingRequiredSearchParameterForMockedHandler() throws Exception {
		SimpleObject response = deserialize(handle(newGetRequest(getURI(), new Parameter("username", "admin"))));
		List<Object> results = Util.getResultsList(response);
		
		Object next = results.iterator().next();
		
		assertEquals(1, results.size());
		assertEquals(MOCKED_USER_NAME_FOR_FIRST_COMPONENT, PropertyUtils.getProperty(next, "display"));
	}
	
	@Test
	public void shouldFindUserByUsernameUsingRequiredAndOptionalSearchParametersForMockedHandler() throws Exception {
		SimpleObject response = deserialize(handle(newGetRequest(getURI(), new Parameter("username", "admin"),
		    new Parameter("preferredLocales", "en"))));
		List<Object> results = Util.getResultsList(response);
		
		Object next = results.iterator().next();
		
		assertEquals(1, results.size());
		assertEquals(MOCKED_USER_NAME_FOR_FIRST_COMPONENT, PropertyUtils.getProperty(next, "display"));
	}
	
	@Test
	public void shouldFindUserByRequiredSystemIdAndOptionalUsernameSearchParametersForMockedHandler() throws Exception {
		SimpleObject response = deserialize(handle(newGetRequest(getURI(), new Parameter("systemId", "bruno"),
		    new Parameter("username", "bruno"))));
		List<Object> results = Util.getResultsList(response);
		
		Object next = results.iterator().next();
		
		assertEquals(1, results.size());
		assertEquals(MOCKED_USER_NAME_FOR_SECOND_COMPONENT, PropertyUtils.getProperty(next, "display"));
	}
	
	@Test
	public void shouldNotFindUserByRequiredSystemIdAndWrongOptionalUsernameSearchParametersForMockedHandler()
	        throws Exception {
		SimpleObject response = deserialize(handle(newGetRequest(getURI(), new Parameter("systemId", "bruno"),
		    new Parameter("username", "james"))));
		List<Object> results = Util.getResultsList(response);
		
		Object next = results.iterator().next();
		
		assertNotEquals(1, results.size());
		assertNotEquals(MOCKED_USER_NAME_FOR_SECOND_COMPONENT, PropertyUtils.getProperty(next, "display"));
	}
	
	@Test
	public void shouldNotFindUserByUsernameUsingWrongRequiredAndCorrectOptionalSearchParametersForMockedHandler()
	        throws Exception {
		SimpleObject response = deserialize(handle(newGetRequest(getURI(), new Parameter("username", "bruno"),
		    new Parameter("preferredLocales", "en"))));
		List<Object> results = Util.getResultsList(response);
		
		Object next = results.iterator().next();
		
		assertNotEquals(1, results.size());
		assertNotEquals(MOCKED_USER_NAME_FOR_THRID_COMPONENT, PropertyUtils.getProperty(next, "display"));
	}
	
	@Test
	public void shouldFindUserByUsernameUsingOptionalSearchParameterForMockedHandler() throws Exception {
		SimpleObject response = deserialize(handle(newGetRequest(getURI(), new Parameter("username", "bruno"))));
		List<Object> results = Util.getResultsList(response);
		
		Object next = results.iterator().next();
		
		assertEquals(1, results.size());
		assertEquals(MOCKED_USER_NAME_FOR_THRID_COMPONENT, PropertyUtils.getProperty(next, "display"));
	}
	
	@Test
	public void shouldNotFindUserByUsernameUsingWrongOptionalSearchParameterForMockedHandler() throws Exception {
		SimpleObject response = deserialize(handle(newGetRequest(getURI(), new Parameter("username", "james"))));
		List<Object> results = Util.getResultsList(response);
		
		assertEquals(0, results.size());
	}
	
	@Test
	public void getUser_shouldListAllUsers() throws Exception {
		
		MockHttpServletRequest req = request(RequestMethod.GET, getURI());
		SimpleObject result = deserialize(handle(req));
		
		Util.log("Users fetched: ", result);
		Assert.assertNotNull(result);
		
		Util.log("Total users fetched: ", getAllCount());
		assertEquals(getAllCount(), Util.getResultsSize(result));
	}
	
	@Test
	public void updateUser_shouldUpdateTheUserPassword() throws Exception {
		User user = service.getUserByUuid(getUuid());
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
		
		handle(newPostRequest(getURI() + "/" + user.getUuid(), "{\"password\":\"" + newPassword + "\"}"));
		Context.logout();
		
		Context.authenticate(username, newPassword);
		assertEquals(user, Context.getAuthenticatedUser());
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest#getURI()
	 */
	@Override
	public String getURI() {
		return "user";
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest#getUuid()
	 */
	@Override
	public String getUuid() {
		return RestTestConstants1_8.USER_UUID;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest#getAllCount()
	 */
	@Override
	public long getAllCount() {
		return service.getAllUsers().size();
	}
}

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

import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.beanutils.PropertyUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openmrs.Role;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.test.Util;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.RestTestConstants1_8;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.bind.annotation.RequestMethod;

public class RoleController1_8Test extends MainResourceControllerTest {
	
	private UserService service;
	
	@Before
	public void init() throws Exception {
		service = Context.getUserService();
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest#getURI()
	 */
	@Override
	public String getURI() {
		return "role";
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest#getAllCount()
	 */
	@Override
	public long getAllCount() {
		return service.getAllRoles().size();
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest#getUuid()
	 */
	@Override
	public String getUuid() {
		return RestTestConstants1_8.ROLE_UUID;
	}
	
	/**
	 * @see RoleController#create(SimpleObject, javax.servlet.http.HttpServletRequest,
	 *      HttpServletResponse)
	 * @verifies create a new Role
	 */
	@Test
	public void createRole_shouldCreateANewRole() throws Exception {
		
		long originalCount = getAllCount();
		
		SimpleObject location = new SimpleObject();
		location.add("name", "Role name");
		location.add("description", "Role description");
		
		String json = new ObjectMapper().writeValueAsString(location);
		
		MockHttpServletRequest req = request(RequestMethod.POST, getURI());
		req.setContent(json.getBytes());
		
		SimpleObject newRole = deserialize(handle(req));
		
		Assert.assertNotNull(PropertyUtils.getProperty(newRole, "uuid"));
		Assert.assertEquals(originalCount + 1, getAllCount());
		
	}
	
	/**
	 * @see RoleController#retrieve(String, javax.servlet.http.HttpServletRequest)
	 * @verifies get a default representation of a Role
	 */
	@Test
	public void getRole_shouldGetADefaultRepresentationOfARole() throws Exception {
		
		MockHttpServletRequest httpReq = request(RequestMethod.GET, getURI() + "/" + getUuid());
		SimpleObject result = deserialize(handle(httpReq));
		
		Assert.assertNotNull(result);
		Assert.assertNotNull(PropertyUtils.getProperty(result, "name"));
		
	}
	
	/**
	 * @see RoleController#retrieve(String, javax.servlet.http.HttpServletRequest)
	 * @verifies get a full representation of a Role
	 */
	@Test
	public void getRole_shouldGetAFullRepresentationOfARole() throws Exception {
		
		MockHttpServletRequest httpReq = request(RequestMethod.GET, getURI() + "/" + getUuid());
		httpReq.addParameter(RestConstants.REQUEST_PROPERTY_FOR_REPRESENTATION, RestConstants.REPRESENTATION_FULL);
		SimpleObject result = deserialize(handle(httpReq));
		
		Assert.assertNotNull(result);
		Assert.assertNotNull(PropertyUtils.getProperty(result, "auditInfo"));
		
	}
	
	/**
	 * @see RoleController#update(String, SimpleObject, javax.servlet.http.HttpServletRequest,
	 *      HttpServletResponse)
	 * @verifies change a property on a Role
	 */
	@Test
	public void updateRole_shouldChangeAPropertyOnARole() throws Exception {
		
		final String editedDescription = "Role description edited";
		String json = "{ \"description\":\"" + editedDescription + "\" }";
		MockHttpServletRequest req = request(RequestMethod.POST, getURI() + "/" + getUuid());
		req.setContent(json.getBytes());
		handle(req);
		
		Role editedRole = service.getRoleByUuid(getUuid());
		Assert.assertNotNull(editedRole);
		Assert.assertEquals(editedDescription, editedRole.getDescription());
		
	}
	
	/**
	 * @see RoleController#delete(String, String, javax.servlet.http.HttpServletRequest,
	 *      HttpServletResponse)
	 * @verifies void a Role
	 */
	@Test
	public void retireRole_shouldRetireARole() throws Exception {
		
		Role role = service.getRoleByUuid(getUuid());
		Assert.assertFalse(role.isRetired());
		
		MockHttpServletRequest req = request(RequestMethod.DELETE, getURI() + "/" + role.getUuid());
		req.addParameter("!purge", "");
		req.addParameter("reason", "random reason");
		handle(req);
		
		Role retiredRole = service.getRoleByUuid(getUuid());
		Assert.assertTrue(retiredRole.isRetired());
		Assert.assertEquals("random reason", retiredRole.getRetireReason());
		
	}
	
	@Test
	public void shouldUnRetireARole() throws Exception {
		Role role = service.getRoleByUuid(getUuid());
		role.setRetired(true);
		role.setRetireReason("random reason");
		service.saveRole(role);
		role = service.getRoleByUuid(getUuid());
		Assert.assertTrue(role.isRetired());
		
		String json = "{\"deleted\": \"false\"}";
		SimpleObject response = deserialize(handle(newPostRequest(getURI() + "/" + getUuid(), json)));
		
		role = service.getRoleByUuid(getUuid());
		Assert.assertFalse(role.isRetired());
		Assert.assertEquals("false", PropertyUtils.getProperty(response, "retired").toString());
		
	}
	
	/**
	 * @see RoleController#search(String, javax.servlet.http.HttpServletRequest,
	 *      HttpServletResponse)
	 * @verifies return no results if there are no matching Roles
	 */
	@Test
	public void findRoles_shouldReturnNoResultsIfThereAreNoMatchingRoles() throws Exception {
		
		MockHttpServletRequest req = request(RequestMethod.GET, getURI());
		req.addParameter("q", "Missing Name");
		SimpleObject result = deserialize(handle(req));
		
		List<Object> hits = (List<Object>) result.get("results");
		Assert.assertEquals(0, hits.size());
		
	}
	
	/**
	 * @see RoleController#search(String, javax.servlet.http.HttpServletRequest,
	 *      HttpServletResponse)
	 * @verifies find matching Roles
	 */
	@Test
	@Ignore("Roles do not support searching yet.")
	public void findRoles_shouldFindMatchingRoles() throws Exception {
		
		MockHttpServletRequest req = request(RequestMethod.GET, getURI());
		req.addParameter("q", "Provider");
		SimpleObject result = deserialize(handle(req));
		
		List<Object> hits = (List<Object>) result.get("results");
		Assert.assertEquals(1, hits.size());
		Assert.assertEquals(service.getRoleByUuid("3480cb6d-c291-46c8-8d3a-96dc33d199fb"),
		    PropertyUtils.getProperty(hits.get(0), "uuid"));
		
	}
	
	/**
	 * @see RoleController#getAll(javax.servlet.http.HttpServletRequest, HttpServletResponse)
	 * @verifies get all Roles
	 */
	@Test
	public void shouldListAllRoles() throws Exception {
		
		MockHttpServletRequest req = request(RequestMethod.GET, getURI());
		SimpleObject result = deserialize(handle(req));
		
		Assert.assertNotNull(result);
		Assert.assertEquals(getAllCount(), Util.getResultsSize(result));
		
	}
	
}

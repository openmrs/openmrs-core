/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.controller.openmrs1_9;

import org.apache.commons.beanutils.PropertyUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.VisitAttributeType;
import org.openmrs.api.VisitService;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.test.Util;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.RestTestConstants1_9;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.bind.annotation.RequestMethod;

import static org.junit.Assert.assertEquals;

import javax.servlet.http.HttpServletResponse;

/**
 * Contains tests for {@link VisitAttributeTypeController} CRUD operations
 */
public class VisitAttributeTypeController1_9Test extends MainResourceControllerTest {
	
	private VisitService service;
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest#getURI()
	 */
	@Override
	public String getURI() {
		return "visitattributetype";
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest#getUuid()
	 */
	@Override
	public String getUuid() {
		return RestTestConstants1_9.VISIT_ATTRIBUTE_TYPE_UUID;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest#getAllCount()
	 */
	@Override
	public long getAllCount() {
		int count = 0;
		for (VisitAttributeType type : service.getAllVisitAttributeTypes()) {
			if (!type.isRetired()) {
				count++;
			}
		}
		return count;
	}
	
	@Before
	public void before() throws Exception {
		service = Context.getVisitService();
		executeDataSet(RestTestConstants1_9.TEST_DATASET);
	}
	
	/**
	 * @see VisitAttributeTypeController#create(SimpleObject, javax.servlet.http.HttpServletRequest,
	 *      HttpServletResponse)
	 */
	@Test
	public void create_shouldCreateANewVisitAttributeType() throws Exception {
		int before = service.getAllVisitAttributeTypes().size();
		String json = "{ \"name\":\"Some attributeType\",\"description\":\"Attribute Type for visit\",\"datatypeClassname\":\"org.openmrs.customdatatype.datatype.FreeTextDatatype\"}";
		
		handle(newPostRequest(getURI(), json));
		
		Assert.assertEquals(before + 1, service.getAllVisitAttributeTypes().size());
	}
	
	/**
	 * @see VisitAttributeTypeController#update(String, SimpleObject,
	 *      javax.servlet.http.HttpServletRequest, HttpServletResponse)
	 */
	@Test
	public void update_shouldChangeAPropertyOnAVisitAttributeType() throws Exception {
		String json = "{\"description\":\"Updated description\"}";
		
		handle(newPostRequest(getURI() + "/" + getUuid(), json));
		
		Assert.assertEquals("Updated description", service.getVisitAttributeType(1).getDescription());
	}
	
	/**
	 * @see VisitAttributeTypeController#delete(String, String,
	 *      javax.servlet.http.HttpServletRequest, HttpServletResponse)
	 */
	@Test
	public void delete_shouldRetireAVisitAttributeType() throws Exception {
		VisitAttributeType visitAttributeType = service.getVisitAttributeType(1);
		Assert.assertFalse(visitAttributeType.isRetired());
		
		handle(newDeleteRequest(getURI() + "/" + getUuid(), new Parameter("reason", "test")));
		
		visitAttributeType = service.getVisitAttributeType(1);
		Assert.assertTrue(visitAttributeType.isRetired());
		Assert.assertEquals("test", visitAttributeType.getRetireReason());
	}
	
	/**
	 * @see VisitAttributeTypeController#getAll(javax.servlet.http.HttpServletRequest,
	 *      HttpServletResponse)
	 */
	@Test
	public void getAll_shouldGellVisitAttributeTypesIfIncludeAllIsSetToTrue() throws Exception {
		SimpleObject result = deserialize(handle(newGetRequest(getURI(), new Parameter(
		        RestConstants.REQUEST_PROPERTY_FOR_INCLUDE_ALL, "true"))));
		
		Assert.assertEquals(5, Util.getResultsSize(result));
	}
	
	/**
	 * @see VisitAttributeTypeController#search(String, javax.servlet.http.HttpServletRequest,
	 *      HttpServletResponse)
	 */
	@Test
	public void search_shouldFindMatchingVisitAttributeTypesExcludingRetiredOnes() throws Exception {
		SimpleObject result = deserialize(handle(newGetRequest(getURI(), new Parameter("q", "date"))));
		
		Assert.assertEquals(2, Util.getResultsSize(result));
	}
	
	/**
	 * @see VisitAttributeTypeController#search(String, javax.servlet.http.HttpServletRequest,
	 *      HttpServletResponse)
	 */
	@Test
	public void search_shouldFindAllMatchingVisitAttributeTypesIfIncludeAllIsSetToTrue() throws Exception {
		SimpleObject result = deserialize(handle(newGetRequest(getURI(), new Parameter("q", "date"), new Parameter(
		        RestConstants.REQUEST_PROPERTY_FOR_INCLUDE_ALL, "true"))));
		
		Assert.assertEquals(3, Util.getResultsSize(result));
	}
	
	/**
	 * @See {@link VisitAttributeTypeController#purge(String, javax.servlet.http.HttpServletRequest, HttpServletResponse)}
	 */
	@Test
	public void purge_shouldPurgeAVisitAttributeType() throws Exception {
		final String visitAttributeTypeUuid = "6770f6d6-7673-11e0-8f03-001e378eb67g";
		Assert.assertNotNull(service.getVisitAttributeTypeByUuid(visitAttributeTypeUuid));
		int originalCount = service.getAllVisitAttributeTypes().size();
		
		handle(newDeleteRequest(getURI() + "/" + visitAttributeTypeUuid, new Parameter("purge", "true")));
		
		Assert.assertNull(service.getVisitAttributeTypeByUuid(visitAttributeTypeUuid));
		Assert.assertEquals(originalCount - 1, service.getAllVisitAttributeTypes().size());
	}
	
	@Test
	public void shouldGetAVisitAttributeTypeByUuid() throws Exception {
		MockHttpServletRequest req = request(RequestMethod.GET, getURI() + "/" + getUuid());
		SimpleObject result = deserialize(handle(req));
		
		VisitAttributeType visitAttributeType = service.getVisitAttributeTypeByUuid(getUuid());
		assertEquals(visitAttributeType.getUuid(), PropertyUtils.getProperty(result, "uuid"));
		assertEquals(visitAttributeType.getName(), PropertyUtils.getProperty(result, "name"));
		assertEquals(visitAttributeType.getDescription(), PropertyUtils.getProperty(result, "description"));
	}
}

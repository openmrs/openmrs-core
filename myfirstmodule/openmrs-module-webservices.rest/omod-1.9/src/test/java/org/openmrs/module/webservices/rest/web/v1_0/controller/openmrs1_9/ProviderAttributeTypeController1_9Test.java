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

import org.junit.Assert;
import org.apache.commons.beanutils.PropertyUtils;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.ProviderAttributeType;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.test.Util;
import org.openmrs.module.webservices.rest.web.RestTestConstants1_9;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.request.WebRequest;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

public class ProviderAttributeTypeController1_9Test extends MainResourceControllerTest {
	
	@Before
	public void before() throws Exception {
		executeDataSet(RestTestConstants1_9.TEST_DATASET);
	}
	
	/**
	 * @see ProviderAttributeTypeController#createProviderAttributeType(SimpleObject,WebRequest)
	 * @verifies create a new ProviderAttributeType
	 */
	@Test
	public void createProviderAttributeType_shouldCreateANewProviderAttributeType() throws Exception {
		int before = Context.getProviderService().getAllProviderAttributeTypes().size();
		String json = "{ \"name\":\"Some attributeType\",\"description\":\"Attribute Type for provider\",\"datatypeClassname\":\"org.openmrs.customdatatype.datatype.FreeTextDatatype\"}";
		
		handle(newPostRequest(getURI(), json));
		
		Assert.assertEquals(before + 1, Context.getProviderService().getAllProviderAttributeTypes().size());
	}
	
	/**
	 * @see ProviderAttributeTypeController#updateProviderAttributeType(ProviderAttributeType,SimpleObject,WebRequest)
	 * @verifies change a property on a provider
	 */
	@Test
	public void updateProviderAttributeType_shouldChangeAPropertyOnAProviderAttributeType() throws Exception {
		String json = "{\"description\":\"Updated description\"}";
		handle(newPostRequest(getURI() + "/" + RestTestConstants1_9.PROVIDER_ATTRIBUTE_TYPE_UUID, json));
		
		Assert.assertEquals("Updated description", Context.getProviderService().getProviderAttributeType(1).getDescription());
	}
	
	/**
	 * @see ProviderAttributeTypeController#retireProviderAttributeType(ProviderAttributeType,String,WebRequest)
	 * @verifies void a provider attribute type
	 */
	@Test
	public void retireProviderAttributeType_shouldRetireAProviderAttributeType() throws Exception {
		ProviderAttributeType providerAttributeType = Context.getProviderService().getProviderAttributeType(1);
		Assert.assertFalse(providerAttributeType.isRetired());
		
		MockHttpServletRequest request = request(RequestMethod.DELETE, getURI() + "/" + getUuid());
		request.addParameter("reason", "test");
		
		handle(request);
		
		providerAttributeType = Context.getProviderService().getProviderAttributeType(1);
		Assert.assertTrue(providerAttributeType.isRetired());
		Assert.assertEquals("test", providerAttributeType.getRetireReason());
	}
	
	/**
	 * @see ProviderAttributeTypeController#findProviderAttributeTypes(String,WebRequest,HttpServletResponse)
	 * @verifies return no results if there are no matching provider(s)
	 */
	@Test
	public void findProviderAttributeTypes_shouldReturnNoResultsIfThereAreNoMatchingProviders() throws Exception {
		MockHttpServletRequest request = newGetRequest(getURI());
		request.addParameter("q", "zzzznotype");
		
		SimpleObject result = deserialize(handle(request));
		Assert.assertEquals(0, Util.getResultsSize(result));
	}
	
	/**
	 * @see ProviderAttributeTypeController#findProviderAttributeTypes(String,WebRequest,HttpServletResponse)
	 * @verifies find matching provider attribute types
	 */
	@Test
	public void findProviderAttributeTypes_shouldFindMatchingProviderAttributeTypes() throws Exception {
		MockHttpServletRequest request = newGetRequest(getURI());
		request.addParameter("q", "Joining");
		
		SimpleObject response = deserialize(handle(request));
		Assert.assertEquals(1, Util.getResultsSize(response));
		
		List<Object> results = Util.getResultsList(response);
		Object result = results.get(0);
		
		Assert.assertEquals(RestTestConstants1_9.PROVIDER_ATTRIBUTE_TYPE_UUID, PropertyUtils.getProperty(result, "uuid"));
		Assert.assertNotNull(PropertyUtils.getProperty(result, "links"));
		Assert.assertNotNull(PropertyUtils.getProperty(result, "display"));
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest#getURI()
	 */
	@Override
	public String getURI() {
		return "providerattributetype";
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest#getUuid()
	 */
	@Override
	public String getUuid() {
		return RestTestConstants1_9.PROVIDER_ATTRIBUTE_TYPE_UUID;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest#getAllCount()
	 */
	@Override
	public long getAllCount() {
		return Context.getProviderService().getAllProviderAttributeTypes().size();
	}
}

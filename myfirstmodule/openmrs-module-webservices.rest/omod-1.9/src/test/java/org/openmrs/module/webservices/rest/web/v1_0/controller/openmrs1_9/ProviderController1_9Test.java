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
import org.openmrs.Provider;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RestTestConstants1_9;
import org.openmrs.module.webservices.rest.web.response.ConversionException;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.request.WebRequest;

import javax.servlet.http.HttpServletResponse;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ProviderController1_9Test extends MainResourceControllerTest {
	
	@Before
	public void before() throws Exception {
		executeDataSet(RestTestConstants1_9.TEST_DATASET);
	}
	
	/**
	 * @see ProviderController#createProvider(SimpleObject,WebRequest)
	 * @verifies create a new Provider
	 */
	@Test
	public void createProvider_shouldCreateANewProvider() throws Exception {
		int before = Context.getProviderService().getAllProviders().size();
		String json = "{ \"person\": \"da7f524f-27ce-4bb2-86d6-6d1d05312bd5\", \"identifier\":\"abc123ez\" }";
		
		handle(newPostRequest(getURI(), json));
		Assert.assertEquals(before + 1, Context.getProviderService().getAllProviders().size());
	}
	
	/**
	 * @verifies create a new Provider with Attributes
	 */
	@Test
	public void createProvider_shouldCreateANewProviderWithAttributes() throws Exception {
		int before = Context.getProviderService().getAllProviders().size();
		String json = "{ \"person\": \"da7f524f-27ce-4bb2-86d6-6d1d05312bd5\"," + "\"identifier\":\"abc123ez\","
		        + "\"attributes\":[{\"attributeType\":\"" + RestTestConstants1_9.PROVIDER_ATTRIBUTE_TYPE_UUID
		        + "\",\"value\":\"2005-01-01\"}]}";
		handle(newPostRequest(getURI(), json));
		Assert.assertEquals(before + 1, Context.getProviderService().getAllProviders().size());
		Provider provider = Context.getProviderService().getAllProviders().get(1);
		Assert.assertEquals(1, provider.getAttributes().size());
	}
	
	/**
	 * @see ProviderController#updateProvider(Provider,SimpleObject,WebRequest)
	 * @verifies should fail when changing a person property on a Provider
	 */
	@Test(expected = ConversionException.class)
	public void updateProvider_shouldFailWhenChangingAPersonPropertyOnAProvider() throws Exception {
		Date now = new Date();
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String json = "{\"birthdate\":\"" + df.format(now) + "\"}";
		
		handle(newPostRequest(getURI() + "/" + RestTestConstants1_9.PROVIDER_UUID, json));
	}
	
	/**
	 * @see ProviderController#voidProvider(Provider,String,WebRequest)
	 * @verifies void a Provider
	 */
	@Test
	public void voidProvider_shouldRetireAProvider() throws Exception {
		Provider pat = Context.getProviderService().getProvider(2);
		Assert.assertFalse(pat.isRetired());
		
		MockHttpServletRequest request = request(RequestMethod.DELETE, getURI() + "/" + RestTestConstants1_9.PROVIDER_UUID);
		request.addParameter("reason", "unit test");
		handle(request);
		
		pat = Context.getProviderService().getProvider(2);
		Assert.assertTrue(pat.isRetired());
		Assert.assertEquals("unit test", pat.getRetireReason());
	}
	
	/**
	 * @see ProviderController#findProviders(String,WebRequest,HttpServletResponse)
	 * @verifies return no results if there are no matching Providers
	 */
	@Test
	public void findProviders_shouldReturnNoResultsIfThereAreNoMatchingProviders() throws Exception {
		MockHttpServletRequest request = newGetRequest(getURI());
		request.addParameter("q", "zzzznobody");
		
		List<?> results = (List<?>) deserialize(handle(request)).get("results");
		Assert.assertEquals(0, results.size());
	}
	
	/**
	 * @see ProviderController#findProviders(String,WebRequest,HttpServletResponse)
	 * @verifies find matching Providers
	 */
	@Test
	public void findProviders_shouldFindMatchingProviders() throws Exception {
		MockHttpServletRequest request = newGetRequest(getURI());
		request.addParameter("q", "Hornblower");
		
		List<?> results = (List<?>) deserialize(handle(request)).get("results");
		Assert.assertEquals(1, results.size());
		Object result = results.get(0);
		Assert.assertEquals(RestTestConstants1_9.PROVIDER_UUID, PropertyUtils.getProperty(result, "uuid"));
		Assert.assertNotNull(PropertyUtils.getProperty(result, "links"));
		Assert.assertNotNull(PropertyUtils.getProperty(result, "display"));
	}
	
	@Test
	public void shouldFindProviderByUserUuid() throws Exception {
		MockHttpServletRequest request = newGetRequest(getURI());
		request.addParameter("user", "c98a1558-e131-11de-babe-001e378eb67e");
		
		List<?> results = (List<?>) deserialize(handle(request)).get("results");
		Assert.assertNotSame(0, results.size());
		
		Object next = results.iterator().next();
		Assert.assertEquals(getUuid(), (String) PropertyUtils.getProperty(next, "uuid"));
	}
	
	@Test
	public void shouldEditAProvider() throws Exception {
		final String EDITED_PERSON_UUID = "da7f524f-27ce-4bb2-86d6-6d1d05312bd5";
		Provider provider = Context.getProviderService().getProviderByUuid(getUuid());
		Assert.assertFalse(EDITED_PERSON_UUID.equals(provider.getPerson().getUuid()));
		
		String json = "{\"person\":\"" + EDITED_PERSON_UUID + "\"" + "}";
		handle(newPostRequest(getURI() + "/" + getUuid(), json));
		
		Provider updatedProvider = Context.getProviderService().getProviderByUuid(getUuid());
		Assert.assertEquals(EDITED_PERSON_UUID, updatedProvider.getPerson().getUuid());
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest#getURI()
	 */
	@Override
	public String getURI() {
		return "provider";
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest#getUuid()
	 */
	@Override
	public String getUuid() {
		return RestTestConstants1_9.PROVIDER_UUID;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest#getAllCount()
	 */
	@Override
	public long getAllCount() {
		return Context.getProviderService().getAllProviders(false).size();
	}
}

/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.controller.openmrs2_0;

import org.apache.commons.beanutils.PropertyUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.BaseOpenmrsObject;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.v1_0.RestTestConstants2_0;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ProviderController2_0Test extends MainResourceControllerTest {
	
	@Before
	public void setup() throws Exception {
		executeDataSet(RestTestConstants2_0.PROVIDER_TEST_DATA_XML);
	}
	
	@Test
	public void doGetAll_shouldProcessIncludeAllParameter() throws Exception {

		{
			// setup
			MockHttpServletRequest request = newGetRequest(getURI());

			// Replay
			List<?> allProvidersDef = deserialize(handle(request)).get("results");

			// Verify: no retired by default
			for (Object provider : allProvidersDef) {
				Assert.assertNotSame(true, PropertyUtils.getProperty(provider, "retired"));
			}
		}

		{
			// Setup
			MockHttpServletRequest request = newGetRequest(getURI());
			request.setParameter("includeAll", "true");

			// Replay
			Object allProviders = (deserialize(handle(request)).get("results"));
			List<Object> allProvidersUuid = ((List<Map>) allProviders).stream().map(
					p -> p.get("uuid")
			).collect(Collectors.toList());

			// Verify: 'includeAll=true' same as Java API
			Assert.assertArrayEquals(
					Context.getProviderService().getAllProviders(true).stream()
							.map(BaseOpenmrsObject::getUuid).toArray(),
					allProvidersUuid.toArray()
			);
		}

		{
			// Setup
			MockHttpServletRequest request = newGetRequest(getURI());
			request.setParameter("includeAll", "false");

			// Replay
			Object allNonRetired = (deserialize(handle(request)).get("results"));
			List<Object> allNonRetiredUuid = ((List<Map>) allNonRetired).stream().map(
					p -> p.get("uuid")
			).collect(Collectors.toList());

			// Verify: 'includeAll=false' same as Java API
			Assert.assertArrayEquals(
					Context.getProviderService().getAllProviders(false).stream()
							.map(BaseOpenmrsObject::getUuid).toArray(),
					allNonRetiredUuid.toArray()
			);
		}

	}
	
	@Test
	public void doSearch_shouldProcessIncludeAll() throws Exception {

		String providerName = "test";

		{
			// Setup
			MockHttpServletRequest request = newGetRequest(getURI());
			request.setParameter("q", providerName);

			// Replay
			List<?> allProvidersDef = deserialize(handle(request)).get("results");
			List<Object> allUuidDef = ((List<Map>) allProvidersDef).stream().map(
					p -> p.get("uuid")
			).collect(Collectors.toList());

			// Verify: no retired by default
			Assert.assertArrayEquals(
					Context.getProviderService().getProviders(providerName, null, null, null, false)
							.stream().map(
							BaseOpenmrsObject::getUuid
					).toArray(),
					allUuidDef.toArray()
			);
		}
		{
			// Setup
			MockHttpServletRequest request = newGetRequest(getURI());
			request.setParameter("q", providerName);
			request.setParameter("includeAll", "false");

			// Replay
			List<?> allNonRetired = deserialize(handle(request)).get("results");
			List<Object> nonRetiredUuids = ((List<Map>) allNonRetired).stream().map(
					p -> p.get("uuid")
			).collect(Collectors.toList());

			// Verify: 'includeAll=false' same as Java API
			Assert.assertArrayEquals(
					Context.getProviderService().getProviders(providerName, null, null, null, false)
							.stream().map(
							BaseOpenmrsObject::getUuid
					).toArray(),
					nonRetiredUuids.toArray()
			);
		}

		{
			// Setup
			MockHttpServletRequest request = newGetRequest(getURI());
			request.setParameter("q", providerName);
			request.setParameter("includeAll", "true");

			// Replay
			List<?> allProviders = deserialize(handle(request)).get("results");
			List<Object> allUuids = ((List<Map>) allProviders).stream().map(
					p -> p.get("uuid")
			).collect(Collectors.toList());

			// Verify: 'includeAll=true' same as Java API
			Assert.assertArrayEquals(
					Context.getProviderService().getProviders(providerName, null, null, null, true)
							.stream().map(
							BaseOpenmrsObject::getUuid
					).toArray(),
					allUuids.toArray()
			);
		}
	}
	
	@Override
	public String getURI() {
		return "provider";
	}
	
	@Override
	public String getUuid() {
		return RestTestConstants2_0.PROVIDER_UUID;
	}
	
	@Override
	public long getAllCount() {
		return Context.getProviderService().getAllProviders(false).size();
	}
}

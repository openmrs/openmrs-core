/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.controller;

import org.apache.commons.beanutils.PropertyUtils;
import org.junit.Assert;
import org.junit.Test;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.test.Util;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Facilitates testing controllers.
 */
public abstract class MainResourceControllerTest extends RestControllerTestUtils {
	
	public static class Parameter {
		
		public String name;
		
		public String value;
		
		public Parameter(String name, String value) {
			this.name = name;
			this.value = value;
		}
	}
	
	@Test
	public void shouldGetDefaultByUuid() throws Exception {
		MockHttpServletResponse response = handle(request(RequestMethod.GET, getURI() + "/" + getUuid()));
		SimpleObject result = deserialize(response);
		
		Assert.assertNotNull(result);
		Assert.assertEquals(getUuid(), PropertyUtils.getProperty(result, "uuid"));
	}
	
	@Test
	public void shouldGetRefByUuid() throws Exception {
		MockHttpServletRequest request = request(RequestMethod.GET, getURI() + "/" + getUuid());
		request.addParameter("v", "ref");
		SimpleObject result = deserialize(handle(request));
		
		Assert.assertNotNull(result);
		Assert.assertEquals(getUuid(), PropertyUtils.getProperty(result, "uuid"));
	}
	
	@Test
	public void shouldGetFullByUuid() throws Exception {
		MockHttpServletRequest request = request(RequestMethod.GET, getURI() + "/" + getUuid());
		request.addParameter("v", "full");
		SimpleObject result = deserialize(handle(request));
		
		Assert.assertNotNull(result);
		Assert.assertEquals(getUuid(), PropertyUtils.getProperty(result, "uuid"));
	}
	
	@Test
	public void shouldGetAll() throws Exception {
		SimpleObject result = deserialize(handle(request(RequestMethod.GET, getURI())));
		
		Assert.assertNotNull(result);
		Assert.assertEquals(getAllCount(), Util.getResultsSize(result));
	}
	
	/**
	 * @return the URI of the resource
	 */
	public abstract String getURI();
	
	/**
	 * @return the uuid of an existing object
	 */
	public abstract String getUuid();
	
	/**
	 * @return the count of all not retired/voided objects
	 */
	public abstract long getAllCount();
	
}

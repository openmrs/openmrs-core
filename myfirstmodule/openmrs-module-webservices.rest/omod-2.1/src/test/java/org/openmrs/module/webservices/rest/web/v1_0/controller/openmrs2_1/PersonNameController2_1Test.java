/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.controller.openmrs2_1;

import org.apache.commons.beanutils.PropertyUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.bind.annotation.RequestMethod;

import static org.openmrs.module.webservices.rest.web.RestTestConstants1_8.PERSON_NAME_UUID;
import static org.openmrs.module.webservices.rest.web.RestTestConstants1_8.PERSON_UUID;

/**
 * Tests functionality of {@link PersonNameController}.
 */
public class PersonNameController2_1Test extends MainResourceControllerTest {
	
	private PersonService service;
	
	@Before
	public void before() {
		this.service = Context.getPersonService();
	}
	
	/**
	 * @see MainResourceControllerTest#getURI()
	 */
	@Override
	public String getURI() {
		return "person/" + PERSON_UUID + "/name";
	}
	
	/**
	 * @see MainResourceControllerTest#getAllCount()
	 */
	@Override
	public long getAllCount() {
		return service.getPersonByUuid(PERSON_UUID).getNames().size();
	}
	
	/**
	 * @see MainResourceControllerTest#getUuid()
	 */
	@Override
	public String getUuid() {
		return PERSON_NAME_UUID;
	}
	
	@Test
	public void shouldAddNameToPerson() throws Exception {
		long originalCount = getAllCount();
		
		SimpleObject personName = new SimpleObject();
		personName.add("givenName", "name1");
		
		String json = new ObjectMapper().writeValueAsString(personName);
		
		MockHttpServletRequest req = request(RequestMethod.POST, getURI());
		req.setContent(json.getBytes());
		
		SimpleObject newPersonName = deserialize(handle(req));
		
		Assert.assertNotNull(PropertyUtils.getProperty(newPersonName, "uuid"));
		Assert.assertEquals(originalCount + 1, getAllCount());
	}
}

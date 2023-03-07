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
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.v1_0.RestTestConstants2_1;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Tests functionality of {@link ConceptSourceController}.
 */
public class ConceptSourceController2_1Test extends MainResourceControllerTest {
	
	private ConceptService service;
	
	@Before
	public void before() {
		this.service = Context.getConceptService();
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest#getURI()
	 */
	@Override
	public String getURI() {
		return "conceptsource";
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest#getAllCount()
	 */
	@Override
	public long getAllCount() {
		return service.getAllConceptSources(false).size();
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest#getUuid()
	 */
	@Override
	public String getUuid() {
		return RestTestConstants2_1.CONCEPT_SOURCE_UUID;
	}
	
	@Test
	public void shouldCreateAConceptSource() throws Exception {
		long originalCount = getAllCount();
		
		SimpleObject conceptSource = new SimpleObject();
		conceptSource.add("name", "test name");
		conceptSource.add("description", "test description");
		conceptSource.add("uniqueId", "some_random_unique_id");
		
		String json = new ObjectMapper().writeValueAsString(conceptSource);
		
		MockHttpServletRequest req = request(RequestMethod.POST, getURI());
		req.setContent(json.getBytes());
		
		SimpleObject newConceptSource = deserialize(handle(req));
		
		Assert.assertNotNull(PropertyUtils.getProperty(newConceptSource, "uuid"));
		Assert.assertEquals(originalCount + 1, getAllCount());
	}
	
}

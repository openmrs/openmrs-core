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

import static org.junit.Assert.assertEquals;

import org.apache.commons.beanutils.PropertyUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.ConceptAttribute;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.v1_0.RestTestConstants2_0;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Tests functionality of {@link ConceptAttributeController}.
 */
public class ConceptAttributeController2_0Test extends MainResourceControllerTest {
	
	private ConceptService service;
	
	/**
	 * @see MainResourceControllerTest#getURI()
	 */
	@Override
	public String getURI() {
		return "concept/" + RestTestConstants2_0.CONCEPT_UUID + "/attribute";
	}
	
	/**
	 * @see MainResourceControllerTest#getUuid()
	 */
	@Override
	public String getUuid() {
		return RestTestConstants2_0.CONCEPT_ATTRIBUTE_UUID;
	}
	
	/**
	 * @see MainResourceControllerTest#getAllCount()
	 */
	@Override
	public long getAllCount() {
		return service.getConceptByUuid(RestTestConstants2_0.CONCEPT_UUID).getActiveAttributes().size();
	}
	
	@Before
	public void before() throws Exception {
		executeDataSet(RestTestConstants2_0.CONCEPT_ATTRIBUTE_DATA_SET);
		this.service = Context.getConceptService();
	}
	
	@Test
	public void shouldAddAttributeToConcept() throws Exception {
		int before = service.getConceptByUuid(RestTestConstants2_0.CONCEPT_UUID).getAttributes().size();
		String json = "{\"attributeType\":\"" + RestTestConstants2_0.CONCEPT_ATTRIBUTE_TYPE_UUID
		        + "\", \"value\":\"2012-05-05\"}";
		handle(newPostRequest(getURI(), json));
		int after = service.getConceptByUuid(RestTestConstants2_0.CONCEPT_UUID).getAttributes().size();
		Assert.assertEquals(before + 1, after);
	}
	
	@Test
	public void shouldEditConceptAttribute() throws Exception {
		String json = "{ \"attributeType\":\"" + RestTestConstants2_0.CONCEPT_ATTRIBUTE_TYPE_UUID
		        + "\", \"value\": \"2015-04-12\" }";
		
		ConceptAttribute conceptAttribute = service.getConceptAttributeByUuid(getUuid());
		Assert.assertEquals("2011-04-25", conceptAttribute.getValueReference());
		
		handle(newPostRequest(getURI() + "/" + getUuid(), json));
		
		conceptAttribute = service.getConceptAttributeByUuid(getUuid());
		Assert.assertEquals("2015-04-12", conceptAttribute.getValueReference());
	}
	
	@Test
	public void shouldVoidAttribute() throws Exception {
		ConceptAttribute conceptAttribute = service.getConceptAttributeByUuid(getUuid());
		Assert.assertFalse(conceptAttribute.isVoided());
		
		MockHttpServletRequest request = request(RequestMethod.DELETE, getURI() + "/" + getUuid());
		request.addParameter("reason", "unit test");
		handle(request);
		
		conceptAttribute = service.getConceptAttributeByUuid(getUuid());
		Assert.assertTrue(conceptAttribute.isVoided());
		Assert.assertEquals("unit test", conceptAttribute.getVoidReason());
	}
	
	@Test
	public void shouldGetAConceptAttributeByUuid() throws Exception {
		MockHttpServletRequest req = request(RequestMethod.GET, getURI() + "/" + getUuid());
		SimpleObject result = deserialize(handle(req));

		ConceptAttribute conceptAttribute = service.getConceptAttributeByUuid(getUuid());
		assertEquals(conceptAttribute.getUuid(), PropertyUtils.getProperty(result, "uuid"));
	}
}

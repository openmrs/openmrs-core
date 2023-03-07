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
import org.openmrs.ConceptAttributeType;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.test.Util;
import org.openmrs.module.webservices.rest.web.v1_0.RestTestConstants2_0;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Tests functionality of {@link ConceptAttributeTypeController}.
 */
public class ConceptAttributeTypeController2_0Test extends MainResourceControllerTest {

    private ConceptService service;

    /**
     * @see MainResourceControllerTest#getURI()
     */
    @Override
    public String getURI() {
        return "conceptattributetype";
    }

    /**
     * @see MainResourceControllerTest#getUuid()
     */
    @Override
    public String getUuid() {
        return RestTestConstants2_0.CONCEPT_ATTRIBUTE_TYPE_UUID;
    }

    /**
     * @see MainResourceControllerTest#getAllCount()
     */
    @Override
    public long getAllCount() {
        return service.getAllConceptAttributeTypes().size();
    }

    @Before
    public void before() throws Exception {
        executeDataSet(RestTestConstants2_0.CONCEPT_ATTRIBUTE_DATA_SET);
        this.service = Context.getConceptService();
    }

    @Test
    public void shouldCreateConceptAttributeType() throws Exception {
        long originalCount = getAllCount();
        String json = "{\"name\": \"Time Span\",\"description\": \"This attribute type will record the time span for the concept\"," +
                "\"datatypeClassname\": \"org.openmrs.customdatatype.datatype.LongFreeTextDatatype\",\"minOccurs\": 0,\"maxOccurs\": 1," +
                "\"datatypeConfig\": \"default\",\"preferredHandlerClassname\": \"org.openmrs.web.attribute.handler.LongFreeTextTextareaHandler\",\"handlerConfig\": null}";

        MockHttpServletRequest req = request(RequestMethod.POST, getURI());
        req.setContent(json.getBytes());

        Object newConceptAttributeType = deserialize(handle(req));
        Assert.assertNotNull(PropertyUtils.getProperty(newConceptAttributeType, "uuid"));
        Assert.assertEquals(originalCount + 1 , getAllCount());

    }
    
    @Test
    public void shouldPurgeConceptAttributeType() throws Exception {
    	final String UUID = "9516cc50-6f9f-11e0-8414-001e378eb67f";
    	Assert.assertNotNull(service.getConceptAttributeTypeByUuid(UUID));
    	MockHttpServletRequest req = request(RequestMethod.DELETE, getURI() + "/" + UUID);
    	req.addParameter("purge", "true");
    	handle(req);
    	Assert.assertNull(service.getConceptAttributeTypeByUuid(UUID));
    }
    
	@Test
	public void shouldListAllConceptAttributeTypes() throws Exception {
		MockHttpServletRequest req = request(RequestMethod.GET, getURI());
		SimpleObject result = deserialize(handle(req));
		
		Assert.assertNotNull(result);
		Assert.assertEquals(getAllCount(), Util.getResultsSize(result));
	}

    @Test
    public void shouldUpdateConceptAttributeType() throws Exception {
        final String CONCEPT_ATTRIBUTE_TYPE_UUID = "9516cc50-6f9f-11e0-8414-001e378eb67e";
        ConceptAttributeType existingConceptAttributeType = service.getConceptAttributeTypeByUuid(CONCEPT_ATTRIBUTE_TYPE_UUID);
        Assert.assertNotNull(existingConceptAttributeType);

        String json = "{\"name\": \"new updated name\", \"description\": \"Dummy description update\", \"datatypeClassname\": \"org.openmrs.customdatatype.datatype.LongFreeTextDatatype\"}";

        handle(newPostRequest(getURI() + "/" + existingConceptAttributeType.getUuid(), json));
        ConceptAttributeType updatedConceptAttributeType = service.getConceptAttributeTypeByUuid(CONCEPT_ATTRIBUTE_TYPE_UUID);

        Assert.assertNotNull(updatedConceptAttributeType);
        Assert.assertEquals("new updated name", updatedConceptAttributeType.getName());
        Assert.assertEquals("Dummy description update", updatedConceptAttributeType.getDescription());
        Assert.assertEquals("org.openmrs.customdatatype.datatype.LongFreeTextDatatype", updatedConceptAttributeType.getDatatypeClassname());
    }
    
    @Test
    public void shouldGetAConceptAttributeTypeByUuid() throws Exception {
    	MockHttpServletRequest req = request(RequestMethod.GET, getURI() + "/" + getUuid());
    	SimpleObject result = deserialize(handle(req));
    	
    	ConceptAttributeType conceptAttributeType = service.getConceptAttributeTypeByUuid(getUuid());
    	Assert.assertEquals(conceptAttributeType.getUuid(), PropertyUtils.getProperty(result, "uuid"));
    	Assert.assertEquals(conceptAttributeType.getName(), PropertyUtils.getProperty(result, "name"));
    }

    @Test
    public void shouldRetireAConceptAttributeType() throws Exception {
        final String UUID = "9516cc50-6f9f-11e0-8414-001e378eb67e";
        ConceptAttributeType conceptAttributeType = service.getConceptAttributeTypeByUuid(UUID);
        Assert.assertNotNull(conceptAttributeType);
        Assert.assertFalse(conceptAttributeType.getRetired());
        Assert.assertNull(conceptAttributeType.getDateRetired());
        Assert.assertNull(conceptAttributeType.getRetiredBy());

        MockHttpServletRequest req = request(RequestMethod.DELETE, getURI() + "/" + UUID);
        req.addParameter("!purge", "");
        req.addParameter("reason", "let it retire for a time");
        handle(req);

        conceptAttributeType = service.getConceptAttributeTypeByUuid(UUID);
        Assert.assertTrue(conceptAttributeType.getRetired());
        Assert.assertNotNull(conceptAttributeType.getDateRetired());
        Assert.assertNotNull(conceptAttributeType.getRetiredBy());
        Assert.assertEquals("let it retire for a time", conceptAttributeType.getRetireReason());
    }
}

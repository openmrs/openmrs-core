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
import org.junit.Before;
import org.junit.Test;
import org.openmrs.VisitAttribute;
import org.openmrs.api.VisitService;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.test.Util;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.RestTestConstants1_9;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest;

import java.text.SimpleDateFormat;
import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_9.VisitAttributeResource1_9;

/**
 * Tests functionality of {@link VisitAttributeController}.
 */
public class VisitAttributeController1_9Test extends MainResourceControllerTest {
	
	private VisitService service;
	
	private static final String DATE_PATTERN = "yyyy-MM-dd";
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest#getURI()
	 */
	@Override
	public String getURI() {
		return "visit/" + RestTestConstants1_9.VISIT_UUID + "/attribute";
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest#getUuid()
	 */
	@Override
	public String getUuid() {
		return RestTestConstants1_9.VISIT_ATTRIBUTE_UUID;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest#getAllCount()
	 */
	@Override
	public long getAllCount() {
		return service.getVisitByUuid(RestTestConstants1_9.VISIT_UUID).getActiveAttributes().size();
	}
	
	@Before
	public void before() throws Exception {
		executeDataSet(RestTestConstants1_9.TEST_DATASET);
		this.service = Context.getVisitService();
	}
	
	@Test
	public void shouldListAllAttributesForAVisit() throws Exception {
		SimpleObject result = deserialize(handle(newGetRequest(getURI(), new Parameter(
		        RestConstants.REQUEST_PROPERTY_FOR_INCLUDE_ALL, "true"))));
		
		Assert.assertNotNull(result);
		Assert.assertEquals(3, Util.getResultsSize(result));
	}
	
	@Test
	public void shouldAddAnAttributeToAVisit() throws Exception {
		int before = service.getVisitByUuid(RestTestConstants1_9.VISIT_UUID).getAttributes().size();
		String json = "{\"attributeType\":\"6770f6d6-7673-11e0-8f03-001e378eb67g\", \"value\":\"2012-08-25\"}";
		
		handle(newPostRequest(getURI(), json));
		
		int after = service.getVisitByUuid(RestTestConstants1_9.VISIT_UUID).getAttributes().size();
		Assert.assertEquals(before + 1, after);
	}
	
	@Test
	public void shouldEditAVisitAttribute() throws Exception {
		final String newValue = "2012-05-05";
		VisitAttribute va = service.getVisitAttributeByUuid(RestTestConstants1_9.VISIT_ATTRIBUTE_UUID);
		Assert.assertFalse(new SimpleDateFormat(DATE_PATTERN).parse(newValue).equals(va.getValue()));
		String json = "{ \"value\":\"2012-05-05\" }";
		
		VisitAttribute visitAttribute = service.getVisitAttributeByUuid(RestTestConstants1_9.VISIT_ATTRIBUTE_UUID);
		Assert.assertEquals("Audit Date", visitAttribute.getAttributeType().getName());
		
		handle(newPostRequest(getURI() + "/" + getUuid(), json));
		
		VisitAttribute updated = service.getVisitAttributeByUuid(RestTestConstants1_9.VISIT_ATTRIBUTE_UUID);
		Assert.assertTrue(new SimpleDateFormat(DATE_PATTERN).parse(newValue).equals(updated.getValue()));
	}
	
	@Test
	public void shouldVoidAVisitAttribute() throws Exception {
		VisitAttribute visitAttribute = service.getVisitAttributeByUuid(RestTestConstants1_9.VISIT_ATTRIBUTE_UUID);
		Assert.assertFalse(visitAttribute.isVoided());
		
		handle(newDeleteRequest(getURI() + "/" + getUuid(), new Parameter("reason", "unit test")));
		
		visitAttribute = service.getVisitAttributeByUuid(RestTestConstants1_9.VISIT_ATTRIBUTE_UUID);
		Assert.assertTrue(visitAttribute.isVoided());
		Assert.assertEquals("unit test", visitAttribute.getVoidReason());
	}
	
	@Test
	public void shouldNotThrowNullPointerExceptionIfMaxOccursIsNull() {
		VisitAttribute visitAttribute = service.getVisitAttributeByUuid("7c2tyr18-6faa-11e0-7899-001e378eb66d");
		Assert.assertFalse(visitAttribute.isVoided());
		Assert.assertNull(visitAttribute.getAttributeType().getMaxOccurs());
		
		VisitAttributeResource1_9 visitAttributeResource1_9 = new VisitAttributeResource1_9();
		visitAttributeResource1_9.save(visitAttribute);
	}
}

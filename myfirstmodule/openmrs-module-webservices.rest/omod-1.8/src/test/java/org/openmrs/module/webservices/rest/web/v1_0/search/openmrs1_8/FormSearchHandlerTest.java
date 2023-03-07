/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.search.openmrs1_8;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.api.FormService;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RestTestConstants1_8;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.bind.annotation.RequestMethod;

public class FormSearchHandlerTest extends MainResourceControllerTest {

	private FormService formService;

	private static final String FORM_DATASET_XML = "formTestDataSet.xml";

	@Before
	public void setUp() throws Exception {
		formService = Context.getFormService();
		executeDataSet(FORM_DATASET_XML);
	}

	@Override
	public String getURI() {
		return "form";
	}

	@Override
	public String getUuid() {
		return RestTestConstants1_8.FORM_UUID;
	}

	@Override
	public long getAllCount() {
		return formService.getAllForms(false).size();
	}

	/**
	 * @verifies return location by tag uuid
	 * @see FormSearchHandler1_8#getSearchConfig()
	 */
	@Test
	public void getSearchConfig_shouldReturnPublishedForms() throws Exception {
		MockHttpServletRequest req = request(RequestMethod.GET, getURI());
		req.addParameter("published", "true");
		SimpleObject result = deserialize(handle(req));
		List<Object> hits = result.get("results");
		Assert.assertEquals(3, hits.size());
	}
	@Test
	public void getSearchConfig_shouldReturnUnpublishedForms() throws Exception{
		MockHttpServletRequest req = request(RequestMethod.GET, getURI());
		req.addParameter("published", "false");
		SimpleObject result = deserialize(handle(req));
		List<Object> hits = result.get("results");
		Assert.assertEquals(2, hits.size());
	}
}

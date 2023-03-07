/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.controller.openmrs1_11;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.test.Util;
import org.openmrs.module.webservices.rest.web.RestTestConstants1_8;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ObsController1_11Test extends MainResourceControllerTest {
	
	@Test
	public void shouldCreateAnObsWithFormFieldNamespaceAndFormFieldPath() throws Exception {
		long originalCount = getAllCount();
		SimpleObject obs = new SimpleObject();
		obs.add("person", "da7f524f-27ce-4bb2-86d6-6d1d05312bd5");
		obs.add("concept", "c607c80f-1ea9-4da3-bb88-6276ce8868dd");
		obs.add("obsDatetime", "2018-11-13T00:00:00.000-0500");
		obs.add("value", 180.0);
		obs.add("formFieldNamespace", "ohri-forms");
		obs.add("formFieldPath", "some-path");
		
		String json = new ObjectMapper().writeValueAsString(obs);
		SimpleObject ret = deserialize(handle(newPostRequest(getURI(), json)));
		assertEquals("ohri-forms", Util.getByPath(ret, "formFieldNamespace"));
		assertEquals("some-path", Util.getByPath(ret, "formFieldPath"));
		assertEquals(++originalCount, getAllCount());
	}

	@Override
	public String getURI() {
		return "obs";
	}

	@Override
	public String getUuid() {
		return RestTestConstants1_8.OBS_UUID;
	}

	@Override
	public long getAllCount() {
		return Context.getObsService().getObservationCount(null, true);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest#shouldGetAll()
	 */
	@Override
	@Test(expected = ResourceDoesNotSupportOperationException.class)
	public void shouldGetAll() throws Exception {
		super.shouldGetAll();
	}
}

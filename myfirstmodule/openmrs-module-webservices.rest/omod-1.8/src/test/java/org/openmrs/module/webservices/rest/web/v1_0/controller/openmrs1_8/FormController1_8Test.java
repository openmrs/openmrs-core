/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.controller.openmrs1_8;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import org.junit.Before;
import org.junit.Test;

import org.openmrs.Form;
import org.openmrs.api.context.Context;
import org.openmrs.api.FormService;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RestTestConstants1_8;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest;
import org.apache.commons.beanutils.PropertyUtils;

/**
 * Tests functionality of {@link FormController}.
 */
public class FormController1_8Test extends MainResourceControllerTest {
	
	private FormService service;
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest#getURI()
	 */
	@Override
	public String getURI() {
		return "form";
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest#getUuid()
	 */
	@Override
	public String getUuid() {
		return RestTestConstants1_8.FORM_UUID;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest#getAllCount()
	 */
	@Override
	public long getAllCount() {
		return 1;
	}
	
	@Before
	public void before() {
		this.service = Context.getFormService();
	}
	
	@Test
	public void shouldUnRetireAForm() throws Exception {
		Form form = service.getFormByUuid(getUuid());
		form.setRetired(true);
		form.setRetireReason("random reason");
		service.saveForm(form);
		form = service.getFormByUuid(getUuid());
		assertTrue(form.isRetired());
		
		String json = "{\"deleted\": \"false\"}";
		SimpleObject response = deserialize(handle(newPostRequest(getURI() + "/" + getUuid(), json)));
		
		form = service.getFormByUuid(getUuid());
		assertFalse(form.isRetired());
		assertEquals("false", PropertyUtils.getProperty(response, "retired").toString());
		
	}
	
}

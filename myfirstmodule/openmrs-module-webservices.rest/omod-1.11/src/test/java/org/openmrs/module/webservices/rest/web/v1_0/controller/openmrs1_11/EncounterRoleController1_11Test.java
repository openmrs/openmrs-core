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

import org.apache.commons.beanutils.PropertyUtils;
import org.junit.Assert;
import org.junit.Test;
import org.openmrs.EncounterRole;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.test.Util;
import org.openmrs.module.webservices.rest.web.RestTestConstants1_11;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.bind.annotation.RequestMethod;

public class EncounterRoleController1_11Test extends MainResourceControllerTest {
	
	@Override
	public String getURI() {
		return "encounterrole";
	}
	
	@Override
	public String getUuid() {
		return RestTestConstants1_11.ENCOUNTER_ROLE_UUID;
	}
	
	@Override
	public long getAllCount() {
		return 1;
	}
	
	@Test
	public void shouldGetAnEncounterRoleByName() throws Exception {
		final String ROLE_NAME = "Unknown";
		
		MockHttpServletRequest req = request(RequestMethod.GET, getURI());
		req.setParameter("q", ROLE_NAME);
		req.setParameter("v", "default");
		SimpleObject result = deserialize(handle(req));
		Object encounterRoleObject = Util.getResultsList(result).get(0);
		
		EncounterRole encounterRole = Context.getEncounterService().getEncounterRoleByName(ROLE_NAME);
		Assert.assertEquals(encounterRole.getUuid(), PropertyUtils.getProperty(encounterRoleObject, "uuid"));
		Assert.assertEquals(encounterRole.getName(), PropertyUtils.getProperty(encounterRoleObject, "name"));
		Assert.assertEquals(encounterRole.getDescription(), PropertyUtils.getProperty(encounterRoleObject, "description"));
	}
}

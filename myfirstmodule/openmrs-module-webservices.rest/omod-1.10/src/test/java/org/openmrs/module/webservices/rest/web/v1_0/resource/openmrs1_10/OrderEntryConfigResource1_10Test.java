/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_10;

import org.apache.struts.mock.MockHttpServletRequest;
import org.apache.struts.mock.MockHttpServletResponse;
import org.junit.Test;
import org.mockito.Mock;
import org.openmrs.Concept;
import org.openmrs.api.OrderService;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceController;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

public class OrderEntryConfigResource1_10Test extends BaseModuleWebContextSensitiveTest {
	
	@Autowired
	private MainResourceController mainResourceController;
	
	@Mock
	private OrderService orderService;
	
	@Test
	public void testGetAll() throws Exception {
		Concept route = new Concept();
		when(orderService.getDrugRoutes()).thenReturn(Arrays.asList(route));
		Concept dosingUnit = new Concept();
		when(orderService.getDrugDosingUnits()).thenReturn(Arrays.asList(dosingUnit));
		
		MockHttpServletResponse response = new MockHttpServletResponse();
		SimpleObject config = mainResourceController.get("orderentryconfig", new MockHttpServletRequest(), response);
		
		List<SimpleObject> temp = (List<SimpleObject>) config.get("drugRoutes");
		assertThat(temp.size(), is(1));
		assertThat((String) temp.get(0).get("uuid"), is(route.getUuid()));
		
		temp = (List<SimpleObject>) config.get("drugDosingUnits");
		assertThat(temp.size(), is(1));
		assertThat((String) temp.get(0).get("uuid"), is(dosingUnit.getUuid()));
	}
	
}

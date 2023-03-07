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
import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.Extension;
import org.openmrs.module.web.extension.AdministrationSectionExt;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.test.Util;
import org.openmrs.module.webservices.rest.web.MockModuleFactoryWrapper;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.api.RestService;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest;
import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs2_0.AdministrationLinksResource2_0;
import org.openmrs.module.webservices.rest.web.v1_0.wrapper.AdministrationSectionLinks;
import org.powermock.reflect.Whitebox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.bind.annotation.RequestMethod;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Tests Read operations for {@link AdministrationSectionExt} via web service calls
 */
public class AdministrationLinksController2_0Test extends MainResourceControllerTest {

	@Autowired
	RestService restService;

	MockModuleFactoryWrapper mockModuleFactory = new MockModuleFactoryWrapper();

	@Before
	public void setUp() throws Exception {
		setupMockRestWsModuleAdminListExtension();
		setupMockAtlasModuleAdminListExtension();

		AdministrationLinksResource2_0 administrationLinksResource = (AdministrationLinksResource2_0) restService
				.getResourceBySupportedClass(AdministrationSectionLinks.class);

		Whitebox.setInternalState(administrationLinksResource, "moduleFactoryWrapper", mockModuleFactory);
	}

	@Override
	public String getURI() {
		return "administrationlinks";
	}

	@Override
	public String getUuid() {
		return RestConstants.MODULE_ID;
	}

	@Override
	public long getAllCount() {
		return 2;
	}

	@Test
	public void shouldReturnListOfLinksForInstalledModules() throws Exception {
		MockHttpServletRequest req = request(RequestMethod.GET, getURI());
		SimpleObject result = deserialize(handle(req));

		assertEquals(2, Util.getResultsSize(result));

		List<Object> results = Util.getResultsList(result);
		assertCorrectWsModuleLinks(results.get(0));
		assertCorrectAtlasModuleLinks(results.get(1));
	}

	@Test
	public void shouldReturnListOfLinksForOneModuleByUuid() throws Exception {
		MockHttpServletRequest req = request(RequestMethod.GET, getURI() + "/" + getUuid());
		SimpleObject result = deserialize(handle(req));

		assertCorrectWsModuleLinks(result);
	}

	private void assertCorrectWsModuleLinks(Object result)
			throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
		assertEquals(RestConstants.MODULE_ID, PropertyUtils.getProperty(result, "uuid"));
		assertEquals("WS-links", PropertyUtils.getProperty(result, "title"));

		Object links = PropertyUtils.getProperty(result, "administrationLinks");
		assertNotNull(links);
	}

	private void assertCorrectAtlasModuleLinks(Object result)
			throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
		assertEquals("atlas", PropertyUtils.getProperty(result, "uuid"));
		assertEquals("Atlas-links", PropertyUtils.getProperty(result, "title"));

		Object links = PropertyUtils.getProperty(result, "administrationLinks");
		assertNotNull(links);
	}

	private void setupMockRestWsModuleAdminListExtension() {
		Extension extension = new AdministrationSectionExt() {

			@Override
			public String getTitle() {
				return "WS-links";
			}

			@Override
			public Map<String, String> getLinks() {
				Map<String, String> linksMap = new HashMap<>();
				linksMap.put("link1", "ws.first.link");
				linksMap.put("link2", "ws.second.link");
				return linksMap;
			}

			@Override
			public String getPointId() {
				return "org.openmrs.admin.list";
			}
		};
		extension.setModuleId(RestConstants.MODULE_ID);
		mockModuleFactory.loadedExtensions.add(extension);
	}

	private void setupMockAtlasModuleAdminListExtension() {
		Extension extension = new AdministrationSectionExt() {

			@Override
			public String getTitle() {
				return "Atlas-links";
			}

			@Override
			public Map<String, String> getLinks() {
				Map<String, String> linksMap = new HashMap<>();
				linksMap.put("link3", "atlas.first.link");
				return linksMap;
			}

			@Override
			public String getPointId() {
				return "org.openmrs.admin.list";
			}
		};
		extension.setModuleId("atlas");
		mockModuleFactory.loadedExtensions.add(extension);
	}
}

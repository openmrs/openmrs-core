/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.obs;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.openmrs.GlobalProperty;
import org.openmrs.Obs;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.StorageService;
import org.openmrs.api.storage.ObjectMetadata;
import org.openmrs.obs.handler.TextHandler;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.openmrs.util.OpenmrsConstants;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TextHandlerTest extends BaseContextSensitiveTest {

	@Autowired
	AdministrationService adminService;

	@Autowired
	TextHandler handler;

	@Autowired
	StorageService storageService;

	@Test
	public void shouldReturnSupportedViews() {

		String[] actualViews = handler.getSupportedViews();
		String[] expectedViews = { ComplexObsHandler.TEXT_VIEW, ComplexObsHandler.RAW_VIEW, ComplexObsHandler.URI_VIEW };

		assertArrayEquals(actualViews, expectedViews);
	}

	@Test
	public void shouldSupportRawView() {

		assertTrue(handler.supportsView(ComplexObsHandler.RAW_VIEW));
		assertTrue(handler.supportsView(ComplexObsHandler.TEXT_VIEW));
		assertTrue(handler.supportsView(ComplexObsHandler.URI_VIEW));
	}

	@Test
	public void shouldNotSupportOtherViews() {

		assertFalse(handler.supportsView(ComplexObsHandler.HTML_VIEW));
		assertFalse(handler.supportsView(ComplexObsHandler.PREVIEW_VIEW));
		assertFalse(handler.supportsView(ComplexObsHandler.TITLE_VIEW));
		assertFalse(handler.supportsView(""));
		assertFalse(handler.supportsView(null));
	}

	@Test
	public void saveObs_shouldRetrieveCorrectMimetypeAndTitle() throws Exception {
		String filename = "TestingComplexObsSaving.txt";
		ComplexData complexData = new ComplexData(filename, "Teststring");

		// Construct 2 Obs to also cover the case where the filename exists already
		Obs obs1 = new Obs();
		obs1.setComplexData(complexData);

		Obs obs2 = new Obs();
		obs2.setComplexData(complexData);

		adminService.saveGlobalProperty(new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_COMPLEX_OBS_DIR, "obs"));

		handler.saveObs(obs1);
		handler.saveObs(obs2);

		Obs complexObs1 = handler.getObs(obs1, "RAW_VIEW");
		Obs complexObs2 = handler.getObs(obs2, "RAW_VIEW");
		assertEquals(complexObs1.getComplexData().getMimeType(), "text/plain");
		assertEquals(complexObs1.getComplexData().getTitle(), filename);
		assertEquals(complexObs2.getComplexData().getMimeType(), "text/plain");
		assertEquals(complexObs2.getComplexData().getTitle(), filename);
	}

	@Test
	public void getObs_shouldRetrieveMimetypeAndLengthForUriView() throws Exception {
		String filename = "TestingComplexObsSaving.txt";

		adminService.saveGlobalProperty(new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_COMPLEX_OBS_DIR, "obs"));

		String key = storageService.saveData(new ByteArrayInputStream("Teststring".getBytes(StandardCharsets.UTF_8)),
		    ObjectMetadata.builder().setFilename(filename).build(), "obs");

		Obs obs = new Obs();
		obs.setValueComplex(filename + " file |" + key);

		Obs uriObs = handler.getObs(obs, ComplexObsHandler.URI_VIEW);
		assertEquals(filename + " file ", uriObs.getComplexData().getTitle());
		assertEquals("text/plain", uriObs.getComplexData().getMimeType());
		assertEquals(10L, uriObs.getComplexData().getLength());
	}
}

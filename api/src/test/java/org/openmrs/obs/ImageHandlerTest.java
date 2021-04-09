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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.openmrs.GlobalProperty;
import org.openmrs.Obs;
import org.openmrs.api.AdministrationService;
import org.openmrs.obs.handler.ImageHandler;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.openmrs.util.OpenmrsConstants;
import org.springframework.beans.factory.annotation.Autowired;

public class ImageHandlerTest extends BaseContextSensitiveTest {
	
	@Autowired
	private AdministrationService adminService;
	@TempDir
	public Path complexObsTestFolder;
	ImageHandler handler;
	@BeforeEach
	public void setUp() {
		handler = new ImageHandler();
	}
	
	@Test
	public void shouldReturnSupportedViews() {
		String[] actualViews = handler.getSupportedViews();
		String[] expectedViews = { ComplexObsHandler.RAW_VIEW };
		
		assertArrayEquals(actualViews, expectedViews);
	}
	
	@Test
	public void shouldSupportRawView() {
		
		assertTrue(handler.supportsView(ComplexObsHandler.RAW_VIEW));
	}
	
	@Test
	public void shouldNotSupportOtherViews() {
		
		assertFalse(handler.supportsView(ComplexObsHandler.HTML_VIEW));
		assertFalse(handler.supportsView(ComplexObsHandler.PREVIEW_VIEW));
		assertFalse(handler.supportsView(ComplexObsHandler.TEXT_VIEW));
		assertFalse(handler.supportsView(ComplexObsHandler.TITLE_VIEW));
		assertFalse(handler.supportsView(ComplexObsHandler.URI_VIEW));
		assertFalse(handler.supportsView(""));
		assertFalse(handler.supportsView(null));
	}
	
	@Test
	public void saveObs_shouldRetrieveCorrectMimetype() throws IOException {
		String mimetype = "image/png";
		String filename = "TestingComplexObsSaving.png";
		File sourceFile = Paths.get("src", "test", "resources", "ComplexObsTestImage.png").toFile();
		
		BufferedImage img = ImageIO.read(sourceFile);
		
		ComplexData complexData = new ComplexData(filename, img);
		
		// Construct 2 Obs to also cover the case where the filename exists already
		Obs obs1 = new Obs();
		obs1.setComplexData(complexData);
		
		Obs obs2 = new Obs();
		obs2.setComplexData(complexData);
		
		adminService.saveGlobalProperty(new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_COMPLEX_OBS_DIR,
		        complexObsTestFolder.toAbsolutePath().toString()));
		
		handler.saveObs(obs1);
		handler.saveObs(obs2);
		
		// Get observation
		Obs complexObs1 = handler.getObs(obs1, "RAW_VIEW");
		Obs complexObs2 = handler.getObs(obs2, "RAW_VIEW");
		
		assertEquals(complexObs1.getComplexData().getMimeType(), mimetype);
		assertEquals(complexObs2.getComplexData().getMimeType(), mimetype);
	}
	
	@Test
	public void saveObs_shouldHandleByteArrays() throws IOException {
		Path sourceFile = Paths.get("src", "test", "resources", "ComplexObsTestImage.png");
		
		byte[] bytes = Files.readAllBytes(sourceFile);
		ComplexData complexData = new ComplexData("TestingComplexObsSaving.png", bytes);
		Obs obs = new Obs();
		obs.setComplexData(complexData);
		adminService.saveGlobalProperty(new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_COMPLEX_OBS_DIR,
		        complexObsTestFolder.toAbsolutePath().toString()));
		handler.saveObs(obs);
	}
}

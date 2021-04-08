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
import static org.junit.jupiter.api.condition.OS.WINDOWS;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.io.TempDir;
import org.openmrs.GlobalProperty;
import org.openmrs.Obs;
import org.openmrs.api.AdministrationService;
import org.openmrs.obs.handler.MediaHandler;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.openmrs.util.OpenmrsConstants;
import org.springframework.beans.factory.annotation.Autowired;

public class MediaHandlerTest extends BaseContextSensitiveTest {
	
	@Autowired
	private AdministrationService adminService;
	
	@TempDir
	public Path complexObsTestFolder;
	
	MediaHandler handler;
	
	@BeforeEach
	public void setUp() {
		handler = new MediaHandler();
	}
	
	@Test
    public void shouldReturnSupportedViews() {
		String[] actualViews = handler.getSupportedViews();

		assertArrayEquals(actualViews, new String[]{ ComplexObsHandler.RAW_VIEW });
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
	@DisabledOnOs(WINDOWS)
	public void saveObs_shouldRetrieveCorrectMimetype() throws IOException {
		
		adminService.saveGlobalProperty(new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_COMPLEX_OBS_DIR,
		        complexObsTestFolder.toAbsolutePath().toString()));
		
		File sourceFile = Paths.get("src", "test", "resources", "ComplexObsTestAudio.mp3").toFile();
		
		Obs complexObs1 = null;
		Obs complexObs2 = null;
		try (FileInputStream in1 = new FileInputStream(sourceFile);
				 FileInputStream in2 = new FileInputStream(sourceFile)
			) {
			ComplexData complexData1 = new ComplexData("TestingComplexObsSaving.mp3", in1);
			ComplexData complexData2 = new ComplexData("TestingComplexObsSaving.mp3", in2);
			
			// Construct 2 Obs to also cover the case where the filename exists already
			Obs obs1 = new Obs();
			obs1.setComplexData(complexData1);
			Obs obs2 = new Obs();
			obs2.setComplexData(complexData2);
			
			handler.saveObs(obs1);
			handler.saveObs(obs2);
			
			complexObs1 = handler.getObs(obs1, "RAW_VIEW");
			complexObs2 = handler.getObs(obs2, "RAW_VIEW");
			
			assertEquals("audio/mpeg", complexObs1.getComplexData().getMimeType());
			assertEquals("audio/mpeg", complexObs2.getComplexData().getMimeType());
		} finally {
			((InputStream) complexObs1.getComplexData().getData()).close();
			((InputStream) complexObs1.getComplexData().getData()).close();
		}
	}
}

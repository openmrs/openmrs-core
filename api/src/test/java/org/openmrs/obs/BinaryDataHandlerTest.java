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
import static org.openmrs.test.jupiter.BaseContextSensitiveTest.log;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
<<<<<<< HEAD
import java.util.logging.Logger;
import org.apache.commons.logging.Log;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.apache.commons.io.FileUtils;
=======
<<<<<<< HEAD

=======
import org.slf4j.LoggerFactory;


import org.apache.commons.io.FileUtils;
>>>>>>> e09e68135 (TRUNK-7521: Complex obs handlers to return obs as is when underlying file is null)
>>>>>>> 58a93d278 (TRUNK-7521: Complex obs handlers to return obs as is when underlying file is null)
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.openmrs.GlobalProperty;
import org.openmrs.Obs;
import org.openmrs.api.AdministrationService;
import org.openmrs.obs.handler.BinaryDataHandler;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.openmrs.util.OpenmrsConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;


public class BinaryDataHandlerTest extends BaseContextSensitiveTest {
	
	@Autowired
	private AdministrationService adminService;

	@TempDir
	public Path complexObsTestFolder;

	BinaryDataHandler handler;

<<<<<<< HEAD
	private Obs obs;

=======
<<<<<<< HEAD
=======
	private Obs obs;
	
>>>>>>> e09e68135 (TRUNK-7521: Complex obs handlers to return obs as is when underlying file is null)
>>>>>>> 58a93d278 (TRUNK-7521: Complex obs handlers to return obs as is when underlying file is null)
	@BeforeEach
    public void setUp() {
    handler = new BinaryDataHandler();
    obs = new Obs();
    obs.setId(1); // set the id and other necessary properties
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
		String mimetype = "application/octet-stream";
		String filename = "TestingComplexObsSaving";
		byte[] content = "Teststring".getBytes();
		
		ComplexData complexData = new ComplexData(filename, content);
		
		// Construct 2 Obs to also cover the case where the filename exists already
		Obs obs1 = new Obs();
		obs1.setComplexData(complexData);
		
		Obs obs2 = new Obs();
		obs2.setComplexData(complexData);

		adminService.saveGlobalProperty(new GlobalProperty(
			OpenmrsConstants.GLOBAL_PROPERTY_COMPLEX_OBS_DIR,
			complexObsTestFolder.toAbsolutePath().toString()
		));

		// Execute save
		handler.saveObs(obs1);
		handler.saveObs(obs2);
		
		// Get observation
		Obs complexObs1 = handler.getObs(obs1, "RAW_VIEW");
		Obs complexObs2 = handler.getObs(obs2, "RAW_VIEW");
		
		assertEquals(complexObs1.getComplexData().getMimeType(), mimetype);
		assertEquals(complexObs2.getComplexData().getMimeType(), mimetype);
	}

	@Test
    public void getObs_shouldReturnOriginalObsIfFileIsNull() {
    Obs result = handler.getObs(obs, ComplexObsHandler.RAW_VIEW);
    assertEquals(obs, result);
}
<<<<<<< HEAD

@Test
public ResponseEntity<byte[]> getObs_shouldReturnOriginalObsIfFileIsValid() throws Exception {
=======
<<<<<<< HEAD
=======

@Test
public ResponseEntity<byte[]> getObs_shouldReturnOriginalObsIfFileIsValid() throws Exception {
	org.slf4j.Logger log = LoggerFactory.getLogger(getClass());


>>>>>>> 58a93d278 (TRUNK-7521: Complex obs handlers to return obs as is when underlying file is null)
    try {
        Obs resultObs = handler.getObs(obs, ComplexObsHandler.RAW_VIEW);

        if (resultObs == null) {
            throw new RuntimeException("Failed to retrieve complex obs");
        }

        ComplexData complexData = resultObs.getComplexData();

        if (complexData == null) {
            throw new RuntimeException("Complex data is null");
        }

        File[] files = (complexData).getFiles();

        if (files == null || files.length == 0) {
            throw new RuntimeException("Complex data files are null or empty");
        }

        // Get the file content as a byte array
        byte[] fileContent = FileUtils.readFileToByteArray(files[0]);

        // Set the response headers
        HttpHeaders headers = new HttpHeaders();
<<<<<<< HEAD
        headers.setContentType(MediaType.parseMediaType(complexData.getMimeType()));
=======
        headers.setContentType(org.springframework.http.MediaType.parseMediaType(complexData.getMimeType()));
>>>>>>> 58a93d278 (TRUNK-7521: Complex obs handlers to return obs as is when underlying file is null)
        headers.setContentLength(fileContent.length);
        headers.setContentDisposition(ContentDisposition.builder("attachment").filename(complexData.getTitle()).build());

        // Return the response
        ResponseEntity<byte[]> response = new ResponseEntity<>(fileContent, headers, HttpStatus.OK);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    } catch (Exception e) {
<<<<<<< HEAD
=======
		
>>>>>>> 58a93d278 (TRUNK-7521: Complex obs handlers to return obs as is when underlying file is null)
        log.error("Error getting complex obs", e);
        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
	return null;
}

public ResponseEntity<byte[]> getObs(Obs obs) throws Exception {
    return getObs_shouldReturnOriginalObsIfFileIsValid();
}

		public AdministrationService getAdminService() {
			return adminService;
		}

		public void setAdminService(AdministrationService adminService) {
			this.adminService = adminService;
		}

		public Path getComplexObsTestFolder() {
			return complexObsTestFolder;
		}

		public void setComplexObsTestFolder(Path complexObsTestFolder) {
			this.complexObsTestFolder = complexObsTestFolder;
		}

		public BinaryDataHandler getHandler() {
			return handler;
		}

		public void setHandler(BinaryDataHandler handler) {
			this.handler = handler;
		}

		public Obs getObs() {
			return obs;
		}

		public void setObs(Obs obs) {
			this.obs = obs;
		}
}


<<<<<<< HEAD
        
=======
        
>>>>>>> e09e68135 (TRUNK-7521: Complex obs handlers to return obs as is when underlying file is null)
>>>>>>> 58a93d278 (TRUNK-7521: Complex obs handlers to return obs as is when underlying file is null)

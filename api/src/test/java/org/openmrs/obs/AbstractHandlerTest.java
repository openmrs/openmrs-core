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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import org.apache.commons.io.FilenameUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.openmrs.GlobalProperty;
import org.openmrs.Obs;
import org.openmrs.api.APIException;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.StorageService;
import org.openmrs.obs.handler.AbstractHandler;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.openmrs.util.OpenmrsConstants;
import org.springframework.beans.factory.annotation.Autowired;

public class AbstractHandlerTest extends BaseContextSensitiveTest {
	
	private final String FILENAME = "mytxtfile.txt";
	
	private AbstractHandler handler;
	
	@Autowired
	private AdministrationService adminService;
	
	@Autowired
	private StorageService storageService;
	
	@BeforeEach
	public void initializeContext() throws APIException {
		handler = new  AbstractHandler(adminService, storageService);
		
		adminService.saveGlobalProperty(new GlobalProperty(
			OpenmrsConstants.GLOBAL_PROPERTY_COMPLEX_OBS_DIR, "obs"
		));
	}
	
	@Test
	public void saveObs_shouldNeverOverwritePreviousFiles() {
		String content1 = "A";
		String content2 = "B";
		
		for (int i = 0; i <= 101; i++) {
			String currentData = (i % 2 == 0) ? content1 : content2;
			
			ComplexData complexData = new ComplexData(FILENAME, currentData.getBytes(StandardCharsets.UTF_8));
			
			Obs obs = new Obs();
			obs.setComplexData(complexData);
			
			handler.saveObs(obs);

			Obs fetchedObs = handler.getObs(obs, null);
			
			assertEquals(currentData, new String((byte[]) fetchedObs.getComplexData().getData()));
		}
	}
	
	@Test
	public void saveObs_shouldPreserveTitleWithoutExtension() {
		ComplexData complexDataWithTitle = new ComplexData(FILENAME, "A".getBytes(StandardCharsets.UTF_8));
		
		Obs obsWithTitle = new Obs();
		obsWithTitle.setComplexData(complexDataWithTitle);
		
		handler.saveObs(obsWithTitle);
		
		String[] nameWithTitle = obsWithTitle.getValueComplex().split("_|\\.");
		
		String titlePart = nameWithTitle[0];
		
		assertEquals(titlePart, FilenameUtils.removeExtension(FILENAME));
	}
	
	@Test
	public void saveObs_shouldCorrectlySaveFileWithoutTitle() {
		ComplexData complexDataWithNullTitle = new ComplexData(null, "test".getBytes(StandardCharsets.UTF_8));
		
		Obs obsWithNullTitle = new Obs();
		obsWithNullTitle.setComplexData(complexDataWithNullTitle);
		
		handler.saveObs(obsWithNullTitle);
		
		String[] nameWithNullTitle = obsWithNullTitle.getValueComplex().split("\\|");
		
		String filename = nameWithNullTitle[0];
		String key = nameWithNullTitle[1];
		
		assertEquals(filename, key);
	}
	
}

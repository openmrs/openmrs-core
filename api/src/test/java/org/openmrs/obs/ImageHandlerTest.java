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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.openmrs.Obs;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.obs.handler.AbstractHandler;
import org.openmrs.obs.handler.ImageHandler;
import org.openmrs.util.OpenmrsUtil;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ AbstractHandler.class, OpenmrsUtil.class, Context.class })

public class ImageHandlerTest {
	
	private final String FILENAME = "TestingComplexObsSaving.png";
	
	private File sourceFile;
	
	private String filepath;
	
	@Mock
	private AdministrationService administrationService;

    @Test
    public void shouldReturnSupportedViews() {
        ImageHandler handler = new ImageHandler();
        String[] actualViews = handler.getSupportedViews();
        String[] expectedViews = { ComplexObsHandler.RAW_VIEW };

        assertArrayEquals(actualViews, expectedViews);
    }

    @Test
    public void shouldSupportRawView() {
        ImageHandler handler = new ImageHandler();

        assertTrue(handler.supportsView(ComplexObsHandler.RAW_VIEW));
    }

    @Test
    public void shouldNotSupportOtherViews() {
        ImageHandler handler = new ImageHandler();

        assertFalse(handler.supportsView(ComplexObsHandler.HTML_VIEW));
        assertFalse(handler.supportsView(ComplexObsHandler.PREVIEW_VIEW));
        assertFalse(handler.supportsView(ComplexObsHandler.TEXT_VIEW));
        assertFalse(handler.supportsView(ComplexObsHandler.TITLE_VIEW));
        assertFalse(handler.supportsView(ComplexObsHandler.URI_VIEW));
        assertFalse(handler.supportsView(""));
        assertFalse(handler.supportsView((String) null));
    }
	
	/** This method sets up the test data's parameters for the mime type tests  **/
	@Before
	public void initVariablesForMimetypeTests() {
		filepath = new File("target" + File.separator + "test-classes").getAbsolutePath();
		sourceFile = new File(
		        "src" + File.separator + "test" + File.separator + "resources" + File.separator + "ComplexObsTestImage.png");
	}
	
	@Test
	public void shouldRetrieveCorrectMimetype() throws IOException {
		final String mimetype = "image/png";
		
		BufferedImage img = ImageIO.read(sourceFile);
		
		ComplexData complexData = new ComplexData(FILENAME, img);
		
		// Construct 2 Obs to also cover the case where the filename exists already
		Obs obs1 = new Obs();
		obs1.setComplexData(complexData);
		
		Obs obs2 = new Obs();
		obs2.setComplexData(complexData);
		
		// Mocked methods
		mockStatic(Context.class);
		when(Context.getAdministrationService()).thenReturn(administrationService);
		when(administrationService.getGlobalProperty(any())).thenReturn(filepath);
		
		ImageHandler handler = new ImageHandler();
		
		// Execute save
		handler.saveObs(obs1);
		handler.saveObs(obs2);
		
		// Get observation
		Obs complexObs = handler.getObs(obs1, "RAW_VIEW");
		Obs complexObs2 = handler.getObs(obs2, "RAW_VIEW");
		
		assertTrue(complexObs.getComplexData().getMimeType().equals(mimetype));
		assertTrue(complexObs2.getComplexData().getMimeType().equals(mimetype));
		
		// Delete created files to avoid cluttering
		File obsFile1 = ImageHandler.getComplexDataFile(obs1);
		File obsFile2 = ImageHandler.getComplexDataFile(obs2);
		obsFile1.delete();
		obsFile2.delete();
	}
	
}

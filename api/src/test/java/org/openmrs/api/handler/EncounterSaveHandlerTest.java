/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.handler;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;

import junit.framework.Assert;

import org.junit.Test;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.api.context.Context;
import org.openmrs.obs.ComplexData;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsUtil;

/**
 *
 */
public class EncounterSaveHandlerTest extends BaseContextSensitiveTest {
	
	protected static final String COMPLEX_OBS_XML = "org/openmrs/api/include/ObsServiceTest-complex.xml";
	
	@Test
	@Verifies(value = "should save complex data associated with Obs", method = "handle(ConceptName,User,Date,String)")
	public void handle_shouldSaveComplexDataAssociatedWithObs() throws Exception {
		executeDataSet(COMPLEX_OBS_XML);
		BufferedImage image = createImage();
		Obs o = new Obs();
		o.setConcept(Context.getConceptService().getConcept(8473));
		o.setComplexData(new ComplexData("complex_obs_image_test.gif", image));
		Encounter e = new Encounter();
		e.addObs(o);
		File createdFile = null;
		EncounterSaveHandler handler = new EncounterSaveHandler();
		try {
			handler.handle(e, null, null, null);
			File complexObsDir = OpenmrsUtil.getDirectoryInApplicationDataDirectory(Context.getAdministrationService()
			        .getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_COMPLEX_OBS_DIR));
			createdFile = new File(complexObsDir, o.getValueComplex().split("\\|")[1]);
			Assert.assertTrue(createdFile.exists());
		}
		catch (Exception ex) {
			Assert.fail();
		}
		finally {
			if (createdFile != null && createdFile.exists())
				createdFile.delete();
		}
	}
	
	@Test
	@Verifies(value = "shouldNotFailIfConceptComplexOrHandlerIsNotFound", method = "handle(ConceptName,User,Date,String)")
	public void handle_shouldNotFailIfConceptComplexOrHandlerIsNotFound() throws Exception {
		executeDataSet(COMPLEX_OBS_XML);
		BufferedImage image = createImage();
		
		Obs o = new Obs();
		o.setConcept(null);
		Obs o1 = new Obs();
		o1.setConcept(Context.getConceptService().getConcept(8473));
		o1.setComplexData(new ComplexData("complex_obs_image_test.gif", image));
		Encounter e = new Encounter();
		e.addObs(o);
		e.addObs(o1);
		File createdFile = null;
		EncounterSaveHandler handler = new EncounterSaveHandler();
		try {
			handler.handle(e, null, null, null);
			File complexObsDir = OpenmrsUtil.getDirectoryInApplicationDataDirectory(Context.getAdministrationService()
			        .getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_COMPLEX_OBS_DIR));
			createdFile = new File(complexObsDir, o1.getValueComplex().split("\\|")[1]);
			Assert.assertTrue(createdFile.exists());
		}
		catch (Exception ex) {
			Assert.fail();
		}
		finally {
			if (createdFile != null && createdFile.exists())
				createdFile.delete();
		}
		
	}
	
	public BufferedImage createImage() {
		int width = 10;
		int height = 10;
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		WritableRaster raster = image.getRaster();
		int[] colorArray = new int[3];
		int h = 255;
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				if (i == 0 || j == 0 || i == width - 1 || j == height - 1 || (i > width / 3 && i < 2 * width / 3)
				        && (j > height / 3 && j < 2 * height / 3)) {
					colorArray[0] = h;
					colorArray[1] = h;
					colorArray[2] = 0;
				} else {
					colorArray[0] = 0;
					colorArray[1] = 0;
					colorArray[2] = h;
				}
				raster.setPixel(i, j, colorArray);
			}
		}
		
		return image;
	}
}

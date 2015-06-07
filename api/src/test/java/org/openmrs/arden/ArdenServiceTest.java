/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.arden;

import java.io.File;

import org.junit.Ignore;
import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;

/**
 * TODO Finish this arden test class? delete this test class?
 */
public class ArdenServiceTest extends BaseContextSensitiveTest {
	
	int MAX_MLM = 1000;
	
	/**
	 * @see {@link ArdenService#compileFile(String,String)}
	 */
	@Test
	@Ignore
	@Verifies(value = "should get and parse mlms", method = "compileFile(String,String)")
	public void compileFile_shouldGetAndParseMlms() throws Exception {
		
		String mlmDirectory = System.getProperty("user.dir") + "\\mlm_to_test\\";
		String javaDirectory = System.getProperty("user.dir") + "\\mlm_to_test\\";
		
		File mlmDir = new File(mlmDirectory);
		
		String[] mlmFiles = mlmDir.list();
		
		for (String mlmFile : mlmFiles) {
			if (mlmFile.endsWith(".mlm")) {
				//System.out.println("Parsing: " + mlmFile);
				Context.getArdenService().compileFile(mlmDirectory + mlmFile, javaDirectory);
			}
		}
	}
	
}

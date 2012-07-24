/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
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

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
package org.openmrs.api.arden;

import org.junit.Test;
import java.io.File;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;

/**
 * TODO Finish this arden test class?  delete this test class?
 */
public class ArdenServiceTest extends BaseContextSensitiveTest {
	int MAX_MLM = 1000;
	
	@Test
	public void shouldClass() throws Exception {

		String mlmDirectory = "c:\\mlm\\"; //"C:\\WORKSPACE\\chica\\ruleLibrary\\"; //"C:\\Documents and Settings\\tmdugan\\workspace\\chica\\ruleLibrary\\test\\";
		String javaDirectory = "c:\\mlm\\"; //"C:\\WORKSPACE\\chica\\ruleLibrary\\New\\";  //"C:\\Documents and Settings\\tmdugan\\workspace\\chica\\ruleLibrary\\test\\";
		
		File mlmDir = new File(mlmDirectory);
		
		String[] mlmFiles = mlmDir.list();
		
		for (String mlmFile:mlmFiles) {
			if(mlmFile.endsWith(".mlm")){
			System.out.println("Parsing: "+mlmFile);
			Context.getArdenService().compileFile(mlmDirectory+mlmFile, javaDirectory);
		}}
	}
	
}
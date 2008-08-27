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
package org.openmrs.test.api.arden;

import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.test.testutil.BaseContextSensitiveTest;

/**
 * TODO Finish this arden test class?  delete this test class?
 */
public class ArdenServiceTest extends BaseContextSensitiveTest {
	int MAX_MLM = 1000;
	
	@Test
	public void shouldClass() throws Exception {

	//	Integer pid = 1;
	//	Patient patient;
	//	ArdenDataSource dataSource;
		String [] mlmNames = {
			//	"directexphiriskcountry.mlm",
			//	"directtbcontact.mlm",
			//	"indirectexphiriskcountry.mlm",
			//	"indirecttbcontact.mlm",
			//	"HiRiskLeadScreen.mlm",
				"leadspecpws2.mlm"
		};
		String defaultPath = "asd fasdf asdf test/arden test/";
		
		for (int i = 0; i<mlmNames.length; i++) {
			Context.getArdenService().compileFile(defaultPath + mlmNames[i], "output");
		}
			
	//	Context.getArdenService().compileFile("test/arden test/directexphiriskcountry.mlm");
	

 /*		patient = Context.getPatientService().getPatient(pid);
		dataSource = new DefaultArdenDataSource(); 

 		
		ArdenRule [] mlms = {
				 new HiRiskLeadScreen(patient ,dataSource)
//				,new directtbcontact(patient, dataSource)
					
		};
		for (int i = 0; i < mlms.length; i++){
			
			ArdenRule mlm = mlms[i].getInstance();
			if(mlm != null) {
				if(mlm.evaluate()) {
					System.out.println(mlm.doAction());
					mlm.printDebug();
				}
				
			}
		}
*/	
	}
	
}
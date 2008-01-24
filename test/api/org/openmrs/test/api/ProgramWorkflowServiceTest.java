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
package org.openmrs.test.api;

import java.util.Date;

import org.openmrs.PatientProgram;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.ProgramWorkflowService;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;

/**
 * This class tests methods in the PatientService class
 * 
 * TODO Add methods to test all methods in PatientService class
 */
public class ProgramWorkflowServiceTest extends BaseContextSensitiveTest {
	
	protected static final String CREATE_PATIENT_PROGRAMS_XML = "org/openmrs/test/api/include/ProgramWorkflowServiceTest-createPatientProgram.xml";
	
	protected ProgramWorkflowService pws = null; 
	protected AdministrationService adminService = null;
	protected EncounterService encounterService = null;
	
	@Override
	protected void onSetUpInTransaction() throws Exception {
		initializeInMemoryDatabase();
		authenticate();
		
		if (pws == null) {
			pws = Context.getProgramWorkflowService();
			adminService = Context.getAdministrationService();
			encounterService = Context.getEncounterService();
		}
	}

	/**
	 * Tests fetching a PatientProgram, updating and saving it, and 
	 * subsequently fetching the updated value.
	 * 
	 * @throws Exception
	 */
	public void testGetPatientProgram() throws Exception {
		
		Date today = new Date();
		executeDataSet(CREATE_PATIENT_PROGRAMS_XML);
		
		PatientProgram patientProgram = pws.getPatientProgram(1);
		assertEquals(patientProgram.getProgram().getProgramId(), Integer.valueOf(1));
		
		patientProgram.setDateCompleted(today);
		patientProgram.setChangedBy(Context.getAuthenticatedUser());
		patientProgram.setDateChanged(today);
		pws.updatePatientProgram(patientProgram);
		
		PatientProgram ptProg = pws.getPatientProgram(1);
		assertEquals(ptProg.getDateCompleted(), today);
	}
	
	
	
//	/**
//	 * This method should be uncommented when you want to examine the actual hibernate
//	 * sql calls being made.  The calls that should be limiting the number of returned
//	 * patients should show a "top" or "limit" in the sql -- this proves hibernate's
//	 * use of a native sql limit as opposed to a java-only limit.  
//	 * 
//	 * Note: if enabled, this test will be considerably slower
//     * 
//     * @see org.openmrs.BaseContextSensitiveTest#getRuntimeProperties()
//     */
//    @Override
//    public Properties getRuntimeProperties() {
//	    Properties props = super.getRuntimeProperties();
//	    props.setProperty("hibernate.show_sql", "true");
//	    
//    	return props;
//    }

	
	
}

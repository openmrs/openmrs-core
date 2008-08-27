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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.PatientProgram;
import org.openmrs.User;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.ProgramWorkflowService;
import org.openmrs.api.context.Context;
import org.openmrs.test.testutil.BaseContextSensitiveTest;

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
	
	@Before
	public void runBeforeEachTest() throws Exception {
		executeDataSet(CREATE_PATIENT_PROGRAMS_XML);
		
		if (pws == null) {
			pws = Context.getProgramWorkflowService();
			adminService = Context.getAdministrationService();
			encounterService = Context.getEncounterService();
		}
	}

	/**
	 * @see org.openmrs.test.testutil.BaseContextSensitiveTest#useInMemoryDatabase()
	 *
	@Override
	public Boolean useInMemoryDatabase( ) {
		return false;
	}
	 */
		
	/**
	 * Tests fetching a PatientProgram, updating and saving it, and 
	 * subsequently fetching the updated value.
	 * 
	 * To use in MySQL database:
	 * Uncomment method useInMemoryDatabase() and comment out 
	 * call to initializeInMemoryDatabase() and executeDataSet()
	 * within onSetupTransaction() .
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldUpdatePatientProgram( ) throws Exception {
		
		Date today = new Date();
		
		PatientProgram patientProgram = pws.getPatientProgram(1);
		Date dateCompleted = patientProgram.getDateCompleted();
		Date dateChanged = patientProgram.getDateChanged();
		User changedBy = patientProgram.getChangedBy();
		if (null != dateCompleted) {
			System.out.println("Date Completed: " + dateCompleted);
		}
		if (null != dateChanged) {
			System.out.println("Date Changed: " + dateChanged);
		}
		if (null != changedBy) {
			System.out.println("Changed By: " + changedBy.toString());
		}
		
		patientProgram.setDateCompleted(today);
		patientProgram.setChangedBy(Context.getAuthenticatedUser());
		patientProgram.setDateChanged(today);
		pws.savePatientProgram(patientProgram);
		
		// Uncomment to commit to database
		// setComplete( );
		
		PatientProgram ptProg = pws.getPatientProgram(1);
		Date dateCompleted2 = patientProgram.getDateCompleted();
		Date dateChanged2 = patientProgram.getDateChanged();
		User changedBy2 = patientProgram.getChangedBy();

		if (null != dateCompleted2) {
			System.out.println("Date Completed: " + dateCompleted2);
		}
		if (null != dateChanged2) {
			System.out.println("Date Changed: " + dateChanged2);
		}
		if (null != changedBy2) {
			System.out.println("Changed By: " + changedBy2.toString());
		}
		
		assertNotNull(ptProg.getDateCompleted());
		assertEquals(today, ptProg.getDateCompleted());
		
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

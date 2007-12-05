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
package org.openmrs.reporting.export;

import junit.framework.TestCase;

/**
 * Tests the {@link DataExportReportObject} class
 * 
 * TODO clean up, finish, add methods to this test class
 */
public class DataExportTest extends TestCase {
	
	/**
	 * TODO finish and comment method
	 * 
	 * @throws Exception
	 */
	public void testCalcuateAge() throws Exception { 
		
		DataExportReportObject export = new DataExportReportObject();
		export.setName("TEST_EXPORT");

		SimpleColumn patientId = new SimpleColumn();
		patientId.setColumnName("PATIENT_ID");
		patientId.setReturnValue("$!{fn.patientId}");
		export.getColumns().add(patientId);
		
		SimpleColumn gender = new SimpleColumn();
		gender.setColumnName("GENDER");
		gender.setReturnValue("$!{fn.getPatientAttr('Person', 'gender')}");
		export.getColumns().add(gender);		
		
		SimpleColumn birthdate = new SimpleColumn();
		birthdate.setColumnName("BIRTHDATE");
		birthdate.setReturnValue("$!{fn.formatDate('short', $fn.getPatientAttr('Person', 'birthdate'))}");
		export.getColumns().add(birthdate);		

		SimpleColumn age = new SimpleColumn();
		age.setColumnName("AGE");
		age.setReturnValue("$!{fn.calculateAge($fn.getPatientAttr('Person', 'birthdate'))}");
		export.getColumns().add(age);		

		// TODO how to test this properly without needing visual confirmation?
		
	}

}
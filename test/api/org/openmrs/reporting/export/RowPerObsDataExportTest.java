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

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.Test;
import org.openmrs.Cohort;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.util.OpenmrsUtil;

/**
 * Tests the {@link RowPerObsDataExportReportObject} class
 * 
 * TODO clean up, finish, add methods to this test class
 */
public class RowPerObsDataExportTest extends BaseContextSensitiveTest {
	
	/**
	 * Make sure that more than one row is printed out for each patient 
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldExportMoreThanRowPerPatient() throws Exception {
		executeDataSet("org/openmrs/reporting/export/include/DataExportTest-patients.xml");
		executeDataSet("org/openmrs/reporting/export/include/DataExportTest-obs.xml");
		
		RowPerObsDataExportReportObject export = new RowPerObsDataExportReportObject();
		export.setName("Given names export");
		
		export.addSimpleColumn("PATIENT_ID", "$!{fn.patientId}");
		
		export.addSimpleColumn("Name", "$!{fn.getPatientAttr('PersonName', 'givenName')}");
		
		export.setRowPerObsColumn("Weight (KG)", "5089", new String[] {"location"});
		
		Cohort patients = new Cohort();
		patients.addMember(2);
		patients.addMember(7);
		
		DataExportUtil.generateExport(export, patients, "\t", null);
		File exportFile = DataExportUtil.getGeneratedFile(export);
		
		//System.out.println("Template String: \n" + export.generateTemplate());
		String expectedOutput = "PATIENT_ID	Name	Weight (KG)	Weight (KG)_location\n2	John	10.0	Test Location\n2	John	9.0	Test Location\n2	John	8.0	Test Location\n2	John	7.0	Test Location\n2	John	6.0	Test Location\n2	John	5.0	Test Location\n2	John	4.0	Test Location\n2	John	3.0	Test Location\n2	John	2.0	Test Location\n2	John	1.0	Test Location\n7	Collet	61.0	Xanadu\n";
		String output = OpenmrsUtil.getFileAsString(exportFile);
		exportFile.delete();
		
		//System.out.println("exportFile: \n" + output);
		//TestUtil.printAssignableToSingleString(output);
		assertEquals("The output is not right.", expectedOutput, output);
		
	}
	
	/**
	 * If a patient doens't have data, the concept column data should not be printed
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldExportBlankCellsForPatientsWithoutMatchingData() throws Exception {
		executeDataSet("org/openmrs/reporting/export/include/DataExportTest-patients.xml");
		executeDataSet("org/openmrs/reporting/export/include/DataExportTest-obs.xml");
		
		RowPerObsDataExportReportObject export = new RowPerObsDataExportReportObject();
		export.setName("Given names export");
		
		export.addSimpleColumn("PATIENT_ID", "$!{fn.patientId}");
		
		export.addSimpleColumn("Name", "$!{fn.getPatientAttr('PersonName', 'givenName')}");
		
		export.setRowPerObsColumn("Weight (KG)", "5089", new String[] {"location"});
		
		Cohort patients = new Cohort();
		patients.addMember(2);
		patients.addMember(6);
		patients.addMember(7);
		patients.addMember(8);
		
		DataExportUtil.generateExport(export, patients, "\t", null);
		File exportFile = DataExportUtil.getGeneratedFile(export);
		
		//System.out.println("Template String: \n" + export.generateTemplate());
		String expectedOutput = "PATIENT_ID	Name	Weight (KG)	Weight (KG)_location\n2	John	10.0	Test Location\n2	John	9.0	Test Location\n2	John	8.0	Test Location\n2	John	7.0	Test Location\n2	John	6.0	Test Location\n2	John	5.0	Test Location\n2	John	4.0	Test Location\n2	John	3.0	Test Location\n2	John	2.0	Test Location\n2	John	1.0	Test Location\n6	Johnny	\n7	Collet	61.0	Xanadu\n8	Anet	\n";
		String output = OpenmrsUtil.getFileAsString(exportFile);
		exportFile.delete();
		
		//System.out.println("exportFile: \n" + output);
		//TestUtil.printAssignableToSingleString(output);
		assertEquals("The output is not right.", expectedOutput, output);
		
	}
}
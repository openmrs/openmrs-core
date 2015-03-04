/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.reporting.export;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.Test;
import org.openmrs.Cohort;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.util.OpenmrsUtil;

/**
 * Tests the {@link RowPerObsDataExportReportObject} class TODO clean up, finish, add methods to
 * this test class
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
		
		export.setRowPerObsColumn("Weight (KG)", "5089", new String[] { "location" });
		
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
		
		export.setRowPerObsColumn("Weight (KG)", "5089", new String[] { "location" });
		
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

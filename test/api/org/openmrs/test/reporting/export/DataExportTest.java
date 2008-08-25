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
package org.openmrs.test.reporting.export;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.junit.Test;
import org.openmrs.Cohort;
import org.openmrs.reporting.export.ConceptColumn;
import org.openmrs.reporting.export.DataExportReportObject;
import org.openmrs.reporting.export.DataExportUtil;
import org.openmrs.reporting.export.SimpleColumn;
import org.openmrs.test.testutil.BaseContextSensitiveTest;
import org.openmrs.util.OpenmrsUtil;

/**
 * Tests the {@link DataExportReportObject} class
 * 
 * TODO clean up, finish, add methods to this test class
 */
public class DataExportTest extends BaseContextSensitiveTest {
	
	/**
	 * TODO finish and comment method
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldCalcuateAge() throws Exception { 
		
		initializeInMemoryDatabase();
		executeDataSet("org/openmrs/test/reporting/export/include/DataExportTest-patients.xml");
		authenticate();
		
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
		
		Cohort patients = new Cohort();
		patients.addMember(2);
		
		DataExportUtil.generateExport(export, patients, "\t", null);
		File exportFile = DataExportUtil.getGeneratedFile(export);		
		
		String expectedOutput = "PATIENT_ID	GENDER	BIRTHDATE	AGE\n2	M	01/01/2000	XXX\n";
		// adjust expected output for every year
		Calendar cal = new GregorianCalendar();
		int expectedAge = cal.get(Calendar.YEAR);
		expectedOutput = expectedOutput.replace("XXX", String.valueOf(expectedAge - 2000));
		
		String output = OpenmrsUtil.getFileAsString(exportFile);
		exportFile.delete();
		
		//System.out.println("exportFile: " + output);
		assertEquals("The output is not right.", expectedOutput, output);
	}
	
	/**
	 * Tests the getFirstNObsWithValues method in the DataExportFunctions class
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldFirstNObs() throws Exception {
		initializeInMemoryDatabase();
		executeDataSet("org/openmrs/test/reporting/export/include/DataExportTest-patients.xml");
		executeDataSet("org/openmrs/test/reporting/export/include/DataExportTest-obs.xml");
		authenticate();
		
		DataExportReportObject export = new DataExportReportObject();
		export.setName("FIRST 2 WEIGHTS");
		
		SimpleColumn patientId = new SimpleColumn();
		patientId.setColumnName("PATIENT_ID");
		patientId.setReturnValue("$!{fn.patientId}");
		export.getColumns().add(patientId);
		
		ConceptColumn firstNObs = new ConceptColumn();
		firstNObs.setColumnName("WEIGHT");
		firstNObs.setColumnType("concept");
		firstNObs.setConceptId(5089);
		firstNObs.setConceptName("Weight (KG)");
		firstNObs.setExtras(new String[] {"location"});
		firstNObs.setModifier(DataExportReportObject.MODIFIER_FIRST_NUM);
		firstNObs.setModifierNum(2);
		export.getColumns().add(firstNObs);
		
		Cohort patients = new Cohort();
		patients.addMember(2);
		
		DataExportUtil.generateExport(export, patients, "\t", null);
		File exportFile = DataExportUtil.getGeneratedFile(export);
		
		//System.out.println("Template String: " + export.generateTemplate());
		String expectedOutput = "PATIENT_ID	WEIGHT	WEIGHT_location 	WEIGHT_(1)	WEIGHT_location_(1)\n2	1.0	Test Location	2.0	Test Location\n";
		String output = OpenmrsUtil.getFileAsString(exportFile);
		exportFile.delete();
		
		//System.out.println("exportFile: \n" + output);
		assertEquals("The output is not right.", expectedOutput, output);
		
		
		
		
		// test 1 as the number of obs to fetch
		export = new DataExportReportObject();
		export.setName("FIRST 1 WEIGHTS");
		
		export.addSimpleColumn("PATIENT_ID", "$!{fn.patientId}");
		
		firstNObs = new ConceptColumn();
		firstNObs.setColumnName("WEIGHT");
		firstNObs.setColumnType("concept");
		firstNObs.setConceptId(5089);
		firstNObs.setConceptName("Weight (KG)");
		firstNObs.setExtras(new String[] {"location"});
		firstNObs.setModifier(DataExportReportObject.MODIFIER_FIRST_NUM);
		firstNObs.setModifierNum(1);
		export.getColumns().add(firstNObs);
		
		DataExportUtil.generateExport(export, patients, "\t", null);
		exportFile = DataExportUtil.getGeneratedFile(export);
		
		//System.out.println("Template String: " + export.generateTemplate());
		expectedOutput = "PATIENT_ID	WEIGHT	WEIGHT_location\n2	1.0	Test Location\n";
		output = OpenmrsUtil.getFileAsString(exportFile);
		exportFile.delete();
		
		//System.out.println("exportFile: " + output);
		assertEquals("The output is not what was expected", expectedOutput, output);
		
		
		
		
		// test -1 as the number of obs to fetch
		export = new DataExportReportObject();
		export.setName("FIRST -1 WEIGHTS");
		
		export.addSimpleColumn("PATIENT_ID", "$!{fn.patientId}");
		
		firstNObs = new ConceptColumn();
		firstNObs.setColumnName("WEIGHT");
		firstNObs.setColumnType("concept");
		firstNObs.setConceptId(5089);
		firstNObs.setConceptName("Weight (KG)");
		firstNObs.setExtras(new String[] {"location"});
		firstNObs.setModifier(DataExportReportObject.MODIFIER_FIRST_NUM);
		firstNObs.setModifierNum(-1);
		export.getColumns().add(firstNObs);
		
		DataExportUtil.generateExport(export, patients, "\t", null);
		exportFile = DataExportUtil.getGeneratedFile(export);
		
		//System.out.println("Template String: \n" + export.generateTemplate());
		expectedOutput = "PATIENT_ID	WEIGHT	WEIGHT_location\n2	10.0	Test Location	9.0	Test Location	8.0	Test Location	7.0	Test Location	6.0	Test Location	5.0	Test Location	4.0	Test Location	3.0	Test Location	2.0	Test Location	1.0	Test Location\n";
		output = OpenmrsUtil.getFileAsString(exportFile);
		exportFile.delete();
		
		//System.out.println("exportFile: " + output);
		assertEquals("The output is not what was expected", expectedOutput, output);
		
	}
	
	/**
	 * test first N function when there are no obs for it.  Make sure that it returns
	 * blank cells instead of null cells 
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldFirstNObsWithZeroObsReturned() throws Exception {
		initializeInMemoryDatabase();
		executeDataSet("org/openmrs/test/reporting/export/include/DataExportTest-patients.xml");
		executeDataSet("org/openmrs/test/reporting/export/include/DataExportTest-obs.xml");
		authenticate();
		
		DataExportReportObject export = new DataExportReportObject();
		export.setName("NO CONCEPT, THEN GET WEIGHTS");
		
		export.addSimpleColumn("PATIENT_ID", "$!{fn.patientId}");
		
		// no obs should be returned for concept "5090"
		ConceptColumn firstNObs = new ConceptColumn();
		firstNObs.setColumnName("Other");
		firstNObs.setColumnType("concept");
		firstNObs.setConceptId(5090);
		firstNObs.setConceptName("OTHER CONCEPT");
		firstNObs.setExtras(new String[] {"obsDatetime"});
		firstNObs.setModifier(DataExportReportObject.MODIFIER_FIRST_NUM);
		firstNObs.setModifierNum(2);
		export.getColumns().add(firstNObs);
		
		ConceptColumn lastNObs = new ConceptColumn();
		lastNObs.setColumnName("W-last");
		lastNObs.setColumnType("concept");
		lastNObs.setConceptId(5089);
		lastNObs.setConceptName("Weight (KG)");
		lastNObs.setExtras(new String[] {"obsDatetime"});
		lastNObs.setModifier(DataExportReportObject.MODIFIER_LAST_NUM);
		lastNObs.setModifierNum(2);
		export.getColumns().add(lastNObs);
		
		Cohort patients = new Cohort();
		patients.addMember(2);
		
		DataExportUtil.generateExport(export, patients, "\t", null);
		File exportFile = DataExportUtil.getGeneratedFile(export);
		
		//System.out.println("Template String: \n" + export.generateTemplate());
		String expectedOutput = "PATIENT_ID	Other	Other_obsDatetime 	Other_(1)	Other_obsDatetime_(1)	W-last	W-last_obsDatetime 	W-last_(1)	W-last_obsDatetime_(1)\n2					10.0	02/18/2006	9.0	02/17/2006\n";
		String output = OpenmrsUtil.getFileAsString(exportFile);
		exportFile.delete();
		
		//System.out.println("exportFile: \n" + output);
		assertEquals("The output is not right.", expectedOutput, output);
		
	}
	
	/**
	 * Tests the getFirstObs and getFirstObsWithValues methods in the DataExportFunctions class
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldFirstObs() throws Exception {
		initializeInMemoryDatabase();
		executeDataSet("org/openmrs/test/reporting/export/include/DataExportTest-patients.xml");
		executeDataSet("org/openmrs/test/reporting/export/include/DataExportTest-obs.xml");
		authenticate();
		
		DataExportReportObject export = new DataExportReportObject();
		export.setName("FIRST WEIGHT");
		
		SimpleColumn patientId = new SimpleColumn();
		patientId.setColumnName("PATIENT_ID");
		patientId.setReturnValue("$!{fn.patientId}");
		export.getColumns().add(patientId);
		
		ConceptColumn firstObs = new ConceptColumn();
		firstObs.setColumnName("WEIGHT");
		firstObs.setColumnType("concept");
		firstObs.setConceptId(5089);
		firstObs.setConceptName("Weight (KG)");
		firstObs.setModifier(DataExportReportObject.MODIFIER_FIRST);
		export.getColumns().add(firstObs);
		
		Cohort patients = new Cohort();
		patients.addMember(2);
		
		//System.out.println("Template String: \n" + export.generateTemplate());
		
		DataExportUtil.generateExport(export, patients, "\t", null);
		File exportFile = DataExportUtil.getGeneratedFile(export);
		
		String expectedOutput = "PATIENT_ID\tWEIGHT\n2\t1.0\n";
		String output = OpenmrsUtil.getFileAsString(exportFile);
		exportFile.delete();
		
		//System.out.println("exportFile: \n" + output);
		assertEquals("The output is not right.", expectedOutput, output);
		
		// first obs with location
		export = new DataExportReportObject();
		export.setName("FIRST WEIGHT WITH LOCATION");
		export.getColumns().add(patientId);
		
		firstObs = new ConceptColumn();
		firstObs.setColumnName("WEIGHT");
		firstObs.setColumnType("concept");
		firstObs.setConceptId(5089);
		firstObs.setConceptName("Weight (KG)");
		firstObs.setExtras(new String[] {"location"});
		firstObs.setModifier(DataExportReportObject.MODIFIER_FIRST);
		export.getColumns().add(firstObs);
		
		//System.out.println("Template String: \n" + export.generateTemplate());
		
		DataExportUtil.generateExport(export, patients, "\t", null);
		exportFile = DataExportUtil.getGeneratedFile(export);
		
		expectedOutput = "PATIENT_ID\tWEIGHT\tWEIGHT_location\n2\t1.0\tTest Location\n";
		output = OpenmrsUtil.getFileAsString(exportFile);
		exportFile.delete();

		//System.out.println("exportFile: \n" + output);
		assertEquals("The output is not right.", expectedOutput, output);
	}
	
	/**
	 * Tests the getLastNObsWithValues method in the DataExportFunctions class
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldLastNObs() throws Exception {
		initializeInMemoryDatabase();
		executeDataSet("org/openmrs/test/reporting/export/include/DataExportTest-patients.xml");
		executeDataSet("org/openmrs/test/reporting/export/include/DataExportTest-obs.xml");
		authenticate();
		
		DataExportReportObject export = new DataExportReportObject();
		export.setName("Last 2 Weights");
		
		SimpleColumn patientId = new SimpleColumn();
		patientId.setColumnName("PATIENT_ID");
		patientId.setReturnValue("$!{fn.patientId}");
		export.getColumns().add(patientId);
		
		ConceptColumn lastNObs = new ConceptColumn();
		lastNObs.setColumnName("WEIGHT");
		lastNObs.setColumnType("concept");
		lastNObs.setConceptId(5089);
		lastNObs.setConceptName("Weight (KG)");
		lastNObs.setExtras(new String[] {"location"});
		lastNObs.setModifier(DataExportReportObject.MODIFIER_LAST_NUM);
		lastNObs.setModifierNum(2);
		export.getColumns().add(lastNObs);
		
		Cohort patients = new Cohort();
		patients.addMember(2);
		
		DataExportUtil.generateExport(export, patients, "\t", null);
		File exportFile = DataExportUtil.getGeneratedFile(export);		
		
		String expectedOutput = "PATIENT_ID	WEIGHT	WEIGHT_location 	WEIGHT_(1)	WEIGHT_location_(1)\n2	10.0	Test Location	9.0	Test Location\n";
		String output = OpenmrsUtil.getFileAsString(exportFile);
		exportFile.delete();
		
		//System.out.println("exportFile: " + output);
		assertEquals("The output is not right.", expectedOutput, output);
		
		
		
		export = new DataExportReportObject();
		export.setName("Last 1 weights");
		
		patientId = new SimpleColumn();
		patientId.setColumnName("PATIENT_ID");
		patientId.setReturnValue("$!{fn.patientId}");
		export.getColumns().add(patientId);
		
		lastNObs = new ConceptColumn();
		lastNObs.setColumnName("WEIGHT");
		lastNObs.setColumnType("concept");
		lastNObs.setConceptId(5089);
		lastNObs.setConceptName("Weight (KG)");
		lastNObs.setExtras(new String[] {"location"});
		lastNObs.setModifier(DataExportReportObject.MODIFIER_LAST_NUM);
		lastNObs.setModifierNum(1);
		export.getColumns().add(lastNObs);
		
		DataExportUtil.generateExport(export, patients, "\t", null);
		exportFile = DataExportUtil.getGeneratedFile(export);		
		
		System.out.println("Template String: \n" + export.generateTemplate());
		expectedOutput = "PATIENT_ID	WEIGHT	WEIGHT_location\n2	10.0	Test Location\n";
		output = OpenmrsUtil.getFileAsString(exportFile);
		exportFile.delete();
		
		//System.out.println("xxxxxxxxxxxxxxxxxxexportFile: " + output);
		assertEquals("The output is not right.", expectedOutput, output);
	}
	
	/**
	 * Tests the data export keys
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldDataExportKeyAddition() throws Exception {
		initializeInMemoryDatabase();
		executeDataSet("org/openmrs/test/reporting/export/include/DataExportTest-patients.xml");
		authenticate();
		
		DataExportReportObject export = new DataExportReportObject();
		export.setName("Data Export Keys");
		
		SimpleColumn patientId = new SimpleColumn();
		patientId.setColumnName("PATIENT_ID");
		patientId.setReturnValue("$!{fn.patientId}");
		export.getColumns().add(patientId);
		
		// this is the column that will be using the key
		SimpleColumn dataExportKey = new SimpleColumn();
		dataExportKey.setColumnName("bobkey");
		dataExportKey.setReturnValue("$!{bob}");
		export.getColumns().add(dataExportKey);
		
		Cohort patients = new Cohort();
		patients.addMember(2);
		
		// add the key so that we can use it
		DataExportUtil.putDataExportKey("bob", "joe");
		
		DataExportUtil.generateExport(export, patients, "\t", null);
		File exportFile = DataExportUtil.getGeneratedFile(export);		
		
		String expectedOutput = "PATIENT_ID	bobkey\n2	joe\n";
		String output = OpenmrsUtil.getFileAsString(exportFile);
		exportFile.delete();
		
		//System.out.println("exportFile: " + output);
		assertEquals("The output is not right.", expectedOutput, output);
	}
	
	/**
	 * Tests removing data export keys
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldDataExportKeyRemoval() throws Exception {
		initializeInMemoryDatabase();
		executeDataSet("org/openmrs/test/reporting/export/include/DataExportTest-patients.xml");
		authenticate();
		
		DataExportReportObject export = new DataExportReportObject();
		export.setName("Data Export Keys");
		
		SimpleColumn patientId = new SimpleColumn();
		patientId.setColumnName("PATIENT_ID");
		patientId.setReturnValue("$!{fn.patientId}");
		export.getColumns().add(patientId);
		
		// this is the column that will be using the key
		SimpleColumn dataExportKey = new SimpleColumn();
		dataExportKey.setColumnName("bobkey");
		dataExportKey.setReturnValue("$!{bob}");
		export.getColumns().add(dataExportKey);
		
		Cohort patients = new Cohort();
		patients.addMember(2);
		
		// add the key so that we can use it
		DataExportUtil.putDataExportKey("bob", "joe");
		
		// remove the key now and try the data export
		DataExportUtil.removeDataExportKey("bob");
		
		// try to remove things that aren't there
		DataExportUtil.removeDataExportKey("bob");
		DataExportUtil.removeDataExportKey("asdfasdf");
		
		DataExportUtil.generateExport(export, patients, "\t", null);
		File exportFile = DataExportUtil.getGeneratedFile(export);		
		
		String expectedOutput = "PATIENT_ID	bobkey\n2	\n";
		String output = OpenmrsUtil.getFileAsString(exportFile);
		exportFile.delete();
		
		//System.out.println("exportFile: " + output);
		assertEquals("The output is not right.", expectedOutput, output);
	}
	
	/**
	 * Tests getting data export keys
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldDataExportKeyGetting() throws Exception {
		// add the key so that we can use it
		DataExportUtil.putDataExportKey("bob", "joe");
		
		assertEquals("joe", DataExportUtil.getDataExportKey("bob"));
		
		// get a bogus key.  should return null (and not error out)
		assertNull(DataExportUtil.getDataExportKey("asdfasdf"));
	}
	
	/**
	 * Test the name option for data exports 
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldGetNames() throws Exception {
		initializeInMemoryDatabase();
		executeDataSet("org/openmrs/test/reporting/export/include/DataExportTest-patients.xml");
		authenticate();
		
		DataExportReportObject export = new DataExportReportObject();
		export.setName("Given names export");
		
		export.addSimpleColumn("PATIENT_ID", "$!{fn.patientId}");
		
		export.addSimpleColumn("Name", "$!{fn.getPatientAttr('PersonName', 'givenName')}");
		
		Cohort patients = new Cohort();
		patients.addMember(2);
		
		DataExportUtil.generateExport(export, patients, "\t", null);
		File exportFile = DataExportUtil.getGeneratedFile(export);
		
		//System.out.println("Template String: \n" + export.generateTemplate());
		String expectedOutput = "PATIENT_ID	Name\n2	John\n";
		String output = OpenmrsUtil.getFileAsString(exportFile);
		exportFile.delete();
		
		//System.out.println("exportFile: \n" + output);
		assertEquals("The output is not right.", expectedOutput, output);
		
	}
}
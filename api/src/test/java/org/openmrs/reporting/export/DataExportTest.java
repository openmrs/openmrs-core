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
import static org.junit.Assert.assertNull;
import static org.powermock.api.mockito.PowerMockito.mock;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.openmrs.Cohort;
import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.CohortService;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.PatientService;
import org.openmrs.api.PatientSetService;
import org.openmrs.api.context.Context;
import org.openmrs.test.TestUtil;
import org.openmrs.util.OpenmrsUtil;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * Tests the {@link DataExportReportObject} class TODO clean up, finish, add methods to this test
 * class
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest( { Context.class })
@SuppressWarnings( { "rawtypes", "unchecked", "deprecation" })
public class DataExportTest {
	
	private Log log = LogFactory.getLog(getClass());
	
	private PatientService ps;
	
	private PatientSetService pss;
	
	private ConceptService cs;
	
	private EncounterService es;
	
	@Before
	public void beforeClass() {
		ps = PowerMockito.mock(PatientService.class);
		pss = PowerMockito.mock(PatientSetService.class);
		cs = PowerMockito.mock(ConceptService.class);
		es = PowerMockito.mock(EncounterService.class);
		
		PowerMockito.stub(PowerMockito.method(Context.class, "getPatientSetService")).toReturn(pss);
		PowerMockito.stub(PowerMockito.method(Context.class, "getPatientService")).toReturn(ps);
		PowerMockito.stub(PowerMockito.method(Context.class, "getConceptService")).toReturn(cs);
		PowerMockito.stub(PowerMockito.method(Context.class, "getEncounterService")).toReturn(es);
		
		//Don't invoke Context.clearSession()
		PowerMockito.spy(Context.class);
		PowerMockito.doNothing().when(Context.class);
		Context.clearSession();
	}
	
	/**
	 * TODO finish and comment method
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldCalculateAge() throws Exception {
		
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
		
		Map<Integer, Object> genders = new HashMap<Integer, Object>();
		genders.put(2, "M");
		PowerMockito.when(
		    pss.getPatientAttributes(Mockito.any(Cohort.class), Mockito.argThat(TestUtil.equalsMatcher("Person")), Mockito
		            .argThat(TestUtil.equalsMatcher("gender")), Mockito.anyBoolean())).thenReturn(genders);
		
		final int birthYear = 2000;
		final String birthDateString = "01/01/" + birthYear;// adjust expected output for every year
		Calendar cal = new GregorianCalendar();
		int currentYear = cal.get(Calendar.YEAR);
		Map<Integer, Object> birthdates = new HashMap<Integer, Object>();
		birthdates.put(2, new SimpleDateFormat("mm/dd/yyyy").parse(birthDateString));
		PowerMockito.when(
		    pss.getPatientAttributes(Mockito.any(Cohort.class), Mockito.argThat(TestUtil.equalsMatcher("Person")), Mockito
		            .argThat(TestUtil.equalsMatcher("birthdate")), Mockito.anyBoolean())).thenReturn(birthdates);
		
		Map<Integer, Object> ages = new HashMap<Integer, Object>();
		ages.put(2, currentYear - birthYear);//since birthdate is 01/01 we can ignore month and date to get years
		PowerMockito.when(
		    pss.getPatientAttributes(Mockito.any(Cohort.class), Mockito.argThat(TestUtil.equalsMatcher("Person")), Mockito
		            .argThat(TestUtil.equalsMatcher("age")), Mockito.anyBoolean())).thenReturn(ages);
		
		DataExportUtil.generateExport(export, patients, "\t", null);
		File exportFile = DataExportUtil.getGeneratedFile(export);
		
		String expectedOutput = "PATIENT_ID	GENDER	BIRTHDATE	AGE\n2	M	" + birthDateString + "	XXX\n";
		expectedOutput = expectedOutput.replace("XXX", String.valueOf(currentYear - birthYear));
		
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
	public void shouldExportFirstNObs() throws Exception {
		log.debug("Testing execution time - start");
		
		DataExportReportObject export = new DataExportReportObject();
		export.setName("FIRST 2 WEIGHTS");
		
		SimpleColumn patientId = new SimpleColumn();
		patientId.setColumnName("PATIENT_ID");
		patientId.setReturnValue("$!{fn.patientId}");
		export.getColumns().add(patientId);
		
		final int conceptId = 5089;
		ConceptColumn firstNObs = new ConceptColumn();
		firstNObs.setColumnName("WEIGHT");
		firstNObs.setColumnType("concept");
		firstNObs.setConceptId(conceptId);
		firstNObs.setConceptName("Weight (KG)");
		firstNObs.setExtras(new String[] { "location" });
		firstNObs.setModifier(DataExportReportObject.MODIFIER_FIRST_NUM);
		firstNObs.setModifierNum(2);
		export.getColumns().add(firstNObs);
		
		final Integer pId = 2;
		Cohort patients = new Cohort();
		patients.addMember(pId);
		
		log.debug("Testing execution time - middle");
		Concept weightConcept = new Concept(conceptId);
		PowerMockito.when(cs.getConcept(Mockito.anyInt())).thenReturn(weightConcept);
		
		Map<Integer, List<List<Object>>> patientIdObservationsMap = new HashMap<Integer, List<List<Object>>>();
		patientIdObservationsMap.put(pId, addNTestObs(2, true));
		
		PowerMockito.when(
		    pss.getObservationsValues(Mockito.any(Cohort.class), Mockito.argThat(TestUtil.equalsMatcher(weightConcept)),
		        Mockito.anyListOf(String.class))).thenReturn(patientIdObservationsMap);
		
		DataExportUtil.generateExport(export, patients, "\t", null);
		File exportFile = DataExportUtil.getGeneratedFile(export);
		
		log.debug("Testing execution time - end");
		
		//System.out.println("Template String: " + export.generateTemplate());
		String expectedOutput = "PATIENT_ID	\"WEIGHT\"	\"WEIGHT_location\" 	\"WEIGHT_(1)\"	\"WEIGHT_location_(1)\"\n2	1.0	Test Location	2.0	Test Location\n";
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
		firstNObs.setConceptId(conceptId);
		firstNObs.setConceptName("Weight (KG)");
		firstNObs.setExtras(new String[] { "location" });
		firstNObs.setModifier(DataExportReportObject.MODIFIER_FIRST_NUM);
		firstNObs.setModifierNum(1);
		export.getColumns().add(firstNObs);
		
		//update the map to have only one obs
		patientIdObservationsMap.put(pId, addNTestObs(1, true));
		
		DataExportUtil.generateExport(export, patients, "\t", null);
		
		exportFile = DataExportUtil.getGeneratedFile(export);
		
		//System.out.println("Template String: " + export.generateTemplate());
		expectedOutput = "PATIENT_ID	\"WEIGHT\"	\"WEIGHT_location\"\n2	1.0	Test Location\n";
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
		firstNObs.setExtras(new String[] { "location" });
		firstNObs.setModifier(DataExportReportObject.MODIFIER_FIRST_NUM);
		firstNObs.setModifierNum(-1);
		export.getColumns().add(firstNObs);
		
		patientIdObservationsMap.put(pId, addNTestObs(10, true));
		
		DataExportUtil.generateExport(export, patients, "\t", null);
		exportFile = DataExportUtil.getGeneratedFile(export);
		
		//System.out.println("Template String: \n" + export.generateTemplate());
		expectedOutput = "PATIENT_ID	\"WEIGHT\"	\"WEIGHT_location\"\n2	10.0	Test Location	9.0	Test Location	8.0	Test Location	7.0	Test Location	6.0	Test Location	5.0	Test Location	4.0	Test Location	3.0	Test Location	2.0	Test Location	1.0	Test Location\n";
		
		output = OpenmrsUtil.getFileAsString(exportFile);
		exportFile.delete();
		
		//System.out.println("exportFile: " + output);
		assertEquals("The output is not what was expected", expectedOutput, output);
		
	}
	
	/**
	 * test first N function when there are no obs for it. Make sure that it returns blank cells
	 * instead of null cells
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldExportFirstNObsWithZeroObsReturned() throws Exception {
		
		DataExportReportObject export = new DataExportReportObject();
		export.setName("NO CONCEPT, THEN GET WEIGHTS");
		
		export.addSimpleColumn("PATIENT_ID", "$!{fn.patientId}");
		
		// no obs should be returned for concept "5090"
		ConceptColumn firstNObs = new ConceptColumn();
		firstNObs.setColumnName("Other");
		firstNObs.setColumnType("concept");
		final int otherConceptId = 5090;
		firstNObs.setConceptId(otherConceptId);
		firstNObs.setConceptName("OTHER CONCEPT");
		firstNObs.setExtras(new String[] { "obsDatetime" });
		firstNObs.setModifier(DataExportReportObject.MODIFIER_FIRST_NUM);
		firstNObs.setModifierNum(2);
		export.getColumns().add(firstNObs);
		
		ConceptColumn lastNObs = new ConceptColumn();
		lastNObs.setColumnName("W-last");
		lastNObs.setColumnType("concept");
		final int weightConceptId = 5089;
		lastNObs.setConceptId(weightConceptId);
		lastNObs.setConceptName("Weight (KG)");
		lastNObs.setExtras(new String[] { "obsDatetime" });
		lastNObs.setModifier(DataExportReportObject.MODIFIER_LAST_NUM);
		lastNObs.setModifierNum(2);
		export.getColumns().add(lastNObs);
		
		Cohort patients = new Cohort();
		int pId = 2;
		patients.addMember(pId);
		
		Concept otherConcept = new Concept(otherConceptId);
		PowerMockito.when(cs.getConcept(Mockito.argThat(TestUtil.equalsMatcher(otherConceptId)))).thenReturn(otherConcept);
		
		Concept weightConcept = new Concept(weightConceptId);
		PowerMockito.when(cs.getConcept(Mockito.argThat(TestUtil.equalsMatcher(weightConceptId)))).thenReturn(weightConcept);
		
		List observations = new ArrayList<List<Object>>(2);
		List obs1Values = new ArrayList<Object>(2);
		obs1Values.add(10.0);
		obs1Values.add("18/02/2006");
		observations.add(obs1Values);
		List obs2Values = new ArrayList<Object>(2);
		obs2Values.add(9.0);
		obs2Values.add("17/02/2006");
		observations.add(obs2Values);
		
		Map<Integer, List<List<Object>>> patientIdObservationsMap = new HashMap<Integer, List<List<Object>>>();
		patientIdObservationsMap.put(pId, observations);
		
		PowerMockito.when(
		    pss.getObservationsValues(Mockito.any(Cohort.class), Mockito.argThat(TestUtil.equalsMatcher(weightConcept)),
		        Mockito.anyListOf(String.class))).thenReturn(patientIdObservationsMap);
		
		DataExportUtil.generateExport(export, patients, "\t", null);
		File exportFile = DataExportUtil.getGeneratedFile(export);
		
		//System.out.println("Template String: \n" + export.generateTemplate());
		String expectedOutput = "PATIENT_ID	\"Other\"	\"Other_obsDatetime\" 	\"Other_(1)\"	\"Other_obsDatetime_(1)\"	\"W-last\"	\"W-last_obsDatetime\" 	\"W-last_(1)\"	\"W-last_obsDatetime_(1)\"\n2					10.0	18/02/2006	9.0	17/02/2006\n";
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
	public void shouldExportFirstObs() throws Exception {
		
		DataExportReportObject export = new DataExportReportObject();
		export.setName("FIRST WEIGHT");
		
		SimpleColumn patientId = new SimpleColumn();
		patientId.setColumnName("PATIENT_ID");
		patientId.setReturnValue("$!{fn.patientId}");
		export.getColumns().add(patientId);
		
		ConceptColumn firstObs = new ConceptColumn();
		firstObs.setColumnName("WEIGHT");
		firstObs.setColumnType("concept");
		final int conceptId = 5089;
		firstObs.setConceptId(conceptId);
		firstObs.setConceptName("Weight (KG)");
		firstObs.setModifier(DataExportReportObject.MODIFIER_FIRST);
		export.getColumns().add(firstObs);
		
		Cohort patients = new Cohort();
		final int pId = 2;
		patients.addMember(pId);
		
		Concept weightConcept = new Concept(conceptId);
		PowerMockito.when(cs.getConcept(Mockito.anyInt())).thenReturn(weightConcept);
		
		Map<Integer, List<List<Object>>> patientIdObservationsMap = new HashMap<Integer, List<List<Object>>>();
		patientIdObservationsMap.put(pId, addNTestObs(1, false));
		
		PowerMockito.when(
		    pss.getObservationsValues(Mockito.any(Cohort.class), Mockito.argThat(TestUtil.equalsMatcher(weightConcept)),
		        Mockito.anyListOf(String.class))).thenReturn(patientIdObservationsMap);
		//System.out.println("Template String: \n" + export.generateTemplate());
		
		DataExportUtil.generateExport(export, patients, "\t", null);
		File exportFile = DataExportUtil.getGeneratedFile(export);
		
		String expectedOutput = "PATIENT_ID\t\"WEIGHT\"\n2\t1.0\n";
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
		firstObs.setExtras(new String[] { "location" });
		firstObs.setModifier(DataExportReportObject.MODIFIER_FIRST);
		export.getColumns().add(firstObs);
		
		//System.out.println("Template String: \n" + export.generateTemplate());
		patientIdObservationsMap.put(pId, addNTestObs(1, true));
		
		DataExportUtil.generateExport(export, patients, "\t", null);
		exportFile = DataExportUtil.getGeneratedFile(export);
		
		expectedOutput = "PATIENT_ID\t\"WEIGHT\"\t\"WEIGHT_location\"\n2\t1.0\tTest Location\n";
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
	public void shouldExportLastNObs() throws Exception {
		
		DataExportReportObject export = new DataExportReportObject();
		export.setName("Last 2 Weights");
		
		SimpleColumn patientId = new SimpleColumn();
		patientId.setColumnName("PATIENT_ID");
		patientId.setReturnValue("$!{fn.patientId}");
		export.getColumns().add(patientId);
		
		ConceptColumn lastNObs = new ConceptColumn();
		lastNObs.setColumnName("WEIGHT");
		lastNObs.setColumnType("concept");
		final int weightConceptId = 5089;
		lastNObs.setConceptId(weightConceptId);
		lastNObs.setConceptName("Weight (KG)");
		lastNObs.setExtras(new String[] { "location" });
		lastNObs.setModifier(DataExportReportObject.MODIFIER_LAST_NUM);
		lastNObs.setModifierNum(2);
		export.getColumns().add(lastNObs);
		
		Cohort patients = new Cohort();
		final int pId = 2;
		patients.addMember(2);
		
		Concept weightConcept = new Concept(weightConceptId);
		PowerMockito.when(cs.getConcept(Mockito.argThat(TestUtil.equalsMatcher(weightConceptId)))).thenReturn(weightConcept);
		
		List observations = new ArrayList<List<Object>>(2);
		List obs1Values = new ArrayList<Object>(2);
		obs1Values.add(10.0);
		obs1Values.add("Test Location");
		observations.add(obs1Values);
		List obs2Values = new ArrayList<Object>(2);
		obs2Values.add(9.0);
		obs2Values.add("Test Location");
		observations.add(obs2Values);
		
		Map<Integer, List<List<Object>>> patientIdObservationsMap = new HashMap<Integer, List<List<Object>>>();
		patientIdObservationsMap.put(pId, observations);
		
		PowerMockito.when(
		    pss.getObservationsValues(Mockito.any(Cohort.class), Mockito.argThat(TestUtil.equalsMatcher(weightConcept)),
		        Mockito.anyListOf(String.class))).thenReturn(patientIdObservationsMap);
		
		DataExportUtil.generateExport(export, patients, "\t", null);
		File exportFile = DataExportUtil.getGeneratedFile(export);
		
		String expectedOutput = "PATIENT_ID	\"WEIGHT\"	\"WEIGHT_location\" 	\"WEIGHT_(1)\"	\"WEIGHT_location_(1)\"\n2	10.0	Test Location	9.0	Test Location\n";
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
		lastNObs.setExtras(new String[] { "location" });
		lastNObs.setModifier(DataExportReportObject.MODIFIER_LAST_NUM);
		lastNObs.setModifierNum(1);
		export.getColumns().add(lastNObs);
		
		List observations2 = new ArrayList<List<Object>>(1);
		observations2.add(obs1Values);
		patientIdObservationsMap.put(pId, observations2);
		
		DataExportUtil.generateExport(export, patients, "\t", null);
		exportFile = DataExportUtil.getGeneratedFile(export);
		
		//System.out.println("Template String: \n" + export.generateTemplate());
		expectedOutput = "PATIENT_ID	\"WEIGHT\"	\"WEIGHT_location\"\n2	10.0	Test Location\n";
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
		
		DataExportReportObject export = new DataExportReportObject();
		export.setName("Given names export");
		
		export.addSimpleColumn("PATIENT_ID", "$!{fn.patientId}");
		
		export.addSimpleColumn("Name", "$!{fn.getPatientAttr('PersonName', 'givenName')}");
		
		Cohort patients = new Cohort();
		int pId = 2;
		patients.addMember(pId);
		
		Map<Integer, Object> names = new HashMap<Integer, Object>();
		names.put(pId, "John");
		PowerMockito.when(
		    pss.getPatientAttributes(Mockito.any(Cohort.class), Mockito.argThat(TestUtil.equalsMatcher("PersonName")),
		        Mockito.argThat(TestUtil.equalsMatcher("givenName")), Mockito.anyBoolean())).thenReturn(names);
		
		DataExportUtil.generateExport(export, patients, "\t", null);
		File exportFile = DataExportUtil.getGeneratedFile(export);
		
		//System.out.println("Template String: \n" + export.generateTemplate());
		String expectedOutput = "PATIENT_ID	Name\n2	John\n";
		String output = OpenmrsUtil.getFileAsString(exportFile);
		exportFile.delete();
		
		//System.out.println("exportFile: \n" + output);
		assertEquals("The output is not right.", expectedOutput, output);
		
	}
	
	/**
	 * Makes sure that the getFirstObs method on the DataExportFunctions object never throws a null
	 * pointer exception if the patient doesn't have any obs. Regression test for ticket #1028
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldNotFailOnFirstObsIfPatientDoesntHaveAnObs() throws Exception {
		
		DataExportReportObject export = new DataExportReportObject();
		export.setName("FIRST WEIGHT");
		
		SimpleColumn patientId = new SimpleColumn("PATIENT_ID", "$!{fn.patientId}");
		export.getColumns().add(patientId);
		
		final Integer weightConceptId = 5089;
		ConceptColumn firstObs = new ConceptColumn("WEIGHT", DataExportReportObject.MODIFIER_FIRST, 1, weightConceptId
		        .toString(), null);
		export.getColumns().add(firstObs);
		
		// set the cohort to a patient hat doesn't have a weight obs
		Cohort patients = new Cohort();
		patients.addMember(6);
		
		Concept weightConcept = new Concept(weightConceptId);
		PowerMockito.when(cs.getConcept(Mockito.argThat(TestUtil.equalsMatcher(weightConceptId)))).thenReturn(weightConcept);
		
		//System.out.println("Template String: \n" + export.generateTemplate());
		
		DataExportUtil.generateExport(export, patients, "\t", null);
		File exportFile = DataExportUtil.getGeneratedFile(export);
		
		String expectedOutput = "PATIENT_ID\t\"WEIGHT\"\n6\t\n";
		String output = OpenmrsUtil.getFileAsString(exportFile);
		exportFile.delete();
		
		//System.out.println("exportFile: \n" + output);
		assertEquals("The output is not right.", expectedOutput, output);
		
	}
	
	/**
	 * Tests the "Cohort" column on data exports to make sure that they are exporting the right data
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldExportCohortColumns() throws Exception {
		CohortService cohortservice = PowerMockito.mock(CohortService.class);
		PowerMockito.stub(PowerMockito.method(Context.class, "getCohortService")).toReturn(cohortservice);
		
		// First create a cohort. TODO maybe move this to xml
		final Integer cohortId = 100;
		Cohort cohort = new Cohort(cohortId);
		cohort.setName("A Cohort");
		cohort.setDescription("Just for testing");
		cohort.addMember(2);
		
		DataExportReportObject export = new DataExportReportObject();
		export.setName("Cohort column");
		
		SimpleColumn patientId = new SimpleColumn("PATIENT_ID", "$!{fn.patientId}");
		export.getColumns().add(patientId);
		
		CohortColumn cohortCol = new CohortColumn("InCohort", cohort.getCohortId(), null, null, "Yes", "No");
		export.getColumns().add(cohortCol);
		
		// set the cohort to two patients, one of which is in the specified cohort
		Cohort patients = new Cohort();
		patients.addMember(2);
		patients.addMember(6);
		
		//System.out.println("Template String: \n" + export.generateTemplate());
		PowerMockito.when(cohortservice.getCohort(Mockito.argThat(TestUtil.equalsMatcher(cohortId)))).thenReturn(cohort);
		
		DataExportUtil.generateExport(export, patients, "\t", null);
		File exportFile = DataExportUtil.getGeneratedFile(export);
		
		String expectedOutput = "PATIENT_ID\tInCohort\n2\tYes\n6\tNo\n";
		String output = OpenmrsUtil.getFileAsString(exportFile);
		exportFile.delete();
		
		//System.out.println("exportFile: \n" + output);
		assertEquals("The output is not right.", expectedOutput, output);
	}
	
	/**
	 * Makes sure that the getFirstObs method on the DataExportFunctions object never throws a null
	 * pointer exception if the patient doesn't have any obs. Regression test for ticket #1028
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldGetContructColumns() throws Exception {
		
		DataExportReportObject export = new DataExportReportObject();
		export.setName("CONSTRUCT EXPORT");
		
		SimpleColumn patientId = new SimpleColumn("PATIENT_ID", "$!{fn.patientId}");
		export.getColumns().add(patientId);
		
		final Integer constructConceptId = 23;
		ConceptColumn firstObs = new ConceptColumn("CONSTRUCT", DataExportReportObject.MODIFIER_FIRST, 1, constructConceptId
		        .toString(), null);
		export.getColumns().add(firstObs);
		
		final Integer weightConceptId = 5089;
		ConceptColumn secondObs = new ConceptColumn("WEIGHT", DataExportReportObject.MODIFIER_FIRST, 1, weightConceptId
		        .toString(), null);
		export.getColumns().add(secondObs);
		
		// set the cohort to a patient that doesn't have a weight obs
		Cohort patients = new Cohort();
		final int pId7 = 7;
		patients.addMember(pId7);
		patients.addMember(8);
		
		AdministrationService as = mock(AdministrationService.class);
		PowerMockito.stub(PowerMockito.method(Context.class, "getAdministrationService")).toReturn(as);
		
		Concept weightConcept = new Concept(weightConceptId);
		PowerMockito.when(cs.getConcept(Mockito.argThat(TestUtil.equalsMatcher(weightConceptId)))).thenReturn(weightConcept);
		
		Concept constructConcept = new Concept(constructConceptId);
		constructConcept.setSet(true);
		PowerMockito.when(cs.getConcept(Mockito.argThat(TestUtil.equalsMatcher(constructConceptId)))).thenReturn(
		    constructConcept);
		
		final int foodAssistanceConceptId = 18;
		Concept foodAssistanceConcept = new Concept(foodAssistanceConceptId);
		foodAssistanceConcept.addName(new ConceptName("FOOD ASSISTANCE", Locale.ENGLISH));
		PowerMockito.when(cs.getConcept(Mockito.argThat(TestUtil.equalsMatcher(foodAssistanceConceptId)))).thenReturn(
		    foodAssistanceConcept);
		
		final int dateOfFoodAssistanceConceptId = 19;
		Concept dateOfFoodAssistanceConcept = new Concept(dateOfFoodAssistanceConceptId);
		dateOfFoodAssistanceConcept.addName(new ConceptName("DATE OF FOOD ASSISTANCE", Locale.ENGLISH));
		PowerMockito.when(cs.getConcept(Mockito.argThat(TestUtil.equalsMatcher(dateOfFoodAssistanceConceptId)))).thenReturn(
		    dateOfFoodAssistanceConcept);
		
		final int favoriteFoodConceptId = 20;
		Concept favoriteFoodConcept = new Concept(favoriteFoodConceptId);
		favoriteFoodConcept.addName(new ConceptName("FAVORITE FOOD, NON-CODED", Locale.ENGLISH));
		PowerMockito.when(cs.getConcept(Mockito.argThat(TestUtil.equalsMatcher(favoriteFoodConceptId)))).thenReturn(
		    favoriteFoodConcept);
		
		PowerMockito.when(cs.getConceptsByConceptSet(Mockito.argThat(TestUtil.equalsMatcher(constructConcept)))).thenReturn(
		    Arrays.asList(foodAssistanceConcept, dateOfFoodAssistanceConcept, favoriteFoodConcept));
		
		Map<Integer, List<List<Object>>> patientIdFoodAssistanceMap = new HashMap<Integer, List<List<Object>>>(1);
		patientIdFoodAssistanceMap.put(pId7, createTestObsData("YES"));
		PowerMockito.when(
		    pss.getObservationsValues(Mockito.any(Cohort.class), Mockito.argThat(TestUtil
		            .equalsMatcher(foodAssistanceConcept)), Mockito.anyListOf(String.class))).thenReturn(
		    patientIdFoodAssistanceMap);
		
		Map<Integer, List<List<Object>>> patientIdDateOfFoodAssistanceMap = new HashMap<Integer, List<List<Object>>>(1);
		patientIdDateOfFoodAssistanceMap.put(pId7, createTestObsData("14/08/2008"));
		PowerMockito.when(
		    pss.getObservationsValues(Mockito.any(Cohort.class), Mockito.argThat(TestUtil
		            .equalsMatcher(dateOfFoodAssistanceConcept)), Mockito.anyListOf(String.class))).thenReturn(
		    patientIdDateOfFoodAssistanceMap);
		
		Map<Integer, List<List<Object>>> patientIdFavoriteFoodMap = new HashMap<Integer, List<List<Object>>>(1);
		patientIdFavoriteFoodMap.put(pId7, createTestObsData("PB and J"));
		PowerMockito.when(
		    pss.getObservationsValues(Mockito.any(Cohort.class), Mockito
		            .argThat(TestUtil.equalsMatcher(favoriteFoodConcept)), Mockito.anyListOf(String.class))).thenReturn(
		    patientIdFavoriteFoodMap);
		
		Map<Integer, List<List<Object>>> patientIdWeightsMap = new HashMap<Integer, List<List<Object>>>(1);
		patientIdWeightsMap.put(pId7, createTestObsData("50.0"));
		PowerMockito.when(
		    pss.getObservationsValues(Mockito.any(Cohort.class), Mockito.argThat(TestUtil.equalsMatcher(weightConcept)),
		        Mockito.anyListOf(String.class))).thenReturn(patientIdWeightsMap);
		
		DataExportUtil.generateExport(export, patients, "\t", null);
		File exportFile = DataExportUtil.getGeneratedFile(export);
		
		String expectedOutput = "PATIENT_ID	\"FOOD ASSISTANCE\"	\"DATE OF FOOD ASSISTANCE\"	\"FAVORITE FOOD, NON-CODED\"	\"WEIGHT\"\n7	YES	14/08/2008	PB and J	50.0\n8				\n";
		String output = OpenmrsUtil.getFileAsString(exportFile);
		exportFile.delete();
		
		assertEquals("The output is not right.", expectedOutput, output);
		
	}
	
	private static List<List<Object>> addNTestObs(int n, boolean includeLocation) {
		List observations = new ArrayList<List<Object>>(n);
		for (int i = n; i > 0; i--) {
			List obsValues = new ArrayList<Object>((includeLocation) ? 2 : 1);
			obsValues.add((double) i);
			if (includeLocation)
				obsValues.add("Test Location");
			observations.add(obsValues);
		}
		
		return observations;
	}
	
	private static List<List<Object>> createTestObsData(Object value) {
		List observations = new ArrayList<List<Object>>(1);
		List obsValues = new ArrayList<Object>(1);
		obsValues.add(value);
		observations.add(obsValues);
		
		return observations;
	}
}

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

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import org.openmrs.DataEntryStatistic;
import org.openmrs.FieldType;
import org.openmrs.PatientIdentifierType;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.FormService;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.reporting.DataTable;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.util.OpenmrsUtil;

/**
 * TODO clean up and finish this test class. Should test all methods
 * in the AdministrationService
 */
public class AdministrationServiceTest extends BaseContextSensitiveTest {
	
	private PatientService ps = null;
	private AdministrationService as = null;
	private FormService formService = null;
	
	@Override
	protected void onSetUpInTransaction() throws Exception {
		initializeInMemoryDatabase();
		authenticate();
		
		ps = Context.getPatientService();
		as = Context.getAdministrationService();
		formService = Context.getFormService();
	}

	/**
	 * TODO make this method not visual-verification dependent
	 * 
	 * @throws Exception
	 */
	public void testDataEntryStats() throws Exception {
		
		Calendar c = new GregorianCalendar();
		c.set(2006, 6, 12);
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		Date fromDate = c.getTime();
		c.add(Calendar.DATE, 7);
		Date toDate = c.getTime();
		
		Date toDateToUse = OpenmrsUtil.lastSecondOfDay(toDate);
		String encUserColumn = null;
		String orderUserColumn = null;
		List<DataEntryStatistic> stats = Context.getAdministrationService().getDataEntryStatistics(fromDate, toDateToUse, encUserColumn, orderUserColumn, "location");
		DataTable table = DataEntryStatistic.tableByUserAndType(stats, true);
		System.out.print("Data entry stats output: " + table.getHtmlTable());
	}
	
	/**
	 * Test the creation/deletion/update of field types
	 * 
	 * @throws Exception
	 */
	public void testFieldType() throws Exception {
		
		//testing creation
		
		FieldType fieldType = new FieldType();
		
		fieldType.setName("testing");
		fieldType.setDescription("desc");
		fieldType.setIsSet(true);
		
		as.createFieldType(fieldType);
		
		FieldType newFieldType = formService.getFieldType(fieldType.getFieldTypeId());
		assertNotNull(newFieldType);
		
		List<FieldType> fieldTypes = formService.getFieldTypes();
		
		//make sure we get a list
		assertNotNull(fieldTypes);
		
		assertNotNull(fieldType);
		
		boolean found = false;
		for(Iterator<FieldType> i = fieldTypes.iterator(); i.hasNext();) {
			FieldType fieldType2 = i.next();
			assertNotNull(fieldType2);
			//check .equals function
			assertTrue(fieldType.equals(fieldType2) == (fieldType.getFieldTypeId().equals(fieldType2.getFieldTypeId())));
			//mark found flag
			if (fieldType.equals(fieldType2))
				found = true;
		}
		
		//assert that the new fieldType was returned in the list
		assertTrue(found);
		
		
		//check update
		newFieldType.setName("another test");
		as.updateFieldType(newFieldType);
		
		FieldType newerFieldType = formService.getFieldType(newFieldType.getFieldTypeId());
		assertTrue(newerFieldType.getName().equals(newFieldType.getName()));
		
		
		//check deletion
		as.deleteFieldType(newFieldType);
		assertNull(formService.getFieldType(newFieldType.getFieldTypeId()));

	}
	
	/**
	 * Test create/update/delete of patient identifier type
	 * 
	 * @throws Exception
	 */
	public void testPatientIdentifierType() throws Exception {
		
		//testing creation
		
		PatientIdentifierType patientIdentifierType = new PatientIdentifierType();
		
		patientIdentifierType.setName("testing");
		patientIdentifierType.setDescription("desc");
		patientIdentifierType.setCheckDigit(false);
		patientIdentifierType.setRequired(false);
		
		as.createPatientIdentifierType(patientIdentifierType);
		
		PatientIdentifierType newPatientIdentifierType = ps.getPatientIdentifierType(patientIdentifierType.getPatientIdentifierTypeId());
		assertNotNull(newPatientIdentifierType);
		
		List<PatientIdentifierType> patientIdentifierTypes = ps.getAllPatientIdentifierTypes();
		
		//make sure we get a list
		assertNotNull(patientIdentifierTypes);
		
		boolean found = false;
		for(Iterator<PatientIdentifierType> i = patientIdentifierTypes.iterator(); i.hasNext();) {
			PatientIdentifierType patientIdentifierType2 = i.next();
			assertNotNull(patientIdentifierType);
			//check .equals function
			assertTrue(patientIdentifierType.equals(patientIdentifierType2) == (patientIdentifierType.getPatientIdentifierTypeId().equals(patientIdentifierType2.getPatientIdentifierTypeId())));
			//mark found flag
			if (patientIdentifierType.equals(patientIdentifierType2))
				found = true;
		}
		
		//assert that the new patientIdentifierType was returned in the list
		assertTrue(found);
		
		
		//check update
		newPatientIdentifierType.setName("another test");
		as.updatePatientIdentifierType(newPatientIdentifierType);
		
		PatientIdentifierType newerPatientIdentifierType = ps.getPatientIdentifierType(newPatientIdentifierType.getPatientIdentifierTypeId());
		assertTrue(newerPatientIdentifierType.getPatientIdentifierTypeId().equals(newPatientIdentifierType.getPatientIdentifierTypeId()));
		
		
		//check deletion
		as.deletePatientIdentifierType(newPatientIdentifierType);
		assertNull(ps.getPatientIdentifierType(newPatientIdentifierType.getPatientIdentifierTypeId()));

	}
	
		
	/**
	 * Tests the AdministrationService.executeSql method with a sql statement
	 * containing a valid group by clause
	 * 
	 * @throws Exception
	 */
	public void testExecuteSqlGroupBy() throws Exception {
		
		String sql = "select encounter1_.location_id, encounter1_.creator, encounter1_.encounter_type, encounter1_.form_id, location2_.location_id, count(obs0_.obs_id) from obs obs0_ right outer join encounter encounter1_ on obs0_.encounter_id=encounter1_.encounter_id inner join location location2_ on encounter1_.location_id=location2_.location_id inner join users user3_ on encounter1_.creator=user3_.user_id inner join person user3_1_ on user3_.user_id=user3_1_.person_id inner join encounter_type encountert4_ on encounter1_.encounter_type=encountert4_.encounter_type_id inner join form form5_ on encounter1_.form_id=form5_.form_id where encounter1_.date_created>='2007-05-05' and encounter1_.date_created<= '2008-05-05' group by encounter1_.location_id, encounter1_.creator , encounter1_.encounter_type , encounter1_.form_id";
		as.executeSQL(sql, true);
		
		String sql2 = "select encounter_id, count(*) from encounter encounter_id group by encounter_id";
		as.executeSQL(sql2, true);
	}
	
}

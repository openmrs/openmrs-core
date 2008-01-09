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

import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.database.QueryDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.ext.hsqldb.HsqldbDataTypeFactory;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.api.EncounterService;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;

/**
 * TODO test all methods in EncounterService
 */
public class EncounterServiceTest extends BaseContextSensitiveTest {
	
	protected static final String ENC_INITIAL_DATA_XML = "org/openmrs/test/include/EncounterServiceTest-initialData.xml";
	
	@Override
	protected void onSetUpInTransaction() throws Exception {
		initializeInMemoryDatabase();
		executeDataSet(ENC_INITIAL_DATA_XML);
		authenticate();
	}
	
	/**
	 * TODO: make this method work. database needs to be setup. dbunit?
	 * 
	 * Test creating and update an Encounter
	 * 
	 * @throws Exception
	 */
	public void testEncounterCreateUpdateDelete() throws Exception {
		authenticate();
		
		EncounterService es = Context.getEncounterService();
		PatientService ps = Context.getPatientService();
		Encounter enc = new Encounter();

		//testing creation
		
		Location loc1 = es.getLocations().get(0);
		assertNotNull("We need a location", loc1);
		EncounterType encType1 = es.getEncounterTypes().get(0);
		assertNotNull("We need an encounter type", encType1);
		Date d1 = new Date();
		Patient pat1 = new Patient(3);
		User pro1 = Context.getAuthenticatedUser();
		
		enc.setLocation(loc1);
		enc.setEncounterType(encType1);
		enc.setEncounterDatetime(d1);
		enc.setPatient(pat1);
		enc.setProvider(pro1);

		es.createEncounter(enc);
		
		Encounter newEnc = es.getEncounter(enc.getEncounterId());
		assertNotNull("We should get back an encounter", newEnc);
		assertTrue("The created encounter needs to equal the pojo encounter", enc.equals(newEnc));
		
		
		//testing update
		
		Location loc2 = es.getLocations().get(1);
		assertNotNull("We need a location", loc2);
		EncounterType encType2 = es.getEncounterTypes().get(1);
		assertNotNull("we need an encounter type", encType2);
		Date d2 = new Date(d1.getTime() + 25);
		Patient pat2 = ps.getPatient(2);
		assertNotNull("We need a patient", pat2);
		
		enc.setLocation(loc2);
		enc.setEncounterType(encType2);
		enc.setEncounterDatetime(d2);
		enc.setPatient(pat2);

		es.updateEncounter(newEnc);
		
		Encounter newestEnc = es.getEncounter(newEnc.getEncounterId());

		assertFalse("The location should be different", loc1.equals(loc2));
		assertTrue("The location should be different", newestEnc.getLocation().equals(loc2));
		assertFalse("The enc should have changed", encType1.equals(encType2));
		assertTrue("The enc type needs to have been set", newestEnc.getEncounterType().equals(encType2));
		assertFalse("Make sure the dates changed slightly", d1.equals(d2));
		assertTrue("The date needs to have been set", newestEnc.getEncounterDatetime().equals(d2));
		assertFalse("The patient should be different", pat1.equals(pat2));
		assertTrue("The patient should have been set", newestEnc.getPatient().equals(pat2));		
		
		// print out the encounter table
		System.out.println("The current row in the encounter table:");
		IDatabaseConnection connection = new DatabaseConnection(getConnection());
		DatabaseConfig config = connection.getConfig();
		config.setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY,
		                   new HsqldbDataTypeFactory());
        QueryDataSet outputSet = new QueryDataSet(connection);
        outputSet.addTable("encounter");
        FlatXmlDataSet.write(outputSet, System.out);
		
		//testing deletion
		
		es.deleteEncounter(newEnc);
		
		Encounter e = es.getEncounter(newEnc.getEncounterId());
		
		assertNull("We shouldn't find the encounter after deletion", e);
		
		
	}
	
}

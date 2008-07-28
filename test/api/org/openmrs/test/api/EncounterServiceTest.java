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
import java.util.Iterator;
import java.util.List;

import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.database.QueryDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.ext.hsqldb.HsqldbDataTypeFactory;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.api.EncounterService;
import org.openmrs.api.LocationService;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;

/**
 * TODO test all methods in EncounterService
 */
public class EncounterServiceTest extends BaseContextSensitiveTest {
	
	protected static final String ENC_INITIAL_DATA_XML = "org/openmrs/test/api/include/EncounterServiceTest-initialData.xml";
	
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
	public void testShouldEncounterCreateUpdateDelete() throws Exception {
		authenticate();
		
		EncounterService es = Context.getEncounterService();
		LocationService locationService = Context.getLocationService();
		PatientService ps = Context.getPatientService();
		Encounter enc = new Encounter();

		//testing creation
		
		Location loc1 = locationService.getAllLocations().get(0);
		assertNotNull("We need a location", loc1);
		EncounterType encType1 = es.getAllEncounterTypes().get(0);
		assertNotNull("We need an encounter type", encType1);
		Date d1 = new Date();
		Patient pat1 = new Patient(3);
		User pro1 = Context.getAuthenticatedUser();
		
		enc.setLocation(loc1);
		enc.setEncounterType(encType1);
		enc.setEncounterDatetime(d1);
		enc.setPatient(pat1);
		enc.setProvider(pro1);

		es.saveEncounter(enc);
		
		Encounter newEnc = es.getEncounter(enc.getEncounterId());
		assertNotNull("We should get back an encounter", newEnc);
		assertTrue("The created encounter needs to equal the pojo encounter", enc.equals(newEnc));
		
		
		//testing update
		
		Location loc2 = locationService.getAllLocations().get(1);
		assertNotNull("We need a location", loc2);
		EncounterType encType2 = es.getAllEncounterTypes().get(1);
		assertNotNull("we need an encounter type", encType2);
		Date d2 = new Date(d1.getTime() + 25);
		Patient pat2 = ps.getPatient(2);
		assertNotNull("We need a patient", pat2);
		
		enc.setLocation(loc2);
		enc.setEncounterType(encType2);
		enc.setEncounterDatetime(d2);
		enc.setPatient(pat2);

		es.saveEncounter(newEnc);
		
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
		
		es.purgeEncounter(newEnc);
		
		Encounter e = es.getEncounter(newEnc.getEncounterId());
		
		assertNull("We shouldn't find the encounter after deletion", e);

	}
	
	/**
	 * You should be able to add an obs to an encounter, save the encounter,
	 * and have the obs automatically persisted.
	 * 
	 * @throws Exception
	 */
	public void testShouldAddObsToEncounter() throws Exception {
		EncounterService es = Context.getEncounterService();
		LocationService locationService = Context.getLocationService();
		PatientService ps = Context.getPatientService();
		Encounter enc = new Encounter();

		//create an encounter
		Location loc1 = locationService.getAllLocations().get(0);
		assertNotNull("We need a location", loc1);
		EncounterType encType1 = es.getAllEncounterTypes().get(0);
		assertNotNull("We need an encounter type", encType1);
		Date d1 = new Date();
		Patient pat1 = new Patient(3);
		User pro1 = Context.getAuthenticatedUser();
		enc.setLocation(loc1);
		enc.setEncounterType(encType1);
		enc.setEncounterDatetime(d1);
		enc.setPatient(pat1);
		enc.setProvider(pro1);
		es.saveEncounter(enc);
		
		// Now add an obs to it
		Obs newObs = new Obs();
		Concept concept = Context.getConceptService().getConcept(1);
		newObs.setConcept(concept);
		newObs.setValueNumeric(50d);
		newObs.setObsDatetime(new Date());
		enc.addObs(newObs);
		es.saveEncounter(enc);
	}
	
	/**
	 * Test create/update/delete of encounter type
	 * 
	 * @throws Exception
	 */
	public void testShouldEncounterType() throws Exception {
		EncounterService encounterService = Context.getEncounterService();
		//testing creation
		
		EncounterType encounterType = new EncounterType();
		
		encounterType.setName("testing");
		encounterType.setDescription("desc");
		
		encounterService.saveEncounterType(encounterType);
		
		//assertNotNull(newE);
		
		EncounterType newEncounterType = encounterService.getEncounterType(encounterType.getEncounterTypeId());
		assertNotNull(newEncounterType);
		
		List<EncounterType> encounterTypes = encounterService.getAllEncounterTypes();
		
		//make sure we get a list
		assertNotNull(encounterTypes);
		
		boolean found = false;
		for(Iterator<EncounterType> i = encounterTypes.iterator(); i.hasNext();) {
			EncounterType encounterType2 = i.next();
			assertNotNull(encounterType);
			//check .equals function
			assertTrue(encounterType.equals(encounterType2) == (encounterType.getEncounterTypeId().equals(encounterType2.getEncounterTypeId())));
			//mark found flag
			if (encounterType.equals(encounterType2))
				found = true;
		}
		
		//assert that the new encounterType was returned in the list
		assertTrue(found);
		
		
		//check update
		newEncounterType.setName("another test");
		encounterService.saveEncounterType(newEncounterType);
		
		EncounterType newerEncounterType = encounterService.getEncounterType(newEncounterType.getEncounterTypeId());
		assertTrue(newerEncounterType.getName().equals(newEncounterType.getName()));
		
		//check deletion
		encounterService.purgeEncounterType(newEncounterType);
		assertNull(encounterService.getEncounterType(newEncounterType.getEncounterTypeId()));
	
	}
	
	public void testShouldModifyEncounterDatetime() throws Exception {
		authenticate();
		
		//First, create an encounter with an obs:
		
		EncounterService es = Context.getEncounterService();
		LocationService locationService = Context.getLocationService();
		PatientService ps = Context.getPatientService();
		Encounter enc = new Encounter();
		
		Location loc1 = locationService.getAllLocations().get(0);
		assertNotNull("We need a location", loc1);
		EncounterType encType1 = es.getAllEncounterTypes().get(0);
		assertNotNull("We need an encounter type", encType1);
		Date d1 = new Date();
		Patient pat1 = new Patient(3);
		User pro1 = Context.getAuthenticatedUser();
		
		enc.setLocation(loc1);
		enc.setEncounterType(encType1);
		enc.setEncounterDatetime(d1);
		enc.setPatient(pat1);
		enc.setProvider(pro1);

		Obs o = new Obs();
		o.setConcept(Context.getConceptService().getConcept(1));
		o.setCreator(Context.getAuthenticatedUser());
		o.setDateCreated(new Date());
		o.setEncounter(enc);
		o.setVoided(false);
		o.setLocation(loc1);
		o.setPatient(pat1);
		o.setValueDatetime(new Date());
		o.setObsDatetime(enc.getEncounterDatetime());
		enc.addObs(o);
		es.saveEncounter(enc);
		
		//now modify the encounterDatetime and re-save.
		
		enc.setEncounterDatetime(new Date(5000));
		es.saveEncounter(enc);
		
		
	}
}

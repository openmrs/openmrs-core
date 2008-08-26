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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Form;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.api.EncounterService;
import org.openmrs.api.ObsService;
import org.openmrs.api.context.Context;
import org.openmrs.test.testutil.BaseContextSensitiveTest;

/**
 * Tests all methods in the {@link EncounterService}
 */
public class EncounterServiceTest extends BaseContextSensitiveTest {
	
	protected static final String ENC_INITIAL_DATA_XML = "org/openmrs/test/api/include/EncounterServiceTest-initialData.xml";
	
	/**
	 * This method is run before all of the tests in this class
	 * because it has the @Before annotation on it.  This will
	 * add the contents of {@link #ENC_INITIAL_DATA_XML} to
	 * the current database
	 * 
	 * @see BaseContextSensitiveTest#runBeforeAllUnitTests()
	 * 
	 * @throws Exception
	 */
	@Before
	public void runBeforeEachTest() throws Exception {
		executeDataSet(ENC_INITIAL_DATA_XML);
	}
	
	/**
	 * Test to make sure that a simple save to a new 
	 * encounter gets persisted to the database
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldSaveEncounterSuccessfully() throws Exception {
		Encounter encounter = new Encounter();

		Location loc1 = new Location(1);
		EncounterType encType1 = new EncounterType(1);
		Date d1 = new Date();
		Patient pat1 = new Patient(3);
		User pro1 = new User(1);
		
		encounter.setLocation(loc1);
		encounter.setEncounterType(encType1);
		encounter.setEncounterDatetime(d1);
		encounter.setPatient(pat1);
		encounter.setProvider(pro1);

		EncounterService es = Context.getEncounterService();
		es.saveEncounter(encounter);
		
		assertNotNull("The saved encounter should have an encounter id now", encounter.getEncounterId());
		Encounter newSavedEncounter = es.getEncounter(encounter.getEncounterId());
		assertNotNull("We should get back an encounter", newSavedEncounter);
		assertTrue("The created encounter needs to equal the pojo encounter", encounter.equals(newSavedEncounter));
	}
	
	/**
	 * Test a simple update to an encounter that is already in the 
	 * database.  
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldUpdateEncounterSuccessfully() throws Exception {
		EncounterService es = Context.getEncounterService();
		
		// get the encounter from the database
		Encounter encounter = es.getEncounter(1);
		
		// save the current values for comparison later
		Patient origPatient = encounter.getPatient();
		Location origLocation = encounter.getLocation();
		Date origDate = encounter.getEncounterDatetime();
		EncounterType origEncType = encounter.getEncounterType();
		
		// add values that are different than the ones in the initialData.xml file
		Location loc2 = new Location(2);
		EncounterType encType2 = new EncounterType(2);
		Date d2 = new Date();
		Patient pat2 = new Patient(2);
		
		encounter.setLocation(loc2);
		encounter.setEncounterType(encType2);
		encounter.setEncounterDatetime(d2);
		encounter.setPatient(pat2);

		// save to the db
		es.saveEncounter(encounter);
		
		// fetch that encounter from the db
		Encounter newestEnc = es.getEncounter(encounter.getEncounterId());

		assertFalse("The location should be different", origLocation.equals(loc2));
		assertTrue("The location should be different", newestEnc.getLocation().equals(loc2));
		assertFalse("The enc should have changed", origEncType.equals(encType2));
		assertTrue("The enc type needs to have been set", newestEnc.getEncounterType().equals(encType2));
		assertFalse("Make sure the dates changed slightly", origDate.equals(d2));
		assertTrue("The date needs to have been set", newestEnc.getEncounterDatetime().equals(d2));
		assertFalse("The patient should be different", origPatient.equals(pat2));
		assertTrue("The patient should have been set", newestEnc.getPatient().equals(pat2));	
	}
	
	/**
	 * Make sure that purging an encounter removes the row
	 * from the database
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldPurgeEncounter() throws Exception {
		
		EncounterService es = Context.getEncounterService();
		
		// fetch the encounter to delete from the db
		Encounter encounterToDelete = es.getEncounter(1);
		
		es.purgeEncounter(encounterToDelete);
		
		// try to refetch the encounter. should get a null object
		Encounter e = es.getEncounter(encounterToDelete.getEncounterId());
		assertNull("We shouldn't find the encounter after deletion", e);
	}
	
	/**
	 * Make sure that purging an encounter removes the row
	 * from the database
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldPurgeEncounterAndCascadeToObsAndOrders() throws Exception {
		
		EncounterService es = Context.getEncounterService();
		
		// fetch the encounter to delete from the db
		Encounter encounterToDelete = es.getEncounter(1);
		
		es.purgeEncounter(encounterToDelete, true);
		
		// try to refetch the encounter. should get a null object
		Encounter e = es.getEncounter(encounterToDelete.getEncounterId());
		assertNull("We shouldn't find the encounter after deletion", e);
		
		ObsService obsService = Context.getObsService();
		assertNull(obsService.getObs(1));
		assertNull(obsService.getObs(2));
		assertNull(obsService.getObs(3));
		
		assertNull(Context.getOrderService().getOrder(1));
	}
	
	/**
	 * You should be able to add an obs to an encounter, save the encounter,
	 * and have the obs automatically persisted.
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldCascadeSaveToObsFromEncounter() throws Exception {
		EncounterService es = Context.getEncounterService();

		// get an encounter from the database
		Encounter encounter = es.getEncounter(1);
		assertNotNull(encounter.getEncounterDatetime());
		
		// Now add an obs to it
		Obs obs = new Obs();
		obs.setConcept(new Concept(1));
		obs.setValueNumeric(50d);
		obs.setObsDatetime(new Date());
		encounter.addObs(obs);
		
		// make sure it was added
		assertTrue(obs.getEncounter().equals(encounter));
		
		// there should not be an obs id before saving the encounter
		assertNull(obs.getObsId());
		
		es.saveEncounter(encounter);
		
		// the obs id should have been populated during the save
		assertNotNull(obs.getObsId());
	}
	
	/**
	 * When the date on an encounter is modified and then saved, 
	 * the encounterservice changes all of the obsdatetimes to 
	 * the new datetime.
	 * 
	 * This test is showing error http://dev.openmrs.org/ticket/934
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldModifyEncounterDatetime() throws Exception {
		EncounterService es = Context.getEncounterService();
		Encounter enc = es.getEncounter(1);
		
		Obs o = new Obs();
		o.setConcept(new Concept(1));
		o.setCreator(Context.getAuthenticatedUser());
		o.setDateCreated(new Date());
		o.setPerson(enc.getPatient());
		o.setObsDatetime(enc.getEncounterDatetime());
		
		Date newDate = new Date();
		// sanity check to make sure the new date is different than the enc date
		assertNotSame(enc.getEncounterDatetime(), newDate);
		
		enc.setEncounterDatetime(newDate);
		
		// save the encounter.  The obs should pick up the encounter's date
		//enc.addObs(o);
		es.saveEncounter(enc);
		
		assertEquals(enc.getEncounterDatetime(), newDate);
		for (Obs obs : enc.getAllObs()) {
			if (obs.getObsId().equals(3))
				// make sure different obs datetimes from the encounter datetime
				// are not edited to the new time
				assertNotSame(newDate, obs.getObsDatetime());
			else
				// make sure all obs were changed
				assertEquals(newDate, obs.getObsDatetime());
		}
	}
	
	/**
	 * When the date on an encounter is modified and then saved, 
	 * the encounterservice changes all of the obsdatetimes to 
	 * the new datetime.
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldModifyEncounterDatetimeWithNewObs() throws Exception {
		EncounterService es = Context.getEncounterService();
		Encounter enc = es.getEncounter(1);
		
		Obs o = new Obs();
		o.setConcept(new Concept(1));
		o.setCreator(Context.getAuthenticatedUser());
		o.setDateCreated(new Date());
		o.setPerson(enc.getPatient());
		o.setObsDatetime(enc.getEncounterDatetime());
		
		Date newDate = new Date();
		// sanity check to make sure the new date is different than the enc date
		assertNotSame(enc.getEncounterDatetime(), newDate);
		
		enc.setEncounterDatetime(newDate);
		
		// save the encounter.  The obs should pick up the encounter's date
		enc.addObs(o);
		es.saveEncounter(enc);
		
		assertEquals(enc.getEncounterDatetime(), newDate);
		assertEquals(enc.getEncounterDatetime(), o.getObsDatetime());
	}
	
	/**
	 * Make sure the creator is preserved when passed into 
	 * {@link EncounterService#saveEncounter(Encounter)}
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldCreateEncounterWithoutOverwritingCreator() throws Exception {
		EncounterService encounterService = Context.getEncounterService();
		
		// the encounter to save without a dateCreated
		Encounter encounter = new Encounter();
		encounter.setLocation(new Location(1));
		encounter.setEncounterType(new EncounterType(1));
		encounter.setEncounterDatetime(new Date());
		encounter.setPatient(new Patient(2));
		encounter.setCreator(new User(4));
		
		// make sure the logged in user isn't the user we're testing with
		assertNotSame(encounter.getCreator(), Context.getAuthenticatedUser());
		
		encounterService.saveEncounter(encounter);
		
		// make sure an encounter id was created
		assertNotNull(encounter.getEncounterId());
		
		// make sure the encounter creator is user 4 not user 1
		assertEquals(encounter.getCreator(), new User(4));
		assertNotSame(encounter.getCreator(), Context.getAuthenticatedUser());
		
		// make sure we can fetch this new encounter
		// from the database and its values are the same as the passed in ones
		Encounter newEncounter = encounterService.getEncounter(encounter.getEncounterId());
		assertNotNull(newEncounter);
		assertEquals(encounter.getCreator(), new User(4));
		assertNotSame(encounter.getCreator(), Context.getAuthenticatedUser());
	}
	
	/**
	 * Make sure the creator and dateCreated values are preserved when 
	 * passed into {@link EncounterService#saveEncounter(Encounter)}
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldCreateEncounterWithoutOverwritingCreatorOrDateCreated() throws Exception {
		EncounterService encounterService = Context.getEncounterService();
		
		// the encounter to save without a dateCreated
		Encounter encounter = new Encounter();
		encounter.setLocation(new Location(1));
		encounter.setEncounterType(new EncounterType(1));
		encounter.setEncounterDatetime(new Date());
		encounter.setPatient(new Patient(2));
		encounter.setCreator(new User(4));
		Date date = new Date(System.currentTimeMillis() - 5000); // make sure we have a date that isn't "right now"
		encounter.setDateCreated(date);
		
		// make sure the logged in user isn't the user we're testing with
		assertNotSame(encounter.getCreator(), Context.getAuthenticatedUser());
		
		encounterService.saveEncounter(encounter);
		
		// make sure an encounter id was created
		assertNotNull(encounter.getEncounterId());
		
		// make sure the encounter creator is user 4 not user 1
		assertEquals(encounter.getCreator(), new User(4));
		assertNotSame(encounter.getCreator(), Context.getAuthenticatedUser());
		
		// make sure the encounter date created wasn't overwritten
		assertEquals(date, encounter.getDateCreated());
		
		// make sure we can fetch this new encounter
		// from the database and its values are the same as the passed in ones
		Encounter newEncounter = encounterService.getEncounter(encounter.getEncounterId());
		assertNotNull(newEncounter);
		assertEquals(encounter.getCreator(), new User(4));
		assertNotSame(encounter.getCreator(), Context.getAuthenticatedUser());
		assertEquals(date, encounter.getDateCreated());
	}
	
	/**
	 * Make sure the dateCreated is preserved when 
	 * passed into {@link EncounterService#saveEncounter(Encounter)}
	 * and no creator is passed in
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldCreateEncounterWithoutOverwritingDateCreated() throws Exception {
		EncounterService encounterService = Context.getEncounterService();
		
		// the encounter to save without a dateCreated
		Encounter encounter = new Encounter();
		encounter.setLocation(new Location(1));
		encounter.setEncounterType(new EncounterType(1));
		encounter.setEncounterDatetime(new Date());
		encounter.setPatient(new Patient(2));
		Date date = new Date(System.currentTimeMillis() - 5000); // make sure we have a date that isn't "right now"
		encounter.setDateCreated(date);
		
		encounterService.saveEncounter(encounter);
		
		// make sure an encounter id was created
		assertNotNull(encounter.getEncounterId());
		
		// make sure the encounter date created wasn't overwritten
		assertEquals(date, encounter.getDateCreated());
		
		// make sure we can fetch this new encounter
		// from the database and its values are the same as the passed in ones
		Encounter newEncounter = encounterService.getEncounter(encounter.getEncounterId());
		assertNotNull(newEncounter);
		assertEquals(date, encounter.getDateCreated());
	}
	
	/**
	 * Make sure the obs and order creator and dateCreated is preserved when 
	 * passed into {@link EncounterService#saveEncounter(Encounter)}
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldCreateEncounterWithoutOverwritingObsAndOrdersCreatorOrDateCreated() throws Exception {
		EncounterService encounterService = Context.getEncounterService();
		
		// the encounter to save without a dateCreated
		Encounter encounter = new Encounter();
		encounter.setLocation(new Location(1));
		encounter.setEncounterType(new EncounterType(1));
		encounter.setEncounterDatetime(new Date());
		encounter.setPatient(new Patient(2));
		Date date = new Date(System.currentTimeMillis() - 5000); // make sure we have a date that isn't "right now"
		encounter.setDateCreated(date);
		User creator = new User(1);
		encounter.setCreator(creator);
		
		// create and add an obs to this encounter
		Obs obs = new Obs(new Patient(2), new Concept(1), new Date(), new Location(1));
		obs.setDateCreated(date);
		obs.setCreator(creator);
		encounter.addObs(obs);
		
		// create and add an order to this encounter
		Order order = new Order();
		order.setConcept(new Concept(1));
		order.setPatient(new Patient(2));
		order.setDateCreated(date);
		order.setCreator(creator);
		encounter.addOrder(order);
		
		// make sure the logged in user isn't the user we're testing with
		assertNotSame(encounter.getCreator(), Context.getAuthenticatedUser());
		
		encounterService.saveEncounter(encounter);
		
		// make sure the obs date created and creator are the same as what we set
		Obs createdObs = Context.getObsService().getObs(obs.getObsId());
		assertEquals(date, createdObs.getDateCreated());
		assertEquals(creator, createdObs.getCreator());
		
		// make sure the order date created and creator are the same as what we set
		Order createdOrder = Context.getOrderService().getOrder(order.getOrderId());
		assertEquals(date, createdOrder.getDateCreated());
		assertEquals(creator, createdOrder.getCreator());
	}
	
	/**
	 * Make sure the creator and dateCreated valueus are added when 
	 * passed into {@link EncounterService#saveEncounter(Encounter)}
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldCreateEncounterAndCascadeCreatorAndDateCreatedToOrders() throws Exception {
		EncounterService encounterService = Context.getEncounterService();
		
		// the encounter to save without a dateCreated
		Encounter encounter = new Encounter();
		encounter.setLocation(new Location(1));
		encounter.setEncounterType(new EncounterType(1));
		encounter.setEncounterDatetime(new Date());
		encounter.setPatient(new Patient(2));
		
		// create and add an order to this encounter
		Order order = new Order();
		order.setConcept(new Concept(1));
		order.setPatient(new Patient(2));
		encounter.addOrder(order);
		
		// make sure the logged in user isn't the user we're testing with
		assertNotSame(encounter.getCreator(), Context.getAuthenticatedUser());
		
		encounterService.saveEncounter(encounter);
		
		// make sure the order date created and creator are the same as what we set
		Order createdOrder = Context.getOrderService().getOrder(order.getOrderId());
		assertNotNull(encounter.getDateCreated());
		assertNotNull(createdOrder.getDateCreated());
		assertEquals(encounter.getDateCreated(), createdOrder.getDateCreated());
		
		assertNotNull(encounter.getCreator());
		assertNotNull(createdOrder.getCreator());
		assertEquals(encounter.getCreator(), createdOrder.getCreator());
	}
	
	/**
	 * Should get all nonvoided encounters by the patient they
	 * are associated to
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldGetNonVoidedEncountersByPatient() throws Exception {
		EncounterService encounterService = Context.getEncounterService();
		
		List<Encounter> encounters = encounterService.getEncountersByPatient(new Patient(3));
		assertEquals(1, encounters.size());
		
		// sanity check to make sure there is a voided encounter associated with patient#3
		Encounter voidedEncounter = encounterService.getEncounter(2);
		assertTrue(voidedEncounter.isVoided());
		assertEquals(new Patient(3), voidedEncounter.getPatient());
	}
	
	/**
	 * An error should be thrown when passing a null argument to
	 * {@link EncounterService#getEncountersByPatient(Patient)}
	 * 
	 * @throws Exception
	 */
	@Test(expected=IllegalArgumentException.class)
	public void shouldThrowErrorWhenGettingEncountersByPatientWithNullParam() throws Exception {
		Context.getEncounterService().getEncountersByPatient(null);
	}
	
	/**
	 * Should get all nonvoided encounters by the patient id they
	 * are associated to
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldGetNonVoidedEncountersByPatientId() throws Exception {
		EncounterService encounterService = Context.getEncounterService();
		
		List<Encounter> encounters = encounterService.getEncountersByPatientId(3);
		assertEquals(1, encounters.size());
		
		// sanity check to make sure there is a voided encounter associated with patient#3
		Encounter voidedEncounter = encounterService.getEncounter(2);
		assertTrue(voidedEncounter.isVoided());
		assertEquals(new Patient(3), voidedEncounter.getPatient());
	}
	
	/**
	 * An error should be thrown when passing a null argument to
	 * {@link EncounterService#getEncountersByPatientId(Integer)}
	 * 
	 * @throws Exception
	 */
	@Test(expected=IllegalArgumentException.class)
	public void shouldThrowErrorWhenGettingEncountersByPatientIdWithNullParam() throws Exception {
		Context.getEncounterService().getEncountersByPatientId(null);
	}
	
	/**
	 * Should get all nonvoided encounters by the patient they
	 * are associated to
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldGetNonVoidedEncountersByPatientIdentifier() throws Exception {
		EncounterService encounterService = Context.getEncounterService();
		
		List<Encounter> encounters = encounterService.getEncountersByPatientIdentifier("12345");
		assertEquals(1, encounters.size());
		
		// sanity check to make sure there is a voided encounter associated with patient#3
		Encounter voidedEncounter = encounterService.getEncounter(2);
		assertTrue(voidedEncounter.isVoided());
		assertEquals(new Patient(3), voidedEncounter.getPatient());
	}
	
	/**
	 * An error should be thrown when passing a null argument to
	 * {@link EncounterService#getEncountersByPatientIdentifier(String)}
	 * 
	 * @throws Exception
	 */
	@Test(expected=IllegalArgumentException.class)
	public void shouldThrowErrorWhenGettingEncountersByPatientIdentifierWithNullParam() throws Exception {
		Context.getEncounterService().getEncountersByPatientIdentifier(null);
	}
	
	/**
	 * Make sure {@link EncounterService#voidEncounter(Encounter, String)}
	 * marks all the voided stuff correctly
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldVoidAnEncounter() throws Exception {
		EncounterService encounterService = Context.getEncounterService();
		
		// get a nonvoided encounter
		Encounter encounter = encounterService.getEncounter(1);
		assertFalse(encounter.isVoided());
		assertNull(encounter.getVoidedBy());
		assertNull(encounter.getVoidReason());
		assertNull(encounter.getDateVoided());
		
		Encounter voidedEnc = encounterService.voidEncounter(encounter, "Just Testing");
		
		// make sure its still the same object
		assertEquals(voidedEnc, encounter);
		
		// make sure that all the values were filled in
		assertTrue(voidedEnc.isVoided());
		assertNotNull(voidedEnc.getDateVoided());
		assertEquals(Context.getAuthenticatedUser(), voidedEnc.getVoidedBy());
		assertEquals("Just Testing", voidedEnc.getVoidReason());
	}
	
	/**
	 * Obs that are on an encounter should be voided when the encounter is voided
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldCascadeVoidToObsWhenVoidingAnEncounter() throws Exception {
		EncounterService encounterService = Context.getEncounterService();
		
		// get a nonvoided encounter that has some obs
		Encounter encounter = encounterService.getEncounter(1);
		encounterService.voidEncounter(encounter, "Just Testing");
		
		Obs obs = Context.getObsService().getObs(1);
		assertTrue(obs.isVoided());
		assertEquals("Just Testing", obs.getVoidReason());
	}
	
	/**
	 * Orders that are on an encounter should be voided when the encounter is voided
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldCascadeVoidToOrdersWhenVoidingAnEncounter() throws Exception {
		EncounterService encounterService = Context.getEncounterService();
		
		// get a nonvoided encounter that has some obs
		Encounter encounter = encounterService.getEncounter(1);
		encounterService.voidEncounter(encounter, "Just Testing");
		
		Order order = Context.getOrderService().getOrder(1);
		assertTrue(order.isVoided());
		assertEquals("Just Testing", order.getVoidReason());
	}
	
	/**
	 * Obs that are on an encounter should be unvoided when the encounter is unvoided
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldCascadeUnvoidToObsWhenVoidingAnEncounter() throws Exception {
		EncounterService encounterService = Context.getEncounterService();
		
		// get a voided encounter that has some voided obs
		Encounter encounter = encounterService.getEncounter(2);
		encounterService.unvoidEncounter(encounter);
		
		Obs obs = Context.getObsService().getObs(4);
		assertFalse(obs.isVoided());
		assertNull(obs.getVoidReason());
	}
	
	/**
	 * Orders that are on an encounter should be unvoided when the encounter is unvoided
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldCascadeUnvoidToOrdersWhenVoidingAnEncounter() throws Exception {
		EncounterService encounterService = Context.getEncounterService();
		
		// get a voided encounter that has some voided obs
		Encounter encounter = encounterService.getEncounter(2);
		encounterService.unvoidEncounter(encounter);
		
		Order order = Context.getOrderService().getOrder(2);
		assertFalse(order.isVoided());
		assertNull(order.getVoidReason());
	}
	
	/**
	 * A void_reason value should be required
	 *  
	 * @throws Exception
	 */
	@Test(expected=IllegalArgumentException.class)
	public void shouldThrowErrorWhenVoidingEncounterWithNullReason() throws Exception {
		EncounterService encounterService = Context.getEncounterService();
		Encounter type = encounterService.getEncounter(1);
		encounterService.voidEncounter(type, null);
	}
	
	/**
	 * Make sure {@link EncounterService#unvoidEncounter(Encounter)}
	 * unmarks all the voided stuff correctly
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldUnVoidAnEncounter() throws Exception {
		EncounterService encounterService = Context.getEncounterService();
		
		// get an already voided encounter
		Encounter encounter = encounterService.getEncounter(2);
		assertTrue(encounter.isVoided());
		assertNotNull(encounter.getVoidedBy());
		assertNotNull(encounter.getVoidReason());
		assertNotNull(encounter.getDateVoided());
		
		Encounter unvoidedEnc = encounterService.unvoidEncounter(encounter);
		
		// make sure its still the same object
		assertEquals(unvoidedEnc, encounter);
		
		// make sure that all the values were unfilled in
		assertFalse(unvoidedEnc.isVoided());
		assertNull(unvoidedEnc.getDateVoided());
		assertNull(unvoidedEnc.getVoidedBy());
		assertNull(unvoidedEnc.getVoidReason());
	}
	
	/**
	 * Get encounters by their locations
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldGetEncountersByLocation() throws Exception {
		List<Encounter> encounters = Context.getEncounterService().getEncounters(null, new Location(1), null, null, null, null, true);
		assertEquals(4, encounters.size());
	}
	
	/**
	 * Get encounters that are after a certain date
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldGetEncountersFromDate() throws Exception {
		Date fromDate = new SimpleDateFormat("yyyy-dd-MM").parse("2006-01-01");
		List<Encounter> encounters = Context.getEncounterService().getEncounters(null, null, fromDate, null, null, null, true);
		assertEquals(4, encounters.size());
		assertEquals(2, encounters.get(0).getEncounterId().intValue());
		assertEquals(3, encounters.get(1).getEncounterId().intValue());
		assertEquals(4, encounters.get(2).getEncounterId().intValue());
		assertEquals(5, encounters.get(3).getEncounterId().intValue());
	}
	
	/**
	 * Get encounters that are up to a certain date
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldGetEncountersToDate() throws Exception {
		Date toDate = new SimpleDateFormat("yyyy-dd-MM").parse("2006-01-01");
		List<Encounter> encounters = Context.getEncounterService().getEncounters(null, null, null, toDate, null, null, true);
		assertEquals(1, encounters.size());
		assertEquals(1, encounters.get(0).getEncounterId().intValue());
	}
	
	/**
	 * Get encounters that are assigned to a form
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldGetEncountersByForm() throws Exception {
		List<Form> forms = new Vector<Form>();
		forms.add(new Form(1));
		List<Encounter> encounters = Context.getEncounterService().getEncounters(null, null, null, null, forms, null, true);
		assertEquals(5, encounters.size());
	}
	
	/**
	 * Get encounters that have a certain type
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldGetEncountersByType() throws Exception {
		List<EncounterType> types = new Vector<EncounterType>();
		types.add(new EncounterType(1));
		List<Encounter> encounters = Context.getEncounterService().getEncounters(null, null, null, null, null, types, true);
		assertEquals(4, encounters.size());
	}
	
	/**
	 * Get encounters that are voided/nonvoided
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldGetVoidedEncounters() throws Exception {
		assertEquals(4, Context.getEncounterService().getEncounters(null, null, null, null, null, null, false).size());
		assertEquals(5, Context.getEncounterService().getEncounters(null, null, null, null, null, null, true).size());
	}
	
	/**
	 * Test that saving an encounter type works
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldCreateEncounterTypeSuccessfully() throws Exception {
		EncounterService encounterService = Context.getEncounterService();
		
		EncounterType encounterType = new EncounterType();
		
		encounterType.setName("testing");
		encounterType.setDescription("desc");
		
		encounterService.saveEncounterType(encounterType);
		
		// make sure an encounter type id was created
		assertNotNull(encounterType.getEncounterTypeId());
		
		// make sure we can fetch this new encounter type
		// from the database
		EncounterType newEncounterType = encounterService.getEncounterType(encounterType.getEncounterTypeId());
		assertNotNull(newEncounterType);
	}
	
	/**
	 * Make sure the creator is preserved when passed into 
	 * {@link EncounterService#saveEncounterType(EncounterType)}
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldCreateEncounterTypeWithoutOverwritingCreator() throws Exception {
		EncounterService encounterService = Context.getEncounterService();
		
		// the encounter to save without a dateCreated
		EncounterType encounterType = new EncounterType();
		encounterType.setName("testing");
		encounterType.setDescription("desc");
		encounterType.setCreator(new User(4));
		
		// make sure the logged in user isn't the user we're testing with
		assertNotSame(encounterType.getCreator(), Context.getAuthenticatedUser());
		
		encounterService.saveEncounterType(encounterType);
		
		// make sure an encounter type id was created
		assertNotNull(encounterType.getEncounterTypeId());
		
		// make sure the encounter type creator is user 4 not user 1
		assertEquals(encounterType.getCreator(), new User(4));
		assertNotSame(encounterType.getCreator(), Context.getAuthenticatedUser());
		
		// make sure we can fetch this new encounter type
		// from the database and its values are the same as the passed in ones
		EncounterType newEncounterType = encounterService.getEncounterType(encounterType.getEncounterTypeId());
		assertNotNull(newEncounterType);
		assertEquals(encounterType.getCreator(), new User(4));
		assertNotSame(encounterType.getCreator(), Context.getAuthenticatedUser());
	}
	
	/**
	 * Make sure the creator and dateCreated values are preserved when 
	 * passed into {@link EncounterService#saveEncounterType(EncounterType)}
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldCreateEncounterTypeWithoutOverwritingCreatorOrDateCreated() throws Exception {
		EncounterService encounterService = Context.getEncounterService();
		
		// the encounter to save without a dateCreated
		EncounterType encounterType = new EncounterType();
		encounterType.setName("testing");
		encounterType.setDescription("desc");
		encounterType.setCreator(new User(4));
		Date date = new Date(System.currentTimeMillis() - 5000); // make sure we have a date that isn't "right now"
		encounterType.setDateCreated(date);
		
		// make sure the logged in user isn't the user we're testing with
		assertNotSame(encounterType.getCreator(), Context.getAuthenticatedUser());
		
		encounterService.saveEncounterType(encounterType);
		
		// make sure an encounter type id was created
		assertNotNull(encounterType.getEncounterTypeId());
		
		// make sure the encounter type creator is user 4 not user 1
		assertEquals(encounterType.getCreator(), new User(4));
		assertNotSame(encounterType.getCreator(), Context.getAuthenticatedUser());
		
		// make sure the encounter type date created wasn't overwritten
		assertEquals(date, encounterType.getDateCreated());
		
		// make sure we can fetch this new encounter type
		// from the database and its values are the same as the passed in ones
		EncounterType newEncounterType = encounterService.getEncounterType(encounterType.getEncounterTypeId());
		assertNotNull(newEncounterType);
		assertEquals(encounterType.getCreator(), new User(4));
		assertNotSame(encounterType.getCreator(), Context.getAuthenticatedUser());
		assertEquals(date, encounterType.getDateCreated());
	}
	
	/**
	 * Make sure the dateCreated is preserved when 
	 * passed into {@link EncounterService#saveEncounterType(EncounterType)}
	 * and no creator is passed in
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldCreateEncounterTypeWithoutOverwritingDateCreated() throws Exception {
		EncounterService encounterService = Context.getEncounterService();
		
		// the encounter to save without a dateCreated
		EncounterType encounterType = new EncounterType();
		encounterType.setName("testing");
		encounterType.setDescription("desc");
		Date date = new Date(System.currentTimeMillis() - 5000); // make sure we have a date that isn't "right now"
		encounterType.setDateCreated(date);
		
		// make sure the logged in user isn't the user we're testing with
		assertNotSame(encounterType.getCreator(), Context.getAuthenticatedUser());
		
		encounterService.saveEncounterType(encounterType);
		
		// make sure an encounter type id was created
		assertNotNull(encounterType.getEncounterTypeId());
		
		// make sure the encounter type date created wasn't overwritten
		assertEquals(date, encounterType.getDateCreated());
		
		// make sure we can fetch this new encounter type
		// from the database and its values are the same as the passed in ones
		EncounterType newEncounterType = encounterService.getEncounterType(encounterType.getEncounterTypeId());
		assertNotNull(newEncounterType);
		assertEquals(date, encounterType.getDateCreated());
	}
	
	/**
	 * There should be two encounters in the system with the name "Test Enc Type A" and
	 * one should be retired and one not.  Only the non retired one should be returned here 
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldGetEncounterTypeByName() throws Exception {
		EncounterService encounterService = Context.getEncounterService();
		
		// loop over retired and nonretired types to make sure
		// that there are two "Test Enc Type A" types (one retired, one not)
		boolean foundRetired = false;
		boolean foundNonRetired = false;
		int countOfTestEncType2s = 0;
		for (EncounterType encType : encounterService.getAllEncounterTypes(true)) {
			if (encType.getName().equals("Test Enc Type A")) {
				countOfTestEncType2s++;
				if (encType.isRetired())
					foundRetired = true;
				else
					foundNonRetired = true;
			}
		}
		// check that both were set to true
		assertEquals("We are only expecting to have two types: one retired, one not", 2, countOfTestEncType2s);
		assertTrue("No retired type was found in the db", foundRetired);
		assertTrue("No non-retired type was found in the db", foundNonRetired);
		
		// we should not get two types here, the second one is retired
		EncounterType type = encounterService.getEncounterType("Test Enc Type A");
		assertEquals(2, type.getEncounterTypeId().intValue());
		assertFalse(type.isRetired());
	}
	
	/**
	 * Make sure that the "Some Retired Type" type is not returned
	 * because it is retired in {@link EncounterService#getEncounterType(String)}
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldNotGetRetiredEncounterTypeByName() throws Exception {
		EncounterService encounterService = Context.getEncounterService();
		
		// we should get a null here because this named type is retired
		EncounterType type = encounterService.getEncounterType("Some Retired Type");
		assertNull(type);
	}
	
	/**
	 * Make sure that we are matching on exact name and not partial name
	 * in {@link EncounterService#getEncounterType(String)}
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldNotGetEncounterTypeByInExactName() throws Exception {
		EncounterService encounterService = Context.getEncounterService();
		
		// we should not get two types here, the second one is retired
		EncounterType type = encounterService.getEncounterType("Test Enc Type A");
		assertEquals(2, type.getEncounterTypeId().intValue());
		
		// we should not get any encounters here even though "Test Enc" is similar 
		// to a name that is in the db
		EncounterType typeByInExactName = encounterService.getEncounterType("Test Enc");
		assertNull(typeByInExactName);
	}
	
	/**
	 * Make sure that we are not throwing an error with a null parameter to
	 * {@link EncounterService#getEncounterType(String)}
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldNotThrowErrorWithNullEncounterTypeName() throws Exception {
		EncounterService encounterService = Context.getEncounterService();
		
		// we should not get an error here...but silently return nothing
		EncounterType type = encounterService.getEncounterType((String)null);
		assertNull(type);
	}
	
	/**
	 * Test that getting the current encounter types from
	 * the database works
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldGetAllNonRetiredEncounterTypes() throws Exception {
		EncounterService encounterService = Context.getEncounterService();
		List<EncounterType> encounterTypes = encounterService.getAllEncounterTypes(false);
		
		//make sure we get a list
		assertNotNull(encounterTypes);
		
		// make sure we only get the two non retired encounter types
		// defined in the initialData.xml
		assertEquals(2, encounterTypes.size());
	}
	
	/**
	 * Tests {@link EncounterService#getAllEncounterTypes()} pass through
	 * to {@link EncounterService#getAllEncounterTypes(boolean)} with false
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldGetAllEncounterTypesExcludingRetired() throws Exception {
		EncounterService encounterService = Context.getEncounterService();
		List<EncounterType> types = encounterService.getAllEncounterTypes();
		assertNotNull(types);
		assertEquals(2, types.size());
		
		// check each returned type to make sure its not retired
		for (EncounterType type : types) {
			assertFalse(type.isRetired());
		}
		
		// sanity check to make sure that there was a retired type in the db
		// loop over both retired and non-retired to make sure there is at
		// least one retired type
		boolean foundRetired = false;
		for (EncounterType type : encounterService.getAllEncounterTypes(true)) {
			if (type.isRetired())
				foundRetired = true;
		}
		assertTrue("There should be a retired type in the database so that the getAllEncounterTypes() method can be tested correctly", foundRetired);
	}
	
	/**
	 * Make sure that {@link EncounterService#getAllEncounterTypes(boolean)}
	 * with a true parameter returns the retired types
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldGetAllEncounterTypesIncludingRetired() throws Exception {
		EncounterService encounterService = Context.getEncounterService();
		boolean foundRetired = false;
		List<EncounterType> types = encounterService.getAllEncounterTypes(true);
		
		// there should be four types in the database
		assertEquals(5, types.size());
		
		for (EncounterType type : types) {
			if (type.isRetired())
				foundRetired = true;
		}
		assertTrue("Retired types should be returned as well", foundRetired);
	}
	
	/**
	 * Find the types that start with "Test Enc" using 
	 * {@link EncounterService#findEncounterTypes(String)}
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldFindEncounterTypes() throws Exception {
		EncounterService encounterService = Context.getEncounterService();
		List<EncounterType> types = encounterService.findEncounterTypes("Test Enc");
		assertEquals(3, types.size());
	}
	
	/**
	 * Find the types that start with "Test Enc" case-INsensitive using 
	 * {@link EncounterService#findEncounterTypes(String)}
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldFindEncounterTypeByNameCaseInsensitive() throws Exception {
		EncounterService encounterService = Context.getEncounterService();
		List<EncounterType> types = encounterService.findEncounterTypes("Test ENC");
		assertEquals(3, types.size());
	}
	
	/**
	 * {@link EncounterService#findEncounterTypes(String)} should return 
	 * retired types as well as nonretired ones
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldFindRetiredEncounterTypes() throws Exception {
		EncounterService encounterService = Context.getEncounterService();
		List<EncounterType> types = encounterService.findEncounterTypes("Test Enc");
		assertEquals(3, types.size());
		
		// make sure at least one of the types was retired
		boolean foundRetired = false;
		for (EncounterType type : types) {
			if (type.isRetired())
				foundRetired = true;
		}
		assertTrue("Retired types should be returned as well", foundRetired);
	}
	
	/**
	 * No types should be returned when using a substring other than the
	 * starting substring from {@link EncounterService#findEncounterTypes(String)}
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldFindEncounterTypesOnlyMatchingAtBeginningOfName() throws Exception {
		EncounterService encounterService = Context.getEncounterService();
		List<EncounterType> types = encounterService.findEncounterTypes("Test Enc Type");
		assertEquals(3, types.size());
		
		types = encounterService.findEncounterTypes("Enc Type");
		assertEquals(0, types.size());
	}
	
	/**
	 * {@link EncounterService#findEncounterTypes(String)} should return results
	 * ordered on EncounterType.name with nonretired ones first
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldFindEncounterTypesOrderedByName() throws Exception {
		EncounterService encounterService = Context.getEncounterService();
		List<EncounterType> types = encounterService.findEncounterTypes("Test Enc");
		
		// make sure the order is id 2, 3, 1
		assertEquals(2, types.get(0).getEncounterTypeId().intValue());
		assertEquals(3, types.get(1).getEncounterTypeId().intValue());
		assertEquals(1, types.get(2).getEncounterTypeId().intValue());
		
		// this test expects that id #2 and id #3 have the same name and that 
		// id #3 is retired 
		assertEquals(types.get(0).getName(), types.get(1).getName());
		assertTrue(types.get(1).isRetired());
	}
	
	/**
	 * Make sure {@link EncounterService#retireEncounterType(EncounterType, String)}
	 * marks all the retired stuff correctly
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldRetireAnEncounterType() throws Exception {
		EncounterService encounterService = Context.getEncounterService();
		EncounterType type = encounterService.getEncounterType(1);
		assertFalse(type.isRetired());
		assertNull(type.getRetiredBy());
		assertNull(type.getRetireReason());
		assertNull(type.getDateRetired());
		
		EncounterType retiredEncType = encounterService.retireEncounterType(type, "Just Testing");
		
		// make sure its still the same object
		assertEquals(retiredEncType, type);
		
		// make sure that all the values were filled in
		assertTrue(retiredEncType.isRetired());
		assertNotNull(retiredEncType.getDateRetired());
		assertEquals(Context.getAuthenticatedUser(), retiredEncType.getRetiredBy());
		assertEquals("Just Testing", retiredEncType.getRetireReason());
	}
	
	/**
	 * A retire_reason value should be required
	 *  
	 * @throws Exception
	 */
	@Test(expected=IllegalArgumentException.class)
	public void shouldThrowErrorWhenRetiringEncounterTypeWithNullReason() throws Exception {
		EncounterService encounterService = Context.getEncounterService();
		EncounterType type = encounterService.getEncounterType(1);
		encounterService.retireEncounterType(type, null);
	}
	
	/**
	 * Make sure {@link EncounterService#unretireEncounterType(EncounterType)}
	 * unmarks all the retired stuff correctly
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldUnRetireAnEncounterType() throws Exception {
		EncounterService encounterService = Context.getEncounterService();
		EncounterType type = encounterService.getEncounterType(3);
		//TestUtil.printOutTableContents(getConnection(), "encounter_type", "encounter");
		assertTrue(type.isRetired());
		assertNotNull(type.getRetiredBy());
		assertNotNull(type.getRetireReason());
		assertNotNull(type.getDateRetired());
		
		EncounterType unretiredEncType = encounterService.unretireEncounterType(type);
		
		// make sure its still the same object
		assertEquals(unretiredEncType, type);
		
		// make sure that all the values were unfilled in
		assertFalse(unretiredEncType.isRetired());
		assertNull(unretiredEncType.getDateRetired());
		assertNull(unretiredEncType.getRetiredBy());
		assertNull(unretiredEncType.getRetireReason());
	}
	
	/**
	 * Test that updating a current encounter type works
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldUpdateEncounterTypeName() throws Exception {
		EncounterService encounterService = Context.getEncounterService();
		
		EncounterType encounterTypeToChange = encounterService.getEncounterType(1);
		
		// change the name of the type
		encounterTypeToChange.setName("another test");
		
		// save the type to the database
		encounterService.saveEncounterType(encounterTypeToChange);
		
		// make sure the encounter type id didn't change
		assertEquals(1, encounterTypeToChange.getEncounterTypeId().intValue());
		
		// refetch the encounter type from the database
		EncounterType fetchedEncounterType = encounterService.getEncounterType(1);
		assertTrue(fetchedEncounterType.getName().equals("another test"));
	}
	
	/**
	 * Check that a purged encounterType is no longer in the database
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldPurgeEncounterType() throws Exception {
		EncounterService encounterService = Context.getEncounterService();
		
		EncounterType encounterTypeToPurge = encounterService.getEncounterType(1);
		assertNotNull(encounterTypeToPurge);
		
		//check deletion
		encounterService.purgeEncounterType(encounterTypeToPurge);
		assertNull(encounterService.getEncounterType(1));
	}
	
	/**
	 * Trying to get an encounter with a null id should fail
	 * 
	 * @throws Exception
	 */
	@Test(expected=IllegalArgumentException.class)
	public void shouldThrowErrorWhenGettingEncounterById() throws Exception {
		Context.getEncounterService().getEncounter(null);
	}
	
	/**
	 * Trying to get an encounter type with a null id should fail
	 * 
	 * @throws Exception
	 */
	@Test(expected=IllegalArgumentException.class)
	public void shouldThrowErrorWhenGettingEncounterTypeById() throws Exception {
		Context.getEncounterService().getEncounterType((Integer)null);
	}
	
}

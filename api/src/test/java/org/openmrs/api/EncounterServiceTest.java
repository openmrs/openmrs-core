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
package org.openmrs.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Cohort;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Form;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;

/**
 * Tests all methods in the {@link EncounterService}
 */
public class EncounterServiceTest extends BaseContextSensitiveTest {
	
	protected static final String ENC_INITIAL_DATA_XML = "org/openmrs/api/include/EncounterServiceTest-initialData.xml";
	
	/**
	 * This method is run before all of the tests in this class because it has the @Before
	 * annotation on it. This will add the contents of {@link #ENC_INITIAL_DATA_XML} to the current
	 * database
	 * 
	 * @see BaseContextSensitiveTest#runBeforeAllUnitTests()
	 * @throws Exception
	 */
	@Before
	public void runBeforeEachTest() throws Exception {
		executeDataSet(ENC_INITIAL_DATA_XML);
	}
	
	/**
	 * @see {@link EncounterService#saveEncounter(Encounter)}
	 */
	@Test
	@Verifies(value = "should save encounter with basic details", method = "saveEncounter(Encounter)")
	public void saveEncounter_shouldSaveEncounterWithBasicDetails() throws Exception {
		Encounter encounter = new Encounter();
		encounter.setLocation(new Location(1));
		encounter.setEncounterType(new EncounterType(1));
		encounter.setEncounterDatetime(new Date());
		encounter.setPatient(new Patient(3));
		encounter.setProvider(new Person(1));
		
		EncounterService es = Context.getEncounterService();
		es.saveEncounter(encounter);
		
		assertNotNull("The saved encounter should have an encounter id now", encounter.getEncounterId());
		Encounter newSavedEncounter = es.getEncounter(encounter.getEncounterId());
		assertNotNull("We should get back an encounter", newSavedEncounter);
		assertTrue("The created encounter needs to equal the pojo encounter", encounter.equals(newSavedEncounter));
	}
	
	/**
	 * @see {@link EncounterService#saveEncounter(Encounter)}
	 */
	@Test
	@Verifies(value = "should update encounter successfully", method = "saveEncounter(Encounter)")
	public void saveEncounter_shouldUpdateEncounterSuccessfully() throws Exception {
		EncounterService es = Context.getEncounterService();
		
		// get the encounter from the database
		Encounter encounter = es.getEncounter(1);
		
		// save the current values for comparison later
		Patient origPatient = encounter.getPatient();
		Location origLocation = encounter.getLocation();
		Date origDate = encounter.getEncounterDatetime();
		EncounterType origEncType = encounter.getEncounterType();
		
		// add values that are different than the ones in the initialData.xml
		// file
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
	 * You should be able to add an obs to an encounter, save the encounter, and have the obs
	 * automatically persisted. Added to test bug reported in ticket #827
	 * 
	 * @see {@link EncounterService#saveEncounter(Encounter)}
	 */
	@Test
	@Verifies(value = "should cascade save to contained obs", method = "saveEncounter(Encounter)")
	public void saveEncounter_shouldCascadeSaveToContainedObs() throws Exception {
		EncounterService es = Context.getEncounterService();
		
		// First, create a new Encounter
		Encounter enc = new Encounter();
		enc.setLocation(new Location(1));
		enc.setEncounterType(new EncounterType(1));
		enc.setEncounterDatetime(new Date());
		enc.setPatient(new Patient(3));
		enc.setProvider(new Person(1));
		es.saveEncounter(enc);
		
		// Now add an obs to it
		Obs newObs = new Obs();
		newObs.setConcept(new Concept(1));
		newObs.setValueNumeric(50d);
		
		enc.addObs(newObs);
		es.saveEncounter(enc);
		
		assertNotNull(newObs.getObsId());
	}
	
	/**
	 * When you save the encounter with a changed location, the location change should be cascaded
	 * to all the obs associated with the encounter that had the same location as the encounter.
	 * 
	 * @see {@link EncounterService#saveEncounter(Encounter)}
	 */
	@Test
	@Verifies(value = "should cascade location change in encounter to contained obs", method = "saveEncounter(Encounter)")
	public void saveEncounter_shouldCascadeChangeOfLocationInEncounterToContainedObs() throws Exception {
		EncounterService es = Context.getEncounterService();
		
		// First, create a new Encounter
		Encounter enc = new Encounter();
		enc.setLocation(new Location(1));
		enc.setEncounterType(new EncounterType(1));
		enc.setEncounterDatetime(new Date());
		enc.setPatient(new Patient(3));
		enc.setProvider(new Person(1));
		es.saveEncounter(enc);
		
		// Now add an obs to it
		Obs newObs = new Obs();
		newObs.setConcept(new Concept(1));
		newObs.setValueNumeric(50d);
		newObs.setLocation(new Location(1));
		
		enc.addObs(newObs);
		es.saveEncounter(enc);
		
		enc.setLocation(new Location(2));
		es.saveEncounter(enc);
		assertEquals(enc.getLocation(), newObs.getLocation());
	}
	
	/**
	 * When you save the encounter with a changed location, the location change should not be
	 * cascaded to all the obs associated with the encounter that had a different location from
	 * encounter's location.
	 * 
	 * @see {@link EncounterService#saveEncounter(Encounter)}
	 */
	@Test
	@Verifies(value = "should not cascade location change in encounter to contained obs if the initial location of the encounter and the obs are different", method = "saveEncounter(Encounter)")
	public void saveEncounter_shouldNotCascadeLocationChangeForDifferentInitialLocations() throws Exception {
		EncounterService es = Context.getEncounterService();
		
		// First, create a new Encounter
		Encounter enc = new Encounter();
		enc.setLocation(new Location(1));
		enc.setEncounterType(new EncounterType(1));
		enc.setEncounterDatetime(new Date());
		enc.setPatient(new Patient(3));
		enc.setProvider(new Person(1));
		es.saveEncounter(enc);
		
		// Now add an obs to it
		Obs newObs = new Obs();
		newObs.setConcept(new Concept(1));
		newObs.setValueNumeric(50d);
		newObs.setLocation(new Location(2));
		
		enc.addObs(newObs);
		es.saveEncounter(enc);
		
		enc.setLocation(new Location(2));
		es.saveEncounter(enc);
		assertNotSame(enc.getLocation(), newObs.getLocation());
	}
	
	/**
	 * Make sure that purging an encounter removes the row from the database
	 * 
	 * @see {@link EncounterService#purgeEncounter(Encounter)}
	 */
	@Test
	@Verifies(value = "should purgeEncounter", method = "purgeEncounter(Encounter)")
	public void purgeEncounter_shouldPurgeEncounter() throws Exception {
		
		EncounterService es = Context.getEncounterService();
		
		// fetch the encounter to delete from the db
		Encounter encounterToDelete = es.getEncounter(1);
		
		es.purgeEncounter(encounterToDelete);
		
		// try to refetch the encounter. should get a null object
		Encounter e = es.getEncounter(encounterToDelete.getEncounterId());
		assertNull("We shouldn't find the encounter after deletion", e);
	}
	
	/**
	 * Make sure that purging an encounter removes the row from the database
	 * 
	 * @see {@link EncounterService#purgeEncounter(Encounter,null)}
	 */
	@Test
	@Verifies(value = "should cascade purge to obs and orders", method = "purgeEncounter(Encounter,null)")
	public void purgeEncounter_shouldCascadePurgeToObsAndOrders() throws Exception {
		
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
	 * You should be able to add an obs to an encounter, save the encounter, and have the obs
	 * automatically persisted.
	 * 
	 * @see {@link EncounterService#saveEncounter(Encounter)}
	 */
	@Test
	@Verifies(value = "should cascade save to contained obs when encounter already exists", method = "saveEncounter(Encounter)")
	public void saveEncounter_shouldCascadeSaveToContainedObsWhenEncounterAlreadyExists() throws Exception {
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
		
		// there should not be an obs id before saving the encounter
		assertNull(obs.getObsId());
		
		es.saveEncounter(encounter);
		
		// the obs id should have been populated during the save
		assertNotNull(obs.getObsId());
	}
	
	/**
	 * You should be able to add an obs without an obsDatetime to an encounter, save the encounter,
	 * and have the obs automatically persisted with the same date as the encounter. Added to test
	 * bug reported in {@link Ticket#827}
	 * 
	 * @see {@link EncounterService#saveEncounter(Encounter)}
	 */
	@Test
	@Verifies(value = "should cascade encounter datetime to obs", method = "saveEncounter(Encounter)")
	public void saveEncounter_shouldCascadeEncounterDatetimeToObs() throws Exception {
		EncounterService es = Context.getEncounterService();
		
		// get an encounter from the database
		Encounter encounter = es.getEncounter(1);
		assertNotNull(encounter.getEncounterDatetime());
		
		// Now add an obs to it and do NOT set the obs datetime
		Obs obs = new Obs();
		obs.setConcept(new Concept(1));
		obs.setValueNumeric(50d);
		encounter.addObs(obs);
		
		es.saveEncounter(encounter);
		
		assertTrue(encounter.getEncounterDatetime().equals(obs.getObsDatetime()));
	}
	
	/**
	 * When the date on an encounter is modified and then saved, the encounterservice changes all of
	 * the obsdatetimes to the new datetime. This test is showing error
	 * http://dev.openmrs.org/ticket/934
	 * 
	 * @see {@link EncounterService#saveEncounter(Encounter)}
	 */
	@Test
	@Verifies(value = "should only cascade the obsdatetimes to obs with different initial obsdatetimes", method = "saveEncounter(Encounter)")
	public void saveEncounter_shouldOnlyCascadeTheObsdatetimesToObsWithDifferentInitialObsdatetimes() throws Exception {
		EncounterService es = Context.getEncounterService();
		Encounter enc = es.getEncounter(1);
		Date newDate = new Date();
		enc.setEncounterDatetime(newDate);
		
		// save the encounter. The obs should pick up the encounter's date
		es.saveEncounter(enc);
		
		boolean foundObs3 = false;
		for (Obs obs : enc.getAllObs()) {
			if (obs.getObsId().equals(3)) {
				// make sure different obs datetimes from the encounter datetime
				// are not edited to the new time
				assertNotSame(newDate, obs.getObsDatetime());
				foundObs3 = true;
			} else
				// make sure all obs were changed
				assertEquals(newDate, obs.getObsDatetime());
		}
		assertTrue(foundObs3);
	}
	
	/**
	 * @see {@link EncounterService#saveEncounter(Encounter)}
	 */
	@Test
	@Verifies(value = "should not overwrite creator if non null", method = "saveEncounter(Encounter)")
	public void saveEncounter_shouldNotOverwriteCreatorIfNonNull() throws Exception {
		EncounterService encounterService = Context.getEncounterService();
		
		// the encounter to save with a non null creator
		Encounter encounter = new Encounter();
		encounter.setLocation(new Location(1));
		encounter.setEncounterType(new EncounterType(1));
		encounter.setEncounterDatetime(new Date());
		encounter.setPatient(new Patient(2));
		encounter.setCreator(new User(4));
		
		// make sure the logged in user isn't the user we're testing with
		assertNotSame(encounter.getCreator(), Context.getAuthenticatedUser());
		
		encounterService.saveEncounter(encounter);
		
		// make sure the encounter creator is user 4 not user 1
		assertEquals(encounter.getCreator(), new User(4));
		
		// make sure we can fetch this new encounter
		// from the database and its values are the same as the passed in ones
		Encounter newEncounter = encounterService.getEncounter(encounter.getEncounterId());
		assertNotNull(newEncounter);
		assertEquals(encounter.getCreator(), new User(4));
		assertNotSame(encounter.getCreator(), Context.getAuthenticatedUser());
	}
	
	/**
	 * @see {@link EncounterService#saveEncounter(Encounter)}
	 */
	@Test
	@Verifies(value = "should not overwrite dateCreated if non null", method = "saveEncounter(Encounter)")
	public void saveEncounter_shouldNotOverwriteDateCreatedIfNonNull() throws Exception {
		EncounterService encounterService = Context.getEncounterService();
		
		// the encounter to save without a dateCreated
		Encounter encounter = new Encounter();
		encounter.setLocation(new Location(1));
		encounter.setEncounterType(new EncounterType(1));
		encounter.setEncounterDatetime(new Date());
		encounter.setPatient(new Patient(2));
		encounter.setCreator(new User(4));
		Date date = new Date(System.currentTimeMillis() - 5000); // make sure we
		// have a
		// date that
		// isn't
		// "right now"
		encounter.setDateCreated(date);
		
		encounterService.saveEncounter(encounter);
		
		// make sure the encounter creator is user 4 not user 1
		assertEquals(encounter.getCreator(), new User(4));
		assertNotSame(encounter.getCreator(), Context.getAuthenticatedUser());
		
		// make sure the encounter date created wasn't overwritten
		assertEquals(date, encounter.getDateCreated());
		
		// make sure we can fetch this new encounter
		// from the database and its values are the same as the passed in ones
		Encounter newEncounter = encounterService.getEncounter(encounter.getEncounterId());
		assertNotNull(newEncounter);
		assertEquals(date, encounter.getDateCreated());
	}
	
	/**
	 * Make sure the obs and order creator and dateCreated is preserved when passed into
	 * {@link EncounterService#saveEncounter(Encounter)}
	 * 
	 * @see {@link EncounterService#saveEncounter(Encounter)}
	 */
	@Test
	@Verifies(value = "should not overwrite obs and orders creator or dateCreated", method = "saveEncounter(Encounter)")
	public void saveEncounter_shouldNotOverwriteObsAndOrdersCreatorOrDateCreated() throws Exception {
		EncounterService encounterService = Context.getEncounterService();
		
		// the encounter to save without a dateCreated
		Encounter encounter = new Encounter();
		encounter.setLocation(new Location(1));
		encounter.setEncounterType(new EncounterType(1));
		encounter.setEncounterDatetime(new Date());
		encounter.setPatient(new Patient(2));
		Date date = new Date(System.currentTimeMillis() - 5000); // make sure we
		// have a
		// date that
		// isn't
		// "right now"
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
		
		// make sure the obs date created and creator are the same as what we
		// set
		Obs createdObs = Context.getObsService().getObs(obs.getObsId());
		assertEquals(date, createdObs.getDateCreated());
		assertEquals(creator, createdObs.getCreator());
		
		// make sure the order date created and creator are the same as what we
		// set
		Order createdOrder = Context.getOrderService().getOrder(order.getOrderId());
		assertEquals(date, createdOrder.getDateCreated());
		assertEquals(creator, createdOrder.getCreator());
	}
	
	/**
	 * @see {@link EncounterService#saveEncounter(Encounter)}
	 */
	@Test
	@Verifies(value = "should cascade creator and dateCreated to orders", method = "saveEncounter(Encounter)")
	public void saveEncounter_shouldCascadeCreatorAndDateCreatedToOrders() throws Exception {
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
		
		// make sure the order date created and creator are the same as what we
		// set
		Order createdOrder = Context.getOrderService().getOrder(order.getOrderId());
		assertNotNull(encounter.getDateCreated());
		assertNotNull(createdOrder.getDateCreated());
		assertEquals(encounter.getDateCreated(), createdOrder.getDateCreated());
		
		assertNotNull(encounter.getCreator());
		assertNotNull(createdOrder.getCreator());
		assertEquals(encounter.getCreator(), createdOrder.getCreator());
	}
	
	/**
	 * @see {@link EncounterService#getEncountersByPatient(Patient)}
	 */
	@Test
	@Verifies(value = "should not get voided encounters", method = "getEncountersByPatient(Patient)")
	public void getEncountersByPatient_shouldNotGetVoidedEncounters() throws Exception {
		EncounterService encounterService = Context.getEncounterService();
		
		List<Encounter> encounters = encounterService.getEncountersByPatient(new Patient(3));
		assertEquals(2, encounters.size());
	}
	
	/**
	 * @see {@link EncounterService#getEncountersByPatient(Patient)}
	 */
	@Test(expected = IllegalArgumentException.class)
	@Verifies(value = "should throw error when given null parameter", method = "getEncountersByPatient(Patient)")
	public void getEncountersByPatient_shouldThrowErrorWhenGivenNullParameter() throws Exception {
		Context.getEncounterService().getEncountersByPatient((Patient) null);
	}
	
	/**
	 * @see {@link EncounterService#getEncountersByPatientId(Integer)}
	 */
	@Test
	@Verifies(value = "should not get voided encounters", method = "getEncountersByPatientId(Integer)")
	public void getEncountersByPatientId_shouldNotGetVoidedEncounters() throws Exception {
		EncounterService encounterService = Context.getEncounterService();
		
		List<Encounter> encounters = encounterService.getEncountersByPatientId(3);
		assertEquals(2, encounters.size());
	}
	
	/**
	 * @see {@link EncounterService#getEncountersByPatientId(Integer)}
	 */
	@Test(expected = IllegalArgumentException.class)
	@Verifies(value = "should throw error if given a null parameter", method = "getEncountersByPatientId(Integer)")
	public void getEncountersByPatientId_shouldThrowErrorIfGivenANullParameter() throws Exception {
		Context.getEncounterService().getEncountersByPatientId(null);
	}
	
	/**
	 * @see {@link EncounterService#getEncountersByPatientIdentifier(String)}
	 */
	@Test
	@Verifies(value = "should not get voided encounters", method = "getEncountersByPatientIdentifier(String)")
	public void getEncountersByPatientIdentifier_shouldNotGetVoidedEncounters() throws Exception {
		EncounterService encounterService = Context.getEncounterService();
		
		List<Encounter> encounters = encounterService.getEncountersByPatientIdentifier("12345");
		assertEquals(2, encounters.size());
	}
	
	/**
	 * @see {@link EncounterService#getEncountersByPatientIdentifier(String)}
	 */
	@Test(expected = IllegalArgumentException.class)
	@Verifies(value = "should throw error if given null parameter", method = "getEncountersByPatientIdentifier(String)")
	public void getEncountersByPatientIdentifier_shouldThrowErrorIfGivenNullParameter() throws Exception {
		Context.getEncounterService().getEncountersByPatientIdentifier(null);
	}
	
	/**
	 * Make sure {@link EncounterService#voidEncounter(Encounter, String)} marks all the voided
	 * stuff correctly
	 * 
	 * @see {@link EncounterService#voidEncounter(Encounter,String)}
	 */
	@Test
	@Verifies(value = "should void encounter and set attributes", method = "voidEncounter(Encounter,String)")
	public void voidEncounter_shouldVoidEncounterAndSetAttributes() throws Exception {
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
	 * @see {@link EncounterService#voidEncounter(Encounter,String)}
	 */
	@Test
	@Verifies(value = "should cascade to obs", method = "voidEncounter(Encounter,String)")
	public void voidEncounter_shouldCascadeToObs() throws Exception {
		EncounterService encounterService = Context.getEncounterService();
		
		// get a nonvoided encounter that has some obs
		Encounter encounter = encounterService.getEncounter(1);
		encounterService.voidEncounter(encounter, "Just Testing");
		
		Obs obs = Context.getObsService().getObs(1);
		assertTrue(obs.isVoided());
		assertEquals("Just Testing", obs.getVoidReason());
	}
	
	/**
	 * @see {@link EncounterService#voidEncounter(Encounter,String)}
	 */
	@Test
	@Verifies(value = "should cascade to orders", method = "voidEncounter(Encounter,String)")
	public void voidEncounter_shouldCascadeToOrders() throws Exception {
		EncounterService encounterService = Context.getEncounterService();
		
		// get a nonvoided encounter that has some obs
		Encounter encounter = encounterService.getEncounter(1);
		encounterService.voidEncounter(encounter, "Just Testing");
		
		Order order = Context.getOrderService().getOrder(1);
		assertTrue(order.isVoided());
		assertEquals("Just Testing", order.getVoidReason());
	}
	
	/**
	 * @see {@link EncounterService#unvoidEncounter(Encounter)}
	 */
	@Test
	@Verifies(value = "should cascade unvoid to obs", method = "unvoidEncounter(Encounter)")
	public void unvoidEncounter_shouldCascadeUnvoidToObs() throws Exception {
		EncounterService encounterService = Context.getEncounterService();
		
		// get a voided encounter that has some voided obs
		Encounter encounter = encounterService.getEncounter(2);
		encounterService.unvoidEncounter(encounter);
		
		Obs obs = Context.getObsService().getObs(4);
		assertFalse(obs.isVoided());
		assertNull(obs.getVoidReason());
	}
	
	/**
	 * @see {@link EncounterService#unvoidEncounter(Encounter)}
	 */
	@Test
	@Verifies(value = "should cascade unvoid to orders", method = "unvoidEncounter(Encounter)")
	public void unvoidEncounter_shouldCascadeUnvoidToOrders() throws Exception {
		EncounterService encounterService = Context.getEncounterService();
		
		// get a voided encounter that has some voided obs
		Encounter encounter = encounterService.getEncounter(2);
		encounterService.unvoidEncounter(encounter);
		
		Order order = Context.getOrderService().getOrder(2);
		assertFalse(order.isVoided());
		assertNull(order.getVoidReason());
	}
	
	/**
	 * @see {@link EncounterService#voidEncounter(Encounter,String)}
	 */
	@Test(expected = IllegalArgumentException.class)
	@Verifies(value = "should throw error with null reason parameter", method = "voidEncounter(Encounter,String)")
	public void voidEncounter_shouldThrowErrorWithNullReasonParameter() throws Exception {
		EncounterService encounterService = Context.getEncounterService();
		Encounter type = encounterService.getEncounter(1);
		encounterService.voidEncounter(type, null);
	}
	
	/**
	 * @see {@link EncounterService#unvoidEncounter(Encounter)}
	 */
	@Test
	@Verifies(value = "should unvoid and unmark all attributes", method = "unvoidEncounter(Encounter)")
	public void unvoidEncounter_shouldUnvoidAndUnmarkAllAttributes() throws Exception {
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
	 * @see {@link EncounterService#getEncounters(Patient, Location, Date, Date, java.util.Collection, java.util.Collection, java.util.Collection, boolean)}
	 */
	@Test
	@Verifies(value = "should get encounters by location", method = "getEncounters(Patient,Location,Date,Date,Collection<QForm;>,Collection<QEncounterType;>,Collection<QUser;>,null)")
	public void getEncounters_shouldGetEncountersByLocation() throws Exception {
		List<Encounter> encounters = Context.getEncounterService().getEncounters(null, new Location(1), null, null, null,
		    null, null, true);
		assertEquals(5, encounters.size());
	}
	
	/**
	 * Get encounters that are after a certain date, and ensure the comparison is INCLUSIVE of the
	 * given date
	 * 
	 * @see {@link EncounterService#getEncounters(Patient, Location, Date, Date, java.util.Collection, java.util.Collection, java.util.Collection, boolean)}
	 */
	@Test
	@Verifies(value = "should get encounters on or after date", method = "getEncounters(Patient,Location,Date,Date,Collection<QForm;>,Collection<QEncounterType;>,Collection<QUser;>,null)")
	public void getEncounters_shouldGetEncountersOnOrAfterDate() throws Exception {
		// there is only one nonvoided encounter, on 2005-01-01
		DateFormat ymd = new SimpleDateFormat("yyyy-MM-dd");
		
		// test for a min date long before all dates
		List<Encounter> encounters = Context.getEncounterService().getEncounters(null, null, ymd.parse("2004-12-31"), null,
		    null, null, null, false);
		assertEquals(4, encounters.size());
		assertEquals(1, encounters.get(0).getEncounterId().intValue());
		
		// test for exact date search
		encounters = Context.getEncounterService().getEncounters(null, null, ymd.parse("2005-01-01"), null, null, null,
		    null, false);
		assertEquals(4, encounters.size());
		
		// test for one day later
		encounters = Context.getEncounterService().getEncounters(null, null, ymd.parse("2005-01-02"), null, null, null,
		    null, false);
		assertEquals(3, encounters.size());
		assertEquals(3, encounters.get(0).getEncounterId().intValue());
		assertEquals(4, encounters.get(1).getEncounterId().intValue());
		assertEquals(5, encounters.get(2).getEncounterId().intValue());
	}
	
	/**
	 * @see {@link EncounterService#getEncounters(Patient, Location, Date, Date, java.util.Collection, java.util.Collection, java.util.Collection, boolean)}
	 */
	@Test
	@Verifies(value = "should get encounters on or up to a date", method = "getEncounters(Patient,Location,Date,Date,Collection<QForm;>,Collection<QEncounterType;>,Collection<QUser;>,null)")
	public void getEncounters_shouldGetEncountersOnOrUpToADate() throws Exception {
		Date toDate = new SimpleDateFormat("yyyy-dd-MM").parse("2006-01-01");
		List<Encounter> encounters = Context.getEncounterService().getEncounters(null, null, null, toDate, null, null, null,
		    true);
		assertEquals(2, encounters.size());
		assertEquals(15, encounters.get(0).getEncounterId().intValue());
		assertEquals(1, encounters.get(1).getEncounterId().intValue());
	}
	
	/**
	 * @see {@link EncounterService#getEncounters(Patient, Location, Date, Date, java.util.Collection, java.util.Collection, java.util.Collection, boolean)}
	 */
	@Test
	@Verifies(value = "should get encounters by form", method = "getEncounters(Patient,Location,Date,Date,Collection<QForm;>,Collection<QEncounterType;>,Collection<QUser;>,null)")
	public void getEncounters_shouldGetEncountersByForm() throws Exception {
		List<Form> forms = new Vector<Form>();
		forms.add(new Form(1));
		List<Encounter> encounters = Context.getEncounterService().getEncounters(null, null, null, null, forms, null, null,
		    true);
		assertEquals(6, encounters.size());
	}
	
	/**
	 * @see {@link EncounterService#getEncounters(Patient, Location, Date, Date, java.util.Collection, java.util.Collection, java.util.Collection, boolean)}
	 */
	@Test
	@Verifies(value = "should get encounters by provider", method = "getEncounters(Patient,Location,Date,Date,Collection<QForm;>,Collection<QEncounterType;>,Collection<QUser;>,null)")
	public void getEncounters_shouldGetEncountersByProvider() throws Exception {
		List<User> providers = new ArrayList<User>();
		providers.add(new User(1));
		List<Encounter> encounters = Context.getEncounterService().getEncounters(null, null, null, null, null, null,
		    providers, true);
		assertEquals(3, encounters.size());
	}
	
	/**
	 * @see {@link EncounterService#getEncounters(Patient, Location, Date, Date, java.util.Collection, java.util.Collection, java.util.Collection, boolean)}
	 */
	@Test
	@Verifies(value = "should get encounters by type", method = "getEncounters(Patient,Location,Date,Date,Collection<QForm;>,Collection<QEncounterType;>,Collection<QUser;>,null)")
	public void getEncounters_shouldGetEncountersByType() throws Exception {
		List<EncounterType> types = new Vector<EncounterType>();
		types.add(new EncounterType(1));
		List<Encounter> encounters = Context.getEncounterService().getEncounters(null, null, null, null, null, types, null,
		    true);
		assertEquals(5, encounters.size());
	}
	
	/**
	 * @see {@link EncounterService#getEncounters(Patient, Location, Date, Date, java.util.Collection, java.util.Collection, java.util.Collection, boolean)}
	 */
	@Test
	@Verifies(value = "should exclude voided encounters", method = "getEncounters(Patient,Location,Date,Date,Collection<QForm;>,Collection<QEncounterType;>,Collection<QUser;>,null)")
	public void getEncounters_shouldExcludeVoidedEncounters() throws Exception {
		assertEquals(5, Context.getEncounterService().getEncounters(null, null, null, null, null, null, null, false).size());
	}
	
	/**
	 * @see {@link EncounterService#getEncounters(Patient, Location, Date, Date, java.util.Collection, java.util.Collection, java.util.Collection, boolean)}
	 */
	@Test
	@Verifies(value = "should include voided encounters", method = "getEncounters(Patient,Location,Date,Date,Collection<QForm;>,Collection<QEncounterType;>,Collection<QUser;>,null)")
	public void getEncounters_shouldIncludeVoidedEncounters() throws Exception {
		assertEquals(6, Context.getEncounterService().getEncounters(null, null, null, null, null, null, null, true).size());
	}
	
	/**
	 * @see {@link EncounterService#saveEncounterType(EncounterType)}
	 */
	@Test
	@Verifies(value = "should save encounter type", method = "saveEncounterType(EncounterType)")
	public void saveEncounterType_shouldSaveEncounterType() throws Exception {
		EncounterService encounterService = Context.getEncounterService();
		
		EncounterType encounterType = new EncounterType("testing", "desc");
		encounterService.saveEncounterType(encounterType);
		
		// make sure an encounter type id was created
		assertNotNull(encounterType.getEncounterTypeId());
		
		// make sure we can fetch this new encounter type
		// from the database
		EncounterType newEncounterType = encounterService.getEncounterType(encounterType.getEncounterTypeId());
		assertNotNull(newEncounterType);
	}
	
	/**
	 * @see {@link EncounterService#saveEncounterType(EncounterType)}
	 */
	@Test
	@Verifies(value = "should not overwrite creator", method = "saveEncounterType(EncounterType)")
	public void saveEncounterType_shouldNotOverwriteCreator() throws Exception {
		EncounterService encounterService = Context.getEncounterService();
		
		// the encounter to save without a dateCreated
		EncounterType encounterType = new EncounterType("testing", "desc");
		encounterType.setCreator(new User(4));
		
		// make sure the logged in user isn't the user we're testing with
		assertNotSame(encounterType.getCreator(), Context.getAuthenticatedUser());
		
		encounterService.saveEncounterType(encounterType);
		
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
	 * @see {@link EncounterService#saveEncounterType(EncounterType)}
	 */
	@Test
	@Verifies(value = "should not overwrite creator or date created", method = "saveEncounterType(EncounterType)")
	public void saveEncounterType_shouldNotOverwriteCreatorOrDateCreated() throws Exception {
		EncounterService encounterService = Context.getEncounterService();
		
		// the encounter to save without a dateCreated
		EncounterType encounterType = new EncounterType("testing", "desc");
		encounterType.setCreator(new User(4));
		Date date = new Date(System.currentTimeMillis() - 5000); // make sure we
		// have a
		// date that
		// isn't
		// "right now"
		encounterType.setDateCreated(date);
		
		// make sure the logged in user isn't the user we're testing with
		assertNotSame(encounterType.getCreator(), Context.getAuthenticatedUser());
		
		encounterService.saveEncounterType(encounterType);
		
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
	 * @see {@link EncounterService#saveEncounterType(EncounterType)}
	 */
	@Test
	@Verifies(value = "should not overwrite date created", method = "saveEncounterType(EncounterType)")
	public void saveEncounterType_shouldNotOverwriteDateCreated() throws Exception {
		EncounterService encounterService = Context.getEncounterService();
		
		// the encounter to save without a dateCreated
		EncounterType encounterType = new EncounterType();
		encounterType.setName("testing");
		encounterType.setDescription("desc");
		Date date = new Date(System.currentTimeMillis() - 5000); // make sure we
		// have a
		// date that
		// isn't
		// "right now"
		encounterType.setDateCreated(date);
		
		encounterService.saveEncounterType(encounterType);
		
		// make sure the encounter type date created wasn't overwritten
		assertEquals(date, encounterType.getDateCreated());
		
		// make sure we can fetch this new encounter type
		// from the database and its values are the same as the passed in ones
		EncounterType newEncounterType = encounterService.getEncounterType(encounterType.getEncounterTypeId());
		assertNotNull(newEncounterType);
		assertEquals(date, encounterType.getDateCreated());
	}
	
	/**
	 * There should be two encounters in the system with the name "Test Enc Type A" and one should
	 * be retired and one not. Only the non retired one should be returned here
	 * 
	 * @see {@link EncounterService#getEncounterType(String)}
	 */
	@Test
	@Verifies(value = "should not get retired types", method = "getEncounterType(String)")
	public void getEncounterType_shouldNotGetRetiredTypes() throws Exception {
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
	 * Make sure that the "Some Retired Type" type is not returned because it is retired in
	 * {@link EncounterService#getEncounterType(String)}
	 * 
	 * @see {@link EncounterService#getEncounterType(String)}
	 */
	@Test
	@Verifies(value = "should return null if only retired type found", method = "getEncounterType(String)")
	public void getEncounterType_shouldReturnNullIfOnlyRetiredTypeFound() throws Exception {
		EncounterService encounterService = Context.getEncounterService();
		
		// sanity check to make sure 'some retired type' is in the dataset
		assertTrue(encounterService.getEncounterType(4).isRetired());
		assertEquals("Some Retired Type", encounterService.getEncounterType(4).getName());
		
		// we should get a null here because this named type is retired
		EncounterType type = encounterService.getEncounterType("Some Retired Type");
		assertNull(type);
	}
	
	/**
	 * Make sure that we are matching on exact name and not partial name in
	 * 
	 * @see {@link EncounterService#getEncounterType(String)}
	 */
	@Test
	@Verifies(value = "should not get by inexact name", method = "getEncounterType(String)")
	public void getEncounterType_shouldNotGetByInexactName() throws Exception {
		EncounterService encounterService = Context.getEncounterService();
		
		// we should not get two types here, the second one is retired
		EncounterType type = encounterService.getEncounterType("Test Enc Type A");
		assertEquals(2, type.getEncounterTypeId().intValue());
		
		// we should not get any encounters here even though "Test Enc" is
		// similar
		// to a name that is in the db
		EncounterType typeByInExactName = encounterService.getEncounterType("Test Enc");
		assertNull(typeByInExactName);
	}
	
	/**
	 * Make sure that we are not throwing an error with a null parameter to
	 * 
	 * @see {@link EncounterService#getEncounterType(String)}
	 */
	@Test
	@Verifies(value = "should return null with null name parameter", method = "getEncounterType(String)")
	public void getEncounterType_shouldReturnNullWithNullNameParameter() throws Exception {
		EncounterService encounterService = Context.getEncounterService();
		
		// we should not get an error here...but silently return nothing
		EncounterType type = encounterService.getEncounterType((String) null);
		assertNull(type);
	}
	
	/**
	 * @see {@link EncounterService#getAllEncounterTypes(boolean)}
	 */
	@Test
	@Verifies(value = "should not return retired types", method = "getAllEncounterTypes(boolean)")
	public void getAllEncounterTypes_shouldNotReturnRetiredTypes() throws Exception {
		EncounterService encounterService = Context.getEncounterService();
		List<EncounterType> encounterTypes = encounterService.getAllEncounterTypes(false);
		
		// make sure we get a list
		assertNotNull(encounterTypes);
		
		// make sure we only get the two non retired encounter types
		// defined in the initialData.xml
		assertEquals(2, encounterTypes.size());
	}
	
	/**
	 * @see {@link EncounterService#getAllEncounterTypes(null)}
	 */
	@Test
	@Verifies(value = "should include retired types with true includeRetired parameter", method = "getAllEncounterTypes(null)")
	public void getAllEncounterTypes_shouldIncludeRetiredTypesWithTrueIncludeRetiredParameter() throws Exception {
		EncounterService encounterService = Context.getEncounterService();
		boolean foundRetired = false;
		List<EncounterType> types = encounterService.getAllEncounterTypes(true);
		
		// there should be five types in the database
		assertEquals(5, types.size());
		
		for (EncounterType type : types) {
			if (type.isRetired())
				foundRetired = true;
		}
		assertTrue("Retired types should be returned as well", foundRetired);
	}
	
	/**
	 * @see {@link EncounterService#findEncounterTypes(String)}
	 */
	@Test
	@Verifies(value = "should return types by partial name match", method = "findEncounterTypes(String)")
	public void findEncounterTypes_shouldReturnTypesByPartialNameMatch() throws Exception {
		EncounterService encounterService = Context.getEncounterService();
		List<EncounterType> types = encounterService.findEncounterTypes("Test Enc");
		assertEquals(3, types.size());
	}
	
	/**
	 * @see {@link EncounterService#findEncounterTypes(String)}
	 */
	@Test
	@Verifies(value = "should return types by partial case insensitive match", method = "findEncounterTypes(String)")
	public void findEncounterTypes_shouldReturnTypesByPartialCaseInsensitiveMatch() throws Exception {
		EncounterService encounterService = Context.getEncounterService();
		List<EncounterType> types = encounterService.findEncounterTypes("Test ENC");
		assertEquals(3, types.size());
	}
	
	/**
	 * @see {@link EncounterService#findEncounterTypes(String)}
	 */
	@Test
	@Verifies(value = "should include retired types in the results", method = "findEncounterTypes(String)")
	public void findEncounterTypes_shouldIncludeRetiredTypesInTheResults() throws Exception {
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
	 * No types should be returned when using a substring other than the starting substring
	 * 
	 * @see {@link EncounterService#findEncounterTypes(String)}
	 */
	@Test
	@Verifies(value = "should not partial match name on internal substrings", method = "findEncounterTypes(String)")
	public void findEncounterTypes_shouldNotPartialMatchNameOnInternalSubstrings() throws Exception {
		EncounterService encounterService = Context.getEncounterService();
		List<EncounterType> types = encounterService.findEncounterTypes("Test Enc Type");
		assertEquals(3, types.size());
		
		types = encounterService.findEncounterTypes("Enc Type");
		assertEquals(0, types.size());
	}
	
	/**
	 * @see {@link EncounterService#findEncounterTypes(String)}
	 */
	@Test
	@Verifies(value = "should return types ordered on name and nonretired first", method = "findEncounterTypes(String)")
	public void findEncounterTypes_shouldReturnTypesOrderedOnNameAndNonretiredFirst() throws Exception {
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
	 * @see {@link EncounterService#retireEncounterType(EncounterType,String)}
	 */
	@Test
	@Verifies(value = "should retire type and set attributes", method = "retireEncounterType(EncounterType,String)")
	public void retireEncounterType_shouldRetireTypeAndSetAttributes() throws Exception {
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
	 * @see {@link EncounterService#retireEncounterType(EncounterType,String)}
	 */
	@Test(expected = IllegalArgumentException.class)
	@Verifies(value = "should throw error if given null reason parameter", method = "retireEncounterType(EncounterType,String)")
	public void retireEncounterType_shouldThrowErrorIfGivenNullReasonParameter() throws Exception {
		EncounterService encounterService = Context.getEncounterService();
		EncounterType type = encounterService.getEncounterType(1);
		encounterService.retireEncounterType(type, null);
	}
	
	/**
	 * @see {@link EncounterService#unretireEncounterType(EncounterType)}
	 */
	@Test
	@Verifies(value = "should unretire type and unmark attributes", method = "unretireEncounterType(EncounterType)")
	public void unretireEncounterType_shouldUnretireTypeAndUnmarkAttributes() throws Exception {
		EncounterService encounterService = Context.getEncounterService();
		EncounterType type = encounterService.getEncounterType(3);
		// TestUtil.printOutTableContents(getConnection(), "encounter_type",
		// "encounter");
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
	 * @see {@link EncounterService#saveEncounterType(EncounterType)}
	 */
	@Test
	@Verifies(value = "should update an existing encounter type name", method = "saveEncounterType(EncounterType)")
	public void saveEncounterType_shouldUpdateAnExistingEncounterTypeName() throws Exception {
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
	 * @see {@link EncounterService#purgeEncounterType(EncounterType)}
	 */
	@Test
	@Verifies(value = "should purge type", method = "purgeEncounterType(EncounterType)")
	public void purgeEncounterType_shouldPurgeType() throws Exception {
		EncounterService encounterService = Context.getEncounterService();
		
		EncounterType encounterTypeToPurge = encounterService.getEncounterType(1);
		assertNotNull(encounterTypeToPurge);
		
		// check deletion
		encounterService.purgeEncounterType(encounterTypeToPurge);
		assertNull(encounterService.getEncounterType(1));
	}
	
	/**
	 * @see {@link EncounterService#getEncounter(Integer)}
	 */
	@Test(expected = IllegalArgumentException.class)
	@Verifies(value = "should throw error if given null parameter", method = "getEncounter(Integer)")
	public void getEncounter_shouldThrowErrorIfGivenNullParameter() throws Exception {
		Context.getEncounterService().getEncounter(null);
	}
	
	/**
	 * @see {@link EncounterService#getEncounterType(Integer)}
	 */
	@Test(expected = IllegalArgumentException.class)
	@Verifies(value = "should throw error if given null parameter", method = "getEncounterType(Integer)")
	public void getEncounterType_shouldThrowErrorIfGivenNullParameter() throws Exception {
		Context.getEncounterService().getEncounterType((Integer) null);
	}
	
	/**
	 * @see {@link EncounterService#saveEncounter(Encounter)}
	 */
	@Test
	@Verifies(value = "should cascade patient to orders in the encounter", method = "saveEncounter(Encounter)")
	public void saveEncounter_shouldCascadePatientToOrdersInTheEncounter() throws Exception {
		Encounter enc = Context.getEncounterService().getEncounter(15);
		Order existing = enc.getOrders().iterator().next();
		
		// for some reason the xml for the existing encounter has already given
		// this order a different patient than the encounter that it's contained
		// in, but let's verify that:
		Assert.assertNotSame(enc.getPatient(), existing.getPatient());
		
		Context.getEncounterService().saveEncounter(enc);
		Assert.assertEquals(enc.getPatient(), existing.getPatient());
	}
	
	/**
	 * @see {@link EncounterService#getEncounterByUuid(String)}
	 */
	@Test
	@Verifies(value = "should find object given valid uuid", method = "getEncounterByUuid(String)")
	public void getEncounterByUuid_shouldFindObjectGivenValidUuid() throws Exception {
		String uuid = "6519d653-393b-4118-9c83-a3715b82d4ac";
		Encounter encounter = Context.getEncounterService().getEncounterByUuid(uuid);
		Assert.assertEquals(3, (int) encounter.getEncounterId());
	}
	
	/**
	 * @see {@link EncounterService#getEncounterByUuid(String)}
	 */
	@Test
	@Verifies(value = "should return null if no object found with given uuid", method = "getEncounterByUuid(String)")
	public void getEncounterByUuid_shouldReturnNullIfNoObjectFoundWithGivenUuid() throws Exception {
		Assert.assertNull(Context.getEncounterService().getEncounterByUuid("some invalid uuid"));
	}
	
	/**
	 * @see {@link EncounterService#getEncounterTypeByUuid(String)}
	 */
	@Test
	@Verifies(value = "should find object given valid uuid", method = "getEncounterTypeByUuid(String)")
	public void getEncounterTypeByUuid_shouldFindObjectGivenValidUuid() throws Exception {
		String uuid = "02c533ab-b74b-4ee4-b6e5-ffb6d09a0ac8";
		EncounterType encounterType = Context.getEncounterService().getEncounterTypeByUuid(uuid);
		Assert.assertEquals(6, (int) encounterType.getEncounterTypeId());
	}
	
	/**
	 * @see {@link EncounterService#getEncounterTypeByUuid(String)}
	 */
	@Test
	@Verifies(value = "should return null if no object found with given uuid", method = "getEncounterTypeByUuid(String)")
	public void getEncounterTypeByUuid_shouldReturnNullIfNoObjectFoundWithGivenUuid() throws Exception {
		Assert.assertNull(Context.getEncounterService().getEncounterTypeByUuid("some invalid uuid"));
	}
	
	/**
	 * @see {@link EncounterService#getEncountersByPatient(String,boolean)}
	 * @see {@link EncounterService#getEncountersByPatient(String)}
	 */
	@Test
	@Verifies(value = "should get all unvoided encounters for the given patient identifier", method = "getEncountersByPatient(String,boolean)")
	public void getEncountersByPatient_shouldGetAllUnvoidedEncountersForTheGivenPatientIdentifier() throws Exception {
		EncounterService encounterService = Context.getEncounterService();
		
		List<Encounter> encounters = encounterService.getEncountersByPatient("12345", false);
		assertEquals(2, encounters.size());
	}
	
	/**
	 * @see {@link EncounterService#getEncountersByPatient(String,boolean)}
	 */
	@Test
	@Verifies(value = "should get all unvoided encounters for the given patient name", method = "getEncountersByPatient(String,boolean)")
	public void getEncountersByPatient_shouldGetAllUnvoidedEncountersForTheGivenPatientName() throws Exception {
		EncounterService encounterService = Context.getEncounterService();
		
		List<Encounter> encounters = encounterService.getEncountersByPatient("John", false);
		assertEquals(2, encounters.size());
	}
	
	/**
	 * @see {@link EncounterService#getEncountersByPatient(String,boolean)}
	 */
	@Test
	@Verifies(value = "should include voided encounters in the returned list if includedVoided is true", method = "getEncountersByPatient(String,boolean)")
	public void getEncountersByPatient_shouldIncludeVoidedEncountersInTheReturnedListIfIncludedVoidedIsTrue()
	                                                                                                         throws Exception {
		EncounterService encounterService = Context.getEncounterService();
		
		List<Encounter> encounters = encounterService.getEncountersByPatient("12345", true);
		assertEquals(3, encounters.size());
	}
	
	/**
	 * @see {@link EncounterService#getEncountersByPatient(String,boolean)}
	 */
	@Test(expected = IllegalArgumentException.class)
	@Verifies(value = "should throw error if given null parameter", method = "getEncountersByPatient(String,boolean)")
	public void getEncountersByPatient_shouldThrowErrorIfGivenNullParameter() throws Exception {
		Context.getEncounterService().getEncountersByPatient(null, false);
	}
	
	/**
	 * Tests that all encounters for all patients in a cohort are returned
	 * 
	 * @see {@link EncounterService#getAllEncounters(Cohort)}
	 */
	@Test
	@Verifies(value = "should get all encounters for a cohort of patients", method = "getAllEncounters(Cohort)")
	public void getAllEncounters_shouldGetAllEncountersForACohortOfPatients() throws Exception {
		Cohort cohort = new Cohort();
		cohort.addMember(7);
		Map<Integer, List<Encounter>> allEncounters = Context.getEncounterService().getAllEncounters(cohort);
		Assert.assertEquals(1, allEncounters.size());
		Assert.assertEquals(3, allEncounters.get(7).size());
	}
	
}

/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openmrs.Cohort;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.EncounterRole;
import org.openmrs.EncounterType;
import org.openmrs.Form;
import org.openmrs.GlobalProperty;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.PersonName;
import org.openmrs.Privilege;
import org.openmrs.Provider;
import org.openmrs.Role;
import org.openmrs.TestOrder;
import org.openmrs.User;
import org.openmrs.Visit;
import org.openmrs.VisitType;
import org.openmrs.api.context.Context;
import org.openmrs.api.handler.EncounterVisitHandler;
import org.openmrs.api.handler.ExistingOrNewVisitAssignmentHandler;
import org.openmrs.api.handler.ExistingVisitAssignmentHandler;
import org.openmrs.api.handler.NoVisitAssignmentHandler;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.PrivilegeConstants;

import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests all methods in the {@link EncounterService}
 */
public class EncounterServiceTest extends BaseContextSensitiveTest {
	
	protected static final String ENC_INITIAL_DATA_XML = "org/openmrs/api/include/EncounterServiceTest-initialData.xml";
	
	protected static final String UNIQUE_ENC_WITH_PAGING_XML = "org/openmrs/api/include/EncounterServiceTest-pagingWithUniqueEncounters.xml";
	
	protected static final String TRANSFER_ENC_DATA_XML = "org/openmrs/api/include/EncounterServiceTest-transferEncounter.xml";
	
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
		Encounter encounter = buildEncounter();
		
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
		Encounter enc = buildEncounter();
		
		//add an obs to the encounter
		Obs groupObs = new Obs();
		groupObs.setConcept(new Concept(1));
		groupObs.setValueNumeric(50d);
		
		// add an obs to the group
		Obs childObs = new Obs();
		childObs.setConcept(new Concept(1));
		childObs.setValueNumeric(50d);
		groupObs.addGroupMember(childObs);
		enc.addObs(groupObs);
		
		//confirm that save and new enc id are cascaded to obs groupMembers
		//even though childObs aren't directly associated to enc
		assertNotNull("save succeeds without error", es.saveEncounter(enc));
		assertTrue("enc save succeeds", enc.getId() > 0);
		
		assertNotNull("obs save succeeds", groupObs.getObsId());
		assertEquals("encounter id propogated", groupObs.getEncounter().getId(), enc.getId());
		assertEquals("encounter time propogated", groupObs.getObsDatetime(), enc.getEncounterDatetime());
		assertNotNull("obs save succeeds", childObs.getObsId());
		assertEquals("encounter id propogated", childObs.getEncounter().getId(), enc.getId());
		assertEquals("encounter time propogated", childObs.getObsDatetime(), enc.getEncounterDatetime());
		
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
		Encounter enc = buildEncounter();
		
		es.saveEncounter(enc);
		
		// Now add an obs to it
		Obs newObs = new Obs();
		newObs.setConcept(new Concept(1));
		newObs.setValueNumeric(50d);
		Location location = new Location(1);
		newObs.setLocation(location);
		
		enc.addObs(newObs);
		es.saveEncounter(enc);
		
		enc.setLocation(location);
		es.saveEncounter(enc);
		assertEquals(enc.getLocation(), newObs.getLocation());
	}
	
	private Encounter buildEncounter() {
		// First, create a new Encounter
		Encounter enc = new Encounter();
		enc.setLocation(Context.getLocationService().getLocation(1));
		enc.setEncounterType(Context.getEncounterService().getEncounterType(1));
		enc.setEncounterDatetime(new Date());
		enc.setPatient(Context.getPatientService().getPatient(3));
		enc.addProvider(Context.getEncounterService().getEncounterRole(1), Context.getProviderService().getProvider(1));
		return enc;
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
		Encounter enc = buildEncounter();
		es.saveEncounter(enc);
		
		// Now add an obs to it
		Obs newObs = buildObs();
		
		enc.addObs(newObs);
		es.saveEncounter(enc);
		
		enc.setLocation(new Location(2));
		es.saveEncounter(enc);
		assertNotSame(enc.getLocation(), newObs.getLocation());
	}
	
	private Obs buildObs() {
		Obs newObs = new Obs();
		newObs.setConcept(new Concept(1));
		newObs.setValueNumeric(50d);
		newObs.setLocation(new Location(2));
		return newObs;
	}
	
	/**
	 * @see {@link EncounterService#unvoidEncounter(Encounter)}
	 */
	@Test
	@Verifies(value = "should set date stopped on the original after adding revise order", method = "saveEncounter(Encounter)")
	public void saveEncounter_shouldSetDateStoppedOnTheOriginalAfterAddingReviseOrder() throws Exception {
		EncounterService es = Context.getEncounterService();
		
		TestOrder order = (TestOrder) Context.getOrderService().getOrder(7);
		Assert.assertNull(order.getDateStopped());
		
		Encounter encounter = es.getEncounter(6);
		TestOrder reviseOrder = order.cloneForRevision();
		reviseOrder.setOrderer(Context.getProviderService().getProvider(1));
		
		encounter.addOrder(reviseOrder);
		es.saveEncounter(encounter);
		
		Context.flushSession();
		Context.clearSession();
		
		Date dateStopped = Context.getOrderService().getOrder(7).getDateStopped();
		Assert.assertNotNull(dateStopped);
	}
	
	/**
	 * @see {@link EncounterService#unvoidEncounter(Encounter)}
	 */
	@Test
	@Verifies(value = "should not cascade to existing order", method = "saveEncounter(Encounter)")
	public void saveEncounter_shouldNotCascadeToExistingOrder() throws Exception {
		EncounterService es = Context.getEncounterService();
		
		TestOrder order = (TestOrder) Context.getOrderService().getOrder(7);
		order.setVoided(true);
		
		Encounter encounter = es.getEncounter(6);
		es.saveEncounter(encounter);
		
		String sql = "SELECT voided FROM orders WHERE order_id=7";
		Boolean voided = (Boolean) Context.getAdministrationService().executeSQL(sql, true).get(0).get(0);
		Assert.assertFalse(voided);
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
		Encounter encounter = buildEncounter();
		encounter.setCreator(new User(4));
		
		// make sure the logged in user isn't the user we're testing with
		assertNotSame(encounter.getCreator(), Context.getAuthenticatedUser());
		
		encounterService.saveEncounter(encounter);
		
		// make sure the encounter creator is user 4 not user 1
		assertEquals(4, encounter.getCreator().getId().intValue());
		
		// make sure we can fetch this new encounter
		// from the database and its values are the same as the passed in ones
		Encounter newEncounter = encounterService.getEncounter(encounter.getEncounterId());
		assertNotNull(newEncounter);
		assertEquals(4, encounter.getCreator().getId().intValue());
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
		Encounter encounter = buildEncounter();
		encounter.setCreator(new User(4));
		Date date = new Date(System.currentTimeMillis() - 5000); // make sure we
		// have a
		// date that
		// isn't
		// "right now"
		encounter.setDateCreated(date);
		
		encounterService.saveEncounter(encounter);
		
		// make sure the encounter creator is user 4 not user 1
		assertEquals(4, encounter.getCreator().getId().intValue());
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
		Encounter encounter = buildEncounter();
		Date date = new Date(System.currentTimeMillis() - 5000); // make sure we
		// have a
		// date that
		// isn't
		// "right now"
		encounter.setDateCreated(date);
		User creator = new User(1);
		encounter.setCreator(creator);
		
		ConceptService cs = Context.getConceptService();
		// create and add an obs to this encounter
		Obs obs = new Obs(new Patient(2), cs.getConcept(1), new Date(), new Location(1));
		obs.setDateCreated(date);
		obs.setCreator(creator);
		encounter.addObs(obs);
		
		OrderService os = Context.getOrderService();
		// create and add an order to this encounter
		TestOrder order = new TestOrder();
		order.setConcept(Context.getConceptService().getConcept(5497));
		order.setPatient(new Patient(2));
		order.setDateActivated(new Date());
		order.setOrderType(os.getOrderType(2));
		order.setOrderer(Context.getProviderService().getProvider(1));
		Field field = Order.class.getDeclaredField("orderNumber");
		field.setAccessible(true);
		field.set(order, "ORD-1");
		
		order.setDateCreated(date);
		order.setCreator(creator);
		
		order.setCareSetting(os.getCareSetting(1));
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
		Order createdOrder = os.getOrder(order.getOrderId());
		assertEquals(date, createdOrder.getDateCreated());
		assertEquals(creator, createdOrder.getCreator());
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
		assertEquals(6, encounters.size());
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
		assertEquals(5, encounters.size());
		assertEquals(1, encounters.get(0).getEncounterId().intValue());
		
		// test for exact date search
		encounters = Context.getEncounterService().getEncounters(null, null, ymd.parse("2005-01-01"), null, null, null,
		    null, false);
		assertEquals(5, encounters.size());
		
		// test for one day later
		encounters = Context.getEncounterService().getEncounters(null, null, ymd.parse("2005-01-02"), null, null, null,
		    null, false);
		assertEquals(4, encounters.size());
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
		assertEquals(8, encounters.size());
	}
	
	/**
	 * @see {@link EncounterService#getEncounters(Patient, Location, Date, Date, java.util.Collection, java.util.Collection, java.util.Collection, boolean)}
	 */
	@Test
	@Verifies(value = "should get encounters by provider", method = "getEncounters(Patient,Location,Date,Date,Collection<QForm;>,Collection<QEncounterType;>,Collection<QUser;>,null)")
	public void getEncounters_shouldGetEncountersByProvider() throws Exception {
		List<User> providers = new ArrayList<User>();
		providers.add(Context.getUserService().getUser(1));
		List<Encounter> encounters = Context.getEncounterService().getEncounters(null, null, null, null, null, null,
		    providers, true);
		assertEquals(2, encounters.size()); // should be encounters 15 and 16
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
		assertEquals(7, encounters.size());
	}
	
	/**
	 * @see {@link EncounterService#getEncounters(Patient, Location, Date, Date, java.util.Collection, java.util.Collection, java.util.Collection, boolean)}
	 */
	@Test
	@Verifies(value = "should exclude voided encounters", method = "getEncounters(Patient,Location,Date,Date,Collection<QForm;>,Collection<QEncounterType;>,Collection<QUser;>,null)")
	public void getEncounters_shouldExcludeVoidedEncounters() throws Exception {
		assertEquals(6, Context.getEncounterService().getEncounters(null, null, null, null, null, null, null, false).size());
	}
	
	/**
	 * @see {@link EncounterService#getEncounters(Patient, Location, Date, Date, java.util.Collection, java.util.Collection, java.util.Collection, boolean)}
	 */
	@Test
	@Verifies(value = "should include voided encounters", method = "getEncounters(Patient,Location,Date,Date,Collection<QForm;>,Collection<QEncounterType;>,Collection<QUser;>,null)")
	public void getEncounters_shouldIncludeVoidedEncounters() throws Exception {
		assertEquals(8, Context.getEncounterService().getEncounters(null, null, null, null, null, null, null, true).size());
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
		assertEquals(4, encounterType.getCreator().getId().intValue());
		assertNotSame(encounterType.getCreator(), Context.getAuthenticatedUser());
		
		// make sure we can fetch this new encounter type
		// from the database and its values are the same as the passed in ones
		EncounterType newEncounterType = encounterService.getEncounterType(encounterType.getEncounterTypeId());
		assertNotNull(newEncounterType);
		assertEquals(4, encounterType.getCreator().getId().intValue());
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
		assertNotSame(encounterType.getCreator().getId(), Context.getAuthenticatedUser().getId());
		
		// make sure the encounter type date created wasn't overwritten
		assertEquals(date, encounterType.getDateCreated());
		
		// make sure we can fetch this new encounter type
		// from the database and its values are the same as the passed in ones
		EncounterType newEncounterType = encounterService.getEncounterType(encounterType.getEncounterTypeId());
		assertNotNull(newEncounterType);
		assertEquals(4, encounterType.getCreator().getId().intValue());
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
	 * @see {@link EncounterService#getEncounterType(String)}
	 */
	@Test
	@Verifies(value = "should not get retired types", method = "getEncounterType(String)")
	public void getEncounterType_shouldNotGetRetiredTypes() throws Exception {
		EncounterService encounterService = Context.getEncounterService();
		
		// loop over all types to make sure
		// that the retired "Test Enc Type C" exists
		boolean foundRetired = false;
		for (EncounterType encType : encounterService.getAllEncounterTypes(true)) {
			if (encType.getName().equals("Test Enc Type C") && encType.isRetired()) {
				foundRetired = true;
			}
		}
		assertTrue(foundRetired);
		
		assertNull(encounterService.getEncounterType("Test Enc Type C"));
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
		assertEquals(1, types.get(1).getEncounterTypeId().intValue());
		assertEquals(3, types.get(2).getEncounterTypeId().intValue());
		
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
		
		EncounterType encounterTypeToPurge = encounterService.getEncounterType(4);
		assertNotNull(encounterTypeToPurge);
		
		// check deletion
		encounterService.purgeEncounterType(encounterTypeToPurge);
		assertNull(encounterService.getEncounterType(4));
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
		Assert.assertNotSame(enc.getPatient().getId(), existing.getPatient().getId());
		
		Context.getEncounterService().saveEncounter(enc);
		Assert.assertEquals(enc.getPatient().getId(), existing.getPatient().getId());
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
		assertEquals(3, encounters.size());
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
		assertEquals(4, encounters.size());
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
	
	/**
	 * @see {@link EncounterService#getEncounters(Patient, Location, Date, Date, java.util.Collection, java.util.Collection, java.util.Collection, java.util.Collection, java.util.Collection, boolean)}
	 */
	@Test
	@Verifies(value = "should get encounters by visit", method = "getEncounters(Patient,Location,Date,Date,Collection<QForm;>,Collection<QEncounterType;>,Collection<QVisitType;>,Collection<QVisit;>,Collection<QUser;>,null)")
	public void getEncounters_shouldGetEncountersByVisit() throws Exception {
		List<Visit> visits = new ArrayList<Visit>();
		visits.add(new Visit(1));
		List<Encounter> encounters = Context.getEncounterService().getEncounters(null, null, null, null, null, null, null,
		    null, visits, true);
		assertEquals(2, encounters.size());
	}
	
	/**
	 * @see {@link EncounterService#getEncounters(Patient, Location, Date, Date, java.util.Collection, java.util.Collection, java.util.Collection, java.util.Collection, java.util.Collection, boolean)}
	 */
	@Test
	@Verifies(value = "should get encounters by visit type", method = "getEncounters(Patient,Location,Date,Date,Collection<QForm;>,Collection<QEncounterType;>,Collection<QVisitType;>,Collection<QVisit;>,Collection<QUser;>,null)")
	public void getEncounters_shouldGetEncountersByVisitType() throws Exception {
		List<VisitType> visitTypes = new Vector<VisitType>();
		visitTypes.add(new VisitType(2));
		List<Encounter> encounters = Context.getEncounterService().getEncounters(null, null, null, null, null, null, null,
		    visitTypes, null, true);
		assertEquals(2, encounters.size());
	}
	
	/**
	 * @see EncounterService#getEncountersByVisit(Visit, boolean)
	 * @verifies get active encounters by visit
	 */
	@Test
	public void getEncountersByVisit_shouldGetActiveEncountersByVisit() throws Exception {
		List<Encounter> encounters = Context.getEncounterService().getEncountersByVisit(new Visit(1), false);
		assertEquals(1, encounters.size());
	}
	
	/**
	 * @see EncounterService#getEncountersByVisit(Visit, boolean)
	 * @verifies include voided encounters when includeVoided is true
	 */
	@Test
	public void getEncountersByVisit_shouldIncludeVoidedEncountersWhenIncludeVoidedIsTrue() throws Exception {
		List<Encounter> encounters = Context.getEncounterService().getEncountersByVisit(new Visit(1), true);
		assertEquals(2, encounters.size());
	}
	
	/**
	 * @see {@link EncounterService#getCountOfEncounters(String,null)}
	 */
	@Test
	@Verifies(value = "should get the correct count of unique encounters", method = "getCountOfEncounters(String,null)")
	public void getCountOfEncounters_shouldGetTheCorrectCountOfUniqueEncounters() throws Exception {
		executeDataSet(UNIQUE_ENC_WITH_PAGING_XML);
		Assert.assertEquals(4, Context.getEncounterService().getCountOfEncounters("qwerty", true).intValue());
	}
	
	/**
	 * TODO see ticket https://tickets.openmrs.org/browse/TRUNK-1956 to fix this test
	 * 
	 * @see {@link EncounterService#getEncounters(String,Integer,Integer,null,null)}
	 */
	@Test
	@Ignore
	@Verifies(value = "should get all the unique encounters that match the specified parameter values", method = "getEncounters(String,Integer,Integer,null,null)")
	public void getEncounters_shouldGetAllTheUniqueEncountersThatMatchTheSpecifiedParameterValues() throws Exception {
		executeDataSet(UNIQUE_ENC_WITH_PAGING_XML);
		List<Encounter> encs = Context.getEncounterService().getEncounters("qwerty", 0, 4, true);
		Assert.assertEquals(4, encs.size());
	}
	
	/**
	 * TODO see ticket https://tickets.openmrs.org/browse/TRUNK-1956 to fix this test
	 * 
	 * @see {@link EncounterService#getEncounters(String,Integer,Integer,null,null)}
	 */
	@Test
	@Ignore
	@Verifies(value = "should not return voided encounters if includeVoided is set to true", method = "getEncounters(String,Integer,Integer,null,null)")
	public void getEncounters_shouldNotReturnVoidedEncountersIfIncludeVoidedIsSetToTrue() throws Exception {
		executeDataSet(UNIQUE_ENC_WITH_PAGING_XML);
		List<Encounter> encs = Context.getEncounterService().getEncounters("qwerty", 0, 3, false);
		Assert.assertEquals(3, encs.size());
	}
	
	/**
	 * @see org.openmrs.api.EncounterService#saveEncounterRole(org.openmrs.EncounterRole)
	 */
	@Test
	@Verifies(value = "should save encounter role with basic details", method = "saveEncounterRole(EncounterRole)")
	public void saveEncounterRole_shouldSaveEncounterRoleWithBasicDetails() throws Exception {
		EncounterRole encounterRole = new EncounterRole();
		encounterRole.setName("Attending physician");
		encounterRole.setDescription("The person in charge");
		EncounterService encounterService = Context.getEncounterService();
		encounterService.saveEncounterRole(encounterRole);
		
		assertNotNull("The saved encounter role should have an encounter role id now", encounterRole.getEncounterRoleId());
		EncounterRole newSavedEncounterRole = encounterService.getEncounterRole(encounterRole.getEncounterRoleId());
		assertNotNull("We should get back an encounter role", newSavedEncounterRole);
		assertEquals(encounterRole, newSavedEncounterRole);
		assertTrue("The created encounter role needs to equal the pojo encounter role", encounterRole
		        .equals(newSavedEncounterRole));
		
	}
	
	/**
	 * Make sure that purging an encounter removes the row from the database
	 * 
	 * @see {@link EncounterService#purgeEncounterRole(org.openmrs.EncounterRole)}
	 */
	@Test
	@Verifies(value = "should purge Encounter Role", method = "purgeEncounterRole(EncounterRole)")
	public void purgeEncounterRole_shouldPurgeEncounterRole() throws Exception {
		EncounterService encounterService = Context.getEncounterService();
		EncounterRole encounterRole = encounterService.getEncounterRole(1);
		encounterService.purgeEncounterRole(encounterRole);
		EncounterRole fetchedEncounterRole = encounterService.getEncounterRole(encounterRole.getEncounterRoleId());
		assertNull("We shouldn't find the encounter after deletion", fetchedEncounterRole);
	}
	
	/**
	 * @see {@link EncounterService#getAllEncounterRoles(boolean)}
	 */
	@Test
	@Verifies(value = "get all encounter roles based on include retired flag", method = "getAllEncounterRoles(boolean)")
	public void getAllEncounterRoles_shouldGetAllEncounterRolesBasedOnIncludeRetiredFlag() throws Exception {
		EncounterService encounterService = Context.getEncounterService();
		List<EncounterRole> encounterRoles = encounterService.getAllEncounterRoles(true);
		assertEquals("get all encounter roles including retired", 3, encounterRoles.size());
		encounterRoles = encounterService.getAllEncounterRoles(false);
		assertEquals("get all encounter roles excluding retired", 2, encounterRoles.size());
	}
	
	/**
	 * @see {@link EncounterService#getEncounterRoleByUuid(String)}
	 */
	@Test
	@Verifies(value = "find encounter role based on uuid", method = "getEncounterRoleByUuid(String)")
	public void getEncounterRoleByUuid_shouldFindEncounterRoleBasedOnUuid() throws Exception {
		EncounterService encounterService = Context.getEncounterService();
		EncounterRole encounterRole = encounterService.getEncounterRoleByUuid("430bbb70-6a9c-4e1e-badb-9d1054b1b5e9");
		assertNotNull("valid uuid should be returned", encounterRole);
		encounterRole = encounterService.getEncounterRoleByUuid("invaid uuid");
		assertNull("returns null for invalid uuid", encounterRole);
	}
	
	/**
	 * @see {@link EncounterService#getEncounterRoleByName(String)}
	 */
	@Test
	@Verifies(value = "find encounter role based on its name", method = "getEncounterRoleByName(String)")
	public void getEncounterRoleByName_shouldFindEncounterRoleByName() throws Exception {
		EncounterService encounterService = Context.getEncounterService();
		EncounterRole encounterRole = new EncounterRole();
		String name = "surgeon role";
		encounterRole.setDescription("The surgeon");
		encounterRole.setName(name);
		encounterRole = encounterService.saveEncounterRole(encounterRole);
		
		EncounterRole retrievedEncounterRole = encounterService.getEncounterRoleByName(name);
		assertNotNull("valid EncounterRole object should be returned", retrievedEncounterRole);
		assertEquals(encounterRole.getUuid(), retrievedEncounterRole.getUuid());
		
	}
	
	/**
	 * @see {@link EncounterService#retireEncounterRole(org.openmrs.EncounterRole, String)}
	 */
	@Test
	@Verifies(value = "should retire type and set attributes", method = "retireEncounterRole(EncounterRole,String)")
	public void retireEncounterRole_shouldRetireTypeAndSetAttributes() throws Exception {
		EncounterService encounterService = Context.getEncounterService();
		EncounterRole encounterRole = encounterService.getEncounterRole(1);
		assertFalse(encounterRole.isRetired());
		assertNull(encounterRole.getRetiredBy());
		assertNull(encounterRole.getRetireReason());
		assertNull(encounterRole.getDateRetired());
		EncounterRole retiredEncounterRole = encounterService.retireEncounterRole(encounterRole, "Just Testing");
		
		assertEquals(retiredEncounterRole, encounterRole);
		assertTrue(retiredEncounterRole.isRetired());
		assertNotNull(retiredEncounterRole.getDateRetired());
		assertEquals(Context.getAuthenticatedUser(), retiredEncounterRole.getRetiredBy());
		assertEquals("Just Testing", retiredEncounterRole.getRetireReason());
	}
	
	/**
	 * @see {@link EncounterService#retireEncounterRole(org.openmrs.EncounterRole, String)}
	 */
	@Test(expected = IllegalArgumentException.class)
	@Verifies(value = "should throw error if given null reason parameter", method = "retireEncounterRole(EncounterRole,String)")
	public void retireEncounterRole_shouldThrowErrorIfGivenNullReasonParameter() throws Exception {
		EncounterService encounterService = Context.getEncounterService();
		EncounterRole encounterRole = encounterService.getEncounterRole(1);
		encounterService.retireEncounterRole(encounterRole, null);
	}
	
	/**
	 * @see {@link EncounterService#unretireEncounterRole(org.openmrs.EncounterRole)}
	 */
	@Test
	@Verifies(value = "should unretire type and unmark attributes", method = "unretireEncounterRole(EncounterRole)")
	public void unretireEncounterRole_shouldUnretireTypeAndUnmarkAttributes() throws Exception {
		EncounterService encounterService = Context.getEncounterService();
		EncounterRole encounterRole = encounterService.getEncounterRole(2);
		assertTrue(encounterRole.isRetired());
		assertNotNull(encounterRole.getRetiredBy());
		assertNotNull(encounterRole.getRetireReason());
		assertNotNull(encounterRole.getDateRetired());
		EncounterRole unretiredEncounterRole = encounterService.unretireEncounterRole(encounterRole);
		
		assertEquals(unretiredEncounterRole, encounterRole);
		assertFalse(unretiredEncounterRole.isRetired());
		assertNull(unretiredEncounterRole.getDateRetired());
		assertNull(unretiredEncounterRole.getRetiredBy());
		assertNull(unretiredEncounterRole.getRetireReason());
	}
	
	/**
	 * @see EncounterService#saveEncounter(Encounter)
	 * @verifies cascade delete encounter providers
	 */
	@Test
	public void saveEncounter_shouldCascadeDeleteEncounterProviders() throws Exception {
		//given
		Encounter encounter = new Encounter();
		encounter.setLocation(new Location(1));
		encounter.setEncounterType(new EncounterType(1));
		encounter.setEncounterDatetime(new Date());
		encounter.setPatient(new Patient(3));
		
		EncounterRole role = new EncounterRole();
		role.setName("role");
		role = Context.getEncounterService().saveEncounterRole(role);
		
		Provider provider = new Provider();
		provider.setIdentifier("id1");
		provider.setPerson(newPerson("name"));
		provider = Context.getProviderService().saveProvider(provider);
		
		Provider provider2 = new Provider();
		provider2.setIdentifier("id2");
		provider2.setPerson(newPerson("name2"));
		provider2 = Context.getProviderService().saveProvider(provider2);
		
		encounter.addProvider(role, provider);
		encounter.addProvider(role, provider2);
		
		EncounterService es = Context.getEncounterService();
		es.saveEncounter(encounter);
		Context.flushSession();
		Context.clearSession();
		
		//when
		encounter = Context.getEncounterService().getEncounter(encounter.getEncounterId());
		encounter.setProvider(role, provider);
		es.saveEncounter(encounter);
		Context.flushSession();
		Context.clearSession();
		
		//then
		encounter = Context.getEncounterService().getEncounter(encounter.getEncounterId());
		Assert.assertEquals(1, encounter.getProvidersByRole(role).size());
		Assert.assertTrue("Role", encounter.getProvidersByRole(role).contains(provider));
	}
	
	/**
	 * @see EncounterService#saveEncounter(Encounter)
	 * @verifies cascade save encounter providers
	 */
	@Test
	public void saveEncounter_shouldCascadeSaveEncounterProviders() throws Exception {
		//given
		Encounter encounter = new Encounter();
		encounter.setLocation(new Location(1));
		encounter.setEncounterType(new EncounterType(1));
		encounter.setEncounterDatetime(new Date());
		encounter.setPatient(new Patient(3));
		
		EncounterRole role = new EncounterRole();
		role.setName("role");
		role = Context.getEncounterService().saveEncounterRole(role);
		
		EncounterRole role2 = new EncounterRole();
		role2.setName("role2");
		role2 = Context.getEncounterService().saveEncounterRole(role2);
		
		Provider provider = new Provider();
		provider.setIdentifier("id1");
		provider.setPerson(newPerson("name1"));
		provider = Context.getProviderService().saveProvider(provider);
		
		Provider provider2 = new Provider();
		provider2.setIdentifier("id2");
		
		provider2.setPerson(newPerson("name2"));
		provider2 = Context.getProviderService().saveProvider(provider2);
		
		encounter.addProvider(role, provider);
		encounter.addProvider(role, provider2);
		encounter.addProvider(role2, provider2);
		
		//when
		EncounterService es = Context.getEncounterService();
		es.saveEncounter(encounter);
		Context.flushSession();
		Context.clearSession();
		
		//then
		encounter = Context.getEncounterService().getEncounter(encounter.getEncounterId());
		Assert.assertEquals(2, encounter.getProvidersByRole(role).size());
		Assert.assertTrue("Role", encounter.getProvidersByRole(role).containsAll(Arrays.asList(provider, provider2)));
		Assert.assertEquals(1, encounter.getProvidersByRole(role2).size());
		Assert.assertTrue("Role2", encounter.getProvidersByRole(role2).contains(provider2));
	}
	
	/**
	 * @see EncounterService#getEncounters(String,Integer,Integer,boolean)
	 * @verifies return empty list for empty query
	 */
	@Test
	public void getEncounters_shouldReturnEmptyListForEmptyQuery() throws Exception {
		//given
		
		//when
		List<Encounter> encounters = Context.getEncounterService().getEncounters("", null, null, true);
		
		//then
		Assert.assertTrue(encounters.isEmpty());
	}
	
	/**
	 * @see EncounterService#getVisitAssignmentHandlers()
	 * @verifies return the no assignment handler
	 */
	@Test
	public void getVisitAssignmentHandlers_shouldReturnTheNoAssignmentHandler() throws Exception {
		
		List<EncounterVisitHandler> handlers = Context.getEncounterService().getEncounterVisitHandlers();
		
		boolean found = false;
		for (EncounterVisitHandler handler : handlers) {
			if (handler instanceof NoVisitAssignmentHandler)
				found = true;
		}
		
		Assert.assertTrue("The basic 'no assignment' handler was not found", found);
	}
	
	/**
	 * @see EncounterService#getVisitAssignmentHandlers()
	 * @verifies return the existing visit only assignment handler
	 */
	@Test
	public void getVisitAssignmentHandlers_shouldReturnTheExistingVisitOnlyAssignmentHandler() throws Exception {
		
		List<EncounterVisitHandler> handlers = Context.getEncounterService().getEncounterVisitHandlers();
		
		boolean found = false;
		for (EncounterVisitHandler handler : handlers) {
			if (handler instanceof ExistingVisitAssignmentHandler) {
				found = true;
				break;
			}
		}
		
		Assert.assertTrue("The 'existing visit only assignment' handler was not found", found);
	}
	
	/**
	 * @see EncounterService#getVisitAssignmentHandlers()
	 * @verifies return the existing or new visit assignment handler
	 */
	@Test
	public void getVisitAssignmentHandlers_shouldReturnTheExistingOrNewVisitAssignmentHandler() throws Exception {
		
		List<EncounterVisitHandler> handlers = Context.getEncounterService().getEncounterVisitHandlers();
		
		boolean found = false;
		for (EncounterVisitHandler handler : handlers) {
			if (handler instanceof ExistingOrNewVisitAssignmentHandler) {
				found = true;
				break;
			}
		}
		
		Assert.assertTrue("The 'existing or new visit only assignment' handler was not found", found);
	}
	
	/**
	 * @see {@link EncounterService#saveEncounter(Encounter)}
	 */
	@Test
	@Verifies(value = "should not assign encounter to visit if no handler is registered", method = "saveEncounter(Encounter)")
	public void saveEncounter_shouldNotAssignEncounterToVisitIfNoHandlerIsRegistered() throws Exception {
		Encounter encounter = buildEncounter();
		
		//We should have no visit
		assertNull(encounter.getVisit());
		
		Context.getEncounterService().saveEncounter(encounter);
		
		//We should have no visit
		assertNull(encounter.getVisit());
	}
	
	/**
	 * @see {@link EncounterService#saveEncounter(Encounter)}
	 */
	@Test
	@Verifies(value = "should not assign encounter to visit if the no assign handler is registered", method = "saveEncounter(Encounter)")
	public void saveEncounter_shouldNotAssignEncounterToVisitIfTheNoAssignHandlerIsRegistered() throws Exception {
		Encounter encounter = buildEncounter();
		
		//We should have no visit
		assertNull(encounter.getVisit());
		
		GlobalProperty gp = Context.getAdministrationService().getGlobalPropertyObject(
		    OpenmrsConstants.GP_VISIT_ASSIGNMENT_HANDLER);
		gp.setPropertyValue("org.openmrs.api.handler.NoVisitAssignmentHandler");
		Context.getAdministrationService().saveGlobalProperty(gp);
		
		Context.getEncounterService().saveEncounter(encounter);
		
		//We should have no visit.
		assertNull(encounter.getVisit());
	}
	
	/**
	 * @see {@link EncounterService#saveEncounter(Encounter)}
	 */
	@Test
	@Verifies(value = "should assign encounter to visit if the assign to existing handler is registered", method = "saveEncounter(Encounter)")
	public void saveEncounter_shouldAssignEncounterToVisitIfTheAssignToExistingHandlerIsRegistered() throws Exception {
		Encounter encounter = buildEncounter();
		
		//We should have no visit
		assertNull(encounter.getVisit());
		
		GlobalProperty gp = Context.getAdministrationService().getGlobalPropertyObject(
		    OpenmrsConstants.GP_VISIT_ASSIGNMENT_HANDLER);
		gp.setPropertyValue("org.openmrs.api.handler.ExistingVisitAssignmentHandler");
		Context.getAdministrationService().saveGlobalProperty(gp);
		
		Context.getEncounterService().saveEncounter(encounter);
		
		//We should have a visit.
		assertNotNull(encounter.getVisit());
		assertNotNull(encounter.getVisit().getVisitId());
	}
	
	/**
	 * @see {@link EncounterService#saveEncounter(Encounter)}
	 */
	@Test
	@Verifies(value = "should assign encounter to visit if the assign to existing or new handler is registered", method = "saveEncounter(Encounter)")
	public void saveEncounter_shouldAssignEncounterToVisitIfTheAssignToExistingOrNewHandlerIsRegistered() throws Exception {
		Encounter encounter = new Encounter();
		encounter.setLocation(new Location(2));
		encounter.setEncounterType(new EncounterType(1));
		encounter.setEncounterDatetime(new Date());
		encounter.setPatient(new Patient(2));
		encounter.setCreator(new User(4));
		
		//We should have no visit
		assertNull(encounter.getVisit());
		
		GlobalProperty gp = Context.getAdministrationService().getGlobalPropertyObject(
		    OpenmrsConstants.GP_VISIT_ASSIGNMENT_HANDLER);
		gp.setPropertyValue("org.openmrs.api.handler.ExistingOrNewVisitAssignmentHandler");
		Context.getAdministrationService().saveGlobalProperty(gp);
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(encounter.getEncounterDatetime());
		calendar.set(Calendar.YEAR, 1900);
		encounter.setEncounterDatetime(calendar.getTime());
		
		Context.getEncounterService().saveEncounter(encounter);
		
		//We should have a visit.
		assertNotNull(encounter.getVisit());
		
		//The visit should be persisted.
		assertNotNull(encounter.getVisit().getVisitId());
	}
	
	/**
	 * @see {@link EncounterService#getEncountersNotAssignedToAnyVisit(Patient)}
	 */
	@Test
	@Verifies(value = "should return the unvoided encounters not assigned to any visit", method = "getEncountersNotAssignedToAnyVisit(Patient)")
	public void getEncountersNotAssignedToAnyVisit_shouldReturnTheUnvoidedEncountersNotAssignedToAnyVisit() throws Exception {
		executeDataSet(UNIQUE_ENC_WITH_PAGING_XML);
		List<Encounter> encs = Context.getEncounterService().getEncountersNotAssignedToAnyVisit(
		    Context.getPatientService().getPatient(10));
		Assert.assertEquals(2, encs.size());
		Assert.assertNull(encs.get(0).getVisit());
		Assert.assertNull(encs.get(1).getVisit());
	}
	
	/**
	 * @see {@link EncounterService#saveEncounter(Encounter)}
	 */
	@Test
	@Ignore("TRUNK-50")
	@Verifies(value = "should void and create new obs when saving encounter", method = "saveEncounter(Encounter)")
	public void saveEncounter_shouldVoidAndCreateNewObsWhenSavingEncounter() throws Exception {
		// create an encounter
		Encounter encounter = new Encounter();
		encounter.setLocation(new Location(1));
		encounter.setEncounterType(new EncounterType(1));
		encounter.setEncounterDatetime(new Date());
		encounter.setPatient(new Patient(3));
		
		// Now add an obs to it
		Obs obs = new Obs();
		obs.setConcept(new Concept(1));
		obs.setValueNumeric(50d);
		encounter.addObs(obs);
		
		// save the encounter
		EncounterService es = Context.getEncounterService();
		es.saveEncounter(encounter);
		
		// get the id of this obs
		int oldObsId = obs.getObsId();
		
		// now change the obs value
		obs.setValueNumeric(100d);
		
		// resave the encounters
		es.saveEncounter(encounter);
		
		// get the new obs id
		int newObsId = encounter.getAllObs().iterator().next().getId();
		
		Assert.assertTrue(oldObsId != newObsId);
		Assert.assertEquals(2, encounter.getAllObs(true).size());
		Assert.assertEquals(1, encounter.getAllObs().size());
	}
	
	/**
	 * @see {@link EncounterService#voidEncounter(Encounter, String)}
	 */
	@Test
	@Verifies(value = "should not void providers", method = "voidEncounter(Encounter, String)")
	public void voidEncounter_shouldNotVoidProviders() throws Exception {
		EncounterService encounterService = Context.getEncounterService();
		
		Encounter encounter = new Encounter();
		encounter.setLocation(new Location(1));
		encounter.setEncounterType(new EncounterType(1));
		encounter.setEncounterDatetime(new Date());
		encounter.setPatient(new Patient(3));
		
		EncounterRole role = new EncounterRole();
		role.setName("role");
		role = encounterService.saveEncounterRole(role);
		
		Provider provider = new Provider();
		provider.setIdentifier("id1");
		provider.setPerson(newPerson("name"));
		provider = Context.getProviderService().saveProvider(provider);
		
		encounter.addProvider(role, provider);
		encounterService.saveEncounter(encounter);
		
		assertEquals(1, encounter.getProvidersByRoles().size());
		
		encounterService.voidEncounter(encounter, "reason");
		
		encounter = encounterService.getEncounter(encounter.getEncounterId());
		assertEquals(1, encounter.getProvidersByRoles().size());
	}
	
	/**
	 * @see {@link EncounterService#filterEncountersByViewPermissions(List, User)}
	 */
	@Test
	@Verifies(value = "should filter encounters if user is not allowed to see some encounters", method = "filterEncountersByViewPermissions(List, User)")
	public void filterEncountersByViewPermissions_shouldFilterEncountersIfUserIsNotAllowedToSeeSomeEncounters()
	        throws Exception {
		EncounterService encounterService = Context.getEncounterService();
		
		int expectedSize = encounterService.getEncountersByPatientId(3).size();
		
		Encounter encounter = new Encounter();
		encounter.setLocation(new Location(1));
		encounter.setEncounterDatetime(new Date());
		encounter.setPatient(Context.getPatientService().getPatient(3));
		EncounterType encounterType = new EncounterType(1);
		encounterType.setViewPrivilege(Context.getUserService().getPrivilege("Some Privilege For View Encounter Types"));
		encounter.setEncounterType(encounterType);
		
		EncounterRole role = new EncounterRole();
		role.setName("role");
		role = encounterService.saveEncounterRole(role);
		
		Provider provider = new Provider();
		provider.setIdentifier("id1");
		
		provider.setPerson(newPerson("name"));
		provider = Context.getProviderService().saveProvider(provider);
		
		encounter.addProvider(role, provider);
		encounterService.saveEncounter(encounter);
		
		List<Encounter> patientEncounters = encounterService.getEncountersByPatientId(3);
		assertEquals(expectedSize + 1, patientEncounters.size());
		
		if (Context.isAuthenticated()) {
			Context.logout();
		}
		Context.authenticate("test_user", "test");
		Context.addProxyPrivilege(PrivilegeConstants.GET_ENCOUNTERS);
		
		patientEncounters = encounterService.getEncountersByPatientId(3);
		int actualSize = patientEncounters.size();
		
		Context.removeProxyPrivilege(PrivilegeConstants.GET_ENCOUNTERS);
		Context.logout();
		
		assertEquals(actualSize, expectedSize);
		assertTrue(!patientEncounters.contains(encounter));
	}
	
	/**
	 * @see {@link EncounterService#filterEncountersByViewPermissions(List, User)}
	 */
	@Test
	@Verifies(value = "should not filter all encounters when the encounter type's view privilege column is null", method = "filterEncountersByViewPermissions(List, User)")
	public void filterEncountersByViewPermissions_shouldNotFilterAllEncountersWhenTheEncounterTypesViewPrivilegeColumnIsNull()
	        throws Exception {
		EncounterService encounterService = Context.getEncounterService();
		
		int beforeSize = encounterService.getEncountersByPatientId(3).size();
		
		Encounter encounter = new Encounter();
		encounter.setLocation(new Location(1));
		encounter.setEncounterDatetime(new Date());
		encounter.setPatient(Context.getPatientService().getPatient(3));
		EncounterType encounterType = new EncounterType(1);
		// viewPrivilige on encounter type intentionally left null
		encounter.setEncounterType(encounterType);
		
		EncounterRole role = new EncounterRole();
		role.setName("role");
		role = encounterService.saveEncounterRole(role);
		
		Provider provider = new Provider();
		provider.setIdentifier("id1");
		provider.setPerson(newPerson("name"));
		provider = Context.getProviderService().saveProvider(provider);
		
		encounter.addProvider(role, provider);
		encounterService.saveEncounter(encounter);
		
		List<Encounter> patientEncounters = encounterService.getEncountersByPatientId(3);
		assertNotNull(patientEncounters);
		assertEquals(beforeSize + 1, patientEncounters.size());
	}
	
	/**
	 * @see {@link EncounterService#canViewAllEncounterTypes(User)}
	 */
	@Test
	@Verifies(value = "should return true if user is granted to view all encounters", method = "canViewAllEncounterTypes(User)")
	public void canViewAllEncounterTypes_shouldReturnTrueIfUserIsGrantedToViewEncounters() throws Exception {
		EncounterService encounterService = Context.getEncounterService();
		
		EncounterType encounterType = new EncounterType("testing", "desc");
		Privilege viewPrivilege = Context.getUserService().getPrivilege("Some Privilege For View Encounter Types");
		encounterType.setViewPrivilege(viewPrivilege);
		
		encounterService.saveEncounterType(encounterType);
		
		User user = Context.getUserService().getUserByUsername("test_user");
		assertNotNull(user);
		
		assertFalse(encounterService.canViewAllEncounterTypes(user));
		
		Role role = Context.getUserService().getRole("Provider");
		role.addPrivilege(viewPrivilege);
		user.addRole(role);
		
		assertTrue(encounterService.canViewAllEncounterTypes(user));
	}
	
	/**
	 * @see {@link EncounterService#canViewAllEncounterTypes(User)}
	 */
	@Test
	@Verifies(value = "should return true when the encounter type's view privilege column is null", method = "canViewAllEncounterTypes(User)")
	public void canViewAllEncounterTypes_shouldReturnTrueWhenTheEncounterTypesViewPrivilegeColumnIsNull() throws Exception {
		EncounterService encounterService = Context.getEncounterService();
		
		// set viewPrivilege on each encounter type to null
		for (EncounterType encounterType : encounterService.getAllEncounterTypes()) {
			encounterType.setViewPrivilege(null);
			encounterService.saveEncounterType(encounterType);
		}
		
		User user = Context.getUserService().getUserByUsername("test_user");
		assertNotNull(user);
		
		assertTrue(encounterService.canViewAllEncounterTypes(user));
	}
	
	/**
	 * @see {@link EncounterService#canEditAllEncounterTypes(User)}
	 */
	@Test
	@Verifies(value = "should return true if user is granted to edit all encounters", method = "canEditAllEncounterTypes(User)")
	public void canEditAllEncounterTypes_shouldReturnTrueIfUserIsGrantedToEditEncounters() throws Exception {
		EncounterService encounterService = Context.getEncounterService();
		
		EncounterType encounterType = new EncounterType("testing", "desc");
		Privilege editPrivilege = Context.getUserService().getPrivilege("Some Privilege For Edit Encounter Types");
		encounterType.setEditPrivilege(editPrivilege);
		
		encounterService.saveEncounterType(encounterType);
		
		User user = Context.getUserService().getUserByUsername("test_user");
		assertNotNull(user);
		
		assertFalse(encounterService.canEditAllEncounterTypes(user));
		
		Role role = Context.getUserService().getRole("Provider");
		role.addPrivilege(editPrivilege);
		user.addRole(role);
		
		assertTrue(encounterService.canEditAllEncounterTypes(user));
	}
	
	/**
	 * @see {@link EncounterService#canEditAllEncounterTypes(User)}
	 */
	@Test
	@Verifies(value = "should return true when the encounter type's edit privilege column is null", method = "canViewAllEncounterTypes(User)")
	public void canViewAllEncounterTypes_shouldReturnTrueWhenTheEncounterTypesEditPrivilegeColumnIsNull() throws Exception {
		EncounterService encounterService = Context.getEncounterService();
		
		// set editPrivilege on each encounter type to null
		for (EncounterType encounterType : encounterService.getAllEncounterTypes()) {
			encounterType.setEditPrivilege(null);
			encounterService.saveEncounterType(encounterType);
		}
		
		User user = Context.getUserService().getUserByUsername("test_user");
		assertNotNull(user);
		
		assertTrue(encounterService.canEditAllEncounterTypes(user));
	}
	
	/**
	 * @see {@link EncounterService#canEditEncounter(Encounter, User)}
	 */
	@Test
	@Verifies(value = "should return true if user can edit encounter", method = "canEditEncounter(Encounter, User)")
	public void canEditEncounter_shouldReturnTrueIfUserCanEditEncounter() throws Exception {
		// get encounter that has type with edit privilege set
		Encounter encounter = getEncounterWithEditPrivilege();
		
		User user = Context.getUserService().getUserByUsername("test_user");
		assertNotNull(user);
		
		// add required privilege to role in which this user is
		Role role = Context.getUserService().getRole("Provider");
		role.addPrivilege(encounter.getEncounterType().getEditPrivilege());
		user.addRole(role);
		
		assertTrue(Context.getEncounterService().canEditEncounter(encounter, user));
	}
	
	/**
	 * @see {@link EncounterService#canEditEncounter(Encounter, User)}
	 */
	@Test
	@Verifies(value = "should return false if user can not edit encounter", method = "canEditEncounter(Encounter, User)")
	public void canEditEncounter_shouldReturnFalseIfUserCanNotEditEncounter() throws Exception {
		// get encounter that has type with edit privilege set
		Encounter encounter = getEncounterWithEditPrivilege();
		
		User user = Context.getUserService().getUserByUsername("test_user");
		assertNotNull(user);
		
		// left user as is - i.e. without required privilege
		
		assertFalse(Context.getEncounterService().canEditEncounter(encounter, user));
	}
	
	/**
	 * @see {@link EncounterService#canEditEncounter(Encounter, User)}
	 */
	@Test(expected = IllegalArgumentException.class)
	@Verifies(value = "should fail if encounter is null", method = "canEditEncounter(Encounter, User)")
	public void canEditEncounter_shouldFailfIfEncounterIsNull() throws Exception {
		// invoke method using null encounter
		Context.getEncounterService().canEditEncounter(null, null);
	}
	
	/**
	 * @see {@link EncounterService#canViewEncounter(Encounter, User)}
	 */
	@Test
	@Verifies(value = "should return true if user can view encounter", method = "canViewEncounter(Encounter, User)")
	public void canViewEncounter_shouldReturnTrueIfUserCanViewEncounter() throws Exception {
		// get encounter that has type with view privilege set
		Encounter encounter = getEncounterWithViewPrivilege();
		
		User user = Context.getUserService().getUserByUsername("test_user");
		assertNotNull(user);
		
		// add required privilege to role in which this user is
		Role role = Context.getUserService().getRole("Provider");
		role.addPrivilege(encounter.getEncounterType().getViewPrivilege());
		user.addRole(role);
		
		assertTrue(Context.getEncounterService().canViewEncounter(encounter, user));
	}
	
	/**
	 * @see {@link EncounterService#canViewEncounter(Encounter, User)}
	 */
	@Test(expected = IllegalArgumentException.class)
	@Verifies(value = "should fail if encounter is null", method = "canViewEncounter(Encounter, User)")
	public void canViewEncounter_shouldFailfIfEncounterIsNull() throws Exception {
		// invoke method using null encounter
		Context.getEncounterService().canViewEncounter(null, null);
	}
	
	/**
	 * @see {@link EncounterService#canViewEncounter(Encounter, User)}
	 */
	@Test
	@Verifies(value = "should return false if user can not view encounter", method = "canViewEncounter(Encounter, User)")
	public void canViewEncounter_shouldReturnFalseIfUserCanNotViewEncounter() throws Exception {
		// get encounter that has type with view privilege set
		Encounter encounter = getEncounterWithViewPrivilege();
		
		User user = Context.getUserService().getUserByUsername("test_user");
		assertNotNull(user);
		
		// left user as is - i.e. without required privilege
		
		assertFalse(Context.getEncounterService().canViewEncounter(encounter, user));
	}
	
	/**
	 * @see {@link EncounterService#getEncounter(Integer)}
	 */
	@Test(expected = APIException.class)
	@Verifies(value = "should fail if user is not allowed to view encounter by given id", method = "getEncounter(Integer)")
	public void getEncounter_shouldFailIfUserIsNotAllowedToViewEncounterByGivenId() throws Exception {
		// get encounter that has type with view privilege set
		Encounter encounter = getEncounterWithViewPrivilege();
		
		User user = Context.getUserService().getUserByUsername("test_user");
		assertNotNull(user);
		
		// left this user as is - i.e. without required privilege
		// and authenticate under it's account
		Context.becomeUser(user.getSystemId());
		
		// have to add privilege in order to be able to call getEncounter(Integer) method
		Context.addProxyPrivilege(PrivilegeConstants.VIEW_ENCOUNTERS);
		
		assertNull(Context.getEncounterService().getEncounter(encounter.getId()));
	}
	
	/**
	 * @see {@link EncounterService#getEncounter(Integer)}
	 */
	@Test
	@Verifies(value = "should return encounter if user is allowed to view it", method = "getEncounter(Integer)")
	public void getEncounter_shouldReturnEncounterIfUserIsAllowedToViewIt() throws Exception {
		// get encounter that has type with view privilege set
		Encounter encounter = getEncounterWithViewPrivilege();
		
		User user = Context.getUserService().getUserByUsername("test_user");
		assertNotNull(user);
		
		// add required privilege to role in which this user is
		Role role = Context.getUserService().getRole("Provider");
		role.addPrivilege(encounter.getEncounterType().getViewPrivilege());
		user.addRole(role);
		
		// and authenticate under it's account
		Context.becomeUser(user.getSystemId());
		
		// have to add privilege in order to be able to call getEncounter(Integer) method
		Context.addProxyPrivilege(PrivilegeConstants.VIEW_ENCOUNTERS);
		
		assertNotNull(Context.getEncounterService().getEncounter(encounter.getId()));
	}
	
	/**
	 * @see {@link EncounterService#saveEncounter(Encounter)}
	 */
	@Test(expected = APIException.class)
	@Verifies(value = "should fail if user is not supposed to edit encounters of type of given encounter", method = "saveEncounter(Encounter)")
	public void saveEncounter_shouldFailfIfUserIsNotSupposedToEditEncountersOfTypeOfGivenEncounter() throws Exception {
		// get encounter that has type with edit privilege set
		Encounter encounter = getEncounterWithEditPrivilege();
		
		User user = Context.getUserService().getUserByUsername("test_user");
		assertNotNull(user);
		
		// left this user as is - i.e. without required privilege
		// and authenticate under it's account
		Context.becomeUser(user.getSystemId());
		
		// have to add privilege in order to be able to call saveEncounter(Encounter) method
		Context.addProxyPrivilege(PrivilegeConstants.EDIT_ENCOUNTERS);
		
		Context.getEncounterService().saveEncounter(encounter);
	}
	
	/**
	 * @see {@link EncounterService#voidEncounter(Encounter, String)}
	 */
	@Test(expected = APIException.class)
	@Verifies(value = "should fail if user is not supposed to edit encounters of type of given encounter", method = "voidEncounter(Encounter, String)")
	public void voidEncounter_shouldFailfIfUserIsNotSupposedToEditEncountersOfTypeOfGivenEncounter() throws Exception {
		// get encounter that has type with edit privilege set
		Encounter encounter = getEncounterWithEditPrivilege();
		
		User user = Context.getUserService().getUserByUsername("test_user");
		assertNotNull(user);
		
		// left this user as is - i.e. without required privilege
		// and authenticate under it's account
		Context.becomeUser(user.getSystemId());
		
		// have to add privilege in order to be able to call voidEncounter(Encounter,String) method
		Context.addProxyPrivilege(PrivilegeConstants.EDIT_ENCOUNTERS);
		
		Context.getEncounterService().voidEncounter(encounter, "test");
	}
	
	/**
	 * @see {@link EncounterService#unvoidEncounter(Encounter)}
	 */
	@Test(expected = APIException.class)
	@Verifies(value = "should fail if user is not supposed to edit encounters of type of given encounter", method = "voidEncounter(Encounter)")
	public void unvoidEncounter_shouldFailfIfUserIsNotSupposedToEditEncountersOfTypeOfGivenEncounter() throws Exception {
		// get encounter that has type with edit privilege set
		Encounter encounter = getEncounterWithEditPrivilege();
		
		User user = Context.getUserService().getUserByUsername("test_user");
		assertNotNull(user);
		
		// left this user as is - i.e. without required privilege
		// and authenticate under it's account
		Context.becomeUser(user.getSystemId());
		
		// have to add privilege in order to be able to call unvoidEncounter(Encounter) method
		Context.addProxyPrivilege(PrivilegeConstants.EDIT_ENCOUNTERS);
		
		Context.getEncounterService().unvoidEncounter(encounter);
	}
	
	/**
	 * @see {@link EncounterService#purgeEncounter(Encounter)}
	 */
	@Test(expected = APIException.class)
	@Verifies(value = "should fail if user is not supposed to edit encounters of type of given encounter", method = "purgeEncounter(Encounter)")
	public void purgeEncounter_shouldFailfIfUserIsNotSupposedToEditEncountersOfTypeOfGivenEncounter() throws Exception {
		// get encounter that has type with edit privilege set
		Encounter encounter = getEncounterWithEditPrivilege();
		
		User user = Context.getUserService().getUserByUsername("test_user");
		assertNotNull(user);
		
		// left this user as is - i.e. without required privilege
		// and authenticate under it's account
		Context.becomeUser(user.getSystemId());
		
		// have to add privilege in order to be able to call purgeEncounter(Encounter) method
		Context.addProxyPrivilege(PrivilegeConstants.PURGE_ENCOUNTERS);
		
		Context.getEncounterService().purgeEncounter(encounter);
	}
	
	/**
	 * @see {@link EncounterService#purgeEncounter(Encounter,Boolean)}
	 */
	@Test(expected = APIException.class)
	@Verifies(value = "should fail if user is not supposed to edit encounters of type of given encounter", method = "purgeEncounter(Encounter,Boolean)")
	public void purgeEncounterCascade_shouldFailfIfUserIsNotSupposedToEditEncountersOfTypeOfGivenEncounter()
	        throws Exception {
		// get encounter that has type with edit privilege set
		Encounter encounter = getEncounterWithEditPrivilege();
		
		User user = Context.getUserService().getUserByUsername("test_user");
		assertNotNull(user);
		
		// left this user as is - i.e. without required privilege
		// and authenticate under it's account
		Context.becomeUser(user.getSystemId());
		
		// have to add privilege in order to be able to call purgeEncounter(Encounter,Boolean) method
		Context.addProxyPrivilege(PrivilegeConstants.PURGE_ENCOUNTERS);
		
		Context.getEncounterService().purgeEncounter(encounter, Boolean.TRUE);
	}
	
	@Test(expected = APIException.class)
	public void getActiveEncounterVisitHandler_shouldThrowIfBeanWithGivenTypeAndNameNotFound() {
		
		String incorrectBeanName = OpenmrsConstants.REGISTERED_COMPONENT_NAME_PREFIX + "invalidName";
		
		GlobalProperty visitHandlerProperty = new GlobalProperty(OpenmrsConstants.GP_VISIT_ASSIGNMENT_HANDLER,
		        incorrectBeanName);
		
		Context.getAdministrationService().saveGlobalProperty(visitHandlerProperty);
		
		Context.getEncounterService().getActiveEncounterVisitHandler();
	}
	
	@Test
	public void getActiveEncounterVisitHandler_shouldReturnBeanHaveBeenRegisteredWithGivenName() {
		
		String correctBeanName = OpenmrsConstants.REGISTERED_COMPONENT_NAME_PREFIX + "existingOrNewVisitAssignmentHandler";
		
		GlobalProperty visitHandlerProperty = new GlobalProperty(OpenmrsConstants.GP_VISIT_ASSIGNMENT_HANDLER,
		        correctBeanName);
		
		Context.getAdministrationService().saveGlobalProperty(visitHandlerProperty);
		
		EncounterVisitHandler activeEncounterVisitHandler = Context.getEncounterService().getActiveEncounterVisitHandler();
		
		Assert.assertNotNull(activeEncounterVisitHandler);
	}
	
	/**
	 * Gets encounter and adds edit privilege to it
	 * 
	 * @return encounter with type having non null edit privilege
	 */
	private Encounter getEncounterWithEditPrivilege() {
		// create service to be used for encounter manipulations
		EncounterService encounterService = Context.getEncounterService();
		
		Encounter encounter = encounterService.getEncounter(1);
		EncounterType encounterType = encounter.getEncounterType();
		// make sure that encounter type is not null
		assertNotNull(encounterType);
		// set view privilege on this encounter type
		Privilege editPrivilege = Context.getUserService().getPrivilege("Some Privilege For Edit Encounter Types");
		encounterType.setEditPrivilege(editPrivilege);
		encounter.setEncounterType(encounterType);
		// update encounter
		encounter = encounterService.saveEncounter(encounter);
		// make sure that encounter type updated successfully
		assertNotNull(encounter);
		
		return encounter;
	}
	
	/**
	 * Gets encounter and adds view privilege to it
	 * 
	 * @return encounter with type having non null view privilege
	 */
	private Encounter getEncounterWithViewPrivilege() {
		// create service to be used for encounter manipulations
		EncounterService encounterService = Context.getEncounterService();
		
		Encounter encounter = encounterService.getEncounter(1);
		EncounterType encounterType = encounter.getEncounterType();
		// make sure that encounter type is not null
		assertNotNull(encounterType);
		// set view privilege on this encounter type
		Privilege viewPrivilege = Context.getUserService().getPrivilege("Some Privilege For View Encounter Types");
		encounterType.setViewPrivilege(viewPrivilege);
		encounter.setEncounterType(encounterType);
		// update encounter
		encounter = encounterService.saveEncounter(encounter);
		// make sure that encounter was updated successfully
		assertNotNull(encounter);
		
		return encounter;
	}
	
	/**
	 * @see {@link EncounterService#getEncounters(String,Integer,Integer,Integer,null)}
	 */
	@Test
	@Verifies(value = "should fetch encounters by patient id", method = "getEncounters(String,Integer,Integer,Integer,null)")
	public void getEncounters_shouldFetchEncountersByPatientId() throws Exception {
		Assert.assertEquals(2, Context.getEncounterService().getEncounters(null, 3, null, null, false).size());
		Assert.assertEquals(4, Context.getEncounterService().getEncounters(null, 3, null, null, true).size());
	}
	
	/**
	 * @see {@link EncounterService#getEncounters(String,Integer,Integer,Integer,null)}
	 */
	@Test
	@Verifies(value = "should match on the location name", method = "getEncounters(String,Integer,Integer,Integer,null)")
	public void getEncounters_shouldMatchOnTheLocationName() throws Exception {
		Assert.assertEquals(2, Context.getEncounterService().getEncounters("Test Location", 3, null, null, false).size());
		Assert.assertEquals(4, Context.getEncounterService().getEncounters("Test Location", 3, null, null, true).size());
	}
	
	/**
	 * @see {@link EncounterService#getEncounters(String,Integer,Integer,Integer,null)}
	 */
	@Test
	@Verifies(value = "should match on the provider name", method = "getEncounters(String,Integer,Integer,Integer,null)")
	public void getEncounters_shouldMatchOnTheProviderName() throws Exception {
		Assert.assertEquals(1, Context.getEncounterService().getEncounters("phys", 3, null, null, false).size());
	}
	
	/**
	 * @see {@link EncounterService#getEncounters(String,Integer,Integer,Integer,null)}
	 */
	@Test
	@Verifies(value = "should should match on provider identifier", method = "getEncounters(String,Integer,Integer,Integer,null)")
	public void getEncounters_shouldShouldMatchOnProviderIdentifier() throws Exception {
		Assert.assertEquals(1, Context.getEncounterService().getEncounters("2", 3, null, null, false).size());
		Assert.assertEquals(2, Context.getEncounterService().getEncounters("2", 3, null, null, true).size());
	}
	
	/**
	 * @see {@link EncounterService#getEncounters(String,Integer,Integer,Integer,null)}
	 */
	@Test
	@Verifies(value = "should match on the provider person name", method = "getEncounters(String,Integer,Integer,Integer,null)")
	public void getEncounters_shouldMatchOnTheProviderPersonName() throws Exception {
		//Should match on Super User and John3 Doe
		Assert.assertEquals(1, Context.getEncounterService().getEncounters("er jo", 3, null, null, false).size());
		Assert.assertEquals(2, Context.getEncounterService().getEncounters("er jo", 3, null, null, true).size());
		Assert.assertEquals(0, Context.getEncounterService().getEncounters("none", 3, null, null, true).size());
	}
	
	/**
	 * @see {@link EncounterService#getEncounters(String,Integer,Integer,Integer,null)}
	 */
	@Test
	@Verifies(value = "should include voided encounters if includeVoided is set to true", method = "getEncounters(String,Integer,Integer,Integer,null)")
	public void getEncounters_shouldIncludeVoidedEncountersIfIncludeVoidedIsSetToTrue() throws Exception {
		Assert.assertEquals(2, Context.getEncounterService().getEncounters("2", 3, null, null, true).size());
	}
	
	/**
	 * @see {@link EncounterService#getEncounters(String,Integer,Integer,Integer,null)}
	 */
	@Test
	@Verifies(value = "should match on the encounter type name", method = "getEncounters(String,Integer,Integer,Integer,null)")
	public void getEncounters_shouldMatchOnTheEncounterTypeName() throws Exception {
		Assert.assertEquals(2, Context.getEncounterService().getEncounters("Type B", 3, null, null, false).size());
	}
	
	/**
	 * @see {@link EncounterService#getEncounters(String,Integer,Integer,Integer,null)}
	 */
	@Test
	@Verifies(value = "should match on the form name", method = "getEncounters(String,Integer,Integer,Integer,null)")
	public void getEncounters_shouldMatchOnTheFormName() throws Exception {
		Assert.assertEquals(2, Context.getEncounterService().getEncounters("Basic", 3, null, null, false).size());
	}
	
	/**
	 * @see {@link EncounterService#saveEncounterType(EncounterType)}
	 * @see {@link EncounterService#checkIfEncounterTypesAreLocked()}
	 */
	@Test(expected = EncounterTypeLockedException.class)
	@Verifies(value = "should throw error when trying to save encounter type when encounter types are locked", method = "saveEncounterType(EncounterType)")
	public void saveEncounterType_shouldThrowErrorWhenTryingToSaveEncounterTypeWhenEncounterTypesAreLocked()
	        throws Exception {
		GlobalProperty gp = new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_ENCOUNTER_TYPES_LOCKED);
		gp.setPropertyValue("true");
		Context.getAdministrationService().saveGlobalProperty(gp);
		
		EncounterService encounterService = Context.getEncounterService();
		EncounterType encounterType = Context.getEncounterService().getEncounterType(1);
		
		Assert.assertNotNull(encounterType);
		
		encounterService.saveEncounterType(encounterType);
	}
	
	/**
	 * @see {@link EncounterService#retireEncounterType(EncounterType, String)}
	 */
	@Test(expected = EncounterTypeLockedException.class)
	@Verifies(value = "should throw error when trying to retire encounter type when encounter types are locked", method = "retireEncounterType(EncounterType, String)")
	public void retireEncounterType_shouldThrowErrorWhenTryingToRetireEncounterTypeWhenEncounterTypesAreLocked()
	        throws Exception {
		GlobalProperty gp = new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_ENCOUNTER_TYPES_LOCKED);
		gp.setPropertyValue("true");
		Context.getAdministrationService().saveGlobalProperty(gp);
		
		EncounterService encounterService = Context.getEncounterService();
		EncounterType encounterType = Context.getEncounterService().getEncounterType(1);
		Assert.assertNotNull(encounterType);
		
		encounterService.retireEncounterType(encounterType, "reason");
	}
	
	/**
	 * @see {@link EncounterService#unretireEncounterType(EncounterType)}
	 */
	@Test(expected = EncounterTypeLockedException.class)
	@Verifies(value = "should throw error when trying to unretire encounter type when encounter types are locked", method = "unretireEncounterType(EncounterType)")
	public void unretireEncounterType_shouldThrowErrorWhenTryingToUnretireEncounterTypeWhenEncounterTypesAreLocked()
	        throws Exception {
		EncounterService encounterService = Context.getEncounterService();
		EncounterType encounterType = Context.getEncounterService().getEncounterType(2);
		
		GlobalProperty gp = new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_ENCOUNTER_TYPES_LOCKED);
		gp.setPropertyValue("true");
		Context.getAdministrationService().saveGlobalProperty(gp);
		
		encounterService.unretireEncounterType(encounterType);
	}
	
	/**
	 * @see {@link EncounterService#purgeEncounterType(EncounterType)}
	 */
	@Test(expected = EncounterTypeLockedException.class)
	@Verifies(value = "should throw error when trying to delete encounter type when encounter types are locked", method = "purgeEncounterType(EncounterType)")
	public void purgeEncounterType_shouldThrowErrorWhenTryingToDeleteEncounterTypeWhenEncounterTypesAreLocked()
	        throws Exception {
		EncounterService encounterService = Context.getEncounterService();
		EncounterType encounterType = Context.getEncounterService().getEncounterType(1);
		
		Assert.assertNotNull(encounterType);
		
		GlobalProperty gp = new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_ENCOUNTER_TYPES_LOCKED);
		gp.setPropertyValue("true");
		Context.getAdministrationService().saveGlobalProperty(gp);
		
		encounterService.purgeEncounterType(encounterType);
	}
	
	@Test
	@Verifies(value = "find encounter roles based on their name", method = "getEncounterRolesByName(String)")
	public void getEncounterRolesByName_shouldFindEncounterRolesByName() throws Exception {
		EncounterService encounterService = Context.getEncounterService();
		String name = "surgeon";
		
		List<EncounterRole> encounterRoles = encounterService.getEncounterRolesByName(name);
		
		assertNotNull("valid EncounterROle object should be returned", encounterRoles);
		assertEquals(encounterRoles.size(), 1);
		assertEquals(encounterRoles.get(0).getName(), name);
	}
	
	/**
	 * @see EncounterService#transferEncounter(Encounter,Patient)
	 */
	@Test
	@Verifies(value = "transfer an encounter with orders and observations to given patient", method = "transferEncounter(Encounter,Patient)")
	public void transferEncounter_shouldTransferAnEncounterWithOrdersAndObservationsToGivenPatient() throws Exception {
		executeDataSet(TRANSFER_ENC_DATA_XML);
		Patient targetPatient = Context.getPatientService().getPatient(201);
		// encounter has 2 obs which are connected with the same order
		Encounter sourceEncounter = Context.getEncounterService().getEncounter(201);
		
		Assert.assertEquals(1, sourceEncounter.getOrders().size());
		Assert.assertEquals(2, sourceEncounter.getObs().size());
		
		//transfer
		Encounter transferredEncounter = Context.getEncounterService().transferEncounter(sourceEncounter, targetPatient);
		List<Order> transferredOrders = new ArrayList<Order>(transferredEncounter.getOrders());
		List<Obs> transferredObservations = new ArrayList<Obs>(transferredEncounter.getObs());
		
		//check if transferredEncounter is newly created encounter
		Assert.assertNotEquals(sourceEncounter.getId(), transferredEncounter.getId());
		Assert.assertEquals(targetPatient, transferredEncounter.getPatient());
		
		//check order
		Assert.assertEquals(1, transferredOrders.size());
		Order transferredOrder = transferredOrders.get(0);
		Assert.assertEquals(targetPatient, transferredOrder.getPatient());
		
		//check obs
		Assert.assertEquals(2, transferredObservations.size());
		Assert.assertEquals(targetPatient, transferredObservations.get(0).getPerson());
		Assert.assertEquals(targetPatient, transferredObservations.get(1).getPerson());
		
		//check if obs has reference to the same order
		Assert.assertEquals(transferredOrder, transferredObservations.get(0).getOrder());
		Assert.assertEquals(transferredOrder, transferredObservations.get(1).getOrder());
		Assert.assertSame(transferredObservations.get(0).getOrder(), transferredObservations.get(1).getOrder());
		
		//check if form is transferred
		Assert.assertNotNull(transferredEncounter.getForm());
	}
	
	/**
	 * @see EncounterService#transferEncounter(Encounter,Patient)
	 */
	@Test
	@Verifies(value = "void given encounter", method = "transferEncounter(Encounter,Patient)")
	public void transferEncounter_shouldVoidGivenEncounter() throws Exception {
		executeDataSet(TRANSFER_ENC_DATA_XML);
		Patient anyPatient = new Patient(2);
		Encounter sourceEncounter = Context.getEncounterService().getEncounter(200);
		Context.getEncounterService().transferEncounter(sourceEncounter, anyPatient);
		//get fresh encounter from db
		Encounter sourceEncounterAfterTransfer = Context.getEncounterService().getEncounter(sourceEncounter.getId());
		Assert.assertTrue(sourceEncounterAfterTransfer.isVoided());
	}
	
	/**
	 * @see EncounterService#transferEncounter(Encounter,Patient)
	 */
	@Test
	@Verifies(value = "void given encounter visit if given encounter is the only encounter", method = "transferEncounter(Encounter,Patient)")
	public void transferEncounter_shouldVoidGivenEncounterVisitIfGivenEncounterIsTheOnlyEncounter() throws Exception {
		executeDataSet(TRANSFER_ENC_DATA_XML);
		Patient anyPatient = new Patient(2);
		//belongs to visit with id 2 (has this encounter only)
		Encounter sourceEncounter = Context.getEncounterService().getEncounter(200);
		Context.getEncounterService().transferEncounter(sourceEncounter, anyPatient);
		Visit visit = Context.getVisitService().getVisit(200);
		Assert.assertTrue(visit.isVoided());
	}
	
	private Person newPerson(String name) {
		Person person = new Person();
		Set<PersonName> personNames = new TreeSet<PersonName>();
		PersonName personName = new PersonName();
		personName.setFamilyName(name);
		personNames.add(personName);
		person.setNames(personNames);
		return person;
	}
}

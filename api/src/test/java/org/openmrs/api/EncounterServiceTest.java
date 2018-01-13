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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

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
import org.openmrs.OrderGroup;
import org.openmrs.OrderSet;
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
import org.openmrs.api.builder.OrderBuilder;
import org.openmrs.api.context.Context;
import org.openmrs.api.handler.EncounterVisitHandler;
import org.openmrs.api.handler.ExistingOrNewVisitAssignmentHandler;
import org.openmrs.api.handler.ExistingVisitAssignmentHandler;
import org.openmrs.api.handler.NoVisitAssignmentHandler;
import org.openmrs.parameter.EncounterSearchCriteria;
import org.openmrs.parameter.EncounterSearchCriteriaBuilder;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.util.DateUtil;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.PrivilegeConstants;

/**
 * Tests all methods in the {@link EncounterService}
 */
public class EncounterServiceTest extends BaseContextSensitiveTest {
	
	protected static final String ENC_INITIAL_DATA_XML = "org/openmrs/api/include/EncounterServiceTest-initialData.xml";
	
	protected static final String UNIQUE_ENC_WITH_PAGING_XML = "org/openmrs/api/include/EncounterServiceTest-pagingWithUniqueEncounters.xml";
	
	protected static final String TRANSFER_ENC_DATA_XML = "org/openmrs/api/include/EncounterServiceTest-transferEncounter.xml";

	protected static final String ENC_OBS_HIERARCHY_DATA_XML = "org/openmrs/api/include/EncounterServiceTest-saveObsHierarchyTests.xml";

	protected static final String ORDER_SET = "org/openmrs/api/include/OrderSetServiceTest-general.xml";


	/**
	 * This method is run before all of the tests in this class because it has the @Before
	 * annotation on it. This will add the contents of {@link #ENC_INITIAL_DATA_XML} to the current
	 * database
	 *
	 * @see BaseContextSensitiveTest#runBeforeAllUnitTests()
	 * @throws Exception
	 */
	@Before
	public void runBeforeEachTest() {
		executeDataSet(ENC_INITIAL_DATA_XML);
	}

	@Test
	public void saveEncounter_shouldUpdateExistingEncounterWhenAChildObsIsEdited() {
		executeDataSet(ENC_OBS_HIERARCHY_DATA_XML);
		EncounterService es = Context.getEncounterService();
		ObsService os = Context.getObsService();

		Encounter enc = es.getEncounter(100);

		Obs o = os.getObs(101);
		o.setValueText("Obs value updated");

		es.saveEncounter(enc);
		Context.flushSession();
		Context.clearSession();

		updateSearchIndex();

		enc = es.getEncounter(100);

		Set<Obs> obsAtTopLevelUpdated = enc.getObsAtTopLevel(true);
		Obs oParent = os.getObs(100);
		final Obs editedObs = os.getObs(101);
		Obs o2 = os.getObs(102);
		Obs o3 = getNewVersionOfEditedObs(oParent,editedObs);

		assertEquals(1,obsAtTopLevelUpdated.size());
		assertEquals(3, oParent.getGroupMembers(true).size());
		assertTrue(editedObs.getVoided());
		assertFalse(oParent.getVoided());
		assertFalse(o2.getVoided());

		assertNotNull(o3);
		assertFalse(o3.getVoided());
		assertEquals("Obs value updated", o3.getValueText());
	}

	@Test
	public void saveEncounter_shouldUpdateValueOfLeafObsAndNotDuplicateAtEncounterLevel() {
		executeDataSet(ENC_OBS_HIERARCHY_DATA_XML);
		Encounter encounter = Context.getEncounterService().getEncounter(100);
		assertEquals(1, encounter.getObsAtTopLevel(true).size());
		Obs topLevelObs = encounter.getObsAtTopLevel(true).iterator().next();
		topLevelObs.getGroupMembers().iterator().next().setValueText("editing first obs");
		encounter.addObs(topLevelObs);

		Encounter savedEncounter = Context.getEncounterService().saveEncounter(encounter);

		assertEquals(1, savedEncounter.getObsAtTopLevel(true).size());
	}

	@Test
	public void saveEncounter_shouldUpdateExistingEncounterWhenNewObsIsAddedToParentObs() {
		executeDataSet(ENC_OBS_HIERARCHY_DATA_XML);
		ConceptService cs = Context.getConceptService();
		EncounterService es = Context.getEncounterService();
		ObsService os = Context.getObsService();

		Encounter enc = es.getEncounter(100);

		Obs o3 = new Obs();
		o3.setConcept(cs.getConcept(3));
		o3.setDateCreated(new Date());
		o3.setCreator(Context.getAuthenticatedUser());
		o3.setLocation(new Location(1));
		o3.setObsDatetime(new Date());
		o3.setPerson(Context.getPersonService().getPerson(3));
		o3.setValueText("third obs value text");
		o3.setEncounter(enc);

		Obs oParent = os.getObs(100);
		oParent.addGroupMember(o3);

		es.saveEncounter(enc);

		Context.flushSession();
		Context.clearSession();

		enc = es.getEncounter(100);

		Set<Obs> obsAtTopLevelUpdated = enc.getObsAtTopLevel(true);
		assertEquals(1,obsAtTopLevelUpdated.size());

		assertEquals(3, obsAtTopLevelUpdated.iterator().next().getGroupMembers(true).size());

		oParent = os.getObs(100);
		assertTrue(oParent.getGroupMembers(true).contains(os.getObs(101)));
		assertTrue(oParent.getGroupMembers(true).contains(os.getObs(102)));
		assertTrue(oParent.getGroupMembers(true).contains(os.getObs(o3.getObsId())));
	}


	@Test
	public void saveEncounter_shouldSaveEncounterWhenTopLevelObsIsUpdatedByRemovingChildObs() {
		executeDataSet(ENC_OBS_HIERARCHY_DATA_XML);
		EncounterService es = Context.getEncounterService();
		ObsService os = Context.getObsService();

		Encounter enc = es.getEncounter(100);
		Obs oParent = os.getObs(100);
		Obs oChild = os.getObs(101);

		oParent.removeGroupMember(oChild);

		es.saveEncounter(enc);

		Context.flushSession();
		Context.clearSession();

		enc = es.getEncounter(100);

		Set<Obs> obsAtTopLevel = enc.getObsAtTopLevel(true);
		assertEquals(2,obsAtTopLevel.size()); //oChild's new version is still associated to encounter but not part of the oParent anymore

		assertTrue(obsAtTopLevel.contains(os.getObs(100)));

		Obs newObs = obsAtTopLevel.stream().filter(obs -> obs.getObsId() != 100 ).findFirst().get(); //Find the other top level obs which is not 100. i.e. the new obs which is a clone of 101

		assertNotNull(newObs.getPreviousVersion());
		assertEquals(oChild.getObsId(), newObs.getPreviousVersion().getObsId());

		assertTrue(os.getObs(100).getGroupMembers(true).contains(os.getObs(102)));
		//The oChild will still be associated to the parent because when we save oChild, we create a new instance
		//and void the oChild using ObsServiceImpl.voidExistingObs(). We use Context.evictFromSession(obs); to get a fresh copy
		//there by losing the change we made to oChild.
		assertTrue(os.getObs(100).getGroupMembers(true).contains(os.getObs(101)));

	}

	private Obs getNewVersionOfEditedObs(Obs parentObs, Obs originalObs){
		for(Obs childObs: parentObs.getGroupMembers()){
			if(originalObs.equals(childObs.getPreviousVersion())){
				return childObs;
			}
		}
		return null;
	}

	/**
	 * @see EncounterService#saveEncounter(Encounter)
	 */
	@Test
	public void saveEncounter_shouldSaveEncounterWithBasicDetails() {
		Encounter encounter = buildEncounter();
		
		EncounterService es = Context.getEncounterService();
		es.saveEncounter(encounter);
		
		assertNotNull("The saved encounter should have an encounter id now", encounter.getEncounterId());
		Encounter newSavedEncounter = es.getEncounter(encounter.getEncounterId());
		assertNotNull("We should get back an encounter", newSavedEncounter);
		assertTrue("The created encounter needs to equal the pojo encounter", encounter.equals(newSavedEncounter));
	}
	
	/**
	 * @see EncounterService#saveEncounter(Encounter)
	 */
	@Test
	public void saveEncounter_shouldUpdateEncounterSuccessfully() {
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
		assertTrue("The date needs to have been set",
		    DateUtil.truncateToSeconds(newestEnc.getEncounterDatetime()).equals(DateUtil.truncateToSeconds(d2)));
		assertFalse("The patient should be different", origPatient.equals(pat2));
		assertTrue("The patient should have been set", newestEnc.getPatient().equals(pat2));
	}
	
	/**
	 * You should be able to add an obs to an encounter, save the encounter, and have the obs
	 * automatically persisted. Added to test bug reported in ticket #827
	 *
	 * @see EncounterService#saveEncounter(Encounter)
	 */
	@Test
	public void saveEncounter_shouldCascadeSaveToContainedObs() {
		EncounterService es = Context.getEncounterService();
		// First, create a new Encounter
		Encounter enc = buildEncounter();
		
		//add an obs to the encounter
		Obs groupObs = new Obs();
		Concept c = Context.getConceptService().getConcept(1);
		groupObs.setConcept(c);
		
		// add an obs to the group
		Obs childObs = new Obs();
		childObs.setConcept(c);
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
	 * @see EncounterService#saveEncounter(Encounter)
	 */
	@Test
	public void saveEncounter_shouldCascadeChangeOfLocationInEncounterToContainedObs() {
		EncounterService es = Context.getEncounterService();
		Encounter enc = buildEncounter();
		
		es.saveEncounter(enc);
		
		// Now add an obs to it
		Obs newObs = new Obs();
		newObs.setConcept(Context.getConceptService().getConcept(1));
		newObs.setValueNumeric(50d);
		Location location = new Location(1);
		newObs.setLocation(location);
		
		enc.addObs(newObs);
		es.saveEncounter(enc);
		
		enc.setLocation(location);
		es.saveEncounter(enc);
		assertEquals(enc.getLocation(), newObs.getLocation());
	}

	@Test
	public void saveEncounter_shouldSaveEncounterWithComplexObs() {
		executeDataSet(ENC_OBS_HIERARCHY_DATA_XML);
		EncounterService es = Context.getEncounterService();

		Encounter encounter = es.getEncounter(101);
		Obs observation = buildObs();
		observation.setLocation(encounter.getLocation());
		observation.setPerson(encounter.getPatient());
		encounter.addObs(observation);

		es.saveEncounter(encounter);
		Context.flushSession();
		Context.clearSession();

		encounter = es.getEncounter(101);
		assertEquals(2, encounter.getObsAtTopLevel(true).size());
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
	 * @see EncounterService#saveEncounter(Encounter)
	 */
	@Test
	public void saveEncounter_shouldNotCascadeLocationChangeForDifferentInitialLocations() {
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
		newObs.setConcept(Context.getConceptService().getConcept(1));
		newObs.setValueNumeric(50d);
		newObs.setLocation(new Location(2));
		return newObs;
	}
	
	/**
	 * @see EncounterService#unvoidEncounter(Encounter)
	 */
	@Test
	public void saveEncounter_shouldSetDateStoppedOnTheOriginalAfterAddingReviseOrder() {
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
	 * @see EncounterService#unvoidEncounter(Encounter)
	 */
	@Test
	public void saveEncounter_shouldNotCascadeToExistingOrder() {
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
	 * @see EncounterService#purgeEncounter(Encounter)
	 */
	@Test
	public void purgeEncounter_shouldPurgeEncounter() {
		
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
	 * @see EncounterService#purgeEncounter(Encounter,null)
	 */
	@Test
	public void purgeEncounter_shouldCascadePurgeToObsAndOrders() {
		
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
	 * @see EncounterService#saveEncounter(Encounter)
	 */
	@Test
	public void saveEncounter_shouldCascadeSaveToContainedObsWhenEncounterAlreadyExists() {
		EncounterService es = Context.getEncounterService();
		
		// get an encounter from the database
		Encounter encounter = es.getEncounter(1);
		assertNotNull(encounter.getEncounterDatetime());
		
		// Now add an obs to it
		Obs obs = new Obs();
		obs.setConcept(Context.getConceptService().getConcept(1));
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
	 * @see EncounterService#saveEncounter(Encounter)
	 */
	@Test
	public void saveEncounter_shouldCascadeEncounterDatetimeToObs() {
		EncounterService es = Context.getEncounterService();
		
		// get an encounter from the database
		Encounter encounter = es.getEncounter(1);
		assertNotNull(encounter.getEncounterDatetime());
		
		// Now add an obs to it and do NOT set the obs datetime
		Obs obs = new Obs();
		obs.setConcept(Context.getConceptService().getConcept(1));
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
	 * @see EncounterService#saveEncounter(Encounter)
	 */
	@Test
	public void saveEncounter_shouldOnlyCascadeTheObsdatetimesToObsWithDifferentInitialObsdatetimes() {
		EncounterService es = Context.getEncounterService();
		Encounter enc = es.getEncounter(1);
		assertEquals(3, enc.getAllObs(false).size());
		List<Obs> obsWithSameDateBefore = new ArrayList<>();
		Obs obsWithDifferentDateBefore = null;
		for (Obs o : enc.getAllObs(false)) {
			if (enc.getEncounterDatetime().equals(o.getObsDatetime())) {
				obsWithSameDateBefore.add(o);
			} else if (obsWithDifferentDateBefore == null) {
				obsWithDifferentDateBefore = o;
			}
		}
		assertNotNull(obsWithDifferentDateBefore);
		assertEquals(2, obsWithSameDateBefore.size());
		
		Date newDate = new Date();
		enc.setEncounterDatetime(newDate);
		
		// save the encounter. The obs should pick up the encounter's date
		es.saveEncounter(enc);
		Context.flushSession();
		List<Obs> obsWithSameDateAfter = new ArrayList<>();
		Obs obsWithDifferentDateAfter = null;
		for (Obs o : enc.getAllObs(false)) {
			if (enc.getEncounterDatetime().equals(o.getObsDatetime())) {
				obsWithSameDateAfter.add(o);
				assertTrue(obsWithSameDateBefore.contains(o.getPreviousVersion()));
			} else if (obsWithDifferentDateAfter == null) {
				obsWithDifferentDateAfter = o;
			}
		}
		assertNotNull(obsWithDifferentDateAfter);
		assertEquals(2, obsWithSameDateAfter.size());
		assertNull(obsWithDifferentDateAfter.getPreviousVersion());
		assertSame(obsWithDifferentDateBefore, obsWithDifferentDateAfter);
	}
	
	/**
	 * @see EncounterService#saveEncounter(Encounter)
	 */
	@Test
	public void saveEncounter_shouldNotOverwriteCreatorIfNonNull() {
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
	 * @see EncounterService#saveEncounter(Encounter)
	 */
	@Test
	public void saveEncounter_shouldNotOverwriteDateCreatedIfNonNull() {
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
		assertEquals(DateUtil.truncateToSeconds(date), encounter.getDateCreated());
		
		// make sure we can fetch this new encounter
		// from the database and its values are the same as the passed in ones
		Encounter newEncounter = encounterService.getEncounter(encounter.getEncounterId());
		assertNotNull(newEncounter);
		assertEquals(DateUtil.truncateToSeconds(date), encounter.getDateCreated());
	}
	
	/**
	 * Make sure the obs and order creator and dateCreated is preserved when passed into
	 * {@link EncounterService#saveEncounter(Encounter)}
	 * 
	 * @throws SecurityException
	 * @throws NoSuchFieldException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @see EncounterService#saveEncounter(Encounter)
	 */
	@Test
	public void saveEncounter_shouldNotOverwriteObsAndOrdersCreatorOrDateCreated()
	        throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		EncounterService encounterService = Context.getEncounterService();
		
		// the encounter to save without a dateCreated
		Encounter encounter = buildEncounter();
		Date date = new Date(System.currentTimeMillis() - 5000); // make sure we
		// have a date that isn't "right now"
		encounter.setDateCreated(date);
		User creator = new User(1);
		encounter.setCreator(creator);
		
		ConceptService cs = Context.getConceptService();
		// create and add an obs to this encounter
		Obs obs = new Obs(new Patient(2), cs.getConcept(1), new Date(), new Location(1));
		obs.setDateCreated(date);
		obs.setCreator(creator);
		obs.setValueNumeric(50d);
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
		assertEquals(DateUtil.truncateToSeconds(date), createdObs.getDateCreated());
		assertEquals(creator, createdObs.getCreator());
		
		// make sure the order date created and creator are the same as what we
		// set
		Order createdOrder = os.getOrder(order.getOrderId());
		assertEquals(DateUtil.truncateToSeconds(date), createdOrder.getDateCreated());
		assertEquals(creator, createdOrder.getCreator());
	}
	
	/**
	 * @see EncounterService#getEncountersByPatient(Patient)
	 */
	@Test
	public void getEncountersByPatient_shouldNotGetVoidedEncounters() {
		EncounterService encounterService = Context.getEncounterService();
		
		List<Encounter> encounters = encounterService.getEncountersByPatient(new Patient(3));
		assertEquals(2, encounters.size());
	}
	
	/**
	 * @see EncounterService#getEncountersByPatient(Patient)
	 */
	@Test(expected = IllegalArgumentException.class)
	public void getEncountersByPatient_shouldThrowErrorWhenGivenNullParameter() {
		Context.getEncounterService().getEncountersByPatient((Patient) null);
	}
	
	/**
	 * @see EncounterService#getEncountersByPatientId(Integer)
	 */
	@Test
	public void getEncountersByPatientId_shouldNotGetVoidedEncounters() {
		EncounterService encounterService = Context.getEncounterService();
		
		List<Encounter> encounters = encounterService.getEncountersByPatientId(3);
		assertEquals(2, encounters.size());
	}
	
	/**
	 * @see EncounterService#getEncountersByPatientId(Integer)
	 */
	@Test(expected = IllegalArgumentException.class)
	public void getEncountersByPatientId_shouldThrowErrorIfGivenANullParameter() {
		Context.getEncounterService().getEncountersByPatientId(null);
	}
	
	/**
	 * @see EncounterService#getEncountersByPatientIdentifier(String)
	 */
	@Test
	public void getEncountersByPatientIdentifier_shouldNotGetVoidedEncounters() {
		EncounterService encounterService = Context.getEncounterService();
		
		List<Encounter> encounters = encounterService.getEncountersByPatientIdentifier("12345");
		assertEquals(2, encounters.size());
	}
	
	/**
	 * @see EncounterService#getEncountersByPatientIdentifier(String)
	 */
	@Test(expected = IllegalArgumentException.class)
	public void getEncountersByPatientIdentifier_shouldThrowErrorIfGivenNullParameter() {
		Context.getEncounterService().getEncountersByPatientIdentifier(null);
	}
	
	/**
	 * Make sure {@link EncounterService#voidEncounter(Encounter, String)} marks all the voided
	 * stuff correctly
	 *
	 * @see EncounterService#voidEncounter(Encounter,String)
	 */
	@Test
	public void voidEncounter_shouldVoidEncounterAndSetAttributes() {
		EncounterService encounterService = Context.getEncounterService();
		
		// get a nonvoided encounter
		Encounter encounter = encounterService.getEncounter(1);
		assertFalse(encounter.getVoided());
		assertNull(encounter.getVoidedBy());
		assertNull(encounter.getVoidReason());
		assertNull(encounter.getDateVoided());
		
		Encounter voidedEnc = encounterService.voidEncounter(encounter, "Just Testing");
		
		// make sure its still the same object
		assertEquals(voidedEnc, encounter);
		
		// make sure that all the values were filled in
		assertTrue(voidedEnc.getVoided());
		assertNotNull(voidedEnc.getDateVoided());
		assertEquals(Context.getAuthenticatedUser(), voidedEnc.getVoidedBy());
		assertEquals("Just Testing", voidedEnc.getVoidReason());
	}
	
	/**
	 * @see EncounterService#voidEncounter(Encounter,String)
	 */
	@Test
	public void voidEncounter_shouldCascadeToObs() {
		EncounterService encounterService = Context.getEncounterService();
		
		// get a nonvoided encounter that has some obs
		Encounter encounter = encounterService.getEncounter(1);
		encounterService.voidEncounter(encounter, "Just Testing");
		
		Obs obs = Context.getObsService().getObs(1);
		assertTrue(obs.getVoided());
		assertEquals("Just Testing", obs.getVoidReason());
	}
	
	/**
	 * @see EncounterService#voidEncounter(Encounter,String)
	 */
	@Test
	public void voidEncounter_shouldCascadeToOrders() {
		EncounterService encounterService = Context.getEncounterService();
		
		// get a nonvoided encounter that has some obs
		Encounter encounter = encounterService.getEncounter(1);
		encounterService.voidEncounter(encounter, "Just Testing");
		
		Order order = Context.getOrderService().getOrder(1);
		assertTrue(order.getVoided());
		assertEquals("Just Testing", order.getVoidReason());
	}
	
	/**
	 * @see EncounterService#unvoidEncounter(Encounter)
	 */
	@Test
	public void unvoidEncounter_shouldCascadeUnvoidToObs() {
		EncounterService encounterService = Context.getEncounterService();
		
		// get a voided encounter that has some voided obs
		Encounter encounter = encounterService.getEncounter(2);
		encounterService.unvoidEncounter(encounter);
		
		Obs obs = Context.getObsService().getObs(4);
		assertFalse(obs.getVoided());
		assertNull(obs.getVoidReason());
	}
	
	/**
	 * @see EncounterService#unvoidEncounter(Encounter)
	 */
	@Test
	public void unvoidEncounter_shouldCascadeUnvoidToOrders() {
		EncounterService encounterService = Context.getEncounterService();
		
		// get a voided encounter that has some voided obs
		Encounter encounter = encounterService.getEncounter(2);
		encounterService.unvoidEncounter(encounter);
		
		Order order = Context.getOrderService().getOrder(2);
		assertFalse(order.getVoided());
		assertNull(order.getVoidReason());
	}
	
	/**
	 * @see EncounterService#voidEncounter(Encounter,String)
	 */
	@Test(expected = IllegalArgumentException.class)
	public void voidEncounter_shouldThrowErrorWithNullReasonParameter() {
		EncounterService encounterService = Context.getEncounterService();
		Encounter type = encounterService.getEncounter(1);
		encounterService.voidEncounter(type, null);
	}
	
	/**
	 * @see EncounterService#unvoidEncounter(Encounter)
	 */
	@Test
	public void unvoidEncounter_shouldUnvoidAndUnmarkAllAttributes() {
		EncounterService encounterService = Context.getEncounterService();
		
		// get an already voided encounter
		Encounter encounter = encounterService.getEncounter(2);
		assertTrue(encounter.getVoided());
		assertNotNull(encounter.getVoidedBy());
		assertNotNull(encounter.getVoidReason());
		assertNotNull(encounter.getDateVoided());
		
		Encounter unvoidedEnc = encounterService.unvoidEncounter(encounter);
		
		// make sure its still the same object
		assertEquals(unvoidedEnc, encounter);
		
		// make sure that all the values were unfilled in
		assertFalse(unvoidedEnc.getVoided());
		assertNull(unvoidedEnc.getDateVoided());
		assertNull(unvoidedEnc.getVoidedBy());
		assertNull(unvoidedEnc.getVoidReason());
	}
	
	/**
	 * @see EncounterService#getEncounters(Patient, Location, Date, Date, java.util.Collection,
	 *      java.util.Collection, java.util.Collection, boolean)
	 */
	@Test
	public void getEncounters_shouldGetEncountersByLocation() {
		EncounterSearchCriteria encounterSearchCriteria = new EncounterSearchCriteriaBuilder().setLocation(new Location(1))
		        .setIncludeVoided(true).createEncounterSearchCriteria();
		
		List<Encounter> encounters = Context.getEncounterService().getEncounters(encounterSearchCriteria);
		assertEquals(6, encounters.size());
	}
	
	/**
	 * Get encounters that are after a certain date, and ensure the comparison is INCLUSIVE of the
	 * given date
	 * 
	 * @throws ParseException
	 * @see EncounterService#getEncounters(Patient, Location, Date, Date, java.util.Collection,
	 *      java.util.Collection, java.util.Collection, boolean)
	 */
	@Test
	public void getEncounters_shouldGetEncountersOnOrAfterDate() throws ParseException {
		// there is only one nonvoided encounter, on 2005-01-01
		DateFormat ymd = new SimpleDateFormat("yyyy-MM-dd");
		
		// test for a min date long before all dates
		EncounterSearchCriteria encounterSearchCriteria = new EncounterSearchCriteriaBuilder()
		        .setFromDate(ymd.parse("2004-12-31")).setIncludeVoided(false).createEncounterSearchCriteria();
		List<Encounter> encounters = Context.getEncounterService().getEncounters(encounterSearchCriteria);
		assertEquals(5, encounters.size());
		assertEquals(1, encounters.get(0).getEncounterId().intValue());
		
		// test for exact date search
		encounterSearchCriteria = new EncounterSearchCriteriaBuilder().setFromDate(ymd.parse("2005-01-01"))
		        .setIncludeVoided(false).createEncounterSearchCriteria();
		encounters = Context.getEncounterService().getEncounters(encounterSearchCriteria);
		assertEquals(5, encounters.size());
		
		// test for one day later
		encounterSearchCriteria = new EncounterSearchCriteriaBuilder().setFromDate(ymd.parse("2005-01-02"))
		        .setIncludeVoided(false).createEncounterSearchCriteria();
		encounters = Context.getEncounterService().getEncounters(encounterSearchCriteria);
		assertEquals(4, encounters.size());
		assertEquals(3, encounters.get(0).getEncounterId().intValue());
		assertEquals(4, encounters.get(1).getEncounterId().intValue());
		assertEquals(5, encounters.get(2).getEncounterId().intValue());
	}
	
	/**
	 * @throws ParseException
	 * @see EncounterService#getEncounters(Patient, Location, Date, Date, java.util.Collection,
	 *      java.util.Collection, java.util.Collection, boolean)
	 */
	@Test
	public void getEncounters_shouldGetEncountersOnOrUpToADate() throws ParseException {
		Date toDate = new SimpleDateFormat("yyyy-dd-MM").parse("2006-01-01");
		EncounterSearchCriteria encounterSearchCriteria = new EncounterSearchCriteriaBuilder().setToDate(toDate)
		        .setIncludeVoided(true).createEncounterSearchCriteria();
		List<Encounter> encounters = Context.getEncounterService().getEncounters(encounterSearchCriteria);
		assertEquals(2, encounters.size());
		assertEquals(15, encounters.get(0).getEncounterId().intValue());
		assertEquals(1, encounters.get(1).getEncounterId().intValue());
	}
	
	/**
	 * @see EncounterService#getEncounters(Patient, Location, Date, Date, java.util.Collection,
	 *      java.util.Collection, java.util.Collection, boolean)
	 */
	@Test
	public void getEncounters_shouldGetEncountersByForm() {
		List<Form> forms = new ArrayList<>();
		forms.add(new Form(1));
		EncounterSearchCriteria encounterSearchCriteria = new EncounterSearchCriteriaBuilder().setEnteredViaForms(forms)
		        .setIncludeVoided(true).createEncounterSearchCriteria();
		List<Encounter> encounters = Context.getEncounterService().getEncounters(encounterSearchCriteria);
		assertEquals(8, encounters.size());
	}
	
	/**
	 * @see EncounterService#getEncounters(Patient, Location, Date, Date, java.util.Collection,
	 *      java.util.Collection, java.util.Collection, boolean)
	 */
	@Test
	public void getEncounters_shouldGetEncountersByType() {
		List<EncounterType> types = new ArrayList<>();
		types.add(new EncounterType(1));
		EncounterSearchCriteria encounterSearchCriteria = new EncounterSearchCriteriaBuilder().setEncounterTypes(types)
		        .setIncludeVoided(true).createEncounterSearchCriteria();
		List<Encounter> encounters = Context.getEncounterService().getEncounters(encounterSearchCriteria);
		assertEquals(7, encounters.size());
	}
	
	/**
	 * @see EncounterService#getEncounters(Patient, Location, Date, Date, java.util.Collection,
	 *      java.util.Collection, java.util.Collection, boolean)
	 */
	@Test
	public void getEncounters_shouldExcludeVoidedEncounters() {
		EncounterSearchCriteria encounterSearchCriteria = new EncounterSearchCriteriaBuilder().setIncludeVoided(false)
		        .createEncounterSearchCriteria();
		assertEquals(6, Context.getEncounterService().getEncounters(encounterSearchCriteria).size());
	}
	
	/**
	 * @throws ParseException
	 * @see org.openmrs.api.EncounterService#getEncounters(org.openmrs.parameter.EncounterSearchCriteria)
	 */
	@Test
	public void getEncounters_shouldGetEncountersModifiedAfterSpecifiedDate() throws ParseException {
		EncounterService encounterService = Context.getEncounterService();
		Assert.assertEquals(7, encounterService.getEncounters(encounterSearchForVoidedWithDateChanged("2006-01-01")).size());
		Assert.assertEquals(5, encounterService.getEncounters(encounterSearchForVoidedWithDateChanged("2008-06-01")).size());
		Assert.assertEquals(1, encounterService.getEncounters(encounterSearchForVoidedWithDateChanged("2010-01-01")).size());
	}
	
	/**
	 * @see EncounterService#getEncounters(Patient, Location, Date, Date, java.util.Collection,
	 *      java.util.Collection, java.util.Collection, boolean)
	 */
	@Test
	public void getEncounters_shouldIncludeVoidedEncounters() {
		EncounterSearchCriteria encounterSearchCriteria = new EncounterSearchCriteriaBuilder().setIncludeVoided(true)
		        .createEncounterSearchCriteria();
		assertEquals(8, Context.getEncounterService().getEncounters(encounterSearchCriteria).size());
	}
	
	/**
	 * @see EncounterService#saveEncounterType(EncounterType)
	 */
	@Test
	public void saveEncounterType_shouldSaveEncounterType() {
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
	 * @see EncounterService#saveEncounterType(EncounterType)
	 */
	@Test
	public void saveEncounterType_shouldNotOverwriteCreator() {
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
	 * @see EncounterService#saveEncounterType(EncounterType)
	 */
	@Test
	public void saveEncounterType_shouldNotOverwriteCreatorOrDateCreated() {
		EncounterService encounterService = Context.getEncounterService();
		
		// the encounter to save without a dateCreated
		EncounterType encounterType = new EncounterType("testing", "desc");
		encounterType.setCreator(new User(4));
		Date date = new Date(System.currentTimeMillis() - 5000); // make sure we have a date that isn't "right now"
		encounterType.setDateCreated(date);
		
		// make sure the logged in user isn't the user we're testing with
		assertNotSame(encounterType.getCreator(), Context.getAuthenticatedUser());
		
		encounterService.saveEncounterType(encounterType);
		
		// make sure the encounter type creator is user 4 not user 1
		assertNotSame(encounterType.getCreator().getId(), Context.getAuthenticatedUser().getId());
		
		// make sure the encounter type date created wasn't overwritten
		assertEquals(DateUtil.truncateToSeconds(date), encounterType.getDateCreated());
		
		// make sure we can fetch this new encounter type
		// from the database and its values are the same as the passed in ones
		EncounterType newEncounterType = encounterService.getEncounterType(encounterType.getEncounterTypeId());
		assertNotNull(newEncounterType);
		assertEquals(4, encounterType.getCreator().getId().intValue());
		assertNotSame(encounterType.getCreator(), Context.getAuthenticatedUser());
		assertEquals(DateUtil.truncateToSeconds(date), encounterType.getDateCreated());
	}
	
	/**
	 * @see EncounterService#saveEncounterType(EncounterType)
	 */
	@Test
	public void saveEncounterType_shouldNotOverwriteDateCreated() {
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
		assertEquals(DateUtil.truncateToSeconds(date), encounterType.getDateCreated());
		
		// make sure we can fetch this new encounter type
		// from the database and its values are the same as the passed in ones
		EncounterType newEncounterType = encounterService.getEncounterType(encounterType.getEncounterTypeId());
		assertNotNull(newEncounterType);
		assertEquals(DateUtil.truncateToSeconds(date), encounterType.getDateCreated());
	}
	
	/**
	 * @see EncounterService#saveEncounterType(EncounterType)
	 */
	@Test
	public void saveEncounterType_shouldSetAuditInfoIfAnyItemInEncounterTypeIsEdited() {
		EncounterService es = Context.getEncounterService();
		
		// Create encounter type, ensure creator/dateCreated are set, and changedBy and dateChanged are not setDateCreated.
		EncounterType encounterType = es.saveEncounterType(new EncounterType("testing", "desc"));
		User creator = encounterType.getCreator();
		Date dateCreated = encounterType.getDateCreated();
		
		assertNotNull("creator should be set after saving", creator);
		assertNotNull("date creates should be set after saving", dateCreated);
		assertNull("changed by should not be set after creation", encounterType.getChangedBy());
		assertNull("date changed should not be set after creation", encounterType.getDateChanged());
		
		// Edit encounter type.
		encounterType.setDescription("This has been a test!");
		EncounterType editedEt = es.saveEncounterType(encounterType);
		Context.flushSession();
		
		// Ensure creator/dateCreated remain unchanged, and changedBy and dateChanged are set.
		assertTrue("creator should not change during edit", creator.equals(editedEt.getCreator()));
		assertTrue("date created should not changed during edit", dateCreated.equals(editedEt.getDateCreated()));
		assertNotNull("changed by should be set after edit", editedEt.getChangedBy());
		assertNotNull("date changed should be set after edit", editedEt.getDateChanged());
	}
	
	/**
	 * @see EncounterService#getEncounterType(String)
	 */
	@Test
	public void getEncounterType_shouldNotGetRetiredTypes() {
		EncounterService encounterService = Context.getEncounterService();
		
		// loop over all types to make sure
		// that the retired "Test Enc Type C" exists
		boolean foundRetired = false;
		for (EncounterType encType : encounterService.getAllEncounterTypes(true)) {
			if (encType.getName().equals("Test Enc Type C") && encType.getRetired()) {
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
	 * @see EncounterService#getEncounterType(String)
	 */
	@Test
	public void getEncounterType_shouldReturnNullIfOnlyRetiredTypeFound() {
		EncounterService encounterService = Context.getEncounterService();
		
		// sanity check to make sure 'some retired type' is in the dataset
		assertTrue(encounterService.getEncounterType(4).getRetired());
		assertEquals("Some Retired Type", encounterService.getEncounterType(4).getName());
		
		// we should get a null here because this named type is retired
		EncounterType type = encounterService.getEncounterType("Some Retired Type");
		assertNull(type);
	}
	
	/**
	 * Make sure that we are matching on exact name and not partial name in
	 *
	 * @see EncounterService#getEncounterType(String)
	 */
	@Test
	public void getEncounterType_shouldNotGetByInexactName() {
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
	 * @see EncounterService#getEncounterType(String)
	 */
	@Test
	public void getEncounterType_shouldReturnNullWithNullNameParameter() {
		EncounterService encounterService = Context.getEncounterService();
		
		// we should not get an error here...but silently return nothing
		EncounterType type = encounterService.getEncounterType((String) null);
		assertNull(type);
	}
	
	/**
	 * @see EncounterService#getAllEncounterTypes(boolean)
	 */
	@Test
	public void getAllEncounterTypes_shouldNotReturnRetiredTypes() {
		EncounterService encounterService = Context.getEncounterService();
		List<EncounterType> encounterTypes = encounterService.getAllEncounterTypes(false);
		
		// make sure we get a list
		assertNotNull(encounterTypes);
		
		// make sure we only get the two non retired encounter types
		// defined in the initialData.xml
		assertEquals(2, encounterTypes.size());
	}
	
	/**
	 * @see EncounterService#getAllEncounterTypes(null)
	 */
	@Test
	public void getAllEncounterTypes_shouldIncludeRetiredTypesWithTrueIncludeRetiredParameter() {
		EncounterService encounterService = Context.getEncounterService();
		boolean foundRetired = false;
		List<EncounterType> types = encounterService.getAllEncounterTypes(true);
		
		// there should be five types in the database
		assertEquals(5, types.size());
		
		for (EncounterType type : types) {
			if (type.getRetired())
				foundRetired = true;
		}
		assertTrue("Retired types should be returned as well", foundRetired);
	}
	
	/**
	 * @see EncounterService#findEncounterTypes(String)
	 */
	@Test
	public void findEncounterTypes_shouldReturnTypesByPartialNameMatch() {
		EncounterService encounterService = Context.getEncounterService();
		List<EncounterType> types = encounterService.findEncounterTypes("Test Enc");
		assertEquals(3, types.size());
	}
	
	/**
	 * @see EncounterService#findEncounterTypes(String)
	 */
	@Test
	public void findEncounterTypes_shouldReturnTypesByPartialCaseInsensitiveMatch() {
		EncounterService encounterService = Context.getEncounterService();
		List<EncounterType> types = encounterService.findEncounterTypes("Test ENC");
		assertEquals(3, types.size());
	}
	
	/**
	 * @see EncounterService#findEncounterTypes(String)
	 */
	@Test
	public void findEncounterTypes_shouldIncludeRetiredTypesInTheResults() {
		EncounterService encounterService = Context.getEncounterService();
		List<EncounterType> types = encounterService.findEncounterTypes("Test Enc");
		assertEquals(3, types.size());
		
		// make sure at least one of the types was retired
		boolean foundRetired = false;
		for (EncounterType type : types) {
			if (type.getRetired())
				foundRetired = true;
		}
		assertTrue("Retired types should be returned as well", foundRetired);
	}
	
	/**
	 * No types should be returned when using a substring other than the starting substring
	 *
	 * @see EncounterService#findEncounterTypes(String)
	 */
	@Test
	public void findEncounterTypes_shouldNotPartialMatchNameOnInternalSubstrings() {
		EncounterService encounterService = Context.getEncounterService();
		List<EncounterType> types = encounterService.findEncounterTypes("Test Enc Type");
		assertEquals(3, types.size());
		
		types = encounterService.findEncounterTypes("Enc Type");
		assertEquals(0, types.size());
	}
	
	/**
	 * @see EncounterService#findEncounterTypes(String)
	 */
	@Test
	public void findEncounterTypes_shouldReturnTypesOrderedOnNameAndNonretiredFirst() {
		EncounterService encounterService = Context.getEncounterService();
		List<EncounterType> types = encounterService.findEncounterTypes("Test Enc");
		
		// make sure the order is id 2, 3, 1
		assertEquals(2, types.get(0).getEncounterTypeId().intValue());
		assertEquals(1, types.get(1).getEncounterTypeId().intValue());
		assertEquals(3, types.get(2).getEncounterTypeId().intValue());
		
	}
	
	/**
	 * @see EncounterService#retireEncounterType(EncounterType,String)
	 */
	@Test
	public void retireEncounterType_shouldRetireTypeAndSetAttributes() {
		EncounterService encounterService = Context.getEncounterService();
		EncounterType type = encounterService.getEncounterType(1);
		assertFalse(type.getRetired());
		assertNull(type.getRetiredBy());
		assertNull(type.getRetireReason());
		assertNull(type.getDateRetired());
		
		EncounterType retiredEncType = encounterService.retireEncounterType(type, "Just Testing");
		
		// make sure its still the same object
		assertEquals(retiredEncType, type);
		
		// make sure that all the values were filled in
		assertTrue(retiredEncType.getRetired());
		assertNotNull(retiredEncType.getDateRetired());
		assertEquals(Context.getAuthenticatedUser(), retiredEncType.getRetiredBy());
		assertEquals("Just Testing", retiredEncType.getRetireReason());
	}
	
	/**
	 * @see EncounterService#retireEncounterType(EncounterType,String)
	 */
	@Test(expected = IllegalArgumentException.class)
	public void retireEncounterType_shouldThrowErrorIfGivenNullReasonParameter() {
		EncounterService encounterService = Context.getEncounterService();
		EncounterType type = encounterService.getEncounterType(1);
		encounterService.retireEncounterType(type, null);
	}
	
	/**
	 * @see EncounterService#unretireEncounterType(EncounterType)
	 */
	@Test
	public void unretireEncounterType_shouldUnretireTypeAndUnmarkAttributes() {
		EncounterService encounterService = Context.getEncounterService();
		EncounterType type = encounterService.getEncounterType(3);
		assertTrue(type.getRetired());
		assertNotNull(type.getRetiredBy());
		assertNotNull(type.getRetireReason());
		assertNotNull(type.getDateRetired());
		
		EncounterType unretiredEncType = encounterService.unretireEncounterType(type);
		
		// make sure its still the same object
		assertEquals(unretiredEncType, type);
		
		// make sure that all the values were unfilled in
		assertFalse(unretiredEncType.getRetired());
		assertNull(unretiredEncType.getDateRetired());
		assertNull(unretiredEncType.getRetiredBy());
		assertNull(unretiredEncType.getRetireReason());
	}
	
	/**
	 * @see EncounterService#saveEncounterType(EncounterType)
	 */
	@Test
	public void saveEncounterType_shouldUpdateAnExistingEncounterTypeName() {
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
	 * @see EncounterService#purgeEncounterType(EncounterType)
	 */
	@Test
	public void purgeEncounterType_shouldPurgeType() {
		EncounterService encounterService = Context.getEncounterService();
		
		EncounterType encounterTypeToPurge = encounterService.getEncounterType(4);
		assertNotNull(encounterTypeToPurge);
		
		// check deletion
		encounterService.purgeEncounterType(encounterTypeToPurge);
		assertNull(encounterService.getEncounterType(4));
	}
	
	/**
	 * @see EncounterService#getEncounter(Integer)
	 */
	@Test(expected = IllegalArgumentException.class)
	public void getEncounter_shouldThrowErrorIfGivenNullParameter() {
		Context.getEncounterService().getEncounter(null);
	}
	
	/**
	 * @see EncounterService#getEncounterType(Integer)
	 */
	@Test(expected = IllegalArgumentException.class)
	public void getEncounterType_shouldThrowErrorIfGivenNullParameter() {
		Context.getEncounterService().getEncounterType((Integer) null);
	}
	
	/**
	 * @see EncounterService#saveEncounter(Encounter)
	 */
	@Test
	public void saveEncounter_shouldCascadePatientToOrdersInTheEncounter() {
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
	 * @see EncounterService#getEncounterByUuid(String)
	 */
	@Test
	public void getEncounterByUuid_shouldFindObjectGivenValidUuid() {
		String uuid = "6519d653-393b-4118-9c83-a3715b82d4ac";
		Encounter encounter = Context.getEncounterService().getEncounterByUuid(uuid);
		Assert.assertEquals(3, (int) encounter.getEncounterId());
	}
	
	/**
	 * @see EncounterService#getEncounterByUuid(String)
	 */
	@Test
	public void getEncounterByUuid_shouldReturnNullIfNoObjectFoundWithGivenUuid() {
		Assert.assertNull(Context.getEncounterService().getEncounterByUuid("some invalid uuid"));
	}
	
	/**
	 * @see EncounterService#getEncounterTypeByUuid(String)
	 */
	@Test
	public void getEncounterTypeByUuid_shouldFindObjectGivenValidUuid() {
		String uuid = "02c533ab-b74b-4ee4-b6e5-ffb6d09a0ac8";
		EncounterType encounterType = Context.getEncounterService().getEncounterTypeByUuid(uuid);
		Assert.assertEquals(6, (int) encounterType.getEncounterTypeId());
	}
	
	/**
	 * @see EncounterService#getEncounterTypeByUuid(String)
	 */
	@Test
	public void getEncounterTypeByUuid_shouldReturnNullIfNoObjectFoundWithGivenUuid() {
		Assert.assertNull(Context.getEncounterService().getEncounterTypeByUuid("some invalid uuid"));
	}
	
	/**
	 * @see EncounterService#getEncountersByPatient(String,boolean)
	 * @see EncounterService#getEncountersByPatient(String)
	 */
	@Test
	public void getEncountersByPatient_shouldGetAllUnvoidedEncountersForTheGivenPatientIdentifier() {
		EncounterService encounterService = Context.getEncounterService();
		
		List<Encounter> encounters = encounterService.getEncountersByPatient("12345", false);
		assertEquals(2, encounters.size());
	}
	
	/**
	 * @see EncounterService#getEncountersByPatient(String,boolean)
	 */
	@Test
	public void getEncountersByPatient_shouldGetAllUnvoidedEncountersForTheGivenPatientName() {
		EncounterService encounterService = Context.getEncounterService();
		
		List<Encounter> encounters = encounterService.getEncountersByPatient("John", false);
		assertEquals(3, encounters.size());
	}
	
	/**
	 * @see EncounterService#getEncountersByPatient(String,boolean)
	 */
	@Test
	public void getEncountersByPatient_shouldIncludeVoidedEncountersInTheReturnedListIfIncludedVoidedIsTrue()
	{
		EncounterService encounterService = Context.getEncounterService();
		
		List<Encounter> encounters = encounterService.getEncountersByPatient("12345", true);
		assertEquals(4, encounters.size());
	}
	
	/**
	 * @see EncounterService#getEncountersByPatient(String,boolean)
	 */
	@Test(expected = IllegalArgumentException.class)
	public void getEncountersByPatient_shouldThrowErrorIfGivenNullParameter() {
		Context.getEncounterService().getEncountersByPatient(null, false);
	}
	
	/**
	 * Tests that all encounters for all patients in a cohort are returned
	 *
	 * @see EncounterService#getAllEncounters(Cohort)
	 */
	@Test
	public void getAllEncounters_shouldGetAllEncountersForACohortOfPatients() {
		Cohort cohort = new Cohort();
		cohort.addMember(7);
		Map<Integer, List<Encounter>> allEncounters = Context.getEncounterService().getAllEncounters(cohort);
		Assert.assertEquals(1, allEncounters.size());
		Assert.assertEquals(3, allEncounters.get(7).size());
	}
	
	/**
	 * @see EncounterService#getEncounters(Patient, Location, Date, Date, java.util.Collection,
	 *      java.util.Collection, java.util.Collection, java.util.Collection, java.util.Collection,
	 *      boolean)
	 */
	@Test
	public void getEncounters_shouldGetEncountersByVisit() {
		List<Visit> visits = new ArrayList<>();
		visits.add(new Visit(1));
		EncounterSearchCriteria encounterSearchCriteria = new EncounterSearchCriteriaBuilder().setVisits(visits)
		        .setIncludeVoided(true).createEncounterSearchCriteria();
		List<Encounter> encounters = Context.getEncounterService().getEncounters(encounterSearchCriteria);
		assertEquals(2, encounters.size());
	}
	
	/**
	 * @see EncounterService#getEncounters(Patient, Location, Date, Date, java.util.Collection,
	 *      java.util.Collection, java.util.Collection, java.util.Collection, java.util.Collection,
	 *      boolean)
	 */
	@Test
	public void getEncounters_shouldGetEncountersByVisitType() {
		List<VisitType> visitTypes = new ArrayList<>();
		visitTypes.add(new VisitType(2));
		EncounterSearchCriteria encounterSearchCriteria = new EncounterSearchCriteriaBuilder().setVisitTypes(visitTypes)
		        .setIncludeVoided(true).createEncounterSearchCriteria();
		List<Encounter> encounters = Context.getEncounterService().getEncounters(encounterSearchCriteria);
		assertEquals(2, encounters.size());
	}
	
	/**
	 * @see EncounterService#getEncountersByVisit(Visit, boolean)
	 */
	@Test
	public void getEncountersByVisit_shouldGetActiveEncountersByVisit() {
		List<Encounter> encounters = Context.getEncounterService().getEncountersByVisit(new Visit(1), false);
		assertEquals(1, encounters.size());
	}
	
	/**
	 * @see EncounterService#getEncountersByVisit(Visit, boolean)
	 */
	@Test
	public void getEncountersByVisit_shouldIncludeVoidedEncountersWhenIncludeVoidedIsTrue() {
		List<Encounter> encounters = Context.getEncounterService().getEncountersByVisit(new Visit(1), true);
		assertEquals(2, encounters.size());
	}
	
	/**
	 * @see EncounterService#getCountOfEncounters(String,null)
	 */
	@Test
	public void getCountOfEncounters_shouldGetTheCorrectCountOfUniqueEncounters() {
		executeDataSet(UNIQUE_ENC_WITH_PAGING_XML);
		Assert.assertEquals(4, Context.getEncounterService().getCountOfEncounters("qwerty", true).intValue());
	}
	
	/**
	 *
	 * @see EncounterService#getEncounters(String,Integer,Integer,null,null)
	 */
	@Test
	@Ignore
	public void getEncounters_shouldGetAllTheUniqueEncountersThatMatchTheSpecifiedParameterValues() {
		executeDataSet(UNIQUE_ENC_WITH_PAGING_XML);
		List<Encounter> encs = Context.getEncounterService().getEncounters("qwerty", 0, 4, true);
		Assert.assertEquals(4, encs.size());
	}
	
	/**
	 * TODO see ticket https://tickets.openmrs.org/browse/TRUNK-1956 to fix this test
	 *
	 * @see EncounterService#getEncounters(String,Integer,Integer,null,null)
	 */
	@Test
	@Ignore
	public void getEncounters_shouldNotReturnVoidedEncountersIfIncludeVoidedIsSetToTrue() {
		executeDataSet(UNIQUE_ENC_WITH_PAGING_XML);
		List<Encounter> encs = Context.getEncounterService().getEncounters("qwerty", 0, 3, false);
		Assert.assertEquals(3, encs.size());
	}
	
	/**
	 * @see org.openmrs.api.EncounterService#saveEncounterRole(org.openmrs.EncounterRole)
	 */
	@Test
	public void saveEncounterRole_shouldSaveEncounterRoleWithBasicDetails() {
		EncounterRole encounterRole = new EncounterRole();
		encounterRole.setName("Attending physician");
		encounterRole.setDescription("The person in charge");
		EncounterService encounterService = Context.getEncounterService();
		encounterService.saveEncounterRole(encounterRole);
		
		assertNotNull("The saved encounter role should have an encounter role id now", encounterRole.getEncounterRoleId());
		EncounterRole newSavedEncounterRole = encounterService.getEncounterRole(encounterRole.getEncounterRoleId());
		assertNotNull("We should get back an encounter role", newSavedEncounterRole);
		assertEquals(encounterRole, newSavedEncounterRole);
		assertTrue("The created encounter role needs to equal the pojo encounter role",
		    encounterRole.equals(newSavedEncounterRole));
		
	}
	
	/**
	 * Make sure that purging an encounter removes the row from the database
	 *
	 * @see EncounterService#purgeEncounterRole(org.openmrs.EncounterRole)
	 */
	@Test
	public void purgeEncounterRole_shouldPurgeEncounterRole() {
		EncounterService encounterService = Context.getEncounterService();
		EncounterRole encounterRole = encounterService.getEncounterRole(1);
		encounterService.purgeEncounterRole(encounterRole);
		EncounterRole fetchedEncounterRole = encounterService.getEncounterRole(encounterRole.getEncounterRoleId());
		assertNull("We shouldn't find the encounter after deletion", fetchedEncounterRole);
	}
	
	/**
	 * @see EncounterService#getAllEncounterRoles(boolean)
	 */
	@Test
	public void getAllEncounterRoles_shouldGetAllEncounterRolesBasedOnIncludeRetiredFlag() {
		EncounterService encounterService = Context.getEncounterService();
		List<EncounterRole> encounterRoles = encounterService.getAllEncounterRoles(true);
		assertEquals("get all encounter roles including retired", 3, encounterRoles.size());
		encounterRoles = encounterService.getAllEncounterRoles(false);
		assertEquals("get all encounter roles excluding retired", 2, encounterRoles.size());
	}
	
	/**
	 * @see EncounterService#getEncounterRoleByUuid(String)
	 */
	@Test
	public void getEncounterRoleByUuid_shouldFindEncounterRoleBasedOnUuid() {
		EncounterService encounterService = Context.getEncounterService();
		EncounterRole encounterRole = encounterService.getEncounterRoleByUuid("430bbb70-6a9c-4e1e-badb-9d1054b1b5e9");
		assertNotNull("valid uuid should be returned", encounterRole);
		encounterRole = encounterService.getEncounterRoleByUuid("invaid uuid");
		assertNull("returns null for invalid uuid", encounterRole);
	}
	
	/**
	 * @see EncounterService#getEncounterRoleByName(String)
	 */
	@Test
	public void getEncounterRoleByName_shouldFindEncounterRoleByName() {
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
	 * @see EncounterService#retireEncounterRole(org.openmrs.EncounterRole, String)
	 */
	@Test
	public void retireEncounterRole_shouldRetireTypeAndSetAttributes() {
		EncounterService encounterService = Context.getEncounterService();
		EncounterRole encounterRole = encounterService.getEncounterRole(1);
		assertFalse(encounterRole.getRetired());
		assertNull(encounterRole.getRetiredBy());
		assertNull(encounterRole.getRetireReason());
		assertNull(encounterRole.getDateRetired());
		EncounterRole retiredEncounterRole = encounterService.retireEncounterRole(encounterRole, "Just Testing");
		
		assertEquals(retiredEncounterRole, encounterRole);
		assertTrue(retiredEncounterRole.getRetired());
		assertNotNull(retiredEncounterRole.getDateRetired());
		assertEquals(Context.getAuthenticatedUser(), retiredEncounterRole.getRetiredBy());
		assertEquals("Just Testing", retiredEncounterRole.getRetireReason());
	}
	
	/**
	 * @see EncounterService#retireEncounterRole(org.openmrs.EncounterRole, String)
	 */
	@Test(expected = IllegalArgumentException.class)
	public void retireEncounterRole_shouldThrowErrorIfGivenNullReasonParameter() {
		EncounterService encounterService = Context.getEncounterService();
		EncounterRole encounterRole = encounterService.getEncounterRole(1);
		encounterService.retireEncounterRole(encounterRole, null);
	}
	
	/**
	 * @see EncounterService#unretireEncounterRole(org.openmrs.EncounterRole)
	 */
	@Test
	public void unretireEncounterRole_shouldUnretireTypeAndUnmarkAttributes() {
		EncounterService encounterService = Context.getEncounterService();
		EncounterRole encounterRole = encounterService.getEncounterRole(2);
		assertTrue(encounterRole.getRetired());
		assertNotNull(encounterRole.getRetiredBy());
		assertNotNull(encounterRole.getRetireReason());
		assertNotNull(encounterRole.getDateRetired());
		EncounterRole unretiredEncounterRole = encounterService.unretireEncounterRole(encounterRole);
		
		assertEquals(unretiredEncounterRole, encounterRole);
		assertFalse(unretiredEncounterRole.getRetired());
		assertNull(unretiredEncounterRole.getDateRetired());
		assertNull(unretiredEncounterRole.getRetiredBy());
		assertNull(unretiredEncounterRole.getRetireReason());
	}
	
	/**
	 * @see EncounterService#saveEncounter(Encounter)
	 */
	@Test
	public void saveEncounter_shouldCascadeDeleteEncounterProviders() {
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
	 */
	@Test
	public void saveEncounter_shouldCascadeSaveEncounterProviders() {
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
	 */
	@Test
	public void getEncounters_shouldReturnEmptyListForEmptyQuery() {
		//given
		
		//when
		List<Encounter> encounters = Context.getEncounterService().getEncounters("", null, null, true);
		
		//then
		Assert.assertTrue(encounters.isEmpty());
	}
	
	/**
	 * @see EncounterService#getVisitAssignmentHandlers()
	 */
	@Test
	public void getVisitAssignmentHandlers_shouldReturnTheNoAssignmentHandler() {
		
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
	 */
	@Test
	public void getVisitAssignmentHandlers_shouldReturnTheExistingVisitOnlyAssignmentHandler() {
		
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
	 */
	@Test
	public void getVisitAssignmentHandlers_shouldReturnTheExistingOrNewVisitAssignmentHandler() {
		
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
	 * @see EncounterService#saveEncounter(Encounter)
	 */
	@Test
	public void saveEncounter_shouldNotAssignEncounterToVisitIfNoHandlerIsRegistered() {
		Encounter encounter = buildEncounter();
		
		//We should have no visit
		assertNull(encounter.getVisit());
		
		Context.getEncounterService().saveEncounter(encounter);
		
		//We should have no visit
		assertNull(encounter.getVisit());
	}
	
	/**
	 * @see EncounterService#saveEncounter(Encounter)
	 */
	@Test
	public void saveEncounter_shouldNotAssignEncounterToVisitIfTheNoAssignHandlerIsRegistered() {
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
	 * @see EncounterService#saveEncounter(Encounter)
	 */
	@Test
	public void saveEncounter_shouldAssignEncounterToVisitIfTheAssignToExistingHandlerIsRegistered() {
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
	 * @see EncounterService#saveEncounter(Encounter)
	 */
	@Test
	public void saveEncounter_shouldAssignEncounterToVisitIfTheAssignToExistingOrNewHandlerIsRegistered() {
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
	 * @see EncounterService#getEncountersNotAssignedToAnyVisit(Patient)
	 */
	@Test
	public void getEncountersNotAssignedToAnyVisit_shouldReturnTheUnvoidedEncountersNotAssignedToAnyVisit() {
		executeDataSet(UNIQUE_ENC_WITH_PAGING_XML);
		List<Encounter> encs = Context.getEncounterService().getEncountersNotAssignedToAnyVisit(
		    Context.getPatientService().getPatient(10));
		Assert.assertEquals(2, encs.size());
		Assert.assertNull(encs.get(0).getVisit());
		Assert.assertNull(encs.get(1).getVisit());
	}
	
	/**
	 * @see EncounterService#saveEncounter(Encounter)
	 */
	@Test
	public void saveEncounter_shouldVoidAndCreateNewObsWhenSavingEncounter() {
		// create an encounter
		EncounterService es = Context.getEncounterService();
		Encounter encounter = new Encounter();
		encounter.setLocation(Context.getLocationService().getLocation(1));
		encounter.setEncounterType(es.getEncounterType(1));
		encounter.setEncounterDatetime(new Date());
		encounter.setPatient(Context.getPatientService().getPatient(3));
		
		// Now add an obs to it
		Obs obs = new Obs();
		obs.setConcept(Context.getConceptService().getConcept(1));
		obs.setValueNumeric(50d);
		encounter.addObs(obs);
		
		// save the encounter
		es.saveEncounter(encounter);
		
		// get the id of this obs
		int oldObsId = obs.getObsId();
		
		// now change the obs value
		obs.setValueNumeric(100d);
		
		// resave the encounters
		encounter = es.saveEncounter(encounter);
		encounter = es.getEncounter(encounter.getEncounterId());
		// get the new obs id
		int newObsId = encounter.getAllObs().iterator().next().getId();
		
		Assert.assertTrue(oldObsId != newObsId);
		Assert.assertEquals(2, encounter.getAllObs(true).size());
		Assert.assertEquals(1, encounter.getAllObs().size());
	}
	
	/**
	 * @see EncounterService#voidEncounter(Encounter, String)
	 */
	@Test
	public void voidEncounter_shouldNotVoidProviders() {
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
	 * @see EncounterService#filterEncountersByViewPermissions(List, User)
	 */
	@Test
	public void filterEncountersByViewPermissions_shouldFilterEncountersIfUserIsNotAllowedToSeeSomeEncounters()
	{
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
	 * @see EncounterService#filterEncountersByViewPermissions(List, User)
	 */
	@Test
	public void filterEncountersByViewPermissions_shouldNotFilterAllEncountersWhenTheEncounterTypesViewPrivilegeColumnIsNull()
	{
		EncounterService encounterService = Context.getEncounterService();
		
		int beforeSize = encounterService.getEncountersByPatientId(3).size();
		
		Encounter encounter = new Encounter();
		encounter.setLocation(new Location(1));
		encounter.setEncounterDatetime(new Date());
		encounter.setPatient(Context.getPatientService().getPatient(3));
		EncounterType encounterType = new EncounterType(1);
		// viewPrivilege on encounter type intentionally left null
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
	 * @see EncounterService#canViewAllEncounterTypes(User)
	 */
	@Test
	public void canViewAllEncounterTypes_shouldReturnTrueIfUserIsGrantedToViewEncounters() {
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
	 * @see EncounterService#canViewAllEncounterTypes(User)
	 */
	@Test
	public void canViewAllEncounterTypes_shouldReturnTrueWhenTheEncounterTypesViewPrivilegeColumnIsNull() {
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
	 * @see EncounterService#canEditAllEncounterTypes(User)
	 */
	@Test
	public void canEditAllEncounterTypes_shouldReturnTrueIfUserIsGrantedToEditEncounters() {
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
	 * @see EncounterService#canEditAllEncounterTypes(User)
	 */
	@Test
	public void canViewAllEncounterTypes_shouldReturnTrueWhenTheEncounterTypesEditPrivilegeColumnIsNull() {
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
	 * @see EncounterService#canEditEncounter(Encounter, User)
	 */
	@Test
	public void canEditEncounter_shouldReturnTrueIfUserCanEditEncounter() {
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
	 * @see EncounterService#canEditEncounter(Encounter, User)
	 */
	@Test
	public void canEditEncounter_shouldReturnFalseIfUserCanNotEditEncounter() {
		// get encounter that has type with edit privilege set
		Encounter encounter = getEncounterWithEditPrivilege();
		
		User user = Context.getUserService().getUserByUsername("test_user");
		assertNotNull(user);
		
		// left user as is - i.e. without required privilege
		
		assertFalse(Context.getEncounterService().canEditEncounter(encounter, user));
	}
	
	/**
	 * @see EncounterService#canEditEncounter(Encounter, User)
	 */
	@Test(expected = IllegalArgumentException.class)
	public void canEditEncounter_shouldFailfIfEncounterIsNull() {
		// invoke method using null encounter
		Context.getEncounterService().canEditEncounter(null, null);
	}
	
	/**
	 * @see EncounterService#canViewEncounter(Encounter, User)
	 */
	@Test
	public void canViewEncounter_shouldReturnTrueIfUserCanViewEncounter() {
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
	 * @see EncounterService#canViewEncounter(Encounter, User)
	 */
	@Test(expected = IllegalArgumentException.class)
	public void canViewEncounter_shouldFailfIfEncounterIsNull() {
		// invoke method using null encounter
		Context.getEncounterService().canViewEncounter(null, null);
	}
	
	/**
	 * @see EncounterService#canViewEncounter(Encounter, User)
	 */
	@Test
	public void canViewEncounter_shouldReturnFalseIfUserCanNotViewEncounter() {
		// get encounter that has type with view privilege set
		Encounter encounter = getEncounterWithViewPrivilege();
		
		User user = Context.getUserService().getUserByUsername("test_user");
		assertNotNull(user);
		
		// left user as is - i.e. without required privilege
		
		assertFalse(Context.getEncounterService().canViewEncounter(encounter, user));
	}
	
	/**
	 * @see EncounterService#getEncounter(Integer)
	 */
	@Test(expected = APIException.class)
	public void getEncounter_shouldFailIfUserIsNotAllowedToViewEncounterByGivenId() {
		// get encounter that has type with view privilege set
		Encounter encounter = getEncounterWithViewPrivilege();
		
		User user = Context.getUserService().getUserByUsername("test_user");
		assertNotNull(user);
		
		// left this user as is - i.e. without required privilege
		// and authenticate under it's account
		Context.becomeUser(user.getSystemId());
		
		// have to add privilege in order to be able to call getEncounter(Integer) method
		Context.addProxyPrivilege(PrivilegeConstants.GET_ENCOUNTERS);
		
		assertNull(Context.getEncounterService().getEncounter(encounter.getId()));
	}
	
	/**
	 * @see EncounterService#getEncounter(Integer)
	 */
	@Test
	public void getEncounter_shouldReturnEncounterIfUserIsAllowedToViewIt() {
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
		Context.addProxyPrivilege(PrivilegeConstants.GET_ENCOUNTERS);
		
		assertNotNull(Context.getEncounterService().getEncounter(encounter.getId()));
	}
	
	/**
	 * @see EncounterService#saveEncounter(Encounter)
	 */
	@Test(expected = APIException.class)
	public void saveEncounter_shouldFailfIfUserIsNotSupposedToEditEncountersOfTypeOfGivenEncounter() {
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
	 * @see EncounterService#voidEncounter(Encounter, String)
	 */
	@Test(expected = APIException.class)
	public void voidEncounter_shouldFailfIfUserIsNotSupposedToEditEncountersOfTypeOfGivenEncounter() {
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
	 * @see EncounterService#unvoidEncounter(Encounter)
	 */
	@Test(expected = APIException.class)
	public void unvoidEncounter_shouldFailfIfUserIsNotSupposedToEditEncountersOfTypeOfGivenEncounter() {
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
	 * @see EncounterService#purgeEncounter(Encounter)
	 */
	@Test(expected = APIException.class)
	public void purgeEncounter_shouldFailfIfUserIsNotSupposedToEditEncountersOfTypeOfGivenEncounter() {
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
	 * @see EncounterService#purgeEncounter(Encounter,Boolean)
	 */
	@Test(expected = APIException.class)
	public void purgeEncounterCascade_shouldFailfIfUserIsNotSupposedToEditEncountersOfTypeOfGivenEncounter()
	{
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
	 * @see EncounterService#getEncounters(String,Integer,Integer,Integer,null)
	 */
	@Test
	public void getEncounters_shouldFetchEncountersByPatientId() {
		Assert.assertEquals(2, Context.getEncounterService().getEncounters(null, 3, null, null, false).size());
		Assert.assertEquals(4, Context.getEncounterService().getEncounters(null, 3, null, null, true).size());
	}
	
	/**
	 * @see EncounterService#getEncounters(String,Integer,Integer,Integer,null)
	 */
	@Test
	public void getEncounters_shouldMatchOnTheLocationName() {
		Assert.assertEquals(2, Context.getEncounterService().getEncounters("Test Location", 3, null, null, false).size());
		Assert.assertEquals(4, Context.getEncounterService().getEncounters("Test Location", 3, null, null, true).size());
	}
	
	/**
	 * @see EncounterService#getEncounters(String,Integer,Integer,Integer,null)
	 */
	@Test
	public void getEncounters_shouldMatchOnTheProviderName() {
		Assert.assertEquals(1, Context.getEncounterService().getEncounters("phys", 3, null, null, false).size());
	}
	
	/**
	 * @see EncounterService#getEncounters(String,Integer,Integer,Integer,null)
	 */
	@Test
	public void getEncounters_shouldShouldMatchOnProviderIdentifier() {
		Assert.assertEquals(1, Context.getEncounterService().getEncounters("2", 3, null, null, false).size());
		Assert.assertEquals(2, Context.getEncounterService().getEncounters("2", 3, null, null, true).size());
	}
	
	/**
	 * @see EncounterService#getEncounters(String,Integer,Integer,Integer,null)
	 */
	@Test
	public void getEncounters_shouldMatchOnTheProviderPersonName() {
		//Should match on Super User and John3 Doe
		Assert.assertEquals(1, Context.getEncounterService().getEncounters("er jo", 3, null, null, false).size());
		Assert.assertEquals(2, Context.getEncounterService().getEncounters("er jo", 3, null, null, true).size());
		Assert.assertEquals(0, Context.getEncounterService().getEncounters("none", 3, null, null, true).size());
	}
	
	/**
	 * @see EncounterService#getEncounters(String,Integer,Integer,Integer,null)
	 */
	@Test
	public void getEncounters_shouldIncludeVoidedEncountersIfIncludeVoidedIsSetToTrue() {
		Assert.assertEquals(2, Context.getEncounterService().getEncounters("2", 3, null, null, true).size());
	}
	
	/**
	 * @see EncounterService#getEncounters(String,Integer,Integer,Integer,null)
	 */
	@Test
	public void getEncounters_shouldMatchOnTheEncounterTypeName() {
		Assert.assertEquals(2, Context.getEncounterService().getEncounters("Type B", 3, null, null, false).size());
	}
	
	/**
	 * @see EncounterService#getEncounters(String,Integer,Integer,Integer,null)
	 */
	@Test
	public void getEncounters_shouldMatchOnTheFormName() {
		Assert.assertEquals(2, Context.getEncounterService().getEncounters("Basic", 3, null, null, false).size());
	}
	
	/**
	 * @see EncounterService#saveEncounterType(EncounterType)
	 * @see EncounterService#checkIfEncounterTypesAreLocked()
	 */
	@Test(expected = EncounterTypeLockedException.class)
	public void saveEncounterType_shouldThrowErrorWhenTryingToSaveEncounterTypeWhenEncounterTypesAreLocked()
	{
		GlobalProperty gp = new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_ENCOUNTER_TYPES_LOCKED);
		gp.setPropertyValue("true");
		Context.getAdministrationService().saveGlobalProperty(gp);
		
		EncounterService encounterService = Context.getEncounterService();
		EncounterType encounterType = Context.getEncounterService().getEncounterType(1);
		
		Assert.assertNotNull(encounterType);
		
		encounterService.saveEncounterType(encounterType);
	}
	
	/**
	 * @see EncounterService#retireEncounterType(EncounterType, String)
	 */
	@Test(expected = EncounterTypeLockedException.class)
	public void retireEncounterType_shouldThrowErrorWhenTryingToRetireEncounterTypeWhenEncounterTypesAreLocked()
	{
		GlobalProperty gp = new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_ENCOUNTER_TYPES_LOCKED);
		gp.setPropertyValue("true");
		Context.getAdministrationService().saveGlobalProperty(gp);
		
		EncounterService encounterService = Context.getEncounterService();
		EncounterType encounterType = Context.getEncounterService().getEncounterType(1);
		Assert.assertNotNull(encounterType);
		
		encounterService.retireEncounterType(encounterType, "reason");
	}
	
	/**
	 * @see EncounterService#unretireEncounterType(EncounterType)
	 */
	@Test(expected = EncounterTypeLockedException.class)
	public void unretireEncounterType_shouldThrowErrorWhenTryingToUnretireEncounterTypeWhenEncounterTypesAreLocked()
	{
		EncounterService encounterService = Context.getEncounterService();
		EncounterType encounterType = Context.getEncounterService().getEncounterType(2);
		
		GlobalProperty gp = new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_ENCOUNTER_TYPES_LOCKED);
		gp.setPropertyValue("true");
		Context.getAdministrationService().saveGlobalProperty(gp);
		
		encounterService.unretireEncounterType(encounterType);
	}
	
	/**
	 * @see EncounterService#purgeEncounterType(EncounterType)
	 */
	@Test(expected = EncounterTypeLockedException.class)
	public void purgeEncounterType_shouldThrowErrorWhenTryingToDeleteEncounterTypeWhenEncounterTypesAreLocked()
	{
		EncounterService encounterService = Context.getEncounterService();
		EncounterType encounterType = Context.getEncounterService().getEncounterType(1);
		
		Assert.assertNotNull(encounterType);
		
		GlobalProperty gp = new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_ENCOUNTER_TYPES_LOCKED);
		gp.setPropertyValue("true");
		Context.getAdministrationService().saveGlobalProperty(gp);
		
		encounterService.purgeEncounterType(encounterType);
	}
	
	@Test
	public void getEncounterRolesByName_shouldFindEncounterRolesByName() {
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
	public void transferEncounter_shouldTransferAnEncounterWithObservationsButNotOrdersToGivenPatient() {
		executeDataSet(TRANSFER_ENC_DATA_XML);
		Patient targetPatient = Context.getPatientService().getPatient(201);
		// encounter has 2 obs which are connected with the same order
		Encounter sourceEncounter = Context.getEncounterService().getEncounter(201);
		
		Assert.assertEquals(2, sourceEncounter.getOrders().size());
		Assert.assertEquals(2, sourceEncounter.getObs().size());
		
		//transfer
		Encounter transferredEncounter = Context.getEncounterService().transferEncounter(sourceEncounter, targetPatient);
		List<Order> transferredOrders = new ArrayList<>(transferredEncounter.getOrders());
		List<Obs> transferredObservations = new ArrayList<>(transferredEncounter.getObs());
		
		//check if transferredEncounter is newly created encounter
		Assert.assertNotEquals(sourceEncounter.getId(), transferredEncounter.getId());
		Assert.assertEquals(targetPatient, transferredEncounter.getPatient());
		
		//check order
		Assert.assertEquals(0, transferredOrders.size());
		
		//check obs
		Assert.assertEquals(2, transferredObservations.size());
		Assert.assertEquals(targetPatient, transferredObservations.get(0).getPerson());
		Assert.assertEquals(targetPatient, transferredObservations.get(1).getPerson());
		
		Assert.assertNull(transferredObservations.get(0).getOrder());
		Assert.assertNull(transferredObservations.get(1).getOrder());
		
		//check if form is transferred
		Assert.assertNotNull(transferredEncounter.getForm());
	}
	
	/**
	 * @see EncounterService#transferEncounter(Encounter,Patient)
	 */
	@Test
	public void transferEncounter_shouldVoidGivenEncounter() {
		executeDataSet(TRANSFER_ENC_DATA_XML);
		Patient anyPatient = new Patient(2);
		Encounter sourceEncounter = Context.getEncounterService().getEncounter(200);
		Context.getEncounterService().transferEncounter(sourceEncounter, anyPatient);
		//get fresh encounter from db
		Encounter sourceEncounterAfterTransfer = Context.getEncounterService().getEncounter(sourceEncounter.getId());
		Assert.assertTrue(sourceEncounterAfterTransfer.getVoided());
	}
	
	/**
	 * @see EncounterService#transferEncounter(Encounter,Patient)
	 */
	@Test
	public void transferEncounter_shouldVoidGivenEncounterVisitIfGivenEncounterIsTheOnlyEncounter() {
		executeDataSet(TRANSFER_ENC_DATA_XML);
		Patient anyPatient = new Patient(2);
		//belongs to visit with id 2 (has this encounter only)
		Encounter sourceEncounter = Context.getEncounterService().getEncounter(200);
		Context.getEncounterService().transferEncounter(sourceEncounter, anyPatient);
		Visit visit = Context.getVisitService().getVisit(200);
		Assert.assertTrue(visit.getVoided());
	}
	
	private EncounterSearchCriteria encounterSearchForVoidedWithDateChanged(String dateChanged) throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		return new EncounterSearchCriteriaBuilder().setIncludeVoided(true).setDateChanged(sdf.parse(dateChanged))
		        .createEncounterSearchCriteria();
	}
	
	private Person newPerson(String name) {
		Person person = new Person();
		Set<PersonName> personNames = new TreeSet<>();
		PersonName personName = new PersonName();
		personName.setFamilyName(name);
		personNames.add(personName);
		person.setNames(personNames);
		return person;
	}

	@Test
	public void shouldSaveOrderGroupAlongWithOrders() {
		executeDataSet(ORDER_SET);
		
		//Created a new Encounter
		Encounter encounter = new Encounter();
		encounter.setPatient(Context.getPatientService().getPatient(3));
		encounter.setEncounterType(Context.getEncounterService().getEncounterType(1));
		encounter.setEncounterDatetime(new Date());
		
		Context.getEncounterService().saveEncounter(encounter);
		
		Integer encounterId = Context.getEncounterService().getEncounterByUuid(encounter.getUuid()).getId();
		
		//Created a new OrderGroup
		OrderSet orderSet = Context.getOrderSetService().getOrderSet(2000);
		OrderGroup orderGroup = new OrderGroup();
		orderGroup.setOrderSet(orderSet);
		orderGroup.setPatient(encounter.getPatient());
		orderGroup.setEncounter(Context.getEncounterService().getEncounter(encounterId));
		
		//Added this OrderGroup to two new orders
		Order firstOrderWithOrderGroup = new OrderBuilder().withAction(Order.Action.NEW).withPatient(3).withConcept(1000)
		        .withCareSetting(1).withOrderer(1).withEncounter(encounterId).withDateActivated(new Date())
		        .withOrderType(17).withUrgency(Order.Urgency.ON_SCHEDULED_DATE).withScheduledDate(new Date())
		        .withOrderGroup(orderGroup).build();
		
		Order secondOrderWithOrderGroup = new OrderBuilder().withAction(Order.Action.NEW).withPatient(3).withConcept(1001)
		        .withCareSetting(1).withOrderer(1).withEncounter(encounterId).withDateActivated(new Date())
		        .withOrderType(17).withUrgency(Order.Urgency.ON_SCHEDULED_DATE).withScheduledDate(new Date())
		        .withOrderGroup(orderGroup).build();
		
		//Add these orders to the Encounter
		encounter.addOrder(firstOrderWithOrderGroup);
		encounter.addOrder(secondOrderWithOrderGroup);
		
		Context.getEncounterService().saveEncounter(encounter);
		
		Context.flushSession();

		List<Order> orders = new ArrayList<>(
				Context.getEncounterService().getEncounterByUuid(encounter.getUuid()).getOrders());
		
		assertNotNull("OrderGroup is saved", orders.get(0).getOrderGroup());
		assertEquals("OrderGroup isa same for both the orders ", true, orders.get(0).getOrderGroup().equals(
		    orders.get(1).getOrderGroup()));
	}
	
	@Test
	public void shouldSaveMultipleOrderGroupsIfDifferentOrdersHaveDifferentOrderGroups() {
		executeDataSet(ORDER_SET);
		
		Encounter encounter = new Encounter();
		encounter.setPatient(Context.getPatientService().getPatient(3));
		encounter.setEncounterType(Context.getEncounterService().getEncounterType(1));
		encounter.setEncounterDatetime(new Date());
		
		Context.getEncounterService().saveEncounter(encounter);
		
		Integer encounterId = Context.getEncounterService().getEncounterByUuid(encounter.getUuid()).getId();
		
		//Created a new OrderGroup
		OrderSet orderSet = Context.getOrderSetService().getOrderSet(2000);
		OrderGroup orderGroup = new OrderGroup();
		orderGroup.setOrderSet(orderSet);
		orderGroup.setPatient(encounter.getPatient());
		orderGroup.setEncounter(Context.getEncounterService().getEncounter(encounterId));
		
		//Created a new OrderGroup
		OrderGroup orderGroup2 = new OrderGroup();
		orderGroup2.setEncounter(Context.getEncounterService().getEncounter(encounterId));
		orderGroup2.setPatient(encounter.getPatient());
		
		//Added this OrderGroup to two new orders
		Order newOrder1 = new OrderBuilder().withAction(Order.Action.NEW).withPatient(3).withConcept(1000)
		        .withCareSetting(1).withOrderer(1).withEncounter(encounterId).withDateActivated(new Date())
		        .withOrderType(17).withUrgency(Order.Urgency.ON_SCHEDULED_DATE).withScheduledDate(new Date())
		        .withOrderGroup(orderGroup).build();
		
		Order newOrder2 = new OrderBuilder().withAction(Order.Action.NEW).withPatient(3).withConcept(1001)
		        .withCareSetting(1).withOrderer(1).withEncounter(encounterId).withDateActivated(new Date())
		        .withOrderType(17).withUrgency(Order.Urgency.ON_SCHEDULED_DATE).withScheduledDate(new Date())
		        .withOrderGroup(orderGroup).build();
		
		Order newOrder3 = new OrderBuilder().withAction(Order.Action.NEW).withPatient(3).withConcept(1002)
		        .withCareSetting(1).withOrderer(1).withEncounter(encounterId).withDateActivated(new Date())
		        .withOrderType(17).withUrgency(Order.Urgency.ON_SCHEDULED_DATE).withScheduledDate(new Date())
		        .withOrderGroup(orderGroup2).build();
		
		Order newOrder4 = new OrderBuilder().withAction(Order.Action.NEW).withPatient(3).withConcept(1000)
		        .withCareSetting(1).withOrderer(1).withEncounter(encounterId).withDateActivated(new Date())
		        .withOrderType(17).withUrgency(Order.Urgency.ON_SCHEDULED_DATE).withScheduledDate(new Date()).build();
		
		Order newOrder5 = new OrderBuilder().withAction(Order.Action.NEW).withPatient(3).withConcept(1001)
		        .withCareSetting(1).withOrderer(1).withEncounter(encounterId).withDateActivated(new Date())
		        .withOrderType(17).withUrgency(Order.Urgency.ON_SCHEDULED_DATE).withScheduledDate(new Date())
		        .withOrderGroup(orderGroup2).build();
		
		//Add these orders to the Encounter
		encounter.addOrder(newOrder1);
		encounter.addOrder(newOrder2);
		encounter.addOrder(newOrder3);
		encounter.addOrder(newOrder4);
		encounter.addOrder(newOrder5);
		
		Context.getEncounterService().saveEncounter(encounter);
		
		Context.flushSession();

		List<Order> orders = new ArrayList<>(
				Context.getEncounterService().getEncounterByUuid(encounter.getUuid()).getOrders());
		
		HashMap<Integer, OrderGroup> orderGroups = new HashMap<>();
		for (Order order : orders) {
			if (order.getOrderGroup() != null) {
				orderGroups.put(order.getOrderGroup().getId(), order.getOrderGroup());
			}
		}
		
		assertEquals("Two New Order Groups Get Saved", 2, orderGroups.size());
	}
}

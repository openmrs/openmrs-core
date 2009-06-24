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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.api.impl.ObsServiceImpl;
import org.openmrs.obs.ComplexData;
import org.openmrs.obs.ComplexObsHandler;
import org.openmrs.obs.handler.ImageHandler;
import org.openmrs.obs.handler.TextHandler;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsUtil;
import org.openmrs.util.OpenmrsConstants.PERSON_TYPE;

/**
 * TODO clean up and add tests for all methods in ObsService
 */
public class ObsServiceTest extends BaseContextSensitiveTest {
	
	protected static final String INITIAL_OBS_XML = "org/openmrs/api/include/ObsServiceTest-initial.xml";
	
	protected static final String COMPLEX_OBS_XML = "org/openmrs/api/include/ObsServiceTest-complex.xml";
	
	/**
	 * Creates then updates an obs
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldSaveObs() throws Exception {
		
		executeDataSet(INITIAL_OBS_XML);
		
		EncounterService es = Context.getEncounterService();
		LocationService locationService = Context.getLocationService();
		ObsService obsService = Context.getObsService();
		ConceptService conceptService = Context.getConceptService();
		
		Obs o = new Obs();
		
		//testing creation
		
		Order order1 = null;
		Concept concept1 = conceptService.getConcept(1);
		Patient patient1 = new Patient(2);
		Encounter encounter1 = es.getEncounter(1);
		Date datetime1 = new Date();
		Location location1 = locationService.getLocation(1);
		Integer valueGroupId1 = new Integer(5);
		Date valueDatetime1 = new Date();
		Concept valueCoded1 = conceptService.getConcept(2);
		Double valueNumeric1 = 1.0;
		String valueModifier1 = "a1";
		String valueText1 = "value text1";
		String comment1 = "commenting1";
		
		o.setOrder(order1);
		o.setConcept(concept1);
		o.setPerson(patient1);
		o.setEncounter(encounter1);
		o.setObsDatetime(datetime1);
		o.setLocation(location1);
		o.setValueGroupId(valueGroupId1);
		o.setValueDatetime(valueDatetime1);
		o.setValueCoded(valueCoded1);
		o.setValueNumeric(valueNumeric1);
		o.setValueModifier(valueModifier1);
		o.setValueText(valueText1);
		o.setComment(comment1);
		
		// do the initial save to the database
		Obs oSaved = obsService.saveObs(o, null);
		// make sure the returned Obs and the passed in obs
		// now both have primary key obsIds
		assertTrue(oSaved.getObsId().equals(o.getObsId()));
		
		assertNotNull(o.getDateCreated());
		assertNotNull(o.getCreator());
		
		// get the same obs again and change all of the variables to 
		// makes sure that we update correctly.
		
		Obs o1ToUpdate = obsService.getObs(o.getObsId());
		assertNotNull(o1ToUpdate);
		
		Order order2 = null;
		Concept concept2 = conceptService.getConcept(2);
		Patient patient2 = new Patient(1);
		Encounter encounter2 = es.getEncounter(2);
		Date datetime2 = new Date();
		Location location2 = locationService.getLocation(1);
		Integer valueGroupId2 = new Integer(3);
		Date valueDatetime2 = new Date();
		Concept valueCoded2 = conceptService.getConcept(2);
		Double valueNumeric2 = 2.0;
		String valueModifier2 = "cc";
		String valueText2 = "value text2";
		String comment2 = "commenting2";
		
		o1ToUpdate.setOrder(order2);
		o1ToUpdate.setConcept(concept2);
		o1ToUpdate.setPerson(patient2);
		o1ToUpdate.setEncounter(encounter2);
		o1ToUpdate.setObsDatetime(datetime2);
		o1ToUpdate.setLocation(location2);
		o1ToUpdate.setValueGroupId(valueGroupId2);
		o1ToUpdate.setValueDatetime(valueDatetime2);
		o1ToUpdate.setValueCoded(valueCoded2);
		o1ToUpdate.setValueNumeric(valueNumeric2);
		o1ToUpdate.setValueModifier(valueModifier2);
		o1ToUpdate.setValueText(valueText2);
		o1ToUpdate.setComment(comment2);
		
		// do an update in the database for the same Obs
		Obs o1ToUpdateSaved = obsService.saveObs(o1ToUpdate, "Updating o1 with all new values");
		
		// the returned obs should have a different obs id
		assertFalse(o1ToUpdateSaved.getObsId().equals(o1ToUpdate.getObsId()));
		
		// the returned obs should have the new values
		assertTrue(o1ToUpdateSaved.getValueNumeric().equals(valueNumeric2));
		
		/* Grrr. This cannot be checked because java is pass-by-value. 
		  (updating the argument in the saveObs() method does not change which
		  object is pointed to by the o1ToUpdate parameter that was passed in) */
		// the saved obs should NOT have the new values
		//assertFalse(o1ToUpdate.getValueNumeric().equals(valueNumeric2));
		// make sure the dateCreated 
		Obs o3 = obsService.getObs(o1ToUpdateSaved.getObsId());
		
		//o1ToUpdateSaved should equal o3 and neither should equal o1
		assertTrue(o1ToUpdateSaved.equals(o3));
		assertFalse(o1ToUpdateSaved.equals(o));
		assertFalse(o3.equals(o));
		if (order2 != null)
			assertTrue(o3.getOrder().equals(order2));
		assertTrue(o3.getPerson().equals(patient2));
		assertTrue(o3.getComment().equals(comment2));
		assertTrue(o3.getConcept().equals(concept2));
		assertTrue(o3.getEncounter().equals(encounter2));
		assertTrue(o3.getObsDatetime().equals(datetime2));
		assertTrue(o3.getLocation().equals(location2));
		assertTrue(o3.getValueGroupId().equals(valueGroupId2));
		assertTrue(o3.getValueDatetime().equals(valueDatetime2));
		assertTrue(o3.getValueCoded().equals(valueCoded2));
		assertTrue(o3.getValueNumeric().equals(valueNumeric2));
		assertTrue(o3.getValueModifier().equals(valueModifier2));
		assertTrue(o3.getValueText().equals(valueText2));
		
		// test that the obs is voided 
		obsService.voidObs(o3, "testing void function");
		
		Obs o4 = obsService.getObs(o3.getObsId());
		
		assertTrue(o4.getVoidReason().equals("testing void function"));
		assertTrue(o4.getVoidedBy().equals(o3.getVoidedBy()));
		assertTrue(o4.isVoided());
		
	}
	
	/**
	 * Tests that an obs is voided in the db
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldVoidObs() throws Exception {
		executeDataSet(INITIAL_OBS_XML);
		ObsService obsService = Context.getObsService();
		
		Obs obs = new Obs(new Patient(2), new Concept(1), new Date(), new Location(1));
		
		Obs savedObs = obsService.saveObs(obs, null);
		
		// we should get back the same obs as we passed in for a create
		assertTrue(savedObs.equals(obs));
		
		// a voided obs shouldn't be passed through to the database
		obs.setVoided(Boolean.TRUE);
		obs.setVoidedBy(new User(1));
		String reason = "Testing voiding a voided obs";
		Obs notVoidedObs = obsService.voidObs(obs, reason);
		// we should get back the same obs as we passed in for a create
		assertTrue(notVoidedObs.equals(obs));
		
		// the void reason should not have been set by the voidObs method because it
		// should have failed early when it saw we were voiding a voided obs
		assertFalse(reason.equals(obs.getVoidReason()));
		
		// now do a valid voiding
		obs.setVoided(Boolean.FALSE);
		Obs voidedObs = obsService.voidObs(obs, reason);
		// we should get back the same obs as we passed in for a create
		assertTrue(voidedObs.equals(obs));
		
		// the void reason should not have been set by the voidObs method because it
		// should have failed early when it saw we were voiding a voided obs
		assertTrue(reason.equals(obs.getVoidReason()));
		
		Obs voidedObsFetched = obsService.getObs(obs.getObsId());
		assertTrue(voidedObsFetched.isVoided());
		assertTrue(reason.equals(voidedObsFetched.getVoidReason()));
		assertTrue(voidedObs.getObsId().equals(obs.getObsId()));
		assertTrue(voidedObs.getObsId().equals(voidedObs.getObsId()));
		
	}
	
	/**
	 * Tests the auto updating of the creator and dateCreated attrs when saving an obs
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldObsCreatorMetaData() throws Exception {
		
		executeDataSet(INITIAL_OBS_XML);
		
		ObsService obsService = Context.getObsService();
		
		Obs o = new Obs(new Person(2), new Concept(1), new Date(), new Location(1));
		o.setValueNumeric(1.0);
		
		Obs savedObs = obsService.saveObs(o, null);
		
		// there should be creation metadata
		assertNotNull(o.getCreator());
		assertNotNull(o.getDateCreated());
		
		// the metadata should match between passed in object and returned object
		// on the initial save
		assertEquals(savedObs.getCreator(), o.getCreator());
		assertEquals(savedObs.getDateCreated(), o.getDateCreated());
		
		savedObs.setValueNumeric(2.0);
		Obs updatedObs = obsService.saveObs(savedObs, "updating");
		
		// there should still be creation metadata
		assertNotNull(updatedObs.getCreator());
		assertNotNull(updatedObs.getDateCreated());
		
		// the metadata should be different for the obs passed in
		// and the obs that was returned
		assertNotSame(updatedObs.getDateCreated(), o.getDateCreated());
		
	}
	
	/**
	 * This tests certain aspects of saving obs that are obsGroups or are members of other obsGroups
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldObsGroupCreateUpdate() throws Exception {
		executeDataSet(INITIAL_OBS_XML);
		ObsService obsService = Context.getObsService();
		
		Obs obsGroup = new Obs();
		obsGroup.setConcept(new Concept(1));
		obsGroup.setObsDatetime(new Date());
		obsGroup.setPerson(new Patient(2));
		obsGroup.setLocation(new Location(2));
		
		Obs groupMember1 = new Obs();
		groupMember1.setConcept(new Concept(1));
		groupMember1.setValueNumeric(1.0);
		groupMember1.setObsDatetime(new Date());
		groupMember1.setPerson(new Patient(2));
		groupMember1.setLocation(new Location(2));
		obsGroup.addGroupMember(groupMember1);
		
		// sanity check
		assertTrue(obsGroup.hasGroupMembers());
		
		obsService.saveObs(obsGroup, null);
		
		// make sure the api filled in all of the necessary ids
		assertNotNull(obsGroup.getObsId());
		assertNotNull(obsGroup.getCreator());
		assertNotNull(obsGroup.getDateCreated());
		assertNotNull(obsGroup.getGroupMembers());
		assertNotNull(groupMember1.getObsId());
		assertNotNull(groupMember1.getObsGroup());
		assertNotNull(groupMember1.getCreator());
		assertNotNull(groupMember1.getDateCreated());
		
		// save the obs id of the firstly added group member
		Integer groupMember1OriginalObsId = groupMember1.getObsId();
		
		Obs groupMember2 = new Obs();
		groupMember2.setConcept(new Concept(2));
		groupMember2.setObsDatetime(new Date());
		groupMember2.setPerson(new Patient(2));
		groupMember2.setLocation(new Location(2));
		obsGroup.addGroupMember(groupMember2);
		// sanity checks
		assertEquals(2, obsGroup.getGroupMembers().size());
		assertNotNull(groupMember2.getObsGroup());
		
		obsService.saveObs(obsGroup, "Updating obs group");
		
		// make sure the api filled in all of the necessary ids again
		assertNotNull(groupMember2.getObsId());
		assertNotNull(groupMember2.getObsGroup());
		assertNotNull(groupMember2.getCreator());
		assertNotNull(groupMember2.getDateCreated());
		
		// make sure the api didn't change the obsId of the first group member
		Obs firstMember = (Obs) (obsGroup.getGroupMembers().toArray()[0]);
		Obs secondMember = (Obs) (obsGroup.getGroupMembers().toArray()[1]);
		if (firstMember.getConcept().equals(new Concept(1))) {
			// the set of members gets jumpbled after save.  The first one
			// we added above had a concept with id 1.
			assertEquals(groupMember1OriginalObsId, firstMember.getObsId());
			// make sure the second group member is still there
			assertEquals(groupMember2, secondMember);
		} else {
			assertEquals(groupMember1OriginalObsId, secondMember.getObsId());
			// make sure the second group member is still there
			assertEquals(groupMember2, firstMember);
		}
		
	}
	
	/**
	 * This test tests multi-level heirarchy obsGroup cascades for create, delete, update, void, and
	 * unvoid
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldSaveUpdateDeleteVoidObsGroupCascades() throws Exception {
		executeDataSet(INITIAL_OBS_XML);
		
		ObsService os = Context.getObsService();
		ConceptService cs = Context.getConceptService();
		
		//create an obs
		Obs o = new Obs();
		o.setConcept(cs.getConcept(1));
		o.setDateCreated(new Date());
		o.setCreator(Context.getAuthenticatedUser());
		o.setLocation(new Location(1));
		o.setObsDatetime(new Date());
		o.setPerson(new Patient(2));
		o.setValueText("original obs value text");
		
		//create a second obs
		Obs o2 = new Obs();
		o2.setConcept(cs.getConcept(1));
		o2.setDateCreated(new Date());
		o2.setCreator(Context.getAuthenticatedUser());
		o2.setLocation(new Location(1));
		o2.setObsDatetime(new Date());
		o2.setValueText("second obs value text");
		o2.setPerson(new Patient(2));
		
		//create a parent obs
		Obs oParent = new Obs();
		oParent.setConcept(cs.getConcept(2)); //in the concept set table as a set
		oParent.setDateCreated(new Date());
		oParent.setCreator(Context.getAuthenticatedUser());
		oParent.setLocation(new Location(1));
		oParent.setObsDatetime(new Date());
		oParent.setPerson(new Patient(2));
		
		//add o and o2 to the parent obs
		oParent.addGroupMember(o2);
		oParent.addGroupMember(o);
		
		//create a grandparent obs
		Obs oGP = new Obs();
		oGP.setConcept(cs.getConcept(1));
		oGP.setDateCreated(new Date());
		oGP.setCreator(Context.getAuthenticatedUser());
		oGP.setLocation(new Location(1));
		oGP.setObsDatetime(new Date());
		oGP.setPerson(new Patient(2));
		oGP.setValueText("grandparent obs value text");
		
		oGP.addGroupMember(oParent);
		
		//create a leaf observation
		Obs o3 = new Obs();
		o3.setConcept(cs.getConcept(1));
		o3.setDateCreated(new Date());
		o3.setCreator(Context.getAuthenticatedUser());
		o3.setLocation(new Location(1));
		o3.setObsDatetime(new Date());
		o3.setValueText("leaf obs value text");
		o3.setPerson(new Patient(2));
		
		//and add it to the grandparent
		oGP.addGroupMember(o3);
		
		//create a great-grandparent
		Obs oGGP = new Obs();
		oGGP.setConcept(cs.getConcept(1));
		oGGP.setDateCreated(new Date());
		oGGP.setCreator(Context.getAuthenticatedUser());
		oGGP.setLocation(new Location(1));
		oGGP.setObsDatetime(new Date());
		oGGP.setValueText("great grandparent value text");
		oGGP.setPerson(new Patient(2));
		
		oGGP.addGroupMember(oGP);
		
		//create a great-great grandparent
		Obs oGGGP = new Obs();
		oGGGP.setConcept(cs.getConcept(1));
		oGGGP.setDateCreated(new Date());
		oGGGP.setCreator(Context.getAuthenticatedUser());
		oGGGP.setLocation(new Location(1));
		oGGGP.setObsDatetime(new Date());
		oGGGP.setValueText("great great grandparent value text");
		oGGGP.setPerson(new Patient(2));
		
		oGGGP.addGroupMember(oGGP);
		
		//Create the great great grandparent
		os.saveObs(oGGGP, null);
		int oGGGPId = oGGGP.getObsId();
		
		//now navigate the tree and make sure that all tree members have obs_ids
		//indicating that they've been saved (unsaved_value in the hibernate mapping set to null so
		// the notNull assertion is sufficient):		
		Obs testGGGP = os.getObs(oGGGPId);
		assertTrue(testGGGP.isObsGrouping());
		Set<Obs> GGGPmembers = testGGGP.getGroupMembers();
		assertEquals(GGGPmembers.size(), 1);
		for (Obs testGGP : GGGPmembers) {
			assertTrue(testGGP.isObsGrouping());
			assertEquals(testGGP.getGroupMembers().size(), 1);
			assertNotNull(testGGP.getObsId());
			for (Obs testGP : testGGP.getGroupMembers()) {
				assertTrue(testGP.isObsGrouping());
				assertEquals(testGP.getGroupMembers().size(), 2);
				assertNotNull(testGP.getObsId());
				for (Obs parent : testGP.getGroupMembers()) {
					if (parent.isObsGrouping()) {
						assertEquals(parent.getGroupMembers().size(), 2);
						assertNotNull(parent.getObsId());
						for (Obs child : parent.getGroupMembers()) {
							assertNotNull(child.getObsId());
							//make an edit to a value so that we can save the great great grandfather
							//and see if the changes have been reflected:
							child.setValueText("testingUpdate");
						}
					}
					
				}
				
			}
		}
		
		Obs oGGGPThatWasUpdated = os.saveObs(oGGGP, "Updating obs group parent");
		
		//now, re-walk the tree to verify that the bottom-level leaf obs have the new text value:
		
		int childOneId = 0;
		int childTwoId = 0;
		assertTrue(oGGGPThatWasUpdated.isObsGrouping());
		Set<Obs> GGGPmembers2 = oGGGPThatWasUpdated.getGroupMembers();
		assertEquals(GGGPmembers2.size(), 1);
		for (Obs testGGP : GGGPmembers2) {
			assertTrue(testGGP.isObsGrouping());
			assertEquals(testGGP.getGroupMembers().size(), 1);
			assertNotNull(testGGP.getObsId());
			for (Obs testGP : testGGP.getGroupMembers()) {
				assertTrue(testGP.isObsGrouping());
				assertEquals(testGP.getGroupMembers().size(), 2);
				assertNotNull(testGP.getObsId());
				for (Obs parent : testGP.getGroupMembers()) {
					if (parent.isObsGrouping()) {
						assertEquals(parent.getGroupMembers().size(), 2);
						assertNotNull(parent.getObsId());
						int i = 0;
						for (Obs child : parent.getGroupMembers()) {
							assertEquals("testingUpdate", child.getValueText());
							//set childIds, so that we can test voids/unvoids/delete
							if (i == 0)
								childOneId = child.getObsId();
							else
								childTwoId = child.getObsId();
							i++;
						}
					}
					
				}
				
			}
		}
		
		//check voiding:
		//first, just create an Obs, and void it, and verify:
		Obs oVoidTest = new Obs();
		oVoidTest.setConcept(cs.getConcept(1));
		oVoidTest.setDateCreated(new Date());
		oVoidTest.setCreator(Context.getAuthenticatedUser());
		oVoidTest.setLocation(new Location(1));
		oVoidTest.setObsDatetime(new Date());
		oVoidTest.setPerson(new Patient(2));
		oVoidTest.setValueText("value text of soon-to-be-voided obs");
		
		Obs obsThatWasVoided = os.saveObs(oVoidTest, null);
		os.voidObs(obsThatWasVoided, "testing void method");
		
		assertTrue(obsThatWasVoided.getVoided());
		
		//unvoid:
		obsThatWasVoided.setVoided(false);
		assertFalse(obsThatWasVoided.isVoided());
		
		//Now test voiding cascade:
		// i.e. by voiding the grandparent, we void the n-th generation leaf obs
		os.voidObs(oGGGPThatWasUpdated, "testing void cascade");
		assertTrue(oGGGPThatWasUpdated.isVoided());
		
		Obs childLeafObs = os.getObs(childOneId);
		assertTrue(childLeafObs.isVoided());
		
		//now test the un-void:
		os.unvoidObs(oGGGPThatWasUpdated);
		assertFalse(oGGGPThatWasUpdated.isVoided());
		assertFalse(childLeafObs.isVoided());
		
		//test this again using just the os.updateObs method on the great great grandparent:
		
		os.voidObs(oGGGPThatWasUpdated, "testing void cascade");
		childLeafObs = os.getObs(childOneId);
		assertTrue(childLeafObs.isVoided());
		
		os.unvoidObs(oGGGPThatWasUpdated);
		childLeafObs = os.getObs(childOneId);
		assertFalse(childLeafObs.isVoided());
		
		//now, test the feature that unvoid doesn't happen unless child obs has the same dateVoided as
		// the Obj argument that gets passed into unvoid:
		
		os.voidObs(oGGGPThatWasUpdated, "testing void cascade");
		childLeafObs = os.getObs(childOneId);
		assertTrue(childLeafObs.isVoided());
		
		childLeafObs.setDateVoided(new Date(childLeafObs.getDateVoided().getTime() - 5000));
		//os.saveObs(childLeafObs, "saving child leaf obs");
		os.unvoidObs(oGGGPThatWasUpdated);
		
		// commenting this out because junit4 doesn't seem to care
		//commitTransaction(false);
		
		childLeafObs = os.getObs(childOneId);
		Obs childLeafObsTwo = os.getObs(childTwoId);
		
		//childLeafObs had its date voided date changed, so it should not get unvoided by the unvoid cascade
		//childLeafObsTwo should be unvoided, as the dateVoided date is still the same as the great-great
		//grandparent Obs
		
		assertFalse(childLeafObsTwo.getVoided());
		assertTrue(childLeafObs.getVoided());
		
		//finally, check the delete cascade:
		
		os.purgeObs(oGGGPThatWasUpdated);
		
		assertNull(os.getObs(oGGGPThatWasUpdated.getObsId()));
		assertNull(os.getObs(childOneId));
		assertNull(os.getObs(childTwoId));
	}
	
	/**
	 * Creates a simple Obs Group consisting of one parent and one child. Assumes INITIAL_OBS_XML is
	 * being used.
	 */
	private void createObsGroup(Obs parent, Obs child, ConceptService cs, ObsService os) {
		child.setConcept(cs.getConcept(1));
		child.setDateCreated(new Date());
		child.setCreator(Context.getAuthenticatedUser());
		child.setLocation(new Location(1));
		child.setObsDatetime(new Date());
		child.setValueText("test");
		child.setPerson(new Patient(2));
		
		parent.setConcept(cs.getConcept(2)); //in the concept set table as a set
		parent.setDateCreated(new Date());
		parent.setCreator(Context.getAuthenticatedUser());
		parent.setLocation(new Location(1));
		parent.setObsDatetime(new Date());
		parent.setPerson(new Patient(2));
		
		parent.addGroupMember(child);
		
		os.saveObs(parent, null);
	}
	
	/**
	 * This test makes sure that child obs on a parent obs are given an obs group id when the parent
	 * obs is created
	 * 
	 * @throws Throwable
	 */
	@Test
	public void shouldCreateObsGroupId() throws Throwable {
		
		executeDataSet(INITIAL_OBS_XML);
		
		ConceptService cs = Context.getConceptService();
		ObsService os = Context.getObsService();
		
		Obs child = new Obs();
		Obs oParent = new Obs();
		
		createObsGroup(oParent, child, cs, os);
		
		// save the obs ids
		Integer parentObsId = oParent.getObsId();
		assertNotNull(parentObsId);
		
		Integer childObsId = child.getObsId();
		assertNotNull(childObsId);
		
		// clear out the session so we can refetch and test that it saved correctly
		oParent = null;
		child = null;
		Context.clearSession();
		
		// try to get the same obs back and make sure it has children
		
		Obs fetchedParent = os.getObs(parentObsId);
		assertTrue(fetchedParent.isObsGrouping());
		assertEquals(1, fetchedParent.getGroupMembers().size());
		
	}
	
	/**
	 * Unit test for findObsByGroupId... yeah, it's deprecated, but doesn't hurt to check that it's
	 * doing what we want it to be doing.
	 * 
	 * @throws Throwable
	 */
	@SuppressWarnings("deprecation")
    @Test
	public void shouldFindObsByGroupId() throws Throwable {
		executeDataSet(INITIAL_OBS_XML);
		
		ConceptService cs = Context.getConceptService();
		ObsService os = Context.getObsService();
		
		Obs child = new Obs();
		Obs oParent = new Obs();
		
		createObsGroup(oParent, child, cs, os);
		
		//These should both return just the child.
		List<Obs> obs = os.findObsByGroupId(child.getObsGroupId());
		List<Obs> obs2 = os.findObsByGroupId(oParent.getObsId());
		
		//This shouldn't return anything.
		List<Obs> obs3 = os.findObsByGroupId(child.getObsId());
		
		Obs child2 = new Obs();
		child2.setConcept(cs.getConcept(1)); //in the concept set table as a set
		child2.setDateCreated(new Date());
		child2.setCreator(Context.getAuthenticatedUser());
		child2.setLocation(new Location(2));
		child2.setObsDatetime(new Date());
		child2.setPerson(new Patient(2));
		
		oParent.addGroupMember(child2);
		//oParent.setRequiredData(new OpenmrsObject.DefaultRequiredDataHelper(Context.getAuthenticatedUser(), new Date()));
		
		List<Obs> obs4 = os.findObsByGroupId(oParent.getObsId());
		
		assertTrue(obs.equals(obs2));
		assertTrue(obs3.size() == 0);
		assertTrue(obs4.contains(child));
		assertTrue(obs4.contains(child2));
	}
	
	/**
	 * This method gets observations and only fetches obs that are for patients
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldGetObservationsRestrictedToPatients() throws Exception {
		executeDataSet(INITIAL_OBS_XML);
		
		ObsService os = Context.getObsService();
		
		Concept c = new Concept(1); // a pseudo RETURN VISIT DATE
		List<Concept> questions = new Vector<Concept>();
		questions.add(c);
		
		List<PERSON_TYPE> personTypes = new Vector<PERSON_TYPE>();
		personTypes.add(PERSON_TYPE.PATIENT);
		
		List<Obs> obs = os.getObservations(c, "location.locationId asc, obs.valueDatetime asc", ObsService.PATIENT, true);
		
		assertEquals(4, obs.size());
		//os.getObservations(null, null, questions, null, personTypes, null, "obs.valueDatetime asc", null, null, null, null, false);
	}
	
	/**
	 * This method gets observations and only fetches obs that are for patients
	 * 
	 * @see ObsService#getObservations(List, List, List, List, List, List, List, Integer, Integer,
	 *      Date, Date, boolean)
	 */
	@Test
	@Verifies(value = "should compare dates using lte and gte", method = "getObservations(List<QPerson;>,List<QEncounter;>,List<QConcept;>,List<QConcept;>,List<QPERSON_TYPE;>,List<QLocation;>,List<QString;>,Integer,Integer,Date,Date,null)")
	public void getObservations_shouldCompareDatesUsingLteAndGte() throws Exception {
		executeDataSet(INITIAL_OBS_XML);
		
		ObsService os = Context.getObsService();
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		
		// Test 1, No bounderies
		Date sd = df.parse("2006-02-01");
		Date ed = df.parse("2006-02-20");
		List<Obs> obs = os.getObservations(null, null, null, null, null, null, null, null, null, sd, ed, false);
		assertEquals(8, obs.size());
		
		// Test 2, From boundary
		sd = df.parse("2006-02-13");
		ed = df.parse("2006-02-20");
		obs = os.getObservations(null, null, null, null, null, null, null, null, null, sd, ed, false);
		assertEquals(4, obs.size());
		
		// Test 3, To boundary
		sd = df.parse("2006-02-01");
		ed = df.parse("2006-02-15");
		obs = os.getObservations(null, null, null, null, null, null, null, null, null, sd, ed, false);
		assertEquals(7, obs.size());
		
		// Test 4, Both Boundaries
		sd = df.parse("2006-02-11");
		ed = new SimpleDateFormat("yyyy-MM-dd-hh-mm").parse("2006-02-11-11-59");
		obs = os.getObservations(null, null, null, null, null, null, null, null, null, sd, ed, false);
		assertEquals(1, obs.size());
		
		// Test 5, Outside before
		sd = df.parse("2006-02-01");
		ed = df.parse("2006-02-08");
		obs = os.getObservations(null, null, null, null, null, null, null, null, null, sd, ed, false);
		assertEquals(0, obs.size());
		
		// Test 6, Outside After
		sd = df.parse("2006-02-17");
		ed = df.parse("2006-02-20");
		obs = os.getObservations(null, null, null, null, null, null, null, null, null, sd, ed, false);
		assertEquals(0, obs.size());
	}
	
	/**
	 * Uses the OpenmrsUtil.lastSecondOfDay(Date) method to get all observations for a given day
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldGetObservationsOnDay() throws Exception {
		executeDataSet(INITIAL_OBS_XML);
		
		ObsService os = Context.getObsService();
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		
		Date sd = df.parse("2006-02-13");
		Date ed = df.parse("2006-02-13");
		List<Obs> obs = os.getObservations(null, null, null, null, null, null, null, null, null, sd, OpenmrsUtil
		        .lastSecondOfDay(ed), false);
		assertEquals(1, obs.size());
	}
	
	/**
	 * This method gets observations and only fetches obs that are for users
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldGetObservationsRestrictedToUsers() throws Exception {
		executeDataSet(INITIAL_OBS_XML);
		
		ObsService os = Context.getObsService();
		
		Concept c = new Concept(1); // a pseudo RETURN VISIT DATE
		List<Concept> questions = new Vector<Concept>();
		questions.add(c);
		
		List<PERSON_TYPE> personTypes = new Vector<PERSON_TYPE>();
		personTypes.add(PERSON_TYPE.PATIENT);
		
		List<Obs> obs = os.getObservations(c, "location.locationId asc, obs.valueDatetime asc", ObsService.USER, true);
		
		assertEquals(2, obs.size());
		//os.getObservations(null, null, questions, null, personTypes, null, "obs.valueDatetime asc", null, null, null, null, false);
	}
	
	/**
	 * @see {@link ObsService#getComplexObs(Integer,String)}
	 */
	@Test
	@Verifies(value = "should fill in complex data object for complex obs", method = "getComplexObs(Integer,String)")
	public void getComplexObs_shouldFillInComplexDataObjectForComplexObs() throws Exception {
		executeDataSet(COMPLEX_OBS_XML);
		
		ObsService os = Context.getObsService();
		
		Obs complexObs = os.getComplexObs(44, OpenmrsConstants.RAW_VIEW);
		
		Assert.assertNotNull(complexObs);
		Assert.assertTrue(complexObs.isComplex());
		Assert.assertNotNull(complexObs.getValueComplex());
		Assert.assertNotNull(complexObs.getComplexData());
	}
	
	/**
	 * @see {@link ObsService#getComplexObs(Integer,String)}
	 */
	@Test
	@Verifies(value = "should not fail with null view", method = "getComplexObs(Integer,String)")
	public void getComplexObs_shouldNotFailWithNullView() throws Exception {
		executeDataSet(COMPLEX_OBS_XML);
		
		ObsService os = Context.getObsService();
		
		os.getComplexObs(44, null);
	}
	
	/**
	 * @see {@link ObsService#getComplexObs(Integer,String)}
	 */
	@Test
	@Verifies(value = "should return normal obs for non complex obs", method = "getComplexObs(Integer,String)")
	public void getComplexObs_shouldReturnNormalObsForNonComplexObs() throws Exception {
		executeDataSet(COMPLEX_OBS_XML);
		
		ObsService os = Context.getObsService();
		
		Obs normalObs = os.getComplexObs(7, OpenmrsConstants.RAW_VIEW);
		
		Assert.assertFalse(normalObs.isComplex());
	}
	
	/**
	 * @see {@link ObsService#getHandler(String)}
	 */
	@Test
	@Verifies(value = "should have default image and text handlers registered by spring", method = "getHandler(String)")
	public void getHandler_shouldHaveDefaultImageAndTextHandlersRegisteredBySpring() throws Exception {
		ObsService os = Context.getObsService();
		ComplexObsHandler imgHandler = os.getHandler("ImageHandler");
		Assert.assertNotNull(imgHandler);
		
		ComplexObsHandler textHandler = os.getHandler("TextHandler");
		Assert.assertNotNull(textHandler);
	}
	
	/**
	 * @see {@link ObsService#getHandler(String)}
	 */
	@Test
	@Verifies(value = "should get handler with matching key", method = "getHandler(String)")
	public void getHandler_shouldGetHandlerWithMatchingKey() throws Exception {
		ObsService os = Context.getObsService();
		ComplexObsHandler handler = os.getHandler("ImageHandler");
		Assert.assertNotNull(handler);
		Assert.assertTrue(handler instanceof ImageHandler);
	}
	
	/**
	 * @see {@link ObsService#getHandlers()}
	 */
	@Test
	@Verifies(value = "should never return null", method = "getHandlers()")
	public void getHandlers_shouldNeverReturnNull() throws Exception {
		Assert.assertNotNull(Context.getObsService().getHandlers());
		
		// test our current implementation without it being initialized by spring
		Assert.assertNotNull(new ObsServiceImpl().getHandlers());
	}
	
	/**
	 * @see {@link ObsService#registerHandler(String,ComplexObsHandler)}
	 */
	@Test
	@Verifies(value = "should register handler with the given key", method = "registerHandler(String,ComplexObsHandler)")
	public void registerHandler_shouldRegisterHandlerWithTheGivenKey() throws Exception {
		ObsService os = Context.getObsService();
		
		os.registerHandler("DummyHandler", new ImageHandler());
		
		ComplexObsHandler dummyHandler = os.getHandler("DummyHandler");
		Assert.assertNotNull(dummyHandler);
	}
	
	/**
	 * @see {@link ObsService#registerHandler(String,String)}
	 */
	@Test
	@Verifies(value = "should load handler and register key", method = "registerHandler(String,String)")
	public void registerHandler_shouldLoadHandlerAndRegisterKey() throws Exception {
		ObsService os = Context.getObsService();
		
		// name it something other than what we used in the previous test
		os.registerHandler("DummyHandler2", "org.openmrs.obs.handler.ImageHandler");
		
		ComplexObsHandler dummyHandler = os.getHandler("DummyHandler2");
		Assert.assertNotNull(dummyHandler);
	}
	
	/**
	 * @see {@link ObsService#removeHandler(String)}
	 */
	@Test
	@Verifies(value = "should not fail with invalid key", method = "removeHandler(String)")
	public void removeHandler_shouldNotFailWithInvalidKey() throws Exception {
		Context.getObsService().removeHandler("SomeRandomHandler");
	}
	
	/**
	 * @see {@link ObsService#removeHandler(String)}
	 */
	@Test
	@Verifies(value = "should remove handler with matching key", method = "removeHandler(String)")
	public void removeHandler_shouldRemoveHandlerWithMatchingKey() throws Exception {
		ObsService os = Context.getObsService();
		
		// add the handler and make sure its there
		os.registerHandler("DummyHandler3", "org.openmrs.obs.handler.ImageHandler");
		ComplexObsHandler dummyHandler = os.getHandler("DummyHandler3");
		Assert.assertNotNull(dummyHandler);
		
		// now remove the handler and make sure its gone
		os.removeHandler("DummyHandler3");
		ComplexObsHandler dummyHandlerAgain = os.getHandler("DummyHandler3");
		Assert.assertNull(dummyHandlerAgain);
	}
	
	/**
	 * @see {@link ObsService#saveObs(Obs,String)}
	 */
	@Test
	@Verifies(value = "should create new file from complex data for new obs", method = "saveObs(Obs,String)")
	public void saveObs_shouldCreateNewFileFromComplexDataForNewObs() throws Exception {
		executeDataSet(COMPLEX_OBS_XML);
		ObsService os = Context.getObsService();
		ConceptService cs = Context.getConceptService();
		AdministrationService as = Context.getAdministrationService();
		
		// make sure the file isn't there to begin with
		File complexObsDir = OpenmrsUtil.getDirectoryInApplicationDataDirectory(as
		        .getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_COMPLEX_OBS_DIR));
		File createdFile = new File(complexObsDir, "nameOfFile.txt");
		if (createdFile.exists())
			createdFile.delete();
		
		// the complex data to put onto an obs that will be saved
		InputStream inputStream = new ByteArrayInputStream("This is a string to save to a file".getBytes());
		ComplexData complexData = new ComplexData("nameOfFile.txt", inputStream);
		
		// must fetch the concept instead of just new Concept(8473) because the attributes on concept are checked
		// this is a concept mapped to the text handler
		Concept questionConcept = cs.getConcept(8474);
		
		Obs obsToSave = new Obs(new Person(1), questionConcept, new Date(), new Location(1));
		obsToSave.setComplexData(complexData);
		
		try {
			os.saveObs(obsToSave, null);
			
			// make sure the file appears now after the save
			Assert.assertTrue(createdFile.exists());
		}
		finally {
			// we always have to delete this inside the same unit test because it is outside the
			// database and hence can't be "rolled back" like everything else
			createdFile.delete();
		}
	}
	
	/**
	 * @see {@link ObsService#saveObs(Obs,String)}
	 */
	@Test
	@Verifies(value = "should not overwrite file when updating a complex obs", method = "saveObs(Obs,String)")
	public void saveObs_shouldNotOverwriteFileWhenUpdatingAComplexObs() throws Exception {
		executeDataSet(COMPLEX_OBS_XML);
		ObsService os = Context.getObsService();
		ConceptService cs = Context.getConceptService();
		AdministrationService as = Context.getAdministrationService();
		
		// Create the file that was supposedly put there by another obs
		File complexObsDir = OpenmrsUtil.getDirectoryInApplicationDataDirectory(as
		        .getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_COMPLEX_OBS_DIR));
		File previouslyCreatedFile = new File(complexObsDir, "nameOfFile.txt");
		InputStream inputStream = new ByteArrayInputStream("a string to save to a file".getBytes());
		OpenmrsUtil.copyFile(inputStream, new FileOutputStream(previouslyCreatedFile));
		inputStream.close();
		
		// the file we'll be creating...defining it here so we can delete it in a finally block
		File newComplexFile = null;
		try {
			
			long oldFileSize = previouslyCreatedFile.length();
			
			// now add a new file to this obs and update it
			// ...then make sure the original file is still there
			
			// the complex data to put onto an obs that will be saved
			InputStream inputStream2 = new ByteArrayInputStream("diff string to save to a file with the same name"
			        .getBytes());
			ComplexData complexData = new ComplexData("nameOfFile.txt", inputStream2);
			
			// must fetch the concept instead of just new Concept(8473) because the attributes on concept are checked
			// this is a concept mapped to the text handler
			Concept questionConcept = cs.getConcept(8474);
			
			Obs obsToSave = new Obs(new Person(1), questionConcept, new Date(), new Location(1));
			
			obsToSave.setComplexData(complexData);
			
			os.saveObs(obsToSave, null);
			
			// make sure the old file still appears now after the save
			Assert.assertEquals(oldFileSize, previouslyCreatedFile.length());
			
			String valueComplex = obsToSave.getValueComplex();
			String filename = valueComplex.substring(valueComplex.indexOf("|") + 1).trim();
			newComplexFile = new File(complexObsDir, filename);
			// make sure the file appears now after the save
			Assert.assertTrue(newComplexFile.length() > oldFileSize);
		}
		finally {
			// clean up the files we created
			newComplexFile.delete();
			try {
				previouslyCreatedFile.delete();
			}
			catch (Throwable t) {
				// pass 
			}
		}
		
	}
	
	/**
	 * @see {@link ObsService#setHandlers(Map<QString;QComplexObsHandler;>)}
	 */
	@Test
	@Verifies(value = "should add new handlers with new keys", method = "setHandlers(Map<QString;QComplexObsHandler;>)")
	public void setHandlers_shouldAddNewHandlersWithNewKeys() throws Exception {
		ObsService os = Context.getObsService();
		
		Map<String, ComplexObsHandler> handlers = new HashMap<String, ComplexObsHandler>();
		handlers.put("DummyHandler4", new ImageHandler());
		handlers.put("DummyHandler5", new TextHandler());
		
		// set the handlers and make sure they're there
		os.setHandlers(handlers);
		
		ComplexObsHandler dummyHandler4 = os.getHandler("DummyHandler4");
		Assert.assertNotNull(dummyHandler4);
		
		ComplexObsHandler dummyHandler5 = os.getHandler("DummyHandler5");
		Assert.assertNotNull(dummyHandler5);
	}
	
	/**
	 * @see {@link ObsService#setHandlers(Map<QString;QComplexObsHandler;>)}
	 */
	@Test
	@Verifies(value = "should override handlers with same key", method = "setHandlers(Map<QString;QComplexObsHandler;>)")
	public void setHandlers_shouldOverrideHandlersWithSameKey() throws Exception {
		ObsService os = Context.getObsService();
		
		Map<String, ComplexObsHandler> handlers = new HashMap<String, ComplexObsHandler>();
		handlers.put("DummyHandlerToOverride", new ImageHandler());
		
		// set the handlers and make sure they're there
		os.setHandlers(handlers);
		
		ComplexObsHandler dummyHandlerToOverride = os.getHandler("DummyHandlerToOverride");
		Assert.assertTrue(dummyHandlerToOverride instanceof ImageHandler);
		
		// now override that key and make sure the new class is stored
		
		Map<String, ComplexObsHandler> handlersAgain = new HashMap<String, ComplexObsHandler>();
		handlersAgain.put("DummyHandlerToOverride", new TextHandler());
		
		os.setHandlers(handlersAgain);
		
		ComplexObsHandler dummyHandlerToOverrideAgain = os.getHandler("DummyHandlerToOverride");
		Assert.assertTrue(dummyHandlerToOverrideAgain instanceof TextHandler);
		
	}
	
	/**
	 * @see {@link ObsService#saveObs(Obs,String)}
	 */
	@Test
	@Verifies(value = "should void the given obs in the database", method = "saveObs(Obs,String)")
	public void saveObs_shouldVoidTheGivenObsInTheDatabase() throws Exception {
		Obs obs = Context.getObsService().getObs(7);
		obs.setValueNumeric(1.0);
		Context.getObsService().saveObs(obs, "just testing");
		
		// fetch the obs from the database again
		obs = Context.getObsService().getObs(7);
		Assert.assertTrue(obs.isVoided());
	}

	/**
	 * @see {@link ObsService#getObsByUuid(String)}
	 * 
	 */
	@Test
	@Verifies(value = "should find object given valid uuid", method = "getObsByUuid(String)")
	public void getObsByUuid_shouldFindObjectGivenValidUuid() throws Exception {
		String uuid = "39fb7f47-e80a-4056-9285-bd798be13c63";
		Obs obs = Context.getObsService().getObsByUuid(uuid);
		Assert.assertEquals(7,(int) obs.getObsId());
	}

	/**
	 * @see {@link ObsService#getObsByUuid(String)}
	 * 
	 */
	@Test
	@Verifies(value = "should return null if no object found with given uuid", method = "getObsByUuid(String)")
	public void getObsByUuid_shouldReturnNullIfNoObjectFoundWithGivenUuid()
			throws Exception {
		Assert.assertNull(Context.getObsService().getObsByUuid("some invalid uuid"));
	}	
}

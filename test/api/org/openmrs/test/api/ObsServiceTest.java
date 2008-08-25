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

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Location;
import org.openmrs.MimeType;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.LocationService;
import org.openmrs.api.ObsService;
import org.openmrs.api.context.Context;
import org.openmrs.test.testutil.BaseContextSensitiveTest;
import org.openmrs.util.OpenmrsConstants.PERSON_TYPE;
import org.openmrs.validator.ObsValidator;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

/**
 * TODO clean up and add tests for all methods in ObsService
 */
public class ObsServiceTest extends BaseContextSensitiveTest {
	
	protected static final String INITIAL_OBS_XML = "org/openmrs/test/api/include/ObsServiceTest-initial.xml";
	
	@Before
	public void runBeforeEachTest() throws Exception {
		initializeInMemoryDatabase();
		authenticate();
	}

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
		Encounter encounter1 = (Encounter)es.getEncounter(1);
		Date datetime1 = new Date();
		Location location1 = locationService.getLocation(1);
		Integer groupId1 = new Integer(1);
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
		System.out.println("patient2: " + patient2.getPatientId());
		Encounter encounter2 = (Encounter)es.getEncounter(2);
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
		System.out.println("o3.isComplex? " + o3.isComplexObs());
		
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
	 * Creates then updates a complex obs
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldComplexObsCreateUpdateDelete() throws Exception {
		
		// we don't have any complex obs in the system yet
	}	
	
	/**
	 * TODO 
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldMimeType() throws Exception {
		
		ObsService obsService = Context.getObsService();
		
		//testing equals()/hashcode() for mimetype /////////// 
		
		Collection<MimeType> mimeTypes = new HashSet<MimeType>();
		
		MimeType m1 = new MimeType();
		MimeType m2 = new MimeType();
		m1.setMimeType("test1");
		m2.setMimeType("test2");
		mimeTypes.add(m1);
		mimeTypes.add(m2);
		
		assertTrue("Both types should have been added", mimeTypes.size() == 2);
		assertTrue("The first mimetype should be in the list", mimeTypes.contains(m1));
		////////////////////////////////////////
		
		
		//testing creation
		
		MimeType mimeType = new MimeType();
		
		mimeType.setMimeType("testing");
		mimeType.setDescription("desc");
		
		obsService.saveMimeType(mimeType);
		
		MimeType newMimeType = obsService.getMimeType(mimeType.getMimeTypeId());
		assertNotNull(newMimeType);
		
		mimeTypes = obsService.getAllMimeTypes();
		
		//make sure we get a list
		assertNotNull(mimeTypes);
		
		boolean found = false;
		for(Iterator<MimeType> i = mimeTypes.iterator(); i.hasNext();) {
			MimeType mimeType2 = i.next();
			assertNotNull(mimeType);
			//check .equals function
			assertTrue(mimeType.equals(mimeType2) == (mimeType.getMimeTypeId().equals(mimeType2.getMimeTypeId())));
			//mark found flag
			if (mimeType.equals(mimeType2))
				found = true;
		}
		
		//assert that the new mimeType was returned in the list
		assertTrue(found);
		
		
		//check update
		newMimeType.setMimeType("another test");
		obsService.saveMimeType(newMimeType);
		
		MimeType newerMimeType = obsService.getMimeType(newMimeType.getMimeTypeId());
		assertTrue(newerMimeType.getMimeType().equals(newMimeType.getMimeType()));
		
		
		//check deletion
		obsService.purgeMimeType(newerMimeType);
		
		assertNull(obsService.getMimeType(newMimeType.getMimeTypeId()));

	}
	
	/**
	 * Tests the auto updating of the creator and dateCreated attrs 
	 * when saving an obs
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
	 * Auto generated method comment
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldObsValidator() throws Exception {
		executeDataSet(INITIAL_OBS_XML);
		ConceptService conceptService = Context.getConceptService();
		Concept numeric = conceptService.getConcept(1);
		ObsValidator validator = new ObsValidator();

		Obs obs = new Obs();
		
		// set the required properties
		obs.setPerson(new Person(1));
		obs.setObsDatetime(new Date());
		
		Errors errors = new BindException(obs, "obs");
		validator.validate(obs, errors);
		assertTrue("Should have errors: no question", errors.hasErrors());

		obs.setConcept(numeric);
		errors = new BindException(obs, "obs");
		validator.validate(obs, errors);
		assertTrue("Should have errors: no value", errors.hasErrors());

		obs.setValueText("This is text");
		errors = new BindException(obs, "obs");
		validator.validate(obs, errors);
		assertTrue("Should have errors: no numeric value", errors.hasErrors());

		obs.setValueNumeric(350d);
		errors = new BindException(obs, "obs");
		validator.validate(obs, errors);
		assertFalse("Should have no errors.  But has: " + errors, errors.hasErrors());

		Person p = new Person(1);
		
		Obs parent = new Obs();
		parent.setPerson(p);
		parent.setConcept(numeric);
		parent.setValueNumeric(350d);
		Obs child = new Obs();
		child.setPerson(p);
		parent.addGroupMember(child);
		errors = new BindException(parent, "obs");
		validator.validate(parent, errors);
		assertTrue("Should have errors: child is bad", errors.hasErrors());
		
		child.setConcept(numeric);
		child.setValueNumeric(125d);
		errors = new BindException(parent, "obs");
		validator.validate(parent, errors);
		assertFalse("Should have no errors", errors.hasErrors());
		
		child.addGroupMember(parent);
		errors = new BindException(parent, "obs");
		validator.validate(parent, errors);
		assertTrue("Should have errors: cycle in graph", errors.hasErrors());
		
		child.removeGroupMember(parent);
		Obs grandChild = new Obs();
		grandChild.setPerson(p);
		grandChild.setConcept(numeric);
		grandChild.setValueNumeric(77d);
		child.addGroupMember(grandChild);
		grandChild.addGroupMember(parent);
		errors = new BindException(parent, "obs");
		validator.validate(parent, errors);
		assertTrue("Should have errors: cycle in graph", errors.hasErrors());
	}
	
	/**
	 * This tests certain aspects of saving obs that are obsGroups or are
	 * members of other obsGroups
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
		Obs firstMember = (Obs)(obsGroup.getGroupMembers().toArray()[0]);
		Obs secondMember = (Obs)(obsGroup.getGroupMembers().toArray()[1]);
		if (firstMember.getConcept().equals(new Concept(1))) {
			// the set of members gets jumpbled after save.  The first one
			// we added above had a concept with id 1.
			assertEquals(groupMember1OriginalObsId, firstMember.getObsId());
			// make sure the second group member is still there
			assertEquals(groupMember2, secondMember);
		}
		else {
			assertEquals(groupMember1OriginalObsId, secondMember.getObsId());
			// make sure the second group member is still there
			assertEquals(groupMember2, firstMember);
		}
		
	}
	
	/**
	 * 
	 * This test tests multi-level heirarchy obsGroup cascades for create, delete, update, void, and unvoid
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
						for (Obs child:parent.getGroupMembers()){
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
						for (Obs child : parent.getGroupMembers()){
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
	 * Creates a simple Obs Group consisting of one parent and one child.
	 * Assumes INITIAL_OBS_XML is being used.
	 */
	private void createObsGroup(Obs parent, Obs child, ConceptService cs, ObsService os){
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
	 * This test makes sure that child obs on a parent obs are given an obs group id when the 
	 * parent obs is created
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
	 * 
	 * Unit test for findObsByGroupId... yeah, it's deprecated, but doesn't hurt to check
	 * that it's doing what we want it to be doing.
	 * 
	 * @throws Throwable
	 */
	@Test
	public void shouldFindObsByGroupId() throws Throwable{
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
		
		List<Obs> obs4 = os.findObsByGroupId(oParent.getObsId());
		
		assertTrue(obs.equals(obs2));
		assertTrue(obs3.size()==0);
		assertTrue(obs4.contains(child));
		assertTrue(obs4.contains(child2));
	}
	
	/**
	 * This method gets observations and only fetches obs
	 * that are for patients
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
	 * This method gets observations and only fetches obs
	 * that are for users
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
}

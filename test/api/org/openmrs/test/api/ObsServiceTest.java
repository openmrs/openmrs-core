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
import java.util.Set;

import org.openmrs.ComplexObs;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Location;
import org.openmrs.MimeType;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.ObsService;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.validator.ObsValidator;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

/**
 * TODO clean up and add tests for all methods in ObsService
 */
public class ObsServiceTest extends BaseContextSensitiveTest {
	
	protected static final String INITIAL_OBS_XML = "org/openmrs/test/api/include/ObsServiceTest-initial.xml";
	
	@Override
	protected void onSetUpInTransaction() throws Exception {
		initializeInMemoryDatabase();
		authenticate();
	}

	/**
	 * Creates then updates an obs
	 * 
	 * @throws Exception
	 */
	public void testObsCreateUpdateDelete() throws Exception {
		
		executeDataSet(INITIAL_OBS_XML);
		
		EncounterService es = Context.getEncounterService();
		ObsService obsService = Context.getObsService();
		ConceptService conceptService = Context.getConceptService();

		Obs o = new Obs();
		
		//testing creation
		
		Order order1 = null;
		Concept concept1 = conceptService.getConcept(1);
		Patient patient1 = new Patient(2);  // TODO need to create an actual mock patient
		Encounter encounter1 = (Encounter)es.getEncounter(1);
		Date datetime1 = new Date();
		Location location1 = es.getLocation(1);
		Integer valueGroupId1 = new Integer(5);
		//boolean valueBoolean1 = true;
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
		//o.setValueBoolean(valueBoolean1);
		o.setValueDatetime(valueDatetime1);
		o.setValueCoded(valueCoded1);
		o.setValueNumeric(valueNumeric1);
		o.setValueModifier(valueModifier1);
		o.setValueText(valueText1);
		o.setComment(comment1);
		
		obsService.createObs(o);
		
		Obs o2 = obsService.getObs(o.getObsId());
		assertNotNull(o2);
		
		Order order2 = null;
		Concept concept2 = conceptService.getConcept(2);
		Patient patient2 = new Patient(1); // TODO need to create an actual mock patient 2
		System.out.println("patient2: " + patient2.getPatientId());
		Encounter encounter2 = (Encounter)es.getEncounter(2);
		Date datetime2 = new Date();
		Location location2 = es.getLocation(1);
		Integer valueGroupId2 = new Integer(3);
		//boolean valueBoolean2 = false;
		Date valueDatetime2 = new Date();
		Concept valueCoded2 = conceptService.getConcept(2);
		Double valueNumeric2 = 2.0;
		String valueModifier2 = "cc";
		String valueText2 = "value text2";
		String comment2 = "commenting2";
		
		o2.setOrder(order2);
		o2.setConcept(concept2);
		o2.setPerson(patient2);
		o2.setEncounter(encounter2);
		o2.setObsDatetime(datetime2);
		o2.setLocation(location2);
		o2.setValueGroupId(valueGroupId2);
		//o2.setValueBoolean(valueBoolean2);
		o2.setValueDatetime(valueDatetime2);
		o2.setValueCoded(valueCoded2);
		o2.setValueNumeric(valueNumeric2);
		o2.setValueModifier(valueModifier2);
		o2.setValueText(valueText2);
		o2.setComment(comment2);
		
		obsService.updateObs(o2);
		
		Obs o3 = obsService.getObs(o2.getObsId());
		System.out.println("o3.isComplex(): " + o3.isComplexObs());
		
		//o2 should equal o3 and neither should equal o1
		
		assertTrue(o3.equals(o));
		if (order2 != null)
			assertTrue(o3.getOrder().equals(order2));
		assertTrue(o3.getPerson().equals(patient2));
		assertTrue(o3.getComment().equals(comment2));
		assertTrue(o3.getConcept().equals(concept2));
		assertTrue(o3.getEncounter().equals(encounter2));
		assertTrue(o3.getObsDatetime().equals(datetime2));
		assertTrue(o3.getLocation().equals(location2));
		assertTrue(o3.getValueGroupId().equals(valueGroupId2));
		//assertTrue(o3.getValueBoolean().equals(valueBoolean2));
		assertTrue(o3.getValueDatetime().equals(valueDatetime2));
		assertTrue(o3.getValueCoded().equals(valueCoded2));
		assertTrue(o3.getValueNumeric().equals(valueNumeric2));
		assertTrue(o3.getValueModifier().equals(valueModifier2));
		assertTrue(o3.getValueText().equals(valueText2));
		
		obsService.voidObs(o, "testing void function");
		
		Obs o4 = obsService.getObs(o.getObsId());
		
		assertTrue(o4.getVoidReason().equals("testing void function"));
		assertTrue(o4.getVoidedBy().equals(o3.getVoidedBy()));
		assertTrue(o4.isVoided());
		
		obsService.deleteObs(o);
		//TODO what to do on multiple delete?
		//obsService.deleteObs(o3); //gratuitous
		
		assertNull(obsService.getObs(o.getObsId()));
		
	}	
	
	/**
	 * Creates then updates a complex obs
	 * 
	 * @throws Exception
	 */
	public void testComplexObsCreateUpdateDelete() throws Exception {
		
		executeDataSet(INITIAL_OBS_XML);
		
		EncounterService es = Context.getEncounterService();
		ObsService obsService = Context.getObsService();
		ConceptService conceptService = Context.getConceptService();
		AdministrationService as = Context.getAdministrationService();
		
		// create the mock mime type;
		MimeType mimetype1 = new MimeType();
		mimetype1.setDescription("desc");
		mimetype1.setMimeType("mimetype1");
		as.createMimeType(mimetype1);
		
		ComplexObs o = new ComplexObs();
		
		//testing creation
		
		Order order1 = null;
		Concept concept1 = conceptService.getConcept(1);
		Patient patient1 = new Patient(2); // TODO need to create an actual mock patient
		Encounter encounter1 = (Encounter)es.getEncounter(1);
		Date datetime1 = new Date();
		Location location1 = es.getLocation(2);
		Integer valueGroupId1 = new Integer(5);
		//boolean valueBoolean1 = true;
		Date valueDatetime1 = new Date();
		Concept valueCoded1 = conceptService.getConcept(2);
		Double valueNumeric1 = 1.0;
		String valueModifier1 = "a1";
		String valueText1 = "value text1";
		String comment1 = "commenting1";
		String urn1 = "urn1";
		String complexValue1 = "complex value1";
		
		o.setOrder(order1);
		o.setConcept(concept1);
		o.setPerson(patient1);
		o.setEncounter(encounter1);
		o.setObsDatetime(datetime1);
		o.setLocation(location1);
		o.setValueGroupId(valueGroupId1);
		//o.setValueBoolean(valueBoolean1);
		o.setValueDatetime(valueDatetime1);
		o.setValueCoded(valueCoded1);
		o.setValueNumeric(valueNumeric1);
		o.setValueModifier(valueModifier1);
		o.setValueText(valueText1);
		o.setComment(comment1);
		o.setMimeType(mimetype1);
		o.setUrn(urn1);
		o.setComplexValue(complexValue1);
		
		obsService.createObs(o);
		
		ComplexObs o2 = (ComplexObs)obsService.getObs(o.getObsId());
		assertNotNull(o2);
		
		Order order2 = null;
		Concept concept2 = conceptService.getConcept(2);
		Patient patient2 = new Patient(2);  // TODO need to create an actual mock patient
		Encounter encounter2 = (Encounter)es.getEncounter(2);
		Date datetime2 = new Date();
		Location location2 = es.getLocation(2);
		Integer valueGroupId2 = new Integer(3);
		//boolean valueBoolean2 = false;
		Date valueDatetime2 = new Date();
		Concept valueCoded2 = conceptService.getConcept(1);
		Double valueNumeric2 = 2.0;
		String valueModifier2 = "cc";
		String valueText2 = "value text2";
		String comment2 = "commenting2";
		MimeType mimetype2 = mimetype1;
		String urn2 = "urn2";
		String complexValue2 = "complex value2";
		
		o2.setOrder(order2);
		o2.setConcept(concept2);
		o2.setPerson(patient2);
		o2.setEncounter(encounter2);
		o2.setObsDatetime(datetime2);
		o2.setLocation(location2);
		o2.setValueGroupId(valueGroupId2);
		//o2.setValueBoolean(valueBoolean2);
		o2.setValueDatetime(valueDatetime2);
		o2.setValueCoded(valueCoded2);
		o2.setValueNumeric(valueNumeric2);
		o2.setValueModifier(valueModifier2);
		o2.setValueText(valueText2);
		o2.setComment(comment2);
		o2.setMimeType(mimetype2);
		o2.setUrn(urn2);
		o2.setComplexValue(complexValue2);
		
		obsService.updateObs(o2);
		
		ComplexObs o3 = (ComplexObs)obsService.getObs(o2.getObsId());
		Obs o7 = obsService.getObs(o2.getObsId());
		System.out.println("o7.isComplex(): " + o7.isComplexObs());
		//o2=03=o but 
		//(values of o2 = values of o3) != values of o
		
		assertTrue(o3.equals(o));
		if (order2 != null)
			assertTrue(o3.getOrder().equals(order2));
		assertTrue(o3.getPerson().equals(patient2));
		assertTrue(o3.getComment().equals(comment2));
		assertTrue(o3.getConcept().equals(concept2));
		assertTrue(o3.getEncounter().equals(encounter2));
		assertTrue(o3.getObsDatetime().equals(datetime2));
		assertTrue(o3.getLocation().equals(location2));
		assertTrue(o3.getValueGroupId().equals(valueGroupId2));
		//assertTrue(o3.getValueBoolean().equals(valueBoolean2));
		assertTrue(o3.getValueDatetime().equals(valueDatetime2));
		assertTrue(o3.getValueCoded().equals(valueCoded2));
		assertTrue(o3.getValueNumeric().equals(valueNumeric2));
		assertTrue(o3.getValueModifier().equals(valueModifier2));
		assertTrue(o3.getValueText().equals(valueText2));
		assertTrue(o3.getMimeType().equals(mimetype2));
		assertTrue(o3.getUrn().equals(urn2));
		assertTrue(o3.getComplexValue().equals(complexValue2));
		
		obsService.voidObs(o, "testing void function");
		
		Obs o4 = obsService.getObs(o.getObsId());
		
		assertNotNull(o4.getVoidReason());
		assertTrue(o4.getVoidReason().equals("testing void function"));
		assertTrue(o4.getVoidedBy().equals(o3.getVoidedBy()));
		assertTrue(o4.isVoided());
		
		obsService.deleteObs(o);
		//TODO what to do on multiple delete?
		//obsService.deleteObs(o3); //gratuitous
		
		assertNull(obsService.getObs(o.getObsId()));
	}	
	
	/**
	 * Auto generated method comment
	 * 
	 * @throws Exception
	 */
	public void testObsValidator() throws Exception {
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
	public void testObsGroupCreateUpdate() throws Exception {
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
		
		obsService.createObs(obsGroup);
		
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
		
		obsService.updateObs(obsGroup);
		
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
	 * This test tests multi-level heirarchy obsGroup cascads for create, delete, update, void, and unvoid
	 * 
	 * @throws Exception
	 */
	public void testSaveUpdateDeleteVoidObsGroupCascades() throws Exception {
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
		o.setValueText("test");

		//create a second obs
		Obs o2 = new Obs();
		o2.setConcept(cs.getConcept(1));
		o2.setDateCreated(new Date());
		o2.setCreator(Context.getAuthenticatedUser());
		o2.setLocation(new Location(1));
		o2.setObsDatetime(new Date());
		o2.setValueText("test");
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
		oGP.setValueText("test");

		oGP.addGroupMember(oParent);

		//create a leaf observation
		Obs o3 = new Obs();
		o3.setConcept(cs.getConcept(1));
		o3.setDateCreated(new Date());
		o3.setCreator(Context.getAuthenticatedUser());
		o3.setLocation(new Location(1));
		o3.setObsDatetime(new Date());
		o3.setValueText("test");
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
		oGGP.setValueText("test");
		oGGP.setPerson(new Patient(2));
		
		oGGP.addGroupMember(oGP);
		
		//create a great-great grandparent
		Obs oGGGP = new Obs();
		oGGGP.setConcept(cs.getConcept(1));
		oGGGP.setDateCreated(new Date());
		oGGGP.setCreator(Context.getAuthenticatedUser());
		oGGGP.setLocation(new Location(1));
		oGGGP.setObsDatetime(new Date());
		oGGGP.setValueText("test");
		oGGGP.setPerson(new Patient(2));

		oGGGP.addGroupMember(oGGP);

		//Create the great great grandparent
		os.createObs(oGGGP);
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
		
		os.updateObs(oGGGP);
		
		//now, re-walk the tree to verify that the bottom-level leaf obs have the new text value:
	
		int childOneId = 0;
		int childTwoId = 0;
		Obs testGGGP2 = os.getObs(oGGGPId);
		assertTrue(testGGGP2.isObsGrouping());
		Set<Obs> GGGPmembers2 = testGGGP2.getGroupMembers();
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
						for (Obs child:parent.getGroupMembers()){
							assertEquals(child.getValueText(),"testingUpdate");
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
		oVoidTest.setValueText("test");
		
		os.createObs(oVoidTest);
		int oVoidTestId = oVoidTest.getObsId();
		os.voidObs(oVoidTest, "testing void method");
		
		Obs oVoidTestTwo = os.getObs(oVoidTestId);
		assertTrue(oVoidTestTwo.getVoided());
		
		//unvoid:
		oVoidTestTwo.setVoided(false);
		oVoidTest = os.getObs(oVoidTestId);
		assertFalse(oVoidTest.isVoided());
		
		//Now test voiding cascade:
		// i.e. by voiding the grandparent, we void the n-th generation leaf obs
		os.voidObs(oGGGP, "testing void cascade");
		Obs childLeafObs = os.getObs(childOneId);
		assertTrue(childLeafObs.isVoided());
	
		//now test the un-void:
		os.unvoidObs(oGGGP);
		childLeafObs = os.getObs(childOneId);
		assertFalse(childLeafObs.isVoided());
		
		//test this again using just the os.updateObs method on the great great grandparent:
		
		oGGGP.setVoided(true);
		oGGGP.setDateVoided(new Date());
		oGGGP.setVoidedBy(Context.getAuthenticatedUser());
		oGGGP.setVoidReason("test");
		os.voidObs(oGGGP, "testing void cascade");
		childLeafObs = os.getObs(childOneId);
		assertTrue(childLeafObs.isVoided());
		
		oGGGP.setVoided(false);
		os.updateObs(oGGGP);
		childLeafObs = os.getObs(childOneId);
		assertFalse(childLeafObs.isVoided());
		
		
		//now, test the feature that unvoid doesn't happen unless child obs has the same dateVoided as
		// the Obj argument that gets passed into unvoid:
		
		os.voidObs(oGGGP, "testing void cascade");
		childLeafObs = os.getObs(childOneId);
		assertTrue(childLeafObs.isVoided());
		
		childLeafObs.setDateVoided(new Date(childLeafObs.getDateVoided().getTime() - 5000)); 
		os.updateObs(childLeafObs);
		os.unvoidObs(oGGGP);
		
		childLeafObs = os.getObs(childOneId);
		Obs childLeafObsTwo = os.getObs(childTwoId);
		
		//childLeafObs had its date voided date changed, so it should not get unvoided by the unvoid cascade
		//childLeafObsTwo should be unvoided, as the dateVoided date is still the same as the great-great
		//grandparent Obs
		
		assertFalse(childLeafObsTwo.getVoided());
		assertTrue(childLeafObs.getVoided());
		
		//finally, check the delete cascade:
		
		os.deleteObs(oGGGP);
		
		assertNull(os.getObs(oGGGPId));
		assertNull(os.getObs(childOneId));
		assertNull(os.getObs(childTwoId));
	}
	
	/**
	 * This test makes sure that child obs on a parent obs are given an obs group id when the 
	 * parent obs is created
	 * 
	 * @throws Throwable
	 */
	public void testCreateObsGroupId() throws Throwable {
		
		executeDataSet(INITIAL_OBS_XML);

		ConceptService cs = Context.getConceptService();
		ObsService os = Context.getObsService();
		
		Obs o2 = new Obs();
		o2.setConcept(cs.getConcept(1));
		o2.setDateCreated(new Date());
		o2.setCreator(Context.getAuthenticatedUser());
		o2.setLocation(new Location(1));
		o2.setObsDatetime(new Date());
		o2.setValueText("test");
		o2.setPerson(new Patient(2));

		//create a parent obs
		Obs oParent = new Obs();
		oParent.setConcept(cs.getConcept(2)); //in the concept set table as a set
		oParent.setDateCreated(new Date());
		oParent.setCreator(Context.getAuthenticatedUser());
		oParent.setLocation(new Location(1));
		oParent.setObsDatetime(new Date());
		oParent.setPerson(new Patient(2));
		
		oParent.addGroupMember(o2);
		
		os.createObs(oParent);
		
		// save the obs ids
		Integer parentObsId = oParent.getObsId();
		assertNotNull(parentObsId);
		
		Integer childObsId = o2.getObsId();
		assertNotNull(childObsId);
		
		// clear out the session so we can refetch and test that it saved correctly
		oParent = null;
		o2 = null;
		Context.clearSession();
		
		// try to get the same obs back and make sure it has children
		
		Obs fetchedParent = os.getObs(parentObsId);
		assertTrue(fetchedParent.isObsGrouping());
		assertEquals(1, fetchedParent.getGroupMembers().size());
		
		
	}
}

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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Cohort;
import org.openmrs.Concept;
import org.openmrs.DrugOrder;
import org.openmrs.EncounterType;
import org.openmrs.Form;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.PatientProgram;
import org.openmrs.PatientState;
import org.openmrs.Person;
import org.openmrs.Program;
import org.openmrs.ProgramWorkflow;
import org.openmrs.ProgramWorkflowState;
import org.openmrs.Relationship;
import org.openmrs.RelationshipType;
import org.openmrs.api.PatientSetService.GroupMethod;
import org.openmrs.api.PatientSetService.Modifier;
import org.openmrs.api.PatientSetService.TimeModifier;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.TestUtil;
import org.openmrs.test.Verifies;

/**
 *
 */
public class PatientSetServiceTest extends BaseContextSensitiveTest {
	
	PatientSetService service;
	
	protected static final String EXTRA_DATA_XML = "org/openmrs/api/include/PatientSetServiceTest-extraData.xml";
	
	@Before
	public void getService() {
		service = Context.getPatientSetService();
	}
	
	/**
	 * @see {@link PatientSetService#getDrugOrders(Cohort,Concept)}
	 */
	@Test
	@Verifies(value = "should return an empty list if cohort is empty", method = "getDrugOrders(Cohort,Concept)")
	public void getDrugOrders_shouldReturnAnEmptyListIfCohortIsEmpty() throws Exception {
		Cohort nobody = new Cohort();
		Map<Integer, List<DrugOrder>> results = Context.getPatientSetService().getDrugOrders(nobody, null);
		assertNotNull(results);
	}
	
	/**
	 * @see {@link PatientSetService#getPatientsByCharacteristics(String,Date,Date,Integer,Integer,Boolean,Boolean)}
	 */
	@Test
	@Verifies(value = "should get all patients when no parameters given", method = "getPatientsByCharacteristics(String,Date,Date,Integer,Integer,Boolean,Boolean)")
	public void getPatientsByCharacteristics_shouldGetAllPatientsWhenNoParametersGiven() throws Exception {
		Cohort cohort = service.getPatientsByCharacteristics(null, null, null, null, null, null, null);
		Assert.assertEquals(4, cohort.size());
	}
	
	/**
	 * @see {@link PatientSetService#getPatientsByCharacteristics(String,Date,Date,Integer,Integer,Boolean,Boolean,Date)}
	 */
	@Test
	@Verifies(value = "should not get patients born after effectiveDate", method = "getPatientsByCharacteristics(String,Date,Date,Integer,Integer,Boolean,Boolean,Date)")
	public void getPatientsByCharacteristics_shouldNotGetPatientBornAfterEffectiveDate() throws Exception {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		Cohort cohort = null;
		cohort = service.getPatientsByCharacteristics(null, null, null, null, null, null, null, df.parse("1970-01-01"));
		Assert.assertEquals(1, cohort.size());
		cohort = service.getPatientsByCharacteristics(null, null, null, null, null, null, null, df.parse("1980-01-01"));
		Assert.assertEquals(3, cohort.size());
		cohort = service.getPatientsByCharacteristics(null, null, null, null, null, null, null, df.parse("2008-01-01"));
		Assert.assertEquals(4, cohort.size());
	}
	
	/**
	 * @see {@link PatientSetService#getPatientsByCharacteristics(String,Date,Date,Integer,Integer,Boolean,Boolean)}
	 */
	@Test
	@Verifies(value = "should get patients of given gender", method = "getPatientsByCharacteristics(String,Date,Date,Integer,Integer,Boolean,Boolean)")
	public void getPatientsByCharacteristics_shouldGetPatientsOfGivenGender() throws Exception {
		Cohort cohort = service.getPatientsByCharacteristics("m", null, null, null, null, null, null);
		Assert.assertEquals(2, cohort.size());
		Assert.assertTrue(cohort.contains(2));
		Assert.assertTrue(cohort.contains(6));
		
		cohort = service.getPatientsByCharacteristics("f", null, null, null, null, null, null);
		Assert.assertEquals(2, cohort.size());
		Assert.assertTrue(cohort.contains(7));
		Assert.assertTrue(cohort.contains(8));
	}
	
	/**
	 * @see {@link PatientSetService#getPatientsByCharacteristics(String,Date,Date,Integer,Integer,Boolean,Boolean)}
	 */
	@Test
	@Verifies(value = "should get patients who are alive", method = "getPatientsByCharacteristics(String,Date,Date,Integer,Integer,Boolean,Boolean)")
	public void getPatientsByCharacteristics_shouldGetPatientsWhoAreAlive() throws Exception {
		Cohort cohort = service.getPatientsByCharacteristics(null, null, null, null, null, true, null);
		Assert.assertEquals(4, cohort.size());
		Assert.assertTrue(cohort.contains(2));
		Assert.assertTrue(cohort.contains(6));
		Assert.assertTrue(cohort.contains(7));
		Assert.assertTrue(cohort.contains(8));
	}
	
	/**
	 * @see {@link PatientSetService#getPatientsByCharacteristics(String,Date,Date,Integer,Integer,Boolean,Boolean)}
	 */
	@Test
	@Verifies(value = "should get patients who are dead", method = "getPatientsByCharacteristics(String,Date,Date,Integer,Integer,Boolean,Boolean)")
	public void getPatientsByCharacteristics_shouldGetPatientsWhoAreDead() throws Exception {
		Cohort cohort = service.getPatientsByCharacteristics(null, null, null, null, null, null, true);
		Assert.assertEquals(0, cohort.size());
		//Assert.assertTrue(cohort.contains(2));
	}
	
	/**
	 * @see {@link PatientSetService#getPatientsByProgramAndState(org.openmrs.Program,List,Date, Date)}
	 */
	@Test
	@Verifies(value = "should get all patients in any program given null parameters", method = "getPatientsByProgramAndState(Program,List<QProgramWorkflowState;>,Date,Date)")
	public void getPatientsByProgramAndState_shouldGetAllPatientsInAnyProgramGivenNullParameters() throws Exception {
		Cohort cohort = service.getPatientsByProgramAndState(null, null, null, null);
		Assert.assertEquals(2, cohort.size());
		Assert.assertTrue(cohort.contains(2));
		Assert.assertTrue(cohort.contains(7));
	}
	
	/**
	 * @see {@link PatientSetService#getPatientsByProgramAndState(org.openmrs.Program,List,Date,Date)}
	 */
	@Test
	@Verifies(value = "should get patients in program", method = "getPatientsByProgramAndState(Program,List<QProgramWorkflowState;>,Date,Date)")
	public void getPatientsByProgramAndState_shouldGetPatientsInProgram() throws Exception {
		Cohort cohort = service.getPatientsByProgramAndState(Context.getProgramWorkflowService().getProgram(1), null, null,
		    null);
		Assert.assertEquals(1, cohort.size());
		Assert.assertTrue(cohort.contains(2));
	}
	
	/**
	 * @see {@link PatientSetService#getPatientsByProgramAndState(org.openmrs.Program, List, Date, Date)
	 */
	@Test
	@Verifies(value = "should get patients in state", method = "getPatientsByProgramAndState(Program,List<QProgramWorkflowState;>,Date,Date)")
	public void getPatientsByProgramAndState_shouldGetPatientsInState() throws Exception {
		ProgramWorkflowService pws = Context.getProgramWorkflowService();
		Cohort cohort = service.getPatientsByProgramAndState(pws.getProgram(1), Collections.singletonList(pws.getState(2)),
		    null, null);
		Assert.assertEquals(1, cohort.size());
		Assert.assertTrue(cohort.contains(2));
		
		cohort = service.getPatientsByProgramAndState(pws.getProgram(1), Collections.singletonList(pws.getState(4)), null,
		    null);
		Assert.assertEquals(0, cohort.size());
	}
	
	/**
	 * @see {@link PatientSetService#getPatientsByProgramAndState(org.openmrs.Program,List,Date,Date)}
	 */
	@Test
	@Verifies(value = "should get patients in states", method = "getPatientsByProgramAndState(Program,List<QProgramWorkflowState;>,Date,Date)")
	public void getPatientsByProgramAndState_shouldGetPatientsInStates() throws Exception {
		ProgramWorkflowService pws = Context.getProgramWorkflowService();
		List<ProgramWorkflowState> list = new ArrayList<ProgramWorkflowState>();
		list.add(pws.getState(2));
		list.add(pws.getState(4));
		Cohort cohort = service.getPatientsByProgramAndState(pws.getProgram(1), list, null, null);
		Assert.assertEquals(1, cohort.size());
		Assert.assertTrue(cohort.contains(2));
		
		//TODO also test having two states get multiple poeple
	}
	
	/**
	 * @see {@link PatientSetService#getPatientsHavingDrugOrder(java.util.Collection, java.util.Collection, GroupMethod, Date, Date)}
	 */
	@Test
	@Verifies(value = "should get all patients with drug orders given null parameters", method = "getPatientsHavingDrugOrder(Collection<QInteger;>,Collection<QInteger;>,GroupMethod,Date,Date)")
	public void getPatientsHavingDrugOrder_shouldGetAllPatientsWithDrugOrdersGivenNullParameters() throws Exception {
		Cohort cohort = service.getPatientsHavingDrugOrder(null, null, null, null, null);
		Assert.assertEquals(2, cohort.size());
		Assert.assertTrue(cohort.contains(2));
		Assert.assertTrue(cohort.contains(7));
	}
	
	/**
	 * @see {@link PatientSetService#getPatientsHavingDrugOrder(java.util.Collection, java.util.Collection, GroupMethod, Date, Date)}
	 */
	@Test
	@Verifies(value = "should get patients with no drug orders", method = "getPatientsHavingDrugOrder(Collection<QInteger;>,Collection<QInteger;>,GroupMethod,Date,Date)")
	public void getPatientsHavingDrugOrder_shouldGetPatientsWithNoDrugOrders() throws Exception {
		Cohort cohort = service.getPatientsHavingDrugOrder(null, null, GroupMethod.NONE, null, null);
		Assert.assertEquals(2, cohort.size());
		Assert.assertTrue(cohort.contains(6));
		Assert.assertTrue(cohort.contains(8));
	}
	
	/**
	 * @see {@link PatientSetService#getPatientsHavingDrugOrder(java.util.Collection, java.util.Collection, GroupMethod, Date, Date)}
	 */
	@Test
	@Verifies(value = "should get patients with no drug orders for drugs", method = "getPatientsHavingDrugOrder(Collection<QInteger;>,Collection<QInteger;>,GroupMethod,Date,Date)")
	public void getPatientsHavingDrugOrder_shouldGetPatientsWithNoDrugOrdersForDrugs() throws Exception {
		List<Integer> drugIds = new ArrayList<Integer>();
		drugIds.add(2);
		Cohort cohort = service.getPatientsHavingDrugOrder(drugIds, null, null, null, null);
		Assert.assertEquals(1, cohort.size());
		Assert.assertTrue(cohort.contains(2));
	}
	
	/**
	 * @see {@link PatientSetService#getPatientsHavingObs(Integer,TimeModifier,Modifier,Object,Date,Date)}
	 *      test = should get patients by concept and false boolean value
	 */
	@Test
	@Verifies(value = "should get patients by concept and false boolean value", method = "getPatientsHavingObs(Integer,TimeModifier,Modifier,Object,Date,Date)")
	public void getPatientsHavingObs_shouldGetPatientsByConceptAndFalseBooleanValue() throws Exception {
		// there aren't any of these obs in the standard test dataset. Verify that:
		Cohort cohort = service.getPatientsHavingObs(18, TimeModifier.ANY, Modifier.EQUAL, Boolean.FALSE, null, null);
		Assert.assertEquals(0, cohort.size());
		// now create a couple of these so we can try searching.
		DateFormat ymd = new SimpleDateFormat("yyyy-MM-dd");
		{ // start with a true one
			Obs obs = new Obs();
			obs.setPerson(new Person(8));
			obs.setConcept(Context.getConceptService().getConcept(18));
			obs.setObsDatetime(ymd.parse("2007-01-01"));
			obs.setValueBoolean(true);
			obs.setLocation(new Location(1));
			Context.getObsService().saveObs(obs, null);
		}
		{ // then do a false one
			Obs obs = new Obs();
			obs.setPerson(new Person(8));
			obs.setConcept(Context.getConceptService().getConcept(18));
			obs.setObsDatetime(ymd.parse("2008-01-01"));
			obs.setValueBoolean(false);
			obs.setLocation(new Location(1));
			Context.getObsService().saveObs(obs, null);
		}
		// search again
		cohort = service.getPatientsHavingObs(18, TimeModifier.FIRST, Modifier.EQUAL, Boolean.FALSE, null, null);
		Assert.assertEquals(0, cohort.size());
		cohort = service.getPatientsHavingObs(18, TimeModifier.LAST, Modifier.EQUAL, Boolean.FALSE, null, null);
		Assert.assertEquals(1, cohort.size());
		Assert.assertTrue(cohort.contains(8));
	}
	
	/**
	 * @see {@link PatientSetService#getPatientsHavingObs(Integer,TimeModifier,Modifier,Object,Date,Date)}
	 *      test = should get patients by concept and true boolean value
	 */
	@Test
	@Verifies(value = "should get patients by concept and true boolean value", method = "getPatientsHavingObs(Integer,TimeModifier,Modifier,Object,Date,Date)")
	public void getPatientsHavingObs_shouldGetPatientsByConceptAndTrueBooleanValue() throws Exception {
		Cohort cohort = service.getPatientsHavingObs(18, TimeModifier.ANY, Modifier.EQUAL, Boolean.TRUE, null, null);
		Assert.assertEquals(1, cohort.size());
		Assert.assertTrue(cohort.contains(7));
	}
	
	/**
	 * @see {@link PatientSetService#getPatientsHavingEncounters(List<QEncounterType;>,Location,Form,Date,Date,Integer,Integer)}
	 */
	@Test
	@Verifies(value = "should get patients with encounters of multiple types", method = "getPatientsHavingEncounters(List<QEncounterType;>,Location,Form,Date,Date,Integer,Integer)")
	public void getPatientsHavingEncounters_shouldGetPatientsWithEncountersOfMultipleTypes() throws Exception {
		executeDataSet(EXTRA_DATA_XML);
		List<EncounterType> list = new ArrayList<EncounterType>();
		list.add(new EncounterType(1));
		Cohort withOneType = service.getPatientsHavingEncounters(list, null, null, null, null, null, null);
		Assert.assertEquals(2, withOneType.size());
		list.add(new EncounterType(6));
		Cohort withTwoTypes = service.getPatientsHavingEncounters(list, null, null, null, null, null, null);
		Assert.assertEquals(2, withTwoTypes.size());
	}
	
	/**
	 * @see {@link PatientSetService#getPatientsHavingEncounters(EncounterType,Location,Form,Date,Date,Integer,Integer)}
	 */
	@Test
	@Verifies(value = "should get all patients with encounters when no parameters specified", method = "getPatientsHavingEncounters(List<EncounterType>,Location,Form,Date,Date,Integer,Integer)")
	public void getPatientsHavingEncounters_shouldGetAllPatientsWithEncountersWhenNoParametersSpecified() throws Exception {
		Cohort withEncs = Context.getPatientSetService().getPatientsHavingEncounters((EncounterType) null, null, null, null,
		    null, null, null);
		Assert.assertEquals(2, withEncs.size());
		Assert.assertTrue(withEncs.contains(7));
	}
	
	/**
	 * @see {@link PatientSetService#getPatientsHavingEncounters(List<EncounterType>,Location,Form,Date,Date,Integer,Integer)}
	 */
	@Test
	@Verifies(value = "should get all patients with encounters when passed an empty encounterTypeList", method = "getPatientsHavingEncounters(List<EncounterType>,Location,Form,Date,Date,Integer,Integer)")
	public void getPatientsHavingEncounters_shouldGetAllPatientsWithEncountersWhenPassedAnEmptyEncounterTypeList()
	        throws Exception {
		Cohort c = Context.getPatientSetService().getPatientsHavingEncounters(new ArrayList<EncounterType>(), null, null,
		    null, null, null, null);
		Assert.assertEquals(2, c.size());
		Assert.assertTrue(c.contains(7));
	}
	
	/**
	 * @see {@link PatientSetService#getRelationships(Cohort, RelationshipType)}
	 */
	@Test
	@Verifies(value = "should return a list with relationships when a RelationshipType is used", method = "getRelationships(Cohort, RelationshipType)")
	public void getRelationships_shouldReturnListWithRelations() throws Exception {
		RelationshipType testRelationshipType = new RelationshipType(1);
		
		Map<Integer, List<Relationship>> results = Context.getPatientSetService().getRelationships(null,
		    testRelationshipType);
		assertNotNull(results);
		assertTrue("getRelationships should return a result greater than 0", results.size() > 0);
		for (List<Relationship> relationships : results.values()) {
			for (Relationship relationship : relationships) {
				assertEquals(testRelationshipType.getRelationshipTypeId(), relationship.getRelationshipType()
				        .getRelationshipTypeId());
			}
		}
	}
	
	/**
	 * @see {@link PatientSetService#getPatientPrograms(Cohort,Program)}
	 */
	@Test
	@Verifies(value = "should get program enrollments for the given cohort", method = "getPatientPrograms(Cohort,Program)")
	public void getPatientPrograms_shouldGetProgramEnrollmentsForTheGivenCohort() throws Exception {
		Cohort cohort = new Cohort("2,3,4,5,6,7");
		Map<Integer, PatientProgram> map = Context.getPatientSetService().getPatientPrograms(cohort, new Program(2));
		TestUtil.assertCollectionContentsEquals(Arrays.asList(2, 7), map.keySet());
	}
	
	/**
	 * @see {@link PatientSetService#getPersonAttributes(Cohort, String, String, String, String, boolean)}
	 */
	@Test
	@Verifies(value = "should return person attributes of type location", method = "getPersonAttributes(Cohort, String, String, String, String, boolean)")
	public void getPersonAttributes_shouldReturnPersonAttributesOfTypeLocation() throws Exception {
		executeDataSet(EXTRA_DATA_XML);
		Cohort c = service.getAllPatients();
		String attributeName = "Health Center";
		String joinClass = "Location";
		String joinProperty = "locationId";
		String outputColumn = "name";
		boolean returnAll = false;
		Map<Integer, Object> ret = service.getPersonAttributes(c, attributeName, joinClass, joinProperty, outputColumn,
		    returnAll);
		Assert.assertEquals(4, ret.size());
		Assert.assertEquals("Unknown Location", ret.get(2));
		Assert.assertEquals("Xanadu", ret.get(6));
		Assert.assertEquals("Xanadu", ret.get(7));
		Assert.assertEquals("Xanadu", ret.get(8));
	}
	
	/**
	 * @see {@link PatientSetService#getCurrentStates(Cohort, ProgramWorkflow)}
	 */
	@Test
	@Verifies(value = "should return an empty map if cohort is empty", method = "getCurrentStates(Cohort, ProgramWorkflow)")
	public void getCurrentStates_shouldReturnAnEmptyMapIfCohortIsEmpty() throws Exception {
		Cohort nobody = new Cohort();
		ProgramWorkflowService pws = Context.getProgramWorkflowService();
		ProgramWorkflow one = pws.getWorkflow(1);
		
		Map<Integer, PatientState> results = Context.getPatientSetService().getCurrentStates(nobody, one);
		Assert.assertEquals(results.size(), 0);
	}
}

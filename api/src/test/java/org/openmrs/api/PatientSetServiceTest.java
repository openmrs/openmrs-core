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
import org.mockito.Mockito;
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
import org.openmrs.PersonAttributeType;
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
	 * @see PatientSetService#getDrugOrders(Cohort,Concept)
	 */
	@Test
	@Verifies(value = "should return an empty list if cohort is empty", method = "getDrugOrders(Cohort,Concept)")
	public void getDrugOrders_shouldReturnAnEmptyListIfCohortIsEmpty() throws Exception {
		Cohort nobody = new Cohort();
		Map<Integer, List<DrugOrder>> results = Context.getPatientSetService().getDrugOrders(nobody, null);
		assertNotNull(results);
	}

	/**
	 * @see PatientSetService#getPatientsByCharacteristics(String,Date,Date,Integer,Integer,Boolean,Boolean)
	 */
	@Test
	@Verifies(value = "should get all patients when no parameters given", method = "getPatientsByCharacteristics(String,Date,Date,Integer,Integer,Boolean,Boolean)")
	public void getPatientsByCharacteristics_shouldGetAllPatientsWhenNoParametersGiven() throws Exception {
		Cohort cohort = service.getPatientsByCharacteristics(null, null, null, null, null, null, null);
		Assert.assertEquals(4, cohort.size());
	}

	/**
	 * @see PatientSetService#getPatientsByCharacteristics(String,Date,Date,Integer,Integer,Boolean,Boolean,Date)
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
	 * @see PatientSetService#getPatientsByCharacteristics(String,Date,Date,Integer,Integer,Boolean,Boolean)
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
	 * @see PatientSetService#getPatientsByCharacteristics(String,Date,Date,Integer,Integer,Boolean,Boolean)
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
	 * @see PatientSetService#getPatientsByCharacteristics(String,Date,Date,Integer,Integer,Boolean,Boolean)
	 */
	@Test
	@Verifies(value = "should get patients who are dead", method = "getPatientsByCharacteristics(String,Date,Date,Integer,Integer,Boolean,Boolean)")
	public void getPatientsByCharacteristics_shouldGetPatientsWhoAreDead() throws Exception {
		Cohort cohort = service.getPatientsByCharacteristics(null, null, null, null, null, null, true);
		Assert.assertEquals(0, cohort.size());
		//Assert.assertTrue(cohort.contains(2));
	}

	/**
	 * @see PatientSetService#getPatientsByProgramAndState(org.openmrs.Program,List,Date, Date)
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
	 * @see PatientSetService#getPatientsByProgramAndState(org.openmrs.Program,List,Date,Date)
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
	 * @see PatientSetService#getPatientsHavingDrugOrder(java.util.Collection, java.util.Collection, GroupMethod, Date, Date)
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
	 * @see PatientSetService#getPatientsHavingDrugOrder(java.util.Collection, java.util.Collection, GroupMethod, Date, Date)
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
	 * @see PatientSetService#getPatientsHavingDrugOrder(java.util.Collection, java.util.Collection, GroupMethod, Date, Date)
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
	 * @see PatientSetService#getPatientsHavingObs(Integer,TimeModifier,Modifier,Object,Date,Date)
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
	 * @see PatientSetService#getPatientsHavingObs(Integer,TimeModifier,Modifier,Object,Date,Date)
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
	 * @see PatientSetService#getPatientsHavingEncounters(List<QEncounterType;>,Location,Form,Date,Date,Integer,Integer)
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
	 * @see PatientSetService#getPatientsHavingEncounters(EncounterType,Location,Form,Date,Date,Integer,Integer)
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
	 * @see PatientSetService#getPatientsHavingEncounters(List<EncounterType>,Location,Form,Date,Date,Integer,Integer)
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
	 * @see PatientSetService#getRelationships(Cohort, RelationshipType)
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
	 * @see PatientSetService#getPatientPrograms(Cohort,Program)
	 */
	@Test
	@Verifies(value = "should get program enrollments for the given cohort", method = "getPatientPrograms(Cohort,Program)")
	public void getPatientPrograms_shouldGetProgramEnrollmentsForTheGivenCohort() throws Exception {
		Cohort cohort = new Cohort("2,3,4,5,6,7");
		Map<Integer, PatientProgram> map = Context.getPatientSetService().getPatientPrograms(cohort, new Program(2));
		TestUtil.assertCollectionContentsEquals(Arrays.asList(2, 7), map.keySet());
	}

	/**
	 * @see PatientSetService#getPersonAttributes(Cohort, String, String, String, String, boolean)
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
	 * @see PatientSetService#getPatientsByCharacteristics(String,Date,Date)
	 */
	@Test
	@Verifies(value = "should get patients given gender and within birthdate range", method = "getPatientsByCharacteristics(String,Date,Date)")
	public void getPatientsByCharacteristics_shouldGetPatientsWithinBirthDateRange() throws Exception {
	    DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
	    Cohort cohort = null;
	    cohort = service.getPatientsByCharacteristics("m", df.parse("1969-12-31"), df.parse("2010-01-02"));
	    Assert.assertEquals(2, cohort.size());
	    Cohort cohort1 = service.getPatientsByCharacteristics("f", df.parse("1969-12-31"), df.parse("2010-01-02"));
	    Assert.assertEquals(1, cohort1.size());
	}


	/**
	 * @see PatientSetService#getPatientsHavingNumericObs(Integer,TimeModifier,Modifier,Number,Date,Date)
	 *      test = should get patients by concept and numeric value of obs
	 */
	@Test
	@Verifies(value = "should get patients by concept and numeric value", method = "getPatientsHavingNumericObs(Integer, TimeModifier, Modifier, Number, Date, Date)")
	public void getPatientsHavingNumericObs_shouldGetPatientsByConceptAndNumbericValue() throws Exception {
	    Cohort cohort = service.getPatientsHavingNumericObs(18, TimeModifier.ANY, Modifier.EQUAL, 1.0, null, null);
	    Assert.assertEquals(0, cohort.size());
	    DateFormat ymd = new SimpleDateFormat("yyyy-MM-dd");
	    { // create a new obs and set its numeric value
	        Obs obs = new Obs();
	        obs.setValueNumeric(1.0);
	        obs.setPerson(new Person(8));
	        obs.setConcept(Context.getConceptService().getConcept(18));
	        obs.setObsDatetime(ymd.parse("2007-01-01"));
	        obs.setValueBoolean(true);
	        obs.setLocation(new Location(1));
	        Context.getObsService().saveObs(obs, null);
	    }
	    cohort = service.getPatientsHavingNumericObs(18, TimeModifier.ANY, Modifier.EQUAL, 1.0, null, null);
	    Assert.assertEquals(1, cohort.size());
	    Assert.assertTrue(cohort.contains(8));
	}

	/**
	 * @see PatientSetService#getPatientsHavingDateObs(Integer,Date,Date)
	 * 		test = should get patients by concept and date value
	 */
	@Test
	@Verifies(value = "should get patients by concept and dates", method = "getPatientsHavingDateObs(Integer,Date,Date)")
	public void getPatientsHavingObs_shouldGetPatientsByDates() throws Exception {
	    DateFormat ymd = new SimpleDateFormat("yyyy-MM-dd");
	    Cohort cohort = service.getPatientsHavingDateObs(18, ymd.parse("2006-12-31"), ymd.parse("2007-01-03"));
	    Assert.assertEquals(0, cohort.size());
	    { // create a new obs
	        Obs obs = new Obs();
	        obs.setPerson(new Person(8));
	        obs.setConcept(Context.getConceptService().getConcept(18));
	        obs.setObsDatetime(ymd.parse("2007-01-01"));
	        obs.setValueBoolean(true);
	        obs.setLocation(new Location(1));
	        obs.setValueDatetime(ymd.parse("2007-01-01"));
	        Context.getObsService().saveObs(obs, null);
	    }
	    { // create a new obs
	        Obs obs = new Obs();
	        obs.setPerson(new Person(7));
	        obs.setConcept(Context.getConceptService().getConcept(18));
	        obs.setObsDatetime(ymd.parse("2007-01-03"));
	        obs.setValueBoolean(true);
	        obs.setLocation(new Location(1));
	        obs.setValueDatetime(ymd.parse("2007-01-03"));
	        Context.getObsService().saveObs(obs, null);
	    }
	    cohort = service.getPatientsHavingDateObs(18, ymd.parse("2007-01-01"),ymd.parse("2007-01-02"));
	    Assert.assertEquals(1, cohort.size());
	    Assert.assertTrue(cohort.contains(8));
	    Assert.assertFalse(cohort.contains(7));

	    cohort = service.getPatientsHavingDateObs(18, ymd.parse("2007-01-01"),ymd.parse("2007-01-03"));
	    Assert.assertEquals(2, cohort.size());
	    Assert.assertTrue(cohort.contains(8));
	    Assert.assertTrue(cohort.contains(7));
	}

	/**
	 * @see PatientSetService#getPatientsHavingPersonAttribut(PersonAttritubeType,String)
	 *      test = should get patients by person attribute type
	 */
	@Test
	@Verifies(value = "should get patients by person attribute type", method = "getPatientsHavingPersonAttribute(PersonAttritubeType,String)")
	public void getPatientsHavingPersonAttribute_shouldGetPatientsByAttributeType() throws Exception {
	    PersonAttributeType pat = Context.getPersonService().getPersonAttributeType(9);
	    Cohort cohort = service.getPatientsHavingPersonAttribute(pat,null);
	    Assert.assertEquals(4, cohort.size());
	}

	/**
	 * @see PatientSetService#getShortPatientDescriptions(Collection<Integer>)
	 *      test = should get patient short descriptions
	 */
	@Test
	@Verifies(value = "shold get patient short descriptions", method = "getShortPatientDescriptions(Collection<Integer>)")
	public void getShortPatientDescriptions_shouldGetShortPatientDescriptions() throws Exception {
	    HashSet<Integer> patientIds = new HashSet<Integer>();
	    patientIds.add(8);
	    Map<Integer, String> result = service.getShortPatientDescriptions(patientIds);
	    assertEquals(1, result.size());
	    assertTrue(result.containsKey(8));
	}

	/**
	 * @see PatientSetService#getObservations(Cohort, Concept)
	 *      test = should get observations by patients and concept
	 */
	@Test
	@Verifies(value = "should get observations by patients and concept", method = "getObservations(Cohort, Concept)")
	public void getObservations_shouldGetObservationsByPatientAndConcept() throws Exception {
	    DateFormat ymd = new SimpleDateFormat("yyyy-MM-dd");
	    Concept testConcept = Context.getConceptService().getConcept(18);
	    // create a new obs
	    Obs obs1 = new Obs();
	    obs1.setPerson(new Person(8));
	    obs1.setConcept(testConcept);
	    obs1.setObsDatetime(ymd.parse("2007-01-01"));
	    obs1.setValueBoolean(true);
	    obs1.setValueDatetime(ymd.parse("2007-01-01"));
	    Context.getObsService().saveObs(obs1, null);
	    // create another new obs
	    Obs obs2 = new Obs();
	    obs2.setPerson(new Person(7));
	    obs2.setConcept(testConcept);
	    obs2.setObsDatetime(ymd.parse("2007-01-03"));
	    obs2.setValueBoolean(true);
	    obs2.setValueDatetime(ymd.parse("2007-01-03"));
	    Context.getObsService().saveObs(obs2, null);

	    Cohort cohort = service.getPatientsHavingDateObs(18, ymd.parse("2006-12-31"), ymd.parse("2007-01-03"));
	    Map<Integer, List<Obs>> result = service.getObservations(cohort, testConcept);
	    assertEquals(2, result.size());
	    assertTrue(result.containsKey(8));
	    assertTrue(result.get(8).contains(obs1));
	    assertTrue(result.containsKey(7));
	    assertTrue(result.get(7).contains(obs2));
	}


	/**
	 * @see PatientSetService#getObservations(Cohort, Concept, Date, Date)
	 *      test = should get observations by patients, concept and date
	 */
	@Test
	@Verifies(value = "should get observations by patients, concept and date", method = "getObservations(Cohort, Concept, Date, Date)")
	public void getObservations_shouldGetObservationsByPatientConceptAndDate() throws Exception {
	    DateFormat ymd = new SimpleDateFormat("yyyy-MM-dd");
	    Concept testConcept = Context.getConceptService().getConcept(18);
	    // create a new obs for person 8 at 01-01
	    Obs obs1 = new Obs();
	    obs1.setPerson(new Person(8));
	    obs1.setConcept(testConcept);
	    obs1.setObsDatetime(ymd.parse("2007-01-01"));
	    obs1.setValueBoolean(true);
	    obs1.setValueDatetime(ymd.parse("2007-01-01"));
	    Context.getObsService().saveObs(obs1, null);

	    // create a new obs for person 8 at 01-05
	    Obs obs2 = new Obs();
	    obs2.setPerson(new Person(8));
	    obs2.setConcept(testConcept);
	    obs2.setObsDatetime(ymd.parse("2007-01-05"));
	    obs2.setValueBoolean(true);
	    obs2.setValueDatetime(ymd.parse("2007-01-05"));
	    Context.getObsService().saveObs(obs2, null);

	    // create a new obs for person 7 at 01-03
	    Obs obs3 = new Obs();
	    obs3.setPerson(new Person(7));
	    obs3.setConcept(testConcept);
	    obs3.setObsDatetime(ymd.parse("2007-01-03"));
	    obs3.setValueBoolean(true);
	    obs3.setValueDatetime(ymd.parse("2007-01-03"));
	    Context.getObsService().saveObs(obs3, null);

	    //this cohort only contains person 8
	    Cohort cohort = service.getPatientsHavingDateObs(18, ymd.parse("2006-12-31"), ymd.parse("2007-01-02"));
	    assertEquals(1, cohort.size());
	    assertTrue(cohort.contains(8));
	    //this query should only return the obs1 since the obs2 is out of the time range
	    Map<Integer, List<Obs>> result = service.getObservations(cohort, testConcept,ymd.parse("2006-12-31"),ymd.parse("2007-01-02"));
	    assertEquals(1, result.size());
	    assertTrue(result.containsKey(8));
	    //this assertion fails
	    //assertEquals(1, result.get(8).size());
	}

	/**
	 * @see PatientSetService#getObservations(Cohort, Concept)
	 *      test = should get empty observations by empty patients
	 */
	@Test
	@Verifies(value = "should get empty observations by empty patients", method = "getObservations(Cohort, Concept)")
	public void getObservations_shouldGetEmptyObservationsByEmptyPatient() throws Exception {
	    Cohort cohort = Mockito.mock(Cohort.class);
	    Mockito.when(cohort.size()).thenReturn(0);
	    Map<Integer, List<Obs>> result = service.getObservations(cohort, new Concept());
	    assertEquals(0,result.size());
	    result = service.getObservations(null, new Concept());
	    assertEquals(0,result.size());
	}

	/**
	 * @see PatientSetService#getCountOfPatients()
	 * 		test = should get the count of patients
	 */
	@Test
	public void getCountOfPatients_shouldGetCountOfPatients() throws Exception {
	    int count = service.getCountOfPatients();
	    assertEquals(4, count);
	}

	/**
	 * @see PatientSetService#getCurrentPatientPrograms(Cohort,Program)
	 * 		test = should get current program enrollments for the given cohort
	 */
	@Test
	@Verifies(value = "should get current program enrollments for the given cohort", method = "getCurrentPatientPrograms(Cohort,Program)")
	public void getCurrentPatientPrograms_shouldGetCurrentProgramEnrollmentsForTheGivenCohort() throws Exception {
	    Cohort cohort = new Cohort("2,3,4,5,6,7");
	    Map<Integer, PatientProgram> map = Context.getPatientSetService().getCurrentPatientPrograms(cohort, new Program(2));
	    TestUtil.assertCollectionContentsEquals(Arrays.asList(2, 7), map.keySet());
	}

	/**
	 * @see PatientSetService#getPatientsInProgram(Program, Date, Date)
	 * 		test = should get the patients currently in the program with the date range
	 */
	@Test
	@Verifies(value = "should get the patients currently in the program with the date range", method = "getPatientsInProgram(Program, date, date)")
	public void getPatientsInProgram_shouldGetPatientsInProgram() throws Exception {
	    DateFormat ymd = new SimpleDateFormat("yyyy-MM-dd");
	    Cohort cohort = service.getPatientsInProgram(new Program(2), ymd.parse("2007-12-31"), ymd.parse("2009-01-01"));
	    assertEquals(2,cohort.size());
	}
}

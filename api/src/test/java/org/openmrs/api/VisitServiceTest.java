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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.VisitAttribute;
import org.openmrs.VisitAttributeType;
import org.openmrs.VisitType;
import org.openmrs.api.context.Context;
import org.openmrs.customdatatype.datatype.FreeTextDatatype;
import org.openmrs.test.TestUtil;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.openmrs.util.GlobalPropertiesTestHelper;
import org.openmrs.util.OpenmrsConstants;

/**
 * Tests methods in the {@link VisitService}
 * 
 * @since 1.9
 */
public class VisitServiceTest extends BaseContextSensitiveTest {
	
	protected static final String VISITS_WITH_DATES_XML = "org/openmrs/api/include/VisitServiceTest-otherVisits.xml";
	
	protected static final String VISITS_ATTRIBUTES_XML = "org/openmrs/api/include/VisitServiceTest-visitAttributes.xml";
	
	private GlobalPropertiesTestHelper globalPropertiesTestHelper;
	
	private VisitService visitService;
	
	@BeforeEach
	public void before() {
		visitService = Context.getVisitService();
		
		// Allow overlapping visits. Ticket 3963 has introduced optional validation of start and stop dates
		// based on concurrent visits of the same patient. Turning this validation on (i.e. not allowing
		// overlapping visits) breaks existing tests of the visit service.
		//
		globalPropertiesTestHelper = new GlobalPropertiesTestHelper(Context.getAdministrationService());
		globalPropertiesTestHelper.setGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_ALLOW_OVERLAPPING_VISITS, "true");
	}
	
	@Test
	public void getAllVisitTypes_shouldGetAllVisitTypes() {
		List<VisitType> visitTypes = visitService.getAllVisitTypes();
		assertEquals(3, visitTypes.size());
	}
	
	@Test
	public void getVisitType_shouldGetCorrectVisitType() {
		VisitType visitType = visitService.getVisitType(1);

		assertNotNull(visitType);
		assertEquals("Initial HIV Clinic Visit", visitType.getName());
	}
	
	@Test
	public void getVisitType_shouldReturnNullIfVisitTypeIsNotFound() {
		
		assertNull(visitService.getVisitType(4));
	}
	
	@Test
	public void getVisitTypeByUuid_shouldGetCorrentVisitType() {
		VisitType visitType = visitService.getVisitTypeByUuid("c0c579b0-8e59-401d-8a4a-976a0b183519");

		assertNotNull(visitType);
		assertEquals("Initial HIV Clinic Visit", visitType.getName());
	}

	@Test
	public void getVisitTypeByUuid_shouldReturnNullIfVisitTypeIsNotFound() {
		
		assertNull(visitService.getVisitTypeByUuid("759799ab-c9a5-435e-b671-77773ada74e1"));
	}
	
	@Test
	public void getVisitTypes_shouldGetCorrentVisitTypes() {
		List<VisitType> visitTypes = visitService.getVisitTypes("HIV Clinic");
		assertNotNull(visitTypes);
		assertEquals(1, visitTypes.size());
		assertEquals("Initial HIV Clinic Visit", visitTypes.get(0).getName());

		visitTypes = visitService.getVisitTypes("Clinic Visit");
		assertNotNull(visitTypes);
		assertEquals(2, visitTypes.size());
		assertEquals("Initial HIV Clinic Visit", visitTypes.get(0).getName());
		assertEquals("Return TB Clinic Visit", visitTypes.get(1).getName());
	}

	@Test
	public void getVisitTypes_shouldReturnAnEmptyListIfNoVisitTypesAreFound() {
		
		List<VisitType> visitTypes = visitService.getVisitTypes("ClinicVisit");
		
		assertNotNull(visitTypes);
		assertEquals(0, visitTypes.size());
	}
	
	@Test
	public void saveVisitType_shouldSaveNewVisitType() {
		List<VisitType> visitTypes = visitService.getVisitTypes("Some Name");
		assertEquals(0, visitTypes.size());
		
		VisitType visitType = new VisitType("Some Name", "Description");
		visitService.saveVisitType(visitType);
		
		visitTypes = visitService.getVisitTypes("Some Name");
		assertEquals(1, visitTypes.size());
		
		//Should create a new visit type row.
		assertEquals(4, visitService.getAllVisitTypes().size());
	}
	
	@Test
	public void saveVisitType_shouldSaveEditedVisitType() {
		VisitType visitType = visitService.getVisitType(1);
		assertNotNull(visitType);
		assertEquals("Initial HIV Clinic Visit", visitType.getName());
		
		visitType.setName("Edited Name");
		visitType.setDescription("Edited Description");
		visitService.saveVisitType(visitType);
		
		visitType = visitService.getVisitType(1);
		assertNotNull(visitType);
		assertEquals("Edited Name", visitType.getName());
		assertEquals("Edited Description", visitType.getDescription());
		
		//Should not change the number of visit types.
		assertEquals(3, visitService.getAllVisitTypes().size());
	}
	
	@Test
	public void retireVisitType_shouldRetireGivenVisitType() {
		VisitType visitType = visitService.getVisitType(1);
		assertNotNull(visitType);
		assertFalse(visitType.getRetired());
		assertNull(visitType.getRetireReason());
		
		visitService.retireVisitType(visitType, "retire reason");
		
		visitType = visitService.getVisitType(1);
		assertNotNull(visitType);
		assertTrue(visitType.getRetired());
		assertEquals("retire reason", visitType.getRetireReason());
		
		//Should not change the number of visit types.
		assertEquals(3, visitService.getAllVisitTypes().size());
	}
	
	@Test
	public void unretireVisitType_shouldUnretireGivenVisitType() {
		VisitType visitType = visitService.getVisitType(3);
		assertNotNull(visitType);
		assertTrue(visitType.getRetired());
		assertEquals("Some Retire Reason", visitType.getRetireReason());
		
		visitService.unretireVisitType(visitType);
		
		visitType = visitService.getVisitType(3);
		assertNotNull(visitType);
		assertFalse(visitType.getRetired());
		assertNull(visitType.getRetireReason());
		
		//Should not change the number of visit types.
		assertEquals(3, visitService.getAllVisitTypes().size());
	}
	
	@Test
	public void purgeVisitType_shouldDeleteGivenVisitType() {
		VisitType visitType = visitService.getVisitType(3);
		assertNotNull(visitType);
		
		visitService.purgeVisitType(visitType);
		
		visitType = visitService.getVisitType(3);
		assertNull(visitType);
		
		//Should reduce the existing number of visit types.
		assertEquals(2, visitService.getAllVisitTypes().size());
	}
	
	/**
	 * @see VisitService#getAllVisits()
	 */
	@Test
	public void getAllVisits_shouldReturnAllUnvoidedVisits() {
		assertEquals(5, visitService.getAllVisits().size());
	}
	
	/**
	 * @see VisitService#getVisitByUuid(String)
	 */
	@Test
	public void getVisitByUuid_shouldReturnAVisitMatchingTheSpecifiedUuid() {
		Visit visit = visitService.getVisitByUuid("1e5d5d48-6b78-11e0-93c3-18a905e044dc");
		assertNotNull(visit);
		assertEquals(1, visit.getId().intValue());
	}
	
	/**
	 * @see VisitService#saveVisit(Visit)
	 */
	@Test
	public void saveVisit_shouldAddANewVisitToTheDatabase() {
		
		Integer originalSize = visitService.getAllVisits().size();
		Visit visit = new Visit(new Patient(2), new VisitType(1), new Date());
		
		visit = visitService.saveVisit(visit);
		
		assertNotNull(visit.getId());
		assertNotNull(visit.getUuid());
		assertNotNull(visit.getCreator());
		assertNotNull(visit.getDateCreated());
		assertEquals(originalSize + 1, visitService.getAllVisits().size());
	}
	
	/**
	 * @see VisitService#saveVisit(Visit)
	 */
	@Test
	public void saveVisit_shouldSaveAVisitThoughChangedByAndDateCreatedAreNotSetForVisitAttributeExplictly()
	        {
		executeDataSet(VISITS_ATTRIBUTES_XML);
		Visit visit = new Visit(new Patient(2), new VisitType(3), new Date());
		VisitAttribute visitAttribute = createVisitAttributeWithoutCreatorAndDateCreated();
		visit.setAttribute(visitAttribute);
		
		visit = visitService.saveVisit(visit);
		
		assertNotNull(visit.getId());
	}
	
	private VisitAttribute createVisitAttributeWithoutCreatorAndDateCreated() {
		VisitAttribute visitAttribute = new VisitAttribute();
		VisitAttributeType attributeType = visitService.getVisitAttributeType(1);
		attributeType.setName("visit type");
		visitAttribute.setValue(new Date());
		visitAttribute.setAttributeType(attributeType);
		return visitAttribute;
	}
	
	/**
	 * @see VisitService#saveVisit(Visit)
	 */
	@Test
	public void saveVisit_shouldVoidAnAttributeIfMaxOccursIs1AndSameAttributeTypeAlreadyExists() {
		executeDataSet(VISITS_ATTRIBUTES_XML);
		Visit visit = new Visit(new Patient(2), new VisitType(3), new Date());
		visit.setAttribute(createVisitAttribute(new Date()));
		visit.setAttribute(createVisitAttribute(new Date(System.currentTimeMillis() - 1000000)));
		assertEquals(1, visit.getAttributes().size());
		
		visit = visitService.saveVisit(visit);
		
		assertNotNull(visit.getId());
		visit.setAttribute(createVisitAttribute("second visit"));
		assertEquals(2, visit.getAttributes().size());
		VisitAttribute firstAttribute = (VisitAttribute) visit.getAttributes().toArray()[0];
		assertTrue(firstAttribute.getVoided());
	}
	
	private VisitAttribute createVisitAttribute(Object typedValue) {
		VisitAttribute visitAttribute = new VisitAttribute();
		VisitAttributeType attributeType = visitService.getVisitAttributeType(1);
		attributeType.setName("visit type");
		visitAttribute.setValue(typedValue);
		visitAttribute.setAttributeType(attributeType);
		return visitAttribute;
	}
	
	/**
	 * @see VisitService#saveVisit(Visit)
	 */
	@Test
	public void saveVisit_shouldUpdateAnExistingVisitInTheDatabase() {
		Visit visit = visitService.getVisit(2);
		assertNull(visit.getLocation());//this is the field we are editing
		assertNull(visit.getChangedBy());
		assertNull(visit.getDateChanged());
		visit.setLocation(Context.getLocationService().getLocation(1));
		visit = visitService.saveVisit(visit);
		
		Context.flushSession();
		assertNotNull(visit.getChangedBy());
		assertNotNull(visit.getDateChanged());
		assertEquals(Integer.valueOf(1), visit.getLocation().getLocationId());
	}
	
	/**
	 * @see VisitService#voidVisit(Visit,String)
	 */
	@Test
	public void voidVisit_shouldVoidTheVisitAndSetTheVoidReason() {
		Visit visit = visitService.getVisit(1);
		assertFalse(visit.getVoided());
		assertNull(visit.getVoidReason());
		assertNull(visit.getVoidedBy());
		assertNull(visit.getDateVoided());
		
		visit = visitService.voidVisit(visit, "test reason");
		assertTrue(visit.getVoided());
		assertEquals("test reason", visit.getVoidReason());
		assertEquals(Context.getAuthenticatedUser(), visit.getVoidedBy());
		assertNotNull(visit.getDateVoided());
	}
	
	/**
	 * @see VisitService#voidVisit(Visit,String)
	 */
	@Test
	public void voidVisit_shouldVoidEncountersWithVisit() {
		//given
		executeDataSet(VISITS_WITH_DATES_XML);
		Visit visit = visitService.getVisit(7);
		assertFalse(visit.getVoided());
		
		List<Encounter> encountersByVisit = Context.getEncounterService().getEncountersByVisit(visit, false);
		assertFalse(encountersByVisit.isEmpty());
		
		//when
		visit = visitService.voidVisit(visit, "test reason");
		
		//then
		assertTrue(visit.getVoided());
		
		encountersByVisit = Context.getEncounterService().getEncountersByVisit(visit, false);
		assertThat(encountersByVisit, is(empty()));
	}
	
	/**
	 * @see VisitService#unvoidVisit(Visit)
	 */
	@Test
	public void unvoidVisit_shouldUnvoidTheVisitAndUnsetAllTheVoidRelatedFields() {
		Visit visit = visitService.getVisit(6);
		assertTrue(visit.getVoided());
		assertNotNull(visit.getVoidReason());
		assertNotNull(visit.getVoidedBy());
		assertNotNull(visit.getDateVoided());
		
		visit = visitService.unvoidVisit(visit);
		assertFalse(visit.getVoided());
		assertNull(visit.getVoidReason());
		assertNull(visit.getVoidedBy());
		assertNull(visit.getDateVoided());
	}
	
	/**
	 * @see VisitService#unvoidVisit(Visit)
	 */
	@Test
	public void unvoidVisit_shouldUnvoidEncountersVoidedWithVisit() {
		//given
		executeDataSet(VISITS_WITH_DATES_XML);
		Visit visit = visitService.getVisit(7);
		
		List<Encounter> encountersByVisit = Context.getEncounterService().getEncountersByVisit(visit, true);
		assertEquals(2, encountersByVisit.size());
		
		visitService.voidVisit(visit, "test reason");
		assertTrue(visit.getVoided());
		
		encountersByVisit = Context.getEncounterService().getEncountersByVisit(visit, false);
		assertThat(encountersByVisit, is(empty()));
		
		//when
		visit = visitService.unvoidVisit(visit);
		
		//then
		assertFalse(visit.getVoided());
		
		encountersByVisit = Context.getEncounterService().getEncountersByVisit(visit, false);
		assertEquals(1, encountersByVisit.size());
	}
	
	@Test
	public void getVisitsByPatient_shouldReturnEmptyListGivenNull() {
		
		assertThat(visitService.getVisitsByPatient(null), is(empty()));
	}
	
	@Test
	public void getVisitsByPatient_shouldReturnEmptyListGivenPatientWithNullPatientId() {
		
		Patient p = new Patient();
		p.setPatientId(null);
		
		assertThat(visitService.getVisitsByPatient(p), is(empty()));
	}
	
	/**
	 * @see VisitService#getVisitsByPatient(Patient)
	 */
	@Test
	public void getVisitsByPatient_shouldReturnAllUnvoidedVisitsForTheSpecifiedPatient() {
		assertEquals(3, visitService.getVisitsByPatient(new Patient(2)).size());
	}
	
	/**
	 * @see VisitService#getActiveVisitsByPatient(Patient)
	 */
	@Test
	public void getActiveVisitsByPatient_shouldReturnAllUnvoidedActiveVisitsForTheSpecifiedPatient() {
		executeDataSet(VISITS_WITH_DATES_XML);
		assertEquals(4, visitService.getActiveVisitsByPatient(new Patient(2)).size());
	}
	
	@Test
	public void getActiveVisitsByPatient_shouldReturnAllActiveVisitsForTheSpecifiedPatient() {
		executeDataSet(VISITS_WITH_DATES_XML);
		assertEquals(5, visitService.getVisitsByPatient(new Patient(2), false, true).size());
	}
	
	@Test
	public void getActiveVisitsByPatient_shouldReturnAllUnvoidedVisitsForTheSpecifiedPatient() {
		executeDataSet(VISITS_WITH_DATES_XML);
		assertEquals(8, visitService.getVisitsByPatient(new Patient(2), true, false).size());
	}
	
	@Test
	public void getVisitsByPatient_shouldReturnEmptyListGivenNullAndFalse() {
		
		assertThat(visitService.getVisitsByPatient(null, false, false), is(empty()));
	}
	
	@Test
	public void getVisitsByPatient_shouldReturnEmptyListGivenPatientWithNullPatientIdAndFalse() {
		
		Patient p = new Patient();
		p.setPatientId(null);
		
		assertThat(visitService.getVisitsByPatient(p, false, false), is(empty()));
	}
	
	@Test
	public void getVisits_shouldGetVisitsByIndications() {
		assertEquals(1, visitService.getVisits(null, null, null, Collections.singletonList(new Concept(5497)),
		    null, null, null, null, null, true, false).size());
	}
	
	@Test
	public void getVisits_shouldGetVisitsByLocations() {
		List<Location> locations = new ArrayList<>();
		locations.add(new Location(1));
		assertEquals(1, visitService.getVisits(null, null, locations, null, null, null, null, null, null, true,
		    false).size());
	}
	
	@Test
	public void getVisits_shouldGetVisitsByVisitType() {
		List<VisitType> visitTypes = new ArrayList<>();
		visitTypes.add(new VisitType(1));
		assertEquals(4, visitService.getVisits(visitTypes, null, null, null, null, null, null, null, null,
		    true, false).size());
	}
	
	@Test
	public void getVisits_shouldGetVisitsEndedBetweenTheGivenEndDates() {
		executeDataSet(VISITS_WITH_DATES_XML);
		
		Calendar cal = Calendar.getInstance();
		cal.set(2005, Calendar.FEBRUARY, 1, 0, 0, 0);
		Date minEndDate = cal.getTime();
		cal.set(2005, Calendar.FEBRUARY, 2, 23, 59, 0);
		Date maxEndDate = cal.getTime();
		
		assertEquals(2, visitService.getVisits(null, null, null, null, null, null, minEndDate, maxEndDate,
		    null, true, false).size());
	}
	
	/**
	 * Test for TRUNK-3630
	 * 
	 * @throws ParseException
	 * @see VisitService#getVisits(Collection,Collection,Collection,Collection,Date,Date,Date,Date,Map,boolean,boolean)
	 */
	@Test
	public void getVisits_shouldGetVisitsThatAreStillOpenEvenIfMinStartDatetimeIsSpecified() throws ParseException {
		Date minEndDatetime = new SimpleDateFormat("yyyy-MM-dd").parse("2061-01-01");
		// this should get all open non-voided visits (which are ids 1, 2, 3, 4, 5 in standardTestDataset)
		List<Visit> visits = visitService.getVisits(null, null, null, null, null, null, minEndDatetime, null,
		    null, true, false);
		assertEquals(5, visits.size());
		assertTrue(TestUtil.containsId(visits, 1));
		assertTrue(TestUtil.containsId(visits, 2));
		assertTrue(TestUtil.containsId(visits, 3));
		assertTrue(TestUtil.containsId(visits, 4));
		assertTrue(TestUtil.containsId(visits, 5));
	}
	
	@Test
	public void getVisits_shouldGetVisitsStartedBetweenTheGivenStartDates() {
		executeDataSet(VISITS_WITH_DATES_XML);
		
		Calendar cal = Calendar.getInstance();
		cal.set(2005, Calendar.JANUARY, 1, 1, 0, 0);
		Date minStartDate = cal.getTime();
		cal.set(2005, Calendar.JANUARY, 1, 4, 0, 0);
		Date maxStartDate = cal.getTime();
		
		assertEquals(2, visitService.getVisits(null, null, null, null, minStartDate, maxStartDate, null, null,
		    null, true, false).size());
	}
	
	@Test
	public void getVisits_shouldReturnAllVisitsIfIncludeVoidedIsSetToTrue() {
		assertEquals(6, getNumberOfAllVisitsIncludingVoided());
	}
	
	@Test
	public void saveVisitType_shouldThrowErrorWhenNameIsNull() {
		assertThrows(APIException.class, () -> visitService.saveVisitType(new VisitType()));
	}
	
	@Test
	public void saveVisitType_shouldThrowErrorWhenNameIsEmptyString() {
		VisitType visitType = new VisitType("", null);
		assertThrows(APIException.class, () -> visitService.saveVisitType(visitType));
	}
	
	/**
	 * @see VisitService#getAllVisitAttributeTypes()
	 */
	@Test
	public void getAllVisitAttributeTypes_shouldReturnAllVisitAttributeTypesIncludingRetiredOnes() {
		executeDataSet(VISITS_ATTRIBUTES_XML);
		assertEquals(3, visitService.getAllVisitAttributeTypes().size());
	}
	
	/**
	 * @see VisitService#getVisitAttributeType(Integer)
	 */
	@Test
	public void getVisitAttributeType_shouldReturnTheVisitAttributeTypeWithTheGivenId() {
		executeDataSet(VISITS_ATTRIBUTES_XML);
		assertEquals("Audit Date", visitService.getVisitAttributeType(1).getName());
	}
	
	/**
	 * @see VisitService#getVisitAttributeType(Integer)
	 */
	@Test
	public void getVisitAttributeType_shouldReturnNullIfNoVisitAttributeTypeExistsWithTheGivenId() {
		executeDataSet(VISITS_ATTRIBUTES_XML);
		assertNull(visitService.getVisitAttributeType(999));
	}
	
	/**
	 * @see VisitService#getVisitAttributeTypeByUuid(String)
	 */
	@Test
	public void getVisitAttributeTypeByUuid_shouldReturnTheVisitAttributeTypeWithTheGivenUuid() {
		executeDataSet(VISITS_ATTRIBUTES_XML);
		assertEquals("Audit Date", visitService.getVisitAttributeTypeByUuid("9516cc50-6f9f-11e0-8414-001e378eb67e")
		        .getName());
	}
	
	/**
	 * @see VisitService#getVisitAttributeTypeByUuid(String)
	 */
	@Test
	public void getVisitAttributeTypeByUuid_shouldReturnNullIfNoVisitAttributeTypeExistsWithTheGivenUuid() {
		executeDataSet(VISITS_ATTRIBUTES_XML);
		assertNull(visitService.getVisitAttributeTypeByUuid("not-a-uuid"));
	}
	
	/**
	 * @see VisitService#purgeVisitAttributeType(VisitAttributeType)
	 */
	@Test
	public void purgeVisitAttributeType_shouldCompletelyRemoveAVisitAttributeType() {
		executeDataSet(VISITS_ATTRIBUTES_XML);
		assertEquals(3, visitService.getAllVisitAttributeTypes().size());
		visitService.purgeVisitAttributeType(visitService.getVisitAttributeType(2));
		assertEquals(2, visitService.getAllVisitAttributeTypes().size());
	}
	
	/**
	 * @see VisitService#retireVisitAttributeType(VisitAttributeType,String)
	 */
	@Test
	public void retireVisitAttributeType_shouldRetireAVisitAttributeType() {
		executeDataSet(VISITS_ATTRIBUTES_XML);
		VisitAttributeType vat = visitService.getVisitAttributeType(1);
		assertFalse(vat.getRetired());
		visitService.retireVisitAttributeType(vat, "for testing");
		vat = visitService.getVisitAttributeType(1);
		assertTrue(vat.getRetired());
		assertNotNull(vat.getRetiredBy());
		assertNotNull(vat.getDateRetired());
		assertEquals("for testing", vat.getRetireReason());
	}
	
	/**
	 * @see VisitService#saveVisitAttributeType(VisitAttributeType)
	 */
	@Test
	public void saveVisitAttributeType_shouldCreateANewVisitAttributeType() {
		executeDataSet(VISITS_ATTRIBUTES_XML);
		assertEquals(3, visitService.getAllVisitAttributeTypes().size());
		VisitAttributeType vat = new VisitAttributeType();
		vat.setName("Another one");
		vat.setDatatypeClassname(FreeTextDatatype.class.getName());
		visitService.saveVisitAttributeType(vat);
		assertNotNull(vat.getId());
		assertEquals(4, visitService.getAllVisitAttributeTypes().size());
	}
	
	/**
	 * @see VisitService#saveVisitAttributeType(VisitAttributeType)
	 */
	@Test
	public void saveVisitAttributeType_shouldEditAnExistingVisitAttributeType() {
		executeDataSet(VISITS_ATTRIBUTES_XML);
		assertEquals(3, visitService.getAllVisitAttributeTypes().size());
		VisitAttributeType vat = visitService.getVisitAttributeType(1);
		vat.setName("A new name");
		visitService.saveVisitAttributeType(vat);
		assertEquals(3, visitService.getAllVisitAttributeTypes().size());
		assertEquals("A new name", visitService.getVisitAttributeType(1).getName());
	}
	
	/**
	 * @see VisitService#unretireVisitAttributeType(VisitAttributeType)
	 */
	@Test
	public void unretireVisitAttributeType_shouldUnretireARetiredVisitAttributeType() {
		executeDataSet(VISITS_ATTRIBUTES_XML);
		VisitAttributeType vat = visitService.getVisitAttributeType(2);
		assertTrue(vat.getRetired());
		assertNotNull(vat.getDateRetired());
		assertNotNull(vat.getRetiredBy());
		assertNotNull(vat.getRetireReason());
		visitService.unretireVisitAttributeType(vat);
		assertFalse(vat.getRetired());
		assertNull(vat.getDateRetired());
		assertNull(vat.getRetiredBy());
		assertNull(vat.getRetireReason());
	}
	
	/**
	 * @see VisitService#getVisitAttributeByUuid(String)
	 */
	@Test
	public void getVisitAttributeByUuid_shouldGetTheVisitAttributeWithTheGivenUuid() {
		executeDataSet(VISITS_ATTRIBUTES_XML);
		assertEquals("2011-04-25", visitService.getVisitAttributeByUuid("3a2bdb18-6faa-11e0-8414-001e378eb67e")
		        .getValueReference());
	}
	
	/**
	 * @see VisitService#getVisitAttributeByUuid(String)
	 */
	@Test
	public void getVisitAttributeByUuid_shouldReturnNullIfNoVisitAttributeHasTheGivenUuid() {
		executeDataSet(VISITS_ATTRIBUTES_XML);
		assertNull(visitService.getVisitAttributeByUuid("not-a-uuid"));
	}
	
	@Test
	public void getVisits_shouldGetAllVisitsWithGivenAttributeValues() throws ParseException {
		executeDataSet(VISITS_ATTRIBUTES_XML);
		Map<VisitAttributeType, Object> attrs = new HashMap<>();
		attrs.put(visitService.getVisitAttributeType(1), new SimpleDateFormat("yyyy-MM-dd").parse("2011-04-25"));
		List<Visit> visits = visitService.getVisits(null, null, null, null, null, null, null, null, attrs, true, false);
		assertEquals(1, visits.size());
		assertEquals(Integer.valueOf(1), visits.get(0).getVisitId());
	}
	
	@Test
	public void getVisits_shouldNotFindAnyVisitsIfNoneHaveGivenAttributeValues() throws ParseException {
		executeDataSet(VISITS_ATTRIBUTES_XML);
		Map<VisitAttributeType, Object> attrs = new HashMap<>();
		attrs.put(visitService.getVisitAttributeType(1), new SimpleDateFormat("yyyy-MM-dd").parse("1411-04-25"));
		List<Visit> visits = visitService.getVisits(null, null, null, null, null, null, null, null, attrs, true, false);
		assertEquals(0, visits.size());
	}
	
	/**
	 * @see VisitService#saveVisit(Visit)
	 */
	@Test
	public void saveVisit_shouldFailIfValidationErrorsAreFound() {
		
		Visit visit = new Visit();
		//Not setting the patient so that we get validation errors
		visit.setVisitType(visitService.getVisitType(1));
		visit.setStartDatetime(new Date());
		
		assertThrows(APIException.class, () -> visitService.saveVisit(visit));
	}
	
	/**
	 * @see VisitService#saveVisit(Visit)
	 */
	@Test
	public void saveVisit_shouldPassIfNoValidationErrorsAreFound() {
		
		Visit visit = new Visit();
		visit.setPatient(Context.getPatientService().getPatient(2));
		visit.setVisitType(visitService.getVisitType(1));
		visit.setStartDatetime(new Date());
		
		visitService.saveVisit(visit);
	}
	
	/**
	 * @see VisitService#endVisit(Visit,Date)
	 */
	@Test
	public void endVisit_shouldSetStopDateTimeAsCurrentDateIfStopDateIsNull() {
		
		Visit visit = visitService.getVisit(1);
		assertNull(visit.getStopDatetime());
		
		visitService.endVisit(visit, null);
		
		assertNotNull(visit.getStopDatetime());
	}
	
	/**
	 * @see VisitService#endVisit(Visit,Date)
	 */
	@Test
	public void endVisit_shouldNotFailIfNoValidationErrorsAreFound() {
		
		Visit visit = visitService.getVisit(1);
		
		visitService.endVisit(visit, new Date());
	}
	
	/**
	 * @see VisitService#endVisit(Visit,Date)
	 */
	@Test
	public void endVisit_shouldFailIfValidationErrorsAreFound() {
		
		Visit visit = visitService.getVisit(1);
		Calendar cal = Calendar.getInstance();
		cal.setTime(visit.getStartDatetime());
		cal.add(Calendar.DAY_OF_MONTH, -1);
		
		assertThrows(APIException.class, () -> visitService.endVisit(visit, cal.getTime()));
	}
	
	/**
	 * @see VisitService#purgeVisit(Visit)
	 */
	@Test
	public void purgeVisit_shouldEraseTheVisitFromTheDatabase() {
		
		int originalSize = getNumberOfAllVisitsIncludingVoided();
		Visit visit = visitService.getVisit(1);
		
		visitService.purgeVisit(visit);
		
		assertEquals(originalSize - 1, getNumberOfAllVisitsIncludingVoided());
	}

	/**
	 * @see VisitService#purgeVisit(Visit)
	 */
	@Test
	public void purgeVisit_shouldReturnWithoutVoidingIfVisitIsUnsaved() {
		
		int originalSize = getNumberOfAllVisitsIncludingVoided();
		
		visitService.purgeVisit(new Visit());
		
		assertEquals(originalSize, getNumberOfAllVisitsIncludingVoided());
	}
	
	/**
	 * @see VisitService#purgeVisit(Visit)
	 */
	@Test
	public void purgeVisit_shouldFailIfTheVisitHasEncountersAssociatedToIt() {
		Visit visit = visitService.getVisit(1);
		Encounter e = Context.getEncounterService().getEncounter(3);
		visit.addEncounter(e);
		visitService.saveVisit(visit);
		//sanity check
		assertTrue(Context.getEncounterService().getEncountersByVisit(visit, false).size() > 0);
		
		assertThrows(APIException.class, () -> visitService.purgeVisit(visit));
	}
	
	/**
	 * @see VisitService#saveVisit(Visit)
	 */
	@Test
	public void saveVisit_shouldBeAbleToAddAnAttributeToAVisit() {
		Date now = new Date();
		Visit visit = visitService.getVisit(1);
		VisitAttributeType attrType = visitService.getVisitAttributeType(1);
		VisitAttribute attr = new VisitAttribute();
		attr.setAttributeType(attrType);
		attr.setValue(now);
		visit.addAttribute(attr);
		visitService.saveVisit(visit);
		assertEquals(new SimpleDateFormat("yyyy-MM-dd").format(now), attr.getValueReference());
	}
	
	@Test
	public void shouldVoidASimpleAttribute() {
		executeDataSet(VISITS_ATTRIBUTES_XML);
		Visit visit = visitService.getVisit(1);
		VisitAttributeType attrType = visitService.getVisitAttributeType(1);
		List<VisitAttribute> attributes = visit.getActiveAttributes(attrType);
		assertTrue(attributes.size() > 0);
		VisitAttribute attribute = attributes.get(0);
		attribute.setVoided(true);
		visitService.saveVisit(visit);
		assertNotNull(attribute.getVoidedBy());
		assertNotNull(attribute.getDateVoided());
	}
	
	@Test
	public void stopVisits_shouldCloseAllUnvoidedActiveVisitMatchingTheSpecifiedVisitTypes() {
		executeDataSet("org/openmrs/api/include/VisitServiceTest-includeVisitsAndTypeToAutoClose.xml");
		String[] visitTypeNames = StringUtils.split(Context.getAdministrationService().getGlobalProperty(
		    OpenmrsConstants.GP_VISIT_TYPES_TO_AUTO_CLOSE), ",");
		
		String openVisitsQuery = "SELECT visit_id FROM visit WHERE voided = false AND date_stopped IS NULL AND visit_type_id IN (SELECT visit_type_id FROM visit_type WHERE NAME IN ('"
		        + StringUtils.join(visitTypeNames, "','") + "'))";
		int activeVisitCount = Context.getAdministrationService().executeSQL(openVisitsQuery, true).size();
		//sanity check
		assertTrue(activeVisitCount > 0, "There should be some active visits for this test to be valid");
		
		//close any unvoided open visits
		visitService.stopVisits(null);
		
		activeVisitCount = Context.getAdministrationService().executeSQL(openVisitsQuery, true).size();
		
		//all active unvoided visits should have been closed
		assertTrue(activeVisitCount == 0, "Not all active unvoided vists were closed");
	}
	
	/**
	 * @see VisitService#saveVisit(Visit)
	 */
	@Test
	public void saveVisit_shouldSaveNewVisitWithEncountersSuccessfully() {
		
		Integer originalSize = visitService.getAllVisits().size();
		Visit visit = new Visit(new Patient(2), new VisitType(1), new Date());
		
		Encounter encounter = Context.getEncounterService().getEncounter(4);
		visit.addEncounter(encounter);
		
		visitService.saveVisit(visit);
		
		int visitId = visit.getVisitId();
		
		Context.flushSession();
		Context.clearSession();
		
		// reload the visit
		visit = visitService.getVisit(visitId);
		
		assertNotNull(visit.getId());
		assertNotNull(visit.getUuid());
		assertNotNull(visit.getCreator());
		assertNotNull(visit.getDateCreated());
		assertEquals(originalSize + 1, visitService.getAllVisits().size());
		assertEquals(1, visit.getEncounters().size());
		assertEquals(Integer.valueOf(4), ((Encounter) visit.getEncounters().toArray()[0]).getEncounterId());
	}
	
	/**
	 * @see VisitService#saveVisit(Visit)
	 */
	@Test
	public void saveVisit_shouldAssociateEncounterWithVisitOnSaveEncounter() {
		
		Visit visit = visitService.getVisit(1);
		EncounterService es = Context.getEncounterService();
		
		Encounter encounter = new Encounter();
		encounter.setEncounterDatetime(new Date());
		encounter.setPatient(visit.getPatient());
		encounter.setLocation(visit.getLocation());
		encounter.setEncounterType(es.getEncounterType(1));
		
		visit.addEncounter(encounter);
		
		Context.getEncounterService().saveEncounter(encounter);
		Integer encounterId = encounter.getEncounterId();
		
		Context.flushSession();
		Context.clearSession();
		
		// reload the visit
		visit = visitService.getVisit(1);
		
		assertEquals(1, visit.getEncounters().size());
		assertEquals(encounterId, ((Encounter) visit.getEncounters().toArray()[0]).getEncounterId());
	}
	
	/**
	 * @see VisitService#saveVisit(Visit)
	 */
	@Test
	public void saveVisit_shouldNotPersistNewEncounter() {

		Visit visit = visitService.getVisit(1);
		EncounterService es = Context.getEncounterService();
		final int originalCount = es.getEncountersByPatient(visit.getPatient()).size();

		Encounter encounter = new Encounter();
		encounter.setEncounterDatetime(new Date());
		encounter.setEncounterType(es.getEncounterType(1));
		encounter.setPatient(visit.getPatient());
		encounter.setLocation(visit.getLocation());
		visit.addEncounter(encounter);

		visitService.saveVisit(visit);

		Context.flushSession();
		assertNull(encounter.getEncounterId());
		assertEquals(originalCount, es.getEncountersByPatient(visit.getPatient()).size());
	}

	/**
	 * @see VisitService#getAllVisitTypes(boolean)
	 */
	@Test
	public void getAllVisitTypes_shouldGetAllVisitTypesBasedOnIncludeRetiredFlag() {
		VisitService visitService = Context.getVisitService();
		List<VisitType> visitTypes = visitService.getAllVisitTypes(true);
		assertEquals(3, visitTypes.size(), "get all visit types including retired");
		visitTypes = visitService.getAllVisitTypes(false);
		assertEquals(2, visitTypes.size(), "get all visit types excluding retired");
	}

	private int getNumberOfAllVisitsIncludingVoided() {
		return visitService
			.getVisits(null, null, null, null, null, null, null, null, null, true, true)
			.size();
	}

}

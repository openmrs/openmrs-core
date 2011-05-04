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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.VisitAttribute;
import org.openmrs.VisitAttributeType;
import org.openmrs.VisitType;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;

/**
 * Tests methods in the {@link VisitService}
 * 
 * @since 1.9
 */
public class VisitServiceTest extends BaseContextSensitiveTest {
	
	protected static final String VISITS_WITH_DATES_XML = "org/openmrs/api/include/VisitServiceTest-otherVisits.xml";
	
	protected static final String VISITS_ATTRIBUTES_XML = "org/openmrs/api/include/VisitServiceTest-visitAttributes.xml";
	
	private VisitService service;
	
	@Before
	public void before() {
		service = Context.getVisitService();
	}
	
	@Test
	@Verifies(value = "should get all visit types", method = "getAllVisitTypes()")
	public void getAllVisitTypes_shouldGetAllVisitTypes() throws Exception {
		List<VisitType> visitTypes = Context.getVisitService().getAllVisitTypes();
		Assert.assertEquals(3, visitTypes.size());
	}
	
	@Test
	@Verifies(value = "should get correct visit type", method = "getVisitType(Integer)")
	public void getVisitType_shouldGetCorrentVisitType() throws Exception {
		VisitType visitType = Context.getVisitService().getVisitType(1);
		Assert.assertNotNull(visitType);
		Assert.assertEquals("Initial HIV Clinic Visit", visitType.getName());
		
		visitType = Context.getVisitService().getVisitType(2);
		Assert.assertNotNull(visitType);
		Assert.assertEquals("Return TB Clinic Visit", visitType.getName());
		
		visitType = Context.getVisitService().getVisitType(3);
		Assert.assertNotNull(visitType);
		Assert.assertEquals("Hospitalization", visitType.getName());
		
		visitType = Context.getVisitService().getVisitType(4);
		Assert.assertNull(visitType);
	}
	
	@Test
	@Verifies(value = "should get correct visit type", method = "getVisitTypeByUuid(String)")
	public void getVisitTypeByUuid_shouldGetCorrentVisitType() throws Exception {
		VisitType visitType = Context.getVisitService().getVisitTypeByUuid("c0c579b0-8e59-401d-8a4a-976a0b183519");
		Assert.assertNotNull(visitType);
		Assert.assertEquals("Initial HIV Clinic Visit", visitType.getName());
		
		visitType = Context.getVisitService().getVisitTypeByUuid("759799ab-c9a5-435e-b671-77773ada74e4");
		Assert.assertNotNull(visitType);
		Assert.assertEquals("Return TB Clinic Visit", visitType.getName());
		
		visitType = Context.getVisitService().getVisitTypeByUuid("759799ab-c9a5-435e-b671-77773ada74e6");
		Assert.assertNotNull(visitType);
		Assert.assertEquals("Hospitalization", visitType.getName());
		
		visitType = Context.getVisitService().getVisitTypeByUuid("759799ab-c9a5-435e-b671-77773ada74e1");
		Assert.assertNull(visitType);
	}
	
	@Test
	@Verifies(value = "should get correct visit types", method = "getVisitTypes(String)")
	public void getVisitTypes_shouldGetCorrentVisitTypes() throws Exception {
		List<VisitType> visitTypes = Context.getVisitService().getVisitTypes("HIV Clinic");
		Assert.assertNotNull(visitTypes);
		Assert.assertEquals(1, visitTypes.size());
		Assert.assertEquals("Initial HIV Clinic Visit", visitTypes.get(0).getName());
		
		visitTypes = Context.getVisitService().getVisitTypes("Clinic Visit");
		Assert.assertNotNull(visitTypes);
		Assert.assertEquals(2, visitTypes.size());
		Assert.assertEquals("Initial HIV Clinic Visit", visitTypes.get(0).getName());
		Assert.assertEquals("Return TB Clinic Visit", visitTypes.get(1).getName());
		
		visitTypes = Context.getVisitService().getVisitTypes("ClinicVisit");
		Assert.assertNotNull(visitTypes);
		Assert.assertEquals(0, visitTypes.size());
	}
	
	@Test
	@Verifies(value = "should save new visit type", method = "saveVisitType(VisitType)")
	public void saveVisitType_shouldSaveNewVisitType() throws Exception {
		List<VisitType> visitTypes = Context.getVisitService().getVisitTypes("Some Name");
		Assert.assertEquals(0, visitTypes.size());
		
		VisitType visitType = new VisitType("Some Name", "Description");
		Context.getVisitService().saveVisitType(visitType);
		
		visitTypes = Context.getVisitService().getVisitTypes("Some Name");
		Assert.assertEquals(1, visitTypes.size());
		
		//Should create a new visit type row.
		Assert.assertEquals(4, Context.getVisitService().getAllVisitTypes().size());
	}
	
	@Test
	@Verifies(value = "should save edited visit type", method = "saveVisitType(VisitType)")
	public void saveVisitType_shouldSaveEditedVisitType() throws Exception {
		VisitType visitType = Context.getVisitService().getVisitType(1);
		Assert.assertNotNull(visitType);
		Assert.assertEquals("Initial HIV Clinic Visit", visitType.getName());
		
		visitType.setName("Edited Name");
		visitType.setDescription("Edited Description");
		Context.getVisitService().saveVisitType(visitType);
		
		visitType = Context.getVisitService().getVisitType(1);
		Assert.assertNotNull(visitType);
		Assert.assertEquals("Edited Name", visitType.getName());
		Assert.assertEquals("Edited Description", visitType.getDescription());
		
		//Should not change the number of visit types.
		Assert.assertEquals(3, Context.getVisitService().getAllVisitTypes().size());
	}
	
	@Test
	@Verifies(value = "should retire given visit type", method = "retireVisitType(VisitType, String)")
	public void retireVisitType_shouldRetireGivenVisitType() throws Exception {
		VisitType visitType = Context.getVisitService().getVisitType(1);
		Assert.assertNotNull(visitType);
		Assert.assertFalse(visitType.isRetired());
		Assert.assertNull(visitType.getRetireReason());
		
		Context.getVisitService().retireVisitType(visitType, "retire reason");
		
		visitType = Context.getVisitService().getVisitType(1);
		Assert.assertNotNull(visitType);
		Assert.assertTrue(visitType.isRetired());
		Assert.assertEquals("retire reason", visitType.getRetireReason());
		
		//Should not change the number of visit types.
		Assert.assertEquals(3, Context.getVisitService().getAllVisitTypes().size());
	}
	
	@Test
	@Verifies(value = "should unretire given visit type", method = "unretireVisitType(VisitType)")
	public void unretireVisitType_shouldUnretireGivenVisitType() throws Exception {
		VisitType visitType = Context.getVisitService().getVisitType(3);
		Assert.assertNotNull(visitType);
		Assert.assertTrue(visitType.isRetired());
		Assert.assertEquals("Some Retire Reason", visitType.getRetireReason());
		
		Context.getVisitService().unretireVisitType(visitType);
		
		visitType = Context.getVisitService().getVisitType(3);
		Assert.assertNotNull(visitType);
		Assert.assertFalse(visitType.isRetired());
		Assert.assertNull(visitType.getRetireReason());
		
		//Should not change the number of visit types.
		Assert.assertEquals(3, Context.getVisitService().getAllVisitTypes().size());
	}
	
	@Test
	@Verifies(value = "should delete given visit type", method = "purgeVisitType(VisitType)")
	public void purgeVisitType_shouldDeleteGivenVisitType() throws Exception {
		VisitType visitType = Context.getVisitService().getVisitType(3);
		Assert.assertNotNull(visitType);
		
		Context.getVisitService().purgeVisitType(visitType);
		
		visitType = Context.getVisitService().getVisitType(3);
		Assert.assertNull(visitType);
		
		//Should reduce the existing number of visit types.
		Assert.assertEquals(2, Context.getVisitService().getAllVisitTypes().size());
	}
	
	/**
	 * @see {@link VisitService#getAllVisits()}
	 */
	@Test
	@Verifies(value = "should return all unvoided visits", method = "getAllVisits()")
	public void getAllVisits_shouldReturnAllUnvoidedVisits() throws Exception {
		Assert.assertEquals(5, Context.getVisitService().getAllVisits().size());
	}
	
	/**
	 * @see {@link VisitService#getVisitByUuid(String)}
	 */
	@Test
	@Verifies(value = "should return a visit matching the specified uuid", method = "getVisitByUuid(String)")
	public void getVisitByUuid_shouldReturnAVisitMatchingTheSpecifiedUuid() throws Exception {
		Visit visit = Context.getVisitService().getVisitByUuid("1e5d5d48-6b78-11e0-93c3-18a905e044dc");
		Assert.assertNotNull(visit);
		Assert.assertEquals(1, visit.getId().intValue());
	}
	
	/**
	 * @see {@link VisitService#saveVisit(Visit)}
	 */
	@Test
	@Verifies(value = "should add a new visit to the database", method = "saveVisit(Visit)")
	public void saveVisit_shouldAddANewVisitToTheDatabase() throws Exception {
		VisitService vs = Context.getVisitService();
		Integer originalSize = vs.getAllVisits().size();
		Visit visit = new Visit(new Patient(2), new VisitType(1), new Date());
		visit = vs.saveVisit(visit);
		Assert.assertNotNull(visit.getId());
		Assert.assertNotNull(visit.getUuid());
		Assert.assertNotNull(visit.getCreator());
		Assert.assertNotNull(visit.getDateCreated());
		Assert.assertEquals(originalSize + 1, vs.getAllVisits().size());
	}
	
	/**
	 * @see {@link VisitService#saveVisit(Visit)}
	 */
	@Test
	@Ignore
	@Verifies(value = "should update an existing visit in the database", method = "saveVisit(Visit)")
	public void saveVisit_shouldUpdateAnExistingVisitInTheDatabase() throws Exception {
		Visit visit = Context.getVisitService().getVisit(1);
		Assert.assertNull(visit.getLocation());
		Assert.assertNull(visit.getChangedBy());
		Assert.assertNull(visit.getDateChanged());
		visit.setLocation(new Location(1));
		visit = Context.getVisitService().saveVisit(visit);
		//TODO Auditable interceptor is currently not able to set these fields as expected
		Assert.assertNotNull(visit.getChangedBy());
		Assert.assertNotNull(visit.getDateChanged());
	}
	
	/**
	 * @see {@link VisitService#voidVisit(Visit,String)}
	 */
	@Test
	@Verifies(value = "should void the visit and set the voidReason", method = "voidVisit(Visit,String)")
	public void voidVisit_shouldVoidTheVisitAndSetTheVoidReason() throws Exception {
		Visit visit = Context.getVisitService().getVisit(1);
		Assert.assertFalse(visit.isVoided());
		Assert.assertNull(visit.getVoidReason());
		Assert.assertNull(visit.getVoidedBy());
		Assert.assertNull(visit.getDateVoided());
		
		visit = Context.getVisitService().voidVisit(visit, "test reason");
		Assert.assertTrue(visit.isVoided());
		Assert.assertEquals("test reason", visit.getVoidReason());
		Assert.assertEquals(Context.getAuthenticatedUser(), visit.getVoidedBy());
		Assert.assertNotNull(visit.getDateVoided());
	}
	
	/**
	 * @see {@link VisitService#unvoidVisit(Visit)}
	 */
	@Test
	@Verifies(value = "should unvoid the visit and unset all the void related fields", method = "unvoidVisit(Visit)")
	public void unvoidVisit_shouldUnvoidTheVisitAndUnsetAllTheVoidRelatedFields() throws Exception {
		Visit visit = Context.getVisitService().getVisit(6);
		Assert.assertTrue(visit.isVoided());
		Assert.assertNotNull(visit.getVoidReason());
		Assert.assertNotNull(visit.getVoidedBy());
		Assert.assertNotNull(visit.getDateVoided());
		
		visit = Context.getVisitService().unvoidVisit(visit);
		Assert.assertFalse(visit.isVoided());
		Assert.assertNull(visit.getVoidReason());
		Assert.assertNull(visit.getVoidedBy());
		Assert.assertNull(visit.getDateVoided());
	}
	
	/**
	 * @see {@link VisitService#purgeVisit(Visit)}
	 */
	@Test
	@Verifies(value = "should erase the visit from the database", method = "purgeVisit(Visit)")
	public void purgeVisit_shouldEraseTheVisitFromTheDatabase() throws Exception {
		VisitService vs = Context.getVisitService();
		Integer originalSize = vs.getVisits(null, null, null, null, null, null, null, null, null, true).size();
		Visit visit = Context.getVisitService().getVisit(1);
		vs.purgeVisit(visit);
		Assert.assertEquals(originalSize - 1, vs.getVisits(null, null, null, null, null, null, null, null, null, true)
		        .size());
	}
	
	/**
	 * @see {@link VisitService#getVisitsByPatient(Patient)}
	 */
	@Test
	@Verifies(value = "should return all unvoided visits for the specified patient", method = "getVisitsByPatient(Patient)")
	public void getVisitsByPatient_shouldReturnAllUnvoidedVisitsForTheSpecifiedPatient() throws Exception {
		Assert.assertEquals(3, Context.getVisitService().getVisitsByPatient(new Patient(2)).size());
	}
	
	/**
	 * @see {@link VisitService#getActiveVisitsByPatient(Patient)}
	 */
	@Test
	@Verifies(value = "should return all unvoided active visits for the specified patient", method = "getActiveVisitsByPatient(Patient)")
	public void getActiveVisitsByPatient_shouldReturnAllUnvoidedActiveVisitsForTheSpecifiedPatient() throws Exception {
		executeDataSet(VISITS_WITH_DATES_XML);
		Assert.assertEquals(4, Context.getVisitService().getActiveVisitsByPatient(new Patient(2)).size());
		
	}
	
	/**
	 * @see {@link VisitService#getVisits(java.util.Collection, java.util.Collection, java.util.Collection, java.util.Collection, Date, Date, Date, Date, boolean)}
	 */
	@Test
	@Verifies(value = "should get visits by indications", method = "getVisits(Collection<VisitType>,Collection<Patient>,Collection<Location>,Collection<Concept>,Date,Date,Date,Date,boolean)")
	public void getVisits_shouldGetVisitsByIndications() throws Exception {
		Assert.assertEquals(1, Context.getVisitService().getVisits(null, null, null,
		    Collections.singletonList(new Concept(5497)), null, null, null, null, null, false).size());
	}
	
	/**
	 * @see {@link VisitService#getVisits(java.util.Collection, java.util.Collection, java.util.Collection, java.util.Collection, Date, Date, Date, Date, boolean)}
	 */
	@Test
	@Verifies(value = "should get visits by locations", method = "getVisits(Collection<VisitType>,Collection<Patient>,Collection<Location>,Collection<Concept>,Date,Date,Date,Date,boolean)")
	public void getVisits_shouldGetVisitsByLocations() throws Exception {
		List<Location> locations = new ArrayList<Location>();
		locations.add(new Location(1));
		Assert.assertEquals(1, Context.getVisitService().getVisits(null, null, locations, null, null, null, null, null,
		    null, false).size());
	}
	
	/**
	 * @see {@link VisitService#getVisits(java.util.Collection, java.util.Collection, java.util.Collection, java.util.Collection, Date, Date, Date, Date, boolean)}
	 */
	@Test
	@Verifies(value = "should get visits by visit type", method = "getVisits(Collection<VisitType>,Collection<Patient>,Collection<Location>,Collection<Concept>,Date,Date,Date,Date,boolean)")
	public void getVisits_shouldGetVisitsByVisitType() throws Exception {
		List<VisitType> visitTypes = new ArrayList<VisitType>();
		visitTypes.add(new VisitType(1));
		Assert.assertEquals(4, Context.getVisitService().getVisits(visitTypes, null, null, null, null, null, null, null,
		    null, false).size());
	}
	
	/**
	 * @see {@link VisitService#getVisits(java.util.Collection, java.util.Collection, java.util.Collection, java.util.Collection, Date, Date, Date, Date, boolean)}
	 */
	@Test
	@Verifies(value = "should get visits ended between the given end dates", method = "getVisits(Collection<VisitType>,Collection<Patient>,Collection<Location>,Collection<Concept>,Date,Date,Date,Date,boolean)")
	public void getVisits_shouldGetVisitsEndedBetweenTheGivenEndDates() throws Exception {
		executeDataSet(VISITS_WITH_DATES_XML);
		Calendar cal = Calendar.getInstance();
		cal.set(2005, 01, 01, 00, 00, 00);
		Date minEndDate = cal.getTime();
		cal.set(2005, 01, 02, 23, 59, 00);
		Date maxEndDate = cal.getTime();
		Assert.assertEquals(2, Context.getVisitService().getVisits(null, null, null, null, null, null, minEndDate,
		    maxEndDate, null, false).size());
	}
	
	/**
	 * @see {@link VisitService#getVisits(java.util.Collection, java.util.Collection, java.util.Collection, java.util.Collection, Date, Date, Date, Date, boolean)}
	 */
	@Test
	@Verifies(value = "should get visits started between the given start dates", method = "getVisits(Collection<VisitType>,Collection<Patient>,Collection<Location>,Collection<Concept>,Date,Date,Date,Date,boolean)")
	public void getVisits_shouldGetVisitsStartedBetweenTheGivenStartDates() throws Exception {
		executeDataSet(VISITS_WITH_DATES_XML);
		Calendar cal = Calendar.getInstance();
		cal.set(2005, 00, 01, 01, 00, 00);
		Date minStartDate = cal.getTime();
		cal.set(2005, 00, 01, 04, 00, 00);
		Date maxStartDate = cal.getTime();
		Assert.assertEquals(2, Context.getVisitService().getVisits(null, null, null, null, minStartDate, maxStartDate, null,
		    null, null, false).size());
	}
	
	/**
	 * @see {@link VisitService#getVisits(java.util.Collection, java.util.Collection, java.util.Collection, java.util.Collection, Date, Date, Date, Date, boolean)}
	 */
	@Test
	@Verifies(value = "should return all visits if includeVoided is set to true", method = "getVisits(Collection<VisitType>,Collection<Patient>,Collection<Location>,Collection<Concept>,Date,Date,Date,Date,boolean)")
	public void getVisits_shouldReturnAllVisitsIfIncludeVoidedIsSetToTrue() throws Exception {
		Assert.assertEquals(6, Context.getVisitService().getVisits(null, null, null, null, null, null, null, null, null,
		    true).size());
	}
	
	@Test(expected = APIException.class)
	@Verifies(value = "should throw error when name is null", method = "saveVisitType(VisitType)")
	public void saveVisitType_shouldThrowErrorWhenNameIsNull() throws Exception {
		Context.getVisitService().saveVisitType(new VisitType());
	}
	
	@Test(expected = APIException.class)
	@Verifies(value = "should throw error when name is empty string", method = "saveVisitType(VisitType)")
	public void saveVisitType_shouldThrowErrorWhenNameIsEmptyString() throws Exception {
		VisitType visitType = new VisitType("", null);
		Context.getVisitService().saveVisitType(visitType);
	}
	
	/**
	 * @see VisitService#getAllVisitAttributeTypes()
	 * @verifies return all visit attribute types including retired ones
	 */
	@Test
	public void getAllVisitAttributeTypes_shouldReturnAllVisitAttributeTypesIncludingRetiredOnes() throws Exception {
		executeDataSet(VISITS_ATTRIBUTES_XML);
		Assert.assertEquals(2, service.getAllVisitAttributeTypes().size());
	}
	
	/**
	 * @see VisitService#getVisitAttributeType(Integer)
	 * @verifies return the visit attribute type with the given id
	 */
	@Test
	public void getVisitAttributeType_shouldReturnTheVisitAttributeTypeWithTheGivenId() throws Exception {
		executeDataSet(VISITS_ATTRIBUTES_XML);
		Assert.assertEquals("Audit Date", service.getVisitAttributeType(1).getName());
	}
	
	/**
	 * @see VisitService#getVisitAttributeType(Integer)
	 * @verifies return null if no visit attribute type exists with the given id
	 */
	@Test
	public void getVisitAttributeType_shouldReturnNullIfNoVisitAttributeTypeExistsWithTheGivenId() throws Exception {
		executeDataSet(VISITS_ATTRIBUTES_XML);
		Assert.assertNull(service.getVisitAttributeType(999));
	}
	
	/**
	 * @see VisitService#getVisitAttributeTypeByUuid(String)
	 * @verifies return the visit attribute type with the given uuid
	 */
	@Test
	public void getVisitAttributeTypeByUuid_shouldReturnTheVisitAttributeTypeWithTheGivenUuid() throws Exception {
		executeDataSet(VISITS_ATTRIBUTES_XML);
		Assert.assertEquals("Audit Date", service.getVisitAttributeTypeByUuid("9516cc50-6f9f-11e0-8414-001e378eb67e")
		        .getName());
	}
	
	/**
	 * @see VisitService#getVisitAttributeTypeByUuid(String)
	 * @verifies return null if no visit attribute type exists with the given uuid
	 */
	@Test
	public void getVisitAttributeTypeByUuid_shouldReturnNullIfNoVisitAttributeTypeExistsWithTheGivenUuid() throws Exception {
		executeDataSet(VISITS_ATTRIBUTES_XML);
		Assert.assertNull(service.getVisitAttributeTypeByUuid("not-a-uuid"));
	}
	
	/**
	 * @see VisitService#purgeVisitAttributeType(VisitAttributeType)
	 * @verifies completely remove a visit attribute type
	 */
	@Test
	public void purgeVisitAttributeType_shouldCompletelyRemoveAVisitAttributeType() throws Exception {
		executeDataSet(VISITS_ATTRIBUTES_XML);
		Assert.assertEquals(2, service.getAllVisitAttributeTypes().size());
		service.purgeVisitAttributeType(service.getVisitAttributeType(2));
		Assert.assertEquals(1, service.getAllVisitAttributeTypes().size());
	}
	
	/**
	 * @see VisitService#retireVisitAttributeType(VisitAttributeType,String)
	 * @verifies retire a visit attribute type
	 */
	@Test
	public void retireVisitAttributeType_shouldRetireAVisitAttributeType() throws Exception {
		executeDataSet(VISITS_ATTRIBUTES_XML);
		VisitAttributeType vat = service.getVisitAttributeType(1);
		Assert.assertFalse(vat.isRetired());
		service.retireVisitAttributeType(vat, "for testing");
		vat = service.getVisitAttributeType(1);
		Assert.assertTrue(vat.isRetired());
		Assert.assertNotNull(vat.getRetiredBy());
		Assert.assertNotNull(vat.getDateRetired());
		Assert.assertEquals("for testing", vat.getRetireReason());
	}
	
	/**
	 * @see VisitService#saveVisitAttributeType(VisitAttributeType)
	 * @verifies create a new visit attribute type
	 */
	@Test
	public void saveVisitAttributeType_shouldCreateANewVisitAttributeType() throws Exception {
		executeDataSet(VISITS_ATTRIBUTES_XML);
		Assert.assertEquals(2, service.getAllVisitAttributeTypes().size());
		VisitAttributeType vat = new VisitAttributeType();
		vat.setName("Another one");
		vat.setDatatype("string");
		service.saveVisitAttributeType(vat);
		Assert.assertEquals(3, service.getAllVisitAttributeTypes().size());
	}
	
	/**
	 * @see VisitService#saveVisitAttributeType(VisitAttributeType)
	 * @verifies edit an existing visit attribute type
	 */
	@Test
	public void saveVisitAttributeType_shouldEditAnExistingVisitAttributeType() throws Exception {
		executeDataSet(VISITS_ATTRIBUTES_XML);
		Assert.assertEquals(2, service.getAllVisitAttributeTypes().size());
		VisitAttributeType vat = service.getVisitAttributeType(1);
		vat.setName("A new name");
		service.saveVisitAttributeType(vat);
		Assert.assertEquals(2, service.getAllVisitAttributeTypes().size());
		Assert.assertEquals("A new name", service.getVisitAttributeType(1).getName());
	}
	
	/**
	 * @see VisitService#unretireVisitAttributeType(VisitAttributeType)
	 * @verifies unretire a retired visit attribute type
	 */
	@Test
	public void unretireVisitAttributeType_shouldUnretireARetiredVisitAttributeType() throws Exception {
		executeDataSet(VISITS_ATTRIBUTES_XML);
		VisitAttributeType vat = service.getVisitAttributeType(2);
		Assert.assertTrue(vat.isRetired());
		Assert.assertNotNull(vat.getDateRetired());
		Assert.assertNotNull(vat.getRetiredBy());
		Assert.assertNotNull(vat.getRetireReason());
		service.unretireVisitAttributeType(vat);
		Assert.assertFalse(vat.isRetired());
		Assert.assertNull(vat.getDateRetired());
		Assert.assertNull(vat.getRetiredBy());
		Assert.assertNull(vat.getRetireReason());
	}
	
	/**
	 * @see VisitService#getVisitAttributeByUuid(String)
	 * @verifies get the visit attribute with the given uuid
	 */
	@Test
	public void getVisitAttributeByUuid_shouldGetTheVisitAttributeWithTheGivenUuid() throws Exception {
		executeDataSet(VISITS_ATTRIBUTES_XML);
		Assert.assertEquals("2011-04-25", service.getVisitAttributeByUuid("3a2bdb18-6faa-11e0-8414-001e378eb67e")
		        .getSerializedValue());
	}
	
	/**
	 * @see VisitService#getVisitAttributeByUuid(String)
	 * @verifies return null if no visit attribute has the given uuid
	 */
	@Test
	public void getVisitAttributeByUuid_shouldReturnNullIfNoVisitAttributeHasTheGivenUuid() throws Exception {
		executeDataSet(VISITS_ATTRIBUTES_XML);
		Assert.assertNull(service.getVisitAttributeByUuid("not-a-uuid"));
	}
	
	/**
	 * @see VisitService#getVisits(Collection,Collection,Collection,Collection,Date,Date,Date,Date,Map,boolean)
	 * @verifies get all visits with given attribute values
	 */
	@Test
	public void getVisits_shouldGetAllVisitsWithGivenAttributeValues() throws Exception {
		executeDataSet(VISITS_ATTRIBUTES_XML);
		Map<VisitAttributeType, Object> attrs = new HashMap<VisitAttributeType, Object>();
		attrs.put(service.getVisitAttributeType(1), new SimpleDateFormat("yyyy-MM-dd").parse("2011-04-25"));
		List<Visit> visits = service.getVisits(null, null, null, null, null, null, null, null, attrs, false);
		Assert.assertEquals(1, visits.size());
		Assert.assertEquals(Integer.valueOf(1), visits.get(0).getVisitId());
	}
	
	/**
	 * @see VisitService#getVisits(Collection,Collection,Collection,Collection,Date,Date,Date,Date,Map,boolean)
	 * @verifies not find any visits if none have given attribute values
	 */
	@Test
	public void getVisits_shouldNotFindAnyVisitsIfNoneHaveGivenAttributeValues() throws Exception {
		executeDataSet(VISITS_ATTRIBUTES_XML);
		Map<VisitAttributeType, Object> attrs = new HashMap<VisitAttributeType, Object>();
		attrs.put(service.getVisitAttributeType(1), new SimpleDateFormat("yyyy-MM-dd").parse("1411-04-25"));
		List<Visit> visits = service.getVisits(null, null, null, null, null, null, null, null, attrs, false);
		Assert.assertEquals(0, visits.size());
	}
	
	/**
	 * @see {@link VisitService#saveVisit(Visit)}
	 */
	@Test(expected = APIException.class)
	@Verifies(value = "should fail if validation errors are found", method = "saveVisit(Visit)")
	public void saveVisit_shouldFailIfValidationErrorsAreFound() throws Exception {
		VisitService vs = Context.getVisitService();
		Visit visit = new Visit();
		//Not setting the patient so that we get validation errors
		visit.setVisitType(vs.getVisitType(1));
		visit.setStartDatetime(new Date());
		Context.getVisitService().saveVisit(visit);
	}
	
	/**
	 * @see {@link VisitService#saveVisit(Visit)}
	 */
	@Test
	@Verifies(value = "should pass if no validation errors are found", method = "saveVisit(Visit)")
	public void saveVisit_shouldPassIfNoValidationErrorsAreFound() throws Exception {
		VisitService vs = Context.getVisitService();
		Visit visit = new Visit();
		visit.setPatient(Context.getPatientService().getPatient(2));
		visit.setVisitType(vs.getVisitType(1));
		visit.setStartDatetime(new Date());
		Context.getVisitService().saveVisit(visit);
	}
	
	/**
	 * @see {@link VisitService#purgeVisit(Visit)}
	 */
	@Test(expected = APIException.class)
	@Verifies(value = "should fail if the visit has encounters associated to it", method = "purgeVisit(Visit)")
	public void purgeVisit_shouldFailIfTheVisitHasEncountersAssociatedToIt() throws Exception {
		Visit visit = Context.getVisitService().getVisit(1);
		Encounter e = Context.getEncounterService().getEncounter(3);
		e.setVisit(visit);
		Context.getEncounterService().saveEncounter(e);
		//sanity check
		Assert.assertTrue(Context.getEncounterService().getEncountersByVisit(visit).size() > 0);
		Context.getVisitService().purgeVisit(visit);
	}
	
	/**
	 * @see VisitService#saveVisit(Visit)
	 * @verifies be able to add an attribute to a visit
	 */
	@Test
	public void saveVisit_shouldBeAbleToAddAnAttributeToAVisit() throws Exception {
		Visit visit = service.getVisit(1);
		VisitAttributeType attrType = service.getVisitAttributeType(1);
		VisitAttribute attr = new VisitAttribute();
		attr.setAttributeType(attrType);
		attr.setObjectValue(new Date());
		visit.addAttribute(attr);
		service.saveVisit(visit);
	}
}

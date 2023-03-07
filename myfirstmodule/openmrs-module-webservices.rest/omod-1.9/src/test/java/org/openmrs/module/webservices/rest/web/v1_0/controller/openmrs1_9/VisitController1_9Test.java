/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.controller.openmrs1_9;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.beanutils.PropertyUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Encounter;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.VisitAttribute;
import org.openmrs.VisitAttributeType;
import org.openmrs.api.VisitService;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.test.Util;
import org.openmrs.module.webservices.rest.web.RestTestConstants1_9;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.bind.annotation.RequestMethod;

public class VisitController1_9Test extends MainResourceControllerTest {
	
	private VisitService service;
	
	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest#getURI()
	 */
	@Override
	public String getURI() {
		return "visit";
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest#getUuid()
	 */
	@Override
	public String getUuid() {
		return RestTestConstants1_9.VISIT_UUID;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest#getAllCount()
	 */
	@Override
	public long getAllCount() {
		return service.getAllVisits().size();
	}
	
	@Before
	public void before() {
		this.service = Context.getVisitService();
	}
	
	@Test
	public void shouldCreateAVisit() throws Exception {
		int originalCount = service.getAllVisits().size();
		String json = "{ \"patient\":\"5946f880-b197-400b-9caa-a3c661d23041\", \"visitType\":\""
		        + RestTestConstants1_9.VISIT_TYPE_UUID + "\", \"location\":\"" + RestTestConstants1_9.LOCATION_UUID
		        + "\", \"startDatetime\":\"" + DATE_FORMAT.format(new Date()) + "\"}";
		
		Object newVisit = deserialize(handle(newPostRequest(getURI(), json)));
		
		Assert.assertNotNull(PropertyUtils.getProperty(newVisit, "uuid"));
		Assert.assertEquals(originalCount + 1, service.getAllVisits().size());
	}
	
	@Test
	public void shouldGetAVisitByUuid() throws Exception{
		MockHttpServletRequest req = request(RequestMethod.GET, getURI() + "/" + getUuid());
		SimpleObject result = deserialize(handle(req));
		
		Visit visit = service.getVisitByUuid(getUuid());
		assertEquals(visit.getUuid(), PropertyUtils.getProperty(result, "uuid"));
	}
	
	@Test
	public void shouldCreateVisitWithoutStartDatetime() throws Exception {
		int originalCount = service.getAllVisits().size();
		String json = "{ \"patient\":\"5946f880-b197-400b-9caa-a3c661d23041\", \"visitType\":\""
		        + RestTestConstants1_9.VISIT_TYPE_UUID + "\", \"location\":\"" + RestTestConstants1_9.LOCATION_UUID + "\"}";
		
		Object newVisit = deserialize(handle(newPostRequest(getURI(), json)));
		
		Assert.assertNotNull(PropertyUtils.getProperty(newVisit, "uuid"));
		Assert.assertEquals(originalCount + 1, service.getAllVisits().size());
	}

	@Test
	public void shouldCreateAVisitWithEncounters() throws Exception {
		int originalCount = service.getAllVisits().size();
		final String patientUuid = "5946f880-b197-400b-9caa-a3c661d23041";
		Patient patient = Context.getPatientService().getPatientByUuid(patientUuid);
		Assert.assertEquals(0, service.getVisitsByPatient(patient).size());
		String json = "{ \"patient\":\"5946f880-b197-400b-9caa-a3c661d23041\", \"visitType\":\""
		        + RestTestConstants1_9.VISIT_TYPE_UUID
		        + "\", \"location\":\""
		        + RestTestConstants1_9.LOCATION_UUID
		        + "\", \"startDatetime\":\""
		        + DATE_FORMAT.format(new Date())
		        + "\", \"encounters\": [\"6519d653-393b-4118-9c83-a3715b82d4ac\", \"eec646cb-c847-45a7-98bc-91c8c4f70add\"] }";
		Object newVisit = deserialize(handle(newPostRequest(getURI(), json)));
		Assert.assertNotNull(PropertyUtils.getProperty(newVisit, "uuid"));
		Assert.assertEquals(originalCount + 1, service.getAllVisits().size());
		Assert.assertEquals(2, service.getVisitsByPatient(patient).get(0).getEncounters().size());
	}
	
	@Test
	public void shouldCreateAVisitWithEncounterAndObs() throws Exception {
		int originalCount = service.getAllVisits().size();
		final String patientUuid = "5946f880-b197-400b-9caa-a3c661d23041";
		Patient patient = Context.getPatientService().getPatientByUuid(patientUuid);
		Assert.assertEquals(0, service.getVisitsByPatient(patient).size());
		String json = "{ \"patient\":\"5946f880-b197-400b-9caa-a3c661d23041\", \"visitType\":\""
		        + RestTestConstants1_9.VISIT_TYPE_UUID
		        + "\", \"location\":\""
		        + RestTestConstants1_9.LOCATION_UUID
		        + "\", \"startDatetime\":\""
		        + DATE_FORMAT.format(new Date())
		        + "\", \"encounters\": [{\"patient\":\"5946f880-b197-400b-9caa-a3c661d23041\", \"obs\": [{\"concept\":\"89ca642a-dab6-4f20-b712-e12ca4fc6d36\", \"value\":\"b055abd8-a420-4a11-8b98-02ee170a7b54\"}]}] }}] }";
		Object newVisit = deserialize(handle(newPostRequest(getURI(), json)));
		Assert.assertNotNull(PropertyUtils.getProperty(newVisit, "uuid"));
		Assert.assertEquals(originalCount + 1, service.getAllVisits().size());
		Assert.assertEquals(1, service.getVisitsByPatient(patient).get(0).getEncounters().size());
		Iterator<Encounter> encouters = service.getVisitsByPatient(patient).get(0).getEncounters().iterator();
		Assert.assertEquals(1, encouters.next().getObs().size());
	}
	
	@Test
	public void shouldCreateAVisitWithAttributes() throws Exception {
		int originalCount = service.getAllVisits().size();
		String json = "{ \"patient\":\"5946f880-b197-400b-9caa-a3c661d23041\", \"visitType\":\""
		        + RestTestConstants1_9.VISIT_TYPE_UUID + "\", \"location\":\"" + RestTestConstants1_9.LOCATION_UUID
		        + "\", \"startDatetime\":\"" + DATE_FORMAT.format(new Date()) + "\","
		        + "\"attributes\":[{\"attributeType\":\"" + RestTestConstants1_9.VISIT_ATTRIBUTE_TYPE_UUID
		        + "\",\"value\":\"2012-12-01\"}]}";
		Object newVisit = deserialize(handle(newPostRequest(getURI(), json)));
		Assert.assertNotNull(PropertyUtils.getProperty(newVisit, "uuid"));
		Assert.assertEquals(originalCount + 1, service.getAllVisits().size());
	}
	
	@Test
	public void shouldEditAVisit() throws Exception {
		final String newVisitTypeUuid = RestTestConstants1_9.VISIT_TYPE_UUID;
		final String newLocationUuid = "9356400c-a5a2-4532-8f2b-2361b3446eb8";
		final String newIndicationConceptUuid = "c607c80f-1ea9-4da3-bb88-6276ce8868dd";
		final Date newStartDatetime = new Date();
		final Date newStopDatetime = new Date();
		Visit visit = service.getVisitByUuid(RestTestConstants1_9.VISIT_UUID);
		Assert.assertNotNull(visit);
		//sanity checks
		Assert.assertFalse(newVisitTypeUuid.equalsIgnoreCase(visit.getVisitType().getUuid()));
		Assert.assertFalse(newLocationUuid.equalsIgnoreCase(visit.getLocation().getUuid()));
		Assert.assertFalse(newIndicationConceptUuid.equalsIgnoreCase(visit.getIndication().getUuid()));
		Assert.assertFalse(newStartDatetime.equals(visit.getStartDatetime()));
		Assert.assertFalse(newStopDatetime.equals(visit.getStopDatetime()));
		
		String json = "{ \"visitType\":\"" + newVisitTypeUuid + "\", \"location\":\"" + newLocationUuid
		        + "\", \"indication\":\"" + newIndicationConceptUuid + "\", \"startDatetime\":\""
		        + DATE_FORMAT.format(newStartDatetime) + "\", \"stopDatetime\":\"" + DATE_FORMAT.format(newStopDatetime)
		        + "\" }";
		
		handle(newPostRequest(getURI() + "/" + getUuid(), json));
		
		Visit updated = service.getVisitByUuid(RestTestConstants1_9.VISIT_UUID);
		Assert.assertNotNull(updated);
		Assert.assertEquals(newVisitTypeUuid, updated.getVisitType().getUuid());
		Assert.assertEquals(newLocationUuid, updated.getLocation().getUuid());
		Assert.assertEquals(newIndicationConceptUuid, updated.getIndication().getUuid());
		Assert.assertEquals(newStartDatetime, updated.getStartDatetime());
		Assert.assertEquals(newStopDatetime, updated.getStopDatetime());
	}
	
	@Test
	public void shouldEditAVisitWithAttributes() throws Exception {
		executeDataSet(RestTestConstants1_9.TEST_DATASET);
		
		SimpleObject visit = deserialize(handle(newGetRequest(getURI() + "/" + getUuid(), new Parameter("v", "full"))));
		List<Map> attributes = visit.get("attributes");
		Map toEdit = attributes.get(1);
		VisitAttribute toEditAttribute = service.getVisitAttributeByUuid(toEdit.get("uuid").toString());
		Visit existingVisit = service.getVisitByUuid(getUuid());
		final int originalActiveCount = attributes.size();
		assertEquals(2, originalActiveCount);
		assertEquals(3, existingVisit.getAttributes().size());
		//Let's add a new attribute and also updating an existing one
		VisitAttributeType vat = service.getVisitAttributeTypeByUuid("6770f6d6-7673-11e0-8f03-001e378eb67g");
		assertTrue(existingVisit.getActiveAttributes(vat).isEmpty());
		SimpleObject toAdd = new SimpleObject();
		toAdd.add("attributeType", vat.getUuid());
		toAdd.add("value", "2005-01-01");
		attributes.add(toAdd);
		
		final String newValue = "2006-01-01";
		assertNotEquals(newValue, toEditAttribute.getValueReference());
		toEdit.put("value", newValue);
		
		deserialize(handle(newPostRequest(getURI() + "/" + getUuid(), visit)));
		
		assertEquals(originalActiveCount + 1, existingVisit.getActiveAttributes().size());
		assertEquals(newValue, toEditAttribute.getValueReference());
		
	}
	
	@Test
	public void shouldAddEncountersToAnExistingVisitOnEdit() throws Exception {
		Visit visit = service.getVisitByUuid(RestTestConstants1_9.VISIT_UUID);
		Assert.assertEquals(0, visit.getEncounters().size());
		
		String json = "{\"encounters\": [\"6519d653-393b-4118-9c83-a3715b82d4ac\", \"eec646cb-c847-45a7-98bc-91c8c4f70add\"] }";
		
		handle(newPostRequest(getURI() + "/" + getUuid(), json));
		
		Visit updated = service.getVisitByUuid(RestTestConstants1_9.VISIT_UUID);
		Assert.assertEquals(2, updated.getEncounters().size());
	}
	
	@Test
	public void shouldNotReturnVoidedEncounters() throws Exception {
		final String voidedEncounterId = "6519d653-393b-4118-9c83-a3715b82d4ac";
		Visit visit = service.getVisitByUuid(RestTestConstants1_9.VISIT_UUID);
		Assert.assertEquals(0, visit.getEncounters().size());
		
		String json = "{\"encounters\": [\"" + voidedEncounterId + "\", \"eec646cb-c847-45a7-98bc-91c8c4f70add\"] }";
		// add 2 encounters to the visit
		handle(newPostRequest(getURI() + "/" + getUuid(), json));
		
		Visit updated = service.getVisitByUuid(RestTestConstants1_9.VISIT_UUID);
		Assert.assertEquals(2, updated.getEncounters().size());
		
		SimpleObject result = deserialize(handle(newGetRequest(getURI() + "/" + getUuid(), new Parameter("v", "full"))));
		List<Map> encounters = result.get("encounters");
		Assert.assertEquals(2, encounters.size());
		// void one of the encounters
		Encounter encounter = Context.getEncounterService().getEncounterByUuid(voidedEncounterId);
		encounter = Context.getEncounterService().voidEncounter(encounter, "test visit");
		
		result = deserialize(handle(newGetRequest(getURI() + "/" + getUuid(), new Parameter("v", "full"))));
		encounters = result.get("encounters");
		// visit contains now just the one non-voided encounter
		Assert.assertEquals(1, encounters.size());
	}
	
	@Test
	public void shouldRemoveAnEncounterFromAnExistingVisitOnEdit() throws Exception {
		final String encounterId = "6519d653-393b-4118-9c83-a3715b82d4ac";
		Visit visit = service.getVisitByUuid(RestTestConstants1_9.VISIT_UUID);
		//add an encounter to be removed for testing purposes
		visit.getEncounters().add(Context.getEncounterService().getEncounterByUuid(encounterId));
		service.saveVisit(visit);
		Assert.assertEquals(1, visit.getEncounters().size());
		
		String json = "{\"encounters\": [] }";
		
		handle(newPostRequest(getURI() + "/" + getUuid(), json));
		
		Visit updated = service.getVisitByUuid(RestTestConstants1_9.VISIT_UUID);
		Assert.assertEquals(0, updated.getEncounters().size());
	}
	
	@Test
	public void shouldVoidAVisit() throws Exception {
		Visit visit = service.getVisitByUuid(RestTestConstants1_9.VISIT_UUID);
		Assert.assertFalse(visit.isVoided());
		
		handle(newDeleteRequest(getURI() + "/" + getUuid(), new Parameter("reason", "test reason")));
		
		visit = service.getVisitByUuid(RestTestConstants1_9.VISIT_UUID);
		Assert.assertTrue(visit.isVoided());
		Assert.assertEquals("test reason", visit.getVoidReason());
	}
	
	@Test
	public void shouldUnVoidAVisit() throws Exception {
		Visit visit = service.getVisitByUuid(RestTestConstants1_9.VISIT_UUID);
		service.voidVisit(visit, "some random reason");
		visit = service.getVisitByUuid(RestTestConstants1_9.VISIT_UUID);
		Assert.assertTrue(visit.isVoided());
		
		String json = "{\"deleted\": \"false\"}";
		SimpleObject response = deserialize(handle(newPostRequest(getURI() + "/" + getUuid(), json)));
		
		visit = service.getVisitByUuid(getUuid());
		Assert.assertFalse(visit.isVoided());
		Assert.assertEquals("false", PropertyUtils.getProperty(response, "voided").toString());
		
	}
	
	@Test
	public void shouldPurgeAVisit() throws Exception {
		Assert.assertNotNull(service.getVisitByUuid(RestTestConstants1_9.VISIT_UUID));
		int originalCount = service.getAllVisits().size();
		
		handle(newDeleteRequest(getURI() + "/" + getUuid(), new Parameter("purge", "true")));
		
		Assert.assertNull(service.getVisitByUuid(RestTestConstants1_9.VISIT_UUID));
		Assert.assertEquals(originalCount - 1, service.getAllVisits().size());
	}
	
	/**
	 * @see {@link VisitController#searchByPatient(String,HttpServletRequest,HttpServletResponse)}
	 */
	@Test
	public void searchByPatient_shouldGetUnretiredVisitsForThePatient() throws Exception {
		SimpleObject result = deserialize(handle(newGetRequest(getURI(), new Parameter("patient",
		        "da7f524f-27ce-4bb2-86d6-6d1d05312bd5"))));
		
		Assert.assertEquals(3, Util.getResultsSize(result));
	}
	
	@Test
	public void searchBylocation_shouldGetUnretiredVisitsAtLocation() throws Exception {
		SimpleObject result = deserialize(handle(newGetRequest(getURI(), new Parameter("location",
		        "8d6c993e-c2cc-11de-8d13-0010c6dffd0f"))));
		
		Assert.assertEquals(1, Util.getResultsSize(result));
	}
	
	@Test
	public void searchByPatient_shouldGetRetiredVisitsIfIncludeAllIsTrue() throws Exception {
		String patientUUid = "da7f524f-27ce-4bb2-86d6-6d1d05312bd5";
		
		SimpleObject resultWithVoidedVisits = deserialize(handle(newGetRequest(getURI(), new Parameter("patient",
		        patientUUid), new Parameter("includeAll", "true"))));
		SimpleObject resultWithoutVoidedVisits = deserialize(handle(newGetRequest(getURI(), new Parameter("patient",
		        patientUUid))));
		
		int visitIncludingVoidedSize = Util.getResultsSize(resultWithVoidedVisits);
		int visitExcludingVoidedSize = Util.getResultsSize(resultWithoutVoidedVisits);
		
		Assert.assertEquals(4, visitIncludingVoidedSize);
		Assert.assertEquals(3, visitExcludingVoidedSize);
		
		handle(newDeleteRequest(getURI() + "/" + getUuid(), new Parameter("reason", "void test reason")));
		
		resultWithVoidedVisits = deserialize(handle(newGetRequest(getURI(), new Parameter("patient", patientUUid),
		    new Parameter("includeAll", "true"))));
		resultWithoutVoidedVisits = deserialize(handle(newGetRequest(getURI(), new Parameter("patient", patientUUid))));
		Assert.assertEquals(2, Util.getResultsSize(resultWithoutVoidedVisits));
		Assert.assertEquals(4, Util.getResultsSize(resultWithVoidedVisits));
	}
	
	@Test
	public void searchByVisitType_shouldGetVisits() throws Exception {
		String patientUUid = "da7f524f-27ce-4bb2-86d6-6d1d05312bd5"; // patient_id = 2, Horatio Hornblower
		String visitTypeUuid = "759799ab-c9a5-435e-b671-77773ada74e4"; //Return TB Clinic Visit
		
		SimpleObject allVisits = deserialize(handle(newGetRequest(getURI(), new Parameter("patient",
		        patientUUid))));
		
		SimpleObject filteredVisits = deserialize(handle(newGetRequest(getURI(), new Parameter("patient",
		        patientUUid), new Parameter("visitType", visitTypeUuid))));
		
		Assert.assertEquals(3, Util.getResultsSize(allVisits));
		int filteredVisitsSize = Util.getResultsSize(filteredVisits);
		Assert.assertEquals(1, filteredVisitsSize);
		
	}
	
}

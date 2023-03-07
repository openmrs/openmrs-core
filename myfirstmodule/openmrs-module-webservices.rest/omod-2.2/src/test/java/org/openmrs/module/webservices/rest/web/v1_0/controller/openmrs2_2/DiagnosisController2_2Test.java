/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.controller.openmrs2_2;

import static org.openmrs.ConditionVerificationStatus.CONFIRMED;

import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.beanutils.PropertyUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.Condition;
import org.openmrs.Diagnosis;
import org.openmrs.Encounter;
import org.openmrs.Patient;
import org.openmrs.api.DiagnosisService;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.module.webservices.rest.web.v1_0.RestTestConstants2_2;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Tests functionality of {@link DiagnosisController2_2}.
 */
public class DiagnosisController2_2Test extends MainResourceControllerTest {
	
	private DiagnosisService diagnosisService;
	
	private Encounter encounter;
	
	private Patient patient;
	
	private Condition condition;
	
	private Concept concept;
	
	private ConceptName conceptName;
	
	@Before
	public void before() throws Exception {
		executeDataSet(RestTestConstants2_2.DIAGNOSIS_TEST_DATA_XML);
		this.diagnosisService = Context.getDiagnosisService();
		this.encounter = Context.getEncounterService().getEncounter(1);
		this.patient = Context.getPatientService().getPatient(1);
		this.condition = Context.getConditionService().getCondition(1);
		this.concept = Context.getConceptService().getConcept(1);
		this.conceptName = Context.getConceptService().getConceptName(1);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest#getURI()
	 */
	@Override
	public String getURI() {
		return "patientdiagnoses";
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest#getUuid()
	 */
	@Override
	public String getUuid() {
		return RestTestConstants2_2.DIAGNOSIS_UUID;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest#getAllCount()
	 */
	@Override
	public long getAllCount() {
		return diagnosisService.getPrimaryDiagnoses(this.encounter).size();
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest#shouldGetAll()
	 */
	@Override
	@Test(expected = ResourceDoesNotSupportOperationException.class)
	public void shouldGetAll() throws Exception {
		super.shouldGetAll();
	}
	
	@Test
	public void shouldCreateANonCodedDiagnosis() throws Exception {
		long originalCount = getAllCount();
		
		SimpleObject codedOrFreeText = new SimpleObject();
		
		codedOrFreeText.add("nonCoded", "Some condition");
		
		SimpleObject diagnosisSource = new SimpleObject();
		
		diagnosisSource.add("condition", condition.getUuid());
		diagnosisSource.add("certainty", CONFIRMED);
		diagnosisSource.add("diagnosis", codedOrFreeText);
		diagnosisSource.add("rank", 1);
		diagnosisSource.add("encounter", encounter.getUuid());
		diagnosisSource.add("voided", false);
		diagnosisSource.add("patient", patient.getUuid());
		
		String json = new ObjectMapper().writeValueAsString(diagnosisSource);
		
		MockHttpServletRequest req = request(RequestMethod.POST, getURI());
		req.setContent(json.getBytes());
		
		SimpleObject newDiagnosisSource = deserialize(handle(req));
		
		Assert.assertNotNull(PropertyUtils.getProperty(newDiagnosisSource, "uuid"));
		
		Diagnosis diagnosis = diagnosisService.getDiagnosisByUuid(newDiagnosisSource.get("uuid").toString());
		
		LinkedHashMap condition = newDiagnosisSource.get("condition");
		
		LinkedHashMap nonCoded = newDiagnosisSource.get("diagnosis");
		
		LinkedHashMap patient = newDiagnosisSource.get("patient");
		
		Assert.assertNotNull(diagnosis.getEncounter().toString());
		Assert.assertEquals(diagnosis.getCondition().getUuid(), condition.get("uuid"));
		Assert.assertEquals((diagnosis.getCertainty().toString()), newDiagnosisSource.get("certainty"));
		Assert.assertEquals(diagnosis.getRank(), newDiagnosisSource.get("rank"));
		Assert.assertEquals(diagnosis.getEncounter().getUuid(), encounter.getUuid());
		Assert.assertNotNull(newDiagnosisSource.get("encounter"));
		Assert.assertEquals(diagnosis.getVoided(), newDiagnosisSource.get("voided"));
		Assert.assertEquals(diagnosis.getDiagnosis().getNonCoded(), nonCoded.get("nonCoded"));
		Assert.assertEquals(patient.get("uuid"), diagnosis.getPatient().getUuid());
		Assert.assertEquals(originalCount + 1, getAllCount());
	}
	
	@Test
	public void shouldCreateACodedDiagnosis() throws Exception {
		long originalCount = getAllCount();
		
		SimpleObject codedOrFreeText = new SimpleObject();
		
		codedOrFreeText.add("coded", concept.getUuid());
		codedOrFreeText.add("specificName", conceptName.getUuid());
		
		SimpleObject diagnosisSource = new SimpleObject();
		
		diagnosisSource.add("condition", condition.getUuid());
		diagnosisSource.add("certainty", CONFIRMED);
		diagnosisSource.add("diagnosis", codedOrFreeText);
		diagnosisSource.add("rank", 1);
		diagnosisSource.add("encounter", encounter.getUuid());
		diagnosisSource.add("voided", false);
		diagnosisSource.add("patient", patient.getUuid());
		
		String json = new ObjectMapper().writeValueAsString(diagnosisSource);
		
		MockHttpServletRequest req = request(RequestMethod.POST, getURI());
		req.setContent(json.getBytes());
		
		SimpleObject newDiagnosisSource = deserialize(handle(req));
		
		Assert.assertNotNull(PropertyUtils.getProperty(newDiagnosisSource, "uuid"));
		
		Diagnosis diagnosis = diagnosisService.getDiagnosisByUuid(newDiagnosisSource.get("uuid").toString());
		
		LinkedHashMap condition = newDiagnosisSource.get("condition");
		
		LinkedHashMap codedOrText = newDiagnosisSource.get("diagnosis");
		
		LinkedHashMap conceptName = (LinkedHashMap) codedOrText.get("specificName");
		
		LinkedHashMap concept = (LinkedHashMap) codedOrText.get("coded");
		
		LinkedHashMap patient = newDiagnosisSource.get("patient");
		
		Assert.assertNotNull(diagnosis.getEncounter().toString());
		Assert.assertEquals(diagnosis.getCondition().getUuid(), condition.get("uuid"));
		Assert.assertEquals((diagnosis.getCertainty().toString()), newDiagnosisSource.get("certainty"));
		Assert.assertEquals(diagnosis.getRank(), newDiagnosisSource.get("rank"));
		Assert.assertEquals(diagnosis.getEncounter().getUuid(), encounter.getUuid());
		Assert.assertNotNull(newDiagnosisSource.get("encounter"));
		Assert.assertEquals(diagnosis.getVoided(), newDiagnosisSource.get("voided"));
		Assert.assertEquals(diagnosis.getDiagnosis().getCoded().getUuid(), concept.get("uuid"));
		Assert.assertEquals(diagnosis.getDiagnosis().getSpecificName().getUuid(), conceptName.get("uuid"));
		Assert.assertEquals(patient.get("uuid"), diagnosis.getPatient().getUuid());
		Assert.assertEquals(originalCount + 1, getAllCount());
	}
	
	@Test
	public void shouldFetchExistingDiagnosis() throws Exception {
		
		MockHttpServletRequest req = request(RequestMethod.GET, getURI() + "/" + getUuid());
		SimpleObject result = deserialize(handle(req));
		
		Diagnosis diagnosis = diagnosisService.getDiagnosisByUuid(getUuid());
		
		Assert.assertEquals(diagnosis.getUuid(), PropertyUtils.getProperty(result, "uuid"));
	}
	
	@Test
	public void shouldVoidDiagnosis() throws Exception {
		
		Diagnosis diagnosis = diagnosisService.getDiagnosisByUuid(getUuid());
		
		Assert.assertFalse(diagnosis.isVoided());
		
		MockHttpServletRequest req = request(RequestMethod.DELETE, getURI() + "/" + getUuid());
		req.addParameter("reason", "test");
		handle(req);
		
		diagnosis = diagnosisService.getDiagnosisByUuid(getUuid());
		Assert.assertTrue(diagnosis.isVoided());
		Assert.assertEquals("test", diagnosis.getVoidReason());
		Assert.assertNotNull(diagnosis.getDateVoided());
		Assert.assertNotNull(diagnosis.getVoidedBy());
		Assert.assertEquals(Context.getAuthenticatedUser().getUuid(), diagnosis.getVoidedBy().getUuid());
	}
	
	@Test
	public void shouldUnVoidDiagnosis() throws Exception {
		
		Diagnosis diagnosis = diagnosisService.getDiagnosisByUuid(RestTestConstants2_2.VOIDED_DIAGNOSIS_UUID);
		
		Assert.assertTrue(diagnosis.isVoided());
		
		SimpleObject attributes = new SimpleObject();
		attributes.add("voided", false);
		
		String json = new ObjectMapper().writeValueAsString(attributes);
		
		MockHttpServletRequest req = request(RequestMethod.POST, getURI() + "/" + RestTestConstants2_2.VOIDED_DIAGNOSIS_UUID);
		req.setContent(json.getBytes());
		handle(req);
		
		diagnosis = diagnosisService.getDiagnosisByUuid(RestTestConstants2_2.VOIDED_DIAGNOSIS_UUID);
		
		Assert.assertFalse(diagnosis.isVoided());
		Assert.assertNull(diagnosis.getDateVoided());
		Assert.assertNull(diagnosis.getVoidedBy());
		Assert.assertNull(diagnosis.getVoidReason());
		
	}
	
	@Test
	public void shouldUpdateANonCodedDiagnosis() throws Exception {
		Diagnosis diagnosis = diagnosisService.getDiagnosisByUuid(RestTestConstants2_2.UPDATABLE_NON_CODED_DIAGNOSIS_UUID);
		
		Assert.assertFalse(diagnosis.getVoided());
		Assert.assertNull(diagnosis.getDiagnosis().getCoded());
		Assert.assertEquals(2, (int) diagnosis.getRank());
		Assert.assertEquals("CONFIRMED", diagnosis.getCertainty().toString());
		Assert.assertEquals("e804ee60-ecbc-4d70-abda-1e4f6f64e5b5", diagnosis.getCondition().getUuid());
		Assert.assertEquals("34444-fcdb-4a5b-97ea-0d5c4b4315a1", diagnosis.getEncounter().getUuid());
		
		String json = "{\"diagnosis\":{\"coded\":\"" + concept.getUuid() + "\",\"specificName\":\"" + conceptName.getUuid()
		        + "\"},\"condition\":\"" + condition.getUuid()
		        + "\",\"certainty\":\"" + "PROVISIONAL" + "\",\"encounter\":\""
		        + encounter.getUuid() + "\",\"rank\":\"" + 1
		        + "\",\"voided\":\"" + true + "\"}";
		
		handle(newPostRequest(getURI() + "/" + RestTestConstants2_2.UPDATABLE_NON_CODED_DIAGNOSIS_UUID, json));
		
		Diagnosis newDiagnosis = diagnosisService
		        .getDiagnosisByUuid(RestTestConstants2_2.UPDATABLE_NON_CODED_DIAGNOSIS_UUID);
		
		Assert.assertTrue(newDiagnosis.getVoided());
		Assert.assertEquals(concept.getUuid(), newDiagnosis.getDiagnosis().getCoded().getUuid());
		Assert.assertEquals(1, (int) newDiagnosis.getRank());
		Assert.assertEquals(condition.getUuid(), newDiagnosis.getCondition().getUuid());
		Assert.assertEquals("PROVISIONAL", newDiagnosis.getCertainty().toString());
		Assert.assertEquals(encounter.getUuid(), newDiagnosis.getEncounter().getUuid());
	}
	
	@Test
	public void shouldUpdateACodedDiagnosis() throws Exception {
		
		final String nonCoded = "Some condition";
		Diagnosis diagnosis = diagnosisService.getDiagnosisByUuid(RestTestConstants2_2.UPDATABLE_CODED_DIAGNOSIS_UUID);
		
		Assert.assertTrue(diagnosis.getVoided());
		Assert.assertNull(diagnosis.getDiagnosis().getNonCoded());
		Assert.assertEquals(1, (int) diagnosis.getRank());
		Assert.assertEquals("e804ee60-ecbc-4d70-abda-1e4f6f64e5b5", diagnosis.getCondition().getUuid());
		Assert.assertEquals("54444-fcdb-4a5b-97ea-0d5c4b4315a1", diagnosis.getEncounter().getUuid());
		Assert.assertEquals("PROVISIONAL", diagnosis.getCertainty().toString());
		
		String json = "{ \"diagnosis\":{\"coded\":null,\"specificName\":null,\"nonCoded\":\"" + nonCoded
		        + "\"},\"condition\":\"" + condition.getUuid()
		        + "\",\"certainty\":\"" + "CONFIRMED" + "\",\"encounter\":\""
		        + encounter.getUuid() + "\",\"rank\":\"" + 2
		        + "\",\"voided\":\"" + false + "\"}";
		
		handle(newPostRequest(getURI() + "/" + RestTestConstants2_2.UPDATABLE_CODED_DIAGNOSIS_UUID, json));
		
		Diagnosis newDiagnosis = diagnosisService.getDiagnosisByUuid(RestTestConstants2_2.UPDATABLE_CODED_DIAGNOSIS_UUID);
		
		Assert.assertFalse(newDiagnosis.getVoided());
		Assert.assertEquals("Some condition", newDiagnosis.getDiagnosis().getNonCoded());
		Assert.assertEquals(2, (int) newDiagnosis.getRank());
		Assert.assertEquals(condition.getUuid(), newDiagnosis.getCondition().getUuid());
		Assert.assertEquals("CONFIRMED", newDiagnosis.getCertainty().toString());
		Assert.assertEquals(encounter.getUuid(), newDiagnosis.getEncounter().getUuid());
	}
	
	@Test
	public void shouldPurgeDiagnosis() throws Exception {
		Assert.assertNotNull(diagnosisService.getDiagnosisByUuid(getUuid()));
		
		MockHttpServletRequest req = request(RequestMethod.DELETE, getURI() + "/" + getUuid());
		req.addParameter("purge", "true");
		handle(req);
		
		Assert.assertNull(diagnosisService.getDiagnosisByUuid(getUuid()));
	}

	@Test
	public void shouldReturnPatientDiagnosis() throws Exception {
		MockHttpServletRequest request = request(RequestMethod.GET, getURI());
		request.addParameter("patientUuid", "ba1b19c2-3ed6-4f63-b8c0-f762dc8d7562");
		request.addParameter("fromDate", "2017-08-11");
		SimpleObject result = deserialize(handle(request));
		List<Diagnosis> diagnoses = result.get("results");
		Assert.assertEquals(2, diagnoses.size());
	}
}

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

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.CodedOrFreeText;
import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.Condition;
import org.openmrs.ConditionClinicalStatus;
import org.openmrs.ConditionVerificationStatus;
import org.openmrs.Patient;
import org.openmrs.api.ConditionService;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.module.webservices.rest.web.v1_0.RestTestConstants2_2;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.bind.annotation.RequestMethod;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Tests functionality of {@link ConditionController2_2}.
 */
public class ConditionController2_2Test extends MainResourceControllerTest {
	
	private ConditionService conditionService;
	
	private Patient patient;
	
	private Concept concept1, concept2;
	
	private ConceptName conceptName1, conceptName2;
	
	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
	
	@Before
	public void before() throws Exception {
		executeDataSet(RestTestConstants2_2.CONDITION_TEST_DATA_XML);
		
		this.conditionService = Context.getConditionService();
		this.patient = Context.getPatientService().getPatient(2);
		this.concept1 = Context.getConceptService().getConcept(111);
		this.concept2 = Context.getConceptService().getConcept(112);
		this.conceptName1 = Context.getConceptService().getConceptName(1111);
		this.conceptName2 = Context.getConceptService().getConceptName(1112);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest#getURI()
	 */
	@Override
	public String getURI() {
		return "condition";
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest#getUuid()
	 */
	@Override
	public String getUuid() {
		return RestTestConstants2_2.CODED_CONDITION_UUID;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest#getAllCount()
	 */
	@Override
	public long getAllCount() {
		List<Condition> activeConditions = conditionService.getActiveConditions(patient);
		int totalConditions = 0;
		
		if (activeConditions != null) {
			totalConditions = totalConditions + activeConditions.size();
		}
		
		return totalConditions;
	}
	
	@Test
	public void shouldCreateANonCodedCondition() throws Exception {
		CodedOrFreeText cond = new CodedOrFreeText();
		cond.setNonCoded("Some condition");
		
		SimpleObject conditionSource = new SimpleObject();
		conditionSource.add("condition", cond);
		conditionSource.add("patient", patient.getUuid());
		conditionSource.add("clinicalStatus", ConditionClinicalStatus.ACTIVE);
		conditionSource.add("verificationStatus", ConditionVerificationStatus.CONFIRMED);
		conditionSource.add("onsetDate", "2017-01-12 00:00:00");
		
		String json = new ObjectMapper().writeValueAsString(conditionSource);
		
		MockHttpServletRequest req = request(RequestMethod.POST, getURI());
		req.setContent(json.getBytes());
		
		MockHttpServletResponse res = handle(req);
		
		SimpleObject newConditionSource = deserialize(res);
		String uuid = newConditionSource.get("uuid");
		LinkedHashMap nonCoded = newConditionSource.get("condition");
		LinkedHashMap patient = newConditionSource.get("patient");
		
		Assert.assertNotNull(conditionService.getConditionByUuid(uuid));
		
		Condition condition = conditionService.getConditionByUuid(uuid);
		
		Assert.assertEquals(nonCoded.get("nonCoded"), condition.getCondition().getNonCoded());
		Assert.assertEquals(patient.get("uuid"), condition.getPatient().getUuid());
		Assert.assertEquals(newConditionSource.get("clinicalStatus"), condition.getClinicalStatus().toString());
		Assert.assertEquals(newConditionSource.get("verificationStatus"), condition.getVerificationStatus().toString());
		Assert.assertNotNull(newConditionSource.get("onsetDate"));
	}
	
	@Test
	public void shouldCreateACodedCondition() throws Exception {
		SimpleObject codedOrFreeText = new SimpleObject();
		codedOrFreeText.add("coded", concept1.getUuid());
		codedOrFreeText.add("specificName", conceptName1.getUuid());
		
		SimpleObject conditionSource = new SimpleObject();
		conditionSource.add("condition", codedOrFreeText);
		conditionSource.add("patient", patient.getUuid());
		conditionSource.add("clinicalStatus", ConditionClinicalStatus.ACTIVE);
		conditionSource.add("verificationStatus", ConditionVerificationStatus.CONFIRMED);
		conditionSource.add("onsetDate", "2017-01-12 00:00:00");
		
		String json = new ObjectMapper().writeValueAsString(conditionSource);
		
		MockHttpServletRequest req = request(RequestMethod.POST, getURI());
		req.setContent(json.getBytes());
		
		MockHttpServletResponse res = handle(req);
		
		SimpleObject newConditionSource = deserialize(res);
		String uuid = newConditionSource.get("uuid");
		LinkedHashMap cond = newConditionSource.get("condition");
		LinkedHashMap concept = (LinkedHashMap) cond.get("coded");
		LinkedHashMap conceptName = (LinkedHashMap) cond.get("specificName");
		LinkedHashMap patient = newConditionSource.get("patient");
		
		Assert.assertNotNull(conditionService.getConditionByUuid(uuid));
		
		Condition condition = conditionService.getConditionByUuid(uuid);
		
		Assert.assertEquals(concept.get("uuid"), condition.getCondition().getCoded().getUuid());
		Assert.assertEquals(conceptName.get("uuid"), condition.getCondition().getSpecificName().getUuid());
		Assert.assertEquals(patient.get("uuid"), condition.getPatient().getUuid());
		Assert.assertEquals(newConditionSource.get("clinicalStatus"), condition.getClinicalStatus().toString());
		Assert.assertEquals(newConditionSource.get("verificationStatus"), condition.getVerificationStatus().toString());
		Assert.assertNotNull(newConditionSource.get("onsetDate"));
	}
	
	@Test
	public void shouldUpdateANonCodedCondition() throws Exception {
		final String newNonCoded = "Updated non coded condition";
		final ConditionClinicalStatus newClinicalStatus = ConditionClinicalStatus.INACTIVE;
		final ConditionVerificationStatus newVerificationStatus = ConditionVerificationStatus.PROVISIONAL;
		final Date newOnsetDate = new Date();
		final Date newEndDate = new Date();
		final String newAdditionalDetail = "Some extra details.";
		
		Condition condition = conditionService.getConditionByUuid(RestTestConstants2_2.NON_CODED_CONDITION_UUID);
		
		Assert.assertNotNull(condition);
		//sanity checks
		Assert.assertFalse(newNonCoded.equalsIgnoreCase(condition.getCondition().getNonCoded()));
		Assert.assertFalse(newClinicalStatus.equals(condition.getClinicalStatus()));
		Assert.assertFalse(newVerificationStatus.equals(condition.getVerificationStatus()));
		Assert.assertFalse(newOnsetDate.equals(condition.getOnsetDate()));
		Assert.assertFalse(newEndDate.equals(condition.getEndDate()));
		Assert.assertFalse(newAdditionalDetail.equalsIgnoreCase(condition.getAdditionalDetail()));
		
		String json = "{ \"condition\":{\"coded\":null,\"specificName\":null,\"nonCoded\":\"" + newNonCoded
		        + "\"},\"clinicalStatus\":\"" + newClinicalStatus
		        + "\",\"verificationStatus\":\"" + newVerificationStatus + "\",\"onsetDate\":\""
		        + DATE_FORMAT.format(newOnsetDate) + "\",\"endDate\":\"" + DATE_FORMAT.format(newEndDate)
		        + "\",\"additionalDetail\":\"" + newAdditionalDetail + "\"}";
		
		handle(newPostRequest(getURI() + "/" + RestTestConstants2_2.NON_CODED_CONDITION_UUID, json));
		
		Condition updatedCondition = conditionService.getConditionByUuid(RestTestConstants2_2.NON_CODED_CONDITION_UUID);
		
		Assert.assertNotNull(updatedCondition);
		Assert.assertEquals(newNonCoded, updatedCondition.getCondition().getNonCoded());
		Assert.assertEquals(newClinicalStatus, updatedCondition.getClinicalStatus());
		Assert.assertEquals(newVerificationStatus, updatedCondition.getVerificationStatus());
		Assert.assertEquals(newOnsetDate.toString(), updatedCondition.getOnsetDate().toString());
		Assert.assertEquals(newEndDate.toString(), updatedCondition.getEndDate().toString());
		Assert.assertEquals(newAdditionalDetail, updatedCondition.getAdditionalDetail());
	}
	
	@Test
	public void shouldUpdateACodedCondition() throws Exception {
		final String newCoded = concept2.getUuid();
		final String newSpecificName = conceptName2.getUuid();
		final ConditionClinicalStatus newClinicalStatus = ConditionClinicalStatus.INACTIVE;
		final ConditionVerificationStatus newVerificationStatus = ConditionVerificationStatus.PROVISIONAL;
		final Date newOnsetDate = new Date();
		final Date newEndDate = new Date();
		final String newAdditionalDetail = "Some extra details.";
		
		Condition condition = conditionService.getConditionByUuid(RestTestConstants2_2.CODED_CONDITION_UUID);
		
		Assert.assertNotNull(condition);
		//sanity checks
		Assert.assertFalse(newCoded.equalsIgnoreCase(condition.getCondition().getCoded().getUuid()));
		Assert.assertFalse(newSpecificName.equalsIgnoreCase(condition.getCondition().getSpecificName().getUuid()));
		Assert.assertFalse(newClinicalStatus.equals(condition.getClinicalStatus()));
		Assert.assertFalse(newVerificationStatus.equals(condition.getVerificationStatus()));
		Assert.assertFalse(newOnsetDate.equals(condition.getOnsetDate()));
		Assert.assertFalse(newEndDate.equals(condition.getEndDate()));
		Assert.assertFalse(newAdditionalDetail.equalsIgnoreCase(condition.getAdditionalDetail()));
		
		String json = "{\"condition\":{\"coded\":\"" + newCoded + "\",\"specificName\":\"" + newSpecificName
		        + "\"},\"clinicalStatus\":\"" + newClinicalStatus
		        + "\",\"verificationStatus\":\"" + newVerificationStatus + "\",\"onsetDate\":\""
		        + DATE_FORMAT.format(newOnsetDate) + "\",\"endDate\":\"" + DATE_FORMAT.format(newEndDate)
		        + "\",\"additionalDetail\":\"" + newAdditionalDetail + "\"}";
		
		handle(newPostRequest(getURI() + "/" + RestTestConstants2_2.CODED_CONDITION_UUID, json));
		
		Condition updatedCondition = conditionService.getConditionByUuid(RestTestConstants2_2.CODED_CONDITION_UUID);
		
		Assert.assertNotNull(updatedCondition);
		Assert.assertEquals(newCoded, updatedCondition.getCondition().getCoded().getUuid());
		Assert.assertEquals(newSpecificName, updatedCondition.getCondition().getSpecificName().getUuid());
		Assert.assertEquals(newClinicalStatus, updatedCondition.getClinicalStatus());
		Assert.assertEquals(newVerificationStatus, updatedCondition.getVerificationStatus());
		Assert.assertEquals(newOnsetDate.toString(), updatedCondition.getOnsetDate().toString());
		Assert.assertEquals(newEndDate.toString(), updatedCondition.getEndDate().toString());
		Assert.assertEquals(newAdditionalDetail, updatedCondition.getAdditionalDetail());
	}
	
	@Test
	public void shouldVoidACondition() throws Exception {
		Condition condition = conditionService.getConditionByUuid(getUuid());
		Assert.assertFalse(condition.isVoided());
		
		MockHttpServletRequest req = request(RequestMethod.DELETE, getURI() + "/" + getUuid());
		req.addParameter("reason", "unit test");
		handle(req);
		
		condition = conditionService.getConditionByUuid(getUuid());
		Assert.assertEquals(Context.getAuthenticatedUser().getUuid(), condition.getVoidedBy().getUuid());
		Assert.assertTrue(condition.isVoided());
		Assert.assertEquals("unit test", condition.getVoidReason());
		Assert.assertNotNull(condition.getDateVoided());
	}
	
	@Test
	public void shouldUnvoidACondition() throws Exception {
		
		String voidedUuid = RestTestConstants2_2.VOIDED_CONDITION_UUID;
		
		Condition condition = conditionService.getConditionByUuid(voidedUuid);
		Assert.assertEquals(true, condition.getVoided());
		
		SimpleObject attributes = new SimpleObject();
		attributes.add("voided", false);
		
		String json = new ObjectMapper().writeValueAsString(attributes);
		
		MockHttpServletRequest req = request(RequestMethod.POST, getURI() + "/" + voidedUuid);
		req.setContent(json.getBytes());
		handle(req);
		
		condition = conditionService.getConditionByUuid(voidedUuid);
		Assert.assertEquals(false, condition.getVoided());
	}
	
	@Test
	public void shouldPurgeCondition() throws Exception {
		
		Assert.assertNotNull(conditionService.getConditionByUuid(getUuid()));
		
		MockHttpServletRequest req = request(RequestMethod.DELETE, getURI() + "/" + getUuid());
		req.addParameter("purge", "true");
		handle(req);
		
		Assert.assertNull(conditionService.getConditionByUuid(getUuid()));
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
	public void shouldReturnActiveConditions() throws Exception {
		MockHttpServletRequest request = request(RequestMethod.GET, getURI());
		request.addParameter("patientUuid", "da7f524f-27ce-4bb2-86d6-6d1d05312bd5");
		request.addParameter("includeInactive", "false");
		SimpleObject result = deserialize(handle(request));
		List<Condition> conditions = result.get("results");
		Assert.assertEquals(2, conditions.size());
	}
	@Test
	public void shouldReturnAllConditions() throws Exception {
		MockHttpServletRequest request = request(RequestMethod.GET, getURI());
		request.addParameter("patientUuid", "da7f524f-27ce-4bb2-86d6-6d1d05312bd5");
		request.addParameter("includeInactive", "true");
		SimpleObject result = deserialize(handle(request));
		List<Condition> conditions = result.get("results");
		Assert.assertEquals(3, conditions.size());
	}
}

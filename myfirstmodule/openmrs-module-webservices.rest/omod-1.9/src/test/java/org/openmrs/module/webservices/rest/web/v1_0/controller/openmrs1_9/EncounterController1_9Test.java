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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Encounter;
import org.openmrs.EncounterProvider;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.api.EncounterService;
import org.openmrs.api.VisitService;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.test.Util;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.RestTestConstants1_9;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest;
import org.openmrs.util.Format;
import org.openmrs.util.Format.FORMAT_TYPE;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.bind.annotation.RequestMethod;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.Matchers.is;

/**
 * Contains tests for the 19 ext {@link EncounterController} Overrides the failing test methods from
 * the EncounterControllerTest in the rest web services modules in order to make them pass and adds
 * tests specific to the visit property
 */
public class EncounterController1_9Test extends MainResourceControllerTest {
	
	@Before
	public void setup() throws Exception {
		executeDataSet(RestTestConstants1_9.TEST_DATASET);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest#getURI()
	 */
	@Override
	public String getURI() {
		return "encounter";
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest#getUuid()
	 */
	@Override
	public String getUuid() {
		return "6519d653-393b-4118-9c83-a3715b82d4ac";
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest#getAllCount()
	 */
	@Override
	public long getAllCount() {
		Map<Integer, List<Encounter>> allPatientEncounters = Context.getEncounterService().getAllEncounters(null);
		int totalEncounters = 0;
		for (Integer integer : allPatientEncounters.keySet()) {
			List<Encounter> encounters = allPatientEncounters.get(integer);
			if (encounters != null) {
				totalEncounters = totalEncounters + encounters.size();
			}
		}
		return totalEncounters;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest#shouldGetAll()
	 */
	@Override
	@Test(expected = Exception.class)
	public void shouldGetAll() throws Exception {
		super.shouldGetAll();
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.v1_0.controller.EncounterController1_9Test#createEncounter_shouldCreateEncounterWithObsAttributesUnordered()
	 */
	@Test
	public void createEncounter_shouldCreateEncounterWithObsAttributesUnordered() throws Exception {
		long before = getAllCount();
		
		List<SimpleObject> obs = new ArrayList<SimpleObject>();
		
		SimpleObject weight = new SimpleObject();
		weight.put("value", 70);
		weight.put("concept", "c607c80f-1ea9-4da3-bb88-6276ce8868dd");
		obs.add(weight);
		
		SimpleObject civilStatus = new SimpleObject();
		civilStatus.put("value", "92afda7c-78c9-47bd-a841-0de0817027d4");
		civilStatus.put("concept", "89ca642a-dab6-4f20-b712-e12ca4fc6d36");
		obs.add(civilStatus);
		
		SimpleObject encounter = new SimpleObject();
		encounter.put("location", "9356400c-a5a2-4532-8f2b-2361b3446eb8");
		encounter.put("encounterType", "61ae96f4-6afe-4351-b6f8-cd4fc383cce1");
		encounter.put("encounterDatetime", "2011-01-15");
		encounter.put("patient", "da7f524f-27ce-4bb2-86d6-6d1d05312bd5");
		encounter.put("provider", "ba1b19c2-3ed6-4f63-b8c0-f762dc8d7562");
		encounter.put("obs", obs);
		
		MockHttpServletResponse response = handle(newPostRequest(getURI(), encounter));
		SimpleObject newEncounter = deserialize(response);
		
		Assert.assertNotNull(newEncounter);
		Assert.assertEquals(before + 1, getAllCount());
		
		List<Map<String, String>> result = (List<Map<String, String>>) newEncounter.get("obs");
		
		Assert.assertEquals(2, result.size());
		Set<String> obsDisplayValues = new HashSet<String>();
		for (Map<String, String> o : result) {
			obsDisplayValues.add(o.get("display"));
		}
		Assert.assertTrue(obsDisplayValues.contains("CIVIL STATUS: MARRIED"));
		Assert.assertTrue(obsDisplayValues.contains("WEIGHT (KG): 70.0"));
		
	}
	
	@Test
	public void createEncounter_shouldCreateEncounterWithNestedObs() throws Exception {
		List<SimpleObject> obsObject = new ArrayList<SimpleObject>();
		List<SimpleObject> parentGroupMembersObject = new ArrayList<SimpleObject>();
		List<SimpleObject> child1GroupMembersObject = new ArrayList<SimpleObject>();
		
		SimpleObject grandchild = new SimpleObject();
		grandchild.put("value", 1);
		grandchild.put("concept", "c607c80f-1ea9-4da3-bb88-6276ce8868dd");
		child1GroupMembersObject.add(grandchild);
		
		SimpleObject child = new SimpleObject();
		child.put("concept", "c607c80f-1ea9-4da3-bb88-6276ce8868dd");
		child.put("groupMembers", child1GroupMembersObject);
		parentGroupMembersObject.add(child);
		
		SimpleObject parent = new SimpleObject();
		parent.put("concept", "c607c80f-1ea9-4da3-bb88-6276ce8868dd");
		parent.put("groupMembers", parentGroupMembersObject);
		obsObject.add(parent);
		
		SimpleObject encounter = new SimpleObject();
		encounter.put("location", "9356400c-a5a2-4532-8f2b-2361b3446eb8");
		encounter.put("encounterType", "61ae96f4-6afe-4351-b6f8-cd4fc383cce1");
		encounter.put("encounterDatetime", "2011-01-15");
		encounter.put("patient", "da7f524f-27ce-4bb2-86d6-6d1d05312bd5");
		encounter.put("obs", obsObject);
		
		MockHttpServletResponse response = handle(newPostRequest(getURI(), encounter));
		String newEncounterUuid = deserialize(response).get("uuid").toString();
		
		Encounter newEncounter = Context.getEncounterService().getEncounterByUuid(newEncounterUuid);
		Set<Obs> encounterObs = newEncounter.getAllObs();
		Assert.assertThat(encounterObs.size(), is(1));
		
		Obs parentObs = encounterObs.iterator().next();
		Assert.assertTrue(parentObs.hasGroupMembers());
		
		Set<Obs> parentGroupMembers = parentObs.getGroupMembers();
		Assert.assertThat(parentGroupMembers.size(), is(1));
		
		Obs childObs = parentGroupMembers.iterator().next();
		Assert.assertTrue(childObs.hasGroupMembers());
		
		Set<Obs> childGroupMembers = childObs.getGroupMembers();
		Assert.assertThat(childGroupMembers.size(), is(1));
		
		Obs grandchildObs = childGroupMembers.iterator().next();
		Assert.assertThat(grandchildObs.getValueNumeric(), is(1.0));
		
		System.out.println("");
	}
	
	@Test
	public void createEncounter_shouldCreateANewEncounterWithObs() throws Exception {
		long before = getAllCount();
		Util.log("before = ", before);
		
		SimpleObject post = createEncounterWithObs();
		
		MockHttpServletResponse response = handle(newPostRequest(getURI(), post));
		SimpleObject newEncounter = deserialize(response);
		
		Assert.assertNotNull(newEncounter);
		Util.log("after = ", getAllCount());
		Assert.assertEquals(before + 1, getAllCount());
		
		Util.log("created encounter with obs", newEncounter);
		@SuppressWarnings("unchecked")
		List<Map<String, String>> obs = (List<Map<String, String>>) newEncounter.get("obs");
		
		Assert.assertEquals(4, obs.size());
		Set<String> obsDisplayValues = new HashSet<String>();
		for (Map<String, String> o : obs) {
			obsDisplayValues.add(o.get("display"));
		}
		Assert.assertTrue(obsDisplayValues.contains("CIVIL STATUS: MARRIED"));
		Assert.assertTrue(obsDisplayValues.contains("FAVORITE FOOD, NON-CODED: fried chicken"));
		Assert.assertTrue(obsDisplayValues.contains("WEIGHT (KG): 70.0"));
		
		// obs.getValueAsString() uses application Locale and hence have to do this
		Calendar cal = Calendar.getInstance();
		cal.set(2011, Calendar.JUNE, 21, 0, 0, 0);
		String format = Format.format(cal.getTime(), Context.getLocale(), FORMAT_TYPE.TIMESTAMP);
		Assert.assertTrue(obsDisplayValues.contains("DATE OF FOOD ASSISTANCE: " + format));
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.v1_0.controller.EncounterController1_9Test#getEncounter_shouldGetAFullRepresentationOfAEncounterIncludingObsGroups()
	 */
	@Test
	public void getEncounter_shouldGetAFullRepresentationOfAEncounterIncludingObsGroups() throws Exception {
		executeDataSet("encounterWithObsGroup1_9.xml");
		MockHttpServletRequest req = request(RequestMethod.GET, getURI() + "/62967e68-96bb-11e0-8d6b-9b9415a91465");
		req.addParameter(RestConstants.REQUEST_PROPERTY_FOR_REPRESENTATION, RestConstants.REPRESENTATION_FULL);
		MockHttpServletResponse response = handle(req);
		SimpleObject result = deserialize(response);
		
		SimpleDateFormat ymd = new SimpleDateFormat("yyyy-MM-dd");
		
		Util.log("full", result);
		Assert.assertNotNull(result);
		Assert.assertEquals("62967e68-96bb-11e0-8d6b-9b9415a91465", result.get("uuid"));
		Assert.assertNotNull(result.get("obs"));
		Assert.assertEquals("0f97e14e-cdc2-49ac-9255-b5126f8a5147", Util.getByPath(result, "obs[0]/concept/uuid"));
		Assert.assertEquals("96408258-000b-424e-af1a-403919332938",
		    Util.getByPath(result, "obs[0]/groupMembers[0]/concept/uuid"));
		Assert.assertEquals("Some text", Util.getByPath(result, "obs[0]/groupMembers[0]/value"));
		Assert.assertEquals("11716f9c-1434-4f8d-b9fc-9aa14c4d6126",
		    Util.getByPath(result, "obs[0]/groupMembers[1]/concept/uuid"));
		Assert.assertEquals(ConversionUtil.convertToRepresentation(ymd.parse("2011-06-12"), Representation.DEFAULT),
		    Util.getByPath(result, "obs[0]/groupMembers[1]/value"));
		// make sure there's a group in the group
		Assert.assertEquals("0f97e14e-cdc2-49ac-9255-b5126f8a5147",
		    Util.getByPath(result, "obs[0]/groupMembers[2]/concept/uuid"));
		Assert.assertEquals("96408258-000b-424e-af1a-403919332938",
		    Util.getByPath(result, "obs[0]/groupMembers[2]/groupMembers[0]/concept/uuid"));
		Assert.assertEquals("Some text", Util.getByPath(result, "obs[0]/groupMembers[2]/groupMembers[0]/value"));
		Assert.assertEquals("11716f9c-1434-4f8d-b9fc-9aa14c4d6126",
		    Util.getByPath(result, "obs[0]/groupMembers[2]/groupMembers[1]/concept/uuid"));
		Assert.assertEquals(ConversionUtil.convertToRepresentation(ymd.parse("2011-06-12"), Representation.DEFAULT),
		    Util.getByPath(result, "obs[0]/groupMembers[2]/groupMembers[1]/value"));
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.v1_0.controller.EncounterController1_9Test#shouldCreateAnEncounterWithObsAndOrdersOfDifferentTypes()
	 */
	@Test
	public void shouldCreateAnEncounterWithObsAndOrdersOfDifferentTypes() throws Exception {
		String foodAssistanceUuid = "0dde1358-7fcf-4341-a330-f119241a46e8";
		String lunchOrderUuid = "e23733ab-787e-4096-8ba2-577a902d2c2b";
		String lunchInstructions = "Give them yummy food please";
		String triomuneConceptUuid = "d144d24f-6913-4b63-9660-a9108c2bebef";
		String triomuneDrugUuid = "3cfcf118-931c-46f7-8ff6-7b876f0d4202";
		
		long before = getAllCount();
		SimpleObject post = createEncounterWithObs();
		List<SimpleObject> orders = new ArrayList<SimpleObject>();
		orders.add(SimpleObject.parseJson("{ \"type\": \"order\", \"concept\": \"" + foodAssistanceUuid
		        + "\", \"orderType\": \"" + lunchOrderUuid + "\", \"instructions\": \"" + lunchInstructions + "\" }"));
		orders.add(SimpleObject.parseJson("{ \"type\": \"drugorder\", \"concept\": \"" + triomuneConceptUuid
		        + "\", \"drug\": \"" + triomuneDrugUuid + "\", \"dose\": \"1\", \"units\": \"tablet\" }"));
		post.add("orders", orders);
		
		SimpleObject newEncounter = deserialize(handle(newPostRequest(getURI(), post)));
		
		Assert.assertNotNull(newEncounter);
		Assert.assertEquals(before + 1, getAllCount());
		Util.log("created encounter with obs and orders", newEncounter);
		
		@SuppressWarnings("unchecked")
		List<Map<String, String>> newOrders = (List<Map<String, String>>) newEncounter.get("orders");
		
		Assert.assertEquals(2, newOrders.size());
		List<String> lookFor = new ArrayList<String>(Arrays.asList("FOOD ASSISTANCE", "Triomune-30: 1.0 tablet"));
		for (Map<String, String> o : newOrders) {
			lookFor.remove(o.get("display"));
		}
		Assert.assertEquals("Did not find: " + lookFor, 0, lookFor.size());
	}
	
	@Test
	public void createEncounter_shouldCreateANewEncounterWithAVisitProperty() throws Exception {
		long before = getAllCount();
		final String visitUuid = "1e5d5d48-6b78-11e0-93c3-18a905e044dc";
		String json = "{\"visit\":\""
		        + visitUuid
		        + "\",\"location\":\"9356400c-a5a2-4532-8f2b-2361b3446eb8\", \"encounterType\": \"61ae96f4-6afe-4351-b6f8-cd4fc383cce1\", \"encounterDatetime\": \"2011-01-15\", \"patient\": \"da7f524f-27ce-4bb2-86d6-6d1d05312bd5\", \"provider\":\"ba1b19c2-3ed6-4f63-b8c0-f762dc8d7562\"}";
		
		SimpleObject post = new ObjectMapper().readValue(json, SimpleObject.class);
		Object newEncounterObject = deserialize(handle(newPostRequest(getURI(), post)));
		
		Assert.assertNotNull(newEncounterObject);
		Encounter newEncounter = Context.getEncounterService().getEncounterByUuid(
		    ((SimpleObject) newEncounterObject).get("uuid").toString());
		Assert.assertEquals(before + 1, getAllCount());
		//the encounter should have been assigned to the visit
		Assert.assertNotNull(newEncounter);
		Assert.assertNotNull(newEncounter.getVisit());
		Assert.assertEquals(visitUuid, newEncounter.getVisit().getUuid());
	}
	
	@Test
	public void createEncounter_shouldEditVisitPropertyForAnExisitingEncounter() throws Exception {
		EncounterService es = Context.getEncounterService();
		VisitService vs = Context.getVisitService();
		Encounter encounter = es.getEncounterByUuid(getUuid());
		Visit newVisit = new Visit(encounter.getPatient(), vs.getVisitTypeByUuid(RestTestConstants1_9.VISIT_TYPE_UUID),
		        new SimpleDateFormat("yyyy-MM-dd").parse("2008-08-01"));
		vs.saveVisit(newVisit);
		
		String json = "{\"visit\":\"" + newVisit.getUuid() + "\"}";
		SimpleObject post = new ObjectMapper().readValue(json, SimpleObject.class);
		
		Object newEncounterObject = handle(newPostRequest(getURI() + "/" + getUuid(), post));
		
		Assert.assertNotNull(newEncounterObject);
		Encounter update = es.getEncounterByUuid(getUuid());
		//the encounter should have been res assigned to the new visit
		Assert.assertEquals(newVisit, update.getVisit());
	}
	
	@Test
	public void createEncounter_shouldCreateEncounterWithProviders() throws Exception {
		long encountersBefore = getAllCount();
		
		//Post
		SimpleObject newEncounter = deserialize(handle(newPostRequest(getURI(), createEncounterWithProviders())));
		
		Assert.assertNotNull(newEncounter);
		Assert.assertEquals(encountersBefore + 1, getAllCount());
		
		Util.log("Created a new encounter with a list of providers with different roles", newEncounter);
		
		List<?> encounterProviderList = newEncounter.get("encounterProviders");
		Assert.assertEquals(2, encounterProviderList.size());
	}
	
	/**
	 * Copied from The EncounterControllerTest class from the rest web services module
	 * 
	 * @return
	 * @throws Exception
	 */
	private SimpleObject createEncounterWithObs() throws Exception {
		
		List<SimpleObject> obs = new ArrayList<SimpleObject>();
		// weight in kg = 70
		obs.add(SimpleObject.parseJson("{ \"concept\": \"c607c80f-1ea9-4da3-bb88-6276ce8868dd\",\"value\": 70 }"));
		// civil status = married
		obs.add(SimpleObject
		        .parseJson("{ \"concept\": \"89ca642a-dab6-4f20-b712-e12ca4fc6d36\", \"value\": \"92afda7c-78c9-47bd-a841-0de0817027d4\" }"));
		// favorite food, non-coded = fried chicken
		obs.add(SimpleObject
		        .parseJson("{ \"concept\": \"96408258-000b-424e-af1a-403919332938\", \"value\": \"fried chicken\" }"));
		// date of food assistance = 2011-06-21
		obs.add(SimpleObject
		        .parseJson("{ \"concept\": \"11716f9c-1434-4f8d-b9fc-9aa14c4d6126\", \"value\": \"2011-06-21 00:00\" }"));
		
		return new SimpleObject().add("location", "9356400c-a5a2-4532-8f2b-2361b3446eb8")
		        .add("encounterType", "61ae96f4-6afe-4351-b6f8-cd4fc383cce1").add("encounterDatetime", "2011-01-15")
		        .add("patient", "da7f524f-27ce-4bb2-86d6-6d1d05312bd5")
		        .add("provider", "ba1b19c2-3ed6-4f63-b8c0-f762dc8d7562").add("obs", obs);
	}
	
	private SimpleObject createEncounterWithProviders() throws Exception {
		Set<SimpleObject> providers = new HashSet<SimpleObject>();
		
		providers.add(SimpleObject.parseJson("{\"provider\":\"c2299800-dgha-11e0-9572-0800200c9a66\","
		        + "\"encounterRole\":\"a0b03050-c99b-11e0-9572-0800200c9a66\"}"));
		providers.add(SimpleObject.parseJson("{\"provider\":\"c2299800-dgha-11e0-9572-0800200c9a66\","
		        + "\"encounterRole\":\"a0b03050-c99b-11e0-9572-0800201c9a71\"}"));
		
		return new SimpleObject().add("location", "9356400c-a5a2-4532-8f2b-2361b3446eb8")
		        .add("encounterType", "61ae96f4-6afe-4351-b6f8-cd4fc383cce1").add("encounterDatetime", "2015-06-17")
		        .add("patient", "da7f524f-27ce-4bb2-86d6-6d1d05312bd5").add("encounterProviders", providers);
	}
	
	@Test
	public void getEncounter_shouldGetOnlyNonVoidedEncounterProviders() throws Exception {
		Encounter encounter = Context.getEncounterService().getEncounterByUuid(RestTestConstants1_9.SECOND_ENCOUNTER_UUID);
		Set<EncounterProvider> encounterProviders = encounter.getEncounterProviders();
		String voidedEncounterProviderUuid = null;
		if (encounterProviders != null) {
			for (EncounterProvider encounterProvider : encounterProviders) {
				if (encounterProvider.isVoided()) {
					voidedEncounterProviderUuid = encounterProvider.getUuid();
					break;
				}
			}
		}
		Assert.assertNotNull(voidedEncounterProviderUuid);
		// the encounter has a voided encounter provider
		Assert.assertEquals(RestTestConstants1_9.VOIDED_ENCOUNTER_PROVIDER, voidedEncounterProviderUuid);
		
		// retrieve the same encounter via the encounter web service
		MockHttpServletRequest req = request(RequestMethod.GET, getURI() + "/" + RestTestConstants1_9.SECOND_ENCOUNTER_UUID);
		MockHttpServletResponse response = handle(req);
		SimpleObject result = deserialize(response);
		
		voidedEncounterProviderUuid = null;
		List<Map<String, String>> encounterProviderList = result.get("encounterProviders");
		// we now check to make sure the encounter REST web service does not return voided encounter provider
		if (encounterProviderList != null) {
			for (Map<String, String> wsEncounterProvider : encounterProviderList) {
				if (StringUtils.equals(RestTestConstants1_9.VOIDED_ENCOUNTER_PROVIDER, wsEncounterProvider.get("uuid"))) {
					voidedEncounterProviderUuid = wsEncounterProvider.get("uuid");
					// we found the voided encounter provider
					break;
				}
				
			}
		}
		// the voided encounter provider is not returned by the encounter web service
		Assert.assertNull(voidedEncounterProviderUuid);
	}
	
	@Test
	public void updateEncounter_shouldUpdateEncounterProviders() throws Exception {
		EncounterService es = Context.getEncounterService();
		Encounter encounter = es.getEncounterByUuid(RestTestConstants1_9.ENCOUNTER_UUID);
		
		int initialCount = encounter.getEncounterProviders().size();
		
		//Get one EncounterProvider
		EncounterProvider existing = encounter.getEncounterProviders().iterator().next();
		
		Assert.assertNotNull(existing);
		
		String newRoleUuid = "a0b03050-c99b-11e0-9572-0800201c9a71";
		
		Assert.assertNotNull(existing);
		Assert.assertNotEquals(existing.getEncounterRole(), es.getEncounterRoleByUuid(newRoleUuid));
		
		SimpleObject newOne = SimpleObject.parseJson("{" + "\"uuid\": \"" + existing.getUuid() + "\"," + "\"provider\": \""
		        + existing.getProvider().getUuid() + "\"," + "\"encounterRole\": \"" + newRoleUuid + "\","
		        + "\"encounter\": \"" + existing.getEncounter().getUuid() + "\"" + "}");
		
		Set<SimpleObject> encounterProvidersToPost = new HashSet<SimpleObject>();
		encounterProvidersToPost.add(newOne);
		
		SimpleObject encounterToModify = new SimpleObject().add("encounterProviders", encounterProvidersToPost).add("uuid",
		    encounter.getUuid());
		
		//Post the existing encounter
		deserialize(handle(newPostRequest(getURI() + "/" + encounter.getUuid(), encounterToModify)));
		
		Encounter updatedEncounter = es.getEncounterByUuid(RestTestConstants1_9.ENCOUNTER_UUID);
		
		EncounterProvider updateEncounterProvider = getEncounterProviderWthUuid(updatedEncounter.getEncounterProviders(),
		    existing.getUuid());
		
		Assert.assertEquals(initialCount, updatedEncounter.getEncounterProviders().size());
		Assert.assertNotNull(updatedEncounter);
		Assert.assertEquals(es.getEncounterRoleByUuid(newRoleUuid), updateEncounterProvider.getEncounterRole());
	}
	
	@Test
	public void shouldUnVoidAnEncounter() throws Exception {
		EncounterService es = Context.getEncounterService();
		Encounter encounter = es.getEncounterByUuid(RestTestConstants1_9.ENCOUNTER_UUID);
		es.voidEncounter(encounter, "some random reason");
		encounter = es.getEncounterByUuid(RestTestConstants1_9.ENCOUNTER_UUID);
		Assert.assertTrue(encounter.isVoided());
		
		String json = "{\"deleted\": \"false\"}";
		SimpleObject response = deserialize(handle(newPostRequest(getURI() + "/" + RestTestConstants1_9.ENCOUNTER_UUID, json)));
		
		encounter = es.getEncounterByUuid(RestTestConstants1_9.ENCOUNTER_UUID);
		Assert.assertFalse(encounter.isVoided());
		Assert.assertEquals("false", PropertyUtils.getProperty(response, "voided").toString());
		
	}
	
	@Test
	public void shouldFetchAllPatientEncounterIncludingVoidedPatientEncountersIfIncludeAllParamIsSetToTrue()
	        throws Exception {
		Patient patient = Context.getPatientService().getPatientByUuid(RestTestConstants1_9.PATIENT_WITH_ENCOUNTER_UUID);
		
		Encounter enc = Context.getEncounterService().getEncounterByUuid(RestTestConstants1_9.ENCOUNTER_UUID);
		Context.getEncounterService().voidEncounter(enc, "some reason");
		Context.getEncounterService().saveEncounter(enc);
		
		//list of non voided encounters
		List<Encounter> encs = Context.getEncounterService().getEncountersByPatient(patient);
		Assert.assertEquals(1, encs.size());
		
		MockHttpServletRequest req = request(RequestMethod.GET, getURI());
		req.addParameter("patient", RestTestConstants1_9.PATIENT_WITH_ENCOUNTER_UUID);
		req.addParameter("includeAll", "true");
		
		SimpleObject result = deserialize(handle(req));
		List<Object> encounters = result.get("results");
		Assert.assertEquals(2, encounters.size());
	}
	
	@Test
	public void shouldFetchOnlyNonVoidedPatientEncountersIfIncludeAllParamIsSetToFalse() throws Exception {
		Patient patient = Context.getPatientService().getPatientByUuid(RestTestConstants1_9.PATIENT_WITH_ENCOUNTER_UUID);
		
		Encounter enc = Context.getEncounterService().getEncounterByUuid(RestTestConstants1_9.ENCOUNTER_UUID);
		Context.getEncounterService().voidEncounter(enc, "bcbcb");
		Context.getEncounterService().saveEncounter(enc);
		
		//list of non voided encounters
		List<Encounter> encs = Context.getEncounterService().getEncountersByPatient(patient);
		Assert.assertEquals(1, encs.size());
		
		MockHttpServletRequest req = request(RequestMethod.GET, getURI());
		req.addParameter("patient", RestTestConstants1_9.PATIENT_WITH_ENCOUNTER_UUID);
		req.addParameter("includeAll", "false");
		
		SimpleObject result = deserialize(handle(req));
		List<Object> encounters = result.get("results");
		Assert.assertEquals(1, encounters.size());
	}
	
	private EncounterProvider getEncounterProviderWthUuid(Set<EncounterProvider> eps, String uuid) {
		assert eps != null;
		assert uuid != null;
		
		for (EncounterProvider ep : eps) {
			if (uuid.equals(ep.getUuid())) {
				return ep;
			}
		}
		return null;
	}
}

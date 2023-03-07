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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.junit.Assert;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.ConceptComplex;
import org.openmrs.ConceptName;
import org.openmrs.Drug;
import org.openmrs.GlobalProperty;
import org.openmrs.Obs;
import org.openmrs.Person;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.ConceptService;
import org.openmrs.api.ObsService;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.test.Util;
import org.openmrs.module.webservices.rest.web.RestTestConstants1_8;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest;
import org.openmrs.util.OpenmrsConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockMultipartHttpServletRequest;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.xml.bind.DatatypeConverter;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class ObsController1_9Test extends MainResourceControllerTest {

	@Autowired
	ConceptService conceptService;

	@Autowired
	AdministrationService adminService;

	@Test
	public void getObs_shouldGetObsConceptByConceptMappings() throws Exception {
		String json = "{ \"value\":\"" + 10.0 + "\", \"person\":\"" + RestTestConstants1_8.PERSON_UUID
		        + "\", \"concept\":\"SNOMED CT:2332523\", \"obsDatetime\":\"2013-12-09T00:00:00.000+0100\"}";

		Object newObs = deserialize(handle(newPostRequest(getURI(), json)));
		Assert.assertNotNull(PropertyUtils.getProperty(newObs, "concept"));
	}

	@Test
	public void getObs_shouldCreateAnObsWithACodedValueSetAsAMap() throws Exception {
		long originalCount = getAllCount();
		SimpleObject obs = new SimpleObject();
		obs.add("person", "da7f524f-27ce-4bb2-86d6-6d1d05312bd5");
		obs.add("concept", "89ca642a-dab6-4f20-b712-e12ca4fc6d36");
		obs.add("obsDatetime", "2018-11-13T00:00:00.000-0500");
		final String codeValueUuid = "32d3611a-6699-4d52-823f-b4b788bac3e3";
		SimpleObject valueCoded = deserialize(handle(newGetRequest("concept/" + codeValueUuid)));
		obs.add("value", valueCoded);
		String json = new ObjectMapper().writeValueAsString(obs);
		SimpleObject ret = deserialize(handle(newPostRequest(getURI(), json)));
		assertEquals(codeValueUuid, Util.getByPath(ret, "value/uuid"));
		assertEquals(++originalCount, getAllCount());
	}

	@Test
	public void shouldUnVoidAnObs() throws Exception {
		ObsService obsService = Context.getObsService();
		Obs obs = obsService.getObsByUuid(getUuid());
		obsService.voidObs(obs, "some random reason");
		obs = obsService.getObsByUuid(getUuid());
		Assert.assertTrue(obs.isVoided());

		String json = "{\"deleted\": \"false\"}";
		SimpleObject response = deserialize(handle(newPostRequest(getURI() + "/" + getUuid(), json)));

		obs = obsService.getObsByUuid(getUuid());
		Assert.assertFalse(obs.isVoided());
		Assert.assertEquals("false", PropertyUtils.getProperty(response, "voided").toString());

	}

	/**
	 * @see org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest#getURI()
	 */
	@Override
	public String getURI() {
		return "obs";
	}

	@Override
	public long getAllCount() {
		return Context.getObsService().getObservationCount(null, true);
	}

	/**
	 * @see org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest#getUuid()
	 */
	@Override
	public String getUuid() {
		return RestTestConstants1_8.OBS_UUID;
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
	public void searchByEncounter_shouldGetVoidedObsIfIncludeAllIsTrue() throws Exception {

		executeDataSet("encounterWithObsGroup1_9.xml");

		final String ENCOUNTER_UUID = "62967e68-96bb-11e0-8d6b-9b9415a91465";

		MockHttpServletRequest allNonVoidedObsRequest = newGetRequest(getURI());
		allNonVoidedObsRequest.addParameter("encounter", ENCOUNTER_UUID);
		MockHttpServletResponse allNonVoidedObsResponse = handle(allNonVoidedObsRequest);
		List<Object> allNonVoidedObsList = deserialize(allNonVoidedObsResponse).get("results");

		assertEquals(6, allNonVoidedObsList.size());

		MockHttpServletRequest deleteRequest = newDeleteRequest(getURI() + "/" + "11de743c-96cd-11e0-8d6b-9b9415a91465");
		deleteRequest.addParameter("reason", "test voided obs");
		handle(deleteRequest);

		MockHttpServletRequest allObsIncludingVoidedRequest = newGetRequest(getURI());
		allObsIncludingVoidedRequest.addParameter("encounter", ENCOUNTER_UUID);
		allObsIncludingVoidedRequest.addParameter("includeAll", "true");
		MockHttpServletResponse allObsIncludingVoidedResponse = handle(allObsIncludingVoidedRequest);
		List<Object> allObsIncludingVoidedList = deserialize(allObsIncludingVoidedResponse).get("results");

		assertEquals(6, allObsIncludingVoidedList.size());

		MockHttpServletRequest allNonVoidedObsAfterDeleteRequest = newGetRequest(getURI());
		allNonVoidedObsAfterDeleteRequest.addParameter("encounter", ENCOUNTER_UUID);
		MockHttpServletResponse allNonVoidedObsAfterDeleteResponse = handle(allNonVoidedObsAfterDeleteRequest);
		List<Object> allNonVoidedObsAfterDeleteList = deserialize(allNonVoidedObsAfterDeleteResponse).get("results");

		assertEquals(5, allNonVoidedObsAfterDeleteList.size());
	}

	@Test
	public void shouldSubmitProperValueCodedWhenBooleanConceptUuidIsPassedAsValue() throws Exception {
		final String yesConceptUuid = "b055abd8-a420-4a11-8b98-02ee170a7b54";
		final String yesConceptId = "7";
		final String noConceptUuid = "934d8ef1-ea43-4f98-906e-dd03d5faaeb4";
		final String noConceptId = "8";

		AdministrationService as = Context.getAdministrationService();

		as.saveGlobalProperty(new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_TRUE_CONCEPT, yesConceptId));
		as.saveGlobalProperty(new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_FALSE_CONCEPT, noConceptId));

		long before = getAllCount();

		String yesPayload = "{\"concept\":\"89ca642a-dab6-4f20-b712-e12ca4fc6d36\"," + "\"value\":\"" + yesConceptUuid
		        + "\",\"person\":\"5946f880-b197-400b-9caa-a3c661d23041\","
		        + "\"obsDatetime\":\"2015-09-07T00:00:00.000+0530\"}";

		String noPayload = "{\"concept\":\"89ca642a-dab6-4f20-b712-e12ca4fc6d36\"," + "\"value\":\"" + noConceptUuid
		        + "\",\"person\":\"5946f880-b197-400b-9caa-a3c661d23041\","
		        + "\"obsDatetime\":\"2015-09-07T00:00:00.000+0530\"}";

		Object yesCreated = deserialize(handle(newPostRequest(getURI(), yesPayload)));
		Object yesValue = PropertyUtils.getProperty(yesCreated, "value");

		Object noCreated = deserialize(handle(newPostRequest(getURI(), noPayload)));
		Object noValue = PropertyUtils.getProperty(noCreated, "value");

		Assert.assertEquals(before + 2, getAllCount());
		Assert.assertEquals(yesConceptUuid, PropertyUtils.getProperty(yesValue, "uuid"));
		Assert.assertEquals(noConceptUuid, PropertyUtils.getProperty(noValue, "uuid"));
	}

	@Test
	public void shouldPostValueInJsonAndFetchComplexObs() throws Exception {
		ConceptComplex conceptComplex = newConceptComplex();

		InputStream in = getClass().getClassLoader().getResourceAsStream("customTestDataset.xml");

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		IOUtils.copy(in, out);

		String value = DatatypeConverter.printBase64Binary(out.toByteArray());

		String json = "{\"concept\":\"" + conceptComplex.getUuid() + "\"," + "\"value\":\"" + value
		        + "\",\"person\":\"5946f880-b197-400b-9caa-a3c661d23041\","
		        + "\"obsDatetime\":\"2015-09-07T00:00:00.000+0530\"}";

		SimpleObject response = deserialize(handle(newPostRequest(getURI(), json)));

		MockHttpServletResponse rawResponse = handle(newGetRequest(getURI() + "/" + response.get("uuid") + "/value"));

		assertThat(out.toByteArray(), is(equalTo(rawResponse.getContentAsByteArray())));
	}

	@Test
	public void shouldUploadFileAndFetchComplexObs() throws Exception {
		ConceptComplex conceptComplex = newConceptComplex();

		InputStream in = getClass().getClassLoader().getResourceAsStream("customTestDataset.xml");

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		IOUtils.copy(in, out);

		String json = "{\"concept\":\"" + conceptComplex.getUuid()
		        + "\", \"person\":\"5946f880-b197-400b-9caa-a3c661d23041\","
		        + "\"obsDatetime\":\"2015-09-07T00:00:00.000+0530\"}";

		MockMultipartHttpServletRequest request = newUploadRequest(getURI());
		request.addFile(new MockMultipartFile("file", "customTestDataset.xml", null, out.toByteArray()));
		request.addParameter("json", json);

		SimpleObject response = deserialize(handle(request));

		MockHttpServletResponse rawResponse = handle(newGetRequest(getURI() + "/" + response.get("uuid") + "/value"));

		assertThat(out.toByteArray(), is(equalTo(rawResponse.getContentAsByteArray())));
	}

	@Test
	public void getObs_shouldCreateAnObsWhenTheQuestionConceptIsSetAsAMapContainingTheUuid() throws Exception {
		long originalCount = getAllCount();
		SimpleObject obs = new SimpleObject();
		obs.add("person", "da7f524f-27ce-4bb2-86d6-6d1d05312bd5");
		SimpleObject concept = new SimpleObject();
		concept.add("uuid", "c607c80f-1ea9-4da3-bb88-6276ce8868dd");
		concept.add("name", new SimpleObject());
		obs.add("concept", concept);
		obs.add("obsDatetime", "2018-11-13T00:00:00.000-0500");
		obs.add("value", 180.0);
		String json = new ObjectMapper().writeValueAsString(obs);
		handle(newPostRequest(getURI(), json));
		assertEquals(++originalCount, getAllCount());
	}

    @Test
    public void getObs_customRepShouldNotFailIfDrugObsPresent() throws Exception {

        // create a obs that has a value drug
        Drug drug = Context.getConceptService().getDrugByUuid("3cfcf118-931c-46f7-8ff6-7b876f0d4202"); // arbitrary, from standard test dataset
        Concept concept = Context.getConceptService().getConceptByUuid("89ca642a-dab6-4f20-b712-e12ca4fc6d36"); // arbitrary, from standard test dataset
        Person person = Context.getPersonService().getPerson(2); // arbitrary, from standard test dataset
        Obs obs = new Obs();
        obs.setConcept(concept);
        obs.setValueDrug(drug);
        obs.setValueCoded(drug.getConcept());
        obs.setObsDatetime(new Date());
        obs.setPerson(person);
        Context.getObsService().saveObs(obs, "test");

        // retrieve the same obs, specifying a custom rep that expects an obs value to be a concept, not a drug (a concept has a "names" property, but a drug does not)
        MockHttpServletRequest req = request(RequestMethod.GET, getURI() + "/" + obs.getUuid());
        String customRep = "custom:(value:(names))";
        req.addParameter("v", customRep);

        MockHttpServletResponse response = handle(req);
        SimpleObject result = deserialize(response);

        // should not fail, but should return "null" for the value since it's unable to parse it
        // see: https://issues.openmrs.org/browse/RESTWS-816
        assertTrue(result.containsKey("value"));
        assertNull(result.get("value"));
    }

    @Test
    public void shouldGetObsByUuid() throws Exception {
    	MockHttpServletRequest req = request(RequestMethod.GET, getURI() + "/" + getUuid());
    	SimpleObject result = deserialize(handle(req));

    	ObsService obsService = Context.getObsService();
    	Obs obs = obsService.getObsByUuid(getUuid());
    	assertEquals(obs.getUuid(), PropertyUtils.getProperty(result, "uuid"));
    }

    private ConceptComplex newConceptComplex() {
		setupBinaryDataHandler();

		ConceptComplex conceptComplex = new ConceptComplex();
		conceptComplex.setHandler("BinaryDataHandler");
		conceptComplex.addName(new ConceptName("Xml Test Data", Locale.ENGLISH));
		conceptComplex.setDatatype(conceptService.getConceptDatatypeByName("Complex"));
		conceptComplex.setConceptClass(conceptService.getConceptClassByName("Misc"));
		conceptService.saveConcept(conceptComplex);
		return conceptComplex;
	}

	private void setupBinaryDataHandler() {
		adminService.saveGlobalProperty(new GlobalProperty("obs.complex_obs_dir", "complexObsDir"));
	}
}

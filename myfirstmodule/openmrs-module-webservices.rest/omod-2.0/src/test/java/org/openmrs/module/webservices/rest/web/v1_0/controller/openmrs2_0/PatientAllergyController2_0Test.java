/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.controller.openmrs2_0;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Allergen;
import org.openmrs.Allergies;
import org.openmrs.Allergy;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.test.Util;
import org.openmrs.module.webservices.rest.web.v1_0.RestTestConstants2_0;
import org.openmrs.module.webservices.rest.web.response.NoContentFoundException;
import org.openmrs.module.webservices.rest.web.response.ObjectNotFoundException;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest;

public class PatientAllergyController2_0Test extends MainResourceControllerTest {
	
	@Before
	public void init() throws Exception {
		executeDataSet(RestTestConstants2_0.ALLERGY_TEST_DATA_XML);
		executeDataSet(RestTestConstants2_0.OTHER_NON_CODED_CONCEPT_TEST_DATA_XML);
		Allergen.setOtherNonCodedConceptUuid(RestTestConstants2_0.ALLERGY_OTHER_NON_CODED_UUID);
	}
	
	@Override
	public String getURI() {
		return "patient/" + RestTestConstants2_0.PATIENT_UUID + "/allergy";
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest#getUuid()
	 */
	@Override
	public String getUuid() {
		return RestTestConstants2_0.ALLERGY_UUID; // allergy does not support uuid
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest#getAllCount()
	 */
	@Override
	public long getAllCount() {
		return 4;
	}
	
	/**
	 * Delete Allergy
	 */
	@Test
	public void shouldDeleteUniqueAllergy() throws Exception {
		Allergy allergy = Context.getPatientService().getAllergyByUuid(getUuid());
		Assert.assertFalse(allergy.isVoided());
		
		// attempt to delete allergy
		handle(newDeleteRequest(getURI() + "/" + getUuid(), new Parameter("reason", "unit test")));
		
		allergy = Context.getPatientService().getAllergyByUuid(getUuid());
		Assert.assertTrue(allergy.isVoided());
		Assert.assertEquals("unit test", allergy.getVoidReason());
	}
	
	/**
	 * Delete Allergy incorrectly throw ObjectNotFoundException because allergy does not exist
	 */
	@Test(expected = ObjectNotFoundException.class)
	public void shouldThrowExceptionWhenDeletingNonExistentAllergy() throws Exception {
		// attempt to delete allergy
		handle(newDeleteRequest(getURI() + "/" + "NonExistentAllergyUUID", new Parameter("reason", "unit test")));
	}
	
	/**
	 * Delete all Allergies
	 */
	@Test
	public void shouldDeleteAllAllergies() throws Exception {
		Allergy allergy = Context.getPatientService().getAllergyByUuid(getUuid());
		Allergies allergies = Context.getPatientService().getAllergies(allergy.getPatient());
		Assert.assertEquals(Allergies.SEE_LIST, allergies.getAllergyStatus());
		Assert.assertFalse(allergy.isVoided());
		
		// attempt to delete all allergies
		handle(newDeleteRequest(getURI(), new Parameter("reason", "unit test")));
		
		allergy = Context.getPatientService().getAllergyByUuid(getUuid());
		allergies = Context.getPatientService().getAllergies(allergy.getPatient());
		Assert.assertTrue(allergy.isVoided());
		Assert.assertEquals("unit test", allergy.getVoidReason());
		Assert.assertEquals(Allergies.UNKNOWN, allergies.getAllergyStatus());
	}
	
	/*
	 *  Get Allergy 
	 */
	@Test
	public void shouldGetAllergyByUuid() throws Exception {
		Allergy allergy = Context.getPatientService().getAllergyByUuid(getUuid());
		Patient patient = allergy.getPatient();
		
		// attempt to get allergy by uuid
		SimpleObject savedAllergy = deserialize(handle(newGetRequest(getURI() + "/" + getUuid())));
		
		Assert.assertEquals(allergy.getComment(), Util.getByPath(savedAllergy, "comment"));
		Assert.assertEquals(allergy.getSeverity().getUuid(), Util.getByPath(savedAllergy, "severity/uuid"));
		
		Assert.assertEquals(allergy.getAllergen().getCodedAllergen().getUuid(),
		    Util.getByPath(savedAllergy, "allergen/codedAllergen/uuid"));
		Assert.assertEquals(allergy.getAllergen().getAllergenType().toString(),
		    Util.getByPath(savedAllergy, "allergen/allergenType"));
	}
	
	/**
	 * Save Allergy with Coded Allergen
	 */
	@Test
	public void shouldSaveAllergyWithCodedAllergen() throws Exception {
		Allergy allergy = Context.getPatientService().getAllergyByUuid(getUuid());
		Patient patient = allergy.getPatient();
		Allergies allergies = Context.getPatientService().getAllergies(patient);
		Assert.assertEquals(4, allergies.size());
		
		// save allergy with coded allergen
		String json = "{" + " \"comment\" : \"allergy comment\","
		        + " \"severity\" : { \"uuid\" : \"35d3346a-6769-4d52-823f-b4b234bac3e3\"}," + " \"allergen\" : "
		        + " { \"allergenType\" : \"DRUG\", "
		        + " \"codedAllergen\" : { \"uuid\" : \"35d3346a-6769-4d52-823f-b4b234bac3e3\"} " + " } " + "}";
		
		// save allergy 
		SimpleObject savedAllergy = deserialize(handle(newPostRequest(getURI(), json)));
		
		Assert.assertEquals("allergy comment", Util.getByPath(savedAllergy, "comment"));
		Assert.assertEquals("35d3346a-6769-4d52-823f-b4b234bac3e3", Util.getByPath(savedAllergy, "severity/uuid"));
		
		Assert.assertEquals("35d3346a-6769-4d52-823f-b4b234bac3e3",
		    Util.getByPath(savedAllergy, "allergen/codedAllergen/uuid"));
		Assert.assertEquals("DRUG", Util.getByPath(savedAllergy, "allergen/allergenType"));
		
		// assert that a new allergy has been added
		allergies = Context.getPatientService().getAllergies(patient);
		Assert.assertEquals(5, allergies.size());
	}
	
	/**
	 * Save Allergy with Non Coded Allergen
	 */
	@Test
	public void shouldSaveAllergyWithNonCodedAllergen() throws Exception {
		Allergy allergy = Context.getPatientService().getAllergyByUuid(getUuid());
		Patient patient = allergy.getPatient();
		Allergies allergies = Context.getPatientService().getAllergies(patient);
		Assert.assertEquals(4, allergies.size());
		
		// save allergy with non coded allergen
		String json = "{" + " \"comment\" : \"allergy comment\","
		        + " \"severity\" : { \"uuid\" : \"35d3346a-6769-4d52-823f-b4b234bac3e3\"}," + " \"allergen\" : "
		        + " { \"allergenType\" : \"DRUG\", "
		        + " \"codedAllergen\" : { \"uuid\" : \"5622AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"}, "
		        + " \"nonCodedAllergen\" : \"test non coded allergen\"} " + "}";
		
		// save allergy 
		SimpleObject savedAllergy = deserialize(handle(newPostRequest(getURI(), json)));
		
		Assert.assertEquals("allergy comment", Util.getByPath(savedAllergy, "comment"));
		Assert.assertEquals("35d3346a-6769-4d52-823f-b4b234bac3e3", Util.getByPath(savedAllergy, "severity/uuid"));
		
		Assert.assertEquals("5622AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
		    Util.getByPath(savedAllergy, "allergen/codedAllergen/uuid"));
		Assert.assertEquals("test non coded allergen", Util.getByPath(savedAllergy, "allergen/nonCodedAllergen"));
		Assert.assertEquals("DRUG", Util.getByPath(savedAllergy, "allergen/allergenType"));
		
		// assert that a new allergy has been added
		allergies = Context.getPatientService().getAllergies(patient);
		Assert.assertEquals(5, allergies.size());
	}
	
	/**
	 * Save Allergy with Allergy Reactions
	 */
	@Test
	public void shouldSaveAllergyWithReactions() throws Exception {
		Allergy allergy = Context.getPatientService().getAllergyByUuid(getUuid());
		Patient patient = allergy.getPatient();
		Allergies allergies = Context.getPatientService().getAllergies(patient);
		Assert.assertEquals(4, allergies.size());
		
		// update allergy with reactions
		String json = "{ \"reactions\" : " + "[" + "{" + " \"allergy\" : { \"uuid\" : \"" + allergy.getUuid() + "\"},"
		        + " \"reaction\" : { \"uuid\" : \"35d3346a-6769-4d52-823f-b4b234bac3e3\" }" + "}," + "{"
		        + " \"allergy\" : { \"uuid\" : \"" + allergy.getUuid() + "\"},"
		        + " \"reaction\" : { \"uuid\" : \"5622AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\" },"
		        + " \"reactionNonCoded\" : \"test non coded reaction\"" + "}" + "]" + "}";
		SimpleObject savedAllergy = deserialize(handle(newPostRequest(getURI() + "/" + allergy.getUuid(), json)));
		
		Assert.assertEquals("35d3346a-6769-4d52-823f-b4b234bac3e3",
		    Util.getByPath(savedAllergy, "reactions[0]/reaction/uuid"));
		
		Assert.assertEquals("5622AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
		    Util.getByPath(savedAllergy, "reactions[1]/reaction/uuid"));
		Assert.assertEquals("test non coded reaction", Util.getByPath(savedAllergy, "reactions[1]/reactionNonCoded"));
		
		// assert that a allergy has been updated
		allergies = Context.getPatientService().getAllergies(patient);
		Assert.assertEquals(4, allergies.size());
	}
	
	/**
	 * Set No Known Allergies incorrectly using empty post body achieved with PUT with empty post
	 * body when list of allergies is empty throw ResourceDoesNotSupportOperationException because
	 * Patient does not exist
	 */
	@Test(expected = ObjectNotFoundException.class)
	public void shouldThrowExceptionWhenSettingNoKnownAllergiesForNonExistingPatient() throws Exception {
		String nonExistentPatientURI = "patient/nonExistingPatient/allergy";
		String json = "{}";
		
		// attempt to set no known allergies with PUT
		handle(newPutRequest(nonExistentPatientURI, json));
	}
	
	/**
	 * Set No Known Allergies incorrectly using empty post body achieved with PUT with empty post
	 * body when list of allergies is empty throw ResourceDoesNotSupportOperationException because
	 * List of Allergies is not empty
	 */
	@Test(expected = ResourceDoesNotSupportOperationException.class)
	public void shouldThrowExceptionWhenSettingNoKnownAllergiesIfAllergiesNotEmpty() throws Exception {
		Allergy allergy = Context.getPatientService().getAllergyByUuid(getUuid());
		Patient patient = allergy.getPatient();
		Allergies allergies = Context.getPatientService().getAllergies(patient);
		Assert.assertEquals(Allergies.SEE_LIST, allergies.getAllergyStatus());
		
		String json = "{}";
		
		// attempt to set no known allergies with PUT
		handle(newPutRequest(getURI(), json));
	}

	/**
	 * Return no response body with a 204 http response status code for a patient with UNKNOWN Allergies
	 */
	@Test(expected = NoContentFoundException.class)
	public void shouldEnsureFetchingAllergiesForPatientThrowsNoContentFoundException() throws Exception {
		handle(newDeleteRequest(getURI(), new Parameter("reason", "unit test")));
		
		//fetch allergies for a patient with no allergies
		handle(newGetRequest(getURI()));
	}
	
	/**
	 * Set No Known Allergies correctly using empty post body and List of Allergies is empty
	 * achieved with PUT with empty post body when list of allergies is empty
	 */
	@Test
	public void shouldSetNoKnownAllergiesIfAllergiesEmpty() throws Exception {
		Allergy allergy = Context.getPatientService().getAllergyByUuid(getUuid());
		Patient patient = allergy.getPatient();
		Allergies allergies = Context.getPatientService().getAllergies(patient);
		Assert.assertEquals(Allergies.SEE_LIST, allergies.getAllergyStatus());
		
		// delete existing allergies
		handle(newDeleteRequest(getURI(), new Parameter("reason", "unit test")));
		
		allergies = Context.getPatientService().getAllergies(allergy.getPatient());
		Assert.assertEquals(Allergies.UNKNOWN, allergies.getAllergyStatus());
		
		String json = "{}";
		
		// attempt to set no known allergies with PUT
		handle(newPutRequest(getURI(), json));
		
		allergies = Context.getPatientService().getAllergies(patient);
		Assert.assertEquals(Allergies.NO_KNOWN_ALLERGIES, allergies.getAllergyStatus());
	}
	
	/**
	 * Save a new Allergy with its reactions in one call
	 */
	@Test
	public void shouldSaveNewAllergyWithReactionsInOneCall() throws Exception {
		Allergy allergy = Context.getPatientService().getAllergyByUuid(getUuid());
		Patient patient = allergy.getPatient();
		Allergies allergies = Context.getPatientService().getAllergies(patient);
		Assert.assertEquals(4, allergies.size());
		
		// save allergy with coded allergen
		String json = "{" + " \"comment\" : \"allergy comment\","
		        + " \"severity\" : { \"uuid\" : \"35d3346a-6769-4d52-823f-b4b234bac3e3\"}," + " \"allergen\" : "
		        + " { \"allergenType\" : \"DRUG\", "
		        + " \"codedAllergen\" : { \"uuid\" : \"35d3346a-6769-4d52-823f-b4b234bac3e3\"} " + " }, "
		        + "\"reactions\" : " + "[" + "{" + " \"reaction\" : { \"uuid\" : \"35d3346a-6769-4d52-823f-b4b234bac3e3\" }"
		        + "}," + "{" + " \"reaction\" : { \"uuid\" : \"5622AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\" },"
		        + " \"reactionNonCoded\" : \"test non coded reaction\"" + "}" + "]" + "}";
		
		// save allergy 
		SimpleObject savedAllergy = deserialize(handle(newPostRequest(getURI(), json)));
		
		Assert.assertEquals("allergy comment", Util.getByPath(savedAllergy, "comment"));
		Assert.assertEquals("35d3346a-6769-4d52-823f-b4b234bac3e3", Util.getByPath(savedAllergy, "severity/uuid"));
		
		Assert.assertEquals("35d3346a-6769-4d52-823f-b4b234bac3e3",
		    Util.getByPath(savedAllergy, "allergen/codedAllergen/uuid"));
		
		Assert.assertEquals("DRUG", Util.getByPath(savedAllergy, "allergen/allergenType"));
		
		Assert.assertEquals("35d3346a-6769-4d52-823f-b4b234bac3e3",
		    Util.getByPath(savedAllergy, "reactions[0]/reaction/uuid"));
		
		Assert.assertEquals("5622AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
		    Util.getByPath(savedAllergy, "reactions[1]/reaction/uuid"));
		
		Assert.assertEquals("test non coded reaction", Util.getByPath(savedAllergy, "reactions[1]/reactionNonCoded"));
		
		// assert that a new allergy has been added
		allergies = Context.getPatientService().getAllergies(patient);
		Assert.assertEquals(5, allergies.size());
	}
}

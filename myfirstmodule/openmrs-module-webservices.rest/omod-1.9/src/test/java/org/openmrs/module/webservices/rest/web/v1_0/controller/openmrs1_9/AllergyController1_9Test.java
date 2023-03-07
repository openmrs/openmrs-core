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

import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.beanutils.PropertyUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.activelist.Allergy;
import org.openmrs.activelist.AllergyType;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.test.Util;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.RestTestConstants1_8;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest;
import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_8.AllergyResource1_8;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.WebRequest;

public class AllergyController1_9Test extends MainResourceControllerTest {
	
	private static final String ACTIVE_LIST_INITIAL_XML = "customActiveListTest.xml";
	
	@Before
	public void init() throws Exception {
		executeDataSet(ACTIVE_LIST_INITIAL_XML);
	}
	
	/**
	 * @see AllergyController#getAllergy(String,WebRequest)
	 * @verifies get a default representation of an allergy
	 */
	@Test
	public void getAllergy_shouldGetADefaultRepresentationOfAnAllergy() throws Exception {
		SimpleObject result = deserialize(handle(newGetRequest(getURI() + "/" + RestTestConstants1_8.ALLERGY_UUID)));
		Assert.assertNotNull(result);
		Util.log("Allergy fetched (default)", result);
		Assert.assertEquals(RestTestConstants1_8.ALLERGY_UUID, PropertyUtils.getProperty(result, "uuid"));
		Assert.assertNotNull(PropertyUtils.getProperty(result, "links"));
		Assert.assertNotNull(PropertyUtils.getProperty(result, "person"));
		Assert.assertNotNull(PropertyUtils.getProperty(result, "allergen"));
		Assert.assertNull(PropertyUtils.getProperty(result, "auditInfo"));
	}
	
	/**
	 * @see AllergyController#getAllergy(String,WebRequest)
	 * @verifies get a full representation of an allergy
	 */
	@Test
	public void getAllergy_shouldGetAFullRepresentationOfAnAllergy() throws Exception {
		MockHttpServletRequest req = newGetRequest(getURI() + "/" + RestTestConstants1_8.ALLERGY_UUID, new Parameter(
		        RestConstants.REQUEST_PROPERTY_FOR_REPRESENTATION, RestConstants.REPRESENTATION_FULL));
		SimpleObject result = deserialize(handle(req));
		Assert.assertNotNull(result);
		Util.log("Allergy fetched (default)", result);
		Assert.assertEquals(RestTestConstants1_8.ALLERGY_UUID, PropertyUtils.getProperty(result, "uuid"));
		Assert.assertNotNull(PropertyUtils.getProperty(result, "links"));
		Assert.assertNotNull(PropertyUtils.getProperty(result, "person"));
		Assert.assertNotNull(PropertyUtils.getProperty(result, "allergen"));
		Assert.assertNotNull(PropertyUtils.getProperty(result, "auditInfo"));
	}
	
	/**
	 * @see AllergyController#voidAllergy(String,String,WebRequest,HttpServletResponse)
	 * @verifies void an allergy
	 */
	@Test
	public void voidAllergy_shouldVoidAnAllergy() throws Exception {
		Allergy allergy = Context.getPatientService().getAllergy(1);
		Assert.assertFalse(allergy.isVoided());
		
		handle(newDeleteRequest(getURI() + "/" + RestTestConstants1_8.ALLERGY_UUID, new Parameter("reason", "unit test")));
		allergy = Context.getPatientService().getAllergy(1);
		Assert.assertTrue(allergy.isVoided());
		Assert.assertEquals("unit test", allergy.getVoidReason());
	}
	
	/**
	 * @see AllergyResource1_8#getAllergyByPatient(String,
	 *      org.openmrs.module.webservices.rest.web.RequestContext)
	 * @throws Exception
	 */
	@Test
	public void searchByPatient_shouldGetAllergyForAPatient() throws Exception {
		MockHttpServletRequest req = newGetRequest(getURI(),
		    new Parameter("patient", "da7f524f-27ce-4bb2-86d6-6d1d05312bd5"));
		SimpleObject result = deserialize(handle(req));
		List<Object> results = Util.getResultsList(result);
		Assert.assertEquals(1, results.size());
		Assert.assertEquals(RestTestConstants1_8.ALLERGY_UUID, PropertyUtils.getProperty(results.get(0), "uuid"));
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest#getURI()
	 */
	@Override
	public String getURI() {
		return "allergy";
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest#getUuid()
	 */
	@Override
	public String getUuid() {
		return RestTestConstants1_8.ALLERGY_UUID;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest#getAllCount()
	 */
	@Override
	public long getAllCount() {
		return 0;
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
	public void getAllergen_shouldGetAllergenByConceptMappings() throws Exception {
		String json = "{\"person\":\"" + RestTestConstants1_8.PERSON_UUID
		        + "\", \"allergen\":\"SNOMED CT:2332523\", \"startDate\":\"2013-12-09\", \"allergyType\":\""
		        + AllergyType.DRUG + "\"}";
		Object newObs = deserialize(handle(newPostRequest(getURI(), json)));
		Assert.assertNotNull(PropertyUtils.getProperty(newObs, "allergen"));
	}
	
}

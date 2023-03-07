/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.search.openmrs2_2;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.Order;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestTestConstants1_10;
import org.openmrs.module.webservices.rest.web.RestTestConstants1_8;
import org.openmrs.module.webservices.rest.web.response.ObjectNotFoundException;
import org.openmrs.module.webservices.rest.web.v1_0.RestTestConstants2_2;
import org.openmrs.module.webservices.rest.web.v1_0.controller.RestControllerTestUtils;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

public class OrderSearchHandler2_2Test extends RestControllerTestUtils {
	
	protected String getURI() {
		return "order";
	}
	
	/**
	 * @verifies returns orders for a patient
	 * @see OrderSearchHandler2_2#search(RequestContext)
	 */
	@Test
	public void getSearchConfig_shouldReturnOrdersByPatientIdentifier() throws Exception {
		MockHttpServletRequest req = request(RequestMethod.GET, getURI());
		req.addParameter("s", "default");
		req.addParameter("patient", RestTestConstants1_8.PATIENT_UUID);
		
		SimpleObject result = deserialize(handle(req));
		List<Order> orders = result.get("results");
		Assert.assertEquals(11, orders.size());
	}
	
	/**
	 * @verifies returns orders by care setting
	 * @see OrderSearchHandler2_2#search(RequestContext)
	 */
	@Test
	public void getSearchConfig_shouldReturnOrdersByCareSetting() throws Exception {
		MockHttpServletRequest req = request(RequestMethod.GET, getURI());
		req.addParameter("s", "default");
		req.addParameter("careSetting", RestTestConstants2_2.OUTPATIENT_CARE_SETTING_UUID);
		
		SimpleObject result = deserialize(handle(req));
		List<Order> orders = result.get("results");
		Assert.assertEquals(12, orders.size());
	}
	
	/**
	 * @verifies returns all drug orders
	 * @see OrderSearchHandler2_2#search(RequestContext)
	 */
	@Test
	public void getSearchConfig_shouldReturnDrugOrders() throws Exception {
		MockHttpServletRequest req = request(RequestMethod.GET, getURI());
		req.addParameter("s", "default");
		req.addParameter("orderTypes", RestTestConstants1_10.DRUG_ORDER_TYPE_UUID);
		
		SimpleObject result = deserialize(handle(req));
		List<Order> orders = result.get("results");
		Assert.assertEquals(10, orders.size());
	}
	
	/**
	 * @verifies returns drug and test orders
	 * @see OrderSearchHandler2_2#search(RequestContext)
	 */
	@Test
	public void getSearchConfig_shouldReturnDrugAndTestOrders() throws Exception {
		MockHttpServletRequest req = request(RequestMethod.GET, getURI());
		req.addParameter("s", "default");
		req.addParameter("orderTypes", RestTestConstants1_10.DRUG_ORDER_TYPE_UUID
		        + "," + RestTestConstants1_10.TEST_ORDER_TYPE_UUID);
		
		SimpleObject result = deserialize(handle(req));
		List<Order> orders = result.get("results");
		Assert.assertEquals(13, orders.size());
	}
	
	/**
	 * @verifies returns orders matching concepts
	 * @see OrderSearchHandler2_2#search(RequestContext)
	 */
	@Test
	public void getSearchConfig_shouldReturnOrdersByConcepts() throws Exception {
		MockHttpServletRequest req = request(RequestMethod.GET, getURI());
		req.addParameter("s", "default");
		req.addParameter("concepts", RestTestConstants1_10.COUGH_SYRUP_UUID
		        + "," + RestTestConstants1_10.ASPIRIN_UUID);
		
		SimpleObject result = deserialize(handle(req));
		List<Order> orders = result.get("results");
		Assert.assertEquals(6, orders.size());
	}
	
	/**
	 * @verifies returns orders matching activatedOnOrBeforeDate
	 * @see OrderSearchHandler2_2#search(RequestContext)
	 */
	@Test
	public void getSearchConfig_shouldReturnOrdersActivatedBeforeDate() throws Exception {
		MockHttpServletRequest req = request(RequestMethod.GET, getURI());
		req.addParameter("s", "default");
		req.addParameter("activatedOnOrBeforeDate", "2008-08-19");
		
		SimpleObject result = deserialize(handle(req));
		List<Order> orders = result.get("results");
		Assert.assertEquals(11, orders.size());
	}
	
	/**
	 * @verifies returns orders matching activatedOnOrAfterDate
	 * @see OrderSearchHandler2_2#search(RequestContext)
	 */
	@Test
	public void getSearchConfig_shouldReturnOrdersActivatedAfterDate() throws Exception {
		MockHttpServletRequest req = request(RequestMethod.GET, getURI());
		req.addParameter("s", "default");
		req.addParameter("activatedOnOrAfterDate", "2008-08-19");
		
		SimpleObject result = deserialize(handle(req));
		List<Order> orders = result.get("results");
		Assert.assertEquals(3, orders.size());
	}
	
	/**
	 * @verifies returns voided orders
	 * @see OrderSearchHandler2_2#search(RequestContext)
	 */
	@Test
	public void getSearchConfig_shouldIncludeVoidedOrders() throws Exception {
		MockHttpServletRequest req = request(RequestMethod.GET, getURI());
		req.addParameter("s", "default");
		req.addParameter("includeVoided", "true");
		
		SimpleObject result = deserialize(handle(req));
		List<Order> orders = result.get("results");
		Assert.assertEquals(14, orders.size());
	}
	
	/**
	 * @verifies returns voided orders
	 * @see OrderSearchHandler2_2#search(RequestContext)
	 */
	@Test
	public void getSearchConfig_shouldReturnOrdersMatchingAllCriteria() throws Exception {
		MockHttpServletRequest req = request(RequestMethod.GET, getURI());
		req.addParameter("s", "default");
		req.addParameter("patient", RestTestConstants1_8.PATIENT_UUID);
		req.addParameter("careSetting", RestTestConstants2_2.OUTPATIENT_CARE_SETTING_UUID);
		req.addParameter("orderTypes", RestTestConstants1_10.DRUG_ORDER_TYPE_UUID);
		req.addParameter("concepts", RestTestConstants1_10.COUGH_SYRUP_UUID);
		req.addParameter("activatedOnOrAfterDate", "2008-08-19");
		req.addParameter("activatedOnOrBeforeDate", "2009-08-19");
		req.addParameter("includeVoided", "true");
		
		SimpleObject result = deserialize(handle(req));
		List<Order> orders = result.get("results");
		Assert.assertEquals(1, orders.size());
	}
	
	/**
	 * @verifies throws exception for invalid patient uuid
	 * @see OrderSearchHandler2_2#search(RequestContext)
	 */
	@Test(expected = ObjectNotFoundException.class)
	public void getSearchConfig_shouldThrowExceptionForNonExistentPatient() throws Exception {
		MockHttpServletRequest req = request(RequestMethod.GET, getURI());
		req.addParameter("s", "default");
		req.addParameter("patient", "FAKE_PATIENT_123_UUID");
		
		handle(req);
	}
	
	/**
	 * @verifies throws exception for invalid care setting
	 * @see OrderSearchHandler2_2#search(RequestContext)
	 */
	@Test(expected = ObjectNotFoundException.class)
	public void getSearchConfig_shouldThrowExceptionForNonExistentCareSetting() throws Exception {
		MockHttpServletRequest req = request(RequestMethod.GET, getURI());
		req.addParameter("s", "default");
		req.addParameter("careSetting", "FAKE_CARE_SETTING_123_UUID");
		
		handle(req);
	}
	
	/**
	 * @verifies throws exception if all order types are invalid
	 * @see OrderSearchHandler2_2#search(RequestContext)
	 */
	@Test(expected = ObjectNotFoundException.class)
	public void getSearchConfig_shouldThrowExceptionForNonExistentOrderTypes() throws Exception {
		MockHttpServletRequest req = request(RequestMethod.GET, getURI());
		req.addParameter("s", "default");
		req.addParameter("orderTypes", "FAKE_ORDER_TYPE_123_UUID");
		
		handle(req);
	}
	
	/**
	 * @verifies throws exception if all concepts are invalid
	 * @see OrderSearchHandler2_2#search(RequestContext)
	 */
	@Test(expected = ObjectNotFoundException.class)
	public void getSearchConfig_shouldThrowExceptionForNonExistentConcepts() throws Exception {
		MockHttpServletRequest req = request(RequestMethod.GET, getURI());
		req.addParameter("s", "default");
		req.addParameter("concepts", "FAKE_CONCEPT_123_UUID,FAKE_CONCEPT_124_UUID");
		
		handle(req);
	}
}

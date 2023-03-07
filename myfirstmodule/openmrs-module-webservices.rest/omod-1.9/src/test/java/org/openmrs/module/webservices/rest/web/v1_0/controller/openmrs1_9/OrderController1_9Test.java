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

import org.apache.commons.beanutils.PropertyUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.DrugOrder;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.api.OrderService;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.test.Util;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.RestTestConstants1_8;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.bind.annotation.RequestMethod;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Integration tests for the Order resource
 */
public class OrderController1_9Test extends MainResourceControllerTest {
	
	private final static String DRUG_ORDER_UUID = "921de0a3-05c4-444a-be03-e01b4c4b9142";
	
	private final static String PATIENT_UUID = "5946f880-b197-400b-9caa-a3c661d23041";
	
	private OrderService service;
	
	private PatientService patientService;
	
	@Before
	public void before() throws Exception {
		executeDataSet("customTestDataset.xml");
		this.service = Context.getOrderService();
		this.patientService = Context.getPatientService();
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest#getURI()
	 */
	@Override
	public String getURI() {
		return "order";
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest#getAllCount()
	 */
	@Override
	public long getAllCount() {
		return service.getOrders(Order.class, null, null, null, null, null, null).size();
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest#getUuid()
	 */
	@Override
	public String getUuid() {
		return RestTestConstants1_8.ORDER_UUID;
	}
	
	@Test
	public void shouldGetOrderAsRef() throws Exception {
		
		MockHttpServletRequest req = request(RequestMethod.GET, getURI() + "/" + getUuid());
		req.setParameter(RestConstants.REQUEST_PROPERTY_FOR_REPRESENTATION, Representation.REF.getRepresentation());
		SimpleObject result = deserialize(handle(req));
		
		Assert.assertEquals(getUuid(), PropertyUtils.getProperty(result, "uuid"));
		Assert.assertNotNull(PropertyUtils.getProperty(result, "display"));
		Assert.assertNull(PropertyUtils.getProperty(result, "concept"));
		Util.log("order as ref", result);
	}
	
	@Test
	public void shouldGetOrderAsDefault() throws Exception {
		MockHttpServletRequest req = request(RequestMethod.GET, getURI() + "/" + getUuid());
		SimpleObject result = deserialize(handle(req));
		
		Assert.assertEquals(getUuid(), PropertyUtils.getProperty(result, "uuid"));
		Assert.assertNotNull(PropertyUtils.getProperty(result, "display"));
		Assert.assertNotNull(PropertyUtils.getProperty(result, "patient"));
		Assert.assertNotNull(PropertyUtils.getProperty(result, "concept"));
		Util.log("drug order as default", result);
	}
	
	@Test
	public void shouldGetDrugOrderAsRef() throws Exception {
		MockHttpServletRequest req = request(RequestMethod.GET, getURI() + "/" + DRUG_ORDER_UUID);
		req.setParameter(RestConstants.REQUEST_PROPERTY_FOR_REPRESENTATION, Representation.REF.getRepresentation());
		SimpleObject result = deserialize(handle(req));
		
		Assert.assertEquals(DRUG_ORDER_UUID, PropertyUtils.getProperty(result, "uuid"));
		Assert.assertNotNull(PropertyUtils.getProperty(result, "display"));
		Assert.assertNull(PropertyUtils.getProperty(result, "concept"));
	}
	
	@Test
	public void shouldGetDrugOrderAsDefault() throws Exception {
		MockHttpServletRequest req = request(RequestMethod.GET, getURI() + "/" + DRUG_ORDER_UUID);
		SimpleObject result = deserialize(handle(req));
		
		Assert.assertEquals(DRUG_ORDER_UUID, PropertyUtils.getProperty(result, "uuid"));
		Assert.assertNotNull(PropertyUtils.getProperty(result, "display"));
		Assert.assertNotNull(PropertyUtils.getProperty(result, "patient"));
		Assert.assertNotNull(PropertyUtils.getProperty(result, "concept"));
	}
	
	@Test
	public void shouldGetAllOrders() throws Exception {
		
		MockHttpServletRequest req = request(RequestMethod.GET, getURI());
		SimpleObject result = deserialize(handle(req));
		
		int count = service.getOrders(Order.class, null, null, null, null, null, null).size();
		
		Assert.assertEquals(count, Util.getResultsSize(result));
		
	}
	
	@Test
	public void shouldGetAllDrugOrders() throws Exception {
		
		MockHttpServletRequest req = request(RequestMethod.GET, getURI());
		req.setParameter(RestConstants.REQUEST_PROPERTY_FOR_TYPE, "drugorder");
		SimpleObject result = deserialize(handle(req));
		
		int count = service.getOrders(DrugOrder.class, null, null, null, null, null, null).size();
		
		Assert.assertEquals(count, Util.getResultsSize(result));
		
	}
	
	@Test
	public void shouldGetAllOrdersByPatient() throws Exception {
		
		MockHttpServletRequest req = request(RequestMethod.GET, getURI());
		req.setParameter("patient", PATIENT_UUID);
		SimpleObject result = deserialize(handle(req));
		
		Patient patient = patientService.getPatientByUuid(PATIENT_UUID);
		List<Order> orders = service.getOrdersByPatient(patient);
		Assert.assertEquals(orders.size(), Util.getResultsSize(result));
		
	}
	
	@Test
	public void shouldGetAllDrugOrdersByPatient() throws Exception {
		
		MockHttpServletRequest req = request(RequestMethod.GET, getURI());
		req.setParameter(RestConstants.REQUEST_PROPERTY_FOR_TYPE, "drugorder");
		req.setParameter("patient", PATIENT_UUID);
		SimpleObject result = deserialize(handle(req));
		
		Patient patient = patientService.getPatientByUuid(PATIENT_UUID);
		int count = service.getDrugOrdersByPatient(patient).size();
		
		Assert.assertEquals(count, Util.getResultsSize(result));
	}
	
	@Test
	public void shouldCreateOrder() throws Exception {
		
		SimpleObject order = new SimpleObject();
		order.add("type", "order");
		order.add("patient", PATIENT_UUID);
		order.add("concept", "0dde1358-7fcf-4341-a330-f119241a46e8");
		order.add("orderType", "e23733ab-787e-4096-8ba2-577a902d2c2b");
		
		MockHttpServletRequest req = newPostRequest(getURI(), order);
		SimpleObject newOrder = deserialize(handle(req));
		
		Assert.assertEquals(order.get("type"), PropertyUtils.getProperty(newOrder, "type"));
		Assert.assertEquals(order.get("concept"), Util.getByPath(newOrder, "concept/uuid"));
		Assert.assertEquals(order.get("orderType"), Util.getByPath(newOrder, "orderType/uuid"));
		Assert.assertEquals(order.get("patient"), Util.getByPath(newOrder, "patient/uuid"));
	}
	
	@Test
	public void shouldCreateDrugOrder() throws Exception {
		
		SimpleObject order = new SimpleObject();
		order.add("type", "drugorder");
		order.add("patient", PATIENT_UUID);
		order.add("concept", "d144d24f-6913-4b63-9660-a9108c2bebef");
		order.add("drug", "3cfcf118-931c-46f7-8ff6-7b876f0d4202");
		order.add("dose", "1");
		order.add("units", "tablet");
		
		MockHttpServletRequest req = newPostRequest(getURI(), order);
		SimpleObject result = deserialize(handle(req));
		
		Assert.assertEquals(order.get("type"), PropertyUtils.getProperty(result, "type"));
		Assert.assertEquals(order.get("concept"), Util.getByPath(result, "concept/uuid"));
		Assert.assertEquals(order.get("patient"), Util.getByPath(result, "patient/uuid"));
		Assert.assertEquals(order.get("units"), PropertyUtils.getProperty(result, "units"));
		Assert.assertEquals(order.get("drug"), Util.getByPath(result, "drug/uuid"));
	}
	
	@Test
	public void shouldUpdateOrder() throws Exception {
		
		SimpleObject content = new SimpleObject();
		content.add("instructions", "Updated instructions");
		
		MockHttpServletRequest req = newPostRequest(getURI() + "/" + getUuid(), content);
		handle(req);
		
		Assert.assertEquals(content.get("instructions"), service.getOrderByUuid(getUuid()).getInstructions());
	}
	
	@Test
	public void shouldUnVoidAnOrder() throws Exception {
		service = Context.getOrderService();
		Order order = service.getOrderByUuid(getUuid());
		service.voidOrder(order, "some random reason");
		order = service.getOrderByUuid(getUuid());
		Assert.assertTrue(order.isVoided());
		
		String json = "{\"deleted\": \"false\"}";
		SimpleObject response = deserialize(handle(newPostRequest(getURI() + "/" + getUuid(), json)));
		
		order = service.getOrderByUuid(getUuid());
		Assert.assertFalse(order.isVoided());
		Assert.assertEquals("false", PropertyUtils.getProperty(response, "voided").toString());
		
	}
	
	@Test
	public void shouldUpdateDrugOrder() throws Exception {
		
		SimpleObject content = new SimpleObject();
		content.add("dose", "500");
		
		MockHttpServletRequest req = newPostRequest(getURI() + "/" + DRUG_ORDER_UUID, content);
		handle(req);
		
		DrugOrder order = (DrugOrder) service.getOrderByUuid(DRUG_ORDER_UUID);
		Assert.assertEquals(Double.valueOf("500"), order.getDose());
	}
	
	/**
	 * See RESTWS-418 - Allow REST POST requests to accept un-updatable properties if they haven't
	 * been updated
	 */
	@Test
	public void shouldAllowYouToPostANonUpdatablePropertyWithAnUnchangedValue() throws Exception {
		MockHttpServletRequest get = request(RequestMethod.GET, getURI() + "/" + DRUG_ORDER_UUID);
		SimpleObject drugOrder = deserialize(handle(get));
		// doing this will no longer be allowed in OpenMRS 1.10, but it's fine as a test case against 1.8 code
		drugOrder.put("dose", "500");
		
		MockHttpServletRequest post = newPostRequest(getURI() + "/" + DRUG_ORDER_UUID, drugOrder);
		handle(post);
		
		DrugOrder updatedOrder = (DrugOrder) service.getOrderByUuid(DRUG_ORDER_UUID);
		assertThat(updatedOrder.getDose(), is(500d));
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void shouldFailToChangeOrderType() throws Exception {
		
		SimpleObject content = new SimpleObject();
		content.add("type", "order");
		
		MockHttpServletRequest req = newPostRequest(getURI() + "/" + getUuid(), content);
		handle(req);
		
	}
	
	@Test
	public void shouldVoidOrder() throws Exception {
		Order order = service.getOrderByUuid(getUuid());
		Assert.assertTrue(!order.isVoided());
		
		handle(newDeleteRequest(getURI() + "/" + getUuid(), new Parameter("!purge", "random reason")));
		
		order = service.getOrderByUuid(getUuid());
		Assert.assertTrue(order.isVoided());
	}
	
	@Test
	public void shouldVoidDrugOrder() throws Exception {
		Order order = service.getOrderByUuid(DRUG_ORDER_UUID);
		Assert.assertTrue(!order.isVoided());
		
		handle(newDeleteRequest(getURI() + "/" + getUuid(), new Parameter("!purge", "random reason")));
		
		order = service.getOrderByUuid(DRUG_ORDER_UUID);
		Assert.assertTrue(order.isVoided());
	}
	
	@Test
	public void shouldPurgeOrder() throws Exception {
		Assert.assertNotNull(service.getOrderByUuid(getUuid()));
		handle(newDeleteRequest(getURI() + "/" + getUuid(), new Parameter("purge", "true")));
		Assert.assertNull(service.getOrderByUuid(getUuid()));
	}
	
	@Test
	public void shouldPurgeDrugOrder() throws Exception {
		Assert.assertNotNull(service.getOrderByUuid(DRUG_ORDER_UUID));
		handle(newDeleteRequest(getURI() + "/" + getUuid(), new Parameter("purge", "true")));
		Assert.assertNull(service.getOrderByUuid(DRUG_ORDER_UUID));
	}
	
}

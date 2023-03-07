/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.controller.openmrs1_12;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.apache.commons.beanutils.PropertyUtils;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.CareSetting;
import org.openmrs.ConceptClass;
import org.openmrs.Order;
import org.openmrs.OrderType;
import org.openmrs.Patient;
import org.openmrs.api.OrderService;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.test.Util;
import org.openmrs.module.webservices.rest.web.RestTestConstants1_10;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest;
import org.springframework.mock.web.MockHttpServletRequest;

public class OrderController1_12Test extends MainResourceControllerTest {
	
	protected static final String ORDER_ENTRY_DATASET_XML = "org/openmrs/api/include/OrderEntryIntegrationTest-other.xml";
	
	private final static String PATIENT_UUID = "5946f880-b197-400b-9caa-a3c661d23041";
	
	private OrderService orderService;
	
	private PatientService patientService;
	
	@Before
	public void before() throws Exception {
		this.orderService = Context.getOrderService();
		this.patientService = Context.getPatientService();
		executeDataSet(ORDER_ENTRY_DATASET_XML);
	}
	
	@Override
	public String getURI() {
		return "order";
	}
	
	@Override
	public String getUuid() {
		return RestTestConstants1_10.ORDER_UUID;
	}
	
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
	public void shouldPlaceANewDrugOrder() throws Exception {
		executeDataSet(ORDER_ENTRY_DATASET_XML);
		executeDataSet("OrderController1_10Test-conceptMappings.xml");
		CareSetting outPatient = orderService.getCareSettingByUuid(RestTestConstants1_10.CARE_SETTING_UUID);
		Patient patient = patientService.getPatientByUuid(PATIENT_UUID);
		OrderType drugOrderType = orderService.getOrderTypeByName("Drug order");
		if (drugOrderType.getConceptClasses().isEmpty()) {
			ConceptClass drugClass = Context.getConceptService().getConceptClassByName("Drug");
			assertNotNull(drugClass);
			drugOrderType.getConceptClasses().add(drugClass);
		}
		int originalActiveDrugOrderCount = orderService.getActiveOrders(patient, drugOrderType, outPatient, null).size();
		SimpleObject order = new SimpleObject();
		order.add("type", "drugorder");
		order.add("patient", PATIENT_UUID);
		order.add("careSetting", RestTestConstants1_10.CARE_SETTING_UUID);
		order.add("encounter", "e403fafb-e5e4-42d0-9d11-4f52e89d148c");
		order.add("drug", "3cfcf118-931c-46f7-8ff6-7b876f0d4202");
		order.add("orderer", "c2299800-cca9-11e0-9572-0800200c9a66");
		order.add("dosingType", "org.openmrs.SimpleDosingInstructions");
		order.add("dose", "300.0");
		order.add("doseUnits", "557b9699-68a3-11e3-bd76-0800271c1b75");
		order.add("quantity", "20.0");
		order.add("quantityUnits", "5a2aa3db-68a3-11e3-bd76-0800271c1b75");
		order.add("duration", "20");
		order.add("durationUnits", "7bfdcbf0-d9e7-11e3-9c1a-0800200c9a66");
		order.add("frequency", "38090760-7c38-11e4-baa7-0800200c9a67");
		order.add("numRefills", "2");
		order.add("route", "e10ffe54-5184-4efe-8960-cd565ec1cdf8");
		order.add("brandName", "Some brand name");
		order.add("dispenseAsWritten", true);
		order.add("drugNonCoded", "Some non coded drug");
		
		MockHttpServletRequest req = newPostRequest(getURI(), order);
		SimpleObject newOrder = deserialize(handle(req));
		
		List<Order> activeDrugOrders = orderService.getActiveOrders(patient, orderService.getOrderTypeByName("Drug order"),
		    outPatient, null);
		assertEquals(++originalActiveDrugOrderCount, activeDrugOrders.size());
		
		assertNotNull(PropertyUtils.getProperty(newOrder, "orderNumber"));
		assertEquals("NEW", Util.getByPath(newOrder, "action"));
		assertEquals(order.get("patient"), Util.getByPath(newOrder, "patient/uuid"));
		final String expectedConceptUuid = Context.getConceptService().getDrugByUuid(order.get("drug").toString())
		        .getConcept().getUuid();
		assertEquals(expectedConceptUuid, Util.getByPath(newOrder, "concept/uuid"));
		assertEquals(order.get("careSetting"), Util.getByPath(newOrder, "careSetting/uuid"));
		assertNotNull(PropertyUtils.getProperty(newOrder, "dateActivated"));
		assertEquals(order.get("encounter"), Util.getByPath(newOrder, "encounter/uuid"));
		assertEquals(order.get("orderer"), Util.getByPath(newOrder, "orderer/uuid"));
		assertEquals("(NEW) Triomune-30: 300.0 mg UNKNOWN Once 20 day", Util.getByPath(newOrder, "display"));
		assertEquals(order.get("drug"), Util.getByPath(newOrder, "drug/uuid"));
		assertEquals(order.get("dosingType"), Util.getByPath(newOrder, "dosingType"));
		assertEquals(order.get("dose"), Util.getByPath(newOrder, "dose").toString());
		assertEquals(order.get("doseUnits"), Util.getByPath(newOrder, "doseUnits/uuid"));
		assertEquals(order.get("quantity"), Util.getByPath(newOrder, "quantity").toString());
		assertEquals(order.get("quantityUnits"), Util.getByPath(newOrder, "quantityUnits/uuid"));
		assertEquals(order.get("duration"), Util.getByPath(newOrder, "duration").toString());
		assertEquals(order.get("durationUnits"), Util.getByPath(newOrder, "durationUnits/uuid"));
		assertEquals(order.get("frequency"), Util.getByPath(newOrder, "frequency/uuid"));
		assertEquals(order.get("brandName"), Util.getByPath(newOrder, "brandName"));
		assertEquals(order.get("dispenseAsWritten"), Util.getByPath(newOrder, "dispenseAsWritten"));
		assertEquals(order.get("drugNonCoded"), Util.getByPath(newOrder, "drugNonCoded"));
	}
}

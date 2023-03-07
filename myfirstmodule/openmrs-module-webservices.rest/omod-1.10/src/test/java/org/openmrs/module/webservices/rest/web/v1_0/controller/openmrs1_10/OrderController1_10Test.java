/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.controller.openmrs1_10;

import org.apache.commons.beanutils.PropertyUtils;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.CareSetting;
import org.openmrs.ConceptClass;
import org.openmrs.Encounter;
import org.openmrs.Order;
import org.openmrs.OrderType;
import org.openmrs.Patient;
import org.openmrs.api.EncounterService;
import org.openmrs.api.OrderService;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.test.Util;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.RestTestConstants1_10;
import org.openmrs.module.webservices.rest.web.response.ObjectNotFoundException;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Integration tests for the Order resource
 */
public class OrderController1_10Test extends MainResourceControllerTest {
	
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
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest#getURI()
	 */
	@Override
	public String getURI() {
		return "order";
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest#getUuid()
	 */
	@Override
	public String getUuid() {
		return RestTestConstants1_10.ORDER_UUID;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest#getAllCount()
	 */
	@Override
	public long getAllCount() {
		//TODO Not yet supported should be though after reworking OrderService.getOrders
		//See https://tickets.openmrs.org/browse/TRUNK-4173
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
	public void shouldGetOrderByUuid() throws Exception {
		SimpleObject result = deserialize(handle(newGetRequest(getURI() + "/" + getUuid())));
		assertEquals(getUuid(), PropertyUtils.getProperty(result, "uuid"));
	}
	
	@Test
	public void shouldGetOrderByOrderNumber() throws Exception {
		SimpleObject result = deserialize(handle(newGetRequest(getURI() + "/ORD-7")));
		assertEquals(getUuid(), PropertyUtils.getProperty(result, "uuid"));
	}
	
	@Test
	public void shouldPlaceANewOrder() throws Exception {
		executeDataSet("OrderController1_10Test-orderTypeAndConceptClassMap.xml");
		CareSetting outPatient = orderService.getCareSettingByUuid(RestTestConstants1_10.CARE_SETTING_UUID);
		Patient patient = patientService.getPatientByUuid(PATIENT_UUID);
		int originalActiveOrderCount = orderService.getActiveOrders(patient, null, outPatient, null).size();
		
		SimpleObject order = new SimpleObject();
		order.add("type", "order");
		order.add("patient", PATIENT_UUID);
		order.add("concept", "0a9afe04-088b-44ca-9291-0a8c3b5c96fa");
		order.add("careSetting", RestTestConstants1_10.CARE_SETTING_UUID);
		order.add("encounter", "e403fafb-e5e4-42d0-9d11-4f52e89d148c");
		order.add("orderer", "c2299800-cca9-11e0-9572-0800200c9a66");
		order.add("accessionNumber", "100");
		
		MockHttpServletRequest req = newPostRequest(getURI(), order);
		SimpleObject newOrder = deserialize(handle(req));
		
		List<Order> activeOrders = orderService.getActiveOrders(patient, null, outPatient, null);
		assertEquals(++originalActiveOrderCount, activeOrders.size());
		
		assertNotNull(PropertyUtils.getProperty(newOrder, "orderNumber"));
		assertEquals("NEW", Util.getByPath(newOrder, "action"));
		assertEquals(order.get("patient"), Util.getByPath(newOrder, "patient/uuid"));
		assertEquals(order.get("concept"), Util.getByPath(newOrder, "concept/uuid"));
		assertEquals(order.get("careSetting"), Util.getByPath(newOrder, "careSetting/uuid"));
		assertNotNull(PropertyUtils.getProperty(newOrder, "dateActivated"));
		assertEquals(order.get("encounter"), Util.getByPath(newOrder, "encounter/uuid"));
		assertEquals(order.get("orderer"), Util.getByPath(newOrder, "orderer/uuid"));
		assertEquals("100", Util.getByPath(newOrder, "accessionNumber"));
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
	}
	
	@Test
	public void shouldSetDrugOrderDisplayWithoutDrug() throws Exception {
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
		order.add("orderer", "c2299800-cca9-11e0-9572-0800200c9a66");
		order.add("concept", "15f83cd6-64e9-4e06-a5f9-364d3b14a43d");
		order.add("dosingType", "org.openmrs.FreeTextDosingInstructions");
		order.add("dosingInstructions", "Follow these instructions closely");
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
		
		MockHttpServletRequest req = newPostRequest(getURI(), order);
		SimpleObject newOrder = deserialize(handle(req));
		
		List<Order> activeDrugOrders = orderService.getActiveOrders(patient, orderService.getOrderTypeByName("Drug order"),
		    outPatient, null);
		assertEquals(++originalActiveDrugOrderCount, activeDrugOrders.size());
		
		assertEquals("(NEW) ASPIRIN: Follow these instructions closely", Util.getByPath(newOrder, "display"));
	}
	
	@Test
	public void shouldPlaceANewTestOrder() throws Exception {
		executeDataSet(ORDER_ENTRY_DATASET_XML);
		OrderType testOrderType = orderService.getOrderTypeByName("Test order");
		CareSetting outPatient = orderService.getCareSettingByUuid(RestTestConstants1_10.CARE_SETTING_UUID);
		Patient patient = patientService.getPatientByUuid(PATIENT_UUID);
		int originalActiveTestOrderCount = orderService.getActiveOrders(patient,
		    testOrderType, outPatient, null).size();
		
		SimpleObject order = new SimpleObject();
		order.add("type", "testorder");
		order.add("orderType", testOrderType.getUuid());
		order.add("patient", PATIENT_UUID);
		final String cd4CountUuid = "a09ab2c5-878e-4905-b25d-5784167d0216";
		order.add("concept", cd4CountUuid);
		order.add("careSetting", RestTestConstants1_10.CARE_SETTING_UUID);
		order.add("encounter", "e403fafb-e5e4-42d0-9d11-4f52e89d148c");
		order.add("orderer", "c2299800-cca9-11e0-9572-0800200c9a66");
		order.add("clinicalHistory", "Patient had a negative reaction to the test in the past");
		String onceUuid = "38090760-7c38-11e4-baa7-0800200c9a67";
		order.add("frequency", onceUuid);
		order.add("specimenSource", "e10ffe54-5184-4efe-8960-cd565ec1cdf8");
		order.add("numberOfRepeats", "3");
		
		MockHttpServletRequest req = newPostRequest(getURI(), order);
		SimpleObject newOrder = deserialize(handle(req));
		
		List<Order> activeTestOrders = orderService.getActiveOrders(patient, orderService.getOrderTypeByName("Test order"),
		    outPatient, null);
		assertEquals(++originalActiveTestOrderCount, activeTestOrders.size());
		
		assertNotNull(PropertyUtils.getProperty(newOrder, "orderNumber"));
		assertEquals("NEW", Util.getByPath(newOrder, "action"));
		assertEquals(order.get("orderType"), Util.getByPath(newOrder, "orderType/uuid"));
		assertEquals(order.get("patient"), Util.getByPath(newOrder, "patient/uuid"));
		assertEquals(order.get("concept"), Util.getByPath(newOrder, "concept/uuid"));
		assertEquals(order.get("careSetting"), Util.getByPath(newOrder, "careSetting/uuid"));
		assertNotNull(PropertyUtils.getProperty(newOrder, "dateActivated"));
		assertEquals(order.get("encounter"), Util.getByPath(newOrder, "encounter/uuid"));
		assertEquals(order.get("orderer"), Util.getByPath(newOrder, "orderer/uuid"));
		assertEquals(order.get("specimenSource"), Util.getByPath(newOrder, "specimenSource/uuid"));
		assertNull(Util.getByPath(newOrder, "laterality"));
		assertEquals(order.get("clinicalHistory"), Util.getByPath(newOrder, "clinicalHistory"));
		assertEquals(order.get("frequency"), Util.getByPath(newOrder, "frequency/uuid"));
		assertEquals(order.get("numberOfRepeats"), Util.getByPath(newOrder, "numberOfRepeats").toString());
	}
	
	@Test
	public void shouldDiscontinueAnActiveOrder() throws Exception {
		Order orderToDiscontinue = orderService.getOrder(7);
		Patient patient = orderToDiscontinue.getPatient();
		List<Order> originalActiveOrders = orderService.getActiveOrders(patient, null, null, null);
		assertTrue(originalActiveOrders.contains(orderToDiscontinue));
		
		SimpleObject dcOrder = new SimpleObject();
		dcOrder.add("type", "testorder");
		dcOrder.add("action", "DISCONTINUE");
		dcOrder.add("patient", patient.getUuid());
		dcOrder.add("concept", orderToDiscontinue.getConcept().getUuid());
		dcOrder.add("careSetting", orderToDiscontinue.getCareSetting().getUuid());
		dcOrder.add("previousOrder", orderToDiscontinue.getUuid());
		dcOrder.add("encounter", Context.getEncounterService().getEncounter(6).getUuid());
		dcOrder.add("orderer", "c2299800-cca9-11e0-9572-0800200c9a66");
		dcOrder.add("orderReasonNonCoded", "Patient is allergic");
		
		SimpleObject savedDCOrder = deserialize(handle(newPostRequest(getURI(), dcOrder)));
		
		List<Order> newActiveOrders = orderService.getActiveOrders(patient, null, null, null);
		assertEquals(originalActiveOrders.size() - 1, newActiveOrders.size());
		assertFalse(newActiveOrders.contains(orderToDiscontinue));
		assertNotNull(PropertyUtils.getProperty(savedDCOrder, "orderNumber"));
		assertEquals(dcOrder.get("action"), Util.getByPath(savedDCOrder, "action"));
		assertEquals(orderToDiscontinue.getPatient().getUuid(), Util.getByPath(savedDCOrder, "patient/uuid"));
		assertEquals(orderToDiscontinue.getCareSetting().getUuid(), Util.getByPath(savedDCOrder, "careSetting/uuid"));
		assertEquals(dcOrder.get("previousOrder"), Util.getByPath(savedDCOrder, "previousOrder/uuid"));
		assertNotNull(PropertyUtils.getProperty(savedDCOrder, "dateActivated"));
		assertEquals(orderToDiscontinue.getConcept().getUuid(), Util.getByPath(savedDCOrder, "concept/uuid"));
		assertEquals(dcOrder.get("encounter"), Util.getByPath(savedDCOrder, "encounter/uuid"));
		assertEquals(dcOrder.get("orderer"), Util.getByPath(savedDCOrder, "orderer/uuid"));
		assertEquals(dcOrder.get("orderReasonNonCoded"), Util.getByPath(savedDCOrder, "orderReasonNonCoded"));
	}
	
	@Test
	public void shouldReviseAnActiveOrder() throws Exception {
		Order orderToRevise = orderService.getOrder(7);
		Patient patient = orderToRevise.getPatient();
		List<Order> originalActiveOrders = orderService.getActiveOrders(patient, null, null, null);
		assertTrue(originalActiveOrders.contains(orderToRevise));
		
		EncounterService es = Context.getEncounterService();
		Date date = new Date();
		Encounter encounter = new Encounter();
		encounter.setEncounterType(es.getEncounterType(1));
		encounter.setPatient(patient);
		encounter.setEncounterDatetime(date);
		es.saveEncounter(encounter);
		
		SimpleObject revisedOrder = new SimpleObject();
		revisedOrder.add("type", "testorder");
		revisedOrder.add("action", "REVISE");
		revisedOrder.add("previousOrder", orderToRevise.getUuid());
		revisedOrder.add("patient", patient.getUuid());
		revisedOrder.add("careSetting", orderToRevise.getCareSetting().getUuid());
		revisedOrder.add("concept", orderToRevise.getConcept().getUuid());
		revisedOrder.add("encounter", encounter.getUuid());
		revisedOrder.add("orderer", "c2299800-cca9-11e0-9572-0800200c9a66");
		revisedOrder.add("instructions", "To be taken after a meal");
		revisedOrder.add("orderReasonNonCoded", "Changed instructions");
		
		SimpleObject savedOrder = deserialize(handle(newPostRequest(getURI(), revisedOrder)));
		
		List<Order> newActiveOrders = orderService.getActiveOrders(patient, null, null, null);
		assertEquals(originalActiveOrders.size(), newActiveOrders.size());
		assertFalse(newActiveOrders.contains(orderToRevise));
		assertNotNull(PropertyUtils.getProperty(savedOrder, "orderNumber"));
		assertEquals(revisedOrder.get("action"), Util.getByPath(savedOrder, "action"));
		assertEquals(patient.getUuid(), Util.getByPath(savedOrder, "patient/uuid"));
		assertEquals(orderToRevise.getCareSetting().getUuid(), Util.getByPath(savedOrder, "careSetting/uuid"));
		assertEquals(revisedOrder.get("previousOrder"), Util.getByPath(savedOrder, "previousOrder/uuid"));
		assertEquals(revisedOrder.get("concept"), Util.getByPath(savedOrder, "concept/uuid"));
		assertEquals(revisedOrder.get("encounter"), Util.getByPath(savedOrder, "encounter/uuid"));
		assertEquals(revisedOrder.get("orderer"), Util.getByPath(savedOrder, "orderer/uuid"));
		assertEquals(revisedOrder.get("instructions"), Util.getByPath(savedOrder, "instructions"));
		assertEquals(revisedOrder.get("orderReasonNonCoded"), Util.getByPath(savedOrder, "orderReasonNonCoded"));
	}
	
	@Test
	public void shouldGetTheActiveOrdersForAPatient() throws Exception {
		String[] expectedOrderUuids = { orderService.getOrder(3).getUuid(), orderService.getOrder(5).getUuid(),
		        orderService.getOrder(7).getUuid(), orderService.getOrder(222).getUuid(),
		        orderService.getOrder(444).getUuid() };
		SimpleObject results = deserialize(handle(newGetRequest(getURI(), new Parameter("patient",
		        "da7f524f-27ce-4bb2-86d6-6d1d05312bd5"), new Parameter("sort", "desc"), new Parameter("status", "active"))));
		assertEquals(expectedOrderUuids.length, Util.getResultsSize(results));
		List<Object> resultList = Util.getResultsList(results);
		List<String> uuids = Arrays.asList(new String[] { PropertyUtils.getProperty(resultList.get(0), "uuid").toString(),
		        PropertyUtils.getProperty(resultList.get(1), "uuid").toString(),
		        PropertyUtils.getProperty(resultList.get(2), "uuid").toString(),
		        PropertyUtils.getProperty(resultList.get(3), "uuid").toString(),
		        PropertyUtils.getProperty(resultList.get(4), "uuid").toString() });
		assertThat(
		    uuids,
		    contains(orderService.getOrder(7).getUuid(), orderService.getOrder(5).getUuid(), orderService.getOrder(444)
		            .getUuid(), orderService.getOrder(3).getUuid(), orderService.getOrder(222).getUuid()));
	}
	
	@Test
	public void shouldGetTheActiveOrdersForAPatientInTheSpecifiedCareSetting() throws Exception {
		String expectedOrderUuid = orderService.getOrder(222).getUuid();
		SimpleObject results = deserialize(handle(newGetRequest(getURI(), new Parameter("patient",
		        "da7f524f-27ce-4bb2-86d6-6d1d05312bd5"),
		    new Parameter("careSetting", "c365e560-c3ec-11e3-9c1a-0800200c9a66"))));
		assertEquals(1, Util.getResultsSize(results));
		assertEquals(expectedOrderUuid, PropertyUtils.getProperty(Util.getResultsList(results).get(0), "uuid"));
	}
	
	@Test
	public void shouldGetTheActiveOrdersForAPatientAsOfTheSpecifiedDate() throws Exception {
		SimpleObject results = deserialize(handle(newGetRequest(getURI(), new Parameter("patient", patientService
		        .getPatient(2).getUuid()), new Parameter("asOfDate", "2007-12-10"))));
		
		assertEquals(2, Util.getResultsSize(results));
		
		results = deserialize(handle(newGetRequest(getURI(),
		    new Parameter("patient", patientService.getPatient(2).getUuid()), new Parameter("asOfDate",
		            "2007-12-10 00:01:00"))));
		
		assertEquals(1, Util.getResultsSize(results));
	}
	
	@Test
	public void shouldGetTheActiveDrugOrdersForAPatient() throws Exception {
		String[] expectedOrderUuids = { orderService.getOrder(3).getUuid(), orderService.getOrder(5).getUuid(),
		        orderService.getOrder(222).getUuid(), orderService.getOrder(444).getUuid() };
		SimpleObject results = deserialize(handle(newGetRequest(getURI(), new Parameter(
		        RestConstants.REQUEST_PROPERTY_FOR_TYPE, "drugorder"), new Parameter("patient",
		        "da7f524f-27ce-4bb2-86d6-6d1d05312bd5"), new Parameter("sort", "asc"), new Parameter("status", "active"))));
		assertEquals(expectedOrderUuids.length, Util.getResultsSize(results));
		List<Object> resultList = Util.getResultsList(results);
		List<String> uuids = Arrays.asList(new String[] { PropertyUtils.getProperty(resultList.get(0), "uuid").toString(),
		        PropertyUtils.getProperty(resultList.get(1), "uuid").toString(),
		        PropertyUtils.getProperty(resultList.get(2), "uuid").toString(),
		        PropertyUtils.getProperty(resultList.get(3), "uuid").toString(), });
		assertThat(
		    uuids,
		    contains(orderService.getOrder(222).getUuid(), orderService.getOrder(3).getUuid(), orderService.getOrder(444)
		            .getUuid(), orderService.getOrder(5).getUuid()));
	}
	
	@Test
	public void shouldGetTheActiveTestOrdersForAPatient() throws Exception {
		String expectedOrderUuid = orderService.getOrder(7).getUuid();
		SimpleObject results = deserialize(handle(newGetRequest(getURI(), new Parameter(
		        RestConstants.REQUEST_PROPERTY_FOR_TYPE, "testorder"), new Parameter("patient",
		        "da7f524f-27ce-4bb2-86d6-6d1d05312bd5"))));
		assertEquals(1, Util.getResultsSize(results));
		assertEquals(expectedOrderUuid, PropertyUtils.getProperty(Util.getResultsList(results).get(0), "uuid"));
	}
	
	@Test
	public void shouldGetAllInActiveOrdersForAPatient() throws Exception {
		SimpleObject results = deserialize(handle(newGetRequest(getURI(), new Parameter("patient",
		        "da7f524f-27ce-4bb2-86d6-6d1d05312bd5"), new Parameter("status", "inactive"), new Parameter("careSetting",
		        RestTestConstants1_10.CARE_SETTING_UUID), new Parameter("asOfDate", "2007-12-10"))));
		assertEquals(0, Util.getResultsSize(results));
		
		String orderUuid = "dfca4077-493c-496b-8312-856ee5d1cc26";
		results = deserialize(handle(newGetRequest(getURI(),
		    new Parameter("patient", "da7f524f-27ce-4bb2-86d6-6d1d05312bd5"), new Parameter("status", "inactive"),
		    new Parameter("careSetting", RestTestConstants1_10.CARE_SETTING_UUID), new Parameter("asOfDate",
		            "2007-12-10 00:00:01"))));
		assertEquals(1, Util.getResultsSize(results));
		assertEquals(orderUuid, PropertyUtils.getProperty(Util.getResultsList(results).get(0), "uuid"));
		
		results = deserialize(handle(newGetRequest(getURI(),
		    new Parameter("patient", "da7f524f-27ce-4bb2-86d6-6d1d05312bd5"), new Parameter("status", "inactive"),
		    new Parameter("careSetting", RestTestConstants1_10.CARE_SETTING_UUID), new Parameter("asOfDate", "2007-12-17"))));
		assertEquals(1, Util.getResultsSize(results));
		assertEquals(orderUuid, PropertyUtils.getProperty(Util.getResultsList(results).get(0), "uuid"));
		
		results = deserialize(handle(newGetRequest(getURI(),
		    new Parameter("patient", "da7f524f-27ce-4bb2-86d6-6d1d05312bd5"), new Parameter("status", "inactive"),
		    new Parameter("careSetting", RestTestConstants1_10.CARE_SETTING_UUID), new Parameter("asOfDate",
		            "2007-12-17 00:00:01"))));
		assertEquals(2, Util.getResultsSize(results));
		
		String[] expectedOrderUuids = new String[] { orderUuid, "4c96f25c-4949-4f72-9931-d808fbc226dh" };
		List<Object> resultList = Util.getResultsList(results);
		List<String> uuids = Arrays.asList(new String[] { PropertyUtils.getProperty(resultList.get(0), "uuid").toString(),
		        PropertyUtils.getProperty(resultList.get(1), "uuid").toString() });
		assertThat(uuids, hasItems(expectedOrderUuids));
	}
	
	@Test
	public void shouldGetAllOrdersForAPatientInTheSpecifiedCareSetting() throws Exception {
		SimpleObject results = deserialize(handle(newGetRequest(getURI(), new Parameter("patient",
		        "da7f524f-27ce-4bb2-86d6-6d1d05312bd5"), new Parameter("status", "any"), new Parameter("careSetting",
		        RestTestConstants1_10.CARE_SETTING_UUID))));
		assertEquals(8, Util.getResultsSize(results));
	}
	
	@Test
	public void shouldGetAllOrdersForAPatientInTheSpecifiedCareSettingIncludingVoidedOnesIfRequested() throws Exception {
		SimpleObject results = deserialize(handle(newGetRequest(getURI(), new Parameter("patient",
		        "da7f524f-27ce-4bb2-86d6-6d1d05312bd5"), new Parameter("status", "any"), new Parameter("careSetting",
		        RestTestConstants1_10.CARE_SETTING_UUID), new Parameter("includeAll", "true"))));
		assertEquals(9, Util.getResultsSize(results));
	}
	
	@Test
	public void shouldGetOrdersByOrderType() throws Exception {
		// add a new drug order
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
		
		MockHttpServletRequest req = newPostRequest(getURI(), order);
		SimpleObject newDrug = deserialize(handle(req));
		
		List<Order> activeDrugOrders = orderService.getActiveOrders(patient, orderService.getOrderTypeByName("Drug order"),
		    outPatient, null);
		int activeDrugOrderCount = activeDrugOrders.size();
		
		// add a new test order
		order = new SimpleObject();
		order.add("type", "testorder");
		order.add("patient", PATIENT_UUID);
		final String cd4CountUuid = "a09ab2c5-878e-4905-b25d-5784167d0216";
		order.add("concept", cd4CountUuid);
		order.add("careSetting", RestTestConstants1_10.CARE_SETTING_UUID);
		order.add("encounter", "e403fafb-e5e4-42d0-9d11-4f52e89d148c");
		order.add("orderer", "c2299800-cca9-11e0-9572-0800200c9a66");
		order.add("clinicalHistory", "Patient had a negative reaction to the test in the past");
		String onceUuid = "38090760-7c38-11e4-baa7-0800200c9a67";
		order.add("frequency", onceUuid);
		order.add("specimenSource", "e10ffe54-5184-4efe-8960-cd565ec1cdf8");
		order.add("numberOfRepeats", "3");
		req = newPostRequest(getURI(), order);
		SimpleObject newTest = deserialize(handle(req));
		
		List<Order> activeTestOrders = orderService.getActiveOrders(patient, orderService.getOrderTypeByName("Test order"),
		    outPatient, null);
		int activeTestOrderCount = activeTestOrders.size();
		
		// order service should return all orders when no order type filter specified
		req = newGetRequest(getURI(),
		    new Parameter("patient", PATIENT_UUID)
		        );
		SimpleObject orders = deserialize(handle(req));
		ArrayList<Object> resp = (ArrayList<Object>) PropertyUtils.getProperty(orders, "results");
		assertEquals(activeTestOrderCount + activeDrugOrderCount, resp.size());
		
		// order service should filter by test order type
		req = newGetRequest(getURI(),
		    new Parameter("patient", PATIENT_UUID),
		    new Parameter("orderType", RestTestConstants1_10.TEST_ORDER_TYPE_UUID)
		        );
		orders = deserialize(handle(req));
		resp = (ArrayList<Object>) PropertyUtils.getProperty(orders, "results");
		assertEquals(activeTestOrderCount, resp.size());
	}
	
	@Test(expected = ObjectNotFoundException.class)
	public void invalidCareCenterShouldThrowException() throws Exception {
		MockHttpServletRequest req = newGetRequest(getURI(),
		    new Parameter("patient", PATIENT_UUID),
		    new Parameter("careSetting", "FAKE-CARE-SETTING-UUID")
		        );
		handle(req);
	}
}

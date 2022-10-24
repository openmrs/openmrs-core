/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api;

import org.apache.commons.lang3.time.DateUtils;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openmrs.Allergy;
import org.openmrs.CareSetting;
import org.openmrs.Concept;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptDatatype;
import org.openmrs.ConceptDescription;
import org.openmrs.ConceptMap;
import org.openmrs.ConceptMapType;
import org.openmrs.ConceptName;
import org.openmrs.ConceptReferenceTerm;
import org.openmrs.Condition;
import org.openmrs.Diagnosis;
import org.openmrs.Drug;
import org.openmrs.DrugOrder;
import org.openmrs.Encounter;
import org.openmrs.FreeTextDosingInstructions;
import org.openmrs.GlobalProperty;
import org.openmrs.MedicationDispense;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.Order.Action;
import org.openmrs.OrderAttribute;
import org.openmrs.OrderAttributeType;
import org.openmrs.OrderFrequency;
import org.openmrs.OrderGroup;
import org.openmrs.OrderGroupAttribute;
import org.openmrs.OrderGroupAttributeType;
import org.openmrs.OrderSet;
import org.openmrs.OrderType;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.ProviderAttributeType;
import org.openmrs.SimpleDosingInstructions;
import org.openmrs.TestOrder;
import org.openmrs.Visit;
import org.openmrs.VisitAttributeType;
import org.openmrs.api.builder.DrugOrderBuilder;
import org.openmrs.api.builder.OrderBuilder;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.hibernate.HibernateAdministrationDAO;
import org.openmrs.api.db.hibernate.HibernateSessionFactoryBean;
import org.openmrs.api.impl.OrderServiceImpl;
import org.openmrs.customdatatype.datatype.FreeTextDatatype;
import org.openmrs.messagesource.MessageSourceService;
import org.openmrs.order.OrderUtil;
import org.openmrs.order.OrderUtilTest;
import org.openmrs.orders.TimestampOrderNumberGenerator;
import org.openmrs.parameter.OrderSearchCriteria;
import org.openmrs.parameter.OrderSearchCriteriaBuilder;
import org.openmrs.test.TestUtil;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.openmrs.util.DateUtil;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.PrivilegeConstants;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.openmrs.Order.Action.DISCONTINUE;
import static org.openmrs.Order.FulfillerStatus.COMPLETED;
import static org.openmrs.test.OpenmrsMatchers.hasId;
import static org.openmrs.test.TestUtil.containsId;

/**
 * TODO clean up and test all methods in OrderService
 */
public class OrderServiceTest extends BaseContextSensitiveTest {

	private static final String OTHER_ORDER_FREQUENCIES_XML = "org/openmrs/api/include/OrderServiceTest-otherOrderFrequencies.xml";

	protected static final String ORDER_SET = "org/openmrs/api/include/OrderSetServiceTest-general.xml";

	private static final String ORDER_GROUP_ATTRIBUTES = "org/openmrs/api/include/OrderServiceTest-createOrderGroupAttributes.xml";

	private static final String ORDER_ATTRIBUTES = "org/openmrs/api/include/OrderServiceTest-createOrderAttributes.xml";

	@Autowired
	private ConceptService conceptService;

	@Autowired
	private OrderService orderService;

	@Autowired
	private PatientService patientService;

	@Autowired
	private EncounterService encounterService;

	@Autowired
	private ProviderService providerService;

	@Autowired
	private AdministrationService adminService;

	@Autowired
	private OrderSetService orderSetService;

	@Autowired
	private MessageSourceService messageSourceService;
	
	@BeforeEach
	public void setUp(){
		executeDataSet(ORDER_ATTRIBUTES);
		executeDataSet(ORDER_GROUP_ATTRIBUTES);
	}

	public class SomeTestOrder extends TestOrder {}
	

	/**
	 * @see OrderService#saveOrder(org.openmrs.Order, OrderContext)
	 */
	@Test
	public void saveOrder_shouldNotSaveOrderIfOrderDoesntValidate() {
		Order order = new Order();
		order.setPatient(null);
		order.setOrderer(null);
		APIException exception = assertThrows(APIException.class, () -> orderService.saveOrder(order, null));
		assertThat(exception.getMessage(), containsString("failed to validate with reason:"));
	}

	/**
	 * @see OrderService#getOrderByUuid(String)
	 */
	@Test
	public void getOrderByUuid_shouldFindObjectGivenValidUuid() {
		String uuid = "921de0a3-05c4-444a-be03-e01b4c4b9142";
		Order order = orderService.getOrderByUuid(uuid);
		assertEquals(1, (int) order.getOrderId());
	}

	/**
	 * @see OrderService#getOrderByUuid(String)
	 */
	@Test
	public void getOrderByUuid_shouldReturnNullIfNoObjectFoundWithGivenUuid() {
		assertNull(orderService.getOrderByUuid("some invalid uuid"));
	}

	/**
	 * @see OrderService#purgeOrder(org.openmrs.Order, boolean)
	 */
	@Test
	public void purgeOrder_shouldDeleteAnyObsAssociatedToTheOrderWhenCascadeIsTrue() {
		executeDataSet("org/openmrs/api/include/OrderServiceTest-deleteObsThatReference.xml");
		final String ordUuid = "0c96f25c-4949-4f72-9931-d808fbcdb612";
		final String obsUuid = "be3a4d7a-f9ab-47bb-aaad-bc0b452fcda4";
		ObsService os = Context.getObsService();

		Obs obs = os.getObsByUuid(obsUuid);
		assertNotNull(obs);

		Order order = orderService.getOrderByUuid(ordUuid);
		assertNotNull(order);

		//sanity check to ensure that the obs and order are actually related
		assertEquals(order, obs.getOrder());

		//Ensure that passing false does not delete the related obs
		orderService.purgeOrder(order, false);
		assertNotNull(os.getObsByUuid(obsUuid));

		orderService.purgeOrder(order, true);

		//Ensure that actually the order got purged
		assertNull(orderService.getOrderByUuid(ordUuid));

		//Ensure that the related obs got deleted
		assertNull(os.getObsByUuid(obsUuid));
	}

	/**
	 * @see OrderService#purgeOrder(org.openmrs.Order, boolean)
	 */
	@Test
	public void purgeOrder_shouldDeleteOrderFromTheDatabase() {
		final String uuid = "9c21e407-697b-11e3-bd76-0800271c1b75";
		Order order = orderService.getOrderByUuid(uuid);
		assertNotNull(order);
		orderService.purgeOrder(order);
		assertNull(orderService.getOrderByUuid(uuid));
	}

	/**
	 * @throws InterruptedException
	 * @see OrderNumberGenerator#getNewOrderNumber(OrderContext)
	 */
	@Test
	public void getNewOrderNumber_shouldAlwaysReturnUniqueOrderNumbersWhenCalledMultipleTimesWithoutSavingOrders()
		throws InterruptedException {

		int N = 50;
		final Set<String> uniqueOrderNumbers = new HashSet<>(50);
		List<Thread> threads = new ArrayList<>();
		for (int i = 0; i < N; i++) {
			threads.add(new Thread(() -> {
				try {
					Context.openSession();
					Context.addProxyPrivilege(PrivilegeConstants.ADD_ORDERS);
					uniqueOrderNumbers.add(((OrderNumberGenerator) orderService).getNewOrderNumber(null));
				} finally {
					Context.removeProxyPrivilege(PrivilegeConstants.ADD_ORDERS);
					Context.closeSession();
				}
			}));
		}
		for (int i = 0; i < N; ++i) {
			threads.get(i).start();
		}
		for (int i = 0; i < N; ++i) {
			threads.get(i).join();
		}
		//since we used a set we should have the size as N indicating that there were no duplicates
		assertEquals(N, uniqueOrderNumbers.size());
	}

	/**
	 * @see OrderService#getOrderByOrderNumber(String)
	 */
	@Test
	public void getOrderByOrderNumber_shouldFindObjectGivenValidOrderNumber() {
		Order order = orderService.getOrderByOrderNumber("1");
		assertNotNull(order);
		assertEquals(1, (int) order.getOrderId());
	}

	/**
	 * @see OrderService#getOrderByOrderNumber(String)
	 */
	@Test
	public void getOrderByOrderNumber_shouldReturnNullIfNoObjectFoundWithGivenOrderNumber() {
		assertNull(orderService.getOrderByOrderNumber("some invalid order number"));
	}

	/**
	 * @see OrderService#getOrderHistoryByConcept(Patient, Concept)
	 */
	@Test
	public void getOrderHistoryByConcept_shouldReturnOrdersWithTheGivenConcept() {
		//We should have two orders with this concept.
		Concept concept = Context.getConceptService().getConcept(88);
		Patient patient = Context.getPatientService().getPatient(2);
		List<Order> orders = orderService.getOrderHistoryByConcept(patient, concept);

		//They must be sorted by dateActivated starting with the latest
		assertEquals(3, orders.size());
		assertEquals(444, orders.get(0).getOrderId().intValue());
		assertEquals(44, orders.get(1).getOrderId().intValue());
		assertEquals(4, orders.get(2).getOrderId().intValue());

		concept = Context.getConceptService().getConcept(792);
		orders = orderService.getOrderHistoryByConcept(patient, concept);

		//They must be sorted by dateActivated starting with the latest
		assertEquals(4, orders.size());
		assertEquals(3, orders.get(0).getOrderId().intValue());
		assertEquals(222, orders.get(1).getOrderId().intValue());
		assertEquals(22, orders.get(2).getOrderId().intValue());
		assertEquals(2, orders.get(3).getOrderId().intValue());
	}

	/**
	 * @see OrderService#getOrderHistoryByConcept(Patient, Concept)
	 */
	@Test
	public void getOrderHistoryByConcept_shouldReturnEmptyListForConceptWithoutOrders() {
		Concept concept = Context.getConceptService().getConcept(21);
		Patient patient = Context.getPatientService().getPatient(2);
		List<Order> orders = orderService.getOrderHistoryByConcept(patient, concept);
		assertEquals(0, orders.size());
	}

	/**
	 * @see OrderService#getOrderHistoryByConcept(org.openmrs.Patient, org.openmrs.Concept)
	 */
	@Test
	public void getOrderHistoryByConcept_shouldRejectANullConcept() {
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> orderService.getOrderHistoryByConcept(new Patient(), null));
		assertThat(exception.getMessage(), is("patient and concept are required"));
	}

	/**
	 * @see OrderService#getOrderHistoryByConcept(org.openmrs.Patient, org.openmrs.Concept)
	 */
	@Test
	public void getOrderHistoryByConcept_shouldRejectANullPatient() {
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> orderService.getOrderHistoryByConcept(null, new Concept()));
		assertThat(exception.getMessage(), is("patient and concept are required"));
	}

	/**
	 * @see OrderService#getOrderHistoryByOrderNumber(String)
	 */
	@Test
	public void getOrderHistoryByOrderNumber_shouldReturnAllOrderHistoryForGivenOrderNumber() {
		List<Order> orders = orderService.getOrderHistoryByOrderNumber("111");
		assertEquals(2, orders.size());
		assertEquals(111, orders.get(0).getOrderId().intValue());
		assertEquals(1, orders.get(1).getOrderId().intValue());
	}

	/**
	 * @see OrderService#getOrderFrequency(Integer)
	 */
	@Test
	public void getOrderFrequency_shouldReturnTheOrderFrequencyThatMatchesTheSpecifiedId() {
		assertEquals("28090760-7c38-11e3-baa7-0800200c9a66", orderService.getOrderFrequency(1).getUuid());
	}

	/**
	 * @see OrderService#getOrderFrequencyByUuid(String)
	 */
	@Test
	public void getOrderFrequencyByUuid_shouldReturnTheOrderFrequencyThatMatchesTheSpecifiedUuid() {
		assertEquals(1, orderService.getOrderFrequencyByUuid("28090760-7c38-11e3-baa7-0800200c9a66").getOrderFrequencyId()
			.intValue());
	}

	/**
	 * @see OrderService#getOrderFrequencyByConcept(org.openmrs.Concept)
	 */
	@Test
	public void getOrderFrequencyByConcept_shouldReturnTheOrderFrequencyThatMatchesTheSpecifiedConcept() {
		Concept concept = conceptService.getConcept(4);
		assertEquals(3, orderService.getOrderFrequencyByConcept(concept).getOrderFrequencyId().intValue());
	}

	/**
	 * @see OrderService#getOrderFrequencies(boolean)
	 */
	@Test
	public void getOrderFrequencies_shouldReturnOnlyNonRetiredOrderFrequenciesIfIncludeRetiredIsSetToFalse() {
		List<OrderFrequency> orderFrequencies = orderService.getOrderFrequencies(false);
		assertEquals(2, orderFrequencies.size());
		assertTrue(containsId(orderFrequencies, 1));
		assertTrue(containsId(orderFrequencies, 2));
	}

	/**
	 * @see OrderService#getOrderFrequencies(boolean)
	 */
	@Test
	public void getOrderFrequencies_shouldReturnAllTheOrderFrequenciesIfIncludeRetiredIsSetToTrue() {
		List<OrderFrequency> orderFrequencies = orderService.getOrderFrequencies(true);
		assertEquals(3, orderFrequencies.size());
		assertTrue(containsId(orderFrequencies, 1));
		assertTrue(containsId(orderFrequencies, 2));
		assertTrue(containsId(orderFrequencies, 3));
	}

	/**
	 * @see OrderService#getActiveOrders(org.openmrs.Patient, org.openmrs.OrderType,
	 * org.openmrs.CareSetting, java.util.Date)
	 */
	@Test
	public void getActiveOrders_shouldReturnAllActiveOrdersForTheSpecifiedPatient() {
		Patient patient = Context.getPatientService().getPatient(2);
		List<Order> orders = orderService.getActiveOrders(patient, null, null, null);
		assertEquals(5, orders.size());
		Order[] expectedOrders = {orderService.getOrder(222), orderService.getOrder(3), orderService.getOrder(444),
			orderService.getOrder(5), orderService.getOrder(7)};
		assertThat(orders, hasItems(expectedOrders));

		assertTrue(OrderUtilTest.isActiveOrder(orders.get(0), null));
		assertTrue(OrderUtilTest.isActiveOrder(orders.get(1), null));
		assertTrue(OrderUtilTest.isActiveOrder(orders.get(2), null));
		assertTrue(OrderUtilTest.isActiveOrder(orders.get(3), null));
		assertTrue(OrderUtilTest.isActiveOrder(orders.get(4), null));
	}

	/**
	 * @see OrderService#getActiveOrders(org.openmrs.Patient, org.openmrs.OrderType,
	 * org.openmrs.CareSetting, java.util.Date)
	 */
	@Test
	public void getActiveOrders_shouldReturnAllActiveOrdersForTheSpecifiedPatientAndCareSetting() {
		Patient patient = patientService.getPatient(2);
		CareSetting careSetting = orderService.getCareSetting(1);
		List<Order> orders = orderService.getActiveOrders(patient, null, careSetting, null);
		assertEquals(4, orders.size());
		Order[] expectedOrders = {orderService.getOrder(3), orderService.getOrder(444), orderService.getOrder(5),
			orderService.getOrder(7)};
		assertThat(orders, hasItems(expectedOrders));
	}

	/**
	 * @see OrderService#getActiveOrders(org.openmrs.Patient, org.openmrs.OrderType,
	 * org.openmrs.CareSetting, java.util.Date)
	 */
	@Test
	public void getActiveOrders_shouldReturnAllActiveDrugOrdersForTheSpecifiedPatient() {
		Patient patient = patientService.getPatient(2);
		List<Order> orders = orderService.getActiveOrders(patient, orderService.getOrderType(1), null, null);
		assertEquals(4, orders.size());
		Order[] expectedOrders = {orderService.getOrder(222), orderService.getOrder(3), orderService.getOrder(444),
			orderService.getOrder(5)};
		assertThat(orders, hasItems(expectedOrders));
	}

	/**
	 * @see OrderService#getActiveOrders(org.openmrs.Patient, org.openmrs.OrderType,
	 * org.openmrs.CareSetting, java.util.Date)
	 */
	@Test
	public void getActiveOrders_shouldReturnAllActiveTestOrdersForTheSpecifiedPatient() {
		Patient patient = patientService.getPatient(2);
		List<Order> orders = orderService
			.getActiveOrders(patient, orderService.getOrderTypeByName("Test order"), null, null);
		assertEquals(1, orders.size());
		assertEquals(orders.get(0), orderService.getOrder(7));
	}

	/**
	 * @see OrderService#getActiveOrders(org.openmrs.Patient, org.openmrs.OrderType,
	 * org.openmrs.CareSetting, java.util.Date)
	 */
	@Test
	public void getActiveOrders_shouldFailIfPatientIsNull() {
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> orderService.getActiveOrders(null, null, orderService.getCareSetting(1), null));
		assertThat(exception.getMessage(), is("Patient is required when fetching active orders"));
	}

	/**
	 * @throws ParseException
	 * @see OrderService#getActiveOrders(org.openmrs.Patient, org.openmrs.OrderType,
	 * org.openmrs.CareSetting, java.util.Date)
	 */
	@Test
	public void getActiveOrders_shouldReturnActiveOrdersAsOfTheSpecifiedDate() throws ParseException {
		Patient patient = Context.getPatientService().getPatient(2);
		List<Order> orders = orderService.getAllOrdersByPatient(patient);
		assertEquals(12, orders.size());

		Date asOfDate = Context.getDateFormat().parse("10/12/2007");
		orders = orderService.getActiveOrders(patient, null, null, asOfDate);
		assertEquals(2, orders.size());
		assertFalse(orders.contains(orderService.getOrder(22)));//DC
		assertFalse(orders.contains(orderService.getOrder(44)));//DC
		assertFalse(orders.contains(orderService.getOrder(8)));//voided

		Order[] expectedOrders = {orderService.getOrder(9)};

		asOfDate = Context.getDateTimeFormat().parse("10/12/2007 00:01:00");
		orders = orderService.getActiveOrders(patient, null, null, asOfDate);
		assertEquals(1, orders.size());
		assertThat(orders, hasItems(expectedOrders));

		Order[] expectedOrders1 = {orderService.getOrder(3), orderService.getOrder(4), orderService.getOrder(222)};

		asOfDate = Context.getDateFormat().parse("10/04/2008");
		orders = orderService.getActiveOrders(patient, null, null, asOfDate);
		assertEquals(3, orders.size());
		assertThat(orders, hasItems(expectedOrders1));

		asOfDate = Context.getDateTimeFormat().parse("10/04/2008 00:01:00");
		orders = orderService.getActiveOrders(patient, null, null, asOfDate);
		assertEquals(2, orders.size());
		Order[] expectedOrders2 = {orderService.getOrder(222), orderService.getOrder(3)};
		assertThat(orders, hasItems(expectedOrders2));

		Order[] expectedOrders3 = {orderService.getOrder(222), orderService.getOrder(3), orderService.getOrder(444),
			orderService.getOrder(5), orderService.getOrder(6)};
		asOfDate = Context.getDateTimeFormat().parse("26/09/2008 09:24:10");
		orders = orderService.getActiveOrders(patient, null, null, asOfDate);
		assertEquals(5, orders.size());
		assertThat(orders, hasItems(expectedOrders3));

		asOfDate = Context.getDateTimeFormat().parse("26/09/2008 09:25:10");
		orders = orderService.getActiveOrders(patient, null, null, asOfDate);
		assertEquals(4, orders.size());
		Order[] expectedOrders4 = {orderService.getOrder(222), orderService.getOrder(3), orderService.getOrder(444),
			orderService.getOrder(5)};
		assertThat(orders, hasItems(expectedOrders4));

		asOfDate = Context.getDateFormat().parse("04/12/2008");
		orders = orderService.getActiveOrders(patient, null, null, asOfDate);
		assertEquals(5, orders.size());
		Order[] expectedOrders5 = {orderService.getOrder(222), orderService.getOrder(3), orderService.getOrder(444),
			orderService.getOrder(5), orderService.getOrder(7)};
		assertThat(orders, hasItems(expectedOrders5));

		asOfDate = Context.getDateFormat().parse("06/12/2008");
		orders = orderService.getActiveOrders(patient, null, null, asOfDate);
		assertEquals(5, orders.size());
		assertThat(orders, hasItems(expectedOrders5));
	}

	/**
	 * @see OrderService#getActiveOrders(org.openmrs.Patient, org.openmrs.OrderType,
	 * org.openmrs.CareSetting, java.util.Date)
	 */
	@Test
	public void getActiveOrders_shouldReturnAllOrdersIfNoOrderTypeIsSpecified() {
		Patient patient = Context.getPatientService().getPatient(2);
		List<Order> orders = orderService.getActiveOrders(patient, null, null, null);
		assertEquals(5, orders.size());
		Order[] expectedOrders = {orderService.getOrder(222), orderService.getOrder(3), orderService.getOrder(444),
			orderService.getOrder(5), orderService.getOrder(7)};
		assertThat(orders, hasItems(expectedOrders));
	}

	/**
	 * @see OrderService#getActiveOrders(org.openmrs.Patient, org.openmrs.OrderType,
	 * org.openmrs.CareSetting, java.util.Date)
	 */
	@Test
	public void getActiveOrders_shouldIncludeOrdersForSubTypesIfOrderTypeIsSpecified() {
		executeDataSet("org/openmrs/api/include/OrderServiceTest-otherOrders.xml");
		Patient patient = Context.getPatientService().getPatient(2);
		OrderType testOrderType = orderService.getOrderType(2);
		List<Order> orders = orderService.getActiveOrders(patient, testOrderType, null, null);
		assertEquals(5, orders.size());
		Order[] expectedOrder1 = {orderService.getOrder(7), orderService.getOrder(101), orderService.getOrder(102),
			orderService.getOrder(103), orderService.getOrder(104)};
		assertThat(orders, hasItems(expectedOrder1));

		OrderType labTestOrderType = orderService.getOrderType(7);
		orders = orderService.getActiveOrders(patient, labTestOrderType, null, null);
		assertEquals(3, orders.size());
		Order[] expectedOrder2 = {orderService.getOrder(101), orderService.getOrder(103), orderService.getOrder(104)};
		assertThat(orders, hasItems(expectedOrder2));
	}

	/**
	 * @see OrderService#discontinueOrder(org.openmrs.Order, String, java.util.Date,
	 * org.openmrs.Provider, org.openmrs.Encounter)
	 */
	@Test
	public void discontinueOrder_shouldPopulateCorrectAttributesOnTheDiscontinueAndDiscontinuedOrders() {
		Order order = orderService.getOrderByOrderNumber("111");
		Encounter encounter = encounterService.getEncounter(3);
		Provider orderer = providerService.getProvider(1);
		assertTrue(OrderUtilTest.isActiveOrder(order, null));
		Date discontinueDate = new Date();
		String discontinueReasonNonCoded = "Test if I can discontinue this";

		Order discontinueOrder = orderService.discontinueOrder(order, discontinueReasonNonCoded, discontinueDate, orderer,
			encounter);

		assertEquals(order.getDateStopped(), discontinueDate);
		assertNotNull(discontinueOrder);
		assertNotNull(discontinueOrder.getId());
		assertEquals(discontinueOrder.getDateActivated(), discontinueOrder.getAutoExpireDate());
		assertEquals(discontinueOrder.getAction(), Action.DISCONTINUE);
		assertEquals(discontinueOrder.getOrderReasonNonCoded(), discontinueReasonNonCoded);
		assertEquals(discontinueOrder.getPreviousOrder(), order);
	}

	/**
	 * @see OrderService#discontinueOrder(Order, String, Date, Provider, Encounter)
	 */
	@Test
	public void discontinueOrder_shouldPassForAnActiveOrderWhichIsScheduledAndNotStartedAsOfDiscontinueDate() {
		Order order = new Order();
		order.setAction(Action.NEW);
		order.setPatient(Context.getPatientService().getPatient(7));
		order.setConcept(Context.getConceptService().getConcept(5497));
		order.setCareSetting(orderService.getCareSetting(1));
		order.setOrderer(orderService.getOrder(1).getOrderer());
		order.setEncounter(encounterService.getEncounter(3));
		order.setEncounter(encounterService.getEncounter(3));
		order.setOrderType(orderService.getOrderType(17));
		order.setDateActivated(new Date());
		order.setScheduledDate(DateUtils.addMonths(new Date(), 2));
		order.setUrgency(Order.Urgency.ON_SCHEDULED_DATE);
		order = orderService.saveOrder(order, null);

		assertTrue(OrderUtilTest.isActiveOrder(order, null));
		assertFalse(order.isStarted());

		Encounter encounter = encounterService.getEncounter(3);
		Provider orderer = providerService.getProvider(1);
		Date discontinueDate = new Date();
		String discontinueReasonNonCoded = "Test if I can discontinue this";

		Order discontinueOrder = orderService.discontinueOrder(order, discontinueReasonNonCoded, discontinueDate, orderer,
			encounter);

		assertEquals(order.getDateStopped(), discontinueDate);
		assertNotNull(discontinueOrder);
		assertNotNull(discontinueOrder.getId());
		assertEquals(discontinueOrder.getDateActivated(), discontinueOrder.getAutoExpireDate());
		assertEquals(discontinueOrder.getAction(), Action.DISCONTINUE);
		assertEquals(discontinueOrder.getOrderReasonNonCoded(), discontinueReasonNonCoded);
		assertEquals(discontinueOrder.getPreviousOrder(), order);
	}

	/**
	 * @see OrderService#discontinueOrder(org.openmrs.Order, org.openmrs.Concept, java.util.Date,
	 * org.openmrs.Provider, org.openmrs.Encounter)
	 */
	@Test
	public void discontinueOrder_shouldSetCorrectAttributesOnTheDiscontinueAndDiscontinuedOrders() {
		executeDataSet("org/openmrs/api/include/OrderServiceTest-discontinueReason.xml");

		Order order = orderService.getOrderByOrderNumber("111");
		Encounter encounter = encounterService.getEncounter(3);
		Provider orderer = providerService.getProvider(1);
		Date discontinueDate = new Date();
		Concept concept = Context.getConceptService().getConcept(1);

		Order discontinueOrder = orderService.discontinueOrder(order, concept, discontinueDate, orderer, encounter);

		assertEquals(order.getDateStopped(), discontinueDate);
		assertNotNull(discontinueOrder);
		assertNotNull(discontinueOrder.getId());
		assertEquals(discontinueOrder.getDateActivated(), discontinueOrder.getAutoExpireDate());
		assertEquals(discontinueOrder.getAction(), Action.DISCONTINUE);
		assertEquals(discontinueOrder.getOrderReason(), concept);
		assertEquals(discontinueOrder.getPreviousOrder(), order);
	}

	/**
	 * @see OrderService#discontinueOrder(Order, Concept, Date, Provider, Encounter)
	 */
	@Test
	public void discontinueOrder_shouldPassForAnActiveOrderWhichIsScheduledAndNotStartedAsOfDiscontinueDateWithParamConcept() {
		Order order = new Order();
		order.setAction(Action.NEW);
		order.setPatient(Context.getPatientService().getPatient(7));
		order.setConcept(Context.getConceptService().getConcept(5497));
		order.setCareSetting(orderService.getCareSetting(1));
		order.setOrderer(orderService.getOrder(1).getOrderer());
		order.setEncounter(encounterService.getEncounter(3));
		order.setEncounter(encounterService.getEncounter(3));
		order.setOrderType(orderService.getOrderType(17));
		order.setDateActivated(new Date());
		order.setScheduledDate(DateUtils.addMonths(new Date(), 2));
		order.setUrgency(Order.Urgency.ON_SCHEDULED_DATE);
		order = orderService.saveOrder(order, null);

		assertTrue(OrderUtilTest.isActiveOrder(order, null));
		assertFalse(order.isStarted());

		Encounter encounter = encounterService.getEncounter(3);
		Provider orderer = providerService.getProvider(1);
		Date discontinueDate = new Date();
		Concept concept = Context.getConceptService().getConcept(1);

		Order discontinueOrder = orderService.discontinueOrder(order, concept, discontinueDate, orderer, encounter);

		assertEquals(order.getDateStopped(), discontinueDate);
		assertNotNull(discontinueOrder);
		assertNotNull(discontinueOrder.getId());
		assertEquals(discontinueOrder.getDateActivated(), discontinueOrder.getAutoExpireDate());
		assertEquals(discontinueOrder.getAction(), Action.DISCONTINUE);
		assertEquals(discontinueOrder.getOrderReason(), concept);
		assertEquals(discontinueOrder.getPreviousOrder(), order);
	}

	/**
	 * @see OrderService#discontinueOrder(org.openmrs.Order, String, java.util.Date,
	 * org.openmrs.Provider, org.openmrs.Encounter)
	 */
	@Test
	public void discontinueOrder_shouldFailForADiscontinuationOrder() {
		executeDataSet("org/openmrs/api/include/OrderServiceTest-discontinuedOrder.xml");
		Order discontinuationOrder = orderService.getOrder(26);
		assertEquals(Action.DISCONTINUE, discontinuationOrder.getAction());
		Encounter encounter = encounterService.getEncounter(3);
		CannotStopDiscontinuationOrderException exception = assertThrows(CannotStopDiscontinuationOrderException.class, () -> orderService.discontinueOrder(discontinuationOrder, "Test if I can discontinue this", null, null, encounter));
		assertThat(exception.getMessage(), is(messageSourceService.getMessage("Order.action.cannot.discontinue")));
	}

	/**
	 * @see OrderService#discontinueOrder(org.openmrs.Order, org.openmrs.Concept, java.util.Date,
	 * org.openmrs.Provider, org.openmrs.Encounter)
	 */
	@Test
	public void discontinueOrder_shouldNotPassForADiscontinuationOrder() {
		executeDataSet("org/openmrs/api/include/OrderServiceTest-discontinuedOrder.xml");
		executeDataSet("org/openmrs/api/include/OrderServiceTest-discontinueReason.xml");
		Order discontinuationOrder = orderService.getOrder(26);
		assertEquals(Action.DISCONTINUE, discontinuationOrder.getAction());
		Encounter encounter = encounterService.getEncounter(3);
		CannotStopDiscontinuationOrderException exception = assertThrows(CannotStopDiscontinuationOrderException.class, () -> orderService.discontinueOrder(discontinuationOrder, (Concept) null, null, null, encounter));
		assertThat(exception.getMessage(), is(messageSourceService.getMessage("Order.action.cannot.discontinue")));
	}

	/**
	 * @see OrderService#discontinueOrder(org.openmrs.Order, String, java.util.Date,
	 * org.openmrs.Provider, org.openmrs.Encounter)
	 */
	@Test
	public void discontinueOrder_shouldFailForADiscontinuedOrder() {
		Order discontinuationOrder = orderService.getOrder(2);
		assertFalse(discontinuationOrder.isActive());
		assertNotNull(discontinuationOrder.getDateStopped());
		Encounter encounter = encounterService.getEncounter(3);
		CannotStopInactiveOrderException exception = assertThrows(CannotStopInactiveOrderException.class, () -> orderService.discontinueOrder(discontinuationOrder, "some reason", null, null, encounter));
		assertThat(exception.getMessage(), is(messageSourceService.getMessage("Order.cannot.discontinue.inactive")));
	}

	/**
	 * @see OrderService#discontinueOrder(org.openmrs.Order, org.openmrs.Concept, java.util.Date,
	 * org.openmrs.Provider, org.openmrs.Encounter)
	 */
	@Test
	public void discontinueOrder_shouldNotPassForADiscontinuedOrder() {
		Order discontinuationOrder = orderService.getOrder(2);
		assertFalse(discontinuationOrder.isActive());
		assertNotNull(discontinuationOrder.getDateStopped());
		Encounter encounter = encounterService.getEncounter(3);
		CannotStopInactiveOrderException exception = assertThrows(CannotStopInactiveOrderException.class, () -> orderService.discontinueOrder(discontinuationOrder, (Concept) null, null, null, encounter));
		assertThat(exception.getMessage(), is(messageSourceService.getMessage("Order.cannot.discontinue.inactive")));
	}

	/**
	 * @see OrderService#saveOrder(org.openmrs.Order, OrderContext)
	 */
	@Test
	public void saveOrder_shouldDiscontinueExistingActiveOrderIfNewOrderBeingSavedWithActionToDiscontinue() {
		DrugOrder order = new DrugOrder();
		order.setAction(Order.Action.DISCONTINUE);
		order.setOrderReasonNonCoded("Discontinue this");
		order.setDrug(conceptService.getDrug(3));
		order.setEncounter(encounterService.getEncounter(5));
		order.setPatient(patientService.getPatient(7));
		order.setOrderer(providerService.getProvider(1));
		order.setCareSetting(orderService.getCareSetting(1));
		order.setEncounter(encounterService.getEncounter(3));
		order.setOrderType(orderService.getOrderType(1));
		order.setDateActivated(new Date());
		order.setDosingType(SimpleDosingInstructions.class);
		order.setDose(500.0);
		order.setDoseUnits(conceptService.getConcept(50));
		order.setFrequency(orderService.getOrderFrequency(1));
		order.setRoute(conceptService.getConcept(22));
		order.setNumRefills(10);
		order.setQuantity(20.0);
		order.setQuantityUnits(conceptService.getConcept(51));

		//We are trying to discontinue order id 111 in standardTestDataset.xml
		Order expectedPreviousOrder = orderService.getOrder(111);
		assertNull(expectedPreviousOrder.getDateStopped());

		order = (DrugOrder) orderService.saveOrder(order, null);

		assertNotNull(expectedPreviousOrder.getDateStopped(), "should populate dateStopped in previous order");
		assertNotNull(order.getId(), "should save discontinue order");
		assertEquals(expectedPreviousOrder, order.getPreviousOrder());
		assertNotNull(expectedPreviousOrder.getDateStopped());
		assertEquals(order.getDateActivated(), order.getAutoExpireDate());
	}

	/**
	 * @see OrderService#saveOrder(org.openmrs.Order, OrderContext)
	 */
	@Test
	public void saveOrder_shouldDiscontinuePreviousOrderIfItIsNotAlreadyDiscontinued() {
		//We are trying to discontinue order id 111 in standardTestDataset.xml
		DrugOrder order = new DrugOrder();
		order.setAction(Order.Action.DISCONTINUE);
		order.setOrderReasonNonCoded("Discontinue this");
		order.setDrug(conceptService.getDrug(3));
		order.setEncounter(encounterService.getEncounter(5));
		order.setPatient(Context.getPatientService().getPatient(7));
		order.setOrderer(Context.getProviderService().getProvider(1));
		order.setCareSetting(orderService.getCareSetting(1));
		order.setEncounter(encounterService.getEncounter(3));
		order.setOrderType(orderService.getOrderType(1));
		order.setDateActivated(new Date());
		order.setDosingType(SimpleDosingInstructions.class);
		order.setDose(500.0);
		order.setDoseUnits(conceptService.getConcept(50));
		order.setFrequency(orderService.getOrderFrequency(1));
		order.setRoute(conceptService.getConcept(22));
		order.setNumRefills(10);
		order.setQuantity(20.0);
		order.setQuantityUnits(conceptService.getConcept(51));
		Order previousOrder = orderService.getOrder(111);
		assertTrue(OrderUtilTest.isActiveOrder(previousOrder, null));
		order.setPreviousOrder(previousOrder);

		orderService.saveOrder(order, null);
		assertEquals(order.getDateActivated(), order.getAutoExpireDate());
		assertNotNull(previousOrder.getDateStopped(), "previous order should be discontinued");
	}

	/**
	 * @see OrderService#saveOrder(org.openmrs.Order, OrderContext)
	 */
	@Test
	public void saveOrder_shouldFailIfConceptInPreviousOrderDoesNotMatchThisConcept() {
		Order previousOrder = orderService.getOrder(7);
		assertTrue(OrderUtilTest.isActiveOrder(previousOrder, null));
		Order order = previousOrder.cloneForDiscontinuing();
		order.setDateActivated(new Date());
		order.setOrderReasonNonCoded("Discontinue this");
		order.setEncounter(encounterService.getEncounter(6));
		order.setOrderer(providerService.getProvider(1));
		Concept newConcept = conceptService.getConcept(5089);
		assertFalse(previousOrder.getConcept().equals(newConcept));
		order.setConcept(newConcept);

		EditedOrderDoesNotMatchPreviousException exception = assertThrows(EditedOrderDoesNotMatchPreviousException.class, () -> orderService.saveOrder(order, null));
		assertThat(exception.getMessage(), is("The orderable of the previous order and the new one order don't match"));
	}

	/**
	 * @see OrderService#discontinueOrder(org.openmrs.Order, org.openmrs.Concept, java.util.Date,
	 * org.openmrs.Provider, org.openmrs.Encounter)
	 */
	@Test
	public void discontinueOrder_shouldRejectAFutureDiscontinueDate() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.HOUR_OF_DAY, 1);
		Patient patient = Context.getPatientService().getPatient(2);
		CareSetting careSetting = orderService.getCareSetting(1);
		Order orderToDiscontinue = orderService.getActiveOrders(patient, null, careSetting, null).get(0);
		Encounter encounter = encounterService.getEncounter(3);
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> orderService.discontinueOrder(orderToDiscontinue, new Concept(), cal.getTime(), null, encounter));
		assertThat(exception.getMessage(), is("Discontinue date cannot be in the future"));
	}

	/**
	 * @see OrderService#discontinueOrder(org.openmrs.Order, String, java.util.Date,
	 * org.openmrs.Provider, org.openmrs.Encounter)
	 */
	@Test
	public void discontinueOrder_shouldFailIfDiscontinueDateIsInTheFuture() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.HOUR_OF_DAY, 1);
		Order orderToDiscontinue = orderService.getActiveOrders(Context.getPatientService().getPatient(2), null,
			orderService.getCareSetting(1), null).get(0);
		Encounter encounter = encounterService.getEncounter(3);
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> orderService.discontinueOrder(orderToDiscontinue, "Testing", cal.getTime(), null, encounter));
		assertThat(exception.getMessage(), is("Discontinue date cannot be in the future"));
	}

	/**
	 * @see OrderService#saveOrder(org.openmrs.Order, OrderContext)
	 */
	@Test
	public void saveOrder_shouldPassIfTheExistingDrugOrderMatchesTheConceptAndDrugOfTheDCOrder() {
		final DrugOrder orderToDiscontinue = (DrugOrder) orderService.getOrder(444);
		assertTrue(OrderUtilTest.isActiveOrder(orderToDiscontinue, null));

		DrugOrder order = new DrugOrder();
		order.setDrug(orderToDiscontinue.getDrug());
		order.setOrderType(orderService.getOrderTypeByName("Drug order"));
		order.setAction(Order.Action.DISCONTINUE);
		order.setOrderReasonNonCoded("Discontinue this");
		order.setPatient(orderToDiscontinue.getPatient());
		order.setConcept(orderToDiscontinue.getConcept());
		order.setOrderer(orderToDiscontinue.getOrderer());
		order.setCareSetting(orderToDiscontinue.getCareSetting());
		order.setEncounter(encounterService.getEncounter(6));
		order.setDateActivated(new Date());
		order.setDosingType(SimpleDosingInstructions.class);
		order.setDose(orderToDiscontinue.getDose());
		order.setDoseUnits(orderToDiscontinue.getDoseUnits());
		order.setRoute(orderToDiscontinue.getRoute());
		order.setFrequency(orderToDiscontinue.getFrequency());
		order.setQuantity(orderToDiscontinue.getQuantity());
		order.setQuantityUnits(orderToDiscontinue.getQuantityUnits());
		order.setNumRefills(orderToDiscontinue.getNumRefills());

		orderService.saveOrder(order, null);

		assertNotNull(orderToDiscontinue.getDateStopped(), "previous order should be discontinued");
	}

	/**
	 * @see OrderService#saveOrder(org.openmrs.Order, OrderContext)
	 */
	@Test
	public void saveOrder_shouldFailIfTheExistingDrugOrderMatchesTheConceptAndNotDrugOfTheDCOrder() {
		final DrugOrder orderToDiscontinue = (DrugOrder) orderService.getOrder(5);
		assertTrue(OrderUtilTest.isActiveOrder(orderToDiscontinue, null));

		//create a different test drug
		Drug discontinuationOrderDrug = new Drug();
		discontinuationOrderDrug.setConcept(orderToDiscontinue.getConcept());
		discontinuationOrderDrug = conceptService.saveDrug(discontinuationOrderDrug);
		assertNotEquals(discontinuationOrderDrug, orderToDiscontinue.getDrug());
		assertNotNull(orderToDiscontinue.getDrug());

		DrugOrder order = orderToDiscontinue.cloneForRevision();
		order.setDateActivated(new Date());
		order.setOrderer(providerService.getProvider(1));
		order.setEncounter(encounterService.getEncounter(6));
		order.setDrug(discontinuationOrderDrug);
		order.setOrderReasonNonCoded("Discontinue this");

		EditedOrderDoesNotMatchPreviousException exception = assertThrows(EditedOrderDoesNotMatchPreviousException.class, () -> orderService.saveOrder(order, null));
		assertThat(exception.getMessage(), is("The orderable of the previous order and the new one order don't match"));
	}

	/**
	 * previous order
	 *
	 * @see OrderService#saveOrder(org.openmrs.Order, OrderContext)
	 */
	@Test
	public void saveOrder_shouldPassIfTheExistingDrugOrderMatchesTheConceptAndThereIsNoDrugOnThePreviousOrder() {
		DrugOrder orderToDiscontinue = new DrugOrder();
		orderToDiscontinue.setAction(Action.NEW);
		orderToDiscontinue.setPatient(Context.getPatientService().getPatient(7));
		orderToDiscontinue.setConcept(Context.getConceptService().getConcept(5497));
		orderToDiscontinue.setCareSetting(orderService.getCareSetting(1));
		orderToDiscontinue.setOrderer(orderService.getOrder(1).getOrderer());
		orderToDiscontinue.setEncounter(encounterService.getEncounter(3));
		orderToDiscontinue.setDateActivated(new Date());
		orderToDiscontinue.setScheduledDate(new Date());
		orderToDiscontinue.setUrgency(Order.Urgency.ON_SCHEDULED_DATE);
		orderToDiscontinue.setEncounter(encounterService.getEncounter(3));
		orderToDiscontinue.setOrderType(orderService.getOrderType(17));

		orderToDiscontinue.setDrug(null);
		orderToDiscontinue.setDosingType(FreeTextDosingInstructions.class);
		orderToDiscontinue.setDosingInstructions("instructions");
		orderToDiscontinue.setOrderer(providerService.getProvider(1));
		orderToDiscontinue.setDosingInstructions("2 for 5 days");
		orderToDiscontinue.setQuantity(10.0);
		orderToDiscontinue.setQuantityUnits(conceptService.getConcept(51));
		orderToDiscontinue.setNumRefills(2);

		orderService.saveOrder(orderToDiscontinue, null);
		assertTrue(OrderUtilTest.isActiveOrder(orderToDiscontinue, null));

		DrugOrder order = orderToDiscontinue.cloneForDiscontinuing();
		order.setDateActivated(new Date());
		order.setOrderer(providerService.getProvider(1));
		order.setEncounter(encounterService.getEncounter(3));
		order.setOrderReasonNonCoded("Discontinue this");

		orderService.saveOrder(order, null);

		assertNotNull(orderToDiscontinue.getDateStopped(), "previous order should be discontinued");
	}

	/**
	 * @see OrderService#discontinueOrder(org.openmrs.Order, org.openmrs.Concept, java.util.Date,
	 * org.openmrs.Provider, org.openmrs.Encounter)
	 */
	@Test
	public void discontinueOrder_shouldFailForAStoppedOrder() {
		Order orderToDiscontinue = orderService.getOrder(1);
		Encounter encounter = encounterService.getEncounter(3);
		assertNotNull(orderToDiscontinue.getDateStopped());
		CannotStopInactiveOrderException exception = assertThrows(CannotStopInactiveOrderException.class, () -> orderService.discontinueOrder(orderToDiscontinue, Context.getConceptService().getConcept(1), null, null, encounter));
		assertThat(exception.getMessage(), is(messageSourceService.getMessage("Order.cannot.discontinue.inactive")));
	}

	/**
	 * @see OrderService#discontinueOrder(org.openmrs.Order, String, java.util.Date,
	 * org.openmrs.Provider, org.openmrs.Encounter)
	 */
	@Test
	public void discontinueOrder_shouldFailForAVoidedOrder() {
		Order orderToDiscontinue = orderService.getOrder(8);
		Encounter encounter = encounterService.getEncounter(3);
		assertTrue(orderToDiscontinue.getVoided());
		CannotStopInactiveOrderException exception = assertThrows(CannotStopInactiveOrderException.class, () -> orderService.discontinueOrder(orderToDiscontinue, "testing", null, null, encounter));
		assertThat(exception.getMessage(), is(messageSourceService.getMessage("Order.cannot.discontinue.inactive")));
	}

	/**
	 * @see OrderService#discontinueOrder(org.openmrs.Order, org.openmrs.Concept, java.util.Date,
	 * org.openmrs.Provider, org.openmrs.Encounter)
	 */
	@Test
	public void discontinueOrder_shouldFailForAnExpiredOrder() {
		Order orderToDiscontinue = orderService.getOrder(6);
		Encounter encounter = encounterService.getEncounter(3);
		assertNotNull(orderToDiscontinue.getAutoExpireDate());
		assertTrue(orderToDiscontinue.getAutoExpireDate().before(new Date()));
		CannotStopInactiveOrderException exception = assertThrows(CannotStopInactiveOrderException.class, () -> orderService.discontinueOrder(orderToDiscontinue, Context.getConceptService().getConcept(1), null, null, encounter));
		assertThat(exception.getMessage(), is(messageSourceService.getMessage("Order.cannot.discontinue.inactive")));
	}

	/**
	 * @see OrderService#saveOrder(org.openmrs.Order, OrderContext)
	 */
	@Test
	public void saveOrder_shouldNotAllowEditingAnExistingOrder() {
		final DrugOrder order = (DrugOrder) orderService.getOrder(5);
		UnchangeableObjectException exception = assertThrows(UnchangeableObjectException.class, () -> orderService.saveOrder(order, null));
		assertThat(exception.getMessage(), is("Order.cannot.edit.existing"));
	}

	/**
	 * @see OrderService#getCareSettingByUuid(String)
	 */
	@Test
	public void getCareSettingByUuid_shouldReturnTheCareSettingWithTheSpecifiedUuid() {
		CareSetting cs = orderService.getCareSettingByUuid("6f0c9a92-6f24-11e3-af88-005056821db0");
		assertEquals(1, cs.getId().intValue());
	}

	/**
	 * @see OrderService#getCareSettingByName(String)
	 */
	@Test
	public void getCareSettingByName_shouldReturnTheCareSettingWithTheSpecifiedName() {
		CareSetting cs = orderService.getCareSettingByName("INPATIENT");
		assertEquals(2, cs.getId().intValue());

		//should also be case insensitive
		cs = orderService.getCareSettingByName("inpatient");
		assertEquals(2, cs.getId().intValue());
	}

	/**
	 * @see OrderService#getCareSettings(boolean)
	 */
	@Test
	public void getCareSettings_shouldReturnOnlyUnRetiredCareSettingsIfIncludeRetiredIsSetToFalse() {
		List<CareSetting> careSettings = orderService.getCareSettings(false);
		assertEquals(2, careSettings.size());
		assertTrue(containsId(careSettings, 1));
		assertTrue(containsId(careSettings, 2));
	}

	/**
	 * @see OrderService#getCareSettings(boolean)
	 */
	@Test
	public void getCareSettings_shouldReturnRetiredCareSettingsIfIncludeRetiredIsSetToTrue() {
		CareSetting retiredCareSetting = orderService.getCareSetting(3);
		assertTrue(retiredCareSetting.getRetired());
		List<CareSetting> careSettings = orderService.getCareSettings(true);
		assertEquals(3, careSettings.size());
		assertTrue(containsId(careSettings, retiredCareSetting.getCareSettingId()));
	}

	/**
	 * @see OrderService#saveOrder(org.openmrs.Order, OrderContext)
	 */
	@Test
	public void saveOrder_shouldNotAllowRevisingAStoppedOrder() {
		Order originalOrder = orderService.getOrder(1);
		assertNotNull(originalOrder.getDateStopped());
		Order revisedOrder = originalOrder.cloneForRevision();
		revisedOrder.setEncounter(encounterService.getEncounter(4));
		revisedOrder.setInstructions("Take after a meal");
		revisedOrder.setOrderer(providerService.getProvider(1));
		revisedOrder.setDateActivated(new Date());
		CannotStopInactiveOrderException exception = assertThrows(CannotStopInactiveOrderException.class, () -> orderService.saveOrder(revisedOrder, null));
		assertThat(exception.getMessage(), is(messageSourceService.getMessage("Order.cannot.discontinue.inactive")));
	}

	/**
	 * @see OrderService#saveOrder(org.openmrs.Order, OrderContext)
	 */
	@Test
	public void saveOrder_shouldNotAllowRevisingAVoidedOrder() {
		Order originalOrder = orderService.getOrder(8);
		assertTrue(originalOrder.getVoided());
		Order revisedOrder = originalOrder.cloneForRevision();
		revisedOrder.setEncounter(encounterService.getEncounter(6));
		revisedOrder.setInstructions("Take after a meal");
		revisedOrder.setOrderer(providerService.getProvider(1));
		revisedOrder.setDateActivated(new Date());
		CannotStopInactiveOrderException exception = assertThrows(CannotStopInactiveOrderException.class, () -> orderService.saveOrder(revisedOrder, null));
		assertThat(exception.getMessage(), is(messageSourceService.getMessage("Order.cannot.discontinue.inactive")));
	}

	/**
	 * @see OrderService#saveOrder(org.openmrs.Order, OrderContext)
	 */
	@Test
	public void saveOrder_shouldNotAllowRevisingAnExpiredOrder() {
		Order originalOrder = orderService.getOrder(6);
		assertNotNull(originalOrder.getAutoExpireDate());
		assertTrue(originalOrder.getAutoExpireDate().before(new Date()));
		Order revisedOrder = originalOrder.cloneForRevision();
		revisedOrder.setEncounter(encounterService.getEncounter(6));
		revisedOrder.setInstructions("Take after a meal");
		revisedOrder.setOrderer(providerService.getProvider(1));
		revisedOrder.setDateActivated(new Date());
		revisedOrder.setAutoExpireDate(new Date());
		CannotStopInactiveOrderException exception = assertThrows(CannotStopInactiveOrderException.class, () -> orderService.saveOrder(revisedOrder, null));
		assertThat(exception.getMessage(), is(messageSourceService.getMessage("Order.cannot.discontinue.inactive")));
	}

	/**
	 * @see OrderService#saveOrder(org.openmrs.Order, OrderContext)
	 */
	@Test
	public void saveOrder_shouldNotAllowRevisingAnOrderWithNoPreviousOrder() {
		Order originalOrder = orderService.getOrder(111);
		assertTrue(originalOrder.isActive());
		Order revisedOrder = originalOrder.cloneForRevision();
		revisedOrder.setEncounter(encounterService.getEncounter(5));
		revisedOrder.setInstructions("Take after a meal");
		revisedOrder.setPreviousOrder(null);
		revisedOrder.setOrderer(providerService.getProvider(1));
		revisedOrder.setDateActivated(new Date());

		MissingRequiredPropertyException exception = assertThrows(MissingRequiredPropertyException.class, () -> orderService.saveOrder(revisedOrder, null));
		assertThat(exception.getMessage(), is(messageSourceService.getMessage("Order.previous.required")));
	}

	/**
	 * @see OrderService#saveOrder(org.openmrs.Order, OrderContext)
	 */
	@Test
	public void saveOrder_shouldSaveARevisedOrder() {
		Order originalOrder = orderService.getOrder(111);
		assertTrue(originalOrder.isActive());
		final Patient patient = originalOrder.getPatient();
		List<Order> originalActiveOrders = orderService.getActiveOrders(patient, null, null, null);
		final int originalOrderCount = originalActiveOrders.size();
		assertTrue(originalActiveOrders.contains(originalOrder));
		Order revisedOrder = originalOrder.cloneForRevision();
		revisedOrder.setEncounter(encounterService.getEncounter(5));
		revisedOrder.setInstructions("Take after a meal");
		revisedOrder.setDateActivated(new Date());
		revisedOrder.setOrderer(providerService.getProvider(1));
		revisedOrder.setEncounter(encounterService.getEncounter(3));
		orderService.saveOrder(revisedOrder, null);

		List<Order> activeOrders = orderService.getActiveOrders(patient, null, null, null);
		assertEquals(originalOrderCount, activeOrders.size());
		assertEquals(revisedOrder.getDateActivated(), DateUtils.addSeconds(originalOrder.getDateStopped(), 1));
		assertFalse(originalOrder.isActive());
	}

	/**
	 * @see OrderService#updateOrderFulfillerStatus(org.openmrs.Order, Order.FulfillerStatus, String)
	 */
	@Test
	public void updateOrderFulfillerStatus_shouldEditFulfillerStatusInOrder() {
		Order originalOrder = orderService.getOrder(111);
		String commentText = "We got the new order";
		assertNotEquals(originalOrder.getFulfillerStatus(), Order.FulfillerStatus.IN_PROGRESS);

		orderService.updateOrderFulfillerStatus(originalOrder, Order.FulfillerStatus.IN_PROGRESS, commentText);
		Context.flushSession();
		Order updatedOrder = orderService.getOrder(111);

		assertEquals(Order.FulfillerStatus.IN_PROGRESS, updatedOrder.getFulfillerStatus());
		assertEquals(commentText, updatedOrder.getFulfillerComment());
	}

	/**
	 * @see OrderService#updateOrderFulfillerStatus(org.openmrs.Order,
	 * Order.FulfillerStatus, String, String)
	 */
	@Test
	public void updateOrderFulfillerStatus_shouldEditFulfillerStatusWithAccessionNumberInOrder() {
		Order originalOrder = orderService.getOrder(111);
		String commentText = "We got the new order";
		String accessionNumber = "12345";
		assertNotEquals(originalOrder.getAccessionNumber(), accessionNumber);

		orderService.updateOrderFulfillerStatus(originalOrder, Order.FulfillerStatus.IN_PROGRESS, commentText,
			accessionNumber);
		Context.flushSession();
		Order updatedOrder = orderService.getOrder(111);

		assertEquals(Order.FulfillerStatus.IN_PROGRESS, updatedOrder.getFulfillerStatus());
		assertEquals(commentText, updatedOrder.getFulfillerComment());
		assertEquals(accessionNumber, updatedOrder.getAccessionNumber());
	}


	@Test
	public void updateOrderFulfillerStatus_shouldNotUpdateFulfillerStatusNullParameters() {

		// set up the test data
		Order originalOrder = orderService.getOrder(111);
		String commentText = "We got the new order";
		String accessionNumber = "12345";
		assertNotEquals(originalOrder.getAccessionNumber(), accessionNumber);

		orderService.updateOrderFulfillerStatus(originalOrder, Order.FulfillerStatus.IN_PROGRESS, commentText,
			accessionNumber);

		// now call again with all null
		orderService.updateOrderFulfillerStatus(originalOrder, null, null, null);

		Context.flushSession();
		Order updatedOrder = orderService.getOrder(111);

		assertEquals(Order.FulfillerStatus.IN_PROGRESS, updatedOrder.getFulfillerStatus());
		assertEquals(commentText, updatedOrder.getFulfillerComment());
		assertEquals(accessionNumber, updatedOrder.getAccessionNumber());
	}

	@Test
	public void updateOrderFulfillerStatus_shouldUpdateFulfillerStatusWithEmptyStrings() {

		// set up the test data
		Order originalOrder = orderService.getOrder(111);
		String commentText = "We got the new order";
		String accessionNumber = "12345";
		assertNotEquals(originalOrder.getAccessionNumber(), accessionNumber);

		orderService.updateOrderFulfillerStatus(originalOrder, Order.FulfillerStatus.IN_PROGRESS, commentText,
			accessionNumber);

		// now call again with all null
		orderService.updateOrderFulfillerStatus(originalOrder, null, "", "");

		Context.flushSession();
		Order updatedOrder = orderService.getOrder(111);

		assertEquals(Order.FulfillerStatus.IN_PROGRESS, updatedOrder.getFulfillerStatus());
		assertEquals("", updatedOrder.getFulfillerComment());
		assertEquals("", updatedOrder.getAccessionNumber());
	}

	/**
	 * @see OrderService#saveOrder(Order, OrderContext)
	 */
	@Test
	public void saveOrder_shouldSaveARevisedOrderForAScheduledOrderWhichIsNotStarted() {
		Order originalOrder = new Order();
		originalOrder.setAction(Action.NEW);
		originalOrder.setPatient(Context.getPatientService().getPatient(7));
		originalOrder.setConcept(Context.getConceptService().getConcept(5497));
		originalOrder.setCareSetting(orderService.getCareSetting(1));
		originalOrder.setOrderer(orderService.getOrder(1).getOrderer());
		originalOrder.setEncounter(encounterService.getEncounter(3));
		originalOrder.setOrderType(orderService.getOrderType(17));
		originalOrder.setDateActivated(new Date());
		originalOrder.setScheduledDate(DateUtils.addMonths(new Date(), 2));
		originalOrder.setUrgency(Order.Urgency.ON_SCHEDULED_DATE);
		originalOrder = orderService.saveOrder(originalOrder, null);

		assertTrue(originalOrder.isActive());
		final Patient patient = originalOrder.getPatient();
		List<Order> originalActiveOrders = orderService.getActiveOrders(patient, null, null, null);
		final int originalOrderCount = originalActiveOrders.size();
		assertTrue(originalActiveOrders.contains(originalOrder));

		Order revisedOrder = originalOrder.cloneForRevision();
		revisedOrder.setEncounter(encounterService.getEncounter(5));
		revisedOrder.setInstructions("Take after a meal");
		revisedOrder.setDateActivated(new Date());
		revisedOrder.setOrderer(providerService.getProvider(1));
		revisedOrder.setEncounter(encounterService.getEncounter(3));
		orderService.saveOrder(revisedOrder, null);

		List<Order> activeOrders = orderService.getActiveOrders(patient, null, null, null);
		assertEquals(originalOrderCount, activeOrders.size());
		assertEquals(revisedOrder.getDateActivated(), DateUtils.addSeconds(originalOrder.getDateStopped(), 1));
		assertFalse(activeOrders.contains(originalOrder));
		assertFalse(originalOrder.isActive());
	}

	/**
	 * @see OrderService#getOrderFrequencies(String, java.util.Locale, boolean, boolean)
	 */
	@Test
	public void getOrderFrequencies_shouldGetNonRetiredFrequenciesWithNamesMatchingThePhraseIfIncludeRetiredIsFalse() {
		executeDataSet("org/openmrs/api/include/OrderServiceTest-otherOrderFrequencies.xml");
		List<OrderFrequency> orderFrequencies = orderService.getOrderFrequencies("once", Locale.US, false, false);
		assertEquals(2, orderFrequencies.size());
		assertTrue(containsId(orderFrequencies, 100));
		assertTrue(containsId(orderFrequencies, 102));

		//should match anywhere in the concept name
		orderFrequencies = orderService.getOrderFrequencies("nce", Locale.US, false, false);
		assertEquals(2, orderFrequencies.size());
		assertTrue(containsId(orderFrequencies, 100));
		assertTrue(containsId(orderFrequencies, 102));
	}

	/**
	 * @see OrderService#getOrderFrequencies(String, java.util.Locale, boolean, boolean)
	 */
	@Test
	public void getOrderFrequencies_shouldIncludeRetiredFrequenciesIfIncludeRetiredIsSetToTrue() {
		executeDataSet("org/openmrs/api/include/OrderServiceTest-otherOrderFrequencies.xml");
		List<OrderFrequency> orderFrequencies = orderService.getOrderFrequencies("ce", Locale.US, false, true);
		assertEquals(4, orderFrequencies.size());
		assertTrue(containsId(orderFrequencies, 100));
		assertTrue(containsId(orderFrequencies, 101));
		assertTrue(containsId(orderFrequencies, 102));
		assertTrue(containsId(orderFrequencies, 103));
	}

	/**
	 * @see OrderService#getOrderFrequencies(String, java.util.Locale, boolean, boolean)
	 */
	@Test
	public void getOrderFrequencies_shouldGetFrequenciesWithNamesThatMatchThePhraseAndLocalesIfExactLocaleIsFalse() {
		executeDataSet("org/openmrs/api/include/OrderServiceTest-otherOrderFrequencies.xml");
		List<OrderFrequency> orderFrequencies = orderService.getOrderFrequencies("ce", Locale.US, false, false);
		assertEquals(3, orderFrequencies.size());
		assertTrue(containsId(orderFrequencies, 100));
		assertTrue(containsId(orderFrequencies, 101));
		assertTrue(containsId(orderFrequencies, 102));
	}

	/**
	 * @see OrderService#getOrderFrequencies(String, java.util.Locale, boolean, boolean)
	 */
	@Test
	public void getOrderFrequencies_shouldGetFrequenciesWithNamesThatMatchThePhraseAndLocaleIfExactLocaleIsTrue() {
		executeDataSet("org/openmrs/api/include/OrderServiceTest-otherOrderFrequencies.xml");
		List<OrderFrequency> orderFrequencies = orderService.getOrderFrequencies("ce", Locale.US, true, false);
		assertEquals(1, orderFrequencies.size());
		assertEquals(102, orderFrequencies.get(0).getOrderFrequencyId().intValue());

		orderFrequencies = orderService.getOrderFrequencies("ce", Locale.ENGLISH, true, false);
		assertEquals(2, orderFrequencies.size());
		assertTrue(containsId(orderFrequencies, 100));
		assertTrue(containsId(orderFrequencies, 101));
	}

	/**
	 * @see OrderService#getOrderFrequencies(String, java.util.Locale, boolean, boolean)
	 */
	@Test
	public void getOrderFrequencies_shouldReturnUniqueFrequencies() {
		executeDataSet("org/openmrs/api/include/OrderServiceTest-otherOrderFrequencies.xml");
		final String searchPhrase = "once";
		final Locale locale = Locale.ENGLISH;
		List<OrderFrequency> orderFrequencies = orderService.getOrderFrequencies(searchPhrase, locale, true, false);
		assertEquals(1, orderFrequencies.size());
		final OrderFrequency expectedOrderFrequency = orderService.getOrderFrequency(100);
		assertEquals(expectedOrderFrequency, orderFrequencies.get(0));

		//Add a new name to the frequency concept so that our search phrase matches on 2
		//concept names for the same frequency concept
		Concept frequencyConcept = expectedOrderFrequency.getConcept();
		final String newConceptName = searchPhrase + " A Day";
		frequencyConcept.addName(new ConceptName(newConceptName, locale));
		frequencyConcept.addDescription(new ConceptDescription("some description", locale));
		Context.flushSession(); //needed by postgresql
		conceptService.saveConcept(frequencyConcept);

		orderFrequencies = orderService.getOrderFrequencies(searchPhrase, locale, true, false);
		assertEquals(1, orderFrequencies.size());
		assertEquals(expectedOrderFrequency, orderFrequencies.get(0));
	}

	/**
	 * @see OrderService#getOrderFrequencies(String, java.util.Locale, boolean, boolean)
	 */
	@Test
	public void getOrderFrequencies_shouldRejectANullSearchPhrase() {
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> orderService.getOrderFrequencies(null, Locale.ENGLISH, false, false));
		assertThat(exception.getMessage(), is("searchPhrase is required"));
	}

	@Test
	public void retireOrderFrequency_shouldRetireGivenOrderFrequency() {
		OrderFrequency orderFrequency = orderService.getOrderFrequency(1);
		assertNotNull(orderFrequency);
		assertFalse(orderFrequency.getRetired());
		assertNull(orderFrequency.getRetireReason());
		assertNull(orderFrequency.getDateRetired());

		orderService.retireOrderFrequency(orderFrequency, "retire reason");

		orderFrequency = orderService.getOrderFrequency(1);
		assertNotNull(orderFrequency);
		assertTrue(orderFrequency.getRetired());
		assertEquals("retire reason", orderFrequency.getRetireReason());
		assertNotNull(orderFrequency.getDateRetired());

		//Should not change the number of order frequencies.
		assertEquals(3, orderService.getOrderFrequencies(true).size());
	}

	@Test
	public void unretireOrderFrequency_shouldUnretireGivenOrderFrequency() {
		executeDataSet("org/openmrs/api/include/OrderServiceTest-otherOrderFrequencies.xml");
		OrderFrequency orderFrequency = orderService.getOrderFrequency(103);
		assertNotNull(orderFrequency);
		assertTrue(orderFrequency.getRetired());
		assertNotNull(orderFrequency.getRetireReason());
		assertNotNull(orderFrequency.getDateRetired());

		orderService.unretireOrderFrequency(orderFrequency);

		orderFrequency = orderService.getOrderFrequency(103);
		assertNotNull(orderFrequency);
		assertFalse(orderFrequency.getRetired());
		assertNull(orderFrequency.getRetireReason());
		assertNull(orderFrequency.getDateRetired());

		//Should not change the number of order frequencies.
		assertEquals(7, orderService.getOrderFrequencies(true).size());
	}

	@Test
	public void purgeOrderFrequency_shouldDeleteGivenOrderFrequency() {
		OrderFrequency orderFrequency = orderService.getOrderFrequency(3);
		assertNotNull(orderFrequency);

		orderService.purgeOrderFrequency(orderFrequency);

		orderFrequency = orderService.getOrderFrequency(3);
		assertNull(orderFrequency);

		//Should reduce the existing number of order frequencies.
		assertEquals(2, orderService.getOrderFrequencies(true).size());
	}

	/**
	 * @see OrderService#saveOrderFrequency(OrderFrequency)
	 */
	@Test
	public void saveOrderFrequency_shouldAddANewOrderFrequencyToTheDatabase() {
		Concept concept = new Concept();
		concept.addName(new ConceptName("new name", Context.getLocale()));
		concept.addDescription(new ConceptDescription("some description", null));
		concept.setDatatype(new ConceptDatatype(1));
		concept.setConceptClass(conceptService.getConceptClassByName("Frequency"));
		concept = conceptService.saveConcept(concept);
		Integer originalSize = orderService.getOrderFrequencies(true).size();
		OrderFrequency orderFrequency = new OrderFrequency();
		orderFrequency.setConcept(concept);
		orderFrequency.setFrequencyPerDay(2d);

		orderFrequency = orderService.saveOrderFrequency(orderFrequency);

		assertNotNull(orderFrequency.getId());
		assertNotNull(orderFrequency.getUuid());
		assertNotNull(orderFrequency.getCreator());
		assertNotNull(orderFrequency.getDateCreated());
		assertEquals(originalSize + 1, orderService.getOrderFrequencies(true).size());
	}

	/**
	 * @see OrderService#saveOrderFrequency(OrderFrequency)
	 */
	@Test
	public void saveOrderFrequency_shouldEditAnExistingOrderFrequencyThatIsNotInUse() {
		executeDataSet(OTHER_ORDER_FREQUENCIES_XML);
		OrderFrequency orderFrequency = orderService.getOrderFrequency(100);
		assertNotNull(orderFrequency);

		orderFrequency.setFrequencyPerDay(4d);
		orderService.saveOrderFrequency(orderFrequency);
	}

	/**
	 * @see OrderService#purgeOrderFrequency(OrderFrequency)
	 */
	@Test
	public void purgeOrderFrequency_shouldNotAllowDeletingAnOrderFrequencyThatIsInUse() {
		OrderFrequency orderFrequency = orderService.getOrderFrequency(1);
		assertNotNull(orderFrequency);

		CannotDeleteObjectInUseException exception = assertThrows(CannotDeleteObjectInUseException.class, () -> orderService.purgeOrderFrequency(orderFrequency));
		assertThat(exception.getMessage(), is(messageSourceService.getMessage("Order.frequency.cannot.delete")));
	}

	@Test
	public void saveOrderWithScheduledDate_shouldAddANewOrderWithScheduledDateToTheDatabase() {
		Date scheduledDate = new Date();
		Order order = new Order();
		order.setAction(Action.NEW);
		order.setPatient(Context.getPatientService().getPatient(7));
		order.setConcept(Context.getConceptService().getConcept(5497));
		order.setCareSetting(orderService.getCareSetting(1));
		order.setOrderer(orderService.getOrder(1).getOrderer());
		order.setEncounter(encounterService.getEncounter(3));
		order.setDateActivated(new Date());
		order.setScheduledDate(scheduledDate);
		order.setUrgency(Order.Urgency.ON_SCHEDULED_DATE);
		order.setEncounter(encounterService.getEncounter(3));
		order.setOrderType(orderService.getOrderType(17));
		order = orderService.saveOrder(order, null);
		Order newOrder = orderService.getOrder(order.getOrderId());
		assertNotNull(order);
		assertEquals(DateUtil.truncateToSeconds(scheduledDate), order.getScheduledDate());
		assertNotNull(newOrder);
		assertEquals(DateUtil.truncateToSeconds(scheduledDate), newOrder.getScheduledDate());
	}

	/**
	 * @see OrderService#saveOrder(org.openmrs.Order, OrderContext)
	 */
	@Test
	public void saveOrder_shouldSetOrderNumberSpecifiedInTheContextIfSpecified() {
		GlobalProperty gp = new GlobalProperty(OpenmrsConstants.GP_ORDER_NUMBER_GENERATOR_BEAN_ID,
			"orderEntry.OrderNumberGenerator");
		Context.getAdministrationService().saveGlobalProperty(gp);
		Order order = new TestOrder();
		order.setEncounter(encounterService.getEncounter(6));
		order.setPatient(patientService.getPatient(7));
		order.setConcept(conceptService.getConcept(5497));
		order.setOrderer(providerService.getProvider(1));
		order.setCareSetting(orderService.getCareSetting(1));
		order.setOrderType(orderService.getOrderType(2));
		order.setEncounter(encounterService.getEncounter(3));
		order.setDateActivated(new Date());
		OrderContext orderCtxt = new OrderContext();
		final String expectedOrderNumber = "Testing";
		orderCtxt.setAttribute(TimestampOrderNumberGenerator.NEXT_ORDER_NUMBER, expectedOrderNumber);
		order = orderService.saveOrder(order, orderCtxt);
		assertEquals(expectedOrderNumber, order.getOrderNumber());
	}

	/**
	 * @see OrderService#saveOrder(org.openmrs.Order, OrderContext)
	 */
	@Test
	public void saveOrder_shouldSetTheOrderNumberReturnedByTheConfiguredGenerator() {
		GlobalProperty gp = new GlobalProperty(OpenmrsConstants.GP_ORDER_NUMBER_GENERATOR_BEAN_ID,
			"orderEntry.OrderNumberGenerator");
		Context.getAdministrationService().saveGlobalProperty(gp);
		Order order = new TestOrder();
		order.setPatient(patientService.getPatient(7));
		order.setConcept(conceptService.getConcept(5497));
		order.setOrderer(providerService.getProvider(1));
		order.setCareSetting(orderService.getCareSetting(1));
		order.setOrderType(orderService.getOrderType(2));
		order.setEncounter(encounterService.getEncounter(3));
		order.setDateActivated(new Date());
		order = orderService.saveOrder(order, null);
		assertTrue(order.getOrderNumber().startsWith(TimestampOrderNumberGenerator.ORDER_NUMBER_PREFIX));
	}

	/**
	 * @see OrderService#saveOrder(org.openmrs.Order, OrderContext)
	 */
	@Test
	@Disabled("Ignored because it fails after removal of deprecated methods TRUNK-4772")
	public void saveOrder_shouldFailForRevisionOrderIfAnActiveDrugOrderForTheSameConceptAndCareSettingsExists() {
		final Patient patient = patientService.getPatient(2);
		final Concept aspirin = conceptService.getConcept(88);
		DrugOrder firstOrder = new DrugOrder();
		firstOrder.setPatient(patient);
		firstOrder.setConcept(aspirin);
		firstOrder.setEncounter(encounterService.getEncounter(6));
		firstOrder.setOrderer(providerService.getProvider(1));
		firstOrder.setCareSetting(orderService.getCareSetting(2));
		firstOrder.setDrug(conceptService.getDrug(3));
		firstOrder.setDateActivated(new Date());
		firstOrder.setAutoExpireDate(DateUtils.addDays(new Date(), 10));
		firstOrder.setDosingType(FreeTextDosingInstructions.class);
		firstOrder.setDosingInstructions("2 for 5 days");
		firstOrder.setQuantity(10.0);
		firstOrder.setQuantityUnits(conceptService.getConcept(51));
		firstOrder.setNumRefills(0);
		orderService.saveOrder(firstOrder, null);

		//New order in future for same concept and care setting
		DrugOrder secondOrder = new DrugOrder();
		secondOrder.setPatient(firstOrder.getPatient());
		secondOrder.setConcept(firstOrder.getConcept());
		secondOrder.setEncounter(encounterService.getEncounter(6));
		secondOrder.setOrderer(providerService.getProvider(1));
		secondOrder.setCareSetting(firstOrder.getCareSetting());
		secondOrder.setDrug(conceptService.getDrug(3));
		secondOrder.setDateActivated(new Date());
		secondOrder.setScheduledDate(DateUtils.addDays(firstOrder.getEffectiveStopDate(), 1));
		secondOrder.setUrgency(Order.Urgency.ON_SCHEDULED_DATE);
		secondOrder.setDosingType(FreeTextDosingInstructions.class);
		secondOrder.setDosingInstructions("2 for 5 days");
		secondOrder.setQuantity(10.0);
		secondOrder.setQuantityUnits(conceptService.getConcept(51));
		secondOrder.setNumRefills(0);
		orderService.saveOrder(secondOrder, null);

		//Revise second order to have scheduled date overlapping with active order
		DrugOrder revision = secondOrder.cloneForRevision();
		revision.setScheduledDate(DateUtils.addDays(firstOrder.getEffectiveStartDate(), 2));
		revision.setEncounter(encounterService.getEncounter(6));
		revision.setOrderer(providerService.getProvider(1));

		APIException exception = assertThrows(APIException.class, () -> orderService.saveOrder(revision, null));
		assertThat(exception.getMessage(), is("Order.cannot.have.more.than.one"));
	}

	/**
	 * settings exists
	 *
	 * @see OrderService#saveOrder(Order, OrderContext)
	 */
	@Test
	@Disabled("Ignored because it fails after removal of deprecated methods TRUNK-4772")
	public void saveOrder_shouldPassForRevisionOrderIfAnActiveTestOrderForTheSameConceptAndCareSettingsExists() {
		final Patient patient = patientService.getPatient(2);
		final Concept cd4Count = conceptService.getConcept(5497);
		TestOrder activeOrder = new TestOrder();
		activeOrder.setPatient(patient);
		activeOrder.setConcept(cd4Count);
		activeOrder.setEncounter(encounterService.getEncounter(6));
		activeOrder.setOrderer(providerService.getProvider(1));
		activeOrder.setCareSetting(orderService.getCareSetting(2));
		activeOrder.setDateActivated(new Date());
		activeOrder.setAutoExpireDate(DateUtils.addDays(new Date(), 10));
		orderService.saveOrder(activeOrder, null);

		//New order in future for same concept
		TestOrder secondOrder = new TestOrder();
		secondOrder.setPatient(activeOrder.getPatient());
		secondOrder.setConcept(activeOrder.getConcept());
		secondOrder.setEncounter(encounterService.getEncounter(6));
		secondOrder.setOrderer(providerService.getProvider(1));
		secondOrder.setCareSetting(activeOrder.getCareSetting());
		secondOrder.setDateActivated(new Date());
		secondOrder.setScheduledDate(DateUtils.addDays(activeOrder.getEffectiveStopDate(), 1));
		secondOrder.setUrgency(Order.Urgency.ON_SCHEDULED_DATE);
		orderService.saveOrder(secondOrder, null);

		//Revise second order to have scheduled date overlapping with active order
		TestOrder revision = secondOrder.cloneForRevision();
		revision.setScheduledDate(DateUtils.addDays(activeOrder.getEffectiveStartDate(), 2));
		revision.setEncounter(encounterService.getEncounter(6));
		revision.setOrderer(providerService.getProvider(1));

		Order savedSecondOrder = orderService.saveOrder(revision, null);

		assertNotNull(orderService.getOrder(savedSecondOrder.getOrderId()));
	}

	/**
	 * @see OrderService#saveOrder(Order, OrderContext)
	 */
	@Test
	public void saveOrder_shouldFailIfAnActiveDrugOrderForTheSameConceptAndCareSettingExists() {
		final Patient patient = patientService.getPatient(2);
		final Concept triomuneThirty = conceptService.getConcept(792);
		//sanity check that we have an active order for the same concept
		DrugOrder duplicateOrder = (DrugOrder) orderService.getOrder(3);
		assertTrue(duplicateOrder.isActive());
		assertEquals(triomuneThirty, duplicateOrder.getConcept());

		DrugOrder drugOrder = new DrugOrder();
		drugOrder.setPatient(patient);
		drugOrder.setCareSetting(orderService.getCareSetting(1));
		drugOrder.setConcept(triomuneThirty);
		drugOrder.setEncounter(encounterService.getEncounter(6));
		drugOrder.setOrderer(providerService.getProvider(1));
		drugOrder.setCareSetting(duplicateOrder.getCareSetting());
		drugOrder.setDrug(duplicateOrder.getDrug());
		drugOrder.setDose(duplicateOrder.getDose());
		drugOrder.setDoseUnits(duplicateOrder.getDoseUnits());
		drugOrder.setRoute(duplicateOrder.getRoute());
		drugOrder.setFrequency(duplicateOrder.getFrequency());
		drugOrder.setQuantity(duplicateOrder.getQuantity());
		drugOrder.setQuantityUnits(duplicateOrder.getQuantityUnits());
		drugOrder.setNumRefills(duplicateOrder.getNumRefills());

		AmbiguousOrderException exception = assertThrows(AmbiguousOrderException.class, () -> orderService.saveOrder(drugOrder, null));
		;
		assertThat(exception.getMessage(), is("Order.cannot.have.more.than.one"));
	}

	/**
	 * @see OrderService#saveOrder(org.openmrs.Order, OrderContext)
	 */
	@Test
	public void saveOrder_shouldPassIfAnActiveTestOrderForTheSameConceptAndCareSettingExists() {
		final Patient patient = patientService.getPatient(2);
		final Concept cd4Count = conceptService.getConcept(5497);
		//sanity check that we have an active order for the same concept
		TestOrder duplicateOrder = (TestOrder) orderService.getOrder(7);
		assertTrue(duplicateOrder.isActive());
		assertEquals(cd4Count, duplicateOrder.getConcept());

		Order order = new TestOrder();
		order.setPatient(patient);
		order.setCareSetting(orderService.getCareSetting(2));
		order.setConcept(cd4Count);
		order.setEncounter(encounterService.getEncounter(6));
		order.setOrderer(providerService.getProvider(1));
		order.setCareSetting(duplicateOrder.getCareSetting());

		Order savedOrder = orderService.saveOrder(order, null);

		assertNotNull(orderService.getOrder(savedOrder.getOrderId()));
	}

	/**
	 * @see OrderService#saveOrder(org.openmrs.Order, OrderContext)
	 */
	@Test
	@Disabled("Ignored because it fails after removal of deprecated methods TRUNK-4772")
	public void saveOrder_shouldSaveRevisionOrderScheduledOnDateNotOverlappingWithAnActiveOrderForTheSameConceptAndCareSetting() {
		//sanity check that we have an active order
		final Patient patient = patientService.getPatient(2);
		final Concept cd4Count = conceptService.getConcept(5497);
		TestOrder activeOrder = new TestOrder();
		activeOrder.setPatient(patient);
		activeOrder.setConcept(cd4Count);
		activeOrder.setEncounter(encounterService.getEncounter(6));
		activeOrder.setOrderer(providerService.getProvider(1));
		activeOrder.setCareSetting(orderService.getCareSetting(2));
		activeOrder.setDateActivated(new Date());
		activeOrder.setAutoExpireDate(DateUtils.addDays(new Date(), 10));
		orderService.saveOrder(activeOrder, null);

		//New Drug order in future for same concept
		TestOrder secondOrder = new TestOrder();
		secondOrder.setPatient(activeOrder.getPatient());
		secondOrder.setConcept(activeOrder.getConcept());
		secondOrder.setEncounter(encounterService.getEncounter(6));
		secondOrder.setOrderer(providerService.getProvider(1));
		secondOrder.setCareSetting(activeOrder.getCareSetting());
		secondOrder.setDateActivated(new Date());
		secondOrder.setScheduledDate(DateUtils.addDays(activeOrder.getEffectiveStopDate(), 1));
		secondOrder.setUrgency(Order.Urgency.ON_SCHEDULED_DATE);
		orderService.saveOrder(secondOrder, null);

		//Revise Second Order to have scheduled date not overlapping with active order
		TestOrder revision = secondOrder.cloneForRevision();
		revision.setScheduledDate(DateUtils.addDays(activeOrder.getEffectiveStopDate(), 2));
		revision.setEncounter(encounterService.getEncounter(6));
		revision.setOrderer(providerService.getProvider(1));

		Order savedRevisionOrder = orderService.saveOrder(revision, null);

		assertNotNull(orderService.getOrder(savedRevisionOrder.getOrderId()));
	}

	/**
	 * @see OrderService#saveOrder(org.openmrs.Order, OrderContext)
	 */
	@Test
	public void saveOrder_shouldPassIfAnActiveDrugOrderForTheSameConceptAndCareSettingButDifferentFormulationExists() {
		executeDataSet("org/openmrs/api/include/OrderServiceTest-drugOrdersWithSameConceptAndDifferentFormAndStrength.xml");
		final Patient patient = patientService.getPatient(2);
		//sanity check that we have an active order
		DrugOrder existingOrder = (DrugOrder) orderService.getOrder(1000);
		assertTrue(existingOrder.isActive());
		//New Drug order
		DrugOrder order = new DrugOrder();
		order.setPatient(patient);
		order.setConcept(existingOrder.getConcept());
		order.setEncounter(encounterService.getEncounter(6));
		order.setOrderer(providerService.getProvider(1));
		order.setCareSetting(existingOrder.getCareSetting());
		order.setDrug(conceptService.getDrug(3001));
		order.setDosingType(FreeTextDosingInstructions.class);
		order.setDosingInstructions("2 for 5 days");
		order.setQuantity(10.0);
		order.setQuantityUnits(conceptService.getConcept(51));
		order.setNumRefills(2);

		Order savedDrugOrder = orderService.saveOrder(order, null);

		assertNotNull(orderService.getOrder(savedDrugOrder.getOrderId()));
	}

	/**
	 * @see OrderService#saveOrder(org.openmrs.Order, OrderContext)
	 */
	@Test
	public void saveOrder_shouldThrowAmbiguousOrderExceptionIfAnActiveDrugOrderForTheSameDrugFormulationExists() {
		executeDataSet("org/openmrs/api/include/OrderServiceTest-drugOrdersWithSameConceptAndDifferentFormAndStrength.xml");
		final Patient patient = patientService.getPatient(2);
		//sanity check that we have an active order for the same concept
		DrugOrder existingOrder = (DrugOrder) orderService.getOrder(1000);
		assertTrue(existingOrder.isActive());

		//New Drug order
		DrugOrder order = new DrugOrder();
		order.setPatient(patient);
		order.setDrug(existingOrder.getDrug());
		order.setEncounter(encounterService.getEncounter(6));
		order.setOrderer(providerService.getProvider(1));
		order.setCareSetting(existingOrder.getCareSetting());
		order.setDosingType(FreeTextDosingInstructions.class);
		order.setDosingInstructions("2 for 5 days");
		order.setQuantity(10.0);
		order.setQuantityUnits(conceptService.getConcept(51));
		order.setNumRefills(2);

		AmbiguousOrderException exception = assertThrows(AmbiguousOrderException.class, () -> orderService.saveOrder(order, null));
		assertThat(exception.getMessage(), is("Order.cannot.have.more.than.one"));
	}

	/**
	 * @see OrderService#saveOrder(org.openmrs.Order, OrderContext)
	 */
	@Test
	public void saveOrder_shouldPassIfAnActiveOrderForTheSameConceptExistsInADifferentCareSetting() {
		final Patient patient = patientService.getPatient(2);
		final Concept cd4Count = conceptService.getConcept(5497);
		TestOrder duplicateOrder = (TestOrder) orderService.getOrder(7);
		final CareSetting inpatient = orderService.getCareSetting(2);
		assertNotEquals(inpatient, duplicateOrder.getCareSetting());
		assertTrue(duplicateOrder.isActive());
		assertEquals(cd4Count, duplicateOrder.getConcept());
		int initialActiveOrderCount = orderService.getActiveOrders(patient, null, null, null).size();

		TestOrder order = new TestOrder();
		order.setPatient(patient);
		order.setCareSetting(orderService.getCareSetting(2));
		order.setConcept(cd4Count);
		order.setEncounter(encounterService.getEncounter(6));
		order.setOrderer(providerService.getProvider(1));
		order.setCareSetting(inpatient);

		orderService.saveOrder(order, null);
		List<Order> activeOrders = orderService.getActiveOrders(patient, null, null, null);
		assertEquals(++initialActiveOrderCount, activeOrders.size());
	}

	/**
	 * @throws ParseException
	 * @see OrderService#saveOrder(org.openmrs.Order, OrderContext)
	 */
	@Test
	public void saveOrder_shouldRollTheAutoExpireDateToTheEndOfTheDayIfItHasNoTimeComponent() throws ParseException {
		Order order = new TestOrder();
		order.setPatient(patientService.getPatient(2));
		order.setCareSetting(orderService.getCareSetting(2));
		order.setConcept(conceptService.getConcept(5089));
		order.setEncounter(encounterService.getEncounter(6));
		order.setOrderer(providerService.getProvider(1));
		DateFormat dateformat = new SimpleDateFormat("dd/MM/yyyy");
		order.setDateActivated(dateformat.parse("14/08/2014"));
		order.setAutoExpireDate(dateformat.parse("18/08/2014"));

		orderService.saveOrder(order, null);
		dateformat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.S");
		assertEquals(dateformat.parse("18/08/2014 23:59:59.000"), order.getAutoExpireDate());
	}

	/**
	 * @throws ParseException
	 * @see OrderService#saveOrder(org.openmrs.Order, OrderContext)
	 */
	@Test
	public void saveOrder_shouldNotChangeTheAutoExpireDateIfItHasATimeComponent() throws ParseException {
		Order order = new TestOrder();
		order.setPatient(patientService.getPatient(2));
		order.setCareSetting(orderService.getCareSetting(2));
		order.setConcept(conceptService.getConcept(5089));
		order.setEncounter(encounterService.getEncounter(6));
		order.setOrderer(providerService.getProvider(1));
		order.setDateActivated(new Date());
		DateFormat dateformat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		order.setDateActivated(dateformat.parse("14/08/2014 10:00:00"));
		Date autoExpireDate = dateformat.parse("18/08/2014 10:00:00");
		order.setAutoExpireDate(autoExpireDate);

		orderService.saveOrder(order, null);
		assertEquals(autoExpireDate, order.getAutoExpireDate());
	}

	/**
	 * @see OrderService#saveOrder(org.openmrs.Order, OrderContext)
	 */
	@Test
	public void saveOrder_shouldPassIfAnActiveDrugOrderForTheSameDrugFormulationExistsBeyondSchedule() {
		executeDataSet("org/openmrs/api/include/OrderServiceTest-DrugOrders.xml");
		final Patient patient = patientService.getPatient(2);

		DrugOrder existingOrder = (DrugOrder) orderService.getOrder(2000);
		int initialActiveOrderCount = orderService.getActiveOrders(patient, null, null, null).size();

		//New Drug order
		DrugOrder order = new DrugOrder();
		order.setPatient(patient);
		order.setDrug(existingOrder.getDrug());
		order.setEncounter(encounterService.getEncounter(6));
		order.setOrderer(providerService.getProvider(1));
		order.setCareSetting(existingOrder.getCareSetting());
		order.setDosingType(FreeTextDosingInstructions.class);
		order.setDosingInstructions("2 for 10 days");
		order.setQuantity(10.0);
		order.setQuantityUnits(conceptService.getConcept(51));
		order.setNumRefills(2);
		order.setUrgency(Order.Urgency.ON_SCHEDULED_DATE);

		order.setScheduledDate(DateUtils.addDays(existingOrder.getDateStopped(), 1));

		orderService.saveOrder(order, null);
		List<Order> activeOrders = orderService.getActiveOrders(patient, null, null, null);
		assertEquals(++initialActiveOrderCount, activeOrders.size());
	}

	/**
	 * @see OrderService#getOrderType(Integer)
	 */
	@Test
	public void getOrderType_shouldFindOrderTypeObjectGivenValidId() {
		assertEquals("Drug order", orderService.getOrderType(1).getName());
	}

	/**
	 * @see OrderService#getOrderType(Integer)
	 */
	@Test
	public void getOrderType_shouldReturnNullIfNoOrderTypeObjectFoundWithGivenId() {
		OrderType orderType = orderService.getOrderType(1000);
		assertNull(orderType);
	}

	/**
	 * @see OrderService#getOrderTypeByUuid(String)
	 */
	@Test
	public void getOrderTypeByUuid_shouldFindOrderTypeObjectGivenValidUuid() {
		OrderType orderType = orderService.getOrderTypeByUuid("131168f4-15f5-102d-96e4-000c29c2a5d7");
		assertEquals("Drug order", orderType.getName());
	}

	/**
	 * @see OrderService#getOrderTypeByUuid(String)
	 */
	@Test
	public void getOrderTypeByUuid_shouldReturnNullIfNoOrderTypeObjectFoundWithGivenUuid() {
		assertNull(orderService.getOrderTypeByUuid("some random uuid"));
	}

	/**
	 * @see OrderService#getOrderTypes(boolean)
	 */
	@Test
	public void getOrderTypes_shouldGetAllOrderTypesIfIncludeRetiredIsSetToTrue() {
		assertEquals(14, orderService.getOrderTypes(true).size());
	}

	/**
	 * @see OrderService#getOrderTypes(boolean)
	 */
	@Test
	public void getOrderTypes_shouldGetAllNonRetiredOrderTypesIfIncludeRetiredIsSetToFalse() {
		assertEquals(11, orderService.getOrderTypes(false).size());
	}

	/**
	 * @see OrderService#getOrderTypeByName(String)
	 */
	@Test
	public void getOrderTypeByName_shouldReturnTheOrderTypeThatMatchesTheSpecifiedName() {
		OrderType orderType = orderService.getOrderTypeByName("Drug order");
		assertEquals("131168f4-15f5-102d-96e4-000c29c2a5d7", orderType.getUuid());
	}

	/**
	 * @see OrderService#getOrders(org.openmrs.Patient, org.openmrs.CareSetting,
	 * org.openmrs.OrderType, boolean)
	 */
	@Test
	public void getOrders_shouldFailIfPatientIsNull() {
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> orderService.getOrders(null, null, null, false));
		assertThat(exception.getMessage(), is("Patient is required"));
	}

	/**
	 * @see OrderService#getOrders(org.openmrs.Patient, org.openmrs.CareSetting,
	 * org.openmrs.OrderType, boolean)
	 */
	@Test
	public void getOrders_shouldFailIfCareSettingIsNull() {
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> orderService.getOrders(new Patient(), null, null, false));
		assertThat(exception.getMessage(), is("CareSetting is required"));
	}

	/**
	 * @see OrderService#getOrders(org.openmrs.Patient, org.openmrs.CareSetting,
	 * org.openmrs.OrderType, boolean)
	 */
	@Test
	public void getOrders_shouldGetTheOrdersThatMatchAllTheArguments() {
		Patient patient = patientService.getPatient(2);
		CareSetting outPatient = orderService.getCareSetting(1);
		OrderType testOrderType = orderService.getOrderType(2);
		List<Order> testOrders = orderService.getOrders(patient, outPatient, testOrderType, false);
		assertEquals(3, testOrders.size());
		TestUtil.containsId(testOrders, 6);
		TestUtil.containsId(testOrders, 7);
		TestUtil.containsId(testOrders, 9);

		OrderType drugOrderType = orderService.getOrderType(1);
		List<Order> drugOrders = orderService.getOrders(patient, outPatient, drugOrderType, false);
		assertEquals(5, drugOrders.size());
		TestUtil.containsId(drugOrders, 2);
		TestUtil.containsId(drugOrders, 3);
		TestUtil.containsId(drugOrders, 44);
		TestUtil.containsId(drugOrders, 444);
		TestUtil.containsId(drugOrders, 5);

		CareSetting inPatient = orderService.getCareSetting(2);
		List<Order> inPatientDrugOrders = orderService.getOrders(patient, inPatient, drugOrderType, false);
		assertEquals(222, inPatientDrugOrders.get(0).getOrderId().intValue());
	}

	/**
	 * @see OrderService#getOrders(org.openmrs.Patient, org.openmrs.CareSetting,
	 * org.openmrs.OrderType, boolean)
	 */
	@Test
	public void getOrders_shouldGetAllUnvoidedMatchesIfIncludeVoidedIsSetToFalse() {
		Patient patient = patientService.getPatient(2);
		CareSetting outPatient = orderService.getCareSetting(1);
		OrderType testOrderType = orderService.getOrderType(2);
		assertEquals(3, orderService.getOrders(patient, outPatient, testOrderType, false).size());
	}

	/**
	 * @see OrderService#getOrders(org.openmrs.Patient, org.openmrs.CareSetting,
	 * org.openmrs.OrderType, boolean)
	 */
	@Test
	public void getOrders_shouldIncludeVoidedMatchesIfIncludeVoidedIsSetToTrue() {
		Patient patient = patientService.getPatient(2);
		CareSetting outPatient = orderService.getCareSetting(1);
		OrderType testOrderType = orderService.getOrderType(2);
		assertEquals(4, orderService.getOrders(patient, outPatient, testOrderType, true).size());
	}

	/**
	 * @see OrderService#getOrders(org.openmrs.Patient, org.openmrs.CareSetting,
	 * org.openmrs.OrderType, boolean)
	 */
	@Test
	public void getOrders_shouldIncludeOrdersForSubTypesIfOrderTypeIsSpecified() {
		executeDataSet("org/openmrs/api/include/OrderServiceTest-otherOrders.xml");
		Patient patient = patientService.getPatient(2);
		OrderType testOrderType = orderService.getOrderType(2);
		CareSetting outPatient = orderService.getCareSetting(1);
		List<Order> orders = orderService.getOrders(patient, outPatient, testOrderType, false);
		assertEquals(7, orders.size());
		Order[] expectedOrder1 = {orderService.getOrder(6), orderService.getOrder(7), orderService.getOrder(9),
			orderService.getOrder(101), orderService.getOrder(102), orderService.getOrder(103),
			orderService.getOrder(104)};
		assertThat(orders, hasItems(expectedOrder1));

		OrderType labTestOrderType = orderService.getOrderType(7);
		orders = orderService.getOrders(patient, outPatient, labTestOrderType, false);
		assertEquals(3, orderService.getOrders(patient, outPatient, labTestOrderType, false).size());
		Order[] expectedOrder2 = {orderService.getOrder(101), orderService.getOrder(103), orderService.getOrder(104)};
		assertThat(orders, hasItems(expectedOrder2));
	}

	/**
	 * @see OrderService#(OrderSearchCriteria)
	 */
	@Test
	public void getOrders_shouldGetOrdersByPatient() {
		Patient patient = patientService.getPatient(2);
		OrderSearchCriteria orderSearchCriteria = new OrderSearchCriteriaBuilder().setPatient(patient).build();
		List<Order> orders = orderService.getOrders(orderSearchCriteria);
		assertEquals(11, orders.size());
	}

	/**
	 * @see OrderService#(OrderSearchCriteria)
	 */
	@Test
	public void getOrders_shouldGetStoppedOrders() {
		OrderSearchCriteria orderSearchCriteria = new OrderSearchCriteriaBuilder().setIsStopped(true).build();
		List<Order> orders = orderService.getOrders(orderSearchCriteria);
		assertEquals(4, orders.size());
		for (Order order : orders) {
			assertNotNull(order.getDateStopped());
		}
	}

	/**
	 * @see OrderService#(OrderSearchCriteria)
	 */
	@Test
	public void getOrders_shouldReturnOrdersAutoExpiredBeforeDate() {
		Date autoExpireOnOrBeforeDate = new GregorianCalendar(2008, 9, 30).getTime();
		OrderSearchCriteria orderSearchCriteria = new OrderSearchCriteriaBuilder().setAutoExpireOnOrBeforeDate(autoExpireOnOrBeforeDate).build();
		List<Order> orders = orderService.getOrders(orderSearchCriteria);
		assertEquals(4, orders.size());
		for (Order order : orders) {
			assertNotNull(order.getAutoExpireDate());
			assertTrue(autoExpireOnOrBeforeDate.after(order.getAutoExpireDate()));
		}
	}

	/**
	 * @see OrderService#(OrderSearchCriteria)
	 */
	@Test
	public void getOrders_shouldReturnOnlyCanceledOrAutoExpiredOrdersBeforeDate() {
		Date canceledOrExpiredOnOrBeforeDate = new GregorianCalendar(2008, 9, 30).getTime();
		OrderSearchCriteria orderSearchCriteria = new OrderSearchCriteriaBuilder().setCanceledOrExpiredOnOrBeforeDate(canceledOrExpiredOnOrBeforeDate).build();
		List<Order> orders = orderService.getOrders(orderSearchCriteria);
		assertEquals(7, orders.size());
		for (Order order : orders) {
			assertTrue((order.getDateStopped() != null && order.getDateStopped().before(canceledOrExpiredOnOrBeforeDate))
				|| (order.getAutoExpireDate() != null && order.getAutoExpireDate().before(canceledOrExpiredOnOrBeforeDate)));
		}
	}

	/**
	 * @see OrderService#(OrderSearchCriteria)
	 */
	@Test
	public void getOrders_shouldNotReturnCanceledOrAutoExpiredOrders() {
		Date today = Calendar.getInstance().getTime();
		OrderSearchCriteria orderSearchCriteria = new OrderSearchCriteriaBuilder().setExcludeCanceledAndExpired(true).build();
		List<Order> orders = orderService.getOrders(orderSearchCriteria);
		assertEquals(6, orders.size());
		for (Order order : orders) {
			assertTrue((order.getDateStopped() == null || (order.getDateStopped() != null && order.getDateStopped().after(today))) &&
				(order.getAutoExpireDate() == null || (order.getAutoExpireDate() != null && order.getAutoExpireDate().after(today)))
			);
		}
	}

	/**
	 * @see OrderService#(OrderSearchCriteria)
	 */
	@Test
	public void getOrders_shouldreturnOrdersWithFulfillerStatusCompleted() {
		OrderSearchCriteria orderSearchCriteria = new OrderSearchCriteriaBuilder().setFulfillerStatus(Order.FulfillerStatus.valueOf("COMPLETED")).build();
		List<Order> orders = orderService.getOrders(orderSearchCriteria);
		assertEquals(1, orders.size());
		for (Order order : orders) {
			assertEquals(COMPLETED, order.getFulfillerStatus());
		}
	}

	/**
	 * @see OrderService#(OrderSearchCriteria)
	 */
	@Test
	public void getOrders_shouldReturnOrdersWithFulfillerStatusReceivedOrNull() {
		OrderSearchCriteria orderSearchCriteria = new OrderSearchCriteriaBuilder().setFulfillerStatus(Order.FulfillerStatus.valueOf("RECEIVED")).setIncludeNullFulfillerStatus(new Boolean(true)).build();
		List<Order> orders = orderService.getOrders(orderSearchCriteria);
		assertEquals(12, orders.size());
		for (Order order : orders) {
			assertTrue(order.getFulfillerStatus() == Order.FulfillerStatus.RECEIVED ||
				order.getFulfillerStatus() == null);
		}
	}

	/**
	 * @see OrderService#(OrderSearchCriteria)
	 */
	@Test
	public void getOrders_shouldReturnOrdersWithFulfillerStatusNotNull() {
		OrderSearchCriteria orderSearchCriteria = new OrderSearchCriteriaBuilder().setIncludeNullFulfillerStatus(new Boolean(false)).build();
		List<Order> orders = orderService.getOrders(orderSearchCriteria);
		assertEquals(3, orders.size());
		for (Order order : orders) {
			assertTrue(order.getFulfillerStatus() != null);
		}
	}

	/**
	 * @see OrderService#(OrderSearchCriteria)
	 */
	@Test
	public void getOrders_shouldReturnOrdersWithFulfillerStatusNull() {
		OrderSearchCriteria orderSearchCriteria = new OrderSearchCriteriaBuilder().setIncludeNullFulfillerStatus(new Boolean(true)).build();
		List<Order> orders = orderService.getOrders(orderSearchCriteria);
		assertEquals(10, orders.size());
		for (Order order : orders) {
			assertNull(order.getFulfillerStatus());
		}
	}

	/**
	 * @see OrderService#(OrderSearchCriteria)
	 */
	@Test
	public void getOrders_shouldreturnDiscontinuedOrders() {
		OrderSearchCriteria orderSearchCriteria = new OrderSearchCriteriaBuilder().setAction(Order.Action.valueOf("DISCONTINUE")).build();
		List<Order> orders = orderService.getOrders(orderSearchCriteria);
		assertEquals(2, orders.size());
		for (Order order : orders) {
			assertEquals(DISCONTINUE, order.getAction());
		}
	}

	/**
	 * @see OrderService#(OrderSearchCriteria)
	 */
	@Test
	public void getOrders_shouldNotReturnDiscontinuedOrders() {
		OrderSearchCriteria orderSearchCriteria = new OrderSearchCriteriaBuilder().setExcludeDiscontinueOrders(true).build();
		List<Order> orders = orderService.getOrders(orderSearchCriteria);
		assertEquals(11, orders.size());
		for (Order order : orders) {
			assertNotEquals(order.getAction(), org.openmrs.Order.Action.DISCONTINUE);
		}
	}


	/**
	 * @see OrderService#(OrderSearchCriteria)
	 */
	@Test
	public void getOrders_shouldGetOrdersByCareSetting() {
		CareSetting outPatient = orderService.getCareSetting(1);
		OrderSearchCriteria orderSearchCriteria = new OrderSearchCriteriaBuilder().setCareSetting(outPatient).build();
		List<Order> orders = orderService.getOrders(orderSearchCriteria);
		assertEquals(12, orders.size());
	}

	/**
	 * @see OrderService#(OrderSearchCriteria)
	 */
	@Test
	public void getOrders_shouldGetOrdersByConcepts() {
		List<Concept> concepts = new ArrayList<>();
		concepts.add(conceptService.getConcept(88)); // aspirin
		concepts.add(conceptService.getConcept(3)); // cough syrup
		OrderSearchCriteria orderSearchCriteria = new OrderSearchCriteriaBuilder().setConcepts(concepts).build();
		List<Order> orders = orderService.getOrders(orderSearchCriteria);
		assertEquals(6, orders.size());
	}

	/**
	 * @see OrderService#(OrderSearchCriteria)
	 */
	@Test
	public void getOrders_shouldGetOrdersByOrderTypes() {
		List<OrderType> orderTypes = new ArrayList<>();
		orderTypes.add(orderService.getOrderType(1)); // drug order
		OrderSearchCriteria orderSearchCriteria = new OrderSearchCriteriaBuilder().setOrderTypes(orderTypes).build();
		List<Order> orders = orderService.getOrders(orderSearchCriteria);
		assertEquals(10, orders.size());
	}

	/**
	 * @see OrderService#(OrderSearchCriteria)
	 */
	@Test
	public void getOrders_shouldGetOrdersByActivatedOnOrBeforeDate() {
		// should get orders activated any time on this day
		Date activatedOnOrBeforeDate = new GregorianCalendar(2008, 7, 19).getTime();
		OrderSearchCriteria orderSearchCriteria = new OrderSearchCriteriaBuilder().setActivatedOnOrBeforeDate(activatedOnOrBeforeDate).build();
		List<Order> orders = orderService.getOrders(orderSearchCriteria);
		assertEquals(11, orders.size());
	}

	/**
	 * @see OrderService#(OrderSearchCriteria)
	 */
	@Test
	public void getOrders_shouldGetOrdersByActivatedOnOrAfterDate() {
		// hour and minute should be ignored by search
		Date activatedOnOrAfterDate = new GregorianCalendar(2008, 7, 19, 12, 0).getTime();
		OrderSearchCriteria orderSearchCriteria = new OrderSearchCriteriaBuilder().setActivatedOnOrAfterDate(activatedOnOrAfterDate).build();
		List<Order> orders = orderService.getOrders(orderSearchCriteria);
		assertEquals(3, orders.size());
	}

	/**
	 * @see OrderService#(OrderSearchCriteria)
	 */
	@Test
	public void getOrders_shouldGetOrdersByIncludeVoided() {
		OrderSearchCriteria orderSearchCriteria = new OrderSearchCriteriaBuilder().setIncludeVoided(true).build();
		List<Order> orders = orderService.getOrders(orderSearchCriteria);
		assertEquals(14, orders.size());
	}

	/**
	 * @see OrderService#(OrderSearchCriteria)
	 */
	@Test
	public void getOrders_shouldGetTheOrdersByCareSettingAndOrderType() {
		CareSetting outPatient = orderService.getCareSetting(1);
		List<OrderType> orderTypes = new ArrayList<>();
		orderTypes.add(orderService.getOrderType(2)); // test order type
		OrderSearchCriteria orderSearchCriteria = new OrderSearchCriteriaBuilder().setCareSetting(outPatient).setOrderTypes(orderTypes).build();
		List<Order> orders = orderService.getOrders(orderSearchCriteria);
		assertEquals(3, orders.size());
	}

	@Test
	public void getOrders_shouldGetTheOrdersByOrderNumber() {
		OrderSearchCriteria orderSearchCriteria = new OrderSearchCriteriaBuilder().setOrderNumber("ORD-7").build();
		List<Order> orders = orderService.getOrders(orderSearchCriteria);
		assertEquals(1, orders.size());
		assertEquals("2c96f25c-4949-4f72-9931-d808fbc226df", orders.iterator().next().getUuid());
	}

	@Test
	public void getOrders_shouldGetTheOrdersByOrderNumberEvenIfCaseDoesNotMatch() {
		OrderSearchCriteria orderSearchCriteria = new OrderSearchCriteriaBuilder().setOrderNumber("ord-7").build();
		List<Order> orders = orderService.getOrders(orderSearchCriteria);
		assertEquals(1, orders.size());
		assertEquals("2c96f25c-4949-4f72-9931-d808fbc226df", orders.iterator().next().getUuid());
	}

	@Test
	public void getOrders_shouldGetTheOrdersByAccessionNumber() {
		OrderSearchCriteria orderSearchCriteria = new OrderSearchCriteriaBuilder().setAccessionNumber("ACC-123").build();
		List<Order> orders = orderService.getOrders(orderSearchCriteria);
		assertEquals(1, orders.size());
		assertEquals("e1f95924-697a-11e3-bd76-0800271c1b75", orders.iterator().next().getUuid());
	}

	@Test
	public void getOrders_shouldGetTheOrdersByAccessionNumberEvenIfCaseDoesNotMatch() {
		OrderSearchCriteria orderSearchCriteria = new OrderSearchCriteriaBuilder().setAccessionNumber("acc-123").build();
		List<Order> orders = orderService.getOrders(orderSearchCriteria);
		assertEquals(1, orders.size());
		assertEquals("e1f95924-697a-11e3-bd76-0800271c1b75", orders.iterator().next().getUuid());
	}

	/**
	 * @see OrderService#getAllOrdersByPatient(org.openmrs.Patient)
	 */
	@Test
	public void getAllOrdersByPatient_shouldFailIfPatientIsNull() {
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> orderService.getAllOrdersByPatient(null));
		assertThat(exception.getMessage(), is("Patient is required"));
	}

	/**
	 * @see OrderService#getAllOrdersByPatient(org.openmrs.Patient)
	 */
	@Test
	public void getAllOrdersByPatient_shouldGetAllTheOrdersForTheSpecifiedPatient() {
		assertEquals(12, orderService.getAllOrdersByPatient(patientService.getPatient(2)).size());
		assertEquals(2, orderService.getAllOrdersByPatient(patientService.getPatient(7)).size());
	}

	/**
	 * @see OrderService#saveOrder(org.openmrs.Order, OrderContext)
	 */
	@Test
	public void saveOrder_shouldSetOrderTypeIfNullButMappedToTheConceptClass() {
		TestOrder order = new TestOrder();
		order.setPatient(patientService.getPatient(7));
		order.setConcept(conceptService.getConcept(5497));
		order.setOrderer(providerService.getProvider(1));
		order.setCareSetting(orderService.getCareSetting(1));
		order.setEncounter(encounterService.getEncounter(3));
		order.setDateActivated(new Date());
		orderService.saveOrder(order, null);
		assertEquals(2, order.getOrderType().getOrderTypeId().intValue());
	}

	/**
	 * @see OrderService#saveOrder(org.openmrs.Order, OrderContext)
	 */
	@Test
	public void saveOrder_shouldFailIfOrderTypeIsNullAndNotMappedToTheConceptClass() {
		Order order = new Order();
		order.setPatient(patientService.getPatient(7));
		order.setConcept(conceptService.getConcept(9));
		order.setOrderer(providerService.getProvider(1));
		order.setCareSetting(orderService.getCareSetting(1));
		order.setEncounter(encounterService.getEncounter(3));
		order.setDateActivated(new Date());
		OrderEntryException exception = assertThrows(OrderEntryException.class, () -> orderService.saveOrder(order, null));
		assertThat(exception.getMessage(), is("Order.type.cannot.determine"));
	}

	/**
	 * @see OrderService#saveOrderType(org.openmrs.OrderType)
	 */
	@Test
	public void saveOrderType_shouldAddANewOrderTypeToTheDatabase() {
		int orderTypeCount = orderService.getOrderTypes(true).size();
		OrderType orderType = new OrderType();
		orderType.setName("New Order");
		orderType.setJavaClassName("org.openmrs.NewTestOrder");
		orderType.setDescription("New order type for testing");
		orderType.setRetired(false);
		orderType = orderService.saveOrderType(orderType);
		assertNotNull(orderType);
		assertEquals("New Order", orderType.getName());
		assertNotNull(orderType.getId());
		assertEquals((orderTypeCount + 1), orderService.getOrderTypes(true).size());
	}

	/**
	 * @see OrderService#saveOrderType(org.openmrs.OrderType)
	 */
	@Test
	public void saveOrderType_shouldEditAnExistingOrderType() {
		OrderType orderType = orderService.getOrderType(1);
		assertNull(orderType.getDateChanged());
		assertNull(orderType.getChangedBy());
		final String newDescription = "new";
		orderType.setDescription(newDescription);

		orderService.saveOrderType(orderType);
		Context.flushSession();
		assertNotNull(orderType.getDateChanged());
		assertNotNull(orderType.getChangedBy());
	}

	/**
	 * @see OrderService#purgeOrderType(org.openmrs.OrderType)
	 */
	@Test
	public void purgeOrderType_shouldDeleteOrderTypeIfNotInUse() {
		final Integer id = 13;
		OrderType orderType = orderService.getOrderType(id);
		assertNotNull(orderType);
		orderService.purgeOrderType(orderType);
		assertNull(orderService.getOrderType(id));
	}

	/**
	 * @see OrderService#purgeOrderType(org.openmrs.OrderType)
	 */
	@Test
	public void purgeOrderType_shouldNotAllowDeletingAnOrderTypeThatIsInUse() {
		OrderType orderType = orderService.getOrderType(1);
		assertNotNull(orderType);
		CannotDeleteObjectInUseException exception = assertThrows(CannotDeleteObjectInUseException.class, () -> orderService.purgeOrderType(orderType));
		assertThat(exception.getMessage(), is(messageSourceService.getMessage("Order.type.cannot.delete")));
	}

	/**
	 * @see OrderService#retireOrderType(org.openmrs.OrderType, String)
	 */
	@Test
	public void retireOrderType_shouldRetireOrderType() {
		OrderType orderType = orderService.getOrderType(15);
		assertFalse(orderType.getRetired());
		assertNull(orderType.getRetiredBy());
		assertNull(orderType.getRetireReason());
		assertNull(orderType.getDateRetired());
		orderService.retireOrderType(orderType, "Retire for testing purposes");
		orderType = orderService.getOrderType(15);
		assertTrue(orderType.getRetired());
		assertNotNull(orderType.getRetiredBy());
		assertNotNull(orderType.getRetireReason());
		assertNotNull(orderType.getDateRetired());
	}

	/**
	 * @see OrderService#unretireOrderType(org.openmrs.OrderType)
	 */
	@Test
	public void unretireOrderType_shouldUnretireOrderType() {
		OrderType orderType = orderService.getOrderType(16);
		assertTrue(orderType.getRetired());
		assertNotNull(orderType.getRetiredBy());
		assertNotNull(orderType.getRetireReason());
		assertNotNull(orderType.getDateRetired());
		orderService.unretireOrderType(orderType);
		orderType = orderService.getOrderType(16);
		assertFalse(orderType.getRetired());
		assertNull(orderType.getRetiredBy());
		assertNull(orderType.getRetireReason());
		assertNull(orderType.getDateRetired());
	}

	/**
	 * @see OrderService#getSubtypes(org.openmrs.OrderType, boolean)
	 */
	@Test
	public void getOrderSubTypes_shouldGetAllSubOrderTypesWithRetiredOrderTypes() {
		List<OrderType> orderTypeList = orderService.getSubtypes(orderService.getOrderType(2), true);
		assertEquals(7, orderTypeList.size());
	}

	/**
	 * @see OrderService#getSubtypes(org.openmrs.OrderType, boolean)
	 */
	@Test
	public void getOrderSubTypes_shouldGetAllSubOrderTypesWithoutRetiredOrderTypes() {
		List<OrderType> orderTypeList = orderService.getSubtypes(orderService.getOrderType(2), false);
		assertEquals(6, orderTypeList.size());
	}

	/**
	 * @see OrderService#saveOrder(org.openmrs.Order, OrderContext)
	 */
	@Test
	public void saveOrder_shouldDefaultToCareSettingAndOrderTypeDefinedInTheOrderContextIfNull() {
		Order order = new TestOrder();
		order.setPatient(patientService.getPatient(7));
		Concept trimune30 = conceptService.getConcept(792);
		order.setConcept(trimune30);
		order.setOrderer(providerService.getProvider(1));
		order.setEncounter(encounterService.getEncounter(3));
		order.setDateActivated(new Date());
		OrderType expectedOrderType = orderService.getOrderType(2);
		CareSetting expectedCareSetting = orderService.getCareSetting(1);
		OrderContext orderContext = new OrderContext();
		orderContext.setOrderType(expectedOrderType);
		orderContext.setCareSetting(expectedCareSetting);
		order = orderService.saveOrder(order, orderContext);
		assertFalse(expectedOrderType.getConceptClasses().contains(trimune30.getConceptClass()));
		assertEquals(expectedOrderType, order.getOrderType());
		assertEquals(expectedCareSetting, order.getCareSetting());
	}

	/**
	 * @see OrderService#getDiscontinuationOrder(Order)
	 */
	@Test
	public void getDiscontinuationOrder_shouldReturnDiscontinuationOrderIfOrderHasBeenDiscontinued() {
		Order order = orderService.getOrder(111);
		Order discontinuationOrder = orderService.discontinueOrder(order, "no reason", new Date(),
			providerService.getProvider(1), order.getEncounter());

		Order foundDiscontinuationOrder = orderService.getDiscontinuationOrder(order);

		assertThat(foundDiscontinuationOrder, is(discontinuationOrder));
	}

	/**
	 * @see OrderService#getDiscontinuationOrder(Order)
	 */
	@Test
	public void getDiscontinuationOrder_shouldReturnNullIfOrderHasNotBeenDiscontinued() {
		Order order = orderService.getOrder(111);
		Order discontinuationOrder = orderService.getDiscontinuationOrder(order);

		assertThat(discontinuationOrder, is(nullValue()));
	}

	/**
	 * @see OrderService#getOrderTypeByConceptClass(ConceptClass)
	 */
	@Test
	public void getOrderTypeByConceptClass_shouldGetOrderTypeMappedToTheGivenConceptClass() {
		OrderType orderType = orderService.getOrderTypeByConceptClass(Context.getConceptService().getConceptClass(1));

		assertNotNull(orderType);
		assertEquals(2, orderType.getOrderTypeId().intValue());
	}

	/**
	 * @see OrderService#getOrderTypeByConcept(Concept)
	 */
	@Test
	public void getOrderTypeByConcept_shouldGetOrderTypeMappedToTheGivenConcept() {
		OrderType orderType = orderService.getOrderTypeByConcept(Context.getConceptService().getConcept(5089));

		assertNotNull(orderType);
		assertEquals(2, orderType.getOrderTypeId().intValue());
	}

	/**
	 * @see OrderService#saveOrder(org.openmrs.Order, OrderContext)
	 */
	@Test
	public void saveOrder_shouldFailIfConceptInPreviousOrderDoesNotMatchThatOfTheRevisedOrder() {
		Order previousOrder = orderService.getOrder(7);
		Order order = previousOrder.cloneForRevision();
		order.setDateActivated(new Date());
		order.setOrderer(providerService.getProvider(1));
		order.setEncounter(encounterService.getEncounter(6));
		Concept newConcept = conceptService.getConcept(5089);
		assertFalse(previousOrder.getConcept().equals(newConcept));
		order.setConcept(newConcept);

		EditedOrderDoesNotMatchPreviousException exception = assertThrows(EditedOrderDoesNotMatchPreviousException.class, () -> orderService.saveOrder(order, null));
		assertThat(exception.getMessage(), is("The orderable of the previous order and the new one order don't match"));
	}

	/**
	 * @see OrderService#saveOrder(org.openmrs.Order, OrderContext)
	 */
	@Test
	public void saveOrder_shouldFailIfTheExistingDrugOrderMatchesTheConceptAndNotDrugOfTheRevisedOrder() {
		final DrugOrder orderToDiscontinue = (DrugOrder) orderService.getOrder(5);

		//create a different test drug
		Drug discontinuationOrderDrug = new Drug();
		discontinuationOrderDrug.setConcept(orderToDiscontinue.getConcept());
		discontinuationOrderDrug = conceptService.saveDrug(discontinuationOrderDrug);
		assertNotEquals(discontinuationOrderDrug, orderToDiscontinue.getDrug());
		assertNotNull(orderToDiscontinue.getDrug());

		DrugOrder order = orderToDiscontinue.cloneForRevision();
		order.setDateActivated(new Date());
		order.setOrderer(providerService.getProvider(1));
		order.setEncounter(encounterService.getEncounter(6));
		order.setDrug(discontinuationOrderDrug);

		EditedOrderDoesNotMatchPreviousException exception = assertThrows(EditedOrderDoesNotMatchPreviousException.class, () -> orderService.saveOrder(order, null));
		assertThat(exception.getMessage(), is("The orderable of the previous order and the new one order don't match"));
	}

	/**
	 * @see OrderService#saveOrder(org.openmrs.Order, OrderContext)
	 */
	@Test
	public void saveOrder_shouldFailIfTheOrderTypeOfThePreviousOrderDoesNotMatch() {
		Order order = orderService.getOrder(7);
		assertTrue(OrderUtilTest.isActiveOrder(order, null));
		Order discontinuationOrder = order.cloneForDiscontinuing();
		OrderType orderType = orderService.getOrderType(7);
		assertNotEquals(discontinuationOrder.getOrderType(), orderType);
		assertTrue(OrderUtil.isType(discontinuationOrder.getOrderType(), orderType));
		discontinuationOrder.setOrderType(orderType);
		discontinuationOrder.setOrderer(Context.getProviderService().getProvider(1));
		discontinuationOrder.setEncounter(Context.getEncounterService().getEncounter(6));

		EditedOrderDoesNotMatchPreviousException exception = assertThrows(EditedOrderDoesNotMatchPreviousException.class, () -> orderService.saveOrder(discontinuationOrder, null));
		assertThat(exception.getMessage(), is(messageSourceService.getMessage("Order.type.doesnot.match")));
	}

	/**
	 * @see OrderService#saveOrder(org.openmrs.Order, OrderContext)
	 */
	@Test
	public void saveOrder_shouldFailIfTheJavaTypeOfThePreviousOrderDoesNotMatch() throws Exception {

		HibernateSessionFactoryBean sessionFactoryBean = (HibernateSessionFactoryBean) applicationContext
			.getBean("&sessionFactory");
		Configuration configuration = sessionFactoryBean.getConfiguration();

		HibernateAdministrationDAO adminDAO = (HibernateAdministrationDAO) applicationContext.getBean("adminDAO");
		StandardServiceRegistry standardRegistry = new StandardServiceRegistryBuilder()
			.configure().applySettings(configuration.getProperties()).build();

		Metadata metaData = new MetadataSources(standardRegistry).addAnnotatedClass(Allergy.class)
			.addAnnotatedClass(Encounter.class).addAnnotatedClass(SomeTestOrder.class)
			.addAnnotatedClass(Diagnosis.class).addAnnotatedClass(Condition.class)
			.addAnnotatedClass(Visit.class).addAnnotatedClass(VisitAttributeType.class)
			.addAnnotatedClass(MedicationDispense.class)
			.addAnnotatedClass(ProviderAttributeType.class).addAnnotatedClass(ConceptMapType.class).getMetadataBuilder().build();


		Field field = adminDAO.getClass().getDeclaredField("metadata");
		field.setAccessible(true);
		field.set(adminDAO, metaData);

		Order order = orderService.getOrder(7);
		assertTrue(OrderUtilTest.isActiveOrder(order, null));
		Order discontinuationOrder = new SomeTestOrder();
		discontinuationOrder.setCareSetting(order.getCareSetting());
		discontinuationOrder.setConcept(order.getConcept());
		discontinuationOrder.setAction(Action.DISCONTINUE);
		discontinuationOrder.setPreviousOrder(order);
		discontinuationOrder.setPatient(order.getPatient());
		assertTrue(order.getOrderType().getJavaClass().isAssignableFrom(discontinuationOrder.getClass()));
		discontinuationOrder.setOrderType(order.getOrderType());
		discontinuationOrder.setOrderer(Context.getProviderService().getProvider(1));
		discontinuationOrder.setEncounter(Context.getEncounterService().getEncounter(6));

		EditedOrderDoesNotMatchPreviousException exception = assertThrows(EditedOrderDoesNotMatchPreviousException.class, () -> orderService.saveOrder(discontinuationOrder, null));
		assertThat(exception.getMessage(), is(messageSourceService.getMessage("Order.class.doesnot.match")));
	}

	/**
	 * @see OrderService#saveOrder(org.openmrs.Order, OrderContext)
	 */
	@Test
	public void saveOrder_shouldFailIfTheCareSettingOfThePreviousOrderDoesNotMatch() {
		Order order = orderService.getOrder(7);
		assertTrue(OrderUtilTest.isActiveOrder(order, null));
		Order discontinuationOrder = order.cloneForDiscontinuing();
		CareSetting careSetting = orderService.getCareSetting(2);
		assertNotEquals(discontinuationOrder.getCareSetting(), careSetting);
		discontinuationOrder.setCareSetting(careSetting);
		discontinuationOrder.setOrderer(Context.getProviderService().getProvider(1));
		discontinuationOrder.setEncounter(Context.getEncounterService().getEncounter(6));

		EditedOrderDoesNotMatchPreviousException exception = assertThrows(EditedOrderDoesNotMatchPreviousException.class, () -> orderService.saveOrder(discontinuationOrder, null));
		assertThat(exception.getMessage(), is(messageSourceService.getMessage("Order.care.setting.doesnot.match")));
	}

	/**
	 * @see OrderService#saveOrder(org.openmrs.Order, OrderContext)
	 */
	@Test
	public void saveOrder_shouldSetConceptForDrugOrdersIfNull() {
		Patient patient = patientService.getPatient(7);
		CareSetting careSetting = orderService.getCareSetting(2);
		OrderType orderType = orderService.getOrderTypeByName("Drug order");

		//place drug order
		DrugOrder order = new DrugOrder();
		Encounter encounter = encounterService.getEncounter(3);
		order.setEncounter(encounter);
		order.setPatient(patient);
		order.setDrug(conceptService.getDrug(2));
		order.setCareSetting(careSetting);
		order.setOrderer(Context.getProviderService().getProvider(1));
		order.setDateActivated(encounter.getEncounterDatetime());
		order.setOrderType(orderType);
		order.setDosingType(FreeTextDosingInstructions.class);
		order.setInstructions("None");
		order.setDosingInstructions("Test Instruction");
		orderService.saveOrder(order, null);
		assertNotNull(order.getOrderId());
	}

	/**
	 * @see org.openmrs.api.OrderService#getDrugRoutes()
	 */
	@Test
	public void getDrugRoutes_shouldGetDrugRoutesAssociatedConceptPrividedInGlobalProperties() {
		List<Concept> drugRoutesList = orderService.getDrugRoutes();
		assertEquals(1, drugRoutesList.size());
		assertEquals(22, drugRoutesList.get(0).getConceptId().intValue());
	}

	/**
	 * @see OrderService#voidOrder(org.openmrs.Order, String)
	 */
	@Test
	public void voidOrder_shouldVoidAnOrder() {
		Order order = orderService.getOrder(1);
		assertFalse(order.getVoided());
		assertNull(order.getDateVoided());
		assertNull(order.getVoidedBy());
		assertNull(order.getVoidReason());

		orderService.voidOrder(order, "None");
		assertTrue(order.getVoided());
		assertNotNull(order.getDateVoided());
		assertNotNull(order.getVoidedBy());
		assertNotNull(order.getVoidReason());
	}

	/**
	 * @see OrderService#voidOrder(org.openmrs.Order, String)
	 */
	@Test
	public void voidOrder_shouldUnsetDateStoppedOfThePreviousOrderIfTheSpecifiedOrderIsADiscontinuation() {
		Order order = orderService.getOrder(22);
		assertEquals(Action.DISCONTINUE, order.getAction());
		Order previousOrder = order.getPreviousOrder();
		assertNotNull(previousOrder.getDateStopped());
		assertFalse(order.getVoided());

		orderService.voidOrder(order, "None");
		//Ensures order interceptor is okay with all the changes
		Context.flushSession();
		assertTrue(order.getVoided());
		assertNull(previousOrder.getDateStopped());
	}

	/**
	 * @see OrderService#voidOrder(org.openmrs.Order, String)
	 */
	@Test
	public void voidOrder_shouldUnsetDateStoppedOfThePreviousOrderIfTheSpecifiedOrderIsARevision() {
		Order order = orderService.getOrder(111);
		assertEquals(Action.REVISE, order.getAction());
		Order previousOrder = order.getPreviousOrder();
		assertNotNull(previousOrder.getDateStopped());
		assertFalse(order.getVoided());

		orderService.voidOrder(order, "None");
		Context.flushSession();
		assertTrue(order.getVoided());
		assertNull(previousOrder.getDateStopped());
	}

	/**
	 * @see OrderService#unvoidOrder(org.openmrs.Order)
	 */
	@Test
	public void unvoidOrder_shouldUnvoidAnOrder() {
		Order order = orderService.getOrder(8);
		assertTrue(order.getVoided());
		assertNotNull(order.getDateVoided());
		assertNotNull(order.getVoidedBy());
		assertNotNull(order.getVoidReason());

		orderService.unvoidOrder(order);
		assertFalse(order.getVoided());
		assertNull(order.getDateVoided());
		assertNull(order.getVoidedBy());
		assertNull(order.getVoidReason());
	}

	/**
	 * @see OrderService#unvoidOrder(org.openmrs.Order)
	 */
	@Test
	public void unvoidOrder_shouldStopThePreviousOrderIfTheSpecifiedOrderIsADiscontinuation() {
		Order order = orderService.getOrder(22);
		assertEquals(Action.DISCONTINUE, order.getAction());
		Order previousOrder = order.getPreviousOrder();
		assertNotNull(previousOrder.getDateStopped());
		assertFalse(order.getVoided());

		//void the DC order for testing purposes so we can unvoid it later
		orderService.voidOrder(order, "None");
		Context.flushSession();
		assertTrue(order.getVoided());
		assertNull(previousOrder.getDateStopped());

		orderService.unvoidOrder(order);
		Context.flushSession();
		assertFalse(order.getVoided());
		assertNotNull(previousOrder.getDateStopped());
	}

	/**
	 * @see OrderService#unvoidOrder(org.openmrs.Order)
	 */
	@Test
	public void unvoidOrder_shouldStopThePreviousOrderIfTheSpecifiedOrderIsARevision() {
		Order order = orderService.getOrder(111);
		assertEquals(Action.REVISE, order.getAction());
		Order previousOrder = order.getPreviousOrder();
		assertNotNull(previousOrder.getDateStopped());
		assertFalse(order.getVoided());

		//void the revise order for testing purposes so we can unvoid it later
		orderService.voidOrder(order, "None");
		Context.flushSession();
		assertTrue(order.getVoided());
		assertNull(previousOrder.getDateStopped());

		orderService.unvoidOrder(order);
		Context.flushSession();
		assertFalse(order.getVoided());
		assertNotNull(previousOrder.getDateStopped());
	}

	/**
	 * @throws InterruptedException
	 * @see OrderService#unvoidOrder(org.openmrs.Order)
	 */
	@Test
	public void unvoidOrder_shouldFailForADiscontinuationOrderIfThePreviousOrderIsInactive() throws InterruptedException {
		Order order = orderService.getOrder(22);
		assertEquals(Action.DISCONTINUE, order.getAction());
		Order previousOrder = order.getPreviousOrder();
		assertNotNull(previousOrder.getDateStopped());
		assertFalse(order.getVoided());

		//void the DC order for testing purposes so we can unvoid it later
		orderService.voidOrder(order, "None");
		assertTrue(order.getVoided());
		assertNull(previousOrder.getDateStopped());

		//stop the order with a different DC order
		orderService.discontinueOrder(previousOrder, "Testing", null, previousOrder.getOrderer(),
			previousOrder.getEncounter());
		Thread.sleep(10);

		CannotUnvoidOrderException exception = assertThrows(CannotUnvoidOrderException.class, () -> orderService.unvoidOrder(order));
		assertThat(exception.getMessage(), is(messageSourceService.getMessage("Order.action.cannot.unvoid", new Object[]{"discontinuation"}, null)));
	}

	/**
	 * @throws InterruptedException
	 * @see OrderService#unvoidOrder(org.openmrs.Order)
	 */
	@Test
	public void unvoidOrder_shouldFailForAReviseOrderIfThePreviousOrderIsInactive() throws InterruptedException {
		Order order = orderService.getOrder(111);
		assertEquals(Action.REVISE, order.getAction());
		Order previousOrder = order.getPreviousOrder();
		assertNotNull(previousOrder.getDateStopped());
		assertFalse(order.getVoided());

		//void the DC order for testing purposes so we can unvoid it later
		orderService.voidOrder(order, "None");
		assertTrue(order.getVoided());
		assertNull(previousOrder.getDateStopped());

		//stop the order with a different REVISE order
		Order revise = previousOrder.cloneForRevision();
		revise.setOrderer(order.getOrderer());
		revise.setEncounter(order.getEncounter());
		orderService.saveOrder(revise, null);
		Thread.sleep(10);

		CannotUnvoidOrderException exception = assertThrows(CannotUnvoidOrderException.class, () -> orderService.unvoidOrder(order));
		assertThat(exception.getMessage(), is(messageSourceService.getMessage("Order.action.cannot.unvoid", new Object[]{"revision"}, null)));
	}

	/**
	 * @see OrderService#getRevisionOrder(org.openmrs.Order)
	 */
	@Test
	public void getRevisionOrder_shouldReturnRevisionOrderIfOrderHasBeenRevised() {
		assertEquals(orderService.getOrder(111), orderService.getRevisionOrder(orderService.getOrder(1)));
	}

	/**
	 * @see OrderService#getRevisionOrder(org.openmrs.Order)
	 */
	@Test
	public void getRevisionOrder_shouldReturnNullIfOrderHasNotBeenRevised() {
		assertNull(orderService.getRevisionOrder(orderService.getOrder(444)));
	}

	/**
	 * @see OrderService#getDiscontinuationOrder(Order)
	 */
	@Test
	public void getDiscontinuationOrder_shouldReturnNullIfDcOrderIsVoided() {
		Order order = orderService.getOrder(7);
		Order discontinueOrder = orderService.discontinueOrder(order, "Some reason", new Date(),
			providerService.getProvider(1), encounterService.getEncounter(3));
		orderService.voidOrder(discontinueOrder, "Invalid reason");

		Order discontinuationOrder = orderService.getDiscontinuationOrder(order);
		assertThat(discontinuationOrder, is(nullValue()));
	}

	/**
	 * @see OrderService#getDrugDispensingUnits()
	 */
	@Test
	public void getDrugDispensingUnits_shouldReturnTheUnionOfTheDosingAndDispensingUnits() {
		List<Concept> dispensingUnits = orderService.getDrugDispensingUnits();
		assertEquals(2, dispensingUnits.size());
		assertThat(dispensingUnits, containsInAnyOrder(hasId(50), hasId(51)));
	}

	/**
	 * @see OrderService#getDrugDispensingUnits()
	 */
	@Test
	public void getDrugDispensingUnits_shouldReturnAnEmptyListIfNothingIsConfigured() {
		adminService.saveGlobalProperty(new GlobalProperty(OpenmrsConstants.GP_DRUG_DISPENSING_UNITS_CONCEPT_UUID, ""));
		adminService.saveGlobalProperty(new GlobalProperty(OpenmrsConstants.GP_DRUG_DOSING_UNITS_CONCEPT_UUID, ""));
		assertThat(orderService.getDrugDispensingUnits(), is(empty()));
	}

	/**
	 * @see OrderService#getDrugDosingUnits()
	 */
	@Test
	public void getDrugDosingUnits_shouldReturnAListIfGPIsSet() {
		List<Concept> dosingUnits = orderService.getDrugDosingUnits();
		assertEquals(2, dosingUnits.size());
		assertThat(dosingUnits, containsInAnyOrder(hasId(50), hasId(51)));
	}

	/**
	 * @see OrderService#getDrugDosingUnits()
	 */
	@Test
	public void getDrugDosingUnits_shouldReturnAnEmptyListIfNothingIsConfigured() {
		adminService.saveGlobalProperty(new GlobalProperty(OpenmrsConstants.GP_DRUG_DOSING_UNITS_CONCEPT_UUID, ""));
		assertThat(orderService.getDrugDosingUnits(), is(empty()));
	}

	/**
	 * @see OrderService#getDurationUnits()
	 */
	@Test
	public void getDurationUnits_shouldReturnAListIfGPIsSet() {
		List<Concept> durationConcepts = orderService.getDurationUnits();
		assertEquals(1, durationConcepts.size());
		assertEquals(28, durationConcepts.get(0).getConceptId().intValue());
	}

	/**
	 * @see OrderService#getDurationUnits()
	 */
	@Test
	public void getDurationUnits_shouldReturnAnEmptyListIfNothingIsConfigured() {
		adminService.saveGlobalProperty(new GlobalProperty(OpenmrsConstants.GP_DURATION_UNITS_CONCEPT_UUID, ""));
		assertThat(orderService.getDurationUnits(), is(empty()));
	}

	/**
	 * @see OrderService#getRevisionOrder(org.openmrs.Order)
	 */
	@Test
	public void getRevisionOrder_shouldNotReturnAVoidedRevisionOrder() {
		Order order = orderService.getOrder(7);
		Order revision1 = order.cloneForRevision();
		revision1.setEncounter(order.getEncounter());
		revision1.setOrderer(order.getOrderer());
		orderService.saveOrder(revision1, null);
		assertEquals(revision1, orderService.getRevisionOrder(order));
		orderService.voidOrder(revision1, "Testing");
		assertThat(orderService.getRevisionOrder(order), is(nullValue()));

		//should return the new unvoided revision
		Order revision2 = order.cloneForRevision();
		revision2.setEncounter(order.getEncounter());
		revision2.setOrderer(order.getOrderer());
		orderService.saveOrder(revision2, null);
		assertEquals(revision2, orderService.getRevisionOrder(order));
	}

	/**
	 * @see OrderService#saveOrder(org.openmrs.Order, OrderContext)
	 */
	@Test
	public void saveOrder_shouldPassForADiscontinuationOrderWithNoPreviousOrder() {
		TestOrder dcOrder = new TestOrder();
		dcOrder.setAction(Action.DISCONTINUE);
		dcOrder.setPatient(patientService.getPatient(2));
		dcOrder.setCareSetting(orderService.getCareSetting(2));
		dcOrder.setConcept(conceptService.getConcept(5089));
		dcOrder.setEncounter(encounterService.getEncounter(6));
		dcOrder.setOrderer(providerService.getProvider(1));
		orderService.saveOrder(dcOrder, null);
	}

	/**
	 * @see OrderService#getTestSpecimenSources()
	 */
	@Test
	public void getTestSpecimenSources_shouldReturnAListIfGPIsSet() {
		List<Concept> specimenSourceList = orderService.getTestSpecimenSources();
		assertEquals(1, specimenSourceList.size());
		assertEquals(22, specimenSourceList.get(0).getConceptId().intValue());
	}

	/**
	 * @see OrderService#getTestSpecimenSources()
	 */
	@Test
	public void getTestSpecimenSources_shouldReturnAnEmptyListIfNothingIsConfigured() {
		adminService.saveGlobalProperty(new GlobalProperty(OpenmrsConstants.GP_TEST_SPECIMEN_SOURCES_CONCEPT_UUID, ""));
		assertThat(orderService.getTestSpecimenSources(), is(empty()));
	}

	/**
	 * @see OrderService#retireOrderType(org.openmrs.OrderType, String)
	 */
	@Test
	public void retireOrderType_shouldNotRetireIndependentField() {
		OrderType orderType = orderService.getOrderType(2);
		ConceptClass conceptClass = conceptService.getConceptClass(1);
		assertFalse(conceptClass.getRetired());
		orderType.addConceptClass(conceptClass);
		orderService.retireOrderType(orderType, "test retire reason");
		assertFalse(conceptClass.getRetired());
	}

	/**
	 * @see OrderService#saveOrder(org.openmrs.Order, OrderContext)
	 */
	@Test
	public void saveOrder_shouldSetOrderTypeOfDrugOrderToDrugOrderIfNotSetAndConceptNotMapped() {
		Drug drug = conceptService.getDrug(2);
		Concept unmappedConcept = conceptService.getConcept(113);

		assertNull(orderService.getOrderTypeByConcept(unmappedConcept));
		drug.setConcept(unmappedConcept);

		DrugOrder drugOrder = new DrugOrder();
		Encounter encounter = encounterService.getEncounter(3);
		drugOrder.setEncounter(encounter);
		drugOrder.setPatient(patientService.getPatient(7));
		drugOrder.setCareSetting(orderService.getCareSetting(1));
		drugOrder.setOrderer(Context.getProviderService().getProvider(1));
		drugOrder.setDateActivated(encounter.getEncounterDatetime());
		drugOrder.setDrug(drug);
		drugOrder.setDosingType(SimpleDosingInstructions.class);
		drugOrder.setDose(300.0);
		drugOrder.setDoseUnits(conceptService.getConcept(50));
		drugOrder.setQuantity(20.0);
		drugOrder.setQuantityUnits(conceptService.getConcept(51));
		drugOrder.setFrequency(orderService.getOrderFrequency(3));
		drugOrder.setRoute(conceptService.getConcept(22));
		drugOrder.setNumRefills(10);
		drugOrder.setOrderType(null);

		orderService.saveOrder(drugOrder, null);
		assertNotNull(drugOrder.getOrderType());
		assertEquals(orderService.getOrderTypeByUuid(OrderType.DRUG_ORDER_TYPE_UUID), drugOrder.getOrderType());
	}

	/**
	 * @see OrderService#saveOrder(org.openmrs.Order, OrderContext)
	 */
	@Test
	public void saveOrder_shouldSetOrderTypeOfTestOrderToTestOrderIfNotSetAndConceptNotMapped() {
		TestOrder testOrder = new TestOrder();
		testOrder.setPatient(patientService.getPatient(7));
		Concept unmappedConcept = conceptService.getConcept(113);

		assertNull(orderService.getOrderTypeByConcept(unmappedConcept));
		testOrder.setConcept(unmappedConcept);
		testOrder.setOrderer(providerService.getProvider(1));
		testOrder.setCareSetting(orderService.getCareSetting(1));
		Encounter encounter = encounterService.getEncounter(3);
		testOrder.setEncounter(encounter);
		testOrder.setDateActivated(encounter.getEncounterDatetime());
		testOrder.setClinicalHistory("Patient had a negative reaction to the test in the past");
		testOrder.setFrequency(orderService.getOrderFrequency(3));
		testOrder.setSpecimenSource(conceptService.getConcept(22));
		testOrder.setNumberOfRepeats(3);

		orderService.saveOrder(testOrder, null);
		assertNotNull(testOrder.getOrderType());
		assertEquals(orderService.getOrderTypeByUuid(OrderType.TEST_ORDER_TYPE_UUID), testOrder.getOrderType());
	}

	@Test
	public void saveOrder_shouldSetAutoExpireDateOfDrugOrderIfAutoExpireDateIsNotSet() throws ParseException {
		executeDataSet("org/openmrs/api/include/OrderServiceTest-drugOrderAutoExpireDate.xml");
		Drug drug = conceptService.getDrug(3000);
		DrugOrder drugOrder = new DrugOrder();
		Encounter encounter = encounterService.getEncounter(3);
		drugOrder.setEncounter(encounter);
		drugOrder.setPatient(patientService.getPatient(7));
		drugOrder.setCareSetting(orderService.getCareSetting(1));
		drugOrder.setOrderer(Context.getProviderService().getProvider(1));
		drugOrder.setDrug(drug);
		drugOrder.setDosingType(SimpleDosingInstructions.class);
		drugOrder.setDose(300.0);
		drugOrder.setDoseUnits(conceptService.getConcept(50));
		drugOrder.setQuantity(20.0);
		drugOrder.setQuantityUnits(conceptService.getConcept(51));
		drugOrder.setFrequency(orderService.getOrderFrequency(3));
		drugOrder.setRoute(conceptService.getConcept(22));
		drugOrder.setNumRefills(0);
		drugOrder.setOrderType(null);
		drugOrder.setDateActivated(TestUtil.createDateTime("2014-08-03"));
		drugOrder.setDuration(20);// 20 days
		drugOrder.setDurationUnits(conceptService.getConcept(1001));

		Order savedOrder = orderService.saveOrder(drugOrder, null);

		Order loadedOrder = orderService.getOrder(savedOrder.getId());
		assertEquals(TestUtil.createDateTime("2014-08-22 23:59:59"), loadedOrder.getAutoExpireDate());
	}

	@Test
	public void saveOrder_shouldSetAutoExpireDateForReviseOrderWithSimpleDosingInstructions() {
		executeDataSet("org/openmrs/api/include/OrderServiceTest-drugOrderAutoExpireDate.xml");
		DrugOrder originalOrder = (DrugOrder) orderService.getOrder(111);
		assertTrue(originalOrder.isActive());
		DrugOrder revisedOrder = originalOrder.cloneForRevision();
		revisedOrder.setOrderer(originalOrder.getOrderer());
		revisedOrder.setEncounter(originalOrder.getEncounter());

		revisedOrder.setNumRefills(0);
		revisedOrder.setAutoExpireDate(null);
		revisedOrder.setDuration(10);
		revisedOrder.setDurationUnits(conceptService.getConcept(1001));

		orderService.saveOrder(revisedOrder, null);

		assertNotNull(revisedOrder.getAutoExpireDate());
	}

	/**
	 * @see OrderServiceImpl#discontinueExistingOrdersIfNecessary()
	 */
	@Test
	public void saveOrder_shouldThrowAmbiguousOrderExceptionIfDisconnectingMultipleActiveOrdersForTheGivenConcepts() {
		executeDataSet("org/openmrs/api/include/OrderServiceTest-discontinueAmbiguousOrderByConcept.xml");
		DrugOrder order = new DrugOrder();
		order.setAction(Order.Action.DISCONTINUE);
		order.setOrderReasonNonCoded("Discontinue this");
		order.setConcept(conceptService.getConcept(88));
		order.setEncounter(encounterService.getEncounter(7));
		order.setPatient(patientService.getPatient(9));
		order.setOrderer(providerService.getProvider(1));
		order.setCareSetting(orderService.getCareSetting(1));
		assertThrows(AmbiguousOrderException.class, () -> orderService.saveOrder(order, null));
	}

	/**
	 * @see OrderServiceImpl#discontinueExistingOrdersIfNecessary()
	 */
	@Test
	public void saveOrder_shouldThrowAmbiguousOrderExceptionIfDisconnectingMultipleActiveDrugOrdersWithTheSameDrug() {
		executeDataSet("org/openmrs/api/include/OrderServiceTest-ambiguousDrugOrders.xml");
		DrugOrder order = new DrugOrder();
		order.setAction(Order.Action.DISCONTINUE);
		order.setOrderReasonNonCoded("Discontinue this");
		order.setDrug(conceptService.getDrug(3));
		order.setEncounter(encounterService.getEncounter(7));
		order.setPatient(patientService.getPatient(9));
		order.setOrderer(providerService.getProvider(1));
		order.setCareSetting(orderService.getCareSetting(1));
		assertThrows(AmbiguousOrderException.class, () -> orderService.saveOrder(order, null));
	}

	/**
	 * @see OrderService#saveOrder(org.openmrs.Order, OrderContext, org.openmrs.Order[])
	 */
	@Test
	public void saveOrder_shouldPassIfAnKnownDrugOrderForTheSameDrugFormulationSpecified() {
		executeDataSet("org/openmrs/api/include/OrderServiceTest-drugOrdersWithSameConceptAndDifferentFormAndStrength.xml");
		final Patient patient = patientService.getPatient(2);
		//sanity check that we have an active order for the same concept
		DrugOrder existingOrder = (DrugOrder) orderService.getOrder(1000);
		assertTrue(existingOrder.isActive());

		//New Drug order
		DrugOrder order = new DrugOrder();
		order.setPatient(patient);
		order.setDrug(existingOrder.getDrug());
		order.setEncounter(encounterService.getEncounter(6));
		order.setOrderer(providerService.getProvider(1));
		order.setCareSetting(existingOrder.getCareSetting());
		order.setDosingType(FreeTextDosingInstructions.class);
		order.setDosingInstructions("2 for 5 days");
		order.setQuantity(10.0);
		order.setQuantityUnits(conceptService.getConcept(51));
		order.setNumRefills(2);
		OrderContext orderContext = new OrderContext();
		orderContext.setAttribute(OrderService.PARALLEL_ORDERS, new String[]{existingOrder.getUuid()});
		orderService.saveOrder(order, orderContext);
		assertNotNull(orderService.getOrder(order.getOrderId()));
	}

	/**
	 * @see OrderService#getNonCodedDrugConcept()
	 */
	@Test
	public void getNonCodedDrugConcept_shouldReturnNullIfNothingIsConfigured() {
		adminService.saveGlobalProperty(new GlobalProperty(OpenmrsConstants.GP_DRUG_ORDER_DRUG_OTHER, ""));
		assertNull(orderService.getNonCodedDrugConcept());
	}

	/**
	 * @see OrderService#getNonCodedDrugConcept()
	 */
	@Test
	public void getNonCodedDrugConcept_shouldReturnAConceptIfGPIsSet() {
		executeDataSet("org/openmrs/api/include/OrderServiceTest-nonCodedDrugs.xml");
		Concept nonCodedDrugConcept = orderService.getNonCodedDrugConcept();
		assertNotNull(nonCodedDrugConcept);
		assertThat(nonCodedDrugConcept.getConceptId(), is(5584));
		assertEquals(nonCodedDrugConcept.getName().getName(), "DRUG OTHER");

	}

	/**
	 * @see OrderService#saveOrder(Order, OrderContext)
	 */
	@Test
	public void saveOrder_shouldPassIfAnActiveDrugOrderForTheSameConceptAndDifferentDrugNonCodedExists() {
		executeDataSet("org/openmrs/api/include/OrderServiceTest-nonCodedDrugs.xml");
		final Concept nonCodedConcept = orderService.getNonCodedDrugConcept();
		//sanity check that we have an active order for the same concept
		DrugOrder duplicateOrder = (DrugOrder) orderService.getOrder(584);
		assertTrue(duplicateOrder.isActive());
		assertEquals(nonCodedConcept, duplicateOrder.getConcept());

		DrugOrder drugOrder = duplicateOrder.copy();
		drugOrder.setDrugNonCoded("non coded drug paracetemol");

		Order savedOrder = orderService.saveOrder(drugOrder, null);
		assertNotNull(orderService.getOrder(savedOrder.getOrderId()));
	}

	/**
	 * @see OrderService#saveOrder(Order, OrderContext)
	 */
	@Test
	public void saveOrder_shouldFailIfAnActiveDrugOrderForTheSameConceptAndDrugNonCodedAndCareSettingExists() {
		executeDataSet("org/openmrs/api/include/OrderServiceTest-nonCodedDrugs.xml");
		final Concept nonCodedConcept = orderService.getNonCodedDrugConcept();
		//sanity check that we have an active order for the same concept
		DrugOrder duplicateOrder = (DrugOrder) orderService.getOrder(584);
		assertTrue(duplicateOrder.isActive());
		assertEquals(nonCodedConcept, duplicateOrder.getConcept());

		DrugOrder drugOrder = duplicateOrder.copy();
		drugOrder.setDrugNonCoded("non coded drug crocine");

		AmbiguousOrderException exception = assertThrows(AmbiguousOrderException.class, () -> orderService.saveOrder(drugOrder, null));
		assertThat(exception.getMessage(), is("Order.cannot.have.more.than.one"));
	}

	@Test
	public void saveOrder_shouldDiscontinuePreviousNonCodedOrderIfItIsNotAlreadyDiscontinued() {
		//We are trying to discontinue order id 584 in OrderServiceTest-nonCodedDrugs.xml
		executeDataSet("org/openmrs/api/include/OrderServiceTest-nonCodedDrugs.xml");
		DrugOrder previousOrder = (DrugOrder) orderService.getOrder(584);
		DrugOrder drugOrder = previousOrder.cloneForDiscontinuing();
		drugOrder.setPreviousOrder(previousOrder);
		drugOrder.setDateActivated(new Date());
		drugOrder.setOrderer(previousOrder.getOrderer());
		drugOrder.setEncounter(previousOrder.getEncounter());

		Order saveOrder = orderService.saveOrder(drugOrder, null);
		assertNotNull(previousOrder.getDateStopped(), "previous order should be discontinued");
		assertNotNull(orderService.getOrder(saveOrder.getOrderId()));
	}

	@Test
	public void saveOrder_shouldFailDiscontinueNonCodedDrugOrderIfOrderableOfPreviousAndNewOrderDontMatch() {
		executeDataSet("org/openmrs/api/include/OrderServiceTest-nonCodedDrugs.xml");
		DrugOrder previousOrder = (DrugOrder) orderService.getOrder(584);
		DrugOrder drugOrder = previousOrder.cloneForDiscontinuing();
		drugOrder.setDrugNonCoded("non coded drug citrigine");
		drugOrder.setPreviousOrder(previousOrder);
		drugOrder.setDateActivated(new Date());
		drugOrder.setOrderer(providerService.getProvider(1));
		drugOrder.setEncounter(encounterService.getEncounter(6));

		EditedOrderDoesNotMatchPreviousException exception = assertThrows(EditedOrderDoesNotMatchPreviousException.class, () -> orderService.saveOrder(drugOrder, null));
		assertThat(exception.getMessage(), is("The orderable of the previous order and the new one order don't match"));
	}

	@Test
	public void saveOrder_shouldFailIfDrugNonCodedInPreviousDrugOrderDoesNotMatchThatOfTheRevisedDrugOrder() {
		executeDataSet("org/openmrs/api/include/OrderServiceTest-nonCodedDrugs.xml");
		DrugOrder previousOrder = (DrugOrder) orderService.getOrder(584);
		DrugOrder order = previousOrder.cloneForRevision();
		String drugNonCodedParacetemol = "non coded aspirin";

		order.setDateActivated(new Date());
		order.setOrderer(providerService.getProvider(1));
		order.setEncounter(encounterService.getEncounter(6));
		assertFalse(previousOrder.getDrugNonCoded().equals(drugNonCodedParacetemol));
		order.setDrugNonCoded(drugNonCodedParacetemol);
		order.setPreviousOrder(previousOrder);

		EditedOrderDoesNotMatchPreviousException exception = assertThrows(EditedOrderDoesNotMatchPreviousException.class, () -> orderService.saveOrder(order, null));
		assertThat(exception.getMessage(), is("The orderable of the previous order and the new one order don't match"));
	}

	@Test
	public void saveOrder_shouldRevisePreviousNonCodedOrderIfItIsAlreadyExisting() {
		//We are trying to discontinue order id 584 in OrderServiceTest-nonCodedDrugs.xml
		executeDataSet("org/openmrs/api/include/OrderServiceTest-nonCodedDrugs.xml");
		DrugOrder previousOrder = (DrugOrder) orderService.getOrder(584);
		DrugOrder order = previousOrder.cloneForRevision();

		order.setDateActivated(new Date());
		order.setOrderer(providerService.getProvider(1));
		order.setEncounter(encounterService.getEncounter(6));
		order.setAsNeeded(true);
		order.setPreviousOrder(previousOrder);

		DrugOrder saveOrder = (DrugOrder) orderService.saveOrder(order, null);
		assertTrue(saveOrder.getAsNeeded());
		assertNotNull(orderService.getOrder(saveOrder.getOrderId()));
	}

	@Test
	public void saveRetrospectiveOrder_shouldDiscontinueOrderInRetrospectiveEntry() throws ParseException {
		executeDataSet("org/openmrs/api/include/OrderServiceTest-ordersWithAutoExpireDate.xml");
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.S");
		Date originalOrderDateActivated = dateFormat.parse("2008-11-19 09:24:10.0");
		Date discontinuationOrderDate = DateUtils.addDays(originalOrderDateActivated, 2);

		Order originalOrder = orderService.getOrder(201);
		assertNull(originalOrder.getDateStopped());
		assertEquals(dateFormat.parse("2008-11-23 09:24:09.0"), originalOrder.getAutoExpireDate());
		assertFalse(originalOrder.isActive());
		assertTrue(originalOrder.isActive(discontinuationOrderDate));

		Order discontinueationOrder = originalOrder.cloneForDiscontinuing();
		discontinueationOrder.setPreviousOrder(originalOrder);
		discontinueationOrder.setEncounter(encounterService.getEncounter(17));
		discontinueationOrder.setOrderer(providerService.getProvider(1));
		discontinueationOrder.setDateActivated(discontinuationOrderDate);
		orderService.saveRetrospectiveOrder(discontinueationOrder, null);

		assertNotNull(originalOrder.getDateStopped());
		assertEquals(discontinueationOrder.getAutoExpireDate(), discontinueationOrder.getDateActivated());
	}

	@Test
	public void saveRetrospectiveOrder_shouldDiscontinueAndStopActiveOrderInRetrospectiveEntry() throws ParseException {
		executeDataSet("org/openmrs/api/include/OrderServiceTest-ordersWithAutoExpireDate.xml");
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.S");
		Date originalOrderDateActivated = dateFormat.parse("2008-11-19 09:24:10.0");
		Date discontinuationOrderDate = DateUtils.addDays(originalOrderDateActivated, 2);

		Order originalOrder = orderService.getOrder(202);
		assertNull(originalOrder.getDateStopped());
		assertEquals(dateFormat.parse("2008-11-23 09:24:09.0"), originalOrder.getAutoExpireDate());
		assertFalse(originalOrder.isActive());
		assertTrue(originalOrder.isActive(discontinuationOrderDate));

		Order discontinuationOrder = originalOrder.cloneForDiscontinuing();
		discontinuationOrder.setPreviousOrder(null);
		discontinuationOrder.setEncounter(encounterService.getEncounter(17));
		discontinuationOrder.setOrderer(providerService.getProvider(1));
		discontinuationOrder.setDateActivated(discontinuationOrderDate);
		orderService.saveRetrospectiveOrder(discontinuationOrder, null);

		assertNotNull(originalOrder.getDateStopped());
		assertEquals(discontinuationOrder.getAutoExpireDate(), discontinuationOrder.getDateActivated());
	}

	@Test
	public void saveOrder_shouldNotRevisePreviousIfAlreadyStopped() throws ParseException {
		executeDataSet("org/openmrs/api/include/OrderServiceTest-ordersWithAutoExpireDate.xml");
		Order previousOrder = orderService.getOrder(203);
		Date dateActivated = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse("2008-10-19 13:00:00");
		Order order = previousOrder.cloneForRevision();

		order.setDateActivated(dateActivated);
		order.setOrderer(providerService.getProvider(1));
		order.setEncounter(encounterService.getEncounter(18));
		order.setPreviousOrder(previousOrder);

		CannotStopInactiveOrderException exception = assertThrows(CannotStopInactiveOrderException.class, () -> orderService.saveRetrospectiveOrder(order, null));
		assertThat(exception.getMessage(), is(messageSourceService.getMessage("Order.cannot.discontinue.inactive")));
	}

	@Test
	public void saveRetrospectiveOrder_shouldFailIfAnActiveDrugOrderForTheSameConceptAndCareSettingExistsAtOrderDateActivated()
		throws ParseException {
		executeDataSet("org/openmrs/api/include/OrderServiceTest-ordersWithAutoExpireDate.xml");
		Date newOrderDateActivated = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse("2008-11-19 13:00:10");
		final Patient patient = patientService.getPatient(12);
		final Concept orderConcept = conceptService.getConcept(88);
		//sanity check that we have an active order for the same concept
		DrugOrder duplicateOrder = (DrugOrder) orderService.getOrder(202);
		assertTrue(duplicateOrder.isActive(newOrderDateActivated));
		assertEquals(orderConcept, duplicateOrder.getConcept());

		DrugOrder order = new DrugOrder();
		order.setPatient(patient);
		order.setConcept(orderConcept);
		order.setEncounter(encounterService.getEncounter(17));
		order.setOrderer(providerService.getProvider(1));
		order.setCareSetting(duplicateOrder.getCareSetting());
		order.setDateActivated(newOrderDateActivated);
		order.setDrug(duplicateOrder.getDrug());
		order.setDose(duplicateOrder.getDose());
		order.setDoseUnits(duplicateOrder.getDoseUnits());
		order.setRoute(duplicateOrder.getRoute());
		order.setFrequency(duplicateOrder.getFrequency());
		order.setQuantity(duplicateOrder.getQuantity());
		order.setQuantityUnits(duplicateOrder.getQuantityUnits());
		order.setNumRefills(duplicateOrder.getNumRefills());

		AmbiguousOrderException exception = assertThrows(AmbiguousOrderException.class, () -> orderService.saveRetrospectiveOrder(order, null));
		assertThat(exception.getMessage(), is("Order.cannot.have.more.than.one"));
	}

	@Test
	public void shouldSaveOrdersWithSortWeightWhenWithinAOrderGroup() {
		executeDataSet(ORDER_SET);

		Encounter encounter = encounterService.getEncounter(3);

		OrderSet orderSet = Context.getOrderSetService().getOrderSet(2000);
		OrderGroup orderGroup = new OrderGroup();
		orderGroup.setOrderSet(orderSet);
		orderGroup.setPatient(encounter.getPatient());

		orderGroup.setEncounter(encounter);

		Order firstOrderWithOrderGroup = new OrderBuilder().withAction(Order.Action.NEW).withPatient(7).withConcept(1000)
			.withCareSetting(1).withOrderer(1).withEncounter(3).withDateActivated(new Date()).withOrderType(17)
			.withUrgency(Order.Urgency.ON_SCHEDULED_DATE).withScheduledDate(new Date()).withOrderGroup(orderGroup)
			.build();

		Order secondOrderWithOrderGroup = new OrderBuilder().withAction(Order.Action.NEW).withPatient(7).withConcept(1001)
			.withCareSetting(1).withOrderer(1).withEncounter(3).withDateActivated(new Date()).withOrderType(17)
			.withUrgency(Order.Urgency.ON_SCHEDULED_DATE).withScheduledDate(new Date()).withOrderGroup(orderGroup)
			.build();

		Order orderWithoutOrderGroup = new OrderBuilder().withAction(Order.Action.NEW).withPatient(7).withConcept(1000)
			.withCareSetting(1).withOrderer(1).withEncounter(3).withDateActivated(new Date()).withOrderType(17)
			.withUrgency(Order.Urgency.ON_SCHEDULED_DATE).withScheduledDate(new Date()).build();

		Set<Order> orders = new LinkedHashSet<>();
		orders.add(firstOrderWithOrderGroup);
		orders.add(secondOrderWithOrderGroup);
		orders.add(orderWithoutOrderGroup);

		encounter.setOrders(orders);

		for (OrderGroup og : encounter.getOrderGroups()) {
			if (og.getId() == null) {
				Context.getOrderService().saveOrderGroup(og);
			}
		}

		for (Order o : encounter.getOrdersWithoutOrderGroups()) {
			if (o.getId() == null) {
				Context.getOrderService().saveOrder(o, null);
			}
		}

		Context.flushSession();

		OrderGroup savedOrderGroup = Context.getOrderService().getOrderGroupByUuid(orderGroup.getUuid());
		Order savedOrder = Context.getOrderService().getOrderByUuid(orderWithoutOrderGroup.getUuid());

		assertEquals(firstOrderWithOrderGroup.getUuid(), savedOrderGroup.getOrders().get(0).getUuid(),
			"The first order in  savedOrderGroup is the same which is sent first in the List");

		assertEquals(secondOrderWithOrderGroup.getUuid(), savedOrderGroup.getOrders().get(1).getUuid(),
			"The second order in  savedOrderGroup is the same which is sent second in the List");
		assertNull(savedOrder.getSortWeight(), "The order which doesn't belong to an orderGroup has no sortWeight");
		assertThat("The first order has a lower sortWeight than the second", savedOrderGroup.getOrders().get(0)
			.getSortWeight().compareTo(savedOrderGroup.getOrders().get(1).getSortWeight()), is(-1));
	}

	@Test
	public void shouldSetTheCorrectSortWeightWhenAddingAnOrderInOrderGroup() {
		executeDataSet(ORDER_SET);

		Encounter encounter = encounterService.getEncounter(3);

		OrderSet orderSet = Context.getOrderSetService().getOrderSet(2000);
		OrderGroup orderGroup = new OrderGroup();
		orderGroup.setOrderSet(orderSet);
		orderGroup.setPatient(encounter.getPatient());

		orderGroup.setEncounter(encounter);

		Order firstOrderWithOrderGroup = new OrderBuilder().withAction(Order.Action.NEW).withPatient(7).withConcept(1000)
			.withCareSetting(1).withOrderer(1).withEncounter(3).withDateActivated(new Date()).withOrderType(17)
			.withUrgency(Order.Urgency.ON_SCHEDULED_DATE).withScheduledDate(new Date()).withOrderGroup(orderGroup)
			.build();

		Order secondOrderWithOrderGroup = new OrderBuilder().withAction(Order.Action.NEW).withPatient(7).withConcept(1001)
			.withCareSetting(1).withOrderer(1).withEncounter(3).withDateActivated(new Date()).withOrderType(17)
			.withUrgency(Order.Urgency.ON_SCHEDULED_DATE).withScheduledDate(new Date()).withOrderGroup(orderGroup)
			.build();

		Set<Order> orders = new LinkedHashSet<>();
		orders.add(firstOrderWithOrderGroup);
		orders.add(secondOrderWithOrderGroup);

		encounter.setOrders(orders);

		for (OrderGroup og : encounter.getOrderGroups()) {
			if (og.getId() == null) {
				Context.getOrderService().saveOrderGroup(og);
			}
		}

		Context.flushSession();

		OrderGroup savedOrderGroup = Context.getOrderService().getOrderGroupByUuid(orderGroup.getUuid());
		assertEquals(firstOrderWithOrderGroup.getUuid(), savedOrderGroup.getOrders().get(0).getUuid(),
			"The first order in  savedOrderGroup is the same which is sent first in the List");

		assertEquals(secondOrderWithOrderGroup.getUuid(), savedOrderGroup.getOrders().get(1).getUuid(),
			"The second order in  savedOrderGroup is the same which is sent second in the List");
		assertThat("The first order has a lower sortWeight than the second", savedOrderGroup.getOrders().get(0)
			.getSortWeight().compareTo(savedOrderGroup.getOrders().get(1).getSortWeight()), is(-1));

		Order newOrderWithoutAnyPosition = new OrderBuilder().withAction(Order.Action.NEW).withPatient(7).withConcept(1000)
			.withCareSetting(1).withOrderer(1).withEncounter(3).withDateActivated(new Date()).withOrderType(17)
			.withUrgency(Order.Urgency.ON_SCHEDULED_DATE).withScheduledDate(new Date()).withOrderGroup(savedOrderGroup)
			.build();

		savedOrderGroup.addOrder(newOrderWithoutAnyPosition);

		Context.getOrderService().saveOrderGroup(savedOrderGroup);
		Context.flushSession();

		OrderGroup secondSavedOrderGroup = Context.getOrderService().getOrderGroupByUuid(orderGroup.getUuid());

		assertEquals(firstOrderWithOrderGroup.getUuid(), savedOrderGroup.getOrders().get(0).getUuid(), "The first order in  savedOrderGroup is the same which is sent first in the List");

		assertEquals(secondOrderWithOrderGroup.getUuid(), savedOrderGroup.getOrders().get(1).getUuid(), "The second order in  savedOrderGroup is the same which is sent second in the List");

		assertEquals(secondSavedOrderGroup.getOrders().get(2).getUuid(), newOrderWithoutAnyPosition.getUuid(), "The third order in  savedOrderGroup is the same which is sent third in the List");

		assertThat("The third order has a higher sortWeight than the second", savedOrderGroup.getOrders().get(2)
			.getSortWeight().compareTo(savedOrderGroup.getOrders().get(1).getSortWeight()), is(1));
	}

	@Test
	public void shouldSetTheCorrectSortWeightWhenAddingAnOrderAtAPosition() {
		executeDataSet(ORDER_SET);

		Encounter encounter = encounterService.getEncounter(3);

		OrderSet orderSet = Context.getOrderSetService().getOrderSet(2000);
		OrderGroup orderGroup = new OrderGroup();
		orderGroup.setOrderSet(orderSet);
		orderGroup.setPatient(encounter.getPatient());
		orderGroup.setEncounter(encounter);

		Order firstOrderWithOrderGroup = new OrderBuilder().withAction(Order.Action.NEW).withPatient(7).withConcept(1000)
			.withCareSetting(1).withOrderer(1).withEncounter(3).withDateActivated(new Date()).withOrderType(17)
			.withUrgency(Order.Urgency.ON_SCHEDULED_DATE).withScheduledDate(new Date()).withOrderGroup(orderGroup)
			.build();

		Order secondOrderWithOrderGroup = new OrderBuilder().withAction(Order.Action.NEW).withPatient(7).withConcept(1001)
			.withCareSetting(1).withOrderer(1).withEncounter(3).withDateActivated(new Date()).withOrderType(17)
			.withUrgency(Order.Urgency.ON_SCHEDULED_DATE).withScheduledDate(new Date()).withOrderGroup(orderGroup)
			.build();

		Set<Order> orders = new LinkedHashSet<>();
		orders.add(firstOrderWithOrderGroup);
		orders.add(secondOrderWithOrderGroup);

		encounter.setOrders(orders);

		for (OrderGroup og : encounter.getOrderGroups()) {
			if (og.getId() == null) {
				Context.getOrderService().saveOrderGroup(og);
			}
		}

		Context.flushSession();

		OrderGroup savedOrderGroup = Context.getOrderService().getOrderGroupByUuid(orderGroup.getUuid());
		assertEquals(firstOrderWithOrderGroup.getUuid(), savedOrderGroup.getOrders().get(0).getUuid(),
			"The first order in  savedOrderGroup is the same which is sent first in the List");

		assertEquals(secondOrderWithOrderGroup.getUuid(), savedOrderGroup.getOrders().get(1).getUuid(),
			"The second order in  savedOrderGroup is the same which is sent second in the List");
		assertThat("The first order has a lower sortWeight than the second", savedOrderGroup.getOrders().get(0)
			.getSortWeight().compareTo(savedOrderGroup.getOrders().get(1).getSortWeight()), is(-1));

		Order newOrderAtPosition1 = new OrderBuilder().withAction(Order.Action.NEW).withPatient(7).withConcept(1000)
			.withCareSetting(1).withOrderer(1).withEncounter(3).withDateActivated(new Date()).withOrderType(17)
			.withUrgency(Order.Urgency.ON_SCHEDULED_DATE).withScheduledDate(new Date()).withOrderGroup(savedOrderGroup)
			.build();

		Order newOrderAtPosition2 = new OrderBuilder().withAction(Order.Action.NEW).withPatient(7).withConcept(1000)
			.withCareSetting(1).withOrderer(1).withEncounter(3).withDateActivated(new Date()).withOrderType(17)
			.withUrgency(Order.Urgency.ON_SCHEDULED_DATE).withScheduledDate(new Date()).withOrderGroup(savedOrderGroup)
			.build();

		savedOrderGroup.addOrder(newOrderAtPosition1, 0);
		savedOrderGroup.addOrder(newOrderAtPosition2, 1);

		Context.getOrderService().saveOrderGroup(savedOrderGroup);

		OrderGroup secondSavedOrderGroup = Context.getOrderService().getOrderGroupByUuid(orderGroup.getUuid());
		assertEquals(4, savedOrderGroup.getOrders().size());

		assertEquals(newOrderAtPosition1.getUuid(), secondSavedOrderGroup.getOrders().get(0).getUuid(), "The first order in  savedOrderGroup is the same which is sent first in the List");

		assertEquals(newOrderAtPosition2.getUuid(), secondSavedOrderGroup.getOrders().get(1).getUuid(), "The second order in  savedOrderGroup is the same which is sent second in the List");

		assertEquals(firstOrderWithOrderGroup.getUuid(), savedOrderGroup.getOrders().get(2).getUuid(), "The third order in  savedOrderGroup is the same which is sent third in the List");

		assertEquals(secondOrderWithOrderGroup.getUuid(), savedOrderGroup.getOrders().get(3).getUuid(), "The fourth order in  savedOrderGroup is the same which is sent first in the List");

		assertThat("The third order has a lower sortWeight than the fourth", savedOrderGroup.getOrders().get(2)
			.getSortWeight().compareTo(savedOrderGroup.getOrders().get(3).getSortWeight()), is(-1));
		assertThat("The second order has a lower sortWeight than the third", savedOrderGroup.getOrders().get(1)
			.getSortWeight().compareTo(savedOrderGroup.getOrders().get(2).getSortWeight()), is(-1));
		assertThat("The first order has a lower sortWeight than the second", savedOrderGroup.getOrders().get(0)
			.getSortWeight().compareTo(savedOrderGroup.getOrders().get(1).getSortWeight()), is(-1));
	}

	@Test
	public void shouldSetTheCorrectSortWeightWhenAddingAnOrderWithANegativePosition() {
		executeDataSet(ORDER_SET);

		Encounter encounter = encounterService.getEncounter(3);

		OrderSet orderSet = Context.getOrderSetService().getOrderSet(2000);
		OrderGroup orderGroup = new OrderGroup();
		orderGroup.setOrderSet(orderSet);
		orderGroup.setPatient(encounter.getPatient());
		orderGroup.setEncounter(encounter);

		Order firstOrderWithOrderGroup = new OrderBuilder().withAction(Order.Action.NEW).withPatient(7).withConcept(1000)
			.withCareSetting(1).withOrderer(1).withEncounter(3).withDateActivated(new Date()).withOrderType(17)
			.withUrgency(Order.Urgency.ON_SCHEDULED_DATE).withScheduledDate(new Date()).withOrderGroup(orderGroup)
			.build();

		Order secondOrderWithOrderGroup = new OrderBuilder().withAction(Order.Action.NEW).withPatient(7).withConcept(1001)
			.withCareSetting(1).withOrderer(1).withEncounter(3).withDateActivated(new Date()).withOrderType(17)
			.withUrgency(Order.Urgency.ON_SCHEDULED_DATE).withScheduledDate(new Date()).withOrderGroup(orderGroup)
			.build();

		Set<Order> orders = new LinkedHashSet<>();
		orders.add(firstOrderWithOrderGroup);
		orders.add(secondOrderWithOrderGroup);

		encounter.setOrders(orders);

		for (OrderGroup og : encounter.getOrderGroups()) {
			if (og.getId() == null) {
				Context.getOrderService().saveOrderGroup(og);
			}
		}

		Context.flushSession();

		OrderGroup savedOrderGroup = Context.getOrderService().getOrderGroupByUuid(orderGroup.getUuid());

		Order newOrderWithNegativePosition = new OrderBuilder().withAction(Order.Action.NEW).withPatient(7)
			.withConcept(1000).withCareSetting(1).withOrderer(1).withEncounter(3).withDateActivated(new Date())
			.withOrderType(17).withUrgency(Order.Urgency.ON_SCHEDULED_DATE).withScheduledDate(new Date())
			.withOrderGroup(savedOrderGroup).build();

		savedOrderGroup.addOrder(newOrderWithNegativePosition, -1);

		Context.getOrderService().saveOrderGroup(savedOrderGroup);
		Context.flushSession();

		OrderGroup secondSavedOrderGroup = Context.getOrderService().getOrderGroupByUuid(orderGroup.getUuid());
		assertEquals(3, secondSavedOrderGroup.getOrders().size());

		assertEquals(newOrderWithNegativePosition.getUuid(), secondSavedOrderGroup.getOrders().get(2).getUuid(),
			"The new order gets added at the last position");

		assertThat("The new order has a higher sortWeight than the second", secondSavedOrderGroup.getOrders().get(2)
			.getSortWeight().compareTo(secondSavedOrderGroup.getOrders().get(1).getSortWeight()), is(1));

		Order newOrderWithInvalidPosition = new OrderBuilder().withAction(Order.Action.NEW).withPatient(7).withConcept(1000)
			.withCareSetting(1).withOrderer(1).withEncounter(3).withDateActivated(new Date()).withOrderType(17)
			.withUrgency(Order.Urgency.ON_SCHEDULED_DATE).withScheduledDate(new Date()).withOrderGroup(savedOrderGroup)
			.build();
		APIException exception = assertThrows(APIException.class, () -> secondSavedOrderGroup.addOrder(newOrderWithInvalidPosition, secondSavedOrderGroup.getOrders().size() + 1));
		assertThat(exception.getMessage(), is("Cannot add a member which is out of range of the list"));
	}

	/**
	 * @see OrderService#saveOrder(Order, OrderContext)
	 */
	@Test
	public void saveOrderGroup_shouldFailValidationIfAnyOrdersFailValidation() {
		executeDataSet(ORDER_SET);

		Encounter encounter = encounterService.getEncounter(3);
		OrderContext context = new OrderContext();

		// First we confirm that saving a Drug Order on it's own with missing required fields will fail validation

		DrugOrder drugOrder = new DrugOrderBuilder().withPatient(encounter.getPatient().getPatientId())
			.withEncounter(encounter.getEncounterId()).withCareSetting(1).withOrderer(1)
			.withOrderType(1).withDrug(2)
			.withUrgency(Order.Urgency.ROUTINE).withDateActivated(new Date())
			.build();

		Exception expectedValidationError = null;
		try {
			Context.getOrderService().saveOrder(drugOrder, context);
		} catch (Exception e) {
			expectedValidationError = e;
		}

		assertNotNull(expectedValidationError);
		assertEquals(ValidationException.class, expectedValidationError.getClass());
		assertTrue(expectedValidationError.getMessage().contains("Dose is required"));

		// Next, add this to an Order Group and save it within that group, and it should also fail

		OrderSet orderSet = Context.getOrderSetService().getOrderSet(2000);
		OrderGroup orderGroup = new OrderGroup();
		orderGroup.setOrderSet(orderSet);
		orderGroup.setPatient(encounter.getPatient());
		orderGroup.setEncounter(encounter);
		orderGroup.addOrder(drugOrder);
		drugOrder.setOrderGroup(orderGroup);

		Exception expectedGroupValidationError = null;
		try {
			Context.getOrderService().saveOrderGroup(orderGroup);
		} catch (Exception e) {
			expectedGroupValidationError = e;
		}

		assertNotNull(expectedGroupValidationError, "Validation should cause order group to fail to save");
		assertEquals(expectedValidationError.getMessage(), expectedGroupValidationError.getMessage());
	}

	/**
	 * @see OrderService#saveOrder(Order, OrderContext)
	 */
	@Test
	public void saveOrderGroup_shouldSavePreviouslySavedOrderGroup() {
		executeDataSet(ORDER_SET);

		// Create and save initial order group
		Encounter encounter = encounterService.getEncounter(3);
		OrderSet orderSet = Context.getOrderSetService().getOrderSet(2000);
		OrderGroup orderGroup = new OrderGroup();
		orderGroup.setOrderSet(orderSet);
		orderGroup.setPatient(encounter.getPatient());
		orderGroup.setEncounter(encounter);

		Order order = new OrderBuilder().withAction(Order.Action.NEW).withPatient(7).withConcept(1000)
			.withCareSetting(1).withOrderer(1).withEncounter(3).withDateActivated(new Date()).withOrderType(17)
			.withUrgency(Order.Urgency.ON_SCHEDULED_DATE).withScheduledDate(new Date()).withOrderGroup(orderGroup)
			.build();
		orderGroup.addOrder(order);

		Context.getOrderService().saveOrderGroup(orderGroup);
		Integer orderGroupId = orderGroup.getOrderGroupId();
		assertThat(orderGroupId, notNullValue());

		// Re-retrieve this order group, and try to save it
		Context.flushSession();
		Context.clearSession();
		
		orderGroup = Context.getOrderService().getOrderGroup(orderGroupId);
		Context.getOrderService().saveOrderGroup(orderGroup);
	}
	
	@Test
	public void getOrderGroupAttributeTypes_shouldReturnAllOrderGroupAttributeTypes() {
		List<OrderGroupAttributeType> orderGroupAttributeTypes = orderService.getAllOrderGroupAttributeTypes();
		assertEquals(4, orderGroupAttributeTypes.size());
	}
	
	@Test
	public void getOrderGroupAttributeType_shouldReturnNullIfNoOrderGroupAttributeTypeHasTheGivenId() {
		assertNull(orderService.getOrderGroupAttributeType(10));
	}
	
	@Test
	public void getOrderGroupAttributeType_shouldReturnOrderGroupAttributeType() {
		OrderGroupAttributeType orderGroupAttributeType = orderService.getOrderGroupAttributeType(2);
		assertThat(orderGroupAttributeType.getId(), is(2));
	}
	
	@Test
	public void getOrderGroupAttributeTypeByUuid_shouldReturnOrderGroupAttributeTypeByUuid() {
		OrderGroupAttributeType orderGroupAttributeType = orderService
		        .getOrderGroupAttributeTypeByUuid("9cf1bce0-d18e-11ea-87d0-0242ac130003");
		assertEquals("Bacteriology", orderGroupAttributeType.getName());
	}
	
	@Test
	public void saveOrderGroupAttributeType_shouldSaveOrderGroupAttributeTypeGivenOrderGroupAttributeType()
	        throws ParseException {
		int initialGroupOrderAttributeTypeCount = orderService.getAllOrderGroupAttributeTypes().size();
		OrderGroupAttributeType orderGroupAttributeType = new OrderGroupAttributeType();
		orderGroupAttributeType.setName("Surgery");
		orderGroupAttributeType.setDatatypeClassname(FreeTextDatatype.class.getName());
		orderService.saveOrderGroupAttributeType(orderGroupAttributeType);
		assertNotNull(orderGroupAttributeType.getId());
		assertEquals(initialGroupOrderAttributeTypeCount + 1, orderService.getAllOrderGroupAttributeTypes().size());
	}
	
	@Test
	public void saveOrderGroupAttributeType_shouldEditAnExistingOrderGroupAttributeType() {
		//Check for values in the database
		OrderGroupAttributeType orderGroupAttributeType = orderService.getOrderGroupAttributeType(4);
		assertEquals("ECG", orderGroupAttributeType.getName());
		//edit existing values in the database
		orderGroupAttributeType.setName("Laparascopy");
		orderService.saveOrderGroupAttributeType(orderGroupAttributeType);
		//confirm new values are persisted
		assertEquals("Laparascopy", orderGroupAttributeType.getName());
	}
	
	@Test
	public void retireOrderGroupAttributeType_shouldRetireOrderGroupAttributeType() throws ParseException {
		OrderGroupAttributeType orderGroupAttributeType = orderService.getOrderGroupAttributeType(2);
		assertFalse(orderGroupAttributeType.getRetired());
		assertNull(orderGroupAttributeType.getRetiredBy());
		assertNull(orderGroupAttributeType.getRetireReason());
		assertNull(orderGroupAttributeType.getDateRetired());
		orderService.retireOrderGroupAttributeType(orderGroupAttributeType, "Test Retire");
		orderGroupAttributeType = orderService.getOrderGroupAttributeType(2);
		assertTrue(orderGroupAttributeType.getRetired());
		assertNotNull(orderGroupAttributeType.getRetiredBy());
		assertEquals("Test Retire", orderGroupAttributeType.getRetireReason());
		assertNotNull(orderGroupAttributeType.getDateRetired());
	}
	
	@Test
	public void unretireOrderGroupAttributeType_shouldUnretireOrderGroupAttributeType() {
		OrderGroupAttributeType orderGroupAttributeType = orderService.getOrderGroupAttributeType(4);
		assertTrue(orderGroupAttributeType.getRetired());
		assertNotNull(orderGroupAttributeType.getRetiredBy());
		assertNotNull(orderGroupAttributeType.getDateRetired());
		assertNotNull(orderGroupAttributeType.getRetireReason());
		orderService.unretireOrderGroupAttributeType(orderGroupAttributeType);
		assertFalse(orderGroupAttributeType.getRetired());
		assertNull(orderGroupAttributeType.getRetiredBy());
		assertNull(orderGroupAttributeType.getDateRetired());
		assertNull(orderGroupAttributeType.getRetireReason());
	}
	
	@Test
	public void getOrderGroupAttributeTypeByName_shouldReturnOrderGroupAttributeTypeUsingName() {
		OrderGroupAttributeType orderGroupAttributeType = orderService.getOrderGroupAttributeTypeByName("Bacteriology");
		assertEquals("9cf1bce0-d18e-11ea-87d0-0242ac130003", orderGroupAttributeType.getUuid());
	}
	
	@Test
	public void purgeOrderGroupAttributeType_shouldPurgeOrderGroupAttributeType() {
		int initialOrderGroupAttributeTypeCount = orderService.getAllOrderGroupAttributeTypes().size();
		orderService.purgeOrderGroupAttributeType(orderService.getOrderGroupAttributeType(4));
		assertEquals(initialOrderGroupAttributeTypeCount - 1, orderService.getAllOrderGroupAttributeTypes().size());
	}
	
	@Test
	public void getOrderGroupAttributeByUuid_shouldReturnNullIfNonExistingUuidIsProvided() {
		assertNull(orderService.getOrderGroupAttributeTypeByUuid("cbf580ee-d7fb-11ea-87d0-0242ac130003"));
	}
	
	@Test
	public void getOrderGroupAttributeByUuid_shouldReturnOrderGroupAttributeGivenUuid() {
		OrderGroupAttribute orderGroupAttribute = orderService
		        .getOrderGroupAttributeByUuid("86bdcc12-d18d-11ea-87d0-0242ac130003");
		orderGroupAttribute.getValueReference();
		assertEquals("Test 1", orderGroupAttribute.getValueReference());
		assertEquals(1, orderGroupAttribute.getId());
	}

	@Test
	public void saveOrder_shouldAllowARetrospectiveOrderToCloseAnOrderThatExpiredInThePast() throws Exception {
		
		// Ensure that duration units are configured correctly to a snomed duration code
		ConceptReferenceTerm days = new ConceptReferenceTerm();
		days.setConceptSource(conceptService.getConceptSourceByName("SNOMED CT"));
		days.setCode("258703001");
		days.setName("Day(s)");
		conceptService.saveConceptReferenceTerm(days);
		
		Concept daysConcept = conceptService.getConcept(28);
		daysConcept.addConceptMapping(new ConceptMap(days, conceptService.getConceptMapType(2)));
		conceptService.saveConcept(daysConcept);
		
		// First create a retrospective Order on 8/1/2008 with a duration of 60 days.
		// This will set the auto-expire date to 9/29/2008

		Encounter e1 = encounterService.getEncounter(3);
		DrugOrder o1 = new DrugOrderBuilder().withPatient(e1.getPatient().getPatientId())
			.withEncounter(e1.getEncounterId()).withCareSetting(2).withOrderer(1).withUrgency(Order.Urgency.ROUTINE)
			.withDateActivated(e1.getEncounterDatetime())
			.withOrderType(1).withDrug(2)
			.withDosingType(SimpleDosingInstructions.class)
			.build();
		o1.setDose(2d);
		o1.setDoseUnits(conceptService.getConcept(51)); // tab(s)
		o1.setRoute(conceptService.getConcept(22)); // unknown
		o1.setFrequency(orderService.getOrderFrequency(1));
		o1.setDuration(60);
		o1.setDurationUnits(daysConcept); // days
		e1.addOrder(o1);
		encounterService.saveEncounter(e1);
		assertThat(new SimpleDateFormat("yyyy-MM-dd").format(o1.getAutoExpireDate()), is("2008-09-29"));
		assertThat(o1.getDateStopped(), is(nullValue()));

		// Next, create a new Order on 8/15/2008 that revises the above order
		// Encounter 4 is on 8/15/2008 for patient 7
		Encounter e2 = encounterService.getEncounter(4);
		DrugOrder o2 = o1.cloneForRevision();
		o2.setOrderer(providerService.getProvider(1));
		o2.setDateActivated(e2.getEncounterDatetime());
		o2.setDose(3d);
		e2.addOrder(o2);
		encounterService.saveEncounter(e2);
		assertThat(new SimpleDateFormat("yyyy-MM-dd").format(o1.getDateStopped()), is("2008-08-14"));
	}
	
	/**
	 * @see OrderService#saveOrder(org.openmrs.Order, OrderContext)
	 */
	@Test
	public void saveOrder_shouldSaveTheFormNamespaceAndPath() {
		Order order = new TestOrder();
		order.setPatient(patientService.getPatient(7));
		order.setConcept(conceptService.getConcept(5497));
		order.setOrderer(providerService.getProvider(1));
		order.setCareSetting(orderService.getCareSetting(1));
		order.setOrderType(orderService.getOrderType(2));
		order.setEncounter(encounterService.getEncounter(3));
		order.setDateActivated(new Date());
		
		final String NAMESPACE = "namespace";
		final String FORMFIELD_PATH = "formFieldPath";
		order.setFormField(NAMESPACE, FORMFIELD_PATH);
		
		order = orderService.saveOrder(order, null);
		assertEquals(NAMESPACE + "^" + FORMFIELD_PATH, order.getFormNamespaceAndPath());
	}

	@Test
	public void getAllOrderAttributeTypes_shouldReturnAllOrderAttributeTypes() {
		assertThat(orderService.getAllOrderAttributeTypes(), hasSize(4));
	}

	@Test
	public void getOrderAttributeTypeById_shouldReturnNullIfNoOrderAttributeTypeHasTheProvidedId() {
		assertNull(orderService.getOrderAttributeTypeById(15));
	}

	@Test
	public void getOrderAttributeTypeById_shouldReturnOrderAttributeTypeUsingProvidedId() {
		assertThat(orderService.getOrderAttributeTypeById(2).getId(), is(2));
	}

	@Test
	public void getOrderAttributeTypeByUuid_shouldReturnOrderAttributeTypeUsingProvidedUuid() {
		assertEquals("Referral", orderService.getOrderAttributeTypeByUuid(
				"9758d106-79b0-4f45-8d8c-ae8b3f25d72a").getName());
	}
	
	@Test
	public void saveOrderAttributeType_shouldEditTheExistingOrderAttributeType() {
		OrderAttributeType orderAttributeType = orderService.getOrderAttributeTypeById(4);
		assertEquals("Drug", orderAttributeType.getName());
		orderAttributeType.setName("Drug Dispense");
		orderService.saveOrderAttributeType(orderAttributeType);
		assertThat(orderService.getOrderAttributeTypeById(orderAttributeType.getId()).getName(), is("Drug Dispense"));
	}

	@Test
	public void retireOrderAttributeType_shouldRetireTheProvidedOrderAttributeType() throws ParseException {
		OrderAttributeType orderAttributeType = orderService.getOrderAttributeTypeById(2);
		assertFalse(orderAttributeType.getRetired());
		assertNull(orderAttributeType.getRetiredBy());
		assertNull(orderAttributeType.getRetireReason());
		assertNull(orderAttributeType.getDateRetired());
		orderService.retireOrderAttributeType(orderAttributeType, "Test Retire");
		orderAttributeType = orderService.getOrderAttributeTypeById(orderAttributeType.getId());
		assertTrue(orderAttributeType.getRetired());
		assertNotNull(orderAttributeType.getRetiredBy());
		assertEquals("Test Retire", orderAttributeType.getRetireReason());
		assertNotNull(orderAttributeType.getDateRetired());
	}

	@Test
	public void unretireOrderAttributeType_shouldUnretireTheProvidedOrderAttributeType() {
		OrderAttributeType orderAttributeType = orderService.getOrderAttributeTypeById(4);
		assertTrue(orderAttributeType.getRetired());
		assertNotNull(orderAttributeType.getRetiredBy());
		assertNotNull(orderAttributeType.getDateRetired());
		assertNotNull(orderAttributeType.getRetireReason());
		orderService.unretireOrderAttributeType(orderAttributeType);
		assertFalse(orderAttributeType.getRetired());
		assertNull(orderAttributeType.getRetiredBy());
		assertNull(orderAttributeType.getDateRetired());
		assertNull(orderAttributeType.getRetireReason());
	}
	
	@Test
	public void purgeOrderAttributeType_shouldPurgeTheProvidedOrderAttributeType() {
		final int ORIGINAL_COUNT = orderService.getAllOrderAttributeTypes().size();
		orderService.purgeOrderAttributeType(orderService.getOrderAttributeTypeById(3));
		assertNull(orderService.getOrderAttributeTypeById(3));
		assertEquals(ORIGINAL_COUNT - 1, orderService.getAllOrderAttributeTypes().size());
	}

	@Test
	public void getOrderAttributeByUuid_shouldReturnNullIfNonExistingUuidIsProvided() {
		assertNull(orderService.getOrderAttributeByUuid("26bbdf73-4268-4e65-aa72-54cb928870d6"));
	}

	@Test
	public void getOrderAttributeByUuid_shouldReturnOrderAttributeUsingProvidedUuid() {
		OrderAttribute orderAttribute = orderService.getOrderAttributeByUuid("8c3c27e4-030f-410e-86de-a5743b0b3361");
		assertEquals("Testing Reference", orderAttribute.getValueReference());
		assertEquals(1, orderAttribute.getId());
	}
}

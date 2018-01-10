/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.impl;

import static org.openmrs.Order.Action.DISCONTINUE;
import static org.openmrs.Order.Action.REVISE;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.time.DateUtils;
import org.hibernate.proxy.HibernateProxy;
import org.openmrs.CareSetting;
import org.openmrs.Concept;
import org.openmrs.ConceptClass;
import org.openmrs.DrugOrder;
import org.openmrs.Encounter;
import org.openmrs.GlobalProperty;
import org.openmrs.Order;
import org.openmrs.OrderFrequency;
import org.openmrs.OrderGroup;
import org.openmrs.OrderType;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.TestOrder;
import org.openmrs.api.APIException;
import org.openmrs.api.AmbiguousOrderException;
import org.openmrs.api.CannotDeleteObjectInUseException;
import org.openmrs.api.CannotUpdateObjectInUseException;
import org.openmrs.api.GlobalPropertyListener;
import org.openmrs.api.MissingRequiredPropertyException;
import org.openmrs.api.OrderContext;
import org.openmrs.api.OrderNumberGenerator;
import org.openmrs.api.OrderService;
import org.openmrs.api.UnchangeableObjectException;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.OrderDAO;
import org.openmrs.api.CannotStopDiscontinuationOrderException;
import org.openmrs.api.CannotStopInactiveOrderException;
import org.openmrs.api.CannotUnvoidOrderException;
import org.openmrs.api.EditedOrderDoesNotMatchPreviousException;
import org.openmrs.api.OrderEntryException;
import org.openmrs.order.OrderUtil;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * Default implementation of the Order-related services class. This method should not be invoked by
 * itself. Spring injection is used to inject this implementation into the ServiceContext. Which
 * implementation is injected is determined by the spring application context file:
 * /metadata/api/spring/applicationContext.xml
 * 
 * @see org.openmrs.api.OrderService
 */
@Transactional
public class OrderServiceImpl extends BaseOpenmrsService implements OrderService, OrderNumberGenerator, GlobalPropertyListener {
	
	private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);
	
	private static final String ORDER_NUMBER_PREFIX = "ORD-";
	
	protected OrderDAO dao;
	
	private static OrderNumberGenerator orderNumberGenerator = null;

	public OrderServiceImpl() {
	}
	
	/**
	 * @see org.openmrs.api.OrderService#setOrderDAO(org.openmrs.api.db.OrderDAO)
	 */
	@Override
	public void setOrderDAO(OrderDAO dao) {
		this.dao = dao;
	}
	
	/**
	 * @see org.openmrs.api.OrderService#saveOrder(org.openmrs.Order, org.openmrs.api.OrderContext)
	 */
	@Override
	public synchronized Order saveOrder(Order order, OrderContext orderContext) throws APIException {
		return saveOrder(order, orderContext, false);
	}
	
	/**
	 * @see org.openmrs.api.OrderService#saveOrderGroup(org.openmrs.OrderGroup)
	 */
	@Override
	public OrderGroup saveOrderGroup(OrderGroup orderGroup) throws APIException {
		if (orderGroup.getId() == null) {
			dao.saveOrderGroup(orderGroup);
		}
		List<Order> orders = orderGroup.getOrders();
		for (Order order : orders) {
			if (order.getId() == null) {
				saveOrder(order, null);
			}
		}
		return orderGroup;
	}
	
	/**
	 * @see org.openmrs.api.OrderService#saveOrder(org.openmrs.Order, org.openmrs.api.OrderContext)
	 */
	@Override
	public synchronized Order saveRetrospectiveOrder(Order order, OrderContext orderContext) {
		return saveOrder(order, orderContext, true);
	}

	private Order saveOrder(Order order, OrderContext orderContext, boolean isRetrospective) {

		failOnExistingOrder(order);
		ensureDateActivatedIsSet(order);
		ensureConceptIsSet(order);
		ensureDrugOrderAutoExpirationDateIsSet(order);
		ensureOrderTypeIsSet(order,orderContext);
		ensureCareSettingIsSet(order,orderContext);
		failOnOrderTypeMismatch(order);
		
		Order previousOrder = order.getPreviousOrder();
		if (REVISE == order.getAction()) {
			if (previousOrder == null) {
				throw new MissingRequiredPropertyException("Order.previous.required", (Object[]) null);
			}
			stopOrder(previousOrder, aMomentBefore(order.getDateActivated()), isRetrospective);
		} else if (DISCONTINUE == order.getAction()) {
			discontinueExistingOrdersIfNecessary(order, isRetrospective);
		}
		
		if (previousOrder != null) {
			//concept should be the same as on previous order, same applies to drug for drug orders
			if (!order.hasSameOrderableAs(previousOrder)) {
				throw new EditedOrderDoesNotMatchPreviousException("Order.orderable.doesnot.match");
			} else if (!order.getOrderType().equals(previousOrder.getOrderType())) {
				throw new EditedOrderDoesNotMatchPreviousException("Order.type.doesnot.match");
			} else if (!order.getCareSetting().equals(previousOrder.getCareSetting())) {
				throw new EditedOrderDoesNotMatchPreviousException("Order.care.setting.doesnot.match");
			} else if (!getActualType(order).equals(getActualType(previousOrder))) {
				throw new EditedOrderDoesNotMatchPreviousException("Order.class.doesnot.match");
			}
		}
		
		if (DISCONTINUE != order.getAction()) {
			Date asOfDate = new Date();
			if (isRetrospective) {
				asOfDate = order.getDateActivated();
			}
			List<Order> activeOrders = getActiveOrders(order.getPatient(), null, order.getCareSetting(), asOfDate);
			List<String> parallelOrders = Collections.emptyList();
			if (orderContext != null && orderContext.getAttribute(PARALLEL_ORDERS) != null) {
				parallelOrders = Arrays.asList((String[]) orderContext.getAttribute(PARALLEL_ORDERS));
			}
			for (Order activeOrder : activeOrders) {
				//Reject if there is an active drug order for the same orderable with overlapping schedule
				if (!parallelOrders.contains(activeOrder.getUuid())
				        && areDrugOrdersOfSameOrderableAndOverlappingSchedule(order, activeOrder)) {
					throw new AmbiguousOrderException("Order.cannot.have.more.than.one");
				}
			}
		}
		return saveOrderInternal(order, orderContext);
	}

	private void failOnExistingOrder(Order order) {
		if (order.getOrderId() != null) {
			throw new UnchangeableObjectException("Order.cannot.edit.existing");
		}
	}

	private void ensureDateActivatedIsSet(Order order) {
		if (order.getDateActivated() == null) {
			order.setDateActivated(new Date());
		}
	}

	private void ensureConceptIsSet(Order order) {
		Concept concept = order.getConcept();
		if (concept == null && isDrugOrder(order)) {
			DrugOrder drugOrder = (DrugOrder) order;
			if (drugOrder.getDrug() != null) {
				concept = drugOrder.getDrug().getConcept();
				drugOrder.setConcept(concept);
			}
		}
		if (concept == null) {
			throw new MissingRequiredPropertyException("Order.concept.required");
		}
	}

	private void ensureDrugOrderAutoExpirationDateIsSet(Order order) {
		if (isDrugOrder(order)) {
			((DrugOrder) order).setAutoExpireDateBasedOnDuration();
		}
	}

	private void ensureOrderTypeIsSet(Order order, OrderContext orderContext) {
		if (order.getOrderType() != null) {
			return;
		}
		OrderType orderType = null;
		if (orderContext != null) {
			orderType = orderContext.getOrderType();
		}
		if (orderType == null) {
			orderType = getOrderTypeByConcept(order.getConcept());
		}
		if (orderType == null && order instanceof DrugOrder) {
			orderType = Context.getOrderService().getOrderTypeByUuid(OrderType.DRUG_ORDER_TYPE_UUID);
		}
		if (orderType == null && order instanceof TestOrder) {
			orderType = Context.getOrderService().getOrderTypeByUuid(OrderType.TEST_ORDER_TYPE_UUID);
		}
		if (orderType == null) {
			throw new OrderEntryException("Order.type.cannot.determine");
		}
		Order previousOrder = order.getPreviousOrder();
		if (previousOrder != null && !orderType.equals(previousOrder.getOrderType())) {
			throw new OrderEntryException("Order.type.does.not.match");
		}
		order.setOrderType(orderType);
	}

	private void ensureCareSettingIsSet(Order order, OrderContext orderContext) {
		if (order.getCareSetting() != null) {
			return;
		}
		CareSetting careSetting = null;
		if (orderContext != null) {
			careSetting = orderContext.getCareSetting();
		}
		Order previousOrder = order.getPreviousOrder();
		if (careSetting == null || (previousOrder != null && !careSetting.equals(previousOrder.getCareSetting()))) {
			throw new OrderEntryException("Order.care.cannot.determine");
		}
		order.setCareSetting(careSetting);
	}

	private void failOnOrderTypeMismatch(Order order) {
		if (!order.getOrderType().getJavaClass().isAssignableFrom(order.getClass())) {
			throw new OrderEntryException("Order.type.class.does.not.match", new Object[] {
					order.getOrderType().getJavaClass(), order.getClass().getName() });
		}
	}

	private boolean areDrugOrdersOfSameOrderableAndOverlappingSchedule(Order firstOrder, Order secondOrder) {
		return firstOrder.hasSameOrderableAs(secondOrder)
		        && !OpenmrsUtil.nullSafeEquals(firstOrder.getPreviousOrder(), secondOrder)
		        && OrderUtil.checkScheduleOverlap(firstOrder, secondOrder)
		        && firstOrder.getOrderType().equals(
		            Context.getOrderService().getOrderTypeByUuid(OrderType.DRUG_ORDER_TYPE_UUID));
	}

	private boolean isDrugOrder(Order order) {
		return DrugOrder.class.isAssignableFrom(getActualType(order));
	}

	
	/**
	 * To support MySQL datetime values (which are only precise to the second) we subtract one
	 * second. Eventually we may move this method and enhance it to subtract the smallest moment the
	 * underlying database will represent.
	 * 
	 * @param date
	 * @return one moment before date
	 */
	private Date aMomentBefore(Date date) {
		return DateUtils.addSeconds(date, -1);
	}
	
	private Order saveOrderInternal(Order order, OrderContext orderContext) {
		if (order.getOrderId() == null) {
			setProperty(order, "orderNumber", getOrderNumberGenerator().getNewOrderNumber(orderContext));
			
			//DC orders should auto expire upon creating them
			if (DISCONTINUE == order.getAction()) {
				order.setAutoExpireDate(order.getDateActivated());
			} else if (order.getAutoExpireDate() != null) {
				Calendar cal = Calendar.getInstance();
				cal.setTime(order.getAutoExpireDate());
				int hours = cal.get(Calendar.HOUR_OF_DAY);
				int minutes = cal.get(Calendar.MINUTE);
				int seconds = cal.get(Calendar.SECOND);
				cal.get(Calendar.MILLISECOND);
				//roll autoExpireDate to end of day (23:59:59) if no time portion is specified
				if (hours == 0 && minutes == 0 && seconds == 0) {
					cal.set(Calendar.HOUR_OF_DAY, 23);
					cal.set(Calendar.MINUTE, 59);
					cal.set(Calendar.SECOND, 59);
					// the OpenMRS database is only precise to the second
					cal.set(Calendar.MILLISECOND, 0);
					order.setAutoExpireDate(cal.getTime());
				}
			}
		}
		
		return dao.saveOrder(order);
	}
	
	private void setProperty(Order order, String propertyName, Object value) {
		Boolean isAccessible = null;
		Field field = null;
		try {
			field = Order.class.getDeclaredField(propertyName);
			field.setAccessible(true);
			field.set(order, value);
		}
		catch (Exception e) {
			throw new APIException("Order.failed.set.property", new Object[] { propertyName, order }, e);
		}
		finally {
			if (field != null && isAccessible != null) {
				field.setAccessible(isAccessible);
			}
		}
	}
	
	/**
	 * Gets the configured order number generator, if none is specified, it defaults to an instance
	 * if this class
	 * 
	 * @return
	 */
	private OrderNumberGenerator getOrderNumberGenerator() {
		if (orderNumberGenerator == null) {
			String generatorBeanId = Context.getAdministrationService().getGlobalProperty(
			    OpenmrsConstants.GP_ORDER_NUMBER_GENERATOR_BEAN_ID);
			if (StringUtils.hasText(generatorBeanId)) {
				orderNumberGenerator = Context.getRegisteredComponent(generatorBeanId, OrderNumberGenerator.class);
				log.info("Successfully set the configured order number generator");
			} else {
				orderNumberGenerator = this;
				log.info("Setting default order number generator");
			}
		}
		
		return orderNumberGenerator;
	}
	
	/**
	 * If this is a discontinue order, ensure that the previous order is discontinued. If a
	 * previousOrder is present, then ensure this is discontinued. If no previousOrder is present,
	 * then try to find a previousOrder and discontinue it. If cannot find a previousOrder, throw
	 * exception
	 * 
	 * @param order
	 * @param isRetrospective
	 */
	//Ignore and return if this is not an order to discontinue
	private void discontinueExistingOrdersIfNecessary(Order order, Boolean isRetrospective) {
		if (DISCONTINUE != order.getAction()) {
			return;
		}
		
		//Mark previousOrder as discontinued if it is not already
		Order previousOrder = order.getPreviousOrder();
		if (previousOrder != null) {
			stopOrder(previousOrder, aMomentBefore(order.getDateActivated()), isRetrospective);
			return;
		}
		
		//Mark first order found corresponding to this DC order as discontinued.
		Date asOfDate = null;
		if (isRetrospective) {
			asOfDate = order.getDateActivated();
		}
		List<? extends Order> orders = getActiveOrders(order.getPatient(), order.getOrderType(), order.getCareSetting(),
		    asOfDate);
		boolean isDrugOrderAndHasADrug = isDrugOrder(order)
		        && (((DrugOrder) order).getDrug() != null || ((DrugOrder) order).isNonCodedDrug());
		Order orderToBeDiscontinued = null;
		for (Order activeOrder : orders) {
			if (!getActualType(order).equals(getActualType(activeOrder))) {
				continue;
			}
			//For drug orders, the drug must match if the order has a drug
			if (isDrugOrderAndHasADrug) {
				Order existing = order.hasSameOrderableAs(activeOrder) ? activeOrder : null;
				if (existing != null) {
					if (orderToBeDiscontinued == null) {
						orderToBeDiscontinued = existing;
					} else {
						throw new AmbiguousOrderException("Order.discontinuing.ambiguous.orders");
					}
				}
			} else if (activeOrder.getConcept().equals(order.getConcept())) {
				if (orderToBeDiscontinued == null) {
					orderToBeDiscontinued = activeOrder;
				} else {
					throw new AmbiguousOrderException("Order.discontinuing.ambiguous.orders");
				}
			}
		}
		if (orderToBeDiscontinued != null) {
			order.setPreviousOrder(orderToBeDiscontinued);
			stopOrder(orderToBeDiscontinued, aMomentBefore(order.getDateActivated()), isRetrospective);
		}
	}
	
	/**
	 * Returns the class object of the specified persistent object returning the actual persistent
	 * class in case it is a hibernate proxy
	 *
	 * @param persistentObject
	 * @return the Class object
	 */
	private Class<?> getActualType(Object persistentObject) {
		Class<?> type = persistentObject.getClass();
		if (persistentObject instanceof HibernateProxy) {
			type = ((HibernateProxy) persistentObject).getHibernateLazyInitializer().getPersistentClass();
		}
		return type;
	}
	
	/**
	 * @see org.openmrs.api.OrderService#purgeOrder(org.openmrs.Order)
	 */
	@Override
	public void purgeOrder(Order order) throws APIException {
		purgeOrder(order, false);
	}
	
	/**
	 * @see org.openmrs.api.OrderService#purgeOrder(Order)
	 */
	@Override
	public void purgeOrder(Order order, boolean cascade) throws APIException {
		if (cascade) {
			dao.deleteObsThatReference(order);
		}
		
		dao.deleteOrder(order);
	}
	
	/**
	 * @see org.openmrs.api.OrderService#voidOrder(org.openmrs.Order, java.lang.String)
	 */
	@Override
	public Order voidOrder(Order order, String voidReason) throws APIException {
		if (!StringUtils.hasLength(voidReason)) {
			throw new IllegalArgumentException("voidReason cannot be empty or null");
		}
		
		Order previousOrder = order.getPreviousOrder();
		if (previousOrder != null && isDiscontinueOrReviseOrder(order)) {
			setProperty(previousOrder, "dateStopped", null);
		}
		
		return saveOrderInternal(order, null);
	}
	
	/**
	 * @see org.openmrs.api.OrderService#unvoidOrder(org.openmrs.Order)
	 */
	@Override
	public Order unvoidOrder(Order order) throws APIException {
		Order previousOrder = order.getPreviousOrder();
		if (previousOrder != null && isDiscontinueOrReviseOrder(order)) {
			if (!previousOrder.isActive()) {
				final String action = DISCONTINUE == order.getAction() ? "discontinuation" : "revision";
				throw new CannotUnvoidOrderException(action);
			}
			stopOrder(previousOrder, aMomentBefore(order.getDateActivated()), false);
		}
		
		return saveOrderInternal(order, null);
	}
	
	/**
	 * @see org.openmrs.api.OrderService#getOrder(java.lang.Integer)
	 */
	@Override
	@Transactional(readOnly = true)
	public Order getOrder(Integer orderId) throws APIException {
		return dao.getOrder(orderId);
	}
	
	/**
	 * @see OrderService#getOrders(org.openmrs.Patient, org.openmrs.CareSetting,
	 *      org.openmrs.OrderType, boolean)
	 */
	@Override
	public List<Order> getOrders(Patient patient, CareSetting careSetting, OrderType orderType, boolean includeVoided) {
		if (patient == null) {
			throw new IllegalArgumentException("Patient is required");
		}
		if (careSetting == null) {
			throw new IllegalArgumentException("CareSetting is required");
		}
		List<OrderType> orderTypes = null;
		if (orderType != null) {
			orderTypes = new ArrayList<>();
			orderTypes.add(orderType);
			orderTypes.addAll(getSubtypes(orderType, true));
		}
		return dao.getOrders(patient, careSetting, orderTypes, includeVoided, false);
	}
	
	/**
	 * @see OrderService#getAllOrdersByPatient(org.openmrs.Patient)
	 */
	@Override
	public List<Order> getAllOrdersByPatient(Patient patient) {
		if (patient == null) {
			throw new IllegalArgumentException("Patient is required");
		}
		return dao.getOrders(patient, null, null, true, true);
	}
	
	/**
	 * @see org.openmrs.api.OrderService#getOrderByUuid(java.lang.String)
	 */
	@Override
	@Transactional(readOnly = true)
	public Order getOrderByUuid(String uuid) throws APIException {
		return dao.getOrderByUuid(uuid);
	}
	
	/**
	 * @see org.openmrs.api.OrderService#getDiscontinuationOrder(Order)
	 */
	@Transactional(readOnly = true)
	@Override
	public Order getDiscontinuationOrder(Order order) throws APIException {
		return dao.getDiscontinuationOrder(order);
	}
	
	/**
	 * @see org.openmrs.api.OrderService#getRevisionOrder(Order)
	 */
	@Override
	public Order getRevisionOrder(Order order) throws APIException {
		return dao.getRevisionOrder(order);
	}
	
	/**
	 * @see org.openmrs.api.OrderNumberGenerator#getNewOrderNumber(org.openmrs.api.OrderContext)
	 * @param orderContext
	 */
	@Override
	public String getNewOrderNumber(OrderContext orderContext) throws APIException {
		return ORDER_NUMBER_PREFIX + Context.getOrderService().getNextOrderNumberSeedSequenceValue();
	}
	
	/**
	 * @see org.openmrs.api.OrderService#getOrderByOrderNumber(java.lang.String)
	 */
	@Override
	@Transactional(readOnly = true)
	public Order getOrderByOrderNumber(String orderNumber) {
		return dao.getOrderByOrderNumber(orderNumber);
	}
	
	/**
	 * @see org.openmrs.api.OrderService#getOrderHistoryByConcept(org.openmrs.Patient,
	 *      org.openmrs.Concept)
	 */
	@Override
	@Transactional(readOnly = true)
	public List<Order> getOrderHistoryByConcept(Patient patient, Concept concept) {
		if (patient == null || concept == null) {
			throw new IllegalArgumentException("patient and concept are required");
		}
		List<Concept> concepts = new ArrayList<>();
		concepts.add(concept);
		
		List<Patient> patients = new ArrayList<>();
		patients.add(patient);
		
		return dao.getOrders(null, patients, concepts, new ArrayList<>(), new ArrayList<>());
	}
	
	/**
	 * @see org.openmrs.api.OrderService#getNextOrderNumberSeedSequenceValue()
	 */
	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public synchronized Long getNextOrderNumberSeedSequenceValue() {
		return dao.getNextOrderNumberSeedSequenceValue();
	}
	
	/**
	 * @see org.openmrs.api.OrderService#getOrderHistoryByOrderNumber(java.lang.String)
	 */
	@Override
	@Transactional(readOnly = true)
	public List<Order> getOrderHistoryByOrderNumber(String orderNumber) {
		List<Order> orders = new ArrayList<>();
		Order order = dao.getOrderByOrderNumber(orderNumber);
		while (order != null) {
			orders.add(order);
			order = order.getPreviousOrder();
		}
		return orders;
	}
	
	/**
	 * @see org.openmrs.api.OrderService#getActiveOrders(org.openmrs.Patient, org.openmrs.OrderType,
	 *      org.openmrs.CareSetting, java.util.Date)
	 */
	@Override
	@Transactional(readOnly = true)
	public List<Order> getActiveOrders(Patient patient, OrderType orderType, CareSetting careSetting, Date asOfDate) {
		if (patient == null) {
			throw new IllegalArgumentException("Patient is required when fetching active orders");
		}
		if (asOfDate == null) {
			asOfDate = new Date();
		}
		List<OrderType> orderTypes = null;
		if (orderType != null) {
			orderTypes = new ArrayList<>();
			orderTypes.add(orderType);
			orderTypes.addAll(getSubtypes(orderType, true));
		}
		return dao.getActiveOrders(patient, orderTypes, careSetting, asOfDate);
	}
	
	/**
	 * @see org.openmrs.api.OrderService#getCareSetting(Integer)
	 */
	@Override
	public CareSetting getCareSetting(Integer careSettingId) {
		return dao.getCareSetting(careSettingId);
	}
	
	/**
	 * @see org.openmrs.api.OrderService#getCareSettingByUuid(String)
	 */
	@Override
	public CareSetting getCareSettingByUuid(String uuid) {
		return dao.getCareSettingByUuid(uuid);
	}
	
	/**
	 * @see org.openmrs.api.OrderService#getCareSettingByName(String)
	 */
	@Override
	public CareSetting getCareSettingByName(String name) {
		return dao.getCareSettingByName(name);
	}
	
	/**
	 * @see org.openmrs.api.OrderService#getCareSettings(boolean)
	 */
	@Override
	public List<CareSetting> getCareSettings(boolean includeRetired) {
		return dao.getCareSettings(includeRetired);
	}
	
	/**
	 * @see OrderService#getOrderTypeByName(String)
	 */
	@Override
	public OrderType getOrderTypeByName(String orderTypeName) {
		return dao.getOrderTypeByName(orderTypeName);
	}
	
	/**
	 * @see OrderService#getOrderFrequency(Integer)
	 */
	@Override
	public OrderFrequency getOrderFrequency(Integer orderFrequencyId) {
		return dao.getOrderFrequency(orderFrequencyId);
	}
	
	/**
	 * @see OrderService#getOrderFrequencyByUuid(String)
	 */
	@Override
	public OrderFrequency getOrderFrequencyByUuid(String uuid) {
		return dao.getOrderFrequencyByUuid(uuid);
	}
	
	/**
	 * @see OrderService#getOrderFrequencies(boolean)
	 */
	@Override
	public List<OrderFrequency> getOrderFrequencies(boolean includeRetired) {
		return dao.getOrderFrequencies(includeRetired);
	}
	
	/**
	 * @see OrderService#getOrderFrequencies(String, java.util.Locale, boolean, boolean)
	 */
	@Override
	public List<OrderFrequency> getOrderFrequencies(String searchPhrase, Locale locale, boolean exactLocale,
	                                                boolean includeRetired) {
		if (searchPhrase == null) {
			throw new IllegalArgumentException("searchPhrase is required");
		}
		return dao.getOrderFrequencies(searchPhrase, locale, exactLocale, includeRetired);
	}
	
	/**
	 * @see org.openmrs.api.OrderService#discontinueOrder(org.openmrs.Order, org.openmrs.Concept,
	 *      java.util.Date, org.openmrs.Provider, org.openmrs.Encounter)
	 */
	@Override
	public Order discontinueOrder(Order orderToDiscontinue, Concept reasonCoded, Date discontinueDate, Provider orderer,
	                              Encounter encounter) {
		if (discontinueDate == null) {
			discontinueDate = aMomentBefore(new Date());
		}
		stopOrder(orderToDiscontinue, discontinueDate, false);
		Order newOrder = orderToDiscontinue.cloneForDiscontinuing();
		newOrder.setOrderReason(reasonCoded);
		newOrder.setOrderer(orderer);
		newOrder.setEncounter(encounter);
		newOrder.setDateActivated(discontinueDate);
		return saveOrderInternal(newOrder, null);
	}
	
	/**
	 * @see org.openmrs.api.OrderService#discontinueOrder(org.openmrs.Order, String, java.util.Date,
	 *      org.openmrs.Provider, org.openmrs.Encounter)
	 */
	@Override
	public Order discontinueOrder(Order orderToDiscontinue, String reasonNonCoded, Date discontinueDate, Provider orderer,
	                              Encounter encounter) {
		if (discontinueDate == null) {
			discontinueDate = aMomentBefore(new Date());
		}
		stopOrder(orderToDiscontinue, discontinueDate, false);
		Order newOrder = orderToDiscontinue.cloneForDiscontinuing();
		newOrder.setOrderReasonNonCoded(reasonNonCoded);
		newOrder.setOrderer(orderer);
		newOrder.setEncounter(encounter);
		newOrder.setDateActivated(discontinueDate);
		return saveOrderInternal(newOrder, null);
	}
	
	private boolean isDiscontinueOrReviseOrder(Order order) {
		return DISCONTINUE == order.getAction() || REVISE == order.getAction();
	}
	
	/**
	 * Make necessary checks, set necessary fields for discontinuing <code>orderToDiscontinue</code>
	 * and save.
	 *
	 * @param orderToStop
	 * @param discontinueDate
	 */
	private void stopOrder(Order orderToStop, Date discontinueDate, boolean isRetrospective) {
		if (discontinueDate == null) {
			discontinueDate = new Date();
		}
		if (discontinueDate.after(new Date())) {
			throw new IllegalArgumentException("Discontinue date cannot be in the future");
		}
		if (DISCONTINUE == orderToStop.getAction()) {
			throw new CannotStopDiscontinuationOrderException();
		}
		
		if (isRetrospective && orderToStop.getDateStopped() != null) {
			throw new CannotStopInactiveOrderException();
		}
		if (!isRetrospective && !orderToStop.isActive()) {
			throw new CannotStopInactiveOrderException();
		} else if (isRetrospective && !orderToStop.isActive(discontinueDate)) {
			throw new CannotStopInactiveOrderException();
		}
		
		setProperty(orderToStop, "dateStopped", discontinueDate);
		saveOrderInternal(orderToStop, null);
	}
	
	/**
	 * @see org.openmrs.api.OrderService#saveOrderFrequency(org.openmrs.OrderFrequency)
	 */
	@Override
	public OrderFrequency saveOrderFrequency(OrderFrequency orderFrequency) throws APIException {
		if (orderFrequency.getOrderFrequencyId() != null
				&& dao.isOrderFrequencyInUse(orderFrequency)) {		
			throw new CannotUpdateObjectInUseException("Order.frequency.cannot.edit");
		}
		
		return dao.saveOrderFrequency(orderFrequency);
	}
	
	/**
	 * @see org.openmrs.api.OrderService#retireOrderFrequency(org.openmrs.OrderFrequency,
	 *      java.lang.String)
	 */
	@Override
	public OrderFrequency retireOrderFrequency(OrderFrequency orderFrequency, String reason) {
		return dao.saveOrderFrequency(orderFrequency);
	}
	
	/**
	 * @see org.openmrs.api.OrderService#unretireOrderFrequency(org.openmrs.OrderFrequency)
	 */
	@Override
	public OrderFrequency unretireOrderFrequency(OrderFrequency orderFrequency) {
		return Context.getOrderService().saveOrderFrequency(orderFrequency);
	}
	
	/**
	 * @see org.openmrs.api.OrderService#purgeOrderFrequency(org.openmrs.OrderFrequency)
	 */
	@Override
	public void purgeOrderFrequency(OrderFrequency orderFrequency) {
		
		if (dao.isOrderFrequencyInUse(orderFrequency)) {
			throw new CannotDeleteObjectInUseException("Order.frequency.cannot.delete", (Object[]) null);
		}
		
		dao.purgeOrderFrequency(orderFrequency);
	}
	
	/**
	 * @see org.openmrs.api.OrderService#getOrderFrequencyByConcept(org.openmrs.Concept)
	 */
	@Override
	@Transactional(readOnly = true)
	public OrderFrequency getOrderFrequencyByConcept(Concept concept) {
		return dao.getOrderFrequencyByConcept(concept);
	}
	
	/**
	 * @see GlobalPropertyListener#supportsPropertyName(String)
	 */
	@Override
	public boolean supportsPropertyName(String propertyName) {
		return OpenmrsConstants.GP_ORDER_NUMBER_GENERATOR_BEAN_ID.equals(propertyName);
	}
	
	/**
	 * @see GlobalPropertyListener#globalPropertyChanged(org.openmrs.GlobalProperty)
	 */
	@Override
	public void globalPropertyChanged(GlobalProperty newValue) {
		setOrderNumberGenerator(null);
	}
	
	/**
	 * @see GlobalPropertyListener#globalPropertyDeleted(String)
	 */
	@Override
	public void globalPropertyDeleted(String propertyName) {
		setOrderNumberGenerator(null);
	}
	
	/**
	 * Helper method to deter instance methods from setting static fields
	 */
	private static void setOrderNumberGenerator(OrderNumberGenerator orderNumberGenerator) {
		OrderServiceImpl.orderNumberGenerator = orderNumberGenerator;
	}
	
	/**
	 * @see org.openmrs.api.OrderService#getOrderType(Integer)
	 */
	
	@Override
	@Transactional(readOnly = true)
	public OrderType getOrderType(Integer orderTypeId) {
		return dao.getOrderType(orderTypeId);
	}
	
	/**
	 * @see org.openmrs.api.OrderService#getOrderTypeByUuid(String)
	 */
	@Override
	@Transactional(readOnly = true)
	public OrderType getOrderTypeByUuid(String uuid) {
		return dao.getOrderTypeByUuid(uuid);
	}
	
	/**
	 * @see org.openmrs.api.OrderService#getOrderTypes(boolean)
	 */
	@Override
	@Transactional(readOnly = true)
	public List<OrderType> getOrderTypes(boolean includeRetired) {
		return dao.getOrderTypes(includeRetired);
	}
	
	/**
	 * @see org.openmrs.api.OrderService#saveOrderType(org.openmrs.OrderType)
	 */
	@Override
	public OrderType saveOrderType(OrderType orderType) {
		return dao.saveOrderType(orderType);
	}
	
	/**
	 * @see org.openmrs.api.OrderService#purgeOrderType(org.openmrs.OrderType)
	 */
	@Override
	public void purgeOrderType(OrderType orderType) {
		if (dao.isOrderTypeInUse(orderType)) {
			throw new CannotDeleteObjectInUseException("Order.type.cannot.delete", (Object[]) null);
		}
		dao.purgeOrderType(orderType);
	}
	
	/**
	 * @see org.openmrs.api.OrderService#retireOrderType(org.openmrs.OrderType, String)
	 */
	@Override
	public OrderType retireOrderType(OrderType orderType, String reason) {
		return saveOrderType(orderType);
	}
	
	/**
	 * @see org.openmrs.api.OrderService#unretireOrderType(org.openmrs.OrderType)
	 */
	@Override
	public OrderType unretireOrderType(OrderType orderType) {
		return saveOrderType(orderType);
	}
	
	/**
	 * @see org.openmrs.api.OrderService#getSubtypes(org.openmrs.OrderType, boolean)
	 */
	@Override
	@Transactional(readOnly = true)
	public List<OrderType> getSubtypes(OrderType orderType, boolean includeRetired) {
		List<OrderType> allSubtypes = new ArrayList<>();
		List<OrderType> immediateAncestors = dao.getOrderSubtypes(orderType, includeRetired);
		while (!immediateAncestors.isEmpty()) {
			List<OrderType> ancestorsAtNextLevel = new ArrayList<>();
			for (OrderType type : immediateAncestors) {
				allSubtypes.add(type);
				ancestorsAtNextLevel.addAll(dao.getOrderSubtypes(type, includeRetired));
			}
			immediateAncestors = ancestorsAtNextLevel;
		}
		return allSubtypes;
	}
	
	/**
	 * @see org.openmrs.api.OrderService#getOrderTypeByConceptClass(org.openmrs.ConceptClass)
	 */
	@Override
	@Transactional(readOnly = true)
	public OrderType getOrderTypeByConceptClass(ConceptClass conceptClass) {
		return dao.getOrderTypeByConceptClass(conceptClass);
	}
	
	/**
	 * @see org.openmrs.api.OrderService#getOrderTypeByConcept(org.openmrs.Concept)
	 */
	@Override
	@Transactional(readOnly = true)
	public OrderType getOrderTypeByConcept(Concept concept) {
		return Context.getOrderService().getOrderTypeByConceptClass(concept.getConceptClass());
	}
	
	/**
	 * @see org.openmrs.api.OrderService#getDrugRoutes()
	 */
	@Override
	@Transactional(readOnly = true)
	public List<Concept> getDrugRoutes() {
		return getSetMembersOfConceptSetFromGP(OpenmrsConstants.GP_DRUG_ROUTES_CONCEPT_UUID);
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<Concept> getDrugDosingUnits() {
		return getSetMembersOfConceptSetFromGP(OpenmrsConstants.GP_DRUG_DOSING_UNITS_CONCEPT_UUID);
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<Concept> getDrugDispensingUnits() {
		List<Concept> dispensingUnits = new ArrayList<>(
				getSetMembersOfConceptSetFromGP(OpenmrsConstants.GP_DRUG_DISPENSING_UNITS_CONCEPT_UUID));
		for (Concept concept : getDrugDosingUnits()) {
			if (!dispensingUnits.contains(concept)) {
				dispensingUnits.add(concept);
			}
		}
		return dispensingUnits;
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<Concept> getDurationUnits() {
		return getSetMembersOfConceptSetFromGP(OpenmrsConstants.GP_DURATION_UNITS_CONCEPT_UUID);
	}
	
	/**
	 * @see org.openmrs.api.OrderService#getTestSpecimenSources()
	 */
	@Override
	public List<Concept> getTestSpecimenSources() {
		return getSetMembersOfConceptSetFromGP(OpenmrsConstants.GP_TEST_SPECIMEN_SOURCES_CONCEPT_UUID);
	}
	
	@Override
	public Concept getNonCodedDrugConcept() {
		String conceptUuid = Context.getAdministrationService().getGlobalProperty(OpenmrsConstants.GP_DRUG_ORDER_DRUG_OTHER);
		if (StringUtils.hasText(conceptUuid)) {
			return Context.getConceptService().getConceptByUuid(conceptUuid);
		}
		return null;
	}
	
	@Override
	@Transactional(readOnly = true)
	public OrderGroup getOrderGroupByUuid(String uuid) throws APIException {
		return dao.getOrderGroupByUuid(uuid);
	}
	
	@Override
	@Transactional(readOnly = true)
	public OrderGroup getOrderGroup(Integer orderGroupId) throws APIException {
		return dao.getOrderGroupById(orderGroupId);
	}
	
	private List<Concept> getSetMembersOfConceptSetFromGP(String globalProperty) {
		String conceptUuid = Context.getAdministrationService().getGlobalProperty(globalProperty);
		Concept concept = Context.getConceptService().getConceptByUuid(conceptUuid);
		if (concept != null && concept.getSet()) {
			return concept.getSetMembers();
		}
		return Collections.emptyList();
	}
}

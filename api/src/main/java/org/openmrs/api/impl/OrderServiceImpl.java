/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.api.impl;

import static org.openmrs.Order.Action.DISCONTINUE;
import static org.openmrs.Order.Action.REVISE;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.proxy.HibernateProxy;
import org.openmrs.CareSetting;
import org.openmrs.Concept;
import org.openmrs.ConceptClass;
import org.openmrs.DrugOrder;
import org.openmrs.Encounter;
import org.openmrs.GlobalProperty;
import org.openmrs.Order;
import org.openmrs.OrderFrequency;
import org.openmrs.OrderType;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.TestOrder;
import org.openmrs.User;
import org.openmrs.api.APIException;
import org.openmrs.api.GlobalPropertyListener;
import org.openmrs.api.OrderContext;
import org.openmrs.api.OrderNumberGenerator;
import org.openmrs.api.OrderService;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.OrderDAO;
import org.openmrs.order.OrderUtil;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsUtil;
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
	
	protected final Log log = LogFactory.getLog(getClass());
	
	private static final String ORDER_NUMBER_PREFIX = "ORD-";
	
	protected OrderDAO dao;
	
	private static OrderNumberGenerator orderNumberGenerator = null;
	
	public OrderServiceImpl() {
	}
	
	/**
	 * @see org.openmrs.api.OrderService#setOrderDAO(org.openmrs.api.db.OrderDAO)
	 */
	public void setOrderDAO(OrderDAO dao) {
		this.dao = dao;
	}
	
	/**
	 * @see org.openmrs.api.OrderService#saveOrder(org.openmrs.Order, org.openmrs.api.OrderContext)
	 */
	public synchronized Order saveOrder(Order order, OrderContext orderContext) throws APIException {
		if (order.getOrderId() != null) {
			throw new APIException("Cannot edit an existing order, you need to revise it instead");
		}
		if (order.getDateActivated() == null) {
			order.setDateActivated(new Date());
		}
		//Reject if there is an active order for the same orderable with overlapping schedule
		boolean isDrugOrder = DrugOrder.class.isAssignableFrom(getActualType(order));
		Concept concept = order.getConcept();
		if (concept == null && isDrugOrder) {
			DrugOrder drugOrder = (DrugOrder) order;
			if (drugOrder.getDrug() != null) {
				concept = drugOrder.getDrug().getConcept();
				drugOrder.setConcept(concept);
			}
		}
		if (isDrugOrder) {
			((DrugOrder) order).setAutoExpireDateBasedOnDuration();
		}
		
		if (concept == null) {
			throw new APIException("concept is required for an order");
		}
		
		Order previousOrder = order.getPreviousOrder();
		if (order.getOrderType() == null) {
			OrderType orderType = null;
			if (orderContext != null) {
				orderType = orderContext.getOrderType();
			}
			if (orderType == null) {
				orderType = getOrderTypeByConcept(concept);
			}
			//Check if it is instance of DrugOrder
			if (orderType == null && order instanceof DrugOrder) {
				orderType = Context.getOrderService().getOrderTypeByUuid(OrderType.DRUG_ORDER_TYPE_UUID);
			}
			//Check if it is an instance of TestOrder
			if (orderType == null && order instanceof TestOrder) {
				orderType = Context.getOrderService().getOrderTypeByUuid(OrderType.TEST_ORDER_TYPE_UUID);
			}
			
			//this order's order type should match that of the previous
			if (orderType == null || (previousOrder != null && !orderType.equals(previousOrder.getOrderType()))) {
				throw new APIException(
				        "Cannot determine the order type of the order, make sure the concept's class is mapped to an order type");
			}
			
			order.setOrderType(orderType);
		}
		if (order.getCareSetting() == null) {
			CareSetting careSetting = null;
			if (orderContext != null) {
				careSetting = orderContext.getCareSetting();
			}
			if (careSetting == null || (previousOrder != null && !careSetting.equals(previousOrder.getCareSetting()))) {
				throw new APIException("Cannot determine the care setting of the order");
			}
			order.setCareSetting(careSetting);
		}
		
		if (!order.getOrderType().getJavaClass().isAssignableFrom(order.getClass())) {
			throw new APIException("Order type class " + order.getOrderType().getJavaClass()
			        + " does not match the order class " + order.getClass().getName());
		}
		
		if (REVISE == order.getAction()) {
			if (previousOrder == null) {
				throw new APIException("Previous Order is required for a revised order");
			}
			stopOrder(previousOrder, order.getDateActivated());
		} else if (DISCONTINUE == order.getAction()) {
			discontinueExistingOrdersIfNecessary(order);
		}
		
		if (previousOrder != null) {
			//Check that patient, careSetting, concept and drug if is drug order have not changed
			//we need to use a SQL query to by pass the hibernate cache
			boolean isPreviousDrugOrder = DrugOrder.class.isAssignableFrom(previousOrder.getClass());
			List<List<Object>> rows = dao.getOrderFromDatabase(previousOrder, isPreviousDrugOrder);
			List<Object> rowData = rows.get(0);
			if (!rowData.get(0).equals(previousOrder.getPatient().getPatientId())) {
				throw new APIException("Cannot change the patient of an order");
			} else if (!rowData.get(1).equals(previousOrder.getCareSetting().getCareSettingId())) {
				throw new APIException("Cannot change the careSetting of an order");
			} else if (!rowData.get(2).equals(previousOrder.getConcept().getConceptId())) {
				throw new APIException("Cannot change the concept of an order");
			} else if (isPreviousDrugOrder && !rowData.get(3).equals(((DrugOrder) previousOrder).getDrug().getDrugId())) {
				throw new APIException("Cannot change the drug of a drug order");
			}
			
			//concept should be the same as on previous order, same applies to drug for drug orders
			boolean isDrugOrderAndHasADrug = isDrugOrder && ((DrugOrder) order).getDrug() != null;
			if (!OpenmrsUtil.nullSafeEquals(order.getConcept(), previousOrder.getConcept())) {
				throw new APIException("The concept of the previous order and the new one order don't match");
			} else if (isDrugOrderAndHasADrug) {
				DrugOrder drugOrder1 = (DrugOrder) order;
				DrugOrder drugOrder2 = (DrugOrder) previousOrder;
				if (!OpenmrsUtil.nullSafeEquals(drugOrder1.getDrug(), drugOrder2.getDrug())) {
					throw new APIException("The drug of the previous order and the new one order don't match");
				}
			} else if (!order.getOrderType().equals(previousOrder.getOrderType())) {
				throw new APIException("The order type does not match that of the previous order");
			} else if (!order.getCareSetting().equals(previousOrder.getCareSetting())) {
				throw new APIException("The care setting does not match that of the previous order");
			} else if (!getActualType(order).equals(getActualType(previousOrder))) {
				throw new APIException("The class does not match that of the previous order");
			}
		}
		
		if (DISCONTINUE != order.getAction()) {
			List<Order> activeOrders = getActiveOrders(order.getPatient(), null, order.getCareSetting(), null);
			for (Order activeOrder : activeOrders) {
				if (order.hasSameOrderableAs(activeOrder)
				        && !OpenmrsUtil.nullSafeEquals(order.getPreviousOrder(), activeOrder)
				        && OrderUtil.checkScheduleOverlap(order, activeOrder)) {
					throw new APIException(
					        "Cannot have more than one active order for the same orderable and care setting at same time");
				}
			}
		}
		
		return saveOrderInternal(order, orderContext);
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
				int milliseconds = cal.get(Calendar.MILLISECOND);
				//roll autoExpireDate to end of day (23:59:59:999) if no time portion is specified
				if (hours == 0 && minutes == 0 && seconds == 0 && milliseconds == 0) {
					cal.set(Calendar.HOUR_OF_DAY, 23);
					cal.set(Calendar.MINUTE, 59);
					cal.set(Calendar.SECOND, 59);
					cal.set(Calendar.MILLISECOND, 999);
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
			throw new APIException("Failed to set " + propertyName + " for order:" + order, e);
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
	 */
	private void discontinueExistingOrdersIfNecessary(Order order) {
		//Ignore and return if this is not an order to discontinue
		if (DISCONTINUE != order.getAction()) {
			return;
		}
		
		//Mark previousOrder as discontinued if it is not already
		Order previousOrder = order.getPreviousOrder();
		if (previousOrder != null) {
			stopOrder(previousOrder, order.getDateActivated());
			return;
		}
		
		//Mark first order found corresponding to this DC order as discontinued.
		List<? extends Order> orders = getActiveOrders(order.getPatient(), order.getOrderType(), order.getCareSetting(),
		    null);
		boolean isDrugOrderAndHasADrug = DrugOrder.class.isAssignableFrom(getActualType(order))
		        && ((DrugOrder) order).getDrug() != null;
		for (Order activeOrder : orders) {
			if (!getActualType(order).equals(getActualType(activeOrder))) {
				continue;
			}
			boolean shouldMarkAsDiscontinued = false;
			//For drug orders, the drug must match if the order has a drug
			if (isDrugOrderAndHasADrug) {
				DrugOrder drugOrder1 = (DrugOrder) order;
				DrugOrder drugOrder2 = (DrugOrder) activeOrder;
				if (OpenmrsUtil.nullSafeEquals(drugOrder1.getDrug(), drugOrder2.getDrug())) {
					shouldMarkAsDiscontinued = true;
				}
			} else if (activeOrder.getConcept().equals(order.getConcept())) {
				shouldMarkAsDiscontinued = true;
			}
			
			if (shouldMarkAsDiscontinued) {
				order.setPreviousOrder(activeOrder);
				stopOrder(activeOrder, order.getDateActivated());
				break;
			}
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
	public void purgeOrder(Order order) throws APIException {
		purgeOrder(order, false);
	}
	
	/**
	 * @see org.openmrs.api.OrderService#purgeOrder(Order)
	 */
	public void purgeOrder(Order order, boolean cascade) throws APIException {
		if (cascade) {
			dao.deleteObsThatReference(order);
		}
		
		dao.deleteOrder(order);
	}
	
	/**
	 * @see org.openmrs.api.OrderService#voidOrder(org.openmrs.Order, java.lang.String)
	 */
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
	public Order unvoidOrder(Order order) throws APIException {
		Order previousOrder = order.getPreviousOrder();
		if (previousOrder != null && isDiscontinueOrReviseOrder(order)) {
			if (!previousOrder.isActive()) {
				final String action = DISCONTINUE == order.getAction() ? "discontinuation" : "revision";
				throw new APIException("Cannot unvoid a " + action + " order if the previous order is no longer active");
			}
			stopOrder(previousOrder, order.getDateActivated());
		}
		
		return saveOrderInternal(order, null);
	}
	
	/**
	 * @see org.openmrs.api.OrderService#getOrder(java.lang.Integer)
	 */
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
			orderTypes = new ArrayList<OrderType>();
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
		List<Concept> concepts = new Vector<Concept>();
		concepts.add(concept);
		
		List<Patient> patients = new Vector<Patient>();
		patients.add(patient);
		
		return dao.getOrders(null, patients, concepts, new Vector<User>(), new Vector<Encounter>());
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
		List<Order> orders = new ArrayList<Order>();
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
			orderTypes = new ArrayList<OrderType>();
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
	        Encounter encounter) throws Exception {
		stopOrder(orderToDiscontinue, discontinueDate);
		Order newOrder = orderToDiscontinue.cloneForDiscontinuing();
		newOrder.setOrderReason(reasonCoded);
		newOrder.setOrderer(orderer);
		newOrder.setEncounter(encounter);
		if (discontinueDate == null) {
			discontinueDate = new Date();
		}
		newOrder.setDateActivated(discontinueDate);
		return saveOrderInternal(newOrder, null);
	}
	
	/**
	 * @see org.openmrs.api.OrderService#discontinueOrder(org.openmrs.Order, String, java.util.Date,
	 *      org.openmrs.Provider, org.openmrs.Encounter)
	 */
	@Override
	public Order discontinueOrder(Order orderToDiscontinue, String reasonNonCoded, Date discontinueDate, Provider orderer,
	        Encounter encounter) throws Exception {
		stopOrder(orderToDiscontinue, discontinueDate);
		Order newOrder = orderToDiscontinue.cloneForDiscontinuing();
		newOrder.setOrderReasonNonCoded(reasonNonCoded);
		newOrder.setOrderer(orderer);
		newOrder.setEncounter(encounter);
		if (discontinueDate == null) {
			discontinueDate = new Date();
		}
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
	private void stopOrder(Order orderToStop, Date discontinueDate) {
		if (discontinueDate == null) {
			discontinueDate = new Date();
		}
		if (discontinueDate.after(new Date())) {
			throw new IllegalArgumentException("Discontinue date cannot be in the future");
		}
		if (DISCONTINUE == orderToStop.getAction()) {
			throw new APIException("An order with action " + DISCONTINUE + " cannot be discontinued.");
		}
		if (!orderToStop.isActive()) {
			throw new APIException("Cannot discontinue an order that is already stopped, expired or voided");
		}
		setProperty(orderToStop, "dateStopped", discontinueDate);
		saveOrderInternal(orderToStop, null);
	}
	
	/**
	 * @see org.openmrs.api.OrderService#saveOrderFrequency(org.openmrs.OrderFrequency)
	 */
	@Override
	public OrderFrequency saveOrderFrequency(OrderFrequency orderFrequency) throws APIException {
		
		if (orderFrequency.getOrderFrequencyId() != null) {
			if (dao.isOrderFrequencyInUse(orderFrequency)) {
				throw new APIException("This order frequency cannot be edited because it is already in use");
			}
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
		return dao.saveOrderFrequency(orderFrequency);
	}
	
	/**
	 * @see org.openmrs.api.OrderService#purgeOrderFrequency(org.openmrs.OrderFrequency)
	 */
	@Override
	public void purgeOrderFrequency(OrderFrequency orderFrequency) {
		
		if (dao.isOrderFrequencyInUse(orderFrequency)) {
			throw new APIException("This order frequency cannot be deleted because it is already in use");
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
		orderNumberGenerator = null;
	}
	
	/**
	 * @see GlobalPropertyListener#globalPropertyDeleted(String)
	 */
	@Override
	public void globalPropertyDeleted(String propertyName) {
		orderNumberGenerator = null;
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
			throw new APIException("This order type cannot be deleted because it is already in use");
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
		List<OrderType> allSubtypes = new ArrayList<OrderType>();
		List<OrderType> immediateAncestors = dao.getOrderSubtypes(orderType, includeRetired);
		while (!immediateAncestors.isEmpty()) {
			List<OrderType> ancestorsAtNextLevel = new ArrayList<OrderType>();
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
		List<Concept> dispensingUnits = new ArrayList<Concept>();
		dispensingUnits.addAll(getSetMembersOfConceptSetFromGP(OpenmrsConstants.GP_DRUG_DISPENSING_UNITS_CONCEPT_UUID));
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
	
	private List<Concept> getSetMembersOfConceptSetFromGP(String globalProperty) {
		String conceptUuid = Context.getAdministrationService().getGlobalProperty(globalProperty);
		Concept concept = Context.getConceptService().getConceptByUuid(conceptUuid);
		if (concept != null && concept.isSet()) {
			return concept.getSetMembers();
		}
		return Collections.emptyList();
	}
	
}

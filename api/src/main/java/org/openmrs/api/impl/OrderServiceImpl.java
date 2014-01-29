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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.CareSetting;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Order;
import org.openmrs.OrderFrequency;
import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.api.APIException;
import org.openmrs.api.OrderNumberGenerator;
import org.openmrs.api.OrderService;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.OrderDAO;
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
public class OrderServiceImpl extends BaseOpenmrsService implements OrderService, OrderNumberGenerator {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	private static final String ORDER_NUMBER_PREFIX = "ORD-";
	
	protected OrderDAO dao;
	
	public OrderServiceImpl() {
	}
	
	/**
	 * @see org.openmrs.api.OrderService#setOrderDAO(org.openmrs.api.db.OrderDAO)
	 */
	public void setOrderDAO(OrderDAO dao) {
		this.dao = dao;
	}
	
	/**
	 * @see org.openmrs.api.OrderService#saveOrder(org.openmrs.Order)
	 */
	public Order saveOrder(Order order) throws APIException {
		if (order.getOrderId() == null) {
			discontinueExistingOrdersIfRequired(order);
			//TODO call module registered order number generators 
			//and if there is none, use the default below
			try {
				Field field = Order.class.getDeclaredField("orderNumber");
				field.setAccessible(true);
				field.set(order, getNewOrderNumber());
			}
			catch (Exception e) {
				throw new APIException("Failed to assign order number", e);
			}
		}
		
		return dao.saveOrder(order);
	}
	
	/**
	 * If this is a discontinue order, ensure that the previous order is discontinued.
	 * If a previousOrder is present, then ensure this is discontinued.
	 * If no previousOrder is present, then try to find a previousOrder and discontinue it.
	 * If cannot find a previousOrder, throw exception
	 *
	 * @param order
	 */
	private void discontinueExistingOrdersIfRequired(Order order) {
		//Ignore and return if this is not an order to discontinue
		if (!Order.Action.DISCONTINUE.equals(order.getAction()))
			return;
		
		//Discontinue previousOrder if it is not already
		Order previousOrder = order.getPreviousOrder();
		if (previousOrder != null) {
			if (!previousOrder.getConcept().equals(order.getConcept())) {
				throw new APIException("Concept of previous order and this order should be the same");
			}
			
			if (previousOrder.getDateStopped() == null) {
				discontinue(previousOrder, order.getStartDate());
			}
			return;
		}
		
		//Discontinue the first found order corresponding to this DC order.
		List<? extends Order> orders = getActiveOrders(order.getPatient(), order.getClass(), order.getCareSetting(), null);
		for (Order activeOrder : orders) {
			if (activeOrder.getConcept().equals(order.getConcept())) {
				order.setPreviousOrder(activeOrder);
				discontinue(activeOrder, order.getStartDate());
				return;
			}
		}
		
		throw new APIException("Could not find an active order with the concept " + order.getConcept() + " to discontinue. ");
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
		// fail early if this order is already voided
		if (order.getVoided())
			return order;
		
		if (!StringUtils.hasLength(voidReason))
			throw new IllegalArgumentException("voidReason cannot be empty or null");
		
		order.setVoided(Boolean.TRUE);
		order.setVoidReason(voidReason);
		order.setVoidedBy(Context.getAuthenticatedUser());
		if (order.getDateVoided() == null)
			order.setDateVoided(new Date());
		
		return saveOrder(order);
	}
	
	/**
	 * @see org.openmrs.api.OrderService#unvoidOrder(org.openmrs.Order)
	 */
	public Order unvoidOrder(Order order) throws APIException {
		order.setVoided(false);
		return saveOrder(order);
	}
	
	/**
	 * @see org.openmrs.api.OrderService#getOrder(java.lang.Integer)
	 */
	@Transactional(readOnly = true)
	public Order getOrder(Integer orderId) throws APIException {
		return getOrder(orderId, Order.class);
	}
	
	/**
	 * @see org.openmrs.api.OrderService#getOrder(java.lang.Integer, java.lang.Class)
	 */
	@Transactional(readOnly = true)
	public <o extends Order> o getOrder(Integer orderId, Class<o> orderClassType) throws APIException {
		return dao.getOrder(orderId, orderClassType);
	}
	
	/**
	 * @see org.openmrs.api.OrderService#getOrders(java.lang.Class, java.util.List, java.util.List,
	 *      java.util.List, java.util.List)
	 */
	@Transactional(readOnly = true)
	public <Ord extends Order> List<Ord> getOrders(Class<Ord> orderClassType, List<Patient> patients,
	        List<Concept> concepts, List<User> orderers, List<Encounter> encounters) {
		if (orderClassType == null)
			throw new APIException(
			        "orderClassType cannot be null.  An order type of Order.class or DrugOrder.class is required");
		
		if (patients == null)
			patients = new Vector<Patient>();
		
		if (concepts == null)
			concepts = new Vector<Concept>();
		
		if (orderers == null)
			orderers = new Vector<User>();
		
		if (encounters == null)
			encounters = new Vector<Encounter>();
		
		return dao.getOrders(orderClassType, patients, concepts, orderers, encounters);
	}
	
	/**
	 * @see org.openmrs.api.OrderService#getOrderByUuid(java.lang.String)
	 */
	@Transactional(readOnly = true)
	public Order getOrderByUuid(String uuid) throws APIException {
		return dao.getOrderByUuid(uuid);
	}
	
	/**
	 * @see org.openmrs.api.OrderNumberGenerator#getNewOrderNumber()
	 */
	@Override
	public synchronized String getNewOrderNumber() {
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
		if (patient == null)
			throw new IllegalArgumentException("patient is required");
		
		List<Concept> concepts = new Vector<Concept>();
		concepts.add(concept);
		
		List<Patient> patients = new Vector<Patient>();
		patients.add(patient);
		
		return getOrders(Order.class, patients, concepts, null, null);
	}
	
	/**
	 * @see org.openmrs.api.OrderService#getNextOrderNumberSeedSequenceValue()
	 */
	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Long getNextOrderNumberSeedSequenceValue() {
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
	 * @see org.openmrs.api.OrderService#getActiveOrders(org.openmrs.Patient, Class,
	 *      org.openmrs.CareSetting, java.util.Date)
	 */
	@Override
	@Transactional(readOnly = true)
	public <Ord extends Order> List<Ord> getActiveOrders(Patient patient, Class<Ord> orderClass, CareSetting careSetting,
	        Date asOfDate) {
		if (patient == null) {
			throw new IllegalArgumentException("Patient is required when fetching active orders");
		}
		if (asOfDate == null) {
			asOfDate = new Date();
		}
		return dao.getActiveOrders(patient, orderClass, careSetting, asOfDate);
	}
	
	/**
	 * @see org.openmrs.api.OrderService#getCareSetting(Integer)
	 */
	@Override
	public CareSetting getCareSetting(Integer careSettingId) {
		return dao.getCareSetting(careSettingId);
	}
	
	/**
	 * @see OrderService#getOrderFrequency(Integer)
	 */
	@Override
	public OrderFrequency getOrderFrequency(Integer orderFrequencyId) {
		return dao.getOrderFrequency(orderFrequencyId);
	}
	
	/**
	 * @see org.openmrs.api.OrderService#discontinueOrder(org.openmrs.Order, org.openmrs.Concept, java.util.Date)
	 */
	@Override
	public Order discontinueOrder(Order orderToDiscontinue, Concept reasonCoded, Date discontinueDate) {
		discontinue(orderToDiscontinue, discontinueDate);
		
		Order newOrder = orderToDiscontinue.cloneForDiscontinuing();
		newOrder.setDiscontinuedReason(reasonCoded);
		
		return saveOrder(newOrder);
	}
	
	/**
	 * @see org.openmrs.api.OrderService#discontinueOrder(org.openmrs.Order, String, java.util.Date)
	 */
	@Override
	public Order discontinueOrder(Order orderToDiscontinue, String reasonNonCoded, Date discontinueDate) {
		discontinue(orderToDiscontinue, discontinueDate);
		
		Order newOrder = orderToDiscontinue.cloneForDiscontinuing();
		newOrder.setDiscontinuedReasonNonCoded(reasonNonCoded);
		
		return saveOrder(newOrder);
	}
	
	/**
	 * Make necessary checks, set necessary fields for discontinuing <code>orderToDiscontinue</code> and save.
	 *
	 * @param orderToDiscontinue
	 * @param discontinueDate
	 */
	private void discontinue(Order orderToDiscontinue, Date discontinueDate) {
		if (orderToDiscontinue.getAction().equals(Order.Action.DISCONTINUE)) {
			throw new APIException("An order with action " + Order.Action.DISCONTINUE + " cannot be discontinued. ");
		}
		
		orderToDiscontinue.setDateStopped(discontinueDate);
		saveOrder(orderToDiscontinue);
	}
}

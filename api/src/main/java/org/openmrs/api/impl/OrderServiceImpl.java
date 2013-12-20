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
import java.util.Date;
import java.util.List;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.api.APIException;
import org.openmrs.api.OrderNumberGenerator;
import org.openmrs.api.OrderService;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.OrderDAO;
import org.springframework.util.StringUtils;

/**
 * Default implementation of the Order-related services class. This method should not be invoked by
 * itself. Spring injection is used to inject this implementation into the ServiceContext. Which
 * implementation is injected is determined by the spring application context file:
 * /metadata/api/spring/applicationContext.xml
 * 
 * @see org.openmrs.api.OrderService
 */
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
	public Order getOrder(Integer orderId) throws APIException {
		return getOrder(orderId, Order.class);
	}
	
	/**
	 * @see org.openmrs.api.OrderService#getOrder(java.lang.Integer, java.lang.Class)
	 */
	public <o extends Order> o getOrder(Integer orderId, Class<o> orderClassType) throws APIException {
		return dao.getOrder(orderId, orderClassType);
	}
	
	/**
	 * @see org.openmrs.api.OrderService#getOrders(java.lang.Class, java.util.List, java.util.List,
	 *      java.util.List, java.util.List)
	 */
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
	public Order getOrderByUuid(String uuid) throws APIException {
		return dao.getOrderByUuid(uuid);
	}
	
	/**
	 * @see org.openmrs.api.OrderNumberGenerator#getNewOrderNumber()
	 */
	@Override
	public synchronized String getNewOrderNumber() {
		return ORDER_NUMBER_PREFIX + Context.getOrderService().getNextOrderNumberSeed();
	}
	
	/**
	 * @see org.openmrs.api.OrderService#getOrderByOrderNumber(java.lang.String)
	 */
	@Override
	public Order getOrderByOrderNumber(String orderNumber) {
		return dao.getOrderByOrderNumber(orderNumber);
	}
	
	/**
	 * @see org.openmrs.api.OrderService#getOrderHistoryByConcept(org.openmrs.Patient,
	 *      org.openmrs.Concept)
	 */
	@Override
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
	 * @see org.openmrs.api.OrderService#getNextOrderNumberSeed()
	 */
	@Override
	public Long getNextOrderNumberSeed() {
		return dao.getNextOrderNumberSeed();
	}
}

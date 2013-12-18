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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.DrugOrder;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.GlobalProperty;
import org.openmrs.Location;
import org.openmrs.Order;
import org.openmrs.OrderType;
import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.aop.RequiredDataAdvice;
import org.openmrs.api.APIException;
import org.openmrs.api.OrderNumberGenerator;
import org.openmrs.api.OrderService;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.OrderDAO;
import org.openmrs.api.handler.SaveHandler;
import org.openmrs.order.DrugOrderSupport;
import org.openmrs.order.RegimenSuggestion;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.PrivilegeConstants;
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
	
	protected OrderDAO dao;
	
	private static final String ORDER_NUMBER_PREFIX = "ORD-";
	
	private static final String ORDER_NUMBER_START_VALUE = "1";
	
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
		if (order.getOrderId() == null && !StringUtils.hasText(order.getOrderNumber())) {
			//TODO call module registered order number generators 
			//and if there is none, use the default below
			order.setOrderNumber(getNewOrderNumber());
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
	 * @see org.openmrs.api.OrderService#discontinueOrder(org.openmrs.Order, org.openmrs.Concept,
	 *      java.util.Date)
	 */
	public Order discontinueOrder(Order order, Concept discontinueReason, Date discontinueDate) throws APIException {
		order.setDiscontinued(Boolean.TRUE);
		order.setDiscontinuedReason(discontinueReason);
		order.setDiscontinuedDate(discontinueDate);
		order.setDiscontinuedBy(Context.getAuthenticatedUser());
		
		return saveOrder(order);
	}
	
	/**
	 * @see org.openmrs.api.OrderService#undiscontinueOrder(org.openmrs.Order)
	 */
	public Order undiscontinueOrder(Order order) throws APIException {
		order.setDiscontinued(Boolean.FALSE);
		order.setDiscontinuedBy(null);
		order.setDiscontinuedDate(null);
		order.setDiscontinuedReason(null);
		
		return saveOrder(order);
	}
	
	/**
	 * @see org.openmrs.api.OrderService#saveOrderType(org.openmrs.OrderType)
	 */
	public OrderType saveOrderType(OrderType orderType) throws APIException {
		return dao.saveOrderType(orderType);
	}
	
	/**
	 * @see org.openmrs.api.OrderService#retireOrderType(OrderType, String)
	 */
	public OrderType retireOrderType(OrderType orderType, String reason) throws APIException {
		
		orderType.setRetired(true);
		orderType.setRetireReason(reason);
		return saveOrderType(orderType);
	}
	
	/**
	 * @see org.openmrs.api.OrderService#unretireOrderType(org.openmrs.OrderType)
	 */
	public OrderType unretireOrderType(OrderType orderType) throws APIException {
		orderType.setRetired(false);
		return saveOrderType(orderType);
	}
	
	/**
	 * @see org.openmrs.api.OrderService#purgeOrderType(org.openmrs.OrderType)
	 */
	public void purgeOrderType(OrderType orderType) throws APIException {
		dao.deleteOrderType(orderType);
	}
	
	/**
	 * TODO: Refactor, generalize, or remove this method
	 * 
	 * @see org.openmrs.api.OrderService#createOrdersAndEncounter(org.openmrs.Patient,
	 *      java.util.Collection)
	 */
	public void createOrdersAndEncounter(Patient p, Collection<Order> orders) throws APIException {
		
		// Get unknown user (or the authenticated user)
		User unknownUser = Context.getUserService().getUserByUsername("Unknown");
		// TODO: fix this hack
		if (unknownUser == null) {
			unknownUser = Context.getAuthenticatedUser();
		}
		
		// Get unknown location
		Location unknownLocation = Context.getLocationService().getDefaultLocation();
		
		if (unknownUser == null || unknownLocation == null) {
			throw new APIException("Couldn't find a Location and a User named 'Unknown'.");
		}
		
		EncounterType encounterType = Context.getEncounterService().getEncounterType("Regimen Change");
		if (encounterType == null) {
			throw new APIException("Couldn't find an encounter type 'Regimen Change'");
		}
		
		Encounter e = new Encounter();
		e.setPatient(p);
		e.setProvider(unknownUser.getPerson());
		e.setLocation(unknownLocation);
		e.setEncounterDatetime(new Date());
		// TODO: Remove hardcoded encounter type
		e.setEncounterType(encounterType);
		RequiredDataAdvice.recursivelyHandle(SaveHandler.class, e, null);
		for (Order order : orders) {
			e.addOrder(order);
			order.setEncounter(e);
		}
		Context.getEncounterService().saveEncounter(e);
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
	 *      org.openmrs.api.OrderService.ORDER_STATUS, java.util.List, java.util.List,
	 *      java.util.List)
	 */
	public <Ord extends Order> List<Ord> getOrders(Class<Ord> orderClassType, List<Patient> patients,
	        List<Concept> concepts, ORDER_STATUS status, List<User> orderers, List<Encounter> encounters,
	        List<OrderType> orderTypes) {
		if (orderClassType == null)
			throw new APIException(
			        "orderClassType cannot be null.  An order type of Order.class or DrugOrder.class is required");
		
		if (patients == null)
			patients = new Vector<Patient>();
		
		if (concepts == null)
			concepts = new Vector<Concept>();
		
		if (status == null)
			status = ORDER_STATUS.CURRENT;
		
		if (orderers == null)
			orderers = new Vector<User>();
		
		if (encounters == null)
			encounters = new Vector<Encounter>();
		
		if (orderTypes == null)
			orderTypes = new Vector<OrderType>();
		
		return dao.getOrders(orderClassType, patients, concepts, status, orderers, encounters, orderTypes);
	}
	
	/**
	 * @see org.openmrs.api.OrderService#getOrdersByUser(org.openmrs.User)
	 */
	public List<Order> getOrdersByUser(User user) throws APIException {
		if (user == null)
			throw new APIException("Unable to get orders if I am not given a user");
		
		List<User> users = new Vector<User>();
		users.add(user);
		
		return getOrders(Order.class, null, null, ORDER_STATUS.NOTVOIDED, users, null, null);
	}
	
	/**
	 * @see org.openmrs.api.OrderService#getOrdersByPatient(org.openmrs.Patient)
	 */
	public List<Order> getOrdersByPatient(Patient patient) throws APIException {
		if (patient == null)
			throw new APIException("Unable to get orders if I am not given a patient");
		
		List<Patient> patients = new Vector<Patient>();
		patients.add(patient);
		
		return getOrders(Order.class, patients, null, ORDER_STATUS.NOTVOIDED, null, null, null);
	}
	
	/**
	 * @see org.openmrs.api.OrderService#getDrugOrdersByPatient(org.openmrs.Patient, int, boolean)
	 */
	private List<DrugOrder> getDrugOrdersByPatient(Patient patient, int whatToShow, boolean includeVoided) {
		return getDrugOrdersByPatient(patient, convertToOrderStatus(whatToShow), includeVoided);
	}
	
	/**
	 * @see org.openmrs.api.OrderService#getDrugOrdersByPatient(org.openmrs.Patient,
	 *      org.openmrs.api.OrderService.ORDER_STATUS, boolean)
	 */
	@SuppressWarnings("unchecked")
	public List<DrugOrder> getDrugOrdersByPatient(Patient patient, ORDER_STATUS orderStatus, boolean includeVoided) {
		if (patient == null)
			throw new APIException("Unable to get drug orders if not given a patient");
		
		List<Patient> patients = new Vector<Patient>();
		patients.add(patient);
		
		List<DrugOrder> drugOrders = getOrders(DrugOrder.class, patients, null, ORDER_STATUS.ANY, null, null, null);
		
		// loop over the drug orders and add them if they are within the current desired order
		if (drugOrders != null) {
			if (orderStatus == ORDER_STATUS.ANY)
				return drugOrders;
			else {
				// the user wants to limit the type of drug order to get, so loop over
				// them all and do the logic on each 
				
				List<DrugOrder> ret = new ArrayList<DrugOrder>();
				
				for (DrugOrder drugOrder : drugOrders) {
					if (orderStatus == ORDER_STATUS.CURRENT && drugOrder.isCurrent())
						ret.add(drugOrder);
					else if (orderStatus == ORDER_STATUS.CURRENT_AND_FUTURE
					        && (drugOrder.isCurrent() || drugOrder.isFuture()))
						ret.add(drugOrder);
					else if (orderStatus == ORDER_STATUS.NOTVOIDED && !drugOrder.getVoided())
						ret.add(drugOrder);
					else if (orderStatus == ORDER_STATUS.COMPLETE && drugOrder.isDiscontinuedRightNow())
						ret.add(drugOrder);
				}
				
				return ret;
			}
		}
		
		// default return if no drug orders were found in the database
		return Collections.EMPTY_LIST;
	}
	
	/**
	 * @see org.openmrs.api.OrderService#getDrugOrdersByPatient(org.openmrs.Patient,
	 *      org.openmrs.api.OrderService.ORDER_STATUS)
	 */
	public List<DrugOrder> getDrugOrdersByPatient(Patient patient, ORDER_STATUS orderStatus) {
		return getDrugOrdersByPatient(patient, orderStatus, false);
	}
	
	/**
	 * @see org.openmrs.api.OrderService#getAllOrderTypes()
	 */
	public List<OrderType> getAllOrderTypes() throws APIException {
		return getAllOrderTypes(true);
	}
	
	/**
	 * @see org.openmrs.api.OrderService#getAllOrderTypes(boolean)
	 */
	public List<OrderType> getAllOrderTypes(boolean includeRetired) throws APIException {
		return dao.getAllOrderTypes(includeRetired);
	}
	
	/**
	 * @see org.openmrs.api.OrderService#getOrderType(java.lang.Integer)
	 */
	public OrderType getOrderType(Integer orderTypeId) throws APIException {
		return dao.getOrderType(orderTypeId);
	}
	
	/**
	 * @see org.openmrs.api.OrderService#getDrugOrdersByPatient(org.openmrs.Patient)
	 */
	public List<DrugOrder> getDrugOrdersByPatient(Patient patient) throws APIException {
		List<Patient> patients = new Vector<Patient>();
		patients.add(patient);
		
		return getOrders(DrugOrder.class, patients, null, ORDER_STATUS.NOTVOIDED, null, null, null);
	}
	
	/**
	 * @see org.openmrs.api.OrderService#getStandardRegimens()
	 */
	public List<RegimenSuggestion> getStandardRegimens() {
		DrugOrderSupport dos = null;
		List<RegimenSuggestion> standardRegimens = null;
		
		try {
			dos = DrugOrderSupport.getInstance();
		}
		catch (Exception e) {
			log.error("Error getting instance of DrugOrderSupport object", e);
		}
		
		if (dos != null) {
			standardRegimens = dos.getStandardRegimens();
		} else {
			log.error("DrugOrderSupport object is null after new instance");
		}
		
		return standardRegimens;
	}
	
	/**
	 * Convenience method to convert between the old integer Order status and the new enumeration
	 * ORDER_STATUS. This method can be removed when all deprecated methods using the Integer order
	 * status are removed
	 * 
	 * @param oldOrderStatus
	 * @return
	 */
	@SuppressWarnings("deprecation")
	private ORDER_STATUS convertToOrderStatus(Integer oldOrderStatus) {
		switch (oldOrderStatus) {
			case SHOW_CURRENT:
				return ORDER_STATUS.CURRENT;
			case SHOW_ALL:
				return ORDER_STATUS.ANY;
			case SHOW_COMPLETE:
				return ORDER_STATUS.COMPLETE;
			case SHOW_CURRENT_AND_FUTURE:
				return ORDER_STATUS.CURRENT_AND_FUTURE;
				//case SHOW_NOTVOIDED:
				// fall through to default
			default:
				return ORDER_STATUS.NOTVOIDED;
		}
	}
	
	/**
	 * @see org.openmrs.api.OrderService#getOrderByUuid(java.lang.String)
	 */
	public Order getOrderByUuid(String uuid) throws APIException {
		return dao.getOrderByUuid(uuid);
	}
	
	/**
	 * @see org.openmrs.api.OrderService#getOrderTypeByUuid(java.lang.String)
	 */
	public OrderType getOrderTypeByUuid(String uuid) throws APIException {
		return dao.getOrderTypeByUuid(uuid);
	}
	
	/**
	 * @see org.openmrs.api.OrderService#getOrdersByEncounter(org.openmrs.Encounter)
	 */
	public List<Order> getOrdersByEncounter(Encounter encounter) {
		List<Encounter> encounters = new Vector<Encounter>();
		encounters.add(encounter);
		
		return getOrders(Order.class, null, null, null, null, encounters, null);
	}
	
	/**
	 * @see org.openmrs.api.OrderNumberGenerator#getNewOrderNumber()
	 */
	@Override
	public synchronized String getNewOrderNumber() {
		GlobalProperty globalProperty = Context.getAdministrationService().getGlobalPropertyObject(
		    OpenmrsConstants.GLOBAL_PROPERTY_NEXT_ORDER_NUMBER);
		if (globalProperty == null) {
			globalProperty = new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_NEXT_ORDER_NUMBER,
			        ORDER_NUMBER_START_VALUE, "The next order number available for assignment");
		}
		
		String gpTextValue = globalProperty.getPropertyValue();
		if (!StringUtils.hasText(gpTextValue)) {
			gpTextValue = ORDER_NUMBER_START_VALUE;
		}
		
		Long gpNumericValue = null;
		try {
			gpNumericValue = Long.parseLong(gpTextValue);
		}
		catch (NumberFormatException ex) {
			gpNumericValue = 1l;
			gpTextValue = ORDER_NUMBER_START_VALUE;
		}
		
		String orderNumber = ORDER_NUMBER_PREFIX + gpTextValue;
		
		globalProperty.setPropertyValue(String.valueOf(gpNumericValue + 1));
		
		Context.addProxyPrivilege(PrivilegeConstants.MANAGE_GLOBAL_PROPERTIES);
		try {
			Context.getAdministrationService().saveGlobalProperty(globalProperty);
		}
		finally {
			Context.removeProxyPrivilege(PrivilegeConstants.MANAGE_GLOBAL_PROPERTIES);
		}
		
		return orderNumber;
	}
}

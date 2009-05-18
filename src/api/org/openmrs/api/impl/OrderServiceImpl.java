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
import java.util.Map;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.DrugOrder;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.Order;
import org.openmrs.OrderType;
import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.aop.RequiredDataAdvice;
import org.openmrs.api.APIException;
import org.openmrs.api.OrderService;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.OrderDAO;
import org.openmrs.api.handler.SaveHandler;
import org.openmrs.order.DrugOrderSupport;
import org.openmrs.order.OrderUtil;
import org.openmrs.order.RegimenSuggestion;
import org.openmrs.validator.ValidateUtil;
import org.springframework.util.StringUtils;

/**
 * Default implementation of the Order-related services class. This method should not be invoked by
 * itself. Spring injection is used to inject this implementation into the ServiceContext. Which
 * implementation is injected is determined by the spring application context file:
 * /metadata/api/spring/applicationContext.xml
 * 
 * @see org.openmrs.api.OrderService
 */
public class OrderServiceImpl extends BaseOpenmrsService implements OrderService {
	
	protected final Log log = LogFactory.getLog(getClass());
	
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
		ValidateUtil.validate(order);
		
		return dao.saveOrder(order);
	}
	
	/**
	 * @see org.openmrs.api.OrderService#createOrder(org.openmrs.Order)
	 * @deprecated
	 */
	@Deprecated
	public void createOrder(Order order) throws APIException {
		saveOrder(order);
	}
	
	/**
	 * @see org.openmrs.api.OrderService#updateOrder(org.openmrs.Order)
	 * @deprecated
	 */
	@Deprecated
	public void updateOrder(Order order) throws APIException {
		saveOrder(order);
	}
	
	/**
	 * @see org.openmrs.api.OrderService#deleteOrder(org.openmrs.Order)
	 * @deprecated
	 */
	@Deprecated
	public void deleteOrder(Order order) throws APIException {
		purgeOrder(order);
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
			// TODO delete other order stuff before deleting this order
			// (like DrugOrder?)
			throw new APIException("Cascade purging of Orders is not written yet");
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
	 * @see org.openmrs.api.OrderService#createOrderType(org.openmrs.OrderType)
	 * @deprecated
	 */
	@Deprecated
	public void createOrderType(OrderType orderType) throws APIException {
		saveOrderType(orderType);
	}
	
	/**
	 * @see org.openmrs.api.OrderService#updateOrderType(org.openmrs.OrderType)
	 * @deprecated
	 */
	@Deprecated
	public void updateOrderType(OrderType orderType) throws APIException {
		saveOrderType(orderType);
	}
	
	/**
	 * @see org.openmrs.api.OrderService#deleteOrderType(org.openmrs.OrderType)
	 * @deprecated
	 */
	@Deprecated
	public void deleteOrderType(OrderType orderType) throws APIException {
		purgeOrderType(orderType);
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
		e.setProvider(unknownUser);
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
	 * @see org.openmrs.api.OrderService#getDrugOrder(java.lang.Integer)
	 * @deprecated
	 */
	@Deprecated
	public DrugOrder getDrugOrder(Integer drugOrderId) throws APIException {
		return getOrder(drugOrderId, DrugOrder.class);
	}
	
	/**
	 * @see org.openmrs.api.OrderService#getOrder(java.lang.Integer, java.lang.Class)
	 */
	public <o extends Order> o getOrder(Integer orderId, Class<o> orderClassType) throws APIException {
		return dao.getOrder(orderId, orderClassType);
	}
	
	/**
	 * @deprecated This is a dumb method
	 */
	@Deprecated
	public List<Order> getOrders() throws APIException {
		return getOrders(Order.class, null, null, null, null, null, null);
	}
	
	/**
	 * @deprecated This is a dumb method
	 */
	@Deprecated
	public List<DrugOrder> getDrugOrders() throws APIException {
		return getOrders(DrugOrder.class, null, null, null, null, null, null);
	}
	
	/**
	 * @see org.openmrs.api.OrderService#getOrders(java.lang.Class, java.util.List, java.util.List,
	 *      org.openmrs.api.OrderService.ORDER_STATUS, java.util.List, java.util.List,
	 *      java.util.List)
	 */
	public <Ord extends Order> List<Ord> getOrders(Class<Ord> orderClassType, List<Patient> patients,
	                                               List<Concept> concepts, ORDER_STATUS status, List<User> orderers,
	                                               List<Encounter> encounters, List<OrderType> orderTypes) {
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
	 * @see org.openmrs.api.OrderService#getDrugOrdersByPatient(org.openmrs.Patient, int)
	 * @deprecated
	 */
	@Deprecated
	public List<DrugOrder> getDrugOrdersByPatient(Patient patient, int whatToShow) {
		return getDrugOrdersByPatient(patient, whatToShow, false);
	}
	
	/**
	 * @see org.openmrs.api.OrderService#getDrugOrdersByPatient(org.openmrs.Patient, int, boolean)
	 * @deprecated
	 */
	@Deprecated
	public List<DrugOrder> getDrugOrdersByPatient(Patient patient, int whatToShow, boolean includeVoided) {
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
	 * @see org.openmrs.api.OrderService#getOrderTypes()
	 * @deprecated
	 */
	@Deprecated
	public List<OrderType> getOrderTypes() throws APIException {
		return getAllOrderTypes(true);
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
	 * @see org.openmrs.api.OrderService#getDrugSetsByConcepts(java.util.List, java.util.List)
	 * @deprecated use {@link OrderUtil#getDrugSetsByConcepts(List, List)}
	 */
	@Deprecated
	public Map<Concept, List<DrugOrder>> getDrugSetsByConcepts(List<DrugOrder> drugOrders, List<Concept> drugSets)
	                                                                                                              throws APIException {
		return OrderUtil.getDrugSetsByConcepts(drugOrders, drugSets);
	}
	
	/**
	 * @see org.openmrs.api.OrderService#getDrugSetsByDrugSetIdList(java.util.List,
	 *      java.lang.String, java.lang.String)
	 * @deprecated use {@link OrderUtil#getDrugSetsByDrugSetIdList(List, String, String)}
	 */
	@Deprecated
	public Map<String, List<DrugOrder>> getDrugSetsByDrugSetIdList(List<DrugOrder> orderList, String drugSetIdList,
	                                                               String delimiter) {
		return OrderUtil.getDrugSetsByDrugSetIdList(orderList, drugSetIdList, delimiter);
	}
	
	/**
	 * @see org.openmrs.api.OrderService#getDrugSetHeadersByDrugSetIdList(java.lang.String)
	 * @deprecated use {@link OrderUtil#getDrugSetHeadersByDrugSetIdList(String)}
	 */
	@Deprecated
	public Map<String, String> getDrugSetHeadersByDrugSetIdList(String drugSetIds) {
		return OrderUtil.getDrugSetHeadersByDrugSetIdList(drugSetIds);
	}
	
	/**
	 * @see org.openmrs.api.OrderService#discontinueDrugSet(org.openmrs.Patient, java.lang.String,
	 *      org.openmrs.Concept, java.util.Date)
	 * @deprecated use {@link OrderUtil#discontinueDrugSet(Patient, String, Concept, Date)}
	 */
	@Deprecated
	public void discontinueDrugSet(Patient patient, String drugSetId, Concept discontinueReason, Date discontinueDate) {
		OrderUtil.discontinueDrugSet(patient, drugSetId, discontinueReason, discontinueDate);
	}
	
	/**
	 * @see org.openmrs.api.OrderService#voidDrugSet(Patient, String, String, int)
	 * @deprecated use
	 *             {@link OrderUtil#voidDrugSet(Patient, String, String, org.openmrs.api.OrderService.ORDER_STATUS)}
	 */
	@Deprecated
	public void voidDrugSet(Patient patient, String drugSetId, String voidReason, int whatToVoid) {
		OrderUtil.voidDrugSet(patient, drugSetId, voidReason, convertToOrderStatus(whatToVoid));
	}
	
	/**
	 * @see org.openmrs.api.OrderService#discontinueAllOrders(org.openmrs.Patient,
	 *      org.openmrs.Concept, java.util.Date)
	 * @deprecated use {@link OrderUtil#discontinueAllOrders(Patient, Concept, Date)}
	 */
	@Deprecated
	public void discontinueAllOrders(Patient patient, Concept discontinueReason, Date discontinueDate) {
		OrderUtil.discontinueAllOrders(patient, discontinueReason, discontinueDate);
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
}

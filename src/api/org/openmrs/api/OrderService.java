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
package org.openmrs.api;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.openmrs.Concept;
import org.openmrs.DrugOrder;
import org.openmrs.Encounter;
import org.openmrs.Order;
import org.openmrs.OrderType;
import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.annotation.Authorized;
import org.openmrs.api.db.OrderDAO;
import org.openmrs.order.RegimenSuggestion;
import org.openmrs.util.OpenmrsConstants;
import org.springframework.transaction.annotation.Transactional;

/**
 * Contains methods pertaining to creating/deleting/voiding Orders and DrugOrders Use:<br/>
 * 
 * <pre>
 *   Order order = new Order();
 *   order.set___(___);
 *   ...etc
 *   Context.getOrderService().saveOrder(order);
 * </pre>
 */
@Transactional
public interface OrderService extends OpenmrsService {
	
	/**
	 * @deprecated use {@link ORDER_STATUS#CURRENT}
	 */
	public static final int SHOW_CURRENT = 1;
	
	/**
	 * @deprecated use {@link ORDER_STATUS.ANY}
	 */
	public static final int SHOW_ALL = 2;
	
	/**
	 * @deprecated use {@link ORDER_STATUS.COMPLETE}
	 */
	public static final int SHOW_COMPLETE = 3;
	
	/**
	 * @deprecated use {@link ORDER_STATUS.NOTVOIDED}
	 */
	public static final int SHOW_NOTVOIDED = 4;
	
	/**
	 * The type of status to match on an order. Used in getOrder* methods
	 */
	public static enum ORDER_STATUS {
		/**
		 * The patient is considered to be currently on this order
		 */
		CURRENT,

		/**
		 * All orders match on this status
		 */
		ANY,

		/**
		 * Only orders that the patient has completed
		 */
		COMPLETE,

		/**
		 * All orders that have not been voided/deleted
		 */
		NOTVOIDED
	}
	
	/**
	 * Setter for the Order data access object. The dao is used for saving and getting orders
	 * to/from the database
	 * 
	 * @param dao The data access object to use
	 */
	public void setOrderDAO(OrderDAO dao);
	
	/**
	 * @deprecated use #saveOrder(Order)
	 */
	@Authorized(OpenmrsConstants.PRIV_ADD_ORDERS)
	public void createOrder(Order order) throws APIException;
	
	/**
	 * @deprecated use #saveOrder(Order)
	 */
	@Authorized(OpenmrsConstants.PRIV_EDIT_ORDERS)
	public void updateOrder(Order order) throws APIException;
	
	/**
	 * Save or update the given <code>order</code> in the database
	 * 
	 * @param order the Order to save
	 * @return the Order that was saved
	 * @throws APIException
	 * @should not save order if order doesnt validate
	 */
	@Authorized( { OpenmrsConstants.PRIV_EDIT_ORDERS, OpenmrsConstants.PRIV_ADD_ORDERS })
	public Order saveOrder(Order order) throws APIException;
	
	/**
	 * @deprecated use #purgeOrder(Order)
	 */
	@Authorized(OpenmrsConstants.PRIV_DELETE_ORDERS)
	public void deleteOrder(Order order) throws APIException;
	
	/**
	 * Completely delete an order from the database. This should not typically be used unless
	 * desperately needed. Most orders should just be voided. See {@link #voidOrder(Order, String)}
	 * 
	 * @param order The Order to remove from the system
	 * @throws APIException
	 */
	@Authorized(OpenmrsConstants.PRIV_PURGE_ORDERS)
	public void purgeOrder(Order order) throws APIException;
	
	/**
	 * Mark an order as voided. This functionally removes the Order from the system while keeping a
	 * semblance
	 * 
	 * @param voidReason
	 * @param Order to void
	 * @return the Order that was voided
	 * @throws APIException
	 */
	@Authorized(OpenmrsConstants.PRIV_DELETE_ORDERS)
	public Order voidOrder(Order order, String voidReason) throws APIException;
	
	/**
	 * Mark the given order as discontinued. This should be used when patients are no longer on this
	 * Order. If this is was invalid Order, the {@link #voidOrder(Order, String)} method should
	 * probably be used.
	 * 
	 * @param discontinueReason reason for discontinuing this order
	 * @param Order to discontinue
	 * @return The Order that was discontinued
	 * @throws APIException
	 */
	@Authorized(OpenmrsConstants.PRIV_EDIT_ORDERS)
	public Order discontinueOrder(Order order, Concept discontinueReason, Date discontinueDate) throws APIException;
	
	/**
	 * Creates a collection of orders and an encounter to hold them. orders[i].encounter will be set
	 * to the new encounter. If there's an EncounterType with name "Regimen Change", then the
	 * newly-created encounter will have that type
	 * 
	 * @param p the patient to add Orders to
	 * @param orders The Orders to add to the Patient (and to the makeshift Encounter)
	 * @throws APIException if there is no User with username Unknown or no Location with name
	 *             Unknown or Unknown Location, or if there's no encounter type with name 'Regimen
	 *             Change'
	 */
	@Authorized(value = { OpenmrsConstants.PRIV_ADD_ORDERS, OpenmrsConstants.PRIV_ADD_ENCOUNTERS }, requireAll = true)
	public void createOrdersAndEncounter(Patient p, Collection<Order> orders) throws APIException;
	
	/**
	 * Get order by internal primary key identifier
	 * 
	 * @param orderId internal order identifier
	 * @return order with given internal identifier
	 * @throws APIException
	 * @see {@link #getOrder(Integer, Class)}
	 */
	@Transactional(readOnly = true)
	@Authorized(OpenmrsConstants.PRIV_VIEW_ORDERS)
	public Order getOrder(Integer orderId) throws APIException;
	
	/**
	 * Gets the order with the associated order id
	 * 
	 * @param <Ord> An Order type. Currently only org.openmrs.Order or org.openmrs.DrugOrder
	 * @param orderId the primary key of the Order
	 * @param orderClassType The class of Order to fetch (Currently only org.openmrs.Order or
	 *            org.openmrs.DrugOrder)
	 * @return The Order in the system corresponding to given primary key id
	 * @throws APIException
	 */
	@Authorized(OpenmrsConstants.PRIV_VIEW_ORDERS)
	public <Ord extends Order> Ord getOrder(Integer orderId, Class<Ord> orderClassType) throws APIException;
	
	/**
	 * @deprecated use {@link #getOrder(Integer, Class)} with DrugOrder.class as second parameter
	 *             instead
	 */
	@Transactional(readOnly = true)
	@Authorized(OpenmrsConstants.PRIV_VIEW_ORDERS)
	public DrugOrder getDrugOrder(Integer drugOrderId) throws APIException;
	
	/**
	 * This searches for orders given the parameters. Most arguments are optional (nullable). If
	 * multiple arguments are given, the returned orders will match on all arguments.
	 * 
	 * @param <o> The type of Order to get determined by <code>orderType</code> parameter
	 * @param orderClassType The type of Order to get (currently only options are Order and
	 *            DrugOrder)
	 * @param patients The patients to get orders for
	 * @param concepts The concepts in order.getConcept to get orders for
	 * @param status The ORDER_STATUS of the orders for its patient
	 * @param orderers The users/orderers of the
	 * @param encounters The encounters that the orders are assigned to
	 * @param orderTypes The OrderTypes to match on
	 * @return list of Orders matching the parameters
	 */
	@Authorized(OpenmrsConstants.PRIV_VIEW_ORDERS)
	public <Ord extends Order> List<Ord> getOrders(Class<Ord> orderClassType, List<Patient> patients,
	                                               List<Concept> concepts, ORDER_STATUS status, List<User> orderers,
	                                               List<Encounter> encounters, List<OrderType> orderTypes);
	
	/**
	 * @deprecated this method would return a very large list for most systems and doesn't make
	 *             sense to be used. If _all_ Orders are really what is wanted, use
	 *             {@link #getOrders(Class, List, List, org.openmrs.api.OrderService.ORDER_STATUS, List, List, List)}
	 *             with empty parameters
	 */
	@Transactional(readOnly = true)
	@Authorized(OpenmrsConstants.PRIV_VIEW_ORDERS)
	public List<Order> getOrders() throws APIException;
	
	/**
	 * @deprecated this method would return a very large list for most systems and doesn't make
	 *             sense to be used. If _all_ Orders are really what is wanted, use
	 *             {@link #getOrders(Class, List, List, org.openmrs.api.OrderService.ORDER_STATUS, List, List, List)}
	 *             with empty parameters
	 */
	@Transactional(readOnly = true)
	@Authorized(OpenmrsConstants.PRIV_VIEW_ORDERS)
	public List<DrugOrder> getDrugOrders() throws APIException;
	
	/**
	 * Get all orders by the User that is marked as their orderer
	 * 
	 * @return orders list
	 * @throws APIException
	 */
	@Transactional(readOnly = true)
	@Authorized(OpenmrsConstants.PRIV_VIEW_ORDERS)
	public List<Order> getOrdersByUser(User user) throws APIException;
	
	/**
	 * Get all orders by Patient
	 * 
	 * @return orders list
	 * @throws APIException
	 */
	@Transactional(readOnly = true)
	@Authorized(OpenmrsConstants.PRIV_VIEW_ORDERS)
	public List<Order> getOrdersByPatient(Patient patient) throws APIException;
	
	/**
	 * @deprecated use
	 *             {@link #getDrugOrdersByPatient(Patient, org.openmrs.api.OrderService.ORDER_STATUS)}
	 */
	@Transactional(readOnly = true)
	@Authorized(OpenmrsConstants.PRIV_VIEW_ORDERS)
	public List<DrugOrder> getDrugOrdersByPatient(Patient patient, int whatToShow);
	
	/**
	 * Get drug orders for a given patient, not including voided orders
	 * 
	 * @param patient
	 * @param whatToShow
	 * @return List of drug orders, for the given patient, not including voided orders
	 * @see #getDrugOrdersByPatient(Patient, org.openmrs.api.OrderService.ORDER_STATUS, boolean)
	 */
	@Transactional(readOnly = true)
	@Authorized(OpenmrsConstants.PRIV_VIEW_ORDERS)
	public List<DrugOrder> getDrugOrdersByPatient(Patient patient, ORDER_STATUS orderStatus);
	
	/**
	 * @deprecated use
	 *             {@link #getDrugOrdersByPatient(Patient, org.openmrs.api.OrderService.ORDER_STATUS, boolean)}
	 */
	@Transactional(readOnly = true)
	@Authorized(OpenmrsConstants.PRIV_VIEW_ORDERS)
	public List<DrugOrder> getDrugOrdersByPatient(Patient patient, int whatToShow, boolean includeVoided);
	
	/**
	 * Get drug orders for a given patient
	 * 
	 * @param patient the owning Patient of the returned orders
	 * @param orderStatus the status of the orders returned
	 * @param includeVoided true/false whether or not to include voided drug orders
	 * @return List of drug orders for the given patient
	 */
	@Transactional(readOnly = true)
	@Authorized(OpenmrsConstants.PRIV_VIEW_ORDERS)
	public List<DrugOrder> getDrugOrdersByPatient(Patient patient, ORDER_STATUS orderStatus, boolean includeVoided);
	
	/**
	 * Un-discontinue order record. Reverse a previous call to
	 * {@link #discontinueOrder(Order, Concept, Date)}
	 * 
	 * @param order order to be un-discontinued
	 * @see #discontinueOrder(Order, Concept, Date)
	 * @return The Order that was undiscontinued
	 */
	@Authorized(OpenmrsConstants.PRIV_EDIT_ORDERS)
	public Order undiscontinueOrder(Order order) throws APIException;
	
	/**
	 * Unvoid order record. Reverse a previous call to {@link #voidOrder(Order, String)}
	 * 
	 * @param order order to be unvoided
	 * @return the Order that was unvoided
	 */
	@Authorized(OpenmrsConstants.PRIV_DELETE_ORDERS)
	public Order unvoidOrder(Order order) throws APIException;
	
	/**
	 * Save or update the given <code>orderType</code> in the database
	 * 
	 * @param orderType The OrderType to save in the database
	 * @return the freshly saved OrderType
	 * @throws APIException
	 */
	@Authorized(OpenmrsConstants.PRIV_MANAGE_ORDER_TYPES)
	public OrderType saveOrderType(OrderType orderType) throws APIException;
	
	/**
	 * Completely delete the order type from the system. If data has been stored using this
	 * orderType, an exception will be thrown. In that case, consider using the
	 * #retiredOrderType(OrderType, String) method
	 * 
	 * @param orderType
	 * @throws APIException
	 */
	@Authorized(OpenmrsConstants.PRIV_PURGE_ORDER_TYPES)
	public void purgeOrderType(OrderType orderType) throws APIException;
	
	/**
	 * @deprecated use #saveOrderType(OrderType)
	 */
	@Authorized(OpenmrsConstants.PRIV_MANAGE_ORDER_TYPES)
	public void createOrderType(OrderType orderType) throws APIException;
	
	/**
	 * @deprecated use #saveOrderType(OrderType)
	 */
	@Authorized(OpenmrsConstants.PRIV_MANAGE_ORDER_TYPES)
	public void updateOrderType(OrderType orderType) throws APIException;
	
	/**
	 * @deprecated use #purgeOrderType(OrderType)
	 */
	@Authorized(OpenmrsConstants.PRIV_PURGE_ORDER_TYPES)
	public void deleteOrderType(OrderType orderType) throws APIException;
	
	/**
	 * This method essentially takes the given <code>orderType</code> out of active use in OpenMRS.
	 * All references to this order type will remain in place. Future use of this order type are
	 * discouraged.
	 * 
	 * @param orderType the order type to retired
	 * @param reason The reason this order type is being taken our of commission.
	 * @return the retired order type
	 * @throws APIException
	 */
	@Authorized(OpenmrsConstants.PRIV_MANAGE_ORDER_TYPES)
	public OrderType retireOrderType(OrderType orderType, String reason) throws APIException;
	
	/**
	 * This will bring back a previously decommissioned OrderType
	 * 
	 * @param orderType the order type to unretire
	 * @return the retired order type
	 * @throws APIException
	 */
	@Authorized(OpenmrsConstants.PRIV_MANAGE_ORDER_TYPES)
	public OrderType unretireOrderType(OrderType orderType) throws APIException;
	
	/**
	 * Get all order types
	 * 
	 * @return order types list
	 * @throws APIException
	 * @deprecated use #getAllOrderTypes()
	 */
	@Transactional(readOnly = true)
	@Authorized(OpenmrsConstants.PRIV_VIEW_ORDER_TYPES)
	public List<OrderType> getOrderTypes() throws APIException;
	
	/**
	 * Get all order types, including retired ones
	 * 
	 * @return order types list
	 * @see #getAllOrderTypes(boolean)
	 * @throws APIException
	 */
	@Transactional(readOnly = true)
	@Authorized(OpenmrsConstants.PRIV_VIEW_ORDER_TYPES)
	public List<OrderType> getAllOrderTypes() throws APIException;
	
	/**
	 * Get all order types, only showing ones not marked as retired if includeRetired is true
	 * 
	 * @param includeRetired true/false whether to include retired orderTypes in this list
	 * @return order types list
	 * @throws APIException
	 */
	@Transactional(readOnly = true)
	@Authorized(OpenmrsConstants.PRIV_VIEW_ORDER_TYPES)
	public List<OrderType> getAllOrderTypes(boolean includeRetired) throws APIException;
	
	/**
	 * Get orderType by internal identifier
	 * 
	 * @param orderType id
	 * @return orderType with given internal identifier
	 * @throws APIException
	 */
	@Transactional(readOnly = true)
	@Authorized(OpenmrsConstants.PRIV_VIEW_ORDER_TYPES)
	public OrderType getOrderType(Integer orderTypeId) throws APIException;
	
	/**
	 * Get all orders for the given <code>patient</code>
	 * 
	 * @return orders list
	 * @throws APIException
	 */
	@Transactional(readOnly = true)
	@Authorized(OpenmrsConstants.PRIV_VIEW_ORDERS)
	public List<DrugOrder> getDrugOrdersByPatient(Patient patient) throws APIException;
	
	/**
	 * @deprecated use {@link org.openmrs.order.OrderUtil#getDrugSetsByConcepts(List, List)}
	 */
	@Transactional(readOnly = true)
	@Authorized(OpenmrsConstants.PRIV_VIEW_ORDERS)
	public Map<Concept, List<DrugOrder>> getDrugSetsByConcepts(List<DrugOrder> drugOrders, List<Concept> drugSets)
	                                                                                                              throws APIException;
	
	/**
	 * The standard regimens are currently stored in the application context file. See xml elements
	 * after the "STANDARD REGIMENS" comment in the web spring servlet:
	 * /web/WEB-INF/openmrs-servlet.xml (These really should be in the non-web spring app context:
	 * /metadata/api/spring/applicationContext.xml)
	 * 
	 * @return list of RegimenSuggestion objects that have been predefined
	 */
	@Transactional(readOnly = true)
	@Authorized(OpenmrsConstants.PRIV_VIEW_ORDERS)
	public List<RegimenSuggestion> getStandardRegimens();
	
	/**
	 * @deprecated use
	 *             {@link org.openmrs.order.OrderUtil#getDrugSetsByDrugSetIdList(List, String, String)}
	 */
	@Transactional(readOnly = true)
	@Authorized(OpenmrsConstants.PRIV_VIEW_ORDERS)
	public Map<String, List<DrugOrder>> getDrugSetsByDrugSetIdList(List<DrugOrder> orderList, String drugSetIdList,
	                                                               String delimiter);
	
	/**
	 * @deprecated use {@link org.openmrs.order.OrderUtil#getDrugSetHeadersByDrugSetIdList(String)}
	 */
	@Transactional(readOnly = true)
	@Authorized(OpenmrsConstants.PRIV_VIEW_ORDERS)
	public Map<String, String> getDrugSetHeadersByDrugSetIdList(String drugSetIds);
	
	/**
	 * @deprecated use
	 *             {@link org.openmrs.order.OrderUtil#discontinueDrugSet(Patient, String, Concept, Date)}
	 */
	@Authorized(OpenmrsConstants.PRIV_EDIT_ORDERS)
	public void discontinueDrugSet(Patient patient, String drugSetId, Concept discontinueReason, Date discontinueDate);
	
	/**
	 * @deprecated use
	 *             {@link org.openmrs.order.OrderUtil#voidDrugSet(Patient, String, String, org.openmrs.api.OrderService.ORDER_STATUS)}
	 */
	@Authorized(OpenmrsConstants.PRIV_DELETE_ORDERS)
	public void voidDrugSet(Patient patient, String drugSetId, String voidReason, int whatToVoid);
	
	/**
	 * @deprecated use
	 *             {@link org.openmrs.order.OrderUtil#discontinueAllOrders(Patient, Concept, Date)}
	 */
	@Authorized(OpenmrsConstants.PRIV_EDIT_ORDERS)
	public void discontinueAllOrders(Patient patient, Concept discontinueReason, Date discontinueDate) throws APIException;
	
	/**
	 * Gets all orders contained in an encounter
	 * 
	 * @param encounter the encounter in which to search for orders
	 * @return orders contained in the given encounter
	 */
	@Transactional(readOnly = true)
	public List<Order> getOrdersByEncounter(Encounter encounter);
}

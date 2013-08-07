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

import java.util.Date;
import java.util.List;

import org.openmrs.Concept;
import org.openmrs.DrugOrder;
import org.openmrs.Encounter;
import org.openmrs.Order;
import org.openmrs.Order.OrderAction;
import org.openmrs.Orderable;
import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.annotation.Authorized;
import org.openmrs.api.db.OrderDAO;
import org.openmrs.order.RegimenSuggestion;
import org.openmrs.util.PrivilegeConstants;

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
public interface OrderService extends OpenmrsService {
	
	public String DC_REASON_REVISE = "REVISE";
	
	/**
	 * Setter for the Order data access object. The dao is used for saving and getting orders
	 * to/from the database
	 * 
	 * @param dao The data access object to use
	 */
	public void setOrderDAO(OrderDAO dao);
	
	/**
	 * Save or update the given <code>order</code> in the database
	 * 
	 * @param order the Order to save
	 * @return the Order that was saved
	 * @throws APIException
	 * @should not save order if order doesnt validate
	 * @should save discontinued reason non coded
	 * @should not allow you to change the order number of a saved order
	 * @should not allow you to edit an order after it has been activated
	 * @should allow you to edit an order before it is activated
	 * @should not allow you to save an order that is not activated and signed
	 * @should save new version of an existing order
	 * @should asign order number for new order
	 */
	@Authorized( { PrivilegeConstants.EDIT_ORDERS, PrivilegeConstants.ADD_ORDERS })
	public Order saveOrder(Order order) throws APIException;
	
	/**
	 * Completely delete an order from the database. This should not typically be used unless
	 * desperately needed. Most orders should just be voided. See {@link #voidOrder(Order, String)}
	 * 
	 * @param order The Order to remove from the system
	 * @throws APIException
	 * @should delete order
	 */
	@Authorized(PrivilegeConstants.PURGE_ORDERS)
	public void purgeOrder(Order order) throws APIException;
	
	/**
	 * Completely delete an order from the database. This should not typically be used unless
	 * desperately needed. Most orders should just be voided. See {@link #voidOrder(Order, String)}
	 * This method is different from purgeOrder(Order order) above:
	 * If param cascade is false will completely delete an order from the database period
	 * If param cascade is true will completely delete an order from the database and delete any
	 * Obs that references the Order.
	 * 
	 * @param order The Order to remove from the system
	 * @param cascade
	 * @throws APIException
	 * @since 1.9.4
	 * @should delete order
	 * @should delete order when cascade is false
	 * @should delete order when cascade is true and also delete any Obs that references it
	 */
	@Authorized(PrivilegeConstants.PURGE_ORDERS)
	public void purgeOrder(Order order, boolean cascade) throws APIException;
	
	/**
	 * Mark an order as voided. This functionally removes the Order from the system while keeping a
	 * semblance
	 * 
	 * @param voidReason String reason
	 * @param order Order to void
	 * @return the Order that was voided
	 * @throws APIException
	 * @should fail if reason is null
	 * @should fail if reason is empty
	 * @should void given order
	 * @should not change an already voided order
	 */
	@Authorized(PrivilegeConstants.DELETE_ORDERS)
	public Order voidOrder(Order order, String voidReason) throws APIException;
	
	/**
	 * Get order by internal primary key identifier
	 * 
	 * @param orderId internal order identifpurgeier
	 * @return order with given internal identifier
	 * @throws APIException
	 * @see #getOrder(Integer, Class)
	 * @should find object given valid order id
	 * @should return null if no object found with given order id
	 */
	@Authorized(PrivilegeConstants.GET_ORDERS)
	public Order getOrder(Integer orderId) throws APIException;
	
	/**
	 * Get Order by its UUID
	 * 
	 * @param uuid
	 * @return
	 * @should find object given valid uuid
	 * @should return null if no object found with given uuid
	 */
	@Authorized(PrivilegeConstants.GET_ORDERS)
	public Order getOrderByUuid(String uuid) throws APIException;
	
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
	@Authorized(PrivilegeConstants.GET_ORDERS)
	public <Ord extends Order> Ord getOrder(Integer orderId, Class<Ord> orderClassType) throws APIException;
	
	/**
	 * Get all orders by Patient
	 * 
	 * @return orders list
	 * @throws APIException
	 * @should return list of non voided orders for patient
	 */
	@Authorized(PrivilegeConstants.GET_ORDERS)
	public List<Order> getOrdersByPatient(Patient patient) throws APIException;
	
	/**
	 * Get drug orders for a given patient
	 * 
	 * @param patient the owning Patient of the returned orders
	 * @param includeVoided true/false whether or not to include voided drug orders
	 * @return List of drug orders for the given patient
	 */
	@Authorized(PrivilegeConstants.GET_ORDERS)
	public List<DrugOrder> getDrugOrdersByPatient(Patient patient, boolean includeVoided);
	
	/**
	 * Unvoid order record. Reverse a previous call to {@link #voidOrder(Order, String)}
	 * 
	 * @param order order to be unvoided
	 * @return the Order that was unvoided
	 * @should unvoid given order
	 */
	@Authorized(PrivilegeConstants.DELETE_ORDERS)
	public Order unvoidOrder(Order order) throws APIException;
	
	/**
	 * Get all orders for the given <code>patient</code>
	 * 
	 * @return orders list
	 * @throws APIException
	 */
	@Authorized(PrivilegeConstants.GET_ORDERS)
	public List<DrugOrder> getDrugOrdersByPatient(Patient patient) throws APIException;
	
	/**
	 * The standard regimens are currently stored in the application context file. See xml elements
	 * after the "STANDARD REGIMENS" comment in the web spring servlet:
	 * /web/WEB-INF/openmrs-servlet.xml (These really should be in the non-web spring app context:
	 * /metadata/api/spring/applicationContext.xml)
	 * 
	 * @return list of RegimenSuggestion objects that have been predefined
	 */
	@Authorized(PrivilegeConstants.GET_ORDERS)
	public List<RegimenSuggestion> getStandardRegimens();
	
	/**
	 * Gets the latest order with this orderNumber.
	 * 
	 * @param orderNumber the order number.
	 * @return the order object.
	 * @should find object given valid order number
	 * @should return null if no object found with given order number
	 * @since 1.10
	 */
	@Authorized(PrivilegeConstants.GET_ORDERS)
	public Order getOrderByOrderNumber(String orderNumber);
	
	/**
	 * Gets all Order objects that use this Concept for a given patient.
	 * 
	 * @param patient the patient.
	 * @param concept the concept.
	 * @return the list of orders.
	 * @should return orders with the given concept
	 * @should return empty list for concept without orders
	 * @since 1.10
	 */
	@Authorized(PrivilegeConstants.GET_ORDERS)
	public List<Order> getOrderHistoryByConcept(Patient patient, Concept concept);
	
	/**
	 * Gets all Orders that are currently active. An active order is one that:
	 * <ol>
	 * <li>startDate <= <code>date</code>
	 * <li>discontinuedDate is null or >= <code>date</code>
	 * <li>autoExpireDate is null or >= <code>date</code>
	 * <li>dateActivated >= <code>date</code>
	 * <li>action != {@link OrderAction#DISCONTINUE}
	 * </ol>
	 * 
	 * @param p the patient to search on (required)
	 * @param date the date at which the orders should have been active. If null, is presumed to be
	 *            right now.
	 * @return list of active orders
	 * @should get orders with dateActivated before the given date
	 * @should not get orders with discontinuedDate before the given date
	 * @should get orders with startDate before the given date
	 * @since 1.10
	 */
	@Authorized(PrivilegeConstants.GET_ORDERS)
	public List<Order> getActiveOrdersByPatient(Patient p, Date date) throws APIException;
	
	/**
	 * Gets all DrugOrder objects that are currently active. An active DrugOrder is one that:
	 * <ol>
	 * <li>startDate <= <code>date</code>
	 * <li>discontinuedDate is null or >= <code>date</code>
	 * <li>autoExpireDate is null or >= <code>date</code>
	 * <li>dateActivated >= <code>date</code>
	 * <li>action != {@link OrderAction#DISCONTINUE}
	 * </ol>
	 * 
	 * @param p the patient to search on (required)
	 * @param date the date at which the orders should have been active. If null, is presumed to be
	 *            right now.
	 * @return list of active orders
	 * @should get orders with dateActivated before the given date
	 * @should not get orders with discontinuedDate before the given date
	 * @should get orders with startDate before the given date
	 * @since 1.10
	 */
	@Authorized(PrivilegeConstants.GET_ORDERS)
	public List<DrugOrder> getActiveDrugOrdersByPatient(Patient p, Date date) throws APIException;
	
	/**
	 * Finds all {@link Orderable}s that match <code>query</code>, based on a fuzzy comparison. (The
	 * precise comparison is implementation-dependent.) May include heterogenous types of
	 * orderables, e.g. some concepts, some drugs, some lab tests.
	 * 
	 * @param query partial string to be searched for
	 * @return Orderables that fuzzy-match <code>query</code>
	 * @should get orderable concepts by name and drug class
	 * @should get order sets
	 * @should fail if null passed in
	 * @throws APIException when error occurred
	 * @since 1.10
	 */
	public List<Orderable<?>> getOrderables(String query) throws APIException;
	
	/**
	 * This searches for orders given the parameters. Most arguments are optional (nullable). If
	 * multiple arguments are given, the returned orders will match on all arguments.
	 * 
	 * @param orderClassType The type of Order to get (currently only options are Order and
	 *            DrugOrder)
	 * @param patients The patients to get orders for
	 * @param concepts The concepts in order.getConcept to get orders for
	 * @param orderers The users/orderers of the
	 * @param encounters The encounters that the orders are assigned to
	 * @param asOfDate
	 * @param actionsToInclude a list of {@link OrderAction}s that the order must have one of.
	 * @param actionsToExclude a list of {@link OrderAction}s that the order should not have
	 * @return list of Orders matching the parameters
	 * @should not include voided orders
	 */
	@Authorized(PrivilegeConstants.GET_ORDERS)
	public <Ord extends Order> List<Ord> getOrders(Class<Ord> orderClassType, List<Patient> patients,
	        List<Concept> concepts, List<User> orderers, List<Encounter> encounters, Date asOfDate,
	        List<OrderAction> actionsToInclude, List<OrderAction> actionsToExclude);
	
	/**
	 * Mark the given order as discontinued. This should be used when patients are no longer on this
	 * Order. If this is was invalid Order, the {@link #voidOrder(Order, String)} method should
	 * probably be used.
	 * 
	 * @param order the order to be discontinued
	 * @param reason for discontinuing the order
	 * @param user the user discontinuing the order, defaults to authenticated user
	 * @param discontinueDate the date when to discontinue the order, defaults to current date
	 * @return the order that was discontinued
	 * @throws APIException
	 * @should discontinue and return the old order
	 * @should fail if the passed in discontinue date is before the date activated
	 * @should fail if the passed in discontinue date is in the future
	 * @should fail if the order is already discontinued
	 * @should fail if the discontinue date is after the auto expire date
	 * @since 1.9	 
	 *	 */
	@Authorized(PrivilegeConstants.EDIT_ORDERS)
	public Order discontinueOrder(Order order, String reason, User user, Date discontinueDate) throws APIException;
	
	/**
	 * Mark the given order as discontinued. This should be used when a continuing Order needs to be
	 * stopped. If this is was invalid Order, the {@link #voidOrder(Order, String)} method should be
	 * used instead. This method uses dateDiscontinued = now and discontinuedBy = current use
	 * 
	 * @param order the order to be discontinued
	 * @param reason for discontinuing the order
	 * @return the order that was discontinued
	 * @throws APIException
	 * @since 1.9
	 */
	@Authorized(PrivilegeConstants.EDIT_ORDERS)
	public Order discontinueOrder(Order order, String reason) throws APIException;
	
	/**
	 * Get orderable by identifier.
	 * 
	 * @param identifier the identifier
	 * @return requested orderable
	 * @throws APIException
	 * @should fetch an orderable with given identifier
	 * @should fail if null passed in
	 * @should return null if no orderable found with given identifier
	 * @since 1.10	 
	 */
	public Orderable<?> getOrderable(String identifier) throws APIException;
	
	/**
	 * Gets an order number that has not yet been used by any order. This method is only intended to
	 * be used by OpenMRS internally. Client or module code should not use it
	 * 
	 * @return the new order number.
	 * @should always return unique orderNumbers when called multiple times without saving orders
	 * @since 1.10
	 */
	public String getNewOrderNumber();
	
	/**
	 * Gets all drug orders for the given patient and ingredient, which can be either the drug
	 * itself or any ingredient.
	 * 
	 * @param patient
	 * @param ingredient
	 * @return the list of drug orders
	 * @should return drug orders matched by patient and intermediate concept
	 * @should return drug orders matched by patient and drug concept
	 * @should return empty list if no concept matched
	 * @should return empty list if no patient matched
	 * @since 1.10
	 */
	@Authorized(PrivilegeConstants.GET_ORDERS)
	public List<DrugOrder> getDrugOrdersByPatientAndIngredient(Patient patient, Concept ingredient);
	
	/**
	 * Get orders for a given patient
	 * 
	 * @param patient the owning Patient of the returned orders
	 * @param includeVoided true/false whether or not to include voided orders
	 * @return List of orders for the given patient
	 * @should return list of orders for patient with respect to the include voided flag
	 * @since 1.10
	 */
	@Authorized(PrivilegeConstants.GET_ORDERS)
	public List<Order> getOrdersByPatient(Patient patient, boolean includeVoided);
	
	/**
	 * Get orders by encounter
	 * 
	 * @return orders list
	 * @throws APIException
	 * @should return list of non voided orders by encounter
	 * @since 1.10
	 */
	@Authorized(PrivilegeConstants.GET_ORDERS)
	public List<Order> getOrdersByEncounter(Encounter encounter) throws APIException;
	
	/**
	 * Get orders by orderer
	 * 
	 * @return orders list
	 * @throws APIException
	 * @should return list of non voided orders by orderer
	 * @since 1.10
	 */
	@Authorized(PrivilegeConstants.GET_ORDERS)
	public List<Order> getOrdersByOrderer(User user) throws APIException;
	
	/**
	 * Get the history of orders related to a given order number
	 * 
	 * @param orderNumber
	 * @return the list of orders in a history
	 * @throws APIException
	 * @should return the list of orders in a history
	 * @since 1.10
	 */
	@Authorized(PrivilegeConstants.GET_ORDERS)
	public List<Order> getOrderHistoryByOrderNumber(String orderNumber);
	
}

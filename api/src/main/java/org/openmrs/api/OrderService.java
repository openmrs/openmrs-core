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
import org.openmrs.OrderGroup;
import org.openmrs.OrderSet;
import org.openmrs.Orderable;
import org.openmrs.Patient;
import org.openmrs.PublishedOrderSet;
import org.openmrs.User;
import org.openmrs.annotation.Authorized;
import org.openmrs.api.db.OrderDAO;
import org.openmrs.order.RegimenSuggestion;
import org.openmrs.util.PrivilegeConstants;
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
	 */
	@Authorized( { PrivilegeConstants.EDIT_ORDERS, PrivilegeConstants.ADD_ORDERS })
	public Order saveOrder(Order order) throws APIException;
	
	/**
	 * Completely delete an order from the database. This should not typically be used unless
	 * desperately needed. Most orders should just be voided. See {@link #voidOrder(Order, String)}
	 * 
	 * @param order The Order to remove from the system
	 * @throws APIException
	 */
	@Authorized(PrivilegeConstants.PURGE_ORDERS)
	public void purgeOrder(Order order) throws APIException;
	
	/**
	 * Mark an order as voided. This functionally removes the Order from the system while keeping a
	 * semblance
	 * 
	 * @param voidReason String reason
	 * @param order Order to void
	 * @return the Order that was voided
	 * @throws APIException
	 */
	@Authorized(PrivilegeConstants.DELETE_ORDERS)
	public Order voidOrder(Order order, String voidReason) throws APIException;
	
	/**
	 * Get order by internal primary key identifier
	 * 
	 * @param orderId internal order identifier
	 * @return order with given internal identifier
	 * @throws APIException
	 * @see #getOrder(Integer, Class)
	 */
	@Transactional(readOnly = true)
	@Authorized(PrivilegeConstants.VIEW_ORDERS)
	public Order getOrder(Integer orderId) throws APIException;
	
	/**
	 * Get Order by its UUID
	 * 
	 * @param uuid
	 * @return
	 * @should find object given valid uuid
	 * @should return null if no object found with given uuid
	 */
	@Transactional(readOnly = true)
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
	@Authorized(PrivilegeConstants.VIEW_ORDERS)
	public <Ord extends Order> Ord getOrder(Integer orderId, Class<Ord> orderClassType) throws APIException;
	
	/**
	 * Get all orders by Patient
	 * 
	 * @return orders list
	 * @throws APIException
	 */
	@Transactional(readOnly = true)
	@Authorized(PrivilegeConstants.VIEW_ORDERS)
	public List<Order> getOrdersByPatient(Patient patient) throws APIException;
	
	/**
	 * Get drug orders for a given patient
	 * 
	 * @param patient the owning Patient of the returned orders
	 * @param includeVoided true/false whether or not to include voided drug orders
	 * @return List of drug orders for the given patient
	 */
	@Transactional(readOnly = true)
	@Authorized(PrivilegeConstants.VIEW_ORDERS)
	public List<DrugOrder> getDrugOrdersByPatient(Patient patient, boolean includeVoided);
	
	/**
	 * Un-discontinue order record. Reverse a previous call to
	 * {@link #discontinueOrder(Order, Concept, Date)}
	 * 
	 * @param order order to be un-discontinued
	 * @see #discontinueOrder(Order, Concept, Date)
	 * @return The Order that was undiscontinued
	 */
	@Authorized(PrivilegeConstants.EDIT_ORDERS)
	public Order undiscontinueOrder(Order order) throws APIException;
	
	/**
	 * Unvoid order record. Reverse a previous call to {@link #voidOrder(Order, String)}
	 * 
	 * @param order order to be unvoided
	 * @return the Order that was unvoided
	 */
	@Authorized(PrivilegeConstants.DELETE_ORDERS)
	public Order unvoidOrder(Order order) throws APIException;
	
	/**
	 * Get all orders for the given <code>patient</code>
	 * 
	 * @return orders list
	 * @throws APIException
	 */
	@Transactional(readOnly = true)
	@Authorized(PrivilegeConstants.VIEW_ORDERS)
	public List<DrugOrder> getDrugOrdersByPatient(Patient patient) throws APIException;
	
	/**
	 * The standard regimens are currently stored in the application context file. See xml elements
	 * after the "STANDARD REGIMENS" comment in the web spring servlet:
	 * /web/WEB-INF/openmrs-servlet.xml (These really should be in the non-web spring app context:
	 * /metadata/api/spring/applicationContext.xml)
	 * 
	 * @return list of RegimenSuggestion objects that have been predefined
	 */
	@Transactional(readOnly = true)
	@Authorized(PrivilegeConstants.VIEW_ORDERS)
	public List<RegimenSuggestion> getStandardRegimens();
	
	/**
	 * Gets the latest order with this orderNumber.
	 * 
	 * @param orderNumber the order number.
	 * @return the order object.
	 */
	@Transactional(readOnly = true)
	public Order getOrderByOrderNumber(String orderNumber);
	
	/**
	 * Gets all Order objects that use this Concept for a given patient.
	 * 
	 * @param patient the patient.
	 * @param concept the concept.
	 * @return the list of orders.
	 */
	@Transactional(readOnly = true)
	public List<Order> getOrderHistoryByConcept(Patient patient, Concept concept);
	
	/**
	 * Signs an order.
	 * 
	 * @param order the order to sign.
	 * @param provider the user signing the order.
	 * @param date the date the order is signed
	 * @return the signed order.
	 */
	public Order signOrder(Order order, User provider, Date date) throws APIException;
	
	/**
	 * Activates an order.
	 * 
	 * @param order the order to activate.
	 * @param activatedBy the user activating the order.
	 * @param activationDate the date to activate the order on
	 * @return the activated order.
	 */
	public Order activateOrder(Order order, User activatedBy, Date activationDate) throws APIException;
	
	/**
	 * TO DO document how this converts filler to a String
	 * Fills an order.
	 * 
	 * @param order the order object.
	 * @param filler the filling person.
	 * @param dateFilled the date the order was filled (defaults to now, cannot be in future)
	 * @return the filled order.
	 * @throws APIException thrown if the order is not signed yet.
	 */
	public Order fillOrder(Order order, User filler, Date dateFilled) throws APIException;
	
	/**
	 * TO DO javadoc needs to explain the difference between this and the User version of fillOrder.
	 * Fills an order.
	 * 
	 * @param order the order object.
	 * @param filler the filling person.
	 * @param dateFilled the date the order was filled (defaults to now, cannot be in future)
	 * @return the filled order.
	 * @throws APIException thrown if the order is not signed yet.
	 */
	public Order fillOrder(Order order, String filler, Date dateFilled) throws APIException;
	
	/**
	 * Saves, Signs, and Activates an order.
	 * 
	 * @param order the order.
	 * @return the saved, signed and activated order.
	 * @should save sign activate order with unstructured dosing
	 * @should save sign activate order with structured dosing
	 */
	public Order signAndActivateOrder(Order order) throws APIException;
	
	/**
	 * Saves, Signs, and Activates an order.
	 * 
	 * @param order the order.
	 * @param user the user in charge of the order (defaults to authenticated user)
	 * @param date the date to sign and activate the order (cannot be in the future, defaults to
	 *            now)
	 * @return the saved, signed and activated order.
	 * @should save sign activate order with unstructured dosing
	 * @should save sign activate order with structured dosing
	 */
	public Order signAndActivateOrder(Order order, User user, Date date) throws APIException;
	
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
	 */
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
	 */
	List<DrugOrder> getActiveDrugOrdersByPatient(Patient p, Date date) throws APIException;
	
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
	 */
	List<Orderable<?>> getOrderables(String query) throws APIException;
	
	/**
	 * Saves, Signs, and Activates all orders within group.
	 * 
	 * @param group the orders group.
	 * @param user the user in charge of the orders group.
	 * @param activated the date of order activation
	 * @return the saved, signed and activated orders group.
	 * @should sign and activate orders in group
	 * @throws APIException when error occurred
	 */
	public OrderGroup signAndActivateOrdersInGroup(OrderGroup group, User user, Date activated) throws APIException;
	
	/**
	 * Creates or updates an OrderGroup
	 * 
	 * @param group the order group to save
	 * @return saved order group entity
	 * @should save new order group
	 * @should update existing order group
	 * @throws APIException when error occurred
	 */
	public OrderGroup saveOrderGroup(OrderGroup group) throws APIException;
	
	/**
	 * Marks an OrderGroup as deleted, also cascading this down to the orders within the group.
	 * 
	 * @param group the order group to be voided
	 * @param voidReason the cause why order is to be voided
	 * @return voided order group entity
	 * @should void order group
	 * @throws APIException when error occurred
	 */
	public OrderGroup voidOrderGroup(OrderGroup group, String voidReason) throws APIException;
	
	/**
	 * Restores an OrderGroup that had been marked as being deleted.
	 * 
	 * @param group the order group to be unvoided
	 * @return unvoid order group entity
	 * @throws APIException when error occurred
	 */
	public OrderGroup unvoidOrderGroup(OrderGroup group) throws APIException;
	
	/**
	 * Gets order group by its primary key
	 * 
	 * @param orderGroupId the id of order group
	 * @return order group entity if success, null otherwise
	 * @should return order group entity by id
	 * @should return null if order group doesn't exist
	 * @throws APIException when error occurred
	 */
	public OrderGroup getOrderGroup(Integer orderGroupId) throws APIException;
	
	/**
	 * Gets an OrderGroup by its uuid
	 * 
	 * @param uuid the unique identifier of order group
	 * @return order group entity if success, null otherwise
	 * @should get order group by uuid
	 * @throws APIException when error occurred
	 */
	public OrderGroup getOrderGroupByUuid(String uuid) throws APIException;
	
	/**
	 * Gets all non-voided OrderGroups for the specified patient
	 * 
	 * @param patient the patient, whose order groups will be retrieved
	 * @return list of patients order groups in case of success or null otherwise
	 * @should return not empty list of order groups
	 * @throws APIException when error occurred
	 */
	public List<OrderGroup> getOrderGroupsByPatient(Patient patient) throws APIException;
	
	/**
	 * Creates or updates an OrderSet
	 * 
	 * @param orderSet
	 * @return the saved OrderSet
	 * @since 1.9
	 * @should create new order set
	 * @should update existing order set
	 */
	@Authorized(PrivilegeConstants.MANAGE_ORDER_SETS)
	public OrderSet saveOrderSet(OrderSet orderSet);
	
	/**
	 * @param orderSetId
	 * @return the OrderSet with the given id
	 * @since 1.9
	 */
	@Transactional(readOnly = true)
	@Authorized(PrivilegeConstants.VIEW_ORDER_SETS)
	public OrderSet getOrderSet(Integer orderSetId);
	
	/**
	 * @param uuid
	 * @return the OrderSet with the given uuid
	 * @since 1.9
	 */
	@Transactional(readOnly = true)
	@Authorized(PrivilegeConstants.VIEW_ORDER_SETS)
	public OrderSet getOrderSetByUuid(String uuid);
	
	/**
	 * Associates the given Concept with the given OrderSet in the database
	 * 
	 * @param asConcept
	 * @param content
	 * @return the published entity
	 * @since 1.9
	 * @should publish an order set as a concept
	 * @should publish an order set as a concept overwriting the previous entity
	 */
	@Authorized(PrivilegeConstants.MANAGE_ORDER_SETS)
	public PublishedOrderSet publishOrderSet(Concept asConcept, OrderSet content);
	
	/**
	 * @param concept
	 * @return the {@link PublishedOrderSet} associated with the given Concept, or null if none is
	 *         associated
	 * @since 1.9
	 * @should get a published order set by concept
	 */
	@Transactional(readOnly = true)
	@Authorized(PrivilegeConstants.VIEW_ORDER_SETS)
	public PublishedOrderSet getPublishedOrderSet(Concept concept);
	
	/**
	 * @param query
	 * @return all {@link PublishedOrderSet}s that fuzzy-match the given query string
	 * @since 1.9
	 * @should get all published order sets by query
	 */
	@Transactional(readOnly = true)
	@Authorized(PrivilegeConstants.VIEW_ORDER_SETS)
	public List<PublishedOrderSet> getPublishedOrderSets(String query);
	
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
	@Authorized(PrivilegeConstants.VIEW_ORDERS)
	public <Ord extends Order> List<Ord> getOrders(Class<Ord> orderClassType, List<Patient> patients,
	        List<Concept> concepts, List<User> orderers, List<Encounter> encounters, Date asOfDate,
	        List<OrderAction> actionsToInclude, List<OrderAction> actionsToExclude);
	
	/**
	 * Mark the given order as discontinued. This should be used when patients are no longer on this
	 * Order. If this is was invalid Order, the {@link #voidOrder(Order, String)} method should
	 * probably be used.
	 * 
	 * @since 1.9
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
	 */
	@Authorized(PrivilegeConstants.EDIT_ORDERS)
	public Order discontinueOrder(Order order, String reason, User user, Date discontinueDate) throws APIException;
	
	/**
	 * Mark the given order as discontinued. This should be used when a continuing Order needs to be
	 * stopped. If this is was invalid Order, the {@link #voidOrder(Order, String)} method should be
	 * used instead. This method uses dateDiscontinued = now and discontinuedBy = current use
	 * 
	 * @since 1.9
	 * @param order the order to be discontinued
	 * @param reason for discontinuing the order
	 * @return the order that was discontinued
	 * @throws APIException
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
	 */
	@Transactional(readOnly = true)
	public Orderable<?> getOrderable(String identifier) throws APIException;
	
	/**
	 * Gets an order number that has not yet been used by any order. This method is only intended to
	 * be used by OpenMRS internally. Client or module code should not use it
	 * 
	 * @return the new order number.
	 * @should always return unique orderNumbers when called multiple times without saving orders
	 */
	@Transactional(readOnly = true)
	public String getNewOrderNumber();
}

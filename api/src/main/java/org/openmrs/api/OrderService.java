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

import org.openmrs.CareSetting;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Order;
import org.openmrs.OrderFrequency;
import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.annotation.Authorized;
import org.openmrs.api.db.OrderDAO;
import org.openmrs.util.PrivilegeConstants;

/**
 * Contains methods pertaining to creating/deleting/voiding Orders
 */
public interface OrderService extends OpenmrsService {
	
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
	 * Completely delete an order from the database. This should not typically be used unless
	 * desperately needed. Most orders should just be voided. See {@link #voidOrder(Order, String)}
	 * This method is different from purgeOrder(Order order) above: If param cascade is false will
	 * completely delete an order from the database period If param cascade is true will completely
	 * delete an order from the database and delete any Obs that references the Order.
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
	 */
	@Authorized(PrivilegeConstants.DELETE_ORDERS)
	public Order voidOrder(Order order, String voidReason) throws APIException;
	
	/**
	 * Get order by internal primary key identifier
	 * 
	 * @param orderId internal order identifier
	 * @return order with given internal identifier
	 * @throws APIException
	 */
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
	 * This searches for orders given the parameters. Most arguments are optional (nullable). If
	 * multiple arguments are given, the returned orders will match on all arguments. The orders are
	 * sorted by startDate with the latest coming first
	 * 
	 * @param orderClassType The type of Order to get (currently only options are Order and
	 *            DrugOrder)
	 * @param patients The patients to get orders for
	 * @param concepts The concepts in order.getConcept to get orders for
	 * @param orderers The users/orderers of the
	 * @param encounters The encounters that the orders are assigned to
	 * @return list of Orders matching the parameters
	 */
	@Authorized(PrivilegeConstants.VIEW_ORDERS)
	public <Ord extends Order> List<Ord> getOrders(Class<Ord> orderClassType, List<Patient> patients,
	        List<Concept> concepts, List<User> orderers, List<Encounter> encounters);
	
	/**
	 * Unvoid order record. Reverse a previous call to {@link #voidOrder(Order, String)}
	 * 
	 * @param order order to be unvoided
	 * @return the Order that was unvoided
	 */
	@Authorized(PrivilegeConstants.DELETE_ORDERS)
	public Order unvoidOrder(Order order) throws APIException;
	
	/**
	 * Gets the order identified by a given order number
	 * 
	 * @param orderNumber the order number
	 * @return the order object
	 * @should find object given valid order number
	 * @should return null if no object found with given order number
	 */
	public Order getOrderByOrderNumber(String orderNumber);
	
	/**
	 * Gets all Order objects that use this Concept for a given patient. Orders will be returned in
	 * the order in which they occurred, i.e. sorted by startDate starting with the latest
	 * 
	 * @param patient the patient.
	 * @param concept the concept.
	 * @return the list of orders.
	 * @should return orders with the given concept
	 * @should return empty list for concept without orders
	 */
	public List<Order> getOrderHistoryByConcept(Patient patient, Concept concept);
	
	/**
	 * Gets the next available order number seed
	 * 
	 * @return the order number seed
	 */
	@Authorized(PrivilegeConstants.ADD_ORDERS)
	public Long getNextOrderNumberSeedSequenceValue();
	
	/**
	 * Gets the order matching the specified order number and its previous orders in the ordering
	 * they occurred, i.e if this order has a previous order, fetch it and if it also has a previous
	 * order then fetch it until the original one with no previous order is reached
	 * 
	 * @param orderNumber the order number whose history to get
	 * @return a list of orders for given order number
	 * @should return return all order history for given order number
	 */
	public List<Order> getOrderHistoryByOrderNumber(String orderNumber);
	
	/**
	 * Gets all active orders for the specified patient matching the specified CareSetting, Order
	 * class as of the specified date. Below is the criteria for determining an active order:
	 * 
	 * <pre>
	 * <p>
	 * - Not voided
	 * - Not a discontinuation Order i.e one where action != Action#DISCONTINUE
	 * - startDate is before or equal to asOfDate
	 * - dateStopped and autoExpireDate are both null OR if it has dateStopped, then it should be
	 * after asOfDate OR if it has autoExpireDate, then it should be after asOfDate. NOTE: If both
	 * dateStopped and autoExpireDate are set then dateStopped wins because an order can never
	 * expire and then stopped later i.e. you stop an order that hasn't yet expired
	 * <p/>
	 * <pre/>
	 * 
	 * @param patient the patient
	 * @param orderClass the order class to match against, this is required
	 * @param careSetting the care setting, returns all ignoring care setting if value is null
	 * @param asOfDate defaults to current time
	 * @return all active orders for given patient parameters
	 * @should return all active orders for the specified patient
	 * @should return all active orders for the specified patient and care setting
	 * @should return all active drug orders for the specified patient
	 * @should return all active test orders for the specified patient
	 * @should fail if patient is null
	 * @should return active orders as of the specified date
	 */
	public <Ord extends Order> List<Ord> getActiveOrders(Patient patient, Class<Ord> orderClass, CareSetting careSetting,
	        Date asOfDate);
	
	/**
	 * Retrieve care setting by type
	 * 
	 * @param careSettingId
	 * @return the care setting
	 * @since 1.10
	 */
	public CareSetting getCareSetting(Integer careSettingId);
	
	/**
	 * Gets OrderFrequenecy that matches the specified orderFrequencyId
	 * 
	 * @param orderFrequencyId the id to match against
	 * @return OrderFrequency
	 * @since 1.10
	 * @should return the order frequency that matched the specified id
	 */
	public OrderFrequency getOrderFrequency(Integer orderFrequencyId);
}

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

import org.openmrs.Concept;
import org.openmrs.DrugOrder;
import org.openmrs.Encounter;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.annotation.Authorized;
import org.openmrs.api.db.OrderDAO;
import org.openmrs.order.RegimenSuggestion;
import org.openmrs.util.PrivilegeConstants;
import org.springframework.transaction.annotation.Transactional;

/**
 * Contains methods pertaining to creating/deleting/voiding Orders and DrugOrders Use:<br/>
 * 
 * @deprecated Will be removed in version 1.10
 * 
 *             <pre>
 *   Order order = new Order();
 *   order.set___(___);
 *   ...etc
 *   Context.getOrderService().saveOrder(order);
 * </pre>
 */
@Transactional
@Deprecated
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
	 * Mark the given order as discontinued. This should be used when patients are no longer on this
	 * Order. If this is was invalid Order, the {@link #voidOrder(Order, String)} method should
	 * probably be used.
	 * 
	 * @param discontinueReason String reason for discontinuing this order
	 * @param order Order to discontinue
	 * @return The Order that was discontinued
	 * @throws APIException
	 */
	@Authorized(PrivilegeConstants.EDIT_ORDERS)
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
	@Authorized(value = { PrivilegeConstants.ADD_ORDERS, PrivilegeConstants.ADD_ENCOUNTERS }, requireAll = true)
	public void createOrdersAndEncounter(Patient p, Collection<Order> orders) throws APIException;
	
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
	 * This searches for orders given the parameters. Most arguments are optional (nullable). If
	 * multiple arguments are given, the returned orders will match on all arguments.
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
	 * Get all orders by the User that is marked as their orderer
	 * 
	 * @return orders list
	 * @throws APIException
	 */
	@Transactional(readOnly = true)
	@Authorized(PrivilegeConstants.VIEW_ORDERS)
	public List<Order> getOrdersByUser(User user) throws APIException;
	
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
	 * @should return list of drug orders with given status
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
	 * Gets all orders contained in an encounter
	 * 
	 * @param encounter the encounter in which to search for orders
	 * @return orders contained in the given encounter
	 */
	@Transactional(readOnly = true)
	public List<Order> getOrdersByEncounter(Encounter encounter);
}

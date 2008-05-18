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
package org.openmrs.api.db;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.openmrs.Concept;
import org.openmrs.ConceptSet;
import org.openmrs.DrugOrder;
import org.openmrs.Encounter;
import org.openmrs.Order;
import org.openmrs.OrderType;
import org.openmrs.Patient;
import org.openmrs.api.OrderService;

/**
 * Order-related database functions
 * @version 1.0
 */
public interface OrderDAO {

	/**
	 * Create a new OrderType
	 * @param OrderType to create
	 * @throws DAOException
	 */
	public void createOrderType(OrderType orderType) throws DAOException;

	/**
	 * Update OrderType
	 * @param OrderType to update
	 * @throws DAOException
	 */
	public void updateOrderType(OrderType orderType) throws DAOException;

	/**
	 * Delete OrderType
	 * @param OrderType to delete
	 * @throws DAOException
	 */
	public void deleteOrderType(OrderType orderType) throws DAOException;
	
	public void discontinueOrder(Order order, Concept discontinueReason, Date discontinueDate) throws DAOException;
	
	
	/**
	 * Creates a new order record
	 * 
	 * @param order to be created
	 * @throws DAOException
	 */
	public void createOrder(Order order) throws DAOException;

	/**
	 * Get order by internal identifier
	 * 
	 * @param orderId internal order identifier
	 * @return order with given internal identifier
	 * @throws DAOException
	 */
	public Order getOrder(Integer orderId) throws DAOException;

	public DrugOrder getDrugOrder(Integer drugOrderId) throws DAOException;

	/**
	 * Get all orders
	 * 
	 * @return orders list
	 * @throws DAOException
	 */
	public List<Order> getOrders() throws DAOException;

	/**
	 * Get all orders for a particular Patient
	 * 
	 * @return orders list
	 * @throws DAOException
	 */
	public List<Order> getOrdersByPatient(Patient patient) throws DAOException;

	/**
	 * Update order 
	 * 
	 * @param Order order to update
	 * @throws DAOException
	 */
	public void updateOrder(Order order) throws DAOException;
	
	/**
	 * Get order by orderer
	 * 
	 * @param User orderer
	 * @return orders that were ordered by given User
	 * @throws DAOException
	 */
	//public Order getOrder(User orderer) throws DAOException;
		
	/**
	 * Discontinue order record
	 * 
	 * @param order order to be discontinued
	 * @param reason reason for discontinuing order
	 */
	public void discontinueOrder(Order order, Concept reason) throws DAOException;

	/**
	 * Undiscontinue order record
	 * 
	 * @param order order to be undiscontinued
	 */
	public void undiscontinueOrder(Order order) throws DAOException;

	
	/**
	 * Delete order from database. This <b>should not be called</b>
	 * except for testing and administration purposes.  Use the discontinue
	 * method instead.
	 * 
	 * @param orderId internal identifier of order to be deleted
	 * 
	 * @see #discontinueOrder(Order, String) 
	 */
	public void deleteOrder(Order order) throws DAOException;

	/**
	 * Void order record
	 * 
	 * @param order order to be voided
	 * @param reason reason for voiding order
	 */
	public void voidOrder(Order order, String reason) throws DAOException;

	/**
	 * Unvoid order record
	 * 
	 * @param order order to be unvoided
	 */
	public void unvoidOrder(Order order) throws DAOException;

	/**
	 * Get all order types
	 * 
	 * @return order types list
	 * @throws DAOException
	 */
	public List<OrderType> getOrderTypes() throws DAOException;

	/**
	 * Get orderType by internal identifier
	 * 
	 * @param orderType id
	 * @return orderType with given internal identifier
	 * @throws DAOException
	 */
	public OrderType getOrderType(Integer orderTypeId) throws DAOException;

	/**
	 * Get all drug orders
	 * 
	 * @return drug orders list
	 * @throws DAOException
	 */
	public List<DrugOrder> getDrugOrders() throws DAOException;

	/**
	 * Get all drug orders for a particular Patient, excluding voided ones
	 * 
	 * @return drug orders list
	 * @throws DAOException
	 */
	public List<DrugOrder> getDrugOrdersByPatient(Patient patient) throws DAOException;
	
	/**
	 * Get all drug orders for a particular Patient
	 *
	 * @param patient
	 * @param includeVoided
	 * @return List of drug orders for a particular patient
	 * @throws DAOException
	 */
	public List<DrugOrder> getDrugOrdersByPatient(Patient patient, boolean includeVoided) throws DAOException;

	public Map<ConceptSet, List<DrugOrder>> getConceptSetsByDrugOrders(List<DrugOrder> drugOrders) throws DAOException;

	/**
     * @see OrderService#getOrdersByEncounter(Encounter)
     */
    public List<Order> getOrdersByEncounter(Encounter encounter);
}

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

import java.util.List;

import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Order;
import org.openmrs.OrderType;
import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.api.OrderService.ORDER_STATUS;

/**
 * Order-related database functions This class should never be used directly. It should only be used
 * through the {@link org.openmrs.api.OrderService}
 * 
 * @see org.openmrs.api.OrderService
 */
public interface OrderDAO {
	
	// methods for the OrderType java pojo object
	
	/**
	 * @see org.openmrs.api.OrderService#saveOrderType(OrderType)
	 */
	public OrderType saveOrderType(OrderType orderType) throws DAOException;
	
	/**
	 * @see org.openmrs.api.OrderService#purgeOrderType(OrderType)
	 */
	public void deleteOrderType(OrderType orderType) throws DAOException;
	
	/**
	 * @see org.openmrs.api.OrderService#getAllOrderTypes(boolean)
	 */
	public List<OrderType> getAllOrderTypes(boolean includeRetired) throws DAOException;
	
	/**
	 * @see org.openmrs.api.OrderService#getOrderType(Integer)
	 */
	public OrderType getOrderType(Integer orderTypeId) throws DAOException;
	
	// methods for the Order java pojo object
	
	/**
	 * @see org.openmrs.api.OrderService#saveOrder(Order)
	 */
	public Order saveOrder(Order order) throws DAOException;
	
	/**
	 * @see org.openmrs.api.OrderService#purgeOrder(Order,boolean)
	 * @see org.openmrs.api.OrderService#purgeOrder(Order)
	 */
	public void deleteOrder(Order order) throws DAOException;
	
	/**
	 * @see org.openmrs.api.OrderService#getOrder(Integer, Class)
	 * @see org.openmrs.api.OrderService#getOrder(Integer)
	 * @see org.openmrs.api.OrderService#getDrugOrder(Integer)
	 */
	public <Ord extends Order> Ord getOrder(Integer orderId, Class<Ord> classType) throws DAOException;
	
	/**
	 * @see org.openmrs.api.OrderService#getOrders(java.lang.Class, java.util.List, java.util.List,
	 *      org.openmrs.api.OrderService.ORDER_STATUS, java.util.List, java.util.List,
	 *      java.util.List)
	 */
	public <Ord extends Order> List<Ord> getOrders(Class<Ord> orderClassType, List<Patient> patients,
	                                               List<Concept> concepts, ORDER_STATUS status, List<User> orderers,
	                                               List<Encounter> encounters, List<OrderType> orderTypes);
	
}

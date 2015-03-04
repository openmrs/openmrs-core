/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
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
 * Order-related database functions
 * <p>
 * This class should never be used directly. It should only be used through the
 * {@link org.openmrs.api.OrderService}
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
	        List<Concept> concepts, ORDER_STATUS status, List<User> orderers, List<Encounter> encounters,
	        List<OrderType> orderTypes);
	
	/**
	 * Auto generated method comment
	 * 
	 * @param uuid
	 * @return
	 */
	public Order getOrderByUuid(String uuid);
	
	/**
	 * Auto generated method comment
	 * 
	 * @param uuid
	 * @return
	 */
	public OrderType getOrderTypeByUuid(String uuid);
	
	/**
	 * Delete Obs that references an order
	 */
	public void deleteObsThatReference(Order order);
	
}

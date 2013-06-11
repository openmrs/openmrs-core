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

import org.openmrs.Concept;
import org.openmrs.DrugOrder;
import org.openmrs.Encounter;
import org.openmrs.Order;
import org.openmrs.Order.OrderAction;
import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.api.OrderService;

/**
 * Order-related database functions
 * <p>
 * This class should never be used directly. It should only be used through the
 * {@link org.openmrs.api.OrderService}
 * 
 * @see org.openmrs.api.OrderService
 */
public interface OrderDAO {
	
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
	 * @see org.openmrs.api.OrderService#getOrders(Class, List, List, List, List)
	 */
	public <Ord extends Order> List<Ord> getOrders(Class<Ord> orderClassType, List<Patient> patients,
	        List<Concept> concepts, List<User> orderers, List<Encounter> encounters, Date asOfDate,
	        List<OrderAction> actionsToInclude, List<OrderAction> actionsToExclude, boolean includeVoided);
	
	/**
	 * Auto generated method comment
	 * 
	 * @param uuid
	 * @return
	 */
	public Order getOrderByUuid(String uuid);
	
	/**
	 * @see org.openmrs.api.OrderService#getOrderByOrderNumber(java.lang.String)
	 */
	public Order getOrderByOrderNumber(String orderNumber);
	
	/**
	 * Determine the order number of this order as saved in the database (ignoring caches)
	 * 
	 * @param order
	 * @return
	 */
	public String getOrderNumberInDatabase(Order order);
	
	/**
	 * @return the highest orderId that has been persisted to the database
	 * @should return the highest order id
	 */
	public Integer getHighestOrderId();
	
	/**
	 * @see OrderService#getDrugOrdersByPatientAndIngredient(Patient, Concept)
	 */
	public List<DrugOrder> getDrugOrdersByPatientAndIngredient(Patient patient, Concept ingredient);
	
	/**
	 * @see org.openmrs.api.OrderService#getOrderHistoryByOrderNumber(java.lang.String)
	 */
	public List<Order> getOrderHistoryByOrderNumber(String orderNumber);
	
	/**
	 * Delete Obs that references an order
	 */
	public void deleteObsThatReference(Order order);
	
}

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

import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.openmrs.CareSetting;
import org.openmrs.Concept;
import org.openmrs.ConceptClass;
import org.openmrs.Encounter;
import org.openmrs.Order;
import org.openmrs.OrderFrequency;
import org.openmrs.OrderGroup;
import org.openmrs.OrderType;
import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.api.APIException;

/**
 * Order-related database functions
 * <p>
 * This class should never be used directly. It should only be used through the
 * {@link org.openmrs.api.OrderService}
 * 
 * @see org.openmrs.api.OrderService
 */
public interface OrderDAO {
	
	/**
	 * @see org.openmrs.api.OrderService#saveOrder(org.openmrs.Order, org.openmrs.api.OrderContext)
	 */
	Order saveOrder(Order order) throws DAOException;
	
	/**
	 * @see org.openmrs.api.OrderService#purgeOrder(Order)
	 */
	void deleteOrder(Order order) throws DAOException;
	
	/**
	 * @see org.openmrs.api.OrderService#getOrder(Integer)
	 */
	Order getOrder(Integer orderId) throws DAOException;
	
	/**
	 * This searches for orders given the parameters. Most arguments are optional (nullable). If
	 * multiple arguments are given, the returned orders will match on all arguments. The orders are
	 * sorted by startDate with the latest coming first
	 * 
	 * @param orderType The type of Order to get
	 * @param patients The patients to get orders for
	 * @param concepts The concepts in order.getConcept to get orders for
	 * @param orderers The orderers to match on
	 * @param encounters The encounters that the orders are assigned to
	 * @return list of Orders matching the parameters
	 */
	List<Order> getOrders(OrderType orderType, List<Patient> patients, List<Concept> concepts, List<User> orderers,
			List<Encounter> encounters);
	
	/**
	 * @see org.openmrs.api.OrderService#getOrders(org.openmrs.Patient, org.openmrs.CareSetting,
	 *      org.openmrs.OrderType, boolean)
	 */
	List<Order> getOrders(Patient patient, CareSetting careSetting, List<OrderType> orderTypes,
			boolean includeVoided, boolean includeDiscontinuationOrders);
	
	/**
	 * @param uuid
	 * @return order or null
	 */
	Order getOrderByUuid(String uuid);
	
	/**
	 * Delete Obs that references an order
	 */
	void deleteObsThatReference(Order order);
	
	/**
	 * @see org.openmrs.api.OrderService#getOrderByOrderNumber(java.lang.String)
	 */
	Order getOrderByOrderNumber(String orderNumber);
	
	/**
	 * Gets the next available order number seed
	 * 
	 * @return the order number seed
	 */
	Long getNextOrderNumberSeedSequenceValue();
	
	/**
	 * @see org.openmrs.api.OrderService#getActiveOrders(org.openmrs.Patient, org.openmrs.OrderType,
	 *      org.openmrs.CareSetting, java.util.Date)
	 */
	List<Order> getActiveOrders(Patient patient, List<OrderType> orderTypes, CareSetting careSetting, Date asOfDate);
	
	/**
	 * Get care setting by type
	 * 
	 * @param careSettingId
	 * @return the care setting type
	 */
	CareSetting getCareSetting(Integer careSettingId);
	
	/**
	 * @see org.openmrs.api.OrderService#getCareSettingByUuid(String)
	 */
	CareSetting getCareSettingByUuid(String uuid);
	
	/**
	 * @see org.openmrs.api.OrderService#getCareSettingByName(String)
	 */
	CareSetting getCareSettingByName(String name);
	
	/**
	 * @see org.openmrs.api.OrderService#getCareSettings(boolean)
	 */
	List<CareSetting> getCareSettings(boolean includeRetired);
	
	/**
	 * @see org.openmrs.api.OrderService#getOrderTypeByName(String)
	 */
	OrderType getOrderTypeByName(String orderTypeName);
	
	/**
	 * @see org.openmrs.api.OrderService#getOrderFrequency
	 */
	OrderFrequency getOrderFrequency(Integer orderFrequencyId);
	
	/**
	 * @see org.openmrs.api.OrderService#getOrderFrequencyByUuid
	 */
	OrderFrequency getOrderFrequencyByUuid(String uuid);
	
	/**
	 * @see org.openmrs.api.OrderService#getOrderFrequencies(boolean)
	 */
	List<OrderFrequency> getOrderFrequencies(boolean includeRetired);
	
	/**
	 * @see org.openmrs.api.OrderService#getOrderFrequencies(String, java.util.Locale, boolean, boolean)
	 */
	List<OrderFrequency> getOrderFrequencies(String searchPhrase, Locale locale, boolean exactLocale,
			boolean includeRetired);
	
	/**
	 * @see org.openmrs.api.OrderService#saveOrderFrequency(org.openmrs.OrderFrequency)
	 */
	OrderFrequency saveOrderFrequency(OrderFrequency orderFrequency);
	
	/**
	 * @see org.openmrs.api.OrderService#purgeOrderFrequency(org.openmrs.OrderFrequency)
	 */
	void purgeOrderFrequency(OrderFrequency orderFrequency);
	
	/**
	 * Checks if an order frequency is being referenced by any order
	 * 
	 * @param orderFrequency the order frequency
	 * @return true if in use, else false
	 */
	boolean isOrderFrequencyInUse(OrderFrequency orderFrequency);
	
	/**
	 * @see org.openmrs.api.OrderService#getOrderFrequencyByConcept
	 */
	OrderFrequency getOrderFrequencyByConcept(Concept concept);
	
	/**
	 * @see org.openmrs.api.OrderService#getOrderType
	 */
	OrderType getOrderType(Integer orderTypeId);
	
	/**
	 * @see org.openmrs.api.OrderService#getOrderTypeByUuid
	 */
	OrderType getOrderTypeByUuid(String uuid);
	
	/**
	 * @see org.openmrs.api.OrderService#getOrderTypes
	 */
	List<OrderType> getOrderTypes(boolean includeRetired);
	
	/**
	 * @see org.openmrs.api.OrderService#getOrderTypeByConceptClass(org.openmrs.ConceptClass)
	 */
	OrderType getOrderTypeByConceptClass(ConceptClass conceptClass);
	
	/**
	 * @see org.openmrs.api.OrderService#saveOrderType(org.openmrs.OrderType)
	 */
	OrderType saveOrderType(OrderType orderType);
	
	/**
	 * @see org.openmrs.api.OrderService#purgeOrderType(org.openmrs.OrderType)
	 */
	void purgeOrderType(OrderType orderType);
	
	/**
	 * @see org.openmrs.api.OrderService#getSubtypes(org.openmrs.OrderType, boolean)
	 */
	List<OrderType> getOrderSubtypes(OrderType orderType, boolean includeRetired);
	
	/**
	 * Check whether give order type is used by any order
	 * 
	 * @param orderType the order type to check the usage
	 * @return true if used else false
	 */
	boolean isOrderTypeInUse(OrderType orderType);
	
	/**
	 * @see org.openmrs.api.OrderService#getDiscontinuationOrder(Order)
	 */
	Order getDiscontinuationOrder(Order order);
	
	/**
	 * @see org.openmrs.api.OrderService#getRevisionOrder(org.openmrs.Order)
	 */
	Order getRevisionOrder(Order order) throws APIException;
	
	/**
	 * Get the fresh order from the database
	 *
	 * @param order the order to get from the database
	 * @param isOrderADrugOrder is the order a previous order
	 * @return a list of orders from the database
	 */
	List<Object[]> getOrderFromDatabase(Order order, boolean isOrderADrugOrder) throws APIException;

	/**
	 * Saves an orderGroup to the database
	 *
	 * @param orderGroup
	 * @return an orderGroup
	 * @throws DAOException
	 */
	OrderGroup saveOrderGroup(OrderGroup orderGroup) throws DAOException;
	
	/**
	 * @see org.openmrs.api.OrderService#getOrderGroupByUuid(String)
	 */
	OrderGroup getOrderGroupByUuid(String uuid) throws DAOException;
	
	/**
	 * @see org.openmrs.api.OrderService#getOrderGroup(Integer)
	 */
	OrderGroup getOrderGroupById(Integer orderGroupId) throws DAOException;
}

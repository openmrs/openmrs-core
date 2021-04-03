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

import org.openmrs.OrderSet;
import org.openmrs.OrderSetAttribute;
import org.openmrs.OrderSetAttributeType;
import org.openmrs.OrderSetMember;
import org.openmrs.api.OrderSetService;

/**
 * OrderSet-related database functions
 * <p>
 * This class should never be used directly. It should only be used through the
 * {@link org.openmrs.api.OrderSetService}
 *
 * @see org.openmrs.api.OrderSetService
 */
public interface OrderSetDAO {
	
	/**
	 * @see org.openmrs.api.OrderSetService#saveOrderSet(OrderSet)
	 */
	OrderSet save(OrderSet orderSet) throws DAOException;
	
	/**
	 * @see org.openmrs.api.OrderSetService#getOrderSets(boolean)
	 */
	List<OrderSet> getOrderSets(boolean includeRetired) throws DAOException;
	
	/**
	 * @see org.openmrs.api.OrderSetService#getOrderSet(Integer)
	 */
	OrderSet getOrderSetById(Integer orderSetId) throws DAOException;
	
	/**
	 * @see org.openmrs.api.OrderSetService#getOrderSetByUuid(String)
	 */
	OrderSet getOrderSetByUniqueUuid(String orderSetUuid) throws DAOException;
	

	/**
	 * @see org.openmrs.api.OrderSetService#getOrderSetMemberByUuid(String)
	 */
	OrderSetMember getOrderSetMemberByUuid(String uuid) throws DAOException;

	/**
	 * @see OrderSetService#getAllOrderSetAttributeTypes()
	 */
	public List<OrderSetAttributeType> getAllOrderSetAttributeTypes();

	/**
	 * @see OrderSetService#getOrderSetAttributeType(Integer)
	 */
	public OrderSetAttributeType getOrderSetAttributeType(Integer id);

	/**
	 * @see OrderSetService#getOrderSetAttributeTypeByUuid(String)
	 */
	public OrderSetAttributeType getOrderSetAttributeTypeByUuid(String uuid);

	/**
	 * @see OrderSetService#saveOrderSetAttributeType(OrderSetAttributeType)
	 */
	public OrderSetAttributeType saveOrderSetAttributeType(OrderSetAttributeType orderSetAttributeType);

	/**
	 * @see OrderSetService#purgeOrderSetAttributeType(OrderSetAttributeType)
	 */
	public void deleteOrderSetAttributeType(OrderSetAttributeType orderSetAttributeType);

	/**
	 * @see OrderSetService#getOrderSetAttributeByUuid(String)
	 */
	public OrderSetAttribute getOrderSetAttributeByUuid(String uuid);

	/**
	 * @see OrderSetService#getOrderSetAttributeTypeByName(String)
	 */
	public OrderSetAttributeType getOrderSetAttributeTypeByName(String name);

}

/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api;

import java.util.List;

import org.openmrs.OrderSet;
import org.openmrs.OrderSetMember;
import org.openmrs.annotation.Authorized;
import org.openmrs.api.db.OrderSetDAO;
import org.openmrs.util.PrivilegeConstants;

/**
 * Contains methods pertaining to creating/deleting/voiding Order Sets.
 * 
 * @since 1.12
 */
public interface OrderSetService extends OpenmrsService {
	
	/**
	 * Setter for the OrderSet data access object. The dao is used for saving and getting orders
	 * to/from the database
	 *
	 * @param dao The data access object to use
	 */
	void setOrderSetDAO(OrderSetDAO dao);
	
	/**
	 * Save or update the given <code>orderSet</code> in the database. If the OrderSet is retired
	 * it will set retired by and retired date.
	 * If OrderSetMembers are retired, it will set retired by and retired date.
	 *
	 * @param orderSet the OrderSet to save
	 * @return the OrderSet that was saved
	 * @throws APIException
	 */
	@Authorized( { PrivilegeConstants.MANAGE_ORDER_SETS })
	OrderSet saveOrderSet(OrderSet orderSet) throws APIException;
	
	/**
	 * Gets all OrderSets that match the specified parameters excluding discontinuation orderSets.
	 *
	 * @param includeRetired Specifies whether retired orders should be included or not
	 * @return list of OrderSets matching the parameters
	 * @throws APIException
	 */
	@Authorized(PrivilegeConstants.GET_ORDER_SETS)
	List<OrderSet> getOrderSets(boolean includeRetired) throws APIException;
	
	/**
	 * Gets a specific OrderSet with the matched orderSet Id.
	 *
	 * @param orderSetId Specifies a saved orderSet id.
	 * @return OrderSet
	 * @throws APIException
	 */
	@Authorized(PrivilegeConstants.GET_ORDER_SETS)
	OrderSet getOrderSet(Integer orderSetId) throws APIException;
	
	/**
	 * Gets a specific OrderSet with the matched orderSet uuid.
	 *
	 * @param orderSetUuid Specifies a saved orderSet uuid.
	 * @return an orderSet
	 * @throws APIException
	 */
	@Authorized(PrivilegeConstants.GET_ORDER_SETS)
	OrderSet getOrderSetByUuid(String orderSetUuid) throws APIException;
	
	/**
	 * Retires and OrderSet, with matched OrderSet
	 * 
	 * @param orderSet Specifies the OrderSet to be retired
	 * @param retireReason Specifies the reason why the OrderSet has to be retired
	 * @return an orderSet
	 * @throws APIException
	 */
	@Authorized( { PrivilegeConstants.MANAGE_ORDER_SETS })
	OrderSet retireOrderSet(OrderSet orderSet, String retireReason) throws APIException;
	
	/**
	 * UnRetires and OrderSet, with matched OrderSet
	 * 
	 * @param orderSet Specifies the OrderSet to be retired
	 * @return an orderSet
	 * @throws APIException
	 */
	@Authorized( { PrivilegeConstants.MANAGE_ORDER_SETS })
	OrderSet unretireOrderSet(OrderSet orderSet) throws APIException;
	

	/**
	 * Get OrderSetMember by uuid
	 *
	 * @param uuid
	 * @return
	 * @should find object given valid uuid
	 * @should return null if no object found with given uuid
	 */
	@Authorized(PrivilegeConstants.GET_ORDER_SETS)
	OrderSetMember getOrderSetMemberByUuid(String uuid);

}

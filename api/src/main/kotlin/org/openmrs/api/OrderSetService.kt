/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api

import org.openmrs.OrderSet
import org.openmrs.OrderSetAttribute
import org.openmrs.OrderSetAttributeType
import org.openmrs.OrderSetMember
import org.openmrs.annotation.Authorized
import org.openmrs.api.db.OrderSetDAO
import org.openmrs.util.PrivilegeConstants

/**
 * Contains methods pertaining to creating/deleting/voiding Order Sets.
 *
 * @since 1.12
 */
interface OrderSetService : OpenmrsService {

    /**
     * Setter for the OrderSet data access object. The dao is used for saving and getting orders
     * to/from the database.
     *
     * @param dao The data access object to use
     */
    fun setOrderSetDAO(dao: OrderSetDAO)

    /**
     * Save or update the given orderSet in the database. If the OrderSet is retired
     * it will set retired by and retired date.
     * If OrderSetMembers are retired, it will set retired by and retired date.
     *
     * @param orderSet the OrderSet to save
     * @return the OrderSet that was saved
     * @throws APIException if saving fails
     */
    @Authorized(PrivilegeConstants.MANAGE_ORDER_SETS)
    @Throws(APIException::class)
    fun saveOrderSet(orderSet: OrderSet): OrderSet

    /**
     * Gets all OrderSets that match the specified parameters excluding discontinuation orderSets.
     *
     * @param includeRetired Specifies whether retired orders should be included or not
     * @return list of OrderSets matching the parameters
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.GET_ORDER_SETS)
    @Throws(APIException::class)
    fun getOrderSets(includeRetired: Boolean): List<OrderSet>

    /**
     * Gets a specific OrderSet with the matched orderSet Id.
     *
     * @param orderSetId Specifies a saved orderSet id.
     * @return OrderSet
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.GET_ORDER_SETS)
    @Throws(APIException::class)
    fun getOrderSet(orderSetId: Int?): OrderSet?

    /**
     * Gets a specific OrderSet with the matched orderSet uuid.
     *
     * @param orderSetUuid Specifies a saved orderSet uuid.
     * @return an orderSet
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.GET_ORDER_SETS)
    @Throws(APIException::class)
    fun getOrderSetByUuid(orderSetUuid: String): OrderSet?

    /**
     * Retires an OrderSet.
     *
     * @param orderSet Specifies the OrderSet to be retired
     * @param retireReason Specifies the reason why the OrderSet has to be retired
     * @return an orderSet
     * @throws APIException if retirement fails
     */
    @Authorized(PrivilegeConstants.MANAGE_ORDER_SETS)
    @Throws(APIException::class)
    fun retireOrderSet(orderSet: OrderSet, retireReason: String): OrderSet

    /**
     * Unretires an OrderSet.
     *
     * @param orderSet Specifies the OrderSet to be unretired
     * @return an orderSet
     * @throws APIException if unretirement fails
     */
    @Authorized(PrivilegeConstants.MANAGE_ORDER_SETS)
    @Throws(APIException::class)
    fun unretireOrderSet(orderSet: OrderSet): OrderSet

    /**
     * Get OrderSetMember by uuid.
     *
     * @param uuid the OrderSetMember uuid
     * @return the OrderSetMember with the given uuid
     */
    @Authorized(PrivilegeConstants.GET_ORDER_SETS)
    fun getOrderSetMemberByUuid(uuid: String): OrderSetMember?

    /**
     * Get order set attribute by uuid.
     *
     * @param uuid specifies the order set attribute uuid
     * @return the OrderSetAttribute with the given uuid
     * @since 2.4.0
     */
    @Authorized(PrivilegeConstants.GET_ORDER_SETS)
    fun getOrderSetAttributeByUuid(uuid: String): OrderSetAttribute?

    /**
     * Get all order set attribute types.
     *
     * @return all OrderSetAttributeTypes
     * @since 2.4.0
     */
    @Authorized(PrivilegeConstants.GET_ORDER_SET_ATTRIBUTE_TYPES)
    fun getAllOrderSetAttributeTypes(): List<OrderSetAttributeType>

    /**
     * Get order set attribute type from the database by a given internal id.
     *
     * @param id specifies the set attribute type id
     * @return the OrderSetAttributeType with the given internal id
     * @since 2.4.0
     */
    @Authorized(PrivilegeConstants.GET_ORDER_SET_ATTRIBUTE_TYPES)
    fun getOrderSetAttributeType(id: Int?): OrderSetAttributeType?

    /**
     * Get order set attribute type by uuid.
     *
     * @param uuid specifies the order set attribute type uuid
     * @return the OrderSetAttributeType with the given uuid
     * @since 2.4.0
     */
    @Authorized(PrivilegeConstants.GET_ORDER_SET_ATTRIBUTE_TYPES)
    fun getOrderSetAttributeTypeByUuid(uuid: String): OrderSetAttributeType?

    /**
     * Creates or updates the given order set attribute type.
     *
     * @param orderSetAttributeType the order set attribute type to save
     * @return the OrderSetAttributeType created/saved
     * @since 2.4.0
     */
    @Authorized(PrivilegeConstants.MANAGE_ORDER_SET_ATTRIBUTE_TYPES)
    fun saveOrderSetAttributeType(orderSetAttributeType: OrderSetAttributeType): OrderSetAttributeType

    /**
     * Retires the given order set attribute type.
     *
     * @param orderSetAttributeType specifies the order set attribute type to be retired
     * @param reason the reason for retirement
     * @return the orderSetAttribute retired
     * @since 2.4.0
     */
    @Authorized(PrivilegeConstants.MANAGE_ORDER_SET_ATTRIBUTE_TYPES)
    fun retireOrderSetAttributeType(orderSetAttributeType: OrderSetAttributeType, reason: String): OrderSetAttributeType

    /**
     * Restores an order set attribute type that was previously retired.
     *
     * @param orderSetAttributeType the order set attribute type to be un-retired
     * @return the OrderSetAttributeType unretired
     * @since 2.4.0
     */
    @Authorized(PrivilegeConstants.MANAGE_ORDER_SET_ATTRIBUTE_TYPES)
    fun unretireOrderSetAttributeType(orderSetAttributeType: OrderSetAttributeType): OrderSetAttributeType

    /**
     * Completely removes an order set attribute type.
     *
     * @param orderSetAttributeType the order set attribute type to be purged
     * @since 2.4.0
     */
    @Authorized(PrivilegeConstants.PURGE_ORDER_SET_ATTRIBUTE_TYPES)
    fun purgeOrderSetAttributeType(orderSetAttributeType: OrderSetAttributeType)

    /**
     * Retrieves an order set attribute type object based on the name provided.
     *
     * @param orderSetAttributeTypeName fetches a given order set attribute type by name
     * @return the OrderSetAttributeType with the specified name
     * @since 2.4.0
     */
    @Authorized(PrivilegeConstants.GET_ORDER_SET_ATTRIBUTE_TYPES)
    fun getOrderSetAttributeTypeByName(orderSetAttributeTypeName: String): OrderSetAttributeType?
}

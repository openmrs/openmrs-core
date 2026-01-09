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

import org.openmrs.CareSetting
import org.openmrs.Concept
import org.openmrs.ConceptClass
import org.openmrs.Encounter
import org.openmrs.Order
import org.openmrs.OrderAttribute
import org.openmrs.OrderAttributeType
import org.openmrs.OrderFrequency
import org.openmrs.OrderGroup
import org.openmrs.OrderGroupAttribute
import org.openmrs.OrderGroupAttributeType
import org.openmrs.OrderType
import org.openmrs.Patient
import org.openmrs.Provider
import org.openmrs.Visit
import org.openmrs.annotation.Authorized
import org.openmrs.api.db.OrderDAO
import org.openmrs.parameter.OrderSearchCriteria
import org.openmrs.util.PrivilegeConstants
import java.util.Date
import java.util.Locale

/**
 * Contains methods pertaining to creating/deleting/voiding Orders
 */
interface OrderService : OpenmrsService {

    companion object {
        const val PARALLEL_ORDERS: String = "PARALLEL_ORDERS"
    }

    /**
     * Setter for the Order data access object. The dao is used for saving and getting orders
     * to/from the database
     *
     * @param dao The data access object to use
     */
    fun setOrderDAO(dao: OrderDAO)

    /**
     * Save or update the given order in the database. If the OrderType for the order
     * is not specified, then it will be set to the one set on the OrderContext if any,
     * if none exists on the orderContext, then it will be set to the one associated to the
     * ConceptClass of the ordered concept otherwise the save fails.
     *
     * @param order the Order to save
     * @param orderContext the OrderContext object
     * @return the Order that was saved
     * @throws APIException
     */
    @Authorized(PrivilegeConstants.EDIT_ORDERS, PrivilegeConstants.ADD_ORDERS)
    @Throws(APIException::class)
    fun saveOrder(order: Order, orderContext: OrderContext?): Order

    /**
     * Save or update the given retrospective order in the database.
     *
     * @param order the Order to save
     * @param orderContext the OrderContext object
     * @return the Order that was saved
     * @throws APIException
     */
    @Authorized(PrivilegeConstants.EDIT_ORDERS, PrivilegeConstants.ADD_ORDERS)
    @Throws(APIException::class)
    fun saveRetrospectiveOrder(order: Order, orderContext: OrderContext?): Order

    /**
     * Completely delete an order from the database. This should not typically be used unless
     * desperately needed. Most orders should just be voided.
     *
     * @param order The Order to remove from the system
     * @throws APIException
     */
    @Authorized(PrivilegeConstants.PURGE_ORDERS)
    @Throws(APIException::class)
    fun purgeOrder(order: Order)

    /**
     * Completely delete an order from the database. This should not typically be used unless
     * desperately needed. Most orders should just be voided.
     *
     * @param order The Order to remove from the system
     * @param cascade if true will delete any Obs that references the Order
     * @throws APIException
     * @since 1.9.4
     */
    @Authorized(PrivilegeConstants.PURGE_ORDERS)
    @Throws(APIException::class)
    fun purgeOrder(order: Order, cascade: Boolean)

    /**
     * Mark an order as voided. This functionally removes the Order from the system while keeping a
     * semblance
     *
     * @param order Order to void
     * @param voidReason String reason
     * @return the Order that was voided
     * @throws APIException
     */
    @Authorized(PrivilegeConstants.DELETE_ORDERS)
    @Throws(APIException::class)
    fun voidOrder(order: Order, voidReason: String): Order

    /**
     * Get order by internal primary key identifier
     *
     * @param orderId internal order identifier
     * @return order with given internal identifier
     * @throws APIException
     */
    @Authorized(PrivilegeConstants.GET_ORDERS)
    @Throws(APIException::class)
    fun getOrder(orderId: Int?): Order?

    /**
     * Get Order by its UUID
     *
     * @param uuid
     * @return order or null
     */
    @Authorized(PrivilegeConstants.GET_ORDERS)
    @Throws(APIException::class)
    fun getOrderByUuid(uuid: String): Order?

    /**
     * Get discontinuation order for the given order, it is the un voided discontinuation order with
     * a previousOrder that matches the specified order.
     *
     * @param order
     * @return the discontinuation order or null if none
     * @throws APIException
     * @since 1.10
     */
    @Authorized(PrivilegeConstants.GET_ORDERS)
    @Throws(APIException::class)
    fun getDiscontinuationOrder(order: Order): Order?

    /**
     * Get revision order for the given order, it is the order with the changes that was created as
     * a replacement for the specified order.
     *
     * @param order
     * @return the revision order or null if none
     * @throws APIException
     * @since 1.10
     */
    @Authorized(PrivilegeConstants.GET_ORDERS)
    @Throws(APIException::class)
    fun getRevisionOrder(order: Order): Order?

    /**
     * Gets all Orders that match the specified parameters excluding discontinuation orders
     *
     * @param patient the patient to match on
     * @param careSetting the CareSetting to match on
     * @param orderType The OrderType to match on
     * @param includeVoided Specifies whether voided orders should be included or not
     * @return list of Orders matching the parameters
     * @since 1.10
     */
    @Authorized(PrivilegeConstants.GET_ORDERS)
    fun getOrders(patient: Patient, careSetting: CareSetting, orderType: OrderType?, includeVoided: Boolean): List<Order>

    /**
     * Gets all orders for the specified patient including discontinuation orders
     *
     * @param patient the patient to match on
     * @return list of matching Orders
     * @since 1.10
     */
    @Authorized(PrivilegeConstants.GET_ORDERS)
    fun getAllOrdersByPatient(patient: Patient): List<Order>

    /**
     * Get all orders that match a variety of (nullable) criteria contained in the parameter object.
     *
     * @param orderSearchCriteria the object containing search parameters
     * @return a list of orders matching the search criteria
     * @since 2.2
     */
    @Authorized(PrivilegeConstants.GET_ORDERS)
    fun getOrders(orderSearchCriteria: OrderSearchCriteria): List<Order>

    /**
     * Unvoid order record. Reverse a previous call to voidOrder
     *
     * @param order order to be unvoided
     * @return the Order that was unvoided
     */
    @Authorized(PrivilegeConstants.DELETE_ORDERS)
    @Throws(APIException::class)
    fun unvoidOrder(order: Order): Order

    /**
     * Updates the fulfillerStatus of an order and the related comment and finally persists it
     *
     * @param order order whose fulfillerStatus should be changed
     * @param orderFulfillerStatus describes the new Order.FulfillerStatus the order should be set to
     * @param fullFillerComment is a string which describes a comment that is set while changing the FulfillerStatus
     * @return the Order that is updated with an according fulfillerStatus and fulFillerComment
     */
    @Authorized(PrivilegeConstants.EDIT_ORDERS)
    fun updateOrderFulfillerStatus(
        order: Order,
        orderFulfillerStatus: Order.FulfillerStatus?,
        fullFillerComment: String?
    ): Order

    /**
     * Updates the fulfillerStatus of an order and the related comment and finally persists it
     *
     * @param order order whose fulfillerStatus should be changed
     * @param orderFulfillerStatus describes the new Order.FulfillerStatus the order should be set to
     * @param fullFillerComment is a string which describes a comment that is set while changing the FulfillerStatus
     * @param accessionNumber is the accession number to set
     * @return the Order that is updated with an according fulfillerStatus and fulFillerComment and accession number
     */
    @Authorized(PrivilegeConstants.EDIT_ORDERS)
    fun updateOrderFulfillerStatus(
        order: Order,
        orderFulfillerStatus: Order.FulfillerStatus?,
        fullFillerComment: String?,
        accessionNumber: String?
    ): Order

    /**
     * Gets the order identified by a given order number
     *
     * @param orderNumber the order number
     * @return the order object
     */
    @Authorized(PrivilegeConstants.GET_ORDERS)
    fun getOrderByOrderNumber(orderNumber: String): Order?

    /**
     * Gets all Order objects that use this Concept for a given patient. Orders will be returned in
     * the order in which they occurred, i.e. sorted by startDate starting with the latest
     *
     * @param patient the patient.
     * @param concept the concept.
     * @return the list of orders.
     */
    @Authorized(PrivilegeConstants.GET_ORDERS)
    fun getOrderHistoryByConcept(patient: Patient, concept: Concept): List<Order>

    /**
     * Gets the next available order number seed
     *
     * @return the order number seed
     */
    @Authorized(PrivilegeConstants.ADD_ORDERS)
    fun getNextOrderNumberSeedSequenceValue(): java.lang.Long

    /**
     * Gets the order matching the specified order number and its previous orders in the ordering
     * they occurred
     *
     * @param orderNumber the order number whose history to get
     * @return a list of orders for given order number
     */
    @Authorized(PrivilegeConstants.GET_ORDERS)
    fun getOrderHistoryByOrderNumber(orderNumber: String): List<Order>

    /**
     * Gets all active orders for the specified patient matching the specified CareSetting,
     * OrderType as of the specified date.
     *
     * @param patient the patient
     * @param orderType The OrderType to match
     * @param careSetting the care setting, returns all ignoring care setting if value is null
     * @param asOfDate defaults to current time
     * @return all active orders for given patient parameters
     * @since 1.10
     */
    @Authorized(PrivilegeConstants.GET_ORDERS)
    fun getActiveOrders(patient: Patient, orderType: OrderType?, careSetting: CareSetting?, asOfDate: Date?): List<Order>

    /**
     * Retrieve care setting
     *
     * @param careSettingId
     * @return the care setting
     * @since 1.10
     */
    @Authorized(PrivilegeConstants.GET_CARE_SETTINGS)
    fun getCareSetting(careSettingId: Int?): CareSetting?

    /**
     * Gets the CareSetting with the specified uuid
     *
     * @param uuid the uuid to match on
     * @return CareSetting
     */
    @Authorized(PrivilegeConstants.GET_CARE_SETTINGS)
    fun getCareSettingByUuid(uuid: String): CareSetting?

    /**
     * Gets the CareSetting with the specified name
     *
     * @param name the name to match on
     * @return CareSetting
     */
    @Authorized(PrivilegeConstants.GET_CARE_SETTINGS)
    fun getCareSettingByName(name: String): CareSetting?

    /**
     * Gets all non retired CareSettings if includeRetired is set to true otherwise retired ones are
     * included too
     *
     * @param includeRetired specifies whether retired care settings should be returned or not
     * @return A List of CareSettings
     */
    @Authorized(PrivilegeConstants.GET_CARE_SETTINGS)
    fun getCareSettings(includeRetired: Boolean): List<CareSetting>

    /**
     * Gets OrderType that matches the specified name
     *
     * @param orderTypeName the name to match against
     * @return OrderType
     * @since 1.10
     */
    @Authorized(PrivilegeConstants.GET_ORDER_TYPES)
    fun getOrderTypeByName(orderTypeName: String): OrderType?

    /**
     * Gets OrderFrequency that matches the specified orderFrequencyId
     *
     * @param orderFrequencyId the id to match against
     * @return OrderFrequency
     * @since 1.10
     */
    @Authorized(PrivilegeConstants.GET_ORDER_FREQUENCIES)
    fun getOrderFrequency(orderFrequencyId: Int?): OrderFrequency?

    /**
     * Gets OrderFrequency that matches the specified uuid
     *
     * @param uuid the uuid to match against
     * @return OrderFrequency
     * @since 1.10
     */
    @Authorized(PrivilegeConstants.GET_ORDER_FREQUENCIES)
    fun getOrderFrequencyByUuid(uuid: String): OrderFrequency?

    /**
     * Gets an OrderFrequency that matches the specified concept
     *
     * @param concept the concept to match against
     * @return OrderFrequency
     * @since 1.10
     */
    @Authorized(PrivilegeConstants.GET_ORDER_FREQUENCIES)
    fun getOrderFrequencyByConcept(concept: Concept): OrderFrequency?

    /**
     * Gets all order frequencies
     *
     * @param includeRetired specifies whether retired ones should be included or not
     * @return List of OrderFrequency
     * @since 1.10
     */
    @Authorized(PrivilegeConstants.GET_ORDER_FREQUENCIES)
    fun getOrderFrequencies(includeRetired: Boolean): List<OrderFrequency>

    /**
     * Gets all non retired order frequencies associated to concepts that match the specified search phrase
     *
     * @param searchPhrase The string to match on
     * @param locale The locale to match on when searching in associated concept names
     * @param exactLocale If false then order frequencies associated to concepts with names in a broader locale will be matched
     * @param includeRetired Specifies if retired order frequencies that match should be included or not
     * @return List of OrderFrequency
     * @since 1.10
     */
    @Authorized(PrivilegeConstants.GET_ORDER_FREQUENCIES)
    fun getOrderFrequencies(searchPhrase: String, locale: Locale, exactLocale: Boolean, includeRetired: Boolean): List<OrderFrequency>

    /**
     * Discontinues an order. Creates a new order that discontinues the orderToDiscontinue
     *
     * @param orderToDiscontinue
     * @param reasonCoded
     * @param discontinueDate
     * @param orderer
     * @param encounter
     * @return the new order that discontinued orderToDiscontinue
     * @throws APIException
     * @since 1.10
     */
    @Authorized(PrivilegeConstants.ADD_ORDERS, PrivilegeConstants.EDIT_ORDERS)
    @Throws(APIException::class)
    fun discontinueOrder(
        orderToDiscontinue: Order,
        reasonCoded: Concept,
        discontinueDate: Date?,
        orderer: Provider,
        encounter: Encounter
    ): Order

    /**
     * Discontinues an order. Creates a new order that discontinues the orderToDiscontinue.
     *
     * @param orderToDiscontinue
     * @param reasonNonCoded
     * @param discontinueDate
     * @param orderer
     * @param encounter
     * @return the new order that discontinued orderToDiscontinue
     * @throws APIException
     * @since 1.10
     */
    @Authorized(PrivilegeConstants.ADD_ORDERS, PrivilegeConstants.EDIT_ORDERS)
    @Throws(APIException::class)
    fun discontinueOrder(
        orderToDiscontinue: Order,
        reasonNonCoded: String,
        discontinueDate: Date?,
        orderer: Provider,
        encounter: Encounter
    ): Order

    /**
     * Creates or updates the given order frequency in the database
     *
     * @param orderFrequency the order frequency to save
     * @return the order frequency created/saved
     * @throws APIException
     * @since 1.10
     */
    @Authorized(PrivilegeConstants.MANAGE_ORDER_FREQUENCIES)
    @Throws(APIException::class)
    fun saveOrderFrequency(orderFrequency: OrderFrequency): OrderFrequency

    /**
     * Retires the given order frequency in the database
     *
     * @param orderFrequency the order frequency to retire
     * @param reason the retire reason
     * @return the retired order frequency
     * @since 1.10
     */
    @Authorized(PrivilegeConstants.MANAGE_ORDER_FREQUENCIES)
    fun retireOrderFrequency(orderFrequency: OrderFrequency, reason: String): OrderFrequency

    /**
     * Restores an order frequency that was previously retired in the database
     *
     * @param orderFrequency the order frequency to unretire
     * @return the unretired order frequency
     * @since 1.10
     */
    @Authorized(PrivilegeConstants.MANAGE_ORDER_FREQUENCIES)
    fun unretireOrderFrequency(orderFrequency: OrderFrequency): OrderFrequency

    /**
     * Completely removes an order frequency from the database
     *
     * @param orderFrequency the order frequency to purge
     * @throws APIException
     * @since 1.10
     */
    @Authorized(PrivilegeConstants.PURGE_ORDER_FREQUENCIES)
    @Throws(APIException::class)
    fun purgeOrderFrequency(orderFrequency: OrderFrequency)

    /**
     * Get OrderType by orderTypeId
     *
     * @param orderTypeId the orderTypeId to match on
     * @return order type object associated with given id
     * @since 1.10
     */
    @Authorized(PrivilegeConstants.GET_ORDER_TYPES)
    fun getOrderType(orderTypeId: Int?): OrderType?

    /**
     * Get OrderType by uuid
     *
     * @param uuid the uuid to match on
     * @return order type object associated with given uuid
     * @since 1.10
     */
    @Authorized(PrivilegeConstants.GET_ORDER_TYPES)
    fun getOrderTypeByUuid(uuid: String): OrderType?

    /**
     * Get all order types, if includeRetired is set to true then retired ones will be included otherwise not
     *
     * @param includeRetired boolean flag which indicate search needs to look at retired order types or not
     * @return list of order types
     * @since 1.10
     */
    @Authorized(PrivilegeConstants.GET_ORDER_TYPES)
    fun getOrderTypes(includeRetired: Boolean): List<OrderType>

    /**
     * Creates or updates the given order type in the database
     *
     * @param orderType the order type to save
     * @return the order type created/saved
     * @since 1.10
     */
    @Authorized(PrivilegeConstants.MANAGE_ORDER_TYPES)
    fun saveOrderType(orderType: OrderType): OrderType

    /**
     * Completely removes an order type from the database
     *
     * @param orderType the order type to purge
     * @throws APIException
     * @since 1.10
     */
    @Authorized(PrivilegeConstants.PURGE_ORDER_TYPES)
    @Throws(APIException::class)
    fun purgeOrderType(orderType: OrderType)

    /**
     * Retires the given order type in the database
     *
     * @param orderType the order type to retire
     * @param reason the retire reason
     * @return the retired order type
     * @since 1.10
     */
    @Authorized(PrivilegeConstants.MANAGE_ORDER_TYPES)
    fun retireOrderType(orderType: OrderType, reason: String): OrderType

    /**
     * Restores an order type that was previously retired in the database
     *
     * @param orderType the order type to unretire
     * @return the unretired order type
     * @since 1.10
     */
    @Authorized(PrivilegeConstants.MANAGE_ORDER_TYPES)
    fun unretireOrderType(orderType: OrderType): OrderType

    /**
     * Returns all descendants of a given order type
     *
     * @param orderType the order type which needs to search for its' dependencies
     * @param includeRetired boolean flag for include retired order types or not
     * @return list of order type which matches the given order type
     */
    @Authorized(PrivilegeConstants.GET_ORDER_TYPES)
    fun getSubtypes(orderType: OrderType, includeRetired: Boolean): List<OrderType>

    /**
     * Gets the order type mapped to a given concept class
     *
     * @param conceptClass the concept class
     * @return the matching order type
     * @since 1.10
     */
    @Authorized(PrivilegeConstants.GET_ORDER_TYPES)
    fun getOrderTypeByConceptClass(conceptClass: ConceptClass): OrderType?

    /**
     * Gets the order type mapped to a given concept
     *
     * @param concept the concept
     * @return the matching order type
     * @since 1.10
     */
    @Authorized(PrivilegeConstants.GET_ORDER_TYPES)
    fun getOrderTypeByConcept(concept: Concept): OrderType?

    /**
     * Get order types by java class name
     *
     * @param javaClassName the class name used to get the order types
     * @param includeRetired boolean flag for include retired or not
     * @return return the order types associated with given class name
     * @throws APIException
     * @since 3.0.0
     */
    @Authorized(PrivilegeConstants.GET_ORDER_TYPES)
    @Throws(APIException::class)
    fun getOrderTypesByClassName(javaClassName: String, includeRetired: Boolean): List<OrderType>

    /**
     * Get order types by java class name
     *
     * @param javaClassName the class name used to get the order types
     * @param includeSubclasses boolean flag for include subclasses or not
     * @param includeRetired boolean flag for include retired or not
     * @return return the order types associated with given class name
     * @throws APIException
     * @since 3.0.0
     */
    @Authorized(PrivilegeConstants.GET_ORDER_TYPES)
    @Throws(APIException::class)
    fun getOrderTypesByClassName(javaClassName: String, includeSubclasses: Boolean, includeRetired: Boolean): List<OrderType>

    /**
     * Gets the possible drug routes
     *
     * @return concept list of drug routes
     * @since 1.10
     */
    @Authorized(PrivilegeConstants.GET_CONCEPTS)
    fun getDrugRoutes(): List<Concept>

    /**
     * Gets the possible drug dosing units
     *
     * @return concept list of drug dosing units
     * @since 1.10
     */
    @Authorized(PrivilegeConstants.GET_CONCEPTS)
    fun getDrugDosingUnits(): List<Concept>

    /**
     * Gets the possible units of dispensing
     *
     * @return concept list of units of dispensing
     * @since 1.10
     */
    @Authorized(PrivilegeConstants.GET_CONCEPTS)
    fun getDrugDispensingUnits(): List<Concept>

    /**
     * Gets the possible units of duration
     *
     * @return concept list of units of duration
     * @since 1.10
     */
    @Authorized(PrivilegeConstants.GET_CONCEPTS)
    fun getDurationUnits(): List<Concept>

    /**
     * Gets the possible test specimen sources
     *
     * @return concept list of specimen sources
     * @since 1.10
     */
    @Authorized(PrivilegeConstants.GET_CONCEPTS)
    fun getTestSpecimenSources(): List<Concept>

    /**
     * Gets the non coded drug concept
     *
     * @return concept of non coded drug
     * @since 1.12
     */
    @Authorized(PrivilegeConstants.GET_CONCEPTS)
    fun getNonCodedDrugConcept(): Concept?

    /**
     * Fetches the OrderGroup By Uuid.
     *
     * @param uuid Uuid Of the OrderGroup
     * @return saved OrderGroup
     * @throws APIException
     * @since 1.12
     */
    @Authorized(PrivilegeConstants.GET_ORDERS)
    @Throws(APIException::class)
    fun getOrderGroupByUuid(uuid: String): OrderGroup?

    /**
     * Fetches the OrderGroup by Id.
     *
     * @param orderGroupId Id of the OrderGroup
     * @return saved OrderGroup
     * @throws APIException
     * @since 1.12
     */
    @Authorized(PrivilegeConstants.GET_ORDERS)
    @Throws(APIException::class)
    fun getOrderGroup(orderGroupId: Int?): OrderGroup?

    /**
     * Saves the orderGroup. It also saves the list of orders that are present within the orderGroup.
     *
     * @param orderGroup the orderGroup to be saved
     * @throws APIException
     * @since 1.12
     */
    @Authorized(PrivilegeConstants.EDIT_ORDERS, PrivilegeConstants.ADD_ORDERS)
    @Throws(APIException::class)
    fun saveOrderGroup(orderGroup: OrderGroup): OrderGroup

    /**
     * Saves an order group with a specific order context
     *
     * @param orderGroup the order group to be saved
     * @param orderContext the order context data transfer object containing care setting and the order type
     * @return the order group that was saved with the specified order context data
     * @throws APIException
     * @since 2.7.0
     */
    @Authorized(PrivilegeConstants.EDIT_ORDERS, PrivilegeConstants.ADD_ORDERS)
    @Throws(APIException::class)
    fun saveOrderGroup(orderGroup: OrderGroup, orderContext: OrderContext?): OrderGroup

    /**
     * Fetches all order groups for the specified patient
     *
     * @param patient the patient to match on
     * @return list of matching OrderGroups
     * @throws APIException
     * @since 2.4.0
     */
    @Authorized(PrivilegeConstants.GET_ORDERS)
    @Throws(APIException::class)
    fun getOrderGroupsByPatient(patient: Patient): List<OrderGroup>

    /**
     * Fetches all order groups for the specified encounter
     *
     * @param encounter the encounter to match on
     * @return list of matching OrderGroups
     * @throws APIException
     * @since 2.4.0
     */
    @Authorized(PrivilegeConstants.GET_ORDERS)
    @Throws(APIException::class)
    fun getOrderGroupsByEncounter(encounter: Encounter): List<OrderGroup>

    /**
     * Returns all order group attribute types
     *
     * @return all OrderGroupAttributeTypes
     */
    @Authorized(PrivilegeConstants.GET_ORDERS)
    @Throws(APIException::class)
    fun getAllOrderGroupAttributeTypes(): List<OrderGroupAttributeType>

    /**
     * Fetches order group attribute type using provided Id
     *
     * @param orderGroupAttributeTypeId The Id of the order group attribute type to fetch from the database
     * @return the OrderGroupAttributeType with the given internal id
     */
    @Authorized(PrivilegeConstants.GET_ORDERS)
    @Throws(APIException::class)
    fun getOrderGroupAttributeType(orderGroupAttributeTypeId: Int?): OrderGroupAttributeType?

    /**
     * Fetches order group attribute type using provided uuid
     *
     * @param uuid The uuid of the order group attribute type to fetch from the database
     * @return the OrderGroupAttributeType with the given uuid
     */
    @Throws(APIException::class)
    fun getOrderGroupAttributeTypeByUuid(uuid: String): OrderGroupAttributeType?

    /**
     * Creates or updates the given order group attribute type in the database
     *
     * @param orderGroupAttributeType The order group attribute type to save in the database
     * @return the order group attribute type created or saved
     */
    @Authorized(PrivilegeConstants.EDIT_ORDERS, PrivilegeConstants.ADD_ORDERS)
    @Throws(APIException::class)
    fun saveOrderGroupAttributeType(orderGroupAttributeType: OrderGroupAttributeType): OrderGroupAttributeType

    /**
     * Retires the given order group attribute type in the database
     *
     * @param orderGroupAttributeType The order group attribute type to retire
     * @param reason The reason why the order group attribute type is being retired
     * @return the order group attribute type retired
     */
    @Authorized(PrivilegeConstants.MANAGE_ORDER_TYPES)
    @Throws(APIException::class)
    fun retireOrderGroupAttributeType(orderGroupAttributeType: OrderGroupAttributeType, reason: String): OrderGroupAttributeType

    /**
     * Restores an order group attribute type that was previously retired in the database
     *
     * @param orderGroupAttributeType The order group attribute type to unretire
     * @return the order group attribute type unretired
     */
    @Authorized(PrivilegeConstants.MANAGE_ORDER_TYPES)
    @Throws(APIException::class)
    fun unretireOrderGroupAttributeType(orderGroupAttributeType: OrderGroupAttributeType): OrderGroupAttributeType

    /**
     * Completely removes an order group attribute type from the database
     *
     * @param orderGroupAttributeType The order group attribute type to purge
     */
    @Authorized(PrivilegeConstants.PURGE_ORDERS)
    @Throws(APIException::class)
    fun purgeOrderGroupAttributeType(orderGroupAttributeType: OrderGroupAttributeType)

    /**
     * Retrieves an order group attribute type object based on the name provided
     *
     * @param orderGroupAttributeTypeName The name of the order group attribute type to fetch
     * @return the OrderGroupAttributeType with the specified name
     */
    @Authorized(PrivilegeConstants.GET_ORDERS)
    @Throws(APIException::class)
    fun getOrderGroupAttributeTypeByName(orderGroupAttributeTypeName: String): OrderGroupAttributeType?

    /**
     * Fetches a given order group attribute using the provided uuid
     *
     * @param uuid The uuid of the order group attribute to fetch
     * @return the OrderGroupAttribute with the given uuid
     * @since 2.4.0
     */
    @Authorized(PrivilegeConstants.GET_ORDERS)
    @Throws(APIException::class)
    fun getOrderGroupAttributeByUuid(uuid: String): OrderGroupAttribute?

    /**
     * Returns all order attribute types
     *
     * @return all OrderAttributeTypes
     * @since 2.5.0
     */
    @Authorized(PrivilegeConstants.GET_ORDERS)
    @Throws(APIException::class)
    fun getAllOrderAttributeTypes(): List<OrderAttributeType>

    /**
     * Fetches order attribute type using provided Id
     *
     * @param orderAttributeTypeId The Id of the order attribute type to fetch from the database
     * @return the OrderAttributeType with the given internal id
     * @since 2.5.0
     */
    @Authorized(PrivilegeConstants.GET_ORDERS)
    @Throws(APIException::class)
    fun getOrderAttributeTypeById(orderAttributeTypeId: Int?): OrderAttributeType?

    /**
     * Fetches order attribute type using provided uuid
     *
     * @param uuid The uuid of the order attribute type to fetch from the database
     * @return the OrderAttributeType with the given uuid
     * @since 2.5.0
     */
    @Throws(APIException::class)
    fun getOrderAttributeTypeByUuid(uuid: String): OrderAttributeType?

    /**
     * Creates or updates the given order attribute type in the database
     *
     * @param orderAttributeType The order attribute type to save in the database
     * @return the order attribute type created or saved
     * @since 2.5.0
     */
    @Authorized(PrivilegeConstants.EDIT_ORDERS, PrivilegeConstants.ADD_ORDERS)
    @Throws(APIException::class)
    fun saveOrderAttributeType(orderAttributeType: OrderAttributeType): OrderAttributeType

    /**
     * Retires the given order attribute type in the database
     *
     * @param orderAttributeType The order attribute type to retire
     * @param reason The reason why the order attribute type is being retired
     * @return the order attribute type retired
     * @since 2.5.0
     */
    @Authorized(PrivilegeConstants.MANAGE_ORDER_TYPES)
    @Throws(APIException::class)
    fun retireOrderAttributeType(orderAttributeType: OrderAttributeType, reason: String): OrderAttributeType

    /**
     * Restores an order attribute type that was previously retired in the database
     *
     * @param orderAttributeType The order attribute type to unretire
     * @return the order attribute type unretired
     * @since 2.5.0
     */
    @Authorized(PrivilegeConstants.MANAGE_ORDER_TYPES)
    @Throws(APIException::class)
    fun unretireOrderAttributeType(orderAttributeType: OrderAttributeType): OrderAttributeType

    /**
     * Completely removes an order attribute type from the database
     *
     * @param orderAttributeType The order attribute type to purge
     * @since 2.5.0
     */
    @Authorized(PrivilegeConstants.PURGE_ORDERS)
    @Throws(APIException::class)
    fun purgeOrderAttributeType(orderAttributeType: OrderAttributeType)

    /**
     * Retrieves an order attribute type object based on the name provided
     *
     * @param orderAttributeTypeName The name of the order attribute type to fetch
     * @return the OrderAttributeType with the specified name
     * @since 2.5.0
     */
    @Authorized(PrivilegeConstants.GET_ORDERS)
    @Throws(APIException::class)
    fun getOrderAttributeTypeByName(orderAttributeTypeName: String): OrderAttributeType?

    /**
     * Fetches a given order attribute using the provided uuid
     *
     * @param uuid The uuid of the order attribute to fetch
     * @return the OrderAttribute with the given uuid
     * @since 2.5.0
     */
    @Authorized(PrivilegeConstants.GET_ORDERS)
    @Throws(APIException::class)
    fun getOrderAttributeByUuid(uuid: String): OrderAttribute?

    /**
     * Gets all active orders for the specified patient matching the specified CareSetting,
     * OrderType as of the specified date, with an optional visit restriction.
     *
     * @param patient the patient
     * @param visit the Visit to restrict active orders (optional)
     * @param orderType The OrderType to match
     * @param careSetting the care setting, returns all ignoring care setting if value is null
     * @param asOfDate defaults to current time
     * @return all active orders for given patient parameters
     * @since 2.7.0
     */
    @Authorized(PrivilegeConstants.GET_ORDERS)
    fun getActiveOrders(
        patient: Patient,
        visit: Visit?,
        orderType: OrderType?,
        careSetting: CareSetting?,
        asOfDate: Date?
    ): List<Order>

    /**
     * Gets all Orders that match the specified parameters excluding discontinuation orders,
     * with an optional visit restriction.
     *
     * @param patient the patient to match on
     * @param visit the Visit to restrict orders (optional)
     * @param careSetting the CareSetting to match on
     * @param orderType The OrderType to match on
     * @param includeVoided Specifies whether voided orders should be included or not
     * @return list of Orders matching the parameters
     * @since 2.7.0
     */
    @Authorized(PrivilegeConstants.GET_ORDERS)
    fun getOrders(
        patient: Patient,
        visit: Visit?,
        careSetting: CareSetting,
        orderType: OrderType?,
        includeVoided: Boolean
    ): List<Order>
}

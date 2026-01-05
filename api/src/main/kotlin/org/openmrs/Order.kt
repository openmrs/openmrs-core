/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs

import org.hibernate.envers.Audited
import org.openmrs.api.APIException
import org.openmrs.api.db.hibernate.HibernateUtil
import org.openmrs.order.OrderUtil
import org.openmrs.util.ConfigUtil
import org.openmrs.util.OpenmrsConstants
import org.openmrs.util.OpenmrsUtil
import java.util.Date

/**
 * Encapsulates information about the clinical action of a provider requesting something for a
 * patient e.g requesting a test to be performed, prescribing a medication, requesting the patient
 * to enroll on a specific diet etc.
 *
 * @version 1.0
 */
@Audited
open class Order() : BaseCustomizableData<OrderAttribute>(), FormRecordable {

    companion object {
        const val serialVersionUID: Long = 4334343L
    }

    /** @since 1.9.2, 1.10 */
    enum class Urgency {
        ROUTINE, STAT, ON_SCHEDULED_DATE
    }

    /** @since 1.10 */
    enum class Action {
        NEW, REVISE, DISCONTINUE, RENEW
    }

    /**
     * Valid values for the status of an order received from a filler.
     * @since 2.2.0
     * @since 2.6.1 added ON_HOLD & DECLINED
     */
    enum class FulfillerStatus {
        RECEIVED, IN_PROGRESS, EXCEPTION, ON_HOLD, DECLINED, COMPLETED
    }

    var orderId: Int? = null

    var patient: Patient? = null

    var orderType: OrderType? = null

    var concept: Concept? = null

    var instructions: String? = null

    var dateActivated: Date? = null

    var autoExpireDate: Date? = null

    var encounter: Encounter? = null

    var orderer: Provider? = null

    var dateStopped: Date? = null

    var orderReason: Concept? = null

    var accessionNumber: String? = null

    var orderReasonNonCoded: String? = null

    var urgency: Urgency = Urgency.ROUTINE

    private var _orderNumber: String? = null
    var orderNumber: String?
        get() = _orderNumber
        set(value) {
            if (_orderNumber != null && value != _orderNumber) {
                throw APIException("Unable to modify order number")
            }
            if (!ConfigUtil.getProperty(OpenmrsConstants.GP_ALLOW_SETTING_ORDER_NUMBER, false)
                && _orderNumber == null && value != null) {
                throw APIException("Unable to set order number because GP_ALLOW_SETTING_ORDER_NUMBER is set to false")
            }
            _orderNumber = value
        }

    var commentToFulfiller: String? = null

    var careSetting: CareSetting? = null

    /** @since 1.10 */
    var scheduledDate: Date? = null

    var formNamespaceAndPath: String? = null

    /**
     * Allows the orders if ordered as an orderGroup, to maintain a sequence of how members are
     * added in the group.
     */
    var sortWeight: Double? = null

    /**
     * Allows orders to be linked to a previous order - e.g., an order discontinue ampicillin linked
     * to the original ampicillin order (the D/C gets its own order number)
     */
    private var _previousOrder: Order? = null
    var previousOrder: Order?
        get() = HibernateUtil.getRealObjectFromProxy(_previousOrder)
        set(value) { _previousOrder = value }

    /**
     * Represents the action being taken on an order.
     * @see Action
     */
    var action: Action = Action.NEW

    /** @see OrderGroup */
    var orderGroup: OrderGroup? = null

    /**
     * Represents the status of an order received from a fulfiller.
     * @see FulfillerStatus
     * @since 2.2.0
     */
    var fulfillerStatus: FulfillerStatus? = null

    /**
     * Represents the comment that goes along with fulfiller status.
     * @since 2.2.0
     */
    var fulfillerComment: String? = null

    /** Constructor with id */
    constructor(orderId: Int?) : this() {
        this.orderId = orderId
    }

    /**
     * Performs a shallow copy of this Order. Does NOT copy orderId.
     */
    open fun copy(): Order = copyHelper(Order())

    /**
     * The purpose of this method is to allow subclasses of Order to delegate a portion of their
     * copy() method back to the superclass.
     */
    protected open fun copyHelper(target: Order): Order = target.apply {
        patient = this@Order.patient
        orderType = this@Order.orderType
        concept = this@Order.concept
        instructions = this@Order.instructions
        dateActivated = this@Order.dateActivated
        autoExpireDate = this@Order.autoExpireDate
        encounter = this@Order.encounter
        orderer = this@Order.orderer
        creator = this@Order.creator
        dateCreated = this@Order.dateCreated
        dateStopped = this@Order.dateStopped
        orderReason = this@Order.orderReason
        orderReasonNonCoded = this@Order.orderReasonNonCoded
        accessionNumber = this@Order.accessionNumber
        voided = this@Order.voided
        voidedBy = this@Order.voidedBy
        dateVoided = this@Order.dateVoided
        voidReason = this@Order.voidReason
        urgency = this@Order.urgency
        commentToFulfiller = this@Order.commentToFulfiller
        _previousOrder = this@Order._previousOrder
        action = this@Order.action
        _orderNumber = this@Order._orderNumber
        careSetting = this@Order.careSetting
        changedBy = this@Order.changedBy
        dateChanged = this@Order.dateChanged
        scheduledDate = this@Order.scheduledDate
        orderGroup = this@Order.orderGroup
        sortWeight = this@Order.sortWeight
        this@Order.fulfillerComment = this@Order.fulfillerComment
        this@Order.fulfillerStatus = this@Order.fulfillerStatus
        formNamespaceAndPath = this@Order.formNamespaceAndPath
    }

    // Status check methods

    /**
     * Convenience method to determine if the order is activated as of the current date.
     * @since 2.0
     */
    fun isActivated(): Boolean = isActivated(Date())

    /**
     * Convenience method to determine if the order is activated as of the specified date.
     * @since 2.0
     */
    fun isActivated(checkDate: Date?): Boolean {
        val date = checkDate ?: Date()
        return dateActivated != null && OpenmrsUtil.compare(dateActivated, date) <= 0
    }

    /**
     * Convenience method to determine if the order was active as of the current date.
     * @since 1.10.1
     */
    fun isActive(): Boolean = isActive(Date())

    /**
     * Convenience method to determine if the order is active as of the specified date.
     * @since 1.10.1
     */
    fun isActive(aCheckDate: Date?): Boolean {
        if (voided == true || action == Action.DISCONTINUE) {
            return false
        }
        val checkDate = aCheckDate ?: Date()
        return isActivated(checkDate) && !isDiscontinued(checkDate) && !isExpired(checkDate)
    }

    /**
     * Convenience method to determine if order is started as of the current date.
     * @since 1.10.1
     */
    fun isStarted(): Boolean = isStarted(Date())

    /**
     * Convenience method to determine if the order is started as of the specified date.
     * @since 1.10.1
     */
    fun isStarted(aCheckDate: Date?): Boolean {
        if (voided == true) return false
        val effectiveStart = effectiveStartDate ?: return false
        val checkDate = aCheckDate ?: Date()
        return !checkDate.before(effectiveStart)
    }

    /**
     * Convenience method to determine if the order is discontinued as of the specified date.
     */
    fun isDiscontinued(aCheckDate: Date?): Boolean {
        if (dateStopped != null && autoExpireDate != null && dateStopped!!.after(autoExpireDate)) {
            throw APIException("Order.error.invalidDateStoppedAndAutoExpireDate", null as Array<Any>?)
        }
        if (voided == true) return false
        val checkDate = aCheckDate ?: Date()
        if (!isActivated(checkDate) || dateStopped == null) return false
        return checkDate.after(dateStopped)
    }

    /**
     * Convenience method to determine if the order is expired as of the current time.
     * @since 1.10.1
     */
    fun isExpired(): Boolean = isExpired(Date())

    /**
     * Convenience method to determine if order was expired at a given time.
     * @since 1.10.1
     */
    fun isExpired(aCheckDate: Date?): Boolean {
        if (dateStopped != null && autoExpireDate != null && dateStopped!!.after(autoExpireDate)) {
            throw APIException("Order.error.invalidDateStoppedAndAutoExpireDate", null as Array<Any>?)
        }
        if (voided == true) return false
        val checkDate = aCheckDate ?: Date()
        if (!isActivated(checkDate)) return false
        if (isDiscontinued(checkDate) || autoExpireDate == null) return false
        return checkDate.after(autoExpireDate)
    }

    /** @since 1.5 */
    fun isDiscontinuedRightNow(): Boolean = isDiscontinued(Date())

    /**
     * A convenience method to return start of the schedule for order.
     * @since 1.10
     */
    val effectiveStartDate: Date?
        get() = if (urgency == Urgency.ON_SCHEDULED_DATE) scheduledDate else dateActivated

    /**
     * A convenience method to return end of the schedule for order.
     * @since 1.10
     */
    val effectiveStopDate: Date?
        get() = dateStopped ?: autoExpireDate

    // Clone methods

    /**
     * Creates a discontinuation order for this order.
     * @since 1.10
     */
    open fun cloneForDiscontinuing(): Order = Order().apply {
        careSetting = this@Order.careSetting
        concept = this@Order.concept
        action = Action.DISCONTINUE
        previousOrder = this@Order
        patient = this@Order.patient
        orderType = this@Order.orderType
    }

    /**
     * Creates an order for revision from this order.
     * @since 1.10
     */
    open fun cloneForRevision(): Order = cloneForRevisionHelper(Order())

    /**
     * The purpose of this method is to allow subclasses of Order to delegate a portion of their
     * cloneForRevision() method back to the superclass.
     */
    protected open fun cloneForRevisionHelper(target: Order): Order = target.apply {
        if (this@Order.action == Action.DISCONTINUE) {
            action = Action.DISCONTINUE
            previousOrder = this@Order._previousOrder
            dateActivated = this@Order.dateActivated
        } else {
            action = Action.REVISE
            previousOrder = this@Order
            autoExpireDate = this@Order.autoExpireDate
        }
        careSetting = this@Order.careSetting
        concept = this@Order.concept
        patient = this@Order.patient
        orderType = this@Order.orderType
        scheduledDate = this@Order.scheduledDate
        instructions = this@Order.instructions
        urgency = this@Order.urgency
        commentToFulfiller = this@Order.commentToFulfiller
        orderReason = this@Order.orderReason
        orderReasonNonCoded = this@Order.orderReasonNonCoded
        orderGroup = this@Order.orderGroup
        sortWeight = this@Order.sortWeight
        fulfillerStatus = this@Order.fulfillerStatus
        fulfillerComment = this@Order.fulfillerComment
        formNamespaceAndPath = this@Order.formNamespaceAndPath
    }

    /**
     * Checks whether this order's orderType matches or is a sub type of the specified one.
     * @since 1.10
     */
    fun isType(orderType: OrderType): Boolean = OrderUtil.isType(orderType, this.orderType)

    /**
     * Checks whether orderable of this order is same as other order.
     * @since 1.10
     */
    open fun hasSameOrderableAs(otherOrder: Order?): Boolean {
        if (otherOrder == null) return false
        return OpenmrsUtil.nullSafeEquals(concept, otherOrder.concept)
    }

    // FormRecordable implementation

    override fun getFormFieldNamespace(): String? =
        BaseFormRecordableOpenmrsData.getFormFieldNamespace(formNamespaceAndPath)

    override fun getFormFieldPath(): String? =
        BaseFormRecordableOpenmrsData.getFormFieldPath(formNamespaceAndPath)

    override fun setFormField(namespace: String?, formFieldPath: String?) {
        formNamespaceAndPath = BaseFormRecordableOpenmrsData.getFormNamespaceAndPath(namespace, formFieldPath)
    }

    override fun toString(): String {
        val prefix = if (action == Action.DISCONTINUE) "DC " else ""
        return "${prefix}Order. orderId: $orderId patient: $patient concept: $concept care setting: $careSetting"
    }

    override var id: Integer?
        get() = orderId?.let { Integer(it) }
        set(value) { orderId = value?.toInt() }
}

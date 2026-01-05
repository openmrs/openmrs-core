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

import org.apache.commons.lang3.StringUtils
import org.hibernate.envers.Audited
import org.openmrs.util.OpenmrsUtil

/**
 * DrugOrder
 *
 * @version 1.0
 */
@Audited
open class DrugOrder() : Order() {

    companion object {
        const val serialVersionUID: Long = 72232L
    }

    var dose: Double? = null

    var doseUnits: Concept? = null

    /** @since 1.10 (signature changed) */
    var frequency: OrderFrequency? = null

    /** @since 1.10 */
    var asNeeded: Boolean? = false

    var quantity: Double? = null

    /** @since 1.10 */
    var quantityUnits: Concept? = null

    private var _drug: Drug? = null
    var drug: Drug?
        get() = _drug
        set(value) {
            _drug = value
            if (value != null && concept == null) {
                concept = value.concept
            }
        }

    /** @since 1.10 */
    var asNeededCondition: String? = null

    /** @since 1.10 */
    var dosingType: Class<out DosingInstructions> = SimpleDosingInstructions::class.java

    /** @since 1.10 */
    var numRefills: Int? = null

    /** @since 1.10 */
    var dosingInstructions: String? = null

    /** @since 1.10 */
    var duration: Int? = null

    /** @since 1.10 */
    var durationUnits: Concept? = null

    /** @since 1.10 */
    var route: Concept? = null

    /** @since 1.10 */
    var brandName: String? = null

    /** @since 1.10 */
    var dispenseAsWritten: Boolean? = false

    private var _drugNonCoded: String? = null
    /** @since 1.12 */
    var drugNonCoded: String?
        get() = _drugNonCoded
        set(value) {
            _drugNonCoded = if (StringUtils.isNotBlank(value)) value?.trim() else value
        }

    /** Constructor with id */
    constructor(orderId: Int?) : this() {
        this.orderId = orderId
    }

    fun isDrugOrder(): Boolean = true

    override fun copy(): DrugOrder = copyHelper(DrugOrder())

    protected open fun copyHelper(target: DrugOrder): DrugOrder {
        super.copyHelper(target)
        return target.apply {
            dose = this@DrugOrder.dose
            doseUnits = this@DrugOrder.doseUnits
            frequency = this@DrugOrder.frequency
            asNeeded = this@DrugOrder.asNeeded
            asNeededCondition = this@DrugOrder.asNeededCondition
            quantity = this@DrugOrder.quantity
            quantityUnits = this@DrugOrder.quantityUnits
            _drug = this@DrugOrder._drug
            dosingType = this@DrugOrder.dosingType
            dosingInstructions = this@DrugOrder.dosingInstructions
            duration = this@DrugOrder.duration
            durationUnits = this@DrugOrder.durationUnits
            numRefills = this@DrugOrder.numRefills
            route = this@DrugOrder.route
            brandName = this@DrugOrder.brandName
            dispenseAsWritten = this@DrugOrder.dispenseAsWritten
            _drugNonCoded = this@DrugOrder._drugNonCoded
        }
    }

    /**
     * Gets the dosingInstructions instance.
     * @since 1.10
     */
    val dosingInstructionsInstance: DosingInstructions
        get() = try {
            dosingType.getDeclaredConstructor().newInstance().getDosingInstructions(this)
        } catch (e: Exception) {
            throw IllegalStateException(e)
        }

    /** @since 1.10 */
    override fun cloneForDiscontinuing(): DrugOrder = DrugOrder().apply {
        careSetting = this@DrugOrder.careSetting
        concept = this@DrugOrder.concept
        action = Action.DISCONTINUE
        previousOrder = this@DrugOrder
        patient = this@DrugOrder.patient
        _drug = this@DrugOrder._drug
        orderType = this@DrugOrder.orderType
        _drugNonCoded = this@DrugOrder._drugNonCoded
    }

    /** @since 1.10 */
    override fun cloneForRevision(): DrugOrder = cloneForRevisionHelper(DrugOrder())

    protected open fun cloneForRevisionHelper(target: DrugOrder): DrugOrder {
        super.cloneForRevisionHelper(target)
        return target.apply {
            dose = this@DrugOrder.dose
            doseUnits = this@DrugOrder.doseUnits
            frequency = this@DrugOrder.frequency
            asNeeded = this@DrugOrder.asNeeded
            asNeededCondition = this@DrugOrder.asNeededCondition
            quantity = this@DrugOrder.quantity
            quantityUnits = this@DrugOrder.quantityUnits
            _drug = this@DrugOrder._drug
            dosingType = this@DrugOrder.dosingType
            dosingInstructions = this@DrugOrder.dosingInstructions
            duration = this@DrugOrder.duration
            durationUnits = this@DrugOrder.durationUnits
            route = this@DrugOrder.route
            numRefills = this@DrugOrder.numRefills
            brandName = this@DrugOrder.brandName
            dispenseAsWritten = this@DrugOrder.dispenseAsWritten
            _drugNonCoded = this@DrugOrder._drugNonCoded
        }
    }

    /**
     * Sets autoExpireDate based on duration.
     */
    fun setAutoExpireDateBasedOnDuration() {
        if (action != Action.DISCONTINUE && autoExpireDate == null) {
            autoExpireDate = dosingInstructionsInstance.getAutoExpireDate(this)
        }
    }

    /**
     * Set dosing instructions to drug order.
     * @since 1.10
     */
    fun setDosing(di: DosingInstructions) {
        di.setDosingInstructions(this)
    }

    /** @since 1.12 */
    fun isNonCodedDrug(): Boolean = StringUtils.isNotBlank(drugNonCoded)

    /**
     * Checks whether orderable of this drug order is same as other order.
     * @since 1.10
     */
    override fun hasSameOrderableAs(otherOrder: Order?): Boolean {
        if (!super.hasSameOrderableAs(otherOrder)) return false
        if (otherOrder !is DrugOrder) return false

        return if (isNonCodedDrug() || otherOrder.isNonCodedDrug()) {
            OpenmrsUtil.nullSafeEqualsIgnoreCase(drugNonCoded, otherOrder.drugNonCoded)
        } else {
            OpenmrsUtil.nullSafeEquals(drug, otherOrder.drug)
        }
    }

    override fun toString(): String {
        val prefix = if (action == Action.DISCONTINUE) "DC " else ""
        val drugName = when {
            isNonCodedDrug() -> drugNonCoded
            drug != null -> drug?.name
            else -> "[no drug]"
        }
        val endDate = if (isDiscontinuedRightNow()) dateStopped else autoExpireDate
        return "${prefix}DrugOrder($dose$doseUnits of $drugName from $dateActivated to $endDate)"
    }
}

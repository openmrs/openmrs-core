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
import org.openmrs.util.OpenmrsUtil
import java.util.Date
import java.util.Objects

/**
 * @since 2.1.0
 */
@Audited
class CohortMembership() : BaseChangeableOpenmrsData(), Comparable<CohortMembership> {
    
    var cohortMemberId: Int? = null
    
    var cohort: Cohort? = null
    
    var patientId: Int? = null
    
    private var _startDate: Date? = null
    var startDate: Date?
        get() = _startDate?.clone() as Date?
        set(value) {
            _startDate = value?.let { Date(it.time) }
        }
    
    private var _endDate: Date? = null
    var endDate: Date?
        get() = _endDate?.clone() as Date?
        set(value) {
            _endDate = value?.let { Date(it.time) }
        }
    
    constructor(patientId: Int?, startDate: Date? = Date()) : this() {
        this.patientId = patientId
        this._startDate = startDate
    }
    
    /**
     * Compares asOfDate to [startDate, endDate], inclusive of both endpoints.
     * @param asOfDate date to compare if membership is active or inactive
     * @return boolean true/false if membership is active/inactive
     */
    fun isActive(asOfDate: Date? = null): Boolean {
        val date = asOfDate ?: Date()
        return !voided && OpenmrsUtil.compare(_startDate, date) <= 0 &&
            OpenmrsUtil.compareWithNullAsLatest(date, _endDate) <= 0
    }
    
    override var id: Int?
        get() = cohortMemberId
        set(value) {
            cohortMemberId = value
        }
    
    /**
     * Sorts by following fields, in order:
     * 
     * 1. voided (voided memberships sort last)
     * 2. endDate descending (so ended memberships are towards the end, and the older the more towards the end
     * 3. startDate descending (so started more recently is towards the front)
     * 4. patientId ascending (intuitive and consistent tiebreaker for client code)
     * 5. uuid ascending (just so we have a final consistent tie breaker)
     *
     * @param other other membership to compare this to
     * @return value greater than `0` if this is not voided and other is voided; or value less
     *         than `0` if this is voided and other is not voided; if both is voided or not then
     *         value greater than `0` if other.endDate return null; or value less than
     *         `0` if this.endDate return null; if both are null or not then value
     *         greater than `0` if this.endDate is before other.endDate; or value less
     *         than `0` if this.endDate is after other.endDate; if are equal then value
     *         greater than `0` if this.startDate return null; or value less than
     *         `0` if other.startDate return null; if both are null or not then value greater
     *         than `0` if this.startDate is before other.startDate; or value less than
     *         `0` if this.startDate is after other.startDate; if are equal then value
     *         greater than `0` if other.patientId is greater than this.patientId; or
     *         value less than `0` if other.patientId is less than this.patientId; if
     *         are equal then value greater than `0` if other.uuid is greater than
     *         this.uuid; or value less than `0` if other.uuid is less than
     *         this.uuid; or `0` if are equal
     */
    override fun compareTo(other: CohortMembership): Int {
        var ret = voided.compareTo(other.voided)
        if (ret == 0) {
            ret = -OpenmrsUtil.compareWithNullAsLatest(_endDate, other._endDate)
        }
        if (ret == 0) {
            ret = -OpenmrsUtil.compareWithNullAsEarliest(_startDate, other._startDate)
        }
        if (ret == 0) {
            ret = patientId!!.compareTo(other.patientId!!)
        }
        if (ret == 0) {
            ret = uuid.compareTo(other.uuid)
        }
        return ret
    }
    
    /**
     * @since 2.3.0
     * Indicates if a given cohortMembership object is equal to this one
     * 
     * @param other is a CohortMembership object that should be checked for equality with this object
     * @return true if both objects are logically equal. This is the case when endDate, startDate and patientId are equal  
     */
    override fun equals(other: Any?): Boolean {
        if (other == null || other !is CohortMembership) {
            return false
        }
        if (this === other) {
            return true
        }
        
        return (_endDate?.equals(other._endDate) ?: (other._endDate == null)) &&
            (_startDate?.equals(other._startDate) ?: (other._startDate == null)) &&
            (patientId?.equals(other.patientId) ?: (other.patientId == null))
    }
    
    /**
     * @since 2.3.0
     * 
     * Creates a hash code of this object
     */
    override fun hashCode(): Int = Objects.hash(patientId, _endDate, _startDate)
    
    companion object {
        const val serialVersionUID = 0L
    }
}

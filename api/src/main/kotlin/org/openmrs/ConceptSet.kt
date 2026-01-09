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
import java.io.Serializable
import java.util.Date

/**
 * This represents a single concept within a concept set.
 */
@Audited
class ConceptSet() : BaseOpenmrsObject(), Auditable, Serializable, Comparable<ConceptSet> {

    companion object {
        const val serialVersionUID: Long = 3787L
    }

    var conceptSetId: Int? = null

    // concept in the set
    var concept: Concept? = null

    // parent concept that uses this set
    var conceptSet: Concept? = null

    var sortWeight: Double? = null

    override var creator: User? = null

    override var dateCreated: Date? = null

    constructor(concept: Concept?, weight: Double?) : this() {
        this.concept = concept
        this.sortWeight = weight
    }

    override var id: Integer?
        get() = conceptSetId?.let { Integer(it) }
        set(value) { conceptSetId = value?.toInt() }

    /**
     * Not currently used. Always returns null.
     *
     * @see Auditable.getChangedBy
     */
    override var changedBy: User?
        get() = null
        set(_) {}

    /**
     * Not currently used. Always returns null.
     *
     * @see Auditable.getDateChanged
     */
    override var dateChanged: Date?
        get() = null
        set(_) {}

    /**
     * @see Comparable.compareTo
     * Note: this comparator imposes orderings that are inconsistent with equals.
     */
    @Suppress("squid:S1210")
    override fun compareTo(other: ConceptSet): Int {
        var value = OpenmrsUtil.compareWithNullAsLowest<Boolean>(concept?.retired, other.concept?.retired)
        if (value == 0) {
            value = OpenmrsUtil.compareWithNullAsLowest<Double>(sortWeight, other.sortWeight)
        }
        return value
    }
}

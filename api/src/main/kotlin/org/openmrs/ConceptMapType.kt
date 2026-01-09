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

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.codehaus.jackson.annotate.JsonIgnore
import org.hibernate.envers.Audited

/**
 * ConceptMapType are used to define relationships between concepts and concept reference terms e.g
 * IS_A or SAME_AS, BROADER_THAN
 *
 * @since 1.9
 */
@Entity
@Table(name = "concept_map_type")
@Audited
class ConceptMapType() : BaseChangeableOpenmrsMetadata() {

    companion object {
        private const val serialVersionUID: Long = 1L

        const val SAME_AS_MAP_TYPE_UUID: String = "35543629-7d8c-11e1-909d-c80aa9edcf4e"
    }

    @Id
    @Column(name = "concept_map_type_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var conceptMapTypeId: Int? = null

    @Column(name = "is_hidden", nullable = false, length = 1)
    var isHidden: Boolean = false

    constructor(conceptMapTypeId: Int?) : this() {
        this.conceptMapTypeId = conceptMapTypeId
    }

    override var id: Integer?
        get() = conceptMapTypeId?.let { Integer(it) }
        set(value) { conceptMapTypeId = value?.toInt() }

    override fun toString(): String = name ?: ""

    /**
     * Returns true if this concept map type is hidden otherwise false
     *
     * @return true if this concept map type is hidden otherwise false
     *
     * @deprecated as of 2.0, use [isHidden]
     */
    @Deprecated("Use isHidden property", ReplaceWith("isHidden"))
    @JsonIgnore
    fun isHidden(): Boolean = isHidden
}

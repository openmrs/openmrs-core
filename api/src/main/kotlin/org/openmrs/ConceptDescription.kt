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
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.envers.Audited
import java.io.Serializable
import java.util.Date
import java.util.Locale

/**
 * ConceptDescription is the localized description of a concept.
 */
@Audited
@Entity
@Table(name = "concept_description")
class ConceptDescription() : BaseOpenmrsObject(), Auditable, Serializable {

    companion object {
        private const val serialVersionUID: Long = -7223075113369136584L
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "concept_description_id", nullable = false)
    var conceptDescriptionId: Int? = null

    @ManyToOne(optional = false)
    @JoinColumn(name = "concept_id", nullable = false)
    var concept: Concept? = null

    @Column(name = "description", nullable = false, length = 65535, columnDefinition = "text")
    var description: String? = null

    @Column(name = "locale", nullable = false, length = 50)
    var locale: Locale? = null

    @ManyToOne(optional = false)
    @JoinColumn(name = "creator", nullable = false)
    override var creator: User? = null

    @Column(name = "date_created", nullable = false)
    override var dateCreated: Date? = null

    @ManyToOne
    @JoinColumn(name = "changed_by")
    override var changedBy: User? = null

    @Column(name = "date_changed")
    override var dateChanged: Date? = null

    /**
     * Constructor that takes in the primary key for this object
     *
     * @param conceptDescriptionId the id for this description
     */
    constructor(conceptDescriptionId: Int?) : this() {
        this.conceptDescriptionId = conceptDescriptionId
    }

    /**
     * Constructor specifying the description and locale.
     *
     * @param description
     * @param locale
     */
    constructor(description: String?, locale: Locale?) : this() {
        this.locale = locale
        this.description = description
    }

    override var id: Integer?
        get() = conceptDescriptionId?.let { Integer(it) }
        set(value) { conceptDescriptionId = value?.toInt() }

    override fun toString(): String = description ?: ""
}

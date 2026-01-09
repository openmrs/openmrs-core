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
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.codehaus.jackson.annotate.JsonIgnore
import org.hibernate.annotations.BatchSize
import org.hibernate.envers.Audited
import java.io.Serializable
import java.util.Date

/**
 * ConceptNameTag is a textual tag which can be applied to a ConceptName.
 */
@Entity
@Table(name = "concept_name_tag")
@Audited
@BatchSize(size = 25)
class ConceptNameTag() : BaseOpenmrsObject(), Auditable, Voidable, Serializable {

    companion object {
        const val serialVersionUID: Long = 33226787L
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "concept_name_tag_id")
    var conceptNameTagId: Int? = null

    @Column(name = "tag", length = 50, nullable = false)
    var tag: String? = null

    @Column(name = "description", columnDefinition = "TEXT", length = 65535)
    var description: String? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator", nullable = false)
    override var creator: User? = null

    @Column(name = "date_created", nullable = false)
    override var dateCreated: Date? = null

    @Column(name = "voided", nullable = false)
    override var voided: Boolean? = false

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voided_by")
    override var voidedBy: User? = null

    @Column(name = "date_voided")
    override var dateVoided: Date? = null

    @Column(name = "void_reason", length = 255)
    override var voidReason: String? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by")
    override var changedBy: User? = null

    @Column(name = "date_changed")
    override var dateChanged: Date? = null

    /**
     * Public constructor. Use factory methods to obtain copies of the desired tags.
     *
     * @param tag
     * @param description
     */
    constructor(tag: String?, description: String?) : this() {
        this.tag = tag
        this.description = description
    }

    override var id: Integer?
        get() = conceptNameTagId?.let { Integer(it) }
        set(value) { conceptNameTagId = value?.toInt() }

    override fun toString(): String = tag ?: ""

    /**
     * Returns whether the ConceptName has been voided.
     *
     * @return true if the ConceptName has been voided, false otherwise.
     * @deprecated as of 2.0, use [voided]
     */
    @Deprecated("Use voided property", ReplaceWith("voided"))
    @JsonIgnore
    fun isVoided(): Boolean = voided
}

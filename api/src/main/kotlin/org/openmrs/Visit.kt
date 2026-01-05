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

import jakarta.persistence.Access
import jakarta.persistence.AccessType
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.OrderBy
import jakarta.persistence.Table
import org.hibernate.annotations.BatchSize
import org.hibernate.envers.Audited
import org.openmrs.customdatatype.Customizable
import java.util.Date
import java.util.LinkedHashSet

/**
 * A 'visit' is a contiguous time period where encounters occur between patients and healthcare
 * providers. This can function as a grouper for encounters
 *
 * @since 1.9
 */
@Entity
@Table(name = "visit")
@Audited
class Visit() : BaseCustomizableData<VisitAttribute>(), Auditable, Customizable<VisitAttribute> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "visit_id")
    var visitId: Int? = null

    @ManyToOne(optional = false)
    @JoinColumn(name = "patient_id")
    var patient: Patient? = null

    @ManyToOne(optional = false)
    @JoinColumn(name = "visit_type_id")
    var visitType: VisitType? = null

    @ManyToOne
    @JoinColumn(name = "indication_concept_id")
    var indication: Concept? = null

    @ManyToOne
    @JoinColumn(name = "location_id")
    var location: Location? = null

    @Column(name = "date_started", nullable = false, length = 19)
    var startDatetime: Date? = null

    @Column(name = "date_stopped", length = 19)
    var stopDatetime: Date? = null

    @OneToMany(mappedBy = "visit")
    @OrderBy("encounter_datetime desc, encounter_id desc")
    private var _encounters: MutableSet<Encounter>? = null

    @Access(AccessType.PROPERTY)
    @OneToMany(mappedBy = "visit", cascade = [CascadeType.ALL], orphanRemoval = true)
    @OrderBy("voided asc")
    @BatchSize(size = 100)
    private var _visitAttributes: MutableSet<VisitAttribute> = LinkedHashSet()

    override fun getAttributes(): MutableSet<VisitAttribute> = _visitAttributes

    override fun setAttributes(attributes: MutableSet<VisitAttribute>?) {
        _visitAttributes = attributes ?: LinkedHashSet()
    }

    /** Constructor with visitId */
    constructor(visitId: Int?) : this() {
        this.visitId = visitId
    }

    /** Convenience constructor with required fields */
    constructor(patient: Patient?, visitType: VisitType?, startDatetime: Date?) : this() {
        this.patient = patient
        this.visitType = visitType
        this.startDatetime = startDatetime
    }

    var encounters: MutableSet<Encounter>
        get() {
            if (_encounters == null) _encounters = HashSet()
            return _encounters!!
        }
        set(value) { _encounters = value }

    /** Returns non-voided encounters */
    val nonVoidedEncounters: List<Encounter>
        get() = encounters.filterNot { it.voided == true }

    fun addEncounter(encounter: Encounter?) {
        encounter?.apply {
            visit = this@Visit
            encounters.add(this)
        }
    }

    override fun toString(): String = "Visit #$visitId"

    override var id: Integer?
        get() = visitId?.let { Integer(it) }
        set(value) { visitId = value?.toInt() }
}

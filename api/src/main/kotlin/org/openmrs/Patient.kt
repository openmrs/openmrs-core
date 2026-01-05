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

import jakarta.persistence.Cacheable
import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
import org.hibernate.envers.Audited
import java.util.TreeSet

/**
 * Defines a Patient in the system. A patient is simply an extension of a person and all that that
 * implies.
 *
 * @version 2.0
 */
@Audited
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
class Patient() : Person() {

    companion object {
        const val serialVersionUID: Long = 93123L
    }

    private var _patientId: Int? = null

    /** @since 2.0 */
    var allergyStatus: String = Allergies.UNKNOWN

    private var _identifiers: MutableSet<PatientIdentifier>? = null

    init {
        setPatient(true)
    }

    /**
     * Creates a new Patient object from the given [Person] object.
     * All attributes are copied over to the new object.
     * NOTE: All child collection objects are copied as pointers, each individual element is not copied.
     */
    constructor(person: Person?) : this() {
        if (person != null) {
            // Call parent copy constructor logic
            _patientId = person.getPersonId()
            setPersonId(person.getPersonId())
            uuid = person.uuid
            addresses = person.addresses
            names = person.names
            attributes = person.attributes
            gender = person.gender
            birthdate = person.birthdate
            birthtime = person.birthtime
            birthdateEstimated = person.birthdateEstimated
            deathdateEstimated = person.deathdateEstimated
            dead = person.dead
            deathDate = person.deathDate
            causeOfDeath = person.causeOfDeath
            causeOfDeathNonCoded = person.causeOfDeathNonCoded
            personCreator = person.personCreator
            personDateCreated = person.personDateCreated
            personChangedBy = person.personChangedBy
            personDateChanged = person.personDateChanged
            personVoided = person.personVoided
            personVoidedBy = person.personVoidedBy
            personDateVoided = person.personDateVoided
            personVoidReason = person.personVoidReason
        }
        setPatient(true)
    }

    /** Constructor with default patient id */
    constructor(patientId: Int?) : this() {
        super.setPersonId(patientId)
        this._patientId = patientId
        setPatient(true)
    }

    /**
     * Creates a new Patient object from the given [Patient] object.
     * All attributes are copied over (clone/duplicate).
     * @since 2.2.0
     */
    constructor(patient: Patient) : this(patient as Person) {
        this._patientId = patient.getPatientId()
        this.allergyStatus = patient.allergyStatus
        val newIdentifiers = TreeSet<PatientIdentifier>()
        for (pid in patient.identifiers) {
            val identifierClone = pid.clone() as PatientIdentifier
            identifierClone.patient = this
            newIdentifiers.add(identifierClone)
        }
        this._identifiers = newIdentifiers
    }

    fun getPatientId(): Int? {
        if (_patientId == null) {
            _patientId = getPersonId()
        }
        return _patientId
    }

    /**
     * Sets the internal identifier for a patient.
     * **This should never be called directly.** It exists only for the use of the supporting infrastructure.
     */
    fun setPatientId(patientId: Int?) {
        super.setPersonId(patientId)
        this._patientId = patientId
    }

    override fun setPersonId(personId: Int?) {
        super.setPersonId(personId)
        this._patientId = personId
    }

    // Identifiers

    var identifiers: MutableSet<PatientIdentifier>
        get() {
            if (_identifiers == null) _identifiers = TreeSet()
            return _identifiers!!
        }
        set(value) { _identifiers = value }

    fun addIdentifiers(patientIdentifiers: Collection<PatientIdentifier>) {
        patientIdentifiers.forEach { addIdentifier(it) }
    }

    fun addIdentifier(patientIdentifier: PatientIdentifier?) {
        patientIdentifier ?: return
        patientIdentifier.patient = this

        // Check for duplicates
        if (activeIdentifiers.any { it.equalsContent(patientIdentifier) }) {
            return
        }

        identifiers.add(patientIdentifier)
    }

    fun removeIdentifier(patientIdentifier: PatientIdentifier?) {
        patientIdentifier?.let { identifiers.remove(it) }
    }

    /**
     * Returns the first "preferred" identifier, otherwise the first non-voided identifier, or null.
     */
    val patientIdentifier: PatientIdentifier?
        get() {
            if (identifiers.isEmpty()) return null
            return identifiers.firstOrNull { it.preferred == true && it.voided != true }
                ?: identifiers.firstOrNull { it.voided != true }
        }

    fun getPatientIdentifier(pit: PatientIdentifierType): PatientIdentifier? {
        if (identifiers.isEmpty()) return null
        return identifiers.firstOrNull { it.preferred == true && it.voided != true && pit == it.identifierType }
            ?: identifiers.firstOrNull { it.voided != true && pit == it.identifierType }
    }

    fun getPatientIdentifier(identifierTypeId: Int): PatientIdentifier? {
        if (identifiers.isEmpty()) return null
        return identifiers.firstOrNull {
            it.preferred == true && it.voided != true &&
            identifierTypeId == it.identifierType?.patientIdentifierTypeId
        } ?: identifiers.firstOrNull {
            it.voided != true && identifierTypeId == it.identifierType?.patientIdentifierTypeId
        }
    }

    fun getPatientIdentifier(identifierTypeName: String): PatientIdentifier? {
        if (identifiers.isEmpty()) return null
        return identifiers.firstOrNull {
            it.preferred == true && it.voided != true &&
            identifierTypeName == it.identifierType?.name
        } ?: identifiers.firstOrNull {
            it.voided != true && identifierTypeName == it.identifierType?.name
        }
    }

    /** Returns non-voided identifiers with preferred ones first */
    val activeIdentifiers: List<PatientIdentifier>
        get() {
            val preferred = mutableListOf<PatientIdentifier>()
            val nonPreferred = mutableListOf<PatientIdentifier>()
            for (pi in identifiers) {
                if (pi.voided != true) {
                    if (pi.preferred == true) {
                        preferred.add(pi)
                    } else {
                        nonPreferred.add(pi)
                    }
                }
            }
            return preferred + nonPreferred
        }

    fun getPatientIdentifiers(pit: PatientIdentifierType): List<PatientIdentifier> =
        identifiers.filter { it.voided != true && pit == it.identifierType }

    override fun toString(): String = "Patient#$_patientId"

    override var id: Integer?
        get() = getPatientId()?.let { Integer(it) }
        set(value) { setPatientId(value?.toInt()) }

    /** Returns the person represented (this object) */
    val person: Person
        get() = this
}

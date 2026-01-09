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
import org.hibernate.annotations.GenericGenerator
import org.hibernate.annotations.Parameter
import org.hibernate.envers.Audited
import org.openmrs.util.OpenmrsUtil
import java.io.Serializable
import java.util.*

/**
 * PatientState
 */
@Entity
@Table(name = "patient_state")
@Audited
class PatientState() : BaseFormRecordableOpenmrsData(), Serializable, Comparable<PatientState> {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "patient_state_id_seq")
    @GenericGenerator(
        name = "patient_state_id_seq",
        strategy = "native",
        parameters = [Parameter(name = "sequence", value = "patient_state_patient_state_id_seq")]
    )
    @Column(name = "patient_state_id")
    var patientStateId: Int? = null

    @ManyToOne
    @JoinColumn(name = "patient_program_id", nullable = false)
    var patientProgram: PatientProgram? = null

    @ManyToOne
    @JoinColumn(name = "state", nullable = false)
    var state: ProgramWorkflowState? = null

    @Column(name = "start_date", length = 19)
    var startDate: Date? = null

    @Column(name = "end_date", length = 19)
    var endDate: Date? = null

    @ManyToOne
    @JoinColumn(name = "encounter_id")
    var encounter: Encounter? = null

    /** Constructor with id */
    constructor(patientStateId: Int?) : this() {
        this.patientStateId = patientStateId
    }

    /**
     * Does a shallow copy of this PatientState. Does NOT copy patientStateId
     *
     * @return a copy of this PatientState
     */
    fun copy(): PatientState = copyHelper(PatientState())

    /**
     * The purpose of this method is to allow subclasses of PatientState to delegate a portion of
     * their copy() method back to the superclass, in case the base class implementation changes.
     *
     * @param target a PatientState that will have the state of this copied into it
     * @return the PatientState that was passed in, with state copied into it
     */
    protected fun copyHelper(target: PatientState): PatientState {
        target.patientProgram = this.patientProgram
        target.state = this.state
        target.startDate = this.startDate
        target.endDate = this.endDate
        target.encounter = this.encounter
        target.creator = this.creator
        target.dateCreated = this.dateCreated
        target.changedBy = this.changedBy
        target.dateChanged = this.dateChanged
        target.voided = this.voided
        target.voidedBy = this.voidedBy
        target.dateVoided = this.dateVoided
        target.voidReason = this.voidReason
        return target
    }

    /**
     * Returns true if this [PatientState] is active as of the passed [Date]
     *
     * @param onDate - [Date] to check for [PatientState] enrollment
     * @return boolean - true if this [PatientState] is active as of the passed [Date]
     * Should return false if voided and date in range
     * Should return false if voided and date not in range
     * Should return true if not voided and date in range
     * Should return false if not voided and date earlier than startDate
     * Should return false if not voided and date later than endDate
     * Should return true if not voided and date in range with null startDate
     * Should return true if not voided and date in range with null endDate
     * Should return true if not voided and both startDate and endDate nulled
     * Should compare with current date if date null
     */
    fun getActive(onDate: Date?): Boolean {
        val checkDate = onDate ?: Date()
        return !voided!! && 
               (OpenmrsUtil.compareWithNullAsEarliest(startDate, checkDate) <= 0) &&
               (OpenmrsUtil.compareWithNullAsLatest(endDate, checkDate) > 0)
    }

    /**
     * Returns true if this [PatientState] is currently active
     *
     * @return boolean - true if this [PatientState] is currently active
     */
    fun getActive(): Boolean = getActive(null)

    override fun toString(): String {
        return "id=$patientStateId, patientProgram=$patientProgram, state=$state, " +
               "startDate=$startDate, endDate=$endDate, encounter=$encounter, " +
               "dateCreated=$dateCreated, dateChanged=$dateChanged"
    }

    override fun getId(): Int? = patientStateId

    override fun setId(id: Int?) {
        patientStateId = id
    }

    /**
     * Compares by startDate with null as earliest and endDate with null as latest.
     *
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     * Should return positive if startDates equal and this endDate null
     * Should return negative if this startDate null
     * Should pass if two states have the same start date, end date and uuid
     * Should return positive or negative if two states have the same start date and end date but different uuids
     * Note: this comparator imposes orderings that are inconsistent with equals.
     */
    override fun compareTo(other: PatientState): Int {
        var result = OpenmrsUtil.compareWithNullAsEarliest(startDate, other.startDate)
        if (result == 0) {
            result = OpenmrsUtil.compareWithNullAsLatest(endDate, other.endDate)
        }
        if (result == 0) {
            result = OpenmrsUtil.compareWithNullAsGreatest(uuid, other.uuid)
        }
        return result
    }

    companion object {
        private const val serialVersionUID = 0L
    }
}

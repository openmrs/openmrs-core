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

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
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
import org.openmrs.customdatatype.CustomValueDescriptor
import org.openmrs.customdatatype.Customizable
import org.openmrs.util.OpenmrsUtil
import java.io.Serializable
import java.util.*

/**
 * PatientProgram
 */
@Entity
@Table(name = "patient_program")
@Audited
class PatientProgram() : BaseChangeableOpenmrsData(), Customizable<PatientProgramAttribute>, Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "patient_program_id")
    var patientProgramId: Int? = null

    @ManyToOne(optional = false)
    @JoinColumn(name = "patient_id")
    var patient: Patient? = null

    @ManyToOne(optional = false)
    @JoinColumn(name = "program_id")
    var program: Program? = null

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "location_id")
    var location: Location? = null

    @Column(name = "date_enrolled")
    var dateEnrolled: Date? = null

    @Column(name = "date_completed")
    var dateCompleted: Date? = null

    @ManyToOne
    @JoinColumn(name = "outcome_concept_id")
    var outcome: Concept? = null

    @OneToMany(mappedBy = "patientProgram", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.EAGER)
    var states: MutableSet<PatientState> = HashSet()

    @OneToMany(mappedBy = "patientProgram", cascade = [CascadeType.ALL], orphanRemoval = true)
    @OrderBy("voided ASC")
    @BatchSize(size = 100)
    private var attributes: MutableSet<PatientProgramAttribute> = LinkedHashSet()

    /** Constructor with id */
    constructor(patientProgramId: Int?) : this() {
        this.patientProgramId = patientProgramId
    }

    /**
     * Does a mostly-shallow copy of this PatientProgram. Does not copy patientProgramId. The
     * 'states' property will be deep-copied.
     *
     * @return a shallow copy of this PatientProgram
     */
    fun copy(): PatientProgram = copyHelper(PatientProgram())

    /**
     * The purpose of this method is to allow subclasses of PatientProgram to delegate a portion of
     * their copy() method back to the superclass, in case the base class implementation changes.
     *
     * @param target a PatientProgram that will have the state of this copied into it
     * @return the PatientProgram that was passed in, with state copied into it
     */
    protected fun copyHelper(target: PatientProgram): PatientProgram {
        target.patient = this.patient
        target.program = this.program
        target.location = this.location
        target.dateEnrolled = this.dateEnrolled
        target.dateCompleted = this.dateCompleted
        val statesCopy = HashSet<PatientState>()
        this.states.forEach { s ->
            val stateCopy = s.copy()
            stateCopy.patientProgram = target
            statesCopy.add(stateCopy)
        }
        target.states = statesCopy
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
     * Returns true if the associated [Patient] is enrolled in the associated [Program]
     * on the passed [Date]
     *
     * @param onDate - Date to check for PatientProgram enrollment
     * @return boolean - true if the associated [Patient] is enrolled in the associated
     *         [Program] on the passed [Date]
     */
    fun getActive(onDate: Date?): Boolean {
        val checkDate = onDate ?: Date()
        return !voided!! && 
               (dateEnrolled == null || OpenmrsUtil.compare(dateEnrolled, checkDate) <= 0) &&
               (dateCompleted == null || OpenmrsUtil.compare(dateCompleted, checkDate) > 0)
    }

    /**
     * Returns true if the associated [Patient] is currently enrolled in the associated
     * [Program]
     *
     * @return boolean - true if the associated [Patient] is currently enrolled in the
     *         associated [Program]
     */
    fun getActive(): Boolean = getActive(null)

    /**
     * Returns the [PatientState] associated with this PatientProgram that has an id that
     * matches the passed `patientStateId`
     *
     * @param patientStateId - The identifier to use to lookup a [PatientState]
     * @return PatientState that has an id that matches the passed `patientStateId`
     */
    fun getPatientState(patientStateId: Int?): PatientState? {
        return states.find { it.patientStateId == patientStateId }
    }

    /**
     * Attempts to transition the PatientProgram to the passed [ProgramWorkflowState] on the
     * passed [Date] by ending the most recent [PatientState] in the
     * [PatientProgram] and creating a new one with the passed [ProgramWorkflowState]
     * This will throw an IllegalArgumentException if the transition is invalid
     *
     * @param programWorkflowState - The [ProgramWorkflowState] to transition to
     * @param onDate - The [Date] of the transition
     * @throws IllegalArgumentException
     */
    fun transitionToState(programWorkflowState: ProgramWorkflowState, onDate: Date?) {
        val lastState = getCurrentState(programWorkflowState.programWorkflow)
        if (lastState != null && onDate == null) {
            throw IllegalArgumentException("You can't change from a non-null state without giving a change date")
        }
        if (lastState?.endDate != null) {
            throw IllegalArgumentException("You can't change out of a state that has an end date already")
        }
        if (lastState != null && lastState.startDate != null && 
            OpenmrsUtil.compare(lastState.startDate, onDate) > 0) {
            throw IllegalArgumentException("You can't change out of a state before that state started")
        }
        if (lastState != null && 
            !programWorkflowState.programWorkflow.isLegalTransition(lastState.state, programWorkflowState)) {
            throw IllegalArgumentException("You can't change from state ${lastState.state} to $programWorkflowState")
        }
        lastState?.endDate = onDate

        val newState = PatientState().apply {
            patientProgram = this@PatientProgram
            state = programWorkflowState
            startDate = onDate
            if (patientProgram?.dateCompleted != null) {
                endDate = patientProgram?.dateCompleted
            }
        }

        if (programWorkflowState.terminal == true) {
            dateCompleted = onDate
        }

        states.add(newState)
    }

    /**
     * Attempts to void the latest [PatientState] in the [PatientProgram] If earlier
     * PatientStates exist, it will try to reset the endDate to null so that the next latest state
     * becomes the current [PatientState]
     *
     * @param workflow - The [ProgramWorkflow] whose last [PatientState] within the
     *            current [PatientProgram] we want to void
     * @param voidBy - The user who is voiding the [PatientState]
     * @param voidDate - The date to void the [PatientState]
     * @param voidReason - The reason for voiding the [PatientState]
     * Should void state with endDate null if startDates equal
     */
    fun voidLastState(workflow: ProgramWorkflow, voidBy: User?, voidDate: Date?, voidReason: String?) {
        val statesList = statesInWorkflow(workflow, false)
        val checkDate = voidDate ?: Date()
        val last = statesList.lastOrNull()
        val nextToLast = if (statesList.size > 1) statesList[statesList.size - 2] else null

        last?.apply {
            voided = true
            voidedBy = voidBy
            dateVoided = checkDate
            this.voidReason = voidReason
        }

        nextToLast?.apply {
            if (endDate != null) {
                endDate = if (patientProgram?.dateCompleted != null) {
                    patientProgram?.dateCompleted
                } else {
                    null
                }
                dateChanged = checkDate
                changedBy = voidBy
            }
        }
    }

    /**
     * Returns the current [PatientState] for the passed [ProgramWorkflow] within this
     * [PatientProgram].
     *
     * @param programWorkflow The ProgramWorkflow whose current [PatientState] we want to
     *            retrieve
     * @return PatientState The current [PatientState] for the passed [ProgramWorkflow]
     *         within this [PatientProgram]
     */
    fun getCurrentState(programWorkflow: ProgramWorkflow?): PatientState? {
        val now = Date()
        var currentState: PatientState? = null

        for (state in sortedStates) {
            // states are sorted with the most current state at the last position
            if ((programWorkflow == null || state.state?.programWorkflow == programWorkflow) && 
                state.getActive(now)) {
                currentState = state
            }
        }
        return currentState
    }

    /**
     * Returns a Set<PatientState> of all current [PatientState]s for the
     * [PatientProgram]
     *
     * @return Set<PatientState> of all current [PatientState]s for the [PatientProgram]
     */
    fun getCurrentStates(): Set<PatientState> {
        val ret = HashSet<PatientState>()
        val now = Date()
        for (state in states) {
            if (state.getActive(now)) {
                ret.add(state)
            }
        }
        return ret
    }

    /**
     * Returns a Set<PatientState> of all recent [PatientState]s for each workflow of the
     * [PatientProgram]
     *
     * @return Set<PatientState> of all recent [PatientState]s for the [PatientProgram]
     */
    fun getMostRecentStateInEachWorkflow(): Set<PatientState> {
        val map = HashMap<ProgramWorkflow, PatientState>()

        for (state in sortedStates) {
            if (!state.voided!!) {
                state.state?.programWorkflow?.let { workflow ->
                    map[workflow] = state
                }
            }
        }

        return HashSet(map.values)
    }

    /**
     * Returns a List<PatientState> of all [PatientState]s in the passed
     * [ProgramWorkflow] for the [PatientProgram]
     *
     * @param programWorkflow - The [ProgramWorkflow] to check
     * @param includeVoided - If true, return voided [PatientState]s in the returned
     *            [List]
     * @return List<PatientState> of all [PatientState]s in the passed [ProgramWorkflow]
     *         for the [PatientProgram]
     */
    fun statesInWorkflow(programWorkflow: ProgramWorkflow, includeVoided: Boolean): List<PatientState> {
        val ret = ArrayList<PatientState>()
        for (st in sortedStates) {
            if (st.state?.programWorkflow == programWorkflow && (includeVoided || !st.voided!!)) {
                ret.add(st)
            }
        }
        return ret
    }

    override fun toString(): String {
        return "PatientProgram(id=$patientProgramId, patient=$patient, program=$program)"
    }

    override fun getId(): Int? = patientProgramId

    override fun setId(id: Int?) {
        patientProgramId = id
    }

    /**
     * @return states sorted by [PatientState.compareTo]
     */
    private val sortedStates: List<PatientState>
        get() {
            val sortedStatesList = ArrayList(states)
            Collections.sort(sortedStatesList)
            return sortedStatesList
        }

    override fun getAttributes(): Set<PatientProgramAttribute> = attributes

    override fun getActiveAttributes(): Collection<PatientProgramAttribute> {
        return attributes.filter { !it.voided!! }
    }

    override fun getActiveAttributes(ofType: CustomValueDescriptor?): List<PatientProgramAttribute> {
        return attributes.filter { it.attributeType == ofType && !it.voided!! }
    }

    override fun addAttribute(attribute: PatientProgramAttribute) {
        attribute.owner = this
        attributes.add(attribute)
    }

    fun setAttributes(attributes: MutableSet<PatientProgramAttribute>) {
        this.attributes = attributes
    }

    fun setAttribute(attribute: PatientProgramAttribute) {
        val activeAttrs = getActiveAttributes(attribute.attributeType)
        
        if (activeAttrs.size == 1) {
            val existing = activeAttrs[0]
            if (existing.value != attribute.value) {
                if (existing.id != null) {
                    existing.voided = true
                } else {
                    attributes.remove(existing)
                }
                attributes.add(attribute)
                attribute.owner = this
            }
        } else {
            // Void or remove all existing active attributes of this type
            activeAttrs.forEach { existing ->
                if (existing.id != null) {
                    existing.voided = true
                } else {
                    attributes.remove(existing)
                }
            }
            attributes.add(attribute)
            attribute.owner = this
        }
    }

    companion object {
        private const val serialVersionUID = 0L
    }
}

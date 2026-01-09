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
import org.openmrs.util.NaturalStrings

/**
 * ProgramWorkflow
 */
@Audited
open class ProgramWorkflow : BaseChangeableOpenmrsMetadata {
    
    companion object {
        private const val serialVersionUID = 1L
    }
    
    // ******************
    // Properties
    // ******************
    
    open var programWorkflowId: Int? = null
    
    open var program: Program? = null
    
    open var concept: Concept? = null
    
    open var states: MutableSet<ProgramWorkflowState> = mutableSetOf()
    
    // ******************
    // Constructors
    // ******************
    
    /** Default Constructor */
    constructor()
    
    /** Constructor with id */
    constructor(programWorkflowId: Int?) {
        this.programWorkflowId = programWorkflowId
    }
    
    // ******************
    // Instance methods
    // ******************
    
    /**
     * Adds a new [ProgramWorkflowState] to this ProgramWorkflow
     * 
     * @param state - the [ProgramWorkflowState] to add
     */
    open fun addState(state: ProgramWorkflowState) {
        state.programWorkflow = this
        states.add(state)
    }
    
    /**
     * Removes a [ProgramWorkflowState] from this ProgramWorkflow
     * 
     * @param state - the [ProgramWorkflowState] to remove
     */
    open fun removeState(state: ProgramWorkflowState) {
        if (states.contains(state)) {
            states.remove(state)
            state.programWorkflow = null
        }
    }
    
    /**
     * Retires a [ProgramWorkflowState]
     * 
     * @param state - the [ProgramWorkflowState] to retire
     */
    open fun retireState(state: ProgramWorkflowState) {
        state.retired = true
    }
    
    /**
     * Returns a [ProgramWorkflowState] whose primary key id matches the input parameter
     * 
     * @param programWorkflowStateId the primary key Int id to match
     * @return a [ProgramWorkflowState] whose identifier matches the passed
     *         [programWorkflowStateId]
     */
    open fun getState(programWorkflowStateId: Int?): ProgramWorkflowState? {
        return states.firstOrNull { it.programWorkflowStateId == programWorkflowStateId }
    }
    
    /**
     * Returns a [ProgramWorkflowState] whose Concept matches the passed concept
     * 
     * @param concept the Concept to match
     * @return Returns a [ProgramWorkflowState] whose [Concept] matches the passed
     *         [concept]
     */
    open fun getState(concept: Concept?): ProgramWorkflowState? {
        return states.firstOrNull { it.concept == concept }
    }
    
    /**
     * Returns a [ProgramWorkflowState] whose Concept name matches the passed name in any
     * Locale
     * 
     * @param name the Concept name to match in any Locale
     * @return a [ProgramWorkflowState] whose [Concept] name matches the passed
     *         [name]
     */
    open fun getState(name: String?): ProgramWorkflowState? {
        return states.firstOrNull { it.concept?.isNamed(name) == true }
    }
    
    /**
     * Returns a [ProgramWorkflowState] whose [Concept] has any [ConceptName] that
     * matches the given [name]
     * 
     * @param name the [ProgramWorkflowState] name, in any Locale
     * @return a [ProgramWorkflowState] which has the passed [name] in any
     *         Locale
     */
    open fun getStateByName(name: String?): ProgramWorkflowState? {
        return states.firstOrNull { it.concept?.isNamed(name) == true }
    }
    
    /**
     * Returns a Set<[ProgramWorkflowState]> including all non-retired ProgramWorkflowStates
     * and all retired ProgramWorkflowStates in this ProgramWorkflow if [includeRetired]
     * is true
     * 
     * @param includeRetired - if false, returns only non-retired [ProgramWorkflowState]
     *            objects in this ProgramWorkflow
     * @return Set<ProgramWorkflowState> - all ProgramWorkflowStates matching input parameters
     */
    open fun getStates(includeRetired: Boolean): Set<ProgramWorkflowState> {
        return if (includeRetired) {
            states.toSet()
        } else {
            states.filterNot { it.retired }.toSet()
        }
    }
    
    /**
     * Returns a Set<[ProgramWorkflowState]> including all ProgramWorkflowStates, sorted by
     * [ConceptName]
     * 
     * @return Set<ProgramWorkflowState> - all ProgramWorkflowStates, sorted by [ConceptName]
     * Should sort names containing numbers intelligently
     */
    open val sortedStates: Set<ProgramWorkflowState>
        get() {
            val naturalComparator = NaturalStrings.getNaturalComparator()
            val stateComparator = compareBy<ProgramWorkflowState> { state ->
                state.concept?.name?.name
            }.let { comparator ->
                Comparator { o1, o2 -> 
                    naturalComparator.compare(
                        o1.concept?.name?.name, 
                        o2.concept?.name?.name
                    )
                }
            }
            return states.sortedWith(stateComparator).toSet()
        }
    
    /**
     * Returns a List<[ProgramWorkflowState]> including all possible next
     * ProgramWorkflowStates, for the passed [PatientProgram] ordered by [ConceptName]
     * 
     * @param patientProgram - The PatientProgram to check
     * @return List<ProgramWorkflowState> - all possible next ProgramWorkflowStates, for the passed
     *         [PatientProgram] ordered by [ConceptName]
     */
    open fun getPossibleNextStates(patientProgram: PatientProgram): List<ProgramWorkflowState> {
        val currentState = patientProgram.getCurrentState(this)
        return sortedStates.filter { state ->
            isLegalTransition(currentState?.state, state)
        }
    }
    
    /**
     * Check whether it is allowable to transition from [fromState] to
     * [toState].
     * 
     * @param fromState [ProgramWorkflowState] to check transition from
     * @param toState [ProgramWorkflowState] to check transition to
     * @return boolean true if it is allowable to transition from [fromState] to
     *         [toState]
     */
    open fun isLegalTransition(fromState: ProgramWorkflowState?, toState: ProgramWorkflowState): Boolean {
        // If there's no current state then we need to move into an initial state
        if (fromState == null) {
            return toState.initial == true
        }
        
        // Does not allow patient to move into the same state
        return fromState != toState
    }
    
    /** @see Object.toString */
    override fun toString(): String {
        return "ProgramWorkflow(id=$programWorkflowId)"
    }
    
    // ******************
    // Property Access
    // ******************
    
    /**
     * @since 1.5
     * @see org.openmrs.OpenmrsObject.getId
     */
    override var id: Int?
        get() = programWorkflowId
        set(id) {
            programWorkflowId = id
        }
    
    /**
     * Gets the number of states which are not retired
     * 
     * @return the total number of non retired states
     * @since 1.9
     */
    open val nonRetiredStateCount: Int
        get() = getStates(false).size
}

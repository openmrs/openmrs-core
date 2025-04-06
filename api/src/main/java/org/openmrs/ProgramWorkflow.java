/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.hibernate.envers.Audited;
import org.openmrs.util.NaturalStrings;

/**
 * ProgramWorkflow
 */
@Audited
public class ProgramWorkflow extends BaseChangeableOpenmrsMetadata {
    
    private static final long serialVersionUID = 1L;

    // ******************
    // Properties
    // ******************
    
    private Integer programWorkflowId;
    private Program program;
    private Concept concept;
    private Set<ProgramWorkflowState> states = new HashSet<>();

    // ******************
    // Constructors
    // ******************
    
    /** Default Constructor */
    public ProgramWorkflow() {}

    /** Constructor with id */
    public ProgramWorkflow(Integer programWorkflowId) {
        setProgramWorkflowId(programWorkflowId);
    }

    // ******************
    // Instance methods
    // ******************

    /**
     * Adds a new {@link ProgramWorkflowState} to this ProgramWorkflow
     * 
     * @param state the {@link ProgramWorkflowState} to add
     */
    public void addState(ProgramWorkflowState state) {
        state.setProgramWorkflow(this);
        states.add(state);
    }

    /**
     * Removes a {@link ProgramWorkflowState} from this ProgramWorkflow
     * 
     * @param state the {@link ProgramWorkflowState} to remove
     */
    public void removeState(ProgramWorkflowState state) {
        if (states.contains(state)) {
            states.remove(state);
            state.setProgramWorkflow(null);
        }
    }

    /**
     * Retires a {@link ProgramWorkflowState}
     * 
     * @param state the {@link ProgramWorkflowState} to retire
     */
    public void retireState(ProgramWorkflowState state) {
        state.setRetired(true);
    }

    /**
     * Returns a {@link ProgramWorkflowState} by primary key id
     * 
     * @param programWorkflowStateId the primary key {@link Integer} id
     * @return the {@link ProgramWorkflowState} or null if not found
     */
    public ProgramWorkflowState getState(Integer programWorkflowStateId) {
        return states.stream()
                     .filter(s -> s.getProgramWorkflowStateId().equals(programWorkflowStateId))
                     .findFirst()
                     .orElse(null);
    }

    /**
     * Returns a {@link ProgramWorkflowState} by Concept
     * 
     * @param concept the Concept to match
     * @return the {@link ProgramWorkflowState} or null if not found
     */
    public ProgramWorkflowState getState(Concept concept) {
        return states.stream()
                     .filter(s -> s.getConcept().equals(concept))
                     .findFirst()
                     .orElse(null);
    }

    /**
     * Returns a {@link ProgramWorkflowState} by Concept name in any {@link Locale}
     * 
     * @param name the Concept name to match
     * @return the {@link ProgramWorkflowState} or null if not found
     */
    public ProgramWorkflowState getState(String name) {
        return states.stream()
                     .filter(s -> s.getConcept().isNamed(name))
                     .findFirst()
                     .orElse(null);
    }

    /**
     * Returns a Set&lt;{@link ProgramWorkflowState}&gt; including all non-retired ProgramWorkflowStates
     * and all retired if <code>includeRetired</code> is true
     * 
     * @param includeRetired if false, returns only non-retired {@link ProgramWorkflowState}
     * @return Set&lt;ProgramWorkflowState&gt; - matching states
     */
    public Set<ProgramWorkflowState> getStates(boolean includeRetired) {
        Set<ProgramWorkflowState> result = new HashSet<>();
        for (ProgramWorkflowState s : states) {
            if (includeRetired || !s.getRetired()) {
                result.add(s);
            }
        }
        return result;
    }

    /**
     * Returns a Set&lt;{@link ProgramWorkflowState}&gt; sorted by {@link ConceptName}
     * 
     * @return Set&lt;ProgramWorkflowState&gt; - sorted states
     */
    public Set<ProgramWorkflowState> getSortedStates() {
        Comparator<String> naturalComparator = NaturalStrings.getNaturalComparator();
        
        Comparator<ProgramWorkflowState> stateComparator = Comparator.comparing(
            s -> s.getConcept().getName().getName(),
            naturalComparator
        );

        Set<ProgramWorkflowState> sorted = new TreeSet<>(stateComparator);
        sorted.addAll(states);
        return sorted;
    }

    /**
     * Returns a List&lt;{@link ProgramWorkflowState}&gt; of possible next states for the given {@link PatientProgram}
     * 
     * @param patientProgram the PatientProgram to check
     * @return List&lt;ProgramWorkflowState&gt; - possible next states
     */
    public List<ProgramWorkflowState> getPossibleNextStates(PatientProgram patientProgram) {
        List<ProgramWorkflowState> result = new ArrayList<>();
        PatientState currentState = patientProgram.getCurrentState(this);
        
        for (ProgramWorkflowState state : getSortedStates()) {
            if (isLegalTransition(currentState == null ? null : currentState.getState(), state)) {
                result.add(state);
            }
        }
        return result;
    }

    /**
     * Checks if transitioning from <code>fromState</code> to <code>toState</code> is allowable.
     * 
     * @param fromState the state to transition from
     * @param toState the state to transition to
     * @return true if the transition is allowable
     */
    public boolean isLegalTransition(ProgramWorkflowState fromState, ProgramWorkflowState toState) {
        // If there's no current state, move into an initial state
        if (fromState == null) {
            return toState.getInitial();
        }

        // Prevent moving into the same state
        return !fromState.equals(toState);
    }

    /** @see Object#toString() */
    @Override
    public String toString() {
        return "ProgramWorkflow(id=" + getProgramWorkflowId() + ")";
    }

    // ******************
    // Property Access
    // ******************

    public Set<ProgramWorkflowState> getStates() {
        return states;
    }

    public void setStates(Set<ProgramWorkflowState> states) {
        this.states = states;
    }

    public Concept getConcept() {
        return concept;
    }

    public void setConcept(Concept concept) {
        this.concept = concept;
    }

    public Program getProgram() {
        return program;
    }

    public void setProgram(Program program) {
        this.program = program;
    }

    public Integer getProgramWorkflowId() {
        return programWorkflowId;
    }

    public void setProgramWorkflowId(Integer programWorkflowId) {
        this.programWorkflowId = programWorkflowId;
    }

    /**
     * @since 1.5
     * @see org.openmrs.OpenmrsObject#getId()
     */
    @Override
    public Integer getId() {
        return getProgramWorkflowId();
    }

    /**
     * @since 1.5
     * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
     */
    @Override
    public void setId(Integer id) {
        setProgramWorkflowId(id);
    }

    /**
     * Gets the number of non-retired states
     * 
     * @return the total number of non-retired states
     * @since 1.9
     */
    public int getNonRetiredStateCount() {
        return getStates(false).size();
    }
}

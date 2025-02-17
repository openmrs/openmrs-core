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
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;

import org.hibernate.envers.Audited;
import org.openmrs.util.NaturalStrings;

/**
 * ProgramWorkflow
 */
@Audited
@Entity
@Table(name = "program_workflow")
public class ProgramWorkflow extends BaseChangeableOpenmrsMetadata {
	
	private static final long serialVersionUID = 1L;
	
	// ******************
	// Properties
	// *****************
	@Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "program_workflow_program_workflow_id_seq")
    @Column(name = "program_workflow_id")
	private Integer programWorkflowId;
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "program_id", nullable = false)
	private Program program;
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "concept_id", nullable = false)
	private Concept concept;
	@OneToMany(mappedBy = "programWorkflow", cascade = CascadeType.ALL, orphanRemoval = true ,fetch = FetchType.EAGER )
    @OrderBy("dateCreated ASC")
	private Set<ProgramWorkflowState> states = new HashSet<>();
	
	
	

	// ******************
	// Constructors
	// ******************
	
	/** Default Constructor */
	public ProgramWorkflow() {
	}
	
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
	 * @param state - the {@link ProgramWorkflowState} to add
	 */
	public void addState(ProgramWorkflowState state) {
		state.setProgramWorkflow(this);
		getStates().add(state);
	}
	
	/**
	 * Removes a {@link ProgramWorkflowState} from this ProgramWorkflow
	 * 
	 * @param state - the {@link ProgramWorkflowState} to remove
	 */
	public void removeState(ProgramWorkflowState state) {
		if (getStates().contains(state)) {
			getStates().remove(state);
			state.setProgramWorkflow(null);
		}
	}
	
	/**
	 * Retires a {@link ProgramWorkflowState}
	 * 
	 * @param state - the {@link ProgramWorkflowState} to retire
	 */
	public void retireState(ProgramWorkflowState state) {
		state.setRetired(true);
	}
	
	/**
	 * Returns a {@link ProgramWorkflowState} whose primary key id matches the input parameter
	 * 
	 * @param programWorkflowStateId the primary key {@link Integer} id to match
	 * @return a {@link ProgramWorkflowState} whose identifier matches the passed
	 *         <code>programWorkflowStateId</code>
	 */
	public ProgramWorkflowState getState(Integer programWorkflowStateId) {
		for (ProgramWorkflowState s : getStates()) {
			if (s.getProgramWorkflowStateId().equals(programWorkflowStateId)) {
				return s;
			}
		}
		return null;
	}
	
	/**
	 * Returns a {@link ProgramWorkflowState} whose Concept matches the passed concept
	 * 
	 * @param concept the Concept to match
	 * @return Returns a {@link ProgramWorkflowState} whose {@link Concept} matches the passed
	 *         <code>concept</code>
	 */
	public ProgramWorkflowState getState(Concept concept) {
		for (ProgramWorkflowState s : getStates()) {
			if (s.getConcept().equals(concept)) {
				return s;
			}
		}
		return null;
	}
	
	/**
	 * Returns a {@link ProgramWorkflowState} whose Concept name matches the passed name in any
	 * {@link Locale}
	 * 
	 * @param name the Concept name to match in any {@link Locale}
	 * @return a {@link ProgramWorkflowState} whose {@link Concept} name matches the passed
	 *         <code>name</code>
	 */
	public ProgramWorkflowState getState(String name) {
		for (ProgramWorkflowState s : getStates()) {
			if (s.getConcept().isNamed(name)) {
				return s;
			}
		}
		return null;
	}
	
	/**
	 * Returns a {@link ProgramWorkflowState} whose {@link Concept} has any {@link ConceptName} that
	 * matches the given <code>name</code>
	 * 
	 * @param name the {@link ProgramWorkflowState} name, in any {@link Locale}
	 * @return a {@link ProgramWorkflowState} which has the passed <code>name</code> in any
	 *         {@link Locale}
	 */
	public ProgramWorkflowState getStateByName(String name) {
		for (ProgramWorkflowState s : getStates()) {
			if (s.getConcept().isNamed(name)) {
				return s;
			}
		}
		return null;
	}
	
	/**
	 * Returns a Set&lt;{@link ProgramWorkflowState}&gt; including all non-retired ProgramWorkflowStates
	 * and all retired ProgramWorkflowStates in this ProgramWorkflow if <code>includeRetired</code>
	 * is true
	 * 
	 * @param includeRetired - if false, returns only non-retired {@link ProgramWorkflowState}
	 *            objects in this ProgramWorkflow
	 * @return Set&lt;ProgramWorkflowState&gt; - all ProgramWorkflowStates matching input parameters
	 */
	public Set<ProgramWorkflowState> getStates(boolean includeRetired) {
		Set<ProgramWorkflowState> ret = new HashSet<>();
		for (ProgramWorkflowState s : getStates()) {
			if (includeRetired || !s.getRetired()) {
				ret.add(s);
			}
		}
		return ret;
	}
	
	/**
	 * Returns a Set&lt;{@link ProgramWorkflowState}&gt; including all ProgramWorkflowStates, sorted by
	 * {@link ConceptName}
	 * 
	 * @return Set&lt;ProgramWorkflowState&gt; - all ProgramWorkflowStates, sorted by {@link ConceptName}
	 * <strong>Should</strong> sort names containing numbers intelligently
	 */
	public Set<ProgramWorkflowState> getSortedStates() {
		final Comparator<String> naturalComparator = NaturalStrings.getNaturalComparator();
		
		Comparator<ProgramWorkflowState> stateComparator = (o1, o2) -> naturalComparator.compare(o1.getConcept().getName().getName(), o2.getConcept().getName().getName());
		
		Set<ProgramWorkflowState> sorted = new TreeSet<>(stateComparator);
		if (getStates() != null) {
			sorted.addAll(getStates());
		}
		return sorted;
	}
	
	/**
	 * Returns a List&lt;{@link ProgramWorkflowState}&gt; including all possible next
	 * ProgramWorkflowStates, for the passed {@link PatientProgram} ordered by {@link ConceptName}
	 * 
	 * @param patientProgram - The PatientProgram to check
	 * @return List&lt;ProgramWorkflowState&gt; - all possible next ProgramWorkflowStates, for the passed
	 *         {@link PatientProgram} ordered by {@link ConceptName}
	 */
	public List<ProgramWorkflowState> getPossibleNextStates(PatientProgram patientProgram) {
		List<ProgramWorkflowState> ret = new ArrayList<>();
		PatientState currentState = patientProgram.getCurrentState(this);
		for (ProgramWorkflowState st : getSortedStates()) {
			if (isLegalTransition(currentState == null ? null : currentState.getState(), st)) {
				ret.add(st);
			}
		}
		return ret;
	}
	
	/**
	 * Check whether it is allowable to transition from <code>fromState</code> to
	 * <code>toState</code>.
	 * 
	 * @param fromState {@link ProgramWorkflowState} to check transition from
	 * @param toState {@link ProgramWorkflowState} to check transition to
	 * @return boolean true if it is allowable to transition from <code>fromState</code> to
	 *         <code>toState</code>
	 */
	public boolean isLegalTransition(ProgramWorkflowState fromState, ProgramWorkflowState toState) {
		// If there's no current state then we need tom move into an initial state
		if (fromState == null) {
			return toState.getInitial();
		}
		
		// Does not allow patient to move into the same state
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
	 * Gets the number of states which are not retired
	 * 
	 * @return the total number of non retired states
	 * @since 1.9
	 */
	public int getNonRetiredStateCount() {
		return getStates(false).size();
	}
}

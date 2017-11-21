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

/**
 * ProgramWorkflowState
 */
public class ProgramWorkflowState extends BaseChangeableOpenmrsMetadata {
	
	private static final long serialVersionUID = 1L;
	
	// ******************
	// Properties
	// ******************
	
	private Integer programWorkflowStateId;
	
	private ProgramWorkflow programWorkflow;
	
	private Concept concept;
	
	private Boolean initial;
	
	private Boolean terminal;
	
	// ******************
	// Constructors
	// ******************
	
	/** Default Constructor */
	public ProgramWorkflowState() {
	}
	
	/** Constructor with id */
	public ProgramWorkflowState(Integer programWorkflowStateId) {
		setProgramWorkflowStateId(programWorkflowStateId);
	}
	
	// ******************
	// Instance methods
	// ******************
	
	/** @see Object#toString() */
	@Override
	public String toString() {
		return "State " + getConcept().getName() + " initial=" + getInitial() + " terminal=" + getTerminal();
	}
	
	// ******************
	// Property Access
	// ******************
	
	public Concept getConcept() {
		return concept;
	}
	
	public void setConcept(Concept concept) {
		this.concept = concept;
	}
	
	public Boolean getInitial() {
		return initial;
	}
	
	public void setInitial(Boolean initial) {
		this.initial = initial;
	}
	
	public Boolean getTerminal() {
		return terminal;
	}
	
	public void setTerminal(Boolean terminal) {
		this.terminal = terminal;
	}
	
	public ProgramWorkflow getProgramWorkflow() {
		return programWorkflow;
	}
	
	public void setProgramWorkflow(ProgramWorkflow programWorkflow) {
		this.programWorkflow = programWorkflow;
	}
	
	public Integer getProgramWorkflowStateId() {
		return programWorkflowStateId;
	}
	
	public void setProgramWorkflowStateId(Integer programWorkflowStateId) {
		this.programWorkflowStateId = programWorkflowStateId;
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	@Override
	public Integer getId() {
		return getProgramWorkflowStateId();
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	@Override
	public void setId(Integer id) {
		setProgramWorkflowStateId(id);
		
	}
}

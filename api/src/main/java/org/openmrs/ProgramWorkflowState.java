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
	
	private String name;
	
	private String description;
	
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
		return "State " + getName() + " initial=" + getInitial() + " terminal=" + getTerminal();
	}
	
	// ******************
	// Property Access
	// ******************
	
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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	/*
	 * Initializes {@link ProgramWorkflowState} <code>name</code> and <code>description</code> with {@link Concept} properties.
	 * @param concept the {@link Concept} used to initialize the {@link ProgramWorkflowState}
	 */
	public void initializeWorkflowStateWithConcept(Concept concept) {
		this.name = concept.getName().getName();
		if (concept.getDescription() != null) {
			this.description = concept.getDescription().getDescription();
		}
		
	}
}

/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs;

import java.io.Serializable;
import java.util.Date;

/**
 * ProgramWorkflowState
 */
public class ProgramWorkflowState implements Serializable {

	private static final long serialVersionUID = 1L;
	
	// ******************
	// Properties
	// ******************
	
	private Integer programWorkflowStateId;
	private ProgramWorkflow programWorkflow;
	private Concept concept;
	private Boolean initial;
	private Boolean terminal;
	private User creator; 
	private Date dateCreated; 
	private User changedBy;
	private Date dateChanged;
	private Boolean retired = false;
	
	// ******************
	// Constructors
	// ******************
	
	/** Default Constructor */
	public ProgramWorkflowState() { }

	/** Constructor with id */
	public ProgramWorkflowState(Integer programWorkflowStateId) {
		setProgramWorkflowStateId(programWorkflowStateId);
	}

	// ******************
	// Instance methods
	// ******************
	
	/** @see Object#equals(Object) */
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof ProgramWorkflowState) {
			ProgramWorkflowState p = (ProgramWorkflowState)obj;
			if (this.getProgramWorkflowStateId() == null) {
				return p.getProgramWorkflowStateId() == null;
			}
			return (this.getProgramWorkflowStateId().equals(p.getProgramWorkflowStateId()));
		}
		return false;
	}
	
	/** @see Object#toString() */
	public String toString() {
		return("State " + getConcept().getName() + " initial=" + getInitial() + " terminal=" + getTerminal());
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

	public User getCreator() {
		return creator;
	}

	public void setCreator(User creator) {
		this.creator = creator;
	}

	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
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

    public Boolean getRetired() {
    	return retired;
	}

    public Boolean isRetired() {
    	return getRetired();
	}

    public void setRetired(Boolean retired) {
    	this.retired = retired;
	}

    public User getChangedBy() {
    	return changedBy;
	}

    public void setChangedBy(User changedBy) {
    	this.changedBy = changedBy;
	}

    public Date getDateChanged() {
    	return dateChanged;
	}

    public void setDateChanged(Date dateChanged) {
    	this.dateChanged = dateChanged;
		}
}

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

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

/**
 * Program
 */
@Root
public class Program extends BaseOpenmrsMetadata implements java.io.Serializable {
	
	public static final long serialVersionUID = 3214567L;
	
	// ******************
	// Properties
	// ******************
	
	private Integer programId;
	
	private Concept concept;
	
	private Set<ProgramWorkflow> allWorkflows = new HashSet<ProgramWorkflow>();
	
	// ******************
	// Constructors
	// ******************
	
	/** Default Constructor */
	public Program() {
	}
	
	/** Constructor with id */
	public Program(Integer programId) {
		setProgramId(programId);
	}
	
	// ******************
	// Instance methods
	// ******************
	
	/**
	 * Adds a new {@link ProgramWorkflow} to this Program
	 * 
	 * @param workflow - the {@link ProgramWorkflow} to add
	 */
	public void addWorkflow(ProgramWorkflow workflow) {
		workflow.setProgram(this);
		getAllWorkflows().add(workflow);
	}
	
	/**
	 * Removes a {@link ProgramWorkflow} from this Program
	 * 
	 * @param workflow - the {@link ProgramWorkflow} to remove
	 */
	public void removeWorkflow(ProgramWorkflow workflow) {
		if (getAllWorkflows().contains(workflow)) {
			getAllWorkflows().remove(workflow);
			workflow.setProgram(null);
		}
	}
	
	/**
	 * Retires a {@link ProgramWorkflow}
	 * 
	 * @param workflow - the {@link ProgramWorkflow} to retire
	 */
	public void retireWorkflow(ProgramWorkflow workflow) {
		workflow.setRetired(true);
	}
	
	/**
	 * Returns a {@link ProgramWorkflow} whose {@link Concept} has any {@link ConceptName} that
	 * matches the given <code>name</name>
	 * 
	 * @param name the {@link ProgramWorkflow} name, in any {@link Locale}
	 * @return a {@link ProgramWorkflow} which has the passed <code>name</code> in any
	 *         {@link Locale}
	 */
	public ProgramWorkflow getWorkflowByName(String name) {
		for (ProgramWorkflow pw : getAllWorkflows()) {
			if (pw.getConcept().isNamed(name)) {
				return pw;
			}
		}
		return null;
	}
	
	/** @see Object#equals(Object) */
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof Program) {
			Program p = (Program) obj;
			if (this.getProgramId() != null) {
				return (this.getProgramId().equals(p.getProgramId()));
			}
		}
		return this == obj;
	}
	
	/** @see Object#toString() */
	public String toString() {
		return "Program(id=" + getProgramId() + ", concept=" + getConcept() + ", workflows=" + getWorkflows() + ")";
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
	
	@Attribute(required = true)
	public Integer getProgramId() {
		return programId;
	}
	
	@Attribute(required = true)
	public void setProgramId(Integer programId) {
		this.programId = programId;
	}
	
	/**
	 * Get only the non-retired workflows
	 * 
	 * @return Returns a Set<ProgramWorkflow> of all non-retired workflows
	 */
	public Set<ProgramWorkflow> getWorkflows() {
		Set<ProgramWorkflow> ret = new HashSet<ProgramWorkflow>();
		for (ProgramWorkflow workflow : getAllWorkflows()) {
			if (workflow.isRetired() == false)
				ret.add(workflow);
		}
		return ret;
	}
	
	/**
	 * Get the workflow with the specified ID
	 * 
	 * @return the workflow matching the given id or null if none found
	 * @since 1.6
	 */
	public ProgramWorkflow getWorkflow(Integer programWorkflowId) {
		if (getWorkflows() != null) {
			for (ProgramWorkflow wf : getWorkflows())
				if (wf.getId().equals(programWorkflowId))
					return wf;
		}
		return null;
	}
	
	/**
	 * Get all workflows...including the retired ones
	 * 
	 * @return Returns a Set<ProgramWorkflow> of all workflows
	 */
	public Set<ProgramWorkflow> getAllWorkflows() {
		if (allWorkflows == null) {
			allWorkflows = new HashSet<ProgramWorkflow>();
		}
		return allWorkflows;
	}
	
	public void setAllWorkflows(Set<ProgramWorkflow> allWorkflows) {
		this.allWorkflows = allWorkflows;
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	public Integer getId() {
		
		return getProgramId();
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	public void setId(Integer id) {
		setProgramId(id);
		
	}
}

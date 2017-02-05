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

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.openmrs.annotation.AllowDirectAccess;

/**
 * Program
 */
public class Program extends BaseOpenmrsMetadata implements java.io.Serializable {

	public static final long serialVersionUID = 3214567L;

	// ******************
	// Properties
	// ******************

	private Integer programId;
	@Deprecated
	private Concept concept;
	private String name;
	private String description;

	/**
	 * Represents the possible outcomes for this program. The concept should
	 * have answers or a memberSet.
	 */
	private Concept outcomesConcept;

	@AllowDirectAccess
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

	/**
	 * Constructor with name
	 *
	 * @since 1.10
	 */
	public Program(String name) {
		setName(name);
	}

	// ******************
	// Instance methods
	// ******************

	/**
	 * Adds a new {@link ProgramWorkflow} to this Program
	 *
	 * @param workflow
	 *            - the {@link ProgramWorkflow} to add
	 */
	public void addWorkflow(ProgramWorkflow workflow) {
		workflow.setProgram(this);
		getAllWorkflows().add(workflow);
	}

	/**
	 * Removes a {@link ProgramWorkflow} from this Program
	 *
	 * @param workflow
	 *            - the {@link ProgramWorkflow} to remove
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
	 * @param workflow
	 *            - the {@link ProgramWorkflow} to retire
	 */
	public void retireWorkflow(ProgramWorkflow workflow) {
		workflow.setRetired(true);
	}

	/**
	 * Returns a {@link ProgramWorkflow} whose {@link Concept} has any
	 * {@link ConceptName} that matches the given <code>name</code>
	 *
	 * @param name
	 *            the {@link ProgramWorkflow} name, in any {@link Locale}
	 * @return a {@link ProgramWorkflow} which has the passed <code>name</code>
	 *         in any {@link Locale}
	 */
	public ProgramWorkflow getWorkflowByName(String name) {
		for (ProgramWorkflow pw : getAllWorkflows()) {
			if (pw.getConcept().isNamed(name)) {
				return pw;
			}
		}
		return null;
	}

	/**
	 * @see Object#toString()
	 */
	public String toString() {
		return "Program(id=" + getProgramId() + ", concept=" + getConcept() + ", workflows=" + getWorkflows() + ")";
	}

	// ******************
	// Property Access
	// ******************

	/**
	 * @deprecated since 2.1.0 replaced with {@link getName() and
	 *             getDescription()}
	 */
	@Deprecated
	public Concept getConcept() {
		return concept;
	}

	/**
	 * @deprecated since 2.1.0 replaced with {@link setName() and
	 *             setDescription()}
	 */
	@Deprecated
	public void setConcept(Concept concept) {
		this.concept = concept;
	}

	/**
	 * since 2.1.0
	 */
	public String getName() {
		return name;
	}

	/**
	 * since 2.1.0
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * since 2.1.0
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * since 2.1.0
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	public Concept getOutcomesConcept() {
		return outcomesConcept;
	}

	public void setOutcomesConcept(Concept concept) {
		this.outcomesConcept = concept;
	}

	public Integer getProgramId() {
		return programId;
	}

	public void setProgramId(Integer programId) {
		this.programId = programId;
	}

	/**
	 * Get only the non-retired workflows
	 *
	 * @return Returns a Set&lt;ProgramWorkflow&gt; of all non-retired workflows
	 */
	public Set<ProgramWorkflow> getWorkflows() {
		Set<ProgramWorkflow> ret = new HashSet<ProgramWorkflow>();
		for (ProgramWorkflow workflow : getAllWorkflows()) {
			if (!workflow.isRetired()) {
				ret.add(workflow);
			}
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
			for (ProgramWorkflow wf : getWorkflows()) {
				if (wf.getId().equals(programWorkflowId)) {
					return wf;
				}
			}
		}
		return null;
	}

	/**
	 * Get all workflows...including the retired ones
	 *
	 * @return Returns a Set&lt;ProgramWorkflow&gt; of all workflows
	 */
	public Set<ProgramWorkflow> getAllWorkflows() {
		if (allWorkflows == null) {
			allWorkflows = new HashSet<ProgramWorkflow>();
		}
		return allWorkflows;
	}

	public void setAllWorkflows(Set<ProgramWorkflow> allWorkflows) {
		this.allWorkflows = new HashSet<ProgramWorkflow>(allWorkflows);
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

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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * ConceptStateConversion
 */
public class ConceptStateConversion extends BaseOpenmrsObject implements java.io.Serializable {
	
	public static final long serialVersionUID = 3214511L;
	
	protected final Log log = LogFactory.getLog(ConceptStateConversion.class);
	
	// ******************
	// Properties
	// ******************
	
	private Integer conceptStateConversionId;
	
	private Concept concept;
	
	private ProgramWorkflow programWorkflow;
	
	private ProgramWorkflowState programWorkflowState;
	
	// ******************
	// Constructors
	// ******************
	
	/** Default Constructor */
	public ConceptStateConversion() {
	}
	
	/** Constructor with id */
	public ConceptStateConversion(Integer conceptStateConversionId) {
		setConceptStateConversionId(conceptStateConversionId);
	}
	
	// ******************
	// Instance methods
	// ******************
	
	/** @see Object#toString() */
	public String toString() {
		return "ConceptStateConversion: Concept[" + concept + "] results in State [" + programWorkflowState
		        + "] for workflow [" + programWorkflow + "]";
	}
	
	// ******************
	// Property Access
	// ******************
	
	/**
	 * @return Returns the concept.
	 */
	public Concept getConcept() {
		return concept;
	}
	
	/**
	 * @param concept The concept to set.
	 */
	public void setConcept(Concept concept) {
		this.concept = concept;
	}
	
	/**
	 * @return Returns the conceptStateConversionId.
	 */
	public Integer getConceptStateConversionId() {
		return conceptStateConversionId;
	}
	
	/**
	 * @param conceptStateConversionId The conceptStateConversionId to set.
	 */
	public void setConceptStateConversionId(Integer conceptStateConversionId) {
		this.conceptStateConversionId = conceptStateConversionId;
	}
	
	/**
	 * @return Returns the programWorkflow.
	 */
	public ProgramWorkflow getProgramWorkflow() {
		return programWorkflow;
	}
	
	/**
	 * @param programWorkflow The programWorkflow to set.
	 */
	public void setProgramWorkflow(ProgramWorkflow programWorkflow) {
		this.programWorkflow = programWorkflow;
	}
	
	/**
	 * @return Returns the programWorkflowState.
	 */
	public ProgramWorkflowState getProgramWorkflowState() {
		return programWorkflowState;
	}
	
	/**
	 * @param programWorkflowState The programWorkflowState to set.
	 */
	public void setProgramWorkflowState(ProgramWorkflowState programWorkflowState) {
		this.programWorkflowState = programWorkflowState;
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	public Integer getId() {
		return getConceptStateConversionId();
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	public void setId(Integer id) {
		setConceptStateConversionId(id);
	}
}

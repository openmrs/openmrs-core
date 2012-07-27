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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * ConceptStateConversion
 */
public class ConceptStateConversion extends BaseOpenmrsObject implements java.io.Serializable {
	
	public static final long serialVersionUID = 3214511L;
	
	private static final Log log = LogFactory.getLog(ConceptStateConversion.class);
	
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
	
	/** @see Object#equals(Object) */
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof ConceptStateConversion) {
			ConceptStateConversion p = (ConceptStateConversion) obj;
			if (this.getConceptStateConversionId() != null) {
				return (this.getConceptStateConversionId().equals(p.getConceptStateConversionId()));
			}
		}
		return this == obj;
	}
	
	/** @see Object#toString() */
	public String toString() {
		return ("ConceptStateConversion: Concept[" + concept + "] results in State [" + programWorkflowState
		        + "] for workflow [" + programWorkflow + "]");
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

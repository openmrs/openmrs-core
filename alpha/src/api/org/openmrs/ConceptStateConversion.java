package org.openmrs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ConceptStateConversion {
	
	private Log log = LogFactory.getLog(this.getClass());
	
	private Integer conceptStateConversionId;
	private Concept concept;
	private ProgramWorkflow programWorkflow;
	private ProgramWorkflowState programWorkflowState;
	
	public ConceptStateConversion() { }

	public String toString() {
		return("ConceptStateConversion: Concept[" + concept + "] results in State [" + programWorkflowState + "] for workflow [" + programWorkflow + "]");
	}
	
	public boolean equals(Object o) {
		if (o instanceof ConceptStateConversion) {
			ConceptStateConversion other = (ConceptStateConversion) o;
			return getConceptStateConversionId() != null && other.getConceptStateConversionId() != null && getConceptStateConversionId().equals(other.getConceptStateConversionId());
		}
		return false;
	}

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
	
}

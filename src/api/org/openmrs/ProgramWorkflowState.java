package org.openmrs;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ProgramWorkflowState {

	private Log log = LogFactory.getLog(this.getClass());
	
	private Integer programWorkflowStateId;
	private ProgramWorkflow programWorkflow;
	private Concept concept;
	private Boolean initial;
	private Boolean terminal;
	private User creator; 
	private Date dateCreated; 
	private Boolean voided = false; 
	private User voidedBy;
	private Date dateVoided; 
	private String voidReason;
	
	public ProgramWorkflowState() { }

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

	public Date getDateVoided() {
		return dateVoided;
	}

	public void setDateVoided(Date dateVoided) {
		this.dateVoided = dateVoided;
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

	public Boolean getVoided() {
		return voided;
	}

	public void setVoided(Boolean voided) {
		this.voided = voided;
	}

	public User getVoidedBy() {
		return voidedBy;
	}

	public void setVoidedBy(User voidedBy) {
		this.voidedBy = voidedBy;
	}

	public String getVoidReason() {
		return voidReason;
	}

	public void setVoidReason(String voidReason) {
		this.voidReason = voidReason;
	}

	public String toString() {
		return("State " + getConcept().getName(null, false) + " initial=" + getInitial() + " terminal=" + getTerminal());
	}
	
	public boolean equals(Object o) {
		if (o instanceof ProgramWorkflowState) {
			ProgramWorkflowState other = (ProgramWorkflowState) o;
			return getProgramWorkflowStateId() != null && other.getProgramWorkflowStateId() != null && getProgramWorkflowStateId().equals(other.getProgramWorkflowStateId());
		}
		return false;
	}
	
}

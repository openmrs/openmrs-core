package org.openmrs;

import java.util.Date;
import java.util.Set;

public class ProgramWorkflow {

	private Integer programWorkflowId;
	private Program program;
	private Concept concept;
	private User creator; 
	private Date dateCreated;
	private Boolean voided = false; 
	private User voidedBy;
	private Date dateVoided; 
	private String voidReason;
	private Set<ProgramWorkflowState> states;

	public ProgramWorkflow() { }
	
	public Date getDateVoided() {
		return dateVoided;
	}

	public void setDateVoided(Date dateVoided) {
		this.dateVoided = dateVoided;
	}

	public Set<ProgramWorkflowState> getStates() {
		return states;
	}

	public void setStates(Set<ProgramWorkflowState> states) {
		this.states = states;
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

	public Concept getConcept() {
		return concept;
	}

	public void setConcept(Concept concept) {
		this.concept = concept;
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
	
	public void addState(ProgramWorkflowState s) {
		s.setProgramWorkflow(this);
		states.add(s);
	}
	
	public String toString() {
		return "Workflow_" + programWorkflowId; 
	}
	
}

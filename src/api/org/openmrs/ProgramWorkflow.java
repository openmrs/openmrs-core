package org.openmrs;

import java.util.Date;

public class ProgramWorkflow {

	private Integer programWorkflowId;
	private Program program;
	private Concept concept;
	private User creator; 
	private Date dateCreated; 
	
	public ProgramWorkflow() { }

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
	
	public String toString() {
		return "Workflow_" + programWorkflowId; 
	}
	
}

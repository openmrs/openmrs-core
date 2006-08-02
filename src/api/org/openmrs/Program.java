package org.openmrs;

import java.util.Date;
import java.util.Set;

public class Program {

	private Integer programId;
	private Concept concept;
	private User creator; 
	private Date dateCreated; 
	private Set<ProgramWorkflow> workflows;
	
	public Program() { }

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

	public Integer getProgramId() {
		return programId;
	}

	public void setProgramId(Integer programId) {
		this.programId = programId;
	}

	public String toString() {
		return "Program(id=" + programId + ", concept=" + concept + ", workflows=" + workflows + ")";
	}

	public Set<ProgramWorkflow> getWorkflows() {
		return workflows;
	}

	public void setWorkflows(Set<ProgramWorkflow> workflows) {
		this.workflows = workflows;
	}
	
}

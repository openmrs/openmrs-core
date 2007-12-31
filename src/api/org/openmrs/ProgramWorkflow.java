package org.openmrs;

import java.util.Comparator;
import java.util.Date;
import java.util.Set;
import java.util.TreeSet;

import org.openmrs.api.context.Context;

public class ProgramWorkflow implements java.io.Serializable {

	private static final long serialVersionUID = 1L;
	
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
	
	public boolean equals(Object obj) {
		if (obj instanceof ProgramWorkflow) {
			ProgramWorkflow wf = (ProgramWorkflow)obj;
			return (this.getProgramWorkflowId().equals(wf.getProgramWorkflowId()));
		}
		return false;
	}
	
	/**
	 * @return A state that has the given name in any locale, or null if none does
	 */
	public ProgramWorkflowState getStateByName(String name) {
		for (ProgramWorkflowState state : getStates())
			if (state.getConcept().isNamed(name))
				return state;
		return null;
	}
	
	public Date getDateVoided() {
		return dateVoided;
	}

	public void setDateVoided(Date dateVoided) {
		this.dateVoided = dateVoided;
	}

	public Set<ProgramWorkflowState> getStates() {
		return states;
	}

	public Set<ProgramWorkflowState> getSortedStates() {
		TreeSet<ProgramWorkflowState> sorted = new TreeSet<ProgramWorkflowState>(new StateAlphaComparator());
		
		if ( this.getStates() != null ) {
			sorted.addAll(this.getStates());
		}

		return sorted;
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
	
	private class StateAlphaComparator implements Comparator<ProgramWorkflowState> {

		public int compare(ProgramWorkflowState s1, ProgramWorkflowState s2) {
			if ( s1 != null && s2 != null ) {
				String name1 = s1.getConcept().getName(Context.getLocale()).getName();
				String name2 = s2.getConcept().getName(Context.getLocale()).getName();
				if ( name1 != null && name2 != null ) {
					return name1.compareTo(name2);
				}
			}
			return 0;
		}
		
	}
}

package org.openmrs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openmrs.util.OpenmrsUtil;

public class PatientProgram {

	private Integer patientProgramId;
	private Patient patient;
	private Program program;
	private Date dateEnrolled;
	private Date dateCompleted;
	
	private Set<PatientState> states;
	
	private User creator; 
	private Date dateCreated; 
	private User changedBy;
	private Date dateChanged;
	private Boolean voided = false;
	private User voidedBy;
	private Date dateVoided;
	private String voidReason;
	
	public PatientProgram() {
		states = new HashSet<PatientState>();
	}

	public User getCreator() {
		return creator;
	}

	public void setCreator(User creator) {
		this.creator = creator;
	}

	public Date getDateCompleted() {
		return dateCompleted;
	}

	public void setDateCompleted(Date dateCompleted) {
		this.dateCompleted = dateCompleted;
	}

	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	public Date getDateEnrolled() {
		return dateEnrolled;
	}

	public void setDateEnrolled(Date dateEnrolled) {
		this.dateEnrolled = dateEnrolled;
	}

	public Patient getPatient() {
		return patient;
	}

	public void setPatient(Patient patient) {
		this.patient = patient;
	}

	public Integer getPatientProgramId() {
		return patientProgramId;
	}

	public void setPatientProgramId(Integer patientProgramId) {
		this.patientProgramId = patientProgramId;
	}

	public Program getProgram() {
		return program;
	}

	public void setProgram(Program program) {
		this.program = program;
	}

	public User getChangedBy() {
		return changedBy;
	}

	public void setChangedBy(User changedBy) {
		this.changedBy = changedBy;
	}

	public Date getDateChanged() {
		return dateChanged;
	}

	public void setDateChanged(Date dateChanged) {
		this.dateChanged = dateChanged;
	}

	public Set<PatientState> getStates() {
		return states;
	}

	public void setStates(Set<PatientState> states) {
		this.states = states;
	}
	
	public Date getDateVoided() {
		return dateVoided;
	}

	public void setDateVoided(Date dateVoided) {
		this.dateVoided = dateVoided;
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

	public boolean getActive(Date onDate) {
		if (onDate == null)
			onDate = new Date();
		return (dateEnrolled == null || OpenmrsUtil.compare(dateEnrolled, onDate) <= 0) && (dateCompleted == null || OpenmrsUtil.compare(dateCompleted, onDate) > 0); 
	}
	
	public boolean getActive() {
		return getActive(null);
	}
	
	public PatientState getCurrentState() {
		PatientState ret = null;
		
		Set<PatientState> states = this.getStates();
		if ( states != null ) {
			for ( PatientState state : states ) {
				Date now = new Date();
				
				boolean isEnded = (state.getEndDate() != null);
				if (isEnded) isEnded = state.getEndDate().before(now); 
				
				if ( state.getActive() && !state.getVoided() && state.getStartDate().before(now) && !isEnded ) {
					ret = state;
				}
			}
		}

		return ret;
	}
	
	
//	<c:forEach var="state" items="${program.states}">
	//<c:if test="${!state.voided && state.state.programWorkflow.programWorkflowId == workflow.programWorkflowId && state.active}">

	
	public List<PatientState> statesInWorkflow(ProgramWorkflow wf, boolean includeVoided) {
		List<PatientState> ret = new ArrayList<PatientState>();
		for (PatientState st : getStates()) {
			if (st.getState().getProgramWorkflow().equals(wf) && (includeVoided || !st.getVoided()))
				ret.add(st);
		}
		Collections.sort(ret, new Comparator<PatientState>() {
				public int compare(PatientState left, PatientState right) {
					return OpenmrsUtil.compareWithNullAsEarliest(left.getStartDate(), right.getStartDate());
				}
			});
		return ret;
	}
	
}

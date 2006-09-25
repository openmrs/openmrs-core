package org.openmrs;

import java.util.Date;

import org.openmrs.util.OpenmrsUtil;

public class PatientState {

	private Integer patientStateId;
	// private Program program;
	// private ProgramWorkflow programWorkflow;
	private PatientProgram patientProgram;
	private ProgramWorkflowState state;
	private Date startDate;
	private Date endDate;
	
	private User creator; 
	private Date dateCreated;
	private User changedBy;
	private Date dateChanged;
	private Boolean voided = false; 
	private User voidedBy;
	private Date dateVoided; 
	private String voidReason;
	
	public PatientState() { }

	public PatientProgram getPatientProgram() {
		return patientProgram;
	}

	public void setPatientProgram(PatientProgram patientProgram) {
		this.patientProgram = patientProgram;
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

	public Integer getPatientStateId() {
		return patientStateId;
	}

	public void setPatientStateId(Integer patientStatusId) {
		this.patientStateId = patientStatusId;
	}

	public ProgramWorkflowState getState() {
		return state;
	}

	public void setState(ProgramWorkflowState state) {
		this.state = state;
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

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	
	public boolean getActive() {
		return getActive(null);
	}
	
	public boolean getActive(Date onDate) {
		if (onDate == null)
			onDate = new Date();
		return (startDate == null || OpenmrsUtil.compare(startDate, onDate) <= 0) && (endDate == null || OpenmrsUtil.compare(endDate, onDate) > 0);
	}
	
}

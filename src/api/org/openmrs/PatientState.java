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

import java.util.Date;

import org.openmrs.util.OpenmrsUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * PatientState
 */
public class PatientState implements java.io.Serializable {
	
	public static final long serialVersionUID = 0L;
	protected final Log log = LogFactory.getLog(getClass());
	
	// ******************
	// Properties
	// ******************

	private Integer patientStateId;
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
	
	// ******************
	// Constructors
	// ******************
	
	/** Default Constructor */
	public PatientState() { }
	
	/** Constructor with id */
	public PatientState(Integer patientStateId) {
		setPatientStateId(patientStateId);
	}
	
	/**
	 * Does a shallow copy of this PatientState. Does NOT copy patientStateId
	 *
	 * @return a copy of this PatientState
	 */
	public PatientState copy() {
		return copyHelper(new PatientState());
	}
	
	/**
	 * The purpose of this method is to allow subclasses of PatientState to delegate a portion of
	 * their copy() method back to the superclass, in case the base class implementation changes. 
	 * 
	 * @param target a PatientState that will have the state of <code>this</code> copied into it
	 * @return the PatientState that was passed in, with state copied into it
	 */
	protected PatientState copyHelper(PatientState target) {
		target.setPatientProgram(this.getPatientProgram());
		target.setState(this.getState());
		target.setStartDate(this.getStartDate());
		target.setEndDate(this.getEndDate());
		target.setCreator(this.getCreator());
		target.setDateCreated(this.getDateCreated());
		target.setChangedBy(this.getChangedBy());
		target.setDateChanged(this.getDateChanged());
		target.setVoided(this.getVoided());
		target.setVoidedBy(this.getVoidedBy());
		target.setDateVoided(this.getDateVoided());
		target.setVoidReason(this.getVoidReason());
		return target;
	}
	
	// ******************
	// Instance methods
	// ******************
	
	/**
	 * Returns true if this {@link PatientState} is active as of the passed {@link Date}
	 * @param onDate - {@link Date} to check for {@link PatientState} enrollment
	 * @return boolean - true if this {@link PatientState} is active as of the passed {@link Date}
	 */
	public boolean getActive(Date onDate) {
		if (onDate == null) {
			onDate = new Date();
		}
		return !getVoided() && (startDate == null || OpenmrsUtil.compare(startDate, onDate) <= 0) && (endDate == null || OpenmrsUtil.compare(endDate, onDate) > 0);
	}
	
	/**
	 * Returns true if this {@link PatientState} is currently active
	 * @return boolean - true if this {@link PatientState} is currently active
	 */
	public boolean getActive() {
		return getActive(null);
	}
	
	/** @see Object#equals(Object) */
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof PatientState) {
			PatientState p = (PatientState)obj;
			if (this.getPatientStateId() == null) {
				return p.getPatientStateId() == null;
			}
			return (this.getPatientStateId().equals(p.getPatientStateId()));
		}
		return false;
	}

	/** @see Object#toString() */
	public String toString() {
		return "id=" + getPatientStateId() + ", patientProgram=" + getPatientProgram() + ", state=" + getState() + ", startDate=" + getStartDate() + ", endDate=" + getEndDate() + ", dateCreated=" + getDateCreated() + ", dateChanged=" + getDateChanged();
	}
	
	// ******************
	// Property Access
	// ******************

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
}

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

/**
 * PatientState
 */
public class PatientState extends BaseOpenmrsData implements java.io.Serializable, Comparable<PatientState> {
	
	public static final long serialVersionUID = 0L;
	
	// ******************
	// Properties
	// ******************
	
	private Integer patientStateId;
	
	private PatientProgram patientProgram;
	
	private ProgramWorkflowState state;
	
	private Date startDate;
	
	private Date endDate;
	
	// ******************
	// Constructors
	// ******************
	
	/** Default Constructor */
	public PatientState() {
	}
	
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
	 * 
	 * @param onDate - {@link Date} to check for {@link PatientState} enrollment
	 * @return boolean - true if this {@link PatientState} is active as of the passed {@link Date}
	 * @should return false if voided and date in range
	 * @should return false if voided and date not in range
	 * @should return true if not voided and date in range
	 * @should return false if not voided and date earlier than startDate
	 * @should return false if not voided and date later than endDate
	 * @should return true if not voided and date in range with null startDate
	 * @should return true if not voided and date in range with null endDate
	 * @should return true if not voided and both startDate and endDate nulled
	 * @should compare with current date if date null
	 */
	public boolean getActive(Date onDate) {
		if (onDate == null) {
			onDate = new Date();
		}
		return !getVoided() && (OpenmrsUtil.compareWithNullAsEarliest(startDate, onDate) <= 0)
		        && (OpenmrsUtil.compareWithNullAsLatest(endDate, onDate) > 0);
	}
	
	/**
	 * Returns true if this {@link PatientState} is currently active
	 * 
	 * @return boolean - true if this {@link PatientState} is currently active
	 */
	public boolean getActive() {
		return getActive(null);
	}
	
	/** @see Object#toString() */
	public String toString() {
		return "id=" + getPatientStateId() + ", patientProgram=" + getPatientProgram() + ", state=" + getState()
		        + ", startDate=" + getStartDate() + ", endDate=" + getEndDate() + ", dateCreated=" + getDateCreated()
		        + ", dateChanged=" + getDateChanged();
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
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	public Integer getId() {
		return getPatientStateId();
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	public void setId(Integer id) {
		setPatientStateId(id);
	}
	
	/**
	 * Compares by startDate with null as earliest and endDate with null as latest.
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 * @should return positive if startDates equal and this endDate null
	 * @should return negative if this startDate null
	 * @should pass if two states have the same start date, end date and uuid
	 * @should return positive or negative if two states have the same start date and end date but different uuids
	 */
	@Override
	public int compareTo(PatientState o) {
		int result = OpenmrsUtil.compareWithNullAsEarliest(getStartDate(), o.getStartDate());
		if (result == 0) {
			result = OpenmrsUtil.compareWithNullAsLatest(getEndDate(), o.getEndDate());
		}
		if (result == 0) {
			result = OpenmrsUtil.compareWithNullAsGreatest(getUuid(), o.getUuid());
		}
		return result;
	}
}

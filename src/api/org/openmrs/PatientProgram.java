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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openmrs.util.OpenmrsUtil;

public class PatientProgram implements java.io.Serializable {
	
	public static final long serialVersionUID = 0L;

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
		return !getVoided() && (dateEnrolled == null || OpenmrsUtil.compare(dateEnrolled, onDate) <= 0) && (dateCompleted == null || OpenmrsUtil.compare(dateCompleted, onDate) > 0); 
	}
	
	public boolean getActive() {
		return getActive(null);
	}
	
	public PatientState getCurrentState(ProgramWorkflow wf) {
		Set<PatientState> states = this.getStates();
		Date now = new Date();
		if (states != null) {
			for (PatientState state : states) {
				if ( !state.getVoided() &&
						( wf == null || state.getState().getProgramWorkflow().equals(wf) ) &&
						state.getActive(now) )
					return state;
			}
		}
		return null;
	}
	
	@Deprecated
	public PatientState getCurrentState() {
		// TODO: this isn't really right - a patient can have many current states (in different workflows)
		return getCurrentState(null);
	}


	public Set<PatientState> getCurrentStates() {
		
		Set<PatientState> ret = null;
		
		Set<PatientState> states = this.getStates();
		if ( states != null ) {
			Date now = new Date();
			for ( PatientState state : states ) {
				if (!state.getVoided() && state.getActive(now)) {
					if ( ret == null ) ret = new HashSet<PatientState>();
					ret.add(state);
				}
			}
		}
		return ret;
	}

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

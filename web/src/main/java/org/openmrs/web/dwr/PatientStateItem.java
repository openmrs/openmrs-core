/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.dwr;

import java.util.Date;

import org.openmrs.PatientState;
import org.openmrs.api.context.Context;

public class PatientStateItem {
	
	private Integer patientStateId;
	
	private Integer programWorkflowId;
	
	private String stateName;
	
	private String workflowName;
	
	private Date startDate;
	
	private Date endDate;
	
	private String creator;
	
	private Date dateCreated;
	
	public PatientStateItem() {
	}
	
	public PatientStateItem(PatientState s) {
		patientStateId = s.getPatientStateId();
		programWorkflowId = s.getState().getProgramWorkflow().getProgramWorkflowId();
		stateName = s.getState().getConcept().getName(Context.getLocale(), false).getName();
		workflowName = s.getState().getProgramWorkflow().getConcept().getName(Context.getLocale(), false).getName();
		startDate = s.getStartDate();
		endDate = s.getEndDate();
		creator = s.getCreator().getPersonName().getFullName();
		dateCreated = s.getDateCreated();
	}
	
	public Date getEndDate() {
		return endDate;
	}
	
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
	
	public Integer getPatientStateId() {
		return patientStateId;
	}
	
	public void setPatientStateId(Integer patientStateId) {
		this.patientStateId = patientStateId;
	}
	
	public Integer getProgramWorkflowId() {
		return programWorkflowId;
	}
	
	public void setProgramWorkflowId(Integer programWorkflowId) {
		this.programWorkflowId = programWorkflowId;
	}
	
	public Date getStartDate() {
		return startDate;
	}
	
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	
	public String getStateName() {
		return stateName;
	}
	
	public void setStateName(String stateName) {
		this.stateName = stateName;
	}
	
	public String getWorkflowName() {
		return workflowName;
	}
	
	public void setWorkflowName(String workflowName) {
		this.workflowName = workflowName;
	}
	
	public String getCreator() {
		return creator;
	}
	
	public void setCreator(String creator) {
		this.creator = creator;
	}
	
	public Date getDateCreated() {
		return dateCreated;
	}
	
	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}
}

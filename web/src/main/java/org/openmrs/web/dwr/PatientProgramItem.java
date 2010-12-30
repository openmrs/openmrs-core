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
package org.openmrs.web.dwr;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.openmrs.PatientProgram;
import org.openmrs.ProgramWorkflow;
import org.openmrs.api.context.Context;

public class PatientProgramItem {
	
	private Integer patientProgramId;
	
	private Integer patientId;
	
	private String name;
	
	private Date dateEnrolled;
	
	private Date dateCompleted;
	
	private LocationListItem location;
	
	private String creator;
	
	private Date dateCreated;
	
	private String changedBy;
	
	private Date dateChanged;
	
	private Map<String, Integer> workflows; // workflow name -> programWorkflowId
	
	public PatientProgramItem() {
	}
	
	public PatientProgramItem(PatientProgram p) {
		patientProgramId = p.getPatientProgramId();
		patientId = p.getPatient().getPatientId();
		dateEnrolled = p.getDateEnrolled();
		dateCompleted = p.getDateCompleted();
		location = new LocationListItem(p.getLocation());
		creator = p.getCreator().getPersonName().getFullName();
		dateCreated = p.getDateCreated();
		if (p.getChangedBy() != null) {
			changedBy = p.getChangedBy().getPersonName().getFullName();
			dateChanged = p.getDateChanged();
		}
		name = p.getProgram().getConcept().getName(Context.getLocale(), false).getName();
		workflows = new HashMap<String, Integer>();
		for (ProgramWorkflow wf : p.getProgram().getWorkflows()) {
			workflows.put(wf.getConcept().getName(Context.getLocale(), false).getName(), wf.getProgramWorkflowId());
		}
	}
	
	public Map<String, Integer> getWorkflows() {
		return workflows;
	}
	
	public void setWorkflows(Map<String, Integer> workflows) {
		this.workflows = workflows;
	}
	
	public Date getDateCompleted() {
		return dateCompleted;
	}
	
	public void setDateCompleted(Date dateCompleted) {
		this.dateCompleted = dateCompleted;
	}
	
	public Date getDateEnrolled() {
		return dateEnrolled;
	}
	
	public void setDateEnrolled(Date dateEnrolled) {
		this.dateEnrolled = dateEnrolled;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public Integer getPatientId() {
		return patientId;
	}
	
	public void setPatientId(Integer patientId) {
		this.patientId = patientId;
	}
	
	public Integer getPatientProgramId() {
		return patientProgramId;
	}
	
	public void setPatientProgramId(Integer patientProgramId) {
		this.patientProgramId = patientProgramId;
	}
	
	static DateFormat ymdDf = new SimpleDateFormat("yyyy-MM-dd");
	
	public String getDateEnrolledAsYmd() {
		return dateEnrolled == null ? null : ymdDf.format(dateEnrolled);
	}
	
	public String getDateCompletedAsYmd() {
		return dateCompleted == null ? null : ymdDf.format(dateCompleted);
	}
	
	/**
	 * @return the creator
	 */
	public String getCreator() {
		return creator;
	}
	
	/**
	 * @param creator the creator to set
	 */
	public void setCreator(String creator) {
		this.creator = creator;
	}
	
	/**
	 * @return the dateCreated
	 */
	public Date getDateCreated() {
		return dateCreated;
	}
	
	/**
	 * @param dateCreated the dateCreated to set
	 */
	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}
	
	/**
	 * @return the changedBy
	 */
	public String getChangedBy() {
		return changedBy;
	}
	
	/**
	 * @param changedBy the changedBy to set
	 */
	public void setChangedBy(String changedBy) {
		this.changedBy = changedBy;
	}
	
	/**
	 * @return the dateChanged
	 */
	public Date getDateChanged() {
		return dateChanged;
	}
	
	/**
	 * @param dateChanged the dateChanged to set
	 */
	public void setDateChanged(Date dateChanged) {
		this.dateChanged = dateChanged;
	}
	
	/**
	 * @return the location
	 */
	public LocationListItem getLocation() {
		return location;
	}
	
	/**
	 * @param location the location to set
	 */
	public void setLocation(LocationListItem location) {
		this.location = location;
	}
}

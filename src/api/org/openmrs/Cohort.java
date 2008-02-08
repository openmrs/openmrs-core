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

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.reporting.PatientSet;

public class Cohort implements Serializable {

	public static final long serialVersionUID = 0L;
	public Log log = LogFactory.getLog(this.getClass());
	
	private Integer cohortId;
	private String name;
	private String description;
	private User creator;
	private Date dateCreated;
	private Boolean voided = false;
	private User voidedBy;
	private Date dateVoided;
	private String voidReason;
	private Set<Integer> memberIds;

	public Cohort() {
		memberIds = new HashSet<Integer>();
	}
	
	public PatientSet toPatientSet() {
		PatientSet ret = new PatientSet();
		ret.copyPatientIds(getMemberIds());
		return ret;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Cohort{").append("\n");
		sb.append(" name=" + getName()).append("\n");
		sb.append(" description=" + getDescription()).append("\n");
		sb.append(" creator=" + getCreator()).append("\n");
		sb.append(" dateCreated=" + getDateCreated()).append("\n");
		sb.append(" voided=" + getVoided()).append("\n");
		sb.append(" voidedBy=" + getVoidedBy()).append("\n");
		sb.append(" dateVoided=" + getDateVoided()).append("\n");
		sb.append(" voidReason=" + getVoidReason()).append("\n");
		sb.append(" memberIds=" + getMemberIds()).append("\n");
		if (getMemberIds() != null)
			sb.append(" memberIds.class=" + getMemberIds().getClass()).append("\n");
		sb.append(" }");
		return sb.toString();
	}
	
	public boolean equals(Object obj) {
		if (this.getCohortId() == null)
			return false;
		if (obj instanceof Cohort) {
			Cohort c = (Cohort)obj;
			return (this.getCohortId().equals(c.getCohortId()));
		}
		return false;
	}
	
	public int hashCode() {
		if (this.getCohortId() == null) return super.hashCode();
		int hash = 8;
		hash = 31 * this.getCohortId() + hash;
		return hash;
	}
	
	// getters and setters
	
	public Integer getCohortId() {
		return cohortId;
	}
	
	public void setCohortId(Integer cohortId) {
		this.cohortId = cohortId;
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
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
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

	public Set<Integer> getMemberIds() {
		return memberIds;
	}

	public void setMemberIds(Set<Integer> memberIds) {
		this.memberIds = memberIds;
	}
		
}

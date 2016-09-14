/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Date;

public class CohortMembership extends BaseOpenmrsData implements Comparable<CohortMembership> {
	
	public static final long serialVersionUID = 0L;
	
	protected static final Log log = LogFactory.getLog(CohortMembership.class);

	private Integer cohortMemberId;
	
	private Cohort cohort;
	
	private Patient patient;
	
	private Date startDate;
	
	private Date endDate;

	// Constructor
	public CohortMembership() {
	}

	public CohortMembership(Patient patient, Date startDate) {
		this.patient = patient;
		this.startDate = startDate;
	}

	public CohortMembership(Patient patient) {
		this(patient, new Date());
	}
	
	public boolean isMemberActive() {
		return this.getStartDate() != null && this.getStartDate().before(new Date()) && this.getEndDate() == null;
	}

	@Override
	public Integer getId() {
		return getCohortMemberId();
	}

	@Override
	public void setId(Integer id) {
		setCohortMemberId(id);
	}

	public Integer getCohortMemberId() {
		return cohortMemberId;
	}

	public void setCohortMemberId(Integer cohortMemberId) {
		this.cohortMemberId = cohortMemberId;
	}

	public Cohort getCohort() {
		return cohort;
	}
	
	protected void setCohort(Cohort cohort) {
		this.cohort = cohort;
	}
	
	public Patient getPatient() {
		return patient;
	}
	
	public void setPatient(Patient patient) {
		this.patient = patient;
	}
	
	public Date getStartDate() {
		return startDate;
	}
	
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	
	public Date getEndDate() {
		return endDate;
	}
	
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	@Override
	public int compareTo(CohortMembership o) {
		return this.getPatient().getPatientId() - o.getPatient().getPatientId();
	}
}

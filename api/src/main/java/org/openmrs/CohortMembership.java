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

import java.util.Date;

import org.openmrs.util.OpenmrsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @since 2.1.0
 */
public class CohortMembership extends BaseOpenmrsData implements Comparable<CohortMembership> {
	
	public static final long serialVersionUID = 0L;
	
	protected static final Logger log = LoggerFactory.getLogger(CohortMembership.class);
	
	private Integer cohortMemberId;
	
	private Cohort cohort;
	
	private Integer patientId;
	
	private Date startDate;
	
	private Date endDate;
	
	// Constructor
	public CohortMembership() {
	}
	
	public CohortMembership(Integer patientId, Date startDate) {
		this.patientId = patientId;
		this.startDate = startDate;
	}
	
	public CohortMembership(Integer patientId) {
		this(patientId, new Date());
	}
	
	/**
	 * @param asOfDate date to compare if membership is active or inactive
	 * @return boolean true/false if membership is active/inactive
	 */
	public boolean isActive(Date asOfDate) {
		Date date;
		if (asOfDate == null) {
			date = new Date();
		} else {
			date = asOfDate;
		}
		return !this.getVoided() && (date.equals(this.getStartDate()) || date.after(this.getStartDate()))
		        && (this.getEndDate() == null || date.before(this.getEndDate()));
	}
	
	public boolean isActive() {
		return isActive(null);
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
	
	public Integer getPatientId() {
		return patientId;
	}
	
	public void setPatientId(Integer patientId) {
		this.patientId = patientId;
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
	
	/**
	 * Compares the specified membership to this membership, the logic is such that if the items are
	 * inserted into a sorted set, the voided and inactive memberships are pushed to the very end,
	 * while the ordering of the remaining memberships is arbitrary.
	 * 
	 * @param o
	 * @return
	 */
	@Override
	public int compareTo(CohortMembership o) {
		if ((this.getVoided() && !o.getVoided()) || (!this.isActive() && o.isActive())) {
			return 1;
		} else if ((!this.getVoided() && o.getVoided()) || (this.isActive() && !o.isActive())) {
			return -1;
		}
		
		int ret = OpenmrsUtil.compareWithNullAsGreatest(this.getCohort().getCohortId(), o.getCohort().getCohortId());
		if (ret != 0) {
			return ret;
		}
		
		ret = this.getPatientId().compareTo(o.getPatientId());
		if (ret != 0) {
			return ret;
		}
		
		ret = OpenmrsUtil.compareWithNullAsEarliest(this.getEndDate(), o.getEndDate());
		if (ret != 0) {
			return ret;
		}
		
		return OpenmrsUtil.compare(this.getStartDate(), o.getStartDate());
	}
}

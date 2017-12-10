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

/**
 * @since 2.1.0
 */
public class CohortMembership extends BaseChangeableOpenmrsData implements Comparable<CohortMembership> {
	
	public static final long serialVersionUID = 0L;

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
	 * Compares asOfDate to [startDate, endDate], inclusive of both endpoints.
	 * @param asOfDate date to compare if membership is active or inactive
	 * @return boolean true/false if membership is active/inactive
	 */
	public boolean isActive(Date asOfDate) {
		Date date = asOfDate == null ? new Date() : asOfDate;
		return !this.getVoided() && OpenmrsUtil.compare(startDate, date) <= 0
				&& OpenmrsUtil.compareWithNullAsLatest(date, endDate) <= 0;
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
	
	public void setCohort(Cohort cohort) {
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
	
	/**
	 * OpenMRS treats a membership as active from its startDate to endDate <em>inclusive</em> of both.
	 * The underlying database field stores a date+time, so in the common case (where you don't care about the time of day
	 * that cohort membership ended) you want to set the time component to 23:59:59.
	 * @param endDate
	 */
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
	
	/**
	 * Sorts by following fields, in order:
	 * <ol>
	 *     <li>voided (voided memberships sort last)</li>
	 *     <li>endDate descending (so ended memberships are towards the end, and the older the more towards the end</li>
	 *     <li>startDate descending (so started more recently is towards the front)</li>
	 *     <li>patientId ascending (intuitive and consistent tiebreaker for client code)</li>
	 *     <li>uuid ascending (just so we have a final consistent tie breaker)</li>
	 * </ol>
	 *
	 * @param o other membership to compare this to
	 * @return
	 */
	@Override
	public int compareTo(CohortMembership o) {
		int ret = this.getVoided().compareTo(o.getVoided());
		if (ret == 0) {
			ret = -OpenmrsUtil.compareWithNullAsLatest(this.getEndDate(), o.getEndDate());
		}
		if (ret == 0) {
			ret = -OpenmrsUtil.compareWithNullAsEarliest(this.getStartDate(), o.getStartDate());
		}
		if (ret == 0) {
			ret = this.getPatientId().compareTo(o.getPatientId());
		}
		if (ret == 0) {
			ret = this.getUuid().compareTo(o.getUuid());
		}
		return ret;
	}
}

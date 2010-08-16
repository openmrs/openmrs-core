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

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ApproximateDateItem {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	private Integer year;
	
	private Integer month;
	
	private Integer day;
	
	private Date estimatedDate;
	
	private boolean dayApproximated;
	
	private boolean weekApproximated;
	
	private boolean monthApproximated;
	
	private boolean yearApproximated;
	
	/**
	 * @param year the year to set
	 */
	public void setYear(Integer year) {
		this.year = year;
	}
	
	/**
	 * @return the year
	 */
	public Integer getYear() {
		return year;
	}

	/**
	 * @param month the month to set
	 */
	public void setMonth(Integer month) {
		this.month = month;
	}
	
	/**
	 * @return the month
	 */
	public Integer getMonth() {
		return month;
	}
	
	/**
	 * @param day the day to set
	 */
	public void setDay(Integer day) {
		this.day = day;
	}
	
	/**
	 * @return the day
	 */
	public Integer getDay() {
		return day;
	}
	
	/**
	 * @return the estimatedDate
	 */
	public Date getEstimatedDate() {
		return estimatedDate;
	}
	
	/**
	 * @param estimatedDate the estimatedDate to set
	 */
	public void setEstimatedDate(Date estimatedDate) {
		this.estimatedDate = estimatedDate;
	}
	
	/**
	 * @return the dayApproximated
	 */
	public boolean isDayApproximated() {
		return dayApproximated;
	}
	
	/**
	 * @param dayApproximated the dayApproximated to set
	 */
	public void setDayApproximated(boolean dayApproximated) {
		this.dayApproximated = dayApproximated;
	}
	
	/**
	 * @return the weekApproximated
	 */
	public boolean isWeekApproximated() {
		return weekApproximated;
	}
	
	/**
	 * @param weekApproximated the weekApproximated to set
	 */
	public void setWeekApproximated(boolean weekApproximated) {
		this.weekApproximated = weekApproximated;
	}
	
	/**
	 * @return the monthApproximated
	 */
	public boolean isMonthApproximated() {
		return monthApproximated;
	}
	
	/**
	 * @param monthApproximated the monthApproximated to set
	 */
	public void setMonthApproximated(boolean monthApproximated) {
		this.monthApproximated = monthApproximated;
	}

	/**
     * @param yearApproximated the yearApproximated to set
     */
    public void setYearApproximated(boolean yearApproximated) {
	    this.yearApproximated = yearApproximated;
    }

	/**
     * @return the yearApproximated
     */
    public boolean isYearApproximated() {
	    return yearApproximated;
    }
}

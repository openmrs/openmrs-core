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

import java.util.Calendar;
import java.util.Date;

import org.joda.time.DateTimeFieldType;
import org.joda.time.Partial;

/**
 *
 */
public class ApproximateDate implements Comparable<ApproximateDate> {
	
	private int metadata = 0;
	
	private Partial partialDate = null;
	
	public final static int APPROXIMATE_YEAR = 1;
	
	public final static int APPROXIMATE_MONTH = 2;
	
	public final static int APPROXIMATE_DAY = 4;
	
	public final static int APPROXIMATE_WEEK = 8;
	
	public final static int APPROXIMATE_AGE = 16;
	
	protected final static int UNKNOWN_YEAR = 256;
	
	protected final static int UNKNOWN_MONTH = 512;
	
	protected final static int UNKNOWN_DAY = 1024;

	private final static float DAYS_PER_YEAR = 365.2425f;
	
	public enum Fields {
		YEAR, MONTH, DAY;
	}

	public ApproximateDate() {
		partialDate = new Partial();
	}
	
	/**
	 * Initializes the partialDate with an exact date
	 * 
	 * @param date the date to be set
	 */
	public ApproximateDate(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		setDate(calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR), false,
		    false, false);
	}
	
	/**
	 * Initializes the partialDate with an exact date and imposes meta-data on the data
	 * 
	 * @param date
	 * @param metadata
	 */
	public ApproximateDate(Date date, int metadata) {
		this(date);
		this.metadata = metadata;

	}
	
	/**
	 * Used internally to check flag status
	 * 
	 * @param flag the flag to be checked for
	 * @return
	 */
	protected boolean isFlag(int flag) {
		return (metadata & flag) == flag;
	}
	
	/**
	 * Used internally to set flag status
	 * 
	 * @param flag the flag to be modified
	 * @param value the value set to the flag
	 */
	protected void setFlag(int flag, boolean value) {
		if (value) {
			metadata |= flag;
		} else {
			metadata &= ~flag;
		}
	}
	
	/**
	 * Used internally to set the value for any field in the date
	 * 
	 * @param dtFieldType the type of the field to set
	 * @param value value for the
	 * @param approximate
	 * @param approximateFlag
	 * @param unknownFlag
	 **/
	private void setAnyField(DateTimeFieldType dtFieldType, Integer value, boolean approximate, int approximateFlag,
	                         int unknownFlag) {
		if (value != null) {
			partialDate = partialDate.with(dtFieldType, value);
			setFlag(unknownFlag, false);
			setFlag(approximateFlag, approximate);
		} else {
			setFlag(unknownFlag, true);
			setFlag(approximateFlag, false);
		}
	}
	
	/**
	 * Sets
	 * 
	 * @param year
	 * @param approximate
	 * @should set the year value
	 * @should set the approximate value for year
	 */
	public void setYear(Integer year, boolean approximate) {
		setAnyField(DateTimeFieldType.year(), year, approximate, APPROXIMATE_YEAR, UNKNOWN_YEAR);
	}
	
	/**
	 * Auto generated method comment
	 * 
	 * @param month
	 * @param approximate
	 * @should set the month value
	 * @should set the approximate value for month
	 */
	public void setMonth(Integer month, boolean approximate) {
		setAnyField(DateTimeFieldType.monthOfYear(), month, approximate, APPROXIMATE_MONTH, UNKNOWN_MONTH);
	}
	
	/**
	 * Auto generated method comment
	 * 
	 * @param day
	 * @param approximate
	 * @should set the day value
	 * @should set the approximate value for day
	 */
	public void setDay(Integer day, boolean approximate) {
		setAnyField(DateTimeFieldType.dayOfMonth(), day, approximate, APPROXIMATE_DAY, UNKNOWN_DAY);
	}
	
	/**
	 * Returns the year of the date
	 * 
	 * @return year
	 */
	public Integer getYear() {
		return partialDate.get(DateTimeFieldType.year());
	}
	
	/**
	 * Returns the month of the date
	 * 
	 * @return month
	 */
	public Integer getMonth() {
		return partialDate.get(DateTimeFieldType.monthOfYear());
	}
	
	/**
	 * Returns the day of the date
	 * 
	 * @return day
	 */
	public Integer getDay() {
		return partialDate.get(DateTimeFieldType.dayOfMonth());
	}

	/**
	 * Used to set an approximation level for this date
	 * 
	 * @param flag
	 */
	public void setApproximated(int flag) {
		metadata = flag;
	}
	
	/**
	 * Auto generated method comment
	 * 
	 * @return
	 */
	public boolean isApproximated() {
		return metadata > 0;
	}
	
	/**
	 * Auto generated method comment
	 * 
	 * @param flag
	 * @return
	 */
	public boolean isApproximated(int approximatedTo) {
		return isFlag(approximatedTo);
	}
	
	public boolean isDayApproximated() {
		return isFlag(APPROXIMATE_DAY);
	}
	
	public boolean isMonthApproximated() {
		return isFlag(APPROXIMATE_MONTH);
	}
	
	public boolean isYearApproximated() {
		return isFlag(APPROXIMATE_YEAR);
	}
	
	public boolean isAgeApproximated() {
		return isFlag(APPROXIMATE_AGE);
	}

	/**
	 * Set partial date to a specific date
	 * 
	 * @param date
	 */
	public void setDate(Date date) {
		//TODO
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		setDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), false,
		    false, false);
		setApproximated(0);
	}
	
	/**
	 * Auto generated method comment
	 * 
	 * @param year
	 * @param month
	 * @param day
	 * @param yearApprox
	 * @param monthApprox
	 * @param dayApprox
	 * @should properly set the date
	 */
	public void setDate(int year, int month, int day, boolean yearApprox, boolean monthApprox, boolean dayApprox) {
		setYear(year, yearApprox);
		setMonth(month, monthApprox);
		setDay(day, dayApprox);
	}
	
	/**
	 * Auto generated method comment
	 * 
	 * @param age
	 * @should set the date depending on the age
	 */
	public void setDateFromAge(float age) {
		int days = Math.round(age * DAYS_PER_YEAR);
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DAY_OF_MONTH, -days);
		setDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), false,
		    false, false);
		setFlag(APPROXIMATE_AGE, true);
	}
	
	/**
	 * @return Date estimated date based on partial and approximations
	 * @should return properly estimated dates
	 **/
	public Date getDate() {
		//TODO
		int year;
		if (isFlag(UNKNOWN_YEAR)) {
			year = Calendar.getInstance().YEAR;
		} else {
			year = getYear();
		}
		
		int month;
		if (isFlag(UNKNOWN_MONTH)) {
			month = 7;
		} else {
			month = getMonth();
		}
		
		int date;
		if (isFlag(UNKNOWN_DAY)) {
			date = 15;
		} else {
			date = getDay();
		}
		
		if (isFlag(UNKNOWN_MONTH) && isFlag(UNKNOWN_DAY)) {
			month = 7;
			date = 1;
		}

		Calendar cal = Calendar.getInstance();
		cal.set(year, month, date);
		
		return cal.getTime();
	}
	
	/**
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 * @should compare two ApproximateDate's
	 */
	@Override
	public int compareTo(ApproximateDate o) {
		//TODO Advanced comparison incorporating Approximate values and Unknowns
		return this.getDate().compareTo(o.getDate());
	}

}

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
	
	private Integer metadata = null;
	
	private Partial partialDate = null;
	
	public final static int NOT_APPROXIMATED = 0;

	public final static int APPROXIMATE_YEAR = 1;
	
	public final static int APPROXIMATE_MONTH = 2;
	
	public final static int APPROXIMATE_DAY = 4;
	
	public final static int APPROXIMATE_WEEK = 8;
	
	public final static int APPROXIMATE_AGE = 16;
	
	protected final static int UNKNOWN_YEAR = 256;
	
	protected final static int UNKNOWN_MONTH = 512;
	
	protected final static int UNKNOWN_DAY = 1024;

	private final static float DAYS_PER_YEAR = 365.25f;


	/**
	 * default no-arg constructor
	 */
	public ApproximateDate() {
		partialDate = getPartial();
	}
	
	/**
	 * Initializes the partialDate with an exact date
	 * 
	 * @param date the date to be set
	 */
	public ApproximateDate(Date date) {
		//		Calendar calendar = Calendar.getInstance();
		//		calendar.setTime(date);
		setDate(date);
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
	 * @return the metadata
	 */
	public int getMetadata() {
		return metadata;
	}
	
	/**
	 * @param metadata the metadata to set
	 */
	public void setMetadata(int metadata) {
		this.metadata = metadata;
		if(partialDate != null){
			if (isDayUnknown()) {
				partialDate = partialDate.without(DateTimeFieldType.dayOfMonth());
			}
			if (isMonthUnknown()) {
				partialDate = partialDate.without(DateTimeFieldType.monthOfYear());
			}
			if (isYearUnknown()) {
				partialDate = partialDate.without(DateTimeFieldType.monthOfYear());
			}
		}
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
	 * Used internally to retrieve the Partial instance
	 * 
	 * @return the partialDate
	 */
	private Partial getPartial() {
		if (partialDate == null || partialDate.equals(null)) {
			partialDate = new Partial();
		}
		return partialDate;
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
			partialDate = getPartial().with(dtFieldType, value);
			setFlag(unknownFlag, false);
			setFlag(approximateFlag, approximate);
		} else {
			setFlag(unknownFlag, true);
			setFlag(approximateFlag, false);
		}
	}
	
	/**
	 * Sets the year value
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
	 * Sets the month value
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
	 * Sets the day value
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
	 * @should return the year
	 */
	public Integer getYear() {
		return getPartial().get(DateTimeFieldType.year());
	}
	
	/**
	 * Returns the month of the date
	 * 
	 * @return month
	 * @should return the month
	 */
	public Integer getMonth() {
		return getPartial().get(DateTimeFieldType.monthOfYear());
	}
	
	/**
	 * Returns the day of the date
	 * 
	 * @return day
	 * @should return the day
	 */
	public Integer getDay() {
		return getPartial().get(DateTimeFieldType.dayOfMonth());
	}
	
	/**
	 * Used to set an approximation level for this date
	 * 
	 * @param flag
	 * @should set the approximation level
	 */
	public void setApproximated(int flag) {
		metadata = flag;
	}
	
	/**
	 * Checks if any part of the date is approximated
	 * 
	 * @return
	 * @should set the approximation level
	 */
	public boolean isApproximated() {
		return metadata > 0;
	}
	
	/**
	 * Checks if a particular part of the date is approximated
	 * 
	 * @param flag
	 * @return
	 */
	public boolean isApproximated(int approximatedTo) {
		return isFlag(approximatedTo);
	}
	
	/**
	 * Checks if the day value of the date is approximated
	 * 
	 * @return
	 */
	public boolean isDayApproximated() {
		return isFlag(APPROXIMATE_DAY);
	}
	
	/**
	 * Checks if the month value of the date is approximated
	 * 
	 * @return
	 */
	public boolean isMonthApproximated() {
		return isFlag(APPROXIMATE_MONTH);
	}
	
	/**
	 * Checks if the year value of the date is approximated
	 * 
	 * @return
	 */
	public boolean isYearApproximated() {
		return isFlag(APPROXIMATE_YEAR);
	}
	
	/**
	 * Checks if the date is derived using the age
	 * 
	 * @return
	 */
	public boolean isAgeApproximated() {
		return isFlag(APPROXIMATE_AGE);
	}
	
	protected boolean isDayUnknown() {
		return isFlag(UNKNOWN_DAY);
	}
	
	protected boolean isMonthUnknown() {
		return isFlag(UNKNOWN_MONTH);
	}
	
	protected boolean isYearUnknown() {
		return isFlag(UNKNOWN_MONTH);
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
	 * Set partial date to a specific date
	 * 
	 * @param date
	 */
	public void setDate(Calendar calendar) {
		setDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH), false,
		    false, false);
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
		if (!(metadata.equals(null))){
			if (isDayUnknown()) {
				setDay(null, false);
			}else{
				setDay(calendar.get(Calendar.DAY_OF_MONTH), isDayApproximated());
			}
			if (isMonthUnknown()) {
				setMonth(null, false);
			} else {
				setDay(calendar.get(Calendar.MONTH), isDayApproximated());
			}
			if (isYearUnknown()) {
				setYear(null, false);
			} else {
				setDay(calendar.get(Calendar.YEAR), isDayApproximated());
			}
		}
		
		//		setDate(calendar);
	}
	
	/**
	 * Set partial date to an exact date
	 * 
	 * @param date the date to be set
	 */
	public void setExactDate(Date date) {
		//TODO
		setDate(date);
		setApproximated(0);
		setFlag(UNKNOWN_DAY, false);
		setFlag(UNKNOWN_MONTH, false);
		setFlag(UNKNOWN_YEAR, false);
	}

	/**
	 * Auto generated method comment
	 * 
	 * @param age
	 * @should set the date depending on the age
	 */
	public void setDateFromAge(float age) {
		this.setDateFromAge(age, null);
	}
	
	/**
	 * Auto generated method comment
	 * 
	 * @param age
	 * @should set the date depending on the age and a date
	 */
	public void setDateFromAge(float age, Date ageOnDate) {
		int days = Math.round(age * DAYS_PER_YEAR);
		Calendar calendar = Calendar.getInstance();
		if (ageOnDate != null)
			calendar.setTime(ageOnDate);
		calendar.add(Calendar.DAY_OF_MONTH, -days);
		setDate(calendar);
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
		cal.setTimeInMillis(0L);
		cal.set(year, month - 1, date, 0, 0, 0);
		return cal.getTime();
	}
	
	/**
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 * @should compare two ApproximateDates
	 */
	@Override
	public int compareTo(ApproximateDate o) {
		//TODO Advanced comparison incorporating Approximate values and Unknowns
		return this.getDate().compareTo(o.getDate());
	}

}

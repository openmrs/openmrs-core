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

import org.apache.commons.lang3.time.DateUtils;

import java.util.Date;

/**
 * Duration represented using ISO 8601 duration codes
 */
public class ISO8601Duration {
	
	public static final String SECONDS_CODE = "S";
	
	public static final String MINUTES_CODE = "m";
	
	public static final String HOURS_CODE = "H";
	
	public static final String DAYS_CODE = "D";
	
	public static final String WEEKS_CODE = "W";
	
	public static final String MONTHS_CODE = "M";
	
	public static final String YEARS_CODE = "Y";
	
	public static final String RECURRING_INTERVAL_CODE = "R";
	
	public static final String CONCEPT_SOURCE_NAME = "ISO 8601 Duration";
	
	private static final int SECONDS_PER_MINUTE = 60;
	
	private static final int MINUTES_PER_HOUR = 60;
	
	private static final int HOURS_PER_DAY = 24;
	
	private static final int SECONDS_PER_HOUR = SECONDS_PER_MINUTE * MINUTES_PER_HOUR;
	
	private static final int SECONDS_PER_DAY = SECONDS_PER_HOUR * HOURS_PER_DAY;
	
	private final Integer duration;
	
	private final String code;
	
	public ISO8601Duration(Integer duration, String code) {
		this.duration = duration;
		this.code = code;
	}
	
	/**
	 * Add this duration to given startDate
	 * @param startDate
	 * @param frequency is used to calculate time to be added to startDate when duration unit is 'Recurring Interval'
	 * @return date which is startDate plus duration
	 */
	public Date addToDate(Date startDate, OrderFrequency frequency) {
		if (SECONDS_CODE.equals(code))
			return DateUtils.addSeconds(startDate, duration);
		if (MINUTES_CODE.equals(code))
			return DateUtils.addMinutes(startDate, duration);
		if (HOURS_CODE.equals(code))
			return DateUtils.addHours(startDate, duration);
		if (DAYS_CODE.equals(code))
			return DateUtils.addDays(startDate, duration);
		if (WEEKS_CODE.equals(code))
			return DateUtils.addWeeks(startDate, duration);
		if (MONTHS_CODE.equals(code))
			return DateUtils.addMonths(startDate, duration);
		if (YEARS_CODE.equals(code))
			return DateUtils.addYears(startDate, duration);
		if (RECURRING_INTERVAL_CODE.equals(code)) {
			return DateUtils.addSeconds(startDate, (int) (duration * SECONDS_PER_DAY / frequency.getFrequencyPerDay()));
		}
		return null;
	}
}

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
package org.openmrs.logic;

/**
 * Represents a duration of time &mdash; e.g., one year, two weeks, or 18 months. Used within
 * criteria
 * 
 * @see org.openmrs.logic.LogicCriteria
 */
public class Duration {
	
	public static enum Units {
		SECONDS, MINUTES, HOURS, DAYS, WEEKS, MONTHS, YEARS
	}
	
	private Double duration;
	
	private Units units;
	
	private Duration(Double duration, Units units) {
		this.duration = duration;
		this.units = units;
	}
	
	public Double getDuration() {
		return duration;
	}
	
	public Units getUnits() {
		return units;
	}
	
	/**
	 * Returns the equivalent duration in days
	 * 
	 * @return the equivalent duration in days
	 */
	public Double getDurationInDays() {
		switch (units) {
			case SECONDS:
				return duration / 86400;
			case MINUTES:
				return duration / 1440;
			case HOURS:
				return duration / 24;
			case DAYS:
				return duration;
			case WEEKS:
				return duration * 7;
			case MONTHS:
				return duration * 30;
			case YEARS:
				return duration * 365;
			default:
				return 0d;
		}
	}
	
	/**
	 * Returns the equivalent duration in milliseconds
	 * 
	 * @return the equivalent duration in milliseconds
	 */
	public long getDurationInMillis() {
		long d = duration.longValue();
		switch (units) {
			case SECONDS:
				return d * 1000;
			case MINUTES:
				return d * 60000;
			case HOURS:
				return d * 3600000;
			case DAYS:
				return d * 86400000;
			case WEEKS:
				return d * 10080000;
			case MONTHS:
				return d * 2628000000L;
			case YEARS:
				return d * 31536000000L;
			default:
				return 0;
		}
	}
	
	/**
	 * Returns a duration for the given number of seconds
	 * 
	 * @param duration number of seconds for duration
	 * @return <code>Duration</code> object for given number of seconds
	 */
	public static Duration seconds(Double duration) {
		return new Duration(duration, Units.SECONDS);
	}
	
	/**
	 * Returns a duration for the given number of seconds
	 * 
	 * @param duration number of seconds for duration
	 * @return <code>Duration</code> object for given number of seconds
	 */
	public static Duration seconds(int duration) {
		return seconds(new Double(duration));
	}
	
	/**
	 * Returns a duration for the given number of minutes
	 * 
	 * @param duration number of minutes for duration
	 * @return <code>Duration</code> object for given number of minutes
	 */
	public static Duration minutes(Double duration) {
		return new Duration(duration, Units.MINUTES);
	}
	
	/**
	 * Returns a duration for the given number of minutes
	 * 
	 * @param duration number of minutes for duration
	 * @return <code>Duration</code> object for given number of minutes
	 */
	public static Duration minutes(int duration) {
		return minutes(new Double(duration));
	}
	
	/**
	 * Returns a duration for the given number of hours
	 * 
	 * @param duration number of hours for duration
	 * @return <code>Duration</code> object for given number of hours
	 */
	public static Duration hours(Double duration) {
		return new Duration(duration, Units.HOURS);
	}
	
	/**
	 * Returns a duration for the given number of hours
	 * 
	 * @param duration number of hours for duration
	 * @return <code>Duration</code> object for given number of hours
	 */
	public static Duration hours(int duration) {
		return hours(new Double(duration));
	}
	
	/**
	 * Returns a duration for the given number of days
	 * 
	 * @param duration number of days for duration
	 * @return <code>Duration</code> object with specified number of days
	 */
	public static Duration days(Double duration) {
		return new Duration(duration, Units.DAYS);
	}
	
	/**
	 * Returns a duration for the given number of days
	 * 
	 * @param duration number of days for duration
	 * @return <code>Duration</code> object with specified number of days
	 */
	public static Duration days(int duration) {
		return days(new Double(duration));
	}
	
	/**
	 * Returns a duration for the given number of weeks
	 * 
	 * @param duration number of weeks for duration
	 * @return <code>Duration</code> object with specified number of weeks
	 */
	public static Duration weeks(Double duration) {
		return new Duration(duration, Units.WEEKS);
	}
	
	/**
	 * Returns a duration for the given number of weeks
	 * 
	 * @param duration number of weeks for duration
	 * @return <code>Duration</code> object with specified number of weeks
	 */
	public static Duration weeks(int duration) {
		return weeks(new Double(duration));
	}
	
	/**
	 * Returns a duration for the given number of months
	 * 
	 * @param duration number of months for duration
	 * @return <code>Duration</code> object with specified number of months
	 */
	public static Duration months(Double duration) {
		return new Duration(duration, Units.MONTHS);
	}
	
	/**
	 * Returns a duration for the given number of months
	 * 
	 * @param duration number of months for duration
	 * @return <code>Duration</code> object with specified number of months
	 */
	public static Duration months(int duration) {
		return months(new Double(duration));
	}
	
	/**
	 * Returns a duration for the given number of years
	 * 
	 * @param duration number of years for duration
	 * @return <code>Duration</code> object with specified number of years
	 */
	public static Duration years(Double duration) {
		return new Duration(duration, Units.YEARS);
	}
	
	/**
	 * Returns a duration for the given number of years
	 * 
	 * @param duration number of years for duration
	 * @return <code>Duration</code> object with specified number of years
	 */
	public static Duration years(int duration) {
		return years(new Double(duration));
	}
	
}

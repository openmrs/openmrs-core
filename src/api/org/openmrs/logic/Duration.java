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

public class Duration {

	public static enum Units {
		SECONDS, MINUTES, DAYS, WEEKS, MONTHS, YEARS
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

	public Double getDurationInDays() {
		switch (units) {
		case SECONDS:
			return duration / 86400;
		case MINUTES:
			return duration / 1440;
		case DAYS:
			return duration;
		case WEEKS:
			return duration * 7;
		case YEARS:
			return duration * 365;
		default:
			return 0d;
		}
	}
	
	public long getDurationInMillis() {
		long d = duration.longValue();
		switch (units) {
		case SECONDS:
			return d * 1000;
		case MINUTES:
			return d * 60000;
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

	public static Duration seconds(Double duration) {
		return new Duration(duration, Units.SECONDS);
	}
	
	public static Duration seconds(int duration) {
		return new Duration(new Double(duration), Units.SECONDS);
	}
	
	public static Duration minutes(Double duration) {
		return new Duration(duration, Units.MINUTES);
	}
	
	public static Duration minutes(int duration) {
		return new Duration(new Double(duration), Units.MINUTES);
	}
	
	public static Duration days(Double duration) {
		return new Duration(duration, Units.DAYS);
	}
	
	public static Duration days(int duration) {
		return new Duration(new Double(duration), Units.DAYS);
	}

	public static Duration weeks(Double duration) {
		return new Duration(duration, Units.WEEKS);
	}
	
	public static Duration weeks(int duration) {
		return new Duration(new Double(duration), Units.WEEKS);
	}
	
	public static Duration months(Double duration) {
		return new Duration(duration, Units.MONTHS);
	}
	
	public static Duration months(int duration) {
		return new Duration(new Double(duration), Units.MONTHS);
	}
	
	public static Duration years(Double duration) {
		return new Duration(duration, Units.YEARS);
	}
	
	public static Duration years(int duration) {
		return new Duration(new Double(duration), Units.YEARS);
	}
	
}

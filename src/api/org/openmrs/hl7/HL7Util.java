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
package org.openmrs.hl7;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import ca.uhn.hl7v2.HL7Exception;

/**
 * HL7-related utilities
 * 
 * @version 1.0
 */
public class HL7Util {
	
	// Date and time format parsers
	private final static DateFormat TIMESTAMP_FORMAT = new SimpleDateFormat("yyyyMMddHHmmss.SSSZ");
	
	private final static DateFormat TIME_FORMAT = new SimpleDateFormat("HHmmss.SSSZ");
	
	public final static String LOCAL_TIMEZONE_OFFSET = new SimpleDateFormat("Z").format(new Date());
	
	/**
	 * Converts an HL7 timestamp into a java.util.Date object. HL7 timestamps can be created with
	 * varying levels of precision &mdash; e.g., just the year or just the year and month, etc.
	 * Since java.util.Date cannot store a partial value, we fill in defaults like January, 01 at
	 * midnight within the current timezone.
	 * 
	 * @param s HL7 timestamp to be parsed
	 * @return Date object
	 * @throws HL7Exception
	 */
	public static Date parseHL7Timestamp(String s) throws HL7Exception {
		
		// HL7 dates must at least contain year and cannot exceed 24 bytes
		if (s == null || s.length() < 4 || s.length() > 24)
			throw new HL7Exception("Invalid date '" + s + "'");
		
		// Parse timezone (optional in HL7 format)
		String timeZoneOffset;
		int tzPlus = s.indexOf('+');
		int tzMinus = s.indexOf('-');
		boolean timeZoneFlag = (tzPlus > 0 || tzMinus > 0);
		if (timeZoneFlag) {
			int tzIndex;
			if (tzPlus > 0)
				tzIndex = tzPlus;
			else
				tzIndex = tzMinus;
			timeZoneOffset = s.substring(tzIndex);
			if (timeZoneOffset.length() != 5)
				System.out.println("invalid timestamp");
			s = s.substring(0, tzIndex);
		} else
			timeZoneOffset = LOCAL_TIMEZONE_OFFSET;
		
		StringBuffer dateString = new StringBuffer();
		dateString.append(s.substring(0, 4)); // year
		if (s.length() >= 6)
			dateString.append(s.substring(4, 6)); // month
		else
			dateString.append("01");
		if (s.length() >= 8)
			dateString.append(s.substring(6, 8)); //day
		else
			dateString.append("01");
		if (s.length() >= 10)
			dateString.append(s.substring(8, 10)); // hour
		else
			dateString.append("00");
		if (s.length() >= 12)
			dateString.append(s.substring(10, 12)); // minute
		else
			dateString.append("00");
		if (s.length() >= 14)
			dateString.append(s.substring(12, 14)); // seconds
		else
			dateString.append("00");
		if (s.length() >= 15 && s.charAt(14) != '.') // decimal point
			throw new HL7Exception("Invalid date format '" + s + "'");
		else
			dateString.append(".");
		if (s.length() >= 16)
			dateString.append(s.substring(15, 16)); // tenths
		else
			dateString.append("0");
		if (s.length() >= 17)
			dateString.append(s.substring(16, 17)); // hundredths
		else
			dateString.append("0");
		if (s.length() >= 18)
			dateString.append(s.subSequence(17, 18)); // milliseconds
		else
			dateString.append("0");
		dateString.append(timeZoneOffset);
		
		Date date;
		try {
			date = TIMESTAMP_FORMAT.parse(dateString.toString());
		}
		catch (ParseException e) {
			throw new HL7Exception("Error parsing date '" + s + "'");
		}
		return date;
	}
	
	/**
	 * Convenience method for parsing HL7 dates (treated just like a timestamp with only year,
	 * month, and day specified)
	 * 
	 * @see org.openmrs.hl7.HL7Util#parseHL7Timestamp(String)
	 * @throws HL7Exception
	 */
	public static Date parseHL7Date(String s) throws HL7Exception {
		return parseHL7Timestamp(s);
	}
	
	/**
	 * Converts an HL7 time into a java.util.Date object. Since the java.util.Date object cannot
	 * store just the time, the date will remain at the epoch (e.g., January 1, 1970). Time more
	 * precise than microseconds is ignored.
	 * 
	 * @param s HL7 time to be converted
	 * @return Date object set to time specified by HL7
	 * @throws HL7Exception
	 */
	public static Date parseHL7Time(String s) throws HL7Exception {
		
		// Parse timezone (optional in HL7 format)
		String timeZoneOffset;
		int tzPlus = s.indexOf('+');
		int tzMinus = s.indexOf('-');
		boolean timeZoneFlag = (tzPlus > 0 || tzMinus > 0);
		if (timeZoneFlag) {
			int tzIndex;
			if (tzPlus > 0)
				tzIndex = tzPlus;
			else
				tzIndex = tzMinus;
			timeZoneOffset = s.substring(tzIndex);
			if (timeZoneOffset.length() != 5)
				System.out.println("invalid timestamp");
			s = s.substring(0, tzIndex);
		} else
			timeZoneOffset = LOCAL_TIMEZONE_OFFSET;
		
		StringBuffer timeString = new StringBuffer();
		
		if (s.length() < 2 || s.length() > 16)
			throw new HL7Exception("Invalid time format '" + s + "'");
		
		timeString.append(s.substring(0, 2)); // hour
		if (s.length() >= 4)
			timeString.append(s.substring(2, 4)); // minute
		else
			timeString.append("00");
		if (s.length() >= 6)
			timeString.append(s.substring(4, 6)); // seconds
		else
			timeString.append("00");
		if (s.length() >= 7 && s.charAt(6) != '.') // decimal point
			throw new HL7Exception("Invalid time format '" + s + "'");
		else
			timeString.append(".");
		if (s.length() >= 8)
			timeString.append(s.substring(7, 8)); // tenths
		else
			timeString.append("0");
		if (s.length() >= 9)
			timeString.append(s.substring(8, 9)); // hundredths
		else
			timeString.append("0");
		if (s.length() >= 10)
			timeString.append(s.subSequence(9, 10)); // milliseconds
		else
			timeString.append("0");
		timeString.append(timeZoneOffset);
		
		Date date;
		try {
			date = (Date) TIME_FORMAT.parse(timeString.toString());
		}
		catch (ParseException e) {
			throw new HL7Exception("Invalid time format: '" + s + "' [" + timeString + "]", e);
		}
		return date;
	}
}

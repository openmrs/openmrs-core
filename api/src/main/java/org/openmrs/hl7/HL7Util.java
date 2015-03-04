/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.hl7;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsUtil;

import ca.uhn.hl7v2.HL7Exception;

/**
 * HL7-related utilities
 *
 * @version 1.0
 */
public class HL7Util {
	
	private static Log log = LogFactory.getLog(HL7Util.class);
	
	// Date and time format parsers
	private static final String TIMESTAMP_FORMAT = "yyyyMMddHHmmss.SSSZ";
	
	private static final String TIME_FORMAT = "HHmmss.SSSZ";
	
	public static final String LOCAL_TIMEZONE_OFFSET = new SimpleDateFormat("Z").format(new Date());
	
	/**
	 * Converts an HL7 timestamp into a java.util.Date object. HL7 timestamps can be created with
	 * varying levels of precision &mdash; e.g., just the year or just the year and month, etc.
	 * Since java.util.Date cannot store a partial value, we fill in defaults like January, 01 at
	 * midnight within the current timezone.
	 *
	 * @param s HL7 timestamp to be parsed
	 * @return Date object
	 * @throws HL7Exception
	 * @should fail on 78
	 * @should handle 1978
	 * @should fail on 19784
	 * @should handle 197804
	 * @should fail on 197841
	 * @should handle 19780411
	 * @should fail on 197804116
	 * @should handle 1978041106
	 * @should fail on 19780411065
	 * @should handle 197804110615
	 * @should fail on 1978041106153
	 * @should handle 19780411061538
	 * @should handle 19780411061538.1
	 * @should handle 19780411061538.12
	 * @should handle 19780411061538.123
	 * @should handle 19780411061538.1234
	 * @should fail on 197804110615-5
	 * @should handle 197804110615-05
	 * @should handle 197804110615-0200
	 * @should not flub dst with 20091225123000
	 */
	public static Date parseHL7Timestamp(String s) throws HL7Exception {
		
		// HL7 dates must at least contain year and cannot exceed 24 bytes
		if (s == null || s.length() < 4 || s.length() > 24) {
			throw new HL7Exception("Invalid date '" + s + "'");
		}
		
		StringBuffer dateString = new StringBuffer();
		dateString.append(s.substring(0, 4)); // year
		if (s.length() >= 6) {
			dateString.append(s.substring(4, 6)); // month
		} else {
			dateString.append("01");
		}
		if (s.length() >= 8) {
			dateString.append(s.substring(6, 8)); //day
		} else {
			dateString.append("01");
		}
		
		// Parse timezone (optional in HL7 format)
		String timeZoneOffset;
		try {
			Date parsedDay = new SimpleDateFormat("yyyyMMdd").parse(s.substring(0, 8));
			timeZoneOffset = getTimeZoneOffset(s, parsedDay);
		}
		catch (ParseException e) {
			throw new HL7Exception("Error parsing date: '" + s.substring(0, 8) + "' for time zone offset'" + s + "'", e);
		}
		s = s.replace(timeZoneOffset, ""); // remove the timezone from the string
		
		if (s.length() >= 10) {
			dateString.append(s.substring(8, 10)); // hour
		} else {
			dateString.append("00");
		}
		if (s.length() >= 12) {
			dateString.append(s.substring(10, 12)); // minute
		} else {
			dateString.append("00");
		}
		if (s.length() >= 14) {
			dateString.append(s.substring(12, 14)); // seconds
		} else {
			dateString.append("00");
		}
		if (s.length() >= 15 && s.charAt(14) != '.') {
			// decimal point
			throw new HL7Exception("Invalid date format '" + s + "'");
		} else {
			dateString.append(".");
		}
		if (s.length() >= 16) {
			dateString.append(s.substring(15, 16)); // tenths
		} else {
			dateString.append("0");
		}
		if (s.length() >= 17) {
			dateString.append(s.substring(16, 17)); // hundredths
		} else {
			dateString.append("0");
		}
		if (s.length() >= 18) {
			dateString.append(s.subSequence(17, 18)); // milliseconds
		} else {
			dateString.append("0");
		}
		
		dateString.append(timeZoneOffset);
		
		Date date;
		try {
			date = new SimpleDateFormat(TIMESTAMP_FORMAT).parse(dateString.toString());
		}
		catch (ParseException e) {
			throw new HL7Exception("Error parsing date '" + s + "'");
		}
		return date;
	}
	
	/**
	 * Gets the timezone string for this given fullString. If fullString contains a + or - sign, the
	 * strings after those are considered to be the timezone. <br/>
	 * <br/>
	 * If the fullString does not contain a timezone, the timezone is determined from the server's
	 * timezone on the "givenDate". (givenDate is needed to account for daylight savings time.)
	 *
	 * @param fullString the hl7 string being parsed
	 * @param givenDate the date that should be used if no timezone exists on the fullString
	 * @return a string like +0500 or -0500 for the timezone
	 * @should return timezone string if exists in given string
	 * @should return timezone for givenDate and not the current date
	 */
	protected static String getTimeZoneOffset(String fullString, Date givenDate) {
		// Parse timezone (optional in HL7 format)
		String timeZoneOffset;
		int tzPlus = fullString.indexOf('+');
		int tzMinus = fullString.indexOf('-');
		boolean timeZoneFlag = (tzPlus > 0 || tzMinus > 0);
		if (timeZoneFlag) {
			int tzIndex;
			if (tzPlus > 0) {
				tzIndex = tzPlus;
			} else {
				tzIndex = tzMinus;
			}
			timeZoneOffset = fullString.substring(tzIndex);
			if (timeZoneOffset.length() != 5) {
				log.error("Invalid timestamp because its too short: " + timeZoneOffset);
			}
			
		} else {
			//set default timezone offset from the current day
			Calendar cal = Calendar.getInstance();
			cal.setTime(givenDate);
			timeZoneOffset = new SimpleDateFormat("Z").format(cal.getTime());
		}
		
		return timeZoneOffset;
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
	 * @should fail on 197804110615
	 * @should handle 0615
	 * @should handle 061538
	 * @should handle 061538.1
	 * @should handle 061538.12
	 * @should handle 061538.123
	 * @should handle 061538.1234
	 * @should handle 061538-0300
	 */
	public static Date parseHL7Time(String s) throws HL7Exception {
		
		String timeZoneOffset = getTimeZoneOffset(s, new Date());
		s = s.replace(timeZoneOffset, ""); // remove the timezone from the string
		
		StringBuffer timeString = new StringBuffer();
		
		if (s.length() < 2 || s.length() > 16) {
			throw new HL7Exception("Invalid time format '" + s + "'");
		}
		
		timeString.append(s.substring(0, 2)); // hour
		if (s.length() >= 4) {
			timeString.append(s.substring(2, 4)); // minute
		} else {
			timeString.append("00");
		}
		if (s.length() >= 6) {
			timeString.append(s.substring(4, 6)); // seconds
		} else {
			timeString.append("00");
		}
		if (s.length() >= 7 && s.charAt(6) != '.') {
			// decimal point
			throw new HL7Exception("Invalid time format '" + s + "'");
		} else {
			timeString.append(".");
		}
		if (s.length() >= 8) {
			timeString.append(s.substring(7, 8)); // tenths
		} else {
			timeString.append("0");
		}
		if (s.length() >= 9) {
			timeString.append(s.substring(8, 9)); // hundredths
		} else {
			timeString.append("0");
		}
		if (s.length() >= 10) {
			timeString.append(s.subSequence(9, 10)); // milliseconds
		} else {
			timeString.append("0");
		}
		
		// Parse timezone (optional in HL7 format)
		timeString.append(timeZoneOffset);
		
		Date date;
		try {
			date = new SimpleDateFormat(TIME_FORMAT).parse(timeString.toString());
		}
		catch (ParseException e) {
			throw new HL7Exception("Invalid time format: '" + s + "' [" + timeString + "]", e);
		}
		return date;
	}
	
	/**
	 * Gets the destination directory for hl7 archives.
	 *
	 * @return The destination directory for the hl7 in archive
	 */
	public static File getHl7ArchivesDirectory() throws APIException {
		String archiveDir = Context.getAdministrationService().getGlobalProperty(
		    OpenmrsConstants.GLOBAL_PROPERTY_HL7_ARCHIVE_DIRECTORY);
		
		if (StringUtils.isBlank(archiveDir)) {
			log.warn("Invalid value for global property '" + OpenmrsConstants.GLOBAL_PROPERTY_HL7_ARCHIVE_DIRECTORY
			        + "', trying to set a default one");
			archiveDir = HL7Constants.HL7_ARCHIVE_DIRECTORY_NAME;
			
			log.debug("Using '" + archiveDir
			        + "' in the application data directory as the root directory for hl7_in_archives");
		}
		
		//TODO Should take care of the case where the user is using removable media, this might explode
		return OpenmrsUtil.getDirectoryInApplicationDataDirectory(archiveDir);
	}
}

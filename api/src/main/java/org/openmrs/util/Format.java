/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.util;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.Locale;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openmrs.api.context.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Format {
	
	private static final Logger log = LoggerFactory.getLogger(Format.class);
	
	public enum FORMAT_TYPE {
		DATE,
		TIME,
		TIMESTAMP
	}
	
	public static String formatPercentage(double pct) {
		return NumberFormat.getPercentInstance().format(pct);
	}
	
	public static String formatPercentage(Number pct) {
		if (pct == null) {
			return "";
		} else {
			return NumberFormat.getPercentInstance().format(pct.doubleValue());
		}
	}
	
	public static String format(double d) {
		return "" + (d);
	}
	
	public static String format(Double d) {
		return d == null ? "" : format(d.doubleValue());
	}
	
	public static String formatTextBoxDate(Date date) {
		return format(date, Context.getLocale(), FORMAT_TYPE.DATE);
	}
	
	public static String format(Date date) {
		return format(date, Context.getLocale(), FORMAT_TYPE.DATE);
	}
	
	public static String format(Date date, FORMAT_TYPE type) {
		return format(date, Context.getLocale(), type);
	}

	/**
	 * This method formats a date object according to a particular locale and returns the date as a string.
	 * The string can contain only the date (month, day and year), only the time (hours, minutes, seconds) or as a
	 * timestamp (both date and time).
	 *
	 * @param date input date to format as a string
	 * @param locale input locale to determine how to format the date
	 * @param type input type to determine how much information from the date is returned
	 * @return empty string if one of the parameters is null. Otherwise a string object for the date such that it is
	 * formatted according to locale and the amount of information it contains is determined by type.
	 * <strong>Should</strong> not fail when only date is null
	 * <strong>Should</strong> not fail when only locale is null
	 * <strong>Should</strong> not fail when only type is null
	 * <strong>Should</strong> not fail when date and locale is null
	 * <strong>Should</strong> not fail when date and type is null
	 * <strong>Should</strong> not fail when locale and type is null
	 * <strong>Should</strong> not fail when all parameters are null
	 * <strong>Should</strong> not fail when none of the parameters are null
	 */
	public static String format(Date date, Locale locale, FORMAT_TYPE type) {
		if (date == null || locale == null || type == null) {
			return "";
		}
		log.debug("Formatting date: " + date + " with locale " + locale);
		
		DateFormat dateFormat;
		
		if (type == FORMAT_TYPE.TIMESTAMP) {
			dateFormat = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, locale);
		} else if (type == FORMAT_TYPE.TIME) {
			dateFormat = DateFormat.getTimeInstance(DateFormat.MEDIUM, locale);
		} else {
			dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, locale);
		}
		return dateFormat.format(date);
	}
	
	public static String format(Throwable t) {
		return t + "\n" + ExceptionUtils.getStackTrace(t);
	}
	
}

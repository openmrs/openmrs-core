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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.Locale;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;

public class Format {
	
	private static Log log = LogFactory.getLog(Format.class);
	
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
		return "" + ((d == (int) d) ? (int) d : d);
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
	
	public static String format(Date date, Locale locale, FORMAT_TYPE type) {
		log.debug("Formatting date: " + date + " with locale " + locale);
		
		DateFormat dateFormat = null;
		
		if (type == FORMAT_TYPE.TIMESTAMP) {
			dateFormat = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, locale);
		} else if (type == FORMAT_TYPE.TIME) {
			dateFormat = DateFormat.getTimeInstance(DateFormat.MEDIUM, locale);
		} else {
			dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, locale);
		}
		return date == null ? "" : dateFormat.format(date);
	}
	
	public static String format(Throwable t) {
		return t + "\n" + ExceptionUtils.getStackTrace(t);
	}
	
}

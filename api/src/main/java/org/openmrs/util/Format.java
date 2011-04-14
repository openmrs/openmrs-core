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
package org.openmrs.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.text.DateFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;

public class Format {
	
	private static Log log = LogFactory.getLog(Format.class);
	
	public enum FORMAT_TYPE {
		DATE, TIME, TIMESTAMP
	};
	
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
		
		if (type == FORMAT_TYPE.TIMESTAMP)
			dateFormat = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, locale);
		else if (type == FORMAT_TYPE.TIME)
			dateFormat = DateFormat.getTimeInstance(DateFormat.MEDIUM, locale);
		else {
			//if (type == FORMAT_TYPE.DATE) (default)
			dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, locale);
		}
		return date == null ? "" : dateFormat.format(date);
	}
	
	public static String format(Throwable t) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		t.printStackTrace(pw);
		return t + "\n" + sw.toString();
	}
	
}

package org.openmrs.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Format {
	
	private static Log log = LogFactory.getLog(Format.class);

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
		return format(date, Locale.UK);
	}
	
	public static String format(Date date) {
		return format(date, Locale.UK);
	}
	
	public static String format(Date date, Locale locale) {
		log.debug("Formatting date: " + date + " with locale " + locale);
		
		String pattern = OpenmrsConstants.OPENMRS_LOCALE_DATE_PATTERNS().get(locale.toString().toLowerCase());
		if (pattern == null) //if the locale was unrecognized
			pattern = OpenmrsConstants.OPENMRS_LOCALE_DATE_PATTERNS().get(Locale.UK.toString().toLowerCase());
			
		return date == null ? "" : new SimpleDateFormat(pattern).format(date);
	}
	
	public static String format(Throwable t) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		t.printStackTrace(pw);
		return t + "\n" + sw.toString();
	}
	
}

package org.openmrs.util;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Format {

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
		return date == null ? "" : new SimpleDateFormat("dd-MMM-yy").format(date);
	}
	
	public static String format(Date date) {
		return date == null ? "" : new SimpleDateFormat("dd-MMM-yy").format(date);
	}
	
}

package org.openmrs.web;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Util {

	public static String formatTextBoxDate(Date date) {
		if (date == null)
			return "";
		else
		{
			return new SimpleDateFormat("dd-MMM-yy").format(date);
		}
	}
	
}

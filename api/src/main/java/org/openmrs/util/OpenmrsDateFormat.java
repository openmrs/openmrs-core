package org.openmrs.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An extension of SimpleDateFormat that defaults to setLenient(true) and rejects
 * parsing any dates that don't have a 4-digit year
 */
public class OpenmrsDateFormat extends SimpleDateFormat {
	
	private static final long serialVersionUID = 1L;

	public OpenmrsDateFormat(SimpleDateFormat sdf, Locale locale) {
		super(sdf.toPattern(), locale);
		this.setLenient(false);
	}
	
	
	public Date parse(String text) throws ParseException {
		
		// first test to see if the pattern ends in "/yyyy"
		Matcher patternMatch = Pattern.compile("\\/yyyy$").matcher(this.toPattern());
		if (patternMatch.find()) {
			// if it does, make sure that the string to parse ends in "/{digit}{digit}{digit}{digit}"
			Matcher dateMatch = Pattern.compile("\\/\\d{4}$").matcher(text);
			if (!dateMatch.find()) {
				throw new ParseException("Unparseable date \"" + text + "\"", 0);
			}
		}
		// otherwise, verify that the pattern and the string are the same length
		else {
			if (this.toPattern().length() != text.length()) {
				throw new ParseException("Unparseable date \"" + text + "\"", 0);
			}
		}
		
		// if we've passed this validation, just call the SimpleDateFormat.parse() method
		return super.parse(text);
	}
}

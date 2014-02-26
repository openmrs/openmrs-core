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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An extension of SimpleDateFormat that defaults to setLenient(false) and for date patterns
 * that end in yyyy  rejects any dates that don't end in 4 digits (to prevent against
 * 2-digit years being interpreted incorrectly); for date patterns that don't end in yyyy, it verifies
 * that the date string is the same length as the pattern string
 */
public class OpenmrsDateFormat extends SimpleDateFormat {
	
	private static final long serialVersionUID = 1L;
	
	public OpenmrsDateFormat(SimpleDateFormat sdf, Locale locale) {
		super(sdf.toPattern(), locale);
		this.setLenient(false);
	}
	
	public Date parse(String text) throws ParseException {
		
		// first test to see if the pattern ends in "{non-alphanumeric-character}yyyy"
		Matcher patternMatch = Pattern.compile("\\Wyyyy$").matcher(this.toPattern());
		if (patternMatch.find()) {
			// if it does, make sure that the string to parse ends in "{non-alphanumeric-character}{digit}{digit}{digit}{digit}"
			Matcher dateMatch = Pattern.compile("\\W\\d{4}$").matcher(text);
			if (!dateMatch.find()) {
				throw new ParseException("Unparseable date \"" + text + "\" - year must have 4 digits", 0);
			}
		}
		// otherwise, verify that the pattern and the string are the same length
		else {
			if (this.toPattern().length() != text.length()) {
				throw new ParseException("Unparseable date \"" + text
				        + "\" - length of date string doesn't match length of date pattern", 0);
			}
		}
		
		// if we've passed this validation, just call the SimpleDateFormat.parse() method
		return super.parse(text);
	}
}

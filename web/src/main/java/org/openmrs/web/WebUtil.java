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
package org.openmrs.web;

import java.util.Arrays;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.util.LocaleUtility;

public class WebUtil {
	
	private static Log log = LogFactory.getLog(WebUtil.class);
	
	public static String escapeHTML(String s) {
		
		if (s == null)
			return "";
		
		s = s.replace("<", "&lt;");
		s = s.replace(">", "&gt;");
		
		return s;
	}
	
	public static String escapeQuotes(String s) {
		
		if (s == null)
			return "";
		
		s = s.replace("\"", "\\\"");
		
		return s;
	}
	
	public static String escapeNewlines(String s) {
		if (s == null)
			return "";
		
		s = s.replace("\n", "\\n");
		
		return s;
	}
	
	public static String escapeQuotesAndNewlines(String s) {
		if (s == null)
			return "";
		
		s = s.replace("\"", "\\\"");
		s = s.replace("\r\n", "\\r\\n");
		s = s.replace("\n", "\\n");
		
		return s;
	}
	
	/**
	 * Strips out the path from a string if "C:\documents\file.doc", will return "file.doc" if
	 * "file.doc", will return "file.doc" if "/home/file.doc" will return "file.doc"
	 * 
	 * @param filename
	 * @return filename stripped down
	 */
	public static String stripFilename(String filename) {
		if (log.isDebugEnabled())
			log.debug("Stripping filename from: " + filename);
		
		// for unix based filesystems
		int index = filename.lastIndexOf("/");
		if (index != -1)
			filename = filename.substring(index + 1);
		
		// for windows based filesystems
		index = filename.lastIndexOf("\\");
		if (index != -1)
			filename = filename.substring(index + 1);
		
		if (log.isDebugEnabled())
			log.debug("Returning stripped down filename: " + filename);
		
		return filename;
	}
	
	/**
	 * This method checks if input locale string contains control characters and tries to clean up
	 * actually contained ones. Also it parses locale object from string representation and
	 * validates it object.
	 * 
	 * @param localeString input string with locale parameter
	 * @return locale object for input string if CTLs were cleaned up or weren't exist or null if
	 *         could not to clean up CTLs from input string
	 * @should ignore leading spaces
	 * @should accept language only locales
	 * @should not accept invalid locales
	 * @should not fail with empty strings
	 * @should not fail with whitespace only
	 */
	public static Locale normalizeLocale(String localeString) {
		if (localeString == null)
			return null;
		localeString = localeString.trim();
		if (localeString.isEmpty())
			return null;
		int len = localeString.length();
		for (int i = 0; i < len; i++) {
			char c = localeString.charAt(i);
			// allow only ASCII letters and "_" character
			if ((c <= 0x20 || c >= 0x7f) || ((c >= 0x20 || c <= 0x7f) && (!Character.isLetter(c) && c != 0x5f))) {
				if (c == 0x09)
					continue; // allow horizontal tabs
				localeString = localeString.replaceFirst(((Character) c).toString(), "");
				len--;
				i--;
			}
		}
		Locale locale = LocaleUtility.fromSpecification(localeString);
		if (LocaleUtility.isValid(locale))
			return locale;
		else
			return null;
	}
	
	/**
	 * Convenient method that parses the given string object, that contains locale parameters which
	 * are separated by comma. Tries to clean up CTLs and other unsupported chars within input
	 * string. If invalid locales are included, they are not returned in the resultant list
	 * 
	 * @param localesString input string with locale parameters separeted by comma (e.g.
	 *            "en, fr_RW, gh")
	 * @return cleaned up string (or same string) if success or null otherwise
	 * @see #normalizeLocale(String)
	 * @should skip over invalid locales
	 * @should not fail with empty string
	 */
	public static String sanitizeLocales(String localesString) {
		// quick npe check
		if (localesString == null)
			return null;
		
		StringBuffer outputString = new StringBuffer();
		
		boolean first = true;
		
		for (String locale : Arrays.asList(localesString.split(","))) {
			Locale loc = normalizeLocale(locale);
			if (loc != null) {
				if (!first)
					outputString.append(", ");
				else
					first = false; // so commas are inserted from now on
				outputString.append(loc.toString());
			}
		}
		if (outputString.length() > 0)
			return outputString.toString();
		else
			return null;
	}
	
}

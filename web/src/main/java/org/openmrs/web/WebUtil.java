/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.GlobalProperty;
import org.openmrs.api.GlobalPropertyListener;
import org.openmrs.api.context.Context;
import org.openmrs.util.Format.FORMAT_TYPE;
import org.openmrs.util.LocaleUtility;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsDateFormat;
import org.owasp.encoder.Encode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebUtil implements GlobalPropertyListener {
	
	private static final Logger log = LoggerFactory.getLogger(WebUtil.class);
	
	private static String defaultDateCache = null;

	/**
	 * Encodes for (X)HTML text content and text attributes.
	 *
	 * @param s
	 * @return Encoded String
	 */
	public static String escapeHTML(String s) {
		return Encode.forHtml(s);
	}

	/**
	 *  Encodes data for an XML CDATA section.
	 *
	 * @param s
	 * @return Encoded String
	 */
	public static String encodeForCDATA(String s) {
		return Encode.forCDATA(s);
	}

	/**
	 * Encodes for CSS strings.
	 *
	 * @param s
	 * @return Encoded String
	 */
	public static String encodeForCssString(String s) {
		return Encode.forCssString(s);
	}

	/**
	 * Encodes for CSS URL contexts.
	 *
	 * @param s
	 * @return Encoded String
	 */
	public static String encodeForCssUrl(String s) {
		return Encode.forCssUrl(s);
	}

	/**
	 * Encodes for HTML text attributes.
	 *
	 * @param s
	 * @return Encoded String
	 */
	public static String encodeForHtmlAttribute(String s) {
		return Encode.forHtmlAttribute(s);
	}

	/**
	 * Encodes for HTML text content.
	 *
	 * @param s
	 * @return Encoded String
	 */
	public static String encodeForHtmlContent(String s) {
		return Encode.forHtmlContent(s);
	}

	/**
	 * Encodes for unquoted HTML attribute values.
	 *
	 * @param s
	 * @return Encoded String
	 */
	public static String encodeForHtmlUnquotedAttribute(String s) {
		return Encode.forHtmlUnquotedAttribute(s);
	}

	/**
	 * Encodes for a Java string.
	 *
	 * @param s
	 * @return Encoded String
	 */
	public static String encodeForJava(String s) {
		return Encode.forJava(s);
	}

	/**
	 * Encodes for a JavaScript string.
	 *
	 * @param s
	 * @return Encoded String
	 */
	public static String encodeForJavaScript(String s) {
		return Encode.forJavaScript(s);
	}

	/**
	 * This method encodes for JavaScript strings contained within HTML script attributes (such as onclick).
	 *
	 * @param s
	 * @return Encoded String
	 */
	public static String encodeForJavaScriptAttribute(String s) {
		return Encode.forJavaScriptAttribute(s);
	}

	/**
	 * Encodes for JavaScript strings contained within HTML script blocks.
	 *
	 * @param s
	 * @return Encoded String
	 */
	public static String encodeForJavaScriptBlock(String s) {
		return Encode.forJavaScriptBlock(s);
	}

	/**
	 * Encodes for JavaScript strings contained within a JavaScript or JSON file.
	 *
	 * @param s
	 * @return Encoded String
	 */
	public static String encodeForJavaScriptSource(String s) {
		return Encode.forJavaScriptSource(s);
	}

	/**
	 * Performs percent-encoding of a URL according to RFC 3986.
	 *
	 * @param s
	 * @return Encoded String
	 */
	public static String encodeForUri(String s) {
		return Encode.forUri(s);
	}

	/**
	 * Performs percent-encoding for a component of a URI, such as a query parameter name or value, path or query-string.
	 *
	 * @param s
	 * @return Encoded String
	 */
	public static String encodeForUriComponent(String s) {
		return Encode.forUriComponent(s);
	}

	/**
	 * Encodes for XML and XHTML.
	 *
	 * @param s
	 * @return Encoded String
	 */
	public static String encodeForXml(String s) {
		return Encode.forXml(s);
	}

	/**
	 * Encodes for XML and XHTML attribute content.
	 *
	 * @param s
	 * @return Encoded String
	 */
	public static String encodeForXmlAttribute(String s) {
		return Encode.forXmlAttribute(s);
	}

	/**
	 * Encodes for XML comments.
	 *
	 * @param s
	 * @return Encoded String
	 */
	public static String encodeForXmlComment(String s) {
		return Encode.forXmlComment(s);
	}

	/**
	 * Encodes for XML and XHTML text content.
	 *
	 * @param s
	 * @return Encoded String
	 */
	public static String encodeForXmlContent(String s) {
		return Encode.forXmlContent(s);
	}

	public static String escapeQuotes(String s) {
		String tmpS = s;
		if (tmpS == null) {
			return "";
		}
		
		tmpS = tmpS.replace("\"", "\\\"");
		
		return tmpS;
	}
	
	public static String escapeNewlines(String s) {
		String tmpS = s;
		if (tmpS == null) {
			return "";
		}
		
		tmpS = tmpS.replace("\n", "\\n");
		
		return tmpS;
	}
	
	public static String escapeQuotesAndNewlines(String s) {
		String tmpS = s;
		if (tmpS == null) {
			return "";
		}

		tmpS = tmpS.replace("\"", "\\\"");
		tmpS = tmpS.replace("\r\n", "\\r\\n");
		tmpS = tmpS.replace("\n", "\\n");
		
		return tmpS;
	}
	
	/**
	 * Strips out the path from a string if "C:\documents\file.doc", will return "file.doc" if
	 * "file.doc", will return "file.doc" if "/home/file.doc" will return "file.doc"
	 *
	 * @param filename
	 * @return filename stripped down
	 */
	public static String stripFilename(String filename) {
		if (log.isDebugEnabled()) {
			log.debug("Stripping filename from: " + filename);
		}
		
		// for unix based filesystems
		String tmpFilename = filename;
		int index = tmpFilename.lastIndexOf("/");
		if (index != -1) {
			tmpFilename = tmpFilename.substring(index + 1);
		}
		
		// for windows based filesystems
		index = tmpFilename.lastIndexOf("\\");
		if (index != -1) {
			tmpFilename = tmpFilename.substring(index + 1);
		}
		
		if (log.isDebugEnabled()) {
			log.debug("Returning stripped down filename: " + tmpFilename);
		}
		
		return tmpFilename;
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
	 * @should not fail with "_" character only
	 */
	public static Locale normalizeLocale(String localeString) {
		if (localeString == null) {
			return null;
		}
		localeString = localeString.trim();
		if (localeString.isEmpty() || "_".equals(localeString)) {
			return null;
		}
		int len = localeString.length();
		for (int i = 0; i < len; i++) {
			char c = localeString.charAt(i);
			// allow only ASCII letters and "_" character
			if ((c <= 0x20 || c >= 0x7f) || ((c >= 0x20 || c <= 0x7f) && (!Character.isLetter(c) && c != 0x5f))) {
				if (c == 0x09) {
					continue; // allow horizontal tabs
				}
				localeString = localeString.replaceFirst(((Character) c).toString(), "");
				len--;
				i--;
			}
		}
		Locale locale = LocaleUtility.fromSpecification(localeString);
		if (LocaleUtility.isValid(locale)) {
			return locale;
		} else {
			return null;
		}
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
		if (localesString == null) {
			return null;
		}
		
		StringBuilder outputString = new StringBuilder();
		
		boolean first = true;
		
		for (String locale : Arrays.asList(localesString.split(","))) {
			Locale loc = normalizeLocale(locale);
			if (loc != null) {
				if (!first) {
					outputString.append(", ");
				} else {
					first = false; // so commas are inserted from now on
				}
				outputString.append(loc.toString());
			}
		}
		if (outputString.length() > 0) {
			return outputString.toString();
		} else {
			return null;
		}
	}
	
	/**
	 * Method that returns WebConstants.WEBAPP_NAME or an empty string if WebConstants.WEBAPP_NAME
	 * is empty.
	 *
	 * @return return WebConstants.WEBAPP_NAME or empty string if WebConstants.WEBAPP_NAME is null
	 * @should return empty string if WebConstants.WEBAPP_NAME is null
	 */
	public static String getContextPath() {
		return StringUtils.isEmpty(WebConstants.WEBAPP_NAME) ? "" : "/" + WebConstants.WEBAPP_NAME;
	}
	
	public static String formatDate(Date date) {
		return formatDate(date, Context.getLocale(), FORMAT_TYPE.DATE);
	}
	
	public static String formatDate(Date date, Locale locale, FORMAT_TYPE type) {
		log.debug("Formatting date: " + date + " with locale " + locale);
		
		DateFormat dateFormat = null;
		
		if (type == FORMAT_TYPE.TIMESTAMP) {
			String dateTimeFormat = Context.getAdministrationService().getGlobalPropertyValue(
			    OpenmrsConstants.GP_SEARCH_DATE_DISPLAY_FORMAT, null);
			if (StringUtils.isEmpty(dateTimeFormat)) {
				dateFormat = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, locale);
			} else {
				dateFormat = new OpenmrsDateFormat(new SimpleDateFormat(dateTimeFormat), locale);
			}
		} else if (type == FORMAT_TYPE.TIME) {
			String timeFormat = Context.getAdministrationService().getGlobalPropertyValue(
			    OpenmrsConstants.GP_SEARCH_DATE_DISPLAY_FORMAT, null);
			if (StringUtils.isEmpty(timeFormat)) {
				dateFormat = DateFormat.getTimeInstance(DateFormat.MEDIUM, locale);
			} else {
				dateFormat = new OpenmrsDateFormat(new SimpleDateFormat(timeFormat), locale);
			}
		} else if (type == FORMAT_TYPE.DATE) {
			String formatValue = Context.getAdministrationService().getGlobalPropertyValue(
			    OpenmrsConstants.GP_SEARCH_DATE_DISPLAY_FORMAT, "");
			if (StringUtils.isEmpty(formatValue)) {
				dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, locale);
			} else {
				dateFormat = new OpenmrsDateFormat(new SimpleDateFormat(formatValue), locale);
			}
		}
		return date == null ? "" : dateFormat.format(date);
	}
	
	/**
	 * @see org.openmrs.api.GlobalPropertyListener#supportsPropertyName(java.lang.String)
	 */
	@Override
	public boolean supportsPropertyName(String propertyName) {
		return OpenmrsConstants.GP_SEARCH_DATE_DISPLAY_FORMAT.equals(propertyName);
	}
	
	public static void setDefaultDateCache(String defaultDateCache) {
		WebUtil.defaultDateCache = defaultDateCache;
	}
	
	/**
	 * @see org.openmrs.api.GlobalPropertyListener#globalPropertyChanged(org.openmrs.GlobalProperty)
	 */
	@Override
	public void globalPropertyChanged(GlobalProperty newValue) {
		setDefaultDateCache(null);
	}
	
	/**
	 * @see org.openmrs.api.GlobalPropertyListener#globalPropertyDeleted(java.lang.String)
	 */
	@Override
	public void globalPropertyDeleted(String propertyName) {
		setDefaultDateCache(null);
	}
}

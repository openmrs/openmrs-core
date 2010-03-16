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

import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.springframework.util.StringUtils;

/**
 * A utility class for working with Locales.
 */
public class LocaleUtility {
	
	private static Log log = LogFactory.getLog(LocaleUtility.class);
	
	/**
	 * Default internal locale.
	 * 
	 * @deprecated use {@link #getDefaultLocale()} now
	 */
	public static final Locale DEFAULT_LOCALE = Locale.UK;
	
	/**
	 * Gets the default locale specified as a global property.
	 * 
	 * @return default locale object.
	 * @since 1.5
	 * @should not return null if global property does not exist
	 * @should not fail with empty global property value
	 * @should not fail with bogus global property value
	 * @should return locale object for global property
	 */
	public static Locale getDefaultLocale() {
		if (Context.isSessionOpen()) {
			try {
				String locale = Context.getAdministrationService().getGlobalProperty(
				    OpenmrsConstants.GLOBAL_PROPERTY_DEFAULT_LOCALE);
				
				if (StringUtils.hasLength(locale)) {
					try {
						return fromSpecification(locale);
					}
					catch (Exception t) {
						log.warn("Unable to parse default locale global property value: " + locale, t);
					}
				}
			}
			catch (Throwable t) {
				// swallow most of the stack trace for most users
				log.warn("Unable to get locale global property value. " + t.getMessage());
				log.trace("Unable to get locale global property value", t);
			}
		}
		
		return fromSpecification(OpenmrsConstants.GLOBAL_PROPERTY_DEFAULT_LOCALE_DEFAULT_VALUE);
	}
	
	/**
	 * Compatible is a looser matching than that provided by Locale.equals(). Two locales are
	 * considered equal if they are equal, or if either does not have a country specified and the
	 * languages match.
	 * 
	 * @param lhs left hand side Locale
	 * @param rhs right hand side Locale
	 * @return true if the two locales are compatible, false otherwise
	 * @should confirm different language missing country as compatible
	 * @should confirm same language missing country as compatible
	 * @should not confirm different country as compatible
	 * @should confirm matching country as compatible
	 * @should not confirm different language as compatible
	 * @should confirm matching language as compatible
	 */
	public static boolean areCompatible(Locale lhs, Locale rhs) {
		if (lhs.equals(rhs)) {
			return true;
		} else if (("".equals(lhs.getCountry())) || ("".equals(rhs.getCountry()))) {
			// no country specified, so language match is good enough
			if (lhs.getLanguage().equals(rhs.getLanguage())) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Creates a locale based on a string specification. The specification must be conform with the
	 * following format: ll_CC_vv <br/>
	 * <ul>
	 * <li>ll: two-character lowercase ISO-639 language code
	 * <li>CC: two-character uppercase ISO-3166 country code optional
	 * <li>vv: arbitrary length variant code
	 * </ul>
	 * For example: en_US_Traditional_WIN ...represents English language in the United States with
	 * the traditional collation for windows.
	 * 
	 * @param localeSpecification encoded locale specification
	 * @return the representative Locale, or null if the specification is invalid
	 * @should get locale from two character language code
	 * @should get locale from language code and country code
	 * @should get locale from language code country code and variant
	 */
	public static Locale fromSpecification(String localeSpecification) {
		Locale createdLocale = null;
		
		localeSpecification = localeSpecification.trim();
		
		String[] localeComponents = localeSpecification.split("_");
		if (localeComponents.length == 1) {
			createdLocale = new Locale(localeComponents[0]);
		} else if (localeComponents.length == 2) {
			createdLocale = new Locale(localeComponents[0], localeComponents[1]);
		} else if (localeComponents.length > 2) {
			String variant = localeSpecification.substring(localeSpecification.indexOf(localeComponents[2])); // gets everything after the second underscore
			createdLocale = new Locale(localeComponents[0], localeComponents[1], variant);
		}
		
		return createdLocale;
	}
}

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

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.GlobalProperty;
import org.openmrs.api.GlobalPropertyListener;
import org.openmrs.api.context.Context;
import org.springframework.util.StringUtils;

/**
 * A utility class for working with Locales.
 */
public class LocaleUtility implements GlobalPropertyListener {
	
	private static Log log = LogFactory.getLog(LocaleUtility.class);
	
	/**
	 * Cached version of the default locale. This is cached so that we don't have to look it up in
	 * the global property table every page load
	 */
	private static Locale defaultLocaleCache = null;
	
	/**
	 * Cached version of the localeAllowedList. This is cached so that we don't have to look it up
	 * in the global property table every page load
	 */
	private static List<Locale> localesAllowedListCache = null;
	
	/**
	 * Default internal locale.
	 *
	 * @deprecated use {@link #getDefaultLocale()} now
	 */
	@Deprecated
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
	 * @should not cache locale when session is not open
	 */
	public static Locale getDefaultLocale() {
		if (defaultLocaleCache == null) {
			if (Context.isSessionOpen()) {
				try {
					String locale = Context.getAdministrationService().getGlobalProperty(
					    OpenmrsConstants.GLOBAL_PROPERTY_DEFAULT_LOCALE);
					
					if (StringUtils.hasLength(locale)) {
						try {
							defaultLocaleCache = fromSpecification(locale);
						}
						catch (Exception t) {
							log.warn("Unable to parse default locale global property value: " + locale, t);
						}
					}
				}
				catch (Exception e) {
					// swallow most of the stack trace for most users
					log.warn("Unable to get locale global property value. " + e.getMessage());
					log.trace("Unable to get locale global property value", e);
				}
				
				// if we weren't able to load the locale from the global property,
				// use the default one
				if (defaultLocaleCache == null) {
					defaultLocaleCache = fromSpecification(OpenmrsConstants.GLOBAL_PROPERTY_DEFAULT_LOCALE_DEFAULT_VALUE);
				}
			} else {
				// if session is not open, return the default locale without caching
				return fromSpecification(OpenmrsConstants.GLOBAL_PROPERTY_DEFAULT_LOCALE_DEFAULT_VALUE);
			}
			
		}
		
		return defaultLocaleCache;
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
		} else if ((("".equals(lhs.getCountry())) || ("".equals(rhs.getCountry())))
		        && lhs.getLanguage().equals(rhs.getLanguage())) {
			// no country specified, so language match is good enough
			return true;
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
			String variant = localeSpecification.substring(localeSpecification.indexOf(localeComponents[2])); // gets everything after the
			// second underscore
			createdLocale = new Locale(localeComponents[0], localeComponents[1], variant);
		}
		
		return createdLocale;
	}
	
	/**
	 * Utility method that returns a collection of all openmrs system locales, the set includes the
	 * current logged in user's preferred locale if any is set, the default locale, allowed locales
	 * in the order they are specified in the 'allowed.locale.list' global property and 'en' at the
	 * very end of the set if it isn't yet among them.
	 *
	 * @returns a collection of all specified and allowed locales with no duplicates.
	 * @should return a set of locales with a predictable order
	 * @should return a set of locales with no duplicates
	 * @should have default locale as the first element if user has no preferred locale
	 * @should have default locale as the second element if user has a preferred locale
	 * @should always have english included in the returned collection
	 * @should always have default locale default value included in the returned collection
	 * @since 1.7
	 */
	public static Set<Locale> getLocalesInOrder() {
		
		Set<Locale> locales = new LinkedHashSet<Locale>();
		locales.add(Context.getLocale());
		locales.add(getDefaultLocale());
		if (localesAllowedListCache == null) {
			localesAllowedListCache = Context.getAdministrationService().getAllowedLocales();
		}
		
		if (localesAllowedListCache != null) {
			locales.addAll(localesAllowedListCache);
		}
		
		locales.add(Locale.ENGLISH);
		locales.add(fromSpecification(OpenmrsConstants.GLOBAL_PROPERTY_DEFAULT_LOCALE_DEFAULT_VALUE));
		
		return locales;
	}
	
	public static void setDefaultLocaleCache(Locale defaultLocaleCache) {
		LocaleUtility.defaultLocaleCache = defaultLocaleCache;
	}
	
	public static void setLocalesAllowedListCache(List<Locale> localesAllowedListCache) {
		LocaleUtility.localesAllowedListCache = localesAllowedListCache;
	}
	
	@Override
	public void globalPropertyChanged(GlobalProperty newValue) {
		// reset the value
		setDefaultLocaleCache(null);
		setLocalesAllowedListCache(null);
	}
	
	@Override
	public void globalPropertyDeleted(String propertyName) {
		// reset the value
		setDefaultLocaleCache(null);
		setLocalesAllowedListCache(null);
	}
	
	@Override
	public boolean supportsPropertyName(String propertyName) {
		return propertyName.equals(OpenmrsConstants.GLOBAL_PROPERTY_DEFAULT_LOCALE)
		        || propertyName.equals(OpenmrsConstants.GLOBAL_PROPERTY_LOCALE_ALLOWED_LIST);
		
	}
	
	/**
	 * Checks if specified locale object is valid
	 *
	 * @param locale
	 *            object for validation
	 * @return true if locale is available
	 */
	public static boolean isValid(Locale locale) {
		try {
			return locale.getISO3Language() != null && locale.getISO3Country() != null;
		}
		catch (MissingResourceException e) {
			return false;
		}
	}
	
}

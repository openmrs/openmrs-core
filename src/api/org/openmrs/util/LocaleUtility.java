package org.openmrs.util;

import java.util.Locale;

/**
 * A utility class for working with Locales.
 * 
 */
public class LocaleUtility {

	/**
	 * Default internal locale.
	 * 
	 * ABKTODO: this should be defined/configured somewhere else
	 */
	public static final Locale DEFAULT_LOCALE = Locale.ENGLISH;

	/**
	 * Compatible is a looser matching than that provided
	 * by Locale.equals(). Two locales are considered equal
	 * if they are equal, or if either does not have a 
	 * country specified and the languages match.
	 * 
	 * @param lhs left hand side Locale
	 * @param rhs right hand side Locale
	 * @return true if the two locales are compatible, false otherwise
	 */
	public static boolean areCompatible(Locale lhs, Locale rhs) {
		if (lhs.equals(rhs)) {
			return true;
		} else if ((lhs.getCountry() == "") ||
			(rhs.getCountry() == "")) {
			// no country specified, so language match is good enough
			if (lhs.getLanguage().equals(rhs.getLanguage())) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Creates a locale based on a string specification. The specification
	 * must be conform with the following format: ll_CC_vv
	 * 
	 * Where:
	 * 	ll two-character lowercase ISO-639 language code
	 *  CC two-character uppercase ISO-3166 country code optional
	 *  vv arbitrary length variant code
	 * 
	 * For example:
	 * 	en_US_Traditional_WIN
	 * ...represents English language in the United States with the traditional collation for windows.
	 * 
	 * @param localeSpecification encoded locale specification
	 * @return the representative Locale, or null if the specification is invalid
	 */
	public static Locale fromSpecification(String localeSpecification)
	{
		Locale createdLocale = null;
		
		localeSpecification = localeSpecification.trim();
		
		String[] localeComponents = localeSpecification.split("_");
		if (localeComponents.length == 1) {
			createdLocale = new Locale(localeComponents[0]);
		}
		else if (localeComponents.length == 2) {
			createdLocale = new Locale(localeComponents[0], localeComponents[1]);
		}
		else if (localeComponents.length > 2) {
			String variant = localeSpecification.substring(localeSpecification.indexOf(localeComponents[2]));
			createdLocale = new Locale(localeComponents[0], localeComponents[1], variant);
		}
		
		return createdLocale;
	}
}

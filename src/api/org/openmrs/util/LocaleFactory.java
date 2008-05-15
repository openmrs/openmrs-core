package org.openmrs.util;

import java.util.Locale;

/**
 * A factory for creating Locales.
 * 
 */
public class LocaleFactory {

	/**
	 * Default internal locale.
	 * 
	 * ABKTODO: this should be defined/configured somewhere else
	 */
	public static final Locale DEFAULT_LOCALE = Locale.ENGLISH;
	
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

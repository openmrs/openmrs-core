package org.openmrs.web;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;


public class WebConstants {
	
	public static final String OPENMRS_CONTEXT_HTTPSESSION_ATTR			= "__openmrs_context";
	public static final String OPENMRS_CLIENT_IP_HTTPSESSION_ATTR 		= "__openmrs_client_ip";
	public static final String OPENMRS_LOGIN_REDIRECT_HTTPSESSION_ATTR	= "__openmrs_login_redirect"; // TODO change login.jsp to use this constant
	
	public static final String OPENMRS_MSG_ATTR   = "openmrs_msg";
	public static final String OPENMRS_ERROR_ATTR = "openmrs_error";
	
	public static final String OPENMRS_LANGUAGE_COOKIE_NAME = "__openmrs_language";
	
	/**
	 * @return Collection of locales available to openmrs
	 */
	public static final Collection<Locale> OPENMRS_LOCALES() {
		List<Locale> languages = new Vector<Locale>();
		
		languages.add(Locale.US);
		languages.add(Locale.UK);
		languages.add(Locale.FRENCH);
		languages.add(Locale.GERMAN);
		
		return languages;
	}
	
	/**
	 * This method is necessary until SimpleDateFormat(java.util.locale) returns a 
	 *   pattern with a four digit year
	 *   
	 * @return Mapping of Locales to locale specific date pattern
	 */
	public static final Map<String, String> OPENMRS_LOCALE_DATE_PATTERNS() {
		Map<String, String> patterns = new HashMap<String, String>();
		
		patterns.put(Locale.US.toString().toLowerCase(), "MM/dd/yyyy");
		patterns.put(Locale.UK.toString().toLowerCase(), "dd/MM/yyyy");
		patterns.put(Locale.FRENCH.toString().toLowerCase(), "dd/MM/yyyy");
		patterns.put(Locale.GERMAN.toString().toLowerCase(), "MM.dd.yyyy");
		
		return patterns;
	}

}
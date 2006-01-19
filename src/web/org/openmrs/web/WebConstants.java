package org.openmrs.web;

import java.util.HashMap;
import java.util.Map;


public class WebConstants {
	
	public static final String OPENMRS_CONTEXT_HTTPSESSION_ATTR = "__openmrs_context";
	public static final String OPENMRS_CLIENT_IP_HTTPSESSION_ATTR = "__openmrs_client_ip";
	public static final String OPENMRS_LOGIN_REDIRECT_HTTPSESSION_ATTR = "__openmrs_login_redirect"; // TODO change login.jsp to use this constant
	
	public static final String OPENMRS_MSG_ATTR = "openmrs_msg";
	public static final String OPENMRS_ERROR_ATTR = "openmrs_error";
	
	public static final String OPENMRS_LANGUAGE_COOKIE_NAME = "__openmrs_language";
	
	public static final Map<String, String> OPENMRS_LANGUAGES() {
		Map<String, String> languages = new HashMap<String, String>();
		
		languages.put("en", "English");
		languages.put("fr", "Français");
		languages.put("de", "Deutsch");
		
		return languages;
	}

}

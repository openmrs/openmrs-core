package org.openmrs.web;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

public class Util {

	/**
	 * This function is meant for classes/scripts that do not have access to Spring's
	 *   RequestContext.getLocale() function.   
	 * 
	 * @param request
	 * @return locale specific by cookie or fallback to request attribute (user's browser default)
	 */
	public static Locale getLocale(HttpServletRequest request) {
		Locale locale = new OpenmrsCookieLocaleResolver().resolveLocale(request); 
		
		return locale;
	}
	
	public static String escapeHTML(String s) {
		
		s = s.replace("<", "&lt;");
		s = s.replace(">", "&gt;");
		
		return s;
	}


}

package org.openmrs.web;

import java.util.Locale;

import javax.servlet.http.Cookie;
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
		Locale locale = request.getLocale();
		Cookie[] cookies = request.getCookies();
		for (Cookie c : cookies) {
			if (c.getName().equals(Constants.OPENMRS_LANGUAGE_COOKIE_NAME))
				locale = new Locale(c.getValue());
		}
		
		return locale;
	}


}

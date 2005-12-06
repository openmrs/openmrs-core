package org.openmrs.web;

import org.springframework.web.servlet.i18n.CookieLocaleResolver;

public class OpenmrsCookieLocaleResolver extends CookieLocaleResolver {

	/**
	 * @see org.springframework.web.util.CookieGenerator#getCookieName()
	 */
	public String getCookieName() {
		
		return Constants.OPENMRS_LANGUAGE_COOKIE_NAME;
	}
	
}
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

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.openmrs.api.context.Context;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;

public class OpenmrsCookieLocaleResolver extends CookieLocaleResolver {
	
	public Locale resolveLocale(HttpServletRequest request) {
		
		Locale locale;
		
		locale = Context.getLocale();
		if (locale != null) {
			return locale;
		}
		
		//fall back to cookie that was set
		return super.resolveLocale(request);
	}
	
	public void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale) {
		
		HttpSession session = (HttpSession) request.getSession();
		
		// if a user clicks on the locale change links 
		// AND their current default locale is different (so the msg isn't repeated)
		if (request.getParameter("lang") != null && Context.isAuthenticated() && !Context.getLocale().equals(locale)) {
			session.setAttribute(WebConstants.OPENMRS_MSG_ARGS, request.getContextPath());
			session.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "User.localeChangeHint");
		}
		
		Context.setLocale(locale);
		
		response.setLocale(locale);
		
		//still set the cookie for later possible use.
		super.setLocale(request, response, locale);
		
	}
	
	public String getCookieName() {
		return WebConstants.OPENMRS_LANGUAGE_COOKIE_NAME;
	}
	
}

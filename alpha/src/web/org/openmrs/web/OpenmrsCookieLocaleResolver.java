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
		if (locale != null)
			return locale;
				
		//fall back to cookie that was set
		return super.resolveLocale(request);
	}

	public void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale) {

		Context.setLocale(locale);
		
		response.setLocale(locale);
		
		//still set the cookie for later possible use.
		super.setLocale(request, response, locale);
		
		HttpSession session = (HttpSession)request.getSession();
		
		// if a user clicks on the locale change links and their current default locale is null
		if (request.getParameter("lang") != null)
			if (Context.isAuthenticated())
				session.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "User.localeChangeHint");
	}

	public String getCookieName() {
		return WebConstants.OPENMRS_LANGUAGE_COOKIE_NAME;
	}

}
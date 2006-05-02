package org.openmrs.web;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.openmrs.api.context.Context;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;

public class OpenmrsCookieLocaleResolver extends CookieLocaleResolver {

	public Locale resolveLocale(HttpServletRequest request) {
		
		HttpSession httpSession = request.getSession();
		Context context = (Context)httpSession.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		
		Locale locale;
		
		if (context != null) {
			locale = context.getLocale();
			if (locale != null)
				return locale;
		}
				
		//fall back to cookie that was set
		return super.resolveLocale(request);
	}

	public void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale) {
		HttpSession httpSession = request.getSession();
		Context context = (Context)httpSession.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		
		if (context != null)
			context.setLocale(locale);
		
		response.setLocale(locale);
		
		//still set the cookie for later possible use.
		super.setLocale(request, response, locale);
	}

	public String getCookieName() {
		return WebConstants.OPENMRS_LANGUAGE_COOKIE_NAME;
	}

}
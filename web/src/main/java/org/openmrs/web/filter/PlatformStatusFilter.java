/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.filter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.openmrs.web.filter.initialization.InitializationFilter;
import org.openmrs.web.filter.update.UpdateFilter;
import org.openmrs.web.filter.util.CustomResourceLoader;
import org.openmrs.web.filter.util.FilterUtil;

/**
 * This is the fourth filter that is processed. It is only active when OpenMRS has
 * successfully started and has no database updates to run.
 */
public class PlatformStatusFilter extends StartupFilter {

	/**
	 * This url is called by javascript to get the status of the install
	 */
	private static final String PROGRESS_VM_AJAXREQUEST = "progress.vm.ajaxRequest";
	
	private static final String DEFAULT_PAGE = "running.vm";
	
	@Override
	protected void doGet(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse) throws IOException,
			ServletException {
		
		Map<String, Object> referenceMap = new HashMap<String, Object>();
		checkLocaleAttributesForFirstTime(httpRequest);
		// we need to save current user language in references map since it will be used when template
		// will be rendered
		if (httpRequest.getSession().getAttribute(FilterUtil.LOCALE_ATTRIBUTE) != null) {
			referenceMap
			        .put(FilterUtil.LOCALE_ATTRIBUTE, httpRequest.getSession().getAttribute(FilterUtil.LOCALE_ATTRIBUTE));
		}
		
		renderTemplate(DEFAULT_PAGE, referenceMap, httpResponse);
	}

	@Override
	protected void doPost(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse) throws IOException,
			ServletException {
	}

	@Override
	protected Object getModel() {
		return new StatusFilterModel();
	}

	@Override
	public boolean skipFilter(HttpServletRequest request) {
		return !(!PROGRESS_VM_AJAXREQUEST.equals(request.getParameter("page")) && !UpdateFilter.updatesRequired() && !InitializationFilter.initializationRequired());
	}
	
	/**
	 * It sets locale attribute for current session when user is making first GET http request
	 * to application. It retrieves user locale from request object and checks if this locale is
	 * supported by application. If not, it tries to load system default locale. If it's not specified it 
	 * uses {@link Locale#ENGLISH} by default
	 *
	 * @param httpRequest the http request object
	 */
	public void checkLocaleAttributesForFirstTime(HttpServletRequest httpRequest) {
		Locale locale = httpRequest.getLocale();
		String systemDefaultLocale = FilterUtil.readSystemDefaultLocale(null);
		if (CustomResourceLoader.getInstance(httpRequest).getAvailablelocales().contains(locale)) {
			httpRequest.getSession().setAttribute(FilterUtil.LOCALE_ATTRIBUTE, locale.toString());
			log.info("Used client's locale " + locale.toString());
		} else if (StringUtils.isNotBlank(systemDefaultLocale)) {
			httpRequest.getSession().setAttribute(FilterUtil.LOCALE_ATTRIBUTE, systemDefaultLocale);
			log.info("Used system default locale " + systemDefaultLocale);
		} else {
			httpRequest.getSession().setAttribute(FilterUtil.LOCALE_ATTRIBUTE, Locale.ENGLISH.toString());
			log.info("Used default locale " + Locale.ENGLISH.toString());
		}
	}
}

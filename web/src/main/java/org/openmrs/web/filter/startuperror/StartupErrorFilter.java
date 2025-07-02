/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.filter.startuperror;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.openmrs.web.Listener;
import org.openmrs.web.filter.StartupFilter;

/**
 * This is the second filter that is processed. It is only active when OpenMRS has some liquibase
 * updates that need to be run. If updates are needed, this filter/wizard asks for a super user to
 * authenticate and review the updates before continuing.
 */
public class StartupErrorFilter extends StartupFilter {
	
	/**
	 * The velocity macro page to redirect to if an error occurs or on initial startup
	 */
	private static final String DEFAULT_PAGE = "generalerror.vm";
	
	/**
	 * Called by {@link #doFilter(ServletRequest, ServletResponse, FilterChain)} on GET requests
	 *
	 * @param httpRequest
	 * @param httpResponse
	 */
	@Override
	protected void doGet(HttpServletRequest httpRequest, HttpServletResponse httpResponse)
		throws IOException, ServletException {

		if (getUpdateFilterModel().errorAtStartup != null) {
			renderTemplate(DEFAULT_PAGE, new HashMap<>(), httpResponse);
		}
	}
	
	/**
	 * @see org.openmrs.web.filter.StartupFilter#doPost(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doPost(HttpServletRequest httpRequest, HttpServletResponse httpResponse)
	        throws IOException, ServletException {
		// No longer invoking doPost() since core module upload is deprecated
		httpResponse.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "POST not supported during setup.");
	}
	
	/**
	 * @see org.openmrs.web.filter.StartupFilter#getUpdateFilterModel()
	 */
	@Override
	protected StartupErrorFilterModel getUpdateFilterModel() {
		// this object was initialized in the #init(FilterConfig) method
		return new StartupErrorFilterModel(Listener.getErrorAtStartup());
	}
	
	/**
	 * @see org.openmrs.web.filter.StartupFilter#skipFilter(HttpServletRequest)
	 */
	@Override
	public boolean skipFilter(HttpServletRequest request) {
		return !Listener.errorOccurredAtStartup();
	}
	
	/**
	 * @see org.openmrs.web.filter.StartupFilter#getTemplatePrefix()
	 */
	@Override
	protected String getTemplatePrefix() {
		return "org/openmrs/web/filter/startuperror/";
	}
	
}

/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.controller;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openmrs.GlobalProperty;
import org.openmrs.api.GlobalPropertyListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.mvc.LastModified;

/**
 * This controller basically passes requests straight through to their views. When interpretJstl is
 * enabled, ".withjstl" is appended to the view name. (This allows us to use jstl (such as the
 * spring:message tag) in some javascript files.) <br>
 * If you specify any 'rewrites' then the specified paths are remapped, e.g:<br>
 * /scripts/jquery/jquery-1.3.2.min.js -&gt; /scripts/jquery/jquery.min.js <br>
 * All jstl files are cached in the browser until a server restart or a global property is
 * added/changed/deleted
 */
public class PseudoStaticContentController implements Controller, LastModified, GlobalPropertyListener {
	
	private static final Logger log = LoggerFactory.getLogger(PseudoStaticContentController.class);
	
	private Boolean interpretJstl = false;
	
	private Map<String, String> rewrites;
	
	private static Long lastModified = System.currentTimeMillis();
	
	public Boolean getInterpretJstl() {
		return interpretJstl;
	}
	
	public void setInterpretJstl(Boolean interpretJstl) {
		this.interpretJstl = interpretJstl;
	}
	
	public Map<String, String> getRewrites() {
		return rewrites;
	}
	
	public void setRewrites(Map<String, String> rewrites) {
		this.rewrites = rewrites;
	}
	
	@Override
	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException,
	        IOException {
		String path = request.getServletPath() + request.getPathInfo();
		
		if (rewrites != null && rewrites.containsKey(path)) {
			path = rewrites.get(path);
		}
		if (interpretJstl) {
			path += ".withjstl";
		}
		
		return new ModelAndView(path);
	}
	
	@Override
	public long getLastModified(HttpServletRequest request) {
		
		// return a mostly constant last modified date for all files passing
		// through the jsp (.withjstl) servlet
		// this allows the files to cache until we say so
		if (interpretJstl) {
			log.debug("returning last modified date of : {} for : {}", lastModified, request.getPathInfo());
			return lastModified;
		}
		
		// the spring servletdispatcher will try to get the lastModified date
		// from the actual file in this case
		return -1;
	}
	
	public static void setLastModified(Long lastModified) {
		PseudoStaticContentController.lastModified = lastModified;
	}
	
	@Override
	public void globalPropertyChanged(GlobalProperty newValue) {
		// reset for every global property change
		setLastModified(System.currentTimeMillis());
	}
	
	@Override
	public void globalPropertyDeleted(String propertyName) {
		// reset for every global property change
		setLastModified(System.currentTimeMillis());
	}
	
	@Override
	public boolean supportsPropertyName(String propertyName) {
		return true;
	}
	
	public static void invalidateCachedResources(Map<String, String> newValue) {
		setLastModified(System.currentTimeMillis());
	}
}
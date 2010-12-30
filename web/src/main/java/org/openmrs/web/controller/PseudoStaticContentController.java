/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.web.controller;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.GlobalProperty;
import org.openmrs.api.GlobalPropertyListener;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.mvc.LastModified;

/**
 * This controller basically passes requests straight through to their views.
 * When interpretJstl is enabled, ".withjstl" is appended to the view name.
 * (This allows us to use jstl (such as the spring:message tag) in some
 * javascript files.)
 * 
 * <br/>
 * If you specify any 'rewrites' then the specified paths are remapped, e.g:<br/>
 * /scripts/jquery/jquery-1.3.2.min.js -> /scripts/jquery/jquery.min.js
 * 
 * <br/>
 * All jstl files are cached in the browser until a server restart or a global
 * property is added/changed/deleted
 */
public class PseudoStaticContentController implements Controller, LastModified, GlobalPropertyListener {
	
	protected final Log log = LogFactory.getLog(getClass());
	
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
	
	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException,
	                                                                                           IOException {
		String path = request.getServletPath() + request.getPathInfo();
		
		if (rewrites != null && rewrites.containsKey(path))
			path = rewrites.get(path);
		if (interpretJstl)
			path += ".withjstl";
		
		return new ModelAndView(path);
	}
	
	@Override
	public long getLastModified(HttpServletRequest request) {
		
		// return a mostly constant last modified date for all files passing
		// through the jsp (.withjstl) servlet
		// this allows the files to cache until we say so
		if (interpretJstl) {
			if (log.isDebugEnabled())
				log.debug("returning last modified date of : " + lastModified + " for : " + request.getPathInfo());
			return lastModified;
		}
		
		// the spring servletdispatcher will try to get the lastModified date
		// from the actual file in this case
		return -1;
	}
	
	@Override
	public void globalPropertyChanged(GlobalProperty newValue) {
		// reset for every global property change
		lastModified = System.currentTimeMillis();
	}
	
	@Override
	public void globalPropertyDeleted(String propertyName) {
		// reset for every global property change
		lastModified = System.currentTimeMillis();
	}
	
	@Override
	public boolean supportsPropertyName(String propertyName) {
		return true;
	}
}

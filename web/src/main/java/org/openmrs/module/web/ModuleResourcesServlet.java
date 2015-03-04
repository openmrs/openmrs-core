/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.Module;
import org.openmrs.module.ModuleUtil;
import org.openmrs.util.OpenmrsUtil;

public class ModuleResourcesServlet extends HttpServlet {
	
	private static final String MODULE_PATH = "/WEB-INF/view/module/";
	
	private static final long serialVersionUID = 1239820102030344L;
	
	private Log log = LogFactory.getLog(this.getClass());
	
	/**
	 * Used for caching purposes
	 *
	 * @see javax.servlet.http.HttpServlet#getLastModified(javax.servlet.http.HttpServletRequest)
	 */
	@Override
	protected long getLastModified(HttpServletRequest req) {
		File f = getFile(req);
		
		if (f == null) {
			return super.getLastModified(req);
		}
		
		return f.lastModified();
	}
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		log.debug("In service method for module servlet: " + request.getPathInfo());
		
		File f = getFile(request);
		if (f == null) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		
		response.setDateHeader("Last-Modified", f.lastModified());
		response.setContentLength(Long.valueOf(f.length()).intValue());
		String mimeType = getServletContext().getMimeType(f.getName());
		response.setContentType(mimeType);
		
		FileInputStream is = new FileInputStream(f);
		try {
			OpenmrsUtil.copyFile(is, response.getOutputStream());
		}
		finally {
			OpenmrsUtil.closeStream(is);
		}
	}
	
	/**
	 * Turns the given request/path into a File object
	 *
	 * @param request the current http request
	 * @return the file being requested or null if not found
	 */
	protected File getFile(HttpServletRequest request) {
		
		String path = request.getPathInfo();
		
		Module module = ModuleUtil.getModuleForPath(path);
		if (module == null) {
			log.warn("No module handles the path: " + path);
			return null;
		}
		
		String relativePath = ModuleUtil.getPathForResource(module, path);
		String realPath = getServletContext().getRealPath("") + MODULE_PATH + module.getModuleIdAsPath() + "/resources"
		        + relativePath;
		realPath = realPath.replace("/", File.separator);
		
		File f = new File(realPath);
		if (!f.exists()) {
			log.warn("No file with path '" + realPath + "' exists for module '" + module.getModuleId() + "'");
			return null;
		}
		
		return f;
	}
	
}

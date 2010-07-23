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
	
	private final String MODULE_PATH = "/WEB-INF/view/module/";
	
	private static final long serialVersionUID = 1239820102030344L;
	
	private Log log = LogFactory.getLog(this.getClass());
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		log.debug("In service method for module servlet: " + request.getPathInfo());
		
		String path = request.getPathInfo();
		
		Module module = ModuleUtil.getModuleForPath(path);
		if (module == null) {
			log.warn("No module handles the path: " + path);
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		String relativePath = ModuleUtil.getPathForResource(module, path);
		String realPath = getServletContext().getRealPath("") + MODULE_PATH + module.getModuleIdAsPath() + "/resources" + relativePath;
		realPath = realPath.replace("/", File.separator);
		
		log.debug("Real path: " + realPath);
		
		File f = new File(realPath);
		if (!f.exists()) {
			log.warn("No object with path '" + realPath + "' exists for module '" + module.getModuleId() + "'");
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		
		response.setDateHeader("Last-Modified", f.lastModified());
		response.setContentLength(new Long(f.length()).intValue());
		
		FileInputStream is = new FileInputStream(f);
		try {
			OpenmrsUtil.copyFile(is, response.getOutputStream());
		}
		finally {
			OpenmrsUtil.closeStream(is);
		}
		
	}
	
}

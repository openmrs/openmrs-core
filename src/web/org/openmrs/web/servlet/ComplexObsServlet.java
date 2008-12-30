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
package org.openmrs.web.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Obs;
import org.openmrs.api.ObsService;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsUtil;
import org.openmrs.web.WebConstants;

public class ComplexObsServlet extends HttpServlet {
	
	public static final long serialVersionUID = 1234432L;
	
	private static final Log log = LogFactory.getLog(ComplexObsServlet.class);
	
	/**
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String obsId = request.getParameter("obsId");
		String view = request.getParameter("view");
		String viewType = request.getParameter("viewType");
		
		HttpSession session = request.getSession();
		
		if (obsId == null || obsId.length() == 0) {
			session.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "error.null");
			return;
		}
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_VIEW_OBS)) {
			session.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "Privilege required: " + OpenmrsConstants.PRIV_VIEW_OBS);
			session.setAttribute(WebConstants.OPENMRS_LOGIN_REDIRECT_HTTPSESSION_ATTR, request.getRequestURI() + "?"
			        + request.getQueryString());
			response.sendRedirect(request.getContextPath() + "/login.htm");
			return;
		}
		
		ObsService service = Context.getObsService();
		Obs complexObs = service.getObs(Integer.valueOf(obsId));
		
		/*
		 * TODO: Should extracting the filename, etc. be done by the handler?
		 */
		// Extract the url from the Obs.valueComplex and create a file.
		String[] valComplex = complexObs.getValueComplex().split("\\|");
		String url = (valComplex.length < 2) ? valComplex[0] : valComplex[valComplex.length - 1];
		File file = new File(url);
		
		if (!file.exists()) {
			// Try searching in the Application Data Directory.
			String fileName = file.getName();
			File dir = OpenmrsUtil.getDirectoryInApplicationDataDirectory(Context.getAdministrationService()
			        .getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_COMPLEX_OBS_DIR));
			file = new File(dir, fileName);
		}
		if (!file.exists()) {
			throw new ServletException("The file: " + file + " does not exist.");
		}
		
		String filename = response.encodeURL(url);
		
		if ("download".equals(viewType)) {
			response.setHeader("Content-Disposition", "attachment; filename=" + filename);
			response.setHeader("Pragma", "no-cache");
		}
		
		log.debug("Copying file stream to outputstream: " + file.getAbsolutePath());
		
		InputStream inStream = null;
		try {
			inStream = new FileInputStream(file);
			OpenmrsUtil.copyFile(inStream, response.getOutputStream());
		}
		finally {
			if (inStream != null)
				try {
					inStream.close();
				}
				catch (Exception e) {}
		}
		
	}
	
}

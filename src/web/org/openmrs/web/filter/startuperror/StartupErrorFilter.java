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
package org.openmrs.web.filter.startuperror;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.RequestContext;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.servlet.ServletRequestContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.ModuleUtil;
import org.openmrs.module.OpenmrsRequiredModuleException;
import org.openmrs.web.Listener;
import org.openmrs.web.filter.StartupFilter;

/**
 * This is the second filter that is processed. It is only active when OpenMRS has some liquibase
 * updates that need to be run. If updates are needed, this filter/wizard asks for a super user to
 * authenticate and review the updates before continuing.
 */
public class StartupErrorFilter extends StartupFilter {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	/**
	 * The velocity macro page to redirect to if an error occurs or on initial startup
	 */
	private final String DEFAULT_PAGE = "generalerror.vm";
	
	/**
	 * Called by {@link #doFilter(ServletRequest, ServletResponse, FilterChain)} on GET requests
	 * 
	 * @param httpRequest
	 * @param httpResponse
	 */
	protected void doGet(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException,
	                                                                                      ServletException {
		
		if (getModel().errorAtStartup instanceof OpenmrsRequiredModuleException)
			renderTemplate("requiredmoduleerror.vm", new HashMap<String, Object>(), httpResponse);
		else
			renderTemplate(DEFAULT_PAGE, new HashMap<String, Object>(), httpResponse);
	}
	
	/**
	 * @see org.openmrs.web.filter.StartupFilter#doPost(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doPost(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException,
	                                                                                       ServletException {
		// if they are uploading modules
		if (getModel().errorAtStartup instanceof OpenmrsRequiredModuleException) {
			RequestContext requestContext = new ServletRequestContext(httpRequest);
			if (!ServletFileUpload.isMultipartContent(requestContext))
				throw new ServletException("The request is not a valid multipart/form-data upload request");
			
			FileItemFactory factory = new DiskFileItemFactory();
			ServletFileUpload upload = new ServletFileUpload(factory);
			try {
				Context.openSession();
				List<FileItem> items = upload.parseRequest(requestContext);
				for (FileItem item : items) {
					InputStream uploadedStream = item.getInputStream();
					ModuleUtil.insertModuleFile(uploadedStream, item.getName());
				}
			}
			catch (FileUploadException ex) {
				throw new ServletException("Error while uploading file(s)", ex);
			}
			finally {
				Context.closeSession();
			}
			
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("success", Boolean.TRUE);
			renderTemplate("requiredmoduleerror.vm", map, httpResponse);
		}
	}
	
	/**
	 * @see org.openmrs.web.filter.StartupFilter#getModel()
	 */
	protected StartupErrorFilterModel getModel() {
		// this object was initialized in the #init(FilterConfig) method
		return new StartupErrorFilterModel(Listener.getErrorAtStartup());
	}
	
	/**
	 * @see org.openmrs.web.filter.StartupFilter#skipFilter()
	 */
	public boolean skipFilter(HttpServletRequest request) {
		return !Listener.errorOccurredAtStartup();
	}
	
	/**
	 * @see org.openmrs.web.filter.StartupFilter#getTemplatePrefix()
	 */
	protected String getTemplatePrefix() {
		return "org/openmrs/web/filter/startuperror/";
	}
	
}

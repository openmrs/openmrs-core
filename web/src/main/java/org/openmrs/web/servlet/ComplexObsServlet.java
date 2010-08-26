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

import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Obs;
import org.openmrs.api.context.Context;
import org.openmrs.obs.ComplexData;
import org.openmrs.util.OpenmrsUtil;
import org.openmrs.util.PrivilegeConstants;
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
		if (!Context.hasPrivilege(PrivilegeConstants.VIEW_OBS)) {
			session.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "Privilege required: " + PrivilegeConstants.VIEW_OBS);
			session.setAttribute(WebConstants.OPENMRS_LOGIN_REDIRECT_HTTPSESSION_ATTR, request.getRequestURI() + "?"
			        + request.getQueryString());
			response.sendRedirect(request.getContextPath() + "/login.htm");
			return;
		}
		
		Obs complexObs = Context.getObsService().getComplexObs(Integer.valueOf(obsId), view);
		ComplexData cd = complexObs.getComplexData();
		Object data = cd.getData();
		
		if ("download".equals(viewType)) {
			response.setHeader("Content-Disposition", "attachment; filename=" + cd.getTitle());
			response.setHeader("Pragma", "no-cache");
		}
		
		if (data instanceof byte[]) {
			ByteArrayInputStream stream = new ByteArrayInputStream((byte[]) data);
			OpenmrsUtil.copyFile(stream, response.getOutputStream());
		} else if (RenderedImage.class.isAssignableFrom(data.getClass())) {
			RenderedImage img = (RenderedImage) data;
			String[] parts = cd.getTitle().split(".");
			String extension = "jpg"; // default extension
			if (parts.length > 0) {
				extension = parts[parts.length - 1];
			}
			
			ImageIO.write(img, extension, response.getOutputStream());
		} else if (InputStream.class.isAssignableFrom(data.getClass())) {
			InputStream stream = (InputStream) data;
			OpenmrsUtil.copyFile(stream, response.getOutputStream());
			stream.close();
		} else {
			throw new ServletException("Couldn't serialize complex obs data for obsId=" + obsId + " of type "
			        + data.getClass());
		}
	}
	
}

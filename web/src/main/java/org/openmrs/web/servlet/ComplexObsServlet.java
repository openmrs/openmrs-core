/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
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
		String download = request.getParameter("download");
		
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
		
		if (null != download) {
			response.setHeader("Content-Disposition", "attachment; filename=" + cd.getTitle());
			response.setHeader("Pragma", "no-cache");
		}
		
		String mimeType = cd.getMimeType();
		
		if (null != mimeType) {
			response.setHeader("Content-Type", mimeType);
		}
		
		Long length = cd.getLength();
		if (length != null) {
			response.setHeader("Content-Length", String.valueOf(length));
			response.setHeader("Accept-Ranges", "bytes");
		}
		
		if (data instanceof byte[]) {
			ByteArrayInputStream stream = new ByteArrayInputStream((byte[]) data);
			OpenmrsUtil.copyFile(stream, response.getOutputStream());
		} else if (RenderedImage.class.isAssignableFrom(data.getClass())) {
			RenderedImage img = (RenderedImage) data;
			String[] parts = cd.getTitle().split("\\.");
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

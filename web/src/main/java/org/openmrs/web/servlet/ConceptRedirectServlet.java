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

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class simply creates a shortened URL for concept links /concept/### to
 * /dictionary/concept.htm?conceptId=### /concept to /dictionary/index.htm /concept/** to
 * /dictionary/index.htm?phrase=**
 */
public class ConceptRedirectServlet extends HttpServlet {
	
	public static final long serialVersionUID = 1231231123454545L;
	
	private Log log = LogFactory.getLog(this.getClass());
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String contextPath = request.getContextPath();
		String path = request.getPathInfo();
		
		log.debug("path info: " + path);
		if (path != null) {
			path = path.substring(1); // remove first slash
			Integer nextSlash = path.indexOf("/");
			if (nextSlash != -1) {
				path = path.substring(0, nextSlash);
			}
			log.debug("new concept id: " + path);
			try {
				Integer i = Integer.valueOf(path);
				// view the concept if the path info was an integer 
				response.sendRedirect(contextPath + "/dictionary/concept.htm?conceptId=" + i);
				return;
			}
			catch (Exception e) {
				// search for the concept if the path info was not an integer
				response.sendRedirect(contextPath + "/dictionary/index.htm?phrase=" + path);
				return;
			}
		}
		
		// redirect to the search screen if there wasn't any path info
		response.sendRedirect(contextPath + "/dictionary");
	}
	
}

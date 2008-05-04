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

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * This class simply creates a shortened URL for concept links 
 * 
 * /concept/### to /dictionary/concept.htm?conceptId=###
 * /concept to /dictionary/index.htm
 * /concept/** to /dictionary/index.htm?phrase=**
 * 

 *
 */
public class ConceptRedirectServlet extends HttpServlet {

	public static final long serialVersionUID = 1231231123454545L;
	private Log log = LogFactory.getLog(this.getClass());
	
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		
		String contextPath = request.getContextPath();
		String path = request.getPathInfo();
		
		log.debug("path info: " + path);
		if (path != null) {
			path = path.substring(1); // remove first slash
			Integer nextSlash = path.indexOf("/");
			if (nextSlash != -1)
				path = path.substring(0, nextSlash);
			log.debug("new concept id: " + path);
			try {
				Integer i = new Integer(path); 
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
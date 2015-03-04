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
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;

public abstract class AbstractGraphServlet extends HttpServlet {
	
	public static final long serialVersionUID = 1231231L;
	
	private Log log = LogFactory.getLog(AbstractGraphServlet.class);
	
	// Supported mime types
	public static final String PNG_MIME_TYPE = "image/png";
	
	public static final String JPG_MIME_TYPE = "image/jpeg";
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		try {
			// Set default values
			Integer width = Integer.valueOf(500);
			Integer height = Integer.valueOf(300);
			String mimeType = PNG_MIME_TYPE;
			
			// Retrieve custom values
			try {
				width = Integer.parseInt(request.getParameter("width"));
			}
			catch (Exception e) {
				log.error("Error during width parseInt", e);
			}
			
			try {
				height = Integer.parseInt(request.getParameter("height"));
			}
			catch (Exception e) {
				log.error("Error during height parseInt", e);
			}
			
			if (request.getParameter("mimeType") != null) {
				mimeType = request.getParameter("mimeType");
			}
			
			JFreeChart chart = createChart(request, response);
			
			// Modify response to disable caching
			response.setHeader("Pragma", "No-cache");
			response.setDateHeader("Expires", 0);
			response.setHeader("Cache-Control", "no-cache");
			
			// Write chart out to response as image 
			try {
				if (JPG_MIME_TYPE.equalsIgnoreCase(mimeType)) {
					response.setContentType(JPG_MIME_TYPE);
					ChartUtilities.writeChartAsJPEG(response.getOutputStream(), chart, width, height);
				} else if (PNG_MIME_TYPE.equalsIgnoreCase(mimeType)) {
					response.setContentType(PNG_MIME_TYPE);
					ChartUtilities.writeChartAsPNG(response.getOutputStream(), chart, width, height);
				} else {
					// Throw exception: unsupported mime type
				}
			}
			catch (IOException e) {
				log.error(e);
			}
			
		}
		// Add error handling above and remove this try/catch 
		catch (Exception e) {
			log.error(e);
		}
	}
	
	/**
	 * Override this method for each graph
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	protected abstract JFreeChart createChart(HttpServletRequest request, HttpServletResponse response);
	
	/**
	 * 
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
	
}

/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.hl7.web;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.openmrs.api.context.Context;
import org.openmrs.hl7.HL7InQueueProcessor;
import org.openmrs.web.WebConstants;

import ca.uhn.hl7v2.HL7Exception;

public class HL7InQueueProcessorServlet extends HttpServlet {
	
	private static final long serialVersionUID = -5108204671262339759L;
	
	private static HL7InQueueProcessor processor;
	
	// private Log log = LogFactory.getLog(this.getClass());
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession httpSession = request.getSession();
		
		if (!Context.isAuthenticated()) {
			httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "auth.session.expired");
			response.sendRedirect(request.getContextPath() + "/logout");
			return;
		}
		
		ServletOutputStream out = response.getOutputStream();
		
		try {
			getHL7InQueueProcessor().processHL7InQueue();
			out.print("HL7 inbound queue processor has started");
		}
		catch (HL7Exception e) {
			out.print("Unable to start HL7 inbound queue processor. Perhaps it is already going?");
		}
		
	}
	
	/**
	 * Get the HL7 In queue queue processor.
	 * 
	 * @return an instance of the HL7 In queue processor
	 */
	private HL7InQueueProcessor getHL7InQueueProcessor() {
		if (processor == null) {
			processor = new HL7InQueueProcessor();
		}
		return processor;
	}
}

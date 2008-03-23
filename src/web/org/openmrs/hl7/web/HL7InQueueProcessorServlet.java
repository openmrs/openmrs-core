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

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		HttpSession httpSession = request.getSession();

		if (!Context.isAuthenticated()) {
			httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR,
					"auth.session.expired");
			response.sendRedirect(request.getContextPath() + "/logout");
			return;
		}

		ServletOutputStream out = response.getOutputStream();

		try {
			getHL7InQueueProcessor().processHL7InQueue();
			out.print("HL7 inbound queue processor has started");
		} catch (HL7Exception e) {
			out
					.print("Unable to start HL7 inbound queue processor. Perhaps it is already going?");
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

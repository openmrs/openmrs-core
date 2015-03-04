/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.controller.remotecommunication;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.hl7.HL7InQueue;
import org.openmrs.hl7.HL7Service;
import org.openmrs.hl7.HL7Source;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

public class PostHl7Controller implements Controller {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	private String formView;
	
	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Map<String, Object> model = new HashMap<String, Object>();
		Boolean success = false;
		if (!Context.isAuthenticated()) {
			String username = request.getParameter("username");
			String password = request.getParameter("password");
			if (StringUtils.hasText(username) && StringUtils.hasText(password)) {
				Context.authenticate(username, password);
			} else {
				model.put("error", "PostHl7.missingAuthentication");
			}
		}
		if (Context.isAuthenticated()) {
			String message = request.getParameter("hl7Message");
			String hl7Source = request.getParameter("source");
			if (StringUtils.hasText(message) && StringUtils.hasText(hl7Source)) {
				HL7Service service = Context.getHL7Service();
				HL7Source source = service.getHL7SourceByName(hl7Source);
				
				HL7InQueue hl7InQueue = new HL7InQueue();
				hl7InQueue.setHL7Data(message);
				hl7InQueue.setHL7Source(source);
				log.debug("source: " + hl7Source + " , message: " + message);
				Context.getHL7Service().saveHL7InQueue(hl7InQueue);
				success = true;
			} else {
				model.put("error", "PostHl7.sourceAndhl7MessageParametersRequired");
			}
		}
		model.put("success", success);
		return new ModelAndView(formView, "model", model);
	}
	
	public String getFormView() {
		return formView;
	}
	
	public void setFormView(String formView) {
		this.formView = formView;
	}
	
}

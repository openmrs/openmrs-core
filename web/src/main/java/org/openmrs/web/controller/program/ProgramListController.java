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
package org.openmrs.web.controller.program;

import java.util.List;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Program;
import org.openmrs.api.APIException;
import org.openmrs.api.ProgramWorkflowService;
import org.openmrs.api.context.Context;
import org.openmrs.web.WebConstants;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

public class ProgramListController extends SimpleFormController {
	
	/** Logger for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());
	
	/**
	 * This is called prior to displaying a form for the first time. It tells Spring the
	 * form/command object to load into the request
	 *
	 * @see org.springframework.web.servlet.mvc.AbstractFormController#formBackingObject(javax.servlet.http.HttpServletRequest)
	 */
	protected Object formBackingObject(HttpServletRequest request) throws ServletException {
		
		//default empty Object
		List<Program> programList = new Vector<Program>();
		
		//only fill the Object if the user has authenticated properly
		if (Context.isAuthenticated()) {
			ProgramWorkflowService ps = Context.getProgramWorkflowService();
			programList = ps.getAllPrograms();
		}
		
		return programList;
	}
	
	protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object obj,
	        BindException errors) throws Exception {
		
		HttpSession httpSession = request.getSession();
		
		String view = getFormView();
		if (Context.isAuthenticated()) {
			String[] programList = request.getParameterValues("programId");
			ProgramWorkflowService ps = Context.getProgramWorkflowService();
			
			StringBuilder success = new StringBuilder("");
			StringBuilder error = new StringBuilder();
			
			MessageSourceAccessor msa = getMessageSourceAccessor();
			String deleted = msa.getMessage("general.deleted");
			String notDeleted = msa.getMessage("general.cannot.delete");
			String textProgram = msa.getMessage("Program.program");
			String noneDeleted = msa.getMessage("Program.nonedeleted");
			if (programList != null) {
				for (String p : programList) {
					
					try {
						ps.purgeProgram(ps.getProgram(Integer.valueOf(p)));
						if (!success.toString().equals("")) {
							success.append("<br/>");
						}
						success.append(textProgram);
						success.append(" ");
						success.append(p);
						success.append(" ");
						success.append(deleted);
					}
					catch (APIException e) {
						log.warn("Error deleting program", e);
						if (!error.toString().equals("")) {
							error.append("<br/>");
						}
						error.append(textProgram).append(" ").append(p).append(" ").append(notDeleted);
					}
				}
			} else {
				success.append(noneDeleted);
			}
			view = getSuccessView();
			if (!success.toString().equals("")) {
				httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, success.toString());
			}
			if (!error.toString().equals("")) {
				httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, error.toString());
			}
		}
		
		return new ModelAndView(new RedirectView(view));
	}
	
}

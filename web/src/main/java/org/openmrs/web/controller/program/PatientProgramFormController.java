/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.controller.program;

import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientProgram;
import org.openmrs.Program;
import org.openmrs.ProgramWorkflow;
import org.openmrs.ProgramWorkflowState;
import org.openmrs.api.APIException;
import org.openmrs.api.ProgramWorkflowService;
import org.openmrs.api.context.Context;
import org.openmrs.web.WebConstants;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.view.RedirectView;

public class PatientProgramFormController implements Controller {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
		// can't do anything without a method
		return null;
	}
	
	public ModelAndView enroll(HttpServletRequest request, HttpServletResponse response) throws ServletException,
	        IOException {
		
		String returnPage = request.getParameter("returnPage");
		if (returnPage == null) {
			throw new IllegalArgumentException("must specify a returnPage parameter in a call to enroll()");
		}
		
		String patientIdStr = request.getParameter("patientId");
		String programIdStr = request.getParameter("programId");
		String enrollmentDateStr = request.getParameter("dateEnrolled");
		String locationIdStr = request.getParameter("locationId");
		String completionDateStr = request.getParameter("dateCompleted");
		
		log.debug("enroll " + patientIdStr + " in " + programIdStr + " on " + enrollmentDateStr);
		
		ProgramWorkflowService pws = Context.getProgramWorkflowService();
		
		// make sure we parse dates the same was as if we were using the initBinder + property editor method 
		CustomDateEditor cde = new CustomDateEditor(Context.getDateFormat(), true, 10);
		cde.setAsText(enrollmentDateStr);
		Date enrollmentDate = (Date) cde.getValue();
		cde.setAsText(completionDateStr);
		Date completionDate = (Date) cde.getValue();
		Patient patient = Context.getPatientService().getPatient(Integer.valueOf(patientIdStr));
		
		Location location;
		try {
			location = Context.getLocationService().getLocation(Integer.valueOf(locationIdStr));
		}
		catch (Exception e) {
			location = null;
		}
		
		Program program;
		try {
			program = pws.getProgram(Integer.valueOf(programIdStr));
		}
		catch (NumberFormatException e) {
			request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "Program.error.programRequired");
			return new ModelAndView(new RedirectView(returnPage));
		}
		if (enrollmentDate == null) {
			request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "Program.error.enrollmentDateRequired");
		} else if (!pws.getPatientPrograms(patient, program, null, completionDate, enrollmentDate, null, false).isEmpty()) {
			request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "Program.error.already");
		} else {
			PatientProgram pp = new PatientProgram();
			pp.setPatient(patient);
			pp.setLocation(location);
			pp.setProgram(program);
			pp.setDateEnrolled(enrollmentDate);
			pp.setDateCompleted(completionDate);
			
			// Set any initial states if passed in
			for (ProgramWorkflow workflow : program.getAllWorkflows()) {
				String stateIdStr = request.getParameter("initialState." + workflow.getProgramWorkflowId());
				if (StringUtils.hasText(stateIdStr)) {
					Integer stateId = Integer.valueOf(stateIdStr);
					ProgramWorkflowState state = workflow.getState(stateId);
					log.debug("Transitioning to state: " + state);
					pp.transitionToState(state, enrollmentDate);
				}
			}
			try {
				String message = validateWithErrorCodes(pp);
				if (message != null) {
					request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR, message);
				} else {
					Context.getProgramWorkflowService().savePatientProgram(pp);
				}
			}
			catch (APIException e) {
				request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR, e.getMessage());
			}
		}
		return new ModelAndView(new RedirectView(returnPage));
	}
	
	public ModelAndView complete(HttpServletRequest request, HttpServletResponse response) throws ServletException,
	        IOException {
		
		String returnPage = request.getParameter("returnPage");
		if (returnPage == null) {
			throw new IllegalArgumentException("must specify a returnPage parameter in a call to enroll()");
		}
		
		String patientProgramIdStr = request.getParameter("patientProgramId");
		String dateCompletedStr = request.getParameter("dateCompleted");
		
		// make sure we parse dates the same was as if we were using the initBinder + property editor method 
		CustomDateEditor cde = new CustomDateEditor(Context.getDateFormat(), true, 10);
		cde.setAsText(dateCompletedStr);
		Date dateCompleted = (Date) cde.getValue();
		
		PatientProgram p = Context.getProgramWorkflowService().getPatientProgram(Integer.valueOf(patientProgramIdStr));
		p.setDateCompleted(dateCompleted);
		try {
			String message = validateWithErrorCodes(p);
			if (message != null) {
				request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR, message);
			} else {
				Context.getProgramWorkflowService().savePatientProgram(p);
			}
		}
		catch (APIException e) {
			request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR, e.getMessage());
		}
		
		return new ModelAndView(new RedirectView(returnPage));
	}
	
	private String validateWithErrorCodes(Object obj) {
		Errors errors = new BindException(obj, "");
		Context.getAdministrationService().validate(obj, errors);
		if (errors.hasErrors()) {
			StringBuilder message = new StringBuilder();
			for (FieldError error : errors.getFieldErrors()) {
				message.append(Context.getMessageSourceService().getMessage(error.getCode())).append("<br />");
			}
			return message.toString();
		}
		return null;
	}
}

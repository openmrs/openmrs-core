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
package org.openmrs.web.controller.patientset;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Cohort;
import org.openmrs.api.context.Context;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.view.RedirectView;

public class PatientSetController implements Controller {

	protected final Log log = LogFactory.getLog(getClass());
	
    public ModelAndView handleRequest(HttpServletRequest request,
    		HttpServletResponse response) throws ServletException, IOException {
    	
		Cohort ps = Context.getPatientSetService().getMyPatientSet();
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("patientSet", ps);

    	return new ModelAndView("/analysis/patientSetTest", "model", model);
    }
    
    /**
     * Sets the user's PatientSet to be the comma-separated list of patientIds 
     */
    public ModelAndView setPatientSet(HttpServletRequest request,
    		HttpServletResponse response) throws ServletException, IOException {

		String url = request.getParameter("url");
		String ps = request.getParameter("patientIds");
		if (ps == null) {
			ps = "";
		}
		
		Cohort patientSet = new Cohort(ps);
		Context.getPatientSetService().setMyPatientSet(patientSet);
		log.debug("Set user's PatientSet (" + patientSet.size() + " patients)");
		
		if (patientSet.size() > 0) {
			if ("true".equals(request.getParameter("appendPatientId")))
				url += (url.indexOf('?') >= 0 ? "&" : "?") + "patientId=" + patientSet.getMemberIds().iterator().next();
			if ("true".equals(request.getParameter("showPatientSet")))
				url += (url.indexOf('?') >= 0 ? "&" : "?") + "showPatientSet=true";
		}

		return new ModelAndView(new RedirectView(url));
	}

    /**
     * Clears the PatientSet in the user's session 
     */
    public ModelAndView clearPatientSet(HttpServletRequest request,
    		HttpServletResponse response) throws ServletException, IOException {

		String url = request.getParameter("url");
		
		Context.getPatientSetService().clearMyPatientSet();
		log.debug("Cleared user's PatientSet");
		return new ModelAndView(new RedirectView(url));
    }
    
    /**
     * Adds to the PatientSet in the user's session.
     * Adds a single patientId from the "patientId" parameter, or a comma-separated list from the "patientIds" parameter. 
     */
    public ModelAndView addToSet(HttpServletRequest request,
    		HttpServletResponse response) throws ServletException, IOException {

		String url = request.getParameter("url");
		String id = request.getParameter("patientId");
		String ids = request.getParameter("patientIds");
		
		Cohort patientSet = Context.getPatientSetService().getMyPatientSet();
		
		if (id != null) {
			try {
				patientSet.addMember(Integer.valueOf(id.trim()));
			} catch (NumberFormatException ex) { }
		}
		
		if (ids != null) {
			for (String s : ids.split(",")) {
				try {
					patientSet.addMember(Integer.valueOf(s.trim()));
				} catch (NumberFormatException ex) { }
			}
		}
		
		if (patientSet.size() > 0) {
			if ("true".equals(request.getParameter("appendPatientId")))
				url += (url.indexOf('?') >= 0 ? "&" : "?") + "patientId=" + (id != null ? id : patientSet.getMemberIds().iterator().next());
			if ("true".equals(request.getParameter("showPatientSet")))
				url += (url.indexOf('?') >= 0 ? "&" : "?") + "showPatientSet=true";
		}

		return new ModelAndView(new RedirectView(url));
	}

   
    /**
     * Removes patients from the PatientSet in the user's session.
     * Removes a single patientId from the "patientId" parameter, or a comma-separated list from the "patientIds" parameter. 
     */
    public ModelAndView removeFromSet(HttpServletRequest request,
    		HttpServletResponse response) throws ServletException, IOException {

		String url = request.getParameter("url");
		String id = request.getParameter("patientId");
		String ids = request.getParameter("patientIds");
		
		Cohort patientSet = Context.getPatientSetService().getMyPatientSet();
		
		if (id != null) {
			try {
				patientSet.removeMember(Integer.valueOf(id.trim()));
			} catch (NumberFormatException ex) { }
		}
		
		if (ids != null) {
			for (String s : ids.split(",")) {
				try {
					patientSet.removeMember(Integer.valueOf(s.trim()));
				} catch (NumberFormatException ex) { }
			}
		}
		
		if (patientSet.size() > 0) {
			if ("true".equals(request.getParameter("appendPatientId")))
				url += (url.indexOf('?') >= 0 ? "&" : "?") + "patientId=" + patientSet.getMemberIds().iterator().next();
			if ("true".equals(request.getParameter("showPatientSet")))
				url += (url.indexOf('?') >= 0 ? "&" : "?") + "showPatientSet=true";
		}

		return new ModelAndView(new RedirectView(url));
	}

}
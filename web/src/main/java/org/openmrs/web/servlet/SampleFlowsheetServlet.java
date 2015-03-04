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
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.util.PrivilegeConstants;
import org.openmrs.web.WebConstants;

public class SampleFlowsheetServlet extends HttpServlet {
	
	private static final long serialVersionUID = -2794221430160461220L;
	
	private Log log = LogFactory.getLog(this.getClass());
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		log.debug("Getting sample flowsheet");
		
		response.setContentType("text/html");
		HttpSession session = request.getSession();
		
		String pid = request.getParameter("pid");
		if (pid == null || pid.length() == 0) {
			session.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "error.null");
			return;
		}
		
		if (!Context.isAuthenticated() || !Context.hasPrivilege(PrivilegeConstants.VIEW_PATIENTS)
		        || !Context.hasPrivilege(PrivilegeConstants.VIEW_OBS)) {
			session.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "Privileges required: " + PrivilegeConstants.VIEW_PATIENTS
			        + " and " + PrivilegeConstants.VIEW_OBS);
			session.setAttribute(WebConstants.OPENMRS_LOGIN_REDIRECT_HTTPSESSION_ATTR, request.getRequestURI() + "?"
			        + request.getQueryString());
			response.sendRedirect(request.getContextPath() + "/login.htm");
			return;
		}
		
		ServletOutputStream out = response.getOutputStream();
		
		Integer patientId = Integer.parseInt(pid);
		Patient patient = Context.getPatientService().getPatient(patientId);
		List<Obs> obsList = Context.getObsService().getObservationsByPerson(patient);
		
		if (obsList == null || obsList.size() < 1) {
			out.print("No observations found");
			return;
		}
		
		out.println("<style>");
		out.println(".header { font-family:Arial; font-weight:bold; text-align: center; font-size: 1.5em;}");
		out
		        .println(".label { font-family:Arial; text-align:right; color:#808080; font-style:italic; font-size: 0.6em; vertical-align: top;}");
		out.println(".value { font-family:Arial; text-align:left; vertical-align:top; }");
		out.println("</style>");
		out.println("<table cellspacing=0 cellpadding=3>");
		Locale locale = Context.getLocale();
		Calendar date = Calendar.getInstance();
		date.set(1900, Calendar.JANUARY, 1);
		Calendar obsDate = Calendar.getInstance();
		for (Obs obs : obsList) {
			obsDate.setTime(obs.getObsDatetime());
			if (Math.abs(obsDate.getTimeInMillis() - date.getTimeInMillis()) > 86400000) {
				date = obsDate;
				out.println("<tr><td class=header colspan=2>" + Context.getDateFormat().format(date.getTime())
				        + "</td></tr>");
			}
			StringBuilder s = new StringBuilder("<tr><td class=label>");
			s.append(getName(obs, locale));
			s.append("</td><td class=value>");
			s.append(getValue(obs, locale));
			s.append("</td></tr>");
			out.println(s.toString());
		}
		out.println("</table>");
	}
	
	private String getName(Obs obs, Locale locale) {
		return getName(obs.getConcept(), locale);
	}
	
	private String getName(Concept concept, Locale locale) {
		String foundName = "";
		ConceptName shortName = concept.getBestShortName(locale);
		if (shortName != null) {
			foundName = shortName.getName();
		} else {
			ConceptName name = concept.getName(locale);
			if (name != null) {
				foundName = name.getName();
			}
		}
		return foundName;
	}
	
	private String getValue(Obs obs, Locale locale) {
		return obs.getValueAsString(locale);
	}
}

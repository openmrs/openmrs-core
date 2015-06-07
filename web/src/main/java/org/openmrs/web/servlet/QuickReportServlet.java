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
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.LocationService;
import org.openmrs.api.ObsService;
import org.openmrs.api.context.Context;
import org.openmrs.util.PrivilegeConstants;
import org.openmrs.web.WebConstants;

public class QuickReportServlet extends HttpServlet {
	
	public static final long serialVersionUID = 1231231L;
	
	private Log log = LogFactory.getLog(this.getClass());
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html");
		
		String reportType = request.getParameter("reportType");
		HttpSession session = request.getSession();
		
		if (reportType == null || reportType.length() == 0) {
			session.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "error.null");
			return;
		}
		if (!Context.hasPrivilege(PrivilegeConstants.VIEW_PATIENTS)) {
			session.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "Privilege required: " + PrivilegeConstants.VIEW_PATIENTS);
			session.setAttribute(WebConstants.OPENMRS_LOGIN_REDIRECT_HTTPSESSION_ATTR, request.getRequestURI() + "?"
			        + request.getQueryString());
			response.sendRedirect(request.getContextPath() + "/login.htm");
			return;
		}
		
		try {
			Velocity.init();
		}
		catch (Exception e) {
			log.error("Error initializing Velocity engine", e);
		}
		VelocityContext velocityContext = new VelocityContext();
		PrintWriter report = response.getWriter();
		
		report.append("Report: " + reportType + "<br/><br/>\n\n");
		
		if (reportType.equals("RETURN VISIT DATE THIS WEEK")) {
			doReturnVisitDate(velocityContext, report, request);
		}
		if (reportType.equals("ATTENDED CLINIC THIS WEEK")) {
			doAttendedClinic(velocityContext, report, request);
		} else if (reportType.equals("VOIDED OBS")) {
			doVoidedObs(velocityContext, report, request);
		}
		
		try {
			Velocity.evaluate(velocityContext, report, this.getClass().getName(), getTemplate(reportType));
		}
		catch (Exception e) {
			log.error("Error evaluating report type " + reportType, e);
		}
		
	}
	
	private void doReturnVisitDate(VelocityContext velocityContext, PrintWriter report, HttpServletRequest request)
	        throws ServletException {
		ObsService os = Context.getObsService();
		EncounterService es = Context.getEncounterService();
		ConceptService cs = Context.getConceptService();
		
		DateFormat dateFormat = Context.getDateFormat();
		
		Concept c = cs.getConcept(Integer.valueOf("5096")); // RETURN VISIT DATE
		Calendar cal = Calendar.getInstance();
		
		Date start;
		Date end;
		
		String startDate = request.getParameter("startDate");
		String endDate = request.getParameter("endDate");
		String location = request.getParameter("location");
		
		if (startDate != null && startDate.length() != 0) {
			try {
				cal.setTime(dateFormat.parse(startDate));
			}
			catch (ParseException e) {
				throw new ServletException("Error parsing 'Start Date'", e);
			}
		} else {
			cal.setTime(new Date());
		}
		
		// if they don't input an end date, assume they meant "this week"
		if (endDate == null || "".equals(endDate)) {
			while (cal.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
				cal.add(Calendar.DAY_OF_MONTH, -1);
			}
			start = cal.getTime();
			cal.add(Calendar.DAY_OF_MONTH, 7);
			end = cal.getTime();
		} else {
			// they put in an end date, assume literal start and end
			start = cal.getTime();
			try {
				cal.setTime(dateFormat.parse(endDate));
			}
			catch (ParseException e) {
				throw new ServletException("Error parsing 'End Date'", e);
			}
			end = cal.getTime();
		}
		
		List<Obs> allObs = null;
		
		if (location == null || "".equals(location)) {
			allObs = os.getObservations(c, "location.locationId asc, obs.valueDatetime asc", ObsService.PATIENT, true);
		} else {
			Location locationObj = es.getLocation(Integer.valueOf(location));
			allObs = os.getObservations(c, locationObj, "location.locationId asc, obs.valueDatetime asc",
			    ObsService.PATIENT, true);
		}
		
		List<Obs> obs = new Vector<Obs>();
		
		for (Obs o : allObs) {
			if (o.getValueDatetime() != null && o.getValueDatetime().after(start) && o.getValueDatetime().before(end)) {
				obs.add(o);
			}
		}
		
		velocityContext.put("observations", obs);
	}
	
	private void doAttendedClinic(VelocityContext velocityContext, PrintWriter report, HttpServletRequest request)
	        throws ServletException {
		EncounterService es = Context.getEncounterService();
		LocationService ls = Context.getLocationService();
		
		DateFormat dateFormat = Context.getDateFormat();
		velocityContext.put("date", dateFormat);
		
		Calendar cal = Calendar.getInstance();
		
		Date start;
		Date end;
		
		String startDate = request.getParameter("startDate");
		String endDate = request.getParameter("endDate");
		String location = request.getParameter("location");
		
		if (startDate != null && startDate.length() != 0) {
			try {
				cal.setTime(dateFormat.parse(startDate));
			}
			catch (ParseException e) {
				throw new ServletException("Error parsing 'Start Date'", e);
			}
		} else {
			cal.setTime(new Date());
		}
		
		// if they don't input an end date, assume they meant "this week"
		if (endDate == null || "".equals(endDate)) {
			while (cal.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
				cal.add(Calendar.DAY_OF_MONTH, -1);
			}
			start = cal.getTime();
			cal.add(Calendar.DAY_OF_MONTH, 7);
			end = cal.getTime();
		} else {
			// they put in an end date, assume literal start and end
			start = cal.getTime();
			try {
				cal.setTime(dateFormat.parse(endDate));
			}
			catch (ParseException e) {
				throw new ServletException("Error parsing 'End Date'", e);
			}
			end = cal.getTime();
		}
		
		Collection<Encounter> encounters = null;
		
		if (location == null || "".equals(location)) {
			encounters = es.getEncounters(null, null, start, end, null, null, null, true);
		} else {
			Location locationObj = ls.getLocation(Integer.valueOf(location));
			encounters = es.getEncounters(null, locationObj, start, end, null, null, null, true);
		}
		
		if (encounters != null) {
			velocityContext.put("encounters", encounters);
		} else {
			report.append("No Encounters found");
		}
	}
	
	private void doVoidedObs(VelocityContext velocityContext, PrintWriter report, HttpServletRequest request)
	        throws ServletException {
		ObsService os = Context.getObsService();
		
		DateFormat dateFormat = Context.getDateFormat();
		velocityContext.put("date", dateFormat);
		
		Calendar cal = Calendar.getInstance();
		
		Date start;
		Date end;
		
		String startDate = request.getParameter("startDate");
		String endDate = request.getParameter("endDate");
		
		if (startDate != null && startDate.length() != 0) {
			try {
				cal.setTime(dateFormat.parse(startDate));
			}
			catch (ParseException e) {
				throw new ServletException("Error parsing 'Start Date'", e);
			}
		} else {
			cal.setTime(new Date());
		}
		
		// if they don't input an end date, assume they meant "this week"
		if (StringUtils.isEmpty(endDate)) {
			while (cal.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
				cal.add(Calendar.DAY_OF_MONTH, -1);
			}
			start = cal.getTime();
			cal.add(Calendar.DAY_OF_MONTH, 7);
			end = cal.getTime();
		} else {
			// they put in an end date, assume literal start and end
			start = cal.getTime();
			try {
				cal.setTime(dateFormat.parse(endDate));
			}
			catch (ParseException e) {
				throw new ServletException("Error parsing 'End Date'", e);
			}
			end = cal.getTime();
		}
		
		List<Obs> allObs = null;
		allObs = os.getObservations(null, null, null, null, null, null, null, null, null, start, end, true);
		
		List<Obs> obs = new Vector<Obs>();
		for (Obs o : allObs) {
			if (o.getVoided()) {
				obs.add(o);
			}
		}
		
		velocityContext.put("observations", obs);
	}
	
	// TODO temporary placement of template string
	private String getTemplate(String reportType) {
		
		String template = "<table>\n";
		
		if (reportType.equals("RETURN VISIT DATE THIS WEEK")) {
			template += "#foreach($o in $observations)\n";
			template += " <tr>\n";
			template += "  <td>$!{o.Patient.PersonName.GivenName} $!{o.Patient.PersonName.MiddleName} $!{o.Patient.PersonName.FamilyName}</td>\n";
			template += "  <td>$!{o.Patient.PatientIdentifier}</td>\n";
			template += "  <td>$!{o.Location.Name}</td>\n";
			template += "  <td>$!{date.format($!{o.Encounter.EncounterDatetime})}</td>\n";
			template += "  <td>$!{date.format($o.ValueDatetime)}</td>\n";
			template += " </tr>\n";
			template += "#end\n";
		}
		if (reportType.equals("ATTENDED CLINIC THIS WEEK")) {
			template += "#foreach($e in $encounters)\n";
			template += " <tr>\n";
			template += "  <td>$!{e.Patient.PersonName.GivenName} $!{e.Patient.PersonName.MiddleName} $!{e.Patient.PersonName.FamilyName}</td>\n";
			template += "  <td>$!{e.Patient.PatientIdentifier}</td>\n";
			template += "  <td>$!{e.Location.Name}</td>\n";
			template += "  <td>$!{date.format($e.encounterDatetime)}</td>\n";
			template += " </tr>\n";
			template += "#end\n";
		} else if (reportType.equals("VOIDED OBS")) {
			template += " <tr> \n";
			template += "  <th>Id</th><th>Patient</th><th>Encounter</th>";
			template += "  <th>Concept</th><th>Voided Answer</th>";
			template += "  <th>Comment</th><th>Voided By</th><th>Void Reason</th> \n";
			template += " </tr>\n";
			
			template += "#foreach($o in $observations)\n";
			template += " <tr>\n";
			template += "  <td><a href='admin/observations/obs.form?obsId=$!{o.ObsId}'>$!{o.ObsId}</a></td>\n";
			template += "  <td><a href='admin/patients/patient.form?patientId=$!{o.Person.personId}'>$!{o.Person.personName}</a></td>\n";
			template += "  <td><a href='admin/encounters/encounter.form?encounterId=$!{o.Encounter.EncounterId}'>$!{o.Encounter.EncounterId}</a></td>\n";
			template += "  <td>$!{o.Concept.getName(locale)}</td>\n";
			template += "  <td>$!{o.getValueAsString(locale)}</td>\n";
			template += "  <td>$!{o.Comment}</td>\n";
			template += "  <td>$!{o.VoidedBy.FirstName} $!{o.VoidedBy.LastName} $!{date.format($o.DateVoided)}</td>\n";
			template += "  <td>$!{o.VoidReason}</td>\n";
			template += " </tr>\n";
			template += "#end\n";
		}
		
		template += "</table>\n";
		
		return template;
	}
}

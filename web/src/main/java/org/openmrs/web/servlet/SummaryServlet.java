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
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Cohort;
import org.openmrs.Concept;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.logic.LogicException;
import org.openmrs.logic.LogicService;
import org.openmrs.logic.result.Result;
import org.openmrs.util.OpenmrsUtil;
import org.openmrs.util.PrivilegeConstants;
import org.openmrs.web.WebConstants;
import org.springframework.web.bind.ServletRequestUtils;

public class SummaryServlet extends HttpServlet {
	
	public static final long serialVersionUID = 1231231L;
	
	private Log log = LogFactory.getLog(this.getClass());
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doAll(request, response);
	}
	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doAll(request, response);
	}
	
	/**
	 * Run both Post and Get
	 *
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	private void doAll(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html");
		
		HttpSession session = request.getSession();
		
		if (!Context.hasPrivilege(PrivilegeConstants.VIEW_PATIENTS)) {
			session.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "Privilege required: " + PrivilegeConstants.VIEW_PATIENTS);
			response.sendRedirect(request.getContextPath() + "/login.htm");
			return;
		}
		
		PrintWriter summary = response.getWriter();
		
		Cohort patientSet = getPatientSet(request, response);
		
		PatientService patientService = Context.getPatientService();
		LogicService logic = Context.getLogicService();
		
		summary.write("<clinicalSummaryList>\n");
		for (Integer patientId : patientSet.getMemberIds()) {
			try {
				Result xml = logic.eval(patientService.getPatient(patientId), "CLINICAL SUMMARY");
				
				// Output results
				String s = xml.toString();
				summary.write(s);
				String[] lines = s.split("\n");
				for (int x = 1; x < lines.length; x++) {
					summary.write(lines[x] + "\n");
				}
			}
			catch (LogicException e) {
				throw new ServletException("Error while evaluating rule CLINICAL SUMMARY for patient: " + patientId, e);
			}
		}
		summary.write("</clinicalSummaryList>");
	}
	
	/**
	 * Churn through the request object and return a conglomerated patientSet
	 *
	 * @param request
	 * @param response
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 */
	private Cohort getPatientSet(HttpServletRequest request, HttpServletResponse response) throws ServletException,
	        IOException {
		
		DateFormat dateFormat = Context.getDateFormat();
		String startDateString = ServletRequestUtils.getStringParameter(request, "startDate", "");
		String endDateString = ServletRequestUtils.getStringParameter(request, "endDate", "");
		String locationString = ServletRequestUtils.getStringParameter(request, "location", "");
		String identifierStrings = ServletRequestUtils.getStringParameter(request, "patientIdentifiers", "");
		
		Cohort ps = new Cohort();
		
		// get patients according to start/end "Return Visit Date"
		if ((startDateString.length() != 0) || (endDateString.length() != 0)) {
			Concept c = Context.getConceptService().getConcept(Integer.valueOf("5096")); // RETURN VISIT DATE
			Calendar cal = Calendar.getInstance();
			Date startDate;
			Date endDate;
			
			if (startDateString.length() != 0) {
				try {
					cal.setTime(dateFormat.parse(startDateString));
				}
				catch (ParseException e) {
					throw new ServletException("Error parsing 'Start Date'", e);
				}
			} else {
				cal.setTime(new Date());
			}
			
			// if they don't input an end date, assume they meant "this week"
			if ("".equals(endDateString)) {
				while (cal.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
					cal.add(Calendar.DAY_OF_MONTH, -1);
				}
				startDate = cal.getTime();
				cal.add(Calendar.DAY_OF_MONTH, 7);
				endDate = cal.getTime();
			} else {
				// they put in an end date, assume literal start and end
				startDate = cal.getTime();
				try {
					cal.setTime(dateFormat.parse(endDateString));
				}
				catch (ParseException e) {
					throw new ServletException("Error parsing 'End Date'", e);
				}
				endDate = cal.getTime();
			}
			ps = Cohort.union(ps, Context.getPatientSetService().getPatientsHavingDateObs(c.getConceptId(), startDate,
			    endDate));
			log.debug("PatientSet length after adding Return Visit obs: " + ps.size());
		}
		
		// get all patients whose last encounter was at the given location
		if (locationString.length() > 0) {
			ps = Cohort.union(ps, Context.getPatientSetService().getPatientsHavingLocation(Integer.valueOf(locationString)));
		}
		
		List<String> identifiers = new Vector<String>();
		
		// get patient identifiers from text box
		if (identifierStrings.length() > 0) {
			String[] stringArr = identifierStrings.split("\\s");
			for (int x = 0; x < stringArr.length; x++) {
				String s = stringArr[x].trim();
				if (s.length() > 0) {
					identifiers.add(s);
				}
			}
		}
		
		// if they submitted identifiers in the textarea or via a web submission
		if (identifiers.size() > 0) {
			
			// validate check digits on identifiers
			for (int x = 0; x < identifiers.size(); x++) {
				String id = identifiers.get(x);
				try {
					if (!OpenmrsUtil.isValidCheckDigit(id)) {
						log.warn("Invalid check digit: '" + id + "' at location " + x);
					}
				}
				catch (Exception e) {
					log.warn("Invalid check digit: '" + id + "' at location " + x, e);
				}
			}
			
			ps = Cohort.union(ps, Context.getPatientSetService().convertPatientIdentifier(identifiers));
		}
		
		return ps;
	}
	
}

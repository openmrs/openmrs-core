/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.controller.visit;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.directwebremoting.util.Logger;
import org.openmrs.Encounter;
import org.openmrs.Form;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.Visit;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsUtil;
import org.openmrs.web.controller.PortletControllerUtil;
import org.openmrs.web.controller.bean.DatatableRequest;
import org.openmrs.web.controller.bean.DatatableResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Lists visits.
 */
@Controller
public class VisitListController {
	
	protected final Logger log = Logger.getLogger(getClass());
	
	public static final String VISITS_PATH = "/admin/visits/datatable";
	
	public static final String PATIENT = "patient";
	
	/**
	 * It handles calls from DataTables.
	 * 
	 * @param patient
	 * @param request
	 * @return {@link DatatableResponse}
	 */
	@RequestMapping(VISITS_PATH)
	public @ResponseBody
	DatatableResponse getVisits(@ModelAttribute Patient patient, HttpServletRequest request) {
		DatatableRequest datatable = DatatableRequest.parseRequest(request);
		
		DatatableResponse response = new DatatableResponse(datatable);
		
		Integer totalVisitsCount = Context.getEncounterService().getEncountersByVisitsAndPatientCount(patient, false, null);
		response.setiTotalRecords(totalVisitsCount);
		
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("person", patient);
		PortletControllerUtil.addFormToEditAndViewUrlMaps(model);
		@SuppressWarnings("unchecked")
		Map<Form, String> formToViewUrlMap = (Map<Form, String>) model.get("formToViewUrlMap");
		
		@SuppressWarnings("unchecked")
		Map<Form, String> formToEditUrlMap = (Map<Form, String>) model.get("formToEditUrlMap");
		
		if (!StringUtils.isBlank(datatable.getsSearch())) {
			Integer filteredVisitsCount = Context.getEncounterService().getEncountersByVisitsAndPatientCount(patient, false,
			    datatable.getsSearch());
			response.setiTotalDisplayRecords(filteredVisitsCount);
		} else {
			response.setiTotalDisplayRecords(totalVisitsCount);
		}
		
		List<Encounter> encounters = Context.getEncounterService().getEncountersByVisitsAndPatient(patient, false,
		    datatable.getsSearch(), datatable.getiDisplayStart(), datatable.getiDisplayLength());
		
		response.setsColumns("visitId", "visitActive", "visitType", "visitLocation", "visitFrom", "visitTo",
		    "visitIndication", "firstInVisit", "lastInVisit", "encounterId", "encounterDate", "encounterType",
		    "encounterProviders", "encounterLocation", "encounterEnterer", "formViewURL");
		
		for (Encounter encounter : encounters) {
			Map<String, String> row = new HashMap<String, String>();
			
			if (encounter.getVisit() != null) {
				Visit visit = encounter.getVisit();
				row.put("visitId", visit.getId().toString());
				row.put("visitActive", Boolean.toString(isActive(visit.getStartDatetime(), visit.getStopDatetime())));
				row.put("visitType", visit.getVisitType().getName());
				row.put("visitLocation", (visit.getLocation() != null) ? visit.getLocation().getName() : "");
				row.put("visitFrom", Context.getDateFormat().format(visit.getStartDatetime()));
				
				if (visit.getStopDatetime() != null) {
					row.put("visitTo", Context.getDateFormat().format(visit.getStopDatetime()));
				}
				
				if (visit.getIndication() != null && visit.getIndication().getName() != null) {
					row.put("visitIndication", visit.getIndication().getName().getName());
				}
				
				Object[] visitEncounters = visit.getEncounters().toArray();
				if (visitEncounters.length > 0) {
					if (encounter.equals(visitEncounters[0])) {
						row.put("firstInVisit", Boolean.TRUE.toString());
					}
					if (encounter.equals(visitEncounters[visitEncounters.length - 1])) {
						row.put("lastInVisit", Boolean.TRUE.toString());
					}
				} else {
					row.put("firstInVisit", Boolean.TRUE.toString());
					row.put("lastInVisit", Boolean.TRUE.toString());
				}
			}
			
			if (encounter.getId() != null) { //If it is not mocked encounter
				row.put("encounterId", encounter.getId().toString());
				row.put("encounterDate", Context.getDateFormat().format(encounter.getEncounterDatetime()));
				row.put("encounterType", encounter.getEncounterType().getName());
				row.put("encounterProviders", getProviders(encounter));
				row.put("encounterLocation", (encounter.getLocation() != null) ? encounter.getLocation().getName() : "");
				row.put("encounterEnterer", (encounter.getCreator() != null) ? encounter.getCreator().getPersonName()
				        .getFullName() : "");
				row.put("formViewURL", getViewFormURL(request, formToViewUrlMap, formToEditUrlMap, encounter));
			}
			
			response.addRow(row);
		}
		
		return response;
	}
	
	private String getViewFormURL(HttpServletRequest request, Map<Form, String> formToViewUrlMap,
	        Map<Form, String> formToEditUrlMap, Encounter encounter) {
		String viewFormURL = formToViewUrlMap.get(encounter.getForm());
		if (viewFormURL == null) {
			viewFormURL = formToEditUrlMap.get(encounter.getForm());
		}
		if (viewFormURL != null) {
			viewFormURL = request.getContextPath() + "/" + viewFormURL + "?encounterId=" + encounter.getId();
		} else {
			viewFormURL = request.getContextPath() + "/admin/encounters/encounter.form?encounterId=" + encounter.getId();
		}
		return viewFormURL;
	}
	
	private String getProviders(Encounter encounter) {
		StringBuilder providersBuilder = new StringBuilder();
		for (Set<Provider> providers : encounter.getProvidersByRoles().values()) {
			for (Provider provider : providers) {
				providersBuilder.append(provider.getName());
				providersBuilder.append(", ");
			}
		}
		
		if (providersBuilder.length() > 1) {
			return providersBuilder.substring(0, providersBuilder.length() - 2);
		} else {
			return "";
		}
	}
	
	@ModelAttribute
	public Patient getPatient(@RequestParam(PATIENT) Integer patientId) {
		return Context.getPatientService().getPatient(patientId);
	}
	
	private boolean isActive(Date start, Date end) {
		Date now = new Date();
		if (OpenmrsUtil.compare(now, start) >= 0 && OpenmrsUtil.compareWithNullAsLatest(now, end) < 0) {
			return true;
		}
		return false;
	}
}

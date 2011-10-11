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
package org.openmrs.web.controller.visit;

import java.util.Arrays;
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
	
	public static final class Path {
		
		public static final String VISITS = "/admin/visits/datatable";
	}
	
	public static final class Param {
		
		public static final String PATIENT = "patient";
	}
	
	/**
	 * It handles calls from DataTables.
	 * 
	 * @param patient
	 * @param request
	 * @return {@link DatatableResponse}
	 */
	@RequestMapping(Path.VISITS)
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
		    "visitIndication", "encounterId", "encounterDate", "encounterType", "encounterProviders", "encounterLocation",
		    "encounterEnterer", "formViewURL", "formEditURL", "firstInVisit", "lastInVisit");
		
		String[] row;
		for (Encounter encounter : encounters) {
			row = new String[17];
			Arrays.fill(row, "");
			
			if (encounter.getVisit() != null) {
				Visit visit = encounter.getVisit();
				row[0] = visit.getId().toString();
				row[1] = Boolean.toString(isActive(visit.getStartDatetime(), visit.getStopDatetime()));
				row[2] = visit.getVisitType().getName();
				row[3] = (visit.getLocation() != null) ? visit.getLocation().getName() : "";
				row[4] = Context.getDateFormat().format(visit.getStartDatetime());
				
				if (visit.getStopDatetime() != null) {
					row[5] = Context.getDateFormat().format(visit.getStopDatetime());
				}
				
				if (visit.getIndication() != null && visit.getIndication().getName() != null) {
					row[6] = visit.getIndication().getName().getName();
				}
				
				Object[] visitEncounters = visit.getEncounters().toArray();
				if (visitEncounters.length > 0) {
					if (encounter.equals(visitEncounters[0])) {
						row[15] = Boolean.TRUE.toString();
					} else if (encounter.equals(visitEncounters[visitEncounters.length - 1])) {
						row[16] = Boolean.TRUE.toString();
					}
				} else {
					row[15] = Boolean.TRUE.toString();
					row[16] = Boolean.TRUE.toString();
				}
			}
			
			if (encounter.getId() != null) { //If it is not mocked encounter
				row[7] = encounter.getId().toString();
				row[8] = Context.getDateFormat().format(encounter.getEncounterDatetime());
				row[9] = encounter.getEncounterType().getName();
				row[10] = getProviders(encounter);
				row[11] = (encounter.getLocation() != null) ? encounter.getLocation().getName() : "";
				row[12] = (encounter.getCreator() != null) ? encounter.getCreator().getPersonName().toString() : "";
				row[13] = getViewFormURL(request, formToViewUrlMap, encounter);
				row[14] = getEditFormURL(request, formToEditUrlMap, encounter);
			}
			
			response.addRow(row);
		}
		
		return response;
	}
	
	private String getViewFormURL(HttpServletRequest request, Map<Form, String> formToViewUrlMap, Encounter encounter) {
		String viewFormURL = formToViewUrlMap.get(encounter.getForm());
		if (viewFormURL != null) {
			viewFormURL = request.getContextPath() + "/" + viewFormURL + "?encounterId=" + encounter.getId();
		} else {
			viewFormURL = request.getContextPath() + "/admin/encounters/encounterDisplay.list?encounterId="
			        + encounter.getId();
		}
		return viewFormURL;
	}
	
	private String getEditFormURL(HttpServletRequest request, Map<Form, String> formToEditUrlMap, Encounter encounter) {
		String editFormURL = formToEditUrlMap.get(encounter.getForm());
		if (editFormURL != null) {
			editFormURL = request.getContextPath() + "/" + editFormURL + "&encounterId=" + encounter.getId();
		} else {
			editFormURL = request.getContextPath() + "/admin/encounters/encounter.form?encounterId=" + encounter.getId();
		}
		return editFormURL;
	}
	
	private String getProviders(Encounter encounter) {
		StringBuilder providersBuilder = new StringBuilder();
		for (Set<Provider> providers : encounter.getProvidersByRoles().values()) {
			for (Provider provider : providers) {
				if (provider.getPerson() != null) {
					providersBuilder.append(provider.getPerson().getPersonName().getFullName());
				} else {
					providersBuilder.append(provider.getIdentifier());
				}
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
	public Patient getPatient(@RequestParam(Param.PATIENT) Integer patientId) {
		return Context.getPatientService().getPatient(patientId);
	}
	
	private boolean isActive(Date start, Date end) {
		Date now = new Date();
		if (OpenmrsUtil.compare(now, start) >= 0) {
			if (OpenmrsUtil.compareWithNullAsLatest(now, end) < 0) {
				return true;
			}
		}
		return false;
	}
}

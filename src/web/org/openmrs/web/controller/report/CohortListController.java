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
package org.openmrs.web.controller.report;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.openmrs.Cohort;
import org.openmrs.api.context.Context;
import org.openmrs.reporting.AbstractReportObject;
import org.openmrs.reporting.PatientSearch;
import org.openmrs.reporting.PatientSearchReportObject;
import org.openmrs.reporting.ReportObject;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.web.WebConstants;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

/**
 *
 */
public class CohortListController extends SimpleFormController {
	
	protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
		super.initBinder(request, binder);
	}
	
	protected Object formBackingObject(HttpServletRequest request) throws ServletException {
		List<Cohort> cohorts = new ArrayList<Cohort>();
		if (Context.isAuthenticated()) {
			cohorts = Context.getCohortService().getCohorts();
			Collections.sort(cohorts, new Comparator<Cohort>() {
				
				public int compare(Cohort a, Cohort b) {
					int temp = a.getVoided().compareTo(b.getVoided());
					if (temp == 0)
						temp = a.getCohortId().compareTo(b.getCohortId());
					return temp;
				}
			});
		}
		return cohorts;
	}
	
	protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object obj,
	                                BindException errors) throws Exception {
		
		String action = request.getParameter("method");
		String error = "";
		MessageSourceAccessor msa = getMessageSourceAccessor();
		String title = msa.getMessage("Cohort.title");
		String refByCompSearch = msa.getMessage("Cohort.referencedByACompositePatientSearch");
		String couldNotDelete = msa.getMessage("Cohort.couldNotDelete");
		HttpSession httpSession = request.getSession();
		
		if ("delete".equals(action)) {
			String[] toDelete = request.getParameterValues("cohortId");
			if (toDelete != null) {
				List<AbstractReportObject> savedSearches = Context.getReportObjectService().getReportObjectsByType(
				    OpenmrsConstants.REPORT_OBJECT_TYPE_PATIENTSEARCH);
				for (String s : toDelete) {
					int compositeTest = 0;
					for (ReportObject ro : savedSearches) {
						PatientSearchReportObject psro = (PatientSearchReportObject) ro;
						if (psro.getPatientSearch().isComposition()) {
							List<Object> psList = psro.getPatientSearch().getParsedComposition();
							for (Object psObj : psList) {
								if (psObj.getClass().getName().contains("org.openmrs.reporting.PatientSearch")) {
									PatientSearch psInner = (PatientSearch) psObj;
									if (psInner.getSavedCohortId() != null) {
										if (psInner.getSavedCohortId() == Integer.valueOf(Integer.valueOf(s)).intValue()) {
											compositeTest = 1;
										}
									}
								}
							}
						}
					}
					if (compositeTest == 0) {
						String reason = request.getParameter("voidReason");
						Cohort cohort = Context.getCohortService().getCohort(Integer.valueOf(s));
						Context.getCohortService().voidCohort(cohort, reason);
					} else {
						if (!error.equals("")) {
							error += "<Br>";
						}
						error += couldNotDelete + " " + title + " " + s + ", " + refByCompSearch;
					}
					if (!error.equals("")) {
						httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, error);
					}
				}
				return new ModelAndView(new RedirectView(getSuccessView()));
			}
		}
		return showForm(request, response, errors);
	}
	
	protected Map referenceData(HttpServletRequest request, Object obj, Errors errs) throws Exception {
		return new HashMap<String, Object>();
	}
}

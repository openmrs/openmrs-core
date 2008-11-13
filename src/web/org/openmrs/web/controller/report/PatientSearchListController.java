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

import java.util.List;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.APIException;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.reporting.AbstractReportObject;
import org.openmrs.reporting.ReportObject;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.web.WebConstants;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;
import org.openmrs.reporting.PatientSearchReportObject;
import org.openmrs.reporting.PatientSearch;

/**
 * 
 */
public class PatientSearchListController extends SimpleFormController {

	/** Logger for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());

	protected void initBinder(HttpServletRequest request,
	        ServletRequestDataBinder binder) throws Exception {
		super.initBinder(request, binder);

		binder.registerCustomEditor(java.lang.Integer.class,
		                            new CustomNumberEditor(java.lang.Integer.class,
		                                                   true));
	}

	protected ModelAndView onSubmit(HttpServletRequest request,
	        HttpServletResponse response, Object obj, BindException errors)
	        throws Exception {

		HttpSession httpSession = request.getSession();
		String view = getFormView();
		if (Context.isAuthenticated()) {
			String[] reportList = request.getParameterValues("patientSearchId");
			String action = request.getParameter("action");
			AdministrationService as = Context.getAdministrationService();
			String success = "";
			String error = "";
			MessageSourceAccessor msa = getMessageSourceAccessor();
			String deleted = msa.getMessage("general.deleted");
			String notDeleted = msa.getMessage("general.cannot.delete");
			String textPatientSearch = msa.getMessage("Patient.search");
			String noneDeleted = msa.getMessage("PatientSearch.nonedeleted");
			String isInComp = msa.getMessage("PatientSearch.isAnElementInSavedComposition");

			if (msa.getMessage("PatientSearch.delete").equals(action)) {
				if (reportList != null) {
					List<AbstractReportObject> savedSearches = Context.getReportObjectService()
					                                                  .getReportObjectsByType(OpenmrsConstants.REPORT_OBJECT_TYPE_PATIENTSEARCH);
					for (String p : reportList) {
						int compositeTest = 0;
						String psUsedInTheseCompositeSearches = "";
						for (ReportObject ro : savedSearches) {
							PatientSearchReportObject psro = (PatientSearchReportObject) ro;
							if (psro.getPatientSearch().isComposition()) {
								List<Object> psList = psro.getPatientSearch()
								                          .getParsedComposition();
								for (Object psObj : psList) {
									if (psObj.getClass()
									         .getName()
									         .contains("org.openmrs.reporting.PatientSearch")) {
										PatientSearch psInner = (PatientSearch) psObj;
										if (psInner.getSavedSearchId() != null) {
											if (psInner.getSavedSearchId() == Integer.valueOf(p)
											                                         .intValue()) {
												compositeTest = 1;
												if (!psUsedInTheseCompositeSearches.equals(""))
													psUsedInTheseCompositeSearches += ", ";
												psUsedInTheseCompositeSearches += "'"
												        + Context.getReportObjectService()
												                 .getReportObject(Integer.valueOf(psro.getReportObjectId()))
												                 .getName()
												        + "'";
											}
										}
									}
								}
							}
						}
						if (compositeTest == 0) {
							try {
								as.deleteReportObject(Integer.valueOf(p));
								if (!success.equals(""))
									success += "<br/>";
								success += textPatientSearch + " " + p + " "
								        + deleted;
							} catch (APIException e) {
								log.warn("Error deleting report object", e);
								if (!error.equals(""))
									error += "<br/>";
								error += textPatientSearch + " " + p + " "
								        + notDeleted;
							}
						} else {
							if (!error.equals(""))
								error += "<br/>";
							error += textPatientSearch + " " + p + " "
							        + notDeleted + ", " + isInComp + " "
							        + psUsedInTheseCompositeSearches;
						}
					}
				} else {
					success += noneDeleted;
				}
			}
			view = getSuccessView();
			if (!success.equals(""))
				httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, success);
			if (!error.equals(""))
				httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, error);
		}

		return new ModelAndView(new RedirectView(view));
	}

	protected Object formBackingObject(HttpServletRequest request)
	        throws ServletException {

		List<AbstractReportObject> searches = new Vector<AbstractReportObject>();

		if (Context.isAuthenticated()) {
			searches = Context.getReportObjectService()
			                  .getReportObjectsByType(OpenmrsConstants.REPORT_OBJECT_TYPE_PATIENTSEARCH);

		}
		return searches;
	}

}

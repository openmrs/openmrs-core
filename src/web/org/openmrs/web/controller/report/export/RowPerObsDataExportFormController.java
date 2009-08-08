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
package org.openmrs.web.controller.report.export;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.layout.web.address.AddressSupport;
import org.openmrs.layout.web.address.AddressTemplate;
import org.openmrs.propertyeditor.LocationEditor;
import org.openmrs.reporting.ReportObjectService;
import org.openmrs.reporting.export.DataExportReportObject;
import org.openmrs.reporting.export.ExportColumn;
import org.openmrs.reporting.export.RowPerObsDataExportReportObject;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.web.WebConstants;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

/**
 * Controller for the row per obs data export form controller
 */
public class RowPerObsDataExportFormController extends SimpleFormController {
	
	/** Logger for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());
	
	/**
	 * Allows for Integers to be used as values in input tags. Normally, only strings and lists are
	 * expected
	 * 
	 * @see org.springframework.web.servlet.mvc.BaseCommandController#initBinder(javax.servlet.http.HttpServletRequest,
	 *      org.springframework.web.bind.ServletRequestDataBinder)
	 */
	protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
		super.initBinder(request, binder);
		
		binder.registerCustomEditor(java.lang.Integer.class, new CustomNumberEditor(java.lang.Integer.class, true));
		binder.registerCustomEditor(org.openmrs.Location.class, new LocationEditor());
	}
	
	/**
	 * The onSubmit function receives the form/command object that was modified by the input form
	 * and saves it to the db
	 * 
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#onSubmit(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse, java.lang.Object,
	 *      org.springframework.validation.BindException)
	 */
	protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object obj,
	                                BindException errors) throws Exception {
		
		HttpSession httpSession = request.getSession();
		
		String view = getFormView();
		
		if (Context.isAuthenticated()) {
			RowPerObsDataExportReportObject report = (RowPerObsDataExportReportObject) obj;
			
			// create PatientSet from selected values in report
			String[] patientIds = request.getParameterValues("patientId");
			report.setPatientIds(new Vector<Integer>());
			if (patientIds != null)
				for (String patientId : patientIds)
					if (patientId != null && !patientId.equals(""))
						report.addPatientId(Integer.valueOf(patientId));
			
			Integer location = ServletRequestUtils.getIntParameter(request, "location", 0);
			if (location > 0)
				report.setLocation(Context.getLocationService().getLocation(location));
			
			// define columns for report object
			String[] columnIds = request.getParameterValues("columnId");
			report.setColumns(new Vector<ExportColumn>());
			if (columnIds != null) {
				for (String columnId : columnIds) {
					String columnName = request.getParameter("simpleName_" + columnId);
					if (columnName != null)
						// simple column
						report.addSimpleColumn(columnName, request.getParameter("simpleValue_" + columnId));
					else {
						columnName = request.getParameter("conceptColumnName_" + columnId);
						if (columnName != null) {
							// concept column
							String conceptId = request.getParameter("conceptId_" + columnId);
							try {
								Integer.valueOf(conceptId);
							}
							catch (NumberFormatException e) {
								// for backwards compatibility to pre 1.0.43
								Concept c = Context.getConceptService().getConceptByName(conceptId);
								if (c == null)
									throw new APIException("Concept name : + '" + conceptId
									        + "' could not be found in the dictionary");
								conceptId = c.getConceptId().toString();
							}
							String[] extras = request.getParameterValues("conceptExtra_" + columnId);
							report.setRowPerObsColumn(columnName, conceptId, extras);
						} else {
							columnName = request.getParameter("calculatedName_" + columnId);
							if (columnName != null) {
								// calculated column
								String columnValue = request.getParameter("calculatedValue_" + columnId);
								report.addCalculatedColumn(columnName, columnValue);
							} else {
								columnName = request.getParameter("cohortName_" + columnId);
								if (columnName != null) {
									// cohort column
									String cohortIdValue = request.getParameter("cohortIdValue_" + columnId);
									String filterIdValue = request.getParameter("filterIdValue_" + columnId);
									String searchIdValue = request.getParameter("patientSearchIdValue_" + columnId);
									String valueIfTrue = request.getParameter("cohortIfTrue_" + columnId);
									String valueIfFalse = request.getParameter("cohortIfFalse_" + columnId);
									Integer cohortId = null;
									Integer filterId = null;
									Integer searchId = null;
									try {
										cohortId = Integer.valueOf(cohortIdValue);
									}
									catch (Exception ex) {}
									try {
										filterId = Integer.valueOf(filterIdValue);
									}
									catch (Exception ex) {}
									try {
										searchId = Integer.valueOf(searchIdValue);
									}
									catch (Exception ex) {}
									if (cohortId != null || filterId != null || searchId != null)
										report.addCohortColumn(columnName, cohortId, filterId, searchId, valueIfTrue,
										    valueIfFalse);
								} else
									log.warn("Cannot determine column type for column: " + columnId);
							}
						}
					}
				}
			}
			
			String saveAsNew = ServletRequestUtils.getStringParameter(request, "saveAsNew", "");
			if (!saveAsNew.equals(""))
				report.setReportObjectId(null);
			
			Context.getReportObjectService().saveReportObject(report);
			
			String action = ServletRequestUtils.getRequiredStringParameter(request, "action");
			MessageSourceAccessor msa = getMessageSourceAccessor();
			if (action.equals(msa.getMessage("DataExport.save"))) {
				view = getSuccessView();
				httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "DataExport.saved");
			}
		}
		
		return new ModelAndView(new RedirectView(view));
	}
	
	/**
	 * This is called prior to displaying a form for the first time. It tells Spring the
	 * form/command object to load into the request
	 * 
	 * @see org.springframework.web.servlet.mvc.AbstractFormController#formBackingObject(javax.servlet.http.HttpServletRequest)
	 */
	protected Object formBackingObject(HttpServletRequest request) throws ServletException {
		
		DataExportReportObject report = null;
		
		if (Context.isAuthenticated()) {
			ReportObjectService rs = Context.getReportObjectService();
			String reportId = request.getParameter("dataExportId");
			if (reportId != null)
				report = (RowPerObsDataExportReportObject) rs.getReportObject(Integer.valueOf(reportId));
		}
		
		if (report == null)
			report = new RowPerObsDataExportReportObject();
		
		return report;
	}
	
	/**
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#referenceData(javax.servlet.http.HttpServletRequest,
	 *      java.lang.Object, org.springframework.validation.Errors)
	 */
	protected Map<String, Object> referenceData(HttpServletRequest request, Object obj, Errors errs) throws Exception {
		
		Map<String, Object> map = new HashMap<String, Object>();
		String defaultVerbose = "false";
		
		if (Context.isAuthenticated()) {
			defaultVerbose = Context.getAuthenticatedUser().getUserProperty(OpenmrsConstants.USER_PROPERTY_SHOW_VERBOSE);
			AddressSupport support = AddressSupport.getInstance();
			AddressTemplate template = support.getDefaultLayoutTemplate();
			map.put("addressTemplate", template);
		}
		map.put("defaultVerbose", defaultVerbose.equals("true") ? true : false);
		
		return map;
	}
	
}

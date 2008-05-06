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

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.openmrs.reporting.ReportService;
import org.openmrs.reporting.export.DataExportReportObject;
import org.openmrs.reporting.export.DataExportUtil;
import org.openmrs.web.WebConstants;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

public class DataExportListController extends SimpleFormController {
	
    /** Logger for this class and subclasses */
    protected final Log log = LogFactory.getLog(getClass());
    
	/**
	 * 
	 * Allows for Integers to be used as values in input tags.
	 *   Normally, only strings and lists are expected 
	 * 
	 * @see org.springframework.web.servlet.mvc.BaseCommandController#initBinder(javax.servlet.http.HttpServletRequest, org.springframework.web.bind.ServletRequestDataBinder)
	 */
	protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
		super.initBinder(request, binder);
        binder.registerCustomEditor(java.lang.Integer.class,
                new CustomNumberEditor(java.lang.Integer.class, true));
	}

	/** 
	 * 
	 * The onSubmit function receives the form/command object that was modified
	 *   by the input form and saves it to the db
	 * 
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#onSubmit(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.Object, org.springframework.validation.BindException)
	 */
	protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object obj, BindException errors) throws Exception {
		
		HttpSession httpSession = request.getSession();
		
		String view = getFormView();
		if (Context.isAuthenticated()) {
			String[] reportList = request.getParameterValues("dataExportId");
			String action = request.getParameter("action");
			
			AdministrationService as = Context.getAdministrationService();
			
			String success = "";
			String error = "";
			
			MessageSourceAccessor msa = getMessageSourceAccessor();
			String deleted = msa.getMessage("general.deleted");
			String notDeleted = msa.getMessage("general.cannot.delete");
			String textDataExport = msa.getMessage("DataExport.dataExport");
			String noneDeleted = msa.getMessage("DataExport.nonedeleted");
			
			String generated = msa.getMessage("DataExport.generated");
			String notGenerated = msa.getMessage("DataExport.notGenerated");
			String noneGenerated = msa.getMessage("DataExport.noneGenerated");
			
			if (msa.getMessage("DataExport.generate").equals(action)) {
				if (reportList == null)
					success = noneGenerated;
				else {
					ReportService rs = Context.getReportService();
					for (String id : reportList) {
						DataExportReportObject report = null;
						try {
							report = (DataExportReportObject)rs.getReportObject(Integer.valueOf(id));
							DataExportUtil.generateExport(report, null);
							if (!success.equals("")) success += "<br/>";
							success += textDataExport + " '" + report.getName() + "' " + generated;
						}
						catch (Exception e) {
							log.warn("Error generating report object", e);
							if (!error.equals("")) error += "<br/>";
							if (report == null)
								error += textDataExport + " #" + id + " " + notGenerated;
							else
								error += textDataExport + " '" + report.getName() + "' " + notGenerated;
						}
					}
				}
			}
			else if (msa.getMessage("DataExport.delete").equals(action)) {
				
				if ( reportList != null ) {
					for (String p : reportList) {
						//TODO convenience method deleteDataExport(Integer) ??
						try {
							as.deleteReportObject(Integer.valueOf(p));
							if (!success.equals("")) success += "<br/>";
							success += textDataExport + " " + p + " " + deleted;
						}
						catch (APIException e) {
							log.warn("Error deleting report object", e);
							if (!error.equals("")) error += "<br/>";
							error += textDataExport + " " + p + " " + notDeleted;
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

	/**
	 * 
	 * This is called prior to displaying a form for the first time.  It tells Spring
	 *   the form/command object to load into the request
	 * 
	 * @see org.springframework.web.servlet.mvc.AbstractFormController#formBackingObject(javax.servlet.http.HttpServletRequest)
	 */
    protected Object formBackingObject(HttpServletRequest request) throws ServletException {

		//default empty Object
		List<AbstractReportObject> reportList = new Vector<AbstractReportObject>();
		
		//only fill the Object is the user has authenticated properly
		if (Context.isAuthenticated()) {
			ReportService rs = Context.getReportService();
			//ReportService rs = new TestReportService();
	    	reportList = rs.getReportObjectsByType(DataExportReportObject.TYPE_NAME);
		}
    	
        return reportList;
    }

	/**
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#referenceData(javax.servlet.http.HttpServletRequest, java.lang.Object, org.springframework.validation.Errors)
	 */
    @Override
	@SuppressWarnings("unchecked")
	protected Map referenceData(HttpServletRequest request, Object command, Errors errors) throws Exception {
		
		Map<String, Object> map = new HashMap<String, Object>();
		
		List<AbstractReportObject> reportList = (List<AbstractReportObject>) command;
		Map<AbstractReportObject, Date> generatedDates = new HashMap<AbstractReportObject, Date>();
		Map<AbstractReportObject, String> generatedSizes = new HashMap<AbstractReportObject, String>();
		
		// add the last modified date of the generated file as reference data
		for (AbstractReportObject report : reportList) {
			File file = DataExportUtil.getGeneratedFile((DataExportReportObject) report);
			
			if (file.exists()) {
				generatedDates.put(report, new Date(file.lastModified()));
				
				Long size = file.length(); //returned in bytes
				if (size > 1024*1024)
					generatedSizes.put(report, size/(1024*1024) + "MB");
				else if (size > 1024)
					generatedSizes.put(report, size/1024 + "kB");
				else 
					generatedSizes.put(report, size + "B");
			}
		}
		
		map.put("generatedDates", generatedDates);
		map.put("generatedSizes", generatedSizes);
		
		return map;
		
	}

	
    
    
}
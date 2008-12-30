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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.ReportService;
import org.openmrs.api.context.Context;
import org.openmrs.report.ReportSchema;
import org.openmrs.report.ReportSchemaXml;
import org.openmrs.web.WebConstants;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

/**
 * Controller to save do the saving and getting of the report schema
 */
public class ReportSchemaXmlFormController extends SimpleFormController implements Validator {
	
	Log log = LogFactory.getLog(getClass());
	
	/**
	 * @see org.springframework.web.servlet.mvc.AbstractFormController#formBackingObject(javax.servlet.http.HttpServletRequest)
	 */
	@Override
	protected Object formBackingObject(HttpServletRequest request) throws Exception {
		ReportService reportService = (ReportService) Context.getService(ReportService.class);
		
		Integer reportSchemaId = ServletRequestUtils.getIntParameter(request, "reportSchemaId");
		
		log.debug("Getting report schema xml with schema id: " + reportSchemaId);
		
		if (reportSchemaId != null) {
			// fetch the desired reportSchemaXml from the database
			ReportSchemaXml reportSchemaXml = reportService.getReportSchemaXml(reportSchemaId);
			
			// update the stored xml with the reportSchemaId that is in the reportSchemaXml.reportSchemaId column
			reportSchemaXml.updateXmlFromAttributes();
			
			return reportSchemaXml;
		} else
			return new ReportSchemaXml();
	}
	
	/**
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#onSubmit(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse, java.lang.Object,
	 *      org.springframework.validation.BindException)
	 */
	@Override
	protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object commandObject,
	                                BindException errors) throws Exception {
		ReportSchemaXml reportSchemaXml = (ReportSchemaXml) commandObject;
		ReportService reportService = (ReportService) Context.getService(ReportService.class);
		
		try {
			// create a new object out of their xml in order to verify xml and copy out the id/name/desc
			ReportSchema schema = reportService.getReportSchema(reportSchemaXml);
			
			// if the user changed the reportSchemaId to create a new one, create a new object here 
			// so hibernate doesn't complain
			if (reportSchemaXml.getReportSchemaId() != null
			        && !schema.getReportSchemaId().equals(reportSchemaXml.getReportSchemaId())) {
				String xml = reportSchemaXml.getXml();
				reportSchemaXml = new ReportSchemaXml();
				reportSchemaXml.setXml(xml);
			}
			
			reportSchemaXml.populateFromReportSchema(schema);
			
			// save the xml to the database
			reportService.saveReportSchemaXml(reportSchemaXml);
			
		}
		catch (Exception ex) {
			log.warn("Exception building ReportSchema from XML", ex);
			if (ex.getCause() != null) {
				Throwable temp = ex.getCause();
				while (temp.getCause() != null)
					temp = temp.getCause();
				errors.rejectValue("xml", temp.getMessage());
			} else {
				StringBuilder sb = new StringBuilder();
				sb.append("Invalid XML content<br/>");
				sb.append(ex).append("<br/>");
				for (StackTraceElement e : ex.getStackTrace())
					sb.append(e.toString()).append("<br/>");
				errors.rejectValue("xml", sb.toString());
			}
			return showForm(request, response, errors);
		}
		
		HttpSession httpSession = request.getSession();
		httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "Report.manageSchema.saved");
		return new ModelAndView(new RedirectView(getSuccessView()));
	}
	
	/**
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	public boolean supports(Class c) {
		return c == ReportSchemaXml.class;
	}
	
	/**
	 * @see org.springframework.validation.Validator#validate(java.lang.Object,
	 *      org.springframework.validation.Errors)
	 */
	public void validate(Object commandObject, Errors errors) {
		ValidationUtils.rejectIfEmpty(errors, "xml", "Paste XML for report before saving");
		ReportSchemaXml rsx = (ReportSchemaXml) commandObject;
		try {
			ReportService reportService = (ReportService) Context.getService(ReportService.class);
			ReportSchema schema = reportService.getReportSchema(rsx);
			if (schema == null)
				throw new NullPointerException();
		}
		catch (Exception ex) {
			log.warn("Exception building ReportSchema from XML", ex);
			if (ex.getCause() != null) {
				Throwable temp = ex.getCause();
				while (temp.getCause() != null) {
					temp = temp.getCause();
				}
				errors.rejectValue("xml", temp.getMessage());
			} else {
				StringBuilder sb = new StringBuilder();
				sb.append("Invalid XML content<br/>");
				sb.append(ex).append("<br/>");
				for (StackTraceElement e : ex.getStackTrace())
					sb.append(e.toString()).append("<br/>");
				errors.rejectValue("xml", sb.toString());
			}
		}
	}
	
}

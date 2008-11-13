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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.ReportService;
import org.openmrs.api.context.Context;
import org.openmrs.report.CohortDataSet;
import org.openmrs.report.DataSet;
import org.openmrs.report.RenderingMode;
import org.openmrs.report.ReportData;
import org.openmrs.report.ReportRenderer;
import org.openmrs.report.ReportSchema;
import org.openmrs.web.WebConstants;
import org.openmrs.web.report.CohortReportWebRenderer;
import org.openmrs.web.report.WebReportRenderer;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

public class ReportDataFormController extends SimpleFormController {

	protected final Log log = LogFactory.getLog(getClass());
	
	@Override
    protected Map referenceData(HttpServletRequest request) throws Exception {
		Map<String, Object> ret = new HashMap<String, Object>();

		ReportData report = (ReportData) request.getSession().getAttribute(WebConstants.OPENMRS_REPORT_DATA);
    	if (Context.isAuthenticated() && report != null) {
    		ReportService reportService = (ReportService) Context.getService(ReportService.class);
    		List<RenderingMode> otherRenderingModes = new ArrayList<RenderingMode>(reportService.getRenderingModes(report.getReportSchema()));
    		for (Iterator<RenderingMode> i = otherRenderingModes.iterator(); i.hasNext(); ) {
    			Class temp = i.next().getRenderer().getClass(); 
    			if (temp.equals(CohortReportWebRenderer.class))
    				i.remove();
    		}
    		ret.put("otherRenderingModes", otherRenderingModes);
    	}
		return ret;
    }

	/**
	 * 
	 * The onSubmit function receives the form/command object that was modified
	 *   by the input form and saves it to the db
	 * 
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#onSubmit(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.Object, org.springframework.validation.BindException)
	 */
	protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object obj, BindException errors) throws Exception {

		ReportData report = (ReportData) request.getSession().getAttribute(WebConstants.OPENMRS_REPORT_DATA);
    	
		// If we're authorized to render a report, the report exists, and the user has requested 
		// "rerender" as the action, then we render the report using the appropriate report renderer 
		if (Context.isAuthenticated() && report != null && "rerender".equals(request.getParameter("action"))) {
    		ReportSchema schema = report.getReportSchema();
    		ReportService reportService = (ReportService) Context.getService(ReportService.class);
    		String renderClass = request.getParameter("renderingMode");
    		String renderArg = "";
    	
    		// 
    		if (renderClass.indexOf("!") > 0) {
    			int ind = renderClass.indexOf("!");
    			renderArg = renderClass.substring(ind + 1);
    			renderClass = renderClass.substring(0, ind);
    		}

    		
    		// Figure out how to render the report
			ReportRenderer renderer = reportService.getReportRenderer(renderClass);
			log.info("Re-rendering report with " + renderer.getClass() + " and argument " + renderArg);
			
			// If we're supposed to use a web report renderer, then we just redirect to the appropriate URL 
			if (renderer instanceof WebReportRenderer) { 
				WebReportRenderer webRenderer = (WebReportRenderer) renderer;
				if (webRenderer.getLinkUrl(schema) != null) {
					request.getSession().setAttribute(WebConstants.OPENMRS_REPORT_DATA, report);
					request.getSession().setAttribute(WebConstants.OPENMRS_REPORT_ARGUMENT, renderArg);
					String url = webRenderer.getLinkUrl(schema);
					if (!url.startsWith("/"))
						url = "/" + url;
					url = request.getContextPath() + url;
					return new ModelAndView(new RedirectView(url));
				}
			}
			
			// Otherwise, just render the report 
			// TODO it's possible that a web renderer will handle this -- is that ok?
			String filename = renderer.getFilename(schema, renderArg).replace(" ", "_"); 
			response.setContentType(renderer.getRenderedContentType(schema, renderArg));
			response.setHeader("Content-Disposition", "attachment; filename=" + filename);
			response.setHeader("Pragma", "no-cache");		
			renderer.render(report, renderArg, response.getOutputStream());
			return null;

    	} 
    	
    	else {
			String view = getFormView();
			return new ModelAndView(new RedirectView(view));
		}
	}

	/**
	 * 
	 * This is called prior to displaying a form for the first time.  It tells Spring
	 *   the form/command object to load into the request
	 * 
	 * @see org.springframework.web.servlet.mvc.AbstractFormController#formBackingObject(javax.servlet.http.HttpServletRequest)
	 */
    protected Object formBackingObject(HttpServletRequest request) throws ServletException {
        
    	ReportData report = (ReportData)request.getSession().getAttribute(WebConstants.OPENMRS_REPORT_DATA);
    	
    	if (null != report) {
    		return report;
    	}
    	else {
    		// Avoid the annoying NPE
    		CohortDataSet emptyData = new CohortDataSet();
    		emptyData.setName("empty");
    		Map<String, DataSet> emptyMap = new HashMap<String, DataSet>();
    		emptyMap.put("empty", emptyData);
    		ReportSchema emptySchema = new ReportSchema();
    		emptySchema.setName("empty");
    		ReportData emptyReport = new ReportData();
    		emptyReport.setDataSets(emptyMap);
    		emptyReport.setReportSchema(emptySchema);
    		return emptyReport;
    	}
    }

}
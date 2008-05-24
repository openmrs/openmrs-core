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

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openmrs.api.ReportService;
import org.openmrs.api.context.Context;
import org.openmrs.report.EvaluationContext;
import org.openmrs.report.Parameter;
import org.openmrs.report.RenderingMode;
import org.openmrs.report.ReportData;
import org.openmrs.report.ReportRenderer;
import org.openmrs.report.ReportSchema;
import org.openmrs.util.OpenmrsUtil;
import org.openmrs.web.WebConstants;
import org.openmrs.web.report.WebReportRenderer;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

/**
 * This controller runs a report (which must be passed in with the reportId parameter) after
 * allowing the user to enter parameters (if any) and to choose a ReportRenderer.
 * 
 * If the chosen ReportRenderer is a WebReportRenderer, then the report data is placed in the session
 * and this page redirects to the WebReportRenderer's specified URL. Otherwise the renderer writes to
 * this form's response.
 */
public class RunReportController extends SimpleFormController implements Validator {

	public class CommandObject {
		private ReportSchema schema;
		private Map<String, String> userEnteredParams;
		private List<RenderingMode> renderingModes;
		private String selectedRenderer;
		public CommandObject() {
			userEnteredParams = new LinkedHashMap<String, String>();
		}
		public List<RenderingMode> getRenderingModes() {
        	return renderingModes;
        }
		public void setRenderingModes(List<RenderingMode> rendereringModes) {
        	this.renderingModes = rendereringModes;
        }
		public ReportSchema getSchema() {
        	return schema;
        }
		public void setSchema(ReportSchema schema) {
        	this.schema = schema;
        }
		public String getSelectedRenderer() {
        	return selectedRenderer;
        }
		public void setSelectedRenderer(String selectedRenderer) {
        	this.selectedRenderer = selectedRenderer;
        }
		public Map<String, String> getUserEnteredParams() {
        	return userEnteredParams;
        }
		public void setUserEnteredParams(Map<String, String> userEnteredParams) {
        	this.userEnteredParams = userEnteredParams;
		}		
	}
	
	public boolean supports(Class c) {
	    return c == CommandObject.class;
    }

	public void validate(Object commandObject, Errors errors) {
		CommandObject command = (CommandObject) commandObject;
		ValidationUtils.rejectIfEmpty(errors, "schema", "Missing reportId, or report not found");
		if (command.getSchema() != null) {
			ReportSchema rs = command.getSchema();
			Set<String> requiredParams = new HashSet<String>();
			if (rs.getReportParameters() != null) {
				for (Parameter p : rs.getReportParameters()) {
					if (p.isRequired())
						requiredParams.add(p.getName());
				}
			}
			
			for (Map.Entry<String, String> e : command.getUserEnteredParams().entrySet()) {
				if (StringUtils.hasText(e.getValue()))
					requiredParams.remove(e.getKey());
			}
			if (requiredParams.size() > 0) {
				errors.rejectValue("userEnteredParams", "Enter all parameter values");
			}

			if (rs.getDataSetDefinitions() == null || rs.getDataSetDefinitions().size() == 0)
				errors.rejectValue("schema", "ReportSchema must declare some data set definitions");
		}
		ValidationUtils.rejectIfEmpty(errors, "selectedRenderer", "Pick a renderer");
    }

	
	@Override
    protected Object formBackingObject(HttpServletRequest request) throws Exception {
		CommandObject ret = new CommandObject();
		if (Context.isAuthenticated()) {
			Integer id = Integer.valueOf(request.getParameter("reportId"));
			ReportService reportService = (ReportService) Context.getService(ReportService.class);
			ReportSchema schema = reportService.getReportSchema(id);
			ret.setSchema(schema);
			ret.setRenderingModes(reportService.getRenderingModes(schema));
		}
		return ret;
    }

	@Override
    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object commandObject, BindException errors) throws Exception {
		CommandObject command = (CommandObject) commandObject;
		ReportSchema rs = command.getSchema();
		ReportService reportService = Context.getReportService();
		
		EvaluationContext evalContext = new EvaluationContext();
		
		if (rs.getReportParameters() != null) {
			for (Parameter p : rs.getReportParameters()) {
				if (command.getUserEnteredParams() != null) {
					String valString = command.getUserEnteredParams().get(p.getName());
					Object value;
					if (StringUtils.hasText(valString)) {
						try {
							value = OpenmrsUtil.parse(valString, p.getClazz());
							evalContext.addParameterValue(p, value);
						} catch (Exception ex) {
							errors.rejectValue("userEnteredParams", p.getLabel() + ": " + ex.getMessage());
						}
					}
				}
			}
		}
		if (errors.hasErrors())
			return showForm(request, response, errors);
		
		ReportData data = reportService.evaluate(rs, null, evalContext);
		String renderClass = command.getSelectedRenderer();
		String renderArg = "";
		if (renderClass.indexOf("!") > 0) {
			int ind = renderClass.indexOf("!");
			renderArg = renderClass.substring(ind + 1);
			renderClass = renderClass.substring(0, ind);
		}
		
		ReportRenderer renderer = reportService.getReportRenderer(renderClass);
		
		// If we're supposed to use a web report renderer, then we just redirect to the appropriate URL 
		if (renderer instanceof WebReportRenderer) { 
			WebReportRenderer webRenderer = (WebReportRenderer) renderer;
			if (webRenderer.getLinkUrl(rs) != null) {
				request.getSession().setAttribute(WebConstants.OPENMRS_REPORT_DATA, data);
				request.getSession().setAttribute(WebConstants.OPENMRS_REPORT_ARGUMENT, renderArg);
				String url = webRenderer.getLinkUrl(rs);
				if (!url.startsWith("/"))
					url = "/" + url;
				url = request.getContextPath() + url;
				return new ModelAndView(new RedirectView(url));
			}
		}
		
		// Otherwise, just render the report 
		// TODO it's possible that a web renderer will handle this -- is that ok?
		String filename = renderer.getFilename(rs, renderArg).replace(" ", "_"); 
		response.setContentType(renderer.getRenderedContentType(rs, renderArg));
		response.setHeader("Content-Disposition", "attachment; filename=" + filename);
		response.setHeader("Pragma", "no-cache");		
		renderer.render(data, renderArg, response.getOutputStream());
		return null;
				
    }

}

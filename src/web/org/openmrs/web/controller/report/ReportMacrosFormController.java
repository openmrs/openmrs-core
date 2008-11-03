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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.GlobalProperty;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsConstants;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

/**
 * Allows the user to view and edit the Report XML Macros
 */
public class ReportMacrosFormController extends SimpleFormController {
	
	Log log = LogFactory.getLog(getClass());
	
	class CommandObject {
		private String macros;
		public CommandObject() { }
        public String getMacros() {
        	return macros;
        }
        public void setMacros(String macros) {
        	this.macros = macros;
        }
	}
	
	/**
	 * Returns the string representation of the macro properties
	 * @param request
	 * @return String containing macro properties
	 * @throws Exception
	 */
    protected Object formBackingObject(HttpServletRequest request) throws Exception {
    	CommandObject command = new CommandObject();
    	
    	if (!isFormSubmission(request))
    		command.setMacros(Context.getAdministrationService().getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_REPORT_XML_MACROS));
    	
		return command;
    }
	
	/**
	 * The onSubmit function receives the modified macro property string and saves it
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#onSubmit(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.Object, org.springframework.validation.BindException)
	 */
	protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object obj, BindException errors) throws Exception {
		
		String view = getFormView();
		if (Context.isAuthenticated()) {
			CommandObject command = (CommandObject) obj;
			Context.getAdministrationService().saveGlobalProperty(new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_REPORT_XML_MACROS, command.getMacros()));
			view = getSuccessView();
		}
		return new ModelAndView(new RedirectView(view));
	}
}

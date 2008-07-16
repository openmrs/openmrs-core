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
package org.openmrs.web.controller.form;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Field;
import org.openmrs.api.FormService;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.web.WebConstants;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

public class FieldFormController extends SimpleFormController {
	
    /** Logger for this class and subclasses */
    protected final Log log = LogFactory.getLog(getClass());
    
	protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
		super.initBinder(request, binder);
	    binder.registerCustomEditor(java.lang.Integer.class,
                new CustomNumberEditor(java.lang.Integer.class, true));
	}
    
	protected ModelAndView processFormSubmission(HttpServletRequest request, HttpServletResponse reponse, Object obj, BindException errors) throws Exception {
		
		Field field = (Field)obj;
		
    	field = setObjects(field, request);

		return super.processFormSubmission(request, reponse, field, errors);
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
			Field field = (Field)obj;
			field = setObjects(field, request);
			Context.getFormService().updateField(field);
			view = getSuccessView();
			httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "Field.saved");
			view = view + "?phrase=" + request.getParameter("phrase");
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

		Field field = null;
		
		if (Context.isAuthenticated()) {
			FormService fs = Context.getFormService();
			String fieldId = request.getParameter("fieldId");
	    	if (fieldId != null)
	    		field = fs.getField(Integer.valueOf(fieldId));
		}
		
		if (field == null)
			field = new Field();
    	
        return field;
    }

	protected Map referenceData(HttpServletRequest request, Object obj, Errors errors) throws Exception {
		
		Field field = (Field)obj;
		Locale locale = Context.getLocale();
		
		Map<String, Object> map = new HashMap<String, Object>();
		String defaultVerbose = "false";
		
		if (Context.isAuthenticated()) {
			FormService fs = Context.getFormService();
			//map.put("fieldTypes", es.getFieldTypes());
			map.put("fieldTypes", fs.getFieldTypes());
			if (field.getConcept() != null)
				map.put("conceptName", field.getConcept().getName(locale));
			else
				map.put("conceptName", "");
			defaultVerbose = Context.getAuthenticatedUser().getUserProperty(OpenmrsConstants.USER_PROPERTY_SHOW_VERBOSE); 
		}
		
		map.put("defaultVerbose", defaultVerbose.equals("true") ? true : false);
		
		return map;
	}
	
	private Field setObjects(Field field, HttpServletRequest request) {

		if (Context.isAuthenticated()) {
			String conceptId = request.getParameter("conceptId");
			if (conceptId != null && conceptId.length() > 0)
				field.setConcept(Context.getConceptService().getConcept(Integer.valueOf(conceptId)));
			else
				field.setConcept(null);
			
			field.setFieldType(Context.getFormService().getFieldType(Integer.valueOf(request.getParameter("fieldTypeId"))));
		}
		
		return field;

	}
    
}
/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.controller.form;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.EncounterType;
import org.openmrs.Field;
import org.openmrs.Form;
import org.openmrs.FormField;
import org.openmrs.api.FormService;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.web.WebConstants;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

public class FieldFormController extends SimpleFormController {
	
	/** Logger for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());
	
	@Override
	protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
		super.initBinder(request, binder);
		binder.registerCustomEditor(java.lang.Integer.class, new CustomNumberEditor(java.lang.Integer.class, true));
	}
	
	@Override
	protected ModelAndView processFormSubmission(HttpServletRequest request, HttpServletResponse reponse, Object obj,
	        BindException errors) throws Exception {
		
		Field field = (Field) obj;
		
		field = setObjects(field, request);
		
		return super.processFormSubmission(request, reponse, field, errors);
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
		String action = request.getParameter("action");
		
		if (Context.isAuthenticated()) {
			Field field = (Field) obj;
			field = setObjects(field, request);
			
			if (action != null && action.equals(Context.getMessageSourceService().getMessage("general.delete"))) {
				try {
					Context.getFormService().purgeField(field);
				}
				catch (DataIntegrityViolationException e) {
					httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "error.object.inuse.cannot.purge");
					return new ModelAndView(new RedirectView("field.form?fieldId=" + field.getFieldId()));
				}
				httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "Field.Deleted");
			} else {
				Context.getFormService().saveField(field);
				httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "Field.saved");
			}
		}
		
		view = getSuccessView();
		view = view + "?phrase=" + request.getParameter("phrase");
		
		return new ModelAndView(new RedirectView(view));
	}
	
	/**
	 * This is called prior to displaying a form for the first time. It tells Spring the
	 * form/command object to load into the request
	 *
	 * @see org.springframework.web.servlet.mvc.AbstractFormController#formBackingObject(javax.servlet.http.HttpServletRequest)
	 */
	@Override
	protected Object formBackingObject(HttpServletRequest request) throws ServletException {
		
		Field field = null;
		
		if (Context.isAuthenticated()) {
			FormService fs = Context.getFormService();
			String fieldId = request.getParameter("fieldId");
			if (fieldId != null) {
				field = fs.getField(Integer.valueOf(fieldId));
			}
		}
		
		if (field == null) {
			field = new Field();
		}
		
		return field;
	}
	
	@Override
	protected Map<String, Object> referenceData(HttpServletRequest request, Object obj, Errors errors) throws Exception {
		
		Field field = (Field) obj;
		Locale locale = Context.getLocale();
		FormService fs = Context.getFormService();
		
		Map<String, Object> map = new HashMap<String, Object>();
		String defaultVerbose = "false";
		
		if (Context.isAuthenticated()) {
			map.put("fieldTypes", fs.getAllFieldTypes());
			if (field.getConcept() != null) {
				map.put("conceptName", field.getConcept().getName(locale));
			} else {
				map.put("conceptName", "");
			}
			defaultVerbose = Context.getAuthenticatedUser().getUserProperty(OpenmrsConstants.USER_PROPERTY_SHOW_VERBOSE);
		}
		map.put("defaultVerbose", defaultVerbose.equals("true") ? true : false);
		
		Collection<EncounterType> encounterTypes = new ArrayList<EncounterType>();
		Collection<FormField> containingAnyFormField = new ArrayList<FormField>();
		Collection<FormField> containingAllFormFields = new ArrayList<FormField>();
		Collection<Field> fields = new ArrayList<Field>();
		fields.add(field); // add the field to the fields collection                                                      	
		List<Form> formsReturned = null;
		try {
			formsReturned = fs.getForms(null, null, encounterTypes, null, containingAnyFormField, containingAllFormFields,
			    fields); // Retrieving forms which contain this particular field
		}
		catch (Exception e) {
			// When Object parameter doesn't contain a valid Form object, getFroms() throws an Exception
		}
		
		map.put("formList", formsReturned); // add the returned forms to the ma
		
		return map;
	}
	
	private Field setObjects(Field field, HttpServletRequest request) {
		
		if (Context.isAuthenticated()) {
			String conceptId = request.getParameter("conceptId");
			if (conceptId != null && conceptId.length() > 0) {
				field.setConcept(Context.getConceptService().getConcept(Integer.valueOf(conceptId)));
			} else {
				field.setConcept(null);
			}
			
			field.setFieldType(Context.getFormService().getFieldType(Integer.valueOf(request.getParameter("fieldTypeId"))));
		}
		
		return field;
		
	}
	
}

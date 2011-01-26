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
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.EncounterType;
import org.openmrs.FieldType;
import org.openmrs.Form;
import org.openmrs.FormField;
import org.openmrs.api.FormService;
import org.openmrs.api.context.Context;
import org.openmrs.propertyeditor.EncounterTypeEditor;
import org.openmrs.util.FormUtil;
import org.openmrs.web.WebConstants;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

public class FormFormController extends SimpleFormController {
	
	/** Logger for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());
	
	protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
		super.initBinder(request, binder);
		// NumberFormat nf = NumberFormat.getInstance(new Locale("en_US"));
		binder.registerCustomEditor(java.lang.Integer.class, new CustomNumberEditor(java.lang.Integer.class, true));
		binder.registerCustomEditor(EncounterType.class, new EncounterTypeEditor());
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
			Form form = (Form) obj;
			MessageSourceAccessor msa = getMessageSourceAccessor();
			String action = request.getParameter("action");
			if (action == null) {
				httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "Form.not.saved");
			} else {
				if (action.equals(msa.getMessage("Form.save"))) {
					try {
						// retrieve xslt from request if it was uploaded
						if (request instanceof MultipartHttpServletRequest) {
							MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
							MultipartFile xsltFile = multipartRequest.getFile("xslt_file");
							if (xsltFile != null && !xsltFile.isEmpty()) {
								String xslt = IOUtils.toString(xsltFile.getInputStream());
								form.setXslt(xslt);
							}
						}
						
						// save form
						Context.getFormService().saveForm(form);
						httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "Form.saved");
					}
					catch (Exception e) {
						log.error("Error while saving form " + form.getFormId(), e);
						errors.reject(e.getMessage());
						httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "Form.not.saved");
						return showForm(request, response, errors);
					}
				} else if (action.equals(msa.getMessage("Form.delete"))) {
					try {
						Context.getFormService().purgeForm(form);
						httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "Form.deleted");
					}
					catch (Exception e) {
						log.error("Error while deleting form " + form.getFormId(), e);
						errors.reject(e.getMessage());
						httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "Form.cannot.delete");
						return showForm(request, response, errors);
						//return new ModelAndView(new RedirectView(getSuccessView()));
					}
				} else if (action.equals(msa.getMessage("Form.updateSortOrder"))) {
					
					FormService fs = Context.getFormService();
					
					TreeMap<Integer, TreeSet<FormField>> treeMap = FormUtil.getFormStructure(form);
					for (Integer parentFormFieldId : treeMap.keySet()) {
						float sortWeight = 0;
						for (FormField formField : treeMap.get(parentFormFieldId)) {
							formField.setSortWeight(sortWeight);
							fs.saveFormField(formField);
							sortWeight += 50;
						}
					}
					
				} else {
					try {
						Context.getFormService().duplicateForm(form);
						httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "Form.duplicated");
					}
					catch (Exception e) {
						log.error("Error while duplicating form " + form.getFormId(), e);
						errors.reject(e.getMessage());
						httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "Form.cannot.duplicate");
						return showForm(request, response, errors);
					}
				}
				
				view = getSuccessView();
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
		
		Form form = null;
		
		if (Context.isAuthenticated()) {
			FormService fs = Context.getFormService();
			String formId = request.getParameter("formId");
			if (formId != null)
				try {
					form = fs.getForm(Integer.valueOf(formId));
				}
				catch (NumberFormatException e) {
					;
				} //If formId has no readable value defaults to the case where form==null
		}
		
		if (form == null)
			form = new Form();
		
		return form;
	}
	
	protected Map<String, Object> referenceData(HttpServletRequest request, Object obj, Errors errors) throws Exception {
		
		Map<String, Object> map = new HashMap<String, Object>();
		
		List<FieldType> fieldTypes = new Vector<FieldType>();
		List<EncounterType> encTypes = new Vector<EncounterType>();
		
		if (Context.isAuthenticated()) {
			fieldTypes = Context.getFormService().getAllFieldTypes();
			encTypes = Context.getEncounterService().getAllEncounterTypes();
		}
		
		map.put("fieldTypes", fieldTypes);
		map.put("encounterTypes", encTypes);
		
		return map;
	}
}

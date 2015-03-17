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

import java.util.Calendar;
import java.util.Collections;
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.EncounterType;
import org.openmrs.FieldType;
import org.openmrs.Form;
import org.openmrs.FormField;
import org.openmrs.api.FormService;
import org.openmrs.api.FormsLockedException;
import org.openmrs.api.context.Context;
import org.openmrs.propertyeditor.EncounterTypeEditor;
import org.openmrs.util.FormUtil;
import org.openmrs.util.MetadataComparator;
import org.openmrs.web.WebConstants;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

@SuppressWarnings("deprecation")
public class FormFormController extends SimpleFormController {
	
	/** Logger for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());
	
	@Override
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
	@Override
	protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object obj,
	        BindException errors) throws Exception {
		
		HttpSession httpSession = request.getSession();
		String view = getFormView();
		
		if (Context.isAuthenticated()) {
			Form form = (Form) obj;
			MessageSourceAccessor msa = getMessageSourceAccessor();
			String action = request.getParameter("action");
			try {
				if (action == null) {
					httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "Form.not.saved");
				} else {
					if (action.equals(msa.getMessage("Form.save"))) {
						try {
							// save form
							form = Context.getFormService().saveForm(form);
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
						catch (DataIntegrityViolationException e) {
							httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "Form.cannot.delete");
							return new ModelAndView(new RedirectView("formEdit.form?formId=" + form.getFormId()));
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
						for (Map.Entry<Integer, TreeSet<FormField>> entry : treeMap.entrySet()) {
							float sortWeight = 0;
							for (FormField formField : entry.getValue()) {
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
			catch (FormsLockedException e) {
				log.error("forms.locked", e);
				httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "forms.locked");
				if (form.getFormId() != null) {
					view = "formEdit.form?formId=" + form.getFormId();
				}
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
	@Override
	protected Object formBackingObject(HttpServletRequest request) throws ServletException {
		return getForm(request);
	}
	
	@Override
	protected Map<String, Object> referenceData(HttpServletRequest request, Object obj, Errors errors) throws Exception {
		
		Map<String, Object> map = new HashMap<String, Object>();
		
		List<FieldType> fieldTypes = new Vector<FieldType>();
		List<EncounterType> encTypes = new Vector<EncounterType>();
		
		if (Context.isAuthenticated()) {
			fieldTypes = Context.getFormService().getAllFieldTypes();
			encTypes = Context.getEncounterService().getAllEncounterTypes();
			// Non-retired types first
			Collections.sort(encTypes, new MetadataComparator(Context.getLocale()));
		}
		
		map.put("fieldTypes", fieldTypes);
		map.put("encounterTypes", encTypes);
		map.put("isBasicForm", isBasicForm(getForm(request)));
		
		return map;
	}
	
	/**
	 * Gets the form for a given http request.
	 *
	 * @param request the http request.
	 * @return the form.
	 */
	private Form getForm(HttpServletRequest request) {
		Form form = null;
		
		if (Context.isAuthenticated()) {
			FormService fs = Context.getFormService();
			String formId = request.getParameter("formId");
			if (formId != null) {
				try {
					form = fs.getForm(Integer.valueOf(formId));
				}
				catch (NumberFormatException e) {

				} //If formId has no readable value defaults to the case where form==null
			}
		}
		
		if (form == null) {
			form = new Form();
		}
		
		return form;
	}
	
	/**
	 * Checks if a form is a read only basic form installed with demo data.
	 *
	 * @param form the form.
	 * @return true if this is the demo data basic form, else false.
	 */
	private boolean isBasicForm(Form form) {
		if (form.getFormId() == null || form.getCreator() == null || form.getCreator().getUserId() == null
		        || form.getChangedBy() == null || form.getDateChanged() == null || form.getBuild() == null) {
			return false;
		}
		
		Calendar calender = Calendar.getInstance();
		calender.setTime(form.getDateCreated());
		
		return form.getFormId().intValue() == 1 && form.getCreator().getUserId().intValue() == 1
		        && calender.get(Calendar.YEAR) == 2006 && calender.get(Calendar.MONTH) == 6
		        && calender.get(Calendar.DAY_OF_MONTH) == 18 && form.getBuild().intValue() == 1;
	}
}

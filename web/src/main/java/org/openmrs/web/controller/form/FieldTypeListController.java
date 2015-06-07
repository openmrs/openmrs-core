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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.FieldType;
import org.openmrs.api.APIException;
import org.openmrs.api.FormService;
import org.openmrs.api.context.Context;
import org.openmrs.web.WebConstants;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Vector;

public class FieldTypeListController extends SimpleFormController {
	
	/** Logger for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());
	
	/**
	 * The onSubmit function receives the form/command object that was modified by the input form
	 * and saves it to the db
	 *
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#onSubmit(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse, java.lang.Object,
	 *      org.springframework.validation.BindException)
	 *
	 * @should display a user friendly error message
	 */
	protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object obj,
	        BindException errors) throws Exception {
		
		HttpSession httpSession = request.getSession();
		
		String view = getFormView();
		if (Context.isAuthenticated()) {
			String[] fieldTypeList = request.getParameterValues("fieldTypeId");
			FormService fs = Context.getFormService();
			//FieldTypeService rs = new TestFieldTypeService();
			
			String success = "";
			String error = "";
			
			MessageSourceAccessor msa = getMessageSourceAccessor();
			String deleted = msa.getMessage("general.deleted");
			String notDeleted = msa.getMessage("general.cannot.delete");
			String textFieldType = msa.getMessage("FieldType.fieldType");
			String noneDeleted = msa.getMessage("FieldType.nonedeleted");
			if (fieldTypeList != null) {
				for (String fieldTypeId : fieldTypeList) {
					//TODO convenience method deleteFieldType(Integer) ??
					try {
						fs.purgeFieldType(fs.getFieldType(Integer.valueOf(fieldTypeId)));
						if (!"".equals(success)) {
							success += "<br/>";
						}
						success += textFieldType + " " + fieldTypeId + " " + deleted;
					}
					catch (APIException e) {
						log.warn("Error deleting field type", e);
						if (!"".equals(error)) {
							error += "<br/>";
						}
						error += textFieldType + " " + fieldTypeId + " " + notDeleted;
					}
					catch (DataIntegrityViolationException e) {
						log.error("Unable to delete a field type because it is in use. fieldTypeId: " + fieldTypeId, e);
						httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "FieldType.cannot.delete");
						return new ModelAndView(new RedirectView(getSuccessView()));
					}
				}
			} else {
				success += noneDeleted;
			}
			view = getSuccessView();
			if (!"".equals(success)) {
				httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, success);
			}
			if (!"".equals(error)) {
				httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, error);
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
		
		//default empty Object
		List<FieldType> fieldTypeList = new Vector<FieldType>();
		
		//only fill the Object is the user has authenticated properly
		if (Context.isAuthenticated()) {
			FormService fs = Context.getFormService();
			//FieldTypeService rs = new TestFieldTypeService();
			fieldTypeList = fs.getAllFieldTypes();
		}
		
		return fieldTypeList;
	}
}

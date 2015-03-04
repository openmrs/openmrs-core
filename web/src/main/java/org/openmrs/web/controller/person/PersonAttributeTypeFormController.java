/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.controller.person;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.PersonAttributeType;
import org.openmrs.Privilege;
import org.openmrs.api.APIException;
import org.openmrs.api.PersonAttributeTypeLockedException;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;
import org.openmrs.propertyeditor.PrivilegeEditor;
import org.openmrs.web.WebConstants;
import org.openmrs.web.taglib.fieldgen.FieldGenHandlerFactory;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

/**
 * Controller for adding/editing a single PersonAttributeType
 */
public class PersonAttributeTypeFormController extends SimpleFormController {
	
	/** Logger for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());
	
	/**
	 * Allows for Integers to be used as values in input tags. Normally, only strings and lists are
	 * expected
	 *
	 * @see org.springframework.web.servlet.mvc.BaseCommandController#initBinder(javax.servlet.http.HttpServletRequest,
	 *      org.springframework.web.bind.ServletRequestDataBinder)
	 */
	protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
		super.initBinder(request, binder);
		//NumberFormat nf = NumberFormat.getInstance(new Locale("en_US"));
		binder.registerCustomEditor(java.lang.Integer.class, new CustomNumberEditor(java.lang.Integer.class, true));
		binder.registerCustomEditor(Privilege.class, new PrivilegeEditor());
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
			PersonAttributeType attrType = (PersonAttributeType) obj;
			PersonService ps = Context.getPersonService();
			try {
				if (request.getParameter("save") != null) {
					ps.savePersonAttributeType(attrType);
					view = getSuccessView();
					httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "PersonAttributeType.saved");
				}
				// if the user is retiring out the personAttributeType
				else if (request.getParameter("retire") != null) {
					String retireReason = request.getParameter("retireReason");
					if (attrType.getPersonAttributeTypeId() != null && !(StringUtils.hasText(retireReason))) {
						errors.reject("retireReason", "general.retiredReason.empty");
						return showForm(request, response, errors);
					}
					ps.retirePersonAttributeType(attrType, retireReason);
					httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "PersonAttributeType.retiredSuccessfully");
					view = getSuccessView();
				}
				// if the user is purging the personAttributeType
				else if (request.getParameter("purge") != null) {
					try {
						ps.purgePersonAttributeType(attrType);
						httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "PersonAttributeType.purgedSuccessfully");
						view = getSuccessView();
					}
					catch (DataIntegrityViolationException e) {
						httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "error.object.inuse.cannot.purge");
						view = "personAttributeType.form?personAttributeTypeId=" + attrType.getPersonAttributeTypeId();
					}
				} else if (request.getParameter("unretire") != null) {
					ps.unretirePersonAttributeType(attrType);
					httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "PersonAttributeType.unretiredSuccessfully");
					view = getSuccessView();
				}
			}
			catch (PersonAttributeTypeLockedException e) {
				log.error("PersonAttributeType.locked", e);
				errors.reject(e.getMessage());
				httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "PersonAttributeType.locked");
				return showForm(request, response, errors);
			}
			catch (APIException e) {
				httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "error.general: " + e.getLocalizedMessage());
				view = "personAttributeType.form?personAttributeTypeId=" + attrType.getPersonAttributeTypeId();
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
		
		PersonAttributeType attrType = null;
		
		if (Context.isAuthenticated()) {
			PersonService ps = Context.getPersonService();
			String attrTypeId = request.getParameter("personAttributeTypeId");
			if (attrTypeId != null) {
				attrType = ps.getPersonAttributeType(Integer.valueOf(attrTypeId));
			}
		}
		
		if (attrType == null) {
			attrType = new PersonAttributeType();
		}
		
		return attrType;
	}
	
	/**
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#referenceData(javax.servlet.http.HttpServletRequest,
	 *      java.lang.Object, org.springframework.validation.Errors)
	 */
	protected Map<String, Object> referenceData(HttpServletRequest request, Object obj, Errors errors) throws Exception {
		
		Map<String, Object> map = new HashMap<String, Object>();
		
		List<Privilege> privileges = new ArrayList<Privilege>();
		
		if (Context.isAuthenticated()) {
			privileges = Context.getUserService().getAllPrivileges();
		}
		
		Set<String> formats = new TreeSet<String>(FieldGenHandlerFactory.getSingletonInstance().getHandlers().keySet());
		
		// these formats are handled directly by the FieldGenTag.java class and so aren't in the 
		// "handlers" list in openmrs-servlet.xml
		formats.add("java.lang.Character");
		formats.add("java.lang.Integer");
		formats.add("java.lang.Float");
		formats.add("java.lang.Boolean");
		
		// java.util.Date doesn't work as a PersonAttributeType since it gets saved in a user-date-format-specific way
		formats.remove("java.util.Date");
		
		//Removing these two as per ticket: TRUNK-2460
		formats.remove("org.openmrs.Patient.exitReason");
		formats.remove("org.openmrs.DrugOrder.discontinuedReason");
		
		map.put("privileges", privileges);
		map.put("formats", formats);
		
		return map;
	}
}

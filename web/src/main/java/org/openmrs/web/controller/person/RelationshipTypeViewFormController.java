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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.RelationshipType;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;
import org.openmrs.web.WebConstants;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

/**
 * This is the controller for the relationshipTypeView.form page. It controls the way that
 * relationship types are viewed in openmrs.
 */
public class RelationshipTypeViewFormController extends SimpleFormController {
	
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
		binder.registerCustomEditor(java.lang.Integer.class, new CustomNumberEditor(java.lang.Integer.class, true));
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
			
			String[] relationshipTypeIds = request.getParameterValues("relationshipTypeIds");
			String[] displayOrders = request.getParameterValues("displayOrders");
			List<String> preferredTypes = new ArrayList<String>();
			String[] preferredTypesArray = request.getParameterValues("preferredTypes");
			if (preferredTypesArray != null) {
				Collections.addAll(preferredTypes, preferredTypesArray);
			}
			
			PersonService ps = Context.getPersonService();
			
			for (int i = 0; i < relationshipTypeIds.length; i++) {
				String id = relationshipTypeIds[i];
				String displayOrder = displayOrders[i];
				Boolean preferred = preferredTypes.contains(id);
				RelationshipType type = ps.getRelationshipType(Integer.valueOf(id));
				type.setWeight(Integer.valueOf(displayOrder));
				type.setPreferred(preferred);
				ps.saveRelationshipType(type);
			}
			
			String success = getMessageSourceAccessor().getMessage("RelationshipType.views.saved");
			
			view = getSuccessView();
			httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, success);
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
		List<RelationshipType> relationshipTypeList = new Vector<RelationshipType>();
		
		//only fill the Object is the user has authenticated properly
		if (Context.isAuthenticated()) {
			PersonService ps = Context.getPersonService();
			relationshipTypeList = ps.getAllRelationshipTypes();
		}
		
		return relationshipTypeList;
	}
	
	/**
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#referenceData(javax.servlet.http.HttpServletRequest,
	 *      java.lang.Object, org.springframework.validation.Errors)
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected Map referenceData(HttpServletRequest request, Object command, Errors errors) throws Exception {
		
		Map<String, Object> map = new HashMap<String, Object>();
		List<RelationshipType> types = (List<RelationshipType>) command;
		
		List<RelationshipType> preferredTypes = new Vector<RelationshipType>();
		for (RelationshipType type : types) {
			if (type.isPreferred()) {
				preferredTypes.add(type);
			}
		}
		
		map.put("preferredTypes", preferredTypes);
		
		return map;
		
	}
	
}

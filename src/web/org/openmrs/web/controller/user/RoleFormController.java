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
package org.openmrs.web.controller.user;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Privilege;
import org.openmrs.Role;
import org.openmrs.api.APIException;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.openmrs.propertyeditor.PrivilegeEditor;
import org.openmrs.propertyeditor.RoleEditor;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.web.WebConstants;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

public class RoleFormController extends SimpleFormController {
	
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
		binder.registerCustomEditor(Role.class, new RoleEditor());
	}
	
	/**
	 * @see org.springframework.web.servlet.mvc.AbstractFormController#processFormSubmission(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse, java.lang.Object,
	 *      org.springframework.validation.BindException)
	 */
	@SuppressWarnings("unchecked")
	protected ModelAndView processFormSubmission(HttpServletRequest request, HttpServletResponse response, Object obj,
	                                             BindException errors) throws Exception {
		
		Role role = (Role) obj;
		
		String[] inheritiedRoles = request.getParameterValues("inheritedRoles");
		if (inheritiedRoles == null)
			role.setInheritedRoles(Collections.EMPTY_SET);
		
		String[] privileges = request.getParameterValues("privileges");
		if (privileges == null)
			role.setPrivileges(Collections.EMPTY_SET);
		
		return super.processFormSubmission(request, response, role, errors);
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
			Role role = (Role) obj;
			try {
				Context.getUserService().saveRole(role);
				view = getSuccessView();
				httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "Role.saved");
			}
			catch (APIException e) {
				errors.reject(e.getMessage());
				return showForm(request, response, errors);
			}
		}
		
		return new ModelAndView(new RedirectView(view));
	}
	
	/**
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#referenceData(javax.servlet.http.HttpServletRequest,
	 *      java.lang.Object, org.springframework.validation.Errors)
	 */
	protected Map<String, Object> referenceData(HttpServletRequest request, Object object, Errors errors) throws Exception {
		
		Map<String, Object> map = new HashMap<String, Object>();
		
		Role role = (Role) object;
		
		if (Context.isAuthenticated()) {
			List<Role> allRoles = Context.getUserService().getAllRoles();
			Set<Role> inheritingRoles = new HashSet<Role>();
			allRoles.remove(role);
			for (Role r : allRoles) {
				if (r.getInheritedRoles().contains(role))
					inheritingRoles.add(r);
			}
			
			for (String s : OpenmrsConstants.AUTO_ROLES()) {
				Role r = Context.getUserService().getRole(s);
				allRoles.remove(r);
			}
			
			map.put("allRoles", allRoles);
			map.put("inheritingRoles", inheritingRoles);
			map.put("privileges", Context.getUserService().getAllPrivileges());
			map.put("superuser", OpenmrsConstants.SUPERUSER_ROLE);
		}
		
		return map;
	}
	
	/**
	 * This is called prior to displaying a form for the first time. It tells Spring the
	 * form/command object to load into the request
	 * 
	 * @see org.springframework.web.servlet.mvc.AbstractFormController#formBackingObject(javax.servlet.http.HttpServletRequest)
	 */
	protected Object formBackingObject(HttpServletRequest request) throws ServletException {
		
		Role role = null;
		
		if (Context.isAuthenticated()) {
			UserService us = Context.getUserService();
			String r = request.getParameter("roleName");
			if (r != null)
				role = us.getRole(r);
		}
		
		if (role == null)
			role = new Role();
		
		return role;
	}
	
}

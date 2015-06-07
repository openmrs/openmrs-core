/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.controller.user;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Role;
import org.openmrs.api.APIException;
import org.openmrs.api.CannotDeleteRoleWithChildrenException;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsUtil;
import org.openmrs.util.RoleConstants;
import org.openmrs.web.WebConstants;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class RoleListController extends SimpleFormController {
	
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
			StringBuilder success = new StringBuilder();
			StringBuilder error = new StringBuilder();
			
			MessageSourceAccessor msa = getMessageSourceAccessor();
			
			String[] roleList = ServletRequestUtils.getStringParameters(request, "roleId");
			if (roleList.length > 0) {
				UserService us = Context.getUserService();
				
				String deleted = msa.getMessage("general.deleted");
				String notDeleted = msa.getMessage("Role.cannot.delete");
				String notDeletedWithChild = msa.getMessage("Role.cannot.delete.with.child");
				for (String p : roleList) {
					//TODO convenience method deleteRole(String) ??
					try {
						us.purgeRole(us.getRole(p));
						if (!success.toString().isEmpty()) {
							success.append("<br/>");
						}
						success.append(p).append(" ").append(deleted);
					}
					catch (DataIntegrityViolationException e) {
						handleRoleIntegrityException(e, error, notDeleted, p);
					}
					catch (CannotDeleteRoleWithChildrenException e) {
						handleRoleIntegrityException(e, error, notDeletedWithChild, p);
					}
					catch (APIException e) {
						handleRoleIntegrityException(e, error, notDeleted, p);
					}
				}
			} else {
				error.append(msa.getMessage("Role.select"));
			}
			
			view = getSuccessView();
			if (!success.toString().isEmpty()) {
				httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, success.toString());
			}
			if (!error.toString().isEmpty()) {
				httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, error.toString());
			}
		}
		
		return new ModelAndView(new RedirectView(view));
	}
	
	/**
	 * Logs a role delete data integrity violation exception and returns a user friendly message of
	 * the problem that occurred.
	 *
	 * @param e the exception.
	 * @param error the error message.
	 * @param notDeleted the role not deleted error message.
	 * @return the formatted error message.
	 */
	private void handleRoleIntegrityException(Exception e, StringBuilder error, String notDeleted, String role) {
		log.warn("Error deleting role", e);
		if (!error.toString().isEmpty()) {
			error.append("<br/>");
		}
		error.append(role).append(": ").append(notDeleted);
	}
	
	/**
	 * This is called prior to displaying a form for the first time. It tells Spring the
	 * form/command object to load into the request
	 *
	 * @see org.springframework.web.servlet.mvc.AbstractFormController#formBackingObject(javax.servlet.http.HttpServletRequest)
	 */
	protected Object formBackingObject(HttpServletRequest request) throws ServletException {
		
		//default empty Object
		// Object = the role
		// Boolean= whether or not the role is a core role (not able to be deleted)
		Map<Role, Boolean> roleList = new LinkedHashMap<Role, Boolean>();
		
		//only fill the Object if the user has authenticated properly
		if (Context.isAuthenticated()) {
			UserService us = Context.getUserService();
			for (Role r : us.getAllRoles()) {
				if (OpenmrsUtil.getCoreRoles().keySet().contains(r.getRole())) {
					roleList.put(r, true);
				} else {
					roleList.put(r, false);
				}
			}
		}
		
		return roleList;
	}
	
	/**
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#referenceData(javax.servlet.http.HttpServletRequest)
	 */
	protected Map<String, Object> referenceData(HttpServletRequest request) throws Exception {
		
		Map<String, Object> map = new HashMap<String, Object>();
		
		if (Context.isAuthenticated()) {
			map.put("superuser", RoleConstants.SUPERUSER);
		}
		
		return map;
		
	}
}

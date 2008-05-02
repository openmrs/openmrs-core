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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Person;
import org.openmrs.Role;
import org.openmrs.User;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.web.WebConstants;
import org.openmrs.web.controller.person.PersonFormController;
import org.openmrs.propertyeditor.ConceptEditor;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

/**
 * User-specific form controller.  Creates the model/view etc for editing
 * users.
 * 
 * @see org.openmrs.web.controller.person.PersonFormController
 */
public class UserFormController extends PersonFormController {
	
    /** Logger for this class and subclasses */
    protected static final Log log = LogFactory.getLog(UserFormController.class);
    
    /**
	 * Allows for other Objects to be used as values in input tags.
	 *   Normally, only strings and lists are expected 
	 * 
	 * @see org.springframework.web.servlet.mvc.BaseCommandController#initBinder(javax.servlet.http.HttpServletRequest, org.springframework.web.bind.ServletRequestDataBinder)
	 */
	protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
		super.initBinder(request, binder);
		
		binder.registerCustomEditor(java.lang.Integer.class, 
				new CustomNumberEditor(java.lang.Integer.class, true));
        binder.registerCustomEditor(java.util.Date.class, 
        		new CustomDateEditor(Context.getDateFormat(), true));
        binder.registerCustomEditor(org.openmrs.Concept.class, 
        		new ConceptEditor());
	}

	/**
	 * @see org.springframework.web.servlet.mvc.AbstractFormController#processFormSubmission(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.Object, org.springframework.validation.BindException)
	 */
	protected ModelAndView processFormSubmission(HttpServletRequest request, HttpServletResponse response, Object obj, BindException errors) throws Exception {
		HttpSession httpSession = request.getSession();
		
		User user = (User)obj;
		UserService us = Context.getUserService();
		
		MessageSourceAccessor msa = getMessageSourceAccessor();
		String action = request.getParameter("action");
		
		if (!Context.isAuthenticated()) {
			errors.reject("auth.invalid");
		}
		else if (msa.getMessage("User.assumeIdentity").equals(action)) {
			Context.becomeUser(user.getSystemId());
			httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "User.assumeIdentity.success");
			httpSession.setAttribute(WebConstants.OPENMRS_MSG_ARGS, user.getPersonName());
			return new ModelAndView(new RedirectView(request.getContextPath() + "/index.htm"));
		}
		else if (msa.getMessage("User.delete").equals(action)) {
			us.deleteUser(user);
			return new ModelAndView(new RedirectView(getSuccessView()));
		}
		else {
			// check if username is already in the database
				if (us.hasDuplicateUsername(user))
					errors.rejectValue("username", "error.username.taken");
				
			// check if password and password confirm are identical
				String password = request.getParameter("userFormPassword");
				if (password == null || password.equals("XXXXXXXXXXXXXXX")) password = "";
				String confirm = request.getParameter("confirm");
				if (confirm == null || confirm.equals("XXXXXXXXXXXXXXX")) confirm = "";
				
				if (!password.equals(confirm))
					errors.reject("error.password.match");
				
				if (password.length() == 0 && isNewUser(user))
					errors.reject("error.password.weak");
				
			//check password strength
				if (password.length() > 0) {
					if (password.length() < 6)
						errors.reject("error.password.length");
					if (StringUtils.isAlpha(password))
						errors.reject("error.password.characters");
					if (password.equals(user.getUsername()) || password.equals(user.getSystemId()))
						errors.reject("error.password.weak");
				}
					
			// add Roles to user (because spring can't handle lists as properties...)
				String[] roles = request.getParameterValues("roleStrings");
				Set<Role> newRoles = new HashSet<Role>();
				if (roles != null) {
					for (String r : roles) {
						Role role = us.getRole(r);
						newRoles.add(role);
						user.addRole(role);
					}
				}
				
				/*  TODO check if user can delete privilege
				Collection<Collection> lists = OpenmrsUtil.compareLists(user.getRoles(), set);
				
				Collection toDel = (Collection)lists.toArray()[1];
				for (Object o : toDel) {
					Role r = (Role)o;
					for (Privilege p : r.getPrivileges())
						if (!user.hasPrivilege(p.getPrivilege()))
							throw new APIException("Privilege required: " + p.getPrivilege());
				}
				*/
				
				if (user.getRoles() == null)
					newRoles.clear();
				else
					user.getRoles().retainAll(newRoles);
		}
		
		return super.processFormSubmission(request, response, user, errors);
	}
	

	/**
	 * The onSubmit function receives the form/command object that was modified
	 *   by the input form and saves it to the db
	 * 
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#onSubmit(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.Object, org.springframework.validation.BindException)
	 */
	protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object obj, BindException errors) throws Exception {
		
		HttpSession httpSession = request.getSession();
		
		User user = (User)obj;
		String view = getFormView();
		
		if (Context.isAuthenticated()) {
			
			UserService us = Context.getUserService();

			String password = request.getParameter("userFormPassword");
			if (password == null || password.equals("XXXXXXXXXXXXXXX")) password = "";
			
			Map<String, String> properties = user.getUserProperties();
			if (properties == null)
				properties = new HashMap<String, String>();
			
			Boolean newChangePassword = false;
			String chk = request.getParameter(OpenmrsConstants.USER_PROPERTY_CHANGE_PASSWORD);
			
			if (chk != null)
				newChangePassword = true;
			
			if (!newChangePassword.booleanValue() && properties.containsKey(OpenmrsConstants.USER_PROPERTY_CHANGE_PASSWORD)) {
				properties.remove(OpenmrsConstants.USER_PROPERTY_CHANGE_PASSWORD);
			}
			if (newChangePassword.booleanValue()) {
				properties.put(OpenmrsConstants.USER_PROPERTY_CHANGE_PASSWORD, newChangePassword.toString());
			}
			
			user.setUserProperties(properties);
			
			if (isNewUser(user))
				us.createUser(user, password);
			else {
				us.updateUser(user);

				if (!password.equals("") && Context.hasPrivilege(OpenmrsConstants.PRIV_EDIT_USER_PASSWORDS)) {
					if (log.isDebugEnabled())
						log.debug("calling changePassword for user " + user + " by user " + Context.getAuthenticatedUser());
					us.changePassword(user, password);
				}
				
			}
			
			httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "User.saved");
			view = getSuccessView();
		}
		
		return new ModelAndView(new RedirectView(view));
	}

	/**
	 * This is called prior to displaying a form for the first time.  It tells Spring
	 *   the form/command object to load into the request
	 * 
	 * @see org.springframework.web.servlet.mvc.AbstractFormController#formBackingObject(javax.servlet.http.HttpServletRequest)
	 */
    protected Object formBackingObject(HttpServletRequest request) throws ServletException {

		User user = null;
		
		if (Context.isAuthenticated()) {
			UserService us = Context.getUserService();
			String userId = request.getParameter("userId");
			Integer id = null;
	    	if (userId != null) {
	    		try {
	    			id = Integer.valueOf(userId);
	    			user = us.getUser(id);
	    		}
	    		catch (NumberFormatException numberError) {
	    			log.warn("Invalid userId supplied: '" + userId + "'", numberError);
	    		}
	    		catch (ObjectRetrievalFailureException noUserEx) {
	    			// pass through to the null check
	    		}
	    		
	    		// if no user was found
	    		if (user == null) {
		    		try {
		    			Person person = Context.getPersonService().getPerson(id);
		    			user = new User(person);
		    		}
		    		catch (ObjectRetrievalFailureException noPersonEx) {
		    			log.warn("There is no user or person with id: '" + userId + "'", noPersonEx);
		    			throw new ServletException("There is no user or person with id: '" + userId + "'");
		    		}
	    		}
	    	}
		}
		
		if (user == null) {
			user = new User();
			
			String name = request.getParameter("addName");
			if (name != null) {
				String gender = request.getParameter("addGender");
				String date = request.getParameter("addBirthdate");
				String age = request.getParameter("addAge");
				
				getMiniPerson(user, name, gender, date, age);
			}
		}
		
		setupFormBackingObject(user);
		
		return user;
    }
    
    /**
     * @see org.springframework.web.servlet.mvc.SimpleFormController#referenceData(javax.servlet.http.HttpServletRequest, java.lang.Object, org.springframework.validation.Errors)
     */
    protected Map<String, Object> referenceData(HttpServletRequest request, Object obj, Errors errors) throws Exception {
		
		Map<String, Object> map = new HashMap<String, Object>();
		
		User user = (User)obj;
		
		List<Role> roles = Context.getUserService().getRoles();
		if (roles == null)
			roles = new Vector<Role>();
		
		for (String s : OpenmrsConstants.AUTO_ROLES()) {
			Role r = new Role(s);
			roles.remove(r);
		}
		
		if (Context.isAuthenticated()) {
			map.put("roles", roles);
			if (user.getUserId() == null || Context.hasPrivilege(OpenmrsConstants.PRIV_EDIT_USER_PASSWORDS)); 
				map.put("modifyPasswords", true);
			map.put("changePasswordName", OpenmrsConstants.USER_PROPERTY_CHANGE_PASSWORD);
			String s = user.getUserProperty(OpenmrsConstants.USER_PROPERTY_CHANGE_PASSWORD);
			map.put("changePassword", new Boolean(s).booleanValue());
			
			map.put("isNewUser", isNewUser(user));
		}
		
		super.setupReferenceData(map, user);
		
		return map;
    }
    
    /** 
     * Superficially determines if this form is being filled out for a new user
     * 
     * (basically just looks for a primary key (user_id)
     * 
     * @param user
     * @return true/false if this user is new
     */
    private Boolean isNewUser(User user) {
    	return user == null ? true : user.getUserId() == null;
    }
}

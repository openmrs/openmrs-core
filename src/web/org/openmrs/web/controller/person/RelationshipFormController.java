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
package org.openmrs.web.controller.person;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.Relationship;
import org.openmrs.User;
import org.openmrs.api.PatientService;
import org.openmrs.api.PersonService;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.openmrs.web.WebConstants;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

public class RelationshipFormController extends SimpleFormController {
	
    /** Logger for this class and subclasses */
    protected final Log log = LogFactory.getLog(getClass());
    
	/**
	 * 
	 * Allows for Integers to be used as values in input tags.
	 *   Normally, only strings and lists are expected 
	 * 
	 * @see org.springframework.web.servlet.mvc.BaseCommandController#initBinder(javax.servlet.http.HttpServletRequest, org.springframework.web.bind.ServletRequestDataBinder)
	 */
	protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
		super.initBinder(request, binder);
        //NumberFormat nf = NumberFormat.getInstance(new Locale("en_US"));
        binder.registerCustomEditor(java.lang.Integer.class,
                new CustomNumberEditor(java.lang.Integer.class, true));
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
			Person person = (Person)obj;
			PersonService ps = Context.getPersonService();
			PatientService patientService = Context.getPatientService();
			
			String action = request.getParameter("action");
			if (action == null)
				action = "";
			MessageSourceAccessor msa = getMessageSourceAccessor();
			if (action.equals(msa.getMessage("Relationship.save"))) {
				String[] relTypes = request.getParameterValues("relationshipType");
				String[] patients = request.getParameterValues("patientId");
				log.debug("patients: ");
				for (int x = 0; x < patients.length; x++) {
					log.debug("#" + patients[x]);
				}
				for (int x = 0; x < relTypes.length; x++) {
					Relationship r = new Relationship();
					log.error("relType: " + relTypes[x]);
					log.error("patient: " + patients[x+1]);
					if (relTypes[x].equals("") && !patients[x].equals("")) {
						errors.reject("relationshipType", "error.null");
						return showForm(request, response, errors);
					}
					else if (!relTypes[x].equals("") && !patients[x+1].equals("")) {
						r.setRelationshipType(ps.getRelationshipType(Integer.valueOf(relTypes[x])));
						// TODO change from a patient search to a person search
						Patient patient = patientService.getPatient(Integer.valueOf(patients[x+1]));
						log.debug("patient: " + patient);
						Person relative = null; //patient.getPerson();
						log.debug("relative: " + relative);
						if (relative == null)
							relative = ps.getPerson(patient);
						log.debug("relative: " + relative);
						if (relative == null)
							relative = new Person();
						log.debug("relative: " + relative);
						r.setPersonB(relative);
						r.setPersonA(person);
						ps.updatePerson(person);
						ps.createRelationship(r);
					}
				}
			}
			else if (action.equals(msa.getMessage("Relationship.void"))) {
				//String[] relIds = request.getParameterValues("relationshipId");
				
			}
			else if (action.equals(msa.getMessage("Relationship.unvoid"))) {
				String[] relIds = request.getParameterValues("relationshipId");
				for (String id : relIds)  {
					Relationship r = ps.getRelationship(Integer.valueOf(id));
					ps.unvoidRelationship(r);
				}
				
			}
			view = getSuccessView();

			httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, msa.getMessage("Relationship.saved"));
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

		Person person = null;
		
		if (Context.isAuthenticated()) {
			UserService us = Context.getUserService();
			PersonService ps = Context.getPersonService();
			
			String personId = request.getParameter("personId");
			String patientId = request.getParameter("patientId");
			String userId = request.getParameter("userId");
	    	if (personId != null)
	    		person = ps.getPerson(Integer.valueOf(personId));
	    	else if (patientId != null) {
	    		//Patient pat = ps.getPatient(Integer.valueOf(patientId));
	    		//if (pat.getPerson() == null)
	    		//	person = new Person(pat);
	    		//else
	    		//	person = pat.getPerson();
	    	}
	    	else if (userId != null) {
	    		User user = us.getUser(Integer.valueOf(userId));
	    		person = user;
	    	}
		}
		
		if (person == null)
			person = new Person();
    	
        return person;
    }

	protected Map referenceData(HttpServletRequest request, Object obj, Errors errors) throws Exception {
		
		Map<String, Object> map = new HashMap<String, Object>();
		
		Person person = (Person)obj;
		
		if (Context.isAuthenticated()) {
			//AdministrationService as = Context.getAdministrationService();
			PersonService ps = Context.getPersonService();
			map.put("relationships", ps.getRelationships(person));
			map.put("relationshipTypes", ps.getRelationshipTypes());
		}
		
		return map;
	}
    
    
    
}
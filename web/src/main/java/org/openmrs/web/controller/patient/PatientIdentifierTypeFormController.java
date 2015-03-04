/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.controller.patient;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.PatientIdentifierType;
import org.openmrs.api.APIException;
import org.openmrs.api.PatientIdentifierTypeLockedException;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.patient.IdentifierValidator;
import org.openmrs.web.WebConstants;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

public class PatientIdentifierTypeFormController extends SimpleFormController {
	
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
		
		ModelAndView toReturn = new ModelAndView(new RedirectView(view));
		
		if (Context.isAuthenticated()) {
			
			PatientIdentifierType identifierType = (PatientIdentifierType) obj;
			PatientService ps = Context.getPatientService();
			
			//to save the patient identifier type
			try {
				if (request.getParameter("save") != null) {
					identifierType.setCheckDigit(identifierType.hasValidator());
					ps.savePatientIdentifierType(identifierType);
					httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "PatientIdentifierType.saved");
					toReturn = new ModelAndView(new RedirectView(getSuccessView()));
				}
				// if the user is retiring the identifierType
				else if (request.getParameter("retire") != null) {
					String retireReason = request.getParameter("retireReason");
					if (identifierType.getPatientIdentifierTypeId() != null && !(StringUtils.hasText(retireReason))) {
						errors.reject("retireReason", "general.retiredReason.empty");
						return showForm(request, response, errors);
					}
					ps.retirePatientIdentifierType(identifierType, retireReason);
					httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "PatientIdentifierType.retiredSuccessfully");
					toReturn = new ModelAndView(new RedirectView(getSuccessView()));
				}
				// if the user is purging the identifierType
				else if (request.getParameter("purge") != null) {
					try {
						ps.purgePatientIdentifierType(identifierType);
						httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "PatientIdentifierType.purgedSuccessfully");
						toReturn = new ModelAndView(new RedirectView(getSuccessView()));
					}
					catch (DataIntegrityViolationException e) {
						httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "error.object.inuse.cannot.purge");
						return showForm(request, response, errors);
					}
				}
				// if the user unretiring patient identifier type
				else if (request.getParameter("unretire") != null) {
					ps.unretirePatientIdentifierType(identifierType);
					httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "PatientIdentifierType.unretiredSuccessfully");
					toReturn = new ModelAndView(new RedirectView(getSuccessView()));
				}
			}
			catch (PatientIdentifierTypeLockedException e) {
				log.error("PatientIdentifierType.locked", e);
				errors.reject(e.getMessage());
				httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "PatientIdentifierType.locked");
				return showForm(request, response, errors);
			}
			catch (APIException e) {
				httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "error.general: " + e.getLocalizedMessage());
				return showForm(request, response, errors);
			}
		}
		
		return toReturn;
	}
	
	/**
	 * This is called prior to displaying a form for the first time. It tells Spring the
	 * form/command object to load into the request
	 *
	 * @see org.springframework.web.servlet.mvc.AbstractFormController#formBackingObject(javax.servlet.http.HttpServletRequest)
	 */
	protected Object formBackingObject(HttpServletRequest request) throws ServletException {
		
		PatientIdentifierType identifierType = null;
		
		if (Context.isAuthenticated()) {
			PatientService ps = Context.getPatientService();
			String identifierTypeId = request.getParameter("patientIdentifierTypeId");
			if (identifierTypeId != null) {
				identifierType = ps.getPatientIdentifierType(Integer.valueOf(identifierTypeId));
			}
		}
		
		if (identifierType == null) {
			identifierType = new PatientIdentifierType();
		}
		
		return identifierType;
	}
	
	/**
	 * Called prior to form display. Allows for data to be put in the request to be used in the view
	 *
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#referenceData(javax.servlet.http.HttpServletRequest)
	 */
	protected Map<String, Object> referenceData(HttpServletRequest request, Object obj, Errors err) throws Exception {
		Map<String, Object> toReturn = new LinkedHashMap<String, Object>();
		
		Collection<IdentifierValidator> pivs = Context.getPatientService().getAllIdentifierValidators();
		
		toReturn.put("patientIdentifierValidators", pivs);
		
		String defaultValidatorName = Context.getPatientService().getDefaultIdentifierValidator().getName();
		
		toReturn.put("defaultValidatorName", defaultValidatorName);
		
		toReturn.put("locationBehaviors", PatientIdentifierType.LocationBehavior.values());
		
		toReturn.put("uniquenessBehaviors", PatientIdentifierType.UniquenessBehavior.values());
		
		return toReturn;
	}
	
}

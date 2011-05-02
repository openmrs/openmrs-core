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
package org.openmrs.web.controller.visit;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Encounter;
import org.openmrs.Visit;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.validator.VisitValidator;
import org.openmrs.web.WebConstants;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.context.request.WebRequest;

/**
 * Controller class for creating, editing, deleting, restoring and purging a visit
 */
@Controller
@SessionAttributes({ "visit", "visitTypes" })
public class VisitFormController {
	
	private static final Log log = LogFactory.getLog(VisitFormController.class);
	
	private static final String VISIT_FORM_URL = "/admin/visits/visitForm";
	
	/**
	 * Processes requests to display the visit form
	 * 
	 * @param request the {@link WebRequest} object
	 * @param visitId the patient id of the concept map type to show on the form
	 * @param patientId the patient id of the pateint associated to visit to show on the form
	 * @param model the {@link ModelMap} object
	 */
	@RequestMapping(method = RequestMethod.GET, value = VISIT_FORM_URL)
	public void showForm(WebRequest request, @RequestParam(value = "visitId", required = false) Integer visitId,
	        @RequestParam(value = "patientId", required = false) Integer patientId, ModelMap model) {
		
		Visit visit = null;
		if (visitId != null) {
			visit = Context.getVisitService().getVisit(visitId);
		} else {
			visit = new Visit();
			if (patientId != null)
				visit.setPatient(Context.getPatientService().getPatient(patientId));
		}
		
		model.addAttribute("visit", visit);
		model.addAttribute("visitTypes", Context.getVisitService().getAllVisitTypes());
		setEncounterDetails(visit, model);
	}
	
	/**
	 * Processes requests to save/update a visit
	 * 
	 * @param request the {@link WebRequest} object
	 * @param visit the visit object to save/update
	 * @param status the {@link SessionStatus}
	 * @param result the {@link BindingResult} object
	 * @param model the {@link ModelMap} object
	 * @return the url to forward/redirect to
	 */
	@RequestMapping(method = RequestMethod.POST, value = VISIT_FORM_URL)
	public String saveVisit(WebRequest request, @ModelAttribute("visit") Visit visit, BindingResult result,
	        SessionStatus status, ModelMap model) {
		
		new VisitValidator().validate(visit, result);
		if (!result.hasErrors()) {
			try {
				Context.getVisitService().saveVisit(visit);
				if (log.isDebugEnabled())
					log.debug("Saved visit: " + visit.toString());
				
				request.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "Visit.saved", WebRequest.SCOPE_SESSION);
				status.setComplete();
				
				return "redirect:" + VISIT_FORM_URL + ".form?visitId=" + visit.getVisitId();
			}
			catch (APIException e) {
				log.warn("Error while saving visit(s)", e);
				request.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "Visit.save.error", WebRequest.SCOPE_SESSION);
			}
		}
		
		//We have validation errors or had an exception, refresh the list of encounters to be added 
		//to this visit encounters because the user could have added or removed some in the form
		//via ajax
		setEncounterDetails(visit, model);
		return VISIT_FORM_URL;
	}
	
	/**
	 * Processes requests to void a visit
	 * 
	 * @param request the {@link WebRequest} object
	 * @param visit the visit object to void
	 * @param voidReason the reason why the visit is getting void
	 * @param status the {@link SessionStatus}
	 * @param model the {@link ModelMap} object
	 * @return the url to forward to
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/admin/visits/voidVisit")
	public String voidVisit(WebRequest request, @ModelAttribute(value = "visit") Visit visit,
	        @RequestParam(required = false, value = "voidReason") String voidReason, SessionStatus status, ModelMap model) {
		
		if (!StringUtils.hasText(voidReason))
			voidReason = Context.getMessageSourceService().getMessage("general.default.voidReason");
		
		try {
			Context.getVisitService().voidVisit(visit, voidReason);
			if (log.isDebugEnabled())
				log.debug("Voided visit with id: " + visit.getId());
			request.setAttribute(WebConstants.OPENMRS_MSG_ATTR,
			    Context.getMessageSourceService().getMessage("Visit.voided"), WebRequest.SCOPE_SESSION);
			status.setComplete();
			return "redirect:" + VISIT_FORM_URL + ".form?visitId=" + visit.getVisitId();
		}
		catch (APIException e) {
			log.warn("Error occurred while attempting to void visit", e);
			request.setAttribute(WebConstants.OPENMRS_ERROR_ATTR,
			    Context.getMessageSourceService().getMessage("Visit.void.error"), WebRequest.SCOPE_SESSION);
		}
		
		setEncounterDetails(visit, model);
		return VISIT_FORM_URL;
	}
	
	/**
	 * Processes requests to unvoid a visit
	 * 
	 * @param request the {@link WebRequest} object
	 * @param visit the visit object to unvoid
	 * @param status the {@link SessionStatus}
	 * @param model the {@link ModelMap} object
	 * @return the url to forward to
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/admin/visits/unvoidVisit")
	public String unvoidVisit(WebRequest request, @ModelAttribute(value = "visit") Visit visit, SessionStatus status,
	        ModelMap model) {
		
		try {
			Context.getVisitService().unvoidVisit(visit);
			if (log.isDebugEnabled())
				log.debug("Unvoided visit with id: " + visit.getId());
			request.setAttribute(WebConstants.OPENMRS_MSG_ATTR,
			    Context.getMessageSourceService().getMessage("Visit.unvoided"), WebRequest.SCOPE_SESSION);
			status.setComplete();
			return "redirect:" + VISIT_FORM_URL + ".form?visitId=" + visit.getVisitId();
		}
		catch (APIException e) {
			log.warn("Error occurred while attempting to unvoid visit", e);
			request.setAttribute(WebConstants.OPENMRS_ERROR_ATTR,
			    Context.getMessageSourceService().getMessage("Visit.unvoid.error"), WebRequest.SCOPE_SESSION);
		}
		
		setEncounterDetails(visit, model);
		return VISIT_FORM_URL;
	}
	
	/**
	 * Processes requests to purge a visit
	 * 
	 * @param request the {@link WebRequest} object
	 * @param visit the visit object to purge
	 * @param status the {@link SessionStatus}
	 * @param model the {@link ModelMap} object
	 * @return the url to forward to
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/admin/visits/purgeVisit")
	public String purgeVisit(WebRequest request, @ModelAttribute(value = "visit") Visit visit, SessionStatus status,
	        ModelMap model) {
		try {
			Context.getVisitService().purgeVisit(visit);
			if (log.isDebugEnabled())
				log.debug("Purged visit with id: " + visit.getId());
			request.setAttribute(WebConstants.OPENMRS_MSG_ATTR,
			    Context.getMessageSourceService().getMessage("Visit.purged"), WebRequest.SCOPE_SESSION);
			status.setComplete();
			return "redirect:/admin/";
		}
		catch (APIException e) {
			log.warn("Error occurred while attempting to purge visit", e);
			request.setAttribute(WebConstants.OPENMRS_ERROR_ATTR,
			    Context.getMessageSourceService().getMessage("Visit.purge.error"), WebRequest.SCOPE_SESSION);
		}
		
		setEncounterDetails(visit, model);
		return VISIT_FORM_URL;
	}
	
	/**
	 * Convenience method that adds the encounters of the patient for the specified visit to the
	 * specified model, it also adds the visits associated to the specified visit
	 * 
	 * @param visit
	 * @param model
	 */
	private void setEncounterDetails(Visit visit, ModelMap model) {
		List<Encounter> patientEncounters = null;
		List<Encounter> visitEncounters = null;
		if (visit.getPatient() != null && visit.getPatient().getPatientId() != null)
			patientEncounters = Context.getEncounterService().getEncountersByPatient(visit.getPatient());
		if (visit.getVisitId() != null)
			visitEncounters = Context.getEncounterService().getEncountersByVisit(visit);
		
		if (patientEncounters == null)
			patientEncounters = new ArrayList<Encounter>();
		if (visitEncounters == null)
			visitEncounters = new ArrayList<Encounter>();
		
		//remove all encounters that are already associated to visits
		CollectionUtils.filter(patientEncounters, new Predicate() {
			
			@Override
			public boolean evaluate(Object object) {
				Encounter e = (Encounter) object;
				
				return e.getVisit() == null;
			}
		});
		
		model.addAttribute("visitEncounters", visitEncounters);
		model.addAttribute("encountersToAdd", patientEncounters);
	}
}

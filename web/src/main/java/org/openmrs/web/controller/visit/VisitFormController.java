/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.controller.visit;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Encounter;
import org.openmrs.Visit;
import org.openmrs.VisitAttribute;
import org.openmrs.VisitAttributeType;
import org.openmrs.VisitType;
import org.openmrs.api.APIException;
import org.openmrs.api.EncounterService;
import org.openmrs.api.context.Context;
import org.openmrs.validator.EncounterValidator;
import org.openmrs.validator.VisitValidator;
import org.openmrs.web.WebConstants;
import org.openmrs.web.attribute.WebAttributeUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;
import org.springframework.validation.ValidationUtils;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.context.request.WebRequest;

/**
 * Controller class for creating, editing, deleting, restoring and purging a visit
 */
@Controller
public class VisitFormController {
	
	private static final Log log = LogFactory.getLog(VisitFormController.class);
	
	private static final String VISIT_FORM_URL = "/admin/visits/visit";
	
	private static final String VISIT_FORM = "/admin/visits/visitForm";
	
	private static final String VISIT_END_URL = "/admin/visits/visitEnd";
	
	/*
	@InitBinder
	public void initBinder(WebDataBinder wdb) {
		wdb.registerCustomEditor(java.util.Date.class, new CustomDateEditor(Context.getDateTimeFormat(), true, 10));
	}
	*/

	/**
	 * Processes requests to display the form
	 */
	@RequestMapping(method = RequestMethod.GET, value = VISIT_FORM_URL)
	public String showForm(@ModelAttribute("visit") Visit visit,
	        @RequestParam(required = false, value = "startNow") Boolean startNow, ModelMap model) {
		if (startNow != null && startNow && visit.getStartDatetime() == null) {
			visit.setStartDatetime(new Date());
		}
		if (visit.getVisitId() != null) {
			model.addAttribute("canPurgeVisit", Context.getEncounterService().getEncountersByVisit(visit, true).size() == 0);
		}
		
		addEncounterAndObservationCounts(visit, null, model);
		return VISIT_FORM;
	}
	
	@ModelAttribute("visit")
	public Visit getVisit(@RequestParam(value = "visitId", required = false) Integer visitId,
	        @RequestParam(value = "patientId", required = false) Integer patientId, ModelMap model) {
		Visit visit = null;
		if (visitId != null) {
			visit = Context.getVisitService().getVisit(visitId);
		} else {
			visit = new Visit();
			if (patientId != null) {
				visit.setPatient(Context.getPatientService().getPatient(patientId));
			}
		}
		return visit;
	}
	
	@RequestMapping(value = VISIT_END_URL)
	public String endVisitNow(@ModelAttribute("visit") Visit visit, HttpSession session) {
		visit.setStopDatetime(new Date());
		Context.getVisitService().saveVisit(visit);
		session.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "Visit.ended");
		session.setAttribute(WebConstants.OPENMRS_MSG_ARGS, new String[] { visit.getVisitType().getName() });
		
		return "redirect:" + "/patientDashboard.form?patientId=" + visit.getPatient().getPatientId();
	}
	
	/**
	 * Processes requests to save/update a visit
	 *
	 * @param request the {@link WebRequest} object
	 * @param visit the visit object to save/update
	 * @param result the {@link BindingResult} object
	 * @param model the {@link ModelMap} object
	 * @return the url to forward/redirect to
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(method = RequestMethod.POST, value = VISIT_FORM_URL)
	public String saveVisit(HttpServletRequest request, @ModelAttribute("visit") Visit visit, BindingResult result,
	        ModelMap model) {
		String[] ids = ServletRequestUtils.getStringParameters(request, "encounterIds");
		List<Integer> encounterIds = new ArrayList<Integer>();
		EncounterService es = Context.getEncounterService();
		List<Encounter> encountersToSave = new ArrayList<Encounter>();
		if (!ArrayUtils.isEmpty(ids)) {
			for (String id : ids) {
				if (StringUtils.hasText(id)) {
					encounterIds.add(Integer.valueOf(id));
				}
			}
			//validate that the encounters
			List<Encounter> visitEncounters = (List<Encounter>) model.get("visitEncounters");
			for (Encounter e : visitEncounters) {
				if (!encounterIds.contains(e.getEncounterId())) {
					//this encounter was removed in the UI, remove it from this visit
					e.setVisit(null);
					validateEncounter(e, result);
					if (result.hasErrors()) {
						addEncounterAndObservationCounts(visit, encounterIds, model);
						return VISIT_FORM;
					}
					
					encountersToSave.add(e);
				} else {
					//this is an already added encounter
					encounterIds.remove(e.getEncounterId());
				}
			}
			
			//the remaining encounterIds are for the newly added ones, validate and associate them to this visit
			for (Integer encounterId : encounterIds) {
				Encounter e = es.getEncounter(encounterId);
				if (e != null) {
					e.setVisit(visit);
					validateEncounter(e, result);
					if (result.hasErrors()) {
						addEncounterAndObservationCounts(visit, encounterIds, model);
						return VISIT_FORM;
					}
					
					encountersToSave.add(e);
				}
			}
		}
		
		// manually handle the attribute parameters
		List<VisitAttributeType> attributeTypes = (List<VisitAttributeType>) model.get("attributeTypes");
		
		WebAttributeUtil.handleSubmittedAttributesForType(visit, result, VisitAttribute.class, request, attributeTypes);
		new VisitValidator().validate(visit, result);
		if (!result.hasErrors()) {
			try {
				Context.getVisitService().saveVisit(visit);
				if (log.isDebugEnabled()) {
					log.debug("Saved visit: " + visit.toString());
				}
				request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR, "Visit.saved");
				
				for (Encounter encounter : encountersToSave) {
					es.saveEncounter(encounter);
				}
				
				return "redirect:" + "/patientDashboard.form?patientId=" + visit.getPatient().getPatientId();
			}
			catch (APIException e) {
				log.warn("Error while saving visit(s)", e);
				request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "Visit.save.error");
			}
		}
		
		addEncounterAndObservationCounts(visit, encounterIds, model);
		return VISIT_FORM;
	}
	
	/**
	 * Processes requests to end a visit
	 * @param visit the visit object to updated
	 * @param stopDate which contains the stopDate or null for current time
	 * @param request the {@link WebRequest} object
	 * @return the url to forward/redirect to
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/admin/visits/endVisit")
	public String endVisit(@ModelAttribute(value = "visit") Visit visit,
	        @RequestParam(value = "stopDate", required = false) String stopDate, HttpServletRequest request) {
		
		if (!StringUtils.hasLength(stopDate)) {
			Context.getVisitService().endVisit(visit, null);
		} else {
			try {
				Context.getVisitService().endVisit(visit, Context.getDateTimeFormat().parse(stopDate));
				request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR, "Visit.saved");
				return "redirect:" + "/patientDashboard.form?patientId=" + visit.getPatient().getPatientId();
			}
			catch (ParseException pe) {
				request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "error.date");
			}
			catch (APIException e) {
				request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR, e.getMessage());
			}
		}
		
		return VISIT_FORM;
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
		if (!StringUtils.hasText(voidReason)) {
			voidReason = Context.getMessageSourceService().getMessage("general.default.voidReason");
		}
		
		try {
			Context.getVisitService().voidVisit(visit, voidReason);
			if (log.isDebugEnabled()) {
				log.debug("Voided visit with id: " + visit.getId());
			}
			request.setAttribute(WebConstants.OPENMRS_MSG_ATTR,
			    Context.getMessageSourceService().getMessage("Visit.voided"), WebRequest.SCOPE_SESSION);
			return "redirect:" + VISIT_FORM_URL + ".form?visitId=" + visit.getVisitId() + "&patientId="
			        + visit.getPatient().getPatientId();
		}
		catch (APIException e) {
			log.warn("Error occurred while attempting to void visit", e);
			request.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, Context.getMessageSourceService().getMessage(
			    "Visit.void.error"), WebRequest.SCOPE_SESSION);
		}
		
		addEncounterAndObservationCounts(visit, null, model);
		return VISIT_FORM;
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
			if (log.isDebugEnabled()) {
				log.debug("Unvoided visit with id: " + visit.getId());
			}
			request.setAttribute(WebConstants.OPENMRS_MSG_ATTR, Context.getMessageSourceService().getMessage(
			    "Visit.unvoided"), WebRequest.SCOPE_SESSION);
			return "redirect:" + VISIT_FORM_URL + ".form?visitId=" + visit.getVisitId() + "&patientId="
			        + visit.getPatient().getPatientId();
		}
		catch (APIException e) {
			log.warn("Error occurred while attempting to unvoid visit", e);
			request.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, Context.getMessageSourceService().getMessage(
			    "Visit.unvoid.error"), WebRequest.SCOPE_SESSION);
		}
		
		addEncounterAndObservationCounts(visit, null, model);
		return VISIT_FORM;
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
			Integer patientId = visit.getPatient().getPatientId();
			Context.getVisitService().purgeVisit(visit);
			if (log.isDebugEnabled()) {
				log.debug("Purged visit with id: " + visit.getId());
			}
			request.setAttribute(WebConstants.OPENMRS_MSG_ATTR,
			    Context.getMessageSourceService().getMessage("Visit.purged"), WebRequest.SCOPE_SESSION);
			return "redirect:/patientDashboard.form?patientId=" + patientId;
		}
		catch (APIException e) {
			log.warn("Error occurred while attempting to purge visit", e);
			request.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, Context.getMessageSourceService().getMessage(
			    "Visit.purge.error"), WebRequest.SCOPE_SESSION);
		}
		//there was an exception thrown
		return "redirect:" + VISIT_FORM_URL + ".form?visitId=" + visit.getVisitId() + "&patientId="
		        + visit.getPatient().getPatientId();
	}
	
	@ModelAttribute("visitTypes")
	public List<VisitType> getVisitTypes() throws Exception {
		return Context.getVisitService().getAllVisitTypes(true);
	}
	
	@ModelAttribute("attributeTypes")
	public List<VisitAttributeType> getVisitAttributeTypes() throws Exception {
		return Context.getVisitService().getAllVisitAttributeTypes();
	}
	
	@ModelAttribute("visitEncounters")
	public List<Encounter> setEncounterDetails(@ModelAttribute("visit") Visit visit) {
		List<Encounter> visitEncounters = new ArrayList<Encounter>();
		if (visit.getVisitId() != null) {
			visitEncounters = Context.getEncounterService().getEncountersByVisit(visit, false);
		}
		
		return visitEncounters;
	}
	
	private void validateEncounter(Encounter e, BindingResult result) {
		Errors encounterErrors = new BindException(e, "encounter");
		ValidationUtils.invokeValidator(new EncounterValidator(), e, encounterErrors);
		if (encounterErrors.hasErrors()) {
			//bind the errors to the model object
			for (ObjectError error : encounterErrors.getAllErrors()) {
				result.reject(error.getCode(), error.getArguments(), error.getDefaultMessage());
			}
		}
	}
	
	private void addEncounterAndObservationCounts(Visit visit, List<Integer> encounterIds, ModelMap model) {
		int encounterCount = 0;
		int observationCount = 0;
		EncounterService encounterService = Context.getEncounterService();
		if (visit != null && visit.getId() != null) {
			List<Encounter> encounters = encounterService.getEncountersByVisit(visit, false);
			encounterCount = encounters.size();
			
			if (!encounters.isEmpty()) {
				observationCount = Context.getObsService().getObservationCount(null, encounters, null, null, null, null,
				    null, null, null, false);
			}
		}
		
		if (encounterIds != null) {
			List<Encounter> visitEncounters = new ArrayList<Encounter>();
			for (Integer encounterId : encounterIds) {
				visitEncounters.add(encounterService.getEncounter(encounterId));
			}
			model.put("visitEncounters", visitEncounters);
		}
		
		model.put("encounterCount", encounterCount);
		model.put("observationCount", observationCount);
	}
}

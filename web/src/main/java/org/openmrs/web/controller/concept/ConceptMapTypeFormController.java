/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.controller.concept;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.ConceptMapType;
import org.openmrs.api.APIException;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.validator.ConceptMapTypeValidator;
import org.openmrs.web.WebConstants;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.WebRequest;

/**
 * Controller class for processing requests for managing concept map types
 */
@Controller
public class ConceptMapTypeFormController {
	
	/**
	 * Logger for this class
	 */
	private static final Log log = LogFactory.getLog(ConceptMapTypeFormController.class);
	
	private static final String CONCEPT_MAP_TYPE_LIST_URL = "/admin/concepts/conceptMapTypeList";
	
	private static final String CONCEPT_MAP_TYPE_FORM_URL = "/admin/concepts/conceptMapType";
	
	private static final String CONCEPT_MAP_TYPE_FORM = "/admin/concepts/conceptMapTypeForm";
	
	/**
	 * Processes requests to display the form
	 */
	@RequestMapping(method = RequestMethod.GET, value = CONCEPT_MAP_TYPE_FORM_URL)
	public String showForm() {
		return CONCEPT_MAP_TYPE_FORM;
	}
	
	/**
	 * Processes requests to display a list of the current concept map types in the database
	 *
	 * @param model the {@link ModelMap} object
	 * @param request the {@link WebRequest} object
	 */
	@RequestMapping(method = RequestMethod.GET, value = CONCEPT_MAP_TYPE_LIST_URL)
	public void showConceptMapTypeList(ModelMap model, WebRequest request) {
		ConceptService conceptService = Context.getConceptService();
		List<ConceptMapType> conceptMapTypeList = null;
		conceptMapTypeList = conceptService.getConceptMapTypes(true, true);
		
		model.addAttribute("conceptMapTypeList", conceptMapTypeList);
	}
	
	@ModelAttribute("conceptMapType")
	public ConceptMapType getConceptMapType(
	        @RequestParam(value = "conceptMapTypeId", required = false) Integer conceptMapTypeId) {
		ConceptMapType conceptMapType = null;
		if (conceptMapTypeId != null) {
			conceptMapType = Context.getConceptService().getConceptMapType(conceptMapTypeId);
		}
		
		if (conceptMapType == null) {
			conceptMapType = new ConceptMapType();
		}
		
		return conceptMapType;
	}
	
	/**
	 * Processes requests to save/update a concept map type
	 *
	 * @param request the {@link WebRequest} object
	 * @param conceptMapType the concept map type object to save/update
	 * @param result the {@link BindingResult} object
	 * @return the url to redirect to
	 */
	@RequestMapping(method = RequestMethod.POST, value = CONCEPT_MAP_TYPE_FORM_URL)
	public String saveConceptMapType(WebRequest request, @ModelAttribute("conceptMapType") ConceptMapType conceptMapType,
	        BindingResult result) {
		
		new ConceptMapTypeValidator().validate(conceptMapType, result);
		if (!result.hasErrors()) {
			try {
				Context.getConceptService().saveConceptMapType(conceptMapType);
				if (log.isDebugEnabled()) {
					log.debug("Saved concept map type: " + conceptMapType.toString());
				}
				request.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "ConceptMapType.saved", WebRequest.SCOPE_SESSION);
				
				return "redirect:" + CONCEPT_MAP_TYPE_LIST_URL + ".list";
			}
			catch (APIException e) {
				log.error("Error while saving concept map type(s)", e);
				request.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "ConceptMapType.save.error", WebRequest.SCOPE_SESSION);
			}
		}
		
		//there was an error
		return CONCEPT_MAP_TYPE_FORM;
	}
	
	/**
	 * Processes requests to retire a concept map type
	 *
	 * @param request the {@link WebRequest} object
	 * @param conceptMapType the concept map type object to retire
	 * @param retireReason the reason why the concept map type is getting retired
	 * @return the url to redirect to
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/admin/concepts/retireConceptMapType")
	public String retireConceptMapType(WebRequest request,
	        @ModelAttribute(value = "conceptMapType") ConceptMapType conceptMapType,
	        @RequestParam(required = false, value = "retireReason") String retireReason) {
		
		if (!StringUtils.hasText(retireReason)) {
			retireReason = Context.getMessageSourceService().getMessage("general.default.retireReason");
		}
		
		try {
			Context.getConceptService().retireConceptMapType(conceptMapType, retireReason);
			if (log.isDebugEnabled()) {
				log.debug("Retired concept map type with id: " + conceptMapType.getId());
			}
			request.setAttribute(WebConstants.OPENMRS_MSG_ATTR, Context.getMessageSourceService().getMessage(
			    "ConceptMapType.retired"), WebRequest.SCOPE_SESSION);
			
			return "redirect:" + CONCEPT_MAP_TYPE_LIST_URL + ".list";
		}
		catch (APIException e) {
			log.error("Error occurred while attempting to retire concept map type", e);
			request.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, Context.getMessageSourceService().getMessage(
			    "ConceptMapType.retire.error"), WebRequest.SCOPE_SESSION);
		}
		
		//an error occurred
		return CONCEPT_MAP_TYPE_FORM;
	}
	
	/**
	 * Processes requests to unretire a concept map type
	 *
	 * @param request the {@link WebRequest} object
	 * @param conceptMapType the concept map type object to unretire
	 * @return the url to redirect to
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/admin/concepts/unretireConceptMapType")
	public String unretireConceptMapType(WebRequest request,
	        @ModelAttribute(value = "conceptMapType") ConceptMapType conceptMapType) {
		
		try {
			Context.getConceptService().unretireConceptMapType(conceptMapType);
			if (log.isDebugEnabled()) {
				log.debug("Unretired concept map type with id: " + conceptMapType.getId());
			}
			request.setAttribute(WebConstants.OPENMRS_MSG_ATTR, Context.getMessageSourceService().getMessage(
			    "ConceptMapType.unretired"), WebRequest.SCOPE_SESSION);
			
			return "redirect:" + CONCEPT_MAP_TYPE_LIST_URL + ".list";
		}
		catch (APIException e) {
			log.error("Error occurred while attempting to unretire concept map type", e);
			request.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, Context.getMessageSourceService().getMessage(
			    "ConceptMapType.unretire.error"), WebRequest.SCOPE_SESSION);
		}
		
		//an error occurred
		return CONCEPT_MAP_TYPE_FORM;
	}
	
	/**
	 * Processes requests to purge a concept map type
	 *
	 * @param request the {@link WebRequest} object
	 * @param conceptMapType
	 * @return the url to forward to
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/admin/concepts/purgeConceptMapType")
	public String purgeTerm(WebRequest request, @ModelAttribute(value = "conceptMapType") ConceptMapType conceptMapType) {
		Integer id = conceptMapType.getId();
		try {
			Context.getConceptService().purgeConceptMapType(conceptMapType);
			if (log.isDebugEnabled()) {
				log.debug("Purged concept map type with id: " + id);
			}
			request.setAttribute(WebConstants.OPENMRS_MSG_ATTR, Context.getMessageSourceService().getMessage(
			    "ConceptMapType.purged"), WebRequest.SCOPE_SESSION);
			return "redirect:" + CONCEPT_MAP_TYPE_LIST_URL + ".list";
		}
		catch (APIException e) {
			log.warn("Error occurred while attempting to purge concept map type", e);
			request.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, Context.getMessageSourceService().getMessage(
			    "ConceptMapType.purge.error"), WebRequest.SCOPE_SESSION);
		}
		
		return "redirect:" + CONCEPT_MAP_TYPE_FORM_URL + ".form?conceptMapTypeId=" + id;
	}
}

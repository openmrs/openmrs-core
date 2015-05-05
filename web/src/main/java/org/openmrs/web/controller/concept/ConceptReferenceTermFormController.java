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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.FactoryUtils;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.ConceptReferenceTerm;
import org.openmrs.ConceptReferenceTermMap;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.validator.ConceptReferenceTermValidator;
import org.openmrs.web.WebConstants;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ValidationUtils;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.WebRequest;

/**
 * Controller class for processing requests for managing Concept reference terms
 */
@Controller
public class ConceptReferenceTermFormController {
	
	/**
	 * Logger for this class
	 */
	private static final Log log = LogFactory.getLog(ConceptReferenceTermFormController.class);
	
	private static final String CONCEPT_REFERENCE_TERM_FORM_URL = "/admin/concepts/conceptReferenceTerm";
	
	private static final String CONCEPT_REFERENCE_TERM_FORM = "/admin/concepts/conceptReferenceTermForm";
	
	private static final String FIND_CONCEPT_REFERENCE_TERM_URL = "/admin/concepts/conceptReferenceTerms.htm";
	
	/**
	 * Processes requests to display the form
	 */
	@RequestMapping(method = RequestMethod.GET, value = CONCEPT_REFERENCE_TERM_FORM_URL)
	public String showForm() {
		return CONCEPT_REFERENCE_TERM_FORM;
	}
	
	@ModelAttribute("conceptReferenceTermModel")
	public ConceptReferenceTermModel getConceptReferenceTermFormModel(
	        @RequestParam(value = "conceptReferenceTermId", required = false) Integer conceptReferenceTermId) {
		
		ConceptReferenceTerm conceptReferenceTerm = null;
		if (conceptReferenceTermId != null) {
			conceptReferenceTerm = Context.getConceptService().getConceptReferenceTerm(conceptReferenceTermId);
		}
		if (conceptReferenceTerm == null) {
			conceptReferenceTerm = new ConceptReferenceTerm();
		}
		
		return new ConceptReferenceTermModel(conceptReferenceTerm);
	}
	
	@SuppressWarnings("unchecked")
	@ModelAttribute("referenceTermMappingsToThisTerm")
	public List<ConceptReferenceTermMap> getConceptMappingsToThisTerm(
	        @ModelAttribute ConceptReferenceTerm conceptReferenceTerm) {
		if (conceptReferenceTerm.getConceptReferenceTermId() != null) {
			return Context.getConceptService().getReferenceTermMappingsTo(conceptReferenceTerm);
		}
		
		return ListUtils.EMPTY_LIST;
	}
	
	/**
	 * Processes requests to save/update a concept reference term
	 *
	 * @param request the {@link WebRequest} object
	 * @param conceptReferenceTermModel the concept reference term object to save/update
	 * @param result the {@link BindingResult} object
	 * @return the url to redirect to
	 */
	@RequestMapping(method = RequestMethod.POST, value = CONCEPT_REFERENCE_TERM_FORM_URL)
	public String saveConceptReferenceTerm(WebRequest request,
	        @ModelAttribute(value = "conceptReferenceTermModel") ConceptReferenceTermModel conceptReferenceTermModel,
	        BindingResult result) {
		
		ConceptReferenceTerm conceptReferenceTerm = conceptReferenceTermModel.getConceptReferenceTerm();
		// add all the term maps
		//store ids of already mapped terms so that we don't map a term multiple times
		Set<Integer> mappedTermIds = null;
		for (int x = 0; x < conceptReferenceTermModel.getTermMaps().size(); x++) {
			if (mappedTermIds == null) {
				mappedTermIds = new HashSet<Integer>();
			}
			ConceptReferenceTermMap map = conceptReferenceTermModel.getTermMaps().get(x);
			
			if (map != null && map.getTermB() != null) {
				//skip past this mapping because its term is already in use by another mapping for this term
				if (!mappedTermIds.add(map.getTermB().getConceptReferenceTermId())) {
					continue;
				}
				
				if (request.getParameter("_termMaps[" + x + "].exists") == null) {
					// because of the _termMap[x].exists input name in the jsp, the value will be null for
					// deleted maps, remove the map.
					conceptReferenceTerm.removeConceptReferenceTermMap(map);
				} else {
					if (map.getConceptMapType() == null) {
						result.rejectValue("termMaps[" + x + "]", "ConceptReferenceTerm.error.mapTypeRequired",
						    "Concept Map Type is required");
						log.warn("Concept Map Type is required");
						break;
					} else if (map.getTermB().equals(conceptReferenceTerm)) {
						result.rejectValue("termMaps[" + x + "]", "ConceptReferenceTerm.map.sameTerm",
						    "Cannot map a concept reference term to itself");
						log.warn("Cannot map a concept reference term to itself");
						break;
					} else if (!conceptReferenceTerm.getConceptReferenceTermMaps().contains(map)) {
						conceptReferenceTerm.addConceptReferenceTermMap(map);
					}
				}
			}
		}
		
		//if there are errors  with the term
		if (!result.hasErrors()) {
			try {
				result.pushNestedPath("conceptReferenceTerm");
				ValidationUtils.invokeValidator(new ConceptReferenceTermValidator(), conceptReferenceTerm, result);
				ValidationUtils.invokeValidator(new ConceptReferenceTermWebValidator(), conceptReferenceTerm, result);
			}
			finally {
				result.popNestedPath();
			}
		}
		
		if (!result.hasErrors()) {
			try {
				conceptReferenceTerm = Context.getConceptService().saveConceptReferenceTerm(conceptReferenceTerm);
				if (log.isDebugEnabled()) {
					log.debug("Saved concept reference term: " + conceptReferenceTerm.toString());
				}
				request.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "ConceptReferenceTerm.saved", WebRequest.SCOPE_SESSION);
				return "redirect:" + CONCEPT_REFERENCE_TERM_FORM_URL + ".form?conceptReferenceTermId="
				        + conceptReferenceTerm.getConceptReferenceTermId();
			}
			catch (APIException e) {
				log.error("Error while saving concept reference term", e);
				request.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "ConceptReferenceTerm.save.error",
				    WebRequest.SCOPE_SESSION);
			}
		}
		
		return CONCEPT_REFERENCE_TERM_FORM;
	}
	
	/**
	 * Processes requests to retire concept reference terms
	 *
	 * @param request the {@link WebRequest} object
	 * @param conceptReferenceTermModel the concept reference term model for the term to retire
	 * @param retireReason the reason why the concept reference term is being retired
	 * @return the url to redirect to
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/admin/concepts/retireConceptReferenceTerm")
	public String retireConceptReferenceTerm(WebRequest request,
	        @ModelAttribute(value = "conceptReferenceTermModel") ConceptReferenceTermModel conceptReferenceTermModel,
	        @RequestParam(required = false, value = "retireReason") String retireReason) {
		
		if (!StringUtils.hasText(retireReason)) {
			retireReason = Context.getMessageSourceService().getMessage("general.default.retireReason");
		}
		
		try {
			ConceptReferenceTerm conceptReferenceTerm = conceptReferenceTermModel.getConceptReferenceTerm();
			Context.getConceptService().retireConceptReferenceTerm(conceptReferenceTerm, retireReason);
			if (log.isDebugEnabled()) {
				log.debug("Retired concept reference term with id: " + conceptReferenceTerm.getId());
			}
			request.setAttribute(WebConstants.OPENMRS_MSG_ATTR, Context.getMessageSourceService().getMessage(
			    "ConceptReferenceTerm.retired"), WebRequest.SCOPE_SESSION);
			
			return "redirect:" + FIND_CONCEPT_REFERENCE_TERM_URL;
		}
		catch (APIException e) {
			log.error("Error occurred while retiring concept reference term", e);
			request.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, Context.getMessageSourceService().getMessage(
			    "ConceptReferenceTerm.retire.error"), WebRequest.SCOPE_SESSION);
		}
		
		//an error occurred
		return CONCEPT_REFERENCE_TERM_FORM;
	}
	
	/**
	 * Processes requests to unretire concept reference terms
	 *
	 * @param request the {@link WebRequest} object
	 * @param conceptReferenceTermModel the concept reference term model object for the term to
	 *            unretire
	 * @param retireReason the reason why the concept reference term is being unretired
	 * @return the url to redirect to
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/admin/concepts/unretireConceptReferenceTerm")
	public String unretireConceptReferenceTerm(WebRequest request,
	        @ModelAttribute(value = "conceptReferenceTermModel") ConceptReferenceTermModel conceptReferenceTermModel) {
		
		try {
			ConceptReferenceTerm conceptReferenceTerm = conceptReferenceTermModel.getConceptReferenceTerm();
			Context.getConceptService().unretireConceptReferenceTerm(conceptReferenceTerm);
			if (log.isDebugEnabled()) {
				log.debug("Unretired concept reference term with id: " + conceptReferenceTerm.getId());
			}
			request.setAttribute(WebConstants.OPENMRS_MSG_ATTR, Context.getMessageSourceService().getMessage(
			    "ConceptReferenceTerm.unretired"), WebRequest.SCOPE_SESSION);
			
			return "redirect:" + CONCEPT_REFERENCE_TERM_FORM_URL + ".form?conceptReferenceTermId="
			        + conceptReferenceTerm.getConceptReferenceTermId();
		}
		catch (APIException e) {
			log.error("Error occurred while unretiring concept reference term", e);
			request.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, Context.getMessageSourceService().getMessage(
			    "ConceptReferenceTerm.unretire.error"), WebRequest.SCOPE_SESSION);
		}
		
		//an error occurred, show the form
		return CONCEPT_REFERENCE_TERM_FORM;
	}
	
	/**
	 * Processes requests to purge a concept reference term
	 *
	 * @param request the {@link WebRequest} object
	 * @param conceptReferenceTermModel
	 * @return the url to forward to
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/admin/concepts/purgeConceptReferenceTerm")
	public String purgeTerm(WebRequest request,
	        @ModelAttribute(value = "conceptReferenceTermModel") ConceptReferenceTermModel conceptReferenceTermModel) {
		Integer id = conceptReferenceTermModel.getConceptReferenceTerm().getId();
		try {
			Context.getConceptService().purgeConceptReferenceTerm(conceptReferenceTermModel.getConceptReferenceTerm());
			if (log.isDebugEnabled()) {
				log.debug("Purged concept reference term with id: " + id);
			}
			request.setAttribute(WebConstants.OPENMRS_MSG_ATTR, Context.getMessageSourceService().getMessage(
			    "ConceptReferenceTerm.purged"), WebRequest.SCOPE_SESSION);
			return "redirect:" + FIND_CONCEPT_REFERENCE_TERM_URL;
		}
		catch (APIException e) {
			log.warn("Error occurred while attempting to purge concept reference term", e);
			request.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, Context.getMessageSourceService().getMessage(
			    "ConceptReferenceTerm.purge.error"), WebRequest.SCOPE_SESSION);
		}
		
		//send the user back to form
		return "redirect:" + CONCEPT_REFERENCE_TERM_FORM_URL + ".form?conceptReferenceTermId=" + id;
	}
	
	/**
	 * An object of this class represents a model for this controller
	 */
	public class ConceptReferenceTermModel {
		
		ConceptReferenceTerm conceptReferenceTerm;
		
		private List<ConceptReferenceTermMap> termMaps;
		
		/**
		 * Constructor that creates a concept reference term model object from the specified term
		 *
		 * @param conceptReferenceTerm
		 */
		@SuppressWarnings("unchecked")
		public ConceptReferenceTermModel(ConceptReferenceTerm conceptReferenceTerm) {
			this.conceptReferenceTerm = conceptReferenceTerm;
			ArrayList<ConceptReferenceTermMap> maps = null;
			if (conceptReferenceTerm.getConceptReferenceTermMaps().size() == 0) {
				maps = new ArrayList<ConceptReferenceTermMap>();
				maps.add(new ConceptReferenceTermMap(null, null));
			} else {
				maps = new ArrayList<ConceptReferenceTermMap>(conceptReferenceTerm.getConceptReferenceTermMaps());
			}
			
			termMaps = ListUtils.lazyList(maps, FactoryUtils.instantiateFactory(ConceptReferenceTermMap.class));
		}
		
		/**
		 * @return the conceptReferenceTerm
		 */
		public ConceptReferenceTerm getConceptReferenceTerm() {
			return conceptReferenceTerm;
		}
		
		/**
		 * @param conceptReferenceTerm the conceptReferenceTerm to set
		 */
		public void setConceptReferenceTerm(ConceptReferenceTerm conceptReferenceTerm) {
			this.conceptReferenceTerm = conceptReferenceTerm;
		}
		
		/**
		 * @return the termMaps
		 */
		public List<ConceptReferenceTermMap> getTermMaps() {
			return termMaps;
		}
		
		/**
		 * @param termMaps the termMaps to set
		 */
		public void setTermMaps(List<ConceptReferenceTermMap> termMaps) {
			this.termMaps = termMaps;
		}
	}
}

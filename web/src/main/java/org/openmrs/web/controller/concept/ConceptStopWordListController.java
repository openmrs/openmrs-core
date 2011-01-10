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
package org.openmrs.web.controller.concept;

import java.util.List;

import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.ConceptStopWord;
import org.openmrs.api.ConceptService;
import org.openmrs.api.ConceptStopWordException;
import org.openmrs.api.context.Context;
import org.openmrs.web.WebConstants;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * This is the controlling class for the conceptStopWordList.jsp page. This class used to view the list of 
 * ConceptStopWords with the Locale information. And also to delete ConceptStopWord(s).
 * 
 * @see org.openmrs.ConceptStopWord
 * 
 * @since 1.8
 */
@Controller
@RequestMapping(value = "admin/concepts/conceptStopWord.list")
public class ConceptStopWordListController {
	
	/** Logger for this class and subclasses */
	private static final Log log = LogFactory.getLog(ConceptStopWordListController.class);
	
	/**
	 * Handle the delete action
	 * 
	 * @param session http session
	 * @param conceptStopWordsToBeDeleted array of words to be deleted
	 * @return ConceptStopWordList view
	 * @should delete the given ConceptStopWord in the request parameter
	 * @should add the success delete message in session attribute
	 * @should add the already deleted error message in session attribute if delete the same word
	 *         twice
	 */
	@RequestMapping(method = RequestMethod.POST)
	public String handleSubmission(HttpSession session,
	        @RequestParam(required = false, value = "conceptStopWord") String[] conceptStopWordsToBeDeleted) {
		if (conceptStopWordsToBeDeleted != null) {
			
			ConceptService conceptService = Context.getConceptService();
			for (String conceptStopWordToBeDeleted : conceptStopWordsToBeDeleted) {
				try {
					conceptService.deleteConceptStopWord(new Integer(conceptStopWordToBeDeleted));
					session.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "general.deleted");
				}
				catch (ConceptStopWordException e) {
					log.error("Error on deleting concept stop word", e);
					session.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, e.getMessage());
				}
			}
		} else {
			session.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "ConceptStopWord.error.notSelect");
		}
		
		return showForm(session);
	}
	
	/**
	 * This method to load all the Concept Stop Words in the request attribute and return the
	 * ConceptStopWordList view
	 * 
	 * @param session http session
	 * @return ConceptStopWordList view
	 * @should return Concept Stop Word List View
	 * @should add all ConceptStopWords in session attribute
	 */
	@RequestMapping(method = RequestMethod.GET)
	public String showForm(HttpSession session) {
		
		ConceptService conceptService = Context.getConceptService();
		List<ConceptStopWord> conceptStopWordList = conceptService.getAllConceptStopWords();
		session.setAttribute("conceptStopWordList", conceptStopWordList);
		
		return "admin/concepts/conceptStopWordList";
	}
}

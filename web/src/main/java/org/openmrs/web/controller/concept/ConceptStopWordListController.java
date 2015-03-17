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

import javax.servlet.http.HttpSession;

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
					conceptService.deleteConceptStopWord(Integer.valueOf(conceptStopWordToBeDeleted));
					session.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "general.deleted");
				}
				catch (ConceptStopWordException e) {
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

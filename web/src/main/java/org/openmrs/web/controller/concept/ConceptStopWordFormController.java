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

import java.util.Locale;

import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.ConceptStopWord;
import org.openmrs.api.ConceptStopWordException;
import org.openmrs.api.context.Context;
import org.openmrs.util.LocaleUtility;
import org.openmrs.web.WebConstants;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ValidationUtils;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.SimpleFormController;

/**
 * This is the controlling class for the conceptStopWordForm.jsp page. This class used to
 * add a new ConceptStopWord.
 * 
 * @see org.openmrs.ConceptStopWord
 * 
 * @since 1.8
 */
@Controller
@RequestMapping(value = "admin/concepts/conceptStopWord.form")
public class ConceptStopWordFormController {
	
	/**
	 * Logger for this class and subclasses
	 */
	protected final Log log = LogFactory.getLog(getClass());
	
	/**
	 * Handle the add new ConceptStopWord
	 *
	 * @param httpSession
	 * @param commandObject
	 * @param errors
	 * @return
	 * @should add new ConceptStopWord
	 * @should return error message if a duplicate ConceptStopWord is added
	 * @should return error message for an empty ConceptStopWord
	 */
	@RequestMapping(method = RequestMethod.POST)
	public String handleSubmission(HttpSession httpSession, @ModelAttribute("command") ConceptStopWord conceptStopWord,
	        BindingResult errors) {
		
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "value", "ConceptStopWord.error.value.empty");
		
		if (errors.hasErrors()) {
			return showForm();
		}
		
		try {
			Context.getConceptService().saveConceptStopWord(conceptStopWord);
			httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "ConceptStopWord.saved");
		}
		catch (ConceptStopWordException e) {
			log.error("Error on adding concept stop word", e);
			httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, e.getMessage());
			return showForm();
		}
		
		return "redirect:conceptStopWord.list";
	}
	
	@ModelAttribute("command")
	public ConceptStopWord formBackingObject(HttpSession httpSession) throws Exception {
		httpSession.setAttribute("locales", LocaleUtility.getLocalesInOrder());
		return new ConceptStopWord();
	}
	
	@RequestMapping(method = RequestMethod.GET)
	public String showForm() {
		return "admin/concepts/conceptStopWordForm";
	}
}

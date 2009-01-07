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
package org.openmrs.web.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.ConceptComplex;
import org.openmrs.ConceptNumeric;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class ConceptFormValidator implements Validator {
	
	/** Log for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());
	
	/**
	 * Determines if the command object being submitted is a valid type
	 * 
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
    public boolean supports(Class c) {
		return c.equals(Concept.class) || c.equals(ConceptNumeric.class) || c.equals(ConceptComplex.class);
	}
	
	/**
	 * Checks the form object for any inconsistencies/errors
	 * 
	 * @see org.springframework.validation.Validator#validate(java.lang.Object,
	 *      org.springframework.validation.Errors)
	 */
	public void validate(Object obj, Errors errors) {
		Concept concept = (Concept) obj;
		if (concept == null) {
			errors.rejectValue("concept", "error.general");
		} else {
			//Won't work without name and description properties on Concept
			//ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name", "error.name");
			//ValidationUtils.rejectIfEmptyOrWhitespace(errors, "description", "error.description");
		}
	}
	
}

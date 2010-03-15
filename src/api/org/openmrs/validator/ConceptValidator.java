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
package org.openmrs.validator;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.api.APIException;
import org.openmrs.api.DuplicateConceptNameException;
import org.openmrs.api.context.Context;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Validates methods of the {@link Concept} object.
 */
public class ConceptValidator implements Validator {
	
	/** Log for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());
	
	/**
	 * Determines if the command object being submitted is a valid type
	 * 
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	public boolean supports(Class c) {
		return c.equals(Concept.class);
	}
	
	/**
	 * Checks that a given concept object has a unique name or preferred name across the entire
	 * unretired and preferred concept name space in a given locale(should be the default
	 * application locale set in the openmrs context). Currently this method is called just before
	 * the concept is persisted in the database
	 * 
	 * @should fail if there is a duplicate unretired concept name in the locale
	 * @should fail if the preferred name is an empty string
	 * @should fail if the object parameter is null
	 * @should pass if the found duplicate concept is actually retired
	 * @should pass if the concept is being updated with no name change
	 */
	public void validate(Object obj, Errors errorss) throws APIException, DuplicateConceptNameException {
		
		if (obj == null || !(obj instanceof Concept)) {
			
			if (log.isErrorEnabled())
				log.error("The parameter obj should not be null and must be of type" + Concept.class);
			throw new IllegalArgumentException("Concept name is null or of invalid class");
			
		} else {
			
			Concept newConcept = (Concept) obj;
			String newName = null;
			
			//no name to validate, but why is this the case?
			if (newConcept.getName() == null)
				return;
			
			//if the new concept name is in a different locale other than openmrs' default one
			if (newConcept.getName().getLocale() != null && newConcept.getName().getLocale() != Context.getLocale())
				newName = newConcept.getName().getName();
			else
				newName = newConcept.getPreferredName(Context.getLocale()).getName();
			
			if (StringUtils.isBlank(newName)) {
				
				if (log.isErrorEnabled())
					log.error("No preferred name specified for the concept to be validated");
				throw new APIException("Concept name cannot be an empty String");
			}
			if (log.isDebugEnabled())
				log.debug("Looking up concept names matching " + newName);
			
			List<Concept> matchingConcepts = Context.getConceptService().getConceptsByName(newName);
			
			Set<Concept> duplicates = new HashSet<Concept>();
			
			for (Concept c : matchingConcepts) {
				
				//If updating a concept, read past the concept being updated
				if (newConcept.getConceptId() != null && c.getConceptId() == newConcept.getConceptId())
					continue;
				//get only duplicates that are not retired
				if (c.getPreferredName(Context.getLocale()).getName().equalsIgnoreCase(newName) && !c.isRetired()) {
					duplicates.add(c);
				}
			}
			
			if (duplicates.size() > 0) {
				for (Concept duplicate : duplicates) {
					if (log.isErrorEnabled())
						log.error("The name '" + newName + "' is already being used by concept with Id: "
						        + duplicate.getConceptId());
					throw new DuplicateConceptNameException("Duplicate concept name '" + newName + "'");
					
				}
				
			}
			
		}
		
	}
	
}

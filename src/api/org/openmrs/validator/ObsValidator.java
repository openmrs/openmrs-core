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

import java.util.ArrayList;
import java.util.List;

import org.openmrs.Concept;
import org.openmrs.ConceptDatatype;
import org.openmrs.Obs;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Validator for the Obs class. This class checks for anything set on the Obs object that will cause
 * errors or is incorrect. Things checked are similar to:
 * <ul>
 * <li>all required properties are filled in on the Obs object.
 * <li>checks for no recursion in the obs grouping.
 * <li>Makes sure the obs has at least one value (if not an obs grouping)</li>
 * 
 * @see org.openmrs.Obs
 */
public class ObsValidator implements Validator {
	
	/**
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	public boolean supports(Class c) {
		return Obs.class.isAssignableFrom(c);
	}
	
	/**
	 * @see org.springframework.validation.Validator#validate(java.lang.Object,
	 *      org.springframework.validation.Errors)
	 */
	public void validate(Object obj, Errors errors) {
		Obs obs = (Obs) obj;
		List<Obs> ancestors = new ArrayList<Obs>();
		//ancestors.add(obs);
		validateHelper(obs, errors, ancestors, true);
	}
	
	/**
	 * Checks whether obs has all required values, and also checks to make sure that no obs group
	 * contains any of its ancestors
	 * 
	 * @param obs
	 * @param errors
	 * @param ancestors
	 * @param atRootNode whether or not this is the obs that validate() was originally called on. If
	 *            not then we shouldn't reject fields by name.
	 */
	private void validateHelper(Obs obs, Errors errors, List<Obs> ancestors, boolean atRootNode) {
		if (obs.getPersonId() == null && obs.getPersonId() == null)
			errors.rejectValue("person", "error.null");
		Concept c = obs.getConcept();
		if (c == null) {
			errors.rejectValue("concept", "error.null");
		} else {
			ConceptDatatype dt = c.getDatatype();
			if (dt.isBoolean() && obs.getValueAsBoolean() == null) {
				if (atRootNode)
					errors.rejectValue("valueNumeric", "error.null");
				else
					errors.rejectValue("groupMembers", "Obs.error.inGroupMember");
			} else if (dt.isCoded() && obs.getValueCoded() == null) {
				if (atRootNode)
					errors.rejectValue("valueCoded", "error.null");
				else
					errors.rejectValue("groupMembers", "Obs.error.inGroupMember");
			} else if (dt.isDate() && obs.getValueDatetime() == null) {
				if (atRootNode)
					errors.rejectValue("valueDatetime", "error.null");
				else
					errors.rejectValue("groupMembers", "Obs.error.inGroupMember");
			} else if (dt.isNumeric() && obs.getValueNumeric() == null) {
				if (atRootNode)
					errors.rejectValue("valueNumeric", "error.null");
				else
					errors.rejectValue("groupMembers", "Obs.error.inGroupMember");
			} else if (dt.isText() && obs.getValueText() == null) {
				if (atRootNode)
					errors.rejectValue("valueText", "error.null");
				else
					errors.rejectValue("groupMembers", "Obs.error.inGroupMember");
			}
		}
		
		// If an obs fails validation, don't bother checking its children
		if (errors.hasErrors())
			return;
		
		if (ancestors.contains(obs))
			errors.rejectValue("groupMembers", "Obs.error.groupContainsItself");
		
		if (obs.isObsGrouping()) {
			ancestors.add(obs);
			for (Obs child : obs.getGroupMembers()) {
				validateHelper(child, errors, ancestors, false);
			}
			ancestors.remove(ancestors.size() - 1);
		}
	}
	
}

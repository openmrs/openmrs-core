/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.validator;

import java.util.ArrayList;
import java.util.List;

import org.openmrs.Concept;
import org.openmrs.ConceptDatatype;
import org.openmrs.ConceptNumeric;
import org.openmrs.Obs;
import org.openmrs.annotation.Handler;
import org.openmrs.api.context.Context;
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
@Handler(supports = { Obs.class }, order = 50)
public class ObsValidator implements Validator {
	
	public final static int VALUE_TEXT_MAX_LENGTH = 1000;
	
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
	 * @should fail validation if personId is null
	 * @should fail validation if obsDatetime is null
	 * @should fail validation if concept is null
	 * @should fail validation if concept datatype is boolean and valueBoolean is null
	 * @should fail validation if concept datatype is coded and valueCoded is null
	 * @should fail validation if concept datatype is date and valueDatetime is null
	 * @should fail validation if concept datatype is numeric and valueNumeric is null
	 * @should fail validation if concept datatype is text and valueText is null
	 * @should fail validation if obs ancestors contains obs
	 * @should pass validation if all values present
	 * @should fail validation if the parent obs has values
	 * @should reject an invalid concept and drug combination
	 * @should pass if answer concept and concept of value drug match
	 * @should pass validation if field lengths are correct
	 * @should fail validation if field lengths are not correct
	 */
	public void validate(Object obj, Errors errors) {
		Obs obs = (Obs) obj;
		List<Obs> ancestors = new ArrayList<Obs>();
		//ancestors.add(obs);
		validateHelper(obs, errors, ancestors, true);
		ValidateUtil.validateFieldLengths(errors, obj.getClass(), "accessionNumber", "valueModifier", "valueComplex",
		    "comment", "voidReason");
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
		if (obs.getPersonId() == null) {
			errors.rejectValue("person", "error.null");
		}
		if (obs.getObsDatetime() == null) {
			errors.rejectValue("obsDatetime", "error.null");
		}
		
		// if this is an obs group (i.e., parent) make sure that it has no values (other than valueGroupId) set
		if (obs.hasGroupMembers()) {
			if (obs.getValueCoded() != null) {
				errors.rejectValue("valueCoded", "error.not.null");
			}
			
			if (obs.getValueDrug() != null) {
				errors.rejectValue("valueDrug", "error.not.null");
			}
			
			if (obs.getValueDatetime() != null) {
				errors.rejectValue("valueDatetime", "error.not.null");
			}
			
			if (obs.getValueNumeric() != null) {
				errors.rejectValue("valueNumeric", "error.not.null");
			}
			
			if (obs.getValueModifier() != null) {
				errors.rejectValue("valueModifier", "error.not.null");
			}
			
			if (obs.getValueText() != null) {
				errors.rejectValue("valueText", "error.not.null");
			}
			
			if (obs.getValueBoolean() != null) {
				errors.rejectValue("valueBoolean", "error.not.null");
			}
			
			if (obs.getValueComplex() != null) {
				errors.rejectValue("valueComplex", "error.not.null");
			}
			
		}
		// if this is NOT an obs group, make sure that it has at least one value set (not counting obsGroupId)
		else if (obs.getValueBoolean() == null && obs.getValueCoded() == null && obs.getValueCodedName() == null
		        && obs.getValueComplex() == null && obs.getValueDatetime() == null && obs.getValueDrug() == null
		        && obs.getValueModifier() == null && obs.getValueNumeric() == null && obs.getValueText() == null
		        && obs.getComplexData() == null) {
			errors.reject("error.noValue");
		}
		
		// make sure there is a concept associated with the obs
		Concept c = obs.getConcept();
		if (c == null) {
			errors.rejectValue("concept", "error.null");
		}
		// if there is a concept, and this isn't a group, perform validation tests specific to the concept datatype
		else if (!obs.hasGroupMembers()) {
			ConceptDatatype dt = c.getDatatype();
			if (dt != null) {
				if (dt.isBoolean() && obs.getValueBoolean() == null) {
					if (atRootNode) {
						errors.rejectValue("valueBoolean", "error.null");
					} else {
						errors.rejectValue("groupMembers", "Obs.error.inGroupMember");
					}
				} else if (dt.isCoded() && obs.getValueCoded() == null) {
					if (atRootNode) {
						errors.rejectValue("valueCoded", "error.null");
					} else {
						errors.rejectValue("groupMembers", "Obs.error.inGroupMember");
					}
				} else if ((dt.isDateTime() || dt.isDate() || dt.isTime()) && obs.getValueDatetime() == null) {
					if (atRootNode) {
						errors.rejectValue("valueDatetime", "error.null");
					} else {
						errors.rejectValue("groupMembers", "Obs.error.inGroupMember");
					}
				} else if (dt.isNumeric() && obs.getValueNumeric() == null) {
					if (atRootNode) {
						errors.rejectValue("valueNumeric", "error.null");
					} else {
						errors.rejectValue("groupMembers", "Obs.error.inGroupMember");
					}
				} else if (dt.isNumeric()) {
					ConceptNumeric cn = Context.getConceptService().getConceptNumeric(c.getConceptId());
					// If the concept numeric is not precise, the value cannot be a float, so raise an error 
					if (!cn.isPrecise() && Math.ceil(obs.getValueNumeric()) != obs.getValueNumeric()) {
						if (atRootNode) {
							errors.rejectValue("valueNumeric", "error.precision");
						} else {
							errors.rejectValue("groupMembers", "Obs.error.inGroupMember");
						}
					}
					// If the number is higher than the absolute range, raise an error 
					if (cn.getHiAbsolute() != null && cn.getHiAbsolute() < obs.getValueNumeric()) {
						if (atRootNode) {
							errors.rejectValue("valueNumeric", "error.outOfRange.high");
						} else {
							errors.rejectValue("groupMembers", "Obs.error.inGroupMember");
						}
					}
					// If the number is lower than the absolute range, raise an error as well 
					if (cn.getLowAbsolute() != null && cn.getLowAbsolute() > obs.getValueNumeric()) {
						if (atRootNode) {
							errors.rejectValue("valueNumeric", "error.outOfRange.low");
						} else {
							errors.rejectValue("groupMembers", "Obs.error.inGroupMember");
						}
					}
				} else if (dt.isText() && obs.getValueText() == null) {
					if (atRootNode) {
						errors.rejectValue("valueText", "error.null");
					} else {
						errors.rejectValue("groupMembers", "Obs.error.inGroupMember");
					}
				}
				
				//If valueText is longer than the maxlength, raise an error as well.
				if (dt.isText() && obs.getValueText() != null && obs.getValueText().length() > VALUE_TEXT_MAX_LENGTH) {
					if (atRootNode) {
						errors.rejectValue("valueText", "error.exceededMaxLengthOfField");
					} else {
						errors.rejectValue("groupMembers", "Obs.error.inGroupMember");
					}
				}
			} else { // dt is null
				errors.rejectValue("concept", "must have a datatype");
			}
		}
		
		// If an obs fails validation, don't bother checking its children
		if (errors.hasErrors()) {
			return;
		}
		
		if (ancestors.contains(obs)) {
			errors.rejectValue("groupMembers", "Obs.error.groupContainsItself");
		}
		
		if (obs.isObsGrouping()) {
			ancestors.add(obs);
			for (Obs child : obs.getGroupMembers()) {
				validateHelper(child, errors, ancestors, false);
			}
			ancestors.remove(ancestors.size() - 1);
		}
		
		if (obs.getValueCoded() != null && obs.getValueDrug() != null && obs.getValueDrug().getConcept() != null) {
			Concept trueConcept = Context.getConceptService().getTrueConcept();
			Concept falseConcept = Context.getConceptService().getFalseConcept();
			//Ignore if this is not a true or false response since they are stored as coded too
			if (!obs.getValueCoded().equals(trueConcept) && !obs.getValueCoded().equals(falseConcept)
			        && !obs.getValueDrug().getConcept().equals(obs.getValueCoded())) {
				errors.rejectValue("valueDrug", "Obs.error.invalidDrug");
			}
		}
	}
	
}

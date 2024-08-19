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
import java.util.Objects;

import org.openmrs.Concept;
import org.openmrs.ConceptDatatype;
import org.openmrs.ConceptNumeric;
import org.openmrs.ConceptReferenceRange;
import org.openmrs.Obs;
import org.openmrs.ObsReferenceRange;
import org.openmrs.Person;
import org.openmrs.annotation.Handler;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.util.ConceptReferenceRangeUtility;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Validator for the Obs class. This class checks for anything set on the Obs object that will cause
 * errors or is incorrect. Things checked are similar to:
 * <ul>
 * <li>all required properties are filled in on the Obs object.
 * <li>checks for no recursion in the obs grouping.
 * <li>Makes sure the obs has at least one value (if not an obs grouping)</li>
 * </ul>
 * 
 * @see org.openmrs.Obs
 */
@Handler(supports = { Obs.class }, order = 50)
public class ObsValidator implements Validator {
	
	public static final int VALUE_TEXT_MAX_LENGTH = 65535;
	
	/**
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 * <strong>Should</strong> support Obs class
	 */
	@Override
	public boolean supports(Class<?> c) {
		return Obs.class.isAssignableFrom(c);
	}
	
	/**
	 * @see org.springframework.validation.Validator#validate(java.lang.Object,
	 *      org.springframework.validation.Errors)
	 * <strong>Should</strong> fail validation if personId is null
	 * <strong>Should</strong> fail validation if obsDatetime is null
	 * <strong>Should</strong> fail validation if concept is null
	 * <strong>Should</strong> fail validation if concept datatype is boolean and valueBoolean is null
	 * <strong>Should</strong> fail validation if concept datatype is coded and valueCoded is null
	 * <strong>Should</strong> fail validation if concept datatype is date and valueDatetime is null
	 * <strong>Should</strong> fail validation if concept datatype is numeric and valueNumeric is null
	 * <strong>Should</strong> fail validation if concept datatype is text and valueText is null
	 * <strong>Should</strong> fail validation if obs ancestors contains obs
	 * <strong>Should</strong> pass validation if all values present
	 * <strong>Should</strong> fail validation if the parent obs has values
	 * <strong>Should</strong> reject an invalid concept and drug combination
	 * <strong>Should</strong> pass if answer concept and concept of value drug match
	 * <strong>Should</strong> pass validation if field lengths are correct
	 * <strong>Should</strong> fail validation if field lengths are not correct
	 * <strong>Should</strong> not validate if obs is voided
	 * <strong>Should</strong> not validate a voided child obs
	 * <strong>Should</strong> fail for a null object
	 */
	@Override
	public void validate(Object obj, Errors errors) {
		Obs obs = (Obs) obj;
		if (obs == null) {
			throw new APIException("Obs can't be null");
		} else if (obs.getVoided()) {
			return;
		}
		List<Obs> ancestors = new ArrayList<>();
		validateHelper(obs, errors, ancestors, true);
		ValidateUtil.validateFieldLengths(errors, obj.getClass(), "accessionNumber", "valueModifier", "valueComplex",
		    "comment", "voidReason");
		validateConceptReferenceRange(obs, errors);
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
		
		boolean isObsGroup = obs.hasGroupMembers(true);
		// if this is an obs group (i.e., parent) make sure that it has no values (other than valueGroupId) set
		if (isObsGroup) {
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
		else if (!isObsGroup) {
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
					if (!cn.getAllowDecimal() && Math.ceil(obs.getValueNumeric()) != obs.getValueNumeric()) {
						if (atRootNode) {
							errors.rejectValue("valueNumeric", "Obs.error.precision");
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

	/**
	 * This method validates Obs' numeric values:
	 * <ol>
	 *     <li>Validates Obs in relation to criteria e.g. checks patient's age is within the valid range</li>
	 *     <li>Validates if Obs' numeric value is within the valid range; i.e. >= low absolute && <= high absolute.</li>
	 *     <li>Sets field errors if numeric value is outside the valid range</li>
	 * <ol/>
	 *
	 * @param obs Observation to validate
	 * @param errors Errors to record validation issues
	 *               
	 * @since 2.7.0
	 */
	private void validateConceptReferenceRange(Obs obs, Errors errors) {
		ConceptReferenceRange conceptReferenceRange = getReferenceRange(obs.getConcept(), obs.getPerson());

		if (conceptReferenceRange != null) {
			validateAbsoluteRanges(obs, conceptReferenceRange, errors);
			setObsReferenceRange(obs, conceptReferenceRange);
		} else {
			setObsReferenceRange(obs);
		}
	}

	/**
	 * Evaluates the criteria and return the most strict {@link ConceptReferenceRange} for a given concept.
	 * It considers all valid ranges that match the criteria for the person.
	 *
	 * @param concept The concept to evaluate
	 * @param person The person for whom the range is being evaluated
	 * @return The strictest {@link ConceptReferenceRange}, or null if no valid range is found
	 * 
	 * @since 2.7.0
	 */
	public static ConceptReferenceRange getReferenceRange(Concept concept, Person person) {
		if (concept == null || concept.getDatatype() == null || !concept.getDatatype().isNumeric()) {
			return null;
		}

		List<ConceptReferenceRange> referenceRanges = Context.getConceptService()
			.getConceptReferenceRangesByConceptId(concept.getConceptId());

		if (referenceRanges.isEmpty()) {
			return null;
		}

		ConceptReferenceRangeUtility referenceRangeUtility = new ConceptReferenceRangeUtility();
		List<ConceptReferenceRange> validRanges = new ArrayList<>();

		for (ConceptReferenceRange referenceRange : referenceRanges) {
			if (referenceRangeUtility.evaluateCriteria(referenceRange.getCriteria(), person)) {
				validRanges.add(referenceRange);
			}
		}

		if (validRanges.isEmpty()) {
			return null;
		}
		return findStrictestReferenceRange(validRanges);
	}
	
	/**
	 * Finds the strictest {@link ConceptReferenceRange} from a list of valid ranges.
	 * The strictest range is determined by having the highest lower bound and the lowest upper bound.
	 * e.g.
	 * If ConceptReferenceRange one has a range of 80-150.
	 * and ConceptReferenceRange two has a range of 60 - 140,
	 * the "strictest" range will be 80-140. 
	 *
	 * @param conceptReferenceRanges A list of valid {@link ConceptReferenceRange} objects
	 * @return The strictest {@link ConceptReferenceRange} constructed from the strictest bounds
	 * 
	 * @since 2.7.0
	 */
	public static ConceptReferenceRange findStrictestReferenceRange(List<ConceptReferenceRange> conceptReferenceRanges) {
		Double strictestLowAbsolute = conceptReferenceRanges.stream()
			.map(ConceptReferenceRange::getLowAbsolute)
			.filter(Objects::nonNull)
			.max(Double::compareTo)
			.orElse(null);

		Double strictestHiAbsolute = conceptReferenceRanges.stream()
			.map(ConceptReferenceRange::getHiAbsolute)
			.filter(Objects::nonNull)
			.min(Double::compareTo)
			.orElse(null);

		ConceptReferenceRange strictestRange = new ConceptReferenceRange();
		strictestRange.setLowAbsolute(strictestLowAbsolute);
		strictestRange.setHiAbsolute(strictestHiAbsolute);

		return strictestRange;
	}

	/**
	 * Validates the high and low absolute values of the Obs.
	 *
	 * @param obs Observation to validate
	 * @param conceptReferenceRange ConceptReferenceRange containing the range values
	 * @param errors Errors to record validation issues
	 *
	 * @since 2.7.0
	 */
	private void validateAbsoluteRanges(Obs obs, ConceptReferenceRange conceptReferenceRange, Errors errors) {
		if (conceptReferenceRange.getHiAbsolute() != null && conceptReferenceRange.getHiAbsolute() < obs.getValueNumeric()) {
			errors.rejectValue(
				"valueNumeric", 
				"error.value.outOfRange.high", 
				new Object[] { conceptReferenceRange.getLowAbsolute() },
				null
			);
		}
		if (conceptReferenceRange.getLowAbsolute() != null && conceptReferenceRange.getLowAbsolute() > obs.getValueNumeric()) {
			errors.rejectValue(
				"valueNumeric", 
				"error.value.outOfRange.low", 
				new Object[] { conceptReferenceRange.getLowAbsolute() },
				null
			);
		}
	}

	/**
	 * Builds and sets the ObsReferenceRange for the given Obs.
	 *
	 * @param obs Observation to set the reference range
	 * @param conceptReferenceRange ConceptReferenceRange used to build the ObsReferenceRange
	 *
	 * @since 2.7.0
	 */
	private void setObsReferenceRange(Obs obs, ConceptReferenceRange conceptReferenceRange) {
		ObsReferenceRange obsRefRange = new ObsReferenceRange();

		obsRefRange.setHiAbsolute(conceptReferenceRange.getHiAbsolute());
		obsRefRange.setHiCritical(conceptReferenceRange.getHiCritical());
		obsRefRange.setHiNormal(conceptReferenceRange.getHiNormal());
		obsRefRange.setLowAbsolute(conceptReferenceRange.getLowAbsolute());
		obsRefRange.setLowCritical(conceptReferenceRange.getLowCritical());
		obsRefRange.setLowNormal(conceptReferenceRange.getLowNormal());
		obsRefRange.setObs(obs);

		obs.setReferenceRange(obsRefRange);
	}

	/**
	 * Builds and sets the ObsReferenceRange from concept numeric values.
	 *
	 * @param obs Observation to set the reference range
	 *
	 * @since 2.7.0
	 */
	private void setObsReferenceRange(Obs obs) {
		if (obs.getConcept() == null) {
			return;
		}
		
		ConceptNumeric conceptNumeric = Context.getConceptService().getConceptNumeric(obs.getConcept().getId());

		if (conceptNumeric != null && obs.getId() == null) {
			ObsReferenceRange obsRefRange = new ObsReferenceRange();

			obsRefRange.setHiAbsolute(conceptNumeric.getHiAbsolute());
			obsRefRange.setHiCritical(conceptNumeric.getHiCritical());
			obsRefRange.setHiNormal(conceptNumeric.getHiNormal());
			obsRefRange.setLowAbsolute(conceptNumeric.getLowAbsolute());
			obsRefRange.setLowCritical(conceptNumeric.getLowCritical());
			obsRefRange.setLowNormal(conceptNumeric.getLowNormal());
			obsRefRange.setObs(obs);
			
			obs.setReferenceRange(obsRefRange);
		}
	}
	
}

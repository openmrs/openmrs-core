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
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.openmrs.Concept;
import org.openmrs.ConceptDatatype;
import org.openmrs.ConceptNumeric;
import org.openmrs.ConceptReferenceRange;
import org.openmrs.Obs;
import org.openmrs.ObsReferenceRange;
import org.openmrs.annotation.Handler;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.hibernate.HibernateUtil;
import org.openmrs.util.ConceptReferenceRangeUtility;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
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
		validateHelper(obs, errors, ancestors);
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
	 */
	private void validateHelper(Obs obs, Errors errors, List<Obs> ancestors) {
		boolean inGroupMember = !ancestors.isEmpty();
		if (obs.getPersonId() == null) {
			rejectValue(errors, obs, inGroupMember, "person", "error.null");
		}
		if (obs.getObsDatetime() == null) {
			rejectValue(errors, obs, inGroupMember, "obsDatetime", "error.null");
		}
		
		boolean isObsGroup = obs.hasGroupMembers(true);
		// if this is an obs group (i.e., parent) make sure that it has no values (other than valueGroupId) set
		if (isObsGroup) {
			if (obs.getValueCoded() != null) {
				rejectValue(errors, obs, inGroupMember, "valueCoded", "error.not.null");
			}
			
			if (obs.getValueDrug() != null) {
				rejectValue(errors, obs, inGroupMember, "valueDrug", "error.not.null");
			}
			
			if (obs.getValueDatetime() != null) {
				rejectValue(errors, obs, inGroupMember, "valueDatetime", "error.not.null");
			}
			
			if (obs.getValueNumeric() != null) {
				rejectValue(errors, obs, inGroupMember, "valueNumeric", "error.not.null");
			}
			
			if (obs.getValueModifier() != null) {
				rejectValue(errors, obs, inGroupMember, "valueModifier", "error.not.null");
			}
			
			if (obs.getValueText() != null) {
				rejectValue(errors, obs, inGroupMember, "valueText", "error.not.null");
			}
			
			if (obs.getValueBoolean() != null) {
				rejectValue(errors, obs, inGroupMember, "valueBoolean", "error.not.null");
			}
			
			if (obs.getValueComplex() != null) {
				rejectValue(errors, obs, inGroupMember, "valueComplex", "error.not.null");
			}
			
		}
		// if this is NOT an obs group, make sure that it has at least one value set (not counting obsGroupId)
		else if (obs.getValueBoolean() == null && obs.getValueCoded() == null && obs.getValueCodedName() == null
		        && obs.getValueComplex() == null && obs.getValueDatetime() == null && obs.getValueDrug() == null
		        && obs.getValueModifier() == null && obs.getValueNumeric() == null && obs.getValueText() == null
		        && obs.getComplexData() == null) {
			reject(errors, obs, inGroupMember, "error.noValue");
		}
		
		// make sure there is a concept associated with the obs
		Concept c = obs.getConcept();
		if (c == null) {
			rejectValue(errors, obs, inGroupMember, "concept", "error.null");
		}
		// if there is a concept, and this isn't a group, perform validation tests specific to the concept datatype
		else if (!isObsGroup) {
			ConceptDatatype dt = c.getDatatype();
			if (dt != null) {
				if (dt.isBoolean() && obs.getValueBoolean() == null) {
					rejectValue(errors, obs, inGroupMember, "valueBoolean", "error.null");
				} else if (dt.isCoded() && obs.getValueCoded() == null) {
					rejectValue(errors, obs, inGroupMember, "valueCoded", "error.null");
				} else if ((dt.isDateTime() || dt.isDate() || dt.isTime()) && obs.getValueDatetime() == null) {
					rejectValue(errors, obs, inGroupMember, "valueDatetime", "error.null");
				} else if (dt.isNumeric() && obs.getValueNumeric() == null) {
					rejectValue(errors, obs, inGroupMember, "valueNumeric", "error.null");
				} else if (dt.isNumeric()) {
					ConceptNumeric cn = Context.getConceptService().getConceptNumeric(c.getConceptId());
					// If the concept numeric is not precise, the value cannot be a float, so raise an error 
					if (!cn.getAllowDecimal() && Math.ceil(obs.getValueNumeric()) != obs.getValueNumeric()) {
						rejectValue(errors, obs, inGroupMember, "valueNumeric", "Obs.error.precision");
					}
					
					validateConceptReferenceRange(obs, errors, inGroupMember);
				} else if (dt.isText() && obs.getValueText() == null) {
					rejectValue(errors, obs, inGroupMember, "valueText", "error.null");
				}
				
				//If valueText is longer than the maxlength, raise an error as well.
				if (dt.isText() && obs.getValueText() != null && obs.getValueText().length() > VALUE_TEXT_MAX_LENGTH) {
					rejectValue(errors, obs, inGroupMember, "valueText", "error.exceededMaxLengthOfField");
				}
			} else { // dt is null
				rejectValue(errors, obs, inGroupMember, "concept", "must have a datatype");
			}
		}
		
		// If an obs fails validation, don't bother checking its children
		if (errors.hasErrors()) {
			return;
		}
		
		if (ancestors.contains(obs)) {
			rejectValue(errors, obs, inGroupMember, "groupMembers", "Obs.error.groupContainsItself");
		}
		
		Set<Obs> groupMembers = obs.getGroupMembers();
		if (groupMembers != null && !groupMembers.isEmpty()) {
			List<Obs> orderedGroupMembers = new ArrayList<>(groupMembers);
			orderedGroupMembers.sort(Comparator.comparing(Obs::getUuid, Comparator.nullsLast(String::compareTo)));
			ancestors.add(obs);
			for (int index = 0; index < orderedGroupMembers.size(); index++) {
				Obs child = orderedGroupMembers.get(index);
				if (!child.getVoided()) {
					if (ancestors.contains(child)) {
						rejectValue(errors, obs, inGroupMember, "groupMembers", "Obs.error.groupContainsItself");
						continue;
					}
					errors.pushNestedPath("groupMembers[" + index + "]");
					try {
						validateHelper(child, errors, ancestors);
					} finally {
						errors.popNestedPath();
					}
				}
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
	 */
	private void validateConceptReferenceRange(Obs obs, Errors errors, boolean inGroupMember) {
		ConceptReferenceRange conceptReferenceRange = getReferenceRange(obs);

		if (conceptReferenceRange != null) {
			validateAbsoluteRanges(obs, conceptReferenceRange, errors, inGroupMember);
			
			if (obs.getId() == null) {
				setObsReferenceRange(obs, conceptReferenceRange);
			}
		} else if (obs.getId() == null) {
			setObsReferenceRange(obs);
		}
		setObsInterpretation(obs);
	}

	/**
	 * Evaluates the criteria and return the most strict {@link ConceptReferenceRange} for a given concept
	 * and patient contained in an observation.
	 * It considers all valid ranges that match the criteria for the person.
	 *
	 * @param obs containing The concept and patient for whom the range is being evaluated
	 * @return The strictest {@link ConceptReferenceRange}, or null if no valid range is found
	 * 
	 * @since 2.7.0
	 */
	public ConceptReferenceRange getReferenceRange(Obs obs) {
		Concept concept = HibernateUtil.getRealObjectFromProxy(obs.getConcept());
		if (concept == null || concept.getDatatype() == null || !concept.getDatatype().isNumeric()) {
			return null;
		}
		
		ConceptNumeric conceptNumeric = (ConceptNumeric) concept;

		List<ConceptReferenceRange> referenceRanges = Context.getConceptService()
			.getConceptReferenceRangesByConceptId(concept.getConceptId());

		if (referenceRanges.isEmpty()) {
			return getDefaultReferenceRange(conceptNumeric);
		}

		ConceptReferenceRangeUtility referenceRangeUtility = new ConceptReferenceRangeUtility();
		List<ConceptReferenceRange> validRanges = new ArrayList<>();

		for (ConceptReferenceRange referenceRange : referenceRanges) {
			if (referenceRangeUtility.evaluateCriteria(StringEscapeUtils.unescapeHtml4(referenceRange.getCriteria()), obs)) {
				validRanges.add(referenceRange);
			}
		}

		if (validRanges.isEmpty()) {
			ConceptReferenceRange defaultReferenceRange = getDefaultReferenceRange(conceptNumeric);
			if (defaultReferenceRange != null) {
				validRanges.add(defaultReferenceRange);
			} else {
				return null;
			}
		}
		
		return findStrictestReferenceRange(validRanges);
	}

	/**
	 * Loads the reference range from the ConceptNumeric if no reference ranges are associated with
	 * this concept and person.
	 * 
	 * @param conceptNumeric A {@link ConceptNumeric} to extract the default values from
	 * @return a {@link ConceptReferenceRange} containing the reference range from the concept
	 */
	private static ConceptReferenceRange getDefaultReferenceRange(ConceptNumeric conceptNumeric) {
		if (conceptNumeric == null || (
			conceptNumeric.getHiAbsolute() == null &&
			conceptNumeric.getHiCritical() == null &&
			conceptNumeric.getHiNormal() == null &&
			conceptNumeric.getLowAbsolute() == null &&
			conceptNumeric.getLowCritical() == null &&
			conceptNumeric.getLowNormal() == null
		)) {
			return null;
		}
		
		ConceptReferenceRange defaultReferenceRange = new ConceptReferenceRange();
		defaultReferenceRange.setConceptNumeric(conceptNumeric);
		defaultReferenceRange.setHiAbsolute(conceptNumeric.getHiAbsolute());
		defaultReferenceRange.setHiCritical(conceptNumeric.getHiCritical());
		defaultReferenceRange.setHiNormal(conceptNumeric.getHiNormal());
		defaultReferenceRange.setLowAbsolute(conceptNumeric.getLowAbsolute());
		defaultReferenceRange.setLowCritical(conceptNumeric.getLowCritical());
		defaultReferenceRange.setLowNormal(conceptNumeric.getLowNormal());
		return defaultReferenceRange;
	}

	/**
	 * Finds the strictest {@link ConceptReferenceRange} from a list of valid ranges.
	 * The strictest range is determined separately for each value, e.g., the lowAbsolute will
	 * be the highest lowAbsolute of any matching range, the lowCritical value will be the
	 * highest lowCritical value of any matching range.
	 * e.g.
	 * If ConceptReferenceRange-1 has a range of 80-150.
	 * and ConceptReferenceRange-2 has a range of 60-140,
	 * the "strictest" range will be 80-140. 
	 *
	 * @param conceptReferenceRanges A list of valid {@link ConceptReferenceRange} objects
	 * @return The strictest {@link ConceptReferenceRange} constructed from the strictest bounds
	 */
	private ConceptReferenceRange findStrictestReferenceRange(List<ConceptReferenceRange> conceptReferenceRanges) {
		if (conceptReferenceRanges.size() == 1) {
			return conceptReferenceRanges.get(0);
		}

		ConceptReferenceRange strictestRange = new ConceptReferenceRange();
		strictestRange.setConceptNumeric(conceptReferenceRanges.get(0).getConceptNumeric());

		for (ConceptReferenceRange conceptReferenceRange : conceptReferenceRanges) {
			if (conceptReferenceRange.getLowAbsolute() != null && 
					(strictestRange.getLowAbsolute() == null || strictestRange.getLowAbsolute() < conceptReferenceRange.getLowAbsolute())) {
				strictestRange.setLowAbsolute(conceptReferenceRange.getLowAbsolute());
			}
			
			if (conceptReferenceRange.getLowCritical() != null && 
					(strictestRange.getLowCritical() == null || strictestRange.getLowCritical() < conceptReferenceRange.getLowCritical())) {
				strictestRange.setLowCritical(conceptReferenceRange.getLowCritical());
			}
			
			if (conceptReferenceRange.getLowNormal() != null &&
					(strictestRange.getLowNormal() == null || strictestRange.getLowNormal() < conceptReferenceRange.getLowNormal())) {
				strictestRange.setLowNormal(conceptReferenceRange.getLowNormal());
			}
			
			if (conceptReferenceRange.getHiNormal() != null &&
					(strictestRange.getHiNormal() == null || strictestRange.getHiNormal() > conceptReferenceRange.getHiNormal())) {
				strictestRange.setHiNormal(conceptReferenceRange.getHiNormal());
			}
			
			if (conceptReferenceRange.getHiCritical() != null &&
					(strictestRange.getHiCritical() == null || strictestRange.getHiCritical() > conceptReferenceRange.getHiCritical())) {
				strictestRange.setHiCritical(conceptReferenceRange.getHiCritical());
			}
			
			if (conceptReferenceRange.getHiAbsolute() != null &&
					(strictestRange.getHiAbsolute() == null || strictestRange.getHiAbsolute() > conceptReferenceRange.getHiAbsolute())) {
				strictestRange.setHiAbsolute(conceptReferenceRange.getHiAbsolute());
			}
		}
		
		return strictestRange;
	}

	/**
	 * Validates the high and low absolute values of the Obs.
	 *
	 * @param obs Observation to validate
	 * @param conceptReferenceRange ConceptReferenceRange containing the range values
	 * @param errors Errors to record validation issues
	 */
	private void validateAbsoluteRanges(Obs obs, ConceptReferenceRange conceptReferenceRange, Errors errors, boolean inGroupMember) {
		if (conceptReferenceRange.getHiAbsolute() != null && conceptReferenceRange.getHiAbsolute() < obs.getValueNumeric()) {
			rejectValue(
				errors,
				obs,
				inGroupMember,
				"valueNumeric",
				"error.value.outOfRange.high",
				new Object[] { conceptReferenceRange.getHiAbsolute(), obs.getValueNumeric() }
			);
		}
		
		if (conceptReferenceRange.getLowAbsolute() != null && conceptReferenceRange.getLowAbsolute() > obs.getValueNumeric()) {
			rejectValue(
				errors,
				obs,
				inGroupMember,
				"valueNumeric",
				"error.value.outOfRange.low",
				new Object[] { conceptReferenceRange.getLowAbsolute(), obs.getValueNumeric() }
			);
		}
	}

	private String getGroupMemberIdentifier(Obs obs) {
		String identifier = null;
		Concept concept = obs.getConcept();
		if (concept != null && concept.getName() != null) {
			identifier = concept.getName().getName();
		}
		
		String obsId = obs.getObsId() != null ? obs.getObsId().toString() : obs.getUuid();
		if (StringUtils.isNotBlank(identifier)) {
			return identifier + " (" + obsId + ")";
		}
		return obsId;
	}

	private void rejectValue(Errors errors, Obs obs, boolean inGroupMember, String field, String errorCode) {
		rejectValue(errors, obs, inGroupMember, field, errorCode, null);
	}

	private void rejectValue(Errors errors, Obs obs, boolean inGroupMember, String field, String errorCode, Object[] args) {
		int fieldErrorCount = errors.getFieldErrorCount();
		errors.rejectValue(field, errorCode, args, null);
		if (inGroupMember) {
			FieldError fieldError = errors.getFieldErrors().get(fieldErrorCount);
			String message = Context.getMessageSourceService().getMessage(fieldError, Context.getLocale());
			String detail = field + ": " + message;
			errors.reject("Obs.error.inGroupMember", new Object[] { getGroupMemberIdentifier(obs), detail }, null);
		}
	}

	private void reject(Errors errors, Obs obs, boolean inGroupMember, String errorCode) {
		reject(errors, obs, inGroupMember, errorCode, null);
	}

	private void reject(Errors errors, Obs obs, boolean inGroupMember, String errorCode, Object[] args) {
		int globalErrorCount = errors.getGlobalErrorCount();
		errors.reject(errorCode, args, null);
		if (inGroupMember) {
			String message = Context.getMessageSourceService().getMessage(errors.getGlobalErrors().get(globalErrorCount),
			    Context.getLocale());
			errors.reject("Obs.error.inGroupMember", new Object[] { getGroupMemberIdentifier(obs), message }, null);
		}
	}

	/**
	 * Builds and sets the ObsReferenceRange for the given Obs.
	 *
	 * @param obs Observation to set the reference range
	 * @param conceptReferenceRange ConceptReferenceRange used to build the ObsReferenceRange
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
	 */
	private void setObsReferenceRange(Obs obs) {
		if (obs.getConcept() == null) {
			return;
		}
		
		ConceptNumeric conceptNumeric = Context.getConceptService().getConceptNumeric(obs.getConcept().getId());

		if (conceptNumeric != null) {
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

	/**
	 * This method sets Obs interpretation based on the current obs' numeric value.
	 *
	 * @param obs Observation to set the interpretation
	 */
	private void setObsInterpretation(Obs obs) {
		ObsReferenceRange referenceRange = obs.getReferenceRange();
		if (referenceRange == null || obs.getValueNumeric() == null) {
			return;
		}
		
		Double obsValue = obs.getValueNumeric();
		Double hiCritical = referenceRange.getHiCritical(); 
		Double lowCritical = referenceRange.getLowCritical();
		Double lowNormal = referenceRange.getLowNormal();
		Double hiNormal = referenceRange.getHiNormal();
		
		if (hiCritical != null && obsValue >= hiCritical) {
			obs.setInterpretation(Obs.Interpretation.CRITICALLY_HIGH);
		} else if (hiNormal != null && obsValue > hiNormal) {
			obs.setInterpretation(Obs.Interpretation.HIGH);
		} else if (lowCritical != null && obsValue <= lowCritical) {
			obs.setInterpretation(Obs.Interpretation.CRITICALLY_LOW);
		} else if (lowNormal != null && obsValue < lowNormal) {
			obs.setInterpretation(Obs.Interpretation.LOW);
		} else {
			obs.setInterpretation(Obs.Interpretation.NORMAL);
		}
	}
	
}

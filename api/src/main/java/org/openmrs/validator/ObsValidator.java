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
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.Concept;
import org.openmrs.ConceptDatatype;
import org.openmrs.ConceptMap;
import org.openmrs.ConceptNumeric;
import org.openmrs.ConceptReferenceRange;
import org.openmrs.ConceptReferenceRangeContext;
import org.openmrs.ConceptReferenceTerm;
import org.openmrs.Obs;
import org.openmrs.ObsReferenceRange;
import org.openmrs.annotation.Handler;
import org.openmrs.api.APIException;
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
 * </ul>
 *
 * @see org.openmrs.Obs
 */
@Handler(supports = { Obs.class }, order = 50)
public class ObsValidator implements Validator {

	public static final int VALUE_TEXT_MAX_LENGTH = 65535;

	private static final String GROUP_MEMBERS_FIELD = "groupMembers";

	/**
	 * <p>
	 * <strong>Should</strong> support Obs class
	 *
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	@Override
	public boolean supports(Class<?> c) {
		return Obs.class.isAssignableFrom(c);
	}

	/**
	 * <p>
	 * <strong>Should</strong> fail validation if personId is null<br/>
	 * <strong>Should</strong> fail validation if obsDatetime is null<br/>
	 * <strong>Should</strong> fail validation if concept is null<br/>
	 * <strong>Should</strong> fail validation if concept datatype is boolean and valueBoolean is
	 * null<br/>
	 * <strong>Should</strong> fail validation if concept datatype is coded and valueCoded is null<br/>
	 * <strong>Should</strong> fail validation if concept datatype is date and valueDatetime is
	 * null<br/>
	 * <strong>Should</strong> fail validation if concept datatype is numeric and valueNumeric is
	 * null<br/>
	 * <strong>Should</strong> fail validation if concept datatype is text and valueText is null<br/>
	 * <strong>Should</strong> fail validation if obs ancestors contains obs<br/>
	 * <strong>Should</strong> pass validation if all values present<br/>
	 * <strong>Should</strong> fail validation if the parent obs has values<br/>
	 * <strong>Should</strong> reject an invalid concept and drug combination<br/>
	 * <strong>Should</strong> pass if answer concept and concept of value drug match<br/>
	 * <strong>Should</strong> pass validation if field lengths are correct<br/>
	 * <strong>Should</strong> fail validation if field lengths are not correct<br/>
	 * <strong>Should</strong> not validate if obs is voided<br/>
	 * <strong>Should</strong> not validate a voided child obs<br/>
	 * <strong>Should</strong> fail for a null object
	 *
	 * @see org.springframework.validation.Validator#validate(java.lang.Object,
	 *      org.springframework.validation.Errors)
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
	}

	/**
	 * Checks whether obs has all required values, and also checks to make sure that no obs group
	 * contains any of its ancestors
	 *
	 * @param obs
	 * @param errors
	 * @param ancestors
	 * @param atRootNode whether or not this is the obs that validate() was originally called on. If not
	 *            then we shouldn't reject fields by name.
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
			if (dt == null) {
				errors.rejectValue("concept", "must have a datatype");
			} else {
				validateValueForDatatype(obs, dt, errors, atRootNode);
			}
		}

		// If an obs fails validation, don't bother checking its children
		if (errors.hasErrors()) {
			return;
		}

		if (ancestors.contains(obs)) {
			errors.rejectValue(GROUP_MEMBERS_FIELD, "Obs.error.groupContainsItself");
		}

		Set<Obs> groupMembers = obs.getGroupMembers();
		if (groupMembers != null && !groupMembers.isEmpty()) {
			ancestors.add(obs);
			for (Obs child : groupMembers) {
				if (!child.getVoided()) {
					validateHelper(child, errors, ancestors, false);
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
	 * Performs the value presence/precision checks specific to a concept's datatype, and triggers
	 * interpretation derivation for datatypes that support it. Extracted out of {@link #validateHelper}
	 * so that method's cognitive complexity stays within this project's SonarCloud quality gate.
	 *
	 * @param obs Observation to validate
	 * @param dt the datatype of obs' concept
	 * @param errors Errors to record validation issues
	 * @param atRootNode whether or not this is the obs that validate() was originally called on
	 */
	private void validateValueForDatatype(Obs obs, ConceptDatatype dt, Errors errors, boolean atRootNode) {
		if (dt.isBoolean() && obs.getValueBoolean() == null) {
			rejectValueOrGroupMember(errors, atRootNode, "valueBoolean", "error.null");
		} else if (dt.isCoded() && obs.getValueCoded() == null) {
			rejectValueOrGroupMember(errors, atRootNode, "valueCoded", "error.null");
		} else if (dt.isCoded()) {
			validateAndInterpret(obs, errors, atRootNode);
		} else if ((dt.isDateTime() || dt.isDate() || dt.isTime()) && obs.getValueDatetime() == null) {
			rejectValueOrGroupMember(errors, atRootNode, "valueDatetime", "error.null");
		} else if (dt.isNumeric() && obs.getValueNumeric() == null) {
			rejectValueOrGroupMember(errors, atRootNode, "valueNumeric", "error.null");
		} else if (dt.isNumeric()) {
			validateNumericPrecision(obs, errors, atRootNode);
			validateAndInterpret(obs, errors, atRootNode);
		} else if (dt.isText() && obs.getValueText() == null) {
			rejectValueOrGroupMember(errors, atRootNode, "valueText", "error.null");
		}

		//If valueText is longer than the maxlength, raise an error as well.
		if (dt.isText() && obs.getValueText() != null && obs.getValueText().length() > VALUE_TEXT_MAX_LENGTH) {
			rejectValueOrGroupMember(errors, atRootNode, "valueText", "error.exceededMaxLengthOfField");
		}
	}

	/**
	 * Raises the given field error when validating the root obs, or the generic "in group member" error
	 * otherwise. Centralizes a check that was previously duplicated across every datatype branch in
	 * {@link #validateValueForDatatype}.
	 *
	 * @param errors Errors to record validation issues
	 * @param atRootNode whether or not this is the obs that validate() was originally called on
	 * @param field the field to reject when atRootNode is true
	 * @param errorCode the error code to use when atRootNode is true
	 */
	private void rejectValueOrGroupMember(Errors errors, boolean atRootNode, String field, String errorCode) {
		if (atRootNode) {
			errors.rejectValue(field, errorCode);
		} else {
			errors.rejectValue(GROUP_MEMBERS_FIELD, "Obs.error.inGroupMember");
		}
	}

	/**
	 * Rejects the Obs' numeric value if the concept requires an integer value but a decimal was
	 * supplied.
	 *
	 * @param obs Observation to validate
	 * @param errors Errors to record validation issues
	 * @param atRootNode whether or not this is the obs that validate() was originally called on
	 */
	private void validateNumericPrecision(Obs obs, Errors errors, boolean atRootNode) {
		ConceptNumeric cn = Context.getConceptService().getConceptNumeric(obs.getConcept().getConceptId());
		// If the concept numeric is not precise, the value cannot be a float, so raise an error
		if (!cn.getAllowDecimal() && Math.ceil(obs.getValueNumeric()) != obs.getValueNumeric()) {
			rejectValueOrGroupMember(errors, atRootNode, "valueNumeric", "Obs.error.precision");
		}
	}

	/**
	 * Single dispatch point for deriving an Obs' interpretation. Routes to the interpretation strategy
	 * appropriate for the concept's datatype so that {@link #validateHelper} does not need to know how
	 * each datatype derives its interpretation:
	 * <ul>
	 * <li>numeric observations are validated against their reference range, which also derives their
	 * numeric interpretation (see {@link #validateConceptReferenceRange})</li>
	 * <li>coded observations have their interpretation resolved from the value coded answer's concept
	 * mappings (see {@link #getConceptInterpretation})</li>
	 * </ul>
	 *
	 * @param obs Observation to validate/interpret
	 * @param errors Errors to record validation issues
	 * @param atRootNode whether or not this is the obs that validate() was originally called on
	 */
	private void validateAndInterpret(Obs obs, Errors errors, boolean atRootNode) {
		ConceptDatatype dt = obs.getConcept().getDatatype();

		if (dt.isNumeric()) {
			validateConceptReferenceRange(obs, errors, atRootNode);
			return;
		}

		if (dt.isCoded()) {
			interpretCodedObs(obs);
		}
	}

	/**
	 * Resolves and sets the interpretation of a new coded Obs. Only applies to new observations (i.e.
	 * not yet persisted); edited or copied observations (see {@link Obs#newInstance(Obs)}) already
	 * carry over whatever interpretation they had, so they are left untouched here.
	 *
	 * @param obs Observation whose interpretation should be resolved
	 */
	private void interpretCodedObs(Obs obs) {
		if (obs.getId() != null) {
			return;
		}

		Obs.Interpretation interpretation = getConceptInterpretation(obs);
		if (interpretation != null) {
			obs.setInterpretation(interpretation);
		}
	}

	/**
	 * Resolves the {@link Obs.Interpretation} for a coded Obs by looking for a concept mapping on the
	 * value coded answer whose reference term code matches one of the {@link Obs.Interpretation}
	 * constants, e.g. a "Positive" answer concept mapped to a reference term coded "ABNORMAL".
	 *
	 * @param obs Observation whose value coded answer should be resolved to an interpretation
	 * @return the resolved Interpretation, or null if no mapping matches
	 */
	private Obs.Interpretation getConceptInterpretation(Obs obs) {
		Concept valueCoded = obs.getValueCoded();
		if (valueCoded == null) {
			return null;
		}

		for (ConceptMap conceptMap : valueCoded.getConceptMappings()) {
			ConceptReferenceTerm term = conceptMap.getConceptReferenceTerm();
			if (term == null || StringUtils.isBlank(term.getCode())) {
				continue;
			}

			try {
				return Obs.Interpretation.valueOf(term.getCode().toUpperCase());
			} catch (IllegalArgumentException e) {
				// reference term code does not match a known interpretation; keep looking
			}
		}

		return null;
	}

	/**
	 * This method validates Obs' numeric values:
	 * <ol>
	 * <li>Validates Obs in relation to criteria e.g. checks patient's age is within the valid
	 * range</li>
	 * <li>Validates if Obs' numeric value is within the valid range; i.e. >= low absolute && <= high
	 * absolute.</li>
	 * <li>Sets field errors if numeric value is outside the valid range</li>
	 * <ol/>
	 *
	 * @param obs Observation to validate
	 * @param errors Errors to record validation issues
	 */
	private void validateConceptReferenceRange(Obs obs, Errors errors, boolean atRootNode) {
		ConceptReferenceRange conceptReferenceRange = getReferenceRange(obs);

		if (conceptReferenceRange != null) {
			validateAbsoluteRanges(obs, conceptReferenceRange, errors, atRootNode);

			if (obs.getId() == null) {
				setObsReferenceRange(obs, conceptReferenceRange);
			}
		} else if (obs.getId() == null) {
			setObsReferenceRange(obs);
		}
		setObsInterpretation(obs);
	}

	/**
	 * Evaluates the criteria and return the most strict {@link ConceptReferenceRange} for a given
	 * concept and patient contained in an observation. It considers all valid ranges that match the
	 * criteria for the person.
	 *
	 * @param obs containing The concept and patient for whom the range is being evaluated
	 * @return The strictest {@link ConceptReferenceRange}, or null if no valid range is found
	 * @since 2.7.0
	 */
	public ConceptReferenceRange getReferenceRange(Obs obs) {
		if (obs == null || obs.getPerson() == null || obs.getConcept() == null) {
			return null;
		}
		return Context.getConceptService().getConceptReferenceRange(new ConceptReferenceRangeContext(obs));
	}

	/**
	 * Validates the high and low absolute values of the Obs.
	 *
	 * @param obs Observation to validate
	 * @param conceptReferenceRange ConceptReferenceRange containing the range values
	 * @param errors Errors to record validation issues
	 */
	private void validateAbsoluteRanges(Obs obs, ConceptReferenceRange conceptReferenceRange, Errors errors,
	        boolean atRootNode) {
		if (conceptReferenceRange.getHiAbsolute() != null && conceptReferenceRange.getHiAbsolute() < obs.getValueNumeric()) {
			if (atRootNode) {
				errors.rejectValue("valueNumeric", "error.value.outOfRange.high",
				    new Object[] { conceptReferenceRange.getHiAbsolute() }, null);
			} else {
				errors.rejectValue(GROUP_MEMBERS_FIELD, "Obs.error.inGroupMember", new Object[] {}, null);
			}
		}

		if (conceptReferenceRange.getLowAbsolute() != null
		        && conceptReferenceRange.getLowAbsolute() > obs.getValueNumeric()) {
			if (atRootNode) {
				errors.rejectValue("valueNumeric", "error.value.outOfRange.low",
				    new Object[] { conceptReferenceRange.getLowAbsolute() }, null);
			} else {
				errors.rejectValue(GROUP_MEMBERS_FIELD, "Obs.error.inGroupMember", new Object[] {}, null);
			}
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

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

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.annotation.Handler;
import org.openmrs.api.context.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;

/**
 * This class validates a Patient object.
 */
@Handler(supports = { Patient.class }, order = 25)
public class PatientValidator extends PersonValidator {

	private static final Logger log = LoggerFactory.getLogger(PersonNameValidator.class);

	@Autowired
	private PatientIdentifierValidator patientIdentifierValidator;

	/**
	 * Returns whether or not this validator supports validating a given class.
	 *
	 * @param c The class to check for support.
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	@Override
	public boolean supports(Class<?> c) {
		log.debug("{}.supports: {}", this.getClass().getName(), c.getName());
		return Patient.class.isAssignableFrom(c);
	}

	/**
	 * Validates the given Patient. Currently just checks for errors in identifiers. TODO: Check for
	 * errors in all Patient fields.
	 * <p>
	 * <strong>Should</strong> fail validation if gender is blank<br/>
	 * <strong>Should</strong> fail validation if birthdate makes patient older than 140 years old<br/>
	 * <strong>Should</strong> fail validation if birthdate is a future date<br/>
	 * <strong>Should</strong> fail validation if a preferred patient identifier is not chosen<br/>
	 * <strong>Should</strong> fail validation if voidReason is blank when patient is voided<br/>
	 * <strong>Should</strong> fail validation if causeOfDeath is blank when patient is dead<br/>
	 * <strong>Should</strong> fail validation if a preferred patient identifier is not chosen for
	 * voided patients<br/>
	 * <strong>Should</strong> not fail when patient has only one identifier and its not preferred<br/>
	 * <strong>Should</strong> pass validation if field lengths are correct<br/>
	 * <strong>Should</strong> fail validation if field lengths are not correct<br/>
	 * <strong>Should</strong> fail validation if a required patient identifier is missing<br/>
	 * <strong>Should</strong> pass validation if a required patient identifier is present<br/>
	 * <strong>Should</strong> fail validation if the only identifier for a required type is voided<br/>
	 * <strong>Should</strong> pass validation if a voided required identifier is replaced by an active
	 * one<br/>
	 * <strong>Should</strong> ignore retired identifier types when checking required identifiers<br/>
	 * <strong>Should</strong> not fail validation for voided patients missing required identifiers
	 *
	 * @param obj The patient to validate.
	 * @param errors Errors
	 * @see org.springframework.validation.Validator#validate(java.lang.Object,
	 *      org.springframework.validation.Errors)
	 */
	@Override
	public void validate(Object obj, Errors errors) {
		log.debug("{}.validate...", this.getClass().getName());

		if (obj == null) {
			return;
		}

		super.validate(obj, errors);

		Patient patient = (Patient) obj;

		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "gender", "Person.gender.required");

		validatePreferredIdentifier(patient, errors);

		if (!Boolean.TRUE.equals(patient.getVoided())) {
			validateRequiredIdentifiers(patient, errors);
		}

		validatePatientIdentifiers(patient, errors);

		ValidateUtil.validateFieldLengths(errors, obj.getClass(), "voidReason");
	}

	private void validatePreferredIdentifier(Patient patient, Errors errors) {
		boolean preferredIdentifierChosen = false;

		Collection<PatientIdentifier> identifiers = Boolean.TRUE.equals(patient.getVoided()) ? patient.getIdentifiers()
		        : patient.getActiveIdentifiers();

		for (PatientIdentifier pi : identifiers) {
			if (Boolean.TRUE.equals(pi.getPreferred())) {
				preferredIdentifierChosen = true;
			}
		}

		if (!preferredIdentifierChosen && identifiers.size() != 1) {
			errors.reject("error.preferredIdentifier");
		}
	}

	private void validateRequiredIdentifiers(Patient patient, Errors errors) {
		Collection<PatientIdentifierType> identifierTypes = Context.getPatientService().getAllPatientIdentifierTypes(false);

		Set<PatientIdentifierType> requiredTypes = new HashSet<>();

		for (PatientIdentifierType type : identifierTypes) {
			if (Boolean.TRUE.equals(type.getRequired())) {
				requiredTypes.add(type);
			}
		}

		for (PatientIdentifier pi : patient.getActiveIdentifiers()) {
			if (pi.getIdentifierType() != null) {
				requiredTypes.remove(pi.getIdentifierType());
			}
		}

		if (!requiredTypes.isEmpty()) {
			List<String> missingRequiredIdentifiers = requiredTypes.stream().map(PatientIdentifierType::getName).toList();

			errors.rejectValue("identifiers", "Patient.missingRequiredIdentifier",
			    new Object[] { String.join(", ", missingRequiredIdentifiers) }, null);
		}
	}

	private void validatePatientIdentifiers(Patient patient, Errors errors) {
		if (errors.hasErrors() || patient.getIdentifiers() == null) {
			return;
		}

		int index = 0;

		for (PatientIdentifier identifier : patient.getIdentifiers()) {
			errors.pushNestedPath("identifiers[" + index + "]");
			patientIdentifierValidator.validate(identifier, errors);
			errors.popNestedPath();
			index++;
		}
	}
}

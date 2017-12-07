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

import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.annotation.Handler;
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
		if (log.isDebugEnabled()) {
			log.debug(this.getClass().getName() + ".supports: " + c.getName());
		}
		return Patient.class.isAssignableFrom(c);
	}
	
	/**
	 * Validates the given Patient. Currently just checks for errors in identifiers. TODO: Check for
	 * errors in all Patient fields.
	 * 
	 * @param obj The patient to validate.
	 * @param errors Errors
	 * @see org.springframework.validation.Validator#validate(java.lang.Object,
	 *      org.springframework.validation.Errors)
	 * @should fail validation if gender is blank
	 * @should fail validation if birthdate makes patient older that 120 years old
	 * @should fail validation if birthdate is a future date
	 * @should fail validation if a preferred patient identifier is not chosen
	 * @should fail validation if voidReason is blank when patient is voided
	 * @should fail validation if causeOfDeath is blank when patient is dead
	 * @should fail validation if a preferred patient identifier is not chosen for voided patients
	 * @should not fail when patient has only one identifier and its not preferred
	 * @should pass validation if field lengths are correct
	 * @should fail validation if field lengths are not correct
	 */
	@Override
	public void validate(Object obj, Errors errors) {
		if (log.isDebugEnabled()) {
			log.debug(this.getClass().getName() + ".validate...");
		}
		
		if (obj == null) {
			return;
		}
		
		super.validate(obj, errors);
		
		Patient patient = (Patient) obj;
		
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "gender", "Person.gender.required");
		
		// Make sure they chose a preferred ID
		Boolean preferredIdentifierChosen = false;
		//Voided patients have only voided identifiers since they were voided with the patient, 
		//so get all otherwise get the active ones
		Collection<PatientIdentifier> identifiers = patient.getVoided() ? patient.getIdentifiers() : patient
		        .getActiveIdentifiers();
		for (PatientIdentifier pi : identifiers) {
			if (pi.getPreferred()) {
				preferredIdentifierChosen = true;
			}
		}
		if (!preferredIdentifierChosen && identifiers.size() != 1) {
			errors.reject("error.preferredIdentifier");
		}
		int index = 0;
		if (!errors.hasErrors() && patient.getIdentifiers() != null) {
			// Validate PatientIdentifers
			for (PatientIdentifier identifier : patient.getIdentifiers()) {
				errors.pushNestedPath("identifiers[" + index + "]");
				patientIdentifierValidator.validate(identifier, errors);
				errors.popNestedPath();
				index++;
			}
		}
		ValidateUtil.validateFieldLengths(errors, obj.getClass(), "voidReason");
	}
}

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

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.springframework.validation.ValidationUtils;
import org.openmrs.annotation.Handler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;

/**
 * This class validates a Patient object.
 */
@Handler(supports = { Patient.class }, order = 50)
public class PatientValidator extends PersonValidator {
	
	private static Log log = LogFactory.getLog(PersonNameValidator.class);
	
	@Autowired
	private PatientIdentifierValidator patientIdentifierValidator;
	
	/**
	 * Returns whether or not this validator supports validating a given class.
	 *
	 * @param c The class to check for support.
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	@SuppressWarnings("rawtypes")
	public boolean supports(Class c) {
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
	 */
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
		Collection<PatientIdentifier> identifiers = patient.isVoided() ? patient.getIdentifiers() : patient
		        .getActiveIdentifiers();
		for (PatientIdentifier pi : identifiers) {
			if (pi.isPreferred()) {
				preferredIdentifierChosen = true;
			}
		}
		if (!preferredIdentifierChosen && identifiers.size() != 1) {
			errors.reject("error.preferredIdentifier");
		}
		
		if (!errors.hasErrors()) {
			// Validate PatientIdentifers
			if (patient.getIdentifiers() != null) {
				for (PatientIdentifier identifier : patient.getIdentifiers()) {
					patientIdentifierValidator.validate(identifier, errors);
				}
			}
		}
	}
}

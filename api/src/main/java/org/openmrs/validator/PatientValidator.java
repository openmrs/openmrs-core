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

import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PersonName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

/**
 * This class validates a Patient object. 
 */
public class PatientValidator implements Validator {
	
	private static Log log = LogFactory.getLog(PersonNameValidator.class);

	@Autowired
	PersonNameValidator personNameValidator;

	@Autowired
	PatientIdentifierValidator patientIdentifierValidator;
	
	/**
	 * Returns whether or not this validator supports validating a given class.
	 * 
	 * @param c The class to check for support.
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	public boolean supports(Class c) {
		if(log.isDebugEnabled())
			log.debug(this.getClass().getName() + ".supports: " + c.getName());
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
	 * @should fail validation if voidReason is blank when patient is voided
	 * @should fail validation if causeOfDeath is blank when patient is dead
	 */
	public void validate(Object obj, Errors errors) {
		if(log.isDebugEnabled())
			log.debug(this.getClass().getName()+ ".validate..." );
		
		Patient patient = (Patient) obj;
		
		if (patient != null) {
			for (PersonName personName : patient.getNames()) {
				personNameValidator.validate(personName, errors);
			}
		}

		// Make sure they choose a gender
		if (StringUtils.isBlank(patient.getGender())) errors.rejectValue("gender", "Person.gender.required");

		// check patients birthdate against future dates and really old dates
		if (patient.getBirthdate() != null) {
			if (patient.getBirthdate().after(new Date()))
				errors.rejectValue("birthdate", "error.date.future");
			else {
				Calendar c = Calendar.getInstance();
				c.setTime(new Date());
				c.add(Calendar.YEAR, -120); // patient cannot be older than 120 years old 
				if (patient.getBirthdate().before(c.getTime())) {
					errors.rejectValue("birthdate", "error.date.nonsensical");
				}
			}
		}
		
		//	 Patient Info 
		if (patient.isPersonVoided())
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "voidReason", "error.null");
		if (patient.isDead() && (patient.getCauseOfDeath() == null))
			errors.rejectValue("causeOfDeath", "Patient.dead.causeOfDeathNull");

		if (!errors.hasErrors()) {
			// Validate PatientIdentifers
			if (patient != null && patient.getIdentifiers() != null) {
				for (PatientIdentifier identifier : patient.getIdentifiers()) {
					patientIdentifierValidator.validate(identifier, errors);
				}
			}
		}
	}
}

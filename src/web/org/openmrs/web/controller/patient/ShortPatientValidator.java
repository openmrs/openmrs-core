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
package org.openmrs.web.controller.patient;

import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.validator.PersonNameValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

/**
 * This class validates a Short Patient Model object in the {@link NewPatientFormController}
 */
public class ShortPatientValidator implements Validator {
	
	private static Log log = LogFactory.getLog(PersonNameValidator.class);

	@Autowired
	private PersonNameValidator personNameValidator;

	/**
	 * Returns whether or not this validator supports validating a given class.
	 * 
	 * @param c The class to check for support.
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	public boolean supports(Class c) {
		log.error(this.getClass().getName() + ".supports: " + c.getName());
		return ShortPatientModel.class.isAssignableFrom(c);
	}
	
	/**
	 * Validates the given Patient.
	 * 
	 * @param obj The patient to validate.
	 * @param errors The patient to validate.
	 * @see org.springframework.validation.Validator#validate(java.lang.Object,
	 *      org.springframework.validation.Errors)
	 * @should fail validation if gender is blank
	 * @should fail validation if birthdate is blank
	 * @should fail validation if birthdate makes patient older that 120 years old
	 * @should fail validation if birthdate is a future date
	 * @should fail validation if voidReason is blank when patient is voided
	 * @should fail validation if causeOfDeath is blank when patient is dead
	 */
	public void validate(Object obj, Errors errors) {
		
		ShortPatientModel shortPatientModel = (ShortPatientModel) obj;
		
		personNameValidator.validatePersonName(shortPatientModel.getName(), errors, false, false);

		// Make sure they choose a gender
		if (StringUtils.isBlank(shortPatientModel.getGender())) errors.rejectValue("gender", "Person.gender.required");

		// check patients birthdate against future dates and really old dates
		if (shortPatientModel.getBirthdate() != null) {
			if (shortPatientModel.getBirthdate().after(new Date()))
				errors.rejectValue("birthdate", "error.date.future");
			else {
				Calendar c = Calendar.getInstance();
				c.setTime(new Date());
				c.add(Calendar.YEAR, -120); // patient cannot be older than 120 years old 
				if (shortPatientModel.getBirthdate().before(c.getTime())) {
					errors.rejectValue("birthdate", "error.date.nonsensical");
				}
			}
		} else {
			errors.rejectValue("birthdate", "error.required", new Object[]{"Birthdate"}, "");
		}
		
		//	 Patient Info 
		if (shortPatientModel.getVoided())
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "voidReason", "error.null");
		if (shortPatientModel.isDead() && (shortPatientModel.getCauseOfDeath() == null))
			errors.rejectValue("causeOfDeath", "Patient.dead.causeOfDeathNull");

	}
}

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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.PatientIdentifier;
import org.openmrs.PersonName;
import org.openmrs.api.context.Context;
import org.openmrs.validator.PatientIdentifierValidator;
import org.openmrs.validator.PersonAddressValidator;
import org.openmrs.validator.PersonNameValidator;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

/**
 * This class validates a Short Patient Model object in the {@link NewPatientFormController}
 */
public class ShortPatientFormValidator implements Validator {
	
	private static Log log = LogFactory.getLog(PersonNameValidator.class);
	
	/**
	 * Returns whether or not this validator supports validating a given class.
	 * 
	 * @param c The class to check for support.
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	public boolean supports(Class c) {
		return ShortPatientModel.class.isAssignableFrom(c);
	}
	
	/**
	 * Validates the given Patient.
	 * 
	 * @param obj The patient to validate.
	 * @param errors The patient to validate.
	 * @see org.springframework.validation.Validator#validate(java.lang.Object,
	 *      org.springframework.validation.Errors)
	 * @should pass if the minimum required fields are provided and are valid
	 * @should fail validation if gender is blank
	 * @should fail validation if birthdate is blank
	 * @should fail validation if birthdate makes patient 120 years old or older
	 * @should fail validation if birthdate is a future date
	 * @should fail validation if causeOfDeath is blank when patient is dead
	 * @should fail if all name fields are empty or white space characters
	 * @should fail if no identifiers are added
	 * @should fail if all identifiers have been voided
	 * @should fail if any name has more than 50 characters
	 * @should fail validation if deathdate is a future date
	 * @should fail if the deathdate is before the birthdate incase the patient is dead
	 */
	public void validate(Object obj, Errors errors) {
		if (log.isDebugEnabled())
			log.debug(this.getClass().getName() + ": Validating patient data from the short patient form....");
		
		ShortPatientModel shortPatientModel = (ShortPatientModel) obj;
		PersonName personName = shortPatientModel.getPersonName();
		
		Errors nameErrors = new BindException(personName, "personName");
		new PersonNameValidator().validatePersonName(personName, nameErrors, false, true);
		
		if (nameErrors.hasErrors()) {
			// pick all the personName errors and bind them to the formObject
			Iterator<ObjectError> it = nameErrors.getAllErrors().iterator();
			Set<String> errorCodesWithNoArguments = new HashSet<String>();
			while (it.hasNext()) {
				ObjectError error = it.next();
				// donot show similar error message multiple times in the view
				// unless they
				// take in arguments which will make them atleast different
				if (error.getCode() != null
				        && (!errorCodesWithNoArguments.contains(error.getCode()) || (error.getArguments() != null && error
				                .getArguments().length > 0))) {
					errors.reject(error.getCode(), error.getArguments(), "");
					if (error.getArguments() == null || error.getArguments().length == 0)
						errorCodesWithNoArguments.add(error.getCode());
				}
			}
			// drop the collection
			errorCodesWithNoArguments = null;
		}
		
		if (CollectionUtils.isEmpty(shortPatientModel.getIdentifiers()))
			errors.reject("PatientIdentifier.error.insufficientIdentifiers");
		else {
			boolean nonVoidedIdentifierFound = false;
			for (PatientIdentifier pId : shortPatientModel.getIdentifiers()) {
				if (!pId.isVoided())
					nonVoidedIdentifierFound = true;
				
				new PatientIdentifierValidator().validate(pId, errors);
			}
			// if all the names are voided
			if (!nonVoidedIdentifierFound)
				errors.reject("PatientIdentifier.error.insufficientIdentifiers");
			
		}
		
		// Make sure they chose a gender
		if (StringUtils.isBlank(shortPatientModel.getPatient().getGender()))
			errors.rejectValue("patient.gender", "Person.gender.required");
		
		// check patients birthdate against future dates and really old dates
		if (shortPatientModel.getPatient().getBirthdate() != null) {
			if (shortPatientModel.getPatient().getBirthdate().after(new Date()))
				errors.rejectValue("patient.birthdate", "error.date.future");
			else {
				Calendar c = Calendar.getInstance();
				c.setTime(new Date());
				c.add(Calendar.YEAR, -120); // patient cannot be older than 120
				// years old
				if (shortPatientModel.getPatient().getBirthdate().before(c.getTime())) {
					errors.rejectValue("patient.birthdate", "error.date.nonsensical");
				}
			}
		} else {
			errors.rejectValue("patient.birthdate", "error.required", new Object[] { Context.getMessageSourceService()
			        .getMessage("Person.birthdate") }, "");
		}
		
		//validate the personAddress
		if (shortPatientModel.getPersonAddress() != null) {
			try {
				errors.pushNestedPath("personAddress");
				ValidationUtils.invokeValidator(new PersonAddressValidator(), shortPatientModel.getPersonAddress(), errors);
			}
			finally {
				errors.popNestedPath();
			}
		}
		
		if (shortPatientModel.getPatient().getDead()) {
			if (shortPatientModel.getPatient().getCauseOfDeath() == null)
				errors.rejectValue("patient.causeOfDeath", "Person.dead.causeOfDeathNull");
			
			if (shortPatientModel.getPatient().getDeathDate() != null) {
				if (shortPatientModel.getPatient().getDeathDate().after(new Date()))
					errors.rejectValue("patient.deathDate", "error.date.future");
				// death date has to be after birthdate if both are specified
				if (shortPatientModel.getPatient().getBirthdate() != null
				        && shortPatientModel.getPatient().getDeathDate().before(
				            shortPatientModel.getPatient().getBirthdate()))
					errors.rejectValue("patient.deathDate", "error.deathdate.before.birthdate");
			}
		}
	}
}

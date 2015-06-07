/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
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
import org.openmrs.PersonAddress;
import org.openmrs.PersonName;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsUtil;
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
	 * @should reject a duplicate name
	 * @should reject a duplicate address
	 */
	public void validate(Object obj, Errors errors) {
		if (log.isDebugEnabled()) {
			log.debug(this.getClass().getName() + ": Validating patient data from the short patient form....");
		}
		
		ShortPatientModel shortPatientModel = (ShortPatientModel) obj;
		PersonName personName = shortPatientModel.getPersonName();
		
		//TODO We should be able to let developers and implementations to specify which person name 
		// fields should be used to determine uniqueness
		
		//check if this name has a unique givenName, middleName and familyName combination
		for (PersonName possibleDuplicate : shortPatientModel.getPatient().getNames()) {
			//don't compare the name to itself
			if (OpenmrsUtil.nullSafeEquals(possibleDuplicate.getId(), personName.getId())) {
				continue;
			}
			
			if (OpenmrsUtil.nullSafeEqualsIgnoreCase(possibleDuplicate.getGivenName(), personName.getGivenName())
			        && OpenmrsUtil.nullSafeEqualsIgnoreCase(possibleDuplicate.getMiddleName(), personName.getMiddleName())
			        && OpenmrsUtil.nullSafeEqualsIgnoreCase(possibleDuplicate.getFamilyName(), personName.getFamilyName())) {
				errors.reject("Patient.duplicateName", new Object[] { personName.getFullName() }, personName.getFullName()
				        + " is a duplicate name for the same patient");
			}
		}
		
		Errors nameErrors = new BindException(personName, "personName");
		new PersonNameValidator().validatePersonName(personName, nameErrors, false, true);
		
		if (nameErrors.hasErrors()) {
			// pick all the personName errors and bind them to the formObject
			Iterator<ObjectError> it = nameErrors.getAllErrors().iterator();
			Set<String> errorCodesWithNoArguments = new HashSet<String>();
			while (it.hasNext()) {
				ObjectError error = it.next();
				// don't show similar error message multiple times in the view
				// unless they take in arguments which will make them atleast different
				if (error.getCode() != null
				        && (!errorCodesWithNoArguments.contains(error.getCode()) || (error.getArguments() != null && error
				                .getArguments().length > 0))) {
					errors.reject(error.getCode(), error.getArguments(), "");
					if (error.getArguments() == null || error.getArguments().length == 0) {
						errorCodesWithNoArguments.add(error.getCode());
					}
				}
			}
			// drop the collection
			errorCodesWithNoArguments = null;
		}
		
		//TODO We should be able to let developers and implementations to specify which
		// person address fields should be used to determine uniqueness
		
		//check if this address is unique
		PersonAddress personAddress = shortPatientModel.getPersonAddress();
		for (PersonAddress possibleDuplicate : shortPatientModel.getPatient().getAddresses()) {
			//don't compare the address to itself
			if (OpenmrsUtil.nullSafeEquals(possibleDuplicate.getId(), personAddress.getId())) {
				continue;
			}
			
			if (!possibleDuplicate.isBlank() && !personAddress.isBlank()
			        && possibleDuplicate.toString().equalsIgnoreCase(personAddress.toString())) {
				errors.reject("Patient.duplicateAddress", new Object[] { personAddress.toString() }, personAddress
				        .toString()
				        + " is a duplicate address for the same patient");
			}
		}
		
		//check if all required addres fields are filled
		errors.pushNestedPath("personAddress");
		new PersonAddressValidator().validate(personAddress, errors);
		errors.popNestedPath();
		if (errors.hasErrors()) {
			return;
		}
		
		int index = 0;
		if (CollectionUtils.isEmpty(shortPatientModel.getIdentifiers())) {
			errors.reject("PatientIdentifier.error.insufficientIdentifiers");
		} else {
			boolean nonVoidedIdentifierFound = false;
			for (PatientIdentifier pId : shortPatientModel.getIdentifiers()) {
				//no need to validate unsaved identifiers that have been removed
				if (pId.getPatientIdentifierId() == null && pId.isVoided()) {
					continue;
				}
				
				if (!pId.isVoided()) {
					nonVoidedIdentifierFound = true;
				}
				errors.pushNestedPath("identifiers[" + index + "]");
				new PatientIdentifierValidator().validate(pId, errors);
				errors.popNestedPath();
				index++;
			}
			// if all the names are voided
			if (!nonVoidedIdentifierFound) {
				errors.reject("PatientIdentifier.error.insufficientIdentifiers");
			}
			
		}
		
		// Make sure they chose a gender
		if (StringUtils.isBlank(shortPatientModel.getPatient().getGender())) {
			errors.rejectValue("patient.gender", "Person.gender.required");
		}
		
		// check patients birthdate against future dates and really old dates
		if (shortPatientModel.getPatient().getBirthdate() != null) {
			if (shortPatientModel.getPatient().getBirthdate().after(new Date())) {
				errors.rejectValue("patient.birthdate", "error.date.future");
			} else {
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
			if (shortPatientModel.getPatient().getCauseOfDeath() == null) {
				errors.rejectValue("patient.causeOfDeath", "Person.dead.causeOfDeathNull");
			}
			
			if (shortPatientModel.getPatient().getDeathDate() != null) {
				if (shortPatientModel.getPatient().getDeathDate().after(new Date())) {
					errors.rejectValue("patient.deathDate", "error.date.future");
				}
				// death date has to be after birthdate if both are specified
				if (shortPatientModel.getPatient().getBirthdate() != null
				        && shortPatientModel.getPatient().getDeathDate().before(
				            shortPatientModel.getPatient().getBirthdate())) {
					errors.rejectValue("patient.deathDate", "error.deathdate.before.birthdate");
				}
			}
		}
	}
}

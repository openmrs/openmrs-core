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

import org.apache.log4j.Logger;
import org.openmrs.Person;
import org.openmrs.PersonAddress;
import org.openmrs.PersonName;
import org.openmrs.annotation.Handler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

/**
 * This class validates a Person object.
 *
 * @since 1.9
 */
@Handler(supports = { Person.class }, order = 50)
public class PersonValidator implements Validator {
	
	private Logger log = Logger.getLogger(PersonValidator.class);
	
	@Autowired
	private PersonNameValidator personNameValidator;
	
	@Autowired
	private PersonAddressValidator personAddressValidator;
	
	@Override
	public boolean supports(Class<?> clazz) {
		return Person.class.isAssignableFrom(clazz);
	}
	
	/**
	 * @see org.springframework.validation.Validator#validate(java.lang.Object,
	 *      org.springframework.validation.Errors)
	 * @should fail validation if birthdate makes patient older that 120 years old
	 * @should fail validation if birthdate is a future date
	 * @should fail validation if voidReason is blank when patient is voided
	 * @should fail validation if causeOfDeath is blank when patient is dead
	 * @should pass validation if gender is blank for Persons
	 */
	@Override
	public void validate(Object target, Errors errors) {
		if (log.isDebugEnabled()) {
			log.debug(this.getClass().getName() + ".validate...");
		}
		
		if (target == null) {
			return;
		}
		
		Person person = (Person) target;
		
		boolean atLeastOneNonVoidPersonNameLeft = false;
		for (PersonName personName : person.getNames()) {
			personNameValidator.validate(personName, errors);
			if (!personName.isVoided()) {
				atLeastOneNonVoidPersonNameLeft = true;
			}
		}
		if (!person.isVoided() && !atLeastOneNonVoidPersonNameLeft) {
			errors.rejectValue("names", "Person.shouldHaveAtleastOneNonVoidedName");
		}
		
		//validate the personAddress
		int index = 0;
		for (PersonAddress address : person.getAddresses()) {
			try {
				errors.pushNestedPath("addresses[" + index + "]");
				ValidationUtils.invokeValidator(personAddressValidator, address, errors);
			}
			finally {
				errors.popNestedPath();
				index++;
			}
		}
		
		validateBirthDate(errors, person.getBirthdate());
		
		if (person.isVoided()) {
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "voidReason", "error.null");
		}
		if (person.isDead()) {
			ValidationUtils.rejectIfEmpty(errors, "causeOfDeath", "Person.dead.causeOfDeathNull");
		}
		
		ValidateUtil.validateFieldLengths(errors, Person.class, "gender");
	}
	
	/**
	 * Checks if the birth date specified is in the future or older than 120 years old..
	 *
	 * @param birthDate The birthdate to validate.
	 * @param errors Stores information about errors encountered during validation.
	 */
	private void validateBirthDate(Errors errors, Date birthDate) {
		if (birthDate != null) {
			if (birthDate.after(new Date())) {
				errors.rejectValue("birthdate", "error.date.future");
			} else {
				Calendar c = Calendar.getInstance();
				c.setTime(new Date());
				c.add(Calendar.YEAR, -120);
				if (birthDate.before(c.getTime())) {
					errors.rejectValue("birthdate", "error.date.nonsensical");
				}
			}
		}
	}
	
}

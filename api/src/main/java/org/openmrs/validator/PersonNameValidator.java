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

import org.apache.commons.lang3.StringUtils;
import org.openmrs.PersonName;
import org.openmrs.annotation.Handler;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * This class validates a PersonName object.
 *
 * @since 1.7
 */
@Handler(supports = { PersonName.class }, order = 50)
public class PersonNameValidator implements Validator {

	private static final Logger log = LoggerFactory.getLogger(PersonNameValidator.class);

	/**
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	@Override
	public boolean supports(Class<?> c) {
		return PersonName.class.isAssignableFrom(c);
	}

	/**
	 * Checks whether person name has all required values, and whether values are proper length
	 * <p>
	 * <strong>Should</strong> fail validation if PersonName object is null<br/>
	 * <strong>Should</strong> pass validation if name is invalid but voided<br/>
	 * <strong>Should</strong> pass validation if field lengths are correct<br/>
	 * <strong>Should</strong> fail validation if field lengths are not correct
	 *
	 * @param object
	 * @param errors
	 */
	@Override
	public void validate(Object object, Errors errors) {
		log.debug("{}.validate...", this.getClass().getName());
		PersonName personName = (PersonName) object;
		try {
			if (personName == null) {
				errors.reject("error.name");
				return;
			}
			if (!personName.getVoided()) {
				validatePersonNameInternal(personName, errors);
			}
		} catch (Exception e) {
			errors.reject(e.getMessage());
		}
	}

	private void validatePersonNameInternal(PersonName personName, Errors errors) {
		// Make sure they assign a name
		if (StringUtils.isBlank(personName.getGivenName())
		        || StringUtils.isBlank(personName.getGivenName().replaceAll("\"", ""))) {
			errors.rejectValue("givenName", "Patient.names.required.given.family");
		}

		// Make sure the entered name value is sensible
		String namePattern = Context.getAdministrationService()
		        .getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_NAME_REGEX);
		if (StringUtils.isNotBlank(namePattern)) {
			if (StringUtils.isNotBlank(personName.getGivenName()) && !personName.getGivenName().matches(namePattern)) {
				errors.rejectValue("givenName", "GivenName.invalid");
			}
			if (StringUtils.isNotBlank(personName.getMiddleName()) && !personName.getMiddleName().matches(namePattern)) {
				errors.rejectValue("middleName", "MiddleName.invalid");
			}
			if (StringUtils.isNotBlank(personName.getFamilyName()) && !personName.getFamilyName().matches(namePattern)) {
				errors.rejectValue("familyName", "FamilyName.invalid");
			}
			if (StringUtils.isNotBlank(personName.getFamilyName2()) && !personName.getFamilyName2().matches(namePattern)) {
				errors.rejectValue("familyName2", "FamilyName2.invalid");
			}
		}
		ValidateUtil.validateFieldLengths(errors, personName.getClass(), "prefix", "givenName", "middleName",
		    "familyNamePrefix", "familyName", "familyName2", "familyNameSuffix", "degree", "voidReason");
	}
}

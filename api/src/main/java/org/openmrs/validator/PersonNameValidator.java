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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.PersonName;
import org.openmrs.annotation.Handler;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsConstants;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * This class validates a PersonName object.
 *
 * @since 1.7
 */
@Handler(supports = { PersonName.class }, order = 50)
public class PersonNameValidator implements Validator {
	
	private static Log log = LogFactory.getLog(PersonNameValidator.class);
	
	/**
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	public boolean supports(Class c) {
		return PersonName.class.isAssignableFrom(c);
	}
	
	/**
	 * Checks whether person name has all required values, and whether values are proper length
	 *
	 * @param personName
	 * @param errors
	 * @should fail validation if PersonName object is null
	 * @should pass validation if name is invalid but voided
	 * @should pass validation if field lengths are correct
	 * @should fail validation if field lengths are not correct
	 */
	public void validate(Object object, Errors errors) {
		if (log.isDebugEnabled()) {
			log.debug(this.getClass().getName() + ".validate...");
		}
		PersonName personName = (PersonName) object;
		try {
			// Validate that the person name object is not null
			if (personName == null) {
				errors.reject("error.name");
			} else if (!personName.isVoided()) {
				validatePersonName(personName, errors, true, false);
			}
		}
		catch (Exception e) {
			errors.reject(e.getMessage());
		}
	}
	
	/**
	 * Checks that the given {@link PersonName} is valid
	 *
	 * @param personName the {@link PersonName} to validate
	 * @param errors
	 * @param arrayInd indicates whether or not a names[0] array needs to be prepended to field
	 * @should fail validation if PersonName object is null
	 * @should fail validation if PersonName.givenName is null
	 * @should fail validation if PersonName.givenName is empty
	 * @should fail validation if PersonName.givenName is just spaces
	 * @should fail validation if PersonName.givenName is spaces surrounded by quotation marks
	 * @should pass validation if PersonName.givenName is not blank
	 * @should fail validation if PersonName.familyName is null
	 * @should fail validation if PersonName.familyName is empty
	 * @should fail validation if PersonName.familyName is just spaces
	 * @should fail validation if PersonName.familyName is spaces surrounded by quotation marks
	 * @should pass validation if PersonName.familyName is not blank
	 * @should fail validation if PersonName.prefix is too long
	 * @should pass validation if PersonName.prefix is exactly max length
	 * @should pass validation if PersonName.prefix is less than maximum field length
	 * @should fail validation if PersonName.givenName is too long
	 * @should pass validation if PersonName.givenName is exactly max length
	 * @should pass validation if PersonName.givenName is less than maximum field length
	 * @should fail validation if PersonName.middleName is too long
	 * @should pass validation if PersonName.middleName is exactly max length
	 * @should pass validation if PersonName.middleName is less than maximum field length
	 * @should fail validation if PersonName.familyNamePrefix is too long
	 * @should pass validation if PersonName.familyNamePrefix is exactly max length
	 * @should pass validation if PersonName.familyNamePrefix is less than maximum field length
	 * @should fail validation if PersonName.familyName is too long
	 * @should pass validation if PersonName.familyName is exactly max length
	 * @should pass validation if PersonName.familyName is less than maximum field length
	 * @should fail validation if PersonName.familyName2 is too long
	 * @should pass validation if PersonName.familyName2 is exactly max length
	 * @should pass validation if PersonName.familyName2 is less than maximum field length
	 * @should fail validation if PersonName.familyNameSuffix is too long
	 * @should pass validation if PersonName.familyNameSuffix is exactly max length
	 * @should pass validation if PersonName.familyNameSuffix is less than maximum field length
	 * @should fail validation if PersonName.degree is too long
	 * @should pass validation if PersonName.degree is exactly max length
	 * @should pass validation if PersonName.degree is less than maximum field length
	 * @should fail validation if PersonName.givenName is invalid
	 * @should pass validation if PersonName.givenName is valid
	 * @should fail validation if PersonName.middleName is invalid
	 * @should pass validation if PersonName.middleName is valid
	 * @should fail validation if PersonName.familyName is invalid
	 * @should pass validation if PersonName.familyName is valid
	 * @should fail validation if PersonName.familyName2 is invalid
	 * @should pass validation if PersonName.familyName2 is valid
	 * @should pass validation if regex string is null
	 * @should pass validation if regex string is empty
	 * @should not validate against regex for blank names
	 */
	public void validatePersonName(PersonName personName, Errors errors, boolean arrayInd, boolean testInd) {
		
		if (personName == null) {
			errors.reject("error.name");
			return;
		}
		// Make sure they assign a name
		if (StringUtils.isBlank(personName.getGivenName())
		        || StringUtils.isBlank(personName.getGivenName().replaceAll("\"", ""))) {
			errors.rejectValue(getFieldKey("givenName", arrayInd, testInd), "Patient.names.required.given.family");
		}
		if (StringUtils.isBlank(personName.getFamilyName())
		        || StringUtils.isBlank(personName.getFamilyName().replaceAll("\"", ""))) {
			errors.rejectValue(getFieldKey("familyName", arrayInd, testInd), "Patient.names.required.given.family");
		}
		// Make sure the entered name value is sensible 
		String namePattern = Context.getAdministrationService().getGlobalProperty(
		    OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_NAME_REGEX);
		if (StringUtils.isNotBlank(namePattern)) {
			if (StringUtils.isNotBlank(personName.getGivenName()) && !personName.getGivenName().matches(namePattern)) {
				errors.rejectValue(getFieldKey("givenName", arrayInd, testInd), "GivenName.invalid");
			}
			if (StringUtils.isNotBlank(personName.getMiddleName()) && !personName.getMiddleName().matches(namePattern)) {
				errors.rejectValue(getFieldKey("middleName", arrayInd, testInd), "MiddleName.invalid");
			}
			if (StringUtils.isNotBlank(personName.getFamilyName()) && !personName.getFamilyName().matches(namePattern)) {
				errors.rejectValue(getFieldKey("familyName", arrayInd, testInd), "FamilyName.invalid");
			}
			if (StringUtils.isNotBlank(personName.getFamilyName2()) && !personName.getFamilyName2().matches(namePattern)) {
				errors.rejectValue(getFieldKey("familyName2", arrayInd, testInd), "FamilyName2.invalid");
			}
		}
		ValidateUtil.validateFieldLengths(errors, personName.getClass(), "prefix", "givenName", "middleName",
		    "familyNamePrefix", "familyName", "familyName2", "familyNameSuffix", "degree", "voidReason");
	}
	
	/***********************************************************************************************************
	 * @param field the field name
	 * @param arrayInd indicates whether or not a names[0] array needs to be prepended to field
	 * @return formated
	 */
	private String getFieldKey(String field, boolean arrayInd, boolean testInd) {
		return testInd ? field : arrayInd ? "names[0]." + field : "name." + field;
	}
	
}

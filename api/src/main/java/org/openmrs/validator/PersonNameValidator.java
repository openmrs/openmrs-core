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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.PersonName;
import org.openmrs.annotation.Handler;
import org.openmrs.api.context.Context;
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
	@SuppressWarnings("unchecked")
	public boolean supports(Class c) {
		return PersonName.class.isAssignableFrom(c);
	}
	
	/**
	 * Checks whether person name has all required values, and whether values are proper length
	 * 
	 * @param personName
	 * @param errors
	 * @should fail validation if PersonName object is null
	 */
	public void validate(Object object, Errors errors) {
		if (log.isDebugEnabled())
			log.debug(this.getClass().getName() + ".validate...");
		PersonName personName = (PersonName) object;
		try {
			// Validate that the person name object is not null
			if (personName == null)
				errors.reject("error.name");
			if (!errors.hasErrors())
				validatePersonName(personName, errors, true, false);
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
	 */
	public void validatePersonName(PersonName personName, Errors errors, boolean arrayInd, boolean testInd) {
		
		if (personName == null)
			errors.reject("error.name");
		// Make sure they assign a name
		if (StringUtils.isBlank(personName.getGivenName())
		        || StringUtils.isBlank(personName.getGivenName().replaceAll("\"", "")))
			errors.rejectValue(getFieldKey("givenName", arrayInd, testInd), "Patient.names.required.given.family");
		if (StringUtils.isBlank(personName.getFamilyName())
		        || StringUtils.isBlank(personName.getFamilyName().replaceAll("\"", "")))
			errors.rejectValue(getFieldKey("familyName", arrayInd, testInd), "Patient.names.required.given.family");
		
		// Make sure the length does not exceed database column size
		if (StringUtils.length(personName.getPrefix()) > 50)
			rejectPersonName(errors, "prefix", arrayInd, testInd);
		if (StringUtils.length(personName.getGivenName()) > 50)
			rejectPersonName(errors, "givenName", arrayInd, testInd);
		if (StringUtils.length(personName.getMiddleName()) > 50)
			rejectPersonName(errors, "middleName", arrayInd, testInd);
		if (StringUtils.length(personName.getFamilyNamePrefix()) > 50)
			rejectPersonName(errors, "familyNamePrefix", arrayInd, testInd);
		if (StringUtils.length(personName.getFamilyName()) > 50)
			rejectPersonName(errors, "familyName", arrayInd, testInd);
		if (StringUtils.length(personName.getFamilyName2()) > 50)
			rejectPersonName(errors, "familyName2", arrayInd, testInd);
		if (StringUtils.length(personName.getFamilyNameSuffix()) > 50)
			rejectPersonName(errors, "familyNameSuffix", arrayInd, testInd);
		if (StringUtils.length(personName.getDegree()) > 50)
			rejectPersonName(errors, "degree", arrayInd, testInd);
		
	}
	
	private void rejectPersonName(Errors errors, String fieldKey, boolean arrayInd, boolean testInd) {
		errors.rejectValue(getFieldKey(fieldKey, arrayInd, testInd), "error.name.max.length", new Object[] {
		        getInternationizedFieldName("PersonName." + fieldKey), 50 }, "error.name");
	}
	
	/***********************************************************************************************************
	 * @param field the field name
	 * @param arrayInd indicates whether or not a names[0] array needs to be prepended to field
	 * @return formated
	 */
	private String getFieldKey(String field, boolean arrayInd, boolean testInd) {
		return testInd ? field : arrayInd ? "names[0]." + field : "name." + field;
	}
	
	private static String getInternationizedFieldName(String messageKey) {
		return Context.getMessageSourceService().getMessage(messageKey, null, Context.getLocale());
	}
	
}

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

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.api.BlankIdentifierException;
import org.openmrs.api.IdentifierNotUniqueException;
import org.openmrs.api.InvalidCheckDigitException;
import org.openmrs.api.InvalidIdentifierFormatException;
import org.openmrs.api.PatientIdentifierException;
import org.openmrs.api.context.Context;
import org.openmrs.patient.IdentifierValidator;
import org.openmrs.patient.UnallowedIdentifierException;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * This class validates a PatientIdentifier object.
 */
public class PatientIdentifierValidator implements Validator {
	
	private static Log log = LogFactory.getLog(PatientIdentifierValidator.class);
	
	/**
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	public boolean supports(Class c) {
		return PatientIdentifier.class.isAssignableFrom(c);
	}
	
	/**
	 * Validates a PatientIdentifier.
	 * 
	 * @see org.springframework.validation.Validator#validate(java.lang.Object,
	 *      org.springframework.validation.Errors)
	 */
	public void validate(Object obj, Errors errors) {
		PatientIdentifier pi = (PatientIdentifier) obj;
		try {
			validateIdentifier(pi);
		}
		catch (Exception e) {
			errors.reject(e.getMessage());
		}
	}
	
	/**
	 * Checks that the given {@link PatientIdentifier} is valid
	 * 
	 * @param pi - the {@link PatientIdentifier} to validate
	 * @throws PatientIdentifierException if the {@link PatientIdentifier} is invalid
	 * @should fail validation if PatientIdentifier is null
	 * @should pass validation if PatientIdentifier is voided
	 * @should fail validation if another patient has a matching identifier of the same type
	 * @see #validateIdentifier(String, PatientIdentifierType)
	 */
	public static void validateIdentifier(PatientIdentifier pi) throws PatientIdentifierException {
		
		// Validate that the identifier is non-null
		if (pi == null) {
			throw new BlankIdentifierException("Patient Identifier cannot be null.");
		}
		
		// Only validate if the PatientIdentifier is not voided
		if (!pi.isVoided()) {
			
			// Check is already in use by another patient
			if (Context.getPatientService().isIdentifierInUseByAnotherPatient(pi)) {
				throw new IdentifierNotUniqueException("Identifier " + pi.getIdentifier()
			        + " already in use by another patient");
			}
			
			// Check that this is a identifier is valid
			validateIdentifier(pi.getIdentifier(), pi.getIdentifierType());
		}
	}
	
	/**
	 * Validates that a given identifier string is valid for a given {@link PatientIdentifierType}
	 * Checks for things like blank identifiers, invalid check digits, and invalid format.
	 * 
	 * @param pit - the {@link PatientIdentifierType} to validate against
	 * @param identifier - the identifier to check against the passed {@link PatientIdentifierType}
	 * @throws PatientIdentifierException if the identifier is invalid
	 * @should fail validation if PatientIdentifierType is null
	 * @should fail validation if identifier is blank
	 * @see #checkIdentifierAgainstFormat(String, String)
	 * @see #checkIdentifierAgainstValidator(String, IdentifierValidator)
	 */
	public static void validateIdentifier(String identifier, PatientIdentifierType pit) throws PatientIdentifierException {
		
		log.debug("Checking identifier: " + identifier + " for type: " + pit);
		
		// Validate input parameters
		if (pit == null) {
			throw new BlankIdentifierException("Patient Identifier Type cannot be null");
		}
		if (StringUtils.isBlank(identifier)) {
			throw new BlankIdentifierException("Identifier cannot be null or blank");
		}
		
		checkIdentifierAgainstFormat(identifier, pit.getFormat());
		
		// Check identifier against IdentifierValidator
		if (pit.hasValidator()) {
			IdentifierValidator validator = Context.getPatientService().getIdentifierValidator(pit.getValidator());
			checkIdentifierAgainstValidator(identifier, validator);
		}
		log.debug("The identifier check was successful");
		
	}
	
	/**
	 * Validates that a given identifier string is valid for a given regular expression format
	 * 
	 * @param format - the regular expression format to validate against
	 * @param identifier - the identifier to check against the passed {@link PatientIdentifierType}
	 * @throws PatientIdentifierException if the identifier is does not match the format
	 * @should fail validation if identifier is blank
	 * @should fail validation if identifier does not match the format
	 * @should pass validation if identifier matches the format
	 * @should pass validation if the format is blank
	 */
	public static void checkIdentifierAgainstFormat(String identifier, String format) throws PatientIdentifierException {
		
		log.debug("Checking identifier: " + identifier + " against format: " + format);
		
		if (StringUtils.isBlank(identifier)) {
			throw new BlankIdentifierException("Identifier cannot be null or blank");
		}
		
		if (StringUtils.isBlank(format)) {
			log.debug("Format is blank, identifier passes.");
			return;
		}
		
		// Check identifier against regular expression format
		if (!identifier.matches(format)) {
			log.debug("The two DO NOT match");
			throw new InvalidIdentifierFormatException("Identifier [" + identifier + "] does not match required format: "
			        + format);
		}
		log.debug("The two match!!");
	}
	
	/**
	 * Validates that a given identifier string is valid for a given IdentifierValidator
	 * 
	 * @param identifier the identifier to check against the passed {@link PatientIdentifierType}
	 * @param validator the IdentifierValidator to use to check the identifier
	 * @throws PatientIdentifierException if the identifier is does not match the format
	 * @should fail validation if identifier is blank
	 * @should fail validation if identifier is invalid
	 * @should pass validation if identifier is valid
	 * @should pass validation if validator is null
	 */
	public static void checkIdentifierAgainstValidator(String identifier, IdentifierValidator validator)
	                                                                                                    throws PatientIdentifierException {
		
		log.debug("Checking identifier: " + identifier + " against validator: " + validator);
		
		if (StringUtils.isBlank(identifier)) {
			throw new BlankIdentifierException("Identifier cannot be null or blank");
		}
		
		if (validator == null) {
			log.debug("Validator is null, identifier passes.");
			return;
		}
		
		// Check identifier against IdentifierValidator
		try {
			if (!validator.isValid(identifier)) {
				throw new InvalidCheckDigitException("Invalid check digit for identifier: " + identifier);
			}
		}
		catch (UnallowedIdentifierException e) {
			throw new InvalidCheckDigitException("Identifier " + identifier + " is not appropriate for validation scheme "
			        + validator.getName());
		}
		log.debug("The identifier passed validation.");
		
	}
	
}

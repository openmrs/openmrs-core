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
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PatientIdentifierType.LocationBehavior;
import org.openmrs.PatientIdentifierType.UniquenessBehavior;
import org.openmrs.annotation.Handler;
import org.openmrs.api.BlankIdentifierException;
import org.openmrs.api.IdentifierNotUniqueException;
import org.openmrs.api.InvalidCheckDigitException;
import org.openmrs.api.InvalidIdentifierFormatException;
import org.openmrs.api.PatientIdentifierException;
import org.openmrs.api.context.Context;
import org.openmrs.patient.IdentifierValidator;
import org.openmrs.patient.UnallowedIdentifierException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * This class validates a PatientIdentifier object.
 */
@Handler(supports = { PatientIdentifier.class }, order = 50)
public class PatientIdentifierValidator implements Validator {
	
	private static final Logger log = LoggerFactory.getLogger(PatientIdentifierValidator.class);
	
	/**
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	@Override
	public boolean supports(Class<?> c) {
		return PatientIdentifier.class.isAssignableFrom(c);
	}
	
	/**
	 * Validates a PatientIdentifier.
	 * 
	 * @see org.springframework.validation.Validator#validate(java.lang.Object,
	 *      org.springframework.validation.Errors)
	 * @should pass validation if field lengths are correct
	 * @should fail validation if field lengths are not correct
	 */
	@Override
	public void validate(Object obj, Errors errors) {
		PatientIdentifier pi = (PatientIdentifier) obj;
		try {
			validateIdentifier(pi);
			ValidateUtil.validateFieldLengths(errors, obj.getClass(), "identifier", "voidReason");
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
	 * @should pass if in use and id type uniqueness is set to non unique
	 * @see #validateIdentifier(String, PatientIdentifierType)
	 */
	public static void validateIdentifier(PatientIdentifier pi) throws PatientIdentifierException {
		
		// Validate that the identifier is non-null
		if (pi == null) {
			throw new BlankIdentifierException("PatientIdentifier.error.null");
		}
		
		// Only validate if the PatientIdentifier is not voided
		if (!pi.getVoided()) {
			
			// Check that this is a valid identifier
			validateIdentifier(pi.getIdentifier(), pi.getIdentifierType());
			
			// Check that location is included if it is required (default behavior is to require it)
			LocationBehavior lb = pi.getIdentifierType().getLocationBehavior();
			if (pi.getLocation() == null && (lb == null || lb == LocationBehavior.REQUIRED)) {
				String identifierString = (pi.getIdentifier() != null) ? pi.getIdentifier() : "";
				throw new PatientIdentifierException(Context.getMessageSourceService().getMessage(
				    "PatientIdentifier.location.null", new Object[] { identifierString }, Context.getLocale()));
			}
			
			if (pi.getIdentifierType().getUniquenessBehavior() != UniquenessBehavior.NON_UNIQUE
			        && Context.getPatientService().isIdentifierInUseByAnotherPatient(pi)) {
				// Check is already in use by another patient
				throw new IdentifierNotUniqueException(Context.getMessageSourceService().getMessage(
				    "PatientIdentifier.error.notUniqueWithParameter", new Object[] { pi.getIdentifier() },
				    Context.getLocale()), pi);
			}
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
	 * @see #checkIdentifierAgainstFormat(String, String, String)
	 * @see #checkIdentifierAgainstValidator(String, IdentifierValidator)
	 */
	public static void validateIdentifier(String identifier, PatientIdentifierType pit) throws PatientIdentifierException {
		
		log.debug("Checking identifier: " + identifier + " for type: " + pit);
		
		// Validate input parameters
		if (pit == null) {
			throw new BlankIdentifierException("PatientIdentifierType.null");
		}
		if (StringUtils.isBlank(identifier)) {
			throw new BlankIdentifierException("PatientIdentifier.error.nullOrBlank");
		}
		
		checkIdentifierAgainstFormat(identifier, pit.getFormat(), pit.getFormatDescription());
		
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
	 * @param identifier - the identifier to check against the passed {@link PatientIdentifierType}
	 * @param format - the regular expression format to validate against
	 * @param formatDescription - user-friendly way of describing format (may be null)
	 * @throws PatientIdentifierException if the identifier is does not match the format
	 * @should fail validation if identifier is blank
	 * @should fail validation if identifier does not match the format
	 * @should pass validation if identifier matches the format
	 * @should pass validation if the format is blank
	 * @should include format in error message if no formatDescription is specified
	 * @should include formatDescription in error message if specified
	 */
	public static void checkIdentifierAgainstFormat(String identifier, String format, String formatDescription)
	        throws PatientIdentifierException {
		
		log.debug("Checking identifier: " + identifier + " against format: " + format);
		
		if (StringUtils.isBlank(identifier)) {
			throw new BlankIdentifierException("PatientIdentifier.error.nullOrBlank");
		}
		
		if (StringUtils.isBlank(format)) {
			log.debug("Format is blank, identifier passes.");
			return;
		}
		
		// Check identifier against regular expression format
		if (!identifier.matches(format)) {
			log.debug("The two DO NOT match");
			throw new InvalidIdentifierFormatException(getMessage("PatientIdentifier.error.invalidFormat", identifier,
			    StringUtils.isNotBlank(formatDescription) ? formatDescription : format));
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
			throw new BlankIdentifierException("PatientIdentifier.error.nullOrBlank");
		}
		
		if (validator == null) {
			log.debug("Validator is null, identifier passes.");
			return;
		}
		
		// Check identifier against IdentifierValidator
		try {
			if (!validator.isValid(identifier)) {
				throw new InvalidCheckDigitException(getMessage("PatientIdentifier.error.checkDigitWithParameter",
				    identifier));
			}
		}
		catch (UnallowedIdentifierException e) {
			throw new InvalidCheckDigitException(getMessage("PatientIdentifier.error.unallowedIdentifier", identifier,
			    validator.getName()));
		}
		log.debug("The identifier passed validation.");
		
	}
	
	private static String getMessage(String messageKey, String... arguments) {
		return Context.getMessageSourceService().getMessage(messageKey, arguments, Context.getLocale());
	}
}

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

import java.util.Date;

import org.apache.commons.lang3.ObjectUtils;
import org.openmrs.Encounter;
import org.openmrs.Visit;
import org.openmrs.annotation.Handler;
import org.openmrs.api.APIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

/**
 * Validator for {@link Encounter} class
 *
 * @since 1.9
 */
@Handler(supports = { Encounter.class }, order = 50)
public class EncounterValidator implements Validator {
	
	private static final Logger log = LoggerFactory.getLogger(EncounterValidator.class);
	
	/**
	 * Returns whether or not this validator supports validating a given class.
	 *
	 * @param c The class to check for support.
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	@Override
	public boolean supports(Class<?> c) {
		log.debug("{}.supports: {}", this.getClass().getName(), c.getName());
		return Encounter.class.isAssignableFrom(c);
	}
	
	/**
	 * Validates the given Encounter. Currently checks if the patient has been set and also ensures
	 * that the patient for an encounter and the visit it is associated to if any, are the same.
	 *
	 * @param obj The encounter to validate.
	 * @param errors Errors
	 * @see org.springframework.validation.Validator#validate(java.lang.Object,
	 *      org.springframework.validation.Errors)
	 * <strong>Should</strong> fail if the patients for the visit and the encounter dont match
	 * <strong>Should</strong> fail if patient is not set
	 * <strong>Should</strong> fail if encounter type is not set
	 * <strong>Should</strong> fail if encounter dateTime is not set
	 * <strong>Should</strong> fail if encounter dateTime is after current dateTime
	 * <strong>Should</strong> fail if encounter dateTime is before visit startDateTime
	 * <strong>Should</strong> fail if encounter dateTime is after visit stopDateTime
	 * <strong>Should</strong> pass validation if field lengths are correct
	 * <strong>Should</strong> fail validation if field lengths are not correct
	 */
	@Override
	public void validate(Object obj, Errors errors) throws APIException {
		log.debug("{}.validate...", this.getClass().getName());
		
		if (obj == null || !(obj instanceof Encounter)) {
			throw new IllegalArgumentException("The parameter obj should not be null and must be of type " + Encounter.class);
		}
		
		Encounter encounter = (Encounter) obj;
		
		ValidationUtils.rejectIfEmpty(errors, "encounterType", "Encounter.error.encounterType.required",
		    "Encounter type is Required");
		
		ValidationUtils.rejectIfEmpty(errors, "patient", "Encounter.error.patient.required", "Patient is required");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "encounterDatetime", "Encounter.datetime.required");
		if (encounter.getVisit() != null && !ObjectUtils.equals(encounter.getVisit().getPatient(), encounter.getPatient())) {
			errors.rejectValue("visit", "Encounter.visit.patients.dontMatch",
			    "The patient for the encounter and visit should be the same");
		}
		
		Date encounterDateTime = encounter.getEncounterDatetime();
		
		if (encounterDateTime != null && encounterDateTime.after(new Date())) {
			errors.rejectValue("encounterDatetime", "Encounter.datetimeShouldBeBeforeCurrent",
			    "The encounter datetime should be before the current date.");
		}
		
		Visit visit = encounter.getVisit();
		if (visit != null && encounterDateTime != null) {
			if (visit.getStartDatetime() != null && encounterDateTime.before(visit.getStartDatetime())) {
				errors.rejectValue("encounterDatetime", "Encounter.datetimeShouldBeInVisitDatesRange",
				    "The encounter datetime should be between the visit start and stop dates.");
			}
			
			if (visit.getStopDatetime() != null && encounterDateTime.after(visit.getStopDatetime())) {
				errors.rejectValue("encounterDatetime", "Encounter.datetimeShouldBeInVisitDatesRange",
				    "The encounter datetime should be between the visit start and stop dates.");
			}
		}
		ValidateUtil.validateFieldLengths(errors, obj.getClass(), "voidReason");
	}
}

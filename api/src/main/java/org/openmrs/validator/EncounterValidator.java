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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Encounter;
import org.openmrs.EncounterRole;
import org.openmrs.Provider;
import org.openmrs.Visit;
import org.openmrs.annotation.Handler;
import org.openmrs.api.APIException;
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
	
	private static final Log log = LogFactory.getLog(EncounterValidator.class);
	
	/**
	 * Returns whether or not this validator supports validating a given class.
	 * 
	 * @param c The class to check for support.
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	@SuppressWarnings("rawtypes")
	public boolean supports(Class c) {
		if (log.isDebugEnabled())
			log.debug(this.getClass().getName() + ".supports: " + c.getName());
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
	 * @should fail if the patients for the visit and the encounter dont match
	 * @should fail if patient is not set
	 * @should fail if encounter dateTime is after current dateTime
	 * @should fail if encounter dateTime is before visit startDateTime
	 * @should fail if encounter dateTime is after visit stopDateTime
	 */
	public void validate(Object obj, Errors errors) throws APIException {
		if (log.isDebugEnabled())
			log.debug(this.getClass().getName() + ".validate...");
		
		if (obj == null || !(obj instanceof Encounter))
			throw new IllegalArgumentException("The parameter obj should not be null and must be of type " + Encounter.class);
		
		Encounter encounter = (Encounter) obj;
		
		if (encounter != null) {
			ValidationUtils.rejectIfEmpty(errors, "patient", "Encounter.error.patient.required", "Patient is required");
			if (encounter.getVisit() != null && !encounter.getVisit().getPatient().equals(encounter.getPatient())) {
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
			
			Map<Integer, EncounterRole> encounterMap = new HashMap<Integer, EncounterRole>();
			
			Map<EncounterRole, Set<Provider>> providers = encounter.getProvidersByRoles();
			for (Entry<EncounterRole, Set<Provider>> entry : providers.entrySet()) {
				EncounterRole encounterRole = entry.getKey();
				
				if (encounterMap.containsKey(encounterRole.getEncounterRoleId())) {
					errors.rejectValue("providersByRoles", "Encounter.error.duplicateProviderEncounterRole",
					    "Provider cannot be added more than once for the same encounter role");
					break;
				}
				
				encounterMap.put(encounterRole.getEncounterRoleId(), encounterRole);
			}
		}
	}
}

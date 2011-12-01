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
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.PatientProgram;
import org.openmrs.PatientState;
import org.openmrs.ProgramWorkflowState;
import org.openmrs.annotation.Handler;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsUtil;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

/**
 * This class validates a {@link PatientProgram} object
 * 
 * @since 1.9
 */
@Handler(supports = { PatientProgram.class }, order = 50)
public class PatientProgramValidator implements Validator {
	
	private static final Log log = LogFactory.getLog(PatientProgramValidator.class);
	
	/**
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	@SuppressWarnings("rawtypes")
	public boolean supports(Class c) {
		return PatientProgram.class.isAssignableFrom(c);
	}
	
	/**
	 * Validates the given PatientProgram.
	 * 
	 * @param obj The patient program to validate.
	 * @param errors Errors
	 * @see org.springframework.validation.Validator#validate(java.lang.Object,
	 *      org.springframework.validation.Errors)
	 * @should fail validation if obj is null
	 * @should fail if the patient field is blank
	 * @should fail if there is more than one patientState with the same states and startDates
	 * @should pass if the start and end dates for any patient state are equal
	 * @should pass if the start and end dates for any patient state are both null
	 * @should fail if any patient state has an end date before its start date
	 */
	public void validate(Object obj, Errors errors) {
		if (log.isDebugEnabled())
			log.debug(this.getClass().getName() + ".validate...");
		
		if (obj == null)
			throw new IllegalArgumentException("The parameter obj should not be null");
		
		PatientProgram program = (PatientProgram) obj;
		ValidationUtils.rejectIfEmpty(errors, "patient", "error.required", Context.getMessageSourceService().getMessage(
		    "general.patient"));
		
		Set<PatientState> patientStates = program.getStates();
		if (patientStates != null) {
			//map to keep track of valid unique state and start date combinations
			Map<ProgramWorkflowState, Date> stateStartDateMap = null;
			//check for duplicate states
			for (PatientState patientState : patientStates) {
				if (patientState.isVoided())
					continue;
				
				if (patientState.getStartDate() != null) {
					if (OpenmrsUtil.compareWithNullAsLatest(patientState.getEndDate(), patientState.getStartDate()) < 0) {
						errors.reject("PatientState.error.endDateCannotBeBeforeEndDate");
						return;
					}
				}
				
				if (stateStartDateMap == null)
					stateStartDateMap = new HashMap<ProgramWorkflowState, Date>();
				
				//check if we already have a patient state with the same work flow state and start date
				//note that we can have null start dates, meaning we won't allow multiple patient states with
				//the same work flow states and null start dates
				if (stateStartDateMap.containsKey(patientState.getState())
				        && OpenmrsUtil.nullSafeEquals(stateStartDateMap.get(patientState.getState()), patientState
				                .getStartDate())) {
					errors.reject("PatientState.error.duplicatePatientStates");
					return;
				}
				
				stateStartDateMap.put(patientState.getState(), patientState.getStartDate());
			}
		}
		
		//TODO validate other fields
	}
}

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
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.PatientProgram;
import org.openmrs.PatientState;
import org.openmrs.ProgramWorkflow;
import org.openmrs.annotation.Handler;
import org.openmrs.api.context.Context;
import org.openmrs.messagesource.MessageSourceService;
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
	 * @should fail if the start date for any patient state is null
	 * @should fail if any patient state has an end date before its start date
	 * @should fail if the program property is null
	 * @should fail if any patient states overlap each other in the same work flow
	 * @should fail if any patient states have the same start dates in the same work flow
	 * @should fail if a patientState has an invalid work flow state
	 * @should fail if a patient program has duplicate states in the same work flow
	 * @should fail if a patient is in multiple states in the same work flow
	 * @should pass if a patient is in multiple states in different work flows
	 * @should pass for a valid program
	 */
	public void validate(Object obj, Errors errors) {
		if (log.isDebugEnabled())
			log.debug(this.getClass().getName() + ".validate...");
		
		if (obj == null)
			throw new IllegalArgumentException("The parameter obj should not be null");
		MessageSourceService mss = Context.getMessageSourceService();
		PatientProgram patientProgram = (PatientProgram) obj;
		ValidationUtils.rejectIfEmpty(errors, "patient", "error.required",
		    new Object[] { mss.getMessage("general.patient") });
		ValidationUtils.rejectIfEmpty(errors, "program", "error.required",
		    new Object[] { mss.getMessage("Program.program") });
		
		if (errors.hasErrors())
			return;
		
		Set<ProgramWorkflow> workFlows = patientProgram.getProgram().getWorkflows();
		//Patient state validation is specific to a work flow
		for (ProgramWorkflow workFlow : workFlows) {
			Set<PatientState> patientStates = patientProgram.getStates();
			if (patientStates != null) {
				//Set to store to keep track of unique valid state and start date combinations
				Set<String> statesAndStartDates = new HashSet<String>();
				PatientState latestState = null;
				Set<Date> uniqueStartDates = new HashSet<Date>();
				boolean foundCurrentPatientState = false;
				for (PatientState patientState : patientStates) {
					if (patientState.isVoided())
						continue;
					
					String missingRequiredFieldCode = null;
					if (patientState.getStartDate() == null)
						missingRequiredFieldCode = "general.dateStart";
					else if (patientState.getState() == null)
						missingRequiredFieldCode = "State.state";
					
					if (missingRequiredFieldCode != null) {
						errors.reject("PatientState.error.requiredField", new Object[] { mss
						        .getMessage(missingRequiredFieldCode) }, null);
						return;
					}
					
					//state should belong to one of the workflows in the program
					boolean isValidPatientState = false;
					for (ProgramWorkflow wf : workFlows) {
						if (wf.getStates().contains(patientState.getState())) {
							isValidPatientState = true;
							break;
						}
					}
					
					if (!isValidPatientState) {
						errors.reject("PatientState.error.invalidPatientState", new Object[] { patientState }, null);
						return;
					}
					
					//will validate it with other states in its workflow
					if (!patientState.getState().getProgramWorkflow().equals(workFlow))
						continue;
					
					if (OpenmrsUtil.compareWithNullAsLatest(patientState.getEndDate(), patientState.getStartDate()) < 0) {
						errors.reject("PatientState.error.endDateCannotBeBeforeStartDate");
						return;
					} else if (statesAndStartDates.contains(patientState.getState().getId() + ""
					        + patientState.getStartDate())) {
						// we already have a patient state with the same work flow state and start date
						errors.reject("PatientState.error.duplicatePatientStates");
						return;
					}
					
					//Ensure that the patient is only in one state at a given time
					if (!foundCurrentPatientState && patientState.getEndDate() == null)
						foundCurrentPatientState = true;
					else if (foundCurrentPatientState && patientState.getEndDate() == null) {
						errors.reject("PatientProgram.error.cannotBeInMultipleStates");
						return;
					}
					
					if (latestState == null)
						latestState = patientState;
					else {
						if (OpenmrsUtil.compare(patientState.getStartDate(), latestState.getStartDate()) == 0
						        || uniqueStartDates.contains(patientState.getStartDate())) {
							errors.reject("PatientProgram.error.foundStatesWithSameStartDates");
							return;
						} else if (patientState.compareTo(latestState) > 0) {
							//patient should have already left this state since it is older
							if (latestState.getEndDate() == null) {
								errors.reject("PatientProgram.error.cannotBeInMultipleStates");
								return;
							} else if (OpenmrsUtil.compare(patientState.getStartDate(), latestState.getEndDate()) < 0) {
								//current state was started before a previous state was ended
								errors.reject("PatientProgram.error.foundOverlappingStates", new Object[] {
								        patientState.getStartDate(), latestState.getEndDate() }, null);
								return;
							}
							latestState = patientState;
						} else if (patientState.compareTo(latestState) < 0) {
							//patient should have already left this state since it is older
							if (patientState.getEndDate() == null) {
								errors.reject("PatientProgram.error.cannotBeInMultipleStates");
								return;
							} else if (OpenmrsUtil.compare(latestState.getStartDate(), patientState.getEndDate()) < 0) {
								//latest state was started before a previous state was ended
								errors.reject("PatientProgram.error.foundOverlappingStates");
								return;
							}
						}
					}
					
					uniqueStartDates.add(patientState.getStartDate());
					statesAndStartDates.add(patientState.getState().getId() + "" + patientState.getStartDate());
				}
			}
		}
		//
	}
}

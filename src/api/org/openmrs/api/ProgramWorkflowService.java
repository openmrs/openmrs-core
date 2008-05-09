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
package org.openmrs.api;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.openmrs.Cohort;
import org.openmrs.Concept;
import org.openmrs.ConceptStateConversion;
import org.openmrs.Patient;
import org.openmrs.PatientProgram;
import org.openmrs.PatientState;
import org.openmrs.Program;
import org.openmrs.ProgramWorkflow;
import org.openmrs.ProgramWorkflowState;
import org.openmrs.User;
import org.openmrs.api.db.ProgramWorkflowDAO;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface ProgramWorkflowService {

	public void setProgramWorkflowDAO(ProgramWorkflowDAO dao);

	@Transactional(readOnly=true)
	public List<Program> getPrograms();

	public void createOrUpdateProgram(Program p);

	@Transactional(readOnly=true)
	public Program getProgram(Integer id);

	@Transactional(readOnly=true)
	public Program getProgram(String name);

	public void retireProgram(Program p);

	public void createWorkflow(ProgramWorkflow w);

	@Transactional(readOnly=true)
	public ProgramWorkflow getWorkflow(Integer id);

	@Transactional(readOnly=true)
	public ProgramWorkflow getWorkflow(Program program, String name);

	public void updateWorkflow(ProgramWorkflow w);

	public void voidWorkflow(ProgramWorkflow w, String reason);

	@Transactional(readOnly=true)
	public List<ProgramWorkflowState> getStates();
	
	@Transactional(readOnly=true)
	public List<ProgramWorkflowState> getStates(boolean includeVoided);
	
	@Transactional(readOnly=true)
	public ProgramWorkflowState getState(Integer id);

	@Transactional(readOnly=true)
	public ProgramWorkflowState getState(ProgramWorkflow wf, String name);

	public void createPatientProgram(PatientProgram p);

	public void updatePatientProgram(PatientProgram p);

	@Transactional(readOnly=true)
	public PatientProgram getPatientProgram(Integer id);

	@Transactional(readOnly=true)
	public PatientState getPatientState(Integer id);

	@Transactional(readOnly=true)
	public Collection<PatientProgram> getPatientPrograms(Patient patient);
	
	@Transactional(readOnly=true)
	public List<PatientProgram> getPatientPrograms(Cohort cohort, Collection<Program> programs);

	public void enrollPatientInProgram(Patient patient, Program program,
			Date enrollmentDate, Date completionDate, User creator);

	public void voidPatientProgram(PatientProgram p, String reason);

	/**
	 * @return patientIds of all patients who are enrolled in _program_ between _fromDate_ and _toDate_ 
	 */
	@Transactional(readOnly=true)
	public Collection<Integer> patientsInProgram(Program program,
			Date fromDate, Date toDate);

	@Transactional(readOnly=true)
	public Collection<PatientProgram> getCurrentPrograms(Patient patient,
			Date onDate);

	// TODO: move this into Patient (probably make this a lazily-loaded hibernate mapping).
	// This is just a quick implementation without changing any hibernate mappings
	@Transactional(readOnly=true)
	public PatientState getLatestState(PatientProgram patientProgram,
			ProgramWorkflow workflow);

	@Transactional(readOnly=true)
	public List<ProgramWorkflowState> getPossibleNextStates(
			PatientProgram patientProgram, ProgramWorkflow workflow);

	// TODO: once we have a table of legal state transitions, then use that instead of this simple algorithm
	@Transactional(readOnly=true)
	public boolean isLegalTransition(ProgramWorkflowState fromState,
			ProgramWorkflowState toState);

	public void changeToState(PatientProgram patientProgram,
			ProgramWorkflow wf, ProgramWorkflowState st, Date onDate);

	/**
	 * Voids the last unvoided state in the given workflow, and clears the endDate of the next-to-last unvoided state.  
	 * @param patientProgram
	 * @param wf
	 * @param voidReason
	 */
	public void voidLastState(PatientProgram patientProgram,
			ProgramWorkflow wf, String voidReason);

	/**
	 * @return Returns true if _patient_ is enrolled in _program_ anytime between _fromDate_ and _toDate_. (null values for those dates mean beginning-of-time and end-of-time)
	 */
	@Transactional(readOnly=true)
	public boolean isInProgram(Patient patient, Program program, Date fromDate,
			Date toDate);

	public void createConceptStateConversion(ConceptStateConversion csc);

	public void updateConceptStateConversion(ConceptStateConversion csc);

	public void deleteConceptStateConversion(ConceptStateConversion csc);

	@Transactional(readOnly=true)
	public ConceptStateConversion getConceptStateConversion(Integer id);

	@Transactional(readOnly=true)
	public List<ConceptStateConversion> getAllConversions();

	public void triggerStateConversion(Patient patient, Concept reasonForExit, Date dateConverted);

	@Transactional(readOnly=true)
	public ConceptStateConversion getConceptStateConversion(ProgramWorkflow workflow, Concept trigger);

	@Transactional(readOnly=true)
	public Set<ProgramWorkflow> getCurrentWorkflowsByPatient(Patient patient);

	@Transactional(readOnly=true)
	public Set<ProgramWorkflow> getCurrentWorkflowsByPatientProgram(PatientProgram program);
}
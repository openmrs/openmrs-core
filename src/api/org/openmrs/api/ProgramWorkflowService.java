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
import org.openmrs.annotation.Authorized;
import org.openmrs.api.db.ProgramWorkflowDAO;
import org.openmrs.util.OpenmrsConstants;
import org.springframework.transaction.annotation.Transactional;

/**
 * Contains methods pertaining to management of Programs, ProgramWorkflows, ProgramWorkflowStates,
 * PatientPrograms, PatientStates, and ConceptStateConversions Use:<br/>
 * 
 * <pre>
 *   Program program = new Program();
 *   program.set___(___);
 *   ...etc
 *   Context.getProgramWorkflowService().saveProgram(program);
 * </pre>
 */
@Transactional
public interface ProgramWorkflowService extends OpenmrsService {
	
	/**
	 * Setter for the ProgramWorkflow DataAccessObject (DAO). The DAO is used for saving and
	 * retrieving from the database
	 * 
	 * @param dao - The DAO for this service
	 */
	public void setProgramWorkflowDAO(ProgramWorkflowDAO dao);
	
	// **************************
	// PROGRAM
	// **************************
	
	/**
	 * Save <code>program</code> to database (create if new or update if changed)
	 * 
	 * @param program is the Program to be saved to the database
	 * @return The Program that was saved
	 * @throws APIException
	 * @should create program workflows
	 */
	@Authorized( { OpenmrsConstants.PRIV_MANAGE_PROGRAMS })
	public Program saveProgram(Program program) throws APIException;
	
	/**
	 * Returns a program given that programs primary key <code>programId</code> A null value is
	 * returned if no program exists with this programId.
	 * 
	 * @param programId integer primary key of the program to find
	 * @return Program object that has program.programId = <code>programId</code> passed in.
	 * @throws APIException
	 */
	@Authorized( { OpenmrsConstants.PRIV_VIEW_PROGRAMS })
	@Transactional(readOnly = true)
	public Program getProgram(Integer programId) throws APIException;
	
	/**
	 * @deprecated use {@link #getProgramByName(String)}
	 */
	@Transactional(readOnly = true)
	public Program getProgram(String name);
	
	/**
	 * Returns a program given the program's exact <code>name</code> A null value is returned if
	 * there is no program with this name
	 * 
	 * @param name the exact name of the program to match on
	 * @return Program matching the <code>name</code> to Program.name
	 * @throws APIException
	 */
	@Authorized( { OpenmrsConstants.PRIV_VIEW_PROGRAMS })
	@Transactional(readOnly = true)
	public Program getProgramByName(String name) throws APIException;
	
	/**
	 * Returns all programs, includes retired programs. This method delegates to the
	 * #getAllPrograms(boolean) method
	 * 
	 * @return List<Program> of all existing programs, including retired programs
	 * @throws APIException
	 */
	@Authorized( { OpenmrsConstants.PRIV_VIEW_PROGRAMS })
	@Transactional(readOnly = true)
	public List<Program> getAllPrograms() throws APIException;
	
	/**
	 * Returns all programs
	 * 
	 * @param includeRetired whether or not to include retired programs
	 * @return List<Program> all existing programs, including retired based on the input parameter
	 * @throws APIException
	 */
	@Authorized( { OpenmrsConstants.PRIV_VIEW_PROGRAMS })
	@Transactional(readOnly = true)
	public List<Program> getAllPrograms(boolean includeRetired) throws APIException;
	
	/**
	 * Returns programs that match the given string. A null list will never be returned. An empty
	 * list will be returned if there are no programs matching this <code>nameFragment</code>
	 * 
	 * @param nameFragment is the string used to search for programs
	 * @return List<Program> - list of Programs whose name matches the input parameter
	 * @throws APIException
	 */
	@Authorized( { OpenmrsConstants.PRIV_VIEW_PROGRAMS })
	@Transactional(readOnly = true)
	public List<Program> getPrograms(String nameFragment) throws APIException;
	
	/**
	 * Completely remove a program from the database (not reversible) This method delegates to
	 * #purgeProgram(program, boolean) method
	 * 
	 * @param program the Program to clean out of the database.
	 * @throws APIException
	 */
	@Authorized( { OpenmrsConstants.PRIV_MANAGE_PROGRAMS })
	public void purgeProgram(Program program) throws APIException;
	
	/**
	 * Completely remove a program from the database (not reversible)
	 * 
	 * @param cascade <code>true</code> to delete related content
	 * @throws APIException
	 */
	@Authorized( { OpenmrsConstants.PRIV_MANAGE_PROGRAMS })
	public void purgeProgram(Program program, boolean cascade) throws APIException;
	
	/**
	 * Retires the given program
	 * 
	 * @param program Program to be retired
	 * @return the Program which has been retired
	 * @throws APIException
	 */
	@Authorized( { OpenmrsConstants.PRIV_MANAGE_PROGRAMS })
	public Program retireProgram(Program program) throws APIException;
	
	/**
	 * Unretires the given program
	 * 
	 * @param program Program to be unretired
	 * @return the Program which has been unretired
	 * @throws APIException
	 */
	@Authorized( { OpenmrsConstants.PRIV_MANAGE_PROGRAMS })
	public Program unRetireProgram(Program program) throws APIException;
	
	// **************************
	// PATIENT PROGRAM 
	// **************************
	
	/**
	 * Save patientProgram to database (create if new or update if changed)
	 * 
	 * @param patientProgram is the PatientProgram to be saved to the database
	 * @return PatientProgram - the saved PatientProgram
	 * @throws APIException
	 * @should update patient program
	 */
	@Authorized( { OpenmrsConstants.PRIV_ADD_PATIENT_PROGRAMS, OpenmrsConstants.PRIV_EDIT_PATIENT_PROGRAMS })
	public PatientProgram savePatientProgram(PatientProgram patientProgram) throws APIException;
	
	/**
	 * Returns a PatientProgram given that PatientPrograms primary key <code>patientProgramId</code>
	 * A null value is returned if no PatientProgram exists with this patientProgramId.
	 * 
	 * @param patientProgramId integer primary key of the PatientProgram to find
	 * @return PatientProgram object that has patientProgram.patientProgramId =
	 *         <code>patientProgramId</code> passed in.
	 * @throws APIException
	 */
	@Authorized( { OpenmrsConstants.PRIV_VIEW_PATIENT_PROGRAMS })
	@Transactional(readOnly = true)
	public PatientProgram getPatientProgram(Integer patientProgramId) throws APIException;
	
	/**
	 * Returns PatientPrograms that match the input parameters. If an input parameter is set to
	 * null, the parameter will not be used. Calling this method will all null parameters will
	 * return all PatientPrograms in the database A null list will never be returned. An empty list
	 * will be returned if there are no programs matching the input criteria
	 * 
	 * @param patient - if supplied all PatientPrograms returned will be for this Patient
	 * @param program - if supplied all PatientPrograms returned will be for this Program
	 * @param minEnrollmentDate - if supplied will limit PatientPrograms to those with enrollments
	 *            on or after this Date
	 * @param maxEnrollmentDate - if supplied will limit PatientPrograms to those with enrollments
	 *            on or before this Date
	 * @param minCompletionDate - if supplied will limit PatientPrograms to those completed on or
	 *            after this Date OR not yet completed
	 * @param maxCompletionDate - if supplied will limit PatientPrograms to those completed on or
	 *            before this Date
	 * @param includeVoided - if true, will also include voided PatientPrograms
	 * @return List<PatientProgram> of PatientPrograms that match the passed input parameters
	 * @throws APIException
	 */
	@Authorized( { OpenmrsConstants.PRIV_VIEW_PATIENT_PROGRAMS })
	@Transactional(readOnly = true)
	public List<PatientProgram> getPatientPrograms(Patient patient, Program program, Date minEnrollmentDate,
	                                               Date maxEnrollmentDate, Date minCompletionDate, Date maxCompletionDate,
	                                               boolean includeVoided) throws APIException;
	
	/**
	 * Completely remove a patientProgram from the database (not reversible) This method delegates
	 * to #purgePatientProgram(patientProgram, boolean) method
	 * 
	 * @param patientProgram the PatientProgram to clean out of the database.
	 * @throws APIException
	 */
	@Authorized( { OpenmrsConstants.PRIV_PURGE_PATIENT_PROGRAMS })
	public void purgePatientProgram(PatientProgram patientProgram) throws APIException;
	
	/**
	 * Completely remove a patientProgram from the database (not reversible)
	 * 
	 * @param patientProgram the PatientProgram to clean out of the database.
	 * @param cascade <code>true</code> to delete related content
	 * @throws APIException
	 */
	@Authorized( { OpenmrsConstants.PRIV_PURGE_PATIENT_PROGRAMS })
	public void purgePatientProgram(PatientProgram patientProgram, boolean cascade) throws APIException;
	
	/**
	 * Voids the given patientProgram
	 * 
	 * @param patientProgram patientProgram to be voided
	 * @param reason is the reason why the patientProgram is being voided
	 * @return the voided PatientProgram
	 * @throws APIException
	 */
	@Authorized( { OpenmrsConstants.PRIV_DELETE_PATIENT_PROGRAMS })
	public PatientProgram voidPatientProgram(PatientProgram patientProgram, String reason) throws APIException;
	
	/**
	 * Unvoids the given patientProgram
	 * 
	 * @param patientProgram patientProgram to be un-voided
	 * @return the voided PatientProgram
	 * @throws APIException
	 */
	@Authorized( { OpenmrsConstants.PRIV_DELETE_PATIENT_PROGRAMS })
	public PatientProgram unvoidPatientProgram(PatientProgram patientProgram) throws APIException;
	
	// **************************
	// CONCEPT STATE CONVERSION
	// **************************
	
	/**
	 * Save ConceptStateConversion to database (create if new or update if changed)
	 * 
	 * @param conceptStateConversion - The ConceptStateConversion to save
	 * @return ConceptStateConversion - The saved ConceptStateConversion
	 * @throws APIException
	 */
	@Authorized( { OpenmrsConstants.PRIV_ADD_PATIENT_PROGRAMS, OpenmrsConstants.PRIV_EDIT_PATIENT_PROGRAMS })
	public ConceptStateConversion saveConceptStateConversion(ConceptStateConversion conceptStateConversion)
	                                                                                                       throws APIException;
	
	/**
	 * Returns a conceptStateConversion given that conceptStateConversions primary key
	 * <code>conceptStateConversionId</code> A null value is returned if no conceptStateConversion
	 * exists with this conceptStateConversionId.
	 * 
	 * @param conceptStateConversionId integer primary key of the conceptStateConversion to find
	 * @return ConceptStateConversion object that has
	 *         conceptStateConversion.conceptStateConversionId =
	 *         <code>conceptStateConversionId</code> passed in.
	 * @throws APIException
	 */
	@Authorized( { OpenmrsConstants.PRIV_VIEW_PROGRAMS })
	@Transactional(readOnly = true)
	public ConceptStateConversion getConceptStateConversion(Integer conceptStateConversionId) throws APIException;
	
	/**
	 * Returns all conceptStateConversions
	 * 
	 * @return List<ConceptStateConversion> of all ConceptStateConversions that exist
	 * @throws APIException
	 */
	@Authorized( { OpenmrsConstants.PRIV_VIEW_PROGRAMS })
	@Transactional(readOnly = true)
	public List<ConceptStateConversion> getAllConceptStateConversions() throws APIException;
	
	/**
	 * Completely remove a conceptStateConversion from the database (not reversible) This method
	 * delegates to #purgeConceptStateConversion(conceptStateConversion, boolean) method
	 * 
	 * @param conceptStateConversion the ConceptStateConversion to clean out of the database.
	 * @throws APIException
	 */
	@Authorized( { OpenmrsConstants.PRIV_MANAGE_PROGRAMS })
	public void purgeConceptStateConversion(ConceptStateConversion conceptStateConversion) throws APIException;
	
	/**
	 * Completely remove a conceptStateConversion from the database (not reversible)
	 * 
	 * @param conceptStateConversion the ConceptStateConversion to clean out of the database.
	 * @param cascade <code>true</code> to delete related content
	 * @throws APIException
	 */
	@Authorized( { OpenmrsConstants.PRIV_MANAGE_PROGRAMS })
	public void purgeConceptStateConversion(ConceptStateConversion conceptStateConversion, boolean cascade)
	                                                                                                       throws APIException;
	
	/**
	 * Triggers any ConceptStateConversion that exists for the passed <code>reasonForExit</code>
	 * concept and any ProgramWorkflow in the PatientPrograms for the <code>patient</code>
	 * 
	 * @param patient - the Patient to trigger the ConceptStateConversion on
	 * @param reasonForExit - the Concept to trigger the ConceptStateConversion
	 * @param dateConverted - the Date of the ConceptStateConversion
	 * @throws APIException
	 */
	public void triggerStateConversion(Patient patient, Concept reasonForExit, Date dateConverted) throws APIException;
	
	/**
	 * Retrieves the ConceptStateConversion that matches the passed <code>ProgramWorkflow</code> and
	 * <code>Concept</code>
	 * 
	 * @param workflow - the ProgramWorkflow to check
	 * @param trigger - the Concept to check
	 * @return ConceptStateConversion that matches the passed <code>ProgramWorkflow</code> and
	 *         <code>Concept</code>
	 * @throws APIException
	 */
	@Transactional(readOnly = true)
	public ConceptStateConversion getConceptStateConversion(ProgramWorkflow workflow, Concept trigger) throws APIException;
	
	// **************************
	// DEPRECATED PROGRAM
	// **************************
	
	/**
	 * Create a new program
	 * 
	 * @param program Program to create
	 * @throws APIException
	 * @deprecated use {@link #saveProgram(Program)}
	 */
	@Authorized( { OpenmrsConstants.PRIV_MANAGE_PROGRAMS })
	public void createOrUpdateProgram(Program program) throws APIException;
	
	/**
	 * Returns all programs, includes retired programs.
	 * 
	 * @return List<Program> of all existing programs
	 * @deprecated use {@link #getAllPrograms()}
	 * @throws APIException
	 */
	@Authorized( { OpenmrsConstants.PRIV_VIEW_PROGRAMS })
	@Transactional(readOnly = true)
	public List<Program> getPrograms() throws APIException;
	
	// **************************
	// DEPRECATED PROGRAM WORKFLOW
	// **************************
	
	/**
	 * Create a new programWorkflow
	 * 
	 * @param programWorkflow - The ProgramWorkflow to create
	 * @deprecated use {@link Program#addWorkflow(ProgramWorkflow) followed by @link
	 *             #saveProgram(Program)}
	 * @throws APIException
	 */
	@Authorized( { OpenmrsConstants.PRIV_MANAGE_PROGRAMS })
	public void createWorkflow(ProgramWorkflow programWorkflow) throws APIException;
	
	/**
	 * Update a programWorkflow
	 * 
	 * @param programWorkflow - The ProgramWorkflow to update
	 * @deprecated use {@link #saveProgram(Program) to save changes to all ProgramWorkflows for the
	 *             given Program}
	 * @throws APIException
	 */
	@Authorized( { OpenmrsConstants.PRIV_MANAGE_PROGRAMS })
	public void updateWorkflow(ProgramWorkflow programWorkflow) throws APIException;
	
	/**
	 * Returns a programWorkflow given that programWorkflows primary key
	 * <code>programWorkflowId</code>
	 * 
	 * @param id integer primary key of the ProgramWorkflow to find
	 * @return ProgramWorkflow object that has an id that matches the input parameter
	 * @deprecated ProgramWorkflows should not be retrieved directly, but rather through the
	 *             programs they belong to: use {@link org.openmrs.Program#getWorkflows()}
	 * @throws APIException
	 */
	@Authorized( { OpenmrsConstants.PRIV_VIEW_PROGRAMS })
	@Transactional(readOnly = true)
	public ProgramWorkflow getWorkflow(Integer id) throws APIException;
	
	/**
	 * Returns a programWorkflow with the given name within the given Program
	 * 
	 * @param program - The Program of the ProgramWorkflow to return
	 * @param name - The name of the ProgramWorkflow to return
	 * @return ProgramWorkflow - The ProgramWorkflow that matches the passed Program and name
	 * @deprecated use {@link Program#getWorkflowByName(String)}
	 * @throws APIException
	 */
	@Authorized( { OpenmrsConstants.PRIV_VIEW_PROGRAMS })
	@Transactional(readOnly = true)
	public ProgramWorkflow getWorkflow(Program program, String name) throws APIException;
	
	/**
	 * Retires the given programWorkflow
	 * 
	 * @param programWorkflow - The ProgramWorkflow to retire
	 * @param reason - The reason for retiring the ProgramWorkflow
	 * @deprecated use {@link ProgramWorkflow#setRetired(Boolean) followed by @link
	 *             #saveProgram(Program)}
	 * @throws APIException
	 */
	@Authorized( { OpenmrsConstants.PRIV_MANAGE_PROGRAMS })
	public void voidWorkflow(ProgramWorkflow programWorkflow, String reason) throws APIException;
	
	// **************************
	// DEPRECATED PROGRAM WORKFLOW STATE
	// **************************
	
	/**
	 * Returns all ProgramWorkflowStates
	 * 
	 * @return List<ProgramWorkflowState> - all ProgramWorkflowStates that exist
	 * @deprecated ProgramWorkflowStates should be retrieved from the {@link ProgramWorkflow} they
	 *             belong to
	 * @see ProgramWorkflow#getStates()
	 * @throws APIException
	 */
	@Authorized( { OpenmrsConstants.PRIV_VIEW_PROGRAMS })
	@Transactional(readOnly = true)
	public List<ProgramWorkflowState> getStates() throws APIException;
	
	/**
	 * Returns all ProgramWorkflowStates
	 * 
	 * @param includeVoided - if false, only returns non-voided ProgramWorkflowStates
	 * @return List<ProgramWorkflowState> - all ProgramWorkflowStates that exist, including voided
	 *         based on the input parameter
	 * @deprecated ProgramWorkflowStates should be retrieved from the {@link ProgramWorkflow} they
	 *             belong to
	 * @see ProgramWorkflow#getStates(boolean)
	 * @throws APIException
	 */
	@Authorized( { OpenmrsConstants.PRIV_VIEW_PROGRAMS })
	@Transactional(readOnly = true)
	public List<ProgramWorkflowState> getStates(boolean includeVoided) throws APIException;
	
	/**
	 * Returns ProgramWorkflowState with the passed primary key id
	 * 
	 * @param id - The primary key id of the ProgramWorkflowState to return
	 * @return ProgramWorkflowState - returns ProgramWorkflowState whose primary key id matches the
	 *         passed id
	 * @deprecated ProgramWorkflowStates should be retrieved from the {@link ProgramWorkflow} they
	 *             belong to
	 * @see ProgramWorkflow#getState(Integer)
	 * @throws APIException
	 */
	@Authorized( { OpenmrsConstants.PRIV_VIEW_PROGRAMS })
	@Transactional(readOnly = true)
	public ProgramWorkflowState getState(Integer id) throws APIException;
	
	/**
	 * Returns ProgramWorkflowState with the passed <code>name</code> in the passed
	 * <code>programWorkflow</code>
	 * 
	 * @param programWorkflow - The programWorkflow to check for ProgramWorkflowState
	 * @param name - the name of the programWorkflowState to look for
	 * @return ProgramWorkflowState - the ProgramWorkflowState with the passed <code>name</code> in
	 *         the passed <code>programWorkflow</code>
	 * @deprecated ProgramWorkflowStates should be retrieved from the {@link ProgramWorkflow} they
	 *             belong to
	 * @see ProgramWorkflow#getStateByName(String)
	 * @throws APIException
	 */
	@Authorized( { OpenmrsConstants.PRIV_VIEW_PROGRAMS })
	@Transactional(readOnly = true)
	public ProgramWorkflowState getState(ProgramWorkflow programWorkflow, String name) throws APIException;
	
	/**
	 * Returns List of ProgramWorkflowStates that a patient is allowed to transition into given
	 * their current program
	 * 
	 * @param patientProgram - the PatientProgram to retrieve possible next transitions from
	 * @param workflow - the ProgramWorkflow to retrieve possible next transitions from
	 * @return List<ProgramWorkflowState> - returns List<ProgramWorkflowState> that a patient with
	 *         the given PatientProgram and ProgramWorkflow is allowed to transition into
	 * @deprecated use {@link ProgramWorkflow#getPossibleNextStates(PatientProgram)}
	 * @throws APIException
	 */
	@Authorized( { OpenmrsConstants.PRIV_VIEW_PROGRAMS })
	@Transactional(readOnly = true)
	public List<ProgramWorkflowState> getPossibleNextStates(PatientProgram patientProgram, ProgramWorkflow workflow)
	                                                                                                                throws APIException;
	
	/**
	 * Returns boolean indicating whether it is legal to transition from one ProgramWorkflowState to
	 * another
	 * 
	 * @param fromState - the ProgramWorkflowState to use as the state to check transitions from
	 * @param toState - the ProgramWorkflowState to use as the state to check transitions into from
	 *            <code>fromState</code>
	 * @return boolean - returns true if a legal transition exists from <code>fromState</code> to
	 *         <code>toState</code>
	 * @deprecated use
	 *             {@link ProgramWorkflow#isLegalTransition(ProgramWorkflowState, ProgramWorkflowState)}
	 * @throws APIException
	 */
	@Authorized( { OpenmrsConstants.PRIV_VIEW_PROGRAMS })
	@Transactional(readOnly = true)
	public boolean isLegalTransition(ProgramWorkflowState fromState, ProgramWorkflowState toState) throws APIException;
	
	// **************************
	// DEPRECATED PATIENT PROGRAM 
	// **************************
	
	/**
	 * Create a new patientProgram
	 * 
	 * @param patientProgram - The PatientProgram to create
	 * @deprecated use {@link #savePatientProgram(PatientProgram)}
	 * @throws APIException
	 */
	@Authorized( { OpenmrsConstants.PRIV_ADD_PATIENT_PROGRAMS })
	public void createPatientProgram(PatientProgram patientProgram) throws APIException;
	
	/**
	 * Update a patientProgram
	 * 
	 * @param patientProgram - The PatientProgram to update
	 * @deprecated use {@link #savePatientProgram(PatientProgram)}
	 * @throws APIException
	 */
	@Authorized( { OpenmrsConstants.PRIV_EDIT_PATIENT_PROGRAMS })
	public void updatePatientProgram(PatientProgram patientProgram) throws APIException;
	
	/**
	 * Create a new PatientProgram
	 * 
	 * @param patient - The Patient to enroll
	 * @param program - The Program to enroll the <code>patient</code> into
	 * @param enrollmentDate - The Date to use as the enrollment date in the <code>program</code>
	 *            for the <code>patient</code>
	 * @param completionDate - The Date to use as the completion date in the <code>program</code>
	 *            for the <code>patient</code>
	 * @param creator - The User who is enrolling this <code>patient</code>
	 * @deprecated use {new PatientProgram(...) followed by @link
	 *             #savePatientProgram(PatientProgram)}
	 * @throws APIException
	 */
	@Authorized( { OpenmrsConstants.PRIV_ADD_PATIENT_PROGRAMS })
	public void enrollPatientInProgram(Patient patient, Program program, Date enrollmentDate, Date completionDate,
	                                   User creator) throws APIException;
	
	/**
	 * Returns a Collection<PatientProgram> of all PatientPrograms for the passed
	 * <code>patient</code>
	 * 
	 * @param patient - The Patient to retrieve all PatientPrograms for
	 * @return Collection<PatientProgram> of all PatientPrograms for the passed <code>patient</code>
	 * @deprecated use
	 *             {@link #getPatientPrograms(Patient, Program, Date, Date, Date, Date, boolean)}
	 * @throws APIException
	 */
	@Authorized( { OpenmrsConstants.PRIV_VIEW_PATIENT_PROGRAMS })
	@Transactional(readOnly = true)
	public Collection<PatientProgram> getPatientPrograms(Patient patient) throws APIException;
	
	/**
	 * Get Collection<Integer> of PatientIds for patients who are enrolled in program between
	 * fromDate and toDate
	 * 
	 * @param program - The Program to check for patient enrollment
	 * @param fromDate - Used to check whether patients were enrolled in the <code>program</code> on
	 *            or after this Date
	 * @param toDate - Used to check whether patients were enrolled in the <code>program</code> on
	 *            or before this Date
	 * @return Collection<Integer> containing all patientIds for patients who were enrolled in the
	 *         <code>program</code> between <code>fromDate</code> and <code>toDate</code>
	 * @deprecated use
	 *             {@link #getPatientPrograms(Patient, Program, Date, Date, Date, Date, boolean)}
	 *             which can be Iterated across to return collection of patient ids
	 * @throws APIException
	 */
	@Authorized( { OpenmrsConstants.PRIV_VIEW_PATIENT_PROGRAMS })
	@Transactional(readOnly = true)
	public Collection<Integer> patientsInProgram(Program program, Date fromDate, Date toDate) throws APIException;
	
	/**
	 * Get Collection of PatientPrograms for patients that are current as of the passed Date
	 * 
	 * @param patient - The Patient to check for program enrollment
	 * @param onDate - Specifies only to return programs that the patient is in as of this Date
	 * @return Collection<PatientProgram> that contains all PatientPrograms are current for the
	 *         <code>patient</code> as of <code>onDate</code>
	 * @deprecated use
	 *             {@link #getPatientPrograms(Patient, Program, Date, Date, Date, Date, boolean)}
	 * @throws APIException
	 */
	@Authorized( { OpenmrsConstants.PRIV_VIEW_PATIENT_PROGRAMS })
	@Transactional(readOnly = true)
	public Collection<PatientProgram> getCurrentPrograms(Patient patient, Date onDate) throws APIException;
	
	/**
	 * Return boolean indicating if Patient was enrolled into the Program between Date and Date
	 * 
	 * @param patient - The Patient to check for enrollment
	 * @param program - The Program to check for enrollment
	 * @param fromDate - Used to check whether patients were enrolled in the <code>program</code> on
	 *            or after this Date
	 * @param toDate - Used to check whether patients were enrolled in the <code>program</code> on
	 *            or before this Date
	 * @return boolean - Returns true if the <code>patient</code> was enrolled in the
	 *         <code>program</code> between <code>fromDate</code> and <code>toDate</code>
	 * @deprecated use
	 *             {@link #getPatientPrograms(Patient, Program, Date, Date, Date, Date, boolean)}
	 * @throws APIException
	 */
	@Authorized( { OpenmrsConstants.PRIV_VIEW_PATIENT_PROGRAMS })
	@Transactional(readOnly = true)
	public boolean isInProgram(Patient patient, Program program, Date fromDate, Date toDate) throws APIException;
	
	// **************************
	// DEPRECATED PATIENT STATE 
	// **************************
	
	/**
	 * Get a PatientState by patientStateId
	 * 
	 * @see PatientProgram
	 * @param patientStateId - The primary key id of the PatientState to return
	 * @return The PatientState whose primary key id matches the input <code>patientStateId</code>
	 * @deprecated use {@link PatientProgram#getPatientState(Integer)}
	 * @throws APIException
	 */
	@Authorized( { OpenmrsConstants.PRIV_VIEW_PATIENT_PROGRAMS })
	@Transactional(readOnly = true)
	public PatientState getPatientState(Integer patientStateId) throws APIException;
	
	/**
	 * Get the most recent PatientState for a given PatientProgram and ProgramWorkflow
	 * 
	 * @param patientProgram - The PatientProgram whose states to check
	 * @param programWorkflow - The ProgramWorkflow whose current state to check within the given
	 *            <code>patientProgram</code>
	 * @return PatientState - The PatientState that is most recent for the
	 *         <code>programWorkflow</code> within the given <code>patientProgram</code>
	 * @deprecated use {@link PatientProgram#getCurrentState(ProgramWorkflow)}
	 * @throws APIException
	 */
	@Authorized( { OpenmrsConstants.PRIV_VIEW_PATIENT_PROGRAMS })
	@Transactional(readOnly = true)
	public PatientState getLatestState(PatientProgram patientProgram, ProgramWorkflow programWorkflow) throws APIException;
	
	/**
	 * Returns a Set of current ProgramWorkflows for the given Patient
	 * 
	 * @param patient - The Patient to check
	 * @return Set<ProgramWorkflow> containing all of the current ProgramWorkflows for the
	 *         <code>patient</code>
	 * @deprecated No current use outside of this service. Should be retrieved from Patient,
	 *             PatientProgram, and PatientState
	 * @throws APIException
	 */
	@Authorized( { OpenmrsConstants.PRIV_VIEW_PATIENT_PROGRAMS })
	@Transactional(readOnly = true)
	public Set<ProgramWorkflow> getCurrentWorkflowsByPatient(Patient patient) throws APIException;
	
	/**
	 * Returns a Set of current ProgramWorkflows for the given PatientProgram
	 * 
	 * @param program - The PatientProgram to check
	 * @return Set<ProgramWorkflow> containing all of the current ProgramWorkflows for the
	 *         <code>program</code>
	 * @deprecated No current use outside of this service. Should be retrieved from Patient,
	 *             PatientProgram, and PatientState
	 * @throws APIException
	 */
	@Authorized( { OpenmrsConstants.PRIV_VIEW_PATIENT_PROGRAMS })
	@Transactional(readOnly = true)
	public Set<ProgramWorkflow> getCurrentWorkflowsByPatientProgram(PatientProgram program) throws APIException;
	
	/**
	 * Change the state of the passed PatientPrograms ProgramWorkflow to the passed
	 * ProgramWorkflowState on the passed Date
	 * 
	 * @param patientProgram - The PatientProgram whose state you wish to change
	 * @param workflow - The ProgramWorkflow whose within the <code>patientProgram</code> whose
	 *            state you wish to change
	 * @param state - The ProgramWorkflowState you wish to change the ProgramWorkflow to
	 * @param onDate - The Date that you wish the State change to take place
	 * @deprecated use {@link PatientProgram#transitionToState(ProgramWorkflowState, Date)}
	 * @throws APIException
	 */
	@Authorized( { OpenmrsConstants.PRIV_ADD_PATIENT_PROGRAMS, OpenmrsConstants.PRIV_EDIT_PATIENT_PROGRAMS })
	public void changeToState(PatientProgram patientProgram, ProgramWorkflow workflow, ProgramWorkflowState state,
	                          Date onDate) throws APIException;
	
	/**
	 * TODO: refactor?
	 * 
	 * @param cohort
	 * @param programs
	 * @return List<PatientProgram> for all Patients in the given Cohort that are in the given programs
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_PATIENT_PROGRAMS })
	public List<PatientProgram> getPatientPrograms(Cohort cohort, Collection<Program> programs);
	
	/**
	 * Terminatate the passed PatientPrograms ProgramWorkflow to the passed ProgramWorkflowState on
	 * the passed Date
	 * 
	 * @param patientProgram - The PatientProgram whose state you wish to change
	 * @param finalState - The ProgramWorkflowState you wish to change the ProgramWorkflow to
	 * @param terminatedOn - The Date that you wish the State change to take place
	 * @deprecated use {@link PatientProgram#transitionToState(ProgramWorkflowState, Date)}
	 * @throws APIException
	 */
	@Authorized( { OpenmrsConstants.PRIV_ADD_PATIENT_PROGRAMS, OpenmrsConstants.PRIV_EDIT_PATIENT_PROGRAMS })
	public void terminatePatientProgram(PatientProgram patientProgram, ProgramWorkflowState finalState, Date terminatedOn);
	
	/**
	 * Voids the last non-voided ProgramWorkflowState in the given ProgramWorkflow for the given
	 * PatientProgram, and clears the endDate of the next-to-last non-voided state.
	 * 
	 * @param patientProgram - The patientProgram to check
	 * @param wf - The ProgramWorkflow to check
	 * @param voidReason - The reason for voiding
	 * @deprecated use {@link PatientProgram#voidLastState(ProgramWorkflow, User, Date, String)}
	 * @throws APIException
	 */
	@Authorized( { OpenmrsConstants.PRIV_EDIT_PATIENT_PROGRAMS })
	public void voidLastState(PatientProgram patientProgram, ProgramWorkflow wf, String voidReason) throws APIException;
	
	// **************************
	// DEPRECATED CONCEPT STATE CONVERSION
	// **************************
	
	/**
	 * Create a new ConceptStateConversion
	 * 
	 * @param conceptStateConversion - The ConceptStateConversion to create
	 * @deprecated use {@link #saveConceptStateConversion(ConceptStateConversion)}
	 * @throws APIException
	 */
	@Authorized( { OpenmrsConstants.PRIV_ADD_PATIENT_PROGRAMS })
	public void createConceptStateConversion(ConceptStateConversion conceptStateConversion) throws APIException;
	
	/**
	 * Update a ConceptStateConversion
	 * 
	 * @param conceptStateConversion - The ConceptStateConversion to update
	 * @deprecated use {@link #saveConceptStateConversion(ConceptStateConversion)}
	 * @throws APIException
	 */
	@Authorized( { OpenmrsConstants.PRIV_EDIT_PATIENT_PROGRAMS })
	public void updateConceptStateConversion(ConceptStateConversion conceptStateConversion) throws APIException;
	
	/**
	 * Returns all conceptStateConversions, includes retired conceptStateConversions.
	 * 
	 * @return List<ConceptStateConversion> of all ConceptStateConversions that exist, including
	 *         retired
	 * @see #getAllConceptStateConversions()
	 * @deprecated use {@link #getAllConceptStateConversions()}
	 * @throws APIException
	 */
	@Authorized( { OpenmrsConstants.PRIV_VIEW_PROGRAMS })
	@Transactional(readOnly = true)
	public List<ConceptStateConversion> getAllConversions() throws APIException;
	
	/**
	 * Delete a ConceptStateConversion
	 * 
	 * @param csc - The ConceptStateConversion to delete from the database
	 * @deprecated use {@link #purgeConceptStateConversion(ConceptStateConversion)}
	 * @throws APIException
	 */
	public void deleteConceptStateConversion(ConceptStateConversion csc) throws APIException;
}

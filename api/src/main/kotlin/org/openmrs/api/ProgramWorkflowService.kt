/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api

import org.openmrs.Cohort
import org.openmrs.Concept
import org.openmrs.ConceptStateConversion
import org.openmrs.Patient
import org.openmrs.PatientProgram
import org.openmrs.PatientProgramAttribute
import org.openmrs.PatientState
import org.openmrs.Program
import org.openmrs.ProgramAttributeType
import org.openmrs.ProgramWorkflow
import org.openmrs.ProgramWorkflowState
import org.openmrs.annotation.Authorized
import org.openmrs.api.db.ProgramWorkflowDAO
import org.openmrs.util.PrivilegeConstants
import org.springframework.transaction.annotation.Transactional
import java.util.Date

/**
 * Contains methods pertaining to management of Programs, ProgramWorkflows, ProgramWorkflowStates,
 * PatientPrograms, PatientStates, and ConceptStateConversions.
 *
 * Use:
 * ```
 * val program = Program()
 * program.set___(___) // ... etc
 * Context.getProgramWorkflowService().saveProgram(program)
 * ```
 */
interface ProgramWorkflowService : OpenmrsService {

    /**
     * Setter for the ProgramWorkflow DataAccessObject (DAO). The DAO is used for saving and
     * retrieving from the database.
     *
     * @param dao - The DAO for this service
     */
    fun setProgramWorkflowDAO(dao: ProgramWorkflowDAO)

    // **************************
    // PROGRAM
    // **************************

    /**
     * Save program to database (create if new or update if changed).
     *
     * @param program is the Program to be saved to the database
     * @return The Program that was saved
     * @throws APIException if saving fails
     */
    @Authorized(PrivilegeConstants.MANAGE_PROGRAMS)
    @Throws(APIException::class)
    fun saveProgram(program: Program): Program

    /**
     * Returns a program given that programs primary key programId.
     *
     * @param programId integer primary key of the program to find
     * @return Program object that has program.programId = programId passed in
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.GET_PROGRAMS)
    @Throws(APIException::class)
    fun getProgram(programId: Int?): Program?

    /**
     * Returns a program given the program's exact name.
     *
     * @param name the exact name of the program to match on
     * @return Program matching the name to Program.name
     * @throws APIException if retrieval fails
     * @throws ProgramNameDuplicatedException when there are more than one program with the given name
     */
    @Authorized(PrivilegeConstants.GET_PROGRAMS)
    @Throws(APIException::class)
    fun getProgramByName(name: String): Program?

    /**
     * Returns all programs, includes retired programs.
     *
     * @return List of all existing programs, including retired programs
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.GET_PROGRAMS)
    @Throws(APIException::class)
    fun getAllPrograms(): List<Program>

    /**
     * Returns all programs.
     *
     * @param includeRetired whether or not to include retired programs
     * @return List of all existing programs, including retired based on the input parameter
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.GET_PROGRAMS)
    @Throws(APIException::class)
    fun getAllPrograms(includeRetired: Boolean): List<Program>

    /**
     * Returns programs that match the given string.
     *
     * @param nameFragment is the string used to search for programs
     * @return List of Programs whose name matches the input parameter
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.GET_PROGRAMS)
    @Throws(APIException::class)
    fun getPrograms(nameFragment: String): List<Program>

    /**
     * Completely remove a program from the database (not reversible).
     *
     * @param program the Program to clean out of the database.
     * @throws APIException if purging fails
     */
    @Authorized(PrivilegeConstants.MANAGE_PROGRAMS)
    @Throws(APIException::class)
    fun purgeProgram(program: Program)

    /**
     * Completely remove a program from the database (not reversible).
     *
     * @param program the Program to clean out of the database.
     * @param cascade true to delete related content
     * @throws APIException if purging fails
     */
    @Authorized(PrivilegeConstants.MANAGE_PROGRAMS)
    @Throws(APIException::class)
    fun purgeProgram(program: Program, cascade: Boolean)

    /**
     * Retires the given program.
     *
     * @param program Program to be retired
     * @param reason String for retiring the program
     * @return the Program which has been retired
     * @throws APIException if retiring fails
     */
    @Authorized(PrivilegeConstants.MANAGE_PROGRAMS)
    @Throws(APIException::class)
    fun retireProgram(program: Program, reason: String): Program

    /**
     * Unretires the given program.
     *
     * @param program Program to be unretired
     * @return the Program which has been unretired
     * @throws APIException if unretiring fails
     */
    @Authorized(PrivilegeConstants.MANAGE_PROGRAMS)
    @Throws(APIException::class)
    fun unretireProgram(program: Program): Program

    // **************************
    // PATIENT PROGRAM
    // **************************

    /**
     * Get a program by its uuid.
     *
     * @param uuid the universally unique identifier
     * @return the program which matches the given uuid
     */
    fun getProgramByUuid(uuid: String): Program?

    /**
     * Get a program state by its uuid.
     *
     * @param uuid the universally unique identifier
     * @return the program which matches the given uuid
     */
    fun getPatientStateByUuid(uuid: String): PatientState?

    /**
     * Save patientProgram to database (create if new or update if changed).
     *
     * @param patientProgram is the PatientProgram to be saved to the database
     * @return PatientProgram - the saved PatientProgram
     * @throws APIException if saving fails
     */
    @Authorized(PrivilegeConstants.ADD_PATIENT_PROGRAMS, PrivilegeConstants.EDIT_PATIENT_PROGRAMS)
    @Throws(APIException::class)
    fun savePatientProgram(patientProgram: PatientProgram): PatientProgram

    /**
     * Returns a PatientProgram given that PatientPrograms primary key patientProgramId.
     *
     * @param patientProgramId integer primary key of the PatientProgram to find
     * @return PatientProgram object that has patientProgram.patientProgramId = patientProgramId passed in
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.GET_PATIENT_PROGRAMS)
    @Throws(APIException::class)
    fun getPatientProgram(patientProgramId: Int?): PatientProgram?

    /**
     * Returns PatientPrograms that match the input parameters.
     *
     * @param patient if supplied all PatientPrograms returned will be for this Patient
     * @param program if supplied all PatientPrograms returned will be for this Program
     * @param minEnrollmentDate if supplied will limit PatientPrograms to those with enrollments on or after this Date
     * @param maxEnrollmentDate if supplied will limit PatientPrograms to those with enrollments on or before this Date
     * @param minCompletionDate if supplied will limit PatientPrograms to those completed on or after this Date OR not yet completed
     * @param maxCompletionDate if supplied will limit PatientPrograms to those completed on or before this Date
     * @param includeVoided if true, will also include voided PatientPrograms
     * @return List of PatientPrograms that match the passed input parameters
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.GET_PATIENT_PROGRAMS)
    @Throws(APIException::class)
    fun getPatientPrograms(
        patient: Patient?,
        program: Program?,
        minEnrollmentDate: Date?,
        maxEnrollmentDate: Date?,
        minCompletionDate: Date?,
        maxCompletionDate: Date?,
        includeVoided: Boolean
    ): List<PatientProgram>

    /**
     * Completely remove a patientProgram from the database (not reversible).
     *
     * @param patientProgram the PatientProgram to clean out of the database.
     * @throws APIException if purging fails
     */
    @Authorized(PrivilegeConstants.PURGE_PATIENT_PROGRAMS)
    @Throws(APIException::class)
    fun purgePatientProgram(patientProgram: PatientProgram)

    /**
     * Completely remove a patientProgram from the database (not reversible).
     *
     * @param patientProgram the PatientProgram to clean out of the database.
     * @param cascade true to delete related content
     * @throws APIException if purging fails
     */
    @Authorized(PrivilegeConstants.PURGE_PATIENT_PROGRAMS)
    @Throws(APIException::class)
    fun purgePatientProgram(patientProgram: PatientProgram, cascade: Boolean)

    /**
     * Voids the given patientProgram.
     *
     * @param patientProgram patientProgram to be voided
     * @param reason is the reason why the patientProgram is being voided
     * @return the voided PatientProgram
     * @throws APIException if voiding fails
     */
    @Authorized(PrivilegeConstants.DELETE_PATIENT_PROGRAMS)
    @Throws(APIException::class)
    fun voidPatientProgram(patientProgram: PatientProgram, reason: String): PatientProgram

    /**
     * Unvoids the given patientProgram.
     *
     * @param patientProgram patientProgram to be un-voided
     * @return the voided PatientProgram
     * @throws APIException if unvoiding fails
     */
    @Authorized(PrivilegeConstants.DELETE_PATIENT_PROGRAMS)
    @Throws(APIException::class)
    fun unvoidPatientProgram(patientProgram: PatientProgram): PatientProgram

    /**
     * Get all possible outcome concepts for a program.
     *
     * @param programId the program id
     * @return outcome concepts or empty List if none exist
     */
    @Authorized(PrivilegeConstants.GET_PROGRAMS)
    fun getPossibleOutcomes(programId: Int?): List<Concept>

    // **************************
    // CONCEPT STATE CONVERSION
    // **************************

    /**
     * Get ProgramWorkflow by internal identifier.
     *
     * @param workflowId the primary key of the workflow to find, null not ok
     * @return the program workflow matching given id or null if not found
     * @since 2.2.0
     */
    fun getWorkflow(workflowId: Int?): ProgramWorkflow?

    /**
     * Get ProgramWorkflow by its UUID.
     *
     * @param uuid the uuid
     * @return program work flow or null
     */
    fun getWorkflowByUuid(uuid: String): ProgramWorkflow?

    /**
     * Save ConceptStateConversion to database (create if new or update if changed).
     *
     * @param conceptStateConversion - The ConceptStateConversion to save
     * @return ConceptStateConversion - The saved ConceptStateConversion
     * @throws APIException if saving fails
     */
    @Authorized(PrivilegeConstants.ADD_PATIENT_PROGRAMS, PrivilegeConstants.EDIT_PATIENT_PROGRAMS)
    @Throws(APIException::class)
    fun saveConceptStateConversion(conceptStateConversion: ConceptStateConversion): ConceptStateConversion

    /**
     * Returns a conceptStateConversion given that conceptStateConversions primary key.
     *
     * @param conceptStateConversionId integer primary key of the conceptStateConversion to find
     * @return ConceptStateConversion object that has conceptStateConversion.conceptStateConversionId = conceptStateConversionId passed in
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.GET_PROGRAMS)
    @Throws(APIException::class)
    fun getConceptStateConversion(conceptStateConversionId: Int?): ConceptStateConversion?

    /**
     * Returns all conceptStateConversions.
     *
     * @return List of all ConceptStateConversions that exist
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.GET_PROGRAMS)
    @Throws(APIException::class)
    fun getAllConceptStateConversions(): List<ConceptStateConversion>

    /**
     * Completely remove a conceptStateConversion from the database (not reversible).
     *
     * @param conceptStateConversion the ConceptStateConversion to clean out of the database.
     * @throws APIException if purging fails
     */
    @Authorized(PrivilegeConstants.MANAGE_PROGRAMS)
    @Throws(APIException::class)
    fun purgeConceptStateConversion(conceptStateConversion: ConceptStateConversion)

    /**
     * Completely remove a conceptStateConversion from the database (not reversible).
     *
     * @param conceptStateConversion the ConceptStateConversion to clean out of the database.
     * @param cascade true to delete related content
     * @throws APIException if purging fails
     */
    @Authorized(PrivilegeConstants.MANAGE_PROGRAMS)
    @Throws(APIException::class)
    fun purgeConceptStateConversion(conceptStateConversion: ConceptStateConversion, cascade: Boolean)

    /**
     * Retrieves the ConceptStateConversion that matches the passed ProgramWorkflow and Concept.
     *
     * @param workflow - the ProgramWorkflow to check
     * @param trigger - the Concept to check
     * @return ConceptStateConversion that matches the passed ProgramWorkflow and Concept
     * @throws APIException if retrieval fails
     */
    @Throws(APIException::class)
    fun getConceptStateConversion(workflow: ProgramWorkflow, trigger: Concept): ConceptStateConversion?

    /**
     * Get ProgramWorkflowState by internal identifier.
     *
     * @param stateId the primary key of the state to find, null not ok
     * @return the program workflow state matching given id or null if not found
     * @since 2.2.0
     */
    fun getState(stateId: Int?): ProgramWorkflowState?

    /**
     * Get a state by its uuid.
     *
     * @param uuid the universally unique identifier
     * @return the program workflow state which matches the given uuid
     */
    fun getStateByUuid(uuid: String): ProgramWorkflowState?

    /**
     * Get a patient program by its uuid.
     *
     * @param uuid the universally unique identifier
     * @return the patient program which matches the given uuid
     */
    fun getPatientProgramByUuid(uuid: String): PatientProgram?

    /**
     * Get PatientPrograms for all Patients in the given Cohort that are in the given programs.
     *
     * @param cohort the cohort
     * @param programs the programs
     * @return List of PatientPrograms
     */
    @Authorized(PrivilegeConstants.GET_PATIENT_PROGRAMS)
    fun getPatientPrograms(cohort: Cohort?, programs: @JvmSuppressWildcards Collection<Program>?): List<PatientProgram>

    /**
     * Returns a list of Programs that are using a particular concept.
     *
     * @param concept - The Concept being used.
     * @return - A List of Programs
     */
    @Authorized(PrivilegeConstants.GET_PATIENT_PROGRAMS)
    fun getProgramsByConcept(concept: Concept): List<Program>

    /**
     * Returns a list of ProgramWorkflows that are using a particular concept.
     *
     * @param concept - The Concept being used.
     * @return - A List of ProgramWorkflows
     */
    @Authorized(PrivilegeConstants.GET_PATIENT_PROGRAMS)
    fun getProgramWorkflowsByConcept(concept: Concept): List<ProgramWorkflow>

    /**
     * Returns a list of ProgramWorkflowStates that are using a particular concept.
     *
     * @param concept - The Concept being used.
     * @return - A List of ProgramWorkflowStates
     */
    @Authorized(PrivilegeConstants.GET_PATIENT_PROGRAMS)
    fun getProgramWorkflowStatesByConcept(concept: Concept): List<ProgramWorkflowState>

    /**
     * Get a concept state conversion by its uuid.
     *
     * @param uuid the universally unique identifier
     * @return the concept state conversion which matches the given uuid
     */
    fun getConceptStateConversionByUuid(uuid: String): ConceptStateConversion?

    @Transactional(readOnly = true)
    @Authorized("Get Patient Program Attribute Types")
    fun getAllProgramAttributeTypes(): List<ProgramAttributeType>

    @Transactional(readOnly = true)
    @Authorized("Get Patient Program Attribute Types")
    fun getProgramAttributeType(id: Int?): ProgramAttributeType?

    @Transactional(readOnly = true)
    @Authorized("Get Patient Program Attribute Types")
    fun getProgramAttributeTypeByUuid(uuid: String): ProgramAttributeType?

    @Authorized("Manage Patient Program Attribute Types")
    fun saveProgramAttributeType(programAttributeType: ProgramAttributeType): ProgramAttributeType

    @Authorized("Purge Patient Program Attribute Types")
    fun purgeProgramAttributeType(programAttributeType: ProgramAttributeType)

    @Transactional(readOnly = true)
    @Authorized(PrivilegeConstants.GET_PATIENT_PROGRAMS)
    fun getPatientProgramAttributeByUuid(uuid: String): PatientProgramAttribute?

    fun getPatientProgramAttributeByAttributeName(patients: @JvmSuppressWildcards List<Int>, attributeName: String): Map<Any, Any>

    @Transactional(readOnly = true)
    @Authorized(PrivilegeConstants.GET_PATIENT_PROGRAMS)
    fun getPatientProgramByAttributeNameAndValue(attributeName: String, attributeValue: String): List<PatientProgram>
}

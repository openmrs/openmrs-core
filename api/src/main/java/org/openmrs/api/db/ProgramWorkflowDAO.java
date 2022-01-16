/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.db;

import org.openmrs.ProgramAttributeType;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.openmrs.Cohort;
import org.openmrs.Concept;
import org.openmrs.ConceptStateConversion;
import org.openmrs.Patient;
import org.openmrs.PatientProgram;
import org.openmrs.PatientProgramAttribute;
import org.openmrs.PatientState;
import org.openmrs.Program;
import org.openmrs.ProgramWorkflow;
import org.openmrs.ProgramWorkflowState;

/**
 * Program- and PatientProgram- and ConceptStateConversion-related database functions
 * 
 * @version 1.0
 */
public interface ProgramWorkflowDAO {
	
	// **************************
	// PROGRAM
	// **************************
	
	/**
	 * Saves a Program to the database
	 * 
	 * @param program - The {@link Program} to save
	 * @return The saved {@link Program}
	 * @throws DAOException
	 */
	public Program saveProgram(Program program) throws DAOException;
	
	/**
	 * Retrieves a {@link Program} from the database by primary key programId
	 * 
	 * @param programId - The primary key programId to use to retrieve a {@link Program}
	 * @return Program - The {@link Program} matching the passed programId
	 * @throws DAOException
	 */
	public Program getProgram(Integer programId) throws DAOException;
	
	/**
	 * Returns all programs
	 * 
	 * @param includeRetired whether or not to include retired programs
	 * @return List&lt;Program&gt; all existing programs, including retired based on the input parameter
	 * @throws DAOException
	 */
	public List<Program> getAllPrograms(boolean includeRetired) throws DAOException;
	
	/**
	 * Returns programs that match the given string. A null list will never be returned. An empty
	 * list will be returned if there are no programs matching this <code>nameFragment</code>
	 * 
	 * @param nameFragment is the string used to search for programs
	 * @return List&lt;Program&gt; - list of Programs whose name matches the input parameter
	 * @throws DAOException
	 */
	public List<Program> findPrograms(String nameFragment) throws DAOException;
	
	/**
	 * Completely remove a program from the database (not reversible) This method delegates to
	 * #purgeProgram(program, boolean) method
	 * 
	 * @param program the Program to clean out of the database.
	 * @throws DAOException
	 */
	public void deleteProgram(Program program) throws DAOException;
	
	// **************************
	// PATIENT PROGRAM
	// **************************
	
	/**
	 * Save patientProgram to database (create if new or update if changed)
	 * 
	 * @param patientProgram is the PatientProgram to be saved to the database
	 * @return PatientProgram - the saved PatientProgram
	 * @throws DAOException
	 */
	public PatientProgram savePatientProgram(PatientProgram patientProgram) throws DAOException;
	
	/**
	 * Returns a PatientProgram given that PatientPrograms primary key <code>patientProgramId</code>
	 * A null value is returned if no PatientProgram exists with this patientProgramId.
	 * 
	 * @param id integer primary key of the PatientProgram to find
	 * @return PatientProgram object that has patientProgram.patientProgramId =
	 *         <code>patientProgramId</code> passed in.
	 * @throws DAOException
	 */
	public PatientProgram getPatientProgram(Integer id);
	
	public List<PatientProgram> getPatientPrograms(Cohort cohort, Collection<Program> programs);
	
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
	 * @param includeVoided - boolean, if true will return voided PatientPrograms as well. If false,
	 *            will not return voided PatientPrograms
	 * @return List&lt;PatientProgram&gt; of PatientPrograms that match the passed input parameters
	 * @throws DAOException
	 */
	public List<PatientProgram> getPatientPrograms(Patient patient, Program program, Date minEnrollmentDate,
	        Date maxEnrollmentDate, Date minCompletionDate, Date maxCompletionDate, boolean includeVoided)
	        throws DAOException;
	
	/**
	 * Completely remove a patientProgram from the database (not reversible) This method delegates
	 * to #purgePatientProgram(patientProgram, boolean) method
	 * 
	 * @param patientProgram the PatientProgram to clean out of the database.
	 * @throws DAOException
	 */
	public void deletePatientProgram(PatientProgram patientProgram) throws DAOException;
	
	// **************************
	// CONCEPT STATE CONVERSION
	// **************************
	
	/**
	 * Save ConceptStateConversion to database (create if new or update if changed)
	 * 
	 * @param csc The ConceptStateConversion to save
	 * @return The saved ConceptStateConversion
	 * @throws DAOException
	 */
	public ConceptStateConversion saveConceptStateConversion(ConceptStateConversion csc) throws DAOException;
	
	/**
	 * Returns all conceptStateConversions
	 * 
	 * @return List&lt;ConceptStateConversion&gt; of all ConceptStateConversions that exist
	 * @throws DAOException
	 */
	public List<ConceptStateConversion> getAllConceptStateConversions() throws DAOException;
	
	/**
	 * Returns a conceptStateConversion given that conceptStateConversions primary key
	 * <code>conceptStateConversionId</code> A null value is returned if no conceptStateConversion
	 * exists with this conceptStateConversionId.
	 * 
	 * @param id integer primary key of the conceptStateConversion to find
	 * @return ConceptStateConversion object that has
	 *         conceptStateConversion.conceptStateConversionId =
	 *         <code>conceptStateConversionId</code> passed in.
	 * @throws DAOException
	 */
	public ConceptStateConversion getConceptStateConversion(Integer id);
	
	/**
	 * Completely remove a conceptStateConversion from the database (not reversible)
	 * 
	 * @param csc the ConceptStateConversion to clean out of the database.
	 * @throws DAOException
	 */
	public void deleteConceptStateConversion(ConceptStateConversion csc);
	
	/**
	 * Retrieves the ConceptStateConversion that matches the passed <code>ProgramWorkflow</code> and
	 * <code>Concept</code>
	 * 
	 * @param workflow the ProgramWorkflow to check
	 * @param trigger the Concept to check
	 * @return ConceptStateConversion that matches the passed <code>ProgramWorkflow</code> and
	 *         <code>Concept</code>
	 * @throws DAOException
	 */
	public ConceptStateConversion getConceptStateConversion(ProgramWorkflow workflow, Concept trigger);
	
	/**
	 * @param uuid
	 * @return concept state conversion or null
	 */
	public ConceptStateConversion getConceptStateConversionByUuid(String uuid);
	
	/**
	 * @param uuid
	 * @return patient program or null
	 */
	public PatientProgram getPatientProgramByUuid(String uuid);
	
	/**
	 * @param uuid
	 * @return program or null
	 */
	public Program getProgramByUuid(String uuid);
	
	/**
	 * Retrieves the Programs from the dB which have the given name.
	 * @param name the name of the Programs to retrieve.
	 * @param includeRetired whether to include retired programs or not
	 * <strong>Should</strong> return an empty list when there is no program in the dB with given name
	 * <strong>Should</strong> return only and exactly the programs with the given name
	 * @return all Programs with the given name.
	 */
	public List<Program> getProgramsByName(String name, boolean includeRetired);
	
	/**
	 * Retrieves a {@code ProgramWorkflowState} from the database by its primary key.
	 * 
	 * @param stateId the primary key used to retrieve program workflow state
	 * @return the program workflow state matching given id or null if not found
	 * @since 2.2.0
	 */
	public ProgramWorkflowState getState(Integer stateId);
	
	/**
	 * @param uuid
	 * @return program workflow state or null
	 */
	public ProgramWorkflowState getStateByUuid(String uuid);
	
	public PatientState getPatientStateByUuid(String uuid);
	
	/**
	 * Retrieves a {@code ProgramWorkflow} from the database by its primary key.
	 * 
	 * @param workflowId the primary key used to retrieve program workflow
	 * @return the program workflow matching given id or null if not found
	 * @since 2.2.0
	 */
	public ProgramWorkflow getWorkflow(Integer workflowId);
	
	/**
	 * @param uuid
	 * @return program workflow or null
	 */
	public ProgramWorkflow getWorkflowByUuid(String uuid);
	
	/**
	 * Returns a list of Programs that are using a particular concept.
	 * 
	 * @param concept - The Concept being used.
	 * @return - A List of Programs
	 */
	public List<Program> getProgramsByConcept(Concept concept);
	
	/**
	 * Returns a list of ProgramWorkflows that are using a particular concept.
	 * 
	 * @param concept - The Concept being used.
	 * @return - A List of ProgramWorkflows
	 */
	public List<ProgramWorkflow> getProgramWorkflowsByConcept(Concept concept);
	
	/**
	 * Returns a list of ProgramWorkflowStates that are using a particular concept.
	 * 
	 * @param concept - The Concept being used.
	 * @return - A List of ProgramWorkflowStates
	 */
	public List<ProgramWorkflowState> getProgramWorkflowStatesByConcept(Concept concept);
        public List<ProgramAttributeType> getAllProgramAttributeTypes();

        public ProgramAttributeType getProgramAttributeType(Integer var1);

        public ProgramAttributeType getProgramAttributeTypeByUuid(String var1);

        public ProgramAttributeType saveProgramAttributeType(ProgramAttributeType var1);

        public PatientProgramAttribute getPatientProgramAttributeByUuid(String var1);

        public void purgeProgramAttributeType(ProgramAttributeType var1);

        public List<PatientProgram> getPatientProgramByAttributeNameAndValue(String attributeName, String attributeValue);

        public Map<Object, Object> getPatientProgramAttributeByAttributeName(List<Integer> patientIds, String attributeName);
}

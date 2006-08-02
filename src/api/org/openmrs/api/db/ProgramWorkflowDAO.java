package org.openmrs.api.db;

import java.util.Collection;
import java.util.List;

import org.openmrs.Patient;
import org.openmrs.PatientProgram;
import org.openmrs.Program;
import org.openmrs.ProgramWorkflow;

/**
 * Program- and Workflow-related database functions
 *  
 * @version 1.0
 */
public interface ProgramWorkflowDAO {

	public void createProgram(Program program) throws DAOException;

	public List<Program> getPrograms() throws DAOException;
	
	public Program getProgram(Integer id) throws DAOException;

	public ProgramWorkflow getProgramWorkflowByConceptId(Integer id);

	public void createPatientProgram(PatientProgram p);

	public void updatePatientProgram(PatientProgram p);

	public PatientProgram getPatientProgram(Integer id);

	public Collection<PatientProgram> getPatientPrograms(Patient patient);
	
}

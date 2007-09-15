package org.openmrs.api.db;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.openmrs.Concept;
import org.openmrs.ConceptStateConversion;
import org.openmrs.Patient;
import org.openmrs.PatientProgram;
import org.openmrs.PatientState;
import org.openmrs.Program;
import org.openmrs.ProgramWorkflow;
import org.openmrs.ProgramWorkflowState;

/**
 * Program- and Workflow-related database functions
 *  
 * @version 1.0
 */
public interface ProgramWorkflowDAO {

	public void createOrUpdateProgram(Program program) throws DAOException;

	public List<Program> getPrograms() throws DAOException;
	
	public Program getProgram(Integer id) throws DAOException;

	public void createPatientProgram(PatientProgram p);

	public void updatePatientProgram(PatientProgram p);

	public PatientProgram getPatientProgram(Integer id);

	public PatientState getPatientState(Integer id);

	public Collection<PatientProgram> getPatientPrograms(Patient patient);

	public ProgramWorkflow findWorkflowByProgramAndConcept(Integer programId, Integer conceptId);
	
	public ProgramWorkflow getWorkflow(Integer id);
	
	public void createWorkflow(ProgramWorkflow w);

	public void createState(ProgramWorkflowState s);

	public void createPatientState(PatientState s);

	public void updateState(ProgramWorkflowState s);

	public void updatePatientState(PatientState s);

	public void updateWorkflow(ProgramWorkflow w);

	public List<ProgramWorkflowState> getStates(boolean includeVoided);
	
	public ProgramWorkflowState getState(Integer id);

	public Collection<Integer> patientsInProgram(Program program, Date fromDate, Date toDate);
	
	public void createConceptStateConversion(ConceptStateConversion csc);

	public void updateConceptStateConversion(ConceptStateConversion csc);

	public void deleteConceptStateConversion(ConceptStateConversion csc);

	public ConceptStateConversion getConceptStateConversion(Integer id);

	public List<ConceptStateConversion> getAllConversions();

	public ConceptStateConversion getConceptStateConversion(ProgramWorkflow workflow, Concept trigger);

	/**
     * Auto generated method comment
     * 
     * @param guid
     * @return
     */
    public ConceptStateConversion getConceptStateConversionByGuid(String guid);

	/**
     * Auto generated method comment
     * 
     * @param guid
     * @return
     */
    public PatientProgram getPatientProgramByGuid(String guid);

	/**
     * Auto generated method comment
     * 
     * @param guid
     * @return
     */
    public Program getProgramByGuid(String guid);

	/**
     * Auto generated method comment
     * 
     * @param guid
     * @return
     */
    public ProgramWorkflowState getStateByGuid(String guid);

    public PatientState getPatientStateByGuid(String guid);

	/**
     * Auto generated method comment
     * 
     * @param guid
     * @return
     */
    public ProgramWorkflow getWorkflowByGuid(String guid);
}

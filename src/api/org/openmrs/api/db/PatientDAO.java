package org.openmrs.api.db;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.Tribe;
import org.openmrs.api.APIException;

/**
 * Patient-related database functions
 *
 * @version 1.0
 */
public interface PatientDAO {

	/**
	 * Creates a new patient record
	 * 
	 * @param patient to be created
	 * @returns the created patient
	 * @throws DAOException
	 */
	public Patient createPatient(Patient patient) throws DAOException;

	/**
	 * Get patient by internal identifier
	 * 
	 * @param patientId internal patient identifier
	 * @return patient with given internal identifier
	 * @throws DAOException
	 */
	public Patient getPatient(Integer patientId) throws DAOException;

	/**
	 * Update patient 
	 * 
	 * @param patient to be updated
	 * @returns the crated patient
	 * @throws DAOException
	 */
	public Patient updatePatient(Patient patient) throws DAOException;

	/**
	 * Find all patients with a given identifier
	 * 
	 * @param identifier
	 * @return set of patients matching identifier
	 * @throws DAOException
	 */
	public Set<Patient> getPatientsByIdentifier(String identifier, boolean includeVoided) throws DAOException;
	
	/**
	 * Find all patients with a given identifier and use the regex 
	 * <code>OpenmrsConstants.PATIENT_IDENTIFIER_REGEX</code>
	 * 
	 * Note: Uses NON-STANDARD SQL: "...WHERE identifier REGEXP '...' ..."
	 * 
	 * @param identifier
	 * @param includeVoided
	 * @return
	 * @throws DAOException
	 */
	public Collection<Patient> getPatientsByIdentifierPattern(String identifier, boolean includeVoided) throws DAOException;
	
	/**
	 * Find patients by name
	 * 
	 * @param name
	 * @return set of patients matching name
	 * @throws DAOException
	 */
	public Collection<Patient> getPatientsByName(String name, boolean includeVoided) throws DAOException;
	
	/**
	 * Delete patient from database. This <b>should not be called</b>
	 * except for testing and administration purposes.  Use the void
	 * method instead.
	 * 
	 * @param patient patient to be deleted
	 * 
	 * @see #voidPatient(Patient, String) 
	 */
	public void deletePatient(Patient patient) throws DAOException;
	
	/**
	 * Get all patientIdentifiers
	 * 
	 * @param PatientIdentifierType
	 * @return patientIdentifier list
	 * @throws DAOException
	 */
	public List<PatientIdentifier> getPatientIdentifiers(PatientIdentifierType p) throws DAOException;
	
	/**
	 * Get Patient Identifiers matching the identifier and type 
	 * 
	 * @param identifier
	 * @param PatientIdentifierType
	 * @return patientIdentifier list
	 * @throws DAOException
	 */
	public List<PatientIdentifier> getPatientIdentifiers(String identifier, PatientIdentifierType p) throws DAOException;
	
	
	/**
	 * 
	 * @param pi
	 * @throws APIException
	 */
	public void updatePatientIdentifier(PatientIdentifier pi) throws APIException;
	
	
	/**
	 * Get all patientIdentifier types
	 * 
	 * @return patientIdentifier types list
	 * @throws DAOException
	 */
	public List<PatientIdentifierType> getPatientIdentifierTypes() throws DAOException;

	/**
	 * Get patientIdentifierType by internal identifier
	 * 
	 * @param patientIdentifierType id
	 * @return patientIdentifierType with given internal identifier
	 * @throws DAOException
	 */
	public PatientIdentifierType getPatientIdentifierType(Integer patientIdentifierTypeId) throws DAOException;

	/**
	 * Get patientIdentifierType by name
	 * 
	 * @param name
	 * @return patientIdentifierType with given name
	 * @throws APIException
	 */
	public PatientIdentifierType getPatientIdentifierType(String name) throws DAOException;
	
	/**
	 * Get tribe by internal tribe identifier
	 * 
	 * @return Tribe
	 * @param tribeId 
	 * @throws DAOException
	 */
	public Tribe getTribe(Integer tribeId) throws DAOException;
	
	/**
	 * Get list of tribes that are not retired
	 * 
	 * @return non-retired Tribe list
	 * @throws DAOException
	 */
	public List<Tribe> getTribes() throws DAOException;
	
	/**
	 * Get tribes by partial name lookup
	 * 
	 * @param Search string
	 * @return non-retired Tribe list
	 * @throws DAOException
	 */
	public List<Tribe> findTribes(String s) throws DAOException;
	
	/**
	 * Search the database for patients that share the given attributes
	 * attributes similar to: [gender, tribe, givenName, middleName, familyname]
	 * 
	 * @param attributes
	 * @return list of patients that match other patients
	 */
	public List<Patient> findDuplicatePatients(Set<String> attributes);
	
}

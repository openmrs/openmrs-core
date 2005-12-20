package org.openmrs.api.db;

import java.util.List;
import java.util.Set;

import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifierType;
import org.openmrs.Tribe;

/**
 * Patient-related database functions
 * 
 * @author Ben Wolfe
 * @author Burke Mamlin
 * @version 1.0
 */
public interface PatientDAO {

	/**
	 * Creates a new patient record
	 * 
	 * @param patient to be created
	 * @throws DAOException
	 */
	public void createPatient(Patient patient) throws DAOException;

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
	 * @throws DAOException
	 */
	public void updatePatient(Patient patient) throws DAOException;

	/**
	 * Find all patients with a given identifier
	 * 
	 * @param identifier
	 * @return set of patients matching identifier
	 * @throws DAOException
	 */
	public Set<Patient> getPatientsByIdentifier(String identifier, boolean includeVoided) throws DAOException;
	
	/**
	 * Find patients by name
	 * 
	 * @param name
	 * @return set of patients matching name
	 * @throws DAOException
	 */
	public Set<Patient> getPatientsByName(String name, boolean includeVoided) throws DAOException;
	
	/**
	 * Void patient record (functionally delete patient from system)
	 * 
	 * @param patient patient to be voided
	 * @param reason reason for voiding patient
	 */
	public void voidPatient(Patient patient, String reason) throws DAOException;

	/**
	 * Unvoid patient record 
	 * 
	 * @param patient patient to be revived
	 */
	public void unvoidPatient(Patient patient) throws DAOException;
	
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
	 * Get all locations
	 * 
	 * @return location list
	 * @throws DAOException
	 */
	public List<Location> getLocations() throws DAOException;

	/**
	 * Get location by internal identifier
	 * 
	 * @param location id
	 * @return location with given internal identifier
	 * @throws DAOException
	 */
	public Location getLocation(Integer locationId) throws DAOException;
	
}

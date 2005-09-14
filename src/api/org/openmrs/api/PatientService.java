package org.openmrs.api;

import java.util.List;

import org.openmrs.Patient;

public interface PatientService {

	/**
	 * Creates a new patient record
	 * 
	 * @param patient to be created
	 * @return newly created patient
	 * @throws APIException
	 */
	public Patient createPatient(Patient patient) throws APIException;

	/**
	 * Get patient by internal identifier
	 * 
	 * @param patientId internal patient identifier
	 * @return patient with given internal identifier
	 * @throws APIException
	 */
	public Patient getPatient(Integer patientId) throws APIException;

	/**
	 * Find all patients with a given identifier
	 * 
	 * @param identifier
	 * @return list of patients matching identifier
	 * @throws APIException
	 */
	public List getPatientByIdentifier(String identifier) throws APIException;
		
	/**
	 * Void patient record (functionally delete patient from system)
	 * 
	 * @param patient patient to be voided
	 * @param reason reason for voiding patient
	 */
	public void voidPatient(Patient patient, String reason) throws APIException;
	
	/**
	 * Delete patient from database. This <b>should not be called</b>
	 * except for testing and administration purposes.  Use the void
	 * method instead.
	 * 
	 * @param patient patient to be deleted
	 * 
	 * @see #voidPatient(Patient, String) 
	 */
	public void deletePatient(Patient patient) throws APIException;
	
}

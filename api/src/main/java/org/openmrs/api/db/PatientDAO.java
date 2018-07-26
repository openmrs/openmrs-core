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

import java.util.List;

import org.openmrs.Allergies;
import org.openmrs.Allergy;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.api.PatientService;

/**
 * Database methods for the PatientService
 * 
 * @see org.openmrs.api.context.Context
 * @see org.openmrs.api.PatientService
 */
public interface PatientDAO {
	
	/**
	 * @see org.openmrs.api.PatientService#savePatient(org.openmrs.Patient)
	 */
	public Patient savePatient(Patient patient) throws DAOException;
	
	/**
	 * @see org.openmrs.api.PatientService#getPatient(Integer)
	 */
	public Patient getPatient(Integer patientId) throws DAOException;
	
	/**
	 * Delete patient from database. This <b>should not be called</b> except for testing and
	 * administration purposes. Use the void method instead
	 * 
	 * @param patient patient to be deleted
	 * @see org.openmrs.api.PatientService#deletePatient(org.openmrs.Patient)
	 * @see org.openmrs.api.PatientService#voidPatient(Patient, String)
	 */
	public void deletePatient(Patient patient) throws DAOException;
	
	/**
	 * @see org.openmrs.api.PatientService#getAllPatients(boolean)
	 */
	public List<Patient> getAllPatients(boolean includeVoided) throws DAOException;
	
	/**
	 * @see org.openmrs.api.PatientService#getPatients(String, Integer, Integer)
	 *
	 * @should not get patients by empty query _ signature no 2
	 * @should not get patients by null query _ signature no 2
	 *
	 * @should get patient by given name _ signature no 2
	 * @should get patient by middle name _ signature no 2
	 * @should get patient by family name _ signature no 2
	 * @should get patient by family2 name _ signature no 2
	 * @should get patient by whole name _ signature no 2
	 *
	 * @should not get patient by non-existing single name _ signature no 2
	 * @should not get patient by non-existing name parts _ signature no 2
	 * @should not get patient by mix of existing and non-existing name parts _ signature no 2
	 * @should not get patient by voided name _ signature no 2
	 *
	 * @should get patient by short given name _ signature no 2
	 * @should get patient by short middle name _ signature no 2
	 * @should get patient by short family name _ signature no 2
	 * @should get patient by short family2 name _ signature no 2
	 * @should get patient by whole name made up of short names _ signature no 2
	 * @should get patients by multiple short name parts _ signature no 2
	 *
	 * @should not get patient by non-existing single short name _ signature no 2
	 * @should not get patient by non-existing short name parts _ signature no 2
	 * @should not get patient by mix of existing and non-existing short name parts _ signature no 2
	 * @should not get patient by voided short name _ signature no 2
	 *
	 * @should get patient by identifier _ signature no 2
	 * @should not get patient by non-existing identifier _ signature no 2
	 * @should not get patient by voided identifier _ signature no 2
	 *
	 * @should get no patient by non-existing attribute _ signature no 2
	 * @should get no patient by non-searchable attribute _ signature no 2
	 * @should get no patient by voided attribute _ signature no 2
	 * @should get one patient by attribute _ signature no 2
	 * @should get one patient by random case attribute _ signature no 2
	 * @should not get patients by searching for non-voided and voided attribute _ signature no 2
	 * @should get multiple patients by single attribute _ signature no 2
	 * @should not get patients by multiple attributes _ signature no 2
	 *
	 * @should find eleven out of eleven patients _ signature no 2
	 * @should find the first four out of eleven patients _ signature no 2
	 * @should find the next four out of eleven patients _ signature no 2
	 * @should find the remaining three out of eleven patients _ signature no 2

	 * @should find patients with null as start _ signature no 2
	 * @should find patients with negative start _ signature no 2
	 * @should find patients with null as length _ signature no 2
	 * @should not get patients by zero length _ signature no 2
	 * @should not get patients by negative length _ signature no 2
	 * @should find patients with excessive length _ signature no 2
	 *
	 * @should return distinct patient list _ signature no 2
	 * @should not match voided patients _ signature no 2
	 *
	 * @should get patients with match mode start _ signature no 2
	 * @should get patients with match mode anywhere _ signature no 2
	 * @should not get patients with match mode start _ signature no 2
	 * @should not get patients with match mode anywhere _ signature no 2
	 *
	 */
	public List<Patient> getPatients(String query, Integer start, Integer length) throws DAOException;
	
	/**
	 * @see org.openmrs.api.PatientService#getPatients(String, boolean, Integer, Integer)
	 * @should get voided person when voided true is passed
	 * @should get no voided person when voided false is passed
	 */
	public List<Patient> getPatients(String query, boolean includeVoided, Integer start, Integer length) throws DAOException;
	
	/**
	 * @see PatientService#getPatients(String, String, List, boolean, Integer, Integer)
	 */
	public List<Patient> getPatients(String name, List<PatientIdentifierType> identifierTypes,
		boolean matchIdentifierExactly, Integer start, Integer length) throws DAOException;
	
	
	
	/**
	 * @see org.openmrs.api.PatientService#getPatientIdentifiers(java.lang.String, java.util.List,
	 *      java.util.List, java.util.List, java.lang.Boolean)
	 *      
	 * @should return all matching non voided patient identifiers if is preferred is set to null
	 * @should return all matching non voided patient identifiers if is preferred is set to true
	 * @should return all matching non voided patient identifiers if is preferred is set to false
	 * @should fetch all patient identifiers belong to given patient
	 * @should fetch all patient identifiers belong to given patients
	 * @should fetch patient identifiers that equals given identifier
	 * @should not fetch patient identifiers that partially matches given identifier  
	 * @should not get voided patient identifiers 
	 */
	public List<PatientIdentifier> getPatientIdentifiers(String identifier,
	        List<PatientIdentifierType> patientIdentifierTypes, List<Location> locations, List<Patient> patients,
	        Boolean isPreferred) throws DAOException;
	
	/**
	 * @see org.openmrs.api.PatientService#savePatientIdentifierType(org.openmrs.PatientIdentifierType)
	 */
	public PatientIdentifierType savePatientIdentifierType(PatientIdentifierType patientIdentifierType) throws DAOException;
	
	/**
	 * @should not return null when includeRetired is false
	 * @should not return retired when includeRetired is false
	 * @should not return null when includeRetired is true
	 * @should return all when includeRetired is true
	 * @see org.openmrs.api.PatientService#getAllPatientIdentifierTypes(boolean)
	 */
	public List<PatientIdentifierType> getAllPatientIdentifierTypes(boolean includeRetired) throws DAOException;
	
	/**
	 * @see org.openmrs.api.PatientService#getPatientIdentifierTypes(java.lang.String,
	 *      java.lang.String, java.lang.Boolean, java.lang.Boolean)
	 */
	public List<PatientIdentifierType> getPatientIdentifierTypes(String name, String format, Boolean required,
	        Boolean hasCheckDigit) throws DAOException;
	
	/**
	 * @see org.openmrs.api.PatientService#getPatientIdentifierType(java.lang.Integer)
	 */
	public PatientIdentifierType getPatientIdentifierType(Integer patientIdentifierTypeId) throws DAOException;
	
	/**
	 * @see org.openmrs.api.PatientService#purgePatientIdentifierType(org.openmrs.PatientIdentifierType)
	 */
	public void deletePatientIdentifierType(PatientIdentifierType patientIdentifierType) throws DAOException;
	
	/**
	 * @see org.openmrs.api.PatientService#getDuplicatePatientsByAttributes(java.util.List)
	 */
	public List<Patient> getDuplicatePatientsByAttributes(List<String> attributes) throws DAOException;
	
	/**
	 * @see org.openmrs.api.PatientService#isIdentifierInUseByAnotherPatient(PatientIdentifier)
	 */
	public boolean isIdentifierInUseByAnotherPatient(PatientIdentifier patientIdentifier);
	
	/**
	 * @param uuid
	 * @return patient or null
	 */
	public Patient getPatientByUuid(String uuid);
	
	public PatientIdentifier getPatientIdentifierByUuid(String uuid);
	
	/**
	 * @param uuid
	 * @return patient identifier type or null
	 */
	public PatientIdentifierType getPatientIdentifierTypeByUuid(String uuid);
	
	/**
	 * @see org.openmrs.api.PatientService#getPatientIdentifier(java.lang.Integer)
	 */
	public PatientIdentifier getPatientIdentifier(Integer patientIdentifierId) throws DAOException;
	
	/**
	 * @see org.openmrs.api.PatientService#savePatientIdentifier(org.openmrs.PatientIdentifier)
	 */
	public PatientIdentifier savePatientIdentifier(PatientIdentifier patientIdentifier);
	
	/**
	 * @see org.openmrs.api.PatientService#purgePatientIdentifier(org.openmrs.PatientIdentifier)
	 */
	public void deletePatientIdentifier(PatientIdentifier patientIdentifier) throws DAOException;
	
	/**
	 * @see org.openmrs.api.PatientService#getCountOfPatients(String)
	 *
	 * @should count zero patients when query is empty _ signature no 2
	 * @should count zero patients when query is null _ signature no 2
	 * @should count zero patients for non-matching query _ signature no 2

	 * @should not count voided patients _ signature no 2
	 * @should count single patient _ signature no 2
	 * @should count multiple patients _ signature no 2
	 *
	 * @should count patients by name _ signature no 2
	 * @should count patients by identifier _ signature no 2
	 * @should count patients by searchable attribute _ signature no 2
	 * @should obey attribute match mode
	 */
	public Long getCountOfPatients(String query);
	
	/**
	 * @see org.openmrs.api.PatientService#getCountOfPatients(String, boolean)
	 */
	public Long getCountOfPatients(String query, boolean includeVoided);
	
	/**
	 * Gets a list of allergies that a patient has
	 * 
	 * @param patient the patient
	 * @return the allergy list
	 */
	public List<Allergy> getAllergies(Patient patient);
	
	/**
	 * Gets a patient's allergy status
	 * 
	 * @param patient the patient
	 * @return the allergy status
	 */
	public String getAllergyStatus(Patient patient);
	
	/**
	 * Saves patient allergies to the database.
	 * 
	 * @param patient the patient
	 * @param allergies the allergies
	 * @return the saved allergies
	 */
	public Allergies saveAllergies(Patient patient, Allergies allergies);
	
	/**
	 * Gets a allergy matching the given allergyId
	 * 
	 * @param allergyId of allergy to return
	 * @return the allergy matching the given allergyId
	 * @should return allergy given valid allergyId
	 * @should return null if no object found with given allergyId
	 */
	public Allergy getAllergy(Integer allergyId);
	
	
	/**
	 * Gets a allergy matching the given uuid
	 * 
	 * @param uuid of allergy to return
	 * @since 2.0
	 * @return the allergy matching the given uuid
	 * @should return allergy given valid uuid
	 * @should return null if no object found with given uuid
	 */
	public Allergy getAllergyByUuid(String uuid);
	
	/**
	 * Saves an allergy to the database
	 * 
	 * @param allergy the allergy to save
	 * @return the saved allergy
	 */
	public Allergy saveAllergy(Allergy allergy);
	
}

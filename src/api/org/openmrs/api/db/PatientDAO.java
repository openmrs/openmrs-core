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
package org.openmrs.api.db;

import java.util.List;

import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;

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
	 * @see #voidPatient(Patient, String)
	 */
	public void deletePatient(Patient patient) throws DAOException;
	
	/**
	 * @see org.openmrs.api.PatientService#getAllPatients(boolean)
	 */
	public List<Patient> getAllPatients(boolean includeVoided) throws DAOException;
	
	/**
	 * @see org.openmrs.api.PatientService#getPatients(java.lang.String, java.lang.String,
	 *      java.util.List, boolean)
	 */
	public List<Patient> getPatients(String name, String identifier, List<PatientIdentifierType> identifierTypes,
	                                 boolean matchIdentifierExactly) throws DAOException;
	
	/**
	 * @see org.openmrs.api.PatientService#getPatientIdentifiers(java.lang.String, java.util.List,
	 *      java.util.List, java.util.List, java.lang.Boolean)
	 */
	public List<PatientIdentifier> getPatientIdentifiers(String identifier,
	                                                     List<PatientIdentifierType> patientIdentifierTypes,
	                                                     List<Location> locations, List<Patient> patients,
	                                                     Boolean isPreferred) throws DAOException;
	
	/**
	 * @see org.openmrs.api.PatientService#savePatientIdentifierType(org.openmrs.PatientIdentifierType)
	 */
	public PatientIdentifierType savePatientIdentifierType(PatientIdentifierType patientIdentifierType) throws DAOException;
	
	/**
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
	
}

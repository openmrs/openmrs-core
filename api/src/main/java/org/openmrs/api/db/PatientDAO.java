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

import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.api.PatientService;

import java.util.List;

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
	 * @param searchOnNamesOrIdentifiers specifies if the logic should find patients that match the
	 *            name or identifier otherwise find patients that match both the name and identifier
	 * @see org.openmrs.api.PatientService#getPatients(String, String, List, boolean, Integer,
	 *      Integer)
	 * @should escape percentage character in name phrase
	 * @should escape underscore character in name phrase
	 * @should escape an asterix character in name phrase
	 * @should escape percentage character in identifier phrase
	 * @should escape underscore character in identifier phrase
	 * @should escape an asterix character in identifier phrase
	 * @should get patients with a matching identifier and type
	 */
	public List<Patient> getPatients(String name, String identifier, List<PatientIdentifierType> identifierTypes,
	        boolean matchIdentifierExactly, Integer start, Integer length, boolean searchOnNamesOrIdentifiers)
	        throws DAOException;
	
	/**
	 * @see org.openmrs.api.PatientService#getPatientIdentifiers(java.lang.String, java.util.List,
	 *      java.util.List, java.util.List, java.lang.Boolean)
	 */
	public List<PatientIdentifier> getPatientIdentifiers(String identifier,
	        List<PatientIdentifierType> patientIdentifierTypes, List<Location> locations, List<Patient> patients,
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
	
	/**
	 * @see org.openmrs.api.PatientService#isIdentifierInUseByAnotherPatient(PatientIdentifier)
	 */
	public boolean isIdentifierInUseByAnotherPatient(PatientIdentifier patientIdentifier);
	
	/**
	 * Auto generated method comment
	 * 
	 * @param uuid
	 * @return
	 */
	public Patient getPatientByUuid(String uuid);
	
	public PatientIdentifier getPatientIdentifierByUuid(String uuid);
	
	/**
	 * Auto generated method comment
	 * 
	 * @param uuid
	 * @return
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
	 * @param searchOnNamesOrIdentifiers specifies if the logic should find patients that match the
	 *            name or identifier otherwise find patients that match both the name and identifier
	 * @see PatientService#getCountOfPatients(String)
	 */
	public Long getCountOfPatients(String name, String identifier, List<PatientIdentifierType> identifierTypes,
	        boolean matchIdentifierExactly, boolean searchOnNamesOrIdentifiers);
}

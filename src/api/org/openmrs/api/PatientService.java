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
package org.openmrs.api;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.openmrs.Concept;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.Tribe;
import org.openmrs.annotation.Authorized;
import org.openmrs.api.db.PatientDAO;
import org.openmrs.patient.IdentifierValidator;
import org.openmrs.util.OpenmrsConstants;
import org.springframework.transaction.annotation.Transactional;

/**
 * Contains methods pertaining to Patients in the system
 * 
 * Use:<br/>
 * 
 * <pre>
 *   List<Patient> patients = Context.getPatientService().getAllPatients();
 * </pre>
 * 
 * @see org.openmrs.api.context.Context
 * @see org.openmrs.Patient
 */
@Transactional
public interface PatientService extends OpenmrsService {

	/**
	 * Sets the DAO for this service.  This is done by DI and Spring.  See
	 * the applicationContext-service.xml definition file.
	 * 
	 * @param dao DAO for this service
	 */
	public void setPatientDAO(PatientDAO dao);

	/**
	 * @see #savePatient(Patient)
	 * @deprecated replaced by #savePatient(Patient)
	 */
	@Authorized( { OpenmrsConstants.PRIV_ADD_PATIENTS })
	public Patient createPatient(Patient patient) throws APIException;

	/**
	 * Saved the given <code>patient</code> to the database
	 * 
	 * @param patient patient to be created or updated
	 * @return patient who was created or updated
	 * @throws APIException
	 */
	@Authorized( { OpenmrsConstants.PRIV_ADD_PATIENTS,
	        OpenmrsConstants.PRIV_EDIT_PATIENTS })
	public Patient savePatient(Patient patient) throws APIException;

	/**
	 * Get patient by internal identifier
	 * 
	 * @param patientId internal patient identifier
	 * @return patient with given internal identifier
	 * @throws APIException
	 */
	@Authorized( { OpenmrsConstants.PRIV_VIEW_PATIENTS })
	@Transactional(readOnly = true)
	public Patient getPatient(Integer patientId) throws APIException;

	/**
	 * @see #savePatient(Patient)
	 * @deprecated replaced by #savePatient(Patient)
	 */
	public Patient updatePatient(Patient patient) throws APIException;
	
	/**
	 * Returns all non voided patients in the system
	 * 
	 * @return non voided patients in the system
	 * @see #getAllPatients(boolean)
	 * @throws APIException
	 */
	@Authorized( { OpenmrsConstants.PRIV_VIEW_PATIENTS })
	@Transactional(readOnly = true)
	public List<Patient> getAllPatients() throws APIException;
	
	/**
	 * Returns patients in the system
	 * 
	 * @param includeVoided if false, will limit the search to non-voided patients
	 * @return patients in the system
	 * @throws APIException
	 */
	@Authorized( { OpenmrsConstants.PRIV_VIEW_PATIENTS })
	@Transactional(readOnly = true)
	public List<Patient> getAllPatients(boolean includeVoided) throws APIException;
	
	/**
	 * @deprecated use #getPatientByIdentifier(String) instead
	 */
	@Transactional(readOnly = true)
	public Patient identifierInUse(String identifier,
	        PatientIdentifierType type, Patient ignorePatient);
	
	/**
	 * @deprecated replaced by {@link #getPatients(String, String, List)
	 */
	@Transactional(readOnly = true)
	public List<Patient> getPatientsByIdentifier(String identifier,
	        boolean includeVoided) throws APIException;

	/**
	 * Get patients based on given criteria
	 * 
	 * The identifier is matched with the regex
	 * <code>OpenmrsConstants.PATIENT_IDENTIFIER_REGEX</code>
	 * 
	 * All parameters are optional and nullable.  If null, it is not included
	 * in the search.
	 * 
	 * Will not return voided patients
	 * 
	 * @param name (optional) this is a slight break from the norm, patients with a 
	 * 		partial match on this name will be returned
	 * @param identifier (optional) only patients with a matching identifier are returned
	 * @param identifierTypes (optional) the PatientIdentifierTypes to restrict to
	 * @param matchIdentifierExactly (required) if true, then the given <code>identifier</code>
	 * 		must equal the id in the database.  if false, then the identifier is 'searched' for
	 * 		by using a regular expression 
	 * @return patients that matched the given criteria (and are not voided)
	 * @throws APIException
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_PATIENTS })
	public List<Patient> getPatients(String name, String identifier, List<PatientIdentifierType> identifierTypes, boolean matchIdentifierExactly)
	        throws APIException;
	
	/**
	 * @deprecated replaced by a call to 
	 * 		{@link #getPatients(String, String, List, boolean)} with "false" as the last parameter
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_PATIENTS })
	public List<Patient> getPatients(String name, String identifier, List<PatientIdentifierType> identifierTypes)
	        throws APIException;
	
	/**
	 * @deprecated replaced by getPatients( ... )
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_PATIENTS })
	public List<Patient> getPatientsByIdentifierPattern(String identifier,
	        boolean includeVoided) throws APIException;

	/**
	 * @deprecated replaced by {@link #getPatients(String, String, List)}
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_PATIENTS })
	public List<Patient> getPatientsByName(String name) throws APIException;

	/**
	 * @deprecated replaced by getPatients( ... )
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_PATIENTS })
	public List<Patient> getPatientsByName(String name, boolean includeVoided)
	        throws APIException;

	/**
	 * Void patient record (functionally delete patient from system)
	 * 
	 * @param patient patient to be voided
	 * @param reason reason for voiding patient
	 * @return the voided patient
	 */
	@Authorized( { OpenmrsConstants.PRIV_DELETE_PATIENTS })
	public Patient voidPatient(Patient patient, String reason) throws APIException;

	/**
	 * Unvoid patient record
	 * 
	 * @param patient patient to be revived
	 * @return the revided Patient
	 */
	@Authorized( { OpenmrsConstants.PRIV_DELETE_PATIENTS })
	public Patient unvoidPatient(Patient patient) throws APIException;

	/**
	 * @see #purgePatient(Patient)
	 * @deprecated replaced by {@link #purgePatient(Patient)}
	 */
	@Authorized( { OpenmrsConstants.PRIV_PURGE_PATIENTS })
	public void deletePatient(Patient patient) throws APIException;

	/**
	 * Delete patient from database. This <b>should not be called</b> except
	 * for testing and administration purposes. Use the void method instead.
	 * 
	 * @param patient patient to be deleted
	 * @throws APIException
	 * 
	 * @see #voidPatient(org.openmrs.Patient,java.lang.String)
	 */
	@Authorized( { OpenmrsConstants.PRIV_PURGE_PATIENTS })
	public void purgePatient(Patient patient) throws APIException;

	/**
	 * @deprecated replaced by {@link #getPatientIdentifiers(String, List, List, List, Boolean, Boolean, boolean)}
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_PATIENT_IDENTIFIERS })
	public List<PatientIdentifier> getPatientIdentifiers(
	        PatientIdentifierType patientIdentifierType) throws APIException;

	/**
	 * Get all patientIdentifiers that match all of the given criteria
	 * 
	 * Voided identifiers are not returned
	 * 
	 * @param identifier the full identifier to match on
	 * @param patientIdentifierTypes the type of identifiers to get
	 * @param locations the locations of the identifiers to match
	 * @param patients the patients containing these identifiers
	 * @param isPreferred if true, limits to only preferred identifiers
	 * 			if false, only non preferred.  if null, ignores preferred status
	 * @return PatientIdentifiers matching these criteria
	 * @throws APIException
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_PATIENT_IDENTIFIERS })
	public List<PatientIdentifier> getPatientIdentifiers(String identifier,
	        List<PatientIdentifierType> patientIdentifierTypes, List<Location> locations, 
	        List<Patient> patients, Boolean isPreferred)
	        throws APIException;

	/**
	 * @deprecated replaced by {@link #getPatientIdentifiers(String, List, List, List, Boolean, Boolean, boolean)}
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_PATIENT_IDENTIFIERS })
	public List<PatientIdentifier> getPatientIdentifiers(String identifier,
	        PatientIdentifierType pit) throws APIException;

	/**
	 * Update patient identifier
	 * 
	 * @param patientIdentifier identifier to be updated
	 * @deprecated patient identifiers should not be updated directly; rather,
	 *             after changing patient identifiers, use
	 *             {@link #savePatient(Patient)} to save changes to the database
	 * @throws APIException
	 */
	@Authorized( { OpenmrsConstants.PRIV_EDIT_PATIENT_IDENTIFIERS })
	public void updatePatientIdentifier(PatientIdentifier patientIdentifier)
	        throws APIException;

	/**
	 * Create or update a PatientIdentifierType
	 * 
	 * @param PatientIdentifierType identifier type to create or update
	 * @return the saved type
	 * @throws APIException
	 */
	@Authorized( { OpenmrsConstants.PRIV_MANAGE_IDENTIFIER_TYPES })
	public PatientIdentifierType savePatientIdentifierType(
	        PatientIdentifierType patientIdentifierType) throws APIException;

	/**
	 * @see #getAllPatientIdentifierTypes()
	 * @deprecated replaced by {@link #getAllPatientIdentifierTypes()}
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_IDENTIFIER_TYPES })
	public List<PatientIdentifierType> getPatientIdentifierTypes()
	        throws APIException;

	/**
	 * Get all patientIdentifier types
	 * 
	 * @return patientIdentifier types list
	 * @throws APIException
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_IDENTIFIER_TYPES })
	public List<PatientIdentifierType> getAllPatientIdentifierTypes()
	        throws APIException;
	
	/**
	 * Get all patientIdentifier types
	 * 
	 * @param includeRetired true/false whether retired types should be included
	 * @return patientIdentifier types list
	 * @throws APIException
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_IDENTIFIER_TYPES })
	public List<PatientIdentifierType> getAllPatientIdentifierTypes(boolean includeRetired)
	        throws APIException;
	
	/**
	 * Get all patientIdentifier types that match the given criteria
	 * 
	 * @param name name of the type to match on
	 * @param format the string format to match on
	 * @param required if true, limits to only identifiers marked as required
	 * 			if false, only non required.  if null, ignores required bit
	 * @param hasCheckDigit if true, limits to only check digit'd identifiers
	 * 			if false, only non checkdigit'd.  if null, ignores checkDigit
	 * @return patientIdentifier types list
	 * @throws APIException
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_IDENTIFIER_TYPES })
	public List<PatientIdentifierType> getPatientIdentifierTypes(String name,
	        String format, Boolean required, Boolean hasCheckDigit)
	        throws APIException;
	
	/**
	 * Get patientIdentifierType by internal identifier
	 * 
	 * @param patientIdentifierType id
	 * @return patientIdentifierType with given internal identifier
	 * @throws APIException
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_IDENTIFIER_TYPES })
	public PatientIdentifierType getPatientIdentifierType(
	        Integer patientIdentifierTypeId) throws APIException;

	/**
	 * @deprecated use {@link #getPatientIdentifierTypeByName(String)}
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_IDENTIFIER_TYPES })
	public PatientIdentifierType getPatientIdentifierType(String name)
	        throws APIException;
	
	/**
	 * Get patientIdentifierType by exact name
	 * 
	 * @param name
	 * @return patientIdentifierType with given name
	 * @throws APIException
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_IDENTIFIER_TYPES })
	public PatientIdentifierType getPatientIdentifierTypeByName(String name)
	        throws APIException;

	/**
	 * Retire a type of patient identifier
	 * 
	 * @param patientIdentifierType type of patient identifier to be retired
	 * @param reason the reason to retire this identifier type
	 * @return the retired type
	 * @throws APIException
	 */
	@Authorized( { OpenmrsConstants.PRIV_MANAGE_IDENTIFIER_TYPES })
	public PatientIdentifierType retirePatientIdentifierType(
	        PatientIdentifierType patientIdentifierType, String reason) throws APIException;

	/**
	 * Unretire a type of patient identifier
	 * 
	 * @param patientIdentifierType type of patient identifier to be unretired
	 * @return the unretired type
	 * @throws APIException
	 */
	@Authorized( { OpenmrsConstants.PRIV_MANAGE_IDENTIFIER_TYPES })
	public PatientIdentifierType unretirePatientIdentifierType(
	        PatientIdentifierType patientIdentifierType) throws APIException;

	/**
	 * Purge PatientIdentifierType (cannot be undone)
	 * 
	 * @param PatientIdentifierType to purge from the database
	 * @throws APIException
	 */
	@Authorized( { OpenmrsConstants.PRIV_PURGE_IDENTIFIER_TYPES })
	public void purgePatientIdentifierType(
	        PatientIdentifierType patientIdentifierType) throws APIException;

	/**
	 * Convenience method to validate a patient identifier.  Checks for things like blank
	 * identifiers, invalid check digits, etc
	 * 
	 * @param patientIdentifier identifier to be validated
	 * @see #checkPatientIdentifiers(Patient)
	 * @throws PatientIdentifierException if the identifier is invalid
	 * @deprecated use {@link PatientIdentifierValidator.validate(PatientIdentifier)}
	 */
	@Authorized( { OpenmrsConstants.PRIV_VIEW_PATIENT_IDENTIFIERS })
	public void checkPatientIdentifier(PatientIdentifier patientIdentifier)
	        throws PatientIdentifierException;

	/**
	 * Convenience method to validate all identifiers for a given patient
	 * 
	 * @param patient patient for which to validate identifiers
	 * @see #checkPatientIdentifiers(Patient)
	 * @throws PatientIdentifierException if one or more of the identifiers are invalid
	 */
	@Authorized( { OpenmrsConstants.PRIV_VIEW_PATIENT_IDENTIFIERS })
	public void checkPatientIdentifiers(Patient patient)
	        throws PatientIdentifierException;

	/**
	 * Get tribe by internal tribe identifier
	 * 
	 * @return Tribe
	 * @param tribeId
	 * @deprecated tribe will be moved to patient attribute
	 * @throws APIException
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_TRIBES })
	public Tribe getTribe(Integer tribeId) throws APIException;

	/**
	 * Get list of tribes that are not retired
	 * 
	 * @return non-retired Tribe list
	 * @deprecated tribe will be moved to patient attributes
	 * @throws APIException
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_TRIBES })
	public List<Tribe> getTribes() throws APIException;

	/**
	 * Find tribes by partial name lookup
	 * 
	 * @return non-retired Tribe list
	 * @deprecated tribe will be moved to patient attributes
	 * @throws APIException
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_TRIBES })
	public List<Tribe> findTribes(String search) throws APIException;

	/**
	 * @see #getPatients(String)
	 * @deprecated use #getPatients(String)
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_PATIENTS })
	public List<Patient> findPatients(String query, boolean includeVoided) throws APIException;

	/**
	 * Generic search on patients based on the given string.  Implementations
	 * can use this string to search on name, identifier, etc
	 * 
	 * Voided patients are not returned in search results
	 * 
	 * @param query the string to search on
	 * @return a list of matching Patients
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_PATIENTS })
	public List<Patient> getPatients(String query) throws APIException;
	
	/**
	 * @see #getPatientByExample(Patient)
	 * @deprecated use #getPatientByExample(Patient)
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_PATIENTS })
	public Patient findPatient(Patient patientToMatch) throws APIException;
	
	/**
	 * This method tries to find a patient in the database given the attributes
	 * on the given <code>patientToMatch</code> object.
	 * 
	 * Assumes there could be a PersonAttribute on this Patient with
	 * PersonAttributeType.name = "Other Matching Information". This
	 * PersonAttribute has a "value" that is just key value pairs in the form of
	 * key:value;nextkey:nextvalue;
	 * 
	 * @param patientToMatch
	 * @return null if no match found, a fresh patient object from the db if is
	 *         found
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_PATIENTS })
	public Patient getPatientByExample(Patient patientToMatch) throws APIException;
	
	/**
	 * @deprecated use {@link #getDuplicatePatientsByAttributes(List)}
	 * @see #getDuplicatePatientsByAttributes(List)
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_PATIENTS })
	public List<Patient> findDuplicatePatients(Set<String> attributes) throws APIException;
	
	/**
	 * Search the database for patients that both share the given attributes.  Each
	 * attribute that is passed in must be identical to what is stored for at least one 
	 * other patient for both patients to be returned.  
	 * 
	 * @param attributes attributes on a Person or Patient object.  similar to: [gender, tribe, 
	 * 			givenName, middleName, familyName]
	 * @return list of patients that match other patients
	 * @throws APIException
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_PATIENTS })
	public List<Patient> getDuplicatePatientsByAttributes(List<String> attributes) throws APIException;
	
	/**
	 * Convenience method to join two patients' information into one record.
	 * 
	 * <ol>
	 *   <li> Moves object (encounters/obs) pointing to <code>nonPreferred</code> 
	 *   	to point at <code>preferred</code></li>
	 *   <li> Copies data (gender/birthdate/names/ids/etc) from 
	 *   	<code>nonPreferred</code> to <code>preferred</code> IFF the data 
	 *   	is missing or null in <code>preferred</code></li> 
	 *    <li><code>notPreferred</code> is marked as voided </li>
	 * </ol>
	 * 
	 * @param preferred The Patient to merge to
	 * @param notPreferred The Patient to merge from (and then void)
	 * @throws APIException
	 */
	@Authorized( { OpenmrsConstants.PRIV_EDIT_PATIENTS })
	public void mergePatients(Patient preferred, Patient notPreferred)
	        throws APIException;

	/**
	 * Convenience method to establish that a patient has left the care center.
	 * This API call is responsible for: 1) Closing workflow statuses 2)
	 * Terminating programs 3) Discontinuing orders 4) Flagging patient table
	 * (if applicable) 5) Creating any relevant observations about the patient
	 * 
	 * @param patient - the patient who has exited care
	 * @param dateExited - the declared date/time of the patient's exit
	 * @param reasonForExit - the concept that corresponds with why the patient
	 *        has been declared as exited
	 * @throws APIException
	 */
	// TODO keep this in the PatientService?
	@Authorized( { OpenmrsConstants.PRIV_EDIT_PATIENTS })
	public void exitFromCare(Patient patient, Date dateExited,
	        Concept reasonForExit) throws APIException;

	/**
	 * Convenience method to establish that a patient has died. In addition to
	 * exiting the patient from care (see above), this method will also set the
	 * appropriate patient characteristics to indicate that they have died, when
	 * they died, etc.
	 * 
	 * @param patient - the patient who has died
	 * @param dateDied - the declared date/time of the patient's death
	 * @param causeOfDeath - the concept that corresponds with the reason the
	 *        patient died
	 * @param otherReason - if the concept representing the reason is OTHER
	 *        NON-CODED, and a string-based "other" reason is supplied
	 * @throws APIException
	 */
	// TODO Keep this in the PatientService?
	@Authorized( { OpenmrsConstants.PRIV_EDIT_PATIENTS })
	public void processDeath(Patient patient, Date dateDied,
	        Concept causeOfDeath, String otherReason) throws APIException;

	/**
	 * Convenience method that saves the Obs that indicates when and why the
	 * patient died (including any "other" reason there might be)
	 * 
	 * @param patient - the patient who has died
	 * @param dateDied - the declared date/time of the patient's death
	 * @param causeOfDeath - the concept that corresponds with the reason the
	 *        patient died
	 * @param otherReason - if the concept representing the reason is OTHER
	 *        NON-CODED, and a string-based "other" reason is supplied
	 * @throws APIException
	 */
	// TODO keep this in the PatientService?
	@Authorized(value = { OpenmrsConstants.PRIV_VIEW_PATIENTS,
	        OpenmrsConstants.PRIV_EDIT_OBS }, requireAll = true)
	public void saveCauseOfDeathObs(Patient patient, Date dateDied,
	        Concept causeOfDeath, String otherReason) throws APIException;
	
	/**
     * 
     * @param identifierValidator which validator to get.
     */
    public IdentifierValidator getIdentifierValidator(Class<IdentifierValidator> clazz);

    /**
     * 
     */
    public IdentifierValidator getIdentifierValidator(String pivClassName);
    
    /**
     * 
     * @return the default IdentifierValidator
     */
    public IdentifierValidator getDefaultIdentifierValidator();

	/**
     * @return All registered PatientIdentifierValidators
     * @should return all registered identifier validators
     */
    public Collection<IdentifierValidator> getAllIdentifierValidators();
    
}
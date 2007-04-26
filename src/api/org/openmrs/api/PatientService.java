package org.openmrs.api;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.openmrs.Concept;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.Tribe;
import org.openmrs.annotation.Authorized;
import org.openmrs.api.db.PatientDAO;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface PatientService {

	public void setPatientDAO(PatientDAO dao);

	/**
	 * Creates a new patient record
	 * 
	 * @param patient to be created
	 * @returns the created patient
	 * @throws APIException
	 */
	@Authorized({"Add Patients"})
	public Patient createPatient(Patient patient) throws APIException;

	/**
	 * Get patient by internal identifier
	 * 
	 * @param patientId internal patient identifier
	 * @return patient with given internal identifier
	 * @throws APIException
	 */
	@Authorized({"View Patients"})
	@Transactional(readOnly=true)
	public Patient getPatient(Integer patientId) throws APIException;

	/**
	 * Update patient 
	 * 
	 * @param patient to be updated
	 * @returns the updated patient
	 * @throws APIException
	 */
	public Patient updatePatient(Patient patient) throws APIException;

	@Transactional(readOnly=true)
	public Patient identifierInUse(String identifier,
			PatientIdentifierType type, Patient ignorePatient);

	/**
	 * Find all patients with a given identifier
	 * 
	 * @param identifier
	 * @return set of patients matching identifier
	 * @throws APIException
	 */
	@Transactional(readOnly=true)
	public Set<Patient> getPatientsByIdentifier(String identifier,
			boolean includeVoided) throws APIException;

	/**
	 * Find all patients with a given identifier and use the regex 
	 * <code>OpenmrsConstants.PATIENT_IDENTIFIER_REGEX</code>
	 * 
	 * Note: Uses NON-STANDARD SQL: "...WHERE identifier REGEXP '...' ..."
	 * 
	 * @param identifier
	 * @return set of patients matching identifier
	 * @throws APIException
	 */
	@Transactional(readOnly=true)
	public Set<Patient> getPatientsByIdentifierPattern(String identifier,
			boolean includeVoided) throws APIException;

	@Transactional(readOnly=true)
	public Set<Patient> getPatientsByName(String name) throws APIException;

	/**
	 * Find patients by name
	 * 
	 * @param name
	 * @return set of patients matching name
	 * @throws APIException
	 */
	@Transactional(readOnly=true)
	public Set<Patient> getPatientsByName(String name, boolean includeVoided)
			throws APIException;

	/**
	 * Void patient record (functionally delete patient from system)
	 * 
	 * @param patient patient to be voided
	 * @param reason reason for voiding patient
	 */
	@Authorized({"Edit Patients"})
	public void voidPatient(Patient patient, String reason) throws APIException;

	/**
	 * Unvoid patient record 
	 * 
	 * @param patient patient to be revived
	 */
	@Authorized({"Edit Patients"})
	public void unvoidPatient(Patient patient) throws APIException;

	/**
	 * Delete patient from database. This <b>should not be called</b>
	 * except for testing and administration purposes.  Use the void
	 * method instead.
	 * 
	 * @param patient patient to be deleted
	 * @throws APIException
	 * 
	 * @see voidPatient(org.openmrs.Patient,java.lang.String)
	 */
	public void deletePatient(Patient patient) throws APIException;

	/**
	 * Get all patientIdentifiers 
	 * 
	 * @param pit
	 * @return patientIdentifier list
	 * @throws APIException
	 */
	@Transactional(readOnly=true)
	public List<PatientIdentifier> getPatientIdentifiers(
			PatientIdentifierType pit) throws APIException;

	/**
	 * Get Patient Identifiers matching the identifier and type 
	 * 
	 * @param identifier
	 * @param pit
	 * @return patientIdentifier list
	 * @throws APIException
	 */
	@Transactional(readOnly=true)
	public List<PatientIdentifier> getPatientIdentifiers(String identifier,
			PatientIdentifierType pit) throws APIException;

	/**
	 * Update patient identifier
	 * 
	 * @param patient to be updated
	 * @throws APIException
	 */
	public void updatePatientIdentifier(PatientIdentifier pi)
			throws APIException;

	/**
	 * Get all patientIdentifier types
	 * 
	 * @return patientIdentifier types list
	 * @throws APIException
	 */
	@Transactional(readOnly=true)
	public List<PatientIdentifierType> getPatientIdentifierTypes()
			throws APIException;

	/**
	 * Get patientIdentifierType by internal identifier
	 * 
	 * @param patientIdentifierType id
	 * @return patientIdentifierType with given internal identifier
	 * @throws APIException
	 */
	@Transactional(readOnly=true)
	public PatientIdentifierType getPatientIdentifierType(
			Integer patientIdentifierTypeId) throws APIException;

	/**
	 * Get patientIdentifierType by name
	 * 
	 * @param name
	 * @return patientIdentifierType with given name
	 * @throws APIException
	 */
	@Transactional(readOnly=true)
	public PatientIdentifierType getPatientIdentifierType(String name)
			throws APIException;

	public void checkPatientIdentifier(PatientIdentifier pi) throws PatientIdentifierException;

	public void checkPatientIdentifiers(Patient patient) throws PatientIdentifierException;
	
	/**
	 * Get tribe by internal tribe identifier
	 * 
	 * @return Tribe
	 * @param tribeId 
	 * @throws APIException
	 */
	@Transactional(readOnly=true)
	public Tribe getTribe(Integer tribeId) throws APIException;

	/**
	 * Get list of tribes that are not retired
	 * 
	 * @return non-retired Tribe list
	 * @throws APIException
	 */
	@Transactional(readOnly=true)
	public List<Tribe> getTribes() throws APIException;

	/**
	 * Find tribes by partial name lookup
	 * 
	 * @return non-retired Tribe list
	 * @throws APIException
	 */
	@Transactional(readOnly=true)
	public List<Tribe> findTribes(String search) throws APIException;

	@Transactional(readOnly=true)
	public List<Patient> findPatients(String query, boolean includeVoided);

	/**
	 * Search the database for patients that share the given attributes
	 * attributes similar to: [gender, tribe, givenName, middleName, familyname]
	 * 
	 * @param attributes
	 * @return list of patients that match other patients
	 */
	@Transactional(readOnly=true)
	public List<Patient> findDuplicatePatients(Set<String> attributes);

	/**
	 * 1) Moves object (encounters/obs) pointing to <code>nonPreferred</code> to <code>preferred</code>
	 * 2) Copies data (gender/birthdate/names/ids/etc) from <code>nonPreferred</code> to 
	 * <code>preferred</code> iff the data is missing or null in <code>preferred</code>
	 * 3) <code>notPreferred</code> is marked as voided
	 * @param preferred
	 * @param notPreferred
	 * @throws APIException
	 */
	public void mergePatients(Patient preferred, Patient notPreferred)
			throws APIException;

	
	/**
	 * This is the way to establish that a patient has left the care center.  This API call is responsible for:
	 * 1) Closing workflow statuses
	 * 2) Terminating programs
	 * 3) Discontinuing orders
	 * 4) Flagging patient table (if applicable)
	 * 5) Creating any relevant observations about the patient
	 * @param patient - the patient who has exited care
	 * @param dateExited - the declared date/time of the patient's exit
	 * @param reasonForExit - the concept that corresponds with why the patient has been declared as exited
	 * @throws APIException
	 */
	public void exitFromCare(Patient patient, Date dateExited, Concept reasonForExit)
			throws APIException;

	/**
	 * This is the way to establish that a patient has died.  In addition to exiting the patient from care (see above),
	 * this method will also set the appropriate patient characteristics to indicate that they have died, when they died, etc.
	 * @param patient - the patient who has died
	 * @param dateDied - the declared date/time of the patient's death
	 * @param causeOfDeath - the concept that corresponds with the reason the patient died
	 * @param otherReason - if the concept representing the reason is OTHER NON-CODED, and a string-based "other" reason is supplied
	 * @throws APIException
	 */
	public void processDeath(Patient patient, Date dateDied, Concept causeOfDeath, String otherReason)
			throws APIException;

	/**
	 * This method creates (or updates) the Obs that indicates when and why the patient died (including any "other" reason there might be)
	 * @param patient - the patient who has died
	 * @param dateDied - the declared date/time of the patient's death
	 * @param causeOfDeath - the concept that corresponds with the reason the patient died
	 * @param otherReason - if the concept representing the reason is OTHER NON-CODED, and a string-based "other" reason is supplied
	 * @throws APIException
	 */
	public void saveCauseOfDeathObs(Patient patient, Date dateDied, Concept causeOfDeath, String otherReason)
			throws APIException;

}
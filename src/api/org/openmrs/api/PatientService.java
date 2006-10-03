package org.openmrs.api;

import java.util.List;
import java.util.Set;

import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.Person;
import org.openmrs.Relationship;
import org.openmrs.RelationshipType;
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
	 * @throws APIException
	 */
	@Authorized({"Add Patients"})
	public void createPatient(Patient patient) throws APIException;

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
	 * @throws APIException
	 */
	public void updatePatient(Patient patient) throws APIException;

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

	@Transactional(readOnly=true)
	public Set<Patient> getSimilarPatients(String name, Integer birthyear,
			String gender) throws APIException;

	/**
	 * Void patient record (functionally delete patient from system)
	 * 
	 * @param patient patient to be voided
	 * @param reason reason for voiding patient
	 */
	public void voidPatient(Patient patient, String reason) throws APIException;

	/**
	 * Unvoid patient record 
	 * 
	 * @param patient patient to be revived
	 */
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

	/**
	 * Get relationship by internal relationship identifier
	 * 
	 * @return Relationship
	 * @param relationshipId 
	 * @throws APIException
	 */
	@Transactional(readOnly=true)
	public Relationship getRelationship(Integer relationshipId)
			throws APIException;

	/**
	 * Get list of relationships that are not retired
	 * 
	 * @return non-voided Relationship list
	 * @throws APIException
	 */
	@Transactional(readOnly=true)
	public List<Relationship> getRelationships() throws APIException;

	/**
	 * Get list of relationships that include Person in person_id or relative_id
	 * 
	 * @return Relationship list
	 * @throws APIException
	 */
	@Transactional(readOnly=true)
	public List<Relationship> getRelationships(Person p, boolean showVoided)
			throws APIException;

	@Transactional(readOnly=true)
	public List<Relationship> getRelationships(Person p) throws APIException;

	/**
	 * Get list of relationships that have Person as relative_id, and the given type (which can be null)
	 * @return Relationship list
	 */
	@Transactional(readOnly=true)
	public List<Relationship> getRelationshipsTo(Person toPerson,
			RelationshipType relType) throws APIException;

	/**
	 * Get all relationshipTypes
	 * 
	 * @return relationshipType list
	 * @throws APIException
	 */
	@Transactional(readOnly=true)
	public List<RelationshipType> getRelationshipTypes() throws APIException;

	/**
	 * Get relationshipType by internal identifier
	 * 
	 * @param relationshipType id
	 * @return relationshipType with given internal identifier
	 * @throws APIException
	 */
	@Transactional(readOnly=true)
	public RelationshipType getRelationshipType(Integer relationshipTypeId)
			throws APIException;

	/**
	 * Find relationshipType by name
	 * @throws APIException
	 */
	@Transactional(readOnly=true)
	public RelationshipType findRelationshipType(String relationshipTypeName)
			throws APIException;

	/**
	 * Get all locations
	 * 
	 * @return location list
	 * @throws APIException
	 */
	@Transactional(readOnly=true)
	public List<Location> getLocations() throws APIException;

	/**
	 * Get location by internal identifier
	 * 
	 * @param location id
	 * @return location with given internal identifier
	 * @throws APIException
	 */
	@Transactional(readOnly=true)
	public Location getLocation(Integer locationId) throws APIException;

	/**
	 * Get location by name
	 * 
	 * @param name location's name
	 * @return location with given name
	 * @throws APIException
	 */
	@Transactional(readOnly=true)
	public Location getLocationByName(String name) throws APIException;

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

}
package org.openmrs.api.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Patient;
import org.openmrs.PatientAddress;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PatientName;
import org.openmrs.PatientProgram;
import org.openmrs.Person;
import org.openmrs.ProgramWorkflow;
import org.openmrs.ProgramWorkflowState;
import org.openmrs.Relationship;
import org.openmrs.RelationshipType;
import org.openmrs.Tribe;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.APIException;
import org.openmrs.api.EncounterService;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.PatientDAO;
import org.openmrs.programWorkflow.PatientProgramSupport;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsUtil;

/**
 * Patient-related services
 * 
 * @author Ben Wolfe
 * @author Burke Mamlin
 * @vesrion 1.0
 */
public class PatientServiceImpl implements PatientService {

	private Log log = LogFactory.getLog(this.getClass());
	
	private PatientDAO dao;
	
	public PatientServiceImpl() {	}
	
	private PatientDAO getPatientDAO() {
		return dao;
	}
	
	public void setPatientDAO(PatientDAO dao) {
		this.dao = dao;
	}
	
	/**
	 * Creates a new patient record
	 * 
	 * @param patient to be created
	 * @throws APIException
	 */
	public void createPatient(Patient patient) throws APIException {
		if (log.isDebugEnabled()) {
			String s = "" + patient.getPatientId();
			if (patient.getIdentifiers() != null)
				s += "|" + patient.getIdentifiers();
			
			log.debug(s);
		}
		
		setCollectionProperties(patient);
		
		checkPatientIdentifiers(patient);
		
		getPatientDAO().createPatient(patient);
	}

	/**
	 * Get patient by internal identifier
	 * 
	 * @param patientId internal patient identifier
	 * @return patient with given internal identifier
	 * @throws APIException
	 */
	public Patient getPatient(Integer patientId) throws APIException {
		return getPatientDAO().getPatient(patientId);
	}

	/**
	 * Update patient 
	 * 
	 * @param patient to be updated
	 * @throws APIException
	 */
	public void updatePatient(Patient patient) throws APIException {
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_EDIT_PATIENTS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_EDIT_PATIENTS);
		
		setCollectionProperties(patient);
		
		checkPatientIdentifiers(patient);
		
		getPatientDAO().updatePatient(patient);
	}
	
	/**
	 * Throws an API Exception if one of the patient's identifier already exists 
	 * in the system. Also throws an Exception if a patient has zero identifiers
	 * 
	 * @param patient
	 * @throws APIException
	 */
	private void checkPatientIdentifiers(Patient patient) throws APIException {
		// TODO create a temporary identifier?
		if (patient.getIdentifiers().size() < 1 )
			throw new APIException("At least one Patient Identifier is required");
		
		List<String> identifiersUsed = new Vector<String>();
		
		List<PatientIdentifier> identifiers = new Vector<PatientIdentifier>();
		identifiers.addAll(patient.getIdentifiers());
		
		// Check for duplicate identifiers
		for (PatientIdentifier pi : identifiers) {
			// skip voided identifiers
			if (pi.isVoided()) continue;
			
			// skip and remove invalid/empty identifiers
			if (pi.getIdentifier() == null || pi.getIdentifier().length() == 0) {
				patient.removeIdentifier(pi);
				continue;
			}
			
			// check this patient for duplicate identifiers
			if (pi.getIdentifierType().hasCheckDigit()) {
				if (identifiersUsed.contains(pi.getIdentifier()))
					throw new APIException("This patient has duplicate identifiers for: " + pi.getIdentifier());
				else
					identifiersUsed.add(pi.getIdentifier());
			}
				
			// compare against other identifiers matching the id string and id type
			Patient p = identifierInUse(pi.getIdentifier(), pi.getIdentifierType(), patient);
			if (p != null)
				throw new APIException("Identifier " + pi.getIdentifier() + " in use by patient #" + p.getPatientId());
			
			PatientIdentifierType pit = pi.getIdentifierType();
			String identifier = pi.getIdentifier();
			try {
				if (pit.hasCheckDigit() && !OpenmrsUtil.isValidCheckDigit(identifier)) {
					log.error("hasCheckDigit and is not valid: " + pit.getName() + " " + identifier);
					throw new APIException("Invalid check digit for identifier " + identifier);
				}
				else if (pit.hasCheckDigit() == false && identifier.contains("-")) {
					log.error("hasn't CheckDigit and contains '-': " + pit.getName() + " " + identifier);
					throw new APIException("Invalid character for non-checkdigit identifier " + identifier);
				}
			} catch (Exception e) {
				log.error("exception thrown with: " + pit.getName() + " " + identifier);
				log.error("Error while adding patient identifiers to savedIdentifier list", e);
				throw new APIException("Invalid identifier " + identifier);
			}
		}
		
	}
	
	public Patient identifierInUse(String identifier, PatientIdentifierType type, Patient ignorePatient) {
		List<PatientIdentifier> ids = getPatientIdentifiers(identifier, type);
		for (PatientIdentifier id : ids) {
			if (id.getIdentifierType().hasCheckDigit() && (ignorePatient == null || !id.getPatient().equals(ignorePatient)) )
				return id.getPatient();
		}
		
		return null; 
	}

	/**
	 * Find all patients with a given identifier
	 * 
	 * @param identifier
	 * @return set of patients matching identifier
	 * @throws APIException
	 */
	public Set<Patient> getPatientsByIdentifier(String identifier, boolean includeVoided) throws APIException {
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_VIEW_PATIENTS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_VIEW_PATIENTS);
		return getPatientDAO().getPatientsByIdentifier(identifier, includeVoided);
	}
	
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
	public Set<Patient> getPatientsByIdentifierPattern(String identifier, boolean includeVoided) throws APIException {
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_VIEW_PATIENTS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_VIEW_PATIENTS);
		return getPatientDAO().getPatientsByIdentifierPattern(identifier, includeVoided);
	}
	
	
	public Set<Patient> getPatientsByName(String name) throws APIException {
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_VIEW_PATIENTS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_VIEW_PATIENTS);
		return getPatientsByName(name, false);
	}
	/**
	 * Find patients by name
	 * 
	 * @param name
	 * @return set of patients matching name
	 * @throws APIException
	 */
	public Set<Patient> getPatientsByName(String name, boolean includeVoided) throws APIException {
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_VIEW_PATIENTS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_VIEW_PATIENTS);
		return getPatientDAO().getPatientsByName(name, includeVoided);
	}
	
	
	public Set<Patient> getSimilarPatients(String name, Integer birthyear, String gender) throws APIException {
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_VIEW_PATIENTS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_VIEW_PATIENTS);
		return getPatientDAO().getSimilarPatients(name, birthyear, gender);
	}
	
	
	/**
	 * Void patient record (functionally delete patient from system)
	 * 
	 * @param patient patient to be voided
	 * @param reason reason for voiding patient
	 */
	public void voidPatient(Patient patient, String reason) throws APIException {
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_EDIT_PATIENTS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_EDIT_PATIENTS);
		
		for (PatientName pn : patient.getNames()) {
			if (!pn.isVoided()) {
				pn.setVoided(true);
				pn.setVoidReason(reason);
			}
		}
		for (PatientAddress pa : patient.getAddresses()) {
			if (!pa.isVoided()) {
				pa.setVoided(true);
				pa.setVoidReason(reason);
			}
		}
		for (PatientIdentifier pi : patient.getIdentifiers()) {
			if (!pi.isVoided()) {
				pi.setVoided(true);
				pi.setVoidReason(reason);
			}
		}
		
		patient.setVoided(true);
		patient.setVoidedBy(Context.getAuthenticatedUser());
		patient.setDateVoided(new Date());
		patient.setVoidReason(reason);
		updatePatient(patient);
	}

	/**
	 * Unvoid patient record 
	 * 
	 * @param patient patient to be revived
	 */
	public void unvoidPatient(Patient patient) throws APIException {
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_EDIT_PATIENTS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_EDIT_PATIENTS);
		
		String voidReason = patient.getVoidReason();
		if (voidReason == null)
			voidReason = "";
		
		for (PatientName pn : patient.getNames()) {
			if (voidReason.equals(pn.getVoidReason())) {
				pn.setVoided(false);
				pn.setVoidReason(null);
			}
		}
		for (PatientAddress pa : patient.getAddresses()) {
			if (voidReason.equals(pa.getVoidReason())) {
				pa.setVoided(false);
				pa.setVoidReason(null);
			}
		}
		for (PatientIdentifier pi : patient.getIdentifiers()) {
			if (voidReason.equals(pi.getVoidReason())) {
				pi.setVoided(false);
				pi.setVoidReason(null);
			}
		}
		patient.setVoided(false);
		patient.setVoidedBy(null);
		patient.setDateVoided(null);
		patient.setVoidReason(null);
		updatePatient(patient);
	}
	
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
	public void deletePatient(Patient patient) throws APIException {
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_DELETE_PATIENTS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_DELETE_PATIENTS);
		getPatientDAO().deletePatient(patient);
	}
	
	/**
	 * Get all patientIdentifiers 
	 * 
	 * @param pit
	 * @return patientIdentifier list
	 * @throws APIException
	 */
	public List<PatientIdentifier> getPatientIdentifiers(PatientIdentifierType pit) throws APIException {
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_VIEW_PATIENTS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_VIEW_PATIENTS);
		
		return getPatientDAO().getPatientIdentifiers(pit);
	}
	
	/**
	 * Get Patient Identifiers matching the identifier and type 
	 * 
	 * @param identifier
	 * @param pit
	 * @return patientIdentifier list
	 * @throws APIException
	 */
	public List<PatientIdentifier> getPatientIdentifiers(String identifier, PatientIdentifierType pit) throws APIException {
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_VIEW_PATIENTS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_VIEW_PATIENTS);
		
		return getPatientDAO().getPatientIdentifiers(identifier, pit);
	}
	
	/**
	 * Update patient identifier
	 * 
	 * @param patient to be updated
	 * @throws APIException
	 */
	public void updatePatientIdentifier(PatientIdentifier pi) throws APIException {
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_EDIT_PATIENTS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_EDIT_PATIENTS);
		Patient p = identifierInUse(pi.getIdentifier(), pi.getIdentifierType(), pi.getPatient());
		if (p != null)
			throw new APIException("Identifier in use by patient #" + p.getPatientId());
		
		getPatientDAO().updatePatientIdentifier(pi);
	}
	
	/**
	 * Get all patientIdentifier types
	 * 
	 * @return patientIdentifier types list
	 * @throws APIException
	 */
	public List<PatientIdentifierType> getPatientIdentifierTypes() throws APIException {
		if (!Context.isAuthenticated())
			throw new APIAuthenticationException("Authentication required");
		
		return getPatientDAO().getPatientIdentifierTypes();
	}

	/**
	 * Get patientIdentifierType by internal identifier
	 * 
	 * @param patientIdentifierType id
	 * @return patientIdentifierType with given internal identifier
	 * @throws APIException
	 */
	public PatientIdentifierType getPatientIdentifierType(Integer patientIdentifierTypeId) throws APIException {
		if (!Context.isAuthenticated())
			throw new APIAuthenticationException("Authentication required");
		
		return getPatientDAO().getPatientIdentifierType(patientIdentifierTypeId);
	}

	/**
	 * Get patientIdentifierType by name
	 * 
	 * @param name
	 * @return patientIdentifierType with given name
	 * @throws APIException
	 */
	public PatientIdentifierType getPatientIdentifierType(String name) throws APIException {
		if (!Context.isAuthenticated())
			throw new APIAuthenticationException("Authentication required");
		
		return getPatientDAO().getPatientIdentifierType(name);
	}
	
	/**
	 * Get tribe by internal tribe identifier
	 * 
	 * @return Tribe
	 * @param tribeId 
	 * @throws APIException
	 */
	public Tribe getTribe(Integer tribeId) throws APIException {
		if (!Context.isAuthenticated())
			throw new APIAuthenticationException("Authentication required");
		
		return getPatientDAO().getTribe(tribeId);
	}
	
	/**
	 * Get list of tribes that are not retired
	 * 
	 * @return non-retired Tribe list
	 * @throws APIException
	 */
	public List<Tribe> getTribes() throws APIException {
		if (!Context.isAuthenticated())
			throw new APIAuthenticationException("Authentication required");
		
		return getPatientDAO().getTribes();
	}
	
	/**
	 * Find tribes by partial name lookup
	 * 
	 * @return non-retired Tribe list
	 * @throws APIException
	 */
	public List<Tribe> findTribes(String search) throws APIException {
		if (!Context.isAuthenticated())
			throw new APIAuthenticationException("Authentication required");
		
		return getPatientDAO().findTribes(search);
	}
	
	/**
	 * Get relationship by internal relationship identifier
	 * 
	 * @return Relationship
	 * @param relationshipId 
	 * @throws APIException
	 */
	public Relationship getRelationship(Integer relationshipId) throws APIException {
		if (!Context.isAuthenticated())
			throw new APIAuthenticationException("Authentication required");
		
		return getPatientDAO().getRelationship(relationshipId);
	}
	
	/**
	 * Get list of relationships that are not retired
	 * 
	 * @return non-voided Relationship list
	 * @throws APIException
	 */
	public List<Relationship> getRelationships() throws APIException {
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_RELATIONSHIPS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_MANAGE_RELATIONSHIPS);
		
		return getPatientDAO().getRelationships();
	}

	/**
	 * Get list of relationships that include Person in person_id or relative_id
	 * 
	 * @return Relationship list
	 * @throws APIException
	 */
	public List<Relationship> getRelationships(Person p, boolean showVoided) throws APIException {
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_RELATIONSHIPS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_MANAGE_RELATIONSHIPS);
		
		return getPatientDAO().getRelationships(p, showVoided);
	}
	
	public List<Relationship> getRelationships(Person p) throws APIException {
		return getRelationships(p, true);
	}

	/**
	 * Get list of relationships that have Person as relative_id, and the given type (which can be null)
	 * @return Relationship list
	 */
	public List<Relationship> getRelationshipsTo(Person toPerson, RelationshipType relType) throws APIException {
		List<Relationship> temp = getRelationships(toPerson);
		List<Relationship> ret = new ArrayList<Relationship>();
		for (Relationship rel : temp) {
			if (rel.getRelative().equals(toPerson) &&
					(relType == null || relType.equals(rel.getRelationship()))) {
				ret.add(rel);
			}
		}
		return ret;
	}
	
	/**
	 * Get all relationshipTypes
	 * 
	 * @return relationshipType list
	 * @throws APIException
	 */
	public List<RelationshipType> getRelationshipTypes() throws APIException {
		if (!Context.isAuthenticated())
			throw new APIAuthenticationException("Authentication required");
		
		return getPatientDAO().getRelationshipTypes();
	}
	

	/**
	 * Get relationshipType by internal identifier
	 * 
	 * @param relationshipType id
	 * @return relationshipType with given internal identifier
	 * @throws APIException
	 */
	public RelationshipType getRelationshipType(Integer relationshipTypeId) throws APIException {
		// TODO use 'Authenticated User' option
		if (!Context.isAuthenticated())
			throw new APIAuthenticationException("Authentication required");
		
		return getPatientDAO().getRelationshipType(relationshipTypeId);
	}
	
	/**
	 * Find relationshipType by name
	 * @throws APIException
	 */
	public RelationshipType findRelationshipType(String relationshipTypeName) throws APIException {
		// TODO use 'Authenticated User' option
		if (!Context.isAuthenticated())
			throw new APIAuthenticationException("Authentication required");
		
		return getPatientDAO().findRelationshipType(relationshipTypeName);
	}
	
	public List<Patient> findPatients(String query, boolean includeVoided) {
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_VIEW_PATIENTS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_VIEW_PATIENTS);
		
		List<Patient> patients = new Vector<Patient>();
		PatientDAO dao = getPatientDAO();
		
		//query must be more than 2 characters
		if (query.length() < 3)
			return patients;
		
		// if there is a number in the query string
		if (query.matches(".*\\d+.*")) {
			log.debug("[Identifier search] Query: " + query);
			patients.addAll(dao.getPatientsByIdentifierPattern(query, includeVoided));
		}
		else {
			//there is no number in the string, search on name
			patients.addAll(dao.getPatientsByName(query, includeVoided));
		}
		return patients;
	}
	
	/**
	 * Search the database for patients that share the given attributes
	 * attributes similar to: [gender, tribe, givenName, middleName, familyname]
	 * 
	 * @param attributes
	 * @return list of patients that match other patients
	 */
	public List<Patient> findDuplicatePatients(Set<String> attributes) {
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_VIEW_PATIENTS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_VIEW_PATIENTS);
		
		PatientDAO dao = getPatientDAO();
		return dao.findDuplicatePatients(attributes);
	}
	
	/**
	 * 1) Moves object (encounters/obs) pointing to <code>nonPreferred</code> to <code>preferred</code>
	 * 2) Copies data (gender/birthdate/names/ids/etc) from <code>nonPreferred</code> to 
	 * <code>preferred</code> iff the data is missing or null in <code>preferred</code>
	 * 3) <code>notPreferred</code> is marked as voided
	 * @param preferred
	 * @param notPreferred
	 * @throws APIException
	 */
	public void mergePatients(Patient preferred, Patient notPreferred) throws APIException {
		log.debug("Merging patients: (preferred)" + preferred.getPatientId() + ", (notPreferred) " + notPreferred.getPatientId());
		
		// change all encounters
		EncounterService es = Context.getEncounterService();
		for (Encounter e : es.getEncounters(notPreferred)){
			e.setPatient(preferred);
			log.debug("Merging encounter " + e.getEncounterId() + " to " + preferred.getPatientId());
			es.updateEncounter(e);
		}
		
		// move all identifiers
		for (PatientIdentifier pi : notPreferred.getIdentifiers()) {
			PatientIdentifier tmpIdentifier = new PatientIdentifier();
			tmpIdentifier.setIdentifier(pi.getIdentifier());
			tmpIdentifier.setIdentifierType(null); // don't compare identifier types.
			tmpIdentifier.setLocation(pi.getLocation());
			tmpIdentifier.setPatient(preferred);
			boolean found = false;
			for (PatientIdentifier preferredIdentifier : preferred.getIdentifiers()) {
				if (preferredIdentifier.getIdentifier() != null &&
					preferredIdentifier.getIdentifier().equals(tmpIdentifier.getIdentifier()) &&
					preferredIdentifier.getPatient() != null &&
					preferredIdentifier.getPatient().equals(tmpIdentifier.getPatient()))
						found = true;
			}
			if (!found) {
				tmpIdentifier.setIdentifierType(pi.getIdentifierType());
				tmpIdentifier.setCreator(Context.getAuthenticatedUser());
				tmpIdentifier.setDateCreated(new Date());
				tmpIdentifier.setVoided(false);
				tmpIdentifier.setVoidedBy(null);
				tmpIdentifier.setVoidReason(null);
				preferred.addIdentifier(tmpIdentifier);
				log.debug("Merging identifier " + tmpIdentifier.getIdentifier() + " to " + preferred.getPatientId());
			}
		}
		
		// move all names
		for (PatientName newName : notPreferred.getNames()) {
			boolean containsName = false;
			for (PatientName currentName : preferred.getNames()) {
				String given = newName.getGivenName();
				String middle = newName.getMiddleName();
				String family = newName.getFamilyName();
				
				if ((given != null && given.equals(currentName.getGivenName())) &&
					(middle != null && middle.equals(currentName.getMiddleName())) &&
					(family != null && family.equals(currentName.getFamilyName()))	
						) {
					containsName = true;
				}
			}
			if (!containsName) {
				PatientName tmpName = PatientName.newInstance(newName);
				tmpName.setPatientNameId(null);
				tmpName.setVoided(false);
				tmpName.setVoidedBy(null);
				tmpName.setVoidReason(null);
				preferred.addName(tmpName);
				log.debug("Merging name " + newName.getGivenName() + " to " + preferred.getPatientId());
			}
		}
		
		// move all addresses
		for (PatientAddress newAddress : notPreferred.getAddresses()) {
			boolean containsAddress = false;
			for (PatientAddress currentAddress : preferred.getAddresses()) {
				String address1 = currentAddress.getAddress1();
				String address2 = currentAddress.getAddress2();
				String cityVillage = currentAddress.getCityVillage();
				
				if ((address1 != null && address1.equals(newAddress.getAddress1())) ||
					(address2 != null && address2.equals(newAddress.getAddress2())) ||
					(cityVillage != null && cityVillage.equals(newAddress.getCityVillage()))	
						) {
					containsAddress = true;
				}
			}
			if (!containsAddress) {
				PatientAddress tmpAddress = (PatientAddress)newAddress.clone();
				tmpAddress.setPatientAddressId(null);
				tmpAddress.setVoided(false);
				tmpAddress.setVoidedBy(null);
				tmpAddress.setVoidReason(null);
				preferred.addAddress(tmpAddress);
				log.debug("Merging address " + newAddress.getPatientAddressId() + " to " + preferred.getPatientId());
			}
		}
		
		// move all other patient info
		
		if (!"M".equals(preferred.getGender()) && !"F".equals(preferred.getGender()))
			preferred.setGender(notPreferred.getGender());
		
		if (preferred.getRace() == null || preferred.getRace().equals(""))
			preferred.setRace(notPreferred.getRace());
		
		if (preferred.getBirthdate() == null || preferred.getBirthdate().equals("") ||
				( preferred.getBirthdateEstimated() && !notPreferred.getBirthdateEstimated())) {
			preferred.setBirthdate(notPreferred.getBirthdate());
			preferred.setBirthdateEstimated(notPreferred.getBirthdateEstimated());
		}
		
		if (preferred.getBirthplace() == null || preferred.getBirthplace().equals(""))
			preferred.setBirthplace(notPreferred.getBirthplace());
		
		if (preferred.getTribe() == null)
			preferred.setTribe(notPreferred.getTribe());
		
		if (preferred.getCitizenship() == null || preferred.getCitizenship().equals(""))
			preferred.setCitizenship(notPreferred.getCitizenship());
		
		if (preferred.getMothersName() == null || preferred.getMothersName().equals(""))
			preferred.setMothersName(notPreferred.getMothersName());
		
		if (preferred.getCivilStatus() == null)
			preferred.setCivilStatus(notPreferred.getCivilStatus());
		
		if (preferred.getDeathDate() == null || preferred.getDeathDate().equals(""))
			preferred.setDeathDate(notPreferred.getDeathDate());
		
		if (preferred.getCauseOfDeath() == null || preferred.getCauseOfDeath().equals(""))
			preferred.setCauseOfDeath(notPreferred.getCauseOfDeath());
		
		if (preferred.getHealthDistrict() == null || preferred.getHealthDistrict().equals(""))
			preferred.setHealthDistrict(notPreferred.getHealthDistrict());
		
		if (preferred.getHealthCenter() == null)
			preferred.setHealthCenter(notPreferred.getHealthCenter());
		
		// void the non preferred patient
		voidPatient(notPreferred, "Merged with patient #" + preferred.getPatientId());
		
		// Save the newly update preferred patient
		// This must be called _after_ voiding the nonPreferred patient so that
		//  a "Duplicate Identifier" error doesn't pop up.
		updatePatient(preferred);
		
	}
	
	/**
	 * Iterates over Names/Addresses/Identifiers to set dateCreated and creator properties if needed
	 * @param patient
	 */
	private void setCollectionProperties(Patient patient) {
		if (patient.getCreator() == null) {
			patient.setCreator(Context.getAuthenticatedUser());
			patient.setDateCreated(new Date());
		}
		patient.setChangedBy(Context.getAuthenticatedUser());
		patient.setDateChanged(new Date());
		if (patient.getAddresses() != null)
			for (PatientAddress pAddress : patient.getAddresses()) {
				if (pAddress.getDateCreated() == null) {
					pAddress.setDateCreated(new Date());
					pAddress.setCreator(Context.getAuthenticatedUser());
					pAddress.setPatient(patient);
				}
			}
		if (patient.getNames() != null)
			for (PatientName pName : patient.getNames()) {
				if (pName.getDateCreated() == null) {
					pName.setDateCreated(new Date());
					pName.setCreator(Context.getAuthenticatedUser());
					pName.setPatient(patient);
				}
			}
		if (patient.getIdentifiers() != null)
			for (PatientIdentifier pIdentifier : patient.getIdentifiers()) {
				if (pIdentifier.getDateCreated() == null) {
					pIdentifier.setDateCreated(new Date());
					pIdentifier.setCreator(Context.getAuthenticatedUser());
					pIdentifier.setPatient(patient);
				}
			}
	}

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
	public void exitFromCare(Patient patient, Date dateExited, Concept reasonForExit) {
		if ( patient != null && dateExited != null && reasonForExit != null ) {

			// need to add a new (terminal) state to patient workflows
			List<PatientProgram> programs = (List<PatientProgram>)Context.getProgramWorkflowService().getPatientPrograms(patient);
			PatientProgramSupport support = PatientProgramSupport.getInstance();
			Set<Concept> resultingStates = support.getStatesByReason(reasonForExit);
			
			if ( programs != null && resultingStates != null ) {
				for ( PatientProgram program : programs ) {
					for ( ProgramWorkflow workflow : program.getProgram().getWorkflows() ) {
						Set<ProgramWorkflowState> wfStates = workflow.getStates();
						for ( ProgramWorkflowState wfState : wfStates ) {
							if ( resultingStates.contains(wfState.getConcept()) ) {
								Context.getProgramWorkflowService().changeToState(program, workflow, wfState, dateExited );
							}
						}
					}
				}
			} else {
				log.debug("Patient has no programs");
			}
			
			// need to discontinue any open orders for this patient
			Context.getOrderService().discontinueAllOrders(patient, reasonForExit, dateExited);
		} else {
			if ( patient == null )
					throw new APIException("Attempting to set an invalid patient's status to 'dead'");
			if ( dateExited == null ) 
					throw new APIException("Must supply a valid dateDied when indicating that a patient has died");
			if ( reasonForExit == null ) 
					throw new APIException("Must supply a valid causeOfDeath (even if 'Unknown') when indicating that a patient has died");
		}
	}

	/**
	 * This is the way to establish that a patient has died.  In addition to exiting the patient from care (see above),
	 * this method will also set the appropriate patient characteristics to indicate that they have died, when they died, etc.
	 * @param patient - the patient who has died
	 * @param dateDied - the declared date/time of the patient's death
	 * @param causeOfDeath - the concept that corresponds with the reason the patient died
	 * @throws APIException
	 */
	public void processDeath(Patient patient, Date dateDied, Concept causeOfDeath) {
		if ( patient != null && dateDied != null && causeOfDeath != null ) {
			// set appropriate patient characteristics
			patient.setDead(true);
			patient.setDeathDate(dateDied);
			// TODO: cause of death should be a concept
			patient.setCauseOfDeath(causeOfDeath);
			updatePatient(patient);
			
			// exit from program
			exitFromCare(patient, dateDied, new Concept());
		} else {
			if ( patient == null )
					throw new APIException("Attempting to set an invalid patient's status to 'dead'");
			if ( dateDied == null ) 
					throw new APIException("Must supply a valid dateDied when indicating that a patient has died");
			if ( causeOfDeath == null ) 
					throw new APIException("Must supply a valid causeOfDeath (even if 'Unknown') when indicating that a patient has died");
		}
	}

}

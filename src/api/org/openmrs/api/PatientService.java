package org.openmrs.api;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Encounter;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientAddress;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PatientName;
import org.openmrs.Person;
import org.openmrs.Relationship;
import org.openmrs.RelationshipType;
import org.openmrs.Tribe;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.DAOContext;
import org.openmrs.api.db.PatientDAO;
import org.openmrs.util.OpenmrsConstants;

/**
 * Patient-related services
 * 
 * @author Ben Wolfe
 * @author Burke Mamlin
 * @vesrion 1.0
 */
public class PatientService {

	private Log log = LogFactory.getLog(this.getClass());
	
	private Context context;
	private DAOContext daoContext;
	
	public PatientService(Context c, DAOContext d) {
		this.context = c;
		this.daoContext = d;
	}
	
	private PatientDAO getPatientDAO() {
		return daoContext.getPatientDAO();
	}
	
	/**
	 * Creates a new patient record
	 * 
	 * @param patient to be created
	 * @throws APIException
	 */
	public void createPatient(Patient patient) throws APIException {
		if (log.isDebugEnabled()) {
			String s = patient.getPatientId().toString();
			if (patient.getIdentifiers() != null)
				s += "|" + patient.getIdentifiers();
			
			log.debug(s);
		}
		
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_ADD_PATIENTS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_ADD_PATIENTS);
		
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
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_VIEW_PATIENTS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_VIEW_PATIENTS);
		return getPatientDAO().getPatient(patientId);
	}

	/**
	 * Update patient 
	 * 
	 * @param patient to be updated
	 * @throws APIException
	 */
	public void updatePatient(Patient patient) throws APIException {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_EDIT_PATIENTS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_EDIT_PATIENTS);
		
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
		
		// Check for duplicate identifiers
		for (Object obj : patient.getIdentifiers().toArray()) {
			PatientIdentifier pi = (PatientIdentifier)obj;
			if (!pi.isVoided()) {
				if (pi.getIdentifier() == null || pi.getIdentifier().length() == 0) {
					patient.removeIdentifier(pi);
					continue;
				}
				List<PatientIdentifier> ids = getPatientIdentifiers(pi.getIdentifier(), pi.getIdentifierType());
				for (PatientIdentifier id : ids) {
					if (!id.getIdentifierType().hasCheckDigit() || id.getPatient().equals(patient))
						continue;
					log.debug("Patient: " + patient.getPatientId());
					log.debug("Identifier: " + pi);
					throw new APIException("Identifier " + pi.getIdentifier() + " in use by patient #" + id.getPatient().getPatientId());
				}
			}
		}
		
	}

	/**
	 * Find all patients with a given identifier
	 * 
	 * @param identifier
	 * @return set of patients matching identifier
	 * @throws APIException
	 */
	public Set<Patient> getPatientsByIdentifier(String identifier, boolean includeVoided) throws APIException {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_VIEW_PATIENTS))
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
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_VIEW_PATIENTS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_VIEW_PATIENTS);
		return getPatientDAO().getPatientsByIdentifierPattern(identifier, includeVoided);
	}
	
	
	public Set<Patient> getPatientsByName(String name) throws APIException {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_VIEW_PATIENTS))
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
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_VIEW_PATIENTS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_VIEW_PATIENTS);
		return getPatientDAO().getPatientsByName(name, includeVoided);
	}
	
	
	public Set<Patient> getSimilarPatients(String name, Integer birthyear, String gender) throws APIException {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_VIEW_PATIENTS))
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
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_EDIT_PATIENTS))
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
		patient.setVoidedBy(context.getAuthenticatedUser());
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
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_EDIT_PATIENTS))
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
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_DELETE_PATIENTS))
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
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_VIEW_PATIENTS))
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
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_VIEW_PATIENTS))
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
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_EDIT_PATIENTS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_EDIT_PATIENTS);
		getPatientDAO().updatePatientIdentifier(pi);
	}
	
	/**
	 * Get all patientIdentifier types
	 * 
	 * @return patientIdentifier types list
	 * @throws APIException
	 */
	public List<PatientIdentifierType> getPatientIdentifierTypes() throws APIException {
		if (!context.isAuthenticated())
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
		if (!context.isAuthenticated())
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
		if (!context.isAuthenticated())
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
		if (!context.isAuthenticated())
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
		if (!context.isAuthenticated())
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
		if (!context.isAuthenticated())
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
		if (!context.isAuthenticated())
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
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_RELATIONSHIPS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_MANAGE_RELATIONSHIPS);
		
		return getPatientDAO().getRelationships();
	}
	
	/**
	 * Get list of relationships that include Person in person_id or relative_id
	 * 
	 * @return Relationship list
	 * @throws APIException
	 */
	public List<Relationship> getRelationships(Person p) throws APIException {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_RELATIONSHIPS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_MANAGE_RELATIONSHIPS);
		
		return getPatientDAO().getRelationships(p);
	}
	
	/**
	 * Get all relationshipTypes
	 * 
	 * @return relationshipType list
	 * @throws APIException
	 */
	public List<RelationshipType> getRelationshipTypes() throws APIException {
		if (!context.isAuthenticated())
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
		if (!context.isAuthenticated())
			throw new APIAuthenticationException("Authentication required");
		
		return getPatientDAO().getRelationshipType(relationshipTypeId);
	}
	
	/**
	 * Get all locations
	 * 
	 * @return location list
	 * @throws APIException
	 */
	public List<Location> getLocations() throws APIException {
		if (!context.isAuthenticated())
			throw new APIAuthenticationException("Authentication required");
		
		return getPatientDAO().getLocations();
	}

	/**
	 * Get location by internal identifier
	 * 
	 * @param location id
	 * @return location with given internal identifier
	 * @throws APIException
	 */
	public Location getLocation(Integer locationId) throws APIException {
		// TODO use 'Authenticated User' option
		if (!context.isAuthenticated())
			throw new APIAuthenticationException("Authentication required");
		
		return getPatientDAO().getLocation(locationId);
	}
	
	/**
	 * Get location by name
	 * 
	 * @param name location's name
	 * @return location with given name
	 * @throws APIException
	 */
	public Location getLocationByName(String name) throws APIException {
		if (!context.isAuthenticated())
			throw new APIAuthenticationException("Authentication required");
		
		return getPatientDAO().getLocationByName(name);
	}
	
	public List<Patient> findPatients(String query, boolean includeVoided) {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_VIEW_PATIENTS))
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
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_VIEW_PATIENTS))
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
		EncounterService es = context.getEncounterService();
		for (Encounter e : es.getEncounters(notPreferred)){
			e.setPatient(preferred);
			log.debug("Merging encounter " + e.getEncounterId() + " to " + preferred.getPatientId());
			es.updateEncounter(e);
		}
		
		// move all identifiers
		for (PatientIdentifier pi : notPreferred.getIdentifiers()) {
			PatientIdentifier tmpIdentifier = new PatientIdentifier();
			tmpIdentifier.setIdentifier(pi.getIdentifier());
			tmpIdentifier.setIdentifierType(pi.getIdentifierType());
			tmpIdentifier.setLocation(pi.getLocation());
			tmpIdentifier.setPatient(preferred);
			if (!preferred.getIdentifiers().contains(tmpIdentifier)) {
				tmpIdentifier.setCreator(context.getAuthenticatedUser());
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
}

/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.impl;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.Allergen;
import org.openmrs.Allergies;
import org.openmrs.Allergy;
import org.openmrs.BaseOpenmrsMetadata;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PatientProgram;
import org.openmrs.Person;
import org.openmrs.PersonAddress;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonName;
import org.openmrs.Relationship;
import org.openmrs.User;
import org.openmrs.Visit;
import org.openmrs.api.APIException;
import org.openmrs.api.BlankIdentifierException;
import org.openmrs.api.DuplicateIdentifierException;
import org.openmrs.api.EncounterService;
import org.openmrs.api.InsufficientIdentifiersException;
import org.openmrs.api.MissingRequiredIdentifierException;
import org.openmrs.api.ObsService;
import org.openmrs.api.PatientIdentifierException;
import org.openmrs.api.PatientIdentifierTypeLockedException;
import org.openmrs.api.PatientService;
import org.openmrs.api.PersonService;
import org.openmrs.api.ProgramWorkflowService;
import org.openmrs.api.UserService;
import org.openmrs.api.VisitService;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.PatientDAO;
import org.openmrs.parameter.EncounterSearchCriteria;
import org.openmrs.parameter.EncounterSearchCriteriaBuilder;
import org.openmrs.patient.IdentifierValidator;
import org.openmrs.patient.impl.LuhnIdentifierValidator;
import org.openmrs.person.PersonMergeLog;
import org.openmrs.person.PersonMergeLogData;
import org.openmrs.serialization.SerializationException;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsUtil;
import org.openmrs.util.PrivilegeConstants;
import org.openmrs.validator.PatientIdentifierValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Default implementation of the patient service. This class should not be used on its own. The
 * current OpenMRS implementation should be fetched from the Context via
 * <code>Context.getPatientService()</code>
 * 
 * @see org.openmrs.api.context.Context
 * @see org.openmrs.api.PatientService
 * @see org.openmrs.api.PersonService
 */
@Transactional
public class PatientServiceImpl extends BaseOpenmrsService implements PatientService {
	
	private static final Logger log = LoggerFactory.getLogger(PatientServiceImpl.class);
	
	private PatientDAO dao;
	
	/**
	 * PatientIdentifierValidators registered through spring's applicationContext-service.xml
	 */
	private static Map<Class<? extends IdentifierValidator>, IdentifierValidator> identifierValidators = null;
	
	/**
	 * @see org.openmrs.api.PatientService#setPatientDAO(org.openmrs.api.db.PatientDAO)
	 */
	@Override
	public void setPatientDAO(PatientDAO dao) {
		this.dao = dao;
	}
	
	/**
	 * Clean up after this class. Set the static var to null so that the classloader can reclaim the
	 * space.
	 * 
	 * @see org.openmrs.api.impl.BaseOpenmrsService#onShutdown()
	 */
	@Override
	public void onShutdown() {
		setIdentifierValidators(null);
	}
	
	/**
	 * @see org.openmrs.api.PatientService#savePatient(org.openmrs.Patient)
	 */
	@Override
	public Patient savePatient(Patient patient) throws APIException {
		requireAppropriatePatientModificationPrivilege(patient);

		if (!patient.getVoided() && patient.getIdentifiers().size() == 1) {
			patient.getPatientIdentifier().setPreferred(true);
		}

		if (!patient.getVoided()) {
			checkPatientIdentifiers(patient);
		}

		setPreferredPatientIdentifier(patient);
		setPreferredPatientName(patient);
		setPreferredPatientAddress(patient);

		return dao.savePatient(patient);
	}

	private void requireAppropriatePatientModificationPrivilege(Patient patient) {
		if (patient.getPatientId() == null) {
			Context.requirePrivilege(PrivilegeConstants.ADD_PATIENTS);
		} else {
			Context.requirePrivilege(PrivilegeConstants.EDIT_PATIENTS);
		}
		if (patient.getVoided()) {
			Context.requirePrivilege(PrivilegeConstants.DELETE_PATIENTS);
		}
	}

	private void setPreferredPatientIdentifier(Patient patient) {
		PatientIdentifier preferredIdentifier = null;
		PatientIdentifier possiblePreferredId = patient.getPatientIdentifier();
		if (possiblePreferredId != null && possiblePreferredId.getPreferred() && !possiblePreferredId.getVoided()) {
			preferredIdentifier = possiblePreferredId;
		}

		for (PatientIdentifier id : patient.getIdentifiers()) {
			if (preferredIdentifier == null && !id.getVoided()) {
				id.setPreferred(true);
				preferredIdentifier = id;
				continue;
			}

			if (!id.equals(preferredIdentifier)) {
				id.setPreferred(false);
			}
		}
	}

	private void setPreferredPatientName(Patient patient) {
		PersonName preferredName = null;
		PersonName possiblePreferredName = patient.getPersonName();
		if (possiblePreferredName != null && possiblePreferredName.getPreferred() && !possiblePreferredName.getVoided()) {
			preferredName = possiblePreferredName;
		}

		for (PersonName name : patient.getNames()) {
			if (preferredName == null && !name.getVoided()) {
				name.setPreferred(true);
				preferredName = name;
				continue;
			}

			if (!name.equals(preferredName)) {
				name.setPreferred(false);
			}
		}
	}
	
	private void setPreferredPatientAddress(Patient patient) {
		PersonAddress preferredAddress = null;
		PersonAddress possiblePreferredAddress = patient.getPersonAddress();
		if (possiblePreferredAddress != null && possiblePreferredAddress.getPreferred()
				&& !possiblePreferredAddress.getVoided()) {
			preferredAddress = possiblePreferredAddress;
		}

		for (PersonAddress address : patient.getAddresses()) {
			if (preferredAddress == null && !address.getVoided()) {
				address.setPreferred(true);
				preferredAddress = address;
				continue;
			}

			if (!address.equals(preferredAddress)) {
				address.setPreferred(false);
			}
		}
	}
	
	/**
	 * @see org.openmrs.api.PatientService#getPatient(java.lang.Integer)
	 */
	@Override
	@Transactional(readOnly = true)
	public Patient getPatient(Integer patientId) throws APIException {
		return dao.getPatient(patientId);
	}
	
	@Override
	@Transactional(readOnly = true)
	public Patient getPatientOrPromotePerson(Integer patientOrPersonId) {
		Patient patient = null;
		try {
			patient = Context.getPatientService().getPatient(patientOrPersonId);
		}
		catch (ClassCastException ex) {
			// If the id refers to Person not Patient, it sometimes will cause class cast exception
			// We will attempt to retrieve the Person and promote to Patient
		}
		if (patient == null) {
			Person toPromote = Context.getPersonService().getPerson(patientOrPersonId);
			if (toPromote != null) {
				patient = new Patient(toPromote);
			}
		}
		return patient;
	}

	/**
	 * @see org.openmrs.api.PatientService#getAllPatients()
	 */
	@Override
	@Transactional(readOnly = true)
	public List<Patient> getAllPatients() throws APIException {
		return Context.getPatientService().getAllPatients(false);
	}
	
	/**
	 * @see org.openmrs.api.PatientService#getAllPatients(boolean)
	 */
	@Override
	@Transactional(readOnly = true)
	public List<Patient> getAllPatients(boolean includeVoided) throws APIException {
		return dao.getAllPatients(includeVoided);
	}
	
	/**
	 * @see org.openmrs.api.PatientService#getPatients(java.lang.String, java.lang.String,
	 *      java.util.List, boolean)
	 */
	@Override
	// TODO - search for usage with non-empty list of patient identifier types
	@Transactional(readOnly = true)
	public List<Patient> getPatients(String name, String identifier, List<PatientIdentifierType> identifierTypes,
	        boolean matchIdentifierExactly) throws APIException {
		
		return Context.getPatientService().getPatients(name, identifier, identifierTypes, matchIdentifierExactly, 0, null);
	}
	
	/**
	 * @see org.openmrs.api.PatientService#checkPatientIdentifiers(org.openmrs.Patient)
	 */
	@Override
	@Transactional(readOnly = true)
	public void checkPatientIdentifiers(Patient patient) throws PatientIdentifierException {
		// check patient has at least one identifier
		if (!patient.getVoided() && patient.getActiveIdentifiers().isEmpty()) {
			throw new InsufficientIdentifiersException("At least one nonvoided Patient Identifier is required");
		}

		final List<PatientIdentifier> patientIdentifiers = new ArrayList<>(patient.getIdentifiers());

		final Set<String> uniqueIdentifiers = new HashSet<>();

		patientIdentifiers.stream()
			.filter(pi -> !pi.getVoided())
			.forEach(pi -> {
				try {
					PatientIdentifierValidator.validateIdentifier(pi);
				}
				catch (BlankIdentifierException bie) {
					patient.removeIdentifier(pi);
					throw bie;
				}

				// check this patient for duplicate identifiers+identifierType
				String compareString = pi.getIdentifier() + " id type #: " + pi.getIdentifierType().getPatientIdentifierTypeId();
				if(! uniqueIdentifiers.add(compareString)) {
					throw new DuplicateIdentifierException("This patient has two identical identifiers of type "
							+ compareString, pi);
				}
			});

		checkForMissingRequiredIdentifiers(patientIdentifiers);

	}

	private void checkForMissingRequiredIdentifiers(List<PatientIdentifier> patientIdentifiers) {
		final Set<PatientIdentifierType> patientIdentifierTypes =
				patientIdentifiers.stream()
						.map(PatientIdentifier::getIdentifierType)
						.collect(Collectors.toSet());

		final List<PatientIdentifierType> requiredTypes = this.getPatientIdentifierTypes(null, null, true, null);
		final Set<String> missingRequiredTypeNames =
				requiredTypes.stream()
						.filter(requiredType -> !patientIdentifierTypes.contains(requiredType))
						.map(BaseOpenmrsMetadata::getName)
						.collect(Collectors.toSet());

		if(! missingRequiredTypeNames.isEmpty()) {
			throw new MissingRequiredIdentifierException(
					"Patient is missing the following required identifier(s): " + String.join(", ", missingRequiredTypeNames));
		}
	}

	/**
	 * @see org.openmrs.api.PatientService#voidPatient(org.openmrs.Patient, java.lang.String)
	 */
	@Override
	public Patient voidPatient(Patient patient, String reason) throws APIException {
		if (patient == null) {
			return null;
		}
		
		// patient and patientidentifier attributes taken care of by the BaseVoidHandler
		//call the DAO layer directly to avoid any further AOP around save*
		return dao.savePatient(patient);
	}
	
	/**
	 * @see org.openmrs.api.PatientService#unvoidPatient(org.openmrs.Patient)
	 */
	@Override
	public Patient unvoidPatient(Patient patient) throws APIException {
		if (patient == null) {
			return null;
		}
		
		// patient and patientidentifier attributes taken care of by the BaseUnvoidHandler
		
		return Context.getPatientService().savePatient(patient);
	}
	
	/**
	 * @see org.openmrs.api.PatientService#purgePatient(org.openmrs.Patient)
	 */
	@Override
	public void purgePatient(Patient patient) throws APIException {
		dao.deletePatient(patient);
	}
	
	// patient identifier section
	
	/**
	 * @see org.openmrs.api.PatientService#getPatientIdentifiers(java.lang.String, java.util.List,
	 *      java.util.List, java.util.List, java.lang.Boolean)
	 */
	@Override
	@Transactional(readOnly = true)
	public List<PatientIdentifier> getPatientIdentifiers(String identifier,
	        List<PatientIdentifierType> patientIdentifierTypes, List<Location> locations, List<Patient> patients,
	        Boolean isPreferred) throws APIException {
		
		if (patientIdentifierTypes == null) {
			patientIdentifierTypes = new ArrayList<>();
		}
		
		if (locations == null) {
			locations = new ArrayList<>();
		}
		
		if (patients == null) {
			patients = new ArrayList<>();
		}
		
		return dao.getPatientIdentifiers(identifier, patientIdentifierTypes, locations, patients, isPreferred);
	}
	// end patient identifier section
	
	// patient identifier _type_ section
	
	/**
	 * 
	 * @see org.openmrs.api.PatientService#savePatientIdentifierType(org.openmrs.PatientIdentifierType)
	 */
	@Override
	public PatientIdentifierType savePatientIdentifierType(PatientIdentifierType patientIdentifierType) throws APIException {
		checkIfPatientIdentifierTypesAreLocked();
		return dao.savePatientIdentifierType(patientIdentifierType);
	}
	
	/**
	 * @see org.openmrs.api.PatientService#getAllPatientIdentifierTypes()
	 */
	@Override
	@Transactional(readOnly = true)
	public List<PatientIdentifierType> getAllPatientIdentifierTypes() throws APIException {
		return Context.getPatientService().getAllPatientIdentifierTypes(false);
	}
	
	/**
	 * @see org.openmrs.api.PatientService#getAllPatientIdentifierTypes(boolean)
	 */
	@Override
	@Transactional(readOnly = true)
	public List<PatientIdentifierType> getAllPatientIdentifierTypes(boolean includeRetired) throws APIException {
		return dao.getAllPatientIdentifierTypes(includeRetired);
	}
	
	/**
	 * @see org.openmrs.api.PatientService#getPatientIdentifierTypes(java.lang.String,
	 *      java.lang.String, java.lang.Boolean, java.lang.Boolean)
	 */
	@Override
	@Transactional(readOnly = true)
	public List<PatientIdentifierType> getPatientIdentifierTypes(String name, String format, Boolean required,
	        Boolean hasCheckDigit) throws APIException {
		List<PatientIdentifierType> patientIdentifierTypes = dao.getPatientIdentifierTypes(name, format, required, hasCheckDigit);
		if (patientIdentifierTypes == null) {
			return new ArrayList<>();
		}
		return patientIdentifierTypes;
	}
	
	/**
	 * @see org.openmrs.api.PatientService#getPatientIdentifierType(java.lang.Integer)
	 */
	@Override
	@Transactional(readOnly = true)
	public PatientIdentifierType getPatientIdentifierType(Integer patientIdentifierTypeId) throws APIException {
		return dao.getPatientIdentifierType(patientIdentifierTypeId);
	}
	
	/**
	 * @see org.openmrs.api.PatientService#getPatientIdentifierTypeByName(java.lang.String)
	 */
	@Override
	@Transactional(readOnly = true)
	public PatientIdentifierType getPatientIdentifierTypeByName(String name) throws APIException {
		List<PatientIdentifierType> types = getPatientIdentifierTypes(name, null, null, null);
		
		if (!types.isEmpty()) {
			return types.get(0);
		}
		
		return null;
	}
	
	/**
	 * @see org.openmrs.api.PatientService#retirePatientIdentifierType(org.openmrs.PatientIdentifierType,
	 *      String)
	 */
	@Override
	public PatientIdentifierType retirePatientIdentifierType(PatientIdentifierType patientIdentifierType, String reason)
	        throws APIException {
		checkIfPatientIdentifierTypesAreLocked();
		if (reason == null || reason.length() < 1) {
			throw new APIException("Patient.identifier.retire.reason", (Object[]) null);
		}
		
		patientIdentifierType.setRetired(true);
		patientIdentifierType.setRetiredBy(Context.getAuthenticatedUser());
		patientIdentifierType.setDateRetired(new Date());
		patientIdentifierType.setRetireReason(reason);
		return Context.getPatientService().savePatientIdentifierType(patientIdentifierType);
	}
	
	/**
	 * @see org.openmrs.api.PatientService#unretirePatientIdentifierType(org.openmrs.PatientIdentifierType)
	 */
	@Override
	public PatientIdentifierType unretirePatientIdentifierType(PatientIdentifierType patientIdentifierType)
	        throws APIException {
		checkIfPatientIdentifierTypesAreLocked();
		patientIdentifierType.setRetired(false);
		patientIdentifierType.setRetiredBy(null);
		patientIdentifierType.setDateRetired(null);
		patientIdentifierType.setRetireReason(null);
		return Context.getPatientService().savePatientIdentifierType(patientIdentifierType);
	}
	
	/**
	 * @see org.openmrs.api.PatientService#purgePatientIdentifierType(org.openmrs.PatientIdentifierType)
	 */
	@Override
	public void purgePatientIdentifierType(PatientIdentifierType patientIdentifierType) throws APIException {
		checkIfPatientIdentifierTypesAreLocked();
		dao.deletePatientIdentifierType(patientIdentifierType);
	}
	
	// end patient identifier _type_ section
	
	/**
	 * @see org.openmrs.api.PatientService#getPatients(java.lang.String)
	 */
	@Override
	@Transactional(readOnly = true)
	public List<Patient> getPatients(String query) throws APIException {
		return Context.getPatientService().getPatients(query, 0, null);
	}
	
	/**
	 * This default implementation simply looks at the OpenMRS internal id (patient_id). If the id
	 * is null, assume this patient isn't found. If the patient_id is not null, try and find that id
	 * in the database
	 * 
	 * @see org.openmrs.api.PatientService#getPatientByExample(org.openmrs.Patient)
	 */
	@Override
	@Transactional(readOnly = true)
	public Patient getPatientByExample(Patient patientToMatch) throws APIException {
		if (patientToMatch == null || patientToMatch.getPatientId() == null) {
			return null;
		}
		
		return Context.getPatientService().getPatient(patientToMatch.getPatientId());
	}
	
	/**
	 * @see org.openmrs.api.PatientService#getDuplicatePatientsByAttributes(java.util.List)
	 */
	@Override
	@Transactional(readOnly = true)
	public List<Patient> getDuplicatePatientsByAttributes(List<String> attributes) throws APIException {
		
		if (attributes == null || attributes.isEmpty()) {
			throw new APIException("Patient.no.attribute", (Object[]) null);
		}
		
		return dao.getDuplicatePatientsByAttributes(attributes);
	}
	
	/**
	 * generate a relationship hash for use in mergePatients; follows the convention:
	 * [relationshipType][A|B][relativeId]
	 * 
	 * @param rel relationship under consideration
	 * @param primary the focus of the hash
	 * @return hash depicting relevant information to avoid duplicates
	 */
	private String relationshipHash(Relationship rel, Person primary) {
		boolean isA = rel.getPersonA().equals(primary);
		return rel.getRelationshipType().getRelationshipTypeId().toString() + (isA ? "A" : "B")
		        + (isA ? rel.getPersonB().getPersonId().toString() : rel.getPersonA().getPersonId().toString());
	}

	/**
	 * 1) Moves object (encounters/obs) pointing to <code>nonPreferred</code> to
	 * <code>preferred</code> 2) Copies data (gender/birthdate/names/ids/etc) from
	 * <code>nonPreferred</code> to <code>preferred</code> iff the data is missing or null in
	 * <code>preferred</code> 3) <code>notPreferred</code> is marked as voided
	 * 
	 * @param preferred
	 * @param notPreferred
	 * @throws APIException
	 * @see org.openmrs.api.PatientService#mergePatients(org.openmrs.Patient, org.openmrs.Patient)
	 */
	@Override
	public void mergePatients(Patient preferred, Patient notPreferred) throws APIException, SerializationException {
		log.debug("Merging patients: (preferred)" + preferred.getPatientId() + ", (notPreferred) "
		        + notPreferred.getPatientId());
		if (preferred.getPatientId().equals(notPreferred.getPatientId())) {
			log.debug("Merge operation cancelled: Cannot merge user" + preferred.getPatientId() + " to self");
			throw new APIException("Patient.merge.cancelled", new Object[] { preferred.getPatientId() });
		}
		requireNoActiveOrderOfSameType(preferred,notPreferred);
		PersonMergeLogData mergedData = new PersonMergeLogData();
		mergeVisits(preferred, notPreferred, mergedData);
		mergeEncounters(preferred, notPreferred, mergedData);
		mergeProgramEnrolments(preferred, notPreferred, mergedData);
		mergeRelationships(preferred, notPreferred, mergedData);
		mergeObservationsNotContainedInEncounters(preferred, notPreferred, mergedData);
		mergeIdentifiers(preferred, notPreferred, mergedData);
		
		mergeNames(preferred, notPreferred, mergedData);
		mergeAddresses(preferred, notPreferred, mergedData);
		mergePersonAttributes(preferred, notPreferred, mergedData);
		mergeGenderInformation(preferred, notPreferred, mergedData);
		mergeDateOfBirth(preferred, notPreferred, mergedData);
		mergeDateOfDeath(preferred, notPreferred, mergedData);
		
		// void the non preferred patient
		Context.getPatientService().voidPatient(notPreferred, "Merged with patient #" + preferred.getPatientId());
		
		// void the person associated with not preferred patient
		Context.getPersonService().voidPerson(notPreferred,
		    "The patient corresponding to this person has been voided and Merged with patient #" + preferred.getPatientId());
		
		// associate the Users associated with the not preferred person, to the preferred person.
		changeUserAssociations(preferred, notPreferred, mergedData);
		
		// Save the newly update preferred patient
		// This must be called _after_ voiding the nonPreferred patient so that
		//  a "Duplicate Identifier" error doesn't pop up.
		savePatient(preferred);
		
		//save the person merge log
		PersonMergeLog personMergeLog = new PersonMergeLog();
		personMergeLog.setWinner(preferred);
		personMergeLog.setLoser(notPreferred);
		personMergeLog.setPersonMergeLogData(mergedData);
		Context.getPersonService().savePersonMergeLog(personMergeLog);
	}
	
	private void requireNoActiveOrderOfSameType(Patient patient1, Patient patient2) {
		String messageKey = "Patient.merge.cannotHaveSameTypeActiveOrders";
		List<Order> ordersByPatient1 = Context.getOrderService().getAllOrdersByPatient(patient1);
		List<Order> ordersByPatient2 = Context.getOrderService().getAllOrdersByPatient(patient2);
		ordersByPatient1.forEach((Order order1) -> ordersByPatient2.forEach((Order order2) -> {
			if (order1.isActive() && order2.isActive() && order1.getOrderType().equals(order2.getOrderType())) {
				Object[] parameters = { patient1.getPatientId(), patient2.getPatientId(), order1.getOrderType() };
				String message = Context.getMessageSourceService().getMessage(messageKey, parameters,
						Context.getLocale());
				log.debug(message);
				throw new APIException(message);
			}
		}));
	}

	private void mergeProgramEnrolments(Patient preferred, Patient notPreferred, PersonMergeLogData mergedData) {
		// copy all program enrollments
		ProgramWorkflowService programService = Context.getProgramWorkflowService();
		for (PatientProgram pp : programService.getPatientPrograms(notPreferred, null, null, null, null, null, false)) {
			if (!pp.getVoided()) {
				PatientProgram enroll = pp.copy();
				enroll.setPatient(preferred);
				log.debug("Copying patientProgram " + pp.getPatientProgramId() + " to " + preferred.getPatientId());
				PatientProgram persisted = programService.savePatientProgram(enroll);
				mergedData.addCreatedProgram(persisted.getUuid());
			}
		}
	}
	
	private void mergeVisits(Patient preferred, Patient notPreferred, PersonMergeLogData mergedData) {
		// move all visits, including voided ones (encounters will be handled below)
		//TODO: this should be a copy, not a move
		
		VisitService visitService = Context.getVisitService();
		
		for (Visit visit : visitService.getVisitsByPatient(notPreferred, true, true)) {
			if (log.isDebugEnabled()) {
				log.debug("Merging visit " + visit.getVisitId() + " to " + preferred.getPatientId());
			}
			visit.setPatient(preferred);
			Visit persisted = visitService.saveVisit(visit);
			mergedData.addMovedVisit(persisted.getUuid());
		}
	}
	
	private void mergeEncounters(Patient preferred, Patient notPreferred, PersonMergeLogData mergedData) {
		// change all encounters. This will cascade to obs and orders contained in those encounters
		// TODO: this should be a copy, not a move
		EncounterService es = Context.getEncounterService();

		EncounterSearchCriteria notPreferredPatientEncounterSearchCriteria = new EncounterSearchCriteriaBuilder()
				.setIncludeVoided(true)
				.setPatient(notPreferred)
				.createEncounterSearchCriteria();
		for (Encounter e : es.getEncounters(notPreferredPatientEncounterSearchCriteria)) {
			e.setPatient(preferred);
			log.debug("Merging encounter " + e.getEncounterId() + " to " + preferred.getPatientId());
			Encounter persisted = es.saveEncounter(e);
			mergedData.addMovedEncounter(persisted.getUuid());
		}
	}
	
	private void mergeRelationships(Patient preferred, Patient notPreferred, PersonMergeLogData mergedData) {
		// copy all relationships
		PersonService personService = Context.getPersonService();
		Set<String> existingRelationships = new HashSet<>();
		// fill in the existing relationships with hashes
		for (Relationship rel : personService.getRelationshipsByPerson(preferred)) {
			existingRelationships.add(relationshipHash(rel, preferred));
		}
		// iterate over notPreferred's relationships and only copy them if they are needed
		for (Relationship rel : personService.getRelationshipsByPerson(notPreferred)) {
			if (!rel.getVoided()) {
				boolean personAisPreferred = rel.getPersonA().equals(preferred);
				boolean personAisNotPreferred = rel.getPersonA().equals(notPreferred);
				boolean personBisPreferred = rel.getPersonB().equals(preferred);
				boolean personBisNotPreferred = rel.getPersonB().equals(notPreferred);
				String relHash = relationshipHash(rel, notPreferred);
				
				if ((personAisPreferred && personBisNotPreferred) || (personBisPreferred && personAisNotPreferred)) {
					// void this relationship if it's between the preferred and notPreferred patients
					personService.voidRelationship(rel, "person " + (personAisNotPreferred ? "A" : "B")
					        + " was merged to person " + (personAisPreferred ? "A" : "B"));
				} else if (existingRelationships.contains(relHash)) {
					// void this relationship if it already exists between preferred and the other side
					personService.voidRelationship(rel, "person " + (personAisNotPreferred ? "A" : "B")
					        + " was merged and a relationship already exists");
				} else {
					// copy this relationship and replace notPreferred with preferred
					Relationship tmpRel = rel.copy();
					if (personAisNotPreferred) {
						tmpRel.setPersonA(preferred);
					}
					if (personBisNotPreferred) {
						tmpRel.setPersonB(preferred);
					}
					log.debug("Copying relationship " + rel.getRelationshipId() + " to " + preferred.getPatientId());
					Relationship persisted = personService.saveRelationship(tmpRel);
					mergedData.addCreatedRelationship(persisted.getUuid());
					// void the existing relationship to the notPreferred
					personService.voidRelationship(rel, "person " + (personAisNotPreferred ? "A" : "B")
					        + " was merged, relationship copied to #" + tmpRel.getRelationshipId());
					// add the relationship hash to existing relationships
					existingRelationships.add(relHash);
				}
				mergedData.addVoidedRelationship(rel.getUuid());
			}
		}
	}
	
	private void mergeObservationsNotContainedInEncounters(Patient preferred, Patient notPreferred,
	        PersonMergeLogData mergedData) {
		// move all obs that weren't contained in encounters
		// TODO: this should be a copy, not a move
		ObsService obsService = Context.getObsService();
		for (Obs obs : obsService.getObservationsByPerson(notPreferred)) {
			if (obs.getEncounter() == null && !obs.getVoided()) {
				obs.setPerson(preferred);
				Obs persisted = obsService.saveObs(obs, "Merged from patient #" + notPreferred.getPatientId());
				mergedData.addMovedIndependentObservation(persisted.getUuid());
			}
		}
	}
	
	private void mergeIdentifiers(Patient preferred, Patient notPreferred, PersonMergeLogData mergedData) {
		// move all identifiers
		// (must be done after all calls to services above so hbm doesn't try to save things prematurely (hacky)
		for (PatientIdentifier pi : notPreferred.getActiveIdentifiers()) {
			PatientIdentifier tmpIdentifier = new PatientIdentifier();
			tmpIdentifier.setIdentifier(pi.getIdentifier());
			tmpIdentifier.setIdentifierType(pi.getIdentifierType());
			tmpIdentifier.setLocation(pi.getLocation());
			tmpIdentifier.setPatient(preferred);
			boolean found = false;
			for (PatientIdentifier preferredIdentifier : preferred.getIdentifiers()) {
				if (preferredIdentifier.getIdentifier() != null
				        && preferredIdentifier.getIdentifier().equals(tmpIdentifier.getIdentifier())
				        && preferredIdentifier.getIdentifierType() != null
				        && preferredIdentifier.getIdentifierType().equals(tmpIdentifier.getIdentifierType())) {
					found = true;
				}
			}
			if (!found) {
				tmpIdentifier.setIdentifierType(pi.getIdentifierType());
				tmpIdentifier.setCreator(Context.getAuthenticatedUser());
				tmpIdentifier.setDateCreated(new Date());
				tmpIdentifier.setVoided(false);
				tmpIdentifier.setVoidedBy(null);
				tmpIdentifier.setVoidReason(null);
				tmpIdentifier.setUuid(UUID.randomUUID().toString());
				// we don't want to change the preferred identifier of the preferred patient
				tmpIdentifier.setPreferred(false);
				preferred.addIdentifier(tmpIdentifier);
				mergedData.addCreatedIdentifier(tmpIdentifier.getUuid());
				log.debug("Merging identifier " + tmpIdentifier.getIdentifier() + " to " + preferred.getPatientId());
			}
		}
	}
	
	private void mergeDateOfDeath(Patient preferred, Patient notPreferred, PersonMergeLogData mergedData) {
		mergedData.setPriorDateOfDeath(preferred.getDeathDate());
		if (preferred.getDeathDate() == null) {
			preferred.setDeathDate(notPreferred.getDeathDate());
		}
		
		if (preferred.getCauseOfDeath() != null) {
			mergedData.setPriorCauseOfDeath(preferred.getCauseOfDeath().getUuid());
		}
		if (preferred.getCauseOfDeath() == null) {
			preferred.setCauseOfDeath(notPreferred.getCauseOfDeath());
		}
	}
	
	private void mergeDateOfBirth(Patient preferred, Patient notPreferred, PersonMergeLogData mergedData) {
		mergedData.setPriorDateOfBirth(preferred.getBirthdate());
		mergedData.setPriorDateOfBirthEstimated(preferred.getBirthdateEstimated());
		if (preferred.getBirthdate() == null || (preferred.getBirthdateEstimated() && !notPreferred.getBirthdateEstimated())) {
			preferred.setBirthdate(notPreferred.getBirthdate());
			preferred.setBirthdateEstimated(notPreferred.getBirthdateEstimated());
		}
	}
	
	private void mergePersonAttributes(Patient preferred, Patient notPreferred, PersonMergeLogData mergedData) {
		// copy person attributes
		for (PersonAttribute attr : notPreferred.getAttributes()) {
			if (!attr.getVoided()) {
				PersonAttribute tmpAttr = attr.copy();
				tmpAttr.setPerson(null);
				tmpAttr.setUuid(UUID.randomUUID().toString());
				preferred.addAttribute(tmpAttr);
				mergedData.addCreatedAttribute(tmpAttr.getUuid());
			}
		}
	}
	
	private void mergeGenderInformation(Patient preferred, Patient notPreferred, PersonMergeLogData mergedData) {
		// move all other patient info
		mergedData.setPriorGender(preferred.getGender());
		if (!"M".equals(preferred.getGender()) && !"F".equals(preferred.getGender())) {
			preferred.setGender(notPreferred.getGender());
		}
	}
	
	private void mergeNames(Patient preferred, Patient notPreferred, PersonMergeLogData mergedData) {
		// move all names
		// (must be done after all calls to services above so hbm doesn't try to save things prematurely (hacky)
		for (PersonName newName : notPreferred.getNames()) {
			boolean containsName = false;
			for (PersonName currentName : preferred.getNames()) {
				containsName = currentName.equalsContent(newName);
				if (containsName) {
					break;
				}
			}
			if (!containsName) {
				PersonName tmpName = constructTemporaryName(newName);
				preferred.addName(tmpName);
				mergedData.addCreatedName(tmpName.getUuid());
				log.debug("Merging name " + newName.getGivenName() + " to " + preferred.getPatientId());
			}
		}
	}
	
	private PersonName constructTemporaryName(PersonName newName) {
		PersonName tmpName = PersonName.newInstance(newName);
		tmpName.setPersonNameId(null);
		tmpName.setVoided(false);
		tmpName.setVoidedBy(null);
		tmpName.setVoidReason(null);
		// we don't want to change the preferred name of the preferred patient
		tmpName.setPreferred(false);
		tmpName.setUuid(UUID.randomUUID().toString());
		return tmpName;
	}
	
	private void mergeAddresses(Patient preferred, Patient notPreferred, PersonMergeLogData mergedData)
	        throws SerializationException {
		// move all addresses
		// (must be done after all calls to services above so hbm doesn't try to save things prematurely (hacky)
		for (PersonAddress newAddress : notPreferred.getAddresses()) {
			boolean containsAddress = false;
			for (PersonAddress currentAddress : preferred.getAddresses()) {
				containsAddress = currentAddress.equalsContent(newAddress);
				if (containsAddress) {
					break;
				}
			}
			if (!containsAddress) {
				PersonAddress tmpAddress = (PersonAddress) newAddress.clone();
				tmpAddress.setPersonAddressId(null);
				tmpAddress.setVoided(false);
				tmpAddress.setVoidedBy(null);
				tmpAddress.setVoidReason(null);
				tmpAddress.setPreferred(false); // addresses from non-preferred patient shouldn't be marked as preferred
				tmpAddress.setUuid(UUID.randomUUID().toString());
				preferred.addAddress(tmpAddress);
				mergedData.addCreatedAddress(tmpAddress.getUuid());
				log.debug("Merging address " + newAddress.getPersonAddressId() + " to " + preferred.getPatientId());
			}
		}
		
		// copy person attributes
		for (PersonAttribute attr : notPreferred.getAttributes()) {
			if (!attr.getVoided()) {
				PersonAttribute tmpAttr = attr.copy();
				tmpAttr.setPerson(null);
				tmpAttr.setUuid(UUID.randomUUID().toString());
				preferred.addAttribute(tmpAttr);
				mergedData.addCreatedAttribute(tmpAttr.getUuid());
			}
		}
		
		// move all other patient info
		mergedData.setPriorGender(preferred.getGender());
		if (!"M".equals(preferred.getGender()) && !"F".equals(preferred.getGender())) {
			preferred.setGender(notPreferred.getGender());
		}

		mergedData.setPriorDateOfBirth(preferred.getBirthdate());
		mergedData.setPriorDateOfBirthEstimated(preferred.getBirthdateEstimated());
		if (preferred.getBirthdate() == null || (preferred.getBirthdateEstimated() && !notPreferred.getBirthdateEstimated())) {
			preferred.setBirthdate(notPreferred.getBirthdate());
			preferred.setBirthdateEstimated(notPreferred.getBirthdateEstimated());
		}
		mergedData.setPriorDateOfDeathEstimated(preferred.getDeathdateEstimated());
		if (preferred.getDeathdateEstimated() == null) {
			preferred.setDeathdateEstimated(notPreferred.getDeathdateEstimated());
		}
		
		mergedData.setPriorDateOfDeath(preferred.getDeathDate());
		if (preferred.getDeathDate() == null) {
			preferred.setDeathDate(notPreferred.getDeathDate());
		}
		
		if (preferred.getCauseOfDeath() != null) {
			mergedData.setPriorCauseOfDeath(preferred.getCauseOfDeath().getUuid());
		}
		if (preferred.getCauseOfDeath() == null) {
			preferred.setCauseOfDeath(notPreferred.getCauseOfDeath());
		}
		
		// void the non preferred patient
		Context.getPatientService().voidPatient(notPreferred, "Merged with patient #" + preferred.getPatientId());
		
		// void the person associated with not preferred patient
		Context.getPersonService().voidPerson(notPreferred,
		    "The patient corresponding to this person has been voided and Merged with patient #" + preferred.getPatientId());
		
		// associate the Users associated with the not preferred person, to the preferred person.
		changeUserAssociations(preferred, notPreferred, mergedData);
		
		// Save the newly update preferred patient
		// This must be called _after_ voiding the nonPreferred patient so that
		//  a "Duplicate Identifier" error doesn't pop up.
		savePatient(preferred);
		
		//save the person merge log
		PersonMergeLog personMergeLog = new PersonMergeLog();
		personMergeLog.setWinner(preferred);
		personMergeLog.setLoser(notPreferred);
		personMergeLog.setPersonMergeLogData(mergedData);
		Context.getPersonService().savePersonMergeLog(personMergeLog);
	}
	
	/**
	 * Change user associations for notPreferred to preferred person.
	 * 
	 * @param preferred
	 * @param notPreferred
	 * @param mergedData a patient merge audit data object to update
	 * @see PatientServiceImpl#mergePatients(Patient, Patient)
	 */
	private void changeUserAssociations(Patient preferred, Person notPreferred, PersonMergeLogData mergedData) {
		UserService userService = Context.getUserService();
		List<User> users = userService.getUsersByPerson(notPreferred, true);
		for (User user : users) {
			user.setPerson(preferred);
			User persisted = userService.saveUser(user);
			if (mergedData != null) {
				mergedData.addMovedUser(persisted.getUuid());
			}
		}
	}
	
	/**
	 * This is the way to establish that a patient has left the care center. This API call is
	 * responsible for:
	 * <ol>
	 * <li>Closing workflow statuses</li>
	 * <li>Terminating programs</li>
	 * <li>Discontinuing orders</li>
	 * <li>Flagging patient table</li>
	 * <li>Creating any relevant observations about the patient (if applicable)</li>
	 * </ol>
	 * 
	 * @param patient - the patient who has exited care
	 * @param dateExited - the declared date/time of the patient's exit
	 * @param reasonForExit - the concept that corresponds with why the patient has been declared as
	 *            exited
	 * @throws APIException
	 */
	public void exitFromCare(Patient patient, Date dateExited, Concept reasonForExit) throws APIException {
		
		if (patient == null) {
			throw new APIException("Patient.invalid.care", (Object[]) null);
		}
		if (dateExited == null) {
			throw new APIException("Patient.no.valid.dateExited", (Object[]) null);
		}
		if (reasonForExit == null) {
			throw new APIException("Patient.no.valid.reasonForExit", (Object[]) null);
		}
		
		// need to create an observation to represent this (otherwise how
		// will we know?)
		saveReasonForExitObs(patient, dateExited, reasonForExit);
	}
	
	/**
	 * TODO: Patients should actually be allowed to exit multiple times
	 * 
	 * @param patient
	 * @param exitDate
	 * @param cause
	 */
	private void saveReasonForExitObs(Patient patient, Date exitDate, Concept cause) throws APIException {
		
		if (patient == null) {
			throw new APIException("Patient.null", (Object[]) null);
		}
		if (exitDate == null) {
			throw new APIException("Patient.exit.date.null", (Object[]) null);
		}
		if (cause == null) {
			throw new APIException("Patient.cause.null", (Object[]) null);
		}
		
		// need to make sure there is an Obs that represents the patient's
		// exit
		log.debug("Patient is exiting, so let's make sure there's an Obs for it");
		
		String codProp = Context.getAdministrationService().getGlobalProperty("concept.reasonExitedCare");
		Concept reasonForExit = Context.getConceptService().getConcept(codProp);
		
		if (reasonForExit != null) {
			List<Obs> obssExit = Context.getObsService().getObservationsByPersonAndConcept(patient, reasonForExit);
			if (obssExit != null) {
				if (obssExit.size() > 1) {
					log.error("Multiple reasons for exit (" + obssExit.size() + ")?  Shouldn't be...");
				} else {
					Obs obsExit;
					if (obssExit.size() == 1) {
						// already has a reason for exit - let's edit it.
						log.debug("Already has a reason for exit, so changing it");
						
						obsExit = obssExit.iterator().next();
						
					} else {
						// no reason for exit obs yet, so let's make one
						log.debug("No reason for exit yet, let's create one.");
						
						obsExit = new Obs();
						obsExit.setPerson(patient);
						obsExit.setConcept(reasonForExit);
						
						Location loc = Context.getLocationService().getDefaultLocation();
						
						if (loc != null) {
							obsExit.setLocation(loc);
						} else {
							log.error("Could not find a suitable location for which to create this new Obs");
						}
					}
					
					if (obsExit != null) {
						// put the right concept and (maybe) text in this
						// obs
						obsExit.setValueCoded(cause);
						obsExit.setValueCodedName(cause.getName()); // ABKTODO: presume current locale?
						obsExit.setObsDatetime(exitDate);
						Context.getObsService().saveObs(obsExit, "updated by PatientService.saveReasonForExit");
					}
				}
			}
		} else {
			log.debug("Reason for exit is null - should not have gotten here without throwing an error on the form.");
		}
		
	}
	
	/**
	 * This is the way to establish that a patient has died. In addition to exiting the patient from
	 * care (see above), this method will also set the appropriate patient characteristics to
	 * indicate that they have died, when they died, etc.
	 * 
	 * @param patient - the patient who has died
	 * @param dateDied - the declared date/time of the patient's death
	 * @param causeOfDeath - the concept that corresponds with the reason the patient died
	 * @param otherReason - in case the causeOfDeath is 'other', a place to store more info
	 * @throws APIException
	 */
	@Override
	public void processDeath(Patient patient, Date dateDied, Concept causeOfDeath, String otherReason) throws APIException {
		
		if (patient != null && dateDied != null && causeOfDeath != null) {
			// set appropriate patient characteristics
			patient.setDead(true);
			patient.setDeathDate(dateDied);
			patient.setCauseOfDeath(causeOfDeath);
			this.savePatient(patient);
			saveCauseOfDeathObs(patient, dateDied, causeOfDeath, otherReason);
			
			// exit from program
			// first, need to get Concept for "Patient Died"
			String strPatientDied = Context.getAdministrationService().getGlobalProperty("concept.patientDied");
			Concept conceptPatientDied = Context.getConceptService().getConcept(strPatientDied);
			
			if (conceptPatientDied == null) {
				log.debug("ConceptPatientDied is null");
			}
			exitFromCare(patient, dateDied, conceptPatientDied);
			
		} else {
			if (patient == null) {
				throw new APIException("Patient.invalid.dead", (Object[]) null);
			}
			if (dateDied == null) {
				throw new APIException("Patient.no.valid.dateDied", (Object[]) null);
			}
			if (causeOfDeath == null) {
				throw new APIException("Patient.no.valid.causeOfDeath", (Object[]) null);
			}
		}
	}
	
	/**
	 * @see org.openmrs.api.PatientService#saveCauseOfDeathObs(org.openmrs.Patient, java.util.Date,
	 *      org.openmrs.Concept, java.lang.String)
	 */
	@Override
	public void saveCauseOfDeathObs(Patient patient, Date deathDate, Concept cause, String otherReason) throws APIException {
		
		if (patient == null) {
			throw new APIException("Patient.null", (Object[]) null);
		}
		if (deathDate == null) {
			throw new APIException("Patient.death.date.null", (Object[]) null);
		}
		if (cause == null) {
			throw new APIException("Patient.cause.null", (Object[]) null);
		}
		
		if (!patient.getDead()) {
			patient.setDead(true);
			patient.setDeathDate(deathDate);
			patient.setCauseOfDeath(cause);
		}
		
		log.debug("Patient is dead, so let's make sure there's an Obs for it");
		// need to make sure there is an Obs that represents the patient's
		// cause of death, if applicable
		
		String codProp = Context.getAdministrationService().getGlobalProperty("concept.causeOfDeath");
		
		Concept causeOfDeath = Context.getConceptService().getConcept(codProp);
		
		if (causeOfDeath != null) {
			List<Obs> obssDeath = Context.getObsService().getObservationsByPersonAndConcept(patient, causeOfDeath);
			if (obssDeath != null) {
				if (obssDeath.size() > 1) {
					log.error("Multiple causes of death (" + obssDeath.size() + ")?  Shouldn't be...");
				} else {
					Obs obsDeath;
					if (obssDeath.size() == 1) {
						// already has a cause of death - let's edit it.
						log.debug("Already has a cause of death, so changing it");
						
						obsDeath = obssDeath.iterator().next();
						
					} else {
						// no cause of death obs yet, so let's make one
						log.debug("No cause of death yet, let's create one.");
						
						obsDeath = new Obs();
						obsDeath.setPerson(patient);
						obsDeath.setConcept(causeOfDeath);
						Location location = Context.getLocationService().getDefaultLocation();
						if (location != null) {
							obsDeath.setLocation(location);
						} else {
							log.error("Could not find a suitable location for which to create this new Obs");
						}
					}
					
					// put the right concept and (maybe) text in this obs
					Concept currCause = patient.getCauseOfDeath();
					if (currCause == null) {
						// set to NONE
						log.debug("Current cause is null, attempting to set to NONE");
						String noneConcept = Context.getAdministrationService().getGlobalProperty("concept.none");
						currCause = Context.getConceptService().getConcept(noneConcept);
					}
					
					if (currCause != null) {
						log.debug("Current cause is not null, setting to value_coded");
						obsDeath.setValueCoded(currCause);
						obsDeath.setValueCodedName(currCause.getName()); // ABKTODO: presume current locale?
						
						Date dateDeath = patient.getDeathDate();
						if (dateDeath == null) {
							dateDeath = new Date();
						}
						obsDeath.setObsDatetime(dateDeath);
						
						// check if this is an "other" concept - if so, then
						// we need to add value_text
						String otherConcept = Context.getAdministrationService().getGlobalProperty("concept.otherNonCoded");
						Concept conceptOther = Context.getConceptService().getConcept(otherConcept);
						if (conceptOther != null) {
							if (conceptOther.equals(currCause)) {
								// seems like this is an other concept -
								// let's try to get the "other" field info
								log.debug("Setting value_text as " + otherReason);
								obsDeath.setValueText(otherReason);
							} else {
								log.debug("New concept is NOT the OTHER concept, so setting to blank");
								obsDeath.setValueText("");
							}
						} else {
							log.debug("Don't seem to know about an OTHER concept, so deleting value_text");
							obsDeath.setValueText("");
						}
						
						Context.getObsService().saveObs(obsDeath, "updated by PatientService.saveCauseOfDeathObs");
					} else {
						log.debug("Current cause is still null - aborting mission");
					}
				}
			}
		} else {
			log.debug("Cause of death is null - should not have gotten here without throwing an error on the form.");
		}
	}
	
	/**
	 * @see org.openmrs.api.PatientService#getPatientByUuid(java.lang.String)
	 */
	@Override
	@Transactional(readOnly = true)
	public Patient getPatientByUuid(String uuid) throws APIException {
		return dao.getPatientByUuid(uuid);
	}
	
	@Override
	@Transactional(readOnly = true)
	public PatientIdentifier getPatientIdentifierByUuid(String uuid) throws APIException {
		return dao.getPatientIdentifierByUuid(uuid);
	}
	
	/**
	 * @see org.openmrs.api.PatientService#getPatientIdentifierTypeByUuid(java.lang.String)
	 */
	@Override
	@Transactional(readOnly = true)
	public PatientIdentifierType getPatientIdentifierTypeByUuid(String uuid) throws APIException {
		return dao.getPatientIdentifierTypeByUuid(uuid);
	}
	
	/**
	 * @see org.openmrs.api.PatientService#getDefaultIdentifierValidator()
	 */
	@Override
	@Transactional(readOnly = true)
	public IdentifierValidator getDefaultIdentifierValidator() {
		String defaultPIV = Context.getAdministrationService().getGlobalProperty(
		    OpenmrsConstants.GLOBAL_PROPERTY_DEFAULT_PATIENT_IDENTIFIER_VALIDATOR, "");
		
		try {
			return identifierValidators.get(Class.forName(defaultPIV));
		}
		catch (ClassNotFoundException e) {
			log.error("Global Property " + OpenmrsConstants.GLOBAL_PROPERTY_DEFAULT_PATIENT_IDENTIFIER_VALIDATOR
			        + " not set to an actual class.", e);
			return identifierValidators.get(LuhnIdentifierValidator.class);
		}
	}
	
	/**
	 * @see org.openmrs.api.PatientService#getIdentifierValidator(java.lang.String)
	 */
	@Override
	public IdentifierValidator getIdentifierValidator(Class<IdentifierValidator> identifierValidator) {
		return identifierValidators.get(identifierValidator);
	}
	
	public Map<Class<? extends IdentifierValidator>, IdentifierValidator> getIdentifierValidators() {
		if (identifierValidators == null) {
			identifierValidators = new LinkedHashMap<>();
		}
		return identifierValidators;
	}
	
	/**
	 * ADDs identifierValidators, doesn't replace them
	 * 
	 * @param identifierValidators
	 */
	public void setIdentifierValidators(Map<Class<? extends IdentifierValidator>, IdentifierValidator> identifierValidators) {
		if (identifierValidators == null) {
			PatientServiceImpl.setStaticIdentifierValidators(null);
			return;
		}
		for (Map.Entry<Class<? extends IdentifierValidator>, IdentifierValidator> entry : identifierValidators.entrySet()) {
			getIdentifierValidators().put(entry.getKey(), entry.getValue());
		}
	}
	
	/**
	 * Sets identifierValidators using static method
	 *
	 * @param currentIdentifierValidators
	 */
	private static void setStaticIdentifierValidators(
	        Map<Class<? extends IdentifierValidator>, IdentifierValidator> currentIdentifierValidators) {
		PatientServiceImpl.identifierValidators = currentIdentifierValidators;
	}
	
	/**
	 * @see org.openmrs.api.PatientService#getAllIdentifierValidators()
	 */
	@Override
	public Collection<IdentifierValidator> getAllIdentifierValidators() {
		return identifierValidators.values();
	}
	
	/**
	 * @see org.openmrs.api.PatientService#getIdentifierValidator(java.lang.String)
	 */
	@Override
	@SuppressWarnings("unchecked")
	@Transactional(readOnly = true)
	public IdentifierValidator getIdentifierValidator(String pivClassName) {
		if (StringUtils.isBlank(pivClassName)) {
			return null;
		}
		
		try {
			return getIdentifierValidator((Class<IdentifierValidator>) Context.loadClass(pivClassName));
		}
		catch (ClassNotFoundException e) {
			log.error("Could not find patient identifier validator " + pivClassName, e);
			return getDefaultIdentifierValidator();
		}
	}
	
	/**
	 * @see org.openmrs.api.PatientService#isIdentifierInUseByAnotherPatient(org.openmrs.PatientIdentifier)
	 */
	@Override
	@Transactional(readOnly = true)
	public boolean isIdentifierInUseByAnotherPatient(PatientIdentifier patientIdentifier) {
		return dao.isIdentifierInUseByAnotherPatient(patientIdentifier);
	}
	
	/**
	 * @see org.openmrs.api.PatientService#getPatientIdentifier(java.lang.Integer)
	 */
	@Override
	@Transactional(readOnly = true)
	public PatientIdentifier getPatientIdentifier(Integer patientIdentifierId) throws APIException {
		return dao.getPatientIdentifier(patientIdentifierId);
	}
	
	/**
	 * @see org.openmrs.api.PatientService#voidPatientIdentifier(org.openmrs.PatientIdentifier,
	 *      java.lang.String)
	 */
	@Override
	public PatientIdentifier voidPatientIdentifier(PatientIdentifier patientIdentifier, String reason) throws APIException {
		
		if (patientIdentifier == null || StringUtils.isBlank(reason)) {
			throw new APIException("Patient.identifier.cannot.be.null", (Object[]) null);
		}
		return Context.getPatientService().savePatientIdentifier(patientIdentifier);
		
	}
	
	/**
	 * @see org.openmrs.api.PatientService#mergePatients(org.openmrs.Patient, java.util.List)
	 */
	@Override
	public void mergePatients(Patient preferred, List<Patient> notPreferred) throws APIException, SerializationException {
		
		for (Patient nonPreferred : notPreferred) {
			mergePatients(preferred, nonPreferred);
		}
	}
	
	/**
	 * @see org.openmrs.api.PatientService#savePatientIdentifier(org.openmrs.PatientIdentifier)
	 */
	@Override
	public PatientIdentifier savePatientIdentifier(PatientIdentifier patientIdentifier) throws APIException {
		//if the argument or the following required fields are not specified
		PatientIdentifierType.LocationBehavior locationBehavior = null;
		if (patientIdentifier != null) {
			locationBehavior = patientIdentifier.getIdentifierType().getLocationBehavior();
		}
		
		if (patientIdentifier == null
		        || patientIdentifier.getPatient() == null
		        || patientIdentifier.getIdentifierType() == null
		        || StringUtils.isBlank(patientIdentifier.getIdentifier())
		        || (locationBehavior == PatientIdentifierType.LocationBehavior.REQUIRED && patientIdentifier.getLocation() == null)) {
			throw new APIException("Patient.identifier.null", (Object[]) null);
		}
		if (patientIdentifier.getPatientIdentifierId() == null) {
			Context.requirePrivilege(PrivilegeConstants.ADD_PATIENT_IDENTIFIERS);
		} else {
			Context.requirePrivilege(PrivilegeConstants.EDIT_PATIENT_IDENTIFIERS);
		}
		
		return dao.savePatientIdentifier(patientIdentifier);
	}
	
	/**
	 * @see org.openmrs.api.PatientService#purgePatientIdentifier(org.openmrs.PatientIdentifier)
	 */
	@Override
	public void purgePatientIdentifier(PatientIdentifier patientIdentifier) throws APIException {
		
		dao.deletePatientIdentifier(patientIdentifier);
		
	}
	
	/**
	 * @see org.openmrs.api.PatientService#getAllergies(org.openmrs.Patient)
	 */
	@Override
	@Transactional(readOnly = true)
	public Allergies getAllergies(Patient patient) {
		if (patient == null) {
			throw new IllegalArgumentException("An existing (NOT NULL) patient is required to get allergies");
		}
		
		Allergies allergies = new Allergies();
		List<Allergy> allergyList = dao.getAllergies(patient);
		if (!allergyList.isEmpty()) {
			allergies.addAll(allergyList);
		} else {
			String status = dao.getAllergyStatus(patient);
			if (Allergies.NO_KNOWN_ALLERGIES.equals(status)) {
				allergies.confirmNoKnownAllergies();
			}
		}
		return allergies;
	}
	
	/**
	 * @see org.openmrs.api.PatientService#setAllergies(org.openmrs.Patient,
	 *      org.openmrs.Allergies)
	 */
	@Override
	public Allergies setAllergies(Patient patient, Allergies allergies) {
		//NOTE We neither delete nor edit allergies. We instead void them.
		//Because we shield the API users from this business logic,
		//we end up with the complicated code below. :)
		
		//get the current allergies as stored in the database
		List<Allergy> dbAllergyList = getAllergies(patient);
		for (Allergy originalAllergy : dbAllergyList) {
			//check if we still have each allergy, else it has just been deleted
			if (allergies.contains(originalAllergy)) {
				//we still have this allergy, check if it has been edited/changed
				Allergy potentiallyEditedAllergy = allergies.getAllergy(originalAllergy.getAllergyId());
				if (!potentiallyEditedAllergy.hasSameValues(originalAllergy)) {
					//allergy has been edited, so void it and create a new one with the current values
					Allergy newAllergy = new Allergy();
					try {
						//remove the edited allergy from our current list, and void id
						allergies.remove(potentiallyEditedAllergy);
						
						//copy values from edited allergy, and add it to the current list
						newAllergy.copy(potentiallyEditedAllergy);
						allergies.add(newAllergy);
						
						//we void its original values, as came from the database, 
						//instead the current ones which have just been copied 
						//into the new allergy we have just created above
						voidAllergy(originalAllergy);
					}
					catch (Exception ex) {
						throw new APIException("Failed to copy edited values", ex);
					}
				}
				continue;
			}
			
			//void the allergy that has been deleted
			voidAllergy(originalAllergy);
		}
		
		for (Allergy allergy : allergies) {
			if (allergy.getAllergyId() == null && allergy.getAllergen().getCodedAllergen() == null
			        && StringUtils.isNotBlank(allergy.getAllergen().getNonCodedAllergen())) {
				
				Concept otherNonCoded = Context.getConceptService().getConceptByUuid(Allergen.getOtherNonCodedConceptUuid());
				if (otherNonCoded == null) {
					throw new APIException("Can't find concept with uuid:" + Allergen.getOtherNonCodedConceptUuid());
				}
				allergy.getAllergen().setCodedAllergen(otherNonCoded);
			}
		}
		
		return dao.saveAllergies(patient, allergies);
	}
	
	/**
	 * Voids a given allergy
	 * 
	 * @param allergy the allergy to void
	 */
	private void voidAllergy(Allergy allergy) {
		allergy.setVoided(true);
		allergy.setVoidedBy(Context.getAuthenticatedUser());
		allergy.setDateVoided(new Date());
		allergy.setVoidReason("Voided by API");
		dao.saveAllergy(allergy);
	}
	
	/**
	 * @see org.openmrs.api.PatientService#getAllergy(java.lang.Integer)
	 */
	@Override
	@Transactional(readOnly = true)
	public Allergy getAllergy(Integer allergyId) throws APIException {
		return dao.getAllergy(allergyId);
	}
	
	/**
	 * @see org.openmrs.api.PatientService#getAllergyByUuid(java.lang.String)
	 */
	@Override
	@Transactional(readOnly = true)
	public Allergy getAllergyByUuid(String uuid) throws APIException {
		return dao.getAllergyByUuid(uuid);
	}
	
	/**
	 * @see org.openmrs.api.PatientService#saveAllergy(org.openmrs.Allergy)
	 */
	@Override
	public void saveAllergy(Allergy allergy) throws APIException {

		dao.saveAllergy(allergy);
	}
	
	/**
	 * @see org.openmrs.api.PatientService#removeAllergy(org.openmrs.Allergy,
	 *      java.lang.String)
	 */
	@Override
	public void removeAllergy(Allergy allergy, String reason) throws APIException {
		voidAllergy(allergy, reason);
	}
	
	/**
	 * @see org.openmrs.api.PatientService#voidAllergy(org.openmrs.Allergy,
	 *      java.lang.String)
	 */
	@Override
	public void voidAllergy(Allergy allergy, String reason) throws APIException {

		allergy.setVoided(true);
		allergy.setVoidedBy(Context.getAuthenticatedUser());
		allergy.setDateVoided(new Date());
		allergy.setVoidReason(reason);
		dao.saveAllergy(allergy);
	}
	
	/**
	 * @see PatientService#getCountOfPatients(String)
	 */
	@Override
	@Transactional(readOnly = true)
	public Integer getCountOfPatients(String query) {
		int count = 0;
		if (StringUtils.isBlank(query)) {
			return count;
		}
		
		return OpenmrsUtil.convertToInteger(dao.getCountOfPatients(query));
	}
	
	/**
	 * @see PatientService#getCountOfPatients(String, boolean)
	 */
	@Override
	@Transactional(readOnly = true)
	public Integer getCountOfPatients(String query, boolean includeVoided) {
		int count = 0;
		if (StringUtils.isBlank(query)) {
			return count;
		}
		
		return OpenmrsUtil.convertToInteger(dao.getCountOfPatients(query, includeVoided));
	}
	
	/**
	 * @see PatientService#getPatients(String, Integer, Integer)
	 */
	@Override
	@Transactional(readOnly = true)
	public List<Patient> getPatients(String query, Integer start, Integer length) throws APIException {
		List<Patient> patients = new ArrayList<>();
		if (StringUtils.isBlank(query)) {
			return patients;
		}
		
		return dao.getPatients(query, start, length);
	}
	
	/**
	 * @see PatientService#getPatients(String, boolean, Integer, Integer)
	 */
	@Override
	@Transactional(readOnly = true)
	public List<Patient> getPatients(String query, boolean includeVoided, Integer start, Integer length) throws APIException {
		if (StringUtils.isBlank(query)) {
			return Collections.emptyList();
		}
		
		return dao.getPatients(query, includeVoided, start, length);
	}
	
	/**
	 * @see PatientService#getPatients(String, String, List, boolean, Integer, Integer)
	 */
	// TODO - search for usage with non-empty list of patient identifier types - not used
	@Override
	@Transactional(readOnly = true)
	public List<Patient> getPatients(String name, String identifier, List<PatientIdentifierType> identifierTypes,
	        boolean matchIdentifierExactly, Integer start, Integer length) throws APIException {
		
		if(identifierTypes == null) {
			return dao.getPatients(name != null ? name : identifier, start, length);
		}
		else {
			return dao.getPatients(name != null ? name : identifier, identifierTypes, matchIdentifierExactly, start, length);
		}
	}
	
	/**
	 * @see PatientService#checkIfPatientIdentifierTypesAreLocked()
	 */
	@Override
	public void checkIfPatientIdentifierTypesAreLocked() {
		String locked = Context.getAdministrationService().getGlobalProperty(
		    OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_IDENTIFIER_TYPES_LOCKED, "false");
		if ("true".equalsIgnoreCase(locked)) {
			throw new PatientIdentifierTypeLockedException();
		}
	}
}

package org.openmrs.api.impl;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptDatatype;
import org.openmrs.ConceptProposal;
import org.openmrs.ConceptSynonym;
import org.openmrs.DataEntryStatistic;
import org.openmrs.EncounterType;
import org.openmrs.FieldType;
import org.openmrs.GlobalProperty;
import org.openmrs.Location;
import org.openmrs.MimeType;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifierType;
import org.openmrs.Person;
import org.openmrs.Privilege;
import org.openmrs.Relationship;
import org.openmrs.RelationshipType;
import org.openmrs.Role;
import org.openmrs.Tribe;
import org.openmrs.User;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.APIException;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.AdministrationDAO;
import org.openmrs.reporting.AbstractReportObject;
import org.openmrs.reporting.Report;
import org.openmrs.util.OpenmrsConstants;
import org.springframework.util.StringUtils;

/**
 * Admin-related services
 * 
 * @author Ben Wolfe
 * @author Burke Mamlin
 * @version 1.0
 */
public class AdministrationServiceImpl implements AdministrationService {
	
	protected Log log = LogFactory.getLog(getClass());
	
	private AdministrationDAO dao;
	
	public AdministrationServiceImpl() {	}
	
	private AdministrationDAO getAdministrationDAO() {
		if (!Context.isAuthenticated())
			throw new APIAuthenticationException("unauthorized access to administration service");
		
		return dao;
	}
	
	public void setAdministrationDAO(AdministrationDAO dao) {
		this.dao = dao;
	}

	/**
	 * Create a new Person
	 * @param Person to create
	 * @throws APIException
	 */
	public void createPerson(Person person) throws APIException {
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_RELATIONSHIPS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_MANAGE_RELATIONSHIPS);

		getAdministrationDAO().createPerson(person);
	}
	
	/**
	 * Update an encounter type
	 * @param Person to update
	 * @throws APIException
	 */
	public void updatePerson(Person person) throws APIException {
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_RELATIONSHIPS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_MANAGE_RELATIONSHIPS);

		getAdministrationDAO().updatePerson(person);
	}

	/**
	 * Delete an encounter type
	 * @param Person to delete
	 * @throws APIException
	 */
	public void deletePerson(Person person) throws APIException {
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_RELATIONSHIPS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_MANAGE_RELATIONSHIPS);

		getAdministrationDAO().deletePerson(person);
	}
	
	/**
	 * 
	 * @param personId to get
	 * @return Person
	 * @throws APIException
	 */
	public Person getPerson(Integer personId) throws APIException {
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_RELATIONSHIPS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_MANAGE_RELATIONSHIPS);

		return getAdministrationDAO().getPerson(personId);
	}
	
	public Person getPerson(Patient pat) throws APIException {
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_RELATIONSHIPS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_MANAGE_RELATIONSHIPS);

		return getAdministrationDAO().getPerson(pat);
	}
	
	public Person getPerson(User user) throws APIException {
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_RELATIONSHIPS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_MANAGE_RELATIONSHIPS);

		return getAdministrationDAO().getPerson(user);
	}
	
	/**
	 * Create a new EncounterType
	 * @param EncounterType to create
	 * @throws APIException
	 */
	public void createEncounterType(EncounterType encounterType) throws APIException {
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_ENCOUNTER_TYPES))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_MANAGE_ENCOUNTER_TYPES);
		encounterType.setCreator(Context.getAuthenticatedUser());
		encounterType.setDateCreated(new Date());
		getAdministrationDAO().createEncounterType(encounterType);
	}

	/**
	 * Update an encounter type
	 * @param EncounterType to update
	 * @throws APIException
	 */
	public void updateEncounterType(EncounterType encounterType) throws APIException {
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_ENCOUNTER_TYPES))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_MANAGE_ENCOUNTER_TYPES);
		
		if (encounterType.getEncounterTypeId() == null)
			createEncounterType(encounterType);
		else
			getAdministrationDAO().updateEncounterType(encounterType);
	}

	/**
	 * Delete an encounter type
	 * @param EncounterType to delete
	 * @throws APIException
	 */
	public void deleteEncounterType(EncounterType encounterType) throws APIException {
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_ENCOUNTER_TYPES))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_MANAGE_ENCOUNTER_TYPES);

		getAdministrationDAO().deleteEncounterType(encounterType);
	}

	/**
	 * Create a new PatientIdentifierType
	 * @param PatientIdentifierType to create
	 * @throws APIException
	 */
	public void createPatientIdentifierType(PatientIdentifierType patientIdentifierType) throws APIException {
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_IDENTIFIER_TYPES))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_MANAGE_IDENTIFIER_TYPES);

		getAdministrationDAO().createPatientIdentifierType(patientIdentifierType);
	}

	/**
	 * Update PatientIdentifierType
	 * @param PatientIdentifierType to update
	 * @throws APIException
	 */
	public void updatePatientIdentifierType(PatientIdentifierType patientIdentifierType) throws APIException {
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_IDENTIFIER_TYPES))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_MANAGE_IDENTIFIER_TYPES);

		getAdministrationDAO().updatePatientIdentifierType(patientIdentifierType);
	}
	
	/**
	 * Delete PatientIdentifierType
	 * @param PatientIdentifierType to delete
	 * @throws APIException
	 */
	public void deletePatientIdentifierType(PatientIdentifierType patientIdentifierType) throws APIException {
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_IDENTIFIER_TYPES))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_MANAGE_IDENTIFIER_TYPES);

		getAdministrationDAO().deletePatientIdentifierType(patientIdentifierType);
	}

	/**
	 * Create a new Tribe
	 * @param Tribe to create
	 * @throws APIException
	 */
	public void createTribe(Tribe tribe) throws APIException {
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_TRIBES))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_MANAGE_TRIBES);

		getAdministrationDAO().createTribe(tribe);
	}

	/**
	 * Update Tribe
	 * @param Tribe to update
	 * @throws APIException
	 */
	public void updateTribe(Tribe tribe) throws APIException {
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_TRIBES))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_MANAGE_TRIBES);

		getAdministrationDAO().updateTribe(tribe);
	}

	/**
	 * Delete Tribe
	 * @param Tribe to delete
	 * @throws APIException
	 */
	public void deleteTribe(Tribe tribe) throws APIException {
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_TRIBES))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_MANAGE_TRIBES);

		getAdministrationDAO().deleteTribe(tribe);
	}
	
	/**
	 * Retire Tribe
	 * @param Tribe to retire
	 * @throws APIException
	 */
	public void retireTribe(Tribe tribe) throws APIException {
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_TRIBES))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_MANAGE_TRIBES);

		getAdministrationDAO().retireTribe(tribe);
	}

	/**
	 * Unretire Tribe
	 * @param Tribe to unretire
	 * @throws APIException
	 */
	public void unretireTribe(Tribe tribe) throws APIException {
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_TRIBES))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_MANAGE_TRIBES);

		getAdministrationDAO().unretireTribe(tribe);
	}
	
	/**
	 * Create a new Relationship
	 * @param Relationship to create
	 * @throws APIException
	 */
	public void createRelationship(Relationship relationship) throws APIException {
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_RELATIONSHIPS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_MANAGE_RELATIONSHIPS);

		getAdministrationDAO().createRelationship(relationship);
	}

	/**
	 * Update Relationship
	 * @param Relationship to update
	 * @throws APIException
	 */
	public void updateRelationship(Relationship relationship) throws APIException {
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_RELATIONSHIPS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_MANAGE_RELATIONSHIPS);

		getAdministrationDAO().updateRelationship(relationship);
	}

	/**
	 * Delete Relationship
	 * @param Relationship to delete
	 * @throws APIException
	 */
	public void deleteRelationship(Relationship relationship) throws APIException {
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_RELATIONSHIPS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_MANAGE_RELATIONSHIPS);

		getAdministrationDAO().deleteRelationship(relationship);
	}
	
	/**
	 * Retire Relationship
	 * @param Relationship to void
	 * @throws APIException
	 */
	public void voidRelationship(Relationship relationship) throws APIException {
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_RELATIONSHIPS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_MANAGE_RELATIONSHIPS);

		if (relationship.getVoided()) {
			return;
		}
		if (relationship.getVoidedBy() == null) {
			relationship.setVoidedBy(Context.getAuthenticatedUser());
		}
		getAdministrationDAO().voidRelationship(relationship);
	}

	/**
	 * Unretire Relationship
	 * @param Relationship to unvoid
	 * @throws APIException
	 */
	public void unvoidRelationship(Relationship relationship) throws APIException {
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_RELATIONSHIPS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_MANAGE_RELATIONSHIPS);

		getAdministrationDAO().unvoidRelationship(relationship);
	}
		
	/**
	 * Create a new FieldType
	 * @param FieldType to create
	 * @throws APIException
	 */
	public void createFieldType(FieldType fieldType) throws APIException {
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_FIELD_TYPES))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_MANAGE_FIELD_TYPES);

		getAdministrationDAO().createFieldType(fieldType);
	}

	/**
	 * Update FieldType
	 * @param FieldType to update
	 * @throws APIException
	 */
	public void updateFieldType(FieldType fieldType) throws APIException {
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_FIELD_TYPES))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_MANAGE_FIELD_TYPES);

		getAdministrationDAO().updateFieldType(fieldType);
	}

	/**
	 * Delete FieldType
	 * @param FieldType to delete
	 * @throws APIException
	 */
	public void deleteFieldType(FieldType fieldType) throws APIException {
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_FIELD_TYPES))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_MANAGE_FIELD_TYPES);

		getAdministrationDAO().deleteFieldType(fieldType);
	}
	
	/**
	 * Create a new MimeType
	 * @param MimeType to create
	 * @throws APIException
	 */
	public void createMimeType(MimeType mimeType) throws APIException {
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_MIME_TYPES))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_MANAGE_MIME_TYPES);

		getAdministrationDAO().createMimeType(mimeType);
	}

	/**
	 * Update MimeType
	 * @param MimeType to update
	 * @throws APIException
	 */
	public void updateMimeType(MimeType mimeType) throws APIException {
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_MIME_TYPES))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_MANAGE_MIME_TYPES);

		getAdministrationDAO().updateMimeType(mimeType);
	}

	/**
	 * Delete MimeType
	 * @param MimeType to delete
	 * @throws APIException
	 */
	public void deleteMimeType(MimeType mimeType) throws APIException {
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_MIME_TYPES))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_MANAGE_MIME_TYPES);

		getAdministrationDAO().deleteMimeType(mimeType);
	}

	/**
	 * Create a new Location
	 * @param Location to create
	 * @throws APIException
	 */
	public void createLocation(Location location) throws APIException {
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_LOCATIONS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_MANAGE_LOCATIONS);

		getAdministrationDAO().createLocation(location);
	}

	/**
	 * Update Location
	 * @param Location to update
	 * @throws APIException
	 */
	public void updateLocation(Location location) throws APIException {
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_LOCATIONS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_MANAGE_LOCATIONS);

		getAdministrationDAO().updateLocation(location);
	}

	/**
	 * Delete Location
	 * @param Location to delete
	 * @throws APIException
	 */
	public void deleteLocation(Location location) throws APIException {
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_LOCATIONS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_MANAGE_LOCATIONS);

		getAdministrationDAO().deleteLocation(location);
	}
	
	/**
	 * Create a new RelationshipType
	 * @param RelationshipType to create
	 * @throws APIException
	 */
	public void createRelationshipType(RelationshipType relationshipType) throws APIException {
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_RELATIONSHIP_TYPES))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_MANAGE_RELATIONSHIP_TYPES);

		getAdministrationDAO().createRelationshipType(relationshipType);
	}

	/**
	 * Update RelationshipType
	 * @param RelationshipType to update
	 * @throws APIException
	 */
	public void updateRelationshipType(RelationshipType relationshipType) throws APIException {
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_RELATIONSHIP_TYPES))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_MANAGE_RELATIONSHIP_TYPES);

		getAdministrationDAO().updateRelationshipType(relationshipType);
	}

	/**
	 * Delete RelationshipType
	 * @param RelationshipType to delete
	 * @throws APIException
	 */
	public void deleteRelationshipType(RelationshipType relationshipType) throws APIException {
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_RELATIONSHIP_TYPES))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_MANAGE_RELATIONSHIP_TYPES);

		getAdministrationDAO().deleteRelationshipType(relationshipType);
	}

	/**
	 * Create a new Role
	 * @param Role to create
	 * @throws APIException
	 */
	public void createRole(Role role) throws APIException {
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_ROLES))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_MANAGE_ROLES);

		checkLooping(role);
		
		checkPrivileges(role);
		
		getAdministrationDAO().createRole(role);
	}

	/**
	 * Update Role
	 * @param Role to update
	 * @throws APIException
	 */
	public void updateRole(Role role) throws APIException {
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_ROLES))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_MANAGE_ROLES);

		checkLooping(role);
		
		checkPrivileges(role);
		
		getAdministrationDAO().updateRole(role);
	}

	/**
	 * Delete Role
	 * @param Role to delete
	 * @throws APIException
	 */
	public void deleteRole(Role role) throws APIException {
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_ROLES))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_MANAGE_ROLES);

		if (OpenmrsConstants.CORE_ROLES().keySet().contains(role.getRole()))
			throw new APIException("Cannot delete a core role");
		getAdministrationDAO().deleteRole(role);
	}

	/**
	 * Create a new Privilege
	 * @param Privilege to create
	 * @throws APIException
	 */
	public void createPrivilege(Privilege privilege) throws APIException {
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_PRIVILEGES))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_MANAGE_PRIVILEGES);

		getAdministrationDAO().createPrivilege(privilege);
	}

	/**
	 * Update Privilege
	 * @param Privilege to update
	 * @throws APIException
	 */
	public void updatePrivilege(Privilege privilege) throws APIException {
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_PRIVILEGES))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_MANAGE_PRIVILEGES);

		getAdministrationDAO().updatePrivilege(privilege);
	}

	/**
	 * Delete Privilege
	 * @param Privilege to delete
	 * @throws APIException
	 */
	public void deletePrivilege(Privilege privilege) throws APIException {
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_PRIVILEGES))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_MANAGE_PRIVILEGES);

		if (OpenmrsConstants.CORE_PRIVILEGES().keySet().contains(privilege.getPrivilege()))
			throw new APIException("Cannot delete a core privilege");
			getAdministrationDAO().deletePrivilege(privilege);
	}

	/**
	 * Create a new ConceptClass
	 * @param ConceptClass to create
	 * @throws APIException
	 */
	public void createConceptClass(ConceptClass cc) throws APIException {
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_CONCEPT_CLASSES))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_MANAGE_CONCEPT_CLASSES);

		getAdministrationDAO().createConceptClass(cc);
	}

	/**
	 * Update ConceptClass
	 * @param ConceptClass to update
	 * @throws APIException
	 */
	public void updateConceptClass(ConceptClass cc) throws APIException {
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_CONCEPT_CLASSES))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_MANAGE_CONCEPT_CLASSES);

		getAdministrationDAO().updateConceptClass(cc);
	}

	/**
	 * Delete ConceptClass
	 * @param ConceptClass to delete
	 * @throws APIException
	 */
	public void deleteConceptClass(ConceptClass cc) throws APIException {
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_CONCEPT_CLASSES))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_MANAGE_CONCEPT_CLASSES);

		getAdministrationDAO().deleteConceptClass(cc);
	}

	/**
	 * Create a new ConceptDatatype
	 * @param ConceptDatatype to create
	 * @throws APIException
	 */
	public void createConceptDatatype(ConceptDatatype cd) throws APIException {
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_CONCEPT_DATATYPES))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_MANAGE_CONCEPT_DATATYPES);

		getAdministrationDAO().createConceptDatatype(cd);
	}

	/**
	 * Update ConceptDatatype
	 * @param ConceptDatatype to update
	 * @throws APIException
	 */
	public void updateConceptDatatype(ConceptDatatype cd) throws APIException {
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_CONCEPT_DATATYPES))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_MANAGE_CONCEPT_DATATYPES);

		getAdministrationDAO().updateConceptDatatype(cd);
	}

	/**
	 * Delete ConceptDatatype
	 * @param ConceptDatatype to delete
	 * @throws APIException
	 */
	public void deleteConceptDatatype(ConceptDatatype cd) throws APIException {
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_CONCEPT_DATATYPES))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_MANAGE_CONCEPT_DATATYPES);

		getAdministrationDAO().deleteConceptDatatype(cd);
	}
	
	/**
	 * Create a new Report
	 * @param Report to create
	 * @throws APIException
	 */
	public void createReport(Report report) throws APIException {
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_ADD_REPORTS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_ADD_REPORTS);

		getAdministrationDAO().createReport(report);
	}

	/**
	 * Update Report
	 * @param Report to update
	 * @throws APIException
	 */
	public void updateReport(Report report) throws APIException {
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_EDIT_REPORTS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_EDIT_REPORTS);

		getAdministrationDAO().updateReport(report);
	}

	/**
	 * Delete Report
	 * @param Report to delete
	 * @throws APIException
	 */
	public void deleteReport(Report report) throws APIException {
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_DELETE_REPORTS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_DELETE_REPORTS);

		getAdministrationDAO().deleteReport(report);
	}
	
	/**
	 * Create a new Report Object
	 * @param Report Object to create
	 * @throws APIException
	 */
	public void createReportObject(AbstractReportObject reportObject) throws APIException {
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_ADD_REPORT_OBJECTS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_ADD_REPORT_OBJECTS);

		getAdministrationDAO().createReportObject(reportObject);
	}

	/**
	 * Update Report Object
	 * @param Report Object to update
	 * @throws APIException
	 */
	public void updateReportObject(AbstractReportObject reportObject) throws APIException {
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_EDIT_REPORT_OBJECTS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_EDIT_REPORT_OBJECTS);

		getAdministrationDAO().updateReportObject(reportObject);
	}

	/**
	 * Delete Report Object
	 * @param Report Object to delete
	 * @throws APIException
	 */
	public void deleteReportObject(Integer reportObjectId) throws APIException {
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_DELETE_REPORT_OBJECTS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_DELETE_REPORT_OBJECTS);

		getAdministrationDAO().deleteReportObject(reportObjectId);
	}

	/**
	 * Iterates over the words in names and synonyms (for each locale) and updates the concept word business table 
	 * @param concept
	 * @throws APIException
	 */
	public void updateConceptWord(Concept concept) throws APIException {
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_EDIT_CONCEPTS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_EDIT_CONCEPTS);

		getAdministrationDAO().updateConceptWord(concept);
	}
	
	/**
	 * Iterates over all concepts calling updateConceptWord(concept)
	 * @throws APIException
	 */
	public void updateConceptWords() throws APIException {
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_EDIT_CONCEPTS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_EDIT_CONCEPTS);

		getAdministrationDAO().updateConceptWords();
	}
	
	/**
	 * Updates the concept set derived business table for this concept (bursting the concept sets) 
	 * @param concept
	 * @throws APIException
	 */
	public void updateConceptSetDerived(Concept concept) throws APIException {
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_EDIT_CONCEPTS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_EDIT_CONCEPTS);

		getAdministrationDAO().updateConceptSetDerived(concept);
	}
	
	/**
	 * Iterates over all concepts calling updateConceptSetDerived(concept)
	 * @throws APIException
	 */
	public void updateConceptSetDerived() throws APIException {
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_EDIT_CONCEPTS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_EDIT_CONCEPTS);

		getAdministrationDAO().updateConceptSetDerived();
	}
	
	public void createConceptProposal(ConceptProposal cp) throws APIException {
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_FORM_ENTRY))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_FORM_ENTRY);

		getAdministrationDAO().createConceptProposal(cp);
	}
	
	public void updateConceptProposal(ConceptProposal cp) throws APIException {
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_FORM_ENTRY))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_FORM_ENTRY);

		cp.setChangedBy(Context.getAuthenticatedUser());
		cp.setDateChanged(new Date());
		getAdministrationDAO().updateConceptProposal(cp);
	}
	
	public void mapConceptProposalToConcept(ConceptProposal cp, Concept mappedConcept) throws APIException {
		
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_ADD_CONCEPTS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_ADD_CONCEPTS);
		
		if (cp.getState().equals(OpenmrsConstants.CONCEPT_PROPOSAL_REJECT)) {
			rejectConceptProposal(cp);
		}
		
		if (mappedConcept == null)
			throw new APIException("Illegal Mapped Concept");
		
		if (cp.getState().equals(OpenmrsConstants.CONCEPT_PROPOSAL_CONCEPT) || 
				!StringUtils.hasText(cp.getFinalText())) {
			cp.setState(OpenmrsConstants.CONCEPT_PROPOSAL_CONCEPT);
			cp.setFinalText("");
		}
		else if (cp.getState().equals(OpenmrsConstants.CONCEPT_PROPOSAL_SYNONYM)); {
			String finalText = cp.getFinalText();
			ConceptSynonym syn = new ConceptSynonym(mappedConcept, finalText, Context.getLocale());
			syn.setDateCreated(new Date());
			syn.setCreator(Context.getAuthenticatedUser());
			
			mappedConcept.addSynonym(syn);
			updateConceptWord(mappedConcept);
		}
		
		cp.setMappedConcept(mappedConcept);
		
		if (cp.getObsConcept() != null) {
			Obs ob = new Obs();
			ob.setEncounter(cp.getEncounter());
			ob.setConcept(cp.getObsConcept());
			ob.setValueCoded(cp.getMappedConcept());
			ob.setCreator(Context.getAuthenticatedUser());
			ob.setDateCreated(cp.getDateCreated());
			ob.setObsDatetime(cp.getEncounter().getEncounterDatetime());
			ob.setLocation(cp.getEncounter().getLocation());
			ob.setPatient(cp.getEncounter().getPatient());
			cp.setObs(ob);
		}
		
		updateConceptProposal(cp);
	}
	
	public void rejectConceptProposal(ConceptProposal cp) {
		cp.setState(OpenmrsConstants.CONCEPT_PROPOSAL_REJECT);
		cp.setFinalText("");
		updateConceptProposal(cp);
	}
	
	/**
	 * This function checks if the authenticated user has all privileges they are giving out to the new role
	 * @param new user that has privileges 
	 */
	private void checkPrivileges(Role role) {
		Collection<Privilege> privileges = role.getPrivileges();
		
		for (Privilege p : privileges) {
			if (!Context.hasPrivilege(p.getPrivilege()))
				throw new APIAuthenticationException("Privilege required: " + p);
		}
	}
	
	private void checkLooping(Role role) {
		
		log.debug("allParentRoles: " + role.getAllParentRoles());
		log.debug("role: " + role);
		
		if (role.getAllParentRoles().contains(role))
			throw new APIAuthenticationException("Invalid Role or parent Role.  A role cannot inherit itself.");
		
	}
	
	public void mrnGeneratorLog(String site, Integer start, Integer count) {
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_EDIT_PATIENTS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_EDIT_PATIENTS);

		getAdministrationDAO().mrnGeneratorLog(site, start, count);
	}
	
	public Collection getMRNGeneratorLog() {
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_EDIT_PATIENTS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_EDIT_PATIENTS);

		return getAdministrationDAO().getMRNGeneratorLog();		
	}
	
	public SortedMap<String,String> getSystemVariables() {
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_VIEW_ADMIN_FUNCTIONS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_VIEW_ADMIN_FUNCTIONS);
		TreeMap<String,String> systemVariables = new TreeMap<String,String>();
		systemVariables.put("OPENMRS_VERSION", String.valueOf(OpenmrsConstants.OPENMRS_VERSION));
		systemVariables.put("DATABASE_VERSION_EXPECTED", String.valueOf(OpenmrsConstants.DATABASE_VERSION_EXPECTED));
		systemVariables.put("DATABASE_VERSION", String.valueOf(OpenmrsConstants.DATABASE_VERSION));
		systemVariables.put("DATABASE_NAME",OpenmrsConstants.DATABASE_NAME);
		systemVariables.put("DATABASE_BUSINESS_NAME",OpenmrsConstants.DATABASE_BUSINESS_NAME);
		systemVariables.put("OBSCURE_PATIENTS", String.valueOf(OpenmrsConstants.OBSCURE_PATIENTS));
		systemVariables.put("OBSCURE_PATIENTS_FAMILY_NAME",OpenmrsConstants.OBSCURE_PATIENTS_FAMILY_NAME);
		systemVariables.put("OBSCURE_PATIENTS_GIVEN_NAME",OpenmrsConstants.OBSCURE_PATIENTS_GIVEN_NAME);
		systemVariables.put("OBSCURE_PATIENTS_MIDDLE_NAME",OpenmrsConstants.OBSCURE_PATIENTS_MIDDLE_NAME);
		systemVariables.put("STOP_WORDS",OpenmrsConstants.STOP_WORDS().toString());
		systemVariables.put("MODULE_REPOSITORY_PATH", String.valueOf(OpenmrsConstants.MODULE_REPOSITORY_PATH));
		systemVariables.put("OPERATING_SYSTEM_KEY", String.valueOf(OpenmrsConstants.OPERATING_SYSTEM_KEY));
		systemVariables.put("OPERATING_SYSTEM", String.valueOf(OpenmrsConstants.OPERATING_SYSTEM));
		systemVariables.put("OPERATING_SYSTEM_WINDOWS_XP", String.valueOf(OpenmrsConstants.OPERATING_SYSTEM_WINDOWS_XP));
		systemVariables.put("OPERATING_SYSTEM_LINUX", String.valueOf(OpenmrsConstants.OPERATING_SYSTEM_LINUX));
		
		return systemVariables;
	}
	
	
	public String getGlobalProperty(String propertyName) {
		// uses direct access to the dao object to bypass isAuthenticated() check
		return dao.getGlobalProperty(propertyName);
	}
	
	public String getGlobalProperty(String propertyName, String defaultValue) { 
		String s = getGlobalProperty(propertyName);
		if (s == null)
			return defaultValue;
		return s;
	}
	
	public List<GlobalProperty> getGlobalProperties() {
		return getAdministrationDAO().getGlobalProperties();
	}
	
	public void setGlobalProperties(List<GlobalProperty> props) {
		getAdministrationDAO().setGlobalProperties(props);
	}
	
	public void deleteGlobalProperty(String propertyName) {
		getAdministrationDAO().deleteGlobalProperty(propertyName);
	}

	public void setGlobalProperty(String propertyName, String propertyValue) {
		getAdministrationDAO().setGlobalProperty(propertyName, propertyValue);
	}

	public void addGlobalProperty(String propertyName, String propertyValue) {
		getAdministrationDAO().addGlobalProperty(propertyName, propertyValue);
	}
	
	public List<DataEntryStatistic> getDataEntryStatistics(Date fromDate, Date toDate, String encounterUserColumn, String orderUserColumn, String groupBy) {
		return getAdministrationDAO().getDataEntryStatistics(fromDate, toDate, encounterUserColumn, orderUserColumn, groupBy);
	}
}

package org.openmrs.api;

import java.util.Collection;
import java.util.List;
import java.util.SortedMap;

import org.openmrs.Concept;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptDatatype;
import org.openmrs.ConceptProposal;
import org.openmrs.EncounterType;
import org.openmrs.FieldType;
import org.openmrs.GlobalProperty;
import org.openmrs.Location;
import org.openmrs.MimeType;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifierType;
import org.openmrs.Person;
import org.openmrs.Privilege;
import org.openmrs.Relationship;
import org.openmrs.RelationshipType;
import org.openmrs.Role;
import org.openmrs.Tribe;
import org.openmrs.User;
import org.openmrs.api.APIException;
import org.openmrs.api.db.AdministrationDAO;
import org.openmrs.reporting.AbstractReportObject;
import org.openmrs.reporting.Report;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface AdministrationService {

	public void setAdministrationDAO(AdministrationDAO dao);

	/**
	 * Create a new Person
	 * @param Person to create
	 * @throws APIException
	 */
	public void createPerson(Person person) throws APIException;
	
	/**
	 * Update an encounter type
	 * @param Person to update
	 * @throws APIException
	 */
	public void updatePerson(Person person) throws APIException;

	/**
	 * Delete an encounter type
	 * @param Person to delete
	 * @throws APIException
	 */
	public void deletePerson(Person person) throws APIException;

	/**
	 * 
	 * @param personId to get
	 * @return Person
	 * @throws APIException
	 */
	@Transactional(readOnly=true)
	public Person getPerson(Integer personId) throws APIException;

	@Transactional(readOnly=true)
	public Person getPerson(Patient pat) throws APIException;

	@Transactional(readOnly=true)
	public Person getPerson(User user) throws APIException;

	/**
	 * Create a new EncounterType
	 * @param EncounterType to create
	 * @throws APIException
	 */
	public void createEncounterType(EncounterType encounterType)
			throws APIException;

	/**
	 * Update an encounter type
	 * @param EncounterType to update
	 * @throws APIException
	 */
	public void updateEncounterType(EncounterType encounterType)
			throws APIException;

	/**
	 * Delete an encounter type
	 * @param EncounterType to delete
	 * @throws APIException
	 */
	public void deleteEncounterType(EncounterType encounterType)
			throws APIException;

	/**
	 * Create a new PatientIdentifierType
	 * @param PatientIdentifierType to create
	 * @throws APIException
	 */
	public void createPatientIdentifierType(
			PatientIdentifierType patientIdentifierType) throws APIException;

	/**
	 * Update PatientIdentifierType
	 * @param PatientIdentifierType to update
	 * @throws APIException
	 */
	public void updatePatientIdentifierType(
			PatientIdentifierType patientIdentifierType) throws APIException;

	/**
	 * Delete PatientIdentifierType
	 * @param PatientIdentifierType to delete
	 * @throws APIException
	 */
	public void deletePatientIdentifierType(
			PatientIdentifierType patientIdentifierType) throws APIException;

	/**
	 * Create a new Tribe
	 * @param Tribe to create
	 * @throws APIException
	 */
	public void createTribe(Tribe tribe) throws APIException;

	/**
	 * Update Tribe
	 * @param Tribe to update
	 * @throws APIException
	 */
	public void updateTribe(Tribe tribe) throws APIException;

	/**
	 * Delete Tribe
	 * @param Tribe to delete
	 * @throws APIException
	 */
	public void deleteTribe(Tribe tribe) throws APIException;

	/**
	 * Retire Tribe
	 * @param Tribe to retire
	 * @throws APIException
	 */
	public void retireTribe(Tribe tribe) throws APIException;

	/**
	 * Unretire Tribe
	 * @param Tribe to unretire
	 * @throws APIException
	 */
	public void unretireTribe(Tribe tribe) throws APIException;

	/**
	 * Create a new Relationship
	 * @param Relationship to create
	 * @throws APIException
	 */
	public void createRelationship(Relationship relationship)
			throws APIException;

	/**
	 * Update Relationship
	 * @param Relationship to update
	 * @throws APIException
	 */
	public void updateRelationship(Relationship relationship)
			throws APIException;

	/**
	 * Delete Relationship
	 * @param Relationship to delete
	 * @throws APIException
	 */
	public void deleteRelationship(Relationship relationship)
			throws APIException;

	/**
	 * Retire Relationship
	 * @param Relationship to void
	 * @throws APIException
	 */
	public void voidRelationship(Relationship relationship) throws APIException;

	/**
	 * Unretire Relationship
	 * @param Relationship to unvoid
	 * @throws APIException
	 */
	public void unvoidRelationship(Relationship relationship)
			throws APIException;

	/**
	 * Create a new FieldType
	 * @param FieldType to create
	 * @throws APIException
	 */
	public void createFieldType(FieldType fieldType) throws APIException;

	/**
	 * Update FieldType
	 * @param FieldType to update
	 * @throws APIException
	 */
	public void updateFieldType(FieldType fieldType) throws APIException;

	/**
	 * Delete FieldType
	 * @param FieldType to delete
	 * @throws APIException
	 */
	public void deleteFieldType(FieldType fieldType) throws APIException;

	/**
	 * Create a new MimeType
	 * @param MimeType to create
	 * @throws APIException
	 */
	public void createMimeType(MimeType mimeType) throws APIException;

	/**
	 * Update MimeType
	 * @param MimeType to update
	 * @throws APIException
	 */
	public void updateMimeType(MimeType mimeType) throws APIException;

	/**
	 * Delete MimeType
	 * @param MimeType to delete
	 * @throws APIException
	 */
	public void deleteMimeType(MimeType mimeType) throws APIException;

	/**
	 * Create a new Location
	 * @param Location to create
	 * @throws APIException
	 */
	public void createLocation(Location location) throws APIException;

	/**
	 * Update Location
	 * @param Location to update
	 * @throws APIException
	 */
	public void updateLocation(Location location) throws APIException;

	/**
	 * Delete Location
	 * @param Location to delete
	 * @throws APIException
	 */
	public void deleteLocation(Location location) throws APIException;

	/**
	 * Create a new RelationshipType
	 * @param RelationshipType to create
	 * @throws APIException
	 */
	public void createRelationshipType(RelationshipType relationshipType)
			throws APIException;

	/**
	 * Update RelationshipType
	 * @param RelationshipType to update
	 * @throws APIException
	 */
	public void updateRelationshipType(RelationshipType relationshipType)
			throws APIException;

	/**
	 * Delete RelationshipType
	 * @param RelationshipType to delete
	 * @throws APIException
	 */
	public void deleteRelationshipType(RelationshipType relationshipType)
			throws APIException;

	/**
	 * Create a new Role
	 * @param Role to create
	 * @throws APIException
	 */
	public void createRole(Role role) throws APIException;

	/**
	 * Update Role
	 * @param Role to update
	 * @throws APIException
	 */
	public void updateRole(Role role) throws APIException;

	/**
	 * Delete Role
	 * @param Role to delete
	 * @throws APIException
	 */
	public void deleteRole(Role role) throws APIException;

	/**
	 * Create a new Privilege
	 * @param Privilege to create
	 * @throws APIException
	 */
	public void createPrivilege(Privilege privilege) throws APIException;

	/**
	 * Update Privilege
	 * @param Privilege to update
	 * @throws APIException
	 */
	public void updatePrivilege(Privilege privilege) throws APIException;

	/**
	 * Delete Privilege
	 * @param Privilege to delete
	 * @throws APIException
	 */
	public void deletePrivilege(Privilege privilege) throws APIException;

	/**
	 * Create a new ConceptClass
	 * @param ConceptClass to create
	 * @throws APIException
	 */
	public void createConceptClass(ConceptClass cc) throws APIException;

	/**
	 * Update ConceptClass
	 * @param ConceptClass to update
	 * @throws APIException
	 */
	public void updateConceptClass(ConceptClass cc) throws APIException;

	/**
	 * Delete ConceptClass
	 * @param ConceptClass to delete
	 * @throws APIException
	 */
	public void deleteConceptClass(ConceptClass cc) throws APIException;

	/**
	 * Create a new ConceptDatatype
	 * @param ConceptDatatype to create
	 * @throws APIException
	 */
	public void createConceptDatatype(ConceptDatatype cd) throws APIException;

	/**
	 * Update ConceptDatatype
	 * @param ConceptDatatype to update
	 * @throws APIException
	 */
	public void updateConceptDatatype(ConceptDatatype cd) throws APIException;

	/**
	 * Delete ConceptDatatype
	 * @param ConceptDatatype to delete
	 * @throws APIException
	 */
	public void deleteConceptDatatype(ConceptDatatype cd) throws APIException;

	/**
	 * Create a new Report
	 * @param Report to create
	 * @throws APIException
	 */
	public void createReport(Report report) throws APIException;

	/**
	 * Update Report
	 * @param Report to update
	 * @throws APIException
	 */
	public void updateReport(Report report) throws APIException;

	/**
	 * Delete Report
	 * @param Report to delete
	 * @throws APIException
	 */
	public void deleteReport(Report report) throws APIException;

	/**
	 * Create a new Report Object
	 * @param Report Object to create
	 * @throws APIException
	 */
	public void createReportObject(AbstractReportObject reportObject)
			throws APIException;

	/**
	 * Update Report Object
	 * @param Report Object to update
	 * @throws APIException
	 */
	public void updateReportObject(AbstractReportObject reportObject)
			throws APIException;

	/**
	 * Delete Report Object
	 * @param Report Object to delete
	 * @throws APIException
	 */
	public void deleteReportObject(Integer reportObjectId) throws APIException;

	/**
	 * Iterates over the words in names and synonyms (for each locale) and updates the concept word business table 
	 * @param concept
	 * @throws APIException
	 */
	public void updateConceptWord(Concept concept) throws APIException;

	/**
	 * Iterates over all concepts calling updateConceptWord(concept)
	 * @throws APIException
	 */
	public void updateConceptWords() throws APIException;

	/**
	 * Updates the concept set derived business table for this concept (bursting the concept sets) 
	 * @param concept
	 * @throws APIException
	 */
	public void updateConceptSetDerived(Concept concept) throws APIException;

	/**
	 * Iterates over all concepts calling updateConceptSetDerived(concept)
	 * @throws APIException
	 */
	public void updateConceptSetDerived() throws APIException;

	public void createConceptProposal(ConceptProposal cp) throws APIException;

	public void updateConceptProposal(ConceptProposal cp) throws APIException;

	public void mapConceptProposalToConcept(ConceptProposal cp,
			Concept mappedConcept) throws APIException;

	public void rejectConceptProposal(ConceptProposal cp);

	public void mrnGeneratorLog(String site, Integer start, Integer count);

	public Collection getMRNGeneratorLog();

	public SortedMap<String, String> getSystemVariables();

	@Transactional(readOnly=true)
	public String getGlobalProperty(String propertyName);

	@Transactional(readOnly=true)
	public String getGlobalProperty(String propertyName, String defaultValue);

	@Transactional(readOnly=true)
	public List<GlobalProperty> getGlobalProperties();

	public void setGlobalProperties(List<GlobalProperty> props);

	public void deleteGlobalProperty(String propertyName);

	public void setGlobalProperty(String propertyName, String propertyValue);

	public void addGlobalProperty(String propertyName, String propertyValue);

}
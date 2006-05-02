package org.openmrs.api.db;

import java.util.Collection;

import org.openmrs.Concept;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptDatatype;
import org.openmrs.ConceptProposal;
import org.openmrs.EncounterType;
import org.openmrs.FieldType;
import org.openmrs.Location;
import org.openmrs.MimeType;
import org.openmrs.OrderType;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifierType;
import org.openmrs.Person;
import org.openmrs.Privilege;
import org.openmrs.Relationship;
import org.openmrs.RelationshipType;
import org.openmrs.Role;
import org.openmrs.Tribe;
import org.openmrs.reporting.AbstractReportObject;
import org.openmrs.reporting.Report;

/**
 * Admin-related database functions
 * 
 * @author Ben Wolfe
 * @author Burke Mamlin
 * @version 1.0
 */
public interface AdministrationDAO {
	
	/**
	 * Create a new Person
	 * @param Person to create
	 * @throws DAOException
	 */
	public void createPerson(Person person) throws DAOException;

	/**
	 * Update a person
	 * @param Person to update
	 * @throws DAOException
	 */
	public void updatePerson(Person person) throws DAOException;

	/**
	 * Delete a person
	 * @param Person to delete
	 * @throws DAOException
	 */
	public void deletePerson(Person person) throws DAOException;
	
	/**
	 * 
	 * @param personId of the Person to retrieve
	 * @return Person
	 * @throws DAOException
	 */
	public Person getPerson(Integer personId) throws DAOException;
	
	public Person getPerson(Patient pat) throws DAOException;
	
	/**
	 * Create a new EncounterType
	 * @param EncounterType to create
	 * @throws DAOException
	 */
	public void createEncounterType(EncounterType encounterType) throws DAOException;

	/**
	 * Update an encounter type
	 * @param EncounterType to update
	 * @throws DAOException
	 */
	public void updateEncounterType(EncounterType encounterType) throws DAOException;

	/**
	 * Delete an encounter type
	 * @param EncounterType to delete
	 * @throws DAOException
	 */
	public void deleteEncounterType(EncounterType encounterType) throws DAOException;

	/**
	 * Create a new PatientIdentifierType
	 * @param PatientIdentifierType to create
	 * @throws DAOException
	 */
	public void createPatientIdentifierType(PatientIdentifierType patientIdentifierType) throws DAOException;

	/**
	 * Update PatientIdentifierType
	 * @param PatientIdentifierType to update
	 * @throws DAOException
	 */
	public void updatePatientIdentifierType(PatientIdentifierType patientIdentifierType) throws DAOException;
	
	/**
	 * Delete PatientIdentifierType
	 * @param PatientIdentifierType to delete
	 * @throws DAOException
	 */
	public void deletePatientIdentifierType(PatientIdentifierType patientIdentifierType) throws DAOException;

	/**
	 * Create a new Tribe
	 * @param Tribe to create
	 * @throws DAOException
	 */
	public void createTribe(Tribe tribe) throws DAOException;

	/**
	 * Update Tribe
	 * @param Tribe to update
	 * @throws DAOException
	 */
	public void updateTribe(Tribe tribe) throws DAOException;

	/**
	 * Delete Tribe
	 * @param Tribe to delete
	 * @throws DAOException
	 */
	public void deleteTribe(Tribe tribe) throws DAOException;	
	
	/**
	 * Retire Tribe
	 * @param Tribe to retire
	 * @throws DAOException
	 */
	public void retireTribe(Tribe tribe) throws DAOException;	

	/**
	 * Unretire Tribe
	 * @param Tribe to unretire
	 * @throws DAOException
	 */
	public void unretireTribe(Tribe tribe) throws DAOException;	

	/**
	 * Create a new Relationship
	 * @param Relationship to create
	 * @throws DAOException
	 */
	public void createRelationship(Relationship relationship) throws DAOException;

	/**
	 * Update Relationship
	 * @param Relationship to update
	 * @throws DAOException
	 */
	public void updateRelationship(Relationship relationship) throws DAOException;

	/**
	 * Delete Relationship
	 * @param Relationship to delete
	 * @throws DAOException
	 */
	public void deleteRelationship(Relationship relationship) throws DAOException;	
	
	/**
	 * Retire Relationship
	 * @param Relationship to void
	 * @throws DAOException
	 */
	public void voidRelationship(Relationship relationship) throws DAOException;	

	/**
	 * Unretire Relationship
	 * @param Relationship to unvoid
	 * @throws DAOException
	 */
	public void unvoidRelationship(Relationship relationship) throws DAOException;	

	
	/**
	 * Create a new OrderType
	 * @param OrderType to create
	 * @throws DAOException
	 */
	public void createOrderType(OrderType orderType) throws DAOException;

	/**
	 * Update OrderType
	 * @param OrderType to update
	 * @throws DAOException
	 */
	public void updateOrderType(OrderType orderType) throws DAOException;

	/**
	 * Delete OrderType
	 * @param OrderType to delete
	 * @throws DAOException
	 */
	public void deleteOrderType(OrderType orderType) throws DAOException;
	
	/**
	 * Create a new FieldType
	 * @param FieldType to create
	 * @throws DAOException
	 */
	public void createFieldType(FieldType fieldType) throws DAOException;

	/**
	 * Update FieldType
	 * @param FieldType to update
	 * @throws DAOException
	 */
	public void updateFieldType(FieldType fieldType) throws DAOException;

	/**
	 * Delete FieldType
	 * @param FieldType to delete
	 * @throws DAOException
	 */
	public void deleteFieldType(FieldType fieldType) throws DAOException;
	
	/**
	 * Create a new MimeType
	 * @param MimeType to create
	 * @throws DAOException
	 */
	public void createMimeType(MimeType mimeType) throws DAOException;

	/**
	 * Update MimeType
	 * @param MimeType to update
	 * @throws DAOException
	 */
	public void updateMimeType(MimeType mimeType) throws DAOException;

	/**
	 * Delete MimeType
	 * @param MimeType to delete
	 * @throws DAOException
	 */
	public void deleteMimeType(MimeType mimeType) throws DAOException;	

	/**
	 * Create a new Location
	 * @param Location to create
	 * @throws DAOException
	 */
	public void createLocation(Location location) throws DAOException;

	/**
	 * Update Location
	 * @param Location to update
	 * @throws DAOException
	 */
	public void updateLocation(Location location) throws DAOException;

	/**
	 * Delete Location
	 * @param Location to delete
	 * @throws DAOException
	 */
	public void deleteLocation(Location location) throws DAOException;	
	
	/**
	 * Create a new RelationshipType
	 * @param RelationshipType to create
	 * @throws DAOException
	 */
	public void createRelationshipType(RelationshipType relationshipType) throws DAOException;

	/**
	 * Update RelationshipType
	 * @param RelationshipType to update
	 * @throws DAOException
	 */
	public void updateRelationshipType(RelationshipType relationshipType) throws DAOException;

	/**
	 * Delete RelationshipType
	 * @param RelationshipType to delete
	 * @throws DAOException
	 */
	public void deleteRelationshipType(RelationshipType relationshipType) throws DAOException;	

	/**
	 * Create a new Role
	 * @param Role to create
	 * @throws DAOException
	 */
	public void createRole(Role role) throws DAOException;

	/**
	 * Update Role
	 * @param Role to update
	 * @throws DAOException
	 */
	public void updateRole(Role role) throws DAOException;

	/**
	 * Delete Role
	 * @param Role to delete
	 * @throws DAOException
	 */
	public void deleteRole(Role role) throws DAOException;	

	/**
	 * Create a new Privilege
	 * @param Privilege to create
	 * @throws DAOException
	 */
	public void createPrivilege(Privilege privilege) throws DAOException;

	/**
	 * Update Privilege
	 * @param Privilege to update
	 * @throws DAOException
	 */
	public void updatePrivilege(Privilege privilege) throws DAOException;

	/**
	 * Delete Privilege
	 * @param Privilege to delete
	 * @throws DAOException
	 */
	public void deletePrivilege(Privilege privilege) throws DAOException;	

	/**
	 * Create a new ConceptClass
	 * @param ConceptClass to create
	 * @throws DAOException
	 */
	public void createConceptClass(ConceptClass cc) throws DAOException;

	/**
	 * Update ConceptClass
	 * @param ConceptClass to update
	 * @throws DAOException
	 */
	public void updateConceptClass(ConceptClass cc) throws DAOException;

	/**
	 * Delete ConceptClass
	 * @param ConceptClass to delete
	 * @throws DAOException
	 */
	public void deleteConceptClass(ConceptClass cc) throws DAOException;	

	/**
	 * Create a new ConceptDatatype
	 * @param ConceptDatatype to create
	 * @throws DAOException
	 */
	public void createConceptDatatype(ConceptDatatype cd) throws DAOException;

	/**
	 * Update ConceptDatatype
	 * @param ConceptDatatype to update
	 * @throws DAOException
	 */
	public void updateConceptDatatype(ConceptDatatype cd) throws DAOException;

	/**
	 * Delete ConceptDatatype
	 * @param ConceptDatatype to delete
	 * @throws DAOException
	 */
	public void deleteConceptDatatype(ConceptDatatype cd) throws DAOException;	
	
	/**
	 * Create a new Report
	 * @param Report to create
	 * @throws DAOException
	 */
	public void createReport(Report r) throws DAOException;

	/**
	 * Update Report
	 * @param Report to update
	 * @throws DAOException
	 */
	public void updateReport(Report r) throws DAOException;

	/**
	 * Delete Report
	 * @param Report to delete
	 * @throws DAOException
	 */
	public void deleteReport(Report r) throws DAOException;
	
	/**
	 * Create a new Report Object
	 * @param Report Object to create
	 * @throws DAOException
	 */
	public void createReportObject(AbstractReportObject ro) throws DAOException;

	/**
	 * Update Report Object
	 * @param Report Object to update
	 * @throws DAOException
	 */
	public void updateReportObject(AbstractReportObject ro) throws DAOException;

	/**
	 * Delete Report Object
	 * @param Report Objectto delete
	 * @throws DAOException
	 */
	public void deleteReportObject(Integer reportObjectId) throws DAOException;

	/**
	 * Iterates over the words in names and synonyms (for each locale) and updates the concept word business table 
	 * @param concept
	 * @throws DAOException
	 */
	public void updateConceptWord(Concept concept) throws DAOException;
	
	/**
	 * Iterates over all concepts calling updateConceptWord(concept)
	 * @throws DAOException
	 */
	public void updateConceptWords() throws DAOException;
	
	/**
	 * Updates the concept set derived business table for this concept (bursting the concept sets) 
	 * @param concept
	 * @throws DAOException
	 */
	public void updateConceptSetDerived(Concept concept) throws DAOException;
	
	/**
	 * Iterates over all concepts calling updateConceptSetDerived(concept)
	 * @throws DAOException
	 */
	public void updateConceptSetDerived() throws DAOException;
	
	public void createConceptProposal(ConceptProposal cp) throws DAOException;
	
	public void updateConceptProposal(ConceptProposal cp) throws DAOException;
	
	public void mrnGeneratorLog(String site, Integer start, Integer count) throws DAOException;
	
	public Collection getMRNGeneratorLog() throws DAOException;
}
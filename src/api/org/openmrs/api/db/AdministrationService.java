package org.openmrs.api.db;

import org.openmrs.Concept;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptDatatype;
import org.openmrs.EncounterType;
import org.openmrs.FieldType;
import org.openmrs.Location;
import org.openmrs.MimeType;
import org.openmrs.OrderType;
import org.openmrs.PatientIdentifierType;
import org.openmrs.Privilege;
import org.openmrs.RelationshipType;
import org.openmrs.Role;
import org.openmrs.Tribe;
import org.openmrs.reporting.Report;

/**
 * Admin-related services
 * 
 * @author Ben Wolfe
 * @version 1.0
 */
public interface AdministrationService {
	
	/**
	 * Create a new EncounterType
	 * @param EncounterType to create
	 * @throws APIException
	 */
	public void createEncounterType(EncounterType encounterType) throws APIException;

	/**
	 * Update an encounter type
	 * @param EncounterType to update
	 * @throws APIException
	 */
	public void updateEncounterType(EncounterType encounterType) throws APIException;

	/**
	 * Delete an encounter type
	 * @param EncounterType to delete
	 * @throws APIException
	 */
	public void deleteEncounterType(EncounterType encounterType) throws APIException;

	/**
	 * Create a new PatientIdentifierType
	 * @param PatientIdentifierType to create
	 * @throws APIException
	 */
	public void createPatientIdentifierType(PatientIdentifierType patientIdentifierType) throws APIException;

	/**
	 * Update PatientIdentifierType
	 * @param PatientIdentifierType to update
	 * @throws APIException
	 */
	public void updatePatientIdentifierType(PatientIdentifierType patientIdentifierType) throws APIException;
	
	/**
	 * Delete PatientIdentifierType
	 * @param PatientIdentifierType to delete
	 * @throws APIException
	 */
	public void deletePatientIdentifierType(PatientIdentifierType patientIdentifierType) throws APIException;

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
	 * Create a new OrderType
	 * @param OrderType to create
	 * @throws APIException
	 */
	public void createOrderType(OrderType orderType) throws APIException;

	/**
	 * Update OrderType
	 * @param OrderType to update
	 * @throws APIException
	 */
	public void updateOrderType(OrderType orderType) throws APIException;

	/**
	 * Delete OrderType
	 * @param OrderType to delete
	 * @throws APIException
	 */
	public void deleteOrderType(OrderType orderType) throws APIException;
	
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
	public void createRelationshipType(RelationshipType relationshipType) throws APIException;

	/**
	 * Update RelationshipType
	 * @param RelationshipType to update
	 * @throws APIException
	 */
	public void updateRelationshipType(RelationshipType relationshipType) throws APIException;

	/**
	 * Delete RelationshipType
	 * @param RelationshipType to delete
	 * @throws APIException
	 */
	public void deleteRelationshipType(RelationshipType relationshipType) throws APIException;	

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
	public void createConceptClass(ConceptClass privilege) throws APIException;

	/**
	 * Update ConceptClass
	 * @param ConceptClass to update
	 * @throws APIException
	 */
	public void updateConceptClass(ConceptClass privilege) throws APIException;

	/**
	 * Delete ConceptClass
	 * @param ConceptClass to delete
	 * @throws APIException
	 */
	public void deleteConceptClass(ConceptClass privilege) throws APIException;	

	/**
	 * Create a new ConceptDatatype
	 * @param ConceptDatatype to create
	 * @throws APIException
	 */
	public void createConceptDatatype(ConceptDatatype privilege) throws APIException;

	/**
	 * Update ConceptDatatype
	 * @param ConceptDatatype to update
	 * @throws APIException
	 */
	public void updateConceptDatatype(ConceptDatatype privilege) throws APIException;

	/**
	 * Delete ConceptDatatype
	 * @param ConceptDatatype to delete
	 * @throws APIException
	 */
	public void deleteConceptDatatype(ConceptDatatype privilege) throws APIException;	
	
	/**
	 * Create a new Report
	 * @param Report to create
	 * @throws APIException
	 */
	public void createReport(Report privilege) throws APIException;

	/**
	 * Update Report
	 * @param Report to update
	 * @throws APIException
	 */
	public void updateReport(Report privilege) throws APIException;

	/**
	 * Delete Report
	 * @param Report to delete
	 * @throws APIException
	 */
	public void deleteReport(Report privilege) throws APIException;
	
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
}

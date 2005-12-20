package org.openmrs.api;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptDatatype;
import org.openmrs.EncounterType;
import org.openmrs.FieldType;
import org.openmrs.Group;
import org.openmrs.Location;
import org.openmrs.MimeType;
import org.openmrs.OrderType;
import org.openmrs.PatientIdentifierType;
import org.openmrs.Privilege;
import org.openmrs.RelationshipType;
import org.openmrs.Role;
import org.openmrs.Tribe;
import org.openmrs.api.context.Context;
import org.openmrs.reporting.Report;

/**
 * Admin-related services
 * 
 * @author Ben Wolfe
 * @author Burke Mamlin
 * @version 1.0
 */
public class AdministrationService {
	
	protected Log log = LogFactory.getLog(getClass());

	Context context;
	
	public AdministrationService(Context context) {
		this.context = context;
	}
	
	/**
	 * Create a new EncounterType
	 * @param EncounterType to create
	 * @throws APIException
	 */
	public void createEncounterType(EncounterType encounterType) throws APIException {
		context.getDAOContext().getAdministrationDAO().createEncounterType(encounterType);
	}

	/**
	 * Update an encounter type
	 * @param EncounterType to update
	 * @throws APIException
	 */
	public void updateEncounterType(EncounterType encounterType) throws APIException {
		context.getDAOContext().getAdministrationDAO().updateEncounterType(encounterType);
	}

	/**
	 * Delete an encounter type
	 * @param EncounterType to delete
	 * @throws APIException
	 */
	public void deleteEncounterType(EncounterType encounterType) throws APIException {
		context.getDAOContext().getAdministrationDAO().deleteEncounterType(encounterType);
	}

	/**
	 * Create a new PatientIdentifierType
	 * @param PatientIdentifierType to create
	 * @throws APIException
	 */
	public void createPatientIdentifierType(PatientIdentifierType patientIdentifierType) throws APIException {
		context.getDAOContext().getAdministrationDAO().createPatientIdentifierType(patientIdentifierType);
	}

	/**
	 * Update PatientIdentifierType
	 * @param PatientIdentifierType to update
	 * @throws APIException
	 */
	public void updatePatientIdentifierType(PatientIdentifierType patientIdentifierType) throws APIException {
		context.getDAOContext().getAdministrationDAO().updatePatientIdentifierType(patientIdentifierType);
	}
	
	/**
	 * Delete PatientIdentifierType
	 * @param PatientIdentifierType to delete
	 * @throws APIException
	 */
	public void deletePatientIdentifierType(PatientIdentifierType patientIdentifierType) throws APIException {
		context.getDAOContext().getAdministrationDAO().deletePatientIdentifierType(patientIdentifierType);
	}

	/**
	 * Create a new Tribe
	 * @param Tribe to create
	 * @throws APIException
	 */
	public void createTribe(Tribe tribe) throws APIException {
		context.getDAOContext().getAdministrationDAO().createTribe(tribe);
	}

	/**
	 * Update Tribe
	 * @param Tribe to update
	 * @throws APIException
	 */
	public void updateTribe(Tribe tribe) throws APIException {
		context.getDAOContext().getAdministrationDAO().updateTribe(tribe);
	}

	/**
	 * Delete Tribe
	 * @param Tribe to delete
	 * @throws APIException
	 */
	public void deleteTribe(Tribe tribe) throws APIException {
		context.getDAOContext().getAdministrationDAO().deleteTribe(tribe);
	}
	
	/**
	 * Retire Tribe
	 * @param Tribe to retire
	 * @throws APIException
	 */
	public void retireTribe(Tribe tribe) throws APIException {
		context.getDAOContext().getAdministrationDAO().retireTribe(tribe);
	}

	/**
	 * Unretire Tribe
	 * @param Tribe to unretire
	 * @throws APIException
	 */
	public void unretireTribe(Tribe tribe) throws APIException {
		context.getDAOContext().getAdministrationDAO().unretireTribe(tribe);
	}
	
	/**
	 * Create a new OrderType
	 * @param OrderType to create
	 * @throws APIException
	 */
	public void createOrderType(OrderType orderType) throws APIException {
		context.getDAOContext().getAdministrationDAO().createOrderType(orderType);
	}

	/**
	 * Update OrderType
	 * @param OrderType to update
	 * @throws APIException
	 */
	public void updateOrderType(OrderType orderType) throws APIException {
		context.getDAOContext().getAdministrationDAO().updateOrderType(orderType);
	}

	/**
	 * Delete OrderType
	 * @param OrderType to delete
	 * @throws APIException
	 */
	public void deleteOrderType(OrderType orderType) throws APIException {
		context.getDAOContext().getAdministrationDAO().deleteOrderType(orderType);
	}
	
	/**
	 * Create a new FieldType
	 * @param FieldType to create
	 * @throws APIException
	 */
	public void createFieldType(FieldType fieldType) throws APIException {
		context.getDAOContext().getAdministrationDAO().createFieldType(fieldType);
	}

	/**
	 * Update FieldType
	 * @param FieldType to update
	 * @throws APIException
	 */
	public void updateFieldType(FieldType fieldType) throws APIException {
		context.getDAOContext().getAdministrationDAO().updateFieldType(fieldType);
	}

	/**
	 * Delete FieldType
	 * @param FieldType to delete
	 * @throws APIException
	 */
	public void deleteFieldType(FieldType fieldType) throws APIException {
		context.getDAOContext().getAdministrationDAO().deleteFieldType(fieldType);
	}
	
	/**
	 * Create a new MimeType
	 * @param MimeType to create
	 * @throws APIException
	 */
	public void createMimeType(MimeType mimeType) throws APIException {
		context.getDAOContext().getAdministrationDAO().createMimeType(mimeType);
	}

	/**
	 * Update MimeType
	 * @param MimeType to update
	 * @throws APIException
	 */
	public void updateMimeType(MimeType mimeType) throws APIException {
		context.getDAOContext().getAdministrationDAO().updateMimeType(mimeType);
	}

	/**
	 * Delete MimeType
	 * @param MimeType to delete
	 * @throws APIException
	 */
	public void deleteMimeType(MimeType mimeType) throws APIException {
		context.getDAOContext().getAdministrationDAO().deleteMimeType(mimeType);
	}

	/**
	 * Create a new Location
	 * @param Location to create
	 * @throws APIException
	 */
	public void createLocation(Location location) throws APIException {
		context.getDAOContext().getAdministrationDAO().createLocation(location);
	}

	/**
	 * Update Location
	 * @param Location to update
	 * @throws APIException
	 */
	public void updateLocation(Location location) throws APIException {
		context.getDAOContext().getAdministrationDAO().updateLocation(location);
	}

	/**
	 * Delete Location
	 * @param Location to delete
	 * @throws APIException
	 */
	public void deleteLocation(Location location) throws APIException {
		context.getDAOContext().getAdministrationDAO().deleteLocation(location);
	}
	
	/**
	 * Create a new RelationshipType
	 * @param RelationshipType to create
	 * @throws APIException
	 */
	public void createRelationshipType(RelationshipType relationshipType) throws APIException {
		context.getDAOContext().getAdministrationDAO().createRelationshipType(relationshipType);
	}

	/**
	 * Update RelationshipType
	 * @param RelationshipType to update
	 * @throws APIException
	 */
	public void updateRelationshipType(RelationshipType relationshipType) throws APIException {
		context.getDAOContext().getAdministrationDAO().updateRelationshipType(relationshipType);
	}

	/**
	 * Delete RelationshipType
	 * @param RelationshipType to delete
	 * @throws APIException
	 */
	public void deleteRelationshipType(RelationshipType relationshipType) throws APIException {
		context.getDAOContext().getAdministrationDAO().deleteRelationshipType(relationshipType);
	}

	/**
	 * Create a new Role
	 * @param Role to create
	 * @throws APIException
	 */
	public void createRole(Role role) throws APIException {
		context.getDAOContext().getAdministrationDAO().createRole(role);
	}

	/**
	 * Update Role
	 * @param Role to update
	 * @throws APIException
	 */
	public void updateRole(Role role) throws APIException {
		context.getDAOContext().getAdministrationDAO().updateRole(role);
	}

	/**
	 * Delete Role
	 * @param Role to delete
	 * @throws APIException
	 */
	public void deleteRole(Role role) throws APIException {
		context.getDAOContext().getAdministrationDAO().deleteRole(role);
	}

	/**
	 * Create a new Privilege
	 * @param Privilege to create
	 * @throws APIException
	 */
	public void createPrivilege(Privilege privilege) throws APIException {
		context.getDAOContext().getAdministrationDAO().createPrivilege(privilege);
	}

	/**
	 * Update Privilege
	 * @param Privilege to update
	 * @throws APIException
	 */
	public void updatePrivilege(Privilege privilege) throws APIException {
		context.getDAOContext().getAdministrationDAO().updatePrivilege(privilege);
	}

	/**
	 * Delete Privilege
	 * @param Privilege to delete
	 * @throws APIException
	 */
	public void deletePrivilege(Privilege privilege) throws APIException {
		context.getDAOContext().getAdministrationDAO().deletePrivilege(privilege);
	}

	/**
	 * Create a new ConceptClass
	 * @param ConceptClass to create
	 * @throws APIException
	 */
	public void createConceptClass(ConceptClass cc) throws APIException {
		context.getDAOContext().getAdministrationDAO().createConceptClass(cc);
	}

	/**
	 * Update ConceptClass
	 * @param ConceptClass to update
	 * @throws APIException
	 */
	public void updateConceptClass(ConceptClass cc) throws APIException {
		context.getDAOContext().getAdministrationDAO().updateConceptClass(cc);
	}

	/**
	 * Delete ConceptClass
	 * @param ConceptClass to delete
	 * @throws APIException
	 */
	public void deleteConceptClass(ConceptClass cc) throws APIException {
		context.getDAOContext().getAdministrationDAO().deleteConceptClass(cc);
	}

	/**
	 * Create a new ConceptDatatype
	 * @param ConceptDatatype to create
	 * @throws APIException
	 */
	public void createConceptDatatype(ConceptDatatype cd) throws APIException {
		context.getDAOContext().getAdministrationDAO().createConceptDatatype(cd);
	}

	/**
	 * Update ConceptDatatype
	 * @param ConceptDatatype to update
	 * @throws APIException
	 */
	public void updateConceptDatatype(ConceptDatatype cd) throws APIException {
		context.getDAOContext().getAdministrationDAO().updateConceptDatatype(cd);
	}

	/**
	 * Delete ConceptDatatype
	 * @param ConceptDatatype to delete
	 * @throws APIException
	 */
	public void deleteConceptDatatype(ConceptDatatype cd) throws APIException {
		context.getDAOContext().getAdministrationDAO().deleteConceptDatatype(cd);
	}
	
	/**
	 * Create a new Report
	 * @param Report to create
	 * @throws APIException
	 */
	public void createReport(Report report) throws APIException {
		context.getDAOContext().getAdministrationDAO().createReport(report);
	}

	/**
	 * Update Report
	 * @param Report to update
	 * @throws APIException
	 */
	public void updateReport(Report report) throws APIException {
		context.getDAOContext().getAdministrationDAO().updateReport(report);
	}

	/**
	 * Delete Report
	 * @param Report to delete
	 * @throws APIException
	 */
	public void deleteReport(Report report) throws APIException {
		context.getDAOContext().getAdministrationDAO().deleteReport(report);
	}
	
	/**
	 * Iterates over the words in names and synonyms (for each locale) and updates the concept word business table 
	 * @param concept
	 * @throws APIException
	 */
	public void updateConceptWord(Concept concept) throws APIException {
		context.getDAOContext().getAdministrationDAO().updateConceptWord(concept);
	}
	
	/**
	 * Iterates over all concepts calling updateConceptWord(concept)
	 * @throws APIException
	 */
	public void updateConceptWords() throws APIException {
		context.getDAOContext().getAdministrationDAO().updateConceptWords();
	}
	
	/**
	 * Updates the concept set derived business table for this concept (bursting the concept sets) 
	 * @param concept
	 * @throws APIException
	 */
	public void updateConceptSetDerived(Concept concept) throws APIException {
		context.getDAOContext().getAdministrationDAO().updateConceptSetDerived(concept);
	}
	
	/**
	 * Iterates over all concepts calling updateConceptSetDerived(concept)
	 * @throws APIException
	 */
	public void updateConceptSetDerived() throws APIException {
		context.getDAOContext().getAdministrationDAO().updateConceptSetDerived();
	}
	
	/**
	 * Create a new Group
	 * @param Group to create
	 * @throws APIException
	 */
	public void createGroup(Group group) throws APIException {
		context.getDAOContext().getAdministrationDAO().createGroup(group);
	}

	/**
	 * Update Group
	 * @param Group to update
	 * @throws APIException
	 */
	public void updateGroup(Group group) throws APIException {
		context.getDAOContext().getAdministrationDAO().updateGroup(group);
	}

	/**
	 * Delete Group
	 * @param Group to delete
	 * @throws APIException
	 */
	public void deleteGroup(Group group) throws APIException {
		context.getDAOContext().getAdministrationDAO().deleteGroup(group);
	}
}

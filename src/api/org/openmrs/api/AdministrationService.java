package org.openmrs.api;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptDatatype;
import org.openmrs.ConceptProposal;
import org.openmrs.ConceptSynonym;
import org.openmrs.EncounterType;
import org.openmrs.FieldType;
import org.openmrs.Group;
import org.openmrs.Location;
import org.openmrs.MimeType;
import org.openmrs.Obs;
import org.openmrs.OrderType;
import org.openmrs.PatientIdentifierType;
import org.openmrs.Privilege;
import org.openmrs.RelationshipType;
import org.openmrs.Role;
import org.openmrs.Tribe;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.AdministrationDAO;
import org.openmrs.api.db.DAOContext;
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
public class AdministrationService {
	
	protected Log log = LogFactory.getLog(getClass());

	private Context context;
	private DAOContext daoContext;
	
	public AdministrationService(Context c, DAOContext d) {
		this.context = c;
		this.daoContext = d;
	}
	
	private AdministrationDAO getAdminDAO() {
		return daoContext.getAdministrationDAO();
	}
	
	/**
	 * Create a new EncounterType
	 * @param EncounterType to create
	 * @throws APIException
	 */
	public void createEncounterType(EncounterType encounterType) throws APIException {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_ENCOUNTER_TYPES))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_MANAGE_ENCOUNTER_TYPES);

		getAdminDAO().createEncounterType(encounterType);
	}

	/**
	 * Update an encounter type
	 * @param EncounterType to update
	 * @throws APIException
	 */
	public void updateEncounterType(EncounterType encounterType) throws APIException {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_ENCOUNTER_TYPES))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_MANAGE_ENCOUNTER_TYPES);

		getAdminDAO().updateEncounterType(encounterType);
	}

	/**
	 * Delete an encounter type
	 * @param EncounterType to delete
	 * @throws APIException
	 */
	public void deleteEncounterType(EncounterType encounterType) throws APIException {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_ENCOUNTER_TYPES))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_MANAGE_ENCOUNTER_TYPES);

		getAdminDAO().deleteEncounterType(encounterType);
	}

	/**
	 * Create a new PatientIdentifierType
	 * @param PatientIdentifierType to create
	 * @throws APIException
	 */
	public void createPatientIdentifierType(PatientIdentifierType patientIdentifierType) throws APIException {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_IDENTIFIER_TYPES))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_MANAGE_IDENTIFIER_TYPES);

		getAdminDAO().createPatientIdentifierType(patientIdentifierType);
	}

	/**
	 * Update PatientIdentifierType
	 * @param PatientIdentifierType to update
	 * @throws APIException
	 */
	public void updatePatientIdentifierType(PatientIdentifierType patientIdentifierType) throws APIException {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_IDENTIFIER_TYPES))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_MANAGE_IDENTIFIER_TYPES);

		getAdminDAO().updatePatientIdentifierType(patientIdentifierType);
	}
	
	/**
	 * Delete PatientIdentifierType
	 * @param PatientIdentifierType to delete
	 * @throws APIException
	 */
	public void deletePatientIdentifierType(PatientIdentifierType patientIdentifierType) throws APIException {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_IDENTIFIER_TYPES))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_MANAGE_IDENTIFIER_TYPES);

		getAdminDAO().deletePatientIdentifierType(patientIdentifierType);
	}

	/**
	 * Create a new Tribe
	 * @param Tribe to create
	 * @throws APIException
	 */
	public void createTribe(Tribe tribe) throws APIException {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_TRIBES))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_MANAGE_TRIBES);

		getAdminDAO().createTribe(tribe);
	}

	/**
	 * Update Tribe
	 * @param Tribe to update
	 * @throws APIException
	 */
	public void updateTribe(Tribe tribe) throws APIException {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_TRIBES))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_MANAGE_TRIBES);

		getAdminDAO().updateTribe(tribe);
	}

	/**
	 * Delete Tribe
	 * @param Tribe to delete
	 * @throws APIException
	 */
	public void deleteTribe(Tribe tribe) throws APIException {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_TRIBES))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_MANAGE_TRIBES);

		getAdminDAO().deleteTribe(tribe);
	}
	
	/**
	 * Retire Tribe
	 * @param Tribe to retire
	 * @throws APIException
	 */
	public void retireTribe(Tribe tribe) throws APIException {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_TRIBES))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_MANAGE_TRIBES);

		getAdminDAO().retireTribe(tribe);
	}

	/**
	 * Unretire Tribe
	 * @param Tribe to unretire
	 * @throws APIException
	 */
	public void unretireTribe(Tribe tribe) throws APIException {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_TRIBES))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_MANAGE_TRIBES);

		getAdminDAO().unretireTribe(tribe);
	}
	
	/**
	 * Create a new OrderType
	 * @param OrderType to create
	 * @throws APIException
	 */
	public void createOrderType(OrderType orderType) throws APIException {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_ORDER_TYPES))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_MANAGE_ORDER_TYPES);

		getAdminDAO().createOrderType(orderType);
	}

	/**
	 * Update OrderType
	 * @param OrderType to update
	 * @throws APIException
	 */
	public void updateOrderType(OrderType orderType) throws APIException {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_ORDER_TYPES))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_MANAGE_ORDER_TYPES);

		getAdminDAO().updateOrderType(orderType);
	}

	/**
	 * Delete OrderType
	 * @param OrderType to delete
	 * @throws APIException
	 */
	public void deleteOrderType(OrderType orderType) throws APIException {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_ORDER_TYPES))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_MANAGE_ORDER_TYPES);

		getAdminDAO().deleteOrderType(orderType);
	}
	
	/**
	 * Create a new FieldType
	 * @param FieldType to create
	 * @throws APIException
	 */
	public void createFieldType(FieldType fieldType) throws APIException {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_FIELD_TYPES))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_MANAGE_FIELD_TYPES);

		getAdminDAO().createFieldType(fieldType);
	}

	/**
	 * Update FieldType
	 * @param FieldType to update
	 * @throws APIException
	 */
	public void updateFieldType(FieldType fieldType) throws APIException {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_FIELD_TYPES))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_MANAGE_FIELD_TYPES);

		getAdminDAO().updateFieldType(fieldType);
	}

	/**
	 * Delete FieldType
	 * @param FieldType to delete
	 * @throws APIException
	 */
	public void deleteFieldType(FieldType fieldType) throws APIException {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_FIELD_TYPES))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_MANAGE_FIELD_TYPES);

		getAdminDAO().deleteFieldType(fieldType);
	}
	
	/**
	 * Create a new MimeType
	 * @param MimeType to create
	 * @throws APIException
	 */
	public void createMimeType(MimeType mimeType) throws APIException {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_MIME_TYPES))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_MANAGE_MIME_TYPES);

		getAdminDAO().createMimeType(mimeType);
	}

	/**
	 * Update MimeType
	 * @param MimeType to update
	 * @throws APIException
	 */
	public void updateMimeType(MimeType mimeType) throws APIException {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_MIME_TYPES))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_MANAGE_MIME_TYPES);

		getAdminDAO().updateMimeType(mimeType);
	}

	/**
	 * Delete MimeType
	 * @param MimeType to delete
	 * @throws APIException
	 */
	public void deleteMimeType(MimeType mimeType) throws APIException {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_MIME_TYPES))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_MANAGE_MIME_TYPES);

		getAdminDAO().deleteMimeType(mimeType);
	}

	/**
	 * Create a new Location
	 * @param Location to create
	 * @throws APIException
	 */
	public void createLocation(Location location) throws APIException {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_LOCATIONS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_MANAGE_LOCATIONS);

		getAdminDAO().createLocation(location);
	}

	/**
	 * Update Location
	 * @param Location to update
	 * @throws APIException
	 */
	public void updateLocation(Location location) throws APIException {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_LOCATIONS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_MANAGE_LOCATIONS);

		getAdminDAO().updateLocation(location);
	}

	/**
	 * Delete Location
	 * @param Location to delete
	 * @throws APIException
	 */
	public void deleteLocation(Location location) throws APIException {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_LOCATIONS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_MANAGE_LOCATIONS);

		getAdminDAO().deleteLocation(location);
	}
	
	/**
	 * Create a new RelationshipType
	 * @param RelationshipType to create
	 * @throws APIException
	 */
	public void createRelationshipType(RelationshipType relationshipType) throws APIException {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_RELATIONSHIP_TYPES))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_MANAGE_RELATIONSHIP_TYPES);

		getAdminDAO().createRelationshipType(relationshipType);
	}

	/**
	 * Update RelationshipType
	 * @param RelationshipType to update
	 * @throws APIException
	 */
	public void updateRelationshipType(RelationshipType relationshipType) throws APIException {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_RELATIONSHIP_TYPES))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_MANAGE_RELATIONSHIP_TYPES);

		getAdminDAO().updateRelationshipType(relationshipType);
	}

	/**
	 * Delete RelationshipType
	 * @param RelationshipType to delete
	 * @throws APIException
	 */
	public void deleteRelationshipType(RelationshipType relationshipType) throws APIException {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_RELATIONSHIP_TYPES))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_MANAGE_RELATIONSHIP_TYPES);

		getAdminDAO().deleteRelationshipType(relationshipType);
	}

	/**
	 * Create a new Role
	 * @param Role to create
	 * @throws APIException
	 */
	public void createRole(Role role) throws APIException {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_ROLES))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_MANAGE_ROLES);

		getAdminDAO().createRole(role);
	}

	/**
	 * Update Role
	 * @param Role to update
	 * @throws APIException
	 */
	public void updateRole(Role role) throws APIException {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_ROLES))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_MANAGE_ROLES);

		getAdminDAO().updateRole(role);
	}

	/**
	 * Delete Role
	 * @param Role to delete
	 * @throws APIException
	 */
	public void deleteRole(Role role) throws APIException {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_ROLES))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_MANAGE_ROLES);

		if (OpenmrsConstants.CORE_ROLES().contains(role.getRole()))
			throw new APIException("Cannot delete a core role");
		getAdminDAO().deleteRole(role);
	}

	/**
	 * Create a new Privilege
	 * @param Privilege to create
	 * @throws APIException
	 */
	public void createPrivilege(Privilege privilege) throws APIException {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_PRIVILEGES))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_MANAGE_PRIVILEGES);

		getAdminDAO().createPrivilege(privilege);
	}

	/**
	 * Update Privilege
	 * @param Privilege to update
	 * @throws APIException
	 */
	public void updatePrivilege(Privilege privilege) throws APIException {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_PRIVILEGES))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_MANAGE_PRIVILEGES);

		getAdminDAO().updatePrivilege(privilege);
	}

	/**
	 * Delete Privilege
	 * @param Privilege to delete
	 * @throws APIException
	 */
	public void deletePrivilege(Privilege privilege) throws APIException {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_PRIVILEGES))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_MANAGE_PRIVILEGES);

		if (OpenmrsConstants.CORE_PRIVILEGES().contains(privilege.getPrivilege()))
			throw new APIException("Cannot delete a core privilege");
			getAdminDAO().deletePrivilege(privilege);
	}

	/**
	 * Create a new ConceptClass
	 * @param ConceptClass to create
	 * @throws APIException
	 */
	public void createConceptClass(ConceptClass cc) throws APIException {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_CONCEPT_CLASSES))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_MANAGE_CONCEPT_CLASSES);

		getAdminDAO().createConceptClass(cc);
	}

	/**
	 * Update ConceptClass
	 * @param ConceptClass to update
	 * @throws APIException
	 */
	public void updateConceptClass(ConceptClass cc) throws APIException {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_CONCEPT_CLASSES))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_MANAGE_CONCEPT_CLASSES);

		getAdminDAO().updateConceptClass(cc);
	}

	/**
	 * Delete ConceptClass
	 * @param ConceptClass to delete
	 * @throws APIException
	 */
	public void deleteConceptClass(ConceptClass cc) throws APIException {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_CONCEPT_CLASSES))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_MANAGE_CONCEPT_CLASSES);

		getAdminDAO().deleteConceptClass(cc);
	}

	/**
	 * Create a new ConceptDatatype
	 * @param ConceptDatatype to create
	 * @throws APIException
	 */
	public void createConceptDatatype(ConceptDatatype cd) throws APIException {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_CONCEPT_DATATYPES))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_MANAGE_CONCEPT_DATATYPES);

		getAdminDAO().createConceptDatatype(cd);
	}

	/**
	 * Update ConceptDatatype
	 * @param ConceptDatatype to update
	 * @throws APIException
	 */
	public void updateConceptDatatype(ConceptDatatype cd) throws APIException {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_CONCEPT_DATATYPES))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_MANAGE_CONCEPT_DATATYPES);

		getAdminDAO().updateConceptDatatype(cd);
	}

	/**
	 * Delete ConceptDatatype
	 * @param ConceptDatatype to delete
	 * @throws APIException
	 */
	public void deleteConceptDatatype(ConceptDatatype cd) throws APIException {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_CONCEPT_DATATYPES))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_MANAGE_CONCEPT_DATATYPES);

		getAdminDAO().deleteConceptDatatype(cd);
	}
	
	/**
	 * Create a new Report
	 * @param Report to create
	 * @throws APIException
	 */
	public void createReport(Report report) throws APIException {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_ADD_REPORTS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_ADD_REPORTS);

		getAdminDAO().createReport(report);
	}

	/**
	 * Update Report
	 * @param Report to update
	 * @throws APIException
	 */
	public void updateReport(Report report) throws APIException {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_EDIT_REPORTS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_EDIT_REPORTS);

		getAdminDAO().updateReport(report);
	}

	/**
	 * Delete Report
	 * @param Report to delete
	 * @throws APIException
	 */
	public void deleteReport(Report report) throws APIException {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_DELETE_REPORTS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_DELETE_REPORTS);

		getAdminDAO().deleteReport(report);
	}
	
	/**
	 * Iterates over the words in names and synonyms (for each locale) and updates the concept word business table 
	 * @param concept
	 * @throws APIException
	 */
	public void updateConceptWord(Concept concept) throws APIException {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_EDIT_CONCEPTS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_EDIT_CONCEPTS);

		getAdminDAO().updateConceptWord(concept);
	}
	
	/**
	 * Iterates over all concepts calling updateConceptWord(concept)
	 * @throws APIException
	 */
	public void updateConceptWords() throws APIException {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_EDIT_CONCEPTS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_EDIT_CONCEPTS);

		getAdminDAO().updateConceptWords();
	}
	
	/**
	 * Updates the concept set derived business table for this concept (bursting the concept sets) 
	 * @param concept
	 * @throws APIException
	 */
	public void updateConceptSetDerived(Concept concept) throws APIException {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_EDIT_CONCEPTS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_EDIT_CONCEPTS);

		getAdminDAO().updateConceptSetDerived(concept);
	}
	
	/**
	 * Iterates over all concepts calling updateConceptSetDerived(concept)
	 * @throws APIException
	 */
	public void updateConceptSetDerived() throws APIException {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_EDIT_CONCEPTS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_EDIT_CONCEPTS);

		getAdminDAO().updateConceptSetDerived();
	}
	
	/**
	 * Create a new Group
	 * @param Group to create
	 * @throws APIException
	 */
	public void createGroup(Group group) throws APIException {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_GROUPS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_MANAGE_GROUPS);

		getAdminDAO().createGroup(group);
	}

	/**
	 * Update Group
	 * @param Group to update
	 * @throws APIException
	 */
	public void updateGroup(Group group) throws APIException {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_GROUPS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_MANAGE_GROUPS);

		getAdminDAO().updateGroup(group);
	}

	/**
	 * Delete Group
	 * @param Group to delete
	 * @throws APIException
	 */
	public void deleteGroup(Group group) throws APIException {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_GROUPS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_MANAGE_GROUPS);

		getAdminDAO().deleteGroup(group);
	}
	
	public void renameGroup(Group group, String newGroupName) throws APIException {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_GROUPS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_MANAGE_GROUPS);

		getAdminDAO().renameGroup(group, newGroupName);
	}

	public void createConceptProposal(ConceptProposal cp) throws APIException {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_FORM_ENTRY))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_FORM_ENTRY);

		getAdminDAO().createConceptProposal(cp);
	}
	
	public void updateConceptProposal(ConceptProposal cp) throws APIException {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_FORM_ENTRY))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_FORM_ENTRY);

		cp.setChangedBy(context.getAuthenticatedUser());
		cp.setDateChanged(new Date());
		getAdminDAO().updateConceptProposal(cp);
	}
	
	public void mapConceptProposalToConcept(ConceptProposal cp, Concept mappedConcept) throws APIException {
		
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_ADD_CONCEPTS))
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
			ConceptSynonym syn = new ConceptSynonym(mappedConcept, cp.getFinalText(), context.getLocale());
			syn.setDateCreated(new Date());
			syn.setCreator(context.getAuthenticatedUser());
			
			mappedConcept.addSynonym(syn);
			updateConceptWord(mappedConcept);
		}
		
		cp.setMappedConcept(mappedConcept);
		
		if (cp.getObsConcept() != null) {
			Obs ob = new Obs();
			ob.setEncounter(cp.getEncounter());
			ob.setConcept(cp.getObsConcept());
			ob.setValueCoded(cp.getMappedConcept());
			ob.setCreator(context.getAuthenticatedUser());
			ob.setDateCreated(new Date());
			ob.setObsDatetime(cp.getEncounter().getEncounterDatetime());
			ob.setLocation(cp.getEncounter().getLocation());
			ob.setPatient(cp.getEncounter().getPatient());
			cp.setObs(ob);
		}
		
		updateConceptProposal(cp);
	}
	
	void rejectConceptProposal(ConceptProposal cp) {
		cp.setState(OpenmrsConstants.CONCEPT_PROPOSAL_REJECT);
		cp.setFinalText("");
		updateConceptProposal(cp);
	}
	
}

package org.openmrs.web.dwr;

import java.util.Collection;
import java.util.List;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Person;
import org.openmrs.Relationship;
import org.openmrs.RelationshipType;
import org.openmrs.api.context.Context;

public class DWRRelationshipService {

	protected final Log log = LogFactory.getLog(getClass());
	
	public Vector<ListItem> getRelationshipTypes() {
		Vector<ListItem> ret = new Vector<ListItem>();
		Collection<RelationshipType> relTypes = Context.getPatientService().getRelationshipTypes();
		for (RelationshipType relType : relTypes) {
			ListItem li = new ListItem();
			li.setId(relType.getRelationshipTypeId());
			li.setName(relType.getName());
			li.setDescription(relType.getDescription());
			ret.add(li);
		}
		return ret;
	}
	
	public Integer getRelationshipTypeId(String relationshipTypeName) {
		List<RelationshipType> relTypes = Context.getPatientService().getRelationshipTypes();
		for (RelationshipType relType : relTypes) {
			if (relType.getName().equals(relationshipTypeName)) {
				return relType.getRelationshipTypeId();
			}
		}
		return null;
	}

	/**
	 * fromPersonId and toPersonId can be either:
	 *     (Integer) personId
	 *     (String) "User." + userId
	 *     (String) "Patient." + patientId
	 */
	public void createRelationship(String fromPersonId, String toPersonId, Integer relationshipTypeId) {
		Person fromPerson = getPersonHelper(fromPersonId);
		Person toPerson = getPersonHelper(toPersonId);
		RelationshipType relType = Context.getPatientService().getRelationshipType(relationshipTypeId);
		Relationship rel = new Relationship();
		rel.setPerson(fromPerson);
		rel.setRelative(toPerson);
		rel.setRelationship(relType);
		Context.getAdministrationService().createRelationship(rel);
	}
	
	public void voidRelationship(Integer relationshipId) {
		Context.getAdministrationService().voidRelationship(Context.getPatientService().getRelationship(relationshipId));
	}
	
	/**
	 * This voids all existing relationships to toPersonId of the given type, and creates a new one 
	 * fromPersonId and toPersonId can be either:
	 *     (Integer) personId
	 *     (String) "User." + userId
	 *     (String) "Patient." + patientId
	 */
	public void setRelationshipTo(String fromPersonId, String toPersonId, String relationshipTypeName) {
		Person fromPerson = getPersonHelper(fromPersonId);
		Person toPerson = getPersonHelper(toPersonId);
		RelationshipType relType = Context.getPatientService().findRelationshipType(relationshipTypeName);
		
		if (relType == null) {
			throw new IllegalArgumentException("Couldn't find RelationshipType '" + relationshipTypeName + "'");
		}

		// void old ones
		List<Relationship> toVoid = Context.getPatientService().getRelationshipsTo(toPerson, relType);
		for (Relationship rel : toVoid) {
			if (!rel.getVoided()) {
				Context.getAdministrationService().voidRelationship(rel);					
			}
		}
		
		// add new one
		Relationship rel = new Relationship();
		rel.setPerson(fromPerson);
		rel.setRelative(toPerson);
		rel.setRelationship(relType);
		Context.getAdministrationService().createRelationship(rel);
	}
	
	public Vector<RelationshipListItem> getRelationships(Integer personId, Integer relationshipTypeId) {
		Vector<RelationshipListItem> ret = new Vector<RelationshipListItem>();
		List<Relationship> rels = Context.getPatientService().getRelationships(Context.getAdministrationService().getPerson(personId));
		for (Relationship rel : rels) {
			if (!rel.getVoided() && 
					(relationshipTypeId == null || rel.getRelationship().getRelationshipTypeId().equals(relationshipTypeId))) {
				ret.add(new RelationshipListItem(rel));
			}
		}
		return ret;
	}
		
	/**
	 * Relationships where personId is the person (not the relative).  
	 */
	public Vector<RelationshipListItem> getRelationshipsFromPerson(Integer personId, String relationshipTypeName) {
		Vector<RelationshipListItem> ret = new Vector<RelationshipListItem>();
		List<Relationship> rels = Context.getPatientService().getRelationships(Context.getAdministrationService().getPerson(personId));
		for (Relationship rel : rels) {
			if (!rel.getVoided() &&
					rel.getPerson().getPersonId().equals(personId) &&
					(relationshipTypeName == null || rel.getRelationship().getName().equals(relationshipTypeName))) {
				ret.add(new RelationshipListItem(rel));
			}
		}
		return ret;
	}
	
	/**
	 * Relationships where personId is the relative (not the person).  
	 */
	public Vector<RelationshipListItem> getRelationshipsToPerson(Integer personId, String relationshipTypeName) {
		Vector<RelationshipListItem> ret = new Vector<RelationshipListItem>();
		List<Relationship> rels = Context.getPatientService().getRelationships(Context.getAdministrationService().getPerson(personId));
		for (Relationship rel : rels) {
			if (!rel.getVoided() &&
					rel.getRelative().getPersonId().equals(personId) &&
					(relationshipTypeName == null || rel.getRelationship().getName().equals(relationshipTypeName))) {
				ret.add(new RelationshipListItem(rel));
			}
		}
		return ret;
	}
	
	
	private Person getPersonHelper(String s) {
		if (s.startsWith("User.")) {
			Integer userId = Integer.valueOf(s.substring(s.indexOf('.') + 1));
			return Context.getAdministrationService().getPerson(Context.getUserService().getUser(userId));			
		} else if (s.startsWith("Patient.")) {
			Integer patientId = Integer.valueOf(s.substring(s.indexOf('.') + 1));
			return Context.getAdministrationService().getPerson(Context.getPatientService().getPatient(patientId));
		} else {
			Integer personId = Integer.valueOf(s);
			return Context.getAdministrationService().getPerson(personId);
		}
	}

}

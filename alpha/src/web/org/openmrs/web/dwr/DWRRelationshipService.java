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
import org.openmrs.web.WebConstants;

import uk.ltd.getahead.dwr.WebContextFactory;

public class DWRRelationshipService {

	protected final Log log = LogFactory.getLog(getClass());
	
	public Vector<ListItem> getRelationshipTypes() {
		Context context = (Context) WebContextFactory.get().getSession()
			.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		if (context != null) {
			Vector<ListItem> ret = new Vector<ListItem>();
			Collection<RelationshipType> relTypes = context.getPatientService().getRelationshipTypes();
			for (RelationshipType relType : relTypes) {
				ListItem li = new ListItem();
				li.setId(relType.getRelationshipTypeId());
				li.setName(relType.getName());
				li.setDescription(relType.getDescription());
				ret.add(li);
			}
			return ret;
		} else {
			return null;
		}
	}
	
	public Integer getRelationshipTypeId(String relationshipTypeName) {
		Context context = (Context) WebContextFactory.get().getSession()
			.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		if (context != null) {
			List<RelationshipType> relTypes = context.getPatientService().getRelationshipTypes();
			for (RelationshipType relType : relTypes) {
				if (relType.getName().equals(relationshipTypeName)) {
					return relType.getRelationshipTypeId();
				}
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
		Context context = (Context) WebContextFactory.get().getSession()
			.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		if (context != null) {
			Person fromPerson = getPersonHelper(context, fromPersonId);
			Person toPerson = getPersonHelper(context, toPersonId);
			RelationshipType relType = context.getPatientService().getRelationshipType(relationshipTypeId);
			Relationship rel = new Relationship();
			rel.setPerson(fromPerson);
			rel.setRelative(toPerson);
			rel.setRelationship(relType);
			context.getAdministrationService().createRelationship(rel);
		}
	}
	
	public void voidRelationship(Integer relationshipId) {
		Context context = (Context) WebContextFactory.get().getSession()
			.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		if (context != null) {
			context.getAdministrationService().voidRelationship(context.getPatientService().getRelationship(relationshipId));
		}
	}
	
	/**
	 * This voids all existing relationships to toPersonId of the given type, and creates a new one 
	 * fromPersonId and toPersonId can be either:
	 *     (Integer) personId
	 *     (String) "User." + userId
	 *     (String) "Patient." + patientId
	 */
	public void setRelationshipTo(String fromPersonId, String toPersonId, String relationshipTypeName) {
		Context context = (Context) WebContextFactory.get().getSession()
			.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		if (context != null) {
			Person fromPerson = getPersonHelper(context, fromPersonId);
			Person toPerson = getPersonHelper(context, toPersonId);
			RelationshipType relType = context.getPatientService().findRelationshipType(relationshipTypeName);
			
			if (relType == null) {
				throw new IllegalArgumentException("Couldn't find RelationshipType '" + relationshipTypeName + "'");
			}

			// void old ones
			List<Relationship> toVoid = context.getPatientService().getRelationshipsTo(toPerson, relType);
			for (Relationship rel : toVoid) {
				if (!rel.getVoided()) {
					context.getAdministrationService().voidRelationship(rel);					
				}
			}
			
			// add new one
			Relationship rel = new Relationship();
			rel.setPerson(fromPerson);
			rel.setRelative(toPerson);
			rel.setRelationship(relType);
			context.getAdministrationService().createRelationship(rel);
		}
	}
	
	public Vector<RelationshipListItem> getRelationships(Integer personId, Integer relationshipTypeId) {
		Context context = (Context) WebContextFactory.get().getSession()
			.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		if (context != null) {
			Vector<RelationshipListItem> ret = new Vector<RelationshipListItem>();
			List<Relationship> rels = context.getPatientService().getRelationships(context.getAdministrationService().getPerson(personId));
			for (Relationship rel : rels) {
				if (!rel.getVoided() && 
						(relationshipTypeId == null || rel.getRelationship().getRelationshipTypeId().equals(relationshipTypeId))) {
					ret.add(new RelationshipListItem(rel));
				}
			}
			return ret;
		} else {
			return null;
		}
	}
		
	/**
	 * Relationships where personId is the person (not the relative).  
	 */
	public Vector<RelationshipListItem> getRelationshipsFromPerson(Integer personId, String relationshipTypeName) {
		Context context = (Context) WebContextFactory.get().getSession()
			.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		if (context != null) {
			Vector<RelationshipListItem> ret = new Vector<RelationshipListItem>();
			List<Relationship> rels = context.getPatientService().getRelationships(context.getAdministrationService().getPerson(personId));
			for (Relationship rel : rels) {
				if (!rel.getVoided() &&
						rel.getPerson().getPersonId().equals(personId) &&
						(relationshipTypeName == null || rel.getRelationship().getName().equals(relationshipTypeName))) {
					ret.add(new RelationshipListItem(rel));
				}
			}
			return ret;
		} else {
			return null;
		}
	}
	
	/**
	 * Relationships where personId is the relative (not the person).  
	 */
	public Vector<RelationshipListItem> getRelationshipsToPerson(Integer personId, String relationshipTypeName) {
		Context context = (Context) WebContextFactory.get().getSession()
			.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		if (context != null) {
			Vector<RelationshipListItem> ret = new Vector<RelationshipListItem>();
			List<Relationship> rels = context.getPatientService().getRelationships(context.getAdministrationService().getPerson(personId));
			for (Relationship rel : rels) {
				if (!rel.getVoided() &&
						rel.getRelative().getPersonId().equals(personId) &&
						(relationshipTypeName == null || rel.getRelationship().getName().equals(relationshipTypeName))) {
					ret.add(new RelationshipListItem(rel));
				}
			}
			return ret;
		} else {
			return null;
		}
	}
	
	
	private Person getPersonHelper(Context c, String s) {
		if (s.startsWith("User.")) {
			Integer userId = Integer.valueOf(s.substring(s.indexOf('.') + 1));
			return c.getAdministrationService().getPerson(c.getUserService().getUser(userId));			
		} else if (s.startsWith("Patient.")) {
			Integer patientId = Integer.valueOf(s.substring(s.indexOf('.') + 1));
			return c.getAdministrationService().getPerson(c.getPatientService().getPatient(patientId));
		} else {
			Integer personId = Integer.valueOf(s);
			return c.getAdministrationService().getPerson(personId);
		}
	}

}

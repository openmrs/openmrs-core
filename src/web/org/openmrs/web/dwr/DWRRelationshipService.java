package org.openmrs.web.dwr;

import java.util.List;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Person;
import org.openmrs.Relationship;
import org.openmrs.RelationshipType;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;

public class DWRRelationshipService {

	protected final Log log = LogFactory.getLog(getClass());

	public void createRelationship(Integer personAId, Integer personBId, Integer relationshipTypeId) {
		PersonService ps = Context.getPersonService();
		Person personA = ps.getPerson(personAId);
		Person personB = ps.getPerson(personBId);
		RelationshipType relType = Context.getPersonService().getRelationshipType(relationshipTypeId);
		Relationship rel = new Relationship();
		rel.setPersonA(personA);
		rel.setPersonB(personB);
		rel.setRelationshipType(relType);
		ps.createRelationship(rel);
	}
	
	public void voidRelationship(Integer relationshipId, String voidReason) {
		Context.getPersonService().voidRelationship(Context.getPersonService().getRelationship(relationshipId), voidReason);
	}
	
	public Vector<RelationshipListItem> getRelationships(Integer personId, Integer relationshipTypeId) {
		Vector<RelationshipListItem> ret = new Vector<RelationshipListItem>();
		List<Relationship> rels = Context.getPersonService().getRelationships(Context.getPersonService().getPerson(personId));
		for (Relationship rel : rels) {
			if (!rel.isVoided() && 
					(relationshipTypeId == null || rel.getRelationshipType().getRelationshipTypeId().equals(relationshipTypeId))) {
				ret.add(new RelationshipListItem(rel));
			}
		}
		return ret;
	}

}

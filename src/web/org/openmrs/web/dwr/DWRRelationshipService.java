package org.openmrs.web.dwr;

import java.util.Collection;
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

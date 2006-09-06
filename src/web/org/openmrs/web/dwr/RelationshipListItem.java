package org.openmrs.web.dwr;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.PatientName;
import org.openmrs.Person;
import org.openmrs.Relationship;
import org.openmrs.User;

public class RelationshipListItem {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	private Integer relationshipId;
	private String fromName;
	private String toName;
	private String relationshipType;
	private Integer fromUserId;
	private Integer fromPatientId;
	private Integer toUserId;
	private Integer toPatientId;

	public RelationshipListItem() { }
	
	public RelationshipListItem(Relationship r) {
		relationshipId = r.getRelationshipId();
		relationshipType = r.getRelationship().getName();
		Person p = r.getPerson();
		if (p.getPatient() != null) {
			Patient patient = p.getPatient();
			PatientName pn = patient.getPatientName();
			fromName = pn.toString();
			fromPatientId = patient.getPatientId();
		} else {
			User user = p.getUser();
			fromName = user.toString();
			fromUserId = user.getUserId();
		}
		p = r.getRelative();
		if (p.getPatient() != null) {
			Patient patient = p.getPatient();
			PatientName pn = patient.getPatientName();
			toName = pn.toString();
			toPatientId = patient.getPatientId();
		} else {
			User user = p.getUser();
			toName = user.toString();
			toUserId = user.getUserId();
		}
	}

	public String getFromName() {
		return fromName;
	}

	public void setFromName(String fromName) {
		this.fromName = fromName;
	}

	public Integer getFromPatientId() {
		return fromPatientId;
	}

	public void setFromPatientId(Integer fromPatientId) {
		this.fromPatientId = fromPatientId;
	}

	public Integer getFromUserId() {
		return fromUserId;
	}

	public void setFromUserId(Integer fromUserId) {
		this.fromUserId = fromUserId;
	}

	public Integer getRelationshipId() {
		return relationshipId;
	}

	public void setRelationshipId(Integer relationshipId) {
		this.relationshipId = relationshipId;
	}

	public String getRelationshipType() {
		return relationshipType;
	}

	public void setRelationshipType(String relationshipType) {
		this.relationshipType = relationshipType;
	}

	public String getToName() {
		return toName;
	}

	public void setToName(String toName) {
		this.toName = toName;
	}

	public Integer getToPatientId() {
		return toPatientId;
	}

	public void setToPatientId(Integer toPatientId) {
		this.toPatientId = toPatientId;
	}

	public Integer getToUserId() {
		return toUserId;
	}

	public void setToUserId(Integer toUserId) {
		this.toUserId = toUserId;
	}

}

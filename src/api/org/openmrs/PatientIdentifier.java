package org.openmrs;

import java.util.Date;

/**
 * PatientIdentifier
 */
public class PatientIdentifier implements java.io.Serializable {

	public static final long serialVersionUID = 1L;

	// Fields

	private Patient patient;
	private String identifier;
	private PatientIdentifierType identifierType;
	private Location location;
	private User creator;
	private Date dateCreated;
	private boolean voided;
	private User voidedBy;
	private Date dateVoided;
	private String voidReason;

	/** default constructor */
	public PatientIdentifier() {
	}

	public User getCreator() {
		return creator;
	}

	public void setCreator(User creator) {
		this.creator = creator;
	}

	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public PatientIdentifierType getIdentifierType() {
		return identifierType;
	}

	public void setIdentifierType(PatientIdentifierType identifierType) {
		this.identifierType = identifierType;
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public Patient getPatient() {
		return patient;
	}

	public void setPatient(Patient patient) {
		this.patient = patient;
	}

	public Date getDateVoided() {
		return dateVoided;
	}

	public void setDateVoided(Date dateVoided) {
		this.dateVoided = dateVoided;
	}

	public boolean isVoided() {
		return voided;
	}

	public void setVoided(boolean voided) {
		this.voided = voided;
	}

	public User getVoidedBy() {
		return voidedBy;
	}

	public void setVoidedBy(User voidedBy) {
		this.voidedBy = voidedBy;
	}

	public String getVoidReason() {
		return voidReason;
	}

	public void setVoidReason(String voidReason) {
		this.voidReason = voidReason;
	}

}
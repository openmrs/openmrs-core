package org.openmrs;

import java.util.Date;

/**
 * PatientIdentifier
 * 
 * @author Ben Wolfe
 * @version 1.0
 */
public class PatientIdentifier implements java.io.Serializable {

	public static final long serialVersionUID = 1123121L;

	// Fields

	private Patient patient;
	private String identifier;
	private PatientIdentifierType identifierType;
	private Location location;
	private User creator;
	private Date dateCreated;
	private Boolean preferred = false;
	private Boolean voided = false;
	private User voidedBy;
	private Date dateVoided;
	private String voidReason;

	/** default constructor */
	public PatientIdentifier() {
	}
	
	/**
	 * Constructor for creating a basic identifier
	 * @param identifier
	 * @param type
	 * @param location
	 */
	public PatientIdentifier(String identifier, PatientIdentifierType type, Location location) {
		this.identifier = identifier;
		this.identifierType = type;
		this.location = location;
	}

	/** 
	 * Compares two objects for similarity
	 * 
	 * @param obj
	 * @return boolean true/false whether or not they are the same objects
	 */
	public boolean equals(Object obj) {
		if (obj instanceof PatientIdentifier) {
			PatientIdentifier p = (PatientIdentifier)obj;
			boolean ret = true;
			if (patient != null && p.getPatient() != null)
				ret = ret && patient.equals(p.getPatient());
			if (identifier != null && p.getIdentifier() != null)
				ret = ret && identifier.equals(p.getIdentifier());
			if (identifierType != null && p.getIdentifierType() != null)
				ret = ret && identifierType.equals(p.getIdentifierType());
			// As of discussion on Feb-8th-2006, location is no longer part of the key for identifier
			//if (location != null && p.getLocation() != null)
			//	ret = ret && location.equals(p.getLocation());
			return ret;
			/*return (this.getPatient().equals(p.getPatient()) &&
					this.getIdentifier().equals(p.getIdentifier()) &&
					this.getIdentifierType().equals(p.getIdentifierType()) &&
					this.getLocation().equals(p.getLocation()));*/
		}
		return false;
	}
	
	public int hashCode() {
		if (this.getPatient() == null && this.getIdentifier() == null && this.getIdentifierType() == null && this.getLocation() == null) return super.hashCode();
		int hash = 5;
		if (getPatient() != null)
			hash += 31 * hash + this.getPatient().hashCode();
		if (getIdentifier() != null)
			hash += 31 * hash + this.getIdentifier().hashCode();
		if (getIdentifierType() != null)
			hash += 31 * hash + this.getIdentifierType().hashCode();
		//if (getLocation() != null)
		//	hash += 31 * hash + this.getLocation().hashCode();
		return hash;
	}

	//property accessors

	/**
	 * @return Returns the creator.
	 */
	public User getCreator() {
		return creator;
	}

	/**
	 * @param creator The creator to set.
	 */
	public void setCreator(User creator) {
		this.creator = creator;
	}

	/**
	 * @return Returns the dateCreated.
	 */
	public Date getDateCreated() {
		return dateCreated;
	}

	/**
	 * @param dateCreated The dateCreated to set.
	 */
	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	/**
	 * @return Returns the dateVoided.
	 */
	public Date getDateVoided() {
		return dateVoided;
	}

	/**
	 * @param dateVoided The dateVoided to set.
	 */
	public void setDateVoided(Date dateVoided) {
		this.dateVoided = dateVoided;
	}

	/**
	 * @return Returns the identifier.
	 */
	public String getIdentifier() {
		return identifier;
	}

	/**
	 * @param identifier The identifier to set.
	 */
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	/**
	 * @return Returns the identifierType.
	 */
	public PatientIdentifierType getIdentifierType() {
		return identifierType;
	}

	/**
	 * @param identifierType The identifierType to set.
	 */
	public void setIdentifierType(PatientIdentifierType identifierType) {
		this.identifierType = identifierType;
	}

	/**
	 * @return Returns the location.
	 */
	public Location getLocation() {
		return location;
	}

	/**
	 * @param location The location to set.
	 */
	public void setLocation(Location location) {
		this.location = location;
	}

	/**
	 * @return Returns the patient.
	 */
	public Patient getPatient() {
		return patient;
	}

	/**
	 * @param patient The patient to set.
	 */
	public void setPatient(Patient patient) {
		this.patient = patient;
	}

	/**
	 * @return Returns the voided.
	 */
	public Boolean isVoided() {
		return voided;
	}
	
	public Boolean getVoided() {
		return isVoided();
	}

	/**
	 * @param voided The voided to set.
	 */
	public void setVoided(Boolean voided) {
		this.voided = voided;
	}

	/**
	 * @return Returns the voidedBy.
	 */
	public User getVoidedBy() {
		return voidedBy;
	}

	/**
	 * @param voidedBy The voidedBy to set.
	 */
	public void setVoidedBy(User voidedBy) {
		this.voidedBy = voidedBy;
	}

	/**
	 * @return Returns the voidReason.
	 */
	public String getVoidReason() {
		return voidReason;
	}

	/**
	 * @param voidReason The voidReason to set.
	 */
	public void setVoidReason(String voidReason) {
		this.voidReason = voidReason;
	}
	
	public String toString() {
		return this.identifier;
	}

	/**
	 * @return Returns the preferred.
	 */
	public Boolean getPreferred() {
		return isPreferred();
	}

	/**
	 * @param preferred The preferred to set.
	 */
	public void setPreferred(Boolean preferred) {
		this.preferred = preferred;
	}
	
	/** 
	 * @return the preferred status
	 */
	public Boolean isPreferred() {
		return preferred;
	}
}
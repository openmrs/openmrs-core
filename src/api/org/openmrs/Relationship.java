package org.openmrs;

import java.util.Date;

/**
 * Relationship 
 */
public class Relationship implements java.io.Serializable {

	public static final long serialVersionUID = 323423L;

	// Fields

	private Integer relationshipId;
	private Person person;
	private RelationshipType relationship;
	private Person relative;
	private User creator;
	private Date dateCreated;
	private Boolean voided = false;
	private User voidedBy;
	private Date dateVoided;
	private String voidReason;

	// Constructors

	/** default constructor */
	public Relationship() {
	}

	/** constructor with id */
	public Relationship(Integer relationshipId) {
		this.relationshipId = relationshipId;
	}

	/** 
	 * Compares two objects for similarity
	 * 
	 * @param obj
	 * @return boolean true/false whether or not they are the same objects
	 */
	public boolean equals(Object obj) {
		if (obj instanceof Relationship) {
			Relationship m = (Relationship)obj;
			return (relationshipId.equals(m.getRelationshipId()));
		}
		return false;
	}
	
	public int hashCode() {
		if (this.getRelationshipId() == null) return super.hashCode();
		return this.getRelationshipId().hashCode();
	}

	// Property accessors

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
	 * @return Returns the person.
	 */
	public Person getPerson() {
		return person;
	}

	/**
	 * @param person The person to set.
	 */
	public void setPerson(Person person) {
		this.person = person;
	}

	/**
	 * @return Returns the relationship.
	 */
	public RelationshipType getRelationship() {
		return relationship;
	}

	/**
	 * @param relationship The relationship to set.
	 */
	public void setRelationship(RelationshipType relationship) {
		this.relationship = relationship;
	}

	/**
	 * @return Returns the relationshipId.
	 */
	public Integer getRelationshipId() {
		return relationshipId;
	}

	/**
	 * @param relationshipId The relationshipId to set.
	 */
	public void setRelationshipId(Integer relationshipId) {
		this.relationshipId = relationshipId;
	}

	/**
	 * @return Returns the relative.
	 */
	public Person getRelative() {
		return relative;
	}

	/**
	 * @param relative The relative to set.
	 */
	public void setRelative(Person relative) {
		this.relative = relative;
	}

	/**
	 * @return Returns the voided.
	 */
	public Boolean getVoided() {
		return voided;
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



}
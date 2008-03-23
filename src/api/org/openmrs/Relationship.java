/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs;

import java.util.Date;

/**
 * Relationship 
 */
public class Relationship implements java.io.Serializable {

	public static final long serialVersionUID = 323423L;

	// Fields

	private Integer relationshipId;
	private Person personA;
	private RelationshipType relationshipType;
	private Person personB;
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
	
	public Relationship(Person personA, Person personB, RelationshipType type) {
		this.personA = personA;
		this.personB = personB;
		this.relationshipType = type;
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
	 * @return Returns the personA.
	 */
	public Person getPersonA() {
		return personA;
	}

	/**
	 * @param person The person to set.
	 */
	public void setPersonA(Person personA) {
		this.personA = personA;
	}

	/**
	 * @return Returns the relationship type.
	 */
	public RelationshipType getRelationshipType() {
		return relationshipType;
	}

	/**
	 * @param relationship The relationship type to set.
	 */
	public void setRelationshipType(RelationshipType type) {
		this.relationshipType = type;
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
	 * @return Returns the personB.
	 */
	public Person getPersonB() {
		return personB;
	}

	/**
	 * @param personB The relative to set.
	 */
	public void setPersonB(Person personB) {
		this.personB = personB;
	}

	/**
	 * @deprecated Use isVoided()
	 * @see isVoided()
	 * @return Returns the voided.
	 */
	public Boolean getVoided() {
		return isVoided();
	}
	
	/**
	 * @return returns the voided status
	 */
	public Boolean isVoided() {
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

	public String toString() {
		String relType = getRelationshipType() == null ? "NULL" : getRelationshipType().getaIsToB();
		return personA + " is the " + relType + " of " + personB;  
	}

}
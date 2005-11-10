package org.openmrs;

import java.util.Date;

/**
 * RelationshipType 
 */
public class RelationshipType implements java.io.Serializable {

	public static final long serialVersionUID = 4223L;

	// Fields

	private Integer relationshipTypeId;
	private String name;
	private String description;
	private User creator;
	private Date dateCreated;

	// Constructors

	/** default constructor */
	public RelationshipType() {
	}

	/** constructor with id */
	public RelationshipType(Integer relationshipTypeId) {
		this.relationshipTypeId = relationshipTypeId;
	}

	/** 
	 * Compares two objects for similarity
	 * 
	 * @param obj
	 * @return boolean true/false whether or not they are the same objects
	 */
	public boolean equals(Object obj) {
		if (obj instanceof RelationshipType) {
			RelationshipType m = (RelationshipType)obj;
			return (relationshipTypeId.equals(m.getRelationshipTypeId()));
		}
		return false;
	}
	
	public int hashCode() {
		if (this.getRelationshipTypeId() == null) return super.hashCode();
		return this.getRelationshipTypeId().hashCode();
	}

	// Property accessors

	/**
	 * @return Returns the relationshipTypeId.
	 */
	public Integer getRelationshipTypeId() {
		return relationshipTypeId;
	}

	/**
	 * @param relationshipTypeId The relationshipTypeId to set.
	 */
	public void setRelationshipTypeId(Integer relationshipTypeId) {
		this.relationshipTypeId = relationshipTypeId;
	}

	/**
	 * @return Returns the description.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description The description to set.
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param relationshipType The name to set.
	 */
	public void setName(String name) {
		this.name = name;
	}

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

}
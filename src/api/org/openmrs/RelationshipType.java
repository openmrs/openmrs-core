package org.openmrs;

import java.util.Date;

/**
 * RelationshipType 
 */
public class RelationshipType implements java.io.Serializable {

	public static final long serialVersionUID = 4223L;

	// Fields

	private Integer relationshipTypeId;
	private String aIsToB;
	private String bIsToA;
	private String description;
	private Integer weight = 0;
	private Boolean preferred = false;
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
		if (relationshipTypeId != null && obj instanceof RelationshipType) {
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
	 * @return the weight
	 */
	public Integer getWeight() {
		return weight;
	}

	/**
	 * @param weight the weight to set
	 */
	public void setWeight(Integer weight) {
		this.weight = weight;
	}

	/**
	 * The java bean spec says that if an attribute has the second letter
	 * capitablized (as the "I" is), the initial "a" is not to be capitalized.
	 * Both Spring and Hibernate use this "getter" definition
	 *  
	 * @return the aIsToB
	 */
	public String getaIsToB() {
		return aIsToB;
	}

	/**
	 * @param isToB the aIsToB to set
	 */
	public void setaIsToB(String aisToB) {
		aIsToB = aisToB;
	}

	/**
	 * @return the bIsToA
	 */
	public String getbIsToA() {
		return bIsToA;
	}

	/**
	 * @deprecated use isPreferred()
	 * @return the preferred status
	 */
	private Boolean getPreferred() {
		return isPreferred();
	}
	
	/**
	 * "Preferred" relationship types are those that should be shown as
	 * default types when adding/editing a person's relationships
	 * 
	 * @return the preferred status
	 */
	public Boolean isPreferred() {
		return preferred;
	}

	/**
	 * "Preferred" relationship types are those that should be shown as
	 * default types when adding/editing a person's relationships
	 *
	 * @param preferred sets the preferred status of this relationship type
	 */
	public void setPreferred(Boolean preferred) {
		this.preferred = preferred;
	}

	/**
	 * @param isToA the bIsToA to set
	 */
	public void setbIsToA(String bisToA) {
		bIsToA = bisToA;
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
	
	public String toString() {
		return getaIsToB() + "/" + getbIsToA();
	}

}
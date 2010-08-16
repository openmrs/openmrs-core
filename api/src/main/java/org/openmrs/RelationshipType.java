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

/**
 * Defines a type of relationship between two people in the database. <br/>
 * <br/>
 * A relationship is two-way. There is a name for the relationship in both directions. <br/>
 * <br/>
 * For example: <br/>
 * a) physician Joe<br/>
 * b) patient Bob<br/>
 * Joe is the "physician of" Bob <u>and</u> Bob is the patient of Joe. Once you can establish one of
 * the two relationships, you automatically know the other. <br/>
 * <br/>
 * ALL relationships are two-way and can be defined as such. <br/>
 * <br/>
 * RelationshipTypes should be defined as <b>gender non-specific</b> For example: A mother and her
 * son. Instead of having a RelationshipType defined as mother-son, it should be defined as
 * Parent-child. (This avoids the duplicative types that would come out like father-son,
 * father-daughter, mother-daughter) <br/>
 * <br/>
 * In English, we run into a tricky RelationshipType with aunts and uncles. We have chosen to define
 * them as aunt/uncle-niece/nephew.
 */
public class RelationshipType extends BaseOpenmrsMetadata implements java.io.Serializable {
	
	public static final long serialVersionUID = 4223L;
	
	// Fields
	
	private Integer relationshipTypeId;
	
	private String aIsToB;
	
	private String bIsToA;
	
	private Integer weight = 0;
	
	private Boolean preferred = false;
	
	// Constructors
	
	/** default constructor */
	public RelationshipType() {
	}
	
	/** constructor with id */
	public RelationshipType(Integer relationshipTypeId) {
		this.relationshipTypeId = relationshipTypeId;
	}
	
	/**
	 * Compares two RelationshipTypes for similarity
	 * 
	 * @param obj RelationshipType to compare to this object
	 * @return boolean true/false whether or not they are the same objects
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (relationshipTypeId != null && obj instanceof RelationshipType) {
			RelationshipType m = (RelationshipType) obj;
			return (relationshipTypeId.equals(m.getRelationshipTypeId()));
		}
		return false;
	}
	
	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		if (this.getRelationshipTypeId() == null)
			return super.hashCode();
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
	 * The java bean specifications says that if an attribute has the second letter capitalized (as
	 * the "I" is), the initial "a" is not to be capitalized. Both Spring and Hibernate use this
	 * "getter" definition
	 * 
	 * @return the aIsToB
	 */
	public String getaIsToB() {
		return aIsToB;
	}
	
	/**
	 * @param aisToB the aIsToB to set
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
	 * @deprecated use isPreferred(). This method is kept around for Spring/Hibernate's use
	 * @return the preferred status
	 */
	@SuppressWarnings("unused")
	private Boolean getPreferred() {
		return isPreferred();
	}
	
	/**
	 * "Preferred" relationship types are those that should be shown as default types when
	 * adding/editing a person's relationships
	 * 
	 * @return the preferred status
	 */
	public Boolean isPreferred() {
		return preferred;
	}
	
	/**
	 * "Preferred" relationship types are those that should be shown as default types when
	 * adding/editing a person's relationships
	 * 
	 * @param preferred sets the preferred status of this relationship type
	 */
	public void setPreferred(Boolean preferred) {
		this.preferred = preferred;
	}
	
	/**
	 * @param bisToA the bIsToA to set
	 */
	public void setbIsToA(String bisToA) {
		bIsToA = bisToA;
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return getaIsToB() + "/" + getbIsToA();
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	public Integer getId() {
		return getRelationshipTypeId();
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	public void setId(Integer id) {
		setRelationshipTypeId(id);
		
	}
	
}

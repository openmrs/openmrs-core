/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs;

import org.codehaus.jackson.annotate.JsonIgnore;

/**
 * Defines a type of relationship between two people in the database. <br>
 * <br>
 * A relationship is two-way. There is a name for the relationship in both directions. <br>
 * <br>
 * For example: <br>
 * a) physician Joe<br>
 * b) patient Bob<br>
 * Joe is the "physician of" Bob <u>and</u> Bob is the patient of Joe. Once you can establish one of
 * the two relationships, you automatically know the other. <br>
 * <br>
 * ALL relationships are two-way and can be defined as such. <br>
 * <br>
 * RelationshipTypes should be defined as <b>gender non-specific</b> For example: A mother and her
 * son. Instead of having a RelationshipType defined as mother-son, it should be defined as
 * Parent-child. (This avoids the duplicative types that would come out like father-son,
 * father-daughter, mother-daughter) <br>
 * <br>
 * In English, we run into a tricky RelationshipType with aunts and uncles. We have chosen to define
 * them as aunt/uncle-niece/nephew.
 */
public class RelationshipType extends BaseChangeableOpenmrsMetadata{
	
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
	 * "Preferred" relationship types are those that should be shown as default types when
	 * adding/editing a person's relationships
	 * 
	 * @return the preferred status
	 * 
	 * @deprecated as of 2.0, use {@link #getPreferred()}
	 */
	@Deprecated
	@JsonIgnore
	public Boolean isPreferred() {
		return getPreferred();
	}
	
	public Boolean getPreferred() {
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
	@Override
	public String toString() {
		return getaIsToB() + "/" + getbIsToA();
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	@Override
	public Integer getId() {
		return getRelationshipTypeId();
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	@Override
	public void setId(Integer id) {
		setRelationshipTypeId(id);
		
	}
	
}

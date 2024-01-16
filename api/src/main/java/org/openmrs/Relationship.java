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

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Date;

/**
 * Relationship
 */
@Entity
@Table(name = "relationship")
public class Relationship extends BaseChangeableOpenmrsData {
	
	public static final long serialVersionUID = 323423L;
	
	// Fields
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "relationship_id_seq")
	@GenericGenerator(
		name = "relationship_id_seq",
		strategy = "native",
		parameters = @Parameter(name = "sequence", value = "relationship_relationship_id_seq")
	)
	@Column(name = "relationship_id")
	private Integer relationshipId;
	@ManyToOne(optional = false)
	@JoinColumn(name = "person_a", nullable = false)
	private Person personA;

	@ManyToOne(optional = false)
	@JoinColumn(name = "relationship", nullable = false)
	private RelationshipType relationshipType;

	@ManyToOne(optional = false)
	@JoinColumn(name = "person_b", nullable = false)
	private Person personB;

	@Column(name = "start_date",length = 19)
	private Date startDate;
	@Column(name = "end_date", length = 19)
	private Date endDate;
	
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
	 * Does a shallow copy of this Relationship. Does NOT copy relationshipId
	 * 
	 * @return a copy of this relationship
	 */
	public Relationship copy() {
		return copyHelper(new Relationship());
	}
	
	/**
	 * The purpose of this method is to allow subclasses of Relationship to delegate a portion of
	 * their copy() method back to the superclass, in case the base class implementation changes.
	 * 
	 * @param target a Relationship that will have the state of <code>this</code> copied into it
	 * @return the Relationship that was passed in, with state copied into it
	 */
	protected Relationship copyHelper(Relationship target) {
		target.personA = getPersonA();
		target.relationshipType = getRelationshipType();
		target.personB = getPersonB();
		target.setCreator(getCreator());
		target.setDateCreated(getDateCreated());
		target.setVoided(getVoided());
		target.setVoidedBy(getVoidedBy());
		target.setDateVoided(getDateVoided());
		target.setVoidReason(getVoidReason());
		return target;
	}
	
	// Property accessors
	
	/**
	 * @return Returns the personA.
	 */
	public Person getPersonA() {
		return personA;
	}
	
	/**
	 * @param personA The person to set.
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
	 * @param type The relationship type to set.
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
	 * If not null, this indicates that the relationship started on a particular date
	 * @since 1.9
	 * @return the relationship's start date.
	 */
	public Date getStartDate() {
		return this.startDate;
	}
	
	/**
	 * @since 1.9
	 * @param startDate relationship's start date.
	 */
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	
	/**
	 * If not null, this indicates that the relationship ended on a particular date
	 * @since 1.9
	 * @return Returns relationship's end date.
	 */
	public Date getEndDate() {
		return this.endDate;
	}
	
	/**
	 * @since 1.9
	 * @param endDate relationship's end date.
	 */
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
	
	@Override
	public String toString() {
		String relType = getRelationshipType() == null ? "NULL" : getRelationshipType().getaIsToB();
		return personA + " is the " + relType + " of " + personB;
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	@Override
	public Integer getId() {
		
		return getRelationshipId();
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	@Override
	public void setId(Integer id) {
		setRelationshipId(id);
		
	}
	
}

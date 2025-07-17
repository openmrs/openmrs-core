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

import org.hibernate.envers.Audited;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.DocumentId;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * A concept reference range defines the acceptable numeric values/ranges of a {@link ConceptNumeric} for specific factors
 * such as age, gender, e.t.c.
 * 
 * <p>
 * The criteria is used to evaluate if certain attributes of a patient meet a certain factor range. 
 * For example, if criteria is of factor age(say age between 1-5), then the ranges only apply for this age group. 
 * </p>
 *
 * @since 2.7.0
 */
@Audited
@Entity
@Table(name = "concept_reference_range")
public class ConceptReferenceRange extends BaseReferenceRange implements OpenmrsObject {
	
	private static final long serialVersionUID = 47329L;

	@DocumentId
	@Id
	@Column(name = "concept_reference_range_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer conceptReferenceRangeId;

	@Column(name = "criteria")
	private String criteria;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "concept_id", nullable = false)
	private ConceptNumeric conceptNumeric;
	
	public ConceptReferenceRange() {
	}

	/**
	 * Gets id of conceptReferenceRange
	 *
	 * @return Returns the ConceptReferenceRangeId.
	 */
	public Integer getConceptReferenceRangeId() {
		return conceptReferenceRangeId;
	}

	/**
	 * Sets conceptReferenceRangeId
	 *
	 * @param conceptReferenceRangeId The conceptReferenceRangeId to set.
	 */
	public void setConceptReferenceRangeId(Integer conceptReferenceRangeId) {
		this.conceptReferenceRangeId = conceptReferenceRangeId;
	}

	/**
	 * Gets the criteria of conceptReferenceRange
	 *
	 * @return criteria
	 */
	public String getCriteria() {
		return this.criteria;
	}

	/**
	 * Sets the criteria of conceptReferenceRange
	 *
	 * @param criteria the criteria to set
	 */
	public void setCriteria(String criteria) {
		this.criteria = criteria;
	}

	/**
	 * Gets conceptNumeric of conceptReferenceRange
	 *
	 * @return Returns the ConceptNumeric.
	 */
	public ConceptNumeric getConceptNumeric() {
		return conceptNumeric;
	}

	/**
	 * Sets conceptNumeric
	 *
	 * @param conceptNumeric concept to set.
	 */
	public void setConceptNumeric(ConceptNumeric conceptNumeric) {
		this.conceptNumeric = conceptNumeric;
	}

	/**
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	@Override
	public Integer getId() {
		return getConceptReferenceRangeId();
	}

	/**
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	@Override
	public void setId(Integer id) {
		setConceptReferenceRangeId(id);
	}
}

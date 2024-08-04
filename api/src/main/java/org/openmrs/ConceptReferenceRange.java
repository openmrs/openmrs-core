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

import org.hibernate.search.annotations.DocumentId;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * A concept reference range is typically a range of a concept for certain factor(s) e.g. age, gender e.t.c. 
 * 
 * <p>
 * The criteria is used to evaluate if certain attributes of a patient meet a certain factor range. 
 * For example, if criteria is of factor age(say age between 1-5), then the ranges only apply for this age group. 
 * </p>
 *
 * @since 2.7.0
 */
@Entity
@Table(name = "concept_reference_range")
public class ConceptReferenceRange extends BaseReferenceRange {
	
	private static final long serialVersionUID = 47329L;

	@DocumentId
	@Id
	@Column(name = "concept_reference_range_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer conceptReferenceRangeId;

	@Column(name = "criteria")
	private String criteria;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "concept_id", nullable = false)
	private Concept concept;
	
	public ConceptReferenceRange() {
	}

	// Setters and getters
	
	/**
	 * @return Returns the conceptRangeId.
	 */
	public Integer getConceptReferenceRangeId() {
		return conceptReferenceRangeId;
	}

	/**
	 * @param conceptReferenceRangeId The conceptReferenceRangeId to set.
	 */
	public void setConceptReferenceRangeId(Integer conceptReferenceRangeId) {
		this.conceptReferenceRangeId = conceptReferenceRangeId;
	}

	/**
	 * Returns the criteria of the conceptReferenceRange
	 *
	 * @return criteria the criteria
	 */
	public String getCriteria() {
		return this.criteria;
	}

	/**
	 * Sets the criteria of the conceptReferenceRange
	 *
	 * @param criteria the criteria to set
	 */
	public void setCriteria(String criteria) {
		this.criteria = criteria;
	}

	/**
	 * @return Returns the concept.
	 */
	public Concept getConcept() {
		return concept;
	}

	/**
	 * @param concept concept to set.
	 */
	public void setConcept(Concept concept) {
		this.concept = concept;
	}

	/**
	 * @see org.openmrs.OpenmrsObject#getId()
	 * @since 2.7.0
	 */
	@Override
	public Integer getId() {
		return getConceptReferenceRangeId();
	}

	/**
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 * @since 2.7.0
	 */
	@Override
	public void setId(Integer id) {
		setConceptReferenceRangeId(id);
	}
}

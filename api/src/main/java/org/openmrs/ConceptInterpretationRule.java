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

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.envers.Audited;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.DocumentId;
import org.hibernate.type.SqlTypes;

/**
 * A concept interpretation rule defines the {@link Obs.Interpretation} to apply to an observation
 * of a coded {@link Concept} when the rule's criteria are met.
 * <p>
 * This is the coded counterpart of {@link ConceptReferenceRange}. Interpretations of coded values
 * are driven purely by criteria rather than by numeric bounds, so this class does not extend
 * {@link BaseReferenceRange}; it shares only the criteria evaluation engine and the
 * {@link Obs.Interpretation} vocabulary.
 * </p>
 * <p>
 * The criteria is a SpEL expression evaluated against a patient, e.g.
 * <code>$patient.getAge() &lt; 5</code>. When several rules of a concept match, the one with the
 * lowest {@link #getPriority()} wins.
 * </p>
 *
 * @since 2.9.0
 */
@Audited
@Entity
@Table(name = "concept_interpretation_rule")
public class ConceptInterpretationRule extends BaseOpenmrsObject {

	private static final long serialVersionUID = 1L;

	@DocumentId
	@Id
	@Column(name = "concept_interpretation_rule_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer conceptInterpretationRuleId;

	@Column(name = "criteria", length = 65535)
	private String criteria;

	@Enumerated(EnumType.STRING)
	@JdbcTypeCode(SqlTypes.VARCHAR)
	@Column(name = "interpretation", length = 32)
	private Obs.Interpretation interpretation;

	@Column(name = "priority")
	private Integer priority;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "concept_id", nullable = false)
	private Concept concept;

	public ConceptInterpretationRule() {
	}

	/**
	 * Gets id of conceptInterpretationRule
	 *
	 * @return Returns the conceptInterpretationRuleId.
	 */
	public Integer getConceptInterpretationRuleId() {
		return conceptInterpretationRuleId;
	}

	/**
	 * Sets conceptInterpretationRuleId
	 *
	 * @param conceptInterpretationRuleId The conceptInterpretationRuleId to set.
	 */
	public void setConceptInterpretationRuleId(Integer conceptInterpretationRuleId) {
		this.conceptInterpretationRuleId = conceptInterpretationRuleId;
	}

	/**
	 * Gets the criteria of conceptInterpretationRule
	 *
	 * @return criteria the SpEL expression that has to evaluate to true for this rule to apply
	 */
	public String getCriteria() {
		return this.criteria;
	}

	/**
	 * Sets the criteria of conceptInterpretationRule
	 *
	 * @param criteria the criteria to set
	 */
	public void setCriteria(String criteria) {
		this.criteria = criteria;
	}

	/**
	 * Gets the interpretation applied when the criteria matches
	 *
	 * @return Returns the interpretation.
	 */
	public Obs.Interpretation getInterpretation() {
		return interpretation;
	}

	/**
	 * Sets the interpretation applied when the criteria matches
	 *
	 * @param interpretation the interpretation to set
	 */
	public void setInterpretation(Obs.Interpretation interpretation) {
		this.interpretation = interpretation;
	}

	/**
	 * Gets the priority used to resolve conflicts between rules of the same concept. The lower the
	 * value, the higher the precedence.
	 *
	 * @return Returns the priority.
	 */
	public Integer getPriority() {
		return priority;
	}

	/**
	 * Sets the priority used to resolve conflicts between rules of the same concept
	 *
	 * @param priority the priority to set
	 */
	public void setPriority(Integer priority) {
		this.priority = priority;
	}

	/**
	 * Gets concept of conceptInterpretationRule
	 *
	 * @return Returns the concept.
	 */
	public Concept getConcept() {
		return concept;
	}

	/**
	 * Sets concept
	 *
	 * @param concept concept to set.
	 */
	public void setConcept(Concept concept) {
		this.concept = concept;
	}

	/**
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	@Override
	public Integer getId() {
		return getConceptInterpretationRuleId();
	}

	/**
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	@Override
	public void setId(Integer id) {
		setConceptInterpretationRuleId(id);
	}
}

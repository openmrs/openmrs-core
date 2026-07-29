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
 * A concept interpretation rule defines the criteria used to determine the
 * {@link Obs.Interpretation} for a coded {@link Concept}.
 * <p>
 * Unlike {@link ConceptReferenceRange}, interpretation rules are evaluated using criteria
 * expressions against coded concept values. The criteria expression determines when a particular
 * interpretation should be applied.
 *
 * @since 3.0.0
 */
@Audited
@Entity
@Table(name = "concept_interpretation_rule")
public class ConceptInterpretationRule extends BaseOpenmrsObject {

	private static final long serialVersionUID = 47329L;

	@DocumentId
	@Id
	@Column(name = "concept_interpretation_rule_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer conceptInterpretationRuleId;

	@Column(name = "criteria", columnDefinition = "TEXT")
	private String criteria;

	@Enumerated(EnumType.STRING)
	@JdbcTypeCode(SqlTypes.VARCHAR)
	@Column(nullable = false)
	private Obs.Interpretation interpretation;

	@Column(name = "priority", nullable = false)
	private Integer priority;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "concept_id", nullable = false)
	private Concept concept;

	public ConceptInterpretationRule() {

	}

	/**
	 * Gets the conceptInterpretationRuleId.
	 *
	 * @return Returns the conceptInterpretationRuleId
	 */
	public Integer getConceptInterpretationRuleId() {
		return conceptInterpretationRuleId;
	}

	/**
	 * Sets the conceptInterpretationRuleId.
	 *
	 * @param conceptInterpretationRuleId the conceptInterpretationRuleId to set
	 */
	public void setConceptInterpretationRuleId(Integer conceptInterpretationRuleId) {
		this.conceptInterpretationRuleId = conceptInterpretationRuleId;
	}

	/**
	 * Gets the rule criteria.
	 *
	 * @return the criteria
	 */
	public String getCriteria() {
		return criteria;
	}

	/**
	 * Sets the rule criteria.
	 *
	 * @param criteria the criteria to set
	 */
	public void setCriteria(String criteria) {
		this.criteria = criteria;
	}

	/**
	 * Gets the interpretation.
	 *
	 * @return the interpretation
	 */
	public Obs.Interpretation getInterpretation() {
		return interpretation;
	}

	/**
	 * Sets the interpretation.
	 *
	 * @param interpretation the interpretation to set
	 */
	public void setInterpretation(Obs.Interpretation interpretation) {
		this.interpretation = interpretation;
	}

	/**
	 * Gets the rule priority.
	 *
	 * @return the priority
	 */
	public Integer getPriority() {
		return priority;
	}

	/**
	 * Sets the rule priority.
	 *
	 * @param priority the priority to set
	 */
	public void setPriority(Integer priority) {
		this.priority = priority;
	}

	/**
	 * Gets the rule's concept
	 *
	 * @return the concept
	 */
	public Concept getConcept() {
		return concept;
	}

	/**
	 * Sets the rule's concept
	 *
	 * @param concept the concept to set
	 */
	public void setConcept(Concept concept) {
		this.concept = concept;
	}

	/**
	 * @see org.openmrs.BaseOpenmrsObject#getId()
	 */
	@Override
	public Integer getId() {
		return getConceptInterpretationRuleId();
	}

	/**
	 * @see org.openmrs.BaseOpenmrsObject#setId(java.lang.Integer)
	 */
	@Override
	public void setId(Integer id) {
		setConceptInterpretationRuleId(id);
	}
}

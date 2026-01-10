/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.GenericGenerator
import org.hibernate.annotations.Parameter
import org.hibernate.envers.Audited

/**
 * ConceptStateConversion
 */
@Entity
@Table(name = "concept_state_conversion")
@Audited
class ConceptStateConversion() : BaseOpenmrsObject() {
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "concept_state_conversion_id_seq")
	@GenericGenerator(
		name = "concept_state_conversion_id_seq",
		strategy = "native",
		parameters = [Parameter(name = "sequence", value = "concept_state_conversion_concept_state_conversion_id_seq")]
	)
	@Column(name = "concept_state_conversion_id", nullable = false)
	var conceptStateConversionId: Int? = null

	@ManyToOne
	@JoinColumn(name = "concept_id", nullable = false)
	var concept: Concept? = null

	@ManyToOne
	@JoinColumn(name = "program_workflow_id", nullable = false)
	var programWorkflow: ProgramWorkflow? = null

	@ManyToOne
	@JoinColumn(name = "program_workflow_state_id", nullable = false)
	var programWorkflowState: ProgramWorkflowState? = null
	
	/**
	 * Constructor with id
	 */
	constructor(conceptStateConversionId: Int?) : this() {
		this.conceptStateConversionId = conceptStateConversionId
	}
	
	override fun toString(): String {
		return "ConceptStateConversion: Concept[$concept] results in State [$programWorkflowState] for workflow [$programWorkflow]"
	}
	
	override fun getId(): Int? = conceptStateConversionId
	
	override fun setId(id: Int?) {
		conceptStateConversionId = id
	}
	
	companion object {
		const val serialVersionUID = 3214511L
	}
}

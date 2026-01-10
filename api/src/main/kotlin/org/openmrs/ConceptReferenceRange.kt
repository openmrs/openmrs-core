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
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.envers.Audited
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.DocumentId

/**
 * A concept reference range defines the acceptable numeric values/ranges of a [ConceptNumeric] for specific factors
 * such as age, gender, e.t.c.
 * 
 * The criteria is used to evaluate if certain attributes of a patient meet a certain factor range. 
 * For example, if criteria is of factor age(say age between 1-5), then the ranges only apply for this age group.
 *
 * @since 2.7.0
 */
@Audited
@Entity
@Table(name = "concept_reference_range")
open class ConceptReferenceRange : BaseReferenceRange(), OpenmrsObject {
	
	@DocumentId
	@Id
	@Column(name = "concept_reference_range_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	var conceptReferenceRangeId: Int? = null

	@Column(name = "criteria", length = 65535)
	var criteria: String? = null

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "concept_id", nullable = false)
	var conceptNumeric: ConceptNumeric? = null

	override fun getId(): Int? = conceptReferenceRangeId

	override fun setId(id: Int?) {
		conceptReferenceRangeId = id
	}
	
	companion object {
		private const val serialVersionUID = 47329L
	}
}

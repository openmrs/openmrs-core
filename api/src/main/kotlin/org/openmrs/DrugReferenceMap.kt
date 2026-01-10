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
import org.hibernate.annotations.Cascade
import org.hibernate.annotations.CascadeType
import org.hibernate.annotations.GenericGenerator
import org.hibernate.annotations.Parameter
import org.hibernate.envers.Audited
import org.hibernate.search.mapper.pojo.automaticindexing.ReindexOnUpdate
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.DocumentId
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.IndexedEmbedded
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.IndexingDependency
import java.io.Serializable
import java.util.Date

/**
 * The DrugReferenceMap map object represents a mapping between a drug and alternative drug
 * terminologies.
 *
 * @since 1.10
 */
@Entity
@Table(name = "drug_reference_map")
@Audited
open class DrugReferenceMap : BaseOpenmrsObject(), Auditable, Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "drug_reference_map_id_seq")
	@GenericGenerator(
		name = "drug_reference_map_id_seq",
		strategy = "native",
		parameters = [Parameter(name = "sequence", value = "drug_reference_map_drug_reference_map_id_seq")]
	)
	@DocumentId
	@Column(name = "drug_reference_map_id")
	var drugReferenceMapId: Int? = null

	@ManyToOne
	@JoinColumn(name = "drug_id", nullable = false)
	var drug: Drug? = null

	@ManyToOne
	@JoinColumn(name = "term_id", nullable = false)
	@Cascade(CascadeType.SAVE_UPDATE)
	@IndexedEmbedded(includeEmbeddedObjectId = true)
	@IndexingDependency(reindexOnUpdate = ReindexOnUpdate.SHALLOW)
	var conceptReferenceTerm: ConceptReferenceTerm? = null

	@ManyToOne
	@JoinColumn(name = "concept_map_type", nullable = false)
	var conceptMapType: ConceptMapType? = null

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "creator", nullable = false)
	override var creator: User? = null

	@Column(name = "date_created", nullable = false, length = 19)
	override var dateCreated: Date? = null

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "changed_by")
	override var changedBy: User? = null

	@Column(name = "date_changed", length = 19)
	override var dateChanged: Date? = null

	constructor()

	constructor(term: ConceptReferenceTerm?, conceptMapType: ConceptMapType?) {
		this.conceptReferenceTerm = term
		this.conceptMapType = conceptMapType
	}

	override fun getId(): Int? = drugReferenceMapId

	override fun setId(id: Int?) {
		drugReferenceMapId = id
	}
	
	companion object {
		const val serialVersionUID = 1L
	}
}

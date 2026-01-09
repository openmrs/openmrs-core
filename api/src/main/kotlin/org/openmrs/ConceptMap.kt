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

import jakarta.persistence.AssociationOverride
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Cascade
import org.hibernate.annotations.CascadeType
import org.hibernate.envers.Audited
import org.hibernate.search.mapper.pojo.automaticindexing.ReindexOnUpdate
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.DocumentId
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.IndexedEmbedded
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.IndexingDependency

/**
 * The concept map object represents a mapping of Concept to ConceptSource. A concept can have 0 to
 * N mappings to any and all concept sources in the database.
 */
@Audited
@Entity
@Table(name = "concept_reference_map")
@AssociationOverride(
    name = "conceptMapType",
    joinColumns = [JoinColumn(name = "concept_map_type_id", nullable = false)]
)
class ConceptMap() : BaseConceptMap() {

    companion object {
        const val serialVersionUID: Long = 754677L
    }

    @DocumentId
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "concept_map_id")
    var conceptMapId: Int? = null

    @ManyToOne(optional = false)
    @JoinColumn(name = "concept_id", nullable = false)
    var concept: Concept? = null

    @IndexedEmbedded
    @IndexingDependency(reindexOnUpdate = ReindexOnUpdate.SHALLOW)
    @ManyToOne(optional = false)
    @Cascade(CascadeType.SAVE_UPDATE)
    @JoinColumn(name = "concept_reference_term_id", nullable = false)
    private var _conceptReferenceTerm: ConceptReferenceTerm? = null

    constructor(conceptMapId: Int?) : this() {
        this.conceptMapId = conceptMapId
    }

    /**
     * Convenience constructor that takes the term to be mapped to and the type of the map
     *
     * @param conceptReferenceTerm the concept reference term to map to
     * @param conceptMapType the concept map type for this concept reference term map
     */
    constructor(conceptReferenceTerm: ConceptReferenceTerm?, conceptMapType: ConceptMapType?) : this() {
        this._conceptReferenceTerm = conceptReferenceTerm
        this.conceptMapType = conceptMapType
    }

    /**
     * @return the conceptReferenceTerm
     * @since 1.9
     */
    var conceptReferenceTerm: ConceptReferenceTerm
        get() = _conceptReferenceTerm ?: ConceptReferenceTerm()
        set(value) { _conceptReferenceTerm = value }

    override var id: Integer?
        get() = conceptMapId?.let { Integer(it) }
        set(value) { conceptMapId = value?.toInt() }

    override fun toString(): String = conceptMapId?.toString() ?: ""
}

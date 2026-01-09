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
 * The concept Reference Term map object represents a mapping between two Concept Reference Terms. A
 * concept reference term can have 0 to N concept reference term mappings to any or all Concept
 * Reference Terms
 *
 * @since 1.9
 */
@Entity
@Table(name = "concept_reference_term_map")
@Audited
class ConceptReferenceTermMap() : BaseConceptMap() {

    companion object {
        private const val serialVersionUID: Long = 1L
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "concept_reference_term_map_id_seq")
    @GenericGenerator(
        name = "concept_reference_term_map_id_seq",
        parameters = [Parameter(name = "sequence", value = "concept_reference_term_map_concept_reference_term_map_id_seq")]
    )
    @Column(name = "concept_reference_term_map_id", nullable = false)
    var conceptReferenceTermMapId: Int? = null

    @ManyToOne(optional = false)
    @JoinColumn(name = "term_a_id", nullable = false)
    var termA: ConceptReferenceTerm? = null

    @ManyToOne(optional = false)
    @JoinColumn(name = "term_b_id", nullable = false)
    var termB: ConceptReferenceTerm? = null

    constructor(conceptReferenceTermMapId: Int?) : this() {
        this.conceptReferenceTermMapId = conceptReferenceTermMapId
    }

    /**
     * Convenience constructor that takes the term to be mapped to and the type of the map
     *
     * @param termB the other concept reference term to map to
     * @param conceptMapType the concept map type for this concept reference term map
     */
    constructor(termB: ConceptReferenceTerm?, conceptMapType: ConceptMapType?) : this() {
        this.termB = termB
        this.conceptMapType = conceptMapType
    }

    override var id: Integer?
        get() = conceptReferenceTermMapId?.let { Integer(it) }
        set(value) { conceptReferenceTermMapId = value?.toInt() }

    override fun equals(other: Any?): Boolean {
        if (other !is ConceptReferenceTermMap) {
            return false
        }
        if (conceptReferenceTermMapId != null && other.conceptReferenceTermMapId != null) {
            return conceptReferenceTermMapId == other.conceptReferenceTermMapId
        }
        return this === other
    }

    override fun hashCode(): Int {
        return conceptReferenceTermMapId?.let {
            var hash = 3
            hash += 31 * it
            hash
        } ?: super.hashCode()
    }

    override fun toString(): String = conceptReferenceTermMapId?.toString() ?: ""
}

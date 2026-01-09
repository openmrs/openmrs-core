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

import jakarta.persistence.AttributeOverride
import jakarta.persistence.AttributeOverrides
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.hibernate.annotations.GenericGenerator
import org.hibernate.annotations.Parameter
import org.hibernate.envers.Audited
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.DocumentId
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.GenericField
import java.util.LinkedHashSet

/**
 * A concept reference term is typically name for a concept by which it is referred in another
 * institution like ICD9, ICD10, SNOMED that keeps a concept dictionary or any other OpenMRS
 * implementation
 *
 * @since 1.9
 */
@Audited
@Entity
@Table(name = "concept_reference_term")
@AttributeOverrides(
    AttributeOverride(name = "name", column = Column(name = "name", nullable = true))
)
class ConceptReferenceTerm() : BaseChangeableOpenmrsMetadata() {

    companion object {
        private const val serialVersionUID: Long = 1L
    }

    @DocumentId
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "concept_reference_term_id_seq")
    @GenericGenerator(
        name = "concept_reference_term_id_seq",
        parameters = [Parameter(name = "sequence", value = "concept_reference_term_concept_reference_term_id_seq")]
    )
    @Column(name = "concept_reference_term_id")
    var conceptReferenceTermId: Int? = null

    @ManyToOne(optional = false)
    @JoinColumn(name = "concept_source_id", nullable = false)
    var conceptSource: ConceptSource? = null

    // The unique code used to identify the reference term in it's reference terminology
    @GenericField
    @Column(name = "code", nullable = false, length = 255)
    var code: String? = null

    @Column(name = "version", length = 50)
    var version: String? = null

    @OneToMany(mappedBy = "termA", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.EAGER)
    private var _conceptReferenceTermMaps: MutableSet<ConceptReferenceTermMap> = LinkedHashSet()

    constructor(conceptReferenceTermId: Int?) : this() {
        this.conceptReferenceTermId = conceptReferenceTermId
    }

    /**
     * Convenience constructor with the required fields filled in
     *
     * @param source the ConceptSource belongs in
     * @param code the code within that concept
     * @param name the user readable name of this term
     * @since 1.9.2, 1.10.0
     */
    constructor(source: ConceptSource?, code: String?, name: String?) : this() {
        this.conceptSource = source
        this.code = code
        this.name = name
    }

    var conceptReferenceTermMaps: MutableSet<ConceptReferenceTermMap>
        get() = _conceptReferenceTermMaps
        set(value) { _conceptReferenceTermMaps = value }

    override var id: Integer?
        get() = conceptReferenceTermId?.let { Integer(it) }
        set(value) { conceptReferenceTermId = value?.toInt() }

    /**
     * Add the given [ConceptReferenceTermMap] object to this concept reference term's list of
     * concept reference term maps. If there is already a corresponding ConceptReferenceTermMap
     * object for this concept reference term already, this one will not be added.
     *
     * @param conceptReferenceTermMap
     * <strong>Should</strong> not add a map where termB is itself
     * <strong>Should</strong> set termA as the term to which a mapping is being added
     * <strong>Should</strong> not add duplicate concept reference term maps
     */
    fun addConceptReferenceTermMap(conceptReferenceTermMap: ConceptReferenceTermMap?) {
        if (conceptReferenceTermMap != null && conceptReferenceTermMap.termB != null
            && this != conceptReferenceTermMap.termB
        ) {
            // can't map a term to itself
            conceptReferenceTermMap.termA = this
            if (!_conceptReferenceTermMaps.contains(conceptReferenceTermMap)) {
                _conceptReferenceTermMaps.add(conceptReferenceTermMap)
            }
        }
    }

    /**
     * Remove the given ConceptReferenceTermMap from the list of conceptReferenceTermMaps for this
     * [ConceptReferenceTerm]
     *
     * @param conceptReferenceTermMap
     * @return true if the entity was removed, false otherwise
     */
    fun removeConceptReferenceTermMap(conceptReferenceTermMap: ConceptReferenceTermMap?): Boolean =
        _conceptReferenceTermMaps.remove(conceptReferenceTermMap)

    override fun toString(): String =
        when {
            code != null && name != null -> "$name($code)"
            code == null -> name ?: ""
            else -> code ?: ""
        }
}

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
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.GenericGenerator
import org.hibernate.annotations.Parameter
import org.hibernate.envers.Audited

/**
 * A concept source is defined as any institution that keeps a concept dictionary. Examples are
 * ICD9, ICD10, SNOMED, or any other OpenMRS implementation
 */
@Entity
@Table(name = "concept_reference_source")
@AttributeOverrides(
    AttributeOverride(name = "name", column = Column(name = "name", nullable = false, length = 50)),
    AttributeOverride(name = "description", column = Column(name = "description", nullable = false, length = 1024))
)
@Audited
class ConceptSource() : BaseChangeableOpenmrsMetadata() {

    companion object {
        const val serialVersionUID: Long = 375L
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "concept_source_id_seq")
    @GenericGenerator(
        name = "concept_source_id_seq",
        strategy = "native",
        parameters = [Parameter(name = "sequence", value = "concept_reference_source_concept_source_id_seq")]
    )
    @Column(name = "concept_source_id", nullable = false)
    var conceptSourceId: Int? = null

    @Column(name = "hl7_code", length = 50)
    var hl7Code: String? = null

    @Column(name = "unique_id", length = 250, unique = true)
    var uniqueId: String? = null

    constructor(conceptSourceId: Int?) : this() {
        this.conceptSourceId = conceptSourceId
    }

    override var id: Integer?
        get() = conceptSourceId?.let { Integer(it) }
        set(value) { conceptSourceId = value?.toInt() }
}

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
import jakarta.persistence.Table
import org.hibernate.envers.Audited
import org.openmrs.attribute.Attribute
import org.openmrs.attribute.BaseAttribute

@Audited
@Entity
@Table(name = "concept_attribute")
@AssociationOverride(
    name = "owner",
    joinColumns = [JoinColumn(name = "concept_id", nullable = false)]
)
class ConceptAttribute : BaseAttribute<ConceptAttributeType, Concept>(), Attribute<ConceptAttributeType, Concept> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "concept_attribute_id")
    var conceptAttributeId: Int? = null

    var concept: Concept?
        get() = owner
        set(value) { owner = value }

    override var id: Integer?
        get() = conceptAttributeId?.let { Integer(it) }
        set(value) { conceptAttributeId = value?.toInt() }
}

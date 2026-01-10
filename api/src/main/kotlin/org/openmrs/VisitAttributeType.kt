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

import jakarta.persistence.*
import org.hibernate.envers.Audited
import org.openmrs.attribute.AttributeType
import org.openmrs.attribute.BaseAttributeType

/**
 * A user-defined extension to the [Visit] class.
 * @see AttributeType
 */
@Entity
@Table(name = "visit_attribute_type")
@Audited
@AttributeOverrides(
    AttributeOverride(name = "description", column = Column(name = "description", length = 1024))
)
class VisitAttributeType : BaseAttributeType<Visit>(), AttributeType<Visit> {

    @Id
    @Column(name = "visit_attribute_type_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var visitAttributeTypeId: Int? = null

    /**
     * @see org.openmrs.OpenmrsObject.getId
     */
    override var id: Int?
        get() = visitAttributeTypeId
        set(value) {
            visitAttributeTypeId = value
        }
}

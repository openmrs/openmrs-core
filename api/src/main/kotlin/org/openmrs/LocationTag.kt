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
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.envers.Audited

/**
 * An LocationTag allows categorization of [Location]s
 *
 * @see Location
 * @since 1.5
 */
@Audited
@Entity
@Table(name = "location_tag")
@AttributeOverride(name = "name", column = Column(name = "name", nullable = false, length = 50))
class LocationTag() : BaseChangeableOpenmrsMetadata() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "location_tag_id", nullable = false)
    var locationTagId: Int? = null

    constructor(locationTagId: Int?) : this() {
        this.locationTagId = locationTagId
    }

    /**
     * Required values constructor. This is the minimum number of values that must be non-null in
     * order to have a successful save to the database
     *
     * @param name the name of this encounter type
     * @param description a short description of why this encounter type exists
     */
    constructor(name: String?, description: String?) : this() {
        this.name = name
        this.description = description
    }

    override fun toString(): String = name ?: ""

    override fun getId(): Int? = locationTagId

    override fun setId(id: Int?) {
        locationTagId = id
    }

    companion object {
        private const val serialVersionUID = 7654L
    }
}

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

/**
 * A value for a user-defined [LocationAttributeType] that is stored on a [Location].
 * @see Attribute
 * @since 1.9
 */
@Audited
@Entity
@Table(name = "location_attribute")
@AssociationOverride(
    name = "owner",
    joinColumns = [JoinColumn(name = "location_id", nullable = false)]
)
class LocationAttribute : BaseAttribute<LocationAttributeType, Location>(), Attribute<LocationAttributeType, Location> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "location_attribute_id")
    var locationAttributeId: Int? = null

    // BaseAttribute<Location> has an "owner" property of type Location, which we re-expose as "location"
    var location: Location?
        get() = owner
        set(value) { owner = value }

    override fun getId(): Int? = locationAttributeId

    override fun setId(id: Int?) {
        locationAttributeId = id
    }
}

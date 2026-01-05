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
import jakarta.persistence.MappedSuperclass
import org.apache.commons.lang3.builder.ToStringBuilder
import org.apache.commons.lang3.builder.ToStringStyle
import org.hibernate.Hibernate
import org.hibernate.envers.Audited
import java.io.Serializable
import java.util.UUID

/**
 * This is the base implementation of the [OpenmrsObject] interface.
 * It implements the uuid variable that all objects are expected to have.
 */
@MappedSuperclass
@Audited
abstract class BaseOpenmrsObject : Serializable, OpenmrsObject {

    @Column(name = "uuid", unique = true, nullable = false, length = 38, updatable = false)
    override var uuid: String = UUID.randomUUID().toString()

    /**
     * Returns a hash code based on the `uuid` field.
     *
     * If the `uuid` field is `null`, it delegates to [Object.hashCode].
     *
     * @see Object.hashCode
     */
    override fun hashCode(): Int = uuid.hashCode()

    /**
     * Returns `true` if and only if `x` and `y` refer to the same
     * object (`x === y` has the value `true`) or both have the same
     * `uuid` (`((x.uuid != null) && x.uuid == y.uuid)` has the value `true`).
     *
     * @see Object.equals
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BaseOpenmrsObject) return false

        // Need to call uuid getter to make sure hibernate proxy objects return the correct uuid.
        // The private member may not be set for a hibernate proxy.
        val thisUuid = this.uuid
        val otherUuid = other.uuid

        // In case of hibernate proxy objects we need to get real classes
        val thisClass = Hibernate.getClass(this)
        val otherClass = Hibernate.getClass(other)

        if (!(thisClass.isAssignableFrom(otherClass) || otherClass.isAssignableFrom(thisClass))) {
            return false
        }

        return thisUuid == otherUuid
    }

    /**
     * Returns a string equal to the value of: `ClassName{hashCode=..., uuid=...}`
     */
    override fun toString(): String =
        ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
            .append("hashCode", Integer.toHexString(hashCode()))
            .append("uuid", uuid)
            .build()
}

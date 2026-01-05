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

import org.hibernate.envers.Audited
import org.slf4j.LoggerFactory

/**
 * Represents a person who may provide care to a patient during an encounter
 *
 * @since 1.9
 */
@Audited
class Provider() : BaseCustomizableMetadata<ProviderAttribute>() {

    companion object {
        private val log = LoggerFactory.getLogger(Provider::class.java)
    }

    var providerId: Int? = null

    var person: Person? = null

    var identifier: String? = null

    /** @since 2.2 */
    var role: Concept? = null

    /** @since 2.2 */
    var speciality: Concept? = null

    /** @since 2.8.0 */
    var providerRole: ProviderRole? = null

    constructor(providerId: Int?) : this() {
        this.providerId = providerId
    }

    override var id: Integer?
        get() = providerId?.let { Integer(it) }
        set(value) { providerId = value?.toInt() }

    override fun toString(): String {
        val providerName = person?.names?.toString() ?: ""
        return "[Provider: providerId:$providerId providerName:$providerName ]"
    }

    override var name: String?
        get() = person?.personName?.fullName.also {
            if (person != null && it == null) {
                log.warn("We no longer support providers who are not linked to person. Set the name on the linked person")
            }
        }
        set(value) { super.name = value }
}

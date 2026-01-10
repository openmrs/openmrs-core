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

/**
 * The frequency at which an Order's action should be repeated, e.g. TWICE DAILY or EVERY 6 HOURS.
 * This class is backed by a Concept for i18n, synonyms, mappings, etc, but it contains additional
 * details an electronic system can use to understand its meaning.
 * 
 * @since 1.10
 */
@Audited
class OrderFrequency : BaseChangeableOpenmrsMetadata() {

    var orderFrequencyId: Int? = null

    var frequencyPerDay: Double? = null

    override var uuid: String? = null

    var concept: Concept? = null

    /**
     * @see org.openmrs.OpenmrsObject.getId
     */
    override var id: Int?
        get() = orderFrequencyId
        set(value) {
            orderFrequencyId = value
        }

    /**
     * @see BaseOpenmrsMetadata.getDescription
     */
    override val name: String?
        get() = concept?.name?.toString()

    /**
     * @see org.openmrs.BaseOpenmrsMetadata.getName
     */
    override val description: String?
        get() = concept?.description?.description

    override fun toString(): String = name ?: super.toString()

    companion object {
        private const val serialVersionUID = 1L
    }
}

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
import org.openmrs.attribute.Attribute
import org.openmrs.attribute.BaseAttribute

/**
 * A value for a user-defined [OrderGroupAttributeType] that is stored in an [OrderGroup].
 * @see Attribute
 * @since 2.4.0
 */
@Audited
class OrderGroupAttribute : BaseAttribute<OrderGroupAttributeType, OrderGroup>(), Attribute<OrderGroupAttributeType, OrderGroup> {

    var orderGroupAttributeId: Int? = null

    /**
     * @return the order group
     */
    var orderGroup: OrderGroup?
        get() = owner
        set(value) {
            owner = value
        }

    /**
     * @see org.openmrs.OpenmrsObject.getId
     */
    override var id: Int?
        get() = orderGroupAttributeId
        set(value) {
            orderGroupAttributeId = value
        }
}

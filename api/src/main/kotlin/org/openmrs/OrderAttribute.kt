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
 * The OrderAttribute, value for the [OrderAttributeType] that is stored in an [Order].
 * @see Attribute
 * @since 2.5.0
 */
@Audited
class OrderAttribute : BaseAttribute<OrderAttributeType, Order>(), Attribute<OrderAttributeType, Order> {

    var orderAttributeId: Int? = null

    /**
     * @return the order
     */
    var order: Order?
        get() = owner
        set(value) {
            owner = value
        }

    /**
     * @see org.openmrs.OpenmrsObject.getId
     */
    override var id: Int?
        get() = orderAttributeId
        set(value) {
            orderAttributeId = value
        }
}

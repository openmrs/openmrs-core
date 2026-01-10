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
import org.openmrs.attribute.AttributeType
import org.openmrs.attribute.BaseAttributeType

/**
 * A user-defined extension to the [OrderGroup] class.
 * @see AttributeType
 * @since 2.4.0
 */
@Audited
class OrderGroupAttributeType : BaseAttributeType<OrderGroup>(), AttributeType<OrderGroup> {

    var orderGroupAttributeTypeId: Int? = null

    /**
     * @see org.openmrs.OpenmrsObject.getId
     */
    override var id: Int?
        get() = orderGroupAttributeTypeId
        set(value) {
            orderGroupAttributeTypeId = value
        }
}

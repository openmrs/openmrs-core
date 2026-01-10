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
 * This is a type of order that adds tests specific attributes like: laterality, clinical history,
 * etc.
 * 
 * @since 1.9.2, 1.10
 */
@Audited
class TestOrder : ServiceOrder() {

    /**
     * @see org.openmrs.ServiceOrder.copy
     */
    override fun copy(): TestOrder {
        val newOrder = TestOrder()
        copyHelper(newOrder)
        return newOrder
    }

    /**
     * Creates a discontinuation order for this.
     * 
     * @see org.openmrs.ServiceOrder.cloneForDiscontinuing
     * @return the newly created order
     */
    override fun cloneForDiscontinuing(): TestOrder {
        val newOrder = TestOrder()
        cloneForDiscontinuingHelper(newOrder)
        return newOrder
    }

    /**
     * Creates a TestOrder for revision from this order, sets the previousOrder, action field and
     * other test order fields.
     * 
     * @return the newly created order
     */
    override fun cloneForRevision(): TestOrder {
        val newOrder = TestOrder()
        cloneForRevisionHelper(newOrder)
        return newOrder
    }

    companion object {
        const val serialVersionUID = 1L
    }
}

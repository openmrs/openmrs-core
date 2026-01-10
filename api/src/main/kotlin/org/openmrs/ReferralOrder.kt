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
 * This is a type of order that adds referral specific attributes.
 * 
 * @since 2.5.0
 */
@Audited
class ReferralOrder : ServiceOrder() {
	
	override fun copy(): ReferralOrder {
		val newOrder = ReferralOrder()
		copyHelper(newOrder)
		return newOrder
	}
	
	/**
	 * Creates a discontinuation order for this.
	 * 
	 * @return the newly created order
	 */
	override fun cloneForDiscontinuing(): ReferralOrder {
		val newOrder = ReferralOrder()
		cloneForDiscontinuingHelper(newOrder)
		return newOrder
	}
	
	/**
	 * Creates a ReferralOrder for revision from this order, sets the previousOrder, action field and
	 * other test order fields.
	 * 
	 * @return the newly created order
	 */
	override fun cloneForRevision(): ReferralOrder {
		val newOrder = ReferralOrder()
		cloneForRevisionHelper(newOrder)
		return newOrder
	}
	
	companion object {
		private const val serialVersionUID = 1L
	}
}

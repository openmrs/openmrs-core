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
 * A value for a user-defined [OrderSetAttributeType] that is stored on a [OrderSet].
 * @see Attribute
 * @since 2.4.0
 */
@Audited
open class OrderSetAttribute : BaseAttribute<OrderSetAttributeType, OrderSet>(), Attribute<OrderSetAttributeType, OrderSet> {
	
	var orderSetAttributeId: Int? = null

	/**
	 * @return the orderSet
	 */
	var orderSet: OrderSet?
		get() = owner
		set(value) { owner = value }

	override fun getId(): Int? = orderSetAttributeId

	override fun setId(id: Int?) {
		orderSetAttributeId = id
	}
	
	companion object {
		private const val serialVersionUID = 1L
	}
}

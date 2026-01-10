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
import org.openmrs.api.APIException

/**
 * Represents the grouping of orders into a set,
 * so as to give decision support for the doctors
 * 
 * @since 1.12
 */
@Audited
open class OrderSet : BaseCustomizableMetadata<OrderSetAttribute>() {
	
	/**
	 * Restrictions put on saving an orderSet.
	 * ALL: All the members of the orderSet need to be selected for saving
	 * ONE: Only one of the member of the orderSet needs to be selected for saving
	 * ANY: Any of the members of the orderSet can be selected for saving
	 */
	enum class Operator {
		ALL, ONE, ANY
	}
	
	var orderSetId: Int? = null
	
	var operator: Operator? = null
	
	var orderSetMembers: MutableList<OrderSetMember>? = null
		get() {
			if (field == null) {
				field = mutableListOf()
			}
			return field
		}
	
	/**
	 * @since 2.3.0
	 */
	var category: Concept? = null

	/**
	 * Adds an orderSetMember to the existing list of orderSetMembers
	 * 
	 * @param orderSetMember the new orderSetMember to be added
	 * @param position the position where it is to be added, if position is null it adds to the last position 
	 */
	fun addOrderSetMember(orderSetMember: OrderSetMember, position: Int? = null) {
		val listIndex = findListIndexForGivenPosition(position)
		orderSetMember.orderSet = this
		orderSetMembers!!.add(listIndex, orderSetMember)
	}
	
	private fun findListIndexForGivenPosition(position: Int?): Int {
		val size = orderSetMembers!!.size
		return when {
			position == null -> size
			position < 0 && position >= (-1 - size) -> position + size + 1
			position > size -> throw APIException("Cannot add a member which is out of range of the list")
			else -> position
		}
	}
	
	override fun getId(): Int? = orderSetId
	
	override fun setId(id: Int?) {
		orderSetId = id
	}

	/**
	 * Fetches the list of orderSetMembers that are not retired
	 *
	 * @return the orderSetMembers that are not retired
	 */
	fun getUnRetiredOrderSetMembers(): List<OrderSetMember> =
		orderSetMembers?.filter { !it.retired } ?: emptyList()

	/**
	 * Removes and orderSetMember from a list of existing orderSetMembers
	 *
	 * @param orderSetMember that is to be removed
	 */
	fun removeOrderSetMember(orderSetMember: OrderSetMember) {
		if (orderSetMembers?.contains(orderSetMember) == true) {
			orderSetMembers!!.remove(orderSetMember)
			orderSetMember.orderSet = null
		}
	}

	/**
	 * Retires an orderSetMember
	 *
	 * @param orderSetMember to be retired
	 */
	fun retireOrderSetMember(orderSetMember: OrderSetMember) {
		orderSetMember.retired = true
	}
	
	companion object {
		const val serialVersionUID = 72232L
	}
}

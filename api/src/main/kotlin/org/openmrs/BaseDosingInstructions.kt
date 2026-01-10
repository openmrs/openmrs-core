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

import org.apache.commons.lang3.time.DateUtils.addSeconds
import java.util.Date

/**
 * @since 2.3.4
 */
abstract class BaseDosingInstructions : DosingInstructions {

	/**
	 * @see DosingInstructions.getAutoExpireDate
	 */
	override fun getAutoExpireDate(drugOrder: DrugOrder): Date? {
		if (drugOrder.duration == null || drugOrder.durationUnits == null) {
			return null
		}
		val durationCode = Duration.getCode(drugOrder.durationUnits) ?: return null
		val duration = Duration(drugOrder.duration, durationCode)
		return aMomentBefore(duration.addToDate(drugOrder.effectiveStartDate, drugOrder.frequency))
	}

	private fun aMomentBefore(date: Date?): Date? {
		return date?.let { addSeconds(it, -1) }
	}
}

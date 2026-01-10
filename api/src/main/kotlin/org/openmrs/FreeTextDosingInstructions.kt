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

import org.openmrs.api.APIException
import org.springframework.validation.Errors
import org.springframework.validation.ValidationUtils
import java.util.Locale

/**
 * @since 1.10
 */
class FreeTextDosingInstructions : BaseDosingInstructions() {
	
	var instructions: String? = null
	
	/**
	 * @see DosingInstructions.getDosingInstructions
	 */
	override fun getDosingInstructionsAsString(locale: Locale): String = instructions ?: ""
	
	/**
	 * @see DosingInstructions.setDosingInstructions
	 */
	override fun setDosingInstructions(order: DrugOrder) {
		order.dosingType = this::class.java
		order.dosingInstructions = instructions
	}
	
	/**
	 * @see DosingInstructions.getDosingInstructions
	 */
	override fun getDosingInstructions(order: DrugOrder): DosingInstructions {
		if (order.dosingType != this::class.java) {
			throw APIException("DrugOrder.error.dosingTypeIsMismatched", arrayOf(this::class.java, order.dosingType))
		}
		return FreeTextDosingInstructions().apply {
			instructions = order.dosingInstructions
		}
	}
	
	override fun validate(order: DrugOrder, errors: Errors) {
		ValidationUtils.rejectIfEmpty(errors, "dosingInstructions",
			"DrugOrder.error.dosingInstructionsIsNullForDosingTypeFreeText")
	}
}

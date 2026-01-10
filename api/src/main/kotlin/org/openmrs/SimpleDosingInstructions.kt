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
class SimpleDosingInstructions : BaseDosingInstructions() {
	
	var dose: Double? = null
	
	var doseUnits: Concept? = null
	
	var route: Concept? = null
	
	var frequency: OrderFrequency? = null
	
	var duration: Int? = null
	
	var durationUnits: Concept? = null
	
	var asNeeded: Boolean? = null
	
	var asNeededCondition: String? = null
	
	var administrationInstructions: String? = null
	
	/**
	 * @see DosingInstructions.getDosingInstructionsAsString
	 */
	override fun getDosingInstructionsAsString(locale: Locale): String {
		return buildString {
			append(dose)
			append(" ")
			append(doseUnits?.getName(locale)?.name)
			append(" ")
			append(route?.getName(locale)?.name)
			append(" ")
			append(frequency)
			duration?.let {
				append(" ")
				append(it)
				append(" ")
				append(durationUnits?.getName(locale)?.name)
			}
			if (asNeeded == true) {
				append(" ")
				append("PRN")
				asNeededCondition?.let {
					append(" ")
					append(it)
				}
			}
			administrationInstructions?.let {
				append(" ")
				append(it)
			}
		}
	}
	
	/**
	 * @see DosingInstructions.setDosingInstructions
	 */
	override fun setDosingInstructions(order: DrugOrder) {
		order.dosingType = this::class.java
		order.dose = this.dose
		order.doseUnits = this.doseUnits
		order.route = this.route
		order.frequency = this.frequency
		order.duration = this.duration
		order.durationUnits = this.durationUnits
		order.asNeeded = this.asNeeded
		order.asNeededCondition = this.asNeededCondition
		order.dosingInstructions = this.administrationInstructions
	}
	
	/**
	 * @see DosingInstructions.getDosingInstructions
	 */
	override fun getDosingInstructions(order: DrugOrder): DosingInstructions {
		if (order.dosingType != this::class.java) {
			throw APIException("DrugOrder.error.dosingTypeIsMismatched", arrayOf(this::class.java.name, order.dosingType))
		}
		return SimpleDosingInstructions().apply {
			dose = order.dose
			doseUnits = order.doseUnits
			route = order.route
			frequency = order.frequency
			duration = order.duration
			durationUnits = order.durationUnits
			asNeeded = order.asNeeded
			asNeededCondition = order.asNeededCondition
			administrationInstructions = order.dosingInstructions
		}
	}
	
	/**
	 * @see DosingInstructions.validate
	 * @param order
	 * @param errors
	 * @should reject a duration unit with a mapping of an invalid type
	 */
	override fun validate(order: DrugOrder, errors: Errors) {
		ValidationUtils.rejectIfEmpty(errors, "dose", "DrugOrder.error.doseIsNullForDosingTypeSimple")
		ValidationUtils.rejectIfEmpty(errors, "doseUnits", "DrugOrder.error.doseUnitsIsNullForDosingTypeSimple")
		ValidationUtils.rejectIfEmpty(errors, "route", "DrugOrder.error.routeIsNullForDosingTypeSimple")
		ValidationUtils.rejectIfEmpty(errors, "frequency", "DrugOrder.error.frequencyIsNullForDosingTypeSimple")
		if (order.autoExpireDate == null && order.durationUnits != null
				&& Duration.getCode(order.durationUnits) == null) {
			errors.rejectValue("durationUnits", "DrugOrder.error.durationUnitsNotMappedToSnomedCtDurationCode")
		}
	}
}

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

import org.apache.commons.lang3.time.DateUtils.*
import org.openmrs.api.APIException
import java.util.Date

/**
 * Duration represented using SNOMED CT duration codes
 * 
 * @since 1.10
 */
class Duration(
	private val duration: Int?,
	private val code: String?
) {
	
	/**
	 * Add this duration to given startDate
	 * 
	 * @param startDate
	 * @param frequency is used to calculate time to be added to startDate when duration unit is
	 *            'Recurring Interval'
	 * @return date which is startDate plus duration
	 */
	fun addToDate(startDate: Date, frequency: OrderFrequency?): Date {
		return when (code) {
			SNOMED_CT_SECONDS_CODE -> addSeconds(startDate, duration!!)
			SNOMED_CT_MINUTES_CODE -> addMinutes(startDate, duration!!)
			SNOMED_CT_HOURS_CODE -> addHours(startDate, duration!!)
			SNOMED_CT_DAYS_CODE -> addDays(startDate, duration!!)
			SNOMED_CT_WEEKS_CODE -> addWeeks(startDate, duration!!)
			SNOMED_CT_MONTHS_CODE -> addMonths(startDate, duration!!)
			SNOMED_CT_YEARS_CODE -> addYears(startDate, duration!!)
			SNOMED_CT_RECURRING_INTERVAL_CODE -> {
				if (frequency == null) {
					throw APIException("Duration.error.frequency.null")
				}
				addSeconds(startDate, (duration!! * SECONDS_PER_DAY / frequency.frequencyPerDay).toInt())
			}
			else -> throw APIException("Duration.unknown.code", arrayOf(code))
		}
	}
	
	companion object {
		const val SNOMED_CT_SECONDS_CODE = "257997001"
		const val SNOMED_CT_MINUTES_CODE = "258701004"
		const val SNOMED_CT_HOURS_CODE = "258702006"
		const val SNOMED_CT_DAYS_CODE = "258703001"
		const val SNOMED_CT_WEEKS_CODE = "258705008"
		const val SNOMED_CT_MONTHS_CODE = "258706009"
		const val SNOMED_CT_YEARS_CODE = "258707000"
		const val SNOMED_CT_RECURRING_INTERVAL_CODE = "252109000"
		const val SNOMED_CT_CONCEPT_SOURCE_HL7_CODE = "SCT"
		
		private const val SECONDS_PER_MINUTE = 60
		private const val MINUTES_PER_HOUR = 60
		private const val HOURS_PER_DAY = 24
		private const val SECONDS_PER_HOUR = SECONDS_PER_MINUTE * MINUTES_PER_HOUR
		private const val SECONDS_PER_DAY = SECONDS_PER_HOUR * HOURS_PER_DAY
		
		/**
		 * Returns concept reference term code of the mapping to the SNOMED CT concept source
		 * 
		 * @param durationUnits
		 * @return a string which is reference term code
		 * @should return null if the concept has no mapping to the SNOMED CT source
		 * @should return the code for the term of the mapping to the SNOMED CT source
		 */
		@JvmStatic
		fun getCode(durationUnits: Concept): String? {
			for (conceptMapping in durationUnits.conceptMappings) {
				val conceptReferenceTerm = conceptMapping.conceptReferenceTerm
				if (ConceptMapType.SAME_AS_MAP_TYPE_UUID == conceptMapping.conceptMapType?.uuid &&
					SNOMED_CT_CONCEPT_SOURCE_HL7_CODE == conceptReferenceTerm?.conceptSource?.hl7Code) {
					return conceptReferenceTerm.code
				}
			}
			return null
		}
	}
}

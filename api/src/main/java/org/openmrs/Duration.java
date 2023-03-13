/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs;

import static org.apache.commons.lang3.time.DateUtils.addHours;
import static org.apache.commons.lang3.time.DateUtils.addMinutes;
import static org.apache.commons.lang3.time.DateUtils.addMonths;
import static org.apache.commons.lang3.time.DateUtils.addWeeks;
import static org.apache.commons.lang3.time.DateUtils.addYears;
import static org.apache.commons.lang3.time.DateUtils.addDays;
import static org.apache.commons.lang3.time.DateUtils.addSeconds;

import java.util.Date;

import org.openmrs.api.APIException;

/**
 * Duration represented using SNOMED CT duration codes
 * 
 * @since 1.10
 */
public class Duration {
	
	public static final String SNOMED_CT_SECONDS_CODE = "257997001";
	
	public static final String SNOMED_CT_MINUTES_CODE = "258701004";
	
	public static final String SNOMED_CT_HOURS_CODE = "258702006";
	
	public static final String SNOMED_CT_DAYS_CODE = "258703001";
	
	public static final String SNOMED_CT_WEEKS_CODE = "258705008";
	
	public static final String SNOMED_CT_MONTHS_CODE = "258706009";
	
	public static final String SNOMED_CT_YEARS_CODE = "258707000";
	
	public static final String SNOMED_CT_RECURRING_INTERVAL_CODE = "252109000";
	
	public static final String SNOMED_CT_CONCEPT_SOURCE_HL7_CODE = "SCT";
	
	private static final int SECONDS_PER_MINUTE = 60;
	
	private static final int MINUTES_PER_HOUR = 60;
	
	private static final int HOURS_PER_DAY = 24;
	
	private static final int SECONDS_PER_HOUR = SECONDS_PER_MINUTE * MINUTES_PER_HOUR;
	
	private static final int SECONDS_PER_DAY = SECONDS_PER_HOUR * HOURS_PER_DAY;
	
	private final Integer duration;
	
	private final String code;
	
	public Duration(Integer duration, String code) {
		this.duration = duration;
		this.code = code;
	}
	
	/**
	 * Add this duration to given startDate
	 * 
	 * @param startDate
	 * @param frequency is used to calculate time to be added to startDate when duration unit is
	 *            'Recurring Interval'
	 * @return date which is startDate plus duration
	 */
	public Date addToDate(Date startDate, OrderFrequency frequency) {
		if (SNOMED_CT_SECONDS_CODE.equals(code)) {
			return addSeconds(startDate, this.duration);
		}
		if (SNOMED_CT_MINUTES_CODE.equals(code)) {
			return addMinutes(startDate, this.duration);
		}
		if (SNOMED_CT_HOURS_CODE.equals(code)) {
			return addHours(startDate, this.duration);
		}
		if (SNOMED_CT_DAYS_CODE.equals(code)) {
			return addDays(startDate, this.duration);
		}
		if (SNOMED_CT_WEEKS_CODE.equals(code)) {
			return addWeeks(startDate, this.duration);
		}
		if (SNOMED_CT_MONTHS_CODE.equals(code)) {
			return addMonths(startDate, this.duration);
		}
		if (SNOMED_CT_YEARS_CODE.equals(code)) {
			return addYears(startDate, this.duration);
		}
		if (SNOMED_CT_RECURRING_INTERVAL_CODE.equals(code)) {
			if (frequency == null) {
				throw new APIException("Duration.error.frequency.null", (Object[]) null);
			}
			return addSeconds(startDate, (int) (this.duration * SECONDS_PER_DAY / frequency.getFrequencyPerDay()));
		} else {
			throw new APIException("Duration.unknown.code", new Object[] { code });
		}
	}
	
	/**
	 * Returns concept reference term code of the mapping to the SNOMED CT concept source
	 * 
	 * @param durationUnits
	 * @return a string which is reference term code
	 * <strong>Should</strong> return null if the concept has no mapping to the SNOMED CT source
	 * <strong>Should</strong> return the code for the term of the mapping to the SNOMED CT source
	 */
	public static String getCode(Concept durationUnits) {
		for (ConceptMap conceptMapping : durationUnits.getConceptMappings()) {
			ConceptReferenceTerm conceptReferenceTerm = conceptMapping.getConceptReferenceTerm();
			if (ConceptMapType.SAME_AS_MAP_TYPE_UUID.equals(conceptMapping.getConceptMapType().getUuid())
			        && Duration.SNOMED_CT_CONCEPT_SOURCE_HL7_CODE.equals(conceptReferenceTerm.getConceptSource()
			                .getHl7Code())) {
				return conceptReferenceTerm.getCode();
			}
		}
		return null;
	}
}

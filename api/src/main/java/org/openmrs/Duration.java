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

import java.util.Date;

import org.openmrs.api.APIException;

import static org.apache.commons.lang3.time.DateUtils.addDays;
import static org.apache.commons.lang3.time.DateUtils.addHours;
import static org.apache.commons.lang3.time.DateUtils.addMinutes;
import static org.apache.commons.lang3.time.DateUtils.addMonths;
import static org.apache.commons.lang3.time.DateUtils.addSeconds;
import static org.apache.commons.lang3.time.DateUtils.addWeeks;
import static org.apache.commons.lang3.time.DateUtils.addYears;

/**
 * Duration represented using UCUM time codes, with SNOMED CT duration codes
 * accepted as a fallback for backward compatibility
 *
 * @since 1.10
 */
public class Duration {

	public static final String SNOMED_CT_SECONDS_CODE = "257997001";

	public static final String SNOMED_CT_MINUTES_CODE = "258701004";

	/**
	 * Active SNOMED CT code for minute. SNOMED CT inactivated the legacy code
	 * {@link #SNOMED_CT_MINUTES_CODE} ("258701004") in its 2021-07-31 release,
	 * and dictionaries such as CIEL now map minutes concepts to this code. The
	 * legacy code is still accepted for backward compatibility.
	 *
	 * @since 3.0.0
	 */
	public static final String SNOMED_CT_MINUTES_CODE_2 = "1156209001";

	public static final String SNOMED_CT_HOURS_CODE = "258702006";

	public static final String SNOMED_CT_DAYS_CODE = "258703001";

	public static final String SNOMED_CT_WEEKS_CODE = "258705008";

	public static final String SNOMED_CT_MONTHS_CODE = "258706009";

	public static final String SNOMED_CT_YEARS_CODE = "258707000";

	public static final String SNOMED_CT_RECURRING_INTERVAL_CODE = "252109000";

	public static final String SNOMED_CT_CONCEPT_SOURCE_HL7_CODE = "SCT";

	/**
	 * UCUM code for the "second" unit of time.
	 * <p>
	 * UCUM (Unified Code for Units of Measure) time codes are standardized and
	 * stable, unlike SNOMED CT codes which are occasionally inactivated and
	 * re-coded. These codes match the FHIR Timing.UnitsOfTime system codes,
	 * aligning core with the FHIR module.
	 *
	 * @since 3.0.0
	 */
	public static final String UCUM_SECONDS_CODE = "s";

	/**
	 * UCUM code for the "minute" unit of time.
	 *
	 * @since 3.0.0
	 */
	public static final String UCUM_MINUTES_CODE = "min";

	/**
	 * UCUM code for the "hour" unit of time.
	 *
	 * @since 3.0.0
	 */
	public static final String UCUM_HOURS_CODE = "h";

	/**
	 * UCUM code for the "day" unit of time.
	 *
	 * @since 3.0.0
	 */
	public static final String UCUM_DAYS_CODE = "d";

	/**
	 * UCUM code for the "week" unit of time.
	 *
	 * @since 3.0.0
	 */
	public static final String UCUM_WEEKS_CODE = "wk";

	/**
	 * UCUM code for the "month" unit of time.
	 *
	 * @since 3.0.0
	 */
	public static final String UCUM_MONTHS_CODE = "mo";

	/**
	 * UCUM code for the "year" unit of time.
	 *
	 * @since 3.0.0
	 */
	public static final String UCUM_YEARS_CODE = "a";

	/**
	 * Identifier for the UCUM concept source.
	 * <p>
	 * OpenMRS core ships no UCUM concept source in its metadata, so the source
	 * cannot be matched by a fixed database id. Deployments (e.g. those loading
	 * the CIEL dictionary) commonly register it with both an hl7Code and a name
	 * of "UCUM". It is therefore matched case-insensitively against either the
	 * concept source's hl7Code or its name.
	 *
	 * @since 3.0.0
	 */
	public static final String UCUM_CONCEPT_SOURCE_HL7_CODE = "UCUM";

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
		if (UCUM_SECONDS_CODE.equals(code) || SNOMED_CT_SECONDS_CODE.equals(code)) {
			return addSeconds(startDate, this.duration);
		}
		if (UCUM_MINUTES_CODE.equals(code) || SNOMED_CT_MINUTES_CODE.equals(code)
		        || SNOMED_CT_MINUTES_CODE_2.equals(code)) {
			return addMinutes(startDate, this.duration);
		}
		if (UCUM_HOURS_CODE.equals(code) || SNOMED_CT_HOURS_CODE.equals(code)) {
			return addHours(startDate, this.duration);
		}
		if (UCUM_DAYS_CODE.equals(code) || SNOMED_CT_DAYS_CODE.equals(code)) {
			return addDays(startDate, this.duration);
		}
		if (UCUM_WEEKS_CODE.equals(code) || SNOMED_CT_WEEKS_CODE.equals(code)) {
			return addWeeks(startDate, this.duration);
		}
		if (UCUM_MONTHS_CODE.equals(code) || SNOMED_CT_MONTHS_CODE.equals(code)) {
			return addMonths(startDate, this.duration);
		}
		if (UCUM_YEARS_CODE.equals(code) || SNOMED_CT_YEARS_CODE.equals(code)) {
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
	 * Returns the SAME-AS reference term code used to resolve the duration unit.
	 * <p>
	 * The UCUM mapping is preferred when present because UCUM time codes are
	 * standardized and stable. When no UCUM SAME-AS mapping exists, the SNOMED CT
	 * SAME-AS mapping is returned for backward compatibility, preserving the
	 * historical behavior for deployments that only carry SNOMED codes.
	 * <p>
	 * <strong>Should</strong> return null if the concept has no mapping to the
	 * SNOMED CT source<br/>
	 * <strong>Should</strong> return the code for the term of the mapping to the
	 * SNOMED CT source<br/>
	 * <strong>Should</strong> prefer the UCUM code when both a UCUM and a SNOMED
	 * CT mapping are present
	 *
	 * @param durationUnits
	 * @return a string which is reference term code
	 * @since 3.0.0 prefers the UCUM SAME-AS mapping, falling back to SNOMED CT
	 */
	public static String getCode(Concept durationUnits) {
		String snomedCtCode = null;
		for (ConceptMap conceptMapping : durationUnits.getConceptMappings()) {
			if (!ConceptMapType.SAME_AS_MAP_TYPE_UUID.equals(conceptMapping.getConceptMapType().getUuid())) {
				continue;
			}
			ConceptReferenceTerm conceptReferenceTerm = conceptMapping.getConceptReferenceTerm();
			ConceptSource conceptSource = conceptReferenceTerm.getConceptSource();
			if (isUcumSource(conceptSource)) {
				return conceptReferenceTerm.getCode();
			}
			if (snomedCtCode == null
			        && Duration.SNOMED_CT_CONCEPT_SOURCE_HL7_CODE.equals(conceptSource.getHl7Code())) {
				snomedCtCode = conceptReferenceTerm.getCode();
			}
		}
		return snomedCtCode;
	}

	/**
	 * Tells whether the given concept source is the UCUM source.
	 * <p>
	 * Matched case-insensitively against either the source's hl7Code or its
	 * name, since core ships no UCUM source with a fixed id.
	 *
	 * @param conceptSource the concept source to inspect
	 * @return true if the source represents UCUM
	 * @since 3.0.0
	 */
	private static boolean isUcumSource(ConceptSource conceptSource) {
		return UCUM_CONCEPT_SOURCE_HL7_CODE.equalsIgnoreCase(conceptSource.getHl7Code())
		        || UCUM_CONCEPT_SOURCE_HL7_CODE.equalsIgnoreCase(conceptSource.getName());
	}
}

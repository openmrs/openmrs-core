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

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.openmrs.api.APIException;

import static org.apache.commons.lang3.time.DateUtils.addDays;
import static org.apache.commons.lang3.time.DateUtils.addHours;
import static org.apache.commons.lang3.time.DateUtils.addMinutes;
import static org.apache.commons.lang3.time.DateUtils.addMonths;
import static org.apache.commons.lang3.time.DateUtils.addSeconds;
import static org.apache.commons.lang3.time.DateUtils.addWeeks;
import static org.apache.commons.lang3.time.DateUtils.addYears;

/**
 * Duration represented using SNOMED CT or UCUM duration codes
 *
 * @since 1.10
 */
public class Duration {

	public static final String SNOMED_CT_SECONDS_CODE = "257997001";

	public static final String SNOMED_CT_MINUTES_CODE = "258701004";

	/**
	 * The SNOMED CT code for minute that is active since the 2021-07-31 release, in which the legacy
	 * code {@link #SNOMED_CT_MINUTES_CODE} was inactivated as ambiguous. Dictionaries that track
	 * current SNOMED CT releases, such as CIEL, map their minutes concepts to this code.
	 *
	 * @since 3.0.0
	 */
	public static final String SNOMED_CT_MINUTES_CODE_2021 = "1156209001";

	public static final String SNOMED_CT_HOURS_CODE = "258702006";

	public static final String SNOMED_CT_DAYS_CODE = "258703001";

	public static final String SNOMED_CT_WEEKS_CODE = "258705008";

	public static final String SNOMED_CT_MONTHS_CODE = "258706009";

	public static final String SNOMED_CT_YEARS_CODE = "258707000";

	public static final String SNOMED_CT_RECURRING_INTERVAL_CODE = "252109000";

	public static final String SNOMED_CT_CONCEPT_SOURCE_HL7_CODE = "SCT";

	/**
	 * Name identifying the concept source for The Unified Code for Units of Measure
	 * (<a href="https://ucum.org">ucum.org</a>), whose codes for the units of time are stable by
	 * design. Dictionaries commonly map duration unit concepts to UCUM in addition to SNOMED CT.
	 *
	 * @since 3.0.0
	 */
	public static final String UCUM_CONCEPT_SOURCE_NAME = "UCUM";

	/**
	 * @since 3.0.0
	 */
	public static final String UCUM_SECONDS_CODE = "s";

	/**
	 * @since 3.0.0
	 */
	public static final String UCUM_MINUTES_CODE = "min";

	/**
	 * @since 3.0.0
	 */
	public static final String UCUM_HOURS_CODE = "h";

	/**
	 * @since 3.0.0
	 */
	public static final String UCUM_DAYS_CODE = "d";

	/**
	 * @since 3.0.0
	 */
	public static final String UCUM_WEEKS_CODE = "wk";

	/**
	 * @since 3.0.0
	 */
	public static final String UCUM_MONTHS_CODE = "mo";

	/**
	 * @since 3.0.0
	 */
	public static final String UCUM_YEARS_CODE = "a";

	private static final int SECONDS_PER_MINUTE = 60;

	private static final int MINUTES_PER_HOUR = 60;

	private static final int HOURS_PER_DAY = 24;

	private static final int SECONDS_PER_HOUR = SECONDS_PER_MINUTE * MINUTES_PER_HOUR;

	private static final int SECONDS_PER_DAY = SECONDS_PER_HOUR * HOURS_PER_DAY;

	/**
	 * The units of time a duration code can denote. Several codes may denote the same unit, e.g. the
	 * legacy SNOMED CT minute code, its active replacement and the UCUM minute code.
	 */
	private enum Unit {

		SECONDS {

			@Override
			Date addToDate(Date startDate, Integer duration, OrderFrequency frequency) {
				return addSeconds(startDate, duration);
			}
		},

		MINUTES {

			@Override
			Date addToDate(Date startDate, Integer duration, OrderFrequency frequency) {
				return addMinutes(startDate, duration);
			}
		},

		HOURS {

			@Override
			Date addToDate(Date startDate, Integer duration, OrderFrequency frequency) {
				return addHours(startDate, duration);
			}
		},

		DAYS {

			@Override
			Date addToDate(Date startDate, Integer duration, OrderFrequency frequency) {
				return addDays(startDate, duration);
			}
		},

		WEEKS {

			@Override
			Date addToDate(Date startDate, Integer duration, OrderFrequency frequency) {
				return addWeeks(startDate, duration);
			}
		},

		MONTHS {

			@Override
			Date addToDate(Date startDate, Integer duration, OrderFrequency frequency) {
				return addMonths(startDate, duration);
			}
		},

		YEARS {

			@Override
			Date addToDate(Date startDate, Integer duration, OrderFrequency frequency) {
				return addYears(startDate, duration);
			}
		},

		RECURRING_INTERVAL {

			@Override
			Date addToDate(Date startDate, Integer duration, OrderFrequency frequency) {
				if (frequency == null) {
					throw new APIException("Duration.error.frequency.null", (Object[]) null);
				}
				return addSeconds(startDate, (int) (duration * SECONDS_PER_DAY / frequency.getFrequencyPerDay()));
			}
		};

		abstract Date addToDate(Date startDate, Integer duration, OrderFrequency frequency);
	}

	private static final Map<String, Unit> UNITS_BY_CODE;

	static {
		Map<String, Unit> unitsByCode = new HashMap<>();
		unitsByCode.put(SNOMED_CT_SECONDS_CODE, Unit.SECONDS);
		unitsByCode.put(UCUM_SECONDS_CODE, Unit.SECONDS);
		unitsByCode.put(SNOMED_CT_MINUTES_CODE, Unit.MINUTES);
		unitsByCode.put(SNOMED_CT_MINUTES_CODE_2021, Unit.MINUTES);
		unitsByCode.put(UCUM_MINUTES_CODE, Unit.MINUTES);
		unitsByCode.put(SNOMED_CT_HOURS_CODE, Unit.HOURS);
		unitsByCode.put(UCUM_HOURS_CODE, Unit.HOURS);
		unitsByCode.put(SNOMED_CT_DAYS_CODE, Unit.DAYS);
		unitsByCode.put(UCUM_DAYS_CODE, Unit.DAYS);
		unitsByCode.put(SNOMED_CT_WEEKS_CODE, Unit.WEEKS);
		unitsByCode.put(UCUM_WEEKS_CODE, Unit.WEEKS);
		unitsByCode.put(SNOMED_CT_MONTHS_CODE, Unit.MONTHS);
		unitsByCode.put(UCUM_MONTHS_CODE, Unit.MONTHS);
		unitsByCode.put(SNOMED_CT_YEARS_CODE, Unit.YEARS);
		unitsByCode.put(UCUM_YEARS_CODE, Unit.YEARS);
		unitsByCode.put(SNOMED_CT_RECURRING_INTERVAL_CODE, Unit.RECURRING_INTERVAL);
		UNITS_BY_CODE = Collections.unmodifiableMap(unitsByCode);
	}

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
		Unit unit = UNITS_BY_CODE.get(code);
		if (unit == null) {
			throw new APIException("Duration.unknown.code", new Object[] { code });
		}
		return unit.addToDate(startDate, this.duration, frequency);
	}

	/**
	 * Returns concept reference term code of the mapping to the SNOMED CT concept source
	 * <p>
	 * Note that the returned code depends on the iteration order of the concept's mappings and is not
	 * necessarily a code this class can interpret; see {@link #getKnownCode(Concept)} for resolving a
	 * concept to a code that is guaranteed to be accepted by {@link #addToDate(Date, OrderFrequency)}.
	 * <p>
	 * <strong>Should</strong> return null if the concept has no mapping to the SNOMED CT source<br/>
	 * <strong>Should</strong> return the code for the term of the mapping to the SNOMED CT source
	 *
	 * @param durationUnits
	 * @return a string which is reference term code
	 */
	public static String getCode(Concept durationUnits) {
		for (ConceptMap conceptMapping : durationUnits.getConceptMappings()) {
			ConceptReferenceTerm conceptReferenceTerm = conceptMapping.getConceptReferenceTerm();
			if (ConceptMapType.SAME_AS_MAP_TYPE_UUID.equals(conceptMapping.getConceptMapType().getUuid())
			        && isSnomedCtSource(conceptReferenceTerm.getConceptSource())) {
				return conceptReferenceTerm.getCode();
			}
		}
		return null;
	}

	/**
	 * Returns a code of the given concept that this class knows how to interpret as a duration unit,
	 * considering all of the concept's SAME-AS mappings to the SNOMED CT and UCUM concept sources.
	 * Every code this method returns is accepted by {@link #addToDate(Date, OrderFrequency)}.
	 * <p>
	 * Unlike {@link #getCode(Concept)}, the unit denoted by the result does not depend on the iteration
	 * order of the concept's mappings, though which of several equivalent codes is returned may.
	 * Dictionaries may map one unit concept to several codes, for example both the legacy and the
	 * current SNOMED CT code for minute, and any of them is accepted as long as all known codes of the
	 * concept denote the same unit. If they denote different units, null is returned, so that a
	 * miscurated dictionary surfaces as a validation error rather than a silently wrong expiry date.
	 * <p>
	 * <strong>Should</strong> return the code of a SNOMED CT SAME-AS mapping with a known code<br/>
	 * <strong>Should</strong> return a minutes code for a concept carrying both the legacy and the
	 * current SNOMED CT minute code<br/>
	 * <strong>Should</strong> return a UCUM code when the concept has only a UCUM mapping<br/>
	 * <strong>Should</strong> return null if no SAME-AS mapping carries a known code<br/>
	 * <strong>Should</strong> return null if known codes of the concept denote different units
	 *
	 * @param durationUnits the duration units concept of a drug order
	 * @return a known reference term code, or null if the concept cannot be resolved to a single unit
	 * @since 3.0.0
	 */
	public static String getKnownCode(Concept durationUnits) {
		String knownCode = null;
		Unit knownUnit = null;
		for (ConceptMap conceptMapping : durationUnits.getConceptMappings()) {
			if (!ConceptMapType.SAME_AS_MAP_TYPE_UUID.equals(conceptMapping.getConceptMapType().getUuid())) {
				continue;
			}
			ConceptReferenceTerm conceptReferenceTerm = conceptMapping.getConceptReferenceTerm();
			ConceptSource conceptSource = conceptReferenceTerm.getConceptSource();
			if (!isSnomedCtSource(conceptSource) && !isUcumSource(conceptSource)) {
				continue;
			}
			Unit unit = UNITS_BY_CODE.get(conceptReferenceTerm.getCode());
			if (unit == null) {
				continue;
			}
			if (knownUnit == null) {
				knownCode = conceptReferenceTerm.getCode();
				knownUnit = unit;
			} else if (knownUnit != unit) {
				return null;
			}
		}
		return knownCode;
	}

	private static boolean isSnomedCtSource(ConceptSource conceptSource) {
		return SNOMED_CT_CONCEPT_SOURCE_HL7_CODE.equals(conceptSource.getHl7Code());
	}

	private static boolean isUcumSource(ConceptSource conceptSource) {
		return UCUM_CONCEPT_SOURCE_NAME.equalsIgnoreCase(conceptSource.getName())
		        || UCUM_CONCEPT_SOURCE_NAME.equals(conceptSource.getHl7Code());
	}
}

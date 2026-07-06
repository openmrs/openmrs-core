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
import java.util.Map;

import org.openmrs.api.APIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	private static final Logger log = LoggerFactory.getLogger(Duration.class);

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
	 * Name identifying the SNOMED CT concept source, used as a fallback for dictionaries that register
	 * the source by name without setting its HL7 code to {@link #SNOMED_CT_CONCEPT_SOURCE_HL7_CODE},
	 * mirroring how {@link #UCUM_CONCEPT_SOURCE_NAME} is matched.
	 *
	 * @since 3.0.0
	 */
	public static final String SNOMED_CT_CONCEPT_SOURCE_NAME = "SNOMED CT";

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

	private static final Map<String, Unit> SNOMED_CT_UNITS_BY_CODE = Map.ofEntries(
	    Map.entry(SNOMED_CT_SECONDS_CODE, Unit.SECONDS), Map.entry(SNOMED_CT_MINUTES_CODE, Unit.MINUTES),
	    Map.entry(SNOMED_CT_MINUTES_CODE_2021, Unit.MINUTES), Map.entry(SNOMED_CT_HOURS_CODE, Unit.HOURS),
	    Map.entry(SNOMED_CT_DAYS_CODE, Unit.DAYS), Map.entry(SNOMED_CT_WEEKS_CODE, Unit.WEEKS),
	    Map.entry(SNOMED_CT_MONTHS_CODE, Unit.MONTHS), Map.entry(SNOMED_CT_YEARS_CODE, Unit.YEARS),
	    Map.entry(SNOMED_CT_RECURRING_INTERVAL_CODE, Unit.RECURRING_INTERVAL));

	private static final Map<String, Unit> UCUM_UNITS_BY_CODE = Map.ofEntries(Map.entry(UCUM_SECONDS_CODE, Unit.SECONDS),
	    Map.entry(UCUM_MINUTES_CODE, Unit.MINUTES), Map.entry(UCUM_HOURS_CODE, Unit.HOURS),
	    Map.entry(UCUM_DAYS_CODE, Unit.DAYS), Map.entry(UCUM_WEEKS_CODE, Unit.WEEKS),
	    Map.entry(UCUM_MONTHS_CODE, Unit.MONTHS), Map.entry(UCUM_YEARS_CODE, Unit.YEARS));

	private final Integer duration;

	private final Unit unit;

	/**
	 * The raw code this duration was constructed from, retained only so {@link #addToDate} can name the
	 * offending code when it is not one this class recognises. Null when the duration was built from an
	 * already-resolved {@link Unit}.
	 */
	private final String code;

	public Duration(Integer duration, String code) {
		this.duration = duration;
		this.code = code;
		this.unit = findUnit(code);
	}

	private Duration(Integer duration, Unit unit) {
		this.duration = duration;
		this.unit = unit;
		this.code = null;
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
		if (unit == null) {
			throw new APIException("Duration.unknown.code", new Object[] { code });
		}
		return unit.addToDate(startDate, this.duration, frequency);
	}

	/**
	 * Resolves a bare code to a {@link Unit} the way the legacy {@link #Duration(Integer, String)}
	 * constructor must: by trying the SNOMED CT then the UCUM code tables, since the constructor
	 * carries no concept source. Returns null when the code is not recognised.
	 */
	private static Unit findUnit(String code) {
		Unit unit = SNOMED_CT_UNITS_BY_CODE.get(code);
		return unit != null ? unit : UCUM_UNITS_BY_CODE.get(code);
	}

	/**
	 * Returns concept reference term code of the mapping to the SNOMED CT concept source
	 * <p>
	 * <strong>Should</strong> return null if the concept has no mapping to the SNOMED CT source<br/>
	 * <strong>Should</strong> return the code for the term of the mapping to the SNOMED CT source
	 *
	 * @param durationUnits
	 * @return a string which is reference term code
	 * @deprecated as of 3.0.0, because the returned code depends on the iteration order of the
	 *             concept's mappings and is not necessarily a code this class can interpret; use
	 *             {@link #getDuration(Integer, Concept)} instead
	 */
	@Deprecated
	public static String getCode(Concept durationUnits) {
		for (ConceptMap conceptMapping : durationUnits.getConceptMappings()) {
			ConceptReferenceTerm conceptReferenceTerm = conceptMapping.getConceptReferenceTerm();
			if (ConceptMapType.SAME_AS_MAP_TYPE_UUID.equals(conceptMapping.getConceptMapType().getUuid())
			        && Duration.SNOMED_CT_CONCEPT_SOURCE_HL7_CODE
			                .equals(conceptReferenceTerm.getConceptSource().getHl7Code())) {
				return conceptReferenceTerm.getCode();
			}
		}
		return null;
	}

	/**
	 * Resolves the given duration units concept to a {@link Duration} of the given length, considering
	 * all of the concept's SAME-AS mappings to the SNOMED CT and UCUM concept sources.
	 * <p>
	 * The result does not depend on the iteration order of the concept's mappings. Dictionaries may map
	 * one unit concept to several codes, for example both the legacy and the current SNOMED CT code for
	 * minute, and the concept resolves as long as all of its known codes denote the same unit. If they
	 * denote different units, null is returned and the conflict is logged, so that a miscurated
	 * dictionary surfaces as a validation error rather than a silently wrong expiry date.
	 * <p>
	 * <strong>Should</strong> resolve a SNOMED CT SAME-AS mapping with a known code<br/>
	 * <strong>Should</strong> resolve a concept carrying both the legacy and the current SNOMED CT
	 * minute code<br/>
	 * <strong>Should</strong> resolve a concept with only a UCUM mapping<br/>
	 * <strong>Should</strong> return null if no SAME-AS mapping carries a known code<br/>
	 * <strong>Should</strong> return null if known codes of the concept denote different units
	 *
	 * @param duration the length of the duration, in the units the concept denotes
	 * @param durationUnits the duration units concept of a drug order
	 * @return a Duration, or null if the concept cannot be resolved to exactly one known unit
	 * @since 3.0.0
	 */
	public static Duration getDuration(Integer duration, Concept durationUnits) {
		Unit knownUnit = null;
		for (ConceptMap conceptMapping : durationUnits.getConceptMappings()) {
			if (!ConceptMapType.SAME_AS_MAP_TYPE_UUID.equals(conceptMapping.getConceptMapType().getUuid())) {
				continue;
			}
			ConceptReferenceTerm conceptReferenceTerm = conceptMapping.getConceptReferenceTerm();
			Unit unit = findUnit(conceptReferenceTerm.getConceptSource(), conceptReferenceTerm.getCode());
			if (unit == null) {
				continue;
			}
			if (knownUnit == null) {
				knownUnit = unit;
			} else if (knownUnit != unit) {
				log.warn("Not resolving duration units concept {} because its SAME-AS mappings denote different units,"
				        + " e.g. {} and {}",
				    durationUnits.getUuid(), knownUnit, unit);
				return null;
			}
		}
		if (knownUnit == null) {
			return null;
		}
		return new Duration(duration, knownUnit);
	}

	private static Unit findUnit(ConceptSource conceptSource, String code) {
		if (isSnomedCtSource(conceptSource)) {
			return SNOMED_CT_UNITS_BY_CODE.get(code);
		}
		if (isUcumSource(conceptSource)) {
			return UCUM_UNITS_BY_CODE.get(code);
		}
		return null;
	}

	private static boolean isSnomedCtSource(ConceptSource conceptSource) {
		return SNOMED_CT_CONCEPT_SOURCE_HL7_CODE.equals(conceptSource.getHl7Code())
		        || SNOMED_CT_CONCEPT_SOURCE_NAME.equalsIgnoreCase(conceptSource.getName());
	}

	private static boolean isUcumSource(ConceptSource conceptSource) {
		return UCUM_CONCEPT_SOURCE_NAME.equalsIgnoreCase(conceptSource.getName())
		        || UCUM_CONCEPT_SOURCE_NAME.equals(conceptSource.getHl7Code());
	}
}

/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAccessor;
import java.util.Date;

/**
 * Utility classes that provide date-related methods
 *
 * @since 2.0
 */
public class DateUtil {

	/**
	 * Legacy space-separated datetime format used when parsing strings without a {@code T} separator
	 */
	public static final String DATETIME_PATTERN = "yyyy-MM-dd HH:mm";

	private DateUtil() {
	}

	/**
	 * Parses a datetime string into a {@link Date}.
	 * <p>
	 * Handles both the modern ISO 8601 format (with a {@code T} separator) and the legacy
	 * space-separated format (e.g. {@code "2023-06-15 10:30:00"}).
	 * <p>
	 * For ISO 8601 strings, the following timezone/offset styles are all accepted:
	 * <ul>
	 * <li>{@code Z} — UTC</li>
	 * <li>{@code +05:00} — offset with colon (standard ISO 8601)</li>
	 * <li>{@code +0500} — offset without colon (used by the REST-WS module)</li>
	 * <li>{@code +05} — hour-only offset</li>
	 * <li>No offset — interpreted as the system default timezone</li>
	 * </ul>
	 *
	 * @param str the datetime string to parse; must not be null
	 * @return the parsed {@link Date}
	 * @throws ParseException if the string cannot be parsed by the legacy formatter
	 * @since 2.8.7, 2.9.0, 3.0.0
	 */
	public static Date parseDatetimeString(String str) throws ParseException {
		if (str.contains("T")) {
			// ISO 8601 format — handles with/without timezone, and both offset styles (+05:00 and +0500)
			// ISO_DATE_TIME only accepts +HH:MM offsets, so we build a formatter that also accepts +HHMM (no colon--which is what the REST-WS module uses)
			DateTimeFormatter isoFormatter = new DateTimeFormatterBuilder().append(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
			        .optionalStart().appendPattern("XXX").optionalEnd() // e.g. +05:00 or Z
			        .optionalStart().appendPattern("XX").optionalEnd() // e.g. +0500
			        .optionalStart().appendPattern("X").optionalEnd() // e.g. +05
			        .toFormatter().withResolverStyle(ResolverStyle.STRICT);
			// Returns a TemporalAccessor — the common supertype for all java.time datetime types
			TemporalAccessor parsed = isoFormatter.parse(str);
			// If the string included a timezone/offset, convert using that offset; otherwise assume system timezone
			Instant instant = parsed.isSupported(ChronoField.OFFSET_SECONDS) ? OffsetDateTime.from(parsed).toInstant()
			        : LocalDateTime.from(parsed).atZone(ZoneId.systemDefault()).toInstant();
			return Date.from(instant);
		} else {
			// Try the seconds-aware pattern first so that values like "2023-06-15 10:30:45" keep
			// their seconds, then fall back to the existing pattern. Both formatters are
			// non-lenient and the whole string must be consumed, so invalid or partially
			// matching input throws a ParseException instead of being silently changed.
			for (String pattern : new String[] { DATETIME_PATTERN + ":ss", DATETIME_PATTERN }) {
				DateFormat legacyFormat = new SimpleDateFormat(pattern);
				legacyFormat.setLenient(false);
				ParsePosition position = new ParsePosition(0);
				Date parsed = legacyFormat.parse(str, position);
				if (parsed != null && position.getIndex() == str.length()) {
					return parsed;
				}
			}
			throw new ParseException("Unable to parse datetime string: " + str, 0);
		}
	}

	/**
	 * @param date
	 * @return date truncated to second precision (e.g. with milliseconds dropped)
	 */
	public static Date truncateToSeconds(Date date) {
		Instant instant = date.toInstant().truncatedTo(ChronoUnit.SECONDS);
		return Date.from(instant);
	}

}

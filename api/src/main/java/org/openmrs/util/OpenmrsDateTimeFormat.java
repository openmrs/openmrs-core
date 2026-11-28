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

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * An extension of SimpleDateFormat used for date+time patterns that defaults to setLenient(false)
 * and rejects any parsed date before the year 1000 (to prevent against 2-digit years being
 * interpreted as years in the first century). Unlike {@link OpenmrsDateFormat}, any text after the
 * parsed pattern is ignored, as with a regular SimpleDateFormat.
 */
class OpenmrsDateTimeFormat extends SimpleDateFormat {

	private static final long serialVersionUID = 1L;

	OpenmrsDateTimeFormat(String pattern) {
		super(pattern);
		this.setLenient(false);
	}

	@Override
	public Date parse(String text, ParsePosition pos) {
		int start = pos.getIndex();
		Date date = super.parse(text, pos);
		if (date != null) {
			calendar.setTime(date);
			if (calendar.get(Calendar.YEAR) < 1000) {
				pos.setIndex(start);
				pos.setErrorIndex(start);
				return null;
			}
		}
		return date;
	}
}

/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.test;

import java.util.Date;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.openmrs.module.webservices.rest.web.ConversionUtil;

/**
 * A matcher that will compare two strings representing datetimes with time zones, accounting for
 * the fact that midnight in Boston is 9pm in Seattle, etc.
 */
public class SameDatetimeMatcher extends BaseMatcher<String> {
	
	private Date expected;
	
	public SameDatetimeMatcher(Date expected) {
		this.expected = expected;
	}
	
	public SameDatetimeMatcher(String expected) {
		this.expected = (Date) ConversionUtil.convert(expected, Date.class);
	}
	
	@Override
	public boolean matches(Object item) {
		Date actual = (Date) ConversionUtil.convert(item, Date.class);
		return expected.getTime() == actual.getTime();
	}
	
	@Override
	public void describeTo(Description description) {
		description.appendText("same time as " + expected);
	}
	
	// convenience factory methods
	
	public static SameDatetimeMatcher sameDatetime(String expected) {
		return new SameDatetimeMatcher(expected);
	}
	
	public static SameDatetimeMatcher sameDatetime(Date expected) {
		return new SameDatetimeMatcher(expected);
	}
	
}

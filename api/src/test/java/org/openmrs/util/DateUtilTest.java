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

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.util.Date;

import org.junit.Test;

public class DateUtilTest {

	@Test
	public void truncateToSeconds_shouldDropMilliseconds() {
		Date withMilliseconds = new Date(123L);
		Date withoutMilliseconds = new Date(0L);
		assertThat(DateUtil.truncateToSeconds(withMilliseconds), is(withoutMilliseconds));
		assertThat(DateUtil.truncateToSeconds(withoutMilliseconds), is(withoutMilliseconds));
	}
}

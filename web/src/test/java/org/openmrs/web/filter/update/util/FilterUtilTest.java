/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.filter.update.util;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.openmrs.web.filter.util.FilterUtil;
import org.openmrs.web.test.BaseWebContextSensitiveTest;

/**
 * Tests some of the methods on the {@link FilterUtil}
 */
public class FilterUtilTest extends BaseWebContextSensitiveTest {
	
	/**
	 * @see FilterUtil#storeLocale(String))
	 */
	@Test
	@Ignore
	public void storeLocale_shouldStoreLocale() {
		FilterUtil.storeLocale("it");
	}
	
	/**
	 * @see {@link FilterUtil#restoreLocale(String))
	 */
	@Test
	@Ignore
	public void storeLocale_shouldRestoreLocale() {
		FilterUtil.storeLocale("it");
		Assert.assertEquals("it", FilterUtil.restoreLocale(FilterUtil.ADMIN_USERNAME));
	}
	
}

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;
import org.openmrs.web.filter.util.FilterUtil;
import org.openmrs.web.test.jupiter.BaseWebContextSensitiveTest;

/**
 * Tests some of the methods on the {@link FilterUtil}
 */
public class FilterUtilTest extends BaseWebContextSensitiveTest {

	private static final String INITIAL_LOCALE = "it";
	
	private static final String UPDATED_LOCALE = "fr";
	
	/**
	 * @see FilterUtil#storeLocale(String)
	 */
	@Test
	public void storeLocale_shouldStoreLocale() {
		FilterUtil.storeLocale(INITIAL_LOCALE);
	}
	
	/**
	 * @see FilterUtil#restoreLocale(String)
	 */
	@Test
	public void storeLocale_shouldRestoreLocale() {
		FilterUtil.storeLocale(INITIAL_LOCALE);
		assertEquals(INITIAL_LOCALE, FilterUtil.restoreLocale(FilterUtil.ADMIN_USERNAME));
	}

	@Test
	public void storeLocale_shouldNotChangeStoredValueWhenStoringSameLocale() {
		FilterUtil.storeLocale(INITIAL_LOCALE);
		assertEquals(INITIAL_LOCALE, FilterUtil.restoreLocale(FilterUtil.ADMIN_USERNAME));

		FilterUtil.storeLocale(INITIAL_LOCALE);
		assertEquals(INITIAL_LOCALE, FilterUtil.restoreLocale(FilterUtil.ADMIN_USERNAME));
	}

	@Test
	public void storeLocale_shouldNotUpdateStoredValueWhenStoringDifferentLocale() {
		FilterUtil.storeLocale(INITIAL_LOCALE);
		assertEquals(INITIAL_LOCALE, FilterUtil.restoreLocale(FilterUtil.ADMIN_USERNAME));

		FilterUtil.storeLocale(UPDATED_LOCALE);
		assertNotEquals(UPDATED_LOCALE, FilterUtil.restoreLocale(FilterUtil.ADMIN_USERNAME));
	}

}

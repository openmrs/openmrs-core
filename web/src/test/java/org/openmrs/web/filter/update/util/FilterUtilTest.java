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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.openmrs.web.filter.util.FilterUtil.getUserIdByName;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.util.DatabaseUpdater;
import org.openmrs.web.filter.util.FilterUtil;
import org.openmrs.web.test.BaseWebContextSensitiveTest;

import java.sql.Connection;
import java.sql.PreparedStatement;

/**
 * Tests some of the methods on the {@link FilterUtil}
 */
public class FilterUtilTest extends BaseWebContextSensitiveTest {

	private static final String INITIAL_LOCALE = "it";
	
	private static final String UPDATED_LOCALE = "fr";
	
	@BeforeEach
	public void setup() {
		resetToDefaultLocale(INITIAL_LOCALE);
	}
	
	/**
	 * @see FilterUtil#storeLocale(String))
	 */
	@Test
	public void storeLocale_shouldStoreLocale() {
		FilterUtil.storeLocale(INITIAL_LOCALE);
	}
	
	/**
	 * @see {@link FilterUtil#restoreLocale(String))
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

		assertTrue(FilterUtil.storeLocale(INITIAL_LOCALE));
		assertEquals(INITIAL_LOCALE, FilterUtil.restoreLocale(FilterUtil.ADMIN_USERNAME));
	}

	@Test
	public void storeLocale_shouldUpdateStoredValueWhenStoringDifferentLocale() {
		FilterUtil.storeLocale(INITIAL_LOCALE);
		assertEquals(INITIAL_LOCALE, FilterUtil.restoreLocale(FilterUtil.ADMIN_USERNAME));

		assertTrue(FilterUtil.storeLocale(UPDATED_LOCALE));
		assertEquals(UPDATED_LOCALE, FilterUtil.restoreLocale(FilterUtil.ADMIN_USERNAME));
	}

	private void resetToDefaultLocale(String locale) {
		try (Connection conn = DatabaseUpdater.getConnection()) {
			Integer userId = getUserIdByName(FilterUtil.ADMIN_USERNAME, conn);
			if (userId != null) {
				try (PreparedStatement deleteStmt = conn.prepareStatement("DELETE FROM user_property WHERE user_id = ? AND property = 'defaultLocale'")) {
					deleteStmt.setInt(1, userId);
					deleteStmt.executeUpdate();
				}
				try (PreparedStatement insertStmt = conn.prepareStatement("INSERT INTO user_property (user_id, property, property_value) VALUES (?, 'defaultLocale', ?)")) {
					insertStmt.setInt(1, userId);
					insertStmt.setString(2, locale);
					insertStmt.executeUpdate();
				}
			}

			try (PreparedStatement globalStmt = conn.prepareStatement("UPDATE global_property SET property_value = ? WHERE property = 'default_locale'")) {
				globalStmt.setString(1, locale);
				globalStmt.executeUpdate();
			}
		} catch (Exception e) {
			throw new RuntimeException("Failed to reset locale for test setup", e);
		}
	}
}

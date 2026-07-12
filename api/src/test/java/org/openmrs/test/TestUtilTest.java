/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.test;
package org.openmrs.test;

import java.util.Properties;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.openmrs.util.OpenmrsUtil;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;

class TestUtilTest {

	@Test
	void getRuntimeProperties_shouldUseApplicationRuntimePropertiesLoader() {
		Properties expected = new Properties();

		try (MockedStatic<OpenmrsUtil> openmrsUtilMock = mockStatic(OpenmrsUtil.class)) {
			openmrsUtilMock.when(() -> OpenmrsUtil.getRuntimeProperties("testapp")).thenReturn(expected);

			assertSame(expected, TestUtil.getRuntimeProperties("testapp"));
		}
	}

	@Test
	void getRuntimeProperties_shouldReturnEmptyPropertiesWhenNoneAreFound() {
		try (MockedStatic<OpenmrsUtil> openmrsUtilMock = mockStatic(OpenmrsUtil.class)) {
			openmrsUtilMock.when(() -> OpenmrsUtil.getRuntimeProperties("testapp")).thenReturn(null);

			assertTrue(TestUtil.getRuntimeProperties("testapp").isEmpty());
		}
	}
}

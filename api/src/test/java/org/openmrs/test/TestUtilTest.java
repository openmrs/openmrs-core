
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

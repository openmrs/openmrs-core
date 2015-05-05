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

import org.junit.Before;
import org.junit.Test;

import org.junit.runner.RunWith;
import org.openmrs.api.APIException;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.core.JdkVersion;

import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(JdkVersion.class)
public class JavaVersionTest {
	
	@Before
	public void setup() {
		PowerMockito.mockStatic(JdkVersion.class);
	}
	
	/**
	 * @see {@link org.openmrs.util.OpenmrsUtil#validateJavaVersion()}
	 */
	@Test(expected = APIException.class)
	public void validateJavaVersion_shouldFailIfTheCurrentJVMVersionIsEarlierThanJava6() {
		when(JdkVersion.getJavaVersion()).thenReturn("1.5.0_20");
		OpenmrsUtil.validateJavaVersion();
	}
	
	/**
	 * @see {@link org.openmrs.util.OpenmrsUtil#validateJavaVersion()}
	 */
	@Test
	public void validateJavaVersion_shouldPassIfTheCurrentJVMVersionIsLaterThanJava5() {
		when(JdkVersion.getJavaVersion()).thenReturn("1.8.0_25");
		OpenmrsUtil.validateJavaVersion();
	}
}

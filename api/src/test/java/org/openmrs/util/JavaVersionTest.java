/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
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

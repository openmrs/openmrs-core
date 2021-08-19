/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.util.databasechange;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;

import liquibase.resource.InputStreamList;
import org.junit.jupiter.api.Test;
import org.openmrs.util.ClassLoaderFileOpener;

public class ClassLoaderFileOpenerTest {
	
	@Test
	public void shouldGetSingleResourceAsStream() throws IOException {
		ClassLoader classLoader = mock(ClassLoader.class);

		when(classLoader.getResource(any()))
			.thenReturn(getClass().getClassLoader().getResource("TestingApplicationContext.xml"));

		ClassLoaderFileOpener classLoaderFileOpener = new ClassLoaderFileOpener(classLoader);
		InputStreamList inputStreamSet = classLoaderFileOpener.openStreams(null, "some path");
		
		assertEquals(1, inputStreamSet.size());
	}
	
	@Test
	public void shouldGetNoResourceAsStream() throws IOException {
		ClassLoader classLoader = mock(ClassLoader.class);
		
		ClassLoaderFileOpener classLoaderFileOpener = new ClassLoaderFileOpener(classLoader);
		InputStreamList inputStreamSet = classLoaderFileOpener.openStreams(null, "");
		
		assertEquals(0, inputStreamSet.size());
	}
	
	@Test
	public void shouldIndicateThatListIsNotSupported() throws IOException {
		ClassLoader classLoader = mock(ClassLoader.class);
		
		ClassLoaderFileOpener classLoaderFileOpener = new ClassLoaderFileOpener(classLoader);
		assertThrows(UnsupportedOperationException.class, () -> classLoaderFileOpener.list("", "", false, false, false));
	}
}

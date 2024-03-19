/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.liquibase;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Collections;

import liquibase.resource.InputStreamList;
import org.junit.jupiter.api.Test;
import org.openmrs.liquibase.OpenmrsClassLoaderResourceAccessor;
import org.openmrs.util.OpenmrsClassLoader;

public class OpenmrsClassLoaderResourceAccessorTest {
	
	@Test
	public void shouldGetSingleResourceAsStream() throws Exception {
		ClassLoader classLoader = mock(ClassLoader.class);

		when(classLoader.getResources(any()))
			.thenReturn(OpenmrsClassLoader.getSystemClassLoader().getResources("TestingApplicationContext.xml"));

		
		OpenmrsClassLoaderResourceAccessor classLoaderFileOpener = new OpenmrsClassLoaderResourceAccessor(classLoader);
        try (InputStreamList inputStreamSet = classLoaderFileOpener.openStreams(null, "some path")) {
            assertEquals(1, inputStreamSet.size());
        }
	}
	
	@Test
	public void shouldGetNoResourceAsStream() throws Exception {
		ClassLoader classLoader = mock(ClassLoader.class);

		when(classLoader.getResources(any()))
			.thenReturn(Collections.emptyEnumeration());
		
		
		try (OpenmrsClassLoaderResourceAccessor classLoaderFileOpener = new OpenmrsClassLoaderResourceAccessor(classLoader);
			 InputStreamList inputStreamSet = classLoaderFileOpener.openStreams(null, "")){
			assertThat(inputStreamSet.size(), is(0));
		}
	}
}

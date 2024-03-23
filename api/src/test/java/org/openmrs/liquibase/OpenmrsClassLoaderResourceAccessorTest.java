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
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import liquibase.resource.Resource;
import org.junit.jupiter.api.Test;
import org.openmrs.util.OpenmrsClassLoader;

public class OpenmrsClassLoaderResourceAccessorTest {
	
	@Test
	public void shouldGetSingleResourceAsStream() throws Exception {
		ClassLoader classLoader = mock(ClassLoader.class);

		when(classLoader.getResources(any()))
			.thenReturn(OpenmrsClassLoader.getSystemClassLoader().getResources("TestingApplicationContext.xml"));
		
		OpenmrsClassLoaderResourceAccessor classLoaderFileOpener2 = new OpenmrsClassLoaderResourceAccessor(classLoader);
		List<Resource> resources = classLoaderFileOpener2.getAll("some path");
		Set<InputStream> inputStreamSet = new HashSet<>();
		for (Resource resource : resources) {
			InputStream in = resource.openInputStream();
			BufferedInputStream bufferedIn = new BufferedInputStream(in);
			inputStreamSet.add(bufferedIn);
		}
		assertEquals(1, inputStreamSet.size());
	}
	
	@Test
	public void shouldGetNoResourceAsStream() throws Exception {
		ClassLoader classLoader = mock(ClassLoader.class);

		when(classLoader.getResources(any()))
			.thenReturn(Collections.emptyEnumeration());
		
		OpenmrsClassLoaderResourceAccessor classLoaderFileOpener2 = new OpenmrsClassLoaderResourceAccessor(classLoader);
		List<Resource> resources = classLoaderFileOpener2.getAll("");
		if (resources != null) {
			Set<InputStream> inputStreamSet = new HashSet<>();
			for (Resource resource : resources) {
				if (resource != null) {
					InputStream in = resource.openInputStream();
					BufferedInputStream bufferedIn = new BufferedInputStream(in);
					inputStreamSet.add(bufferedIn);
				}
			}
			assertThat(inputStreamSet.size(), is(0));
		}
	}
}

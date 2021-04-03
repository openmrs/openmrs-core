/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.filter.startuperror;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.web.Listener;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Tests {@link StartupErrorFilter}.
 */
public class StartupErrorFilterTest {

	private StartupErrorFilter filter;
	
	@BeforeEach
	public void setUp() {
		filter = new StartupErrorFilter();
	}
	
	@AfterEach
	public void reverterrorAtStartup() { 
		Throwable errorAtStartup = null;
		ReflectionTestUtils.setField(Listener.class, "errorAtStartup", errorAtStartup);
	}
	
	@Test
	public void getModel_shouldReturnAStartupErrorFilterModelContainingTheStartupError() {
		
		Exception e = new Exception();
		ReflectionTestUtils.setField(Listener.class, "errorAtStartup", e);
		
		
		StartupErrorFilterModel model = filter.getUpdateFilterModel();
		
		assertThat(model.errorAtStartup, is(e));
	}
	
	@Test
	public void skipFilter_shouldReturnTrueIfNoErrorHasOccuredOnStartup() {
		
		
		
		assertTrue(filter.skipFilter(new MockHttpServletRequest()), "should be true on start without error");
	}
	
	@Test
	public void skipFilter_shouldReturnFalseIfAnErrorHasOccuredOnStartup() {
		Exception e = new Exception();
		ReflectionTestUtils.setField(Listener.class, "errorAtStartup", e);
		
		
		assertFalse(filter.skipFilter(new MockHttpServletRequest()), "should be false on start with error");
	}
}

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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openmrs.web.Listener;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.mock.web.MockHttpServletRequest;

/**
 * Tests {@link StartupErrorFilter}.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(Listener.class)
public class StartupErrorFilterTest {
	
	@Before
	public void setUp() {
		mockStatic(Listener.class);
	}
	
	@Test
	public void getModel_shouldReturnAStartupErrorFilterModelContainingTheStartupError() {
		
		Throwable t = mock(Throwable.class);
		when(Listener.getErrorAtStartup()).thenReturn(t);
		
		StartupErrorFilter filter = new StartupErrorFilter();
		
		StartupErrorFilterModel model = filter.getModel();
		
		assertThat(model.errorAtStartup, is(t));
	}
	
	@Test
	public void skipFilter_shouldReturnTrueIfNoErrorHasOccuredOnStartup() {
		
		when(Listener.errorOccurredAtStartup()).thenReturn(false);
		
		StartupErrorFilter filter = new StartupErrorFilter();
		
		assertTrue("should be true on start without error", filter.skipFilter(new MockHttpServletRequest()));
	}
	
	@Test
	public void skipFilter_shouldReturnFalseIfAnErrorHasOccuredOnStartup() {
		
		when(Listener.errorOccurredAtStartup()).thenReturn(true);
		
		StartupErrorFilter filter = new StartupErrorFilter();
		
		assertFalse("should be false on start with error", filter.skipFilter(new MockHttpServletRequest()));
	}
}

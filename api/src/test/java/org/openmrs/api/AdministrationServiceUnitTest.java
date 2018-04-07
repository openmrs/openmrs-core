/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.api.db.AdministrationDAO;
import org.openmrs.api.impl.AdministrationServiceImpl;

/**
 * Unit tests for {@link AdministrationService}.
 */
public class AdministrationServiceUnitTest {
	
	private AdministrationDAO adminDAO;
	
	private EventListeners eventListeners;
	
	private AdministrationService adminService;
	
	@Before
	public void setUp() {
		
		adminService = new AdministrationServiceImpl();
		adminDAO = mock(AdministrationDAO.class);
		adminService.setAdministrationDAO(adminDAO);
		eventListeners = mock(EventListeners.class);
		((AdministrationServiceImpl) adminService).setEventListeners(eventListeners);
	}
	
	@Test
	public void executeSQL_shouldReturnNullGivenNull() {
		
		adminService.executeSQL(null, true);
		
		verify(adminDAO, never()).executeSQL(anyString(), anyBoolean());
	}
	
	@Test
	public void executeSQL_shouldReturnNullGivenEmptyString() {
		
		adminService.executeSQL(" ", true);
		
		verify(adminDAO, never()).executeSQL(anyString(), anyBoolean());
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void getGlobalPropertyValue_shouldFailIfDefaultValueIsNull() {

		adminService.getGlobalPropertyValue("valid.double", null);
	}

	@Test
	public void addGlobalPropertyListener_shouldAddListener() {

		List<GlobalPropertyListener> listeners = new ArrayList<>();
		when(eventListeners.getGlobalPropertyListeners()).thenReturn(listeners);

		GlobalPropertyListener listener = mock(GlobalPropertyListener.class);

		adminService.addGlobalPropertyListener(listener);

		assertThat(listeners.size(), is(1));
		assertThat(listeners, contains(listener));
	}

	@Test
	public void removeGlobalPropertyListener_shouldRemoveListener() {

		List<GlobalPropertyListener> listeners = new ArrayList<>();
		when(eventListeners.getGlobalPropertyListeners()).thenReturn(listeners);

		GlobalPropertyListener listener = mock(GlobalPropertyListener.class);
		adminService.addGlobalPropertyListener(listener);
		assertThat(listeners.size(), is(1));
		assertThat(listeners, contains(listener));
		
		adminService.removeGlobalPropertyListener(listener);

		assertThat(listeners.size(), is(0));
	}
}

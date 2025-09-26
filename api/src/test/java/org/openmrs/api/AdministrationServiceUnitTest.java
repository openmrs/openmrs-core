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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.ConceptService;
import org.openmrs.api.SerializationService;
import org.openmrs.api.db.AdministrationDAO;
import org.openmrs.api.impl.AdministrationServiceImpl;
import org.openmrs.messagesource.MessageSourceService;

/**
 * Unit tests for {@link AdministrationService}.
 */
@ExtendWith(MockitoExtension.class)
public class AdministrationServiceUnitTest {
	
	@Mock
	private AdministrationDAO adminDAO;
	
	@Mock
	private EventListeners eventListeners;
	
	@Mock
	private MessageSourceService messageSourceService;
	
	@Mock
	private SerializationService serializationService;
	
	@Mock
	private ConceptService conceptService;
	
	private AdministrationServiceImpl adminService;
	
	@BeforeEach
	public void setUp() {
        // Use the constructor injection with DAO
        adminService = new AdministrationServiceImpl(adminDAO, messageSourceService, serializationService, conceptService);
        adminService.setEventListeners(eventListeners);
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
	
	@Test
	public void getGlobalPropertyValue_shouldFailIfDefaultValueIsNull() {

		assertThrows(IllegalArgumentException.class, () -> adminService.getGlobalPropertyValue("valid.double", null));
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

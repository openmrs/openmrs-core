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

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.api.db.AdministrationDAO;
import org.openmrs.api.impl.AdministrationServiceImpl;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.SimpleTransactionStatus;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link AdministrationService}.
 */
public class AdministrationServiceUnitTest {

	private AdministrationDAO adminDAO;

	private EventListeners eventListeners;

	private AdministrationService adminService;

	@BeforeEach
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

	/**
	 * Records the definition of the transaction the select-only path opens, without a database.
	 */
	private static class RecordingTransactionManager implements PlatformTransactionManager {

		private TransactionDefinition definition;

		@Override
		public TransactionStatus getTransaction(TransactionDefinition definition) {
			this.definition = definition;
			return new SimpleTransactionStatus();
		}

		@Override
		public void commit(TransactionStatus status) {
		}

		@Override
		public void rollback(TransactionStatus status) {
		}
	}

	@Test
	public void executeSQL_withSelectOnly_shouldRunTheDaoCallInAReadOnlyTransaction() {

		RecordingTransactionManager manager = new RecordingTransactionManager();
		((AdministrationServiceImpl) adminService).setTransactionManager(manager);
		when(adminDAO.executeSQL("SELECT 1", true)).thenReturn(new ArrayList<>());

		adminService.executeSQL("SELECT 1", true);

		assertNotNull(manager.definition);
		assertEquals(TransactionDefinition.PROPAGATION_REQUIRES_NEW, manager.definition.getPropagationBehavior(),
		    "a read-only scope has to exist outside of whatever writable transaction a caller holds");
		assertTrue(manager.definition.isReadOnly());
		verify(adminDAO).executeSQL("SELECT 1", true);
	}

	@Test
	public void executeSQL_withSelectOnlyFalse_shouldNotOpenATransaction() {

		RecordingTransactionManager manager = new RecordingTransactionManager();
		((AdministrationServiceImpl) adminService).setTransactionManager(manager);

		adminService.executeSQL("UPDATE t SET id = 1", false);

		assertNull(manager.definition);
		verify(adminDAO).executeSQL("UPDATE t SET id = 1", false);
	}

	@Test
	public void executeSQL_withSelectOnly_shouldDelegateDirectlyWhenNoManagerIsSet() {

		assertDoesNotThrow(() -> adminService.executeSQL("SELECT 1", true));

		verify(adminDAO).executeSQL("SELECT 1", true);
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

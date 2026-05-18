/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.logging;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsUtil;
import org.openmrs.util.PrivilegeConstants;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link OpenmrsPropertyLookup}.
 * <p/>
 * This class handles two distinct operational phases:
 * <ul>
 * <li>Normal operations: Context and AdministrationService are available</li>
 * <li>Startup / initialization: Context is not yet available, returns hardcoded defaults</li>
 * </ul>
 */
class OpenmrsPropertyLookupTest {

	private OpenmrsPropertyLookup lookup;

	private MockedStatic<Context> contextMock;

	private MockedStatic<OpenmrsUtil> openmrsUtilMock;

	@BeforeEach
	void setUp() {
		lookup = new OpenmrsPropertyLookup();
		contextMock = mockStatic(Context.class);
		openmrsUtilMock = mockStatic(OpenmrsUtil.class);
	}

	@AfterEach
	void tearDown() {
		contextMock.close();
		openmrsUtilMock.close();
	}

	// --- applicationDirectory ---

	@Test
	void lookup_shouldReturnApplicationDirectoryWhenSet() {
		openmrsUtilMock.when(OpenmrsUtil::getApplicationDataDirectory).thenReturn("/opt/openmrs");

		String result = lookup.lookup(null, "applicationDirectory");

		assertThat(result, equalTo("/opt/openmrs"));
	}

	@Test
	void lookup_shouldReturnNullWhenApplicationDirectoryIsEmpty() {
		openmrsUtilMock.when(OpenmrsUtil::getApplicationDataDirectory).thenReturn("");

		String result = lookup.lookup(null, "applicationDirectory");

		assertThat(result, nullValue());
	}

	@Test
	void lookup_shouldReturnNullWhenApplicationDirectoryIsNull() {
		openmrsUtilMock.when(OpenmrsUtil::getApplicationDataDirectory).thenReturn(null);

		String result = lookup.lookup(null, "applicationDirectory");

		assertThat(result, nullValue());
	}

	// --- logLocation during startup (no AdministrationService) ---

	@Test
	void lookup_shouldReturnEmptyForLogLocationDuringStartup() {
		contextMock.when(Context::getAdministrationService)
		        .thenThrow(new org.openmrs.api.ServiceNotFoundException(AdministrationService.class));

		String result = lookup.lookup(null, "logLocation");

		assertThat(result, equalTo(""));
	}

	// --- logLocation during normal operations ---

	@Test
	void lookup_shouldReturnLogLocationFromGlobalProperty() {
		AdministrationService adminService = mock(AdministrationService.class);
		contextMock.when(Context::getAdministrationService).thenReturn(adminService);
		when(adminService.getGlobalProperty(OpenmrsConstants.GP_LOG_LOCATION)).thenReturn("/var/log/openmrs");

		String result = lookup.lookup(null, "logLocation");

		assertThat(result, equalTo("/var/log/openmrs"));
	}

	@Test
	void lookup_shouldStripTrailingSlashFromLogLocation() {
		AdministrationService adminService = mock(AdministrationService.class);
		contextMock.when(Context::getAdministrationService).thenReturn(adminService);
		when(adminService.getGlobalProperty(OpenmrsConstants.GP_LOG_LOCATION)).thenReturn("/var/log/openmrs/");

		String result = lookup.lookup(null, "logLocation");

		assertThat(result, equalTo("/var/log/openmrs"));
	}

	@Test
	void lookup_shouldReturnEmptyForNullLogLocation() {
		AdministrationService adminService = mock(AdministrationService.class);
		contextMock.when(Context::getAdministrationService).thenReturn(adminService);
		when(adminService.getGlobalProperty(OpenmrsConstants.GP_LOG_LOCATION)).thenReturn(null);

		String result = lookup.lookup(null, "logLocation");

		assertThat(result, equalTo(""));
	}

	@Test
	void lookup_shouldReturnEmptyForBlankLogLocation() {
		AdministrationService adminService = mock(AdministrationService.class);
		contextMock.when(Context::getAdministrationService).thenReturn(adminService);
		when(adminService.getGlobalProperty(OpenmrsConstants.GP_LOG_LOCATION)).thenReturn("   ");

		String result = lookup.lookup(null, "logLocation");

		assertThat(result, equalTo(""));
	}

	// --- logLayout during startup (no AdministrationService) ---

	@Test
	void lookup_shouldReturnDefaultLayoutDuringStartup() {
		contextMock.when(Context::getAdministrationService)
		        .thenThrow(new org.openmrs.api.ServiceNotFoundException(AdministrationService.class));

		String result = lookup.lookup(null, "logLayout");

		assertThat(result, equalTo("%p - %C{1}.%M(%L) |%d{ISO8601}| %m%n"));
	}

	// --- logLayout during normal operations ---

	@Test
	void lookup_shouldReturnLogLayoutFromGlobalProperty() {
		AdministrationService adminService = mock(AdministrationService.class);
		contextMock.when(Context::getAdministrationService).thenReturn(adminService);
		when(adminService.getGlobalProperty(OpenmrsConstants.GP_LOG_LAYOUT)).thenReturn("%d %m%n");

		String result = lookup.lookup(null, "logLayout");

		assertThat(result, equalTo("%d %m%n"));
	}

	@Test
	void lookup_shouldReturnDefaultLayoutWhenGlobalPropertyIsNull() {
		AdministrationService adminService = mock(AdministrationService.class);
		contextMock.when(Context::getAdministrationService).thenReturn(adminService);
		when(adminService.getGlobalProperty(OpenmrsConstants.GP_LOG_LAYOUT)).thenReturn(null);

		String result = lookup.lookup(null, "logLayout");

		assertThat(result, equalTo("%p - %C{1}.%M(%L) |%d{ISO8601}| %m%n"));
	}

	@Test
	void lookup_shouldReturnDefaultLayoutWhenGlobalPropertyIsBlank() {
		AdministrationService adminService = mock(AdministrationService.class);
		contextMock.when(Context::getAdministrationService).thenReturn(adminService);
		when(adminService.getGlobalProperty(OpenmrsConstants.GP_LOG_LAYOUT)).thenReturn("   ");

		String result = lookup.lookup(null, "logLayout");

		assertThat(result, equalTo("%p - %C{1}.%M(%L) |%d{ISO8601}| %m%n"));
	}

	// --- unknown key ---

	@Test
	void lookup_shouldThrowForUnknownKey() {
		IllegalArgumentException ex = org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
		    () -> lookup.lookup(null, "unknownKey"));

		assertThat(ex.getMessage(), containsString("unknownKey"));
	}

	// --- privilege management ---

	@Test
	void lookup_shouldAddAndRemoveProxyPrivilegeWhenFetchingGlobalProperty() {
		AdministrationService adminService = mock(AdministrationService.class);
		contextMock.when(Context::getAdministrationService).thenReturn(adminService);
		when(adminService.getGlobalProperty(OpenmrsConstants.GP_LOG_LAYOUT)).thenReturn("%m%n");

		lookup.lookup(null, "logLayout");

		contextMock.verify(() -> Context.addProxyPrivilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES));
		contextMock.verify(() -> Context.removeProxyPrivilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES));
	}
}

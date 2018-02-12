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

import org.junit.Before;
import org.junit.Test;
import org.openmrs.api.db.AdministrationDAO;
import org.openmrs.api.impl.AdministrationServiceImpl;

import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for {@link AdministrationService}.
 */
public class AdministrationServiceUnitTest {
	
	private AdministrationDAO adminDAO;
	
	private AdministrationService adminService;
	
	@Before
	public void setUp() {
		
		adminService = new AdministrationServiceImpl();
		adminDAO = mock(AdministrationDAO.class);
		adminService.setAdministrationDAO(adminDAO);
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
	
}

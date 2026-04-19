/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.Provider;
import org.openmrs.api.APIException;
import org.openmrs.api.db.ProviderDAO;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for methods specific to {@link ProviderServiceImpl}.
 *
 * @see org.openmrs.api.ProviderServiceTest for integration tests
 */
public class ProviderServiceImplTest {

	private ProviderServiceImpl service;

	private ProviderDAO dao;

	@BeforeEach
	public void setup() {
		service = new ProviderServiceImpl();
		dao = mock(ProviderDAO.class);
		service.setProviderDAO(dao);
	}

	@Test
	public void getProviderByUuid_shouldReturnProvider_whenProviderExists() {
		String uuid = "ba4781f4-6b94-11e0-93c3-18a905e044dc";
		Provider expected = new Provider();

		when(dao.getProviderByUuid(uuid)).thenReturn(expected);

		Provider result = service.getProviderByUuid(uuid);

		assertNotNull(result);
		assertEquals(expected, result);
	}

	@Test
	public void getProviderByUuid_shouldThrowAPIException_whenProviderNotFound() {
		String uuid = "non-existent-uuid";

		when(dao.getProviderByUuid(uuid)).thenReturn(null);

		assertThrows(APIException.class, () -> service.getProviderByUuid(uuid));
	}
}

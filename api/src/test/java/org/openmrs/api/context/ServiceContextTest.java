/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.context;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.openMocks;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.api.APIException;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.openmrs.util.DatabaseUpdateException;
import org.openmrs.util.InputRequiredException;

public class ServiceContextTest extends BaseContextSensitiveTest {
	
	private ServiceContext serviceContext;
	
	private ServiceContext spiedServiceContext;
	
	private boolean isUseSystemClassLoader;
	
	@BeforeEach
	public void setUp() throws InputRequiredException, DatabaseUpdateException {
		openMocks(this);
		serviceContext = Context.getServiceContext();
		spiedServiceContext = spy(serviceContext);
		isUseSystemClassLoader = serviceContext.isUseSystemClassLoader();
	}
	
	@AfterEach
	public void tearDown() {
		serviceContext.setUseSystemClassLoader(isUseSystemClassLoader);
	}
	
	@Test
	public void getModuleOpenmrsServices_shouldRaiseApiExceptionWithNonExistentClass() {
		List<Object> params = new ArrayList<>();
		params.add("org.openmrs.module.webservices.rest.nonexistent.InvalidClass");
		params.add(Object.class);
		
		spiedServiceContext.setUseSystemClassLoader(false);
		APIException thrownException = assertThrows(APIException.class, () -> spiedServiceContext.setModuleService(params));
		
		assertNotNull(thrownException.getMessage());
		assertNotNull(thrownException.getCause());
		
		verify(spiedServiceContext, never()).getMessageService();
		verify(spiedServiceContext, never()).getMessageSourceService();
	}
	
	@Test
	public void getModuleOpenmrsServices_shouldRaiseApiExceptionWithNullClass() {
		List<Object> params = new ArrayList<>();
		params.add(null);
		params.add(Object.class);
		
		spiedServiceContext.setUseSystemClassLoader(false);
		APIException thrownException = assertThrows(APIException.class, () -> spiedServiceContext.setModuleService(params));
		
		assertNotNull(thrownException.getMessage());
		assertNull(thrownException.getCause());
		
		verify(spiedServiceContext, never()).getMessageService();
		verify(spiedServiceContext, never()).getMessageSourceService();
	}
	
	@Test
	public void getModuleOpenmrsServices_shouldRaiseApiExceptionWithNullClassInstance() {
		List<Object> params = new ArrayList<>();
		params.add("org.openmrs.module.webservices.rest.nonexistent.InvalidClass");
		params.add(null);
		
		spiedServiceContext.setUseSystemClassLoader(false);
		APIException thrownException = assertThrows(APIException.class, () -> spiedServiceContext.setModuleService(params));
		
		assertNotNull(thrownException.getMessage());
		assertNull(thrownException.getCause());
		
		verify(spiedServiceContext, never()).getMessageService();
		verify(spiedServiceContext, never()).getMessageSourceService();
	}
}

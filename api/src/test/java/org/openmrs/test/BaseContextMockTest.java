/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.test;

import org.junit.After;
import org.junit.Before;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openmrs.api.context.ContextMockHelper;
import org.openmrs.api.context.UserContext;
import org.openmrs.module.ModuleUtilTest;

/**
 * Tests extending this class have a mocked authenticated UserContext. In addition you can mock
 * Context.get...Service() calls by annotating fields with {@link Mock}.
 * 
 * @see ModuleUtilTest
 * @since 1.11, 1.10, 1.9.9
 */
public abstract class BaseContextMockTest {
	
	@Mock
	protected UserContext userContext;
	
	@InjectMocks
	protected ContextMockHelper contextMockHelper;
	
	/**
	 * Initializes fields annotated with {@link Mock}. Sets userContext and authenticatedUser.
	 */
	@Before
	public void initMocks() {
		MockitoAnnotations.initMocks(this);
	}
	
	@After
	public void revertContextMocks() {
		contextMockHelper.revertMocks();
	}
}

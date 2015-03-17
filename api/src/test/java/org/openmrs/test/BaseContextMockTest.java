/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
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

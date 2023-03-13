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
 * @deprecated as of 2.4
 * <p>openmrs-core migrated its tests from JUnit 4 to JUnit 5.
 * JUnit 4 helpers are still supported so module developers can gradually migrate tests from JUnit 4 to JUnit 5.
 * To migrate your tests follow <a href="https://wiki.openmrs.org/display/docs/How+to+migrate+to+JUnit+5">How to migrate to JUnit 5</a>.
 * The JUnit 5 version of the class is {@link org.openmrs.test.jupiter.BaseContextMockTest}.<p>
 */
@Deprecated
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

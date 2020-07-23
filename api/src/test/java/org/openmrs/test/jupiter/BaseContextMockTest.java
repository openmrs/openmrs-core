/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.test.jupiter;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openmrs.api.context.ContextMockHelper;
import org.openmrs.api.context.UserContext;

/**
 * Tests extending this class have a mocked authenticated UserContext. In addition you can mock
 * Context.get...Service() calls by annotating fields with {@link Mock}.
 * 
 * Use this class for Junit 5 tests.
 * 
 * @since 2.4.0
 */
@ExtendWith(MockitoExtension.class)
public abstract class BaseContextMockTest {
	
	@Mock
	protected UserContext userContext;
	
	@InjectMocks
	protected ContextMockHelper contextMockHelper;
	
	@AfterEach
	public void revertContextMocks() {
		contextMockHelper.revertMocks();
	}
}

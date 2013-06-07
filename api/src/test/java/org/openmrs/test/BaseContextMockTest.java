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
import org.mockito.MockitoAnnotations;
import org.openmrs.api.context.ContextMockHelper;

/**
 * Tests extending this class may have mocked Context.get...Service() calls.
 */
public abstract class BaseContextMockTest {
	
	@InjectMocks
	protected ContextMockHelper contextMockHelper;
	
	@Before
	public void initContextMockHelper() {
		MockitoAnnotations.initMocks(this);
	}
	
	@After
	public void revertContextMockHelper() {
		contextMockHelper.revertMocks();
	}
}

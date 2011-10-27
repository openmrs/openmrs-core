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
package org.openmrs.web.filter.update.util;

import junit.framework.Assert;

import org.junit.Ignore;
import org.junit.Test;
import org.openmrs.test.Verifies;
import org.openmrs.web.filter.util.FilterUtil;
import org.openmrs.web.test.BaseWebContextSensitiveTest;

/**
 * Tests some of the methods on the {@link FilterUtil}
 */
public class FilterUtilTest extends BaseWebContextSensitiveTest {
	
	/**
	 * @see {@link FilterUtil#storeLocale(String))}
	 */
	@Test
	@Ignore
	@Verifies(value = "should store locale", method = "storeLocale(String)")
	public void storeLocale_shouldStoreLocale() throws Exception {
		FilterUtil.storeLocale("it");
	}
	
	/**
	 * @see {@link {@link FilterUtil#restoreLocale(String))}
	 */
	@Test
	@Ignore
	@Verifies(value = "should restore locale", method = "restoreLocale(String)")
	public void storeLocale_shouldRestoreLocale() throws Exception {
		FilterUtil.storeLocale("it");
		Assert.assertEquals("it", FilterUtil.restoreLocale(FilterUtil.ADMIN_USERNAME));
	}
	
}

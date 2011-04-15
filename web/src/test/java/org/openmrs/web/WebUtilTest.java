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
package org.openmrs.web;

import java.util.Locale;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests methods on the {@link WebUtil} class.
 */
public class WebUtilTest {
	
	/**
	 * @see WebUtil#normalizeLocale(String)
	 * @verifies ignore leading spaces
	 */
	@Test
	public void normalizeLocale_shouldIgnoreLeadingSpaces() throws Exception {
		Assert.assertEquals(Locale.ITALIAN, WebUtil.normalizeLocale(" it"));
	}
	
	/**
	 * @see WebUtil#normalizeLocale(String)
	 * @verifies accept language only locales
	 */
	@Test
	public void normalizeLocale_shouldAcceptLanguageOnlyLocales() throws Exception {
		Assert.assertEquals(Locale.FRENCH, WebUtil.normalizeLocale("fr"));
	}
	
	/**
	 * @see WebUtil#normalizeLocale(String)
	 * @verifies not accept invalid locales
	 */
	@Test
	public void normalizeLocale_shouldNotAcceptInvalidLocales() throws Exception {
		Assert.assertNull(WebUtil.normalizeLocale("ptrg"));
		Assert.assertNull(WebUtil.normalizeLocale("usa"));
	}
	
	/**
	 * @see WebUtil#normalizeLocale(String)
	 * @verifies not fail with empty strings
	 */
	@Test
	public void normalizeLocale_shouldNotFailWithEmptyStrings() throws Exception {
		Assert.assertNull(WebUtil.normalizeLocale(""));
	}
	
	/**
	 * @see WebUtil#normalizeLocale(String)
	 * @verifies not fail with whitespace only
	 */
	@Test
	public void normalizeLocale_shouldNotFailWithWhitespaceOnly() throws Exception {
		Assert.assertNull(WebUtil.normalizeLocale("      "));
	}
	
	/**
	 * @see WebUtil#sanitizeLocales(String)
	 * @verifies skip over invalid locales
	 */
	@Test
	public void sanitizeLocales_shouldSkipOverInvalidLocales() throws Exception {
		Assert.assertEquals("fr_RW, it, en", WebUtil.sanitizeLocales("és, qqq, fr_RW, it, enñ"));
	}
	
	/**
	 * @see WebUtil#sanitizeLocales(String)
	 * @verifies not fail with empty string
	 */
	@Test
	public void sanitizeLocales_shouldNotFailWithEmptyString() throws Exception {
		Assert.assertNull(null, WebUtil.sanitizeLocales(""));
	}
}

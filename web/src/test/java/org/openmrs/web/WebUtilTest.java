/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web;

import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Locale;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.BaseOpenmrsObject;

/**
 * Tests methods on the {@link WebUtil} class.
 */
public class WebUtilTest {

	/**
	 * @see org.openmrs.web.WebUtil#getContextPath()
	 */
	@Test
	public void getContextPath_shouldReturnEmptyStringWhenWebAppNameIsNull() {
		WebConstants.WEBAPP_NAME = null;
		Assert.assertEquals("", WebUtil.getContextPath());
	}
	
	/**
	 * @see org.openmrs.web.WebUtil#getContextPath()
	 */
	@Test
	public void getContextPath_shouldReturnEmptyStringWhenWebAppNameIsEmptyString() {
		WebConstants.WEBAPP_NAME = "";
		Assert.assertEquals("", WebUtil.getContextPath());
	}
	
	/**
	 * @see org.openmrs.web.WebUtil#getContextPath()
	 */
	@Test
	public void getContextPath_shouldReturnValueSpecifiedInWebAppName() {
		WebConstants.WEBAPP_NAME = "Value";
		Assert.assertEquals("/Value", WebUtil.getContextPath());
	}
	
	/**
	 * @see WebUtil#normalizeLocale(String)
	 */
	@Test
	public void normalizeLocale_shouldIgnoreLeadingSpaces() {
		Assert.assertEquals(Locale.ITALIAN, WebUtil.normalizeLocale(" it"));
	}
	
	/**
	 * @see WebUtil#normalizeLocale(String)
	 */
	@Test
	public void normalizeLocale_shouldAcceptLanguageOnlyLocales() {
		Assert.assertEquals(Locale.FRENCH, WebUtil.normalizeLocale("fr"));
	}
	
	/**
	 * @see WebUtil#normalizeLocale(String)
	 */
	@Test
	public void normalizeLocale_shouldNotAcceptInvalidLocales() {
		Assert.assertNull(WebUtil.normalizeLocale("ptrg"));
		Assert.assertNull(WebUtil.normalizeLocale("usaa"));
	}
	
	/**
	 * @see WebUtil#normalizeLocale(String)
	 */
	@Test
	public void normalizeLocale_shouldNotFailWithEmptyStrings() {
		Assert.assertNull(WebUtil.normalizeLocale(""));
	}
	
	/**
	 * @see WebUtil#normalizeLocale(String)
	 */
	@Test
	public void normalizeLocale_shouldNotFailWithWhitespaceOnly() {
		Assert.assertNull(WebUtil.normalizeLocale("      "));
	}

	/**
	 * @throws UnsupportedEncodingException
	 * @see WebUtil#normalizeLocale(String)
	 */
	@Test
	public void normalizeLocale_shouldNotFailWithTab() throws UnsupportedEncodingException {
		String s = new String(new byte[]{0x9}, "ASCII");
		Assert.assertNull(WebUtil.normalizeLocale(s));
	}

	/**
	 * @see WebUtil#normalizeLocale(String)
	 */
	@Test
	public void normalizeLocale_shouldNotFailWithUnicode() {
		Assert.assertNull(WebUtil.normalizeLocale("Ši"));
	}

	/**
	 * @see WebUtil#normalizeLocale(String)
	 */
	@Test
	public void normalizeLocale_shouldNotFailWithSingleChar() {
		Assert.assertNull(WebUtil.normalizeLocale("s"));
	}

	/**
	 * @throws UnsupportedEncodingException
	 * @see WebUtil#normalizeLocale(String)
	 */
	@Test
	public void normalizeLocale_shouldNotFailWithUnderline() throws UnsupportedEncodingException {
		String s = new String(new byte[]{0x5f}, "ASCII");
		Assert.assertNull(WebUtil.normalizeLocale(s));
	}

	/**
	 * @see WebUtil#sanitizeLocales(String)
	 */
	@Test
	public void sanitizeLocales_shouldSkipOverInvalidLocales() {
		Assert.assertEquals("fr_RW, it, en", WebUtil.sanitizeLocales("és, qqqq, fr_RW, it, enñ"));
	}
	
	/**
	 * @see WebUtil#sanitizeLocales(String)
	 */
	@Test
	public void sanitizeLocales_shouldNotFailWithEmptyString() {
		Assert.assertNull(null, WebUtil.sanitizeLocales(""));
	}
	
	/**
	 * Utility method to check if a list contains a BaseOpenmrsObject using the id
	 * @param list
	 * @param id
	 * @return true if list contains object with the id else false
	 */
	public static boolean containsId(Collection<? extends BaseOpenmrsObject> list, Integer id) {
		for (BaseOpenmrsObject baseOpenmrsObject : list) {
			if (baseOpenmrsObject.getId().equals(id)) {
				return true;
			}
		}
		return false;
	}
}

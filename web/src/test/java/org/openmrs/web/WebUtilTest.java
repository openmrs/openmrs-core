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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Locale;

import org.junit.jupiter.api.Test;
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
		assertEquals("", WebUtil.getContextPath());
	}
	
	/**
	 * @see org.openmrs.web.WebUtil#getContextPath()
	 */
	@Test
	public void getContextPath_shouldReturnEmptyStringWhenWebAppNameIsEmptyString() {
		WebConstants.WEBAPP_NAME = "";
		assertEquals("", WebUtil.getContextPath());
	}
	
	/**
	 * @see org.openmrs.web.WebUtil#getContextPath()
	 */
	@Test
	public void getContextPath_shouldReturnValueSpecifiedInWebAppName() {
		WebConstants.WEBAPP_NAME = "Value";
		assertEquals("/Value", WebUtil.getContextPath());
	}
	
	/**
	 * @see WebUtil#normalizeLocale(String)
	 */
	@Test
	public void normalizeLocale_shouldIgnoreLeadingSpaces() {
		assertEquals(Locale.ITALIAN, WebUtil.normalizeLocale(" it"));
	}
	
	/**
	 * @see WebUtil#normalizeLocale(String)
	 */
	@Test
	public void normalizeLocale_shouldAcceptLanguageOnlyLocales() {
		assertEquals(Locale.FRENCH, WebUtil.normalizeLocale("fr"));
	}
	
	/**
	 * @see WebUtil#normalizeLocale(String)
	 */
	@Test
	public void normalizeLocale_shouldNotAcceptInvalidLocales() {
		assertNull(WebUtil.normalizeLocale("ptrg"));
		assertNull(WebUtil.normalizeLocale("usaa"));
	}
	
	/**
	 * @see WebUtil#normalizeLocale(String)
	 */
	@Test
	public void normalizeLocale_shouldNotFailWithEmptyStrings() {
		assertNull(WebUtil.normalizeLocale(""));
	}
	
	/**
	 * @see WebUtil#normalizeLocale(String)
	 */
	@Test
	public void normalizeLocale_shouldNotFailWithWhitespaceOnly() {
		assertNull(WebUtil.normalizeLocale("      "));
	}

	/**
	 * @throws UnsupportedEncodingException
	 * @see WebUtil#normalizeLocale(String)
	 */
	@Test
	public void normalizeLocale_shouldNotFailWithTab() throws UnsupportedEncodingException {
		String s = new String(new byte[]{0x9}, "ASCII");
		assertNull(WebUtil.normalizeLocale(s));
	}

	/**
	 * @see WebUtil#normalizeLocale(String)
	 */
	@Test
	public void normalizeLocale_shouldNotFailWithUnicode() {
		assertNull(WebUtil.normalizeLocale("Ši"));
	}

	/**
	 * @see WebUtil#normalizeLocale(String)
	 */
	@Test
	public void normalizeLocale_shouldNotFailWithSingleChar() {
		assertNull(WebUtil.normalizeLocale("s"));
	}

	/**
	 * @throws UnsupportedEncodingException
	 * @see WebUtil#normalizeLocale(String)
	 */
	@Test
	public void normalizeLocale_shouldNotFailWithUnderline() throws UnsupportedEncodingException {
		String s = new String(new byte[]{0x5f}, "ASCII");
		assertNull(WebUtil.normalizeLocale(s));
	}

	/**
	 * @see WebUtil#sanitizeLocales(String)
	 */
	@Test
	public void sanitizeLocales_shouldSkipOverInvalidLocales() {
		assertEquals("fr_RW, it, en", WebUtil.sanitizeLocales("és, qqqq, fr_RW, it, enñ"));
	}
	
	/**
	 * @see WebUtil#sanitizeLocales(String)
	 */
	@Test
	public void sanitizeLocales_shouldNotFailWithEmptyString() {
		assertNull(null, WebUtil.sanitizeLocales(""));
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

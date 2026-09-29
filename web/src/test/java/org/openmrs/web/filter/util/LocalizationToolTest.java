/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.filter.util;

import java.util.Arrays;
import java.util.Locale;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class LocalizationToolTest {

	private final LocalizationTool tool = new LocalizationTool(Locale.ENGLISH);

	/**
	 * @see LocalizationTool#get(Object)
	 */
	@Test
	void get_shouldRenderTheMessageForTheCode() {
		assertEquals("OpenMRS Core {0} Installation Wizard", tool.get("install.header.caption").toString());
	}

	/**
	 * @see LocalizationTool.Message#insert(Object)
	 */
	@Test
	void insert_shouldFormatTheMessageWithTheArgument() {
		assertEquals("OpenMRS Core 3.0.0 Installation Wizard",
		    tool.get("install.header.caption").insert("3.0.0").toString());
	}

	/**
	 * @see LocalizationTool.Message#insert(Object[])
	 */
	@Test
	void insert_shouldAppendToPreviouslyInsertedArguments() {
		LocalizationTool.Message message = tool.get("install.header.caption");

		assertEquals(message.insert("a").insert(Arrays.asList("b", "c")).toString(),
		    message.insert(new Object[] { "a", "b", "c" }).toString());
	}

	/**
	 * @see LocalizationTool#get(Object)
	 */
	@Test
	void get_shouldRenderAMissingCodeWithQuestionMarks() {
		assertEquals("???no.such.code???", tool.get("no.such.code").toString());
	}

	/**
	 * @see LocalizationTool#get(Object)
	 */
	@Test
	void get_shouldRenderANullCodeAsAnEmptyString() {
		assertEquals("", tool.get(null).toString());
	}

	/**
	 * @see LocalizationTool#setLocale(Locale)
	 */
	@Test
	void get_shouldUseTheLocaleSetWhenTheMessageWasCreated() {
		LocalizationTool localizedTool = new LocalizationTool(Locale.FRENCH);
		LocalizationTool.Message french = localizedTool.get("install.header.caption");

		localizedTool.setLocale(Locale.ENGLISH);

		assertNotEquals(french.toString(), localizedTool.get("install.header.caption").toString());
	}
}

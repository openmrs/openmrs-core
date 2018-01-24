/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.GlobalProperty;
import org.openmrs.util.OpenmrsConstants;

/**
 * Tests {@link GlobalLocaleList}.
 */
public class GlobalLocaleListTest {

	private GlobalLocaleList globalLocaleList;

	@Before
	public void setUp() {
		globalLocaleList = new GlobalLocaleList();
	}

	@Test
	public void globalPropertyChanged_shouldSetAllowedLocalesIfGlobalPropertyIsAnEmptyString() {

		GlobalProperty gp = new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOCALE_ALLOWED_LIST, "");

		globalLocaleList.globalPropertyChanged(gp);

		assertThat(globalLocaleList.getAllowedLocales(), contains(Locale.ROOT));
	}
	
	@Test
	public void globalPropertyChanged_shouldSetAllowedLocalesIfGlobalPropertyContainsTwoLocales() {

		GlobalProperty gp = new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOCALE_ALLOWED_LIST, "en_CA,fr");

		globalLocaleList.globalPropertyChanged(gp);

		assertThat(globalLocaleList.getAllowedLocales(), contains(Locale.CANADA, Locale.FRENCH));
	}
}

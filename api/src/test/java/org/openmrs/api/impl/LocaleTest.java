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

import org.junit.jupiter.api.Test;
import org.openmrs.api.context.Context;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.openmrs.util.OpenmrsConstants;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LocaleTest extends BaseContextSensitiveTest {
	
	@Test
	public void getLocale_shouldReturnTheCorrectLocale() {
		Locale currentLocale = Context.getLocale();
		
		assertEquals("GB", currentLocale.getCountry());
		assertEquals("en", currentLocale.getLanguage());
		
		Context.getAdministrationService().setGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOCALE_ALLOWED_LIST, "fr_CA,en_GB");
		Context.getAdministrationService().setGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_DEFAULT_LOCALE, "fr_CA");
		
		Context.flushSession();
		Context.closeSession();
		Context.openSessionWithCurrentUser();
		currentLocale = Context.getLocale();

		// It still returns en_GB as it is returning the locale from cache. Restarting the server
		// doesn't fix the issue either. 
		assertEquals("GB", currentLocale.getCountry());
		assertEquals("en", currentLocale.getLanguage());
		
		assertEquals("CA", currentLocale.getCountry());
		assertEquals("en", currentLocale.getLanguage());	
	}
}

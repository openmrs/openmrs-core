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

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.util.LocaleUtility;

import java.util.*;

public class CustomResourceLoaderTest {

	@Test
	public void updateResources_shouldUpdateResourceWithOverrideProperty() {
		String testMainClassPath = "classpath*:testOverride/messages*.properties";
		String testOverrideClassPath = "classpath*:testOverride/%s_override.properties";

		CustomResourceLoader customResourceLoader  = CustomResourceLoader.getInstance(null);
		Map<Locale, ResourceBundle>  resources = customResourceLoader.updateResources(testMainClassPath,testOverrideClassPath);
		Set<Locale> availablelocales = resources.keySet();
    	
    	Locale expectedLocale = LocaleUtility.fromSpecification("fr");
    	Assert.assertTrue(availablelocales.contains(expectedLocale)); // "fr" as a language is present
		//check property value change from XYZ to ABC
		Assert.assertEquals("ABC",resources.get(expectedLocale).getString("test.install.header.caption"));
		//check non-override property is unaffected  
		Assert.assertEquals("PQR",resources.get(expectedLocale).getString("test.install.header2"));
    }
	

    @Test
	public void updateResources_shouldNotUpdateResourceOnEmptyOverrideProperties() {
		String testMainClassPath = "classpath*:testOverride/messages*.properties";
		String testOverrideClassPath = "classpath*:testOverride/%s_override.properties";

		CustomResourceLoader customResourceLoader  = CustomResourceLoader.getInstance(null);
		Map<Locale, ResourceBundle>  resources = customResourceLoader.updateResources(testMainClassPath,testOverrideClassPath);
		Set<Locale> availablelocales = resources.keySet();

		Locale expectedLocale = LocaleUtility.fromSpecification("en");
		Assert.assertTrue(availablelocales.contains(expectedLocale)); // "en" as a language is present
		//check property value remains unaffected on empty properties file
		Assert.assertEquals("XYZ",resources.get(expectedLocale).getString("test.install.header.caption"));
	}
}

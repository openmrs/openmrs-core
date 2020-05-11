package org.openmrs.web.filter.util;

import junit.framework.TestCase;
import org.openmrs.util.LocaleUtility;

import java.util.*;

public class CustomResourceLoaderTest extends TestCase {
	
	public void testUpdateResource() {
		String testMainClassPath = "classpath*:testOverride/messages*.properties";
		String testOverrideClassPath = "classpath*:testOverride/%s_override.properties";

		CustomResourceLoader customResourceLoader  = CustomResourceLoader.getInstance(null);
		Map<Locale, ResourceBundle>  resources = customResourceLoader.updateResources(testMainClassPath,testOverrideClassPath);
		Set<Locale> availablelocales = resources.keySet();
    	
    	Locale expectedLocale = LocaleUtility.fromSpecification("fr");
    	assertTrue(availablelocales.contains(expectedLocale)); // "fr" as a language is present
		//check property value change from XYZ to ABC
		assertEquals("ABC",resources.get(expectedLocale).getString("test.install.header.caption"));
		//check non-override property is unaffected  
		assertEquals("PQR",resources.get(expectedLocale).getString("test.install.header2"));
    }
	
	public void testUpdateResourceEmptyOverrideProperties() {
		String testMainClassPath = "classpath*:testOverride/messages*.properties";
		String testOverrideClassPath = "classpath*:testOverride/%s_override.properties";

		CustomResourceLoader customResourceLoader  = CustomResourceLoader.getInstance(null);
		Map<Locale, ResourceBundle>  resources = customResourceLoader.updateResources(testMainClassPath,testOverrideClassPath);
		Set<Locale> availablelocales = resources.keySet();

		Locale expectedLocale = LocaleUtility.fromSpecification("en");
		assertTrue(availablelocales.contains(expectedLocale)); // "en" as a language is present
		//check property value remains unaffected on empty properties file
		assertEquals("XYZ",resources.get(expectedLocale).getString("test.install.header.caption"));
	}
}

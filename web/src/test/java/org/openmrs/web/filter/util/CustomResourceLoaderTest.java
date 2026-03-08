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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Unit tests for {@link CustomResourceLoader} locale matching functionality.
 */
class CustomResourceLoaderTest {

	private CustomResourceLoader resourceLoader;

	@BeforeEach
	void setup() {
		Set<Locale> availableLocales = new HashSet<>();
		availableLocales.add(Locale.ENGLISH);
		availableLocales.add(Locale.FRENCH);
		availableLocales.add(new Locale("es"));
		resourceLoader = new CustomResourceLoader(availableLocales);
	}

	@Test
	void findBestMatchLocale_shouldReturnExactMatch() {
		Locale result = resourceLoader.findBestMatchLocale("fr");
		assertEquals(Locale.FRENCH, result);
	}

	@Test
	void findBestMatchLocale_shouldReturnLanguageWhenRegionalLocaleNotAvailable() {
		Locale result = resourceLoader.findBestMatchLocale("fr-BE");
		assertEquals(Locale.FRENCH, result);
	}

	@Test
	void findBestMatchLocale_shouldRespectQualityWeights() {
		Locale result = resourceLoader.findBestMatchLocale("fr-BE,fr;q=0.9,en;q=0.8");
		assertEquals(Locale.FRENCH, result);
	}

	@Test
	void findBestMatchLocale_shouldFallbackWhenHighPriorityLocaleUnavailable() {
		Locale result = resourceLoader.findBestMatchLocale("de;q=0.9,en;q=0.8");
		assertEquals(Locale.ENGLISH, result);
	}

	@Test
	void findBestMatchLocale_shouldReturnDefaultLocaleForNullHeader() {
		Locale result = resourceLoader.findBestMatchLocale(null);
		assertNotNull(result);
	}

	@Test
	void findBestMatchLocale_shouldReturnDefaultLocaleForEmptyHeader() {
		Locale result = resourceLoader.findBestMatchLocale("");
		assertNotNull(result);
	}

	@Test
	void findBestMatchLocale_shouldReturnDefaultLocaleForWhitespaceHeader() {
		Locale result = resourceLoader.findBestMatchLocale("   ");
		assertNotNull(result);
	}

	@Test
	void findBestMatchLocale_shouldReturnDefaultLocaleWhenNoMatch() {
		Locale result = resourceLoader.findBestMatchLocale("de");
		assertNotNull(result);
	}

	@Test
	void findBestMatchLocale_shouldReturnFirstMatchWhenMultipleMatch() {
		Locale result = resourceLoader.findBestMatchLocale("en,fr,es");
		assertEquals(Locale.ENGLISH, result);
	}

	@Test
	void findBestMatchLocale_shouldHandleMalformedHeaderGracefully() {
		Locale result = resourceLoader.findBestMatchLocale("this-is-not-valid;;;");
		assertNotNull(result);
	}

	@Test
	void findBestMatchLocale_shouldHandleComplexAcceptLanguageHeader() {
		Locale result = resourceLoader.findBestMatchLocale("fr-CH,fr;q=0.9,en;q=0.8,de;q=0.7");
		assertEquals(Locale.FRENCH, result);
	}

	@Test
	void constructor_shouldHandleNullLocaleSet() {
		CustomResourceLoader loader = new CustomResourceLoader((Set<Locale>) null);
		assertNotNull(loader.getAvailablelocales());
		assertEquals(0, loader.getAvailablelocales().size());
	}

	@Test
	void constructor_shouldHandleEmptyLocaleSet() {
		CustomResourceLoader loader = new CustomResourceLoader(new HashSet<>());
		assertNotNull(loader.getAvailablelocales());
		assertEquals(0, loader.getAvailablelocales().size());
		Locale result = loader.findBestMatchLocale("fr");
		assertNotNull(result);
	}
}

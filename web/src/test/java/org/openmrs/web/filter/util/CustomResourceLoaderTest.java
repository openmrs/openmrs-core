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
 * These tests verify RFC 4647 language range matching behavior.
 */
public class CustomResourceLoaderTest {

    private CustomResourceLoader resourceLoader;
    private Set<Locale> availableLocales;

    @BeforeEach
    public void setup() {
        // Create a set of available locales for testing
        availableLocales = new HashSet<>();
        availableLocales.add(Locale.ENGLISH);
        availableLocales.add(Locale.FRENCH);
        availableLocales.add(new Locale("es")); // Spanish
        availableLocales.add(new Locale("pt", "BR")); // Portuguese (Brazil)

        // Use the protected constructor to inject test locales
        resourceLoader = new CustomResourceLoader(availableLocales);
    }

    @Test
    public void testFindBestMatchLocale_ExactMatch() {
        // Test exact match for French
        Locale result = resourceLoader.findBestMatchLocale("fr");
        assertEquals(Locale.FRENCH, result);
    }

    @Test
    public void testFindBestMatchLocale_ExactMatchWithRegion() {
        // Test exact match for Portuguese (Brazil)
        Locale result = resourceLoader.findBestMatchLocale("pt-BR");
        assertEquals(new Locale("pt", "BR"), result);
    }

    @Test
    public void testFindBestMatchLocale_RegionalFallback() {
        // Test that fr-BE (Belgian French) falls back to fr (French)
        // since Belgian French is not available but French is
        Locale result = resourceLoader.findBestMatchLocale("fr-BE");
        assertEquals(Locale.FRENCH, result);
    }

    @Test
    public void testFindBestMatchLocale_WithQualityWeights() {
        // Test with quality weights - should match the highest priority available
        // locale
        Locale result = resourceLoader.findBestMatchLocale("fr-BE,fr;q=0.9,en;q=0.8");
        // Should match French (fr) since fr-BE is not available but fr is
        assertEquals(Locale.FRENCH, result);
    }

    @Test
    public void testFindBestMatchLocale_QualityWeightsPreferEnglish() {
        // Test with English having higher priority than unavailable locales
        Locale result = resourceLoader.findBestMatchLocale("de;q=0.9,en;q=0.8");
        // German is not available, so should fall back to English
        assertEquals(Locale.ENGLISH, result);
    }

    @Test
    public void testFindBestMatchLocale_NullHeader() {
        // Test that null header returns default English locale
        Locale result = resourceLoader.findBestMatchLocale(null);
        assertEquals(Locale.ENGLISH, result);
    }

    @Test
    public void testFindBestMatchLocale_EmptyHeader() {
        // Test that empty header returns default English locale
        Locale result = resourceLoader.findBestMatchLocale("");
        assertEquals(Locale.ENGLISH, result);
    }

    @Test
    public void testFindBestMatchLocale_WhitespaceHeader() {
        // Test that whitespace-only header returns default English locale
        Locale result = resourceLoader.findBestMatchLocale("   ");
        assertEquals(Locale.ENGLISH, result);
    }

    @Test
    public void testFindBestMatchLocale_NoMatch() {
        // Test that when no locale matches, it returns English as default
        Locale result = resourceLoader.findBestMatchLocale("de");
        assertEquals(Locale.ENGLISH, result);
    }

    @Test
    public void testFindBestMatchLocale_NoMatchMultiple() {
        // Test with multiple non-matching locales
        Locale result = resourceLoader.findBestMatchLocale("de,it,ja");
        assertEquals(Locale.ENGLISH, result);
    }

    @Test
    public void testFindBestMatchLocale_ComplexHeader() {
        // Test with a complex Accept-Language header
        Locale result = resourceLoader.findBestMatchLocale("fr-CH,fr;q=0.9,en;q=0.8,de;q=0.7,*;q=0.5");
        // Should match French (fr) since fr-CH is not available but fr is
        assertEquals(Locale.FRENCH, result);
    }

    @Test
    public void testFindBestMatchLocale_CaseInsensitive() {
        // Test that locale matching is case-insensitive
        Locale result = resourceLoader.findBestMatchLocale("FR");
        assertEquals(Locale.FRENCH, result);
    }

    @Test
    public void testFindBestMatchLocale_MixedCase() {
        // Test with mixed case locale codes
        Locale result = resourceLoader.findBestMatchLocale("Es");
        assertEquals(new Locale("es"), result);
    }

    @Test
    public void testFindBestMatchLocale_MultipleRangesFirstMatch() {
        // Test that the first matching locale is returned when multiple match
        Locale result = resourceLoader.findBestMatchLocale("en,fr,es");
        // Should match English as it's first in the list
        assertEquals(Locale.ENGLISH, result);
    }

    @Test
    public void testFindBestMatchLocale_PortugueseBrazilFallback() {
        // Test that pt (Portuguese) does NOT match pt-BR
        // RFC 4647 lookup only matches more specific to less specific, not vice versa
        Locale result = resourceLoader.findBestMatchLocale("pt");
        // Since only pt-BR is available (not generic pt), it should return English as
        // default
        assertEquals(Locale.ENGLISH, result);
    }

    @Test
    public void testFindBestMatchLocale_MalformedHeader() {
        // Test with malformed header - should return English as default
        Locale result = resourceLoader.findBestMatchLocale("this-is-not-valid;;;");
        // Should handle gracefully and return default
        assertNotNull(result);
        assertEquals(Locale.ENGLISH, result);
    }

    @Test
    public void testFindBestMatchLocale_WithWildcard() {
        // Test with wildcard in Accept-Language header
        Locale result = resourceLoader.findBestMatchLocale("de,*;q=0.5");
        // German is not available, wildcard should match any available locale
        // In this case, it should still return English as the default
        assertNotNull(result);
    }

    @Test
    public void testConstructorWithNullLocales() {
        // Test that constructor handles null locale set gracefully
        CustomResourceLoader loader = new CustomResourceLoader((Set<Locale>) null);
        assertNotNull(loader.getAvailablelocales());
        assertEquals(0, loader.getAvailablelocales().size());
    }

    @Test
    public void testConstructorWithEmptyLocales() {
        // Test constructor with empty locale set
        CustomResourceLoader loader = new CustomResourceLoader(new HashSet<>());
        assertNotNull(loader.getAvailablelocales());
        assertEquals(0, loader.getAvailablelocales().size());

        // Should still return English as default when no locales are available
        Locale result = loader.findBestMatchLocale("fr");
        assertEquals(Locale.ENGLISH, result);
    }
}
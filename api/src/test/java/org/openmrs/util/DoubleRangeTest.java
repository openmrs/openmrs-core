/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class DoubleRangeTest {

    @Test
    public void shouldContainValueWithinRange() {
        DoubleRange range = new DoubleRange(1.0, 10.0);
        assertTrue(range.contains(5.0));
    }

    @Test
    public void shouldNotContainValueEqualToHigh() {
        DoubleRange range = new DoubleRange(1.0, 10.0);
        assertFalse(range.contains(10.0));
    }

    @Test
    public void shouldContainValueEqualToLow() {
        DoubleRange range = new DoubleRange(1.0, 10.0);
        assertTrue(range.contains(1.0));
    }

    @Test
    public void shouldGetAndSetLowAndHighValues() {
        DoubleRange range = new DoubleRange(1.0, 10.0);
        assertEquals(1.0, range.getLow());
        assertEquals(10.0, range.getHigh());

        range.setLow(2.0);
        range.setHigh(9.0);

        assertEquals(2.0, range.getLow());
        assertEquals(9.0, range.getHigh());
    }

    @Test
    public void equalsShouldConsiderLowAndHigh() {
        DoubleRange r1 = new DoubleRange(1.0, 10.0);
        DoubleRange r2 = new DoubleRange(1.0, 10.0);
        DoubleRange r3 = new DoubleRange(2.0, 10.0);

        assertEquals(r1, r2);
        assertNotEquals(r1, r3);
    }

    @Test
    public void hashCodeShouldBeConsistentWithEquals() {
        DoubleRange r1 = new DoubleRange(1.0, 10.0);
        DoubleRange r2 = new DoubleRange(1.0, 10.0);

        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());
    }

    @Test
    public void compareToShouldOrderByLowThenHigh() {
        DoubleRange r1 = new DoubleRange(1.0, 5.0);
        DoubleRange r2 = new DoubleRange(2.0, 6.0);

        assertTrue(r1.compareTo(r2) < 0);
        assertTrue(r2.compareTo(r1) > 0);
    }

    @Test
    public void toStringShouldReturnRangeRepresentation() {
        DoubleRange range = new DoubleRange(1.0, 10.0);
        String text = range.toString();

        assertNotNull(text);
        assertTrue(text.contains("1.0"));
        assertTrue(text.contains("10.0"));
    }

    @Test
    public void shouldHandleNullBoundsSafely() {
        DoubleRange range = new DoubleRange(null, null);
        assertTrue(range.contains(5.0));
    }

    @Test
    public void shouldReturnFalseForDefaultConstructor() {
        DoubleRange range = new DoubleRange();
        assertFalse(range.contains(5.0));
    }
}
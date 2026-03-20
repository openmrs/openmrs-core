package org.openmrs.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

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
}
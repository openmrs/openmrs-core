package org.openmrs.logic.op;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests the {@link GreaterThanEquals} class
 */
public class GreaterThanEqualsTest {

    /**
     * @verifies returns string
     * @see GreaterThanEquals#toString()
     */
    @Test
    public void toString_shouldReturnString() {
        GreaterThanEquals greaterThanEquals = new GreaterThanEquals();
        final String greaterThanEqualsStr = "GREATER THAN EQUALS";
        Assert.assertEquals(greaterThanEqualsStr, greaterThanEquals.toString());
    }
}

package org.openmrs.logic.op;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests the {@link LessThanEquals} class
 */
public class LessThanEqualsTest {

    /**
     * @verifies returns string
     * @see LessThanEquals#toString()
     */
    @Test
    public void toString_shouldReturnString() {
        LessThanEquals lessThanEquals = new LessThanEquals();
        final String lessThanEqualsStr = "LESS THAN EQUALS";
        Assert.assertEquals(lessThanEqualsStr, lessThanEquals.toString());
    }
}

package org.openmrs.logic.op;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests the {@link GreaterThan} class
 */
public class GreaterThanTest {

    /**
     * @verifies returns string
     * @see GreaterThan#toString()
     */
    @Test
    public void toString_shouldReturnString() {
        GreaterThan greaterThan = new GreaterThan();
        final String greaterThanString = "GREATER THAN";
        Assert.assertEquals(greaterThanString, greaterThan.toString());
    }
}

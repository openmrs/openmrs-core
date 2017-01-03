package org.openmrs.logic.op;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests the {@link LessThan} class
 */
public class LessThanTest {

    /**
     * @verifies returns string
     * @see LessThan#toString()
     */
    @Test
    public void toString_shouldReturnString() {
        LessThan lessThan = new LessThan();
        final String lessThanString = "LESS THAN";
        Assert.assertEquals(lessThanString, lessThan.toString());
    }
}

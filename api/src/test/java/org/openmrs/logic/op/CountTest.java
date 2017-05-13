package org.openmrs.logic.op;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests the {@link Count} class
 */
public class CountTest {

    /**
     * @verifies returns string
     * @see Count#toString()
     */
    @Test
    public void toString_shouldReturnString() {
        Count count = new Count();
        final String countString = "Count";
        Assert.assertEquals(countString, count.toString());
    }
}

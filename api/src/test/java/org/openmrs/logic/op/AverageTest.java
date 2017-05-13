package org.openmrs.logic.op;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests the {@link Average} class
 */
public class AverageTest {

    /**
     * @verifies returns string
     * @see Average#toString()
     */
    @Test
    public void toString_shouldReturnString() {
        Average average = new Average();
        final String averageString = "Average";
        Assert.assertEquals(averageString, average.toString());
    }
}

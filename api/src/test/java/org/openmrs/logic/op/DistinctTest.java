package org.openmrs.logic.op;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests the {@link Distinct} class
 */
public class DistinctTest {

    /**
     * @verifies returns string
     * @see Distinct#toString()
     */
    @Test
    public void toString_shouldReturnString() {
        Distinct distinct = new Distinct();
        final String distinctString = "DISTINCT";
        Assert.assertEquals(distinctString, distinct.toString());
    }
}

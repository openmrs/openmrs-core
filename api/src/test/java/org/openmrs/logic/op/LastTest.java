package org.openmrs.logic.op;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests the {@link Last} class
 */
public class LastTest {

    /**
     * @verifies returns string
     * @see Last#toString()
     */
    @Test
    public void toString_shouldReturnString() {
        Last last = new Last();
        final String lastString = "LAST";
        Assert.assertEquals(lastString, last.toString());
    }
}

package org.openmrs.logic.op;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests the {@link Not} class
 */
public class NotTest {

    /**
     * @verifies returns string
     * @see Not#toString()
     */
    @Test
    public void toString_shouldReturnString() {
        Not not = new Not();
        final String notString = "NOT";
        Assert.assertEquals(notString, not.toString());
    }
}

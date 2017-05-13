package org.openmrs.logic.op;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests the {@link In} class
 */
public class InTest {

    /**
     * @verifies returns string
     * @see In#toString()
     */
    @Test
    public void toString_shouldReturnString() {
        In in = new In();
        final String inString = "IN";
        Assert.assertEquals(inString, in.toString());
    }
}

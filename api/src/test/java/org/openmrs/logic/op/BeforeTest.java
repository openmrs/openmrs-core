package org.openmrs.logic.op;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests the {@link Before} class
 */
public class BeforeTest {

    /**
     * @verifies returns string
     * @see Before#toString()
     */
    @Test
    public void toString_shouldReturnString() {
        Before before = new Before();
        final String beforeString = "BEFORE";
        Assert.assertEquals(beforeString, before.toString());
    }
}

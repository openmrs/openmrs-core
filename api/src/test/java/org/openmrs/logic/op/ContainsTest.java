package org.openmrs.logic.op;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests the {@link Contains} class
 */
public class ContainsTest {

    /**
     * @verifies returns string
     * @see Contains#toString()
     */
    @Test
    public void toString_shouldReturnString() {
        Contains contains = new Contains();
        final String containsString = "CONTAINS";
        Assert.assertEquals(containsString, contains.toString());
    }
}

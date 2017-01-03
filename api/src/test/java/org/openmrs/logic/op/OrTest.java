package org.openmrs.logic.op;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests the {@link Or} class
 */
public class OrTest {

    /**
     * @verifies returns string
     * @see Or#toString()
     */
    @Test
    public void toString_shouldReturnString() {
        Or or = new Or();
        final String orString = "OR";
        Assert.assertEquals(orString, or.toString());
    }
}

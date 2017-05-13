package org.openmrs.logic.op;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests the {@link First} class
 */
public class FirstTest {

    /**
     * @verifies returns string
     * @see First#toString()
     */
    @Test
    public void toString_shouldReturnString() {
        First first = new First();
        final String firstString = "FIRST";
        Assert.assertEquals(firstString, first.toString());
    }
}

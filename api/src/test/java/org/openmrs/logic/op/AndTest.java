package org.openmrs.logic.op;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests the {@link And} class
 */
public class AndTest {

    /**
     * @verifies returns string
     * @see And#toString()
     */
    @Test
    public void toString_shouldReturnString() {
        And and = new And();
        final String andString = "AND";
        Assert.assertEquals(andString, and.toString());
    }
}

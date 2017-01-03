package org.openmrs.logic.op;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests the {@link NotExists} class
 */
public class NotExistsTest {

    /**
     * @verifies returns string
     * @see NotExists#toString()
     */
    @Test
    public void toString_shouldReturnString() {
        NotExists notExists = new NotExists();
        final String notExistsString = "NOT EXISTS";
        Assert.assertEquals(notExistsString, notExists.toString());
    }
}

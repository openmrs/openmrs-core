package org.openmrs.logic.op;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests the {@link Equals} class
 */
public class EqualsTest {

    /**
     * @verifies returns string
     * @see Equals#toString()
     */
    @Test
    public void toString_shouldReturnString() {
        Equals equals = new Equals();
        final String equalsString = "EQUALS";
        Assert.assertEquals(equalsString, equals.toString());
    }

}

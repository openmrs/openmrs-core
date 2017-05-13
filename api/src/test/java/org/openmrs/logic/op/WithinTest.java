package org.openmrs.logic.op;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests the {@link Within} class
 */
public class WithinTest {

    /**
     * @verifies returns string
     * @see Within#toString()
     */
    @Test
    public void toString_shouldReturnString() {
        Within within = new Within();
        final String withinString = "WITHIN";
        Assert.assertEquals(withinString, within.toString());
    }
}

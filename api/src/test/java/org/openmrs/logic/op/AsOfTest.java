package org.openmrs.logic.op;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests the {@link AsOf} class
 */
public class AsOfTest {

    /**
     * @verifies returns string
     * @see AsOf#toString()
     */
    @Test
    public void toString_shouldReturnString() {
        AsOf asOf = new AsOf();
        final String asOfString = "AS OF";
        Assert.assertEquals(asOfString, asOf.toString());
    }
}

/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.logic.op;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests the {@link GreaterThan} class
 */
public class GreaterThanTest {

    /**
     * @verifies returns string
     * @see GreaterThan#toString()
     */
    @Test
    public void toString_shouldReturnString() {
        GreaterThan greaterThan = new GreaterThan();
        final String greaterThanString = "GREATER THAN";
        Assert.assertEquals(greaterThanString, greaterThan.toString());
    }
}

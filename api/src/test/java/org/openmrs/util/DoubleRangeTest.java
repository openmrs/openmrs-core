/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.util;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.test.Verifies;

/**
 * Contains methods to test behavior of DoubleRange methods
 */
public class DoubleRangeTest {
	
	/**
	 * @see {@link DoubleRange#equals(Object)}
	 */
	@Test
	@Verifies(value = "should return true for the same object type and false for different objects", method = "equals(Object o)")
	public void equals_shouldReturnTrueForTheSameObjectTypeAndFalseForDifferentTypeObjects() {
		Double value1 = 0.0;
		Double value2 = 5.0;
		Double value3 = 6.0;
		DoubleRange obj = new DoubleRange(value1, value2);
		DoubleRange objTest1 = new DoubleRange(value1, value2);
		DoubleRange objTest2 = new DoubleRange(value1, value3);
		Object objTest3 = new Object();
		
		Assert.assertTrue(obj.equals(objTest1));
		Assert.assertFalse(obj.equals(objTest2));
		Assert.assertFalse(obj.equals(objTest3));
	}
}

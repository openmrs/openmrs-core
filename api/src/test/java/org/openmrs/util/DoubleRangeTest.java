/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
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

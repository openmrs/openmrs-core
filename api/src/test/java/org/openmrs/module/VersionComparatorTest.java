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
package org.openmrs.module;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

/**
 * Tests the {@link VersionComparator} class
 */
public class VersionComparatorTest {

	/**
	 * Regression test for TRUNK-1668
	 * <br/>
	 * 
	 * @see VersionComparator#compare(String,String)
	 * @verifies compare via numeric value not string value
	 */
	@Test
	public void compare_shouldCompareViaNumericValueNotStringValue() throws Exception {
		String [] correctStringSet = {"1.1", "1.2", "1.7", "1.10", "1.11", "1.20", "2.1.1", "2.1.9", "2.1.10", "2.1.20"};
		String [] randomPurmutationSet = {"1.2", "2.1.10", "2.1.20", "1.1", "1.7", "2.1.1", "1.20", "1.10", "2.1.9", "1.11"};
		
		Arrays.sort(randomPurmutationSet, new VersionComparator());
		assertTrue("", Arrays.equals(correctStringSet, randomPurmutationSet));
	}
	
}

/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

/**
 * Tests the {@link VersionComparator} class
 */
public class VersionComparatorTest {
	
	/**
	 * Regression test for TRUNK-1668
	 * <br>
	 * 
	 * @see VersionComparator#compare(String,String)
	 */
	@Test
	public void compare_shouldCompareViaNumericValueNotStringValue() {
		String[] correctStringSet = { "1.1", "1.2", "1.7", "1.10", "1.11", "1.20", "2.1.1", "2.1.9", "2.1.10", "2.1.20" };
		String[] randomPurmutationSet = { "1.2", "2.1.10", "2.1.20", "1.1", "1.7", "2.1.1", "1.20", "1.10", "2.1.9", "1.11" };
		
		Arrays.sort(randomPurmutationSet, new VersionComparator());
		assertTrue(Arrays.equals(correctStringSet, randomPurmutationSet), "");
	}
	
}

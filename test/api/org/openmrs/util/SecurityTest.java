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
 * Tests the methods on the {@link Security} class
 */
public class SecurityTest {
	
	/**
	 * @see {@link Security#encodeString(String)}
	 */
	@Test
	@Verifies(value = "should encodeStringsToFortyCharacters", method = "encodeString(String)")
	public void encodeString_shouldEncodeStringsToFortyCharacters() throws Exception {
		String hash = Security.encodeString("test" + "c788c6ad82a157b712392ca695dfcf2eed193d7f");
		Assert.assertEquals(40, hash.length());
	}
	
	/**
	 * @see {@link Security#hashMatches(String,String)}
	 */
	@Test
	@Verifies(value = "should matchStringsHashedWithCorrectAlgorithm", method = "hashMatches(String,String)")
	public void hashMatches_shouldMatchStringsHashedWithCorrectAlgorithm() throws Exception {
		Assert.assertTrue(Security.hashMatches("4a1750c8607d0fa237de36c6305715c223415189", "test"
		        + "c788c6ad82a157b712392ca695dfcf2eed193d7f"));
	}
	
	/**
	 * @see {@link Security#hashMatches(String,String)}
	 */
	@Test
	@Verifies(value = "should matchStringsHashedWithOldAlgorithm", method = "hashMatches(String,String)")
	public void hashMatches_shouldMatchStringsHashedWithOldAlgorithm() throws Exception {
		Assert.assertTrue(Security.hashMatches("4a1750c8607dfa237de36c6305715c223415189", "test"
		        + "c788c6ad82a157b712392ca695dfcf2eed193d7f"));
	}
}

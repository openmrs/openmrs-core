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
	
	private static final int HASH_LENGTH = 128;
	
	/**
	 * @see {@link Security#encodeString(String)}
	 */
	@Test
	@Verifies(value = "should encodeStringsTo128Characters", method = "encodeString(String)")
	public void encodeString_shouldEncodeStringsTo128Characters() throws Exception {
		String hash = Security.encodeString("test" + "c788c6ad82a157b712392ca695dfcf2eed193d7f");
		Assert.assertEquals(HASH_LENGTH, hash.length());
	}
	
	/**
	 * @see {@link Security#encodeString(String)}
	 */
	@Test
	@Verifies(value = "should encodeStringsToXCharactersWithXCharactersSalt", method = "encodeString(String)")
	public void encodeString_shouldEncodeStringsToXCharactersWithXCharactersSalt() throws Exception {
		String hash = Security.encodeString("test" + Security.getRandomToken());
		Assert.assertEquals(HASH_LENGTH, hash.length());
	}
	
	/**
	 * @see {@link Security#hashMatches(String,String)}
	 */
	@Test
	@Verifies(value = "should match strings hashed with sha1 algorithm", method = "hashMatches(String,String)")
	public void hashMatches_shouldMatchStringsHashedWithSha1Algorithm() throws Exception {
		Assert.assertTrue(Security.hashMatches("4a1750c8607d0fa237de36c6305715c223415189", "test"
		        + "c788c6ad82a157b712392ca695dfcf2eed193d7f"));
	}
	
	/**
	 * @see {@link Security#hashMatches(String,String)}
	 */
	@Test
	@Verifies(value = "should match strings hashed with sha512 algorithm and 128 characters salt", method = "hashMatches(String,String)")
	public void hashMatches_shouldMatchStringsHashedWithSha512AlgorithmAnd128CharactersSalt() throws Exception {
		String password = "1d1436658853aceceadd72e92f1ae9089a0000fbb38cea519ce34eae9f28523930ecb212177dbd607d83dc275fde3e9ca648deb557d503ad0bcd01a955a394b2";
		String passwordToHash = "test"
		        + "0d7bb319434295261601202e14494b959cdd69c6ceb54ee3890e176ae780ce9edf797f48afde5f39906a6bd75b8a5feeac8f5339615acf7429c7dda85220d329";
		Assert.assertTrue(Security.hashMatches(password, passwordToHash));
	}
	
	/**
	 * @see {@link Security#hashMatches(String,String)}
	 */
	@Test
	@Verifies(value = "should match strings hashed with incorrect sha1 algorithm", method = "hashMatches(String,String)")
	public void hashMatches_shouldMatchStringsHashedWithIncorrectSha1Algorithm() throws Exception {
		Assert.assertTrue(Security.hashMatches("4a1750c8607dfa237de36c6305715c223415189", "test"
		        + "c788c6ad82a157b712392ca695dfcf2eed193d7f"));
	}
	
}

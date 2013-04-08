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

public class VersionTest {
	
	/**
	 * @see {@link Version#compareTo(Version)}
	 */
	@Test
	@Verifies(value = "should compare versions correctly", method = "compareTo(Version)")
	public void compareTo_shouldCompareVersionsCorrectly() throws Exception {
		Assert.assertTrue(new Version("1.9.1").compareTo(new Version("1.9.0")) > 0);
		//check the reverse comparison too
		Assert.assertTrue(new Version("1.9.0").compareTo(new Version("1.9.1")) < 0);
		
		Assert.assertTrue(new Version("1.9").compareTo(new Version("1.9")) == 0);
		Assert.assertTrue(new Version("1.9.1").compareTo(new Version("1.9.1")) == 0);
		
		Assert.assertTrue(new Version("1.9").compareTo(new Version("1.9.0")) == 0);
		Assert.assertTrue(new Version("1.9.0").compareTo(new Version("1.9")) == 0);
		
		Assert.assertTrue(new Version("1.9.1").compareTo(new Version("1.9")) > 0);
		Assert.assertTrue(new Version("1.9").compareTo(new Version("1.9.1")) < 0);
		
		Assert.assertTrue(new Version("1.8").compareTo(new Version("1.9")) < 0);
		Assert.assertTrue(new Version("1.9").compareTo(new Version("1.8")) > 0);
		
		Assert.assertTrue(new Version("2.0").compareTo(new Version("1.9")) > 0);
		Assert.assertTrue(new Version("1.9").compareTo(new Version("2.0")) < 0);
	}
}

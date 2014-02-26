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
package org.openmrs.logic.result;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests the {@link EmptyResult} class
 */
public class EmptyResultTest {
	
	/**
	 * @verifies {@link EmptyResult#isEmpty()}
	 * test = should return true
	 */
	@Test
	public void isEmpty_shouldReturnTrue() throws Exception {
		Assert.assertTrue(new EmptyResult().isEmpty());
	}
	
	/**
	 * @verifies {@link EmptyResult#isNull()}
	 * test = should return true
	 */
	@Test
	public void isNull_shouldReturnTrue() throws Exception {
		Assert.assertTrue(new EmptyResult().isNull());
	}
	
}

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
package org.openmrs;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.openmrs.test.Verifies;

/**
 * Tests methods on the Drug class
 * 
 */
public class DrugTest {
	
	/**
	 * @see {@link Drug#getNumericIdentifier(String)}
	 */
	@Test
	@Verifies(value = "should return numeric identifier of valid string identifier", method = "getNumericIdentifier(Integer)")
	public void getNumericIdentifier_shouldReturnNumericIdentifierOfValidStringIdentifier() throws Exception {
		Integer numericIdentifier = Drug.getNumericIdentifier("org.openmrs.Drug:7");
		Assert.assertEquals(7, numericIdentifier.intValue());
	}
	
	/**
	 * @see {@link Drug#getNumericIdentifier(String)}
	 */
	@Test
	@Verifies(value = "should return null for an invalid string identifier", method = "getNumericIdentifier(Integer)")
	public void getNumericIdentifier_shouldReturnNullForAnInvalidStringIdentifier() throws Exception {
		Logger log = LogManager.getLogger(Drug.class);
		Level initialLevel = log.getLevel();
		log.setLevel(Level.OFF);
		
		Integer numericIdentifier = Drug.getNumericIdentifier("org.openmrs.Drug:7w");
		Assert.assertNull(numericIdentifier);
		
		numericIdentifier = Drug.getNumericIdentifier("org.openmrs.Drug:7  ");
		Assert.assertNull(numericIdentifier);
		
		numericIdentifier = Drug.getNumericIdentifier("some other invalid value");
		Assert.assertNull(numericIdentifier);
		
		numericIdentifier = Drug.getNumericIdentifier("org.openmrs.Drug:");
		Assert.assertNull(numericIdentifier);
		
		log.setLevel(initialLevel);
	}
	
	/**
	 * @see {@link Drug#getNumericIdentifier(String)}
	 */
	@Test(expected = IllegalArgumentException.class)
	@Verifies(value = "fail if null or empty passed in", method = "getNumericIdentifier(String)")
	public void getNumericIdentifier_shouldFailIfNullOrEmptyPassedIn() throws Exception {
		Drug.getNumericIdentifier(null);
		Drug.getNumericIdentifier("");
		Drug.getNumericIdentifier(" ");
	}
}

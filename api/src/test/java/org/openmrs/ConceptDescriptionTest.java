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

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.test.Verifies;

/**
 * Tests for the {@link ConceptDescription} object.
 */
public class ConceptDescriptionTest {
	
	/**
	 * @see {@link ConceptDescription#equals(Object)}
	 */
	@Test
	@Verifies(value = "should compare on id if its non null", method = "equals(Object)")
	public void equals_shouldCompareOnIdIfItsNonNull() throws Exception {
		ConceptDescription firstDesc = new ConceptDescription(1);
		ConceptDescription secondDesc = new ConceptDescription(1);
		Assert.assertTrue(firstDesc.equals(secondDesc));
	}
	
	/**
	 * @see {@link ConceptDescription#equals(Object)}
	 */
	@Test
	@Verifies(value = "should not return true with different objects and null ids", method = "equals(Object)")
	public void equals_shouldNotReturnTrueWithDifferentObjectsAndNullIds() throws Exception {
		ConceptDescription firstDesc = new ConceptDescription();
		ConceptDescription secondDesc = new ConceptDescription();
		Assert.assertFalse(firstDesc.equals(secondDesc));
	}
	
	/**
	 * @see {@link ConceptDescription#equals(Object)}
	 */
	@Test
	@Verifies(value = "should default to object equality", method = "equals(Object)")
	public void equals_shouldDefaultToObjectEquality() throws Exception {
		ConceptDescription firstDesc = new ConceptDescription();
		Assert.assertTrue(firstDesc.equals(firstDesc));
	}
	
}

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
package org.openmrs.comparator;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.PatientIdentifierType;

public class PatientIdentifierTypeDefaultComparatorTest {
	
	/**
	 * Simplified test.
	 * 
	 * @see PatientIdentifierTypeDefaultComparator#compare(PatientIdentifierType,PatientIdentifierType)
	 * @verifies order properly
	 */
	@Test
	public void compare_shouldOrderProperly() throws Exception {
		PatientIdentifierType requiredNotRetired = new PatientIdentifierType();
		requiredNotRetired.setRequired(true);
		requiredNotRetired.setRetired(null);
		PatientIdentifierType requiredRetired1A = new PatientIdentifierType();
		requiredRetired1A.setId(1);
		requiredRetired1A.setRequired(true);
		requiredRetired1A.setRetired(true);
		requiredRetired1A.setName("A");
		PatientIdentifierType requiredRetired2a = new PatientIdentifierType();
		requiredRetired2a.setId(2);
		requiredRetired2a.setRequired(true);
		requiredRetired2a.setRetired(true);
		requiredRetired2a.setName("a");
		
		PatientIdentifierType notRequiredRetired = new PatientIdentifierType();
		notRequiredRetired.setRequired(false);
		notRequiredRetired.setRetired(true);
		PatientIdentifierType notRequiredNotRetiredB = new PatientIdentifierType();
		notRequiredNotRetiredB.setRequired(null);
		notRequiredNotRetiredB.setRetired(false);
		notRequiredNotRetiredB.setName("B");
		PatientIdentifierType notRequiredNotRetiredA = new PatientIdentifierType();
		notRequiredNotRetiredA.setRequired(null);
		notRequiredNotRetiredA.setRetired(false);
		notRequiredNotRetiredA.setName("A");
		
		List<PatientIdentifierType> list = Arrays.asList(notRequiredRetired, requiredRetired2a, notRequiredNotRetiredA,
		    requiredNotRetired, notRequiredNotRetiredB, requiredRetired1A);
		Collections.sort(list, new PatientIdentifierTypeDefaultComparator());
		
		Assert.assertEquals(Arrays.asList(requiredNotRetired, notRequiredNotRetiredA, notRequiredNotRetiredB,
		    requiredRetired1A, requiredRetired2a, notRequiredRetired), list);
	}
}

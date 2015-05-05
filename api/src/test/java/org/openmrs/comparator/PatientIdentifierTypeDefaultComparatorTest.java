/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
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

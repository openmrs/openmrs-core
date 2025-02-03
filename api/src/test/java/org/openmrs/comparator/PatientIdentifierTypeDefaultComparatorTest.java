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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.openmrs.PatientIdentifierType;

public class PatientIdentifierTypeDefaultComparatorTest {
	
	/**
	 * Simplified test.
	 * 
	 * @see PatientIdentifierTypeDefaultComparator#compare(PatientIdentifierType,PatientIdentifierType)
	 */
	@Test
	public void compare_shouldOrderProperly() {
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
		list.sort(new PatientIdentifierTypeDefaultComparator());
		
		assertEquals(Arrays.asList(requiredNotRetired, notRequiredNotRetiredA, notRequiredNotRetiredB,
		    requiredRetired1A, requiredRetired2a, notRequiredRetired), list);
	}

	/*
	 * author: Xin Tang
	 * Partition 1: Retired Status: null first, false second, true last
	 * compare order: retired > name
	 */
	@Test
	public void compare_retiredStatus(){
		PatientIdentifierType requiredNotRetiredA = new PatientIdentifierType();
        requiredNotRetiredA.setRequired(true);
        requiredNotRetiredA.setRetired(false);
		requiredNotRetiredA.setName("A");
        
        PatientIdentifierType requiredUnknownRetiredB = new PatientIdentifierType();
        requiredUnknownRetiredB.setRequired(true);
        requiredUnknownRetiredB.setRetired(null);
		requiredUnknownRetiredB.setName("B");
        
        PatientIdentifierType requiredRetiredC = new PatientIdentifierType();
        requiredRetiredC.setRequired(true);
        requiredRetiredC.setRetired(true);
		requiredRetiredC.setName("C");
        
        List<PatientIdentifierType> list = Arrays.asList(requiredNotRetiredA, requiredUnknownRetiredB, requiredRetiredC);
        list.sort(new PatientIdentifierTypeDefaultComparator());
		assertEquals(Arrays.asList(requiredUnknownRetiredB, requiredNotRetiredA, requiredRetiredC), list);
	}
	/*
	 * author: Xin Tang
	 * Partition 2: Required Status: true first, false second, null last
	 * compare order: required > name
	 */
	@Test
	public void compare_requiredStatus(){
		PatientIdentifierType requiredRetiredA = new PatientIdentifierType();
        requiredRetiredA.setRequired(true);
        requiredRetiredA.setRetired(true);
        requiredRetiredA.setName("A");
        PatientIdentifierType unknownRequiredRetiredB = new PatientIdentifierType();
        unknownRequiredRetiredB.setRequired(null);
        unknownRequiredRetiredB.setRetired(true);
		unknownRequiredRetiredB.setName("B");
        
		PatientIdentifierType notRequiredRetiredC = new PatientIdentifierType();
        notRequiredRetiredC.setRequired(false);
        notRequiredRetiredC.setRetired(true);
        notRequiredRetiredC.setName("C");

        List<PatientIdentifierType> list = Arrays.asList(requiredRetiredA, unknownRequiredRetiredB,  notRequiredRetiredC);
        list.sort(new PatientIdentifierTypeDefaultComparator());
		assertEquals(Arrays.asList(requiredRetiredA, notRequiredRetiredC, unknownRequiredRetiredB), list);
	}
	/*
	 * author: Xin Tang
	 * Partition 2: Required Status: true first, false second, null last
	 * compare order: required > name
	 */
	@Test
    public void compare_requiredAndRetiredOrder(){
		PatientIdentifierType requiredNotRetiredA = new PatientIdentifierType();
        requiredNotRetiredA.setRequired(true);
        requiredNotRetiredA.setRetired(false);
        requiredNotRetiredA.setName("A");
		PatientIdentifierType notRequiredUnknownRetiredZ = new PatientIdentifierType();
        notRequiredUnknownRetiredZ.setRequired(false);
        notRequiredUnknownRetiredZ.setRetired(null);
        notRequiredUnknownRetiredZ.setName("Z");

		List<PatientIdentifierType> list = Arrays.asList(requiredNotRetiredA, notRequiredUnknownRetiredZ);
        list.sort(new PatientIdentifierTypeDefaultComparator());
		assertEquals(Arrays.asList(notRequiredUnknownRetiredZ, requiredNotRetiredA), list);
	}
}

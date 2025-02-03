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
	 * Date: 02/03/2025
	 * Description: Partition 1: Retired Status: null first, false second, true last
	 */
	@Test
	public void compare_retiredStatus(){
		PatientIdentifierType requiredNotRetired1x = new PatientIdentifierType();
        requiredNotRetired1x.setId(1);
        requiredNotRetired1x.setRequired(true);
        requiredNotRetired1x.setRetired(false);
		requiredNotRetired1x.setName("x");
        
        PatientIdentifierType requiredUnknownRetired = new PatientIdentifierType();
        requiredUnknownRetired.setRequired(true);
        requiredUnknownRetired.setRetired(null);
        
        PatientIdentifierType requiredRetired = new PatientIdentifierType();
        requiredRetired.setRequired(true);
        requiredRetired.setRetired(true);
        
        List<PatientIdentifierType> list = Arrays.asList(requiredNotRetired1x, requiredUnknownRetired, requiredRetired);
        list.sort(new PatientIdentifierTypeDefaultComparator());
		assertEquals(Arrays.asList(requiredUnknownRetired, requiredNotRetired1x, requiredRetired), list);
	}
	/*
	 * author: Xin Tang
	 * Date: 02/03/2025
	 * Description: Partition 2: Required Status: false first, true second, null last
	 */
	@Test
	public void compare_requiredStatus(){
		PatientIdentifierType requiredRetired1 = new PatientIdentifierType();
        requiredRetired1.setRequired(true);
        requiredRetired1.setRetired(true);
        
        PatientIdentifierType unknownRequiredRetired = new PatientIdentifierType();
        unknownRequiredRetired.setRequired(null);
        unknownRequiredRetired.setRetired(true);
        
		PatientIdentifierType notRequiredRetired = new PatientIdentifierType();
        notRequiredRetired.setRequired(false);
        notRequiredRetired.setRetired(true);
        
        List<PatientIdentifierType> list = Arrays.asList(requiredRetired1, unknownRequiredRetired,  notRequiredRetired);
        list.sort(new PatientIdentifierTypeDefaultComparator());
		assertEquals(Arrays.asList(requiredRetired1, unknownRequiredRetired, notRequiredRetired), list);
	}

}

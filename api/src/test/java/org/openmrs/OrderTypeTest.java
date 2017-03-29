/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Assert;
import org.junit.Test;

/**
 * This class tests all methods that are not getter or setters in the {@link org.openmrs.OrderType}
 * java object this test class for {@link org.openmrs.OrderType}
 * 
 * @see org.openmrs.OrderType
 */
public class OrderTypeTest {
	
	/**
	 * @see org.openmrs.OrderType#getJavaClass()
	 */
	@Test
	public void setJavaClass_shouldGetJavaClassObject() {
		//Create a new OrderType
		OrderType orderType = new OrderType();
		
		//Test with Integer class
		Class<?> clazz = Integer.class;
		
		orderType.setJavaClassName(clazz.getName());
		Assert.assertEquals(clazz, orderType.getJavaClass());
	}
	
	/**
	 * @see OrderType#addConceptClass(ConceptClass)
	 */
	@Test
	public void addConceptClass_shouldAddTheSpecifiedConceptClass() {
		OrderType ot = new OrderType();
		ConceptClass cc = new ConceptClass();
		ot.addConceptClass(cc);
		assertTrue(ot.getConceptClasses().contains(cc));
	}
	
	/**
	 * Ensures that if the collection implementation gets changed from a set, that duplicates are
	 * not added
	 * 
	 * @see OrderType#addConceptClass(ConceptClass)
	 */
	@Test
	public void addConceptClass_shouldNotAddADuplicateConceptClass() {
		OrderType ot = new OrderType();
		ConceptClass cc1 = new ConceptClass();
		ot.addConceptClass(cc1);
		ot.addConceptClass(cc1);
		assertTrue(ot.getConceptClasses().contains(cc1));
		assertEquals(1, ot.getConceptClasses().size());
	}
}

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

/**
 * This is a type of order that adds tests specific attributes like: laterality, clinical history,
 * etc.
 * 
 * @since 1.9.2, 1.10
 */
public class TestOrder extends ServiceOrder {
	
	public static final long serialVersionUID = 1L;
	
	/**
	 * Default Constructor
	 */
	public TestOrder() {
		
	}
	
	/**
	 * @see org.openmrs.ServiceOrder#copy()
	 */
	@Override
	public TestOrder copy() {
		TestOrder newOrder = new TestOrder();
		super.copyHelper(newOrder);
		return newOrder;
	}
	
	/**
	 * Creates a discontinuation order for this.
	 * 
	 * @see org.openmrs.ServiceOrder#cloneForDiscontinuing()
	 * @return the newly created order
	 */
	@Override
	public TestOrder cloneForDiscontinuing() {
		TestOrder newOrder = new TestOrder();
		super.cloneForDiscontinuingHelper(newOrder);
		return newOrder;
	}
	
	/**
	 * Creates a TestOrder for revision from this order, sets the previousOrder, action field and
	 * other test order fields.
	 * 
	 * @return the newly created order
	 */
	@Override
	public TestOrder cloneForRevision() {
		TestOrder newOrder = new TestOrder();
		super.cloneForRevisionHelper(newOrder);
		return newOrder;
	}
}

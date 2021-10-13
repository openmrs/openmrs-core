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
 * This is a type of order that adds referral specific attributes.
 * 
 * @since 2.5.0
 */
public class ReferralOrder extends ServiceOrder {

	private static final long serialVersionUID = 1L;
	
	/**
	 * Default Constructor
	 */
	public ReferralOrder() {
		
	}
	
	/**
	 * @see org.openmrs.ServiceOrder#copy()
	 */
	@Override
	public ReferralOrder copy() {
		ReferralOrder newOrder = new ReferralOrder();
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
	public ReferralOrder cloneForDiscontinuing() {
		ReferralOrder newOrder = new ReferralOrder();
		super.cloneForDiscontinuingHelper(newOrder);
		return newOrder;
	}
	
	/**
	 * Creates a ReferralOrder for revision from this order, sets the previousOrder, action field and
	 * other test order fields.
	 * 
	 * @return the newly created order
	 */
	@Override
	public ReferralOrder cloneForRevision() {
		ReferralOrder newOrder = new ReferralOrder();
		super.cloneForRevisionHelper(newOrder);
		return newOrder;
	}
}

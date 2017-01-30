/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.db.hibernate;

import org.openmrs.Order;
import org.springframework.stereotype.Component;

/**
 * ImmutableEntityInterceptor for Orders, ensures that orders fields don't get edited except for
 * dateStopped, voided, dateVoided, voidedBy, voidReason
 * 
 * @see ImmutableEntityInterceptor
 * @since 1.10
 */
@Component("immutableOrderInterceptor")
public class ImmutableOrderInterceptor extends ImmutableEntityInterceptor {
	
	private static final String[] MUTABLE_PROPERTY_NAMES = new String[] { "dateStopped", "voided", "dateVoided", "voidedBy",
	        "voidReason", "patient" };
	
	/**
	 * @see ImmutableEntityInterceptor#getSupportedType()
	 */
	@Override
	protected Class<?> getSupportedType() {
		return Order.class;
	}
	
	/**
	 * @see org.openmrs.api.db.hibernate.ImmutableEntityInterceptor#getMutablePropertyNames()
	 */
	@Override
	protected String[] getMutablePropertyNames() {
		return MUTABLE_PROPERTY_NAMES;
	}
	
	/**
	 * @see ImmutableEntityInterceptor#ignoreVoidedOrRetiredObjects()
	 */
	@Override
	protected boolean ignoreVoidedOrRetiredObjects() {
		return true;
	}
}

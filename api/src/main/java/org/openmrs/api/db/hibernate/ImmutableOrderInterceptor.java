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
	        "voidReason" };
	
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

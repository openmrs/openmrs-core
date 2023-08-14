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

import org.openmrs.Obs;
import org.springframework.stereotype.Component;

/**
 * ImmutableEntityInterceptor for Obs, it catches any edited Obs, voids and replaces it with a new
 * one. I also sets the original Obs as the previous Obs for the newly created one. The exceptions
 * are when editing an already voided Obs
 * 
 * @see ImmutableEntityInterceptor
 * @since 2.0.0
 */
@Component("immutableObsInterceptor")
public class ImmutableObsInterceptor extends ImmutableEntityInterceptor {
	
	private static final String[] MUTABLE_PROPERTY_NAMES = new String[] { "voided", "dateVoided", "voidedBy", "voidReason", "groupMembers" };
	
	/**
	 * @see ImmutableEntityInterceptor#getSupportedType()
	 */
	@Override
	protected Class<?> getSupportedType() {
		return Obs.class;
	}
	
	/**
	 * @see ImmutableEntityInterceptor#getMutablePropertyNames()
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

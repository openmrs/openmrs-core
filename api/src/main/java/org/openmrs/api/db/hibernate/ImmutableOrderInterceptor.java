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
 * those in {@code MUTABLE_PROPERTY_NAMES}, whether or not the order is voided. An order only moves
 * to another patient to follow its encounter or in a patient merge.
 *
 * @see ImmutableEntityInterceptor
 * @since 1.10
 */
@Component("immutableOrderInterceptor")
public class ImmutableOrderInterceptor extends ImmutableEntityInterceptor {

	private static final String[] MUTABLE_PROPERTY_NAMES = new String[] { "dateStopped", "voided", "dateVoided", "voidedBy",
	        "changedBy", "dateChanged", "voidReason", "fulfillerStatus", "fulfillerComment", "accessionNumber" };

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
	 * Lets an order move to another patient only to the patient of its encounter, or in a patient
	 * merge.
	 *
	 * @see ImmutableEntityInterceptor#isAllowedChange(Object, String, Object, Object)
	 */
	@Override
	protected boolean isAllowedChange(Object entity, String property, Object previousValue, Object currentValue) {
		return "patient".equals(property) && isAllowedPersonChange(((Order) entity).getEncounter(),
		    toPersonId(previousValue), toPersonId(currentValue));
	}
}

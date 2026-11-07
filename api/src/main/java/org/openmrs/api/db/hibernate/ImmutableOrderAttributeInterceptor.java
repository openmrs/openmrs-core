/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of this file was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.db.hibernate;

import org.openmrs.OrderAttribute;
import org.springframework.stereotype.Component;

/**
 * ImmutableEntityInterceptor for OrderAttribute, ensures that a saved attribute keeps its value;
 * changes go through {@link org.openmrs.BaseCustomizableData#setAttribute}, which voids the saved
 * attribute and adds a new one. Apart from its void fields, a saved attribute row is never edited.
 *
 * @see ImmutableEntityInterceptor
 * @since 2.9.0
 */
@Component("immutableOrderAttributeInterceptor")
public class ImmutableOrderAttributeInterceptor extends ImmutableEntityInterceptor {

	private static final String[] MUTABLE_PROPERTY_NAMES = new String[] { "voided", "dateVoided", "voidedBy", "voidReason",
	        "changedBy", "dateChanged" };

	/**
	 * @see ImmutableEntityInterceptor#getSupportedType()
	 */
	@Override
	protected Class<?> getSupportedType() {
		return OrderAttribute.class;
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
		return false;
	}
}

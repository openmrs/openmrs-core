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

import org.openmrs.Obs;
import org.openmrs.ObsReferenceRange;
import org.springframework.stereotype.Component;

/**
 * ImmutableEntityInterceptor for ObsReferenceRange, ensures that the values of a saved reference
 * range row don't get edited. Core sets the reference range only when an obs is first saved and
 * copies it onto a new row when the obs is edited (see {@link Obs#newInstance(Obs)}), so a saved
 * row always keeps the values its obs was saved with.
 *
 * @see ImmutableEntityInterceptor
 * @since 2.9.0
 */
@Component("immutableObsReferenceRangeInterceptor")
public class ImmutableObsReferenceRangeInterceptor extends ImmutableEntityInterceptor {

	private static final String[] MUTABLE_PROPERTY_NAMES = new String[] {};

	/**
	 * @see ImmutableEntityInterceptor#getSupportedType()
	 */
	@Override
	protected Class<?> getSupportedType() {
		return ObsReferenceRange.class;
	}

	/**
	 * @see ImmutableEntityInterceptor#getMutablePropertyNames()
	 */
	@Override
	protected String[] getMutablePropertyNames() {
		return MUTABLE_PROPERTY_NAMES;
	}
}

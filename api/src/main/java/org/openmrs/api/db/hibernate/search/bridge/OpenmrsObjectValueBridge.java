/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.db.hibernate.search.bridge;

import org.hibernate.search.mapper.pojo.bridge.ValueBridge;
import org.hibernate.search.mapper.pojo.bridge.runtime.ValueBridgeToIndexedValueContext;
import org.openmrs.OpenmrsObject;

/**
 * Indexes {@link OpenmrsObject} as ID.
 * 
 * @since 2.8.0
*/
public class OpenmrsObjectValueBridge implements ValueBridge<OpenmrsObject, String> {

	@Override
	public String toIndexedValue(OpenmrsObject openmrsObject, ValueBridgeToIndexedValueContext valueBridgeToIndexedValueContext) {
		return openmrsObject.getId().toString();
	}
}

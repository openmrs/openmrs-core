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

import org.hibernate.search.bridge.StringBridge;
import org.openmrs.OpenmrsObject;

/**
 * Indexes {@link OpenmrsObject} as ID.
 */
public class OpenmrsObjectFieldBridge implements StringBridge {
	
	/**
	 * @see org.hibernate.search.bridge.StringBridge#objectToString(java.lang.Object)
	 */
	@Override
	public String objectToString(Object obj) {
		OpenmrsObject openmrsObject = (OpenmrsObject) obj;
		return openmrsObject.getId().toString();
	}
	
}

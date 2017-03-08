/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.customdatatype.datatype;

import org.openmrs.OpenmrsObject;
import org.openmrs.customdatatype.SerializingCustomDatatype;

/**
 * This is a superclass for custom datatypes for OpenmrsObjects
 * 
 * @since 2.0.0
 */
public abstract class BaseOpenmrsDatatype<T extends OpenmrsObject> extends SerializingCustomDatatype<T> {
	
	/**
	 * @see org.openmrs.customdatatype.SerializingCustomDatatype#serialize(java.lang.Object)
	 * @should return the uuid of the object
	 */
	@Override
	public String serialize(T typedValue) {
		if (typedValue == null) {
			return null;
		}
		return typedValue.getUuid();
	}
}

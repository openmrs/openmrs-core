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

import org.apache.commons.lang3.StringUtils;
import org.openmrs.customdatatype.SerializingCustomDatatype;
import org.springframework.stereotype.Component;

/**
 * Datatype for float, represented by java.lang.Float
 * @since 2.0
 */
@Component
public class FloatDatatype extends SerializingCustomDatatype<Float> {

	/**
	 * @see SerializingCustomDatatype#serialize(Object)
	 */
	@Override
	public String serialize(Float typedValue) {
		if (typedValue == null) {
			return null;
		}
		return typedValue.toString();
	}

	/**
	 * @see SerializingCustomDatatype#deserialize(String)
	 */
	@Override
	public Float deserialize(String serializedValue) {
		if (StringUtils.isEmpty(serializedValue)) {
			return null;
		}
		return Float.valueOf(serializedValue);
	}
	
}

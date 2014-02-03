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
package org.openmrs.customdatatype.datatype;

import org.apache.commons.lang.StringUtils;
import org.openmrs.customdatatype.SerializingCustomDatatype;
import org.springframework.stereotype.Component;

/**
 * Datatype for boolean, represented by java.lang.Boolean.
 * @since 1.9
 */
@Component
public class BooleanDatatype extends SerializingCustomDatatype<Boolean> {
	
	/**
	 * @see org.openmrs.customdatatype.SerializingCustomDatatype#serialize(java.lang.Object)
	 */
	@Override
	public String serialize(Boolean typedValue) {
		if (typedValue == null) {
			return null;
		}
		return typedValue.toString();
	}
	
	/**
	 * @see org.openmrs.customdatatype.SerializingCustomDatatype#deserialize(java.lang.String)
	 */
	@Override
	public Boolean deserialize(String serializedValue) {
		if (StringUtils.isEmpty(serializedValue)) {
			return null;
		}
		return Boolean.valueOf(serializedValue);
	}
	
}

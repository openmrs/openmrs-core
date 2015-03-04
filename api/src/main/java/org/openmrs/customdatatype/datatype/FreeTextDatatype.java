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

import org.openmrs.customdatatype.SerializingCustomDatatype;
import org.springframework.stereotype.Component;

/**
 * Free-text datatype, represented by a plain String.
 * @since 1.9
 */
@Component
public class FreeTextDatatype extends SerializingCustomDatatype<String> {
	
	/**
	 * @see org.openmrs.customdatatype.SerializingCustomDatatype#serialize(java.lang.Object)
	 */
	@Override
	public String serialize(String typedValue) {
		return typedValue;
	}
	
	/**
	 * @see org.openmrs.customdatatype.SerializingCustomDatatype#deserialize(java.lang.String)
	 */
	@Override
	public String deserialize(String serializedValue) {
		return serializedValue;
	}
	
}

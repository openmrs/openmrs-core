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

import java.util.regex.Pattern;

import org.openmrs.customdatatype.InvalidCustomValueException;
import org.openmrs.customdatatype.SerializingCustomDatatype;
import org.springframework.stereotype.Component;

/**
 * Datatype for a String that is validated against a regular expression
 * @since 1.9
 */
@Component
public class RegexValidatedTextDatatype extends SerializingCustomDatatype<String> {
	
	private Pattern pattern;
	
	/**
	 * @see org.openmrs.customdatatype.CustomDatatype#setConfiguration(java.lang.String)
	 */
	@Override
	public void setConfiguration(String regex) {
		pattern = Pattern.compile(regex);
	}
	
	/**
	 * @see org.openmrs.customdatatype.SerializingCustomDatatype#serialize(java.lang.Object)
	 * @should fail if the string does not match the regex
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
	
	/**
	 * @see org.openmrs.customdatatype.CustomDatatype#validate(java.lang.Object)
	 * @should accept a string that matches the regex
	 * @should fail if the string does not match the regex
	 */
	@Override
	public void validate(String typedValue) throws InvalidCustomValueException {
		if (!pattern.matcher(typedValue).matches()) {
			throw new InvalidCustomValueException("Doesn't match regex");
		}
	}
	
}

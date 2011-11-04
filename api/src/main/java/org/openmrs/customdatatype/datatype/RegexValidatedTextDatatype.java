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

import java.util.regex.Pattern;

import org.openmrs.customdatatype.CustomDatatype;
import org.openmrs.customdatatype.InvalidCustomValueException;
import org.springframework.stereotype.Component;

/**
 * Datatype for a java.lang.String that is validated against a regular expression
 * @since 1.9
 */
@Component
public class RegexValidatedTextDatatype implements CustomDatatype<String> {
	
	private Pattern pattern;
	
	/**
	 * @see org.openmrs.customdatatype.CustomDatatype#setConfiguration(java.lang.String)
	 */
	@Override
	public void setConfiguration(String regex) {
		pattern = Pattern.compile(regex);
	}
	
	/**
	 * @see org.openmrs.customdatatype.CustomDatatype#toReferenceString(java.lang.Object)
	 * @should fail if the string does not match the regex
	 */
	@Override
	public String toReferenceString(String typedValue) throws InvalidCustomValueException {
		validate(typedValue);
		return typedValue;
	}
	
	/**
	 * @see org.openmrs.customdatatype.CustomDatatype#fromReferenceString(java.lang.String)
	 */
	@Override
	public String fromReferenceString(String persistedValue) throws InvalidCustomValueException {
		validate(persistedValue);
		return persistedValue;
	}
	
	/**
	 * @see org.openmrs.customdatatype.CustomDatatype#render(java.lang.String, java.lang.String)
	 */
	@Override
	public String render(String persistedValue, String view) {
		return persistedValue;
	}
	
	/**
	 * @see org.openmrs.customdatatype.CustomDatatype#validateReferenceString(java.lang.String)
	 */
	@Override
	public void validateReferenceString(String persistedValue) throws InvalidCustomValueException {
		validate(persistedValue);
	}
	
	/**
	 * @see org.openmrs.customdatatype.CustomDatatype#validate(java.lang.Object)
	 * @should accept a string that matches the regex
	 * @should fail if the string does not match the regex
	 */
	@Override
	public void validate(String typedValue) throws InvalidCustomValueException {
		if (!pattern.matcher(typedValue).matches())
			throw new InvalidCustomValueException("Doesn't match regex");
	}
	
}

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
import org.openmrs.customdatatype.CustomDatatype;
import org.openmrs.customdatatype.InvalidCustomValueException;
import org.springframework.stereotype.Component;

/**
 * Datatype for boolean, represented by java.lang.Boolean.
 * @since 1.9
 */
@Component
public class Boolean implements CustomDatatype<java.lang.Boolean> {
	
	/**
	 * @see org.openmrs.customdatatype.CustomDatatype#setConfiguration(java.lang.String)
	 */
	@Override
	public void setConfiguration(String config) {
		// not used
	}
	
	/**
	 * @see org.openmrs.customdatatype.CustomDatatype#toReferenceString(java.lang.Object)
	 */
	@Override
	public String toReferenceString(java.lang.Boolean typedValue) throws InvalidCustomValueException {
		return typedValue.toString();
	}
	
	/**
	 * @see org.openmrs.customdatatype.CustomDatatype#fromReferenceString(java.lang.String)
	 */
	@Override
	public java.lang.Boolean fromReferenceString(String persistedValue) throws InvalidCustomValueException {
		if (StringUtils.isEmpty(persistedValue))
			return null;
		return java.lang.Boolean.valueOf(persistedValue);
	}
	
	/**
	 * @see org.openmrs.customdatatype.CustomDatatype#render(java.lang.String, java.lang.String)
	 */
	@Override
	public String render(String referenceString, String view) {
		return referenceString;
	}
	
	/**
	 * @see org.openmrs.customdatatype.CustomDatatype#validateReferenceString(java.lang.String)
	 */
	@Override
	public void validateReferenceString(String persistedValue) throws InvalidCustomValueException {
		if (!(persistedValue == null || "".equals(persistedValue) || "true".equals(persistedValue) || "false"
		        .equals(persistedValue)))
			throw new InvalidCustomValueException("Must be \"true\" or \"false\"");
	}
	
	/**
	 * @see org.openmrs.customdatatype.CustomDatatype#validate(java.lang.Object)
	 */
	@Override
	public void validate(java.lang.Boolean typedValue) throws InvalidCustomValueException {
		// any java.lang.Boolean is legal
	}
	
}

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

import org.openmrs.customdatatype.CustomDatatype;
import org.openmrs.customdatatype.InvalidCustomValueException;
import org.springframework.stereotype.Component;

/**
 * Free-text datatype, represented by a plain String.
 * @since 1.9
 */
@Component
public class FreeText implements CustomDatatype<String> {
	
	/**
	 * @see org.openmrs.customdatatype.CustomDatatype#toReferenceString(java.lang.Object)
	 */
	@Override
	public String toReferenceString(String typedValue) throws InvalidCustomValueException {
		return (String) typedValue;
	}
	
	/**
	 * @see org.openmrs.customdatatype.CustomDatatype#fromReferenceString(java.lang.String)
	 */
	@Override
	public String fromReferenceString(String persistedValue) throws InvalidCustomValueException {
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
		// pass
	}
	
	/**
	 * @see org.openmrs.customdatatype.CustomDatatype#validate(java.lang.Object)
	 */
	@Override
	public void validate(String typedValue) throws InvalidCustomValueException {
		//pass
	}
	
	/**
	 * @see org.openmrs.customdatatype.CustomDatatype#setConfiguration(java.lang.String)
	 */
	@Override
	public void setConfiguration(String config) {
		// not used
	}
	
}

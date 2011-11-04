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

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.openmrs.customdatatype.CustomDatatype;
import org.openmrs.customdatatype.InvalidCustomValueException;
import org.springframework.stereotype.Component;

/**
 * Datatype for a Date (without time), represented by a java.util.Date. 
 * @since 1.9
 */
@Component
public class DateDatatype implements CustomDatatype<Date> {
	
	final static String dateFormat = "yyyy-MM-dd";
	
	/**
	 * @see org.openmrs.customdatatype.CustomDatatype#setConfiguration(java.lang.String)
	 */
	@Override
	public void setConfiguration(String config) {
		// not used
	}
	
	/**
	 * @see org.openmrs.customdatatype.CustomDatatype#toReferenceString(java.lang.Object)
	 * @should convert a date into a ymd string representation
	 */
	@Override
	public String toReferenceString(Date typedValue) throws InvalidCustomValueException {
		return new SimpleDateFormat(dateFormat).format(typedValue);
	}
	
	/**
	 * @see org.openmrs.customdatatype.CustomDatatype#fromReferenceString(java.lang.String)
	 * @should reconstruct a date serialized by this handler
	 */
	@Override
	public Date fromReferenceString(String persistedValue) throws InvalidCustomValueException {
		if (StringUtils.isBlank(persistedValue))
			return null;
		try {
			return new SimpleDateFormat(dateFormat).parse(persistedValue);
		}
		catch (Exception ex) {
			throw new InvalidCustomValueException("Invalid date: " + persistedValue);
		}
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
		validate(fromReferenceString(persistedValue));
	}
	
	/**
	 * @see org.openmrs.customdatatype.CustomDatatype#validate(java.lang.Object)
	 */
	@Override
	public void validate(Date typedValue) throws InvalidCustomValueException {
		// pass
	}
	
}

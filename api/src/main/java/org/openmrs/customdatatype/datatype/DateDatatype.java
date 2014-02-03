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
import org.openmrs.api.context.Context;
import org.openmrs.customdatatype.CustomDatatype;
import org.openmrs.customdatatype.InvalidCustomValueException;
import org.openmrs.customdatatype.SerializingCustomDatatype;
import org.springframework.stereotype.Component;

/**
 * Datatype for a Date (without time), represented by a java.util.Date. 
 * @since 1.9
 */
@Component
public class DateDatatype extends SerializingCustomDatatype<Date> {
	
	final static String dateFormat = "yyyy-MM-dd";
	
	/**
	 * @see org.openmrs.customdatatype.SerializingCustomDatatype#doGetTextSummary(java.lang.Object)
	 */
	@Override
	public CustomDatatype.Summary doGetTextSummary(Date typedValue) {
		return new CustomDatatype.Summary(Context.getDateFormat().format(typedValue), true);
	}
	
	/**
	 * @see org.openmrs.customdatatype.SerializingCustomDatatype#deserialize(java.lang.String)
	 * @should reconstruct a date serialized by this handler
	 */
	@Override
	public Date deserialize(String serializedValue) {
		if (StringUtils.isBlank(serializedValue)) {
			return null;
		}
		try {
			return new SimpleDateFormat(dateFormat).parse(serializedValue);
		}
		catch (Exception ex) {
			throw new InvalidCustomValueException("Invalid date: " + serializedValue);
		}
	}
	
	/**
	 * @see org.openmrs.customdatatype.SerializingCustomDatatype#serialize(java.lang.Object)
	 * @should convert a date into a ymd string representation
	 */
	@Override
	public String serialize(Date typedValue) {
		return new SimpleDateFormat(dateFormat).format(typedValue);
	}
	
}

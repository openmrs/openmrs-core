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
package org.openmrs.serialization.xstream.converter;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.SingleValueConverter;

/**
 * This converter will serialize/deserialize all Timestamp objects in the format
 * "yyyy-MM-dd HH:mm:ss z".
 */
public class CustomSQLTimestampConverter implements SingleValueConverter {
	
	/**
	 * @see com.thoughtworks.xstream.converters.ConverterMatcher#canConvert(java.lang.Class)
	 */
	public boolean canConvert(Class c) {
		return Timestamp.class.isAssignableFrom(c);
	}
	
	/**
	 * Expects strings like "2006-10-20 00:00:00 CST".
	 * 
	 * @see com.thoughtworks.xstream.converters.SingleValueConverter#fromString(java.lang.String)
	 */
	public Object fromString(String s) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
		Date date = null;
		try {
			date = sdf.parse(s);
		}
		catch (ParseException e) {
			throw new ConversionException("Cannot parse date " + s);
		}
		return new Timestamp(date.getTime());
	}
	
	/**
	 * Returns strings like "2006-10-20 00:00:00 CST".
	 * 
	 * @see com.thoughtworks.xstream.converters.SingleValueConverter#toString(java.lang.Object)
	 */
	public String toString(Object o) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
		return sdf.format((Timestamp) o);
	}
	
}

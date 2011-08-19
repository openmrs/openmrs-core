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
package org.openmrs.attribute.handler;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.openmrs.api.context.Context;
import org.openmrs.attribute.AttributeUtil;
import org.openmrs.attribute.InvalidAttributeValueException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Handler for the "date" datatype. Unless specifically configured, it does not allow future dates.
 * @since 1.9 
 */
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class DateAttributeHandler implements AttributeHandler<Date> {
	
	final static String dateFormat = "yyyy-MM-dd";
	
	boolean allowFutureDates = false;
	
	/**
	 * @see org.openmrs.attribute.handler.AttributeHandler#getDatatypeHandled()
	 */
	@Override
	public String getDatatypeHandled() {
		return "date";
	}
	
	/**
	 * @see org.openmrs.attribute.handler.AttributeHandler#setConfiguration(java.lang.String)
	 */
	@Override
	public void setConfiguration(String configString) {
		Map<String, String> config = AttributeUtil.deserializeSimpleConfiguration(configString);
		if (config.containsKey("allowFutureDates"))
			allowFutureDates = Boolean.valueOf(config.get("allowFutureDates"));
		
	}
	
	/**
	 * Does not allow future dates, unless configuration specifically allows them.
	 * @see org.openmrs.attribute.handler.AttributeHandler#validate(java.lang.Object)
	 */
	@Override
	public void validate(Date date) throws InvalidAttributeValueException {
		if (!allowFutureDates && date != null && date.after(new Date()))
			throw new InvalidAttributeValueException("Future dates not allowed");
	}
	
	/**
	 * @see org.openmrs.attribute.handler.AttributeHandler#serialize(java.lang.Object)
	 * @should convert a date into a ymd string representation
	 */
	@Override
	public String serialize(Object date) {
		return new SimpleDateFormat(dateFormat).format(date);
	}
	
	/**
	 * @see org.openmrs.attribute.handler.AttributeHandler#deserialize(java.lang.String)
	 * @should reconstruct a date serialized by this handler
	 */
	@Override
	public Date deserialize(String stringValue) throws InvalidAttributeValueException {
		try {
			return new SimpleDateFormat(dateFormat).parse(stringValue);
		}
		catch (ParseException ex) {
			try {
				return Context.getDateFormat().parse(stringValue);
			}
			catch (ParseException ex2) {
				throw new InvalidAttributeValueException("Cannot convert: " + stringValue, ex);
			}
		}
	}
	
}

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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.openmrs.api.context.Context;
import org.openmrs.attribute.AttributeUtil;
import org.openmrs.attribute.InvalidAttributeValueException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Handler for the "race" datatype. Unless specifically configured, it allows only a fixed set of values.
 * @since 1.9 
 */
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class RaceAttributeHandler implements AttributeHandler<String> {
	
	private static final Set<String> races = new HashSet<String>();
	
	boolean allowCustomRace = false;
	
	static {
		races.add("Cauasoid");
		races.add("Negroid");
		races.add("Mongoloid");
		races.add("Australoid");
	}
	
	/**
	 * @see org.openmrs.attribute.handler.AttributeHandler#getDatatypeHandled()
	 */
	@Override
	public String getDatatypeHandled() {
		return "race";
	}
	
	/**
	 * @see org.openmrs.attribute.handler.AttributeHandler#setConfiguration(java.lang.String)
	 */
	@Override
	public void setConfiguration(String configString) {
		Map<String, String> config = AttributeUtil.deserializeSimpleConfiguration(configString);
		if (config.containsKey("allowCustomRace"))
			allowCustomRace = Boolean.valueOf(config.get("allowCustomRace"));
		
	}
	
	/**
	 * Does not allow future dates, unless configuration specifically allows them.
	 * @see org.openmrs.attribute.handler.AttributeHandler#validate(java.lang.Object)
	 */
	@Override
	public void validate(String race) throws InvalidAttributeValueException {
		if (!allowCustomRace && !races.contains(race))
			throw new InvalidAttributeValueException("Custom races not allowed");
	}
	
	/**
	 * @see org.openmrs.attribute.handler.AttributeHandler#serialize(java.lang.Object)
	 * @should convert a date into a ymd string representation
	 */
	@Override
	public String serialize(Object race) {
		String value = (String) race;
		validate(value);
		return value;
		
	}
	
	/**
	 * @see org.openmrs.attribute.handler.AttributeHandler#deserialize(java.lang.String)
	 * @should reconstruct a date serialized by this handler
	 */
	@Override
	public String deserialize(String stringValue) throws InvalidAttributeValueException {
		return stringValue;
	}
}

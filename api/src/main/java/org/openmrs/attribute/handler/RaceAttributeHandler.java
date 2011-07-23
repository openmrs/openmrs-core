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

import org.apache.commons.lang.StringUtils;
import org.openmrs.attribute.InvalidAttributeValueException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 */
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class RaceAttributeHandler implements AttributeHandler<RaceAttributeHandler.Race> {
	
	@Override
	public String getDatatypeHandled() {
		return "race";
	}
	
	@Override
	public void setConfiguration(String handlerConfig) {
		
	}
	
	@Override
	public void validate(Race typedValue) throws InvalidAttributeValueException {
		
	}
	
	@Override
	public String serialize(Object typedValue) {
		Race r = (Race) typedValue;
		return r.getValue();
	}
	
	@Override
	public Race deserialize(String stringValue) throws InvalidAttributeValueException {
		for (Race race : Race.values()) {
			if (StringUtils.equalsIgnoreCase(stringValue, race.getValue()))
				return race;
		}
		return null;
	}
	
	public enum Race implements StringEnum {
		RACE_CAUASOID("Cauasoid"), RACE_NEGROID("Negroid"), RACE_MONGOLOID("Mongoloid"), RACE_AUSTRALOID("Australoid");
		
		private final String value;
		
		private Race(final String value) {
			this.value = value;
		}
		
		public String getValue() {
			return value;
		}
		
		public String toString() {
			return getValue();
		}
	}
	
}

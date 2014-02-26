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
package org.openmrs.api.db.hibernate;

import java.util.Map;

import org.apache.commons.collections.Predicate;
import org.openmrs.attribute.Attribute;
import org.openmrs.attribute.AttributeType;
import org.openmrs.customdatatype.Customizable;

/**
 * Used in conjunction with commons-collections filter to find attributes with values in the given map.
 * @since 1.9
 */
public class AttributeMatcherPredicate<T extends Customizable, AT extends AttributeType> implements Predicate {
	
	private final Map<AT, String> serializedAttributeValues;
	
	public AttributeMatcherPredicate(Map<AT, String> serializedAttributeValues) {
		this.serializedAttributeValues = serializedAttributeValues;
	}
	
	@Override
	public boolean evaluate(Object o) {
		final T customizable = (T) o;
		for (Map.Entry<AT, String> entry : serializedAttributeValues.entrySet()) {
			for (Object attr : customizable.getActiveAttributes(entry.getKey())) {
				Attribute attribute = (Attribute) attr;
				if (attribute.getValueReference().equals(entry.getValue())) {
					return true;
				}
			}
		}
		return false;
	}
}

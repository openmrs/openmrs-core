/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
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

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
package org.openmrs.attribute;

import org.openmrs.OpenmrsData;
import org.openmrs.attribute.handler.AttributeHandler;

/**
 * Common interface for user-defined attribute that is are added to a base class.
 * Every attribute corresponds to a {@link AttributeType}, which defines, among other things, whether it
 * is required, whether it may repeat, and how it is serialized and deserialized for storage.
 * These attribute types are intended for use cases that would involve adding custom columns to the base
 * table in a less generic system. 
 * For example Visit has VisitAttributes (which implements Attribute<Visit>) that are defined by
 * VisitAttributeTypes.
 * @param <OwningType> the type this attribute can belong to
 * @see AttributeType
 * @see AttributeHolder
 * @see AttributeHandler
 * @since 1.9
 */
public interface Attribute<OwningType extends AttributeHolder<?>> extends OpenmrsData {
	
	/**
	 * @return the owner that this attribute belongs to
	 */
	OwningType getOwner();
	
	/**
	 * Sets the owner of this attribute
	 * 
	 * @param owner
	 */
	void setOwner(OwningType owner);
	
	/**
	 * @return the AttributeType that controls this attribute's behavior
	 */
	AttributeType<OwningType> getAttributeType();
	
	/**
	 * @return the serialized String representation of the attribute, suitable for storing in a database.
	 */
	String getSerializedValue();
	
	/**
	 * Sets the value of this attribute directly to a pre-serialized value. Implementations should
	 * validate this value, rather than setting it blindly, otherwise fetching the value back may throw
	 * an {@link InvalidAttributeValueException}
	 * If you are coding against the OpenMRS API, you probably want to use {@link #setObjectValue(Object)}
	 * instead of this method.
	 * @param serializedValue
	 */
	void setSerializedValue(String serializedValue);
	
	/**
	 * Convenience method to get the typed value of an attribute. (This will result in a background call
	 * to the attribute's type's handler.)
	 * @return the typed value of an attribute
	 * @throws InvalidAttributeValueException 
	 * @see {@link AttributeHandler}
	 */
	Object getObjectValue() throws InvalidAttributeValueException;
	
	/**
	 * Sets the value of this attribute. (This will result in a background call to the attribute's type's
	 * handler's validate and serialize methods.)
	 * @param typedValue
	 * @throws InvalidAttributeValueException
	 * @see {@link AttributeHandler} 
	 */
	<S, T extends S> void setObjectValue(T typedValue) throws InvalidAttributeValueException;
	
}

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

import org.openmrs.BaseOpenmrsData;
import org.openmrs.api.context.Context;
import org.openmrs.attribute.handler.AttributeHandler;

/**
 * Abstract base implementation of {@link Attribute}. Actual implementations should be able to extend this
 * class, and have very little of their own code.  
 * @param <OwningType>
 * @since 1.9
 */
public abstract class BaseAttribute<OwningType extends Customizable<?>> extends BaseOpenmrsData implements Attribute<OwningType> {
	
	private OwningType owner;
	
	private AttributeType<OwningType> attributeType;
	
	private String serializedValue;
	
	/**
	 * @see org.openmrs.attribute.Attribute#getOwner()
	 */
	@Override
	public OwningType getOwner() {
		return owner;
	}
	
	/**
	 * @see org.openmrs.attribute.Attribute#setOwner(org.openmrs.attribute.Customizable)
	 */
	public void setOwner(OwningType owner) {
		this.owner = owner;
	}
	
	/**
	 * @param attributeType the attributeType to set
	 */
	public void setAttributeType(AttributeType<OwningType> attributeType) {
		this.attributeType = attributeType;
	}
	
	/**
	 * @see org.openmrs.attribute.Attribute#getAttributeType()
	 */
	@Override
	public AttributeType<OwningType> getAttributeType() {
		return attributeType;
	}
	
	/**
	 * @see org.openmrs.attribute.Attribute#getSerializedValue()
	 */
	@Override
	public String getSerializedValue() {
		return serializedValue;
	}
	
	/**
	 * @see org.openmrs.attribute.Attribute#setSerializedValue(java.lang.String)
	 */
	@Override
	public void setSerializedValue(String serializedValue) {
		this.serializedValue = serializedValue;
	}
	
	/**
	 * @see org.openmrs.attribute.Attribute#getObjectValue()
	 */
	@Override
	public Object getObjectValue() throws InvalidAttributeValueException {
		return getHandler().deserialize(serializedValue);
	}
	
	/**
	 * @see org.openmrs.attribute.Attribute#setObjectValue(java.lang.Object)
	 */
	@Override
	public <S, T extends S> void setObjectValue(T typedValue) throws InvalidAttributeValueException {
		@SuppressWarnings("unchecked")
		AttributeHandler<S> handler = (AttributeHandler<S>) getHandler();
		handler.validate(typedValue);
		setSerializedValue(handler.serialize(typedValue));
	}
	
	/**
	 * private convenience method to instantiate the handler defined by this class's attribute type
	 */
	private AttributeHandler<?> getHandler() {
		return Context.getAttributeService().getHandler(getAttributeType());
	}
	
}

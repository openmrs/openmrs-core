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
import org.openmrs.customdatatype.CustomDatatype;
import org.openmrs.customdatatype.CustomDatatypeUtil;
import org.openmrs.customdatatype.Customizable;
import org.openmrs.customdatatype.InvalidCustomValueException;
import org.openmrs.util.OpenmrsUtil;

/**
 * Abstract base implementation of {@link Attribute}. Actual implementations should be able to extend this
 * class, and have very little of their own code.  
 * @param <AT>
 * @param <OwningType>
 * @since 1.9
 */
public abstract class BaseAttribute<AT extends AttributeType, OwningType extends Customizable<?>> extends BaseOpenmrsData implements Attribute<AT, OwningType>, Comparable<Attribute> {
	
	private OwningType owner;
	
	private AT attributeType;
	
	private String persistedValue;
	
	/**
	 * @see org.openmrs.attribute.Attribute#getOwner()
	 */
	@Override
	public OwningType getOwner() {
		return owner;
	}
	
	/**
	 * @see org.openmrs.attribute.Attribute#setOwner(org.openmrs.customdatatype.Customizable)
	 */
	public void setOwner(OwningType owner) {
		this.owner = owner;
	}
	
	/**
	 * @param attributeType the attributeType to set
	 */
	public void setAttributeType(AT attributeType) {
		this.attributeType = attributeType;
	}
	
	/**
	 * @see org.openmrs.attribute.Attribute#getAttributeType()
	 */
	@Override
	public AT getAttributeType() {
		return attributeType;
	}
	
	/**
	 * @see org.openmrs.customdatatype.SingleCustomValue#getDescriptor()
	 */
	@Override
	public AT getDescriptor() {
		return getAttributeType();
	}
	
	/**
	 * @see org.openmrs.customdatatype.SingleCustomValue#getValueReference()
	 */
	@Override
	public String getValueReference() {
		return persistedValue;
	}
	
	/**
	 * @see org.openmrs.customdatatype.SingleCustomValue#setValueReference(java.lang.String)
	 */
	@Override
	public void setValueReference(String valueToPersist) throws InvalidCustomValueException {
		this.persistedValue = valueToPersist;
	}
	
	/**
	 * @see org.openmrs.attribute.Attribute#getValue()
	 */
	@Override
	public Object getValue() throws InvalidCustomValueException {
		return CustomDatatypeUtil.getDatatype(getAttributeType()).fromReferenceString(getValueReference());
	}
	
	/**
	 * @see org.openmrs.attribute.Attribute#setValue(java.lang.Object)
	 */
	@Override
	public <T> void setValue(T typedValue) throws InvalidCustomValueException {
		CustomDatatype<T> datatype = (CustomDatatype<T>) CustomDatatypeUtil.getDatatype(getAttributeType());
		datatype.validate(typedValue);
		setValueReference(datatype.toReferenceString(typedValue));
	}
	
	/**
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Attribute other) {
		if (other == null)
			return -1;
		int retValue = isVoided().compareTo(other.isVoided());
		if (retValue == 0)
			retValue = OpenmrsUtil.compareWithNullAsGreatest(getAttributeType().getId(), other.getAttributeType().getId());
		if (retValue == 0)
			retValue = OpenmrsUtil.compareWithNullAsGreatest(getValueReference(), other.getValueReference());
		if (retValue == 0)
			retValue = OpenmrsUtil.compareWithNullAsGreatest(getId(), other.getId());
		return retValue;
	}
	
}

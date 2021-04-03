/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.attribute;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import org.hibernate.search.annotations.Field;
import org.openmrs.BaseChangeableOpenmrsData;
import org.openmrs.customdatatype.CustomDatatypeUtil;
import org.openmrs.customdatatype.Customizable;
import org.openmrs.customdatatype.InvalidCustomValueException;
import org.openmrs.customdatatype.NotYetPersistedException;
import org.openmrs.util.OpenmrsUtil;

/**
 * Abstract base implementation of {@link Attribute}. Actual implementations should be able to extend this
 * class, and have very little of their own code.  
 * @param <AT>
 * @param <OwningType>
 * @since 1.9
 */
@SuppressWarnings("rawtypes")
@MappedSuperclass
public abstract class BaseAttribute<AT extends AttributeType, OwningType extends Customizable<?>> extends BaseChangeableOpenmrsData implements Attribute<AT, OwningType>, Comparable<Attribute> {
	
	private OwningType owner;
	
	private AT attributeType;
	
	// value pulled from the database
	@Field
	@Column(name = "value_reference", nullable = false, length = 65535)
	private String valueReference;
	
	// temporarily holds a typed value, either when getValue() is called the first time (causing valueReference to be converted) or when setValue has been called, but this attribute has not yet been committed to persistent storage
	private transient Object value;
	
	private transient boolean dirty = false;
	
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
	@Override
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
		if (valueReference == null) {
			throw new NotYetPersistedException();
		} else {
			return valueReference;
		}
	}
	
	/**
	 * @see org.openmrs.customdatatype.SingleCustomValue#setValueReferenceInternal(java.lang.String)
	 */
	@Override
	public void setValueReferenceInternal(String valueReference) throws InvalidCustomValueException {
		this.valueReference = valueReference;
		this.dirty = false;
	}
	
	/**
	 * @see org.openmrs.attribute.Attribute#getValue()
	 */
	@Override
	public Object getValue() throws InvalidCustomValueException {
		if (value == null) {
			value = CustomDatatypeUtil.getDatatype(getAttributeType()).fromReferenceString(getValueReference());
		}
		return value;
	}
	
	/**
	 * @see org.openmrs.attribute.Attribute#setValue(java.lang.Object)
	 */
	@Override
	public <T> void setValue(T typedValue) throws InvalidCustomValueException {
		dirty = true;
		value = typedValue;
	}
	
	/**
	 * @return the dirty
	 */
	@Override
	public boolean isDirty() {
		return dirty;
	}
	
	/**
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 * Note: this comparator imposes orderings that are inconsistent with equals.
	 */
	@SuppressWarnings("squid:S1210")
	@Override
	public int compareTo(Attribute other) {
		if (other == null) {
			return -1;
		}
		int retValue = getVoided().compareTo(other.getVoided());
		if (retValue == 0) {
			retValue = OpenmrsUtil.compareWithNullAsGreatest(getAttributeType().getId(), other.getAttributeType().getId());
		}
		if (retValue == 0) {
			retValue = OpenmrsUtil.compareWithNullAsGreatest(getValueReference(), other.getValueReference());
		}
		if (retValue == 0) {
			retValue = OpenmrsUtil.compareWithNullAsGreatest(getId(), other.getId());
		}
		return retValue;
	}
	
}

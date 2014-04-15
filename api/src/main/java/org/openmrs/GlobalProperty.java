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
package org.openmrs;

import org.openmrs.customdatatype.CustomDatatype;
import org.openmrs.customdatatype.CustomDatatypeUtil;
import org.openmrs.customdatatype.CustomValueDescriptor;
import org.openmrs.customdatatype.InvalidCustomValueException;
import org.openmrs.customdatatype.SingleCustomValue;

/**
 * Global properties are simple key-value pairs persisted in the database GPs can be thought of as
 * something similar to environment variables used in operating systems.
 */
public class GlobalProperty extends BaseOpenmrsObject implements CustomValueDescriptor, SingleCustomValue<GlobalProperty> {
	
	private String property = "";
	
	private String propertyValue = "";
	
	private transient Object typedValue;
	
	// if true, indicates that setValue has been called, and we need to invoke CustomDatatype's save
	private boolean dirty = false;
	
	private String description = "";
	
	private String datatypeClassname;
	
	private String datatypeConfig;
	
	private String preferredHandlerClassname;
	
	private String handlerConfig;
	
	/**
	 * Default empty constructor
	 */
	public GlobalProperty() {
	}
	
	/**
	 * Constructor defining the key for this GP
	 *
	 * @param property key to name the property
	 */
	public GlobalProperty(String property) {
		this.property = property;
	}
	
	/**
	 * Constructor defining the key and value of this GP
	 *
	 * @param property key to name the property
	 * @param value value to give to the property
	 */
	public GlobalProperty(String property, String value) {
		this(property);
		this.propertyValue = value;
	}
	
	/**
	 * Constructor defining key/value/description for this GP
	 *
	 * @param property key to name the property
	 * @param value value to give to the property
	 * @param description description of how this property is used
	 */
	public GlobalProperty(String property, String value, String description) {
		this(property, value);
		this.description = description;
	}
	
	/**
	 * Constructor defining key/value/description/customDatatype/datatypeConfig
	 *
	 * @param property
	 * @param value
	 * @param description
	 * @param datatypeClass
	 * @param datatypeConfig
	 *
	 * @since 1.9
	 */
	public GlobalProperty(String property, String value, String description,
	    Class<? extends CustomDatatype<?>> datatypeClass, String datatypeConfig) {
		this(property, value, description);
		this.datatypeClassname = datatypeClass.getName();
		this.datatypeConfig = datatypeConfig;
	}
	
	/**
	 * @return Returns the property.
	 */
	public String getProperty() {
		return property;
	}
	
	/**
	 * @param property The property to set.
	 */
	public void setProperty(String property) {
		this.property = property;
	}
	
	/**
	 * @return Returns the propertyValue.
	 */
	public String getPropertyValue() {
		return propertyValue;
	}
	
	/**
	 * @param propertyValue The propertyValue to set.
	 */
	public void setPropertyValue(String propertyValue) {
		this.propertyValue = propertyValue;
	}
	
	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}
	
	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	public Integer getId() {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	public void setId(Integer id) {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * @see org.openmrs.customdatatype.CustomValueDescriptor#getDatatypeClassname()
	 * @since 1.9
	 */
	@Override
	public String getDatatypeClassname() {
		return datatypeClassname;
	}
	
	/**
	 * @param datatypeClassname the datatypeClassname to set
	 * @since 1.9
	 */
	public void setDatatypeClassname(String datatypeClassname) {
		this.datatypeClassname = datatypeClassname;
	}
	
	/**
	 * @see org.openmrs.customdatatype.CustomValueDescriptor#getDatatypeConfig()
	 * @since 1.9
	 */
	@Override
	public String getDatatypeConfig() {
		return datatypeConfig;
	}
	
	/**
	 * @param datatypeConfig the datatypeConfig to set
	 * @since 1.9
	 */
	public void setDatatypeConfig(String datatypeConfig) {
		this.datatypeConfig = datatypeConfig;
	}
	
	/**
	 * @see org.openmrs.customdatatype.CustomValueDescriptor#getPreferredHandlerClassname()
	 * @since 1.9
	 */
	@Override
	public String getPreferredHandlerClassname() {
		return preferredHandlerClassname;
	}
	
	/**
	 * @param preferredHandlerClassname the preferredHandlerClassname to set
	 * @since 1.9
	 */
	public void setPreferredHandlerClassname(String preferredHandlerClassname) {
		this.preferredHandlerClassname = preferredHandlerClassname;
	}
	
	/**
	 * @see org.openmrs.customdatatype.CustomValueDescriptor#getHandlerConfig()
	 * @since 1.9
	 */
	@Override
	public String getHandlerConfig() {
		return handlerConfig;
	}
	
	/**
	 * @param handlerConfig the handlerConfig to set
	 * @since 1.9
	 */
	public void setHandlerConfig(String handlerConfig) {
		this.handlerConfig = handlerConfig;
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "property: " + getProperty() + " value: " + getPropertyValue();
	}
	
	/**
	 * @see org.openmrs.customdatatype.SingleCustomValue#getDescriptor()
	 *
	 * @since 1.9
	 */
	@Override
	public GlobalProperty getDescriptor() {
		return this;
	}
	
	/**
	 * @see org.openmrs.customdatatype.SingleCustomValue#getValueReference()
	 *
	 * @since 1.9
	 */
	@Override
	public String getValueReference() {
		return getPropertyValue();
	}
	
	/**
	 * @see org.openmrs.customdatatype.SingleCustomValue#setValueReferenceInternal(java.lang.String)
	 *
	 * @since 1.9
	 */
	@Override
	public void setValueReferenceInternal(String valueToPersist) throws InvalidCustomValueException {
		setPropertyValue(valueToPersist);
	}
	
	/**
	 * @see org.openmrs.customdatatype.SingleCustomValue#getValue()
	 *
	 * @since 1.9
	 */
	@Override
	public Object getValue() throws InvalidCustomValueException {
		if (typedValue == null) {
			typedValue = CustomDatatypeUtil.getDatatypeOrDefault(this).fromReferenceString(getValueReference());
		}
		return typedValue;
	}
	
	/**
	 * @see org.openmrs.customdatatype.SingleCustomValue#setValue(java.lang.Object)
	 *
	 * @since 1.9
	 */
	@Override
	public <T> void setValue(T typedValue) throws InvalidCustomValueException {
		this.typedValue = typedValue;
		dirty = true;
	}
	
	/**
	 * @see org.openmrs.customdatatype.SingleCustomValue#isDirty()
	 */
	@Override
	public boolean isDirty() {
		return dirty;
	}
	
}

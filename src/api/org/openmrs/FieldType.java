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


/**
 * FieldType
 */
public class FieldType extends BaseOpenmrsMetadata implements java.io.Serializable {
	
	public static final long serialVersionUID = 35467L;
	
	// Fields
	
	private Integer fieldTypeId;
	
	private Boolean isSet = false;
	
	// Constructors
	
	/** default constructor */
	public FieldType() {
	}
	
	/** constructor with id */
	public FieldType(Integer fieldTypeId) {
		this.fieldTypeId = fieldTypeId;
	}
	
	/**
	 * Compares two objects for similarity
	 * 
	 * @param obj
	 * @return boolean true/false whether or not they are the same objects
	 */
	public boolean equals(Object obj) {
		if (obj instanceof FieldType) {
			FieldType f = (FieldType) obj;
			return (fieldTypeId.equals(f.getFieldTypeId()));
		}
		return false;
	}
	
	public int hashCode() {
		if (this.getFieldTypeId() == null)
			return super.hashCode();
		return this.getFieldTypeId().hashCode();
	}
	
	// Property accessors
	
	/**
	 * @return Returns the fieldTypeId.
	 */
	public Integer getFieldTypeId() {
		return fieldTypeId;
	}
	
	/**
	 * @param fieldTypeId The fieldTypeId to set.
	 */
	public void setFieldTypeId(Integer fieldTypeId) {
		this.fieldTypeId = fieldTypeId;
	}
	
	/**
	 * @return Returns the isSet.
	 */
	public Boolean getIsSet() {
		return isSet;
	}
	
	/**
	 * @param isSet The isSet to set.
	 */
	public void setIsSet(Boolean isSet) {
		this.isSet = isSet;
	}
	
	/**
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	public Integer getId() {
		
		return getFieldTypeId();
	}
	
	/**
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	public void setId(Integer id) {
		setFieldTypeId(id);
		
	}
	
}
